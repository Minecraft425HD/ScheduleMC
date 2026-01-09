package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Räumlicher Index für schnelle Plot-Lookups
 *
 * Verwendet ein Chunk-basiertes Gitter (16x16x16) um Plots zu indizieren.
 * Reduziert Lookup-Komplexität von O(n) auf O(1) in den meisten Fällen.
 */
public class PlotSpatialIndex {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHUNK_SIZE = 16;

    // Map: ChunkKey -> Set of PlotIDs in diesem Chunk
    private final Map<ChunkKey, Set<String>> chunkToPlots = new ConcurrentHashMap<>();

    // Map: PlotID -> Set of ChunkKeys die dieser Plot belegt
    private final Map<String, Set<ChunkKey>> plotToChunks = new ConcurrentHashMap<>();

    /**
     * Fügt einen Plot zum Index hinzu
     */
    public void addPlot(PlotRegion plot) {
        String plotId = plot.getPlotId();
        Set<ChunkKey> chunks = getChunksForPlot(plot);

        // Entferne alte Einträge falls Plot bereits existiert
        removePlot(plotId);

        // Füge zu allen betroffenen Chunks hinzu
        for (ChunkKey chunk : chunks) {
            chunkToPlots.computeIfAbsent(chunk, k -> ConcurrentHashMap.newKeySet())
                        .add(plotId);
        }

        plotToChunks.put(plotId, chunks);

        LOGGER.debug("Plot {} added to index ({} chunks)", plotId, chunks.size());
    }

    /**
     * Entfernt einen Plot aus dem Index
     */
    public void removePlot(String plotId) {
        Set<ChunkKey> chunks = plotToChunks.remove(plotId);
        if (chunks != null) {
            for (ChunkKey chunk : chunks) {
                Set<String> plots = chunkToPlots.get(chunk);
                if (plots != null) {
                    plots.remove(plotId);
                    if (plots.isEmpty()) {
                        chunkToPlots.remove(chunk);
                    }
                }
            }
            LOGGER.debug("Plot {} aus Index entfernt", plotId);
        }
    }

    /**
     * Findet alle Plot-IDs die eine Position enthalten könnten
     *
     * @param pos Die Position
     * @return Set von Plot-IDs die überprüft werden müssen (unmodifiable view)
     */
    public Set<String> getPlotsNear(BlockPos pos) {
        ChunkKey chunk = getChunkKey(pos);
        Set<String> plots = chunkToPlots.get(chunk);

        if (plots == null || plots.isEmpty()) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(plots);
    }

    /**
     * Löscht den gesamten Index
     */
    public void clear() {
        chunkToPlots.clear();
        plotToChunks.clear();
        LOGGER.info("Spatial Index geleert");
    }

    /**
     * Erstellt den Index neu aus allen Plots
     */
    public void rebuild(Collection<PlotRegion> plots) {
        clear();
        for (PlotRegion plot : plots) {
            addPlot(plot);
        }
        LOGGER.info("Spatial Index neu erstellt: {} Plots, {} Chunks",
                   plotToChunks.size(), chunkToPlots.size());
    }

    /**
     * Berechnet alle Chunk-Keys die ein Plot überlappt
     */
    private Set<ChunkKey> getChunksForPlot(PlotRegion plot) {
        Set<ChunkKey> chunks = new HashSet<>();

        BlockPos min = plot.getMin();
        BlockPos max = plot.getMax();

        int minChunkX = toChunkCoord(min.getX());
        int minChunkY = toChunkCoord(min.getY());
        int minChunkZ = toChunkCoord(min.getZ());

        int maxChunkX = toChunkCoord(max.getX());
        int maxChunkY = toChunkCoord(max.getY());
        int maxChunkZ = toChunkCoord(max.getZ());

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cy = minChunkY; cy <= maxChunkY; cy++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    chunks.add(new ChunkKey(cx, cy, cz));
                }
            }
        }

        return chunks;
    }

    /**
     * Berechnet ChunkKey für eine Position
     */
    private ChunkKey getChunkKey(BlockPos pos) {
        return new ChunkKey(
            toChunkCoord(pos.getX()),
            toChunkCoord(pos.getY()),
            toChunkCoord(pos.getZ())
        );
    }

    /**
     * Konvertiert Block-Koordinate zu Chunk-Koordinate
     */
    private int toChunkCoord(int blockCoord) {
        return Math.floorDiv(blockCoord, CHUNK_SIZE);
    }

    /**
     * Gibt Debug-Statistiken zurück
     */
    public String getStats() {
        return String.format("Index: %d Plots, %d Chunks, Avg %.2f Chunks/Plot",
            plotToChunks.size(),
            chunkToPlots.size(),
            plotToChunks.isEmpty() ? 0.0 : (double) chunkToPlots.size() / plotToChunks.size()
        );
    }

    /**
     * Chunk-Koordinaten Key (immutable, thread-safe)
     */
    private static class ChunkKey {
        private final int x, y, z;
        private final int hash;

        ChunkKey(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.hash = Objects.hash(x, y, z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkKey)) return false;
            ChunkKey that = (ChunkKey) o;
            return x == that.x && y == that.y && z == that.z;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return String.format("Chunk[%d,%d,%d]", x, y, z);
        }
    }
}
