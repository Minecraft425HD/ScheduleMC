package de.rolandsw.schedulemc.coca.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Große Extraktionswanne - 24 Blätter Kapazität, 4000 mB Diesel
 */
public class BigExtractionVatBlockEntity extends AbstractExtractionVatBlockEntity {

    public BigExtractionVatBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.BIG_EXTRACTION_VAT.get(), pos, state);
    }

    @Override
    public int getCapacity() {
        return 24;
    }

    @Override
    protected int getExtractionTime() {
        return 6000; // 5 Minuten
    }

    @Override
    public int getMaxDiesel() {
        return 4000; // 4000 mB = 4 Liter
    }
}
