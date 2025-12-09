package de.rolandsw.schedulemc.vehicle.component.engine;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * Component representing a vehicle's engine.
 * Stores engine specifications and state.
 */
public class EngineComponent extends BaseComponent {

    private EngineSpecification specification;
    private float currentRpm = 0.0f;
    private float engineHealth = 1.0f; // 0.0 to 1.0
    private boolean running = false;
    private float temperature = 20.0f; // Celsius
    private float wear = 0.0f; // 0.0 to 1.0

    public EngineComponent(EngineSpecification specification) {
        super(ComponentType.ENGINE);
        this.specification = specification;
    }

    public EngineComponent() {
        this(EngineSpecification.DEFAULT);
    }

    // Getters and Setters
    public EngineSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(EngineSpecification specification) {
        this.specification = specification;
    }

    public float getCurrentRpm() {
        return currentRpm;
    }

    public void setCurrentRpm(float rpm) {
        this.currentRpm = Math.max(0, Math.min(rpm, specification.getMaxRpm()));
    }

    public float getEngineHealth() {
        return engineHealth;
    }

    public void setEngineHealth(float health) {
        this.engineHealth = Math.max(0, Math.min(1, health));
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getWear() {
        return wear;
    }

    public void setWear(float wear) {
        this.wear = Math.max(0, Math.min(1, wear));
    }

    /**
     * Calculates current power output based on RPM and health.
     */
    public float getCurrentPower() {
        if (!running || engineHealth <= 0) {
            return 0.0f;
        }

        float normalizedRpm = currentRpm / specification.getMaxRpm();
        float powerCurve = (float) Math.sin(normalizedRpm * Math.PI); // Peak at 50% RPM
        return specification.getMaxPower() * powerCurve * engineHealth;
    }

    /**
     * Calculates current fuel consumption rate.
     */
    public float getFuelConsumptionRate() {
        if (!running) {
            return 0.0f;
        }

        float idleConsumption = specification.getBaseFuelConsumption() * 0.1f;
        float loadConsumption = specification.getBaseFuelConsumption() * (currentRpm / specification.getMaxRpm());

        return idleConsumption + loadConsumption;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putString("EngineType", specification.getIdentifier());
        tag.putFloat("CurrentRpm", currentRpm);
        tag.putFloat("EngineHealth", engineHealth);
        tag.putBoolean("Running", running);
        tag.putFloat("Temperature", temperature);
        tag.putFloat("Wear", wear);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        String engineType = tag.getString("EngineType");
        this.specification = EngineSpecification.getByIdentifier(engineType);
        this.currentRpm = tag.getFloat("CurrentRpm");
        this.engineHealth = tag.getFloat("EngineHealth");
        this.running = tag.getBoolean("Running");
        this.temperature = tag.getFloat("Temperature");
        this.wear = tag.getFloat("Wear");
    }

    @Override
    public IVehicleComponent duplicate() {
        EngineComponent copy = new EngineComponent(specification);
        copy.currentRpm = this.currentRpm;
        copy.engineHealth = this.engineHealth;
        copy.running = this.running;
        copy.temperature = this.temperature;
        copy.wear = this.wear;
        return copy;
    }

    @Override
    public boolean isValid() {
        return specification != null && engineHealth > 0;
    }
}
