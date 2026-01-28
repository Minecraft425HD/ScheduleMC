package de.rolandsw.schedulemc.region;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
import de.rolandsw.schedulemc.util.InputValidation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
public class PlotManager implements IncrementalSaveManager.ISaveable {

    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile PlotManager instance;

    private PlotManager() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, PlotRegion> plots = new ConcurrentHashMap<>();
    private static final File PLOTS_FILE = new File("config/plotmod_plots.json");
    private static final Gson GSON = GsonHelper.get();

    // THREAD-SAFETY: Lock für atomare Multi-Step-Operationen
    // Schützt Konsistenz zwischen plots, spatialIndex, und plotCache
    private static final Object PLOT_LOCK = new Object();

    // Spatial Index für schnelle Lookups
    private static final PlotSpatialIndex spatialIndex = new PlotSpatialIndex();

    // Plot-Cache für Performance-Optimierung
    private static final PlotCache plotCache = new PlotCache(1000);

    // SICHERHEIT: volatile für Memory Visibility zwischen Threads (IncrementalSaveManager)
    private static volatile boolean dirty = false;
    // SICHERHEIT: AtomicInteger für Thread-safe Plot-ID Inkrement
    private static final AtomicInteger plotCounter = new AtomicInteger(1);
    private static volatile boolean isHealthy = true;
    private static volatile String lastError = null;
    
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
        // SICHERHEIT: Validiere Eingaben
        InputValidation.Result regionResult = InputValidation.validatePlotRegion(pos1, pos2);
        if (!regionResult.isValid()) {
            throw new IllegalArgumentException(regionResult.getError());
        }

        if (customName != null && !customName.isEmpty()) {
            InputValidation.Result nameResult = InputValidation.validatePlotName(customName);
            if (!nameResult.isValid()) {
                throw new IllegalArgumentException(nameResult.getError());
            }
        }

        InputValidation.Result priceResult = InputValidation.validateAmount(price);
        if (!priceResult.isValid()) {
            throw new IllegalArgumentException(priceResult.getError());
        }

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

        // THREAD-SAFETY: Atomare Operation für Plot-Hinzufügung
        synchronized (PLOT_LOCK) {
            plots.put(plotId, plot);
            spatialIndex.addPlot(plot);

            // Automatisch ShopAccount erstellen für Shop-Plots
            if (type.isShop()) {
                ShopAccountManager.getOrCreateAccount(plotId);
                LOGGER.info("ShopAccount automatically created for shop plot: {}", plotId);
            }

            dirty = true;
        }

        LOGGER.info("Plot created: {} ({}) from {} to {} ({}€)",
            plotId, type.getDisplayName(), min.toShortString(), max.toShortString(), price);

