package de.rolandsw.schedulemc.warehouse;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Konfiguration für Lieferpreise
 * Wie viel kostet es, 1 Item zu liefern?
 */
public class DeliveryPriceConfig {

    private static final Map<Item, Integer> PRICES = new HashMap<>();

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

        // Default für alles andere: 5€
    }

    /**
     * Gibt Lieferpreis für ein Item zurück
     */
    public static int getPrice(Item item) {
        return PRICES.getOrDefault(item, 5); // Default: 5€
    }

    /**
     * Setzt Lieferpreis für ein Item
     */
    public static void setPrice(Item item, int price) {
        PRICES.put(item, price);
    }

    /**
     * Gibt alle konfigurierten Preise zurück
     */
    public static Map<Item, Integer> getAllPrices() {
        return new HashMap<>(PRICES);
    }
}
