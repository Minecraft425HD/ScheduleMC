package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Großes Fermentierungsfass BlockEntity
 * Kapazität und Fermentierungszeit werden aus der Config gelesen.
 */
public class BigFermentationBarrelBlockEntity extends AbstractFermentationBarrelBlockEntity {

    public BigFermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.BIG_FERMENTATION_BARREL.get(), pos, state,
                ModConfigHandler.TOBACCO.BIG_FERMENTATION_BARREL_CAPACITY::get,
                ModConfigHandler.TOBACCO.TOBACCO_FERMENTING_TIME::get);
    }
}