        return plot;
    }
    
    /**
     * Generiert eine eindeutige Plot-ID
     * SICHERHEIT: Thread-safe durch AtomicInteger
     */
    private static String generatePlotId() {
        String id;
        do {
            id = "plot_" + plotCounter.getAndIncrement();
        } while (plots.containsKey(id));
        return id;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PLOT-SUCHE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Gibt Plot an einer Position zurück
     *
     * OPTIMIERT:
     * 1. LRU-Cache für häufige Positionen (O(1))
     * 2. Spatial Index für Cache-Misses (O(1))
     * 3. Fallback zur linearen Suche als Sicherheitsnetz (O(n))
     *
     * @param pos Die Position
     * @return Der Plot oder null
     */
    public static PlotRegion getPlotAt(BlockPos pos) {
        // 1. Cache-Lookup (schnellster Pfad)
        PlotRegion cached = plotCache.get(pos);
        if (cached != null) {
            return cached;
        }

        // 2. Spatial Index um nur relevante Plots zu prüfen
        Set<String> candidatePlotIds = spatialIndex.getPlotsNear(pos);

        for (String plotId : candidatePlotIds) {
            PlotRegion plot = plots.get(plotId);
            if (plot != null && plot.contains(pos)) {
                // In Cache einfügen für zukünftige Lookups
                plotCache.put(pos, plot);
                return plot;
            }
        }

        // 3. Fallback: Wenn Spatial Index nichts findet, prüfe alle Plots
        // Dies fängt Edge-Cases ab und wird nur selten ausgeführt
        for (PlotRegion plot : plots.values()) {
            if (plot.contains(pos)) {
                LOGGER.warn("Spatial Index Miss - Plot {} not found in index at position {}",
                           plot.getPlotId(), pos);
                // Re-indexiere diesen Plot
                spatialIndex.addPlot(plot);
                // In Cache einfügen
                plotCache.put(pos, plot);
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
     * THREAD-SAFETY: Synchronized um Atomizität zu gewährleisten
     */
    public static void addPlot(PlotRegion plot) {
        synchronized (PLOT_LOCK) {
            plots.put(plot.getPlotId(), plot);
            spatialIndex.addPlot(plot);

            // Invalidiere Cache-Einträge in der Plot-Region
            plotCache.invalidateRegion(plot.getMin(), plot.getMax());

            dirty = true;
        }
    }

    /**
     * Entfernt einen Plot
     * THREAD-SAFETY: Synchronized um Atomizität zu gewährleisten
     */
    public static boolean removePlot(String plotId) {
        synchronized (PLOT_LOCK) {
            PlotRegion removed = plots.remove(plotId);
            if (removed != null) {
                spatialIndex.removePlot(plotId);

                // Invalidiere alle Cache-Einträge für diesen Plot
                plotCache.invalidatePlot(plotId);

                dirty = true;
                LOGGER.info("Plot removed: {}", plotId);
                return true;
            }
            return false;
        }
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
        LOGGER.info("Spatial index manually rebuilt");
    }

    // ═══════════════════════════════════════════════════════════
    // LADEN & SPEICHERN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Lädt alle Plots aus der Datei mit Backup-Wiederherstellung
     */
    public static void loadPlots() {
        if (!PLOTS_FILE.exists()) {
            LOGGER.info("Plots file does not exist, creating new");
            PLOTS_FILE.getParentFile().mkdirs();
            savePlots();
            isHealthy = true;
            return;
        }

        try {
            loadPlotsFromFile(PLOTS_FILE);
            isHealthy = true;
            lastError = null;
            dirty = false;
            LOGGER.info("Plots loaded successfully: {} plots", plots.size());
            LOGGER.info("Spatial Index: {}", spatialIndex.getStats());
        } catch (Exception e) {
            LOGGER.error("Error loading plots", e);
            lastError = "Failed to load: " + e.getMessage();

            // Versuch Backup wiederherzustellen
            if (BackupManager.restoreFromBackup(PLOTS_FILE)) {
                LOGGER.warn("Plots file corrupt, attempting to restore from backup...");
                try {
                    loadPlotsFromFile(PLOTS_FILE);
                    LOGGER.info("Plots successfully restored from backup: {} plots", plots.size());
                    isHealthy = true;
                    lastError = "Recovered from backup";
                    dirty = false;
                } catch (Exception backupError) {
                    LOGGER.error("CRITICAL: Backup restoration failed!", backupError);
                    handleCriticalLoadFailure();
                }
            } else {
                LOGGER.error("CRITICAL: No backup available for restoration!");
                handleCriticalLoadFailure();
            }
        }
    }

    /**
     * Lädt Plots aus einer spezifischen Datei
     * THREAD-SAFETY: Synchronized um Atomizität während des Ladevorgangs zu gewährleisten
     */
    private static void loadPlotsFromFile(File sourceFile) throws Exception {
        Type mapType = new TypeToken<Map<String, PlotRegion>>(){}.getType();

        try (FileReader reader = new FileReader(sourceFile)) {
            Map<String, PlotRegion> loaded = GSON.fromJson(reader, mapType);

            if (loaded == null) {
                throw new IOException("Loaded plot data is null");
            }

            // THREAD-SAFETY: Atomare Operation für Plot-Neuladen
            synchronized (PLOT_LOCK) {
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

                plotCounter.set(maxId + 1);

                // Spatial Index neu aufbauen
                spatialIndex.rebuild(plots.values());
            }
        }
    }

    /**
     * Behandelt kritischen Ladefehler mit Graceful Degradation
     * THREAD-SAFETY: Synchronized um Atomizität zu gewährleisten
     */
    private static void handleCriticalLoadFailure() {
        LOGGER.error("CRITICAL: Plot system could not be loaded!");
        LOGGER.error("Starting with empty plot system as fallback");

        synchronized (PLOT_LOCK) {
            plots.clear();
            spatialIndex.clear();
            plotCounter.set(1);
            isHealthy = false;
            lastError = "Critical load failure - running with empty data";
        }

        // Erstelle Notfall-Backup der korrupten Datei (außerhalb Lock - I/O Operation)
        if (PLOTS_FILE.exists()) {
            File corruptBackup = new File(PLOTS_FILE.getParent(),
                PLOTS_FILE.getName() + ".CORRUPT_" + System.currentTimeMillis());
            try {
                java.nio.file.Files.copy(PLOTS_FILE.toPath(), corruptBackup.toPath());
                LOGGER.info("Corrupt file backed up to: {}", corruptBackup.getName());
            } catch (IOException e) {
                LOGGER.error("Could not backup corrupt file", e);
            }
        }
    }
    
    /**
     * Speichert alle Plots in die Datei mit Backup
     */
    public static void savePlots() {
        try {
            PLOTS_FILE.getParentFile().mkdirs();

            // Erstelle Backup vor dem Speichern (falls Datei existiert)
            if (PLOTS_FILE.exists() && PLOTS_FILE.length() > 0) {
                BackupManager.createBackup(PLOTS_FILE);
            }

            // Temporäre Datei für atomares Schreiben
            File tempFile = new File(PLOTS_FILE.getParent(), PLOTS_FILE.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                GSON.toJson(plots, writer);
                writer.flush();
            }

            // Atomares Ersetzen
            java.nio.file.Files.move(tempFile.toPath(), PLOTS_FILE.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);

            dirty = false;
            isHealthy = true;
            lastError = null;
            LOGGER.info("Plots saved: {} plots", plots.size());

        } catch (Exception e) {
            LOGGER.error("CRITICAL: Error saving plots", e);
            isHealthy = false;
            lastError = "Save failed: " + e.getMessage();

            // Retry-Mechanismus
            try {
                Thread.sleep(100);
                retrySavePlots();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Retry-Mechanismus für fehlgeschlagene Saves
     */
    private static void retrySavePlots() {
        LOGGER.warn("Attempting to save plots again...");
        try {
            File tempFile = new File(PLOTS_FILE.getParent(), PLOTS_FILE.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                GSON.toJson(plots, writer);
                writer.flush();
            }

            java.nio.file.Files.move(tempFile.toPath(), PLOTS_FILE.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);

            LOGGER.info("Retry successful - plots saved");
            isHealthy = true;
            lastError = null;
            dirty = false;

        } catch (Exception retryError) {
            LOGGER.error("CRITICAL: Retry failed - plots could not be saved!", retryError);
            dirty = true;
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
     * THREAD-SAFETY: Synchronized um Atomizität zu gewährleisten
     */
    public static void clearAllPlots() {
        synchronized (PLOT_LOCK) {
            plots.clear();
            spatialIndex.clear();
            plotCache.clear();
            dirty = true;
            plotCounter.set(1);
            LOGGER.warn("All plots deleted!");
        }
    }
    
    /**
     * Gibt Debug-Info aus
     */
    public static void printDebugInfo() {
        LOGGER.info("═══ PlotManager Debug Info ═══");
        LOGGER.info("Total plots: {}", plots.size());
        LOGGER.info("Dirty flag: {}", dirty);
        LOGGER.info("Plot counter: {}", plotCounter);
        LOGGER.info("File: {}", PLOTS_FILE.getAbsolutePath());

        PlotStatistics stats = getStatistics();
        LOGGER.info("Statistics:");
        LOGGER.info("  - Owned: {}", stats.ownedPlots);
        LOGGER.info("  - Available: {}", stats.availablePlots);
        LOGGER.info("  - For sale: {}", stats.forSale);
        LOGGER.info("  - For rent: {}", stats.forRent);
        LOGGER.info("  - Rented: {}", stats.rented);
        LOGGER.info("  - Total volume: {} blocks", stats.totalVolume);
        LOGGER.info("═══════════════════════════════");
    }

    /**
     * Gibt den Health-Status des Plot-Systems zurück
     */
    public static boolean isHealthy() {
        return isHealthy;
    }

    /**
     * Gibt die letzte Fehlermeldung zurück (oder null wenn gesund)
     */
    public static String getLastError() {
        return lastError;
    }

    /**
     * Gibt detaillierte Health-Informationen zurück
     */
    public static String getHealthInfo() {
        if (isHealthy) {
            PlotCache.CacheStatistics cacheStats = plotCache.getStatistics();
            return Component.translatable("health.plot_manager.healthy",
                plots.size(),
                BackupManager.getBackupCount(PLOTS_FILE),
                String.format("%.1f", cacheStats.hitRate)).getString();
        } else {
            return Component.translatable("health.plot_manager.unhealthy",
                lastError != null ? lastError : "Unknown",
                plots.size()).getString();
        }
    }

    /**
     * Gibt Cache-Statistiken zurück
     */
    public static PlotCache.CacheStatistics getCacheStatistics() {
        return plotCache.getStatistics();
    }

    /**
     * Setzt Cache-Statistiken zurück
     */
    public static void resetCacheStatistics() {
        plotCache.resetStatistics();
    }

    // ═══════════════════════════════════════════════════════════
    // INCREMENTAL SAVE MANAGER INTEGRATION
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void save() {
        savePlots();
    }

    @Override
    public String getName() {
        return "PlotManager";
    }

    @Override
    public int getPriority() {
        return 1; // Hohe Priorität - Plot-Daten sind wichtig
    }

    /**
     * Gibt die Singleton-Instanz zurück (für IncrementalSaveManager Registration)
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static PlotManager getInstance() {
        PlotManager localRef = instance;
        if (localRef == null) {
            synchronized (PlotManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new PlotManager();
                }
            }
        }
        return localRef;
    }
}
