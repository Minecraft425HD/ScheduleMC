package de.rolandsw.schedulemc.production.nbt;

import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.nbt.CompoundTag;

/**
 * Strategy Interface für Pflanzen-NBT-Serialisierung
 *
 * Eliminiert Duplikation in PlantPotBlockEntity save/load Methoden
 * Analog zum PlantGrowthHandler Pattern
 */
public interface PlantSerializer {

    /**
     * Speichert Pflanzendaten in NBT
     *
     * @param potData Die Pflanzentopf-Daten
     * @param tag Der NBT-Tag zum Speichern
     */
    void savePlant(PlantPotData potData, CompoundTag tag);

    /**
     * Lädt Pflanzendaten aus NBT
     *
     * @param potData Die Pflanzentopf-Daten
     * @param tag Der NBT-Tag zum Laden
     */
    void loadPlant(PlantPotData potData, CompoundTag tag);

    /**
     * Gibt den NBT-Tag-Namen für diese Pflanze zurück
     */
    String getPlantTagName();

    /**
     * Prüft ob diese Pflanze vorhanden ist
     */
    boolean hasPlant(PlantPotData potData);
}
