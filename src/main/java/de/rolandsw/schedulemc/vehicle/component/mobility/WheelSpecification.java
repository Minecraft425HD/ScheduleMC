package de.rolandsw.schedulemc.vehicle.component.mobility;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines wheel specifications (size, traction, model).
 * Immutable configuration object.
 */
public class WheelSpecification {

    private static final Map<String, WheelSpecification> REGISTRY = new HashMap<>();

    // Predefined wheel types
    public static final WheelSpecification STANDARD = register("standard",
            new WheelSpecification("standard", 0.8f, 1.0f, 0.9f,
                    new ResourceLocation("schedulemc", "models/entity/wheel_standard.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/wheel_standard.png")));

    public static final WheelSpecification SPORT = register("sport",
            new WheelSpecification("sport", 0.7f, 1.2f, 1.1f,
                    new ResourceLocation("schedulemc", "models/entity/wheel_sport.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/wheel_sport.png")));

    public static final WheelSpecification OFFROAD = register("offroad",
            new WheelSpecification("offroad", 1.0f, 0.9f, 1.3f,
                    new ResourceLocation("schedulemc", "models/entity/wheel_offroad.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/wheel_offroad.png")));

    public static final WheelSpecification TRUCK = register("truck",
            new WheelSpecification("truck", 1.2f, 0.95f, 1.0f,
                    new ResourceLocation("schedulemc", "models/entity/wheel_truck.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/wheel_truck.png")));

    private final String identifier;
    private final float diameter; // In blocks
    private final float baseTraction; // 0.0 to 1.5
    private final float gripMultiplier; // 0.0 to 2.0
    private final ResourceLocation modelPath;
    private final ResourceLocation texturePath;

    public WheelSpecification(String identifier, float diameter, float baseTraction,
                             float gripMultiplier, ResourceLocation modelPath,
                             ResourceLocation texturePath) {
        this.identifier = identifier;
        this.diameter = diameter;
        this.baseTraction = baseTraction;
        this.gripMultiplier = gripMultiplier;
        this.modelPath = modelPath;
        this.texturePath = texturePath;
    }

    public String getIdentifier() {
        return identifier;
    }

    public float getDiameter() {
        return diameter;
    }

    public float getBaseTraction() {
        return baseTraction;
    }

    public float getGripMultiplier() {
        return gripMultiplier;
    }

    public ResourceLocation getModelPath() {
        return modelPath;
    }

    public ResourceLocation getTexturePath() {
        return texturePath;
    }

    // Registry methods
    public static WheelSpecification register(String id, WheelSpecification spec) {
        REGISTRY.put(id, spec);
        return spec;
    }

    public static WheelSpecification getByIdentifier(String id) {
        return REGISTRY.getOrDefault(id, STANDARD);
    }

    public static WheelSpecification custom(String id, float diameter, float traction,
                                           float grip, ResourceLocation model,
                                           ResourceLocation texture) {
        return register(id, new WheelSpecification(id, diameter, traction, grip, model, texture));
    }
}
