package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.production.ProductionSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mittlere Extraktionswanne - 12 Blätter Kapazität, 2000 mB Diesel
 * REFACTORED: Nutzt ProductionSize.MEDIUM für Kapazität
 */
public class MediumExtractionVatBlockEntity extends AbstractExtractionVatBlockEntity {

    public MediumExtractionVatBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.MEDIUM_EXTRACTION_VAT.get(), pos, state, ProductionSize.MEDIUM);
    }

    @Override
    protected int getExtractionTime() {
        return 6000; // 5 Minuten
    }

    @Override
    public int getMaxDiesel() {
        return 2000; // 2000 mB = 2 Liter (2x Refinery-Wert)
    }
}
