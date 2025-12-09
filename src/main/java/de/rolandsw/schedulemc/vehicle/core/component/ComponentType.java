package de.rolandsw.schedulemc.vehicle.core.component;

import net.minecraft.resources.ResourceLocation;

/**
 * Registry for component type identifiers.
 * Uses ResourceLocation for namespacing and extensibility.
 */
public class ComponentType {

    // Engine components
    public static final ResourceLocation ENGINE = createId("engine");

    // Fuel components
    public static final ResourceLocation FUEL_TANK = createId("fuel_tank");
    public static final ResourceLocation FUEL_CONSUMPTION = createId("fuel_consumption");

    // Mobility components
    public static final ResourceLocation WHEELS = createId("wheels");
    public static final ResourceLocation MOVEMENT = createId("movement");

    // Body components
    public static final ResourceLocation BODY = createId("body");
    public static final ResourceLocation BUMPER = createId("bumper");

    // Attribute components
    public static final ResourceLocation DURABILITY = createId("durability");
    public static final ResourceLocation BATTERY = createId("battery");
    public static final ResourceLocation TEMPERATURE = createId("temperature");

    // Storage components
    public static final ResourceLocation INVENTORY = createId("inventory");
    public static final ResourceLocation CARGO = createId("cargo");

    // Control components
    public static final ResourceLocation CONTROLS = createId("controls");
    public static final ResourceLocation SECURITY = createId("security");
    public static final ResourceLocation LICENSE_PLATE = createId("license_plate");

    // Ownership components
    public static final ResourceLocation OWNERSHIP = createId("ownership");

    private static ResourceLocation createId(String path) {
        return new ResourceLocation("schedulemc", "vehicle/" + path);
    }

    /**
     * Creates a custom component type identifier.
     * Useful for addon developers.
     */
    public static ResourceLocation custom(String namespace, String path) {
        return new ResourceLocation(namespace, "vehicle/" + path);
    }
}
