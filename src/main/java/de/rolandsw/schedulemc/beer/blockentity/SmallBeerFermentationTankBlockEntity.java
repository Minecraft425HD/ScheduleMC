package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small Beer Fermentation Tank
 * - Capacity: 8 items
 * - Speed: 1.0x (base speed)
 */
public class SmallBeerFermentationTankBlockEntity extends AbstractBeerFermentationTankBlockEntity {
    public SmallBeerFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.SMALL_BEER_FERMENTATION_TANK.get(), pos, state);
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
