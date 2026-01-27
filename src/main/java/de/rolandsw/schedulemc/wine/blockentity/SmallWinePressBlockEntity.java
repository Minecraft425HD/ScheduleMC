package de.rolandsw.schedulemc.wine.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SmallWinePressBlockEntity extends AbstractWinePressBlockEntity {
    public SmallWinePressBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.SMALL_WINE_PRESS.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 16; // 16 mash items
    }

    @Override
    protected int getPressingTimePerItem() {
        return 200; // 10 seconds per item
    }
}
