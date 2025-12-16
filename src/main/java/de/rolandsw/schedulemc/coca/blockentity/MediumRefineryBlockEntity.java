package de.rolandsw.schedulemc.coca.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mittlere Raffinerie - 12 Pasten Kapazität
 */
public class MediumRefineryBlockEntity extends AbstractRefineryBlockEntity {

    public MediumRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.MEDIUM_REFINERY.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 12;
    }

    @Override
    protected int getRefineryTime() {
        return 8000; // ~6.6 Minuten
    }

    @Override
    public int getMaxFuel() {
        return 1000;
    }
}
