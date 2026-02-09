package de.rolandsw.schedulemc.region;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LRU-Cache für Plot-Lookups
 *
 * Features:
 * - Thread-safe LRU-Eviction
 * - Konfigurierbares Limit
 * - Hit/Miss-Statistiken
 * - Cache-Invalidierung
 *
 * Performance:
 * - O(1) Lookup
 * - O(1) Insert
 * - Automatische Eviction bei Limit
 */
public class PlotCache {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_SIZE = 1000;

    private final Map<BlockPos, CacheEntry> cache;
    private final int maxSize;

    // ReadWriteLock: concurrent reads, exclusive writes (statt synchronized)
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Sekundärer Chunk-Index für schnelle Region-Invalidierung O(chunks) statt O(n)
    private final Map<ChunkPos, Set<BlockPos>> chunkIndex = new ConcurrentHashMap<>();

    // Statistiken
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong invalidations = new AtomicLong(0);

    /**
     * Cache-Entry mit Timestamp für Debugging
     */
    private static class CacheEntry {
        final PlotRegion plot;
        final long insertTime;

        CacheEntry(PlotRegion plot) {
            this.plot = plot;
            this.insertTime = System.currentTimeMillis();
        }
    }

    /**
     * Erstellt Cache mit Standard-Größe (1000 Einträge)
     */
    public PlotCache() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * Erstellt Cache mit custom Größe
     *
     * @param maxSize Maximale Anzahl gecachter Positionen
     */
    public PlotCache(int maxSize) {
        this.maxSize = maxSize;

        // LinkedHashMap mit LRU-Eviction (access-order = true)
        // Nicht mehr Collections.synchronizedMap - stattdessen ReadWriteLock
        this.cache = new LinkedHashMap<BlockPos, CacheEntry>(
            maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<BlockPos, CacheEntry> eldest) {
                return size() > PlotCache.this.maxSize;
            }
        };

