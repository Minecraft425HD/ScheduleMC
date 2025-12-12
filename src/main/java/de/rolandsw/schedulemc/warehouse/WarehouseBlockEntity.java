package de.rolandsw.schedulemc.warehouse;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.DeliveryPriceConfig;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.StateAccount;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.economy.ShopAccount;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Warehouse BlockEntity - Speichert Items für Shops
 *
 * Features:
 * - Config-basierte Slot-Anzahl und Kapazität
 * - Automatische Lieferung alle 3 Tage (Staatskasse zahlt)
 * - Verknüpfung mit Shop-Konto für Expense-Tracking
 * - Unterstützung für mehrere Verkäufer-NPCs
 */
public class WarehouseBlockEntity extends BlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int EXPENSE_RETENTION_DAYS = 30; // Behalte Ausgaben für 30 Tage

    private WarehouseSlot[] slots;
    private List<UUID> linkedSellers = new ArrayList<>();
    private long lastDeliveryDay = -1; // Tag der letzten Lieferung (nicht absolute ticks!)
    @Nullable
    private String shopId; // Referenz zum Shop-Konto
    private List<ExpenseEntry> expenses = new ArrayList<>(); // Ausgaben-Historie

    public WarehouseBlockEntity(BlockPos pos, BlockState state) {
        super(WarehouseBlocks.WAREHOUSE_BLOCK_ENTITY.get(), pos, state);

        int slotCount = ModConfigHandler.COMMON.WAREHOUSE_SLOT_COUNT.get();
        int capacity = ModConfigHandler.COMMON.WAREHOUSE_MAX_CAPACITY_PER_SLOT.get();

        this.slots = new WarehouseSlot[slotCount];
        for (int i = 0; i < slotCount; i++) {
            slots[i] = new WarehouseSlot(capacity);
        }
    }

    /**
     * Initialisiert das Warehouse nach dem Platzieren
     * Setzt lastDeliveryDay auf aktuellen Tag, damit erste Lieferung nach Interval erfolgt
     */
    public void initializeOnPlace(Level level) {
        this.lastDeliveryDay = level.getDayTime() / 24000L;
        LOGGER.info("Warehouse initialized at {}, lastDeliveryDay set to {}", worldPosition.toShortString(), lastDeliveryDay);
        setChanged();
    }

    // ═══════════════════════════════════════════════════════════
    // ITEM MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt Items hinzu (Admin-Befüllung)
     */
    public int addItem(Item item, int amount) {
        for (WarehouseSlot slot : slots) {
            if (slot.canAccept(item) && !slot.isFull()) {
                int added = slot.addStock(item, amount);
                if (added > 0) {
                    setChanged();
                    syncToClient();
                    return added;
                }
            }
        }
        return 0; // Kein Platz
    }

    /**
     * Entfernt Items (Verkauf)
     */
    public int removeItem(Item item, int amount) {
        for (WarehouseSlot slot : slots) {
            if (slot.getAllowedItem() == item) {
                int removed = slot.removeStock(amount);
                if (removed > 0) {
                    setChanged();
                    syncToClient();
                    return removed;
                }
            }
        }
        return 0; // Item nicht gefunden
    }

    /**
     * Prüft ob genug Stock vorhanden ist
     */
    public boolean hasStock(Item item, int amount) {
        return getStock(item) >= amount;
    }

    /**
     * Gibt aktuellen Stock zurück
     */
    public int getStock(Item item) {
        for (WarehouseSlot slot : slots) {
            if (slot.getAllowedItem() == item) {
                return slot.getStock();
            }
        }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════
    // SELLER MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    public void addSeller(UUID sellerId) {
        if (!linkedSellers.contains(sellerId)) {
            linkedSellers.add(sellerId);
            setChanged();
            syncToClient();
        }
    }

    public void removeSeller(UUID sellerId) {
        linkedSellers.remove(sellerId);
        setChanged();
        syncToClient();
    }

    public List<UUID> getLinkedSellers() {
        return new ArrayList<>(linkedSellers);
    }

    // ═══════════════════════════════════════════════════════════
    // SHOP-KONTO
    // ═══════════════════════════════════════════════════════════

    @Nullable
    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
        setChanged();
        syncToClient();
    }

    // ═══════════════════════════════════════════════════════════
    // AUTO-LIEFERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Tick-Methode - NUR für Expense Cleanup
     * Auto-Delivery wird vom WarehouseManager gehandhabt!
     */
    public static void tick(Level level, BlockPos pos, BlockState state, WarehouseBlockEntity be) {
        if (level.isClientSide) return;

        long currentTime = level.getGameTime();

        // Bereinige alte Ausgaben alle 10 Minuten (12000 ticks)
        if (currentTime % 12000 == 0) {
            be.cleanupOldExpenses(currentTime);
        }
    }

    /**
     * Führt Warehouse-Lieferung durch
     * Staatskasse zahlt, Shop-Konto registriert Ausgaben
     */
    public void performDelivery(Level level) {
        LOGGER.info("Warehouse @ {}: performDelivery() aufgerufen, shopId={}", worldPosition.toShortString(), shopId);

        // Berechne Lieferkosten
        int totalCost = 0;
        Map<Item, Integer> toDeliver = new HashMap<>();
        int slotsWithItems = 0;
        int emptySlots = 0;
        int fullSlots = 0;

        for (WarehouseSlot slot : slots) {
            int restockAmount = slot.getRestockAmount();
            if (slot.getAllowedItem() != null) {
                slotsWithItems++;
                if (restockAmount == 0) {
                    fullSlots++;
                }
            } else {
                emptySlots++;
            }

            if (restockAmount > 0 && slot.getAllowedItem() != null) {
                Item item = slot.getAllowedItem();
                int pricePerItem = DeliveryPriceConfig.getPrice(item);
                totalCost += restockAmount * pricePerItem;
                toDeliver.put(item, restockAmount);
                LOGGER.debug("  Slot needs restock: item={}, amount={}, price={}",
                    item.getDescription().getString(), restockAmount, pricePerItem);
            }
        }

        LOGGER.info("Warehouse @ {}: Analysis - slotsWithItems={}, emptySlots={}, fullSlots={}, toDeliver={}, totalCost={}",
            worldPosition.toShortString(), slotsWithItems, emptySlots, fullSlots, toDeliver.size(), totalCost);

        if (totalCost == 0 || toDeliver.isEmpty()) {
            LOGGER.info("Warehouse @ {}: Keine Lieferung notwendig (totalCost={}, toDeliver={})",
                worldPosition.toShortString(), totalCost, toDeliver.size());
            return; // Nichts zu liefern
        }

        // STAATSKASSE ZAHLT!
        if (StateAccount.withdraw(totalCost, "Warehouse-Lieferung @ " + worldPosition.toShortString())) {
            // Liefere Items
            for (Map.Entry<Item, Integer> entry : toDeliver.entrySet()) {
                addItem(entry.getKey(), entry.getValue());
            }

            // Registriere Kosten im Shop-Konto (für 7-Tage-Nettoumsatz)
            if (shopId != null) {
                ShopAccount account = ShopAccountManager.getAccount(shopId);
                if (account != null) {
                    account.addExpense(level, totalCost, "Warehouse-Lieferung");
                }
            }

            // EXPENSE TRACKING: Füge Ausgabe zur Historie hinzu
            addExpense(level.getGameTime(), totalCost, "Auto-Lieferung (" + toDeliver.size() + " Items)");

            LOGGER.info("Warehouse-Lieferung erfolgreich @ {}: {}€ (Staatskasse), {} Items",
                worldPosition.toShortString(), totalCost, toDeliver.size());

            // Benachrichtige nahe Spieler
            final int finalCost = totalCost;
            level.players().stream()
                .filter(player -> player.blockPosition().distSqr(worldPosition) < 2500) // 50 Blöcke
                .forEach(player -> player.sendSystemMessage(
                    Component.literal("§a[Warehouse] Lieferung erhalten! Kosten: " + finalCost + "€")
                ));
        } else {
            LOGGER.warn("Warehouse-Lieferung fehlgeschlagen @ {}: Staatskasse hat nicht genug Geld (benötigt: {}€)",
                worldPosition.toShortString(), totalCost);

            // Benachrichtige nahe Spieler über Fehler
            level.players().stream()
                .filter(player -> player.blockPosition().distSqr(worldPosition) < 2500)
                .forEach(player -> player.sendSystemMessage(
                    Component.literal("§c[Warehouse] Lieferung fehlgeschlagen! Staatskasse leer.")
                ));
        }
    }

    /**
     * Manuelle Lieferung (Admin-Command)
     */
    public void performManualDelivery(Level level) {
        performDelivery(level);

        long currentDay = level.getDayTime() / 24000L;
        lastDeliveryDay = currentDay;
        setChanged();
        syncToClient();
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY & INFO
    // ═══════════════════════════════════════════════════════════

    public WarehouseSlot[] getSlots() {
        return slots;
    }

    public int getEmptySlotCount() {
        int count = 0;
        for (WarehouseSlot slot : slots) {
            if (slot.isEmpty()) count++;
        }
        return count;
    }

    public int getTotalItems() {
        int total = 0;
        for (WarehouseSlot slot : slots) {
            total += slot.getStock();
        }
        return total;
    }

    public int getUsedSlots() {
        int count = 0;
        for (WarehouseSlot slot : slots) {
            if (!slot.isEmpty()) count++;
        }
        return count;
    }

    public long getLastDeliveryDay() {
        return lastDeliveryDay;
    }

    public void setLastDeliveryDay(long day) {
        this.lastDeliveryDay = day;
        setChanged();
    }

    /**
     * Leert alle Slots (Admin-Funktion)
     */
    public void clearAll() {
        for (WarehouseSlot slot : slots) {
            slot.clear();
        }
        setChanged();
        syncToClient();
    }

    // ═══════════════════════════════════════════════════════════
    // EXPENSE TRACKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt eine Ausgabe zur Historie hinzu
     */
    public void addExpense(long timestamp, int amount, String description) {
        expenses.add(new ExpenseEntry(timestamp, amount, description));
        setChanged();
        syncToClient();
    }

    /**
     * Bereinigt alte Ausgaben (älter als EXPENSE_RETENTION_DAYS)
     */
    public void cleanupOldExpenses(long currentTime) {
        expenses.removeIf(entry -> entry.isOlderThan(currentTime, EXPENSE_RETENTION_DAYS));
    }

    /**
     * Gibt alle Ausgaben zurück
     */
    public List<ExpenseEntry> getExpenses() {
        return new ArrayList<>(expenses);
    }

    /**
     * Berechnet Gesamtausgaben über die letzten X Tage
     */
    public int getTotalExpenses(long currentTime, int days) {
        return expenses.stream()
            .filter(entry -> !entry.isOlderThan(currentTime, days))
            .mapToInt(ExpenseEntry::getAmount)
            .sum();
    }

    /**
     * Berechnet durchschnittliche Ausgaben pro Lieferung
     */
    public double getAverageExpensePerDelivery(long currentTime, int days) {
        List<ExpenseEntry> recentExpenses = expenses.stream()
            .filter(entry -> !entry.isOlderThan(currentTime, days))
            .toList();

        if (recentExpenses.isEmpty()) {
            return 0.0;
        }

        int total = recentExpenses.stream()
            .mapToInt(ExpenseEntry::getAmount)
            .sum();

        return (double) total / recentExpenses.size();
    }

    /**
     * Gibt Anzahl der Lieferungen in den letzten X Tagen zurück
     */
    public int getDeliveryCount(long currentTime, int days) {
        return (int) expenses.stream()
            .filter(entry -> !entry.isOlderThan(currentTime, days))
            .count();
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALISIERUNG
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        // Speichere Slots
        ListTag slotsList = new ListTag();
        for (int i = 0; i < slots.length; i++) {
            CompoundTag slotTag = new CompoundTag();
            slots[i].save(slotTag);
            slotTag.putInt("Index", i);
            slotsList.add(slotTag);
        }
        tag.put("Slots", slotsList);

        // Speichere Verkäufer
        if (!linkedSellers.isEmpty()) {
            String[] uuids = linkedSellers.stream()
                .map(UUID::toString)
                .toArray(String[]::new);
            tag.putString("Sellers", String.join(",", uuids));
        }

        tag.putLong("LastDeliveryDay", lastDeliveryDay);

        if (shopId != null) {
            tag.putString("ShopId", shopId);
        }

        // Speichere Ausgaben
        if (!expenses.isEmpty()) {
            ListTag expensesList = new ListTag();
            for (ExpenseEntry expense : expenses) {
                CompoundTag expenseTag = new CompoundTag();
                expense.save(expenseTag);
                expensesList.add(expenseTag);
            }
            tag.put("Expenses", expensesList);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // Lade Slots
        if (tag.contains("Slots")) {
            ListTag slotsList = tag.getList("Slots", 10);
            for (int i = 0; i < slotsList.size(); i++) {
                CompoundTag slotTag = slotsList.getCompound(i);
                int index = slotTag.getInt("Index");
                if (index >= 0 && index < slots.length) {
                    slots[index].load(slotTag);
                }
            }
        }

        // Lade Verkäufer
        if (tag.contains("Sellers")) {
            String sellersStr = tag.getString("Sellers");
            linkedSellers.clear();
            if (!sellersStr.isEmpty()) {
                for (String uuidStr : sellersStr.split(",")) {
                    try {
                        linkedSellers.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Ungültige UUID beim Laden von Warehouse: {}", uuidStr);
                    }
                }
            }
        }

        // Lade lastDeliveryDay (mit Backwards-Compatibility für alte Saves)
        if (tag.contains("LastDeliveryDay")) {
            lastDeliveryDay = tag.getLong("LastDeliveryDay");
        } else if (tag.contains("LastDelivery")) {
            // Alte Saves: Konvertiere absolute Zeit zu Tag
            long oldTime = tag.getLong("LastDelivery");
            lastDeliveryDay = oldTime / 24000L;
            LOGGER.info("Converted old lastDeliveryTime {} to lastDeliveryDay {}", oldTime, lastDeliveryDay);
        } else {
            lastDeliveryDay = -1; // Keine Daten vorhanden
        }

        if (tag.contains("ShopId")) {
            shopId = tag.getString("ShopId");
        }

        // Lade Ausgaben
        if (tag.contains("Expenses")) {
            ListTag expensesList = tag.getList("Expenses", 10);
            expenses.clear();
            for (int i = 0; i < expensesList.size(); i++) {
                CompoundTag expenseTag = expensesList.getCompound(i);
                expenses.add(ExpenseEntry.load(expenseTag));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CLIENT-SERVER SYNCHRONISATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt Sync-Packet für Client-Updates
     */
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Daten für initiales Chunk-Loading
     */
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    /**
     * Empfängt Sync-Packet vom Server
     */
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    /**
     * Synchronisiert Änderungen zum Client
     */
    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
