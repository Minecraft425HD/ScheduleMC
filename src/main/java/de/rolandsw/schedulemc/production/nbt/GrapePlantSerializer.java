package de.rolandsw.schedulemc.production.nbt;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.wine.WineType;
import de.rolandsw.schedulemc.wine.data.GrapePlantData;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

/**
 * Serializer für Weinreben
 */
public class GrapePlantSerializer implements PlantSerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasGrapePlant()) return;

        CompoundTag plantTag = new CompoundTag();
        GrapePlantData plant = potData.getGrapePlant();

        plantTag.putString("Type", plant.getType().name());
        plantTag.putInt("GrowthStage", plant.getGrowthStage());
        plantTag.putInt("TicksGrown", plant.getTicksGrown());

        tag.put(getPlantTagName(), plantTag);
    }

    @Override
    public void loadPlant(PlantPotData potData, CompoundTag tag) {
        if (!tag.contains(getPlantTagName())) return;

        CompoundTag plantTag = tag.getCompound(getPlantTagName());
        WineType type;
        try {
            type = WineType.valueOf(plantTag.getString("Type"));
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!potData.hasGrapePlant()) {
            potData.plantGrapeSeed(type);
        }

        GrapePlantData plant = potData.getGrapePlant();
        if (plant != null) {
            plant.setGrowthStage(plantTag.getInt("GrowthStage"));
            plant.setTicksGrown(plantTag.getInt("TicksGrown"));
        }
    }

    @Override
    public String getPlantTagName() {
        return "GrapePlant";
    }

    @Override
    public boolean hasPlant(PlantPotData potData) {
        return potData.hasGrapePlant();
    }
}
