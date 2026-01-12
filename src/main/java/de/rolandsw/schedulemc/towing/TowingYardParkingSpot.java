package de.rolandsw.schedulemc.towing;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Represents a parking spot at a towing yard
 */
public class TowingYardParkingSpot {
    private final UUID spotId;
    private final BlockPos location;
    private final UUID towingYardPlotId;
    private boolean occupied;
    private UUID vehicleEntityId;
    private UUID ownerPlayerId;
    private double owedAmount;
    private int originalDamage;
    private long towedTimestamp;
    private boolean engineWasRunning;

    public TowingYardParkingSpot(UUID spotId, BlockPos location, UUID towingYardPlotId) {
        this.spotId = spotId;
        this.location = location;
        this.towingYardPlotId = towingYardPlotId;
        this.occupied = false;
    }

    /**
     * Parks a vehicle at this spot
     */
    public void parkVehicle(UUID vehicleId, UUID ownerId, double cost, int damage, boolean engineRunning) {
        this.occupied = true;
        this.vehicleEntityId = vehicleId;
        this.ownerPlayerId = ownerId;
        this.owedAmount = cost;
        this.originalDamage = damage;
        this.towedTimestamp = System.currentTimeMillis();
        this.engineWasRunning = engineRunning;
    }

    /**
     * Removes vehicle from spot (after collection)
     */
    public void clear() {
        this.occupied = false;
        this.vehicleEntityId = null;
        this.ownerPlayerId = null;
        this.owedAmount = 0;
        this.originalDamage = 0;
        this.towedTimestamp = 0;
        this.engineWasRunning = false;
    }

    // Getters
    public UUID getSpotId() {
        return spotId;
    }

    public BlockPos getLocation() {
        return location;
    }

    public UUID getTowingYardPlotId() {
        return towingYardPlotId;
    }

    public boolean isOccupied() {
        return occupied;
    }

    @Nullable
    public UUID getVehicleEntityId() {
        return vehicleEntityId;
    }

    @Nullable
    public UUID getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public double getOwedAmount() {
        return owedAmount;
    }

    public int getOriginalDamage() {
        return originalDamage;
    }

    public long getTowedTimestamp() {
        return towedTimestamp;
    }

    public boolean wasEngineRunning() {
        return engineWasRunning;
    }
}
