package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small Brew Kettle
 * - Capacity: 8 items
 * - Speed: 1.0x (base speed)
 */
public class SmallBrewKettleBlockEntity extends AbstractBrewKettleBlockEntity {
    public SmallBrewKettleBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.SMALL_BREW_KETTLE.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 8;
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.0;
    }
}
