package de.rolandsw.schedulemc.vehicle.system;

import de.rolandsw.schedulemc.vehicle.component.attribute.DurabilityComponent;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.core.system.IVehicleSystem;
import de.rolandsw.schedulemc.vehicle.core.system.SystemType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * System handling vehicle damage and collisions.
 * Detects impacts and applies damage.
 */
public class DamageSystem implements IVehicleSystem {

    private static final float DAMAGE_THRESHOLD_SPEED = 0.3f;
    private static final float DAMAGE_MULTIPLIER = 10.0f;

    @Override
    public ResourceLocation getSystemId() {
        return SystemType.DAMAGE;
    }

    @Override
    public int getPriority() {
        return 300; // After movement and fuel
    }

    @Override
    public void tick(VehicleEntity vehicle, float deltaTime) {
        DurabilityComponent durability = vehicle.getComponent(ComponentType.DURABILITY, DurabilityComponent.class);

        if (durability == null) {
            return;
        }

        // Check for collisions/impacts
        Vec3 motion = vehicle.getDeltaMovement();
        float speed = (float) motion.length();

        // Detect sudden deceleration (collision)
        float previousSpeed = durability.getLastImpactForce();
        float speedDifference = previousSpeed - speed;

        if (speedDifference > DAMAGE_THRESHOLD_SPEED && vehicle.horizontalCollision) {
            // Calculate impact damage
            float impactForce = speedDifference * DAMAGE_MULTIPLIER;
            boolean destroyed = durability.applyDamage(impactForce);

            if (destroyed) {
                handleVehicleDestruction(vehicle);
            }

            durability.setLastImpactForce(speed);
        } else {
            durability.setLastImpactForce(speed);
        }

        // Apply gradual wear from usage
        if (speed > 0.01f) {
            float wearAmount = speed * 0.001f * deltaTime;
            durability.applyDamage(wearAmount);
        }
    }

    private void handleVehicleDestruction(VehicleEntity vehicle) {
        // Stop engine if running
        EngineComponent engine = vehicle.getComponent(ComponentType.ENGINE, EngineComponent.class);
        if (engine != null) {
            engine.setRunning(false);
            engine.setCurrentRpm(0);
        }

        // Could spawn particles, play sound, drop items, etc.
        // For now, just stop the vehicle
        vehicle.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean canProcess(VehicleEntity vehicle) {
        return vehicle.hasComponent(ComponentType.DURABILITY);
    }
}
