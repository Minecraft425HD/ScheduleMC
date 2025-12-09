package de.rolandsw.schedulemc.vehicle.component.body;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines body/chassis specifications.
 * Immutable configuration object.
 */
public class BodySpecification {

    private static final Map<String, BodySpecification> REGISTRY = new HashMap<>();

    // Predefined body types
    public static final BodySpecification SEDAN = register("sedan",
            new BodySpecification("sedan", 4, 1.0f, 0.85f,
                    new ResourceLocation("schedulemc", "models/entity/body_sedan.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/body_sedan.png"),
                    1.625f,  // Width: 26 pixels / 16 = 1.625 blocks (wings at ±13)
                    1.375f,  // Height: 22 pixels / 16 = 1.375 blocks (y: 2 to 24)
                    2.9375f  // Length: 47 pixels / 16 = 2.9375 blocks (z: -24 to +23)
            ));

    public static final BodySpecification SPORT = register("sport",
            new BodySpecification("sport", 2, 0.8f, 0.95f,
                    new ResourceLocation("schedulemc", "models/entity/body_sport.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/body_sport.png")));

    public static final BodySpecification SUV = register("suv",
            new BodySpecification("suv", 5, 1.3f, 0.75f,
                    new ResourceLocation("schedulemc", "models/entity/body_suv.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/body_suv.png")));

    public static final BodySpecification TRUCK = register("truck",
            new BodySpecification("truck", 2, 1.8f, 0.65f,
                    new ResourceLocation("schedulemc", "models/entity/body_truck.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/body_truck.png")));

    public static final BodySpecification TRANSPORTER = register("transporter",
            new BodySpecification("transporter", 3, 1.5f, 0.70f,
                    new ResourceLocation("schedulemc", "models/entity/body_transporter.obj"),
                    new ResourceLocation("schedulemc", "textures/entity/body_transporter.png")));

    private final String identifier;
    private final int passengerSeats;
    private final float weight; // Multiplier
    private final float aerodynamics; // 0.0 to 1.0, higher is better
    private final ResourceLocation modelPath;
    private final ResourceLocation texturePath;
    private final float hitboxWidth;  // Width (X-axis) in blocks
    private final float hitboxHeight; // Height (Y-axis) in blocks
    private final float hitboxLength; // Length (Z-axis) in blocks

    public BodySpecification(String identifier, int passengerSeats, float weight,
                            float aerodynamics, ResourceLocation modelPath,
                            ResourceLocation texturePath) {
        this(identifier, passengerSeats, weight, aerodynamics, modelPath, texturePath, 2.0f, 1.5f, 2.0f);
    }

    public BodySpecification(String identifier, int passengerSeats, float weight,
                            float aerodynamics, ResourceLocation modelPath,
                            ResourceLocation texturePath, float hitboxWidth,
                            float hitboxHeight, float hitboxLength) {
        this.identifier = identifier;
        this.passengerSeats = passengerSeats;
        this.weight = weight;
        this.aerodynamics = aerodynamics;
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.hitboxLength = hitboxLength;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getPassengerSeats() {
        return passengerSeats;
    }

    public float getWeight() {
        return weight;
    }

    public float getAerodynamics() {
        return aerodynamics;
    }

    public ResourceLocation getModelPath() {
        return modelPath;
    }

    public ResourceLocation getTexturePath() {
        return texturePath;
    }

    public float getHitboxWidth() {
        return hitboxWidth;
    }

    public float getHitboxHeight() {
        return hitboxHeight;
    }

    public float getHitboxLength() {
        return hitboxLength;
    }

    // Registry methods
    public static BodySpecification register(String id, BodySpecification spec) {
        REGISTRY.put(id, spec);
        return spec;
    }

    public static BodySpecification getByIdentifier(String id) {
        return REGISTRY.getOrDefault(id, SEDAN);
    }

    public static BodySpecification custom(String id, int seats, float weight,
                                          float aero, ResourceLocation model,
                                          ResourceLocation texture) {
        return register(id, new BodySpecification(id, seats, weight, aero, model, texture));
    }

    public static BodySpecification custom(String id, int seats, float weight,
                                          float aero, ResourceLocation model,
                                          ResourceLocation texture, float hitboxWidth,
                                          float hitboxHeight, float hitboxLength) {
        return register(id, new BodySpecification(id, seats, weight, aero, model, texture,
                hitboxWidth, hitboxHeight, hitboxLength));
    }
}
