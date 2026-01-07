package de.rolandsw.schedulemc.region;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.exceptions.PlotException;
import de.rolandsw.schedulemc.util.GsonHelper;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
import de.rolandsw.schedulemc.util.InputValidation;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
 *
 * Implements IPlotManager for dependency injection and loose coupling.
 */
public class PlotManager implements IPlotManager, IncrementalSaveManager.ISaveable {

    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile PlotManager instance;

    private PlotManager() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    // Cache Configuration
    private static final int DEFAULT_PLOT_CACHE_SIZE = 1000;

    private static final Map<String, PlotRegion> plots = new ConcurrentHashMap<>();
    private static final File PLOTS_FILE = new File("config/plotmod_plots.json");
    private static final Gson GSON = GsonHelper.get();

    // Spatial Index für schnelle Lookups
    private static final PlotSpatialIndex spatialIndex = new PlotSpatialIndex();

    // Plot-Cache für Performance-Optimierung
    private static final PlotCache plotCache = new PlotCache(DEFAULT_PLOT_CACHE_SIZE);

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
     * Creates a plot region from two corner positions with the specified price.
     *
     * This is a convenience method for creating residential plots using the selection tool.
     * The method automatically calculates the minimum and maximum bounds from the two positions.
     * The plot is assigned an auto-generated ID and type RESIDENTIAL.
     *
     * @param pos1 The first corner position of the plot region
     * @param pos2 The second corner position of the plot region (opposite corner)
     * @param price The purchase price for the plot (must be non-negative)
     * @return The newly created PlotRegion instance
     * @throws IllegalArgumentException if the region dimensions are invalid or price is negative
     */
    public static PlotRegion createPlot(@Nonnull BlockPos pos1, @Nonnull BlockPos pos2, double price) {
        return createPlot(pos1, pos2, null, PlotType.RESIDENTIAL, price);
    }

