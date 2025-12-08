package de.rolandsw.schedulemc.vehicle.component.storage;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Component representing vehicle inventory/storage.
 * Stores items that can be accessed via GUI.
 */
public class InventoryComponent extends BaseComponent {

    private final int slotCount;
    private final NonNullList<ItemStack> items;
    private final SimpleContainer inventoryHandler;

    public InventoryComponent(int slotCount) {
        super(ComponentType.INVENTORY);
        this.slotCount = slotCount;
        this.items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        this.inventoryHandler = new SimpleContainer(items.toArray(new ItemStack[0]));
    }

    public InventoryComponent() {
        this(27); // Default 3 rows
    }

    // Getters
    public int getSlotCount() {
        return slotCount;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public SimpleContainer getInventoryHandler() {
        return inventoryHandler;
    }

    /**
     * Gets item in a specific slot.
     */
    public ItemStack getStackInSlot(int slot) {
        if (slot >= 0 && slot < slotCount) {
            return items.get(slot);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Sets item in a specific slot.
     */
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < slotCount) {
            items.set(slot, stack);
            inventoryHandler.setItem(slot, stack);
        }
    }

    /**
     * Clears all items from inventory.
     */
    public void clearInventory() {
        for (int i = 0; i < slotCount; i++) {
            items.set(i, ItemStack.EMPTY);
            inventoryHandler.setItem(i, ItemStack.EMPTY);
        }
    }

    /**
     * Checks if inventory is empty.
     */
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putInt("SlotCount", slotCount);

        ListTag itemList = new ListTag();
        for (int i = 0; i < slotCount; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                stack.save(itemTag);
                itemList.add(itemTag);
            }
        }
        tag.put("Items", itemList);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        ListTag itemList = tag.getList("Items", 10); // 10 = CompoundTag

        for (int i = 0; i < itemList.size(); i++) {
            CompoundTag itemTag = itemList.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;

            if (slot >= 0 && slot < slotCount) {
                ItemStack stack = ItemStack.of(itemTag);
                items.set(slot, stack);
                inventoryHandler.setItem(slot, stack);
            }
        }
    }

    @Override
    public IVehicleComponent duplicate() {
        InventoryComponent copy = new InventoryComponent(slotCount);
        for (int i = 0; i < slotCount; i++) {
            copy.items.set(i, items.get(i).copy());
            copy.inventoryHandler.setItem(i, items.get(i).copy());
        }
        return copy;
    }

    @Override
    public boolean isValid() {
        return slotCount > 0 && items.size() == slotCount;
    }
}
