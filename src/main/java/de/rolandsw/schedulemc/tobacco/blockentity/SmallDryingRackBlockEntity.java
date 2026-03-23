package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleines Trocknungsgestell BlockEntity
 * Kapazität und Trocknungszeit werden aus der Config gelesen.
 */
public class SmallDryingRackBlockEntity extends AbstractDryingRackBlockEntity {

    public SmallDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.SMALL_DRYING_RACK.get(), pos, state,
                ModConfigHandler.TOBACCO.SMALL_DRYING_RACK_CAPACITY::get,
                ModConfigHandler.TOBACCO.TOBACCO_DRYING_TIME::get);
    }
}
