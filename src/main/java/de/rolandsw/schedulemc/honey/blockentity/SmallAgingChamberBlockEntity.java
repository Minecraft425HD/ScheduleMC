package de.rolandsw.schedulemc.honey.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small Aging Chamber - 4 slots, 1.0x speed
 */
public class SmallAgingChamberBlockEntity extends AbstractAgingChamberBlockEntity {
    public SmallAgingChamberBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.SMALL_AGING_CHAMBER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 4;
    }

    @Override
    protected float getSpeedMultiplier() {
        return 1.0f;
    }
}
