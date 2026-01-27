package de.rolandsw.schedulemc.honey.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Large Aging Chamber - 16 slots, 2.0x speed
 */
public class LargeAgingChamberBlockEntity extends AbstractAgingChamberBlockEntity {
    public LargeAgingChamberBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.LARGE_AGING_CHAMBER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 16;
    }

    @Override
    protected float getSpeedMultiplier() {
        return 2.0f;
    }
}
