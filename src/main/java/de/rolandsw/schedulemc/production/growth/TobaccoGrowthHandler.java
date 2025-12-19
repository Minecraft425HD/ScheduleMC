package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Wachstums-Handler für Tabak-Pflanzen
 */
public class TobaccoGrowthHandler extends AbstractPlantGrowthHandler {

    @Override
    public void tick(PlantPotData potData) {
        if (potData.hasTobaccoPlant()) {
            potData.getPlant().tick();
        }
    }

    @Override
    public void updateBlockState(Level level, BlockPos pos, int newStage, PlantPotData potData) {
        if (potData.hasTobaccoPlant()) {
            TobaccoPlantBlock.growToStage(level, pos, newStage, potData.getPlant().getType());
        }
    }

    @Override
    public int getCurrentStage(PlantPotData potData) {
        return potData.hasTobaccoPlant() ? potData.getPlant().getGrowthStage() : -1;
    }

    @Override
    public String getPlantTypeName() {
        return "Tobacco";
    }
}
