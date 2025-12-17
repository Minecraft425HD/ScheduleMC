package de.rolandsw.schedulemc.coca.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleine Raffinerie - 6 Pasten Kapazität
 */
public class SmallRefineryBlockEntity extends AbstractRefineryBlockEntity {

    public SmallRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.SMALL_REFINERY.get(), pos, state);
    }

    @Override
    public int getCapacity() {
        return 6;
    }

    @Override
    protected int getRefineryTime() {
        return 8000; // ~6.6 Minuten
    }

    @Override
    public int getMaxFuel() {
        return 500;
    }
}
