package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.vehicle.IVehicleAPI;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of IVehicleAPI
 *
 * Wrapper für Vehicle-System mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class VehicleAPIImpl implements IVehicleAPI {

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public EntityGenericVehicle spawnVehicle(ServerLevel level, BlockPos position, String vehicleType) {
        if (level == null || position == null || vehicleType == null) {
            throw new IllegalArgumentException("level, position and vehicleType cannot be null");
        }
        // Note: Actual implementation would require VehicleRegistry and EntityType registration
        // This is a placeholder that would need to be implemented with proper Vehicle spawning
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVehicleOwner(EntityGenericVehicle vehicle, UUID ownerUUID) {
        if (vehicle == null || ownerUUID == null) {
            throw new IllegalArgumentException("vehicle and ownerUUID cannot be null");
        }
        vehicle.setOwner(ownerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public UUID getVehicleOwner(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        return vehicle.getOwner();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean refuelVehicle(EntityGenericVehicle vehicle, double amount) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, got: " + amount);
        }

        double currentFuel = getFuelLevel(vehicle);
        double capacity = getFuelCapacity(vehicle);

        if (currentFuel >= capacity) {
            return false; // Tank already full
        }

        double newFuel = Math.min(currentFuel + amount, capacity);
        vehicle.setFuel(newFuel);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFuelLevel(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        return vehicle.getFuel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFuelCapacity(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        return vehicle.getFuelCapacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntityGenericVehicle> getPlayerVehicles(ServerLevel level, UUID ownerUUID) {
        if (level == null || ownerUUID == null) {
            throw new IllegalArgumentException("level and ownerUUID cannot be null");
        }

        return Collections.unmodifiableList(
            level.getAllEntities().stream()
                .filter(entity -> entity instanceof EntityGenericVehicle)
                .map(entity -> (EntityGenericVehicle) entity)
                .filter(vehicle -> ownerUUID.equals(vehicle.getOwner()))
                .collect(Collectors.toList())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVehicle(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        vehicle.discard();
    }
}