    /**
     * Creates a plot region with full configuration including custom name and type.
     *
     * This method provides complete control over plot creation including custom naming,
     * plot type specification, and pricing. Input validation is performed on all parameters
     * including region dimensions, name format, and price. The plot is automatically added
     * to the spatial index for efficient lookups. If the plot type is a shop, a shop account
     * is automatically created.
     *
     * @param pos1 The first corner position of the plot region
     * @param pos2 The second corner position of the plot region (opposite corner)
     * @param customName Optional custom name for the plot (if null or empty, auto-generates "plot_N")
     * @param type The type of plot (RESIDENTIAL, COMMERCIAL, SHOP, etc.)
     * @param price The purchase price for the plot (0 for non-purchasable types, must be non-negative)
     * @return The newly created PlotRegion instance
     * @throws PlotException if coordinates are null, region dimensions are invalid, name format is invalid, price is negative, or plot ID already exists
     */
    public static PlotRegion createPlot(@Nonnull BlockPos pos1, @Nonnull BlockPos pos2, String customName, PlotType type, double price) {
        // SICHERHEIT: Null-Check vor Validierung
        if (pos1 == null || pos2 == null) {
            throw new PlotException("Plot coordinates cannot be null");
        }

        // SICHERHEIT: Validiere Eingaben
        InputValidation.Result regionResult = InputValidation.validatePlotRegion(pos1, pos2);
        if (!regionResult.isValid()) {
            throw new PlotException("Invalid plot region: " + regionResult.getError());
        }

        if (customName != null && !customName.isEmpty()) {
            InputValidation.Result nameResult = InputValidation.validatePlotName(customName);
            if (!nameResult.isValid()) {
                throw new PlotException("Invalid plot name: " + nameResult.getError());
            }
        }

        InputValidation.Result priceResult = InputValidation.validateAmount(price);
        if (!priceResult.isValid()) {
            throw new PlotException("Invalid plot price: " + priceResult.getError());
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

        // SICHERHEIT: Verhindere Überschreibung existierender Plots
        if (plots.containsKey(plotId)) {
            throw new PlotException("Plot with ID '" + plotId + "' already exists");
        }

        PlotRegion plot = new PlotRegion(plotId, min, max, price);
        plot.setType(type);

        plots.put(plotId, plot);

        // SICHERHEIT: Fehlerbehandlung für Spatial Index
        try {
            spatialIndex.addPlot(plot);
        } catch (Exception e) {
            plots.remove(plotId); // Rollback
            throw new PlotException("Failed to add plot to spatial index: " + plotId, e);
        }

        // Automatisch ShopAccount erstellen für Shop-Plots
        if (type.isShop()) {
            try {
                ShopAccountManager.getOrCreateAccount(plotId);
                LOGGER.info("ShopAccount automatisch erstellt für Shop-Plot: {}", plotId);
            } catch (Exception e) {
                // Rollback: Plot und Spatial Index entfernen
                plots.remove(plotId);
                spatialIndex.removePlot(plot);
                throw new PlotException("Failed to create shop account for plot: " + plotId, e);
            }
        }

        dirty = true;

        LOGGER.info("Plot erstellt: {} ({}) von {} bis {} ({}€)",
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
    @Nullable
    public static PlotRegion getPlotAt(@Nonnull BlockPos pos) {
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
                LOGGER.warn("Spatial Index Miss - Plot {} nicht im Index gefunden bei Position {}",
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
     * Retrieves a plot by its unique identifier.
     *
     * This method performs a direct O(1) lookup in the plot map using the plot ID.
     * The plot ID is case-sensitive and must match exactly.
     *
     * @param plotId The unique identifier of the plot to retrieve
     * @return The PlotRegion with the specified ID, or null if no plot exists with that ID
     */
    @Nullable
    public static PlotRegion getPlot(@Nonnull String plotId) {
        return plots.get(plotId);
    }
    
    /**
     * Checks if a plot with the specified ID exists in the system.
     *
     * This is a thread-safe O(1) lookup operation that checks for plot existence
     * without retrieving the full plot data.
     *
     * @param plotId The unique identifier of the plot to check
     * @return true if a plot with this ID exists, false otherwise
     */
    public static boolean hasPlot(@Nonnull String plotId) {
        return plots.containsKey(plotId);
    }
    
    /**
     * Retrieves all plots registered in the system.
     *
     * Returns a defensive copy as a new ArrayList to prevent external modification
     * of the internal plot collection. The list includes all plots regardless of
     * ownership, type, or availability status.
     *
     * @return A new ArrayList containing all PlotRegion instances in the system
     */
    public static List<PlotRegion> getPlots() {
        return new ArrayList<>(plots.values());
    }
    
    /**
     * Retrieves all plots owned by the specified player.
     *
     * This method filters the plot collection to return only plots where the specified
     * player is registered as the owner. The returned list is a new collection that can
     * be safely modified without affecting the plot manager.
     *
     * @param ownerUUID The unique identifier of the player whose plots should be retrieved
     * @return A List of PlotRegion instances owned by the specified player (empty list if player owns no plots)
     */
    public static List<PlotRegion> getPlotsByOwner(@Nonnull UUID ownerUUID) {
        return plots.values().stream()
            .filter(p -> p.isOwnedBy(ownerUUID))
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves all plots that are currently available for purchase.
     *
     * Returns plots that do not have an owner assigned. These are plots that players
     * can potentially purchase or claim. The returned list does not include plots that
     * are already owned, even if they might be for sale by their owner.
     *
     * @return A List of unowned PlotRegion instances (empty list if no plots are available)
     */
    public static List<PlotRegion> getAvailablePlots() {
        return plots.values().stream()
            .filter(p -> !p.hasOwner())
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves all plots that are currently listed for sale.
     *
     * This includes both initially available plots and plots being resold by their owners.
     * A plot is considered "for sale" if it has the forSale flag set, regardless of
     * ownership status.
     *
     * @return A List of PlotRegion instances that are for sale (empty list if none available)
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
     * Adds a plot to the plot management system.
     *
     * This method registers the plot in the main plot map and adds it to the spatial
     * index for efficient position-based lookups. The plot cache is invalidated for the
     * region covered by the new plot. The manager is marked as dirty to ensure the
     * changes are persisted.
     *
     * @param plot The PlotRegion instance to add to the system
     * @throws PlotException if plot is null, plot ID already exists, or spatial index addition fails
     */
    public static void addPlot(@Nonnull PlotRegion plot) {
        // SICHERHEIT: Null-Check
        if (plot == null) {
            throw new PlotException("Cannot add null plot to manager");
        }

        // SICHERHEIT: Verhindere Überschreibung existierender Plots
        if (plots.containsKey(plot.getPlotId())) {
            throw new PlotException("Cannot add plot: ID '" + plot.getPlotId() +
                                    "' already exists. Use update instead or remove existing first");
        }

        plots.put(plot.getPlotId(), plot);

        // SICHERHEIT: Fehlerbehandlung für Spatial Index mit Rollback
        try {
            spatialIndex.addPlot(plot);
        } catch (Exception e) {
            plots.remove(plot.getPlotId()); // Rollback
            throw new PlotException("Failed to add plot to spatial index: " + plot.getPlotId(), e);
        }

        // Invalidiere Cache-Einträge in der Plot-Region
        plotCache.invalidateRegion(plot.getMin(), plot.getMax());

        dirty = true;
    }

    /**
     * Removes a plot from the plot management system by its ID.
     *
     * This method removes the plot from the main plot map, the spatial index, and
     * invalidates all cache entries associated with the plot. The manager is marked
     * as dirty to ensure the changes are persisted. This operation cannot be undone.
     *
     * @param plotId The unique identifier of the plot to remove
     * @return true if the plot was found and removed, false if no plot exists with the specified ID
     * @throws PlotException if plotId is null or empty
     */
    public static boolean removePlot(@Nonnull String plotId) {
        // SICHERHEIT: Null/Empty-Check
        if (plotId == null || plotId.isEmpty()) {
            throw new PlotException("Cannot remove plot: ID cannot be null or empty");
        }
        PlotRegion removed = plots.remove(plotId);
        if (removed != null) {
            spatialIndex.removePlot(plotId);

            // Invalidiere alle Cache-Einträge für diesen Plot
            plotCache.invalidatePlot(plotId);

            dirty = true;
            LOGGER.info("Plot entfernt: {}", plotId);
            return true;
        }
        return false;
    }
    
    /**
     * Removes the plot that contains the specified position.
     *
     * This is a convenience method that first looks up the plot at the given position
     * and then removes it if found. All spatial index and cache entries are updated
     * accordingly.
     *
     * @param pos The position to check for a plot
     * @return true if a plot was found at the position and removed, false if no plot exists at that position
     */
    public static boolean removePlotAt(@Nonnull BlockPos pos) {
        PlotRegion plot = getPlotAt(pos);
        if (plot != null) {
            return removePlot(plot.getPlotId());
        }
        return false;
    }
    
    /**
     * Returns the total number of plots currently registered in the system.
     *
     * This count includes all plots regardless of ownership status, type, or availability.
     * This is a constant-time O(1) operation.
     *
     * @return The total number of plots in the system
     */
    public static int getPlotCount() {
        return plots.size();
    }
    
    /**
     * Calculates and returns the total volume of all plots in the system.
     *
     * The volume is measured in cubic blocks and is calculated by summing the volume
     * of each individual plot. This operation iterates through all plots, so performance
     * scales linearly with the number of plots.
     *
     * @return The total volume of all plots in cubic blocks
     */
    public static long getTotalPlotVolume() {
        return plots.values().stream()
            .mapToLong(PlotRegion::getVolume)
            .sum();
    }
    
    /**
     * Marks the plot manager as having unsaved changes.
     *
     * This flag indicates that plot data has been modified and needs to be persisted
     * to disk. The incremental save manager will automatically save dirty managers
     * during the next save cycle.
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
     * Lädt alle Plots aus der Datei mit Backup-Wiederherstellung
     */
    public static void loadPlots() {
        if (!PLOTS_FILE.exists()) {
            LOGGER.info("Plots-Datei existiert nicht, erstelle neue");
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
            LOGGER.info("Plots erfolgreich geladen: {} Plots", plots.size());
            LOGGER.info("Spatial Index: {}", spatialIndex.getStats());
        } catch (PlotException e) {
            LOGGER.error("Fehler beim Laden der Plots", e);
            lastError = "Failed to load: " + e.getMessage();

            // Versuch Backup wiederherzustellen
            if (BackupManager.restoreFromBackup(PLOTS_FILE)) {
                LOGGER.warn("Plots-Datei korrupt, versuche Backup wiederherzustellen...");
                try {
                    loadPlotsFromFile(PLOTS_FILE);
                    LOGGER.info("Plots erfolgreich von Backup wiederhergestellt: {} Plots", plots.size());
                    isHealthy = true;
                    lastError = "Recovered from backup";
                    dirty = false;
                } catch (PlotException backupError) {
                    LOGGER.error("KRITISCH: Backup-Wiederherstellung fehlgeschlagen!", backupError);
                    handleCriticalLoadFailure();
                }
            } else {
                LOGGER.error("KRITISCH: Kein Backup verfügbar für Wiederherstellung!");
                handleCriticalLoadFailure();
            }
        }
    }

    /**
     * Lädt Plots aus einer spezifischen Datei.
     *
     * @param sourceFile The file to load plot data from
     * @throws PlotException if file reading fails, data is corrupted, or spatial index rebuild fails
     */
    private static void loadPlotsFromFile(File sourceFile) throws PlotException {
        Type mapType = new TypeToken<Map<String, PlotRegion>>(){}.getType();

        try (FileReader reader = new FileReader(sourceFile)) {
            Map<String, PlotRegion> loaded;

            try {
                loaded = GSON.fromJson(reader, mapType);
            } catch (JsonSyntaxException e) {
                throw new PlotException("Failed to parse plot data from file: JSON syntax error", e);
            }

            if (loaded == null) {
                throw new PlotException("Loaded plot data is null - file may be corrupted or empty");
            }

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

            // SICHERHEIT: Spatial Index neu aufbauen mit Error-Handling
            try {
                spatialIndex.rebuild(plots.values());
            } catch (Exception e) {
                throw new PlotException("Failed to rebuild spatial index after loading plots", e);
            }
        } catch (IOException e) {
            throw new PlotException("Failed to read plot data from file: " + sourceFile.getAbsolutePath(), e);
        }
    }

    /**
     * Behandelt kritischen Ladefehler mit Graceful Degradation
     */
    private static void handleCriticalLoadFailure() {
        LOGGER.error("KRITISCH: Plot-System konnte nicht geladen werden!");
        LOGGER.error("Starte mit leerem Plot-System als Fallback");

        plots.clear();
        spatialIndex.clear();
        plotCounter.set(1);
        isHealthy = false;
        lastError = "Critical load failure - running with empty data";

        // Erstelle Notfall-Backup der korrupten Datei
        if (PLOTS_FILE.exists()) {
            File corruptBackup = new File(PLOTS_FILE.getParent(),
                PLOTS_FILE.getName() + ".CORRUPT_" + System.currentTimeMillis());
            try {
                java.nio.file.Files.copy(PLOTS_FILE.toPath(), corruptBackup.toPath());
                LOGGER.info("Korrupte Datei gesichert nach: {}", corruptBackup.getName());
            } catch (IOException e) {
                LOGGER.error("Konnte korrupte Datei nicht sichern", e);
            }
        }
    }
    
    /**
     * Speichert alle Plots in die Datei mit Backup.
     *
     * @throws PlotException if saving fails after retry attempts
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
            LOGGER.info("Plots gespeichert: {} Plots", plots.size());

        } catch (IOException e) {
            LOGGER.error("KRITISCH: Fehler beim Speichern der Plots", e);
            isHealthy = false;
            lastError = "Save failed: " + e.getMessage();

            // Retry-Mechanismus
            try {
                Thread.sleep(100);
                retrySavePlots();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            // SICHERHEIT: Werfe PlotException nach Retry-Versuch
            throw new PlotException("Failed to save plots to disk: " + e.getMessage() +
                                    " (Retry attempted)", e);
        }
    }

    /**
     * Retry-Mechanismus für fehlgeschlagene Saves
     */
    private static void retrySavePlots() {
        LOGGER.warn("Versuche erneut Plots zu speichern...");
        try {
            File tempFile = new File(PLOTS_FILE.getParent(), PLOTS_FILE.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                GSON.toJson(plots, writer);
                writer.flush();
            }

            java.nio.file.Files.move(tempFile.toPath(), PLOTS_FILE.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);

            LOGGER.info("Retry erfolgreich - Plots gespeichert");
            isHealthy = true;
            lastError = null;
            dirty = false;

        } catch (IOException retryError) {
            LOGGER.error("KRITISCH: Retry fehlgeschlagen - Plots konnten nicht gespeichert werden!", retryError);
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
     */
    public static void clearAllPlots() {
        plots.clear();
        spatialIndex.clear();
        plotCache.clear();
        dirty = true;
        plotCounter.set(1);
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
            return String.format("§aGESUND§r - %d Plots, %d Backups, Cache: %.1f%% Hit-Rate",
                plots.size(), BackupManager.getBackupCount(PLOTS_FILE), cacheStats.hitRate);
        } else {
            return String.format("§cUNGESUND§r - Letzter Fehler: %s, %d Plots geladen",
                lastError != null ? lastError : "Unknown", plots.size());
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

    // ═══════════════════════════════════════════════════════════════════════
    // IPlotManager Implementation - Instance Methods
    // These delegate to static methods for backward compatibility
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public PlotRegion createPlot(@Nonnull BlockPos pos1, @Nonnull BlockPos pos2, double price) {
        return PlotManager.createPlot(pos1, pos2, price);
    }

    @Override
    public PlotRegion createPlot(@Nonnull BlockPos pos1, @Nonnull BlockPos pos2, String customName, PlotType type, double price) {
        return PlotManager.createPlot(pos1, pos2, customName, type, price);
    }

    @Override
    @Nullable
    public PlotRegion getPlotAt(@Nonnull BlockPos pos) {
        return PlotManager.getPlotAt(pos);
    }

    @Override
    @Nullable
    public PlotRegion getPlot(@Nonnull String plotId) {
        return PlotManager.getPlot(plotId);
    }

    @Override
    public boolean hasPlot(@Nonnull String plotId) {
        return PlotManager.hasPlot(plotId);
    }

    @Override
    public List<PlotRegion> getPlots() {
        return PlotManager.getPlots();
    }

    @Override
    public List<PlotRegion> getPlotsByOwner(@Nonnull UUID ownerUUID) {
        return PlotManager.getPlotsByOwner(ownerUUID);
    }

    @Override
    public List<PlotRegion> getAvailablePlots() {
        return PlotManager.getAvailablePlots();
    }

    @Override
    public List<PlotRegion> getPlotsForSale() {
        return PlotManager.getPlotsForSale();
    }

    @Override
    public List<PlotRegion> getPlotsForRent() {
        return PlotManager.getPlotsForRent();
    }

    @Override
    public List<PlotRegion> getTopRatedPlots(int limit) {
        return PlotManager.getTopRatedPlots(limit);
    }

    @Override
    public void addPlot(@Nonnull PlotRegion plot) {
        PlotManager.addPlot(plot);
    }

    @Override
    public boolean removePlot(@Nonnull String plotId) {
        return PlotManager.removePlot(plotId);
    }

    @Override
    public boolean removePlotAt(@Nonnull BlockPos pos) {
        return PlotManager.removePlotAt(pos);
    }

    @Override
    public void clearAllPlots() {
        PlotManager.clearAllPlots();
    }

    @Override
    public int getPlotCount() {
        return PlotManager.getPlotCount();
    }

    @Override
    public long getTotalPlotVolume() {
        return PlotManager.getTotalPlotVolume();
    }

    @Override
    public PlotStatistics getStatistics() {
        return PlotManager.getStatistics();
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        return PlotManager.getCacheStatistics();
    }

    @Override
    public void resetCacheStatistics() {
        PlotManager.resetCacheStatistics();
    }

    @Override
    public void loadPlots() {
        PlotManager.loadPlots();
    }

    @Override
    public void savePlots() {
        PlotManager.savePlots();
    }

    @Override
    public void saveIfNeeded() {
        PlotManager.saveIfNeeded();
    }

    @Override
    public void markDirty() {
        PlotManager.markDirty();
    }

    @Override
    public void rebuildSpatialIndex() {
        PlotManager.rebuildSpatialIndex();
    }

    @Override
    public boolean isHealthy() {
        return PlotManager.isHealthy();
    }

    @Override
    @Nullable
    public String getLastError() {
        return PlotManager.getLastError();
    }

    @Override
    public String getHealthInfo() {
        return PlotManager.getHealthInfo();
    }

    @Override
    public void printDebugInfo() {
        PlotManager.printDebugInfo();
    }
}
