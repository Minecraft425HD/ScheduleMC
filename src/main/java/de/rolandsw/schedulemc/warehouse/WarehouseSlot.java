package de.rolandsw.schedulemc.warehouse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

/**
 * Ein Slot im Warehouse der nur ein bestimmtes Item speichern kann
 * Speichert nicht ItemStacks sondern nur Item + Menge (effizienter)
 */
public class WarehouseSlot {
    @Nullable
    private Item allowedItem = null;
    private int currentStock = 0;
    private int maxCapacity;
    private boolean unlimited = false; // Neues Flag für unlimited Items

    public WarehouseSlot(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Kann dieser Slot das Item akzeptieren?
     */
    public boolean canAccept(Item item) {
        return allowedItem == null || allowedItem == item;
    }

    /**
     * Fügt Stock hinzu
     * @return Wie viel tatsächlich hinzugefügt wurde
     */
    public int addStock(Item item, int amount) {
        if (allowedItem == null) {
            allowedItem = item;
        }
        if (allowedItem != item) {
            return 0; // Falsches Item
        }

        int space = maxCapacity - currentStock;
        int toAdd = Math.min(amount, space);
        currentStock += toAdd;
        return toAdd;
    }

    /**
     * Entfernt Stock
     * @return Wie viel tatsächlich entfernt wurde
     */
    public int removeStock(int amount) {
        int toRemove = Math.min(amount, currentStock);
        currentStock -= toRemove;

        // Slot zurücksetzen wenn leer
        if (currentStock == 0) {
            allowedItem = null;
        }

        return toRemove;
    }

    // === STATUS ===

    public boolean isEmpty() {
        return allowedItem == null;
    }

    public boolean isFull() {
        return currentStock >= maxCapacity;
    }

    public int getStock() {
        return currentStock;
    }

    public int getAvailableSpace() {
        return maxCapacity - currentStock;
    }

    @Nullable
    public Item getAllowedItem() {
        return allowedItem;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean isUnlimited() {
        return unlimited;
    }

    public void setUnlimited(boolean unlimited) {
        this.unlimited = unlimited;
    }

    /**
     * Wie viel muss nachgeliefert werden um voll zu sein?
     * Unlimited Items werden NICHT aufgefüllt!
     */
    public int getRestockAmount() {
        if (allowedItem == null || unlimited) {
            return 0; // Keine Lieferung für unlimited Items!
        }
        return maxCapacity - currentStock;
    }

    /**
     * Leert den Slot komplett
     */
    public void clear() {
        this.allowedItem = null;
        this.currentStock = 0;
        this.unlimited = false;
    }

    // === SERIALISIERUNG ===

    public CompoundTag save(CompoundTag tag) {
        if (allowedItem != null) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(allowedItem);
            tag.putString("Item", itemId.toString());
            tag.putInt("Stock", currentStock);
            tag.putBoolean("Unlimited", unlimited);
        }
        tag.putInt("MaxCapacity", maxCapacity);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("Item")) {
            String itemIdStr = tag.getString("Item");
            ResourceLocation itemId = ResourceLocation.parse(itemIdStr);
            this.allowedItem = BuiltInRegistries.ITEM.get(itemId);
            this.currentStock = tag.getInt("Stock");
            this.unlimited = tag.getBoolean("Unlimited");
        }
        if (tag.contains("MaxCapacity")) {
            this.maxCapacity = tag.getInt("MaxCapacity");
        }
    }
}
