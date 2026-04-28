package de.rolandsw.schedulemc.production.nbt;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.data.PoppyPlantData;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

/**
 * Serializer für Mohn-Pflanzen
 */
public class PoppyPlantSerializer implements PlantSerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasPoppyPlant()) return;

        CompoundTag plantTag = new CompoundTag();
        PoppyPlantData plant = potData.getPoppyPlant();

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
        PoppyType type;
        try {
            type = PoppyType.valueOf(plantTag.getString("Type"));
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!potData.hasPoppyPlant()) {
            potData.plantPoppySeed(type);
        }

        PoppyPlantData plant = potData.getPoppyPlant();
        if (plant != null) {
            try {
                plant.setQuality(TobaccoQuality.valueOf(plantTag.getString("Quality")));
            } catch (IllegalArgumentException ex) {
                LOGGER.debug("PoppyPlantSerializer: invalid quality '{}', keeping default",
                        plantTag.getString("Quality"), ex);
            }
            plant.setGrowthStage(plantTag.getInt("GrowthStage"));
            // PoppyPlantData uses incrementTicks not setTicksGrown; skip ticksGrown restoration
        }
    }

    @Override
    public String getPlantTagName() {
        return "PoppyPlant";
    }

    @Override
    public boolean hasPlant(PlantPotData potData) {
        return potData.hasPoppyPlant();
    }
}
