package de.rolandsw.schedulemc.production.nbt;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.data.CocaPlantData;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

/**
 * Serializer für Koka-Pflanzen
 */
public class CocaPlantSerializer implements PlantSerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasCocaPlant()) return;

        CompoundTag plantTag = new CompoundTag();
        CocaPlantData plant = potData.getCocaPlant();

        plantTag.putString("Type", plant.getType().name());
        plantTag.putString("Quality", plant.getQuality().name());
        plantTag.putInt("GrowthStage", plant.getGrowthStage());
        plantTag.putInt("TicksGrown", plant.getTicksGrown());

        tag.put(getPlantTagName(), plantTag);
    }

    @Override
    public void loadPlant(PlantPotData potData, CompoundTag tag) {
        if (!tag.contains(getPlantTagName())) return;

        CompoundTag plantTag = tag.getCompound(getPlantTagName());
        CocaType type;
        try {
            type = CocaType.valueOf(plantTag.getString("Type"));
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!potData.hasCocaPlant()) {
            potData.plantCocaSeed(type);
        }

        CocaPlantData plant = potData.getCocaPlant();
        if (plant != null) {
            try {
                plant.setQuality(TobaccoQuality.valueOf(plantTag.getString("Quality")));
            } catch (IllegalArgumentException ex) {
                LOGGER.debug("CocaPlantSerializer: invalid quality '{}', keeping default",
                        plantTag.getString("Quality"), ex);
            }
            plant.setGrowthStage(plantTag.getInt("GrowthStage"));
            // CocaPlantData uses incrementTicks not setTicksGrown; skip ticksGrown restoration
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
