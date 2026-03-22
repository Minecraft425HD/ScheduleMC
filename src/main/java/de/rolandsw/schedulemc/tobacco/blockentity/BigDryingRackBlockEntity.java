package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Großes Trocknungsgestell BlockEntity
 * Kapazität und Trocknungszeit werden aus der Config gelesen.
 */
public class BigDryingRackBlockEntity extends AbstractDryingRackBlockEntity {

    public BigDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.BIG_DRYING_RACK.get(), pos, state,
                ModConfigHandler.TOBACCO.BIG_DRYING_RACK_CAPACITY::get,
                ModConfigHandler.TOBACCO.TOBACCO_DRYING_TIME::get);
    }
}
