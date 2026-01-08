package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.cannabis.blocks.CannabisPlantBlock;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Wachstums-Handler für Cannabis-Pflanzen
 */
public class CannabisGrowthHandler extends AbstractPlantGrowthHandler {

    @Override
    public void tick(PlantPotData potData) {
        if (potData.hasCannabisPlant()) {
            potData.getCannabisPlant().tick();
        }
    }

    @Override
    public void updateBlockState(Level level, BlockPos pos, int newStage, PlantPotData potData) {
        if (potData.hasCannabisPlant()) {
            CannabisPlantBlock.growToStage(level, pos, newStage, potData.getCannabisPlant().getStrain());
        }
    }

    @Override
    public int getCurrentStage(PlantPotData potData) {
        return potData.hasCannabisPlant() ? potData.getCannabisPlant().getGrowthStage() : -1;
    }

    @Override
    public String getPlantTypeName() {
        return "Cannabis";
    }
}
