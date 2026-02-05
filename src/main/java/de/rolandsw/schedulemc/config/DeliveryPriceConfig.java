package de.rolandsw.schedulemc.config;

import de.rolandsw.schedulemc.economy.EconomyController;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Konfiguration für Lieferpreise
 * Wie viel kostet es, 1 Item zu liefern?
 */
public class DeliveryPriceConfig {

    private static final Map<Item, Integer> PRICES = new ConcurrentHashMap<>();
    private static int defaultPrice = 5;

    static {
        // Grundnahrungsmittel - billig
        PRICES.put(Items.WHEAT, 2);
        PRICES.put(Items.CARROT, 2);
        PRICES.put(Items.POTATO, 2);
        PRICES.put(Items.BEETROOT, 2);
        PRICES.put(Items.APPLE, 3);
        PRICES.put(Items.BREAD, 5);

        // Fleisch
        PRICES.put(Items.BEEF, 8);
        PRICES.put(Items.PORKCHOP, 8);
        PRICES.put(Items.CHICKEN, 6);
        PRICES.put(Items.MUTTON, 7);
        PRICES.put(Items.COOKED_BEEF, 10);
        PRICES.put(Items.COOKED_PORKCHOP, 10);

        // Metalle
        PRICES.put(Items.IRON_INGOT, 15);
        PRICES.put(Items.GOLD_INGOT, 25);
        PRICES.put(Items.COPPER_INGOT, 8);

        // Edelsteine
        PRICES.put(Items.DIAMOND, 100);
        PRICES.put(Items.EMERALD, 80);
        PRICES.put(Items.LAPIS_LAZULI, 10);

        // Holz
        PRICES.put(Items.OAK_LOG, 3);
        PRICES.put(Items.BIRCH_LOG, 3);
        PRICES.put(Items.SPRUCE_LOG, 3);
        PRICES.put(Items.JUNGLE_LOG, 4);
        PRICES.put(Items.ACACIA_LOG, 4);
        PRICES.put(Items.DARK_OAK_LOG, 4);

        // Stein & Baumaterialien
        PRICES.put(Items.COBBLESTONE, 1);
        PRICES.put(Items.STONE, 2);
        PRICES.put(Items.STONE_BRICKS, 3);
        PRICES.put(Items.GLASS, 2);

        // Werkzeuge & Waffen (teurer)
        PRICES.put(Items.IRON_SWORD, 50);
        PRICES.put(Items.IRON_PICKAXE, 60);
        PRICES.put(Items.DIAMOND_SWORD, 300);
        PRICES.put(Items.DIAMOND_PICKAXE, 400);
    }

    /**
     * Setzt den Default-Preis aus der Config
     */
    public static void setDefaultPrice(int price) {
        defaultPrice = price;
    }

    /**
     * Gibt den statischen Basis-Lieferpreis für ein Item zurück.
     * Nutze getDynamicPrice() für den UDPS-angepassten Preis.
     */
    public static int getBasePrice(Item item) {
        return PRICES.getOrDefault(item, defaultPrice);
    }

    /**
     * Gibt Lieferpreis für ein Item zurück.
     * @deprecated Nutze {@link #getDynamicPrice(Item)} für UDPS-basierte Preise
     */
    @Deprecated
    public static int getPrice(Item item) {
        return PRICES.getOrDefault(item, defaultPrice);
    }

    /**
     * Gibt den dynamischen Lieferpreis via UDPS zurück (1 Stück).
     * Berücksichtigt Wirtschaftszyklus und Inflation.
     * Fallback auf statischen Basispreis bei Fehler.
     *
     * @param item Das zu liefernde Item
     * @return Dynamischer Lieferpreis pro Stück
     */
    public static double getDynamicPrice(Item item) {
        int basePrice = getBasePrice(item);
        try {
            return EconomyController.getInstance().getDeliveryPrice(basePrice, 1);
        } catch (Exception e) {
            return basePrice;
        }
    }

    /**
     * Gibt den dynamischen Lieferpreis via UDPS für eine bestimmte Menge zurück.
     *
     * @param item   Das zu liefernde Item
     * @param amount Menge
     * @return Dynamischer Gesamt-Lieferpreis
     */
    public static double getDynamicPrice(Item item, int amount) {
        int basePrice = getBasePrice(item);
        try {
            return EconomyController.getInstance().getDeliveryPrice(basePrice, amount);
        } catch (Exception e) {
            return (double) basePrice * amount;
        }
    }

    /**
     * Setzt Lieferpreis für ein Item
     */
    public static void setPrice(Item item, int price) {
        PRICES.put(item, price);
    }

    /**
     * Gibt alle konfigurierten Basis-Preise zurück
     */
    public static Map<Item, Integer> getAllPrices() {
        return new HashMap<>(PRICES);
    }
}
