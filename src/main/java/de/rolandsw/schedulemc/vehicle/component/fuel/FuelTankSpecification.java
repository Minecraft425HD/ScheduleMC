package de.rolandsw.schedulemc.vehicle.component.fuel;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines fuel tank specifications (capacity, type).
 * Immutable configuration object.
 */
public class FuelTankSpecification {

    private static final Map<String, FuelTankSpecification> REGISTRY = new HashMap<>();

    // Predefined tank sizes (in millibuckets, 1000 mb = 1 bucket)
    public static final FuelTankSpecification SMALL = register("small",
            new FuelTankSpecification("small", 16000)); // 16 buckets

    public static final FuelTankSpecification MEDIUM = register("medium",
            new FuelTankSpecification("medium", 32000)); // 32 buckets

    public static final FuelTankSpecification LARGE = register("large",
            new FuelTankSpecification("large", 64000)); // 64 buckets

    public static final FuelTankSpecification TRUCK = register("truck",
            new FuelTankSpecification("truck", 128000)); // 128 buckets

    private final String identifier;
    private final float capacity; // in millibuckets

    public FuelTankSpecification(String identifier, float capacity) {
        this.identifier = identifier;
        this.capacity = capacity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public float getCapacity() {
        return capacity;
    }

    // Registry methods
    public static FuelTankSpecification register(String id, FuelTankSpecification spec) {
        REGISTRY.put(id, spec);
        return spec;
    }

    public static FuelTankSpecification getByIdentifier(String id) {
        return REGISTRY.getOrDefault(id, MEDIUM);
    }

    public static FuelTankSpecification custom(String id, float capacity) {
        return register(id, new FuelTankSpecification(id, capacity));
    }
}
