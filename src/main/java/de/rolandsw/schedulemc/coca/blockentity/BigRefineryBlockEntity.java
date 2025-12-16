package de.rolandsw.schedulemc.coca.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Große Raffinerie - 24 Pasten Kapazität
 */
public class BigRefineryBlockEntity extends AbstractRefineryBlockEntity {

    public BigRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.BIG_REFINERY.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 24;
    }

    @Override
    protected int getRefineryTime() {
        return 8000; // ~6.6 Minuten
    }

    @Override
    public int getMaxFuel() {
        return 2000;
    }
}
