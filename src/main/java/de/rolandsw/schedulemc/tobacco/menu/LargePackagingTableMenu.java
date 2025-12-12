package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.tobacco.blockentity.LargePackagingTableBlockEntity;
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
 * Container-Menu für Large Packaging Table
 */
public class LargePackagingTableMenu extends AbstractContainerMenu {

    public final LargePackagingTableBlockEntity blockEntity;
    private final Inventory playerInventory;

    public LargePackagingTableMenu(int containerId, Inventory playerInventory, LargePackagingTableBlockEntity blockEntity) {
        super(ModMenuTypes.LARGE_PACKAGING_TABLE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.playerInventory = playerInventory;

        addSlots();
    }

    public LargePackagingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.LARGE_PACKAGING_TABLE_MENU.get(), containerId);
        this.playerInventory = playerInventory;

        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof LargePackagingTableBlockEntity packagingTable) {
            this.blockEntity = packagingTable;
        } else {
            this.blockEntity = null;
        }

        addSlots();
    }

    private void addSlots() {
        if (blockEntity == null) return;

        var handler = blockEntity.getItemHandler();

        // Input Slot (Slot 0) - Links
        this.addSlot(new SlotItemHandler(handler, 0, 56, 35));

        // Output Slots (Slots 1-9) - Rechts (3x3 Grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = 1 + col + row * 3;
                this.addSlot(new SlotItemHandler(handler, index, 116 + col * 18, 17 + row * 18));
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 86));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < 10) {
            if (!this.moveItemStackTo(stack, 10, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
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
