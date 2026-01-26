package de.rolandsw.schedulemc.coffee.menu;

import de.rolandsw.schedulemc.coffee.blockentity.MediumDryingTrayBlockEntity;
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

public class MediumDryingTrayMenu extends AbstractContainerMenu {

    public final MediumDryingTrayBlockEntity blockEntity;
    private final ContainerData data;

    public MediumDryingTrayMenu(int containerId, Inventory playerInventory, MediumDryingTrayBlockEntity blockEntity) {
        super(CoffeeMenuTypes.MEDIUM_DRYING_TRAY_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = new SimpleContainerData(2);
        addDataSlots(this.data);
        addSlots(playerInventory);
    }

    public MediumDryingTrayMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CoffeeMenuTypes.MEDIUM_DRYING_TRAY_MENU.get(), containerId);
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        this.blockEntity = be instanceof MediumDryingTrayBlockEntity tray ? tray : null;
        this.data = new SimpleContainerData(2);
        addDataSlots(this.data);
        addSlots(playerInventory);
    }

    private void addSlots(Inventory playerInventory) {
        if (blockEntity == null) return;
        var handler = blockEntity.getItemHandler();
        this.addSlot(new SlotItemHandler(handler, 0, 56, 35));
        this.addSlot(new SlotItemHandler(handler, 1, 116, 35));
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 84));
        }
    }

    public int getProgress() {
        return blockEntity == null ? 0 : blockEntity.getDryingProgressValue();
    }

    public int getMaxProgress() {
        return blockEntity == null ? 1 : blockEntity.getTotalDryingTime();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
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
        return blockEntity != null && player.distanceToSqr(
            blockEntity.getBlockPos().getX() + 0.5,
            blockEntity.getBlockPos().getY() + 0.5,
            blockEntity.getBlockPos().getZ() + 0.5) <= 64;
    }
}
