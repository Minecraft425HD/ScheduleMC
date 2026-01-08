package de.rolandsw.schedulemc.production.growth;

import de.rolandsw.schedulemc.production.data.PlantPotData;

import javax.annotation.Nullable;

import javax.annotation.Nullable;

/**
 * Factory für PlantGrowthHandler
 *
 * Wählt den richtigen Handler basierend auf Pflanzentyp
 *
 * Performance-Optimierung:
 * - Handler sind stateless und können wiederverwendet werden
 * - Singleton-Instanzen statt neue Instanzen pro Aufruf
 * - Eliminiert Objekt-Allokation und Garbage Collection Overhead
 */
public class PlantGrowthHandlerFactory {

    // Performance-Optimierung: Singleton-Instanzen der Handler (stateless)
    private static final TobaccoGrowthHandler TOBACCO_HANDLER = new TobaccoGrowthHandler();
    private static final CannabisGrowthHandler CANNABIS_HANDLER = new CannabisGrowthHandler();
    private static final CocaGrowthHandler COCA_HANDLER = new CocaGrowthHandler();
    private static final PoppyGrowthHandler POPPY_HANDLER = new PoppyGrowthHandler();
    private static final MushroomGrowthHandler MUSHROOM_HANDLER = new MushroomGrowthHandler();

    /**
     * Gibt den passenden Handler für die Pflanze im Topf zurück
     *
     * @param potData Topf-Daten
     * @return Handler oder null wenn keine Pflanze vorhanden
     */
    @Nullable
    public static PlantGrowthHandler getHandler(PlantPotData potData) {
        if (potData.hasTobaccoPlant()) {
            return TOBACCO_HANDLER;
        }
        if (potData.hasCannabisPlant()) {
            return CANNABIS_HANDLER;
        }
        if (potData.hasCocaPlant()) {
            return COCA_HANDLER;
        }
        if (potData.hasPoppyPlant()) {
            return POPPY_HANDLER;
        }
        if (potData.hasMushroomPlant()) {
            return MUSHROOM_HANDLER;
        }

        return null; // Keine Pflanze vorhanden
    }

    /**
     * Prüft ob ein Handler für diese Pflanze existiert
     */
    public static boolean hasHandler(PlantPotData potData) {
        return getHandler(potData) != null;
    }
}
