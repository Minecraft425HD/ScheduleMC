package de.rolandsw.schedulemc.coca.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleine Extraktionswanne - 6 Blätter Kapazität, 1000 mB Diesel
 */
public class SmallExtractionVatBlockEntity extends AbstractExtractionVatBlockEntity {

    public SmallExtractionVatBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.SMALL_EXTRACTION_VAT.get(), pos, state);
    }

    @Override
    public int getCapacity() {
        return 6;
    }

    @Override
    protected int getExtractionTime() {
        return 6000; // 5 Minuten
    }

    @Override
    public int getMaxDiesel() {
        return 1000; // 1000 mB = 1 Liter
    }
}
