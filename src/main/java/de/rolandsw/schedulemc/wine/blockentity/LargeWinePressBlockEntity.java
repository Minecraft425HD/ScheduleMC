package de.rolandsw.schedulemc.wine.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LargeWinePressBlockEntity extends AbstractWinePressBlockEntity {
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
}
