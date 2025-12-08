package de.rolandsw.schedulemc.vehicle.component.control;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * Component representing vehicle controls and input state.
 * Tracks player input and control state.
 */
public class ControlComponent extends BaseComponent {

    // Input states
    private boolean forwardPressed = false;
    private boolean backwardPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean brakePressed = false;
    private boolean hornPressed = false;

    // Control parameters
    private float steeringAngle = 0.0f; // -1.0 to 1.0
    private float throttle = 0.0f; // 0.0 to 1.0
    private float brake = 0.0f; // 0.0 to 1.0

    // Settings
    private float maxSteeringAngle = 35.0f; // Degrees
    private float steeringSpeed = 2.0f; // Degrees per tick
    private float acceleration = 0.1f;
    private float deceleration = 0.2f;

    public ControlComponent() {
        super(ComponentType.CONTROLS);
    }

    // Input state setters
    public void setForwardPressed(boolean pressed) {
        this.forwardPressed = pressed;
    }

    public void setBackwardPressed(boolean pressed) {
        this.backwardPressed = pressed;
    }

    public void setLeftPressed(boolean pressed) {
        this.leftPressed = pressed;
    }

    public void setRightPressed(boolean pressed) {
        this.rightPressed = pressed;
    }

    public void setBrakePressed(boolean pressed) {
        this.brakePressed = pressed;
    }

    public void setHornPressed(boolean pressed) {
        this.hornPressed = pressed;
    }

    // Input state getters
    public boolean isForwardPressed() {
        return forwardPressed;
    }

    public boolean isBackwardPressed() {
        return backwardPressed;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isBrakePressed() {
        return brakePressed;
    }

    public boolean isHornPressed() {
        return hornPressed;
    }

    // Control parameters
    public float getSteeringAngle() {
        return steeringAngle;
    }

    public void setSteeringAngle(float angle) {
        this.steeringAngle = Math.max(-1.0f, Math.min(1.0f, angle));
    }

    public float getThrottle() {
        return throttle;
    }

    public void setThrottle(float throttle) {
        this.throttle = Math.max(0, Math.min(1, throttle));
    }

    public float getBrake() {
        return brake;
    }

    public void setBrake(float brake) {
        this.brake = Math.max(0, Math.min(1, brake));
    }

    // Settings
    public float getMaxSteeringAngle() {
        return maxSteeringAngle;
    }

    public float getSteeringSpeed() {
        return steeringSpeed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public float getDeceleration() {
        return deceleration;
    }

    /**
     * Updates control state based on input.
     */
    public void updateControls(float deltaTime) {
        // Update steering
        if (leftPressed && !rightPressed) {
            steeringAngle -= steeringSpeed * deltaTime;
        } else if (rightPressed && !leftPressed) {
            steeringAngle += steeringSpeed * deltaTime;
        } else {
            // Return to center
            if (Math.abs(steeringAngle) < steeringSpeed * deltaTime) {
                steeringAngle = 0;
            } else {
                steeringAngle -= Math.signum(steeringAngle) * steeringSpeed * deltaTime;
            }
        }
        steeringAngle = Math.max(-1.0f, Math.min(1.0f, steeringAngle));

        // Update throttle
        if (forwardPressed) {
            throttle += acceleration * deltaTime;
        } else {
            throttle -= deceleration * deltaTime;
        }
        throttle = Math.max(0, Math.min(1, throttle));

        // Update brake
        if (brakePressed || backwardPressed) {
            brake = 1.0f;
        } else {
            brake = 0.0f;
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putFloat("SteeringAngle", steeringAngle);
        tag.putFloat("Throttle", throttle);
        tag.putFloat("Brake", brake);
        tag.putFloat("MaxSteeringAngle", maxSteeringAngle);
        tag.putFloat("SteeringSpeed", steeringSpeed);
        tag.putFloat("Acceleration", acceleration);
        tag.putFloat("Deceleration", deceleration);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.steeringAngle = tag.getFloat("SteeringAngle");
        this.throttle = tag.getFloat("Throttle");
        this.brake = tag.getFloat("Brake");
        this.maxSteeringAngle = tag.getFloat("MaxSteeringAngle");
        this.steeringSpeed = tag.getFloat("SteeringSpeed");
        this.acceleration = tag.getFloat("Acceleration");
        this.deceleration = tag.getFloat("Deceleration");
    }

    @Override
    public IVehicleComponent duplicate() {
        ControlComponent copy = new ControlComponent();
        copy.steeringAngle = this.steeringAngle;
        copy.throttle = this.throttle;
        copy.brake = this.brake;
        copy.maxSteeringAngle = this.maxSteeringAngle;
        copy.steeringSpeed = this.steeringSpeed;
        copy.acceleration = this.acceleration;
        copy.deceleration = this.deceleration;
        return copy;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
