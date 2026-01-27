package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Large Beer Fermentation Tank
 * - Capacity: 32 items
 * - Speed: 2.0x (fastest)
 */
public class LargeBeerFermentationTankBlockEntity extends AbstractBeerFermentationTankBlockEntity {
    public LargeBeerFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.LARGE_BEER_FERMENTATION_TANK.get(), pos, state);
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
