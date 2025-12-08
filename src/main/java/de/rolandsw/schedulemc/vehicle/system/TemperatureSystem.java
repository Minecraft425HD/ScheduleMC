package de.rolandsw.schedulemc.vehicle.system;

import de.rolandsw.schedulemc.vehicle.component.attribute.DurabilityComponent;
import de.rolandsw.schedulemc.vehicle.component.attribute.TemperatureComponent;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.core.system.IVehicleSystem;
import de.rolandsw.schedulemc.vehicle.core.system.SystemType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * System handling vehicle temperature.
 * Manages engine heating/cooling and overheating damage.
 */
public class TemperatureSystem implements IVehicleSystem {

    private static final float ENGINE_HEAT_RATE = 15.0f; // Degrees per second when running
    private static final float OVERHEAT_DAMAGE_RATE = 2.0f; // Damage per second when overheating

    @Override
    public ResourceLocation getSystemId() {
        return SystemType.TEMPERATURE;
    }

    @Override
    public int getPriority() {
        return 280; // Before damage system
    }

    @Override
    public void tick(VehicleEntity vehicle, float deltaTime) {
        TemperatureComponent temperature = vehicle.getComponent(ComponentType.TEMPERATURE, TemperatureComponent.class);
        EngineComponent engine = vehicle.getComponent(ComponentType.ENGINE, EngineComponent.class);

        if (temperature == null) {
            return;
        }

        // Update ambient temperature from biome
        Level world = vehicle.level();
        BlockPos pos = vehicle.blockPosition();
        Biome biome = world.getBiome(pos).value();
        float biomeTemp = biome.getBaseTemperature() * 30.0f; // Approximate conversion to Celsius
        temperature.setAmbientTemperature(biomeTemp);

        // Update engine temperature
        if (engine != null && engine.isRunning()) {
            // Engine heats up when running
            float heatAmount = ENGINE_HEAT_RATE * (engine.getCurrentRpm() / engine.getSpecification().getMaxRpm());
            temperature.heatEngine(heatAmount * deltaTime);
        } else {
            // Engine cools down when off
            temperature.coolEngine(deltaTime);
        }

        // Check for overheating damage
        if (temperature.isOverheating()) {
            DurabilityComponent durability = vehicle.getComponent(ComponentType.DURABILITY, DurabilityComponent.class);
            if (durability != null) {
                durability.applyDamage(OVERHEAT_DAMAGE_RATE * deltaTime);
            }

            // Reduce engine health
            if (engine != null) {
                float healthLoss = 0.01f * deltaTime;
                engine.setEngineHealth(engine.getEngineHealth() - healthLoss);
            }
        }

        // Update cabin temperature
        temperature.updateCabinTemperature(deltaTime);
    }

    @Override
    public boolean canProcess(VehicleEntity vehicle) {
        return vehicle.hasComponent(ComponentType.TEMPERATURE);
    }
}