        LOGGER.debug("PlotCache initialisiert mit maxSize={}", maxSize);
    }

    /**
     * Sucht Plot im Cache
     *
     * @param pos Position
     * @return Plot oder null wenn nicht im Cache
     */
    @Nullable
    public PlotRegion get(BlockPos pos) {
        // ReadWriteLock: get() nutzt writeLock weil LinkedHashMap(access-order=true)
        // bei get() die interne Reihenfolge ändert (LRU-Reorder)
        rwLock.writeLock().lock();
        try {
            CacheEntry entry = cache.get(pos);

            if (entry != null) {
                if (entry.plot.contains(pos)) {
                    hits.incrementAndGet();
                    return entry.plot;
                } else {
                    cache.remove(pos);
                    removeFromChunkIndex(pos);
                    misses.incrementAndGet();
                    return null;
                }
            }

            misses.incrementAndGet();
            return null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Hilfsmethode: Entfernt eine Position aus dem Chunk-Index
     */
    private void removeFromChunkIndex(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> positionsInChunk = chunkIndex.get(chunkPos);
        if (positionsInChunk != null) {
            positionsInChunk.remove(pos);
            if (positionsInChunk.isEmpty()) {
                chunkIndex.remove(chunkPos);
            }
        }
    }

    /**
     * Fügt Plot für Position in Cache ein
     *
     * @param pos Position
     * @param plot Plot (oder null für explizites "kein Plot hier")
     */
    public void put(BlockPos pos, @Nullable PlotRegion plot) {
        if (plot != null) {
            rwLock.writeLock().lock();
            try {
                cache.put(pos, new CacheEntry(plot));
            } finally {
                rwLock.writeLock().unlock();
            }
            ChunkPos chunkPos = new ChunkPos(pos);
            chunkIndex.computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet()).add(pos);
        }
    }

    /**
     * Hilfsmethode: Berechnet ChunkPos aus BlockPos
     */
    private static ChunkPos getChunkPos(BlockPos pos) {
        return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * Invalidiert alle Cache-Einträge für einen Plot
     *
     * OPTIMIERT: Entfernt auch aus Chunk-Index
     *
     * @param plotId Plot-ID
     */
    public void invalidatePlot(String plotId) {
        Set<BlockPos> toRemoveFromChunkIndex = new HashSet<>();

        rwLock.writeLock().lock();
        try {
            cache.entrySet().removeIf(entry -> {
                PlotRegion plot = entry.getValue().plot;
                boolean shouldRemove = plot != null && plot.getPlotId().equals(plotId);
                if (shouldRemove) {
                    toRemoveFromChunkIndex.add(entry.getKey());
                }
                return shouldRemove;
            });
        } finally {
            rwLock.writeLock().unlock();
        }

        // Chunk-Index bereinigen
        for (BlockPos pos : toRemoveFromChunkIndex) {
            ChunkPos chunkPos = new ChunkPos(pos);
            Set<BlockPos> positionsInChunk = chunkIndex.get(chunkPos);
            if (positionsInChunk != null) {
                positionsInChunk.remove(pos);
                if (positionsInChunk.isEmpty()) {
                    chunkIndex.remove(chunkPos);
                }
            }
        }

        invalidations.incrementAndGet();
    }

    /**
     * Invalidiert alle Cache-Einträge in einer Region
     *
     * OPTIMIERT: Nutzt Chunk-Index für O(affected_chunks) statt O(cache_size)
     * Bei typischen Operationen (Plot-Änderung) betrifft das 1-4 Chunks statt 1000 Einträge
     *
     * @param min Min-Position
     * @param max Max-Position
     */
    public void invalidateRegion(BlockPos min, BlockPos max) {
        // Berechne betroffene Chunks (nur die Chunks die tatsächlich in der Region liegen)
        int minChunkX = min.getX() >> 4;
        int maxChunkX = max.getX() >> 4;
        int minChunkZ = min.getZ() >> 4;
        int maxChunkZ = max.getZ() >> 4;

        int removedCount = 0;

        // Iteriere nur über betroffene Chunks (typisch 1-4 statt 1000 Einträge)
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                Set<BlockPos> positionsInChunk = chunkIndex.get(chunkPos);

                if (positionsInChunk != null) {
                    // Kopiere zum sicheren Iterieren
                    Set<BlockPos> toRemove = new HashSet<>();

                    for (BlockPos pos : positionsInChunk) {
                        // Prüfe ob Position tatsächlich in der Region liegt
                        if (pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
                            pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
                            pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ()) {
                            toRemove.add(pos);
                        }
                    }

                    // Entferne aus beiden Indizes
                    for (BlockPos pos : toRemove) {
                        cache.remove(pos);
                        positionsInChunk.remove(pos);
                        removedCount++;
                    }

                    // Chunk-Set aufräumen wenn leer
                    if (positionsInChunk.isEmpty()) {
                        chunkIndex.remove(chunkPos);
                    }
                }
            }
        }

        if (removedCount > 0) {
            invalidations.incrementAndGet();
            LOGGER.debug("PlotCache: {} entries in region invalidated (Chunks: {}x{})",
                removedCount, (maxChunkX - minChunkX + 1), (maxChunkZ - minChunkZ + 1));
        }
    }

    /**
     * Löscht den gesamten Cache
     */
    public void clear() {
        rwLock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
        chunkIndex.clear();
        LOGGER.debug("PlotCache geleert");
    }

    /**
     * Gibt Cache-Größe zurück
     */
    public int size() {
        rwLock.readLock().lock();
        try {
            return cache.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Gibt maximale Cache-Größe zurück
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Gibt Anzahl Cache-Hits zurück
     */
    public long getHits() {
        return hits.get();
    }

    /**
     * Gibt Anzahl Cache-Misses zurück
     */
    public long getMisses() {
        return misses.get();
    }

    /**
     * Gibt Anzahl Invalidierungen zurück
     */
    public long getInvalidations() {
        return invalidations.get();
    }

    /**
     * Berechnet Hit-Rate in Prozent
     */
    public double getHitRate() {
        long totalRequests = hits.get() + misses.get();
        if (totalRequests == 0) return 0.0;
        return (hits.get() * 100.0) / totalRequests;
    }

    /**
     * Gibt detaillierte Statistiken zurück
     */
    public CacheStatistics getStatistics() {
        return new CacheStatistics(
            size(),
            maxSize,
            hits.get(),
            misses.get(),
            invalidations.get(),
            getHitRate()
        );
    }

    /**
     * Setzt Statistiken zurück (nicht Cache-Inhalt!)
     */
    public void resetStatistics() {
        hits.set(0);
        misses.set(0);
        invalidations.set(0);
        LOGGER.debug("PlotCache statistics reset");
    }

    /**
     * Cache-Statistik-Datenklasse
     */
    public static class CacheStatistics {
        public final int currentSize;
        public final int maxSize;
        public final long hits;
        public final long misses;
        public final long invalidations;
        public final double hitRate;

        public CacheStatistics(int currentSize, int maxSize, long hits, long misses,
                             long invalidations, double hitRate) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.hits = hits;
            this.misses = misses;
            this.invalidations = invalidations;
            this.hitRate = hitRate;
        }

        @Override
        public String toString() {
            return String.format(
                "PlotCache[size=%d/%d, hits=%d, misses=%d, hitRate=%.1f%%, invalidations=%d]",
                currentSize, maxSize, hits, misses, hitRate, invalidations
            );
        }

        /**
         * Gibt formatierten String für In-Game-Anzeige zurück
         */
        public String toDisplayString() {
            return String.format(
                "§7Cache: §f%d/%d §7Einträge | §aHit-Rate: §f%.1f%% §7| §eHits: §f%d §7| §cMisses: §f%d",
                currentSize, maxSize, hitRate, hits, misses
            );
        }
    }
}
