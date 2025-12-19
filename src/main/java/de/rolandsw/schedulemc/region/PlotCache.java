package de.rolandsw.schedulemc.region;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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
        this.cache = Collections.synchronizedMap(new LinkedHashMap<BlockPos, CacheEntry>(
            maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<BlockPos, CacheEntry> eldest) {
                return size() > PlotCache.this.maxSize;
            }
        });

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
        CacheEntry entry = cache.get(pos);

        if (entry != null) {
            // Validierung: Prüfe ob Plot noch diese Position enthält
            if (entry.plot.contains(pos)) {
                hits.incrementAndGet();
                return entry.plot;
            } else {
                // Plot hat sich geändert, Entry ist ungültig
                cache.remove(pos);
                misses.incrementAndGet();
                return null;
            }
        }

        misses.incrementAndGet();
        return null;
    }

    /**
     * Fügt Plot für Position in Cache ein
     *
     * @param pos Position
     * @param plot Plot (oder null für explizites "kein Plot hier")
     */
    public void put(BlockPos pos, @Nullable PlotRegion plot) {
        if (plot != null) {
            cache.put(pos, new CacheEntry(plot));
        }
    }

    /**
     * Invalidiert alle Cache-Einträge für einen Plot
     *
     * @param plotId Plot-ID
     */
    public void invalidatePlot(String plotId) {
        synchronized (cache) {
            cache.entrySet().removeIf(entry -> {
                PlotRegion plot = entry.getValue().plot;
                return plot != null && plot.getPlotId().equals(plotId);
            });
        }
        invalidations.incrementAndGet();
    }

    /**
     * Invalidiert alle Cache-Einträge in einer Region
     *
     * Nützlich wenn Plot-Grenzen sich ändern
     *
     * @param min Min-Position
     * @param max Max-Position
     */
    public void invalidateRegion(BlockPos min, BlockPos max) {
        synchronized (cache) {
            cache.entrySet().removeIf(entry -> {
                BlockPos pos = entry.getKey();
                return pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
                       pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
                       pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
            });
        }
        invalidations.incrementAndGet();
    }

    /**
     * Löscht den gesamten Cache
     */
    public void clear() {
        cache.clear();
        LOGGER.debug("PlotCache geleert");
    }

    /**
     * Gibt Cache-Größe zurück
     */
    public int size() {
        return cache.size();
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
        LOGGER.debug("PlotCache-Statistiken zurückgesetzt");
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
