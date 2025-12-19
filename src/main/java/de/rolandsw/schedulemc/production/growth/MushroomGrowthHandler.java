package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/**
 * Wachstums-Handler für Pilze
 *
 * Besonderheit: Benötigt DUNKELHEIT statt Licht!
 */
public class MushroomGrowthHandler extends AbstractPlantGrowthHandler {

    @Override
    public boolean canGrow(Level level, BlockPos pos, PlantPotData potData) {
        if (level == null || !potData.hasMushroomPlant()) return false;

        // Pilze brauchen Dunkelheit - invertierte Licht-Logik!
        return isLightLevelValidForMushroom(level, pos, potData);
    }

    @Override
    public void tick(PlantPotData potData) {
        if (potData.hasMushroomPlant()) {
            potData.getMushroomPlant().tick();
        }
    }

    @Override
    public void updateBlockState(Level level, BlockPos pos, int newStage, PlantPotData potData) {
        // Pilze haben keine visuelle Block-State-Änderung
        // Wachstum ist nur intern in PlantData
    }

    @Override
    public int getCurrentStage(PlantPotData potData) {
        return potData.hasMushroomPlant() ? potData.getMushroomPlant().getGrowthStage() : -1;
    }

    @Override
    public String getPlantTypeName() {
        return "Mushroom";
    }

    /**
     * Prüft ob das Lichtlevel für Pilzwachstum geeignet ist
     */
    private boolean isLightLevelValidForMushroom(Level level, BlockPos pos, PlantPotData potData) {
        var mushroom = potData.getMushroomPlant();
        if (mushroom == null) return false;

        BlockPos checkPos = pos.above();
        int lightLevel = level.getBrightness(LightLayer.BLOCK, checkPos);

        return mushroom.isLightLevelValid(lightLevel);
    }

    /**
     * Überschreibt Standard-Licht-Prüfung (Pilze haben eigene Logik)
     */
    @Override
    protected boolean hasEnoughLight(Level level, BlockPos pos) {
        // Wird nicht verwendet - canGrow() hat eigene Implementierung
        return true;
    }
}
