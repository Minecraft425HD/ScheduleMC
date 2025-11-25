package de.rolandsw.schedulemc.npc.data;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Definiert Standard-Shop-Items für jede Verkäufer-Kategorie
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
        }
    }

    private static void setupBaumarktShop(NPCData.ShopInventory shop) {
        // Baumaterialien
        shop.addEntry(new ItemStack(Items.OAK_PLANKS, 16), 50);
        shop.addEntry(new ItemStack(Items.STONE, 32), 30);
        shop.addEntry(new ItemStack(Items.GLASS, 16), 40);
        shop.addEntry(new ItemStack(Items.IRON_INGOT, 8), 100);
        shop.addEntry(new ItemStack(Items.REDSTONE, 16), 80);
        shop.addEntry(new ItemStack(Items.GLOWSTONE, 8), 60);
        shop.addEntry(new ItemStack(Items.LADDER, 8), 25);
        shop.addEntry(new ItemStack(Items.TORCH, 32), 20);
    }

    private static void setupWaffenhaendlerShop(NPCData.ShopInventory shop) {
        // Waffen & Rüstung
        shop.addEntry(new ItemStack(Items.IRON_SWORD, 1), 150);
        shop.addEntry(new ItemStack(Items.IRON_AXE, 1), 140);
        shop.addEntry(new ItemStack(Items.BOW, 1), 120);
        shop.addEntry(new ItemStack(Items.ARROW, 64), 50);
        shop.addEntry(new ItemStack(Items.IRON_HELMET, 1), 100);
        shop.addEntry(new ItemStack(Items.IRON_CHESTPLATE, 1), 160);
        shop.addEntry(new ItemStack(Items.IRON_LEGGINGS, 1), 140);
        shop.addEntry(new ItemStack(Items.IRON_BOOTS, 1), 80);
        shop.addEntry(new ItemStack(Items.SHIELD, 1), 90);
    }

    private static void setupTankstelleShop(NPCData.ShopInventory shop) {
        // Fortbewegung & Ressourcen
        shop.addEntry(new ItemStack(Items.COAL, 16), 40);
        shop.addEntry(new ItemStack(Items.MINECART, 1), 150);
        shop.addEntry(new ItemStack(Items.POWERED_RAIL, 16), 100);
        shop.addEntry(new ItemStack(Items.RAIL, 32), 60);
        shop.addEntry(new ItemStack(Items.BUCKET, 1), 80);
        shop.addEntry(new ItemStack(Items.WATER_BUCKET, 1), 100);
        shop.addEntry(new ItemStack(Items.LAVA_BUCKET, 1), 200);
        shop.addEntry(new ItemStack(Items.SADDLE, 1), 250);
    }

    private static void setupLebensmittelShop(NPCData.ShopInventory shop) {
        // Nahrung
        shop.addEntry(new ItemStack(Items.BREAD, 8), 30);
        shop.addEntry(new ItemStack(Items.COOKED_BEEF, 8), 60);
        shop.addEntry(new ItemStack(Items.COOKED_PORKCHOP, 8), 55);
        shop.addEntry(new ItemStack(Items.APPLE, 16), 20);
        shop.addEntry(new ItemStack(Items.GOLDEN_APPLE, 1), 200);
        shop.addEntry(new ItemStack(Items.CARROT, 16), 15);
        shop.addEntry(new ItemStack(Items.POTATO, 16), 15);
        shop.addEntry(new ItemStack(Items.CAKE, 1), 80);
        shop.addEntry(new ItemStack(Items.COOKIE, 32), 25);
    }

    private static void setupPersonalmanagementShop(NPCData.ShopInventory shop) {
        // Werkzeuge & Arbeitsmaterialien
        shop.addEntry(new ItemStack(Items.IRON_PICKAXE, 1), 120);
        shop.addEntry(new ItemStack(Items.IRON_SHOVEL, 1), 90);
        shop.addEntry(new ItemStack(Items.IRON_HOE, 1), 80);
        shop.addEntry(new ItemStack(Items.SHEARS, 1), 70);
        shop.addEntry(new ItemStack(Items.FISHING_ROD, 1), 60);
        shop.addEntry(new ItemStack(Items.COMPASS, 1), 100);
        shop.addEntry(new ItemStack(Items.CLOCK, 1), 100);
        shop.addEntry(new ItemStack(Items.MAP, 1), 50);
        shop.addEntry(new ItemStack(Items.NAME_TAG, 1), 150);
    }

    private static void setupIllegalerHaendlerShop(NPCData.ShopInventory shop) {
        // Seltene & illegale Items
        shop.addEntry(new ItemStack(Items.DIAMOND, 1), 500);
        shop.addEntry(new ItemStack(Items.EMERALD, 1), 400);
        shop.addEntry(new ItemStack(Items.NETHERITE_SCRAP, 1), 800);
        shop.addEntry(new ItemStack(Items.ENDER_PEARL, 4), 300);
        shop.addEntry(new ItemStack(Items.BLAZE_ROD, 4), 250);
        shop.addEntry(new ItemStack(Items.GHAST_TEAR, 2), 350);
        shop.addEntry(new ItemStack(Items.ENCHANTED_BOOK, 1), 600);
        shop.addEntry(new ItemStack(Items.TNT, 8), 400);
        shop.addEntry(new ItemStack(Items.END_CRYSTAL, 1), 1000);
    }
}
