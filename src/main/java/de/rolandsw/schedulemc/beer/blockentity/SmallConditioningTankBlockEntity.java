package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small Conditioning Tank
 * - Slots: 4 (2x2)
 * - Speed: 1.0x (base aging speed)
 */
public class SmallConditioningTankBlockEntity extends AbstractConditioningTankBlockEntity {
    public SmallConditioningTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.SMALL_CONDITIONING_TANK.get(), pos, state);
    }

    @Override
    protected int getSlotCount() {
        return 4; // 2x2 grid
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.0;
    }
}
