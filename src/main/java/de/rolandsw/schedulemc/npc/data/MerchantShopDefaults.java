package de.rolandsw.schedulemc.npc.data;

import de.rolandsw.schedulemc.vehicle.items.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Definiert Standard-Shop-Items für jede Verkäufer-Kategorie und Service-Kategorie
 */
public class MerchantShopDefaults {

    /**
     * Fügt Standard-Items zum Shop hinzu basierend auf der Kategorie
     */
    public static void setupShopItems(NPCData npcData, MerchantCategory category) {
        NPCData.ShopInventory shop = npcData.getBuyShop();

        switch (category) {
            case BAUMARKT:
                setupBaumarktShop(shop);
                break;
            case WAFFENHAENDLER:
                setupWaffenhaendlerShop(shop);
                break;
            case TANKSTELLE:
                setupTankstelleShop(shop);
                break;
            case LEBENSMITTEL:
                setupLebensmittelShop(shop);
                break;
            case PERSONALMANAGEMENT:
                setupPersonalmanagementShop(shop);
                break;
            case ILLEGALER_HAENDLER:
                setupIllegalerHaendlerShop(shop);
                break;
            case AUTOHAENDLER:
                setupAutohaendlerShop(shop);
                break;
        }
    }

    /**
     * Fügt Standard-Items zum Shop hinzu basierend auf der Service-Kategorie
     */
    public static void setupServiceShopItems(NPCData npcData, ServiceCategory category) {
        NPCData.ShopInventory shop = npcData.getBuyShop();

        switch (category) {
            case ABSCHLEPPDIENST:
                setupAbschleppdienstShop(shop);
                break;
            case PANNENHILFE:
                setupPannenhilfeShop(shop);
                break;
            case TAXI:
                setupTaxiShop(shop);
                break;
            case NOTDIENST:
                setupNotdienstShop(shop);
                break;
        }
    }

    private static void setupBaumarktShop(NPCData.ShopInventory shop) {
        // Baumaterialien - ALLE als Lager-Items (unlimited=false)
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.OAK_PLANKS, 16), 50, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.STONE, 32), 30, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.GLASS, 16), 40, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_INGOT, 8), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.REDSTONE, 16), 80, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.GLOWSTONE, 8), 60, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.LADDER, 8), 25, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.TORCH, 32), 20, false, 0));
    }

    private static void setupWaffenhaendlerShop(NPCData.ShopInventory shop) {
        // Waffen & Rüstung - ALLE als Lager-Items (unlimited=false)
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_SWORD, 1), 150, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_AXE, 1), 140, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.BOW, 1), 120, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.ARROW, 64), 50, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_HELMET, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_CHESTPLATE, 1), 160, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_LEGGINGS, 1), 140, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_BOOTS, 1), 80, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.SHIELD, 1), 90, false, 0));
    }

    private static void setupTankstelleShop(NPCData.ShopInventory shop) {
        // Fortbewegung & Ressourcen - ALLE als Lager-Items (unlimited=false)
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COAL, 16), 40, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.MINECART, 1), 150, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.POWERED_RAIL, 16), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.RAIL, 32), 60, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.BUCKET, 1), 80, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.WATER_BUCKET, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.LAVA_BUCKET, 1), 200, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.SADDLE, 1), 250, false, 0));
    }

    private static void setupLebensmittelShop(NPCData.ShopInventory shop) {
        // Nahrung - ALLE als Lager-Items (unlimited=false)
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.BREAD, 8), 30, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COOKED_BEEF, 8), 60, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COOKED_PORKCHOP, 8), 55, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.APPLE, 16), 20, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.GOLDEN_APPLE, 1), 200, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.CARROT, 16), 15, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.POTATO, 16), 15, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.CAKE, 1), 80, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COOKIE, 32), 25, false, 0));
    }

    private static void setupPersonalmanagementShop(NPCData.ShopInventory shop) {
        // Werkzeuge & Arbeitsmaterialien - ALLE als Lager-Items (unlimited=false)
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_PICKAXE, 1), 120, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_SHOVEL, 1), 90, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_HOE, 1), 80, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.SHEARS, 1), 70, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.FISHING_ROD, 1), 60, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COMPASS, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.CLOCK, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.MAP, 1), 50, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.NAME_TAG, 1), 150, false, 0));
    }

    private static void setupIllegalerHaendlerShop(NPCData.ShopInventory shop) {
        // Seltene & illegale Items - ALLE als Lager-Items (unlimited=false)
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.DIAMOND, 1), 500, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.EMERALD, 1), 400, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.NETHERITE_SCRAP, 1), 800, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.ENDER_PEARL, 4), 300, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.BLAZE_ROD, 4), 250, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.GHAST_TEAR, 2), 350, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.ENCHANTED_BOOK, 1), 600, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.TNT, 8), 400, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.END_CRYSTAL, 1), 1000, false, 0));
    }

    private static void setupAutohaendlerShop(NPCData.ShopInventory shop) {
        // Fahrzeuge - ALLE als Lager-Items (unlimited=false)
        // Diese werden NICHT ins Inventar gegeben, sondern direkt gespawnt
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(ModItems.SPAWN_VEHICLE_OAK.get(), 1), 5000, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(ModItems.SPAWN_VEHICLE_BIG_OAK.get(), 1), 7500, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(ModItems.SPAWN_VEHICLE_WHITE_TRANSPORTER.get(), 1), 12000, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(ModItems.SPAWN_VEHICLE_WHITE_SUV.get(), 1), 10000, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(ModItems.SPAWN_VEHICLE_WHITE_SPORT.get(), 1), 15000, false, 0));
    }

    // === SERVICE CATEGORY SHOPS ===

    private static void setupAbschleppdienstShop(NPCData.ShopInventory shop) {
        // Abschleppdienst - Reparatur- & Notfallitems
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_INGOT, 8), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.REDSTONE, 16), 80, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.BUCKET, 1), 80, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.WATER_BUCKET, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.TORCH, 32), 20, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COAL, 16), 40, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_PICKAXE, 1), 120, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_SHOVEL, 1), 90, false, 0));
    }

    private static void setupPannenhilfeShop(NPCData.ShopInventory shop) {
        // Pannenhilfe - Werkzeuge & Reparaturmaterial
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_PICKAXE, 1), 120, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_SHOVEL, 1), 90, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.IRON_AXE, 1), 140, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.SHEARS, 1), 70, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.BUCKET, 1), 80, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COAL, 16), 40, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.TORCH, 32), 20, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.LADDER, 8), 25, false, 0));
    }

    private static void setupTaxiShop(NPCData.ShopInventory shop) {
        // Taxi - Reise & Komfort-Items
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COMPASS, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.CLOCK, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.MAP, 1), 50, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.BREAD, 8), 30, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.WATER_BUCKET, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.LEATHER_BOOTS, 1), 60, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.SADDLE, 1), 250, false, 0));
    }

    private static void setupNotdienstShop(NPCData.ShopInventory shop) {
        // Notdienst - Medizin & Notfall-Items
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.GOLDEN_APPLE, 1), 200, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.BREAD, 8), 30, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COOKED_BEEF, 8), 60, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.POTION, 1), 150, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.TORCH, 32), 20, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.COMPASS, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.WATER_BUCKET, 1), 100, false, 0));
        shop.addEntry(new NPCData.ShopEntry(new ItemStack(Items.ENDER_PEARL, 4), 300, false, 0));
    }
}
