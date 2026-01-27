package de.rolandsw.schedulemc.honey.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Medium Aging Chamber - 9 slots, 1.5x speed
 */
public class MediumAgingChamberBlockEntity extends AbstractAgingChamberBlockEntity {
    public MediumAgingChamberBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.MEDIUM_AGING_CHAMBER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 9;
    }

    @Override
    protected float getSpeedMultiplier() {
        return 1.5f;
    }
}
