package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseManager;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brücke zwischen dem Warehouse-System und dem Wirtschaftssystem.
 *
 * Liest Füllstände aller Warehouses und berechnet:
 * - Durchschnittliche Füllrate pro Item-Typ
 * - Warehouse-basierte Preis-Multiplikatoren
 * - Angebot/Nachfrage-Signale für den EconomyController
 *
 * Wird periodisch (alle 5 Minuten) aktualisiert.
 */
public class WarehouseMarketBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Singleton
    private static volatile WarehouseMarketBridge instance;

    /**
     * Gecachte Füllstände pro Item (Item-Registry-Name → Füllrate 0.0-1.0)
     */
    private final ConcurrentHashMap<String, Double> itemFillRates = new ConcurrentHashMap<>();

    /**
     * Gecachte Stock-Levels pro Item
     */
    private final ConcurrentHashMap<String, WarehouseStockLevel> itemStockLevels = new ConcurrentHashMap<>();

    /**
     * Gesamter Warehouse-Stock pro Item
     */
    private final ConcurrentHashMap<String, Integer> totalItemStock = new ConcurrentHashMap<>();

    /**
     * Gesamte Warehouse-Kapazität pro Item
     */
    private final ConcurrentHashMap<String, Integer> totalItemCapacity = new ConcurrentHashMap<>();

    @Nullable
    private MinecraftServer server;

    private WarehouseMarketBridge() {}

    public static WarehouseMarketBridge getInstance() {
        WarehouseMarketBridge localRef = instance;
        if (localRef == null) {
            synchronized (WarehouseMarketBridge.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new WarehouseMarketBridge();
                }
            }
        }
        return localRef;
    }

    public void setServer(@Nullable MinecraftServer server) {
        this.server = server;
    }

    // ═══════════════════════════════════════════════════════════
    // DATEN-SAMMLUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Scannt alle Warehouses und aktualisiert die Füllstands-Daten.
     * Sollte alle 5 Minuten aufgerufen werden.
     */
    public void updateWarehouseData() {
        if (server == null) return;

        // Temporäre Zähler
        ConcurrentHashMap<String, Integer> tempStock = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> tempCapacity = new ConcurrentHashMap<>();

        Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
        int scannedCount = 0;

        for (Map.Entry<String, Set<BlockPos>> entry : allWarehouses.entrySet()) {
            String levelKey = entry.getKey();
            ServerLevel level = findLevel(levelKey);
            if (level == null) continue;

            for (BlockPos pos : entry.getValue()) {
                if (!level.isLoaded(pos)) continue;

                BlockEntity be = level.getBlockEntity(pos);
                if (!(be instanceof WarehouseBlockEntity warehouse)) continue;

                scanWarehouseSlots(warehouse, tempStock, tempCapacity);
                scannedCount++;
            }
        }

        // Daten übernehmen und Fill-Rates berechnen
        totalItemStock.clear();
        totalItemStock.putAll(tempStock);
        totalItemCapacity.clear();
        totalItemCapacity.putAll(tempCapacity);

        // Fill-Rates und Stock-Levels berechnen
        itemFillRates.clear();
        itemStockLevels.clear();

        for (String itemKey : tempCapacity.keySet()) {
            int stock = tempStock.getOrDefault(itemKey, 0);
            int capacity = tempCapacity.getOrDefault(itemKey, 1);

            double fillRate = (double) stock / Math.max(1, capacity);
            itemFillRates.put(itemKey, fillRate);
            itemStockLevels.put(itemKey, WarehouseStockLevel.fromFillPercent(fillRate));
        }

        LOGGER.debug("Warehouse data updated: scanned {} warehouses, tracking {} items",
                scannedCount, itemFillRates.size());
    }

    /**
     * Scannt die Slots eines einzelnen Warehouses.
     */
    private void scanWarehouseSlots(WarehouseBlockEntity warehouse,
                                     ConcurrentHashMap<String, Integer> tempStock,
                                     ConcurrentHashMap<String, Integer> tempCapacity) {
        // Wir müssen über die Slots iterieren - da WarehouseBlockEntity die Slots
        // als Array hat, brauchen wir einen Getter oder Zugriff auf die Slot-Daten
        // Die Slot-Daten sind über getStockInfo() oder ähnlich zugänglich
        // Wir nutzen die bereits verfügbare API
        for (int i = 0; i < getSlotCount(warehouse); i++) {
            WarehouseSlot slot = getSlot(warehouse, i);
            if (slot == null || slot.isEmpty()) continue;

            Item item = slot.getAllowedItem();
            if (item == null) continue;

            String itemKey = item.getDescriptionId();
            tempStock.merge(itemKey, slot.getStock(), Integer::sum);
            tempCapacity.merge(itemKey, slot.getMaxCapacity(), Integer::sum);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PREIS-MULTIPLIKATOREN
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt den Warehouse-basierten Preis-Multiplikator für ein Item zurück.
     *
     * @param itemKey Item-Identifier (Description ID)
     * @return Multiplikator (0.75 = günstig bei Überangebot, 1.4 = teuer bei Knappheit)
     */
    public double getWarehousePriceMultiplier(String itemKey) {
        WarehouseStockLevel level = itemStockLevels.get(itemKey);
        if (level == null) {
            return 1.0; // Kein Warehouse-Daten = neutraler Preis
        }
        return level.getPriceMultiplier();
    }

    /**
     * Gibt den Füllstand für ein Item zurück.
     *
     * @param itemKey Item-Identifier
     * @return Füllrate (0.0-1.0), oder -1 wenn kein Warehouse-Daten vorhanden
     */
    public double getItemFillRate(String itemKey) {
        return itemFillRates.getOrDefault(itemKey, -1.0);
    }

    /**
     * Gibt das Stock-Level für ein Item zurück.
     */
    @Nullable
    public WarehouseStockLevel getItemStockLevel(String itemKey) {
        return itemStockLevels.get(itemKey);
    }

    /**
     * Gibt den Gesamtbestand eines Items in allen Warehouses zurück.
     */
    public int getTotalStock(String itemKey) {
        return totalItemStock.getOrDefault(itemKey, 0);
    }

    /**
     * Gibt die Gesamtkapazität eines Items in allen Warehouses zurück.
     */
    public int getTotalCapacity(String itemKey) {
        return totalItemCapacity.getOrDefault(itemKey, 0);
    }

    // ═══════════════════════════════════════════════════════════
    // S&D SIGNALE
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet ein Supply-Signal basierend auf Warehouse-Füllstand.
     * Hoher Füllstand = hohes Angebot.
     *
     * @param itemKey Item-Identifier
     * @return Supply-Wert (0-200, 100 = neutral)
     */
    public int getSupplySignal(String itemKey) {
        double fillRate = itemFillRates.getOrDefault(itemKey, 0.5);
        return (int) (fillRate * 200);
    }

    /**
     * Berechnet ein Demand-Signal basierend auf Warehouse-Verbrauch.
     * Schnell sinkender Bestand = hohe Nachfrage.
     *
     * @param itemKey Item-Identifier
     * @return Demand-Wert (0-200, 100 = neutral)
     */
    public int getDemandSignal(String itemKey) {
        double fillRate = itemFillRates.getOrDefault(itemKey, 0.5);
        // Inverse: niedrige Füllrate = hohe Nachfrage
        return (int) ((1.0 - fillRate) * 200);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════

    @Nullable
    private ServerLevel findLevel(String levelKey) {
        if (server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().toString().equals(levelKey)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Returns the slot count of a warehouse via its public API.
     */
    private int getSlotCount(WarehouseBlockEntity warehouse) {
        return warehouse.getSlotCount();
    }

    /**
     * Returns a specific slot from a warehouse via its public API.
     */
    @Nullable
    private WarehouseSlot getSlot(WarehouseBlockEntity warehouse, int index) {
        return warehouse.getSlot(index);
    }

    /**
     * Finds a WarehouseSlot containing a given item across all warehouses.
     *
     * @param item the item to search for
     * @return the first matching WarehouseSlot, or null if not found
     */
    @Nullable
    public WarehouseSlot findSlotForItem(Item item) {
        if (server == null) return null;

        Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();

        for (Map.Entry<String, Set<BlockPos>> entry : allWarehouses.entrySet()) {
            String levelKey = entry.getKey();
            ServerLevel level = findLevel(levelKey);
            if (level == null) continue;

            for (BlockPos pos : entry.getValue()) {
                if (!level.isLoaded(pos)) continue;

                BlockEntity be = level.getBlockEntity(pos);
                if (!(be instanceof WarehouseBlockEntity warehouse)) continue;

                for (int i = 0; i < warehouse.getSlotCount(); i++) {
                    WarehouseSlot slot = warehouse.getSlot(i);
                    if (slot != null && slot.getAllowedItem() == item) {
                        return slot;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gibt eine formatierte Warehouse-Markt-Übersicht zurück.
     */
    public String getMarketOverview() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6═══ Warehouse-Markt ═══\n");

        if (itemFillRates.isEmpty()) {
            sb.append("§7Keine Warehouse-Daten verfügbar\n");
            return sb.toString();
        }

        for (Map.Entry<String, WarehouseStockLevel> entry : itemStockLevels.entrySet()) {
            String itemKey = entry.getKey();
            WarehouseStockLevel level = entry.getValue();
            int stock = totalItemStock.getOrDefault(itemKey, 0);
            int capacity = totalItemCapacity.getOrDefault(itemKey, 0);

            sb.append(String.format("§f%s: %s §7(%d/%d) §7Preis: §f×%.2f\n",
                    itemKey, level.getFormattedName(), stock, capacity, level.getPriceMultiplier()));
        }

        return sb.toString();
    }
}
