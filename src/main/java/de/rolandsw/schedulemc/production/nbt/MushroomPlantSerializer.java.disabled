package de.rolandsw.schedulemc.production.nbt;

import de.rolandsw.schedulemc.mushroom.MushroomPlant;
import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;

public class MushroomPlantSerializer implements PlantSerializer {

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasMushroomPlant()) return;

        CompoundTag mushroomTag = new CompoundTag();
        MushroomPlant plant = potData.getMushroomPlant();

        mushroomTag.putString("Type", plant.getType().name());
        mushroomTag.putString("Quality", plant.getQuality().name());
        mushroomTag.putInt("GrowthStage", plant.getGrowthStage());
        mushroomTag.putInt("TicksGrown", plant.getTicksGrown());
        mushroomTag.putInt("CurrentFlush", plant.getCurrentFlush()); // Mushroom-spezifisch
        mushroomTag.putBoolean("HasFertilizer", plant.hasFertilizer());
        mushroomTag.putBoolean("HasGrowthBooster", plant.hasGrowthBooster());
        mushroomTag.putBoolean("HasQualityBooster", plant.hasQualityBooster());

        tag.put(getPlantTagName(), mushroomTag);
    }

    @Override
    public void loadPlant(PlantPotData potData, CompoundTag tag) {
        if (!tag.contains(getPlantTagName())) return;

        CompoundTag mushroomTag = tag.getCompound(getPlantTagName());
        MushroomType type = MushroomType.valueOf(mushroomTag.getString("Type"));

        // Setze Mist-Status vor dem Pflanzen (Mushroom-spezifisch)
        potData.setMist(true);

        if (!potData.hasMushroomPlant()) {
            potData.plantMushroomSpores(type);
        }

        MushroomPlant plant = potData.getMushroomPlant();
        if (plant != null) {
            plant.setQuality(TobaccoQuality.valueOf(mushroomTag.getString("Quality")));
            plant.setGrowthStage(mushroomTag.getInt("GrowthStage"));

            int ticksGrown = mushroomTag.getInt("TicksGrown");
            while (plant.getTicksGrown() < ticksGrown) {
                plant.incrementTicks();
            }

            // Mushroom-spezifisch: CurrentFlush
            if (mushroomTag.contains("CurrentFlush")) {
                plant.setCurrentFlush(mushroomTag.getInt("CurrentFlush"));
            }

            if (mushroomTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
            if (mushroomTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
            if (mushroomTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
        }
    }

    @Override
    public String getPlantTagName() {
        return "MushroomPlant";
    }

    @Override
    public boolean hasPlant(PlantPotData potData) {
        return potData.hasMushroomPlant();
    }
}
