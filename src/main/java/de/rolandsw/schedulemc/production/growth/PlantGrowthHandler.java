package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Strategy Interface für Pflanzen-Wachstum
 *
 * Ermöglicht einheitliche Behandlung aller Pflanzentypen ohne Code-Duplizierung
 */
public interface PlantGrowthHandler {

    /**
     * Prüft ob die Pflanze wachsen kann (Licht, Bedingungen, etc.)
     *
     * @param level Die Welt
     * @param pos Position des Topfes
     * @param potData Topf-Daten
     * @return true wenn Wachstum möglich
     */
    boolean canGrow(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull PlantPotData potData);

    /**
     * Führt einen Wachstums-Tick aus
     *
     * @param potData Topf-Daten
     */
    void tick(@Nonnull PlantPotData potData);

    /**
     * Aktualisiert den Block-State nach Wachstum
     *
     * @param level Die Welt
     * @param pos Position des Topfes
     * @param newStage Neues Wachstumsstadium
     * @param potData Topf-Daten (für Typ/Variant-Informationen)
     */
    void updateBlockState(@Nonnull Level level, @Nonnull BlockPos pos, int newStage, @Nonnull PlantPotData potData);

    /**
     * Gibt das aktuelle Wachstumsstadium zurück
     *
     * @param potData Topf-Daten
     * @return Aktuelles Stadium (0-7)
     */
    int getCurrentStage(@Nonnull PlantPotData potData);

    /**
     * Gibt den Pflanzentyp-Namen zurück (für Logging)
     */
    @Nonnull
    String getPlantTypeName();
}
