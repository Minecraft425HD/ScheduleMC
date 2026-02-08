package de.rolandsw.schedulemc.api.plot;

import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Public Plot API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Plot-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Plot-Erstellung und -Verwaltung</li>
 *   <li>Besitzer- und Permissions-Verwaltung</li>
 *   <li>Plot-Suche nach Position oder Besitzer</li>
 *   <li>Kauf/Verkauf von Plots</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch Verwendung von ConcurrentHashMap.
 *
 * <h2>Performance:</h2>
 * Plot-Lookups nutzen LRU-Cache und Spatial Index für O(1) bzw. O(log n) Performance.
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IPlotAPI plotAPI = ScheduleMCAPI.getPlotAPI();
 *
 * // Plot an Position finden
 * PlotRegion plot = plotAPI.getPlotAt(pos);
 *
 * // Alle Plots eines Spielers
 * List<PlotRegion> myPlots = plotAPI.getPlotsByOwner(playerUUID);
 *
 * // Neuen Plot erstellen
 * PlotRegion newPlot = plotAPI.createPlot(pos1, pos2, "MeinPlot", PlotType.RESIDENTIAL, 1000.0);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IPlotAPI {

    /**
     * Gibt den Plot an einer bestimmten Position zurück.
     * <p>
     * Performance: O(1) durch LRU-Cache, O(log n) bei Cache-Miss durch Spatial Index
     *
     * @param pos Die Position
     * @return Der Plot oder null wenn kein Plot an dieser Position existiert
     * @throws IllegalArgumentException wenn pos null ist
     */
    @Nullable
    PlotRegion getPlotAt(BlockPos pos);

    /**
     * Gibt einen Plot anhand seiner ID zurück.
     *
     * @param plotId Die Plot-ID
     * @return Der Plot oder null wenn nicht gefunden
     * @throws IllegalArgumentException wenn plotId null ist
     */
    @Nullable
    PlotRegion getPlot(String plotId);

    /**
     * Prüft ob ein Plot mit dieser ID existiert.
     *
     * @param plotId Die Plot-ID
     * @return true wenn Plot existiert, false sonst
     * @throws IllegalArgumentException wenn plotId null ist
     */
    boolean hasPlot(String plotId);

    /**
     * Gibt alle Plots eines Besitzers zurück.
     *
     * @param ownerUUID Die UUID des Besitzers
     * @return Liste aller Plots (kann leer sein)
     * @throws IllegalArgumentException wenn ownerUUID null ist
     */
    List<PlotRegion> getPlotsByOwner(UUID ownerUUID);

    /**
     * Gibt alle verfügbaren (kaufbaren) Plots zurück.
     *
     * @return Liste aller Plots ohne Besitzer (kann leer sein)
     */
    List<PlotRegion> getAvailablePlots();

    /**
     * Gibt alle zum Verkauf stehenden Plots zurück.
     *
     * @return Liste aller Plots die zum Verkauf stehen (kann leer sein)
     */
    List<PlotRegion> getPlotsForSale();

    /**
     * Erstellt einen neuen Plot.
     *
     * @param pos1 Erste Ecke des Plots
     * @param pos2 Zweite Ecke des Plots (diagonal gegenüber)
     * @param plotName Name des Plots (optional, wird auto-generiert wenn null)
     * @param type Typ des Plots (RESIDENTIAL, COMMERCIAL, SHOP, PUBLIC, GOVERNMENT)
     * @param price Preis des Plots in Euro
     * @return Der erstellte Plot
     * @throws IllegalArgumentException wenn Positionen ungültig oder Plot zu groß
     */
    PlotRegion createPlot(BlockPos pos1, BlockPos pos2, @Nullable String plotName, PlotType type, double price);

    /**
     * Entfernt einen Plot.
     *
     * @param plotId Die ID des zu entfernenden Plots
     * @return true wenn erfolgreich entfernt, false wenn Plot nicht existiert
     * @throws IllegalArgumentException wenn plotId null ist
     */
    boolean removePlot(String plotId);

    /**
     * Gibt die Gesamtanzahl aller Plots zurück.
     *
     * @return Anzahl der Plots
     */
    int getPlotCount();

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns all plots of a specific type.
     *
     * @param type The plot type
     * @return List of plots matching the type
     * @throws IllegalArgumentException if type is null
     * @since 3.2.0
     */
    List<PlotRegion> getPlotsByType(PlotType type);

    /**
     * Sets the owner of a plot.
     *
     * @param plotId The plot ID
     * @param ownerUUID The new owner's UUID (null to remove owner)
     * @return true if successful
     * @throws IllegalArgumentException if plotId is null
     * @since 3.2.0
     */
    boolean setPlotOwner(String plotId, @Nullable UUID ownerUUID);

    /**
     * Sets the price of a plot.
     *
     * @param plotId The plot ID
     * @param price New price in Euro
     * @return true if successful
     * @throws IllegalArgumentException if plotId is null or price negative
     * @since 3.2.0
     */
    boolean setPlotPrice(String plotId, double price);

    /**
     * Adds a trusted player to a plot.
     *
     * @param plotId The plot ID
     * @param playerUUID The player to trust
     * @return true if successful
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    boolean addTrustedPlayer(String plotId, UUID playerUUID);

    /**
     * Removes a trusted player from a plot.
     *
     * @param plotId The plot ID
     * @param playerUUID The player to untrust
     * @return true if successful
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    boolean removeTrustedPlayer(String plotId, UUID playerUUID);

    /**
     * Returns all trusted players of a plot.
     *
     * @param plotId The plot ID
     * @return Set of trusted player UUIDs
     * @throws IllegalArgumentException if plotId is null
     * @since 3.2.0
     */
    java.util.Set<UUID> getTrustedPlayers(String plotId);

    /**
     * Sets a plot for sale or removes it from sale.
     *
     * @param plotId The plot ID
     * @param forSale true to list for sale, false to delist
     * @return true if successful
     * @throws IllegalArgumentException if plotId is null
     * @since 3.2.0
     */
    boolean setPlotForSale(String plotId, boolean forSale);

    /**
     * Changes the type of an existing plot.
     *
     * @param plotId The plot ID
     * @param newType The new plot type
     * @return true if successful
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    boolean setPlotType(String plotId, PlotType newType);

    /**
     * Returns all plots within a radius of a position.
     *
     * @param center Center position
     * @param radius Search radius in blocks
     * @return List of plots within range
     * @throws IllegalArgumentException if center is null or radius negative
     * @since 3.2.0
     */
    List<PlotRegion> getPlotsInRadius(net.minecraft.core.BlockPos center, double radius);

    /**
     * Returns the total number of plots per type.
     *
     * @return Map of PlotType to count
     * @since 3.2.0
     */
    java.util.Map<PlotType, Integer> getPlotCountByType();
}
