package de.rolandsw.schedulemc.vehicle.core.registry;

import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * Central registry for vehicle component types.
 * Allows dynamic registration of component factories for extensibility.
 */
public class ComponentRegistry {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, ComponentFactory<?>> FACTORIES = new HashMap<>();
    private static final Set<ResourceLocation> REGISTERED_TYPES = new HashSet<>();

    /**
     * Registers a component type with its factory.
     */
    public static <T extends IVehicleComponent> void register(
            ResourceLocation type,
            ComponentFactory<T> factory
    ) {
        if (FACTORIES.containsKey(type)) {
            LOGGER.warn("Component type {} is already registered, overwriting!", type);
        }
        FACTORIES.put(type, factory);
        REGISTERED_TYPES.add(type);
        LOGGER.debug("Registered component type: {}", type);
    }

    /**
     * Creates a component instance from NBT data.
     */
    @Nullable
    public static IVehicleComponent createFromNbt(CompoundTag tag) {
        if (!tag.contains("ComponentType")) {
            LOGGER.error("Component NBT missing ComponentType field!");
            return null;
        }

        ResourceLocation type = new ResourceLocation(tag.getString("ComponentType"));
        ComponentFactory<?> factory = FACTORIES.get(type);

        if (factory == null) {
            LOGGER.error("Unknown component type: {}", type);
            return null;
        }

        try {
            IVehicleComponent component = factory.create();
            component.readFromNbt(tag);
            return component;
        } catch (Exception e) {
            LOGGER.error("Failed to create component of type {}", type, e);
            return null;
        }
    }

    /**
     * Checks if a component type is registered.
     */
    public static boolean isRegistered(ResourceLocation type) {
        return REGISTERED_TYPES.contains(type);
    }

    /**
     * Gets all registered component types.
     */
    public static Set<ResourceLocation> getRegisteredTypes() {
        return Collections.unmodifiableSet(REGISTERED_TYPES);
    }

    /**
     * Clears all registrations (for testing purposes).
     */
    public static void clear() {
        FACTORIES.clear();
        REGISTERED_TYPES.clear();
    }

    /**
     * Functional interface for component factories.
     */
    @FunctionalInterface
    public interface ComponentFactory<T extends IVehicleComponent> {
        T create();
    }
}
