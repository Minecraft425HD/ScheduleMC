package de.rolandsw.schedulemc.region;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * PlotManager - Verwaltet alle Plots
 * 
 * Features:
 * - Laden/Speichern von Plots
 * - Plot-Erstellung aus BlockPos (für Selection Tool)
 * - Plot-Suche nach Position
 * - Auto-Save mit dirty-Flag
 */
public class PlotManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, PlotRegion> plots = new ConcurrentHashMap<>();
    private static final File PLOTS_FILE = new File("config/plotmod_plots.json");
    private static final Gson GSON = GsonHelper.get();

    // Spatial Index für schnelle Lookups
    private static final PlotSpatialIndex spatialIndex = new PlotSpatialIndex();

    private static boolean dirty = false;
    private static int plotCounter = 1;
    
    // ═══════════════════════════════════════════════════════════
    // PLOT ERSTELLEN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Erstellt Plot aus zwei BlockPos (für Selection Tool)
     *
     * @param pos1 Erste Position
     * @param pos2 Zweite Position
     * @param price Preis des Plots
     * @return Der erstellte Plot
     */
    public static PlotRegion createPlot(BlockPos pos1, BlockPos pos2, double price) {
        return createPlot(pos1, pos2, null, PlotType.RESIDENTIAL, price);
    }

    /**
     * Erstellt Plot mit Namen und Typ
     *
     * @param pos1 Erste Position
     * @param pos2 Zweite Position
     * @param customName Optionaler Name (wenn null, wird auto-generiert)
     * @param type Plot-Typ
     * @param price Preis des Plots (0 für nicht-kaufbare Typen)
     * @return Der erstellte Plot
     */
    public static PlotRegion createPlot(BlockPos pos1, BlockPos pos2, String customName, PlotType type, double price) {
        // Finde min/max Koordinaten
        BlockPos min = new BlockPos(
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ())
        );

        BlockPos max = new BlockPos(
            Math.max(pos1.getX(), pos2.getX()),
            Math.max(pos1.getY(), pos2.getY()),
            Math.max(pos1.getZ(), pos2.getZ())
        );

        String plotId = (customName != null && !customName.isEmpty()) ? customName : generatePlotId();
        PlotRegion plot = new PlotRegion(plotId, min, max, price);
        plot.setType(type);

        plots.put(plotId, plot);
        spatialIndex.addPlot(plot);

        // Automatisch ShopAccount erstellen für Shop-Plots
        if (type.isShop()) {
            ShopAccountManager.getOrCreateAccount(plotId);
            LOGGER.info("ShopAccount automatisch erstellt für Shop-Plot: {}", plotId);
        }

        dirty = true;

        LOGGER.info("Plot erstellt: {} ({}) von {} bis {} ({}€)",
            plotId, type.getDisplayName(), min.toShortString(), max.toShortString(), price);

        return plot;
    }
    
    /**
     * Generiert eine eindeutige Plot-ID
     */
    private static String generatePlotId() {
        String id;
        do {
            id = "plot_" + plotCounter++;
        } while (plots.containsKey(id));
        return id;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PLOT-SUCHE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Gibt Plot an einer Position zurück
     *
     * OPTIMIERT: Nutzt Spatial Index für O(1) statt O(n) Lookup
     * Mit Fallback zur linearen Suche als Sicherheitsnetz
     *
     * @param pos Die Position
     * @return Der Plot oder null
     */
    public static PlotRegion getPlotAt(BlockPos pos) {
        // Nutze Spatial Index um nur relevante Plots zu prüfen
        Set<String> candidatePlotIds = spatialIndex.getPlotsNear(pos);

        for (String plotId : candidatePlotIds) {
            PlotRegion plot = plots.get(plotId);
            if (plot != null && plot.contains(pos)) {
                return plot;
            }
        }

        // Fallback: Wenn Spatial Index nichts findet, prüfe alle Plots
        // Dies fängt Edge-Cases ab und wird nur selten ausgeführt
        for (PlotRegion plot : plots.values()) {
            if (plot.contains(pos)) {
                LOGGER.warn("Spatial Index Miss - Plot {} nicht im Index gefunden bei Position {}",
                           plot.getPlotId(), pos);
                // Re-indexiere diesen Plot
                spatialIndex.addPlot(plot);
                return plot;
            }
        }

        return null;
    }
    
    /**
     * Gibt Plot nach ID zurück
     * 
     * @param plotId Die Plot-ID
     * @return Der Plot oder null
     */
    public static PlotRegion getPlot(String plotId) {
        return plots.get(plotId);
    }
    
    /**
     * Prüft ob ein Plot mit dieser ID existiert
     */
    public static boolean hasPlot(String plotId) {
        return plots.containsKey(plotId);
    }
    
    /**
     * Gibt alle Plots zurück
     */
    public static List<PlotRegion> getPlots() {
        return new ArrayList<>(plots.values());
    }
    
    /**
     * Gibt alle Plots eines Besitzers zurück
     */
    public static List<PlotRegion> getPlotsByOwner(UUID ownerUUID) {
        return plots.values().stream()
            .filter(p -> p.isOwnedBy(ownerUUID))
            .collect(Collectors.toList());
    }
    
    /**
     * Gibt alle verfügbaren (kaufbaren) Plots zurück
     */
    public static List<PlotRegion> getAvailablePlots() {
        return plots.values().stream()
            .filter(p -> !p.hasOwner())
            .collect(Collectors.toList());
    }
    
    /**
     * Gibt alle zum Verkauf stehenden Plots zurück
     */
    public static List<PlotRegion> getPlotsForSale() {
        return plots.values().stream()
            .filter(PlotRegion::isForSale)
            .collect(Collectors.toList());
    }
    
    /**
     * Gibt alle zur Miete stehenden Plots zurück
     */
    public static List<PlotRegion> getPlotsForRent() {
        return plots.values().stream()
            .filter(p -> p.isForRent() && !p.isRented())
            .collect(Collectors.toList());
    }
    
    /**
     * Gibt Top X Plots nach Rating zurück
     */
    public static List<PlotRegion> getTopRatedPlots(int limit) {
        return plots.values().stream()
            .filter(p -> p.getRatingCount() > 0)
            .sorted((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // ═══════════════════════════════════════════════════════════
    // PLOT VERWALTUNG
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Fügt einen Plot hinzu
     */
    public static void addPlot(PlotRegion plot) {
        plots.put(plot.getPlotId(), plot);
        spatialIndex.addPlot(plot);
        dirty = true;
    }
    
    /**
     * Entfernt einen Plot
     */
    public static boolean removePlot(String plotId) {
        PlotRegion removed = plots.remove(plotId);
        if (removed != null) {
            spatialIndex.removePlot(plotId);
            dirty = true;
            LOGGER.info("Plot entfernt: {}", plotId);
            return true;
        }
        return false;
    }
    
    /**
     * Entfernt einen Plot an einer Position
     */
    public static boolean removePlotAt(BlockPos pos) {
        PlotRegion plot = getPlotAt(pos);
        if (plot != null) {
            return removePlot(plot.getPlotId());
        }
        return false;
    }
    
    /**
     * Gibt die Anzahl aller Plots zurück
     */
    public static int getPlotCount() {
        return plots.size();
    }
    
    /**
     * Gibt die Gesamtfläche aller Plots zurück
     */
    public static long getTotalPlotVolume() {
        return plots.values().stream()
            .mapToLong(PlotRegion::getVolume)
            .sum();
    }
    
    /**
     * Markiert Daten als geändert
     */
    public static void markDirty() {
        dirty = true;
    }

    /**
     * Baut den Spatial Index neu auf (Admin-Funktion)
     */
    public static void rebuildSpatialIndex() {
        spatialIndex.rebuild(plots.values());
        LOGGER.info("Spatial Index manuell neu aufgebaut");
    }

    // ═══════════════════════════════════════════════════════════
    // LADEN & SPEICHERN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Lädt alle Plots aus der Datei
     */
    public static void loadPlots() {
        try {
            if (!PLOTS_FILE.exists()) {
                LOGGER.info("Plots-Datei existiert nicht, erstelle neue");
                PLOTS_FILE.getParentFile().mkdirs();
                savePlots();
                return;
            }
            
            Type mapType = new TypeToken<Map<String, PlotRegion>>(){}.getType();
            
            try (FileReader reader = new FileReader(PLOTS_FILE)) {
                Map<String, PlotRegion> loaded = GSON.fromJson(reader, mapType);
                
                if (loaded != null) {
                    plots.clear();
                    plots.putAll(loaded);
                    
                    // Finde höchste Plot-Nummer für Counter
                    int maxId = plots.keySet().stream()
                        .filter(id -> id.startsWith("plot_"))
                        .map(id -> id.substring(5))
                        .mapToInt(s -> {
                            try {
                                return Integer.parseInt(s);
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        })
                        .max()
                        .orElse(0);

                    plotCounter = maxId + 1;

                    // Spatial Index neu aufbauen
                    spatialIndex.rebuild(plots.values());

                    LOGGER.info("Plots geladen: {} Plots", plots.size());
                    LOGGER.info("Spatial Index: {}", spatialIndex.getStats());
                }
            }

            dirty = false;
            
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Plots", e);
        }
    }
    
    /**
     * Speichert alle Plots in die Datei
     */
    public static void savePlots() {
        try {
            PLOTS_FILE.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(PLOTS_FILE)) {
                GSON.toJson(plots, writer);
            }
            
            dirty = false;
            LOGGER.info("Plots gespeichert: {} Plots", plots.size());
            
        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der Plots", e);
        }
    }
    
    /**
     * Speichert nur wenn Änderungen vorhanden (dirty)
     */
    public static void saveIfNeeded() {
        if (dirty) {
            savePlots();
        }
    }
    
    /**
     * Prüft ob ungespeicherte Änderungen vorhanden sind
     */
    public static boolean isDirty() {
        return dirty;
    }
    
    // ═══════════════════════════════════════════════════════════
    // STATISTIKEN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Gibt Statistiken zurück
     *
     * OPTIMIERT: Ein einzelner Durchlauf statt 6+ separate Streams
     */
    public static PlotStatistics getStatistics() {
        int totalPlots = 0;
        int ownedPlots = 0;
        int availablePlots = 0;
        int forSale = 0;
        int forRent = 0;
        int rented = 0;
        long totalVolume = 0;

        // Ein einzelner Durchlauf für alle Statistiken
        for (PlotRegion plot : plots.values()) {
            totalPlots++;
            totalVolume += plot.getVolume();

            if (plot.hasOwner()) {
                ownedPlots++;
            } else {
                availablePlots++;
            }

            if (plot.isForSale()) {
                forSale++;
            }

            if (plot.isForRent() && !plot.isRented()) {
                forRent++;
            }

            if (plot.isRented()) {
                rented++;
            }
        }

        return new PlotStatistics(
            totalPlots,
            ownedPlots,
            availablePlots,
            forSale,
            forRent,
            rented,
            totalVolume
        );
    }
    
    /**
     * Statistik-Datenklasse
     */
    public static class PlotStatistics {
        public final int totalPlots;
        public final int ownedPlots;
        public final int availablePlots;
        public final int forSale;
        public final int forRent;
        public final int rented;
        public final long totalVolume;
        
        public PlotStatistics(int totalPlots, int ownedPlots, int availablePlots,
                            int forSale, int forRent, int rented, long totalVolume) {
            this.totalPlots = totalPlots;
            this.ownedPlots = ownedPlots;
            this.availablePlots = availablePlots;
            this.forSale = forSale;
            this.forRent = forRent;
            this.rented = rented;
            this.totalVolume = totalVolume;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Löscht alle Plots (für Tests/Reset)
     */
    public static void clearAllPlots() {
        plots.clear();
        spatialIndex.clear();
        dirty = true;
        plotCounter = 1;
        LOGGER.warn("Alle Plots gelöscht!");
    }
    
    /**
     * Gibt Debug-Info aus
     */
    public static void printDebugInfo() {
        LOGGER.info("═══ PlotManager Debug-Info ═══");
        LOGGER.info("Plots gesamt: {}", plots.size());
        LOGGER.info("Dirty Flag: {}", dirty);
        LOGGER.info("Plot Counter: {}", plotCounter);
        LOGGER.info("Datei: {}", PLOTS_FILE.getAbsolutePath());
        
        PlotStatistics stats = getStatistics();
        LOGGER.info("Statistiken:");
        LOGGER.info("  - Besessen: {}", stats.ownedPlots);
        LOGGER.info("  - Verfügbar: {}", stats.availablePlots);
        LOGGER.info("  - Zu verkaufen: {}", stats.forSale);
        LOGGER.info("  - Zu vermieten: {}", stats.forRent);
        LOGGER.info("  - Vermietet: {}", stats.rented);
        LOGGER.info("  - Gesamtvolumen: {} Blöcke", stats.totalVolume);
        LOGGER.info("═══════════════════════════════");
    }
}
