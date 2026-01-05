package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.production.ProductionSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Große Extraktionswanne - 24 Blätter Kapazität, 4000 mB Diesel
 * REFACTORED: Nutzt ProductionSize.BIG für Kapazität
 */
public class BigExtractionVatBlockEntity extends AbstractExtractionVatBlockEntity {

    public BigExtractionVatBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.BIG_EXTRACTION_VAT.get(), pos, state, ProductionSize.BIG);
    }

    @Override
    protected int getExtractionTime() {
        return STANDARD_EXTRACTION_TIME_TICKS;
    }

    @Override
    public int getMaxDiesel() {
        return 4000; // 4000 mB = 4 Liter (2x Refinery-Wert)
    }
}
