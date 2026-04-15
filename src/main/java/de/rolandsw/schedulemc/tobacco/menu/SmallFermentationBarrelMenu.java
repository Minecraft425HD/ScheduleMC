package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.tobacco.blockentity.SmallFermentationBarrelBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class SmallFermentationBarrelMenu extends AbstractContainerMenu {
    private final SmallFermentationBarrelBlockEntity blockEntity;

    public SmallFermentationBarrelMenu(int id, Inventory inv, SmallFermentationBarrelBlockEntity be) {
        super(ModMenuTypes.SMALL_FERMENTATION_BARREL_MENU.get(), id);
        this.blockEntity = be;
        addPlayerHotbar(inv);
    }

    public SmallFermentationBarrelMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(ModMenuTypes.SMALL_FERMENTATION_BARREL_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof SmallFermentationBarrelBlockEntity e ? e : null; // NOPMD
        addPlayerHotbar(inv);
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
        return ItemStack.EMPTY;
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
