package de.rolandsw.schedulemc.production.nbt;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.data.CannabisPlantData;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

/**
 * Serializer für Cannabis-Pflanzen
 */
public class CannabisPlantSerializer implements PlantSerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasCannabisPlant()) return;

        CompoundTag plantTag = new CompoundTag();
        CannabisPlantData plant = potData.getCannabisPlant();

        plantTag.putString("Strain", plant.getStrain().name());
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
        CannabisStrain strain;
        try {
            strain = CannabisStrain.valueOf(plantTag.getString("Strain"));
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!potData.hasCannabisPlant()) {
            potData.plantCannabisSeed(strain);
        }

        CannabisPlantData plant = potData.getCannabisPlant();
        if (plant != null) {
            try {
                plant.setQuality(CannabisQuality.valueOf(plantTag.getString("Quality")));
            } catch (IllegalArgumentException ex) {
                LOGGER.debug("CannabisPlantSerializer: invalid quality '{}', keeping default",
                        plantTag.getString("Quality"), ex);
            }
            plant.setGrowthStage(plantTag.getInt("GrowthStage"));
            // CannabisPlantData uses incrementTicks not setTicksGrown; restore via direct field approach not available
            // Skip ticksGrown restoration - growthStage is more important

            if (plantTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
            if (plantTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
            if (plantTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
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
