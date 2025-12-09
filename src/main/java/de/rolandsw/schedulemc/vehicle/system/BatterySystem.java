package de.rolandsw.schedulemc.vehicle.system;

import de.rolandsw.schedulemc.vehicle.component.attribute.BatteryComponent;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.core.system.IVehicleSystem;
import de.rolandsw.schedulemc.vehicle.core.system.SystemType;
import net.minecraft.resources.ResourceLocation;

/**
 * System handling battery charging and draining.
 * Charges when engine runs, drains when systems are on.
 */
public class BatterySystem implements IVehicleSystem {

    @Override
    public ResourceLocation getSystemId() {
        return SystemType.BATTERY;
    }

    @Override
    public int getPriority() {
        return 250; // After fuel, before damage
    }

    @Override
    public void tick(VehicleEntity vehicle, float deltaTime) {
        BatteryComponent battery = vehicle.getComponent(ComponentType.BATTERY, BatteryComponent.class);
        EngineComponent engine = vehicle.getComponent(ComponentType.ENGINE, EngineComponent.class);

        if (battery == null) {
            return;
        }

        // Charge battery if engine is running
        if (engine != null && engine.isRunning()) {
            battery.charge(deltaTime);
        } else {
            // Drain battery slowly when engine is off
            battery.drain(deltaTime);
        }
    }

    @Override
    public boolean canProcess(VehicleEntity vehicle) {
        return vehicle.hasComponent(ComponentType.BATTERY);
    }
}
