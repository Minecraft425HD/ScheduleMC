package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.tobacco.blockentity.MediumFermentationBarrelBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class MediumFermentationBarrelMenu extends AbstractContainerMenu {
    private final MediumFermentationBarrelBlockEntity blockEntity;

    public MediumFermentationBarrelMenu(int id, Inventory inv, MediumFermentationBarrelBlockEntity be) {
        super(ModMenuTypes.MEDIUM_FERMENTATION_BARREL_MENU.get(), id);
        this.blockEntity = be;
    }

    public MediumFermentationBarrelMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(ModMenuTypes.MEDIUM_FERMENTATION_BARREL_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof MediumFermentationBarrelBlockEntity e ? e : null; // NOPMD
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
