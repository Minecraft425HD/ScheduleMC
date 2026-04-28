package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.coffee.blocks.CoffeePlantBlock;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class CoffeeGrowthHandler extends AbstractPlantGrowthHandler {

    @Override
    public void tick(PlantPotData potData) {
        if (potData.hasCoffeePlant()) {
            potData.getCoffeePlant().tick();
        }
    }

    @Override
    public void updateBlockState(Level level, BlockPos pos, int newStage, PlantPotData potData) {
        if (potData.hasCoffeePlant()) {
            CoffeePlantBlock.growToStage(level, pos, newStage, potData.getCoffeePlant().getType());
        }
    }

    @Override
    public int getCurrentStage(PlantPotData potData) {
        return potData.hasCoffeePlant() ? potData.getCoffeePlant().getGrowthStage() : -1;
    }

    @Override
    public String getPlantTypeName() {
        return "Coffee";
    }
}
