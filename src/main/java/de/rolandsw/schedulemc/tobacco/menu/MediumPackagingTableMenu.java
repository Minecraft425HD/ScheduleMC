package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.tobacco.blockentity.MediumPackagingTableBlockEntity;
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
 * Container-Menu für Medium Packaging Table.
 *
 * Slot-Layout (passend zu MediumPackagingTableScreen, 176 × 190):
 *  - Slot 0  : Input  – Mitte oben (80, 20)  ← wie Small
 *  - Slots 1–10 : Schachteln-Grid (2 × 5) links (8 + col*18, 50 + row*18)  ← wie Tüten in Small
 *  - Slots 11–19: Hotbar (8 + i*18, 168)  ← wie Small
 */
public class MediumPackagingTableMenu extends AbstractContainerMenu {

    public final MediumPackagingTableBlockEntity blockEntity;
    private final Inventory playerInventory;

    public MediumPackagingTableMenu(int containerId, Inventory playerInventory, MediumPackagingTableBlockEntity blockEntity) {
        super(ModMenuTypes.MEDIUM_PACKAGING_TABLE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.playerInventory = playerInventory;
        addSlots();
    }

    public MediumPackagingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.MEDIUM_PACKAGING_TABLE_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        this.blockEntity = be instanceof MediumPackagingTableBlockEntity pt ? pt : null;
        addSlots();
    }

    private void addSlots() {
        if (blockEntity == null) return;

        var handler = blockEntity.getItemHandler();

        // Input Slot (Slot 0) – oben Mitte, wie Small
        this.addSlot(new SlotItemHandler(handler, 0, 80, 20));

        // Schachteln-Slots (Slots 1–10) – 2 × 5 Grid links, wie Tüten im Small
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                int index = 1 + col + row * 2;
                this.addSlot(new SlotItemHandler(handler, index, 8 + col * 18, 50 + row * 18));
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 168));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy  = stack.copy();

        if (index < 11) {
            if (!this.moveItemStackTo(stack, 11, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 0, 11, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

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
