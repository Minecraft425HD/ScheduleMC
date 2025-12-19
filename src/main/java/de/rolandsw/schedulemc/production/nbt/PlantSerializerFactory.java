package de.rolandsw.schedulemc.production.nbt;

import de.rolandsw.schedulemc.production.data.PlantPotData;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory für PlantSerializer
 *
 * Analog zu PlantGrowthHandlerFactory
 * Eliminiert Duplikation in PlantPotBlockEntity NBT-Serialisierung
 */
public class PlantSerializerFactory {

    private static final List<PlantSerializer> SERIALIZERS = new ArrayList<>();

    static {
        SERIALIZERS.add(new TobaccoPlantSerializer());
        // Note: Additional plant serializers can be added here as they are implemented
        // Template for new serializers:
        // SERIALIZERS.add(new CannabisPlantSerializer());
        // SERIALIZERS.add(new CocaPlantSerializer());
        // SERIALIZERS.add(new PoppyPlantSerializer());
        // SERIALIZERS.add(new MushroomPlantSerializer());
    }

    /**
     * Gibt alle verfügbaren Serializer zurück
     */
    public static List<PlantSerializer> getAllSerializers() {
        return SERIALIZERS;
    }

    /**
     * Gibt den Serializer für die aktuelle Pflanze zurück
     *
     * @return Der passende Serializer oder null wenn keine Pflanze vorhanden
     */
    public static PlantSerializer getSerializer(PlantPotData potData) {
        for (PlantSerializer serializer : SERIALIZERS) {
            if (serializer.hasPlant(potData)) {
                return serializer;
            }
        }
        return null;
    }
}
