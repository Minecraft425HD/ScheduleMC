package de.rolandsw.schedulemc.wine.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MediumWinePressBlockEntity extends AbstractWinePressBlockEntity {
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
}
