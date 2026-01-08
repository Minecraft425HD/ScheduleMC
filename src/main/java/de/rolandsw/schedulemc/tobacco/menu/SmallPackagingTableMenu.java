package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.tobacco.blockentity.SmallPackagingTableBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Container-Menu für Small Packaging Table
 */
public class SmallPackagingTableMenu extends AbstractContainerMenu {

    public final SmallPackagingTableBlockEntity blockEntity;
    private final Inventory playerInventory;

    // Server-side constructor
    public SmallPackagingTableMenu(int containerId, Inventory playerInventory, SmallPackagingTableBlockEntity blockEntity) {
        super(ModMenuTypes.SMALL_PACKAGING_TABLE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.playerInventory = playerInventory;

        addSlots();
    }

    // Client-side constructor
    public SmallPackagingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.SMALL_PACKAGING_TABLE_MENU.get(), containerId);
        this.playerInventory = playerInventory;

        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof SmallPackagingTableBlockEntity packagingTable) {
            this.blockEntity = packagingTable;
        } else {
            this.blockEntity = null;
        }

        addSlots();
    }

    private void addSlots() {
        if (blockEntity == null) return;

        var handler = blockEntity.getItemHandler();

        // Input Slot (Slot 0) - Oben Mitte
        this.addSlot(new SlotItemHandler(handler, 0, 80, 20));

        // Tüten-Slots (Slots 1-10) - Links (2x5 Grid)
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                int index = 1 + col + row * 2;
                this.addSlot(new SlotItemHandler(handler, index, 8 + col * 18, 50 + row * 18));
            }
        }

        // Gläser-Slots (Slots 11-20) - Rechts (2x5 Grid)
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                int index = 11 + col + row * 2;
                this.addSlot(new SlotItemHandler(handler, index, 134 + col * 18, 50 + row * 18));
            }
        }

        // Player Hotbar (nur Schnellzugriffsleiste)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 168));  // Angepasst an neue GUI-Höhe
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        // Von BlockEntity zu Player
        if (index < 21) {
            if (!this.moveItemStackTo(stack, 21, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        }
        // Von Player zu BlockEntity Input
        else {
            if (!this.moveItemStackTo(stack, 0, 21, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
               player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                                   blockEntity.getBlockPos().getY() + 0.5,
                                   blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
