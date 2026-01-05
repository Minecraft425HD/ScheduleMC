package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.production.ProductionSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleine Extraktionswanne - 6 Blätter Kapazität, 1000 mB Diesel
 * REFACTORED: Nutzt ProductionSize.SMALL statt hardcoded Werte
 */
public class SmallExtractionVatBlockEntity extends AbstractExtractionVatBlockEntity {

    public SmallExtractionVatBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.SMALL_EXTRACTION_VAT.get(), pos, state, ProductionSize.SMALL);
    }

    @Override
    protected int getExtractionTime() {
        return STANDARD_EXTRACTION_TIME_TICKS;
    }
}
