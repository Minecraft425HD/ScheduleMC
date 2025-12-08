package de.rolandsw.schedulemc.vehicle.core.system;

import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.core.registry.SystemRegistry;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages execution of all vehicle systems.
 * Processes all vehicles in the world each tick.
 */
public class SystemManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final float TICK_DELTA = 0.05f; // 20 ticks per second

    /**
     * Processes all vehicles in the given world.
     * Called from a tick event handler.
     */
    public static void tickWorld(Level world) {
        if (world.isClientSide()) {
            tickClientSystems(world);
        } else {
            tickServerSystems(world);
        }
    }

    /**
     * Ticks server-side systems.
     */
    private static void tickServerSystems(Level world) {
        List<VehicleEntity> vehicles = getVehiclesInWorld(world);

        for (IVehicleSystem system : SystemRegistry.getSortedSystems()) {
            for (VehicleEntity vehicle : vehicles) {
                if (system.canProcess(vehicle)) {
                    try {
                        system.tick(vehicle, TICK_DELTA);
                    } catch (Exception e) {
                        LOGGER.error("Error in system {} for vehicle {}",
                                system.getSystemId(), vehicle.getId(), e);
                    }
                }
            }
        }
    }

    /**
     * Ticks client-side systems.
     */
    private static void tickClientSystems(Level world) {
        List<VehicleEntity> vehicles = getVehiclesInWorld(world);

        for (IVehicleSystem system : SystemRegistry.getSortedSystems()) {
            // Only process client systems on client
            if (isClientSystem(system)) {
                for (VehicleEntity vehicle : vehicles) {
                    if (system.canProcess(vehicle)) {
                        try {
                            system.tick(vehicle, TICK_DELTA);
                        } catch (Exception e) {
                            LOGGER.error("Error in client system {} for vehicle {}",
                                    system.getSystemId(), vehicle.getId(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets all vehicles in the world.
     */
    private static List<VehicleEntity> getVehiclesInWorld(Level world) {
        List<VehicleEntity> vehicles = new ArrayList<>();

        for (net.minecraft.world.entity.Entity entity : world.getEntities().getAll()) {
            if (entity instanceof VehicleEntity) {
                vehicles.add((VehicleEntity) entity);
            }
        }

        return vehicles;
    }

    /**
     * Checks if a system is client-side only.
     */
    private static boolean isClientSystem(IVehicleSystem system) {
        String id = system.getSystemId().getPath();
        return id.contains("rendering") || id.contains("sound") || id.contains("particle");
    }
}
