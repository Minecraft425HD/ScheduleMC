package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/**
 * Abstrakte Basis-Implementierung für Standard-Pflanzen-Wachstum
 *
 * Enthält gemeinsame Logik:
 * - Licht-Prüfung
 * - Standard-Wachstumsbedingungen
 */
public abstract class AbstractPlantGrowthHandler implements PlantGrowthHandler {

    @Override
    public boolean canGrow(Level level, BlockPos pos, PlantPotData potData) {
        if (level == null) return false;

        // Standard-Licht-Prüfung (kann von Subklassen überschrieben werden)
        return hasEnoughLight(level, pos);
    }

    /**
     * Prüft ob genug Licht für Wachstum vorhanden ist
     */
    protected boolean hasEnoughLight(Level level, BlockPos pos) {
        // Config: Ist Licht-Anforderung aktiviert?
        if (!ModConfigHandler.TOBACCO.REQUIRE_LIGHT_FOR_GROWTH.get()) {
            return true;  // Kein Licht benötigt
        }

        // Prüfe Lichtlevel über der Pflanze (2 Blöcke über Topf)
        BlockPos plantPos = pos.above(2);
        int lightLevel = level.getBrightness(LightLayer.BLOCK, plantPos);

        // Minimales Lichtlevel aus Config
        int minLight = ModConfigHandler.TOBACCO.MIN_LIGHT_LEVEL.get();

        return lightLevel >= minLight;
    }

    /**
     * Standard-Implementierung für Wachstumsstadium
     * Kann von Subklassen überschrieben werden
     */
    @Override
    public abstract int getCurrentStage(PlantPotData potData);

    /**
     * Standard-Implementierung für Tick
     * Kann von Subklassen überschrieben werden
     */
    @Override
    public abstract void tick(PlantPotData potData);
}
