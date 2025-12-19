package de.rolandsw.schedulemc.production.nbt;

import de.rolandsw.schedulemc.poppy.PoppyPlant;
import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;

public class PoppyPlantSerializer implements PlantSerializer {

    @Override
    public void savePlant(PlantPotData potData, CompoundTag tag) {
        if (!potData.hasPoppyPlant()) return;

        CompoundTag poppyTag = new CompoundTag();
        PoppyPlant plant = potData.getPoppyPlant();

        poppyTag.putString("Type", plant.getType().name());
        poppyTag.putString("Quality", plant.getQuality().name());
        poppyTag.putInt("GrowthStage", plant.getGrowthStage());
        poppyTag.putInt("TicksGrown", plant.getTicksGrown());
        poppyTag.putBoolean("HasFertilizer", plant.hasFertilizer());
        poppyTag.putBoolean("HasGrowthBooster", plant.hasGrowthBooster());
        poppyTag.putBoolean("HasQualityBooster", plant.hasQualityBooster());

        tag.put(getPlantTagName(), poppyTag);
    }

    @Override
    public void loadPlant(PlantPotData potData, CompoundTag tag) {
        if (!tag.contains(getPlantTagName())) return;

        CompoundTag poppyTag = tag.getCompound(getPlantTagName());
        PoppyType type = PoppyType.valueOf(poppyTag.getString("Type"));

        if (!potData.hasPoppyPlant()) {
            potData.plantPoppySeed(type);
        }

        PoppyPlant plant = potData.getPoppyPlant();
        if (plant != null) {
            plant.setQuality(TobaccoQuality.valueOf(poppyTag.getString("Quality")));
            plant.setGrowthStage(poppyTag.getInt("GrowthStage"));

            int ticksGrown = poppyTag.getInt("TicksGrown");
            while (plant.getTicksGrown() < ticksGrown) {
                plant.incrementTicks();
            }

            if (poppyTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
            if (poppyTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
            if (poppyTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
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
