package de.rolandsw.schedulemc.production.nbt;

import de.rolandsw.schedulemc.cannabis.CannabisPlant;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.nbt.CompoundTag;

public class CannabisPlantSerializer implements PlantSerializer {

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasCannabisPlant()) return;

        CompoundTag cannabisTag = new CompoundTag();
        CannabisPlant plant = potData.getCannabisPlant();

        cannabisTag.putString("Strain", plant.getStrain().name());
        cannabisTag.putString("Quality", plant.getQuality().name());
        cannabisTag.putInt("GrowthStage", plant.getGrowthStage());
        cannabisTag.putInt("TicksGrown", plant.getTicksGrown());
        cannabisTag.putBoolean("HasFertilizer", plant.hasFertilizer());
        cannabisTag.putBoolean("HasGrowthBooster", plant.hasGrowthBooster());
        cannabisTag.putBoolean("HasQualityBooster", plant.hasQualityBooster());

        tag.put(getPlantTagName(), cannabisTag);
    }

    @Override
    public void loadPlant(PlantPotData potData, CompoundTag tag) {
        if (!tag.contains(getPlantTagName())) return;

        CompoundTag cannabisTag = tag.getCompound(getPlantTagName());
        CannabisStrain strain = CannabisStrain.valueOf(cannabisTag.getString("Strain"));

        if (!potData.hasCannabisPlant()) {
            potData.plantCannabisSeed(strain);
        }

        CannabisPlant plant = potData.getCannabisPlant();
        if (plant != null) {
            plant.setQuality(CannabisQuality.valueOf(cannabisTag.getString("Quality")));
            plant.setGrowthStage(cannabisTag.getInt("GrowthStage"));

            int ticksGrown = cannabisTag.getInt("TicksGrown");
            while (plant.getTicksGrown() < ticksGrown) {
                plant.incrementTicks();
            }

            if (cannabisTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
            if (cannabisTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
            if (cannabisTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
        }
    }

    @Override
    public String getPlantTagName() {
        return "CannabisPlant";
    }

    @Override
    public boolean hasPlant(PlantPotData potData) {
        return potData.hasCannabisPlant();
    }
}
