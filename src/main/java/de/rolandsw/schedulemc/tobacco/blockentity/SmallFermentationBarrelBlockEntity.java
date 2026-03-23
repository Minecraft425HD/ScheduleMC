package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleines Fermentierungsfass BlockEntity
 * Kapazität und Fermentierungszeit werden aus der Config gelesen.
 */
public class SmallFermentationBarrelBlockEntity extends AbstractFermentationBarrelBlockEntity {

    public SmallFermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.SMALL_FERMENTATION_BARREL.get(), pos, state,
                ModConfigHandler.TOBACCO.SMALL_FERMENTATION_BARREL_CAPACITY::get,
                ModConfigHandler.TOBACCO.TOBACCO_FERMENTING_TIME::get);
    }
}
