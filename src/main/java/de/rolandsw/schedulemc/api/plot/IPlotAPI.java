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
}
