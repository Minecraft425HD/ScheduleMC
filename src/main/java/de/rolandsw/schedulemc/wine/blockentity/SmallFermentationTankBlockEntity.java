package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.wine.menu.SmallFermentationTankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SmallFermentationTankBlockEntity extends AbstractFermentationTankBlockEntity implements MenuProvider {
    public SmallFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.SMALL_FERMENTATION_TANK.get(), pos, state);
    }
    @Override protected int getCapacity() { return 16; }
    @Override protected int getFermentationTimePerItem() { return 1200; } // 60 seconds

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_fermentation_tank");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new SmallFermentationTankMenu(id, inv, this);
    }
}
