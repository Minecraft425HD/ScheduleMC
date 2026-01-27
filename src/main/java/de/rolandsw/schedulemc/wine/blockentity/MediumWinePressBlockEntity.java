package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.wine.menu.MediumWinePressMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MediumWinePressBlockEntity extends AbstractWinePressBlockEntity implements MenuProvider {
    public MediumWinePressBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.MEDIUM_WINE_PRESS.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 32; // 32 mash items
    }

    @Override
    protected int getPressingTimePerItem() {
        return 150; // 7.5 seconds per item
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.medium_wine_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new MediumWinePressMenu(id, inv, this);
    }
}
