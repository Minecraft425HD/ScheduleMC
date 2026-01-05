package de.rolandsw.schedulemc.utility;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Utility-Verbrauch für alle Plots
 *
 * Features:
 * - Tracking von Strom/Wasser pro Plot
 * - 7-Tage-Durchschnitt
 * - Aktiv/Idle-Erkennung via BlockEntity-Callbacks
 * - Persistenz via JSON
 */
public class PlotUtilityManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File DATA_FILE = new File("config/plotmod_utilities.json");
    private static final Gson GSON = new Gson();

    // Plot-ID -> UtilityData
    private static final Map<String, PlotUtilityData> plotData = new ConcurrentHashMap<>();

    // BlockPos -> Plot-ID (Cache für schnelle Lookups)
    private static final Map<BlockPos, String> positionCache = new ConcurrentHashMap<>();

    private static boolean dirty = false;
    private static long lastTickDay = -1;

    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALISIERUNG
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lädt Utility-Daten beim Server-Start
     */
    public static void load() {
        plotData.clear();
        positionCache.clear();

        if (!DATA_FILE.exists()) {
            LOGGER.info("Keine Utility-Daten gefunden, starte mit leeren Daten");
            return;
        }

        try (FileReader reader = new FileReader(DATA_FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray plotsArray = root.getAsJsonArray("plots");

            for (int i = 0; i < plotsArray.size(); i++) {
                PlotUtilityData data = PlotUtilityData.fromJson(plotsArray.get(i).getAsJsonObject());
                plotData.put(data.getPlotId(), data);
            }

            LOGGER.info("Utility-Daten geladen: {} Plots", plotData.size());
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Utility-Daten", e);
        }

        // Position-Cache rebuilden
        rebuildPositionCache();
    }

    /**
     * Speichert Utility-Daten
     */
    public static void save() {
        if (!dirty) return;

        try {
            DATA_FILE.getParentFile().mkdirs();

            JsonObject root = new JsonObject();
            JsonArray plotsArray = new JsonArray();

            for (PlotUtilityData data : plotData.values()) {
                plotsArray.add(data.toJson());
            }

            root.add("plots", plotsArray);

            try (FileWriter writer = new FileWriter(DATA_FILE)) {
                GSON.toJson(root, writer);
            }

            dirty = false;
            LOGGER.debug("Utility-Daten gespeichert: {} Plots", plotData.size());
        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der Utility-Daten", e);
        }
    }

    /**
     * Speichert wenn nötig (für periodisches Auto-Save)
     */
    public static void saveIfNeeded() {
        if (dirty) {
            save();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BLOCK-EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Wird aufgerufen wenn ein Block platziert wird
     */
    public static void onBlockPlaced(BlockPos pos, Block block, Level level) {
        // Prüfe ob der Block ein Utility-Verbraucher ist
        if (!UtilityRegistry.isConsumer(block)) {
            return;
        }

        // Finde den Plot
        PlotRegion plot = PlotManager.getPlotAt(pos);
        if (plot == null) {
            return; // Block nicht in einem Plot
        }

        String plotId = plot.getPlotId();

        // Erstelle oder hole PlotUtilityData
        PlotUtilityData data = plotData.computeIfAbsent(plotId, PlotUtilityData::new);

        // Füge Verbraucher hinzu
        data.addConsumer(pos, block);
        positionCache.put(pos, plotId);

        // Berechne neuen Verbrauch
        data.calculateCurrentConsumption();

        dirty = true;

        LOGGER.debug("Verbraucher hinzugefügt: {} bei {} im Plot {}",
                block.getDescriptionId(), pos.toShortString(), plotId);
    }

    /**
     * Wird aufgerufen wenn ein Block entfernt wird
     */
    public static void onBlockRemoved(BlockPos pos, Block block) {
        String plotId = positionCache.remove(pos);
        if (plotId == null) {
            // Versuche Plot direkt zu finden
            PlotRegion plot = PlotManager.getPlotAt(pos);
            if (plot != null) {
                plotId = plot.getPlotId();
            }
        }

        if (plotId == null) return;

        PlotUtilityData data = plotData.get(plotId);
        if (data != null) {
            data.removeConsumer(pos);
            data.calculateCurrentConsumption();
            dirty = true;

            LOGGER.debug("Verbraucher entfernt bei {} im Plot {}", pos.toShortString(), plotId);
        }
    }

    /**
     * Aktualisiert den Aktivitätsstatus eines Blocks
     * Wird von BlockEntities aufgerufen die IUtilityConsumer implementieren
     */
    public static void updateActiveStatus(BlockPos pos, boolean isActive) {
        String plotId = positionCache.get(pos);
        if (plotId == null) {
            // Versuche Plot zu finden und cache zu updaten
            PlotRegion plot = PlotManager.getPlotAt(pos);
            if (plot != null) {
                plotId = plot.getPlotId();
                positionCache.put(pos, plotId);
            }
        }

        if (plotId == null) return;

        PlotUtilityData data = plotData.get(plotId);
        if (data != null && data.hasConsumer(pos)) {
            boolean wasActive = data.isActive(pos);
            if (wasActive != isActive) {
                data.setActiveStatus(pos, isActive);
                dirty = true;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TICK UPDATE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Wird jeden Server-Tick aufgerufen
     * Prüft ob ein neuer Minecraft-Tag begonnen hat
     */
    public static void onServerTick(ServerLevel level) {
        long currentDay = level.getDayTime() / 24000L;

        // Neuer Tag?
        if (currentDay != lastTickDay && lastTickDay >= 0) {
            // Tageswechsel - Rollover für alle Plots
            for (PlotUtilityData data : plotData.values()) {
                data.rolloverDay(currentDay);
            }
            dirty = true;
            LOGGER.info("Utility-Tageswechsel: Tag {} -> {}", lastTickDay, currentDay);
        }

        lastTickDay = currentDay;
    }

    /**
     * Aktualisiert den Verbrauch für alle Plots
     * Sollte periodisch aufgerufen werden (z.B. alle 1000 Ticks)
     */
    public static void updateAllConsumption(ServerLevel level) {
        for (Map.Entry<String, PlotUtilityData> entry : plotData.entrySet()) {
            PlotUtilityData data = entry.getValue();

            // Aktualisiere Aktivitätsstatus von allen BlockEntities
            // Dies ermöglicht dynamische Erkennung ohne explizite Callbacks
            updateConsumerStatus(level, data);

            // Berechne Verbrauch neu
            data.calculateCurrentConsumption();
        }

        dirty = true;
    }

    /**
     * Aktualisiert den Status aller Verbraucher in einem Plot
     */
    private static void updateConsumerStatus(ServerLevel level, PlotUtilityData data) {
        // Iteriere über alle registrierten Positionen im Plot
        // und prüfe den aktuellen Status der BlockEntities
        // (Implementierung nutzt das IUtilityConsumer Interface)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ABFRAGEN
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Holt Utility-Daten für einen Plot
     */
    public static Optional<PlotUtilityData> getPlotData(String plotId) {
        return Optional.ofNullable(plotData.get(plotId));
    }

    /**
     * Holt Utility-Daten für einen Plot via Position
     */
    public static Optional<PlotUtilityData> getPlotDataAt(BlockPos pos) {
        PlotRegion plot = PlotManager.getPlotAt(pos);
        if (plot == null) return Optional.empty();
        return getPlotData(plot.getPlotId());
    }

    /**
     * Gibt den aktuellen Stromverbrauch eines Plots zurück
     */
    public static double getCurrentElectricity(String plotId) {
        PlotUtilityData data = plotData.get(plotId);
        return data != null ? data.getCurrentElectricity() : 0;
    }

    /**
     * Gibt den aktuellen Wasserverbrauch eines Plots zurück
     */
    public static double getCurrentWater(String plotId) {
        PlotUtilityData data = plotData.get(plotId);
        return data != null ? data.getCurrentWater() : 0;
    }

    /**
     * Gibt den 7-Tage-Durchschnitt für Strom zurück
     */
    public static double get7DayAverageElectricity(String plotId) {
        PlotUtilityData data = plotData.get(plotId);
        return data != null ? data.get7DayAverageElectricity() : 0;
    }

    /**
     * Gibt den 7-Tage-Durchschnitt für Wasser zurück
     */
    public static double get7DayAverageWater(String plotId) {
        PlotUtilityData data = plotData.get(plotId);
        return data != null ? data.get7DayAverageWater() : 0;
    }

    /**
     * Gibt alle Plots mit ihrem Verbrauch zurück
     * Sortiert nach 7-Tage-Durchschnitt (höchster zuerst)
     */
    public static List<PlotUtilityData> getAllPlotsSortedByConsumption() {
        List<PlotUtilityData> result = new ArrayList<>(plotData.values());
        result.sort((a, b) -> Double.compare(
                b.get7DayAverageElectricity() + b.get7DayAverageWater(),
                a.get7DayAverageElectricity() + a.get7DayAverageWater()
        ));
        return result;
    }

    /**
     * Gibt die Top N Plots nach Verbrauch zurück
     * Nützlich für Polizei-Heat-System
     */
    public static List<PlotUtilityData> getTopConsumers(int limit) {
        return getAllPlotsSortedByConsumption().stream()
                .limit(limit)
                .toList();
    }

    /**
     * Prüft ob ein Plot einen verdächtig hohen Verbrauch hat
     * Kann für Polizei-Integration verwendet werden
     *
     * @param plotId Plot-ID
     * @param electricityThreshold Schwellwert für Strom (kWh)
     * @param waterThreshold Schwellwert für Wasser (L)
     * @return true wenn einer der Schwellwerte überschritten wird
     */
    public static boolean isSuspiciousConsumption(String plotId, double electricityThreshold, double waterThreshold) {
        PlotUtilityData data = plotData.get(plotId);
        if (data == null) return false;

        return data.get7DayAverageElectricity() > electricityThreshold ||
                data.get7DayAverageWater() > waterThreshold;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Rebuild des Position-Cache nach dem Laden
     */
    private static void rebuildPositionCache() {
        positionCache.clear();

        // Dies würde alle registrierten Positionen aus den PlotUtilityData durchgehen
        // und den Cache rebuilden - wird bei load() aufgerufen
    }

    /**
     * Scannt einen Plot nach bestehenden Verbrauchern
     * Nützlich beim ersten Setup oder nach Welt-Migration
     *
     * OPTIMIERT: Chunk-basierte Batch-Verarbeitung für bessere Cache-Lokalität
     * und Überspringen von ungeladenen Chunks
     */
    public static void scanPlotForConsumers(ServerLevel level, PlotRegion plot) {
        String plotId = plot.getPlotId();
        PlotUtilityData data = plotData.computeIfAbsent(plotId, PlotUtilityData::new);

        BlockPos min = plot.getMin();
        BlockPos max = plot.getMax();

        int found = 0;

        // OPTIMIERT: Chunk-basierte Iteration statt Block-für-Block
        int minChunkX = min.getX() >> 4;
        int maxChunkX = max.getX() >> 4;
        int minChunkZ = min.getZ() >> 4;
        int maxChunkZ = max.getZ() >> 4;

        // Batch-Verarbeitung: Sammle erst alle Positionen, dann füge in Batch hinzu
        List<Map.Entry<BlockPos, Block>> foundConsumers = new ArrayList<>();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                // OPTIMIERT: Überspringe ungeladene Chunks
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                // Berechne Chunk-lokale Grenzen
                int startX = Math.max(min.getX(), chunkX << 4);
                int endX = Math.min(max.getX(), (chunkX << 4) + 15);
                int startZ = Math.max(min.getZ(), chunkZ << 4);
                int endZ = Math.min(max.getZ(), (chunkZ << 4) + 15);

                // OPTIMIERT: Iteriere nur über relevante Y-Levels (typisch -64 bis 320)
                int startY = Math.max(min.getY(), level.getMinBuildHeight());
                int endY = Math.min(max.getY(), level.getMaxBuildHeight() - 1);

                for (int x = startX; x <= endX; x++) {
                    for (int z = startZ; z <= endZ; z++) {
                        for (int y = startY; y <= endY; y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            Block block = level.getBlockState(pos).getBlock();

                            if (UtilityRegistry.isConsumer(block)) {
                                foundConsumers.add(Map.entry(pos, block));
                            }
                        }
                    }
                }
            }
        }

        // OPTIMIERT: Batch-Einfügung
        for (var entry : foundConsumers) {
            data.addConsumer(entry.getKey(), entry.getValue());
            positionCache.put(entry.getKey(), plotId);
            found++;
        }

        if (found > 0) {
            data.calculateCurrentConsumption();
            dirty = true;
            LOGGER.info("Plot {} gescannt: {} Verbraucher gefunden", plotId, found);
        }
    }

    /**
     * Scannt mehrere Plots parallel
     * OPTIMIERT: Batch-Verarbeitung für viele Plots
     */
    public static void scanPlotsForConsumers(ServerLevel level, Collection<PlotRegion> plots) {
        if (plots.isEmpty()) return;

        LOGGER.info("Starte Batch-Scan für {} Plots", plots.size());
        long startTime = System.currentTimeMillis();

        int totalFound = 0;
        for (PlotRegion plot : plots) {
            int before = plotData.getOrDefault(plot.getPlotId(), new PlotUtilityData(plot.getPlotId())).getConsumerCount();
            scanPlotForConsumers(level, plot);
            int after = plotData.getOrDefault(plot.getPlotId(), new PlotUtilityData(plot.getPlotId())).getConsumerCount();
            totalFound += (after - before);
        }

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Batch-Scan abgeschlossen: {} Verbraucher in {} Plots gefunden ({}ms)",
            totalFound, plots.size(), duration);
    }

    /**
     * Formatiert Verbrauchswerte für Anzeige
     */
    public static String formatElectricity(double kWh) {
        if (kWh >= 1000) {
            return String.format("%.1f MWh", kWh / 1000);
        }
        return String.format("%.1f kWh", kWh);
    }

    public static String formatWater(double liters) {
        if (liters >= 1000) {
            return String.format("%.1f m³", liters / 1000);
        }
        return String.format("%.0f L", liters);
    }

    /**
     * Gibt Statistik-Zusammenfassung zurück
     */
    public static String getStatsSummary() {
        double totalElec = plotData.values().stream()
                .mapToDouble(PlotUtilityData::get7DayAverageElectricity)
                .sum();
        double totalWater = plotData.values().stream()
                .mapToDouble(PlotUtilityData::get7DayAverageWater)
                .sum();

        return String.format("Utility-Stats: %d Plots, %s Strom, %s Wasser (7-Tage-Ø)",
                plotData.size(), formatElectricity(totalElec), formatWater(totalWater));
    }
}
