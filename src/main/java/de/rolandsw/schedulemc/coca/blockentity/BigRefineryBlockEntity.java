package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.production.ProductionSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Große Raffinerie - 24 Pasten Kapazität
 * REFACTORED: Nutzt ProductionSize.BIG statt hardcoded Werte
 */
public class BigRefineryBlockEntity extends AbstractRefineryBlockEntity {

    public BigRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.BIG_REFINERY.get(), pos, state, ProductionSize.BIG);
    }

    @Override
    protected int getRefineryTime() {
        return 8000; // ~6.6 Minuten
    }
}
