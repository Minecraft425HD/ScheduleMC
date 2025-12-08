package de.rolandsw.schedulemc.vehicle.core.system;

import net.minecraft.resources.ResourceLocation;

/**
 * Registry for system type identifiers.
 */
public class SystemType {

    // Core systems
    public static final ResourceLocation MOVEMENT = createId("movement");
    public static final ResourceLocation FUEL = createId("fuel");
    public static final ResourceLocation DAMAGE = createId("damage");
    public static final ResourceLocation TEMPERATURE = createId("temperature");
    public static final ResourceLocation BATTERY = createId("battery");

    // Interaction systems
    public static final ResourceLocation CONTROLS = createId("controls");
    public static final ResourceLocation SECURITY = createId("security");

    // Client systems
    public static final ResourceLocation RENDERING = createId("rendering");
    public static final ResourceLocation SOUND = createId("sound");

    private static ResourceLocation createId(String path) {
        return new ResourceLocation("schedulemc", "system/" + path);
    }

    /**
     * Creates a custom system type identifier.
     */
    public static ResourceLocation custom(String namespace, String path) {
        return new ResourceLocation(namespace, "system/" + path);
    }
}
