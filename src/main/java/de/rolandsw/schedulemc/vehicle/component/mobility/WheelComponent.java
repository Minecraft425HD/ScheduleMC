package de.rolandsw.schedulemc.vehicle.component.mobility;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Component representing vehicle wheels.
 * Handles rotation, traction, and wear.
 */
public class WheelComponent extends BaseComponent {

    private WheelSpecification specification;
    private float currentRotation = 0.0f; // Current rotation angle in degrees
    private float rotationSpeed = 0.0f; // Degrees per tick
    private float wear = 0.0f; // 0.0 to 1.0
    private float traction = 1.0f; // Current traction multiplier

    public WheelComponent(WheelSpecification specification) {
        super(ComponentType.WHEELS);
        this.specification = specification;
    }

    public WheelComponent() {
        this(WheelSpecification.STANDARD);
    }

    // Getters and Setters
    public WheelSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(WheelSpecification specification) {
        this.specification = specification;
    }

    public float getCurrentRotation() {
        return currentRotation;
    }

    public void setCurrentRotation(float rotation) {
        this.currentRotation = rotation % 360.0f;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float speed) {
        this.rotationSpeed = speed;
    }

    public float getWear() {
        return wear;
    }

    public void setWear(float wear) {
        this.wear = Math.max(0, Math.min(1, wear));
    }

    public float getTraction() {
        return traction;
    }

    public void setTraction(float traction) {
        this.traction = Math.max(0, Math.min(1, traction));
    }

    /**
     * Updates wheel rotation based on vehicle speed.
     */
    public void updateRotation(float vehicleSpeed) {
        // Convert speed to rotation (simplified physics)
        float circumference = specification.getDiameter() * (float) Math.PI;
        float rotationPerTick = (vehicleSpeed / circumference) * 360.0f;

        rotationSpeed = rotationPerTick * getEffectiveTraction();
        currentRotation = (currentRotation + rotationSpeed) % 360.0f;
    }

    /**
     * Gets effective traction considering wear.
     */
    public float getEffectiveTraction() {
        float wearPenalty = wear * 0.5f; // Max 50% reduction from wear
        return specification.getBaseTraction() * traction * (1.0f - wearPenalty);
    }

    /**
     * Gets effective grip multiplier.
     */
    public float getEffectiveGrip() {
        return specification.getGripMultiplier() * (1.0f - wear * 0.3f);
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putString("WheelType", specification.getIdentifier());
        tag.putFloat("CurrentRotation", currentRotation);
        tag.putFloat("RotationSpeed", rotationSpeed);
        tag.putFloat("Wear", wear);
        tag.putFloat("Traction", traction);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        String wheelType = tag.getString("WheelType");
        this.specification = WheelSpecification.getByIdentifier(wheelType);
        this.currentRotation = tag.getFloat("CurrentRotation");
        this.rotationSpeed = tag.getFloat("RotationSpeed");
        this.wear = tag.getFloat("Wear");
        this.traction = tag.getFloat("Traction");
    }

    @Override
    public IVehicleComponent duplicate() {
        WheelComponent copy = new WheelComponent(specification);
        copy.currentRotation = this.currentRotation;
        copy.rotationSpeed = this.rotationSpeed;
        copy.wear = this.wear;
        copy.traction = this.traction;
        return copy;
    }

    @Override
    public boolean isValid() {
        return specification != null && wear < 1.0f;
    }
}
