package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleines Trocknungsgestell BlockEntity
 * Kapazität: Konfigurierbar (Standard: 6 Tabakblätter)
 */
public class SmallDryingRackBlockEntity extends AbstractDryingRackBlockEntity {

    public SmallDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.SMALL_DRYING_RACK.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return ModConfigHandler.TOBACCO.SMALL_DRYING_RACK_CAPACITY.get();
    }

    @Override
    protected int getDryingTime() {
        return ModConfigHandler.TOBACCO.TOBACCO_DRYING_TIME.get();
    }
}
