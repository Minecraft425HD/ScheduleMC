package de.rolandsw.schedulemc.vehicle.component.attribute;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * Component representing vehicle temperature.
 * Tracks engine and cabin temperature.
 */
public class TemperatureComponent extends BaseComponent {

    private float engineTemperature = 20.0f; // Celsius
    private float cabinTemperature = 20.0f; // Celsius
    private float ambientTemperature = 20.0f; // From biome
    private float maxSafeTemperature = 120.0f; // Engine overheating threshold

    public TemperatureComponent() {
        super(ComponentType.TEMPERATURE);
    }

    // Getters and Setters
    public float getEngineTemperature() {
        return engineTemperature;
    }

    public void setEngineTemperature(float temperature) {
        this.engineTemperature = temperature;
    }

    public float getCabinTemperature() {
        return cabinTemperature;
    }

    public void setCabinTemperature(float temperature) {
        this.cabinTemperature = temperature;
    }

    public float getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(float temperature) {
        this.ambientTemperature = temperature;
    }

    public float getMaxSafeTemperature() {
        return maxSafeTemperature;
    }

    public void setMaxSafeTemperature(float temperature) {
        this.maxSafeTemperature = temperature;
    }

    /**
     * Heats up the engine.
     */
    public void heatEngine(float amount) {
        engineTemperature += amount;
    }

    /**
     * Cools down the engine towards ambient temperature.
     */
    public void coolEngine(float deltaTime) {
        float coolingRate = 5.0f; // Degrees per second
        float difference = engineTemperature - ambientTemperature;

        if (Math.abs(difference) < 0.1f) {
            engineTemperature = ambientTemperature;
        } else {
            engineTemperature -= Math.signum(difference) * coolingRate * deltaTime;
        }
    }

    /**
     * Updates cabin temperature towards ambient.
     */
    public void updateCabinTemperature(float deltaTime) {
        float changeRate = 2.0f; // Degrees per second
        float difference = cabinTemperature - ambientTemperature;

        if (Math.abs(difference) < 0.1f) {
            cabinTemperature = ambientTemperature;
        } else {
            cabinTemperature -= Math.signum(difference) * changeRate * deltaTime;
        }
    }

    /**
     * Checks if engine is overheating.
     */
    public boolean isOverheating() {
        return engineTemperature > maxSafeTemperature;
    }

    /**
     * Gets engine temperature as a normalized value (0 to 1+).
     */
    public float getNormalizedEngineTemperature() {
        return engineTemperature / maxSafeTemperature;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putFloat("EngineTemperature", engineTemperature);
        tag.putFloat("CabinTemperature", cabinTemperature);
        tag.putFloat("AmbientTemperature", ambientTemperature);
        tag.putFloat("MaxSafeTemperature", maxSafeTemperature);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.engineTemperature = tag.getFloat("EngineTemperature");
        this.cabinTemperature = tag.getFloat("CabinTemperature");
        this.ambientTemperature = tag.getFloat("AmbientTemperature");
        this.maxSafeTemperature = tag.getFloat("MaxSafeTemperature");
    }

    @Override
    public IVehicleComponent duplicate() {
        TemperatureComponent copy = new TemperatureComponent();
        copy.engineTemperature = this.engineTemperature;
        copy.cabinTemperature = this.cabinTemperature;
        copy.ambientTemperature = this.ambientTemperature;
        copy.maxSafeTemperature = this.maxSafeTemperature;
        return copy;
    }

    @Override
    public boolean isValid() {
        return true; // Temperature is always valid
    }
}
