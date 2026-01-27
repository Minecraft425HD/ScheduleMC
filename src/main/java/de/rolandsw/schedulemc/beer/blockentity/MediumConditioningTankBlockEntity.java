package de.rolandsw.schedulemc.beer.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Medium Conditioning Tank
 * - Slots: 9 (3x3)
 * - Speed: 1.5x (faster aging)
 */
public class MediumConditioningTankBlockEntity extends AbstractConditioningTankBlockEntity {
    public MediumConditioningTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.MEDIUM_CONDITIONING_TANK.get(), pos, state);
    }

    @Override
    protected int getSlotCount() {
        return 9; // 3x3 grid
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.5;
    }
}
