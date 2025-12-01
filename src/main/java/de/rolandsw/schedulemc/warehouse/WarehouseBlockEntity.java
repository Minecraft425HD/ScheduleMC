package de.rolandsw.schedulemc.warehouse;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.StateAccount;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.economy.ShopAccount;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
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

    private WarehouseSlot[] slots;
    private List<UUID> linkedSellers = new ArrayList<>();
    private long lastDeliveryTime = 0;
    @Nullable
    private String shopId; // Referenz zum Shop-Konto

    public WarehouseBlockEntity(BlockPos pos, BlockState state) {
        // TODO: super(ModBlockEntities.WAREHOUSE.get(), pos, state);
        super(null, pos, state); // Temporär bis BlockEntity registriert ist

        int slotCount = WarehouseConfig.SLOT_COUNT.get();
        int capacity = WarehouseConfig.MAX_CAPACITY_PER_SLOT.get();

        this.slots = new WarehouseSlot[slotCount];
        for (int i = 0; i < slotCount; i++) {
            slots[i] = new WarehouseSlot(capacity);
        }
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
        }
    }

    public void removeSeller(UUID sellerId) {
        linkedSellers.remove(sellerId);
        setChanged();
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
    }

    // ═══════════════════════════════════════════════════════════
    // AUTO-LIEFERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Tick-Methode (muss von Block aufgerufen werden)
     */
    public static void tick(Level level, BlockPos pos, BlockState state, WarehouseBlockEntity be) {
        if (level.isClientSide) return;

        long currentTime = level.getGameTime();
        long intervalTicks = WarehouseConfig.DELIVERY_INTERVAL_DAYS.get() * 24000L;

        if (currentTime - be.lastDeliveryTime >= intervalTicks) {
            be.performDelivery(level);
            be.lastDeliveryTime = currentTime;
            be.setChanged();
        }
    }

    /**
     * Führt Warehouse-Lieferung durch
     * Staatskasse zahlt, Shop-Konto registriert Ausgaben
     */
    private void performDelivery(Level level) {
        // Berechne Lieferkosten
        int totalCost = 0;
        Map<Item, Integer> toDeliver = new HashMap<>();

        for (WarehouseSlot slot : slots) {
            int restockAmount = slot.getRestockAmount();
            if (restockAmount > 0 && slot.getAllowedItem() != null) {
                Item item = slot.getAllowedItem();
                int pricePerItem = DeliveryPriceConfig.getPrice(item);
                totalCost += restockAmount * pricePerItem;
                toDeliver.put(item, restockAmount);
            }
        }

        if (totalCost == 0 || toDeliver.isEmpty()) {
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

            LOGGER.info("Warehouse-Lieferung erfolgreich @ {}: {}€ (Staatskasse), {} Items",
                worldPosition.toShortString(), totalCost, toDeliver.size());

            // Benachrichtige nahe Spieler
            level.players().stream()
                .filter(player -> player.blockPosition().distSqr(worldPosition) < 2500) // 50 Blöcke
                .forEach(player -> player.sendSystemMessage(
                    Component.literal("§a[Warehouse] Lieferung erhalten! Kosten: " + totalCost + "€")
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

        tag.putLong("LastDelivery", lastDeliveryTime);

        if (shopId != null) {
            tag.putString("ShopId", shopId);
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

        lastDeliveryTime = tag.getLong("LastDelivery");

        if (tag.contains("ShopId")) {
            shopId = tag.getString("ShopId");
        }
    }
}
