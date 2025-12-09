package de.rolandsw.schedulemc.vehicle.component.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines engine specifications (power, RPM, fuel consumption, etc.).
 * Immutable configuration object.
 */
public class EngineSpecification {

    private static final Map<String, EngineSpecification> REGISTRY = new HashMap<>();

    // Predefined engine types
    public static final EngineSpecification DEFAULT = register("default",
            new EngineSpecification("default", 100.0f, 5000.0f, 0.5f, 4));

    public static final EngineSpecification INLINE_THREE = register("inline_three",
            new EngineSpecification("inline_three", 75.0f, 6000.0f, 0.4f, 3));

    public static final EngineSpecification INLINE_SIX = register("inline_six",
            new EngineSpecification("inline_six", 200.0f, 7000.0f, 0.8f, 6));

    public static final EngineSpecification TRUCK_V8 = register("truck_v8",
            new EngineSpecification("truck_v8", 350.0f, 4500.0f, 1.2f, 8));

    private final String identifier;
    private final float maxPower; // Horsepower
    private final float maxRpm;
    private final float baseFuelConsumption; // Liters per second at max RPM
    private final int cylinderCount;

    public EngineSpecification(String identifier, float maxPower, float maxRpm,
                              float baseFuelConsumption, int cylinderCount) {
        this.identifier = identifier;
        this.maxPower = maxPower;
        this.maxRpm = maxRpm;
        this.baseFuelConsumption = baseFuelConsumption;
        this.cylinderCount = cylinderCount;
    }

    public String getIdentifier() {
        return identifier;
    }

    public float getMaxPower() {
        return maxPower;
    }

    public float getMaxRpm() {
        return maxRpm;
    }

    public float getBaseFuelConsumption() {
        return baseFuelConsumption;
    }

    public int getCylinderCount() {
        return cylinderCount;
    }

    // Registry methods
    public static EngineSpecification register(String id, EngineSpecification spec) {
        REGISTRY.put(id, spec);
        return spec;
    }

    public static EngineSpecification getByIdentifier(String id) {
        return REGISTRY.getOrDefault(id, DEFAULT);
    }

    public static EngineSpecification custom(String id, float maxPower, float maxRpm,
                                            float fuelConsumption, int cylinders) {
        return register(id, new EngineSpecification(id, maxPower, maxRpm, fuelConsumption, cylinders));
    }
}
