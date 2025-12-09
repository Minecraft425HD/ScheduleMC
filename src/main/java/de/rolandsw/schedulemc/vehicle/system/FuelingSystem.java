package de.rolandsw.schedulemc.vehicle.system;

import de.rolandsw.schedulemc.vehicle.blocks.entity.GasStationBlockEntity;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import de.rolandsw.schedulemc.vehicle.fuel.GasStationRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;

import java.util.UUID;

/**
 * System for handling vehicle fueling at gas stations.
 * Manages fuel transfer and bill creation.
 */
public class FuelingSystem {

    private static final int FUELING_RATE = 100; // mB per interaction
    private static final int MIN_FUEL_TRANSFER = 10; // Minimum mB to transfer

    /**
     * Attempts to fuel a vehicle at a gas station.
     *
     * @param vehicle The vehicle to fuel
     * @param gasStation The gas station block entity
     * @param player The player fueling the vehicle
     * @return The interaction result
     */
    public static InteractionResult tryFuelVehicle(VehicleEntity vehicle, GasStationBlockEntity gasStation, Player player) {
        // Get fuel tank component
        FuelTankComponent fuelTank = vehicle.getComponent(ComponentType.FUEL_TANK, FuelTankComponent.class);

        if (fuelTank == null) {
            player.sendSystemMessage(Component.literal("This vehicle has no fuel tank!"));
            return InteractionResult.FAIL;
        }

        // Check if vehicle needs fuel
        float currentFuel = fuelTank.getCurrentAmount();
        float maxFuel = fuelTank.getSpecification().getCapacity();

        if (currentFuel >= maxFuel) {
            player.sendSystemMessage(Component.literal("Vehicle fuel tank is already full!"));
            return InteractionResult.FAIL;
        }

        // Calculate how much fuel to transfer
        int spaceInTank = (int) ((maxFuel - currentFuel) * 1000); // Convert to mB
        int fuelToTransfer = Math.min(FUELING_RATE, spaceInTank);
        fuelToTransfer = Math.min(fuelToTransfer, gasStation.getFuelAmount());

        if (fuelToTransfer < MIN_FUEL_TRANSFER) {
            if (gasStation.getFuelAmount() < MIN_FUEL_TRANSFER) {
                player.sendSystemMessage(Component.literal("Gas station is out of fuel!"));
            } else {
                player.sendSystemMessage(Component.literal("Vehicle fuel tank is full!"));
            }
            return InteractionResult.FAIL;
        }

        // Transfer fuel
        gasStation.removeFuel(fuelToTransfer);
        fuelTank.fill(Fluids.WATER, fuelToTransfer / 1000.0f); // Convert back to liters (using WATER as fuel placeholder)

        // Calculate cost and create bill
        double cost = gasStation.calculateCost(fuelToTransfer);
        UUID stationId = gasStation.getStationId();

        if (stationId != null) {
            FuelBillManager.addBill(player.getUUID(), stationId, fuelToTransfer, cost);
        }

        // Notify player
        String stationName = gasStation.getStationDisplayName();
        double liters = fuelToTransfer / 1000.0;
        player.sendSystemMessage(Component.literal(
            String.format("Fueled %.2f L at %s. Cost: %.2f€ (added to bill)",
                liters, stationName, cost)
        ));

        return InteractionResult.SUCCESS;
    }

    /**
     * Automatically detects nearby gas stations and attempts to fuel the vehicle.
     * This can be called from a keybind or automatic system.
     *
     * @param vehicle The vehicle to fuel
     * @param player The player in the vehicle
     * @return True if fueling was successful
     */
    public static boolean autoFuel(VehicleEntity vehicle, Player player) {
        // Search for nearby gas stations in a 5 block radius
        // This is a simplified version - you might want to implement proper range checking

        // For now, we'll just return false as this requires more complex position checking
        // This can be implemented later with proper block scanning
        return false;
    }

    /**
     * Checks if a vehicle can be fueled (has fuel tank component).
     *
     * @param vehicle The vehicle to check
     * @return True if the vehicle can be fueled
     */
    public static boolean canFuel(VehicleEntity vehicle) {
        return vehicle.hasComponent(ComponentType.FUEL_TANK);
    }

    /**
     * Gets the current fuel percentage of a vehicle.
     *
     * @param vehicle The vehicle
     * @return Fuel percentage (0.0 to 1.0), or -1 if no fuel tank
     */
    public static float getFuelPercentage(VehicleEntity vehicle) {
        FuelTankComponent fuelTank = vehicle.getComponent(ComponentType.FUEL_TANK, FuelTankComponent.class);
        if (fuelTank == null) {
            return -1;
        }
        return fuelTank.getCurrentAmount() / fuelTank.getSpecification().getCapacity();
    }
}
