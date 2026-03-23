package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mittleres Trocknungsgestell BlockEntity
 * Kapazität und Trocknungszeit werden aus der Config gelesen.
 */
public class MediumDryingRackBlockEntity extends AbstractDryingRackBlockEntity {

    public MediumDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.MEDIUM_DRYING_RACK.get(), pos, state,
                ModConfigHandler.TOBACCO.MEDIUM_DRYING_RACK_CAPACITY::get,
                ModConfigHandler.TOBACCO.TOBACCO_DRYING_TIME::get);
    }
}
