package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.wine.menu.LargeWinePressMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LargeWinePressBlockEntity extends AbstractWinePressBlockEntity implements MenuProvider {
    public LargeWinePressBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.LARGE_WINE_PRESS.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 64; // 64 mash items
    }

    @Override
    protected int getPressingTimePerItem() {
        return 100; // 5 seconds per item
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_wine_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new LargeWinePressMenu(id, inv, this);
    }
}
