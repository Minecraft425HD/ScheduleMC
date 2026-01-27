package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Medium Beer Fermentation Tank
 * - Capacity: 16 items
 * - Speed: 1.5x (faster)
 */
public class MediumBeerFermentationTankBlockEntity extends AbstractBeerFermentationTankBlockEntity {
    public MediumBeerFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.MEDIUM_BEER_FERMENTATION_TANK.get(), pos, state);
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
