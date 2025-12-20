package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.production.ProductionSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleine Raffinerie - 6 Pasten Kapazität
 * REFACTORED: Nutzt ProductionSize.SMALL statt hardcoded Werte
 */
public class SmallRefineryBlockEntity extends AbstractRefineryBlockEntity {

    public SmallRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.SMALL_REFINERY.get(), pos, state, ProductionSize.SMALL);
    }

    @Override
    protected int getRefineryTime() {
        return 8000; // ~6.6 Minuten
    }
}
