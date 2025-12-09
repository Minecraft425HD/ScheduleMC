package de.rolandsw.schedulemc.vehicle.core.registry;

import de.rolandsw.schedulemc.vehicle.core.system.IVehicleSystem;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Central registry for vehicle systems.
 * Manages system registration, initialization, and execution order.
 */
public class SystemRegistry {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, IVehicleSystem> SYSTEMS = new HashMap<>();
    private static final List<IVehicleSystem> SORTED_SYSTEMS = new ArrayList<>();
    private static boolean needsSort = false;

    /**
     * Registers a vehicle system.
     */
    public static void register(IVehicleSystem system) {
        ResourceLocation id = system.getSystemId();
        if (SYSTEMS.containsKey(id)) {
            LOGGER.warn("System {} is already registered, overwriting!", id);
        }
        SYSTEMS.put(id, system);
        needsSort = true;
        LOGGER.debug("Registered system: {} (priority: {})", id, system.getPriority());
    }

    /**
     * Gets a system by its ID.
     */
    @Nullable
    public static IVehicleSystem getSystem(ResourceLocation id) {
        return SYSTEMS.get(id);
    }

    /**
     * Gets all systems sorted by priority.
     */
    public static List<IVehicleSystem> getSortedSystems() {
        if (needsSort) {
            SORTED_SYSTEMS.clear();
            SORTED_SYSTEMS.addAll(SYSTEMS.values());
            SORTED_SYSTEMS.sort(Comparator.comparingInt(IVehicleSystem::getPriority));
            needsSort = false;
        }
        return Collections.unmodifiableList(SORTED_SYSTEMS);
    }

    /**
     * Initializes all registered systems.
     */
    public static void initializeAll() {
        LOGGER.info("Initializing {} vehicle systems...", SYSTEMS.size());
        for (IVehicleSystem system : getSortedSystems()) {
            try {
                system.initialize();
                LOGGER.debug("Initialized system: {}", system.getSystemId());
            } catch (Exception e) {
                LOGGER.error("Failed to initialize system: {}", system.getSystemId(), e);
            }
        }
    }

    /**
     * Shuts down all registered systems.
     */
    public static void shutdownAll() {
        LOGGER.info("Shutting down vehicle systems...");
        List<IVehicleSystem> systems = new ArrayList<>(getSortedSystems());
        Collections.reverse(systems); // Shutdown in reverse order

        for (IVehicleSystem system : systems) {
            try {
                system.shutdown();
                LOGGER.debug("Shut down system: {}", system.getSystemId());
            } catch (Exception e) {
                LOGGER.error("Failed to shutdown system: {}", system.getSystemId(), e);
            }
        }
    }

    /**
     * Clears all registrations (for testing purposes).
     */
    public static void clear() {
        shutdownAll();
        SYSTEMS.clear();
        SORTED_SYSTEMS.clear();
        needsSort = false;
    }
}
