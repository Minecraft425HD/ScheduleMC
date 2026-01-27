package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Large Conditioning Tank
 * - Slots: 16 (4x4)
 * - Speed: 2.0x (fastest aging)
 */
public class LargeConditioningTankBlockEntity extends AbstractConditioningTankBlockEntity {
    public LargeConditioningTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.LARGE_CONDITIONING_TANK.get(), pos, state);
    }

    @Override
    protected int getSlotCount() {
        return 16; // 4x4 grid
    }

    @Override
    protected double getSpeedMultiplier() {
        return 2.0;
    }
}
