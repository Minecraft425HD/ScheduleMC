package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.poppy.blocks.PoppyPlantBlock;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Wachstums-Handler für Poppy-Pflanzen (Opium)
 */
public class PoppyGrowthHandler extends AbstractPlantGrowthHandler {

    @Override
    public void tick(PlantPotData potData) {
        if (potData.hasPoppyPlant()) {
            potData.getPoppyPlant().tick();
        }
    }

    @Override
    public void updateBlockState(Level level, BlockPos pos, int newStage, PlantPotData potData) {
        if (potData.hasPoppyPlant()) {
            PoppyPlantBlock.growToStage(level, pos, newStage, potData.getPoppyPlant().getType());
        }
    }

    @Override
    public int getCurrentStage(PlantPotData potData) {
        return potData.hasPoppyPlant() ? potData.getPoppyPlant().getGrowthStage() : -1;
    }

    @Override
    public String getPlantTypeName() {
        return "Poppy";
    }
}
