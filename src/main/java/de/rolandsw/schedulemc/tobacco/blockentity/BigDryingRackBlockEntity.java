package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Großes Trocknungsgestell BlockEntity
 * Kapazität: Konfigurierbar (Standard: 10 Tabakblätter)
 */
public class BigDryingRackBlockEntity extends AbstractDryingRackBlockEntity {

    public BigDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.BIG_DRYING_RACK.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return ModConfigHandler.TOBACCO.BIG_DRYING_RACK_CAPACITY.get();
    }

    @Override
    protected int getDryingTime() {
        return ModConfigHandler.TOBACCO.TOBACCO_DRYING_TIME.get();
    }
}
