package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mittleres Fermentierungsfass BlockEntity
 * Kapazität: Konfigurierbar (Standard: 8 Tabakblätter)
 */
public class MediumFermentationBarrelBlockEntity extends AbstractFermentationBarrelBlockEntity {

    public MediumFermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.MEDIUM_FERMENTATION_BARREL.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return ModConfigHandler.TOBACCO.MEDIUM_FERMENTATION_BARREL_CAPACITY.get();
    }

    @Override
    protected int getFermentationTime() {
        return ModConfigHandler.TOBACCO.TOBACCO_FERMENTING_TIME.get();
    }
}
