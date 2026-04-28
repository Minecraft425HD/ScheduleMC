package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.wine.blocks.GrapevineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class GrapeGrowthHandler extends AbstractPlantGrowthHandler {

    @Override
    public void tick(PlantPotData potData) {
        if (potData.hasGrapePlant()) {
            potData.getGrapePlant().tick();
        }
    }

    @Override
    public void updateBlockState(Level level, BlockPos pos, int newStage, PlantPotData potData) {
        if (potData.hasGrapePlant()) {
            GrapevineBlock.growToStage(level, pos, newStage, potData.getGrapePlant().getType());
        }
    }

    @Override
    public int getCurrentStage(PlantPotData potData) {
        return potData.hasGrapePlant() ? potData.getGrapePlant().getGrowthStage() : -1;
    }

    @Override
    public String getPlantTypeName() {
        return "Grape";
    }
}
