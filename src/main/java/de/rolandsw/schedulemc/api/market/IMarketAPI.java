package de.rolandsw.schedulemc.api.market;

import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Public Market API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das dynamische Markt-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Dynamische Preisgestaltung</li>
 *   <li>Angebot und Nachfrage</li>
 *   <li>Markttrends und Statistiken</li>
 *   <li>Preishistorie</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch ConcurrentHashMap.
 *
 * <h2>Preis-Dynamik:</h2>
 * Preise ändern sich basierend auf:
 * - Spieler-Käufen und -Verkäufen
 * - Produktionsmenge
 * - Zeit und Trends
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IMarketAPI marketAPI = ScheduleMCAPI.getMarketAPI();
 *
 * // Aktuellen Preis abrufen
 * double price = marketAPI.getCurrentPrice(item);
 *
 * // Item kaufen (Preis steigt)
 * marketAPI.recordPurchase(item, 10);
 *
 * // Item verkaufen (Preis sinkt)
 * marketAPI.recordSale(item, 5);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IMarketAPI {

    /**
     * Gibt den aktuellen Marktpreis eines Items zurück.
     *
     * @param item Das Item
     * @return Aktueller Preis in Euro
     * @throws IllegalArgumentException wenn item null ist
     */
    double getCurrentPrice(Item item);

    /**
     * Gibt den Basis-Preis eines Items zurück.
     *
     * @param item Das Item
     * @return Basis-Preis ohne dynamische Anpassung
     * @throws IllegalArgumentException wenn item null ist
     */
    double getBasePrice(Item item);

    /**
     * Registriert einen Kauf (erhöht Nachfrage, steigert Preis).
     *
     * @param item Das gekaufte Item
     * @param amount Die Menge
     * @throws IllegalArgumentException wenn item null oder amount < 1
     */
    void recordPurchase(Item item, int amount);

    /**
     * Registriert einen Verkauf (erhöht Angebot, senkt Preis).
     *
     * @param item Das verkaufte Item
     * @param amount Die Menge
     * @throws IllegalArgumentException wenn item null oder amount < 1
     */
    void recordSale(Item item, int amount);

    /**
     * Gibt den Preismultiplikator zurück.
     * <p>
     * 1.0 = Basis-Preis, 1.5 = 50% teurer, 0.8 = 20% günstiger
     *
     * @param item Das Item
     * @return Preis-Multiplikator (typisch 0.5 - 2.0)
     * @throws IllegalArgumentException wenn item null ist
     */
    double getPriceMultiplier(Item item);

    /**
     * Gibt die aktuelle Nachfrage zurück.
     *
     * @param item Das Item
     * @return Nachfrage-Level (0-100)
     * @throws IllegalArgumentException wenn item null ist
     */
    int getDemandLevel(Item item);

    /**
     * Gibt das aktuelle Angebot zurück.
     *
     * @param item Das Item
     * @return Angebots-Level (0-100)
     * @throws IllegalArgumentException wenn item null ist
     */
    int getSupplyLevel(Item item);

    /**
     * Gibt alle Marktpreise zurück.
     *
     * @return Map von Item zu Preis
     */
    Map<Item, Double> getAllPrices();

    /**
     * Setzt den Basis-Preis eines Items (Admin-Funktion).
     *
     * @param item Das Item
     * @param basePrice Der neue Basis-Preis
     * @throws IllegalArgumentException wenn item null oder basePrice < 0
     */
    void setBasePrice(Item item, double basePrice);

    /**
     * Setzt Markt-Daten zurück (Admin-Funktion).
     *
     * @param item Das Item (null für alle Items)
     */
    void resetMarketData(@Nullable Item item);

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns items sorted by price (highest first).
     *
     * @param limit Maximum number of entries
     * @return Sorted list of Item-Price pairs
     * @throws IllegalArgumentException if limit < 1
     * @since 3.2.0
     */
    java.util.List<java.util.Map.Entry<Item, Double>> getTopPricedItems(int limit);

    /**
     * Returns items sorted by demand (highest first).
     *
     * @param limit Maximum number of entries
     * @return Sorted list of Item-Demand pairs
     * @throws IllegalArgumentException if limit < 1
     * @since 3.2.0
     */
    java.util.List<java.util.Map.Entry<Item, Integer>> getTopDemandItems(int limit);

    /**
     * Checks if an item has market data.
     *
     * @param item The item
     * @return true if market data exists
     * @throws IllegalArgumentException if item is null
     * @since 3.2.0
     */
    boolean hasMarketData(Item item);

    /**
     * Returns the number of tracked items in the market.
     *
     * @return Count of items with market data
     * @since 3.2.0
     */
    int getTrackedItemCount();

    /**
     * Resets all market data for all items.
     *
     * @since 3.2.0
     */
    void resetAllMarketData();
}
