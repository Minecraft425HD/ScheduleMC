package de.rolandsw.schedulemc.coffee.menu;

import de.rolandsw.schedulemc.coffee.blockentity.CoffeePackagingTableBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Container-Menu für Coffee Packaging Table
 * 3 Slots: Coffee Input, Packaging Material, Output
 */
public class CoffeePackagingTableMenu extends AbstractContainerMenu {

    public final CoffeePackagingTableBlockEntity blockEntity;
    private final ContainerData data;
    private final Inventory playerInventory;

    // Server-side constructor
    public CoffeePackagingTableMenu(int containerId, Inventory playerInventory, CoffeePackagingTableBlockEntity blockEntity) {
        super(CoffeeMenuTypes.COFFEE_PACKAGING_TABLE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.playerInventory = playerInventory;
        this.data = blockEntity.getContainerData();

        addDataSlots(data);
        addSlots();
    }

    // Client-side constructor
    public CoffeePackagingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CoffeeMenuTypes.COFFEE_PACKAGING_TABLE_MENU.get(), containerId);
        this.playerInventory = playerInventory;

        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof CoffeePackagingTableBlockEntity packagingTable) {
            this.blockEntity = packagingTable;
            this.data = packagingTable.getContainerData();
        } else {
            this.blockEntity = null;
            this.data = new SimpleContainerData(3);
        }

        addDataSlots(data);
        addSlots();
    }

    private void addSlots() {
        if (blockEntity == null) return;

        var handler = blockEntity.getItemHandler();

        // Slot 0: Coffee Input (Ground Coffee) - Links
        this.addSlot(new SlotItemHandler(handler, 0, 56, 35));

        // Slot 1: Packaging Material (Coffee Bags) - Mitte-Links
        this.addSlot(new SlotItemHandler(handler, 1, 86, 35));

        // Slot 2: Output (Packaged Coffee) - Rechts
        this.addSlot(new SlotItemHandler(handler, 2, 126, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false; // Output-only slot
            }
        });

        // Player Hotbar (nur Schnellzugriffsleiste)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 122));
        }
    }

    /**
     * Gibt den aktuellen Fortschritt zurück (0-200)
     */
    public int getProgress() {
        if (blockEntity == null) return 0;
        return data.get(0);
    }

    /**
     * Gibt den maximalen Fortschritt zurück (immer 200 = 10 Sekunden)
     */
    public int getMaxProgress() {
        return 200;
    }

    /**
     * Gibt die gewählte Paketgröße zurück (0=SMALL, 1=MEDIUM, 2=LARGE)
     */
    public int getSelectedPackageSize() {
        if (blockEntity == null) return 1;
        return data.get(1);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        // Von BlockEntity zu Player
        if (index < 3) {
            if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        }
        // Von Player zu BlockEntity Input Slots
        else {
            // Versuche zuerst Coffee Input (Slot 0)
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                // Dann Packaging Material (Slot 1)
                if (!this.moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
               player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                                   blockEntity.getBlockPos().getY() + 0.5,
                                   blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    /**
     * Synchronisiert Daten zwischen Server und Client
     */
    public void updateData() {
        if (blockEntity == null) return;

        // Progress wird automatisch über ContainerData synchronisiert
        // Hier können zusätzliche Synchronisierungen hinzugefügt werden
    }
}
