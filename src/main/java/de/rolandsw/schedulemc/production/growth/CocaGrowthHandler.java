package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.coca.blocks.CocaPlantBlock;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Wachstums-Handler für Coca-Pflanzen
 */
public class CocaGrowthHandler extends AbstractPlantGrowthHandler {

    @Override
    public void tick(PlantPotData potData) {
        if (potData.hasCocaPlant()) {
            potData.getCocaPlant().tick();
        }
    }

    @Override
    public void updateBlockState(Level level, BlockPos pos, int newStage, PlantPotData potData) {
        if (potData.hasCocaPlant()) {
            CocaPlantBlock.growToStage(level, pos, newStage, potData.getCocaPlant().getType());
        }
    }

    @Override
    public int getCurrentStage(PlantPotData potData) {
        return potData.hasCocaPlant() ? potData.getCocaPlant().getGrowthStage() : -1;
    }

    @Override
    public String getPlantTypeName() {
        return "Coca";
    }
}
