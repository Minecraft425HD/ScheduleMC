package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Large Brew Kettle
 * - Capacity: 32 items
 * - Speed: 2.0x (fastest)
 */
public class LargeBrewKettleBlockEntity extends AbstractBrewKettleBlockEntity {
    public LargeBrewKettleBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.LARGE_BREW_KETTLE.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 32;
    }

    @Override
    protected double getSpeedMultiplier() {
        return 2.0;
    }
}
