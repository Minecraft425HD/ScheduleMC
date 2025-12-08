package de.rolandsw.schedulemc.vehicle.system;

import de.rolandsw.schedulemc.vehicle.component.engine.EngineComponent;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.core.system.IVehicleSystem;
import de.rolandsw.schedulemc.vehicle.core.system.SystemType;
import net.minecraft.resources.ResourceLocation;

/**
 * System handling fuel consumption and management.
 * Processes fuel usage based on engine state.
 */
public class FuelSystem implements IVehicleSystem {

    @Override
    public ResourceLocation getSystemId() {
        return SystemType.FUEL;
    }

    @Override
    public int getPriority() {
        return 200; // After movement
    }

    @Override
    public void tick(VehicleEntity vehicle, float deltaTime) {
        EngineComponent engine = vehicle.getComponent(ComponentType.ENGINE, EngineComponent.class);
        FuelTankComponent tank = vehicle.getComponent(ComponentType.FUEL_TANK, FuelTankComponent.class);

        if (engine == null || tank == null) {
            return;
        }

        // If engine is running, consume fuel
        if (engine.isRunning()) {
            float consumptionRate = engine.getFuelConsumptionRate();
            float fuelNeeded = consumptionRate * deltaTime;

            if (tank.hasEnoughFuel(fuelNeeded)) {
                tank.drain(fuelNeeded);
            } else {
                // Out of fuel - stop engine
                engine.setRunning(false);
                engine.setCurrentRpm(0);
            }
        }
    }

    @Override
    public boolean canProcess(VehicleEntity vehicle) {
        return vehicle.hasComponent(ComponentType.ENGINE)
                && vehicle.hasComponent(ComponentType.FUEL_TANK);
    }
}
