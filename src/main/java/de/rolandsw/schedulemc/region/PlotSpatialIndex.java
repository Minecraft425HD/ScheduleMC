package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Objects;
import java.util.Collections;
import java.util.Objects;
import java.util.HashSet;
import java.util.Objects;
import java.util.Collections;
import java.util.Objects;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.Objects;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;
import java.util.Collections;
import java.util.Objects;

/**
 * Hochperformanter räumlicher Index für schnelle Plot-Lookups in ScheduleMC.
 *
 * <p>Dieser Index implementiert ein Chunk-basiertes 3D-Gitter-System zur Beschleunigung
 * von räumlichen Abfragen. Durch Aufteilung der Welt in 16×16×16 Chunk-Bereiche und
 * Indizierung von Plots nach überlappenden Chunks wird die Lookup-Komplexität drastisch
 * reduziert.</p>
 *
 * <h2>Performance-Vorteile:</h2>
 * <ul>
 *   <li><b>Lookup-Komplexität:</b> O(n) → O(1) in den meisten Fällen</li>
 *   <li><b>Speicher-Effizienz:</b> Nur belegte Chunks werden indexiert</li>
 *   <li><b>Thread-Safety:</b> Vollständig thread-sicher durch ConcurrentHashMap</li>
 *   <li><b>Automatische Aufräumung:</b> Leere Chunks werden automatisch entfernt</li>
 * </ul>
 *
 * <h2>Funktionsweise:</h2>
 * <p>Der Index arbeitet mit zwei bidirektionalen Maps:</p>
 * <ul>
 *   <li><b>ChunkKey → PlotIDs:</b> Welche Plots überlappen einen Chunk?</li>
 *   <li><b>PlotID → ChunkKeys:</b> Welche Chunks überlappt ein Plot?</li>
 * </ul>
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * PlotSpatialIndex index = new PlotSpatialIndex();
 *
 * // 1. Füge Plots hinzu
 * PlotRegion plot1 = new PlotRegion(...);
 * index.addPlot(plot1);
 *
 * // 2. Schnelle Position-Abfrage
 * BlockPos playerPos = new BlockPos(100, 64, 200);
 * Set<String> candidates = index.getPlotsNear(playerPos); // O(1) Lookup!
 *
 * // 3. Exakte Prüfung nur für Kandidaten (statt aller Plots)
 * for (String plotId : candidates) {
 *     PlotRegion plot = plotManager.getPlot(plotId);
 *     if (plot.contains(playerPos)) {
 *         // Spieler ist in diesem Plot
 *     }
 * }
 * }</pre>
 *
 * <h2>Performance-Beispiel:</h2>
 * <table border="1">
 *   <tr><th>Szenario</th><th>Ohne Index</th><th>Mit Index</th><th>Speedup</th></tr>
 *   <tr><td>1.000 Plots</td><td>1.000 Checks</td><td>~1-5 Checks</td><td>200-1000x</td></tr>
 *   <tr><td>10.000 Plots</td><td>10.000 Checks</td><td>~1-5 Checks</td><td>2000-10000x</td></tr>
 * </table>
 *
 * <h2>Thread-Safety:</h2>
 * <p>Vollständig thread-sicher durch ConcurrentHashMap für beide Index-Maps und
 * ConcurrentHashMap.newKeySet() für Plot-Sets pro Chunk.</p>
 *
 * @author ScheduleMC Team
 * @version 1.0
 * @since 1.0.0
 * @see PlotRegion
 * @see RegionManager
 */
public class PlotSpatialIndex {

    /**
     * Logger für Debug-Ausgaben und Index-Operationen.
     */
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Größe eines Chunks in Blöcken (16×16×16).
     * <p>Entspricht Minecraft-Chunk-Größe für konsistente Aufteilung.</p>
     */
    private static final int CHUNK_SIZE = 16;

    /**
     * Index-Map: ChunkKey → Set von PlotIDs in diesem Chunk.
     * <p>Ermöglicht schnelles Finden aller Plots die einen Chunk überlappen.
     * Thread-sicher durch ConcurrentHashMap.</p>
     */
    private final Map<ChunkKey, Set<String>> chunkToPlots = new ConcurrentHashMap<>();

    /**
     * Reverse-Index-Map: PlotID → Set von ChunkKeys die dieser Plot belegt.
     * <p>Ermöglicht effizientes Entfernen von Plots aus allen betroffenen Chunks.
     * Thread-sicher durch ConcurrentHashMap.</p>
     */
    private final Map<String, Set<ChunkKey>> plotToChunks = new ConcurrentHashMap<>();

