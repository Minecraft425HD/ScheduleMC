package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.tobacco.blocks.GrowLightSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
     *
     * FIX: Prüft zuerst direkt auf GrowLightSlabBlock (2-3 Blöcke über Topf)
     * und nutzt dessen volle Lichtemission. Sonst Fallback auf getBrightness().
     */
    protected boolean hasEnoughLight(Level level, BlockPos pos) {
        // Config: Ist Licht-Anforderung aktiviert?
        if (!ModConfigHandler.TOBACCO.REQUIRE_LIGHT_FOR_GROWTH.get()) {
            return true;  // Kein Licht benötigt
        }

        // Minimales Lichtlevel aus Config
        int minLight = ModConfigHandler.TOBACCO.MIN_LIGHT_LEVEL.get();

        // FIX: Prüfe direkt auf GrowLightSlabBlock an Positionen 2 und 3
        // (Pflanze: Block 1+2, Grow Light kann bei 2 oder 3 sein)
        for (int yOffset = 2; yOffset <= 3; yOffset++) {
            BlockPos checkPos = pos.above(yOffset);
            BlockState checkState = level.getBlockState(checkPos);
            Block checkBlock = checkState.getBlock();

            if (checkBlock instanceof GrowLightSlabBlock growLight) {
                // Grow Light gefunden! Nutze volle Lichtemission direkt vom Block
                int lightLevel = growLight.getLightEmission(checkState, level, checkPos);
                return lightLevel >= minLight;
            }
        }

        // Kein Grow Light → Fallback auf normale Licht-Prüfung
        // (z.B. Sonnenlicht, Fackeln, etc.)
        BlockPos plantPos = pos.above(2);
        int lightLevel = level.getBrightness(LightLayer.BLOCK, plantPos);

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
