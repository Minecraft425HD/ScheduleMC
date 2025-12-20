package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.production.ProductionSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mittlere Raffinerie - 12 Pasten Kapazität
 * REFACTORED: Nutzt ProductionSize.MEDIUM statt hardcoded Werte
 */
public class MediumRefineryBlockEntity extends AbstractRefineryBlockEntity {

    public MediumRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.MEDIUM_REFINERY.get(), pos, state, ProductionSize.MEDIUM);
    }

    @Override
    protected int getRefineryTime() {
        return 8000; // ~6.6 Minuten
    }
}
