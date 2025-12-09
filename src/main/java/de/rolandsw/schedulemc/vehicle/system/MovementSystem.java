package de.rolandsw.schedulemc.vehicle.system;

import de.rolandsw.schedulemc.vehicle.component.attribute.DurabilityComponent;
import de.rolandsw.schedulemc.vehicle.component.body.BodyComponent;
import de.rolandsw.schedulemc.vehicle.component.control.ControlComponent;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineComponent;
import de.rolandsw.schedulemc.vehicle.component.mobility.WheelComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.core.system.IVehicleSystem;
import de.rolandsw.schedulemc.vehicle.core.system.SystemType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * System handling vehicle movement and physics.
 * Processes engine power, steering, and applies forces.
 */
public class MovementSystem implements IVehicleSystem {

    private static final float GRAVITY = 0.08f;
    private static final float DRAG_COEFFICIENT = 0.98f;
    private static final float GROUND_FRICTION = 0.85f;

    @Override
    public ResourceLocation getSystemId() {
        return SystemType.MOVEMENT;
    }

    @Override
    public int getPriority() {
        return 100; // Run early
    }

    @Override
    public void tick(VehicleEntity vehicle, float deltaTime) {
        // Get required components
        EngineComponent engine = vehicle.getComponent(ComponentType.ENGINE, EngineComponent.class);
        ControlComponent controls = vehicle.getComponent(ComponentType.CONTROLS, ControlComponent.class);
        WheelComponent wheels = vehicle.getComponent(ComponentType.WHEELS, WheelComponent.class);
        BodyComponent body = vehicle.getComponent(ComponentType.BODY, BodyComponent.class);
        DurabilityComponent durability = vehicle.getComponent(ComponentType.DURABILITY, DurabilityComponent.class);

        if (engine == null || controls == null || wheels == null || body == null) {
            return; // Can't move without these
        }

        // Check if vehicle is functional
        if (durability != null && !durability.isFunctional()) {
            return; // Vehicle is destroyed
        }

        // Update controls
        controls.updateControls(deltaTime);

        // Calculate movement
        Vec3 currentMotion = vehicle.getDeltaMovement();

        // Apply engine power if running
        if (engine.isRunning() && controls.getThrottle() > 0) {
            float power = engine.getCurrentPower();
            float thrust = power * controls.getThrottle() * wheels.getEffectiveTraction();
            thrust /= body.getWeightMultiplier();

            // Apply thrust in forward direction
            Vec3 forward = Vec3.directionFromRotation(0, vehicle.getYRot());
            Vec3 thrustVec = forward.scale(thrust * 0.01f * deltaTime);

            currentMotion = currentMotion.add(thrustVec);
        }

        // Apply steering
        if (currentMotion.horizontalDistanceSqr() > 0.001) {
            float steeringForce = controls.getSteeringAngle() * wheels.getEffectiveGrip();
            float newYaw = vehicle.getYRot() + steeringForce * 2.0f;
            vehicle.setYRot(newYaw);
            vehicle.yRotO = newYaw;
        }

        // Apply braking
        if (controls.getBrake() > 0) {
            currentMotion = currentMotion.scale(Math.pow(GROUND_FRICTION, controls.getBrake() * 2));
        }

        // Apply drag and friction
        currentMotion = currentMotion.multiply(
                DRAG_COEFFICIENT * body.getAerodynamicEfficiency(),
                0.98,
                DRAG_COEFFICIENT * body.getAerodynamicEfficiency()
        );

        // Apply gravity if not on ground
        if (!vehicle.onGround()) {
            currentMotion = currentMotion.add(0, -GRAVITY * deltaTime, 0);
        } else {
            currentMotion = currentMotion.multiply(GROUND_FRICTION, 1.0, GROUND_FRICTION);
        }

        // Update wheel rotation
        float speed = (float) currentMotion.horizontalDistance();
        wheels.updateRotation(speed);

        // Apply motion
        vehicle.setDeltaMovement(currentMotion);
        vehicle.move(net.minecraft.world.entity.MoverType.SELF, currentMotion);
    }

    @Override
    public boolean canProcess(VehicleEntity vehicle) {
        // Need at minimum: engine, controls, wheels, body
        return vehicle.hasComponent(ComponentType.ENGINE)
                && vehicle.hasComponent(ComponentType.CONTROLS)
                && vehicle.hasComponent(ComponentType.WHEELS)
                && vehicle.hasComponent(ComponentType.BODY);
    }
}
