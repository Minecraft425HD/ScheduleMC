package de.rolandsw.schedulemc.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.data.ShopItem;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet das Shop-System mit Items und Preisen
 *
 * OPTIMIERT: Thread-safe durch ConcurrentHashMap
 */
public class ShopManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File file = new File("config/plotmod_shop.json");
    private static final Gson gson = GsonHelper.get();
    private static final Map<String, ShopItem> items = new ConcurrentHashMap<>();
    private static boolean needsSave = false;
    
    /**
     * Lädt Shop-Items
     */
    public static void load() {
        if (!file.exists()) {
            LOGGER.info("Keine Shop-Datei gefunden, erstelle Standard-Items");
            createDefaultItems();
            save();
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, ShopItem>>(){}.getType();
            Map<String, ShopItem> loaded = gson.fromJson(reader, type);
            
            if (loaded != null) {
                items.clear();
                items.putAll(loaded);
                LOGGER.info("Shop-Items geladen: {} Items", items.size());
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Laden der Shop-Items", e);
        }
    }
    
    /**
     * Speichert Shop-Items
     */
    public static void save() {
        try {
            file.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(items, writer);
                needsSave = false;
                LOGGER.info("Shop-Items gespeichert: {} Items", items.size());
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Shop-Items", e);
        }
    }
    
    public static void saveIfNeeded() {
        if (needsSave) save();
    }
    
    private static void markDirty() {
        needsSave = true;
    }
    
    /**
     * Erstellt Standard-Shop-Items
     */
    private static void createDefaultItems() {
        // Baumaterialien
        addItem("minecraft:stone", 1.0, 0.5);
        addItem("minecraft:cobblestone", 0.5, 0.25);
        addItem("minecraft:dirt", 0.1, 0.05);
        addItem("minecraft:oak_log", 2.0, 1.0);
        addItem("minecraft:oak_planks", 0.5, 0.25);
        addItem("minecraft:glass", 2.0, 1.0);
        addItem("minecraft:white_wool", 3.0, 1.5);
        
        // Erze & Materialien
        addItem("minecraft:coal", 5.0, 2.5);
        addItem("minecraft:iron_ingot", 10.0, 5.0);
        addItem("minecraft:gold_ingot", 20.0, 10.0);
        addItem("minecraft:diamond", 100.0, 50.0);
        addItem("minecraft:emerald", 150.0, 75.0);
        addItem("minecraft:netherite_ingot", 500.0, 250.0);
        
        // Werkzeuge (teurer)
        addItem("minecraft:iron_pickaxe", 50.0, 25.0);
        addItem("minecraft:iron_axe", 50.0, 25.0);
        addItem("minecraft:iron_shovel", 40.0, 20.0);
        addItem("minecraft:diamond_pickaxe", 300.0, 150.0);
        
        // Nahrung
        addItem("minecraft:bread", 5.0, 2.5);
        addItem("minecraft:cooked_beef", 8.0, 4.0);
        addItem("minecraft:golden_apple", 50.0, 25.0);
        
        // Redstone & Tech
        addItem("minecraft:redstone", 10.0, 5.0);
        addItem("minecraft:glowstone", 15.0, 7.5);
        addItem("minecraft:piston", 20.0, 10.0);
        
        LOGGER.info("Standard-Shop-Items erstellt");
    }
    
    /**
     * Fügt Item zum Shop hinzu
     */
    public static void addItem(String itemId, double buyPrice, double sellPrice) {
        ShopItem item = new ShopItem(itemId, buyPrice, sellPrice);
        items.put(itemId, item);
        markDirty();
    }
    
    /**
     * Entfernt Item aus Shop
     */
    public static void removeItem(String itemId) {
        items.remove(itemId);
        markDirty();
    }
    
    /**
     * Gibt ShopItem zurück
     */
    public static ShopItem getItem(String itemId) {
        return items.get(itemId);
    }
    
    /**
     * Prüft ob Item im Shop ist
     */
    public static boolean hasItem(String itemId) {
        return items.containsKey(itemId);
    }
    
    /**
     * Gibt alle Shop-Items zurück
     */
    public static Collection<ShopItem> getAllItems() {
        return new ArrayList<>(items.values());
    }
    
    /**
     * Gibt kaufbare Items zurück
     */
    public static List<ShopItem> getBuyableItems() {
        List<ShopItem> buyable = new ArrayList<>();
        for (ShopItem item : items.values()) {
            if (item.canBuy() && item.isAvailable()) {
                buyable.add(item);
            }
        }
        return buyable;
    }
    
    /**
     * Gibt verkaufbare Items zurück
     */
    public static List<ShopItem> getSellableItems() {
        List<ShopItem> sellable = new ArrayList<>();
        for (ShopItem item : items.values()) {
            if (item.canSell() && item.isAvailable()) {
                sellable.add(item);
            }
        }
        return sellable;
    }
    
    /**
     * Berechnet Kaufpreis
     */
    public static double calculateBuyPrice(String itemId, int amount) {
        ShopItem item = getItem(itemId);
        if (item == null) return 0;
        return item.getTotalBuyPrice(amount);
    }
    
    /**
     * Berechnet Verkaufspreis
     */
    public static double calculateSellPrice(String itemId, int amount) {
        ShopItem item = getItem(itemId);
        if (item == null) return 0;
        return item.getTotalSellPrice(amount);
    }
    
    /**
     * Aktualisiert Preise (Admin)
     */
    public static void updatePrices(String itemId, double buyPrice, double sellPrice) {
        ShopItem item = getItem(itemId);
        if (item != null) {
            item.setBuyPrice(buyPrice);
            item.setSellPrice(sellPrice);
            markDirty();
            LOGGER.info("Preise aktualisiert für: {}", itemId);
        }
    }
    
    /**
     * Setzt Lagerbestand (Admin)
     */
    public static void setStock(String itemId, int stock) {
        ShopItem item = getItem(itemId);
        if (item != null) {
            item.setStock(stock);
            markDirty();
        }
    }
    
    /**
     * Gibt Anzahl der Shop-Items zurück
     */
    public static int getItemCount() {
        return items.size();
    }
}
