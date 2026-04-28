package de.rolandsw.schedulemc.production.nbt;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.data.CoffeePlantData;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

/**
 * Serializer für Kaffee-Pflanzen
 */
public class CoffeePlantSerializer implements PlantSerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasCoffeePlant()) return;

        CompoundTag plantTag = new CompoundTag();
        CoffeePlantData plant = potData.getCoffeePlant();

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
        CoffeeType type;
        try {
            type = CoffeeType.valueOf(plantTag.getString("Type"));
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!potData.hasCoffeePlant()) {
            potData.plantCoffeeSeed(type);
        }

        CoffeePlantData plant = potData.getCoffeePlant();
        if (plant != null) {
            try {
                plant.setQuality(CoffeeQuality.valueOf(plantTag.getString("Quality")));
            } catch (IllegalArgumentException ex) {
                LOGGER.debug("CoffeePlantSerializer: invalid quality '{}', keeping default",
                        plantTag.getString("Quality"), ex);
            }
            plant.setGrowthStage(plantTag.getInt("GrowthStage"));
            plant.setTicksGrown(plantTag.getInt("TicksGrown"));
        }
    }

    @Override
    public String getPlantTagName() {
        return "CoffeePlant";
    }

    @Override
    public boolean hasPlant(PlantPotData potData) {
        return potData.hasCoffeePlant();
    }
}
