package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleines Fermentierungsfass BlockEntity
 * Kapazität: Konfigurierbar (Standard: 6 Tabakblätter)
 */
public class SmallFermentationBarrelBlockEntity extends AbstractFermentationBarrelBlockEntity {

    public SmallFermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.SMALL_FERMENTATION_BARREL.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return ModConfigHandler.TOBACCO.SMALL_FERMENTATION_BARREL_CAPACITY.get();
    }

    @Override
    protected int getFermentationTime() {
        return ModConfigHandler.TOBACCO.TOBACCO_FERMENTING_TIME.get();
    }
}
