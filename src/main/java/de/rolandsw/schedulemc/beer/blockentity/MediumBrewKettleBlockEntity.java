package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Medium Brew Kettle
 * - Capacity: 16 items
 * - Speed: 1.5x (faster)
 */
public class MediumBrewKettleBlockEntity extends AbstractBrewKettleBlockEntity {
    public MediumBrewKettleBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.MEDIUM_BREW_KETTLE.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 16;
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.5;
    }
}
