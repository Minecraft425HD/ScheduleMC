package de.rolandsw.schedulemc.vehicle.core.system;

import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.resources.ResourceLocation;

/**
 * Base interface for all vehicle systems in the ECS architecture.
 * Systems contain the logic that operates on components.
 */
public interface IVehicleSystem {

    /**
     * Gets the unique identifier for this system.
     * @return ResourceLocation identifying the system
     */
    ResourceLocation getSystemId();

    /**
     * Gets the execution priority of this system.
     * Lower values execute first.
     * @return Priority value (0-1000)
     */
    default int getPriority() {
        return 500;
    }

    /**
     * Called every tick for each vehicle that has the required components.
     * @param vehicle The vehicle entity to process
     * @param deltaTime Time since last tick in seconds
     */
    void tick(VehicleEntity vehicle, float deltaTime);

    /**
     * Checks if this system should process the given vehicle.
     * @param vehicle The vehicle to check
     * @return true if this system can process the vehicle
     */
    boolean canProcess(VehicleEntity vehicle);

    /**
     * Called when the system is initialized.
     */
    default void initialize() {}

    /**
     * Called when the system is shut down.
     */
    default void shutdown() {}
}
