package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.tobacco.blockentity.BigFermentationBarrelBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BigFermentationBarrelMenu extends AbstractContainerMenu {
    private final BigFermentationBarrelBlockEntity blockEntity;

    public BigFermentationBarrelMenu(int id, Inventory inv, BigFermentationBarrelBlockEntity be) {
        super(ModMenuTypes.BIG_FERMENTATION_BARREL_MENU.get(), id);
        this.blockEntity = be;
        addMachineSlots();
        addPlayerHotbar(inv);
    }

    public BigFermentationBarrelMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(ModMenuTypes.BIG_FERMENTATION_BARREL_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof BigFermentationBarrelBlockEntity e ? e : null; // NOPMD
        addMachineSlots();
        addPlayerHotbar(inv);
    }

    private void addMachineSlots() {
        if (blockEntity == null) return;
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 56, 35));
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 1, 116, 35));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, 8 + i * 18, 84));
        }
    }

    public int getInputCount() {
        return blockEntity == null ? 0 : blockEntity.getInputCount();
    }

    public int getOutputCount() {
        return blockEntity == null ? 0 : blockEntity.getOutputCount();
    }

    public int getCapacity() {
        return blockEntity == null ? 0 : blockEntity.getCapacityValue();
    }

    public int getProgressPercent() {
        return blockEntity == null ? 0 : Math.round(blockEntity.getAverageFermentationPercentage() * 100f);
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

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && player.distanceToSqr(
            blockEntity.getBlockPos().getX() + 0.5,
            blockEntity.getBlockPos().getY() + 0.5,
            blockEntity.getBlockPos().getZ() + 0.5
        ) <= 64;
    }
}
