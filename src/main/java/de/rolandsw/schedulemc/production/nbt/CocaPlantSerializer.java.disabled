package de.rolandsw.schedulemc.production.nbt;

import de.rolandsw.schedulemc.coca.CocaPlant;
import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;

public class CocaPlantSerializer implements PlantSerializer {

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasCocaPlant()) return;

        CompoundTag cocaTag = new CompoundTag();
        CocaPlant plant = potData.getCocaPlant();

        cocaTag.putString("Type", plant.getType().name());
        cocaTag.putString("Quality", plant.getQuality().name());
        cocaTag.putInt("GrowthStage", plant.getGrowthStage());
        cocaTag.putInt("TicksGrown", plant.getTicksGrown());
        cocaTag.putBoolean("HasFertilizer", plant.hasFertilizer());
        cocaTag.putBoolean("HasGrowthBooster", plant.hasGrowthBooster());
        cocaTag.putBoolean("HasQualityBooster", plant.hasQualityBooster());

        tag.put(getPlantTagName(), cocaTag);
    }

    @Override
    public void loadPlant(PlantPotData potData, CompoundTag tag) {
        if (!tag.contains(getPlantTagName())) return;

        CompoundTag cocaTag = tag.getCompound(getPlantTagName());
        CocaType type = CocaType.valueOf(cocaTag.getString("Type"));

        if (!potData.hasCocaPlant()) {
            potData.plantCocaSeed(type);
        }

        CocaPlant plant = potData.getCocaPlant();
        if (plant != null) {
            plant.setQuality(TobaccoQuality.valueOf(cocaTag.getString("Quality")));
            plant.setGrowthStage(cocaTag.getInt("GrowthStage"));

            int ticksGrown = cocaTag.getInt("TicksGrown");
            while (plant.getTicksGrown() < ticksGrown) {
                plant.incrementTicks();
            }

            if (cocaTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
            if (cocaTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
            if (cocaTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
        }
    }

    @Override
    public String getPlantTagName() {
        return "CocaPlant";
    }

    @Override
    public boolean hasPlant(PlantPotData potData) {
        return potData.hasCocaPlant();
    }
}
