package de.rolandsw.schedulemc.production.nbt;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.mushroom.data.MushroomPlantData;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

/**
 * Serializer für Pilzkulturen
 */
public class MushroomPlantSerializer implements PlantSerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasMushroomPlant()) return;

        CompoundTag plantTag = new CompoundTag();
        MushroomPlantData plant = potData.getMushroomPlant();

        plantTag.putString("Type", plant.getType().name());
        plantTag.putString("Quality", plant.getQuality().name());
        plantTag.putInt("GrowthStage", plant.getGrowthStage());
        plantTag.putInt("TicksGrown", plant.getTicksGrown());
        plantTag.putInt("CurrentFlush", plant.getCurrentFlush());

        tag.put(getPlantTagName(), plantTag);
    }

    @Override
    public void loadPlant(PlantPotData potData, CompoundTag tag) {
        if (!tag.contains(getPlantTagName())) return;

        CompoundTag plantTag = tag.getCompound(getPlantTagName());
        MushroomType type;
        try {
            type = MushroomType.valueOf(plantTag.getString("Type"));
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!potData.hasMushroomPlant()) {
            // Mushrooms require hasMist to be true for plantMushroomSpore to succeed
            potData.setMist(true);
            potData.plantMushroomSpore(type);
        }

        MushroomPlantData plant = potData.getMushroomPlant();
        if (plant != null) {
            try {
                plant.setQuality(TobaccoQuality.valueOf(plantTag.getString("Quality")));
            } catch (IllegalArgumentException ex) {
                LOGGER.debug("MushroomPlantSerializer: invalid quality '{}', keeping default",
                        plantTag.getString("Quality"), ex);
            }
            plant.setGrowthStage(plantTag.getInt("GrowthStage"));
            plant.setCurrentFlush(plantTag.getInt("CurrentFlush"));
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