    /**
     * Fügt einen Plot zum räumlichen Index hinzu.
     *
     * <p>Berechnet alle Chunks die der Plot überlappt und fügt ihn zu allen
     * betroffenen Chunks hinzu. Falls Plot bereits existiert, werden alte
     * Einträge automatisch entfernt (idempotent).</p>
     *
     * @param plot PlotRegion die indexiert werden soll (non-null)
     * @see #getChunksForPlot(PlotRegion)
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

        LOGGER.debug("Plot {} zu Index hinzugefügt ({} Chunks)", plotId, chunks.size());
    }

    /**
     * Entfernt einen Plot vollständig aus dem räumlichen Index.
     *
     * <p>Entfernt den Plot aus allen Chunks die er belegt. Leere Chunks werden
     * automatisch aus dem Index entfernt zur Speicher-Optimierung.</p>
     *
     * @param plotId ID des zu entfernenden Plots
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
     * Findet alle Plot-IDs die eine Position enthalten könnten (Kandidaten).
     *
     * <p>Gibt alle Plots zurück die den Chunk überlappen in dem sich die Position
     * befindet. Dies ist eine O(1)-Operation und reduziert drastisch die Anzahl
     * der zu prüfenden Plots.</p>
     *
     * <h3>Wichtig:</h3>
     * <p>Rückgabewert enthält nur <b>Kandidaten</b> - exakte Positionsprüfung
     * mit {@link PlotRegion#contains(BlockPos)} ist noch erforderlich!</p>
     *
     * @param pos Die zu prüfende BlockPos (non-null)
     * @return Unveränderliches Set von Plot-IDs die geprüft werden müssen (leer wenn keine)
     * @see PlotRegion#contains(BlockPos)
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
     * Löscht den gesamten räumlichen Index.
     *
     * <p>Entfernt alle Index-Einträge. Nützlich vor kompletten Neuaufbau.</p>
     *
     * @see #rebuild(Collection)
     */
    public void clear() {
        chunkToPlots.clear();
        plotToChunks.clear();
        LOGGER.info("Spatial Index geleert");
    }

    /**
     * Erstellt den räumlichen Index komplett neu aus allen Plots.
     *
     * <p>Löscht bestehenden Index und indexiert alle übergebenen Plots.
     * Sollte aufgerufen werden nach Laden aller Plots aus Persistenz.</p>
     *
     * @param plots Collection aller Plots die indexiert werden sollen
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
     * Berechnet alle Chunk-Keys die ein Plot überlappt.
     *
     * <p>Iteriert durch alle Chunk-Koordinaten zwischen min/max des Plots
     * und erstellt ChunkKeys für jeden überlappenden Chunk.</p>
     *
     * @param plot PlotRegion für die Chunks berechnet werden sollen
     * @return Set aller überlappenden ChunkKeys
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
     * Berechnet ChunkKey für eine gegebene BlockPos.
     *
     * <p>Konvertiert Block-Koordinaten zu Chunk-Koordinaten.</p>
     *
     * @param pos BlockPos für die der ChunkKey berechnet werden soll
     * @return ChunkKey der den Chunk identifiziert
     */
    private ChunkKey getChunkKey(BlockPos pos) {
        return new ChunkKey(
            toChunkCoord(pos.getX()),
            toChunkCoord(pos.getY()),
            toChunkCoord(pos.getZ())
        );
    }

    /**
     * Konvertiert Block-Koordinate zu Chunk-Koordinate.
     *
     * <p>Verwendet Math.floorDiv für korrekte Behandlung negativer Koordinaten.</p>
     *
     * <h3>Beispiele:</h3>
     * <ul>
     *   <li>0-15 → Chunk 0</li>
     *   <li>16-31 → Chunk 1</li>
     *   <li>-1 bis -16 → Chunk -1</li>
     * </ul>
     *
     * @param blockCoord Block-Koordinate (X, Y oder Z)
     * @return Chunk-Koordinate
     */
    private int toChunkCoord(int blockCoord) {
        return Math.floorDiv(blockCoord, CHUNK_SIZE);
    }

    /**
     * Gibt formatierte Debug-Statistiken über den Index zurück.
     *
     * <p>Enthält Anzahl indexierter Plots, belegter Chunks und durchschnittliche
     * Chunk-Anzahl pro Plot.</p>
     *
     * @return Formatierter Statistik-String
     */
    public String getStats() {
        return String.format("Index: %d Plots, %d Chunks, Avg %.2f Chunks/Plot",
            plotToChunks.size(),
            chunkToPlots.size(),
            plotToChunks.isEmpty() ? 0.0 : (double) chunkToPlots.size() / plotToChunks.size()
        );
    }

    /**
     * Immutable 3D-Chunk-Koordinaten-Key für räumliche Indexierung.
     *
     * <p>Repräsentiert einen Chunk durch (x, y, z) Koordinaten. Implementiert
     * {@code equals()} und {@code hashCode()} für Verwendung als Map-Key.
     * Hash-Code wird beim Erstellen berechnet und gecacht für Performance.</p>
     *
     * <h3>Thread-Safety:</h3>
     * <p>Vollständig immutable und thread-safe da alle Felder final sind.</p>
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
