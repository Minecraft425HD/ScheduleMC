package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Service Interface für Plot-Verwaltung
 *
 * Dieses Interface definiert den Contract für das Plot-Management-System und ermöglicht:
 * - Bessere Testbarkeit durch Mock-Implementierungen
 * - Dependency Injection
 * - Loose Coupling zwischen Komponenten
 * - Multiple Implementierungen (z.B. Test-PlotManager, Production-PlotManager)
 *
 * @author ScheduleMC Team
 * @since 3.2.0
 */
public interface IPlotManager {

    // ========== Plot Creation ==========

    /**
     * Creates a new plot from two corner positions with default type and price
     *
     * @param pos1 First corner position
     * @param pos2 Second corner position (diagonal from pos1)
     * @param price Plot purchase price
     * @return The created plot region
     * @throws de.rolandsw.schedulemc.exceptions.PlotException if plot creation fails
     */
    PlotRegion createPlot(@Nonnull BlockPos pos1, @Nonnull BlockPos pos2, double price);

    /**
     * Creates a new plot with custom name and type
     *
     * @param pos1 First corner position
     * @param pos2 Second corner position (diagonal from pos1)
     * @param customName Custom plot name (optional)
     * @param type Plot type (WOHNUNG, HAUS, etc.)
     * @param price Plot purchase price
     * @return The created plot region
     * @throws de.rolandsw.schedulemc.exceptions.PlotException if plot creation fails or overlaps exist
     */
    PlotRegion createPlot(@Nonnull BlockPos pos1, @Nonnull BlockPos pos2, String customName, PlotType type, double price);

    // ========== Plot Lookup ==========

    /**
     * Finds a plot at the given position using spatial indexing
     *
     * @param pos The block position to check
     * @return The plot at this position, or null if none exists
     */
    @Nullable
    PlotRegion getPlotAt(@Nonnull BlockPos pos);

    /**
     * Retrieves a plot by its unique identifier
     *
     * @param plotId The plot's unique ID
     * @return The plot, or null if not found
     */
    @Nullable
    PlotRegion getPlot(@Nonnull String plotId);

    /**
     * Checks if a plot with the given ID exists
     *
     * @param plotId The plot ID to check
     * @return true if plot exists, false otherwise
     */
    boolean hasPlot(@Nonnull String plotId);

    // ========== Plot Queries ==========

    /**
     * Retrieves all plots in the system
     *
     * @return Unmodifiable list of all plots
     */
    List<PlotRegion> getPlots();

    /**
     * Finds all plots owned by a specific player
     *
     * @param ownerUUID The player's unique identifier
     * @return List of plots owned by the player
     */
    List<PlotRegion> getPlotsByOwner(@Nonnull UUID ownerUUID);

    /**
     * Finds all plots that have no owner
     *
     * @return List of available (unowned) plots
     */
    List<PlotRegion> getAvailablePlots();

    /**
     * Finds all plots that are for sale
     *
     * @return List of plots marked for sale
     */
    List<PlotRegion> getPlotsForSale();

    /**
     * Finds all plots that are available for rent
     *
     * @return List of plots marked for rent
     */
    List<PlotRegion> getPlotsForRent();

    /**
     * Retrieves the highest-rated plots
     *
     * @param limit Maximum number of plots to return
     * @return List of top-rated plots, sorted by rating (descending)
     */
    List<PlotRegion> getTopRatedPlots(int limit);

    // ========== Plot Management ==========

    /**
     * Adds a plot to the management system
     *
     * @param plot The plot to add
     * @throws de.rolandsw.schedulemc.exceptions.PlotException if plot ID already exists or overlaps with existing plot
     */
    void addPlot(@Nonnull PlotRegion plot);

    /**
     * Removes a plot by its unique identifier
     *
     * @param plotId The plot ID to remove
     * @return true if plot was removed, false if not found
     */
    boolean removePlot(@Nonnull String plotId);

    /**
     * Removes the plot at the given position
     *
     * @param pos The position to check
     * @return true if plot was removed, false if no plot exists at position
     */
    boolean removePlotAt(@Nonnull BlockPos pos);

    /**
     * Removes all plots from the system
     * WARNING: This is a destructive operation!
     */
    void clearAllPlots();

    // ========== Statistics & Info ==========

    /**
     * Returns the total number of plots
     *
     * @return Plot count
     */
    int getPlotCount();

    /**
     * Calculates the total volume of all plots
     *
     * @return Total block volume across all plots
     */
    long getTotalPlotVolume();

    /**
     * Retrieves comprehensive plot statistics
     *
     * @return Statistics object with counts and metrics
     */
    PlotManager.PlotStatistics getStatistics();

    /**
     * Returns spatial index cache statistics
     *
     * @return Cache performance metrics
     */
    PlotCache.CacheStatistics getCacheStatistics();

    /**
     * Resets cache statistics counters
     */
    void resetCacheStatistics();

    // ========== Persistence ==========

    /**
     * Loads all plots from persistent storage
     *
     * @throws RuntimeException if loading fails critically
     */
    void loadPlots();

    /**
     * Saves all plots to persistent storage immediately
     */
    void savePlots();

    /**
     * Saves plots only if there are unsaved changes
     */
    void saveIfNeeded();

    /**
     * Marks the plot data as dirty (requiring save)
     */
    void markDirty();

    // ========== System Operations ==========

    /**
     * Rebuilds the spatial index for efficient position lookups
     * Should be called after bulk plot modifications
     */
    void rebuildSpatialIndex();

    /**
     * Checks if the plot management system is healthy
     *
     * @return true if healthy, false if there are issues
     */
    boolean isHealthy();

    /**
     * Returns the last error message if system is unhealthy
     *
     * @return Error message, or null if healthy
     */
    @Nullable
    String getLastError();

    /**
     * Returns formatted health information for monitoring
     *
     * @return Formatted health status string
     */
    String getHealthInfo();

    /**
     * Prints debug information to console
     */
    void printDebugInfo();
}
