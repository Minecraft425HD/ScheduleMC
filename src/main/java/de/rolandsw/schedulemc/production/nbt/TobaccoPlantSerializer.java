package de.rolandsw.schedulemc.production.nbt;

import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPlantData;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.nbt.CompoundTag;

/**
 * Serializer für Tabak-Pflanzen
 */
public class TobaccoPlantSerializer implements PlantSerializer {

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasTobaccoPlant()) return;

        CompoundTag plantTag = new CompoundTag();
        TobaccoPlantData plant = potData.getPlant();

        plantTag.putString("Type", plant.getType().name());
        plantTag.putString("Quality", plant.getQuality().name());
        plantTag.putInt("GrowthStage", plant.getGrowthStage());
        plantTag.putInt("TicksGrown", plant.getTicksGrown());
        plantTag.putBoolean("HasFertilizer", plant.hasFertilizer());
        plantTag.putBoolean("HasGrowthBooster", plant.hasGrowthBooster());
        plantTag.putBoolean("HasQualityBooster", plant.hasQualityBooster());

        tag.put(getPlantTagName(), plantTag);
    }

    @Override
    public void loadPlant(PlantPotData potData, CompoundTag tag) {
        if (!tag.contains(getPlantTagName())) return;

        CompoundTag plantTag = tag.getCompound(getPlantTagName());
        TobaccoType type;
        try {
            type = TobaccoType.valueOf(plantTag.getString("Type"));
        } catch (IllegalArgumentException e) {
            return; // Ungueltige NBT-Daten - sicher ignorieren
        }

        if (!potData.hasTobaccoPlant()) {
            potData.plantSeed(type);
        }

        TobaccoPlantData plant = potData.getPlant();
        if (plant != null) {
            try {
                plant.setQuality(TobaccoQuality.valueOf(plantTag.getString("Quality")));
            } catch (IllegalArgumentException e) {
                // Ungueltige Qualitaet - Standard beibehalten
            }
            plant.setGrowthStage(plantTag.getInt("GrowthStage"));

            int ticksGrown = plantTag.getInt("TicksGrown");
            while (plant.getTicksGrown() < ticksGrown) {
                plant.incrementTicks();
            }

            if (plantTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
            if (plantTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
            if (plantTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
        }
    }

    @Override
    public String getPlantTagName() {
        return "Plant";
    }

    @Override
    public boolean hasPlant(PlantPotData potData) {
        return potData.hasTobaccoPlant();
    }
}
