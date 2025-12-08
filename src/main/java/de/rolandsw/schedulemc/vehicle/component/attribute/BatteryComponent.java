package de.rolandsw.schedulemc.vehicle.component.attribute;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * Component representing vehicle battery.
 * Required for starting the engine and electrical systems.
 */
public class BatteryComponent extends BaseComponent {

    private float maxCharge = 100.0f;
    private float currentCharge = 100.0f;
    private float chargeRate = 1.0f; // Per second when engine running
    private float drainRate = 0.1f; // Per second when engine off but systems on

    public BatteryComponent() {
        super(ComponentType.BATTERY);
    }

    public BatteryComponent(float maxCharge, float chargeRate, float drainRate) {
        super(ComponentType.BATTERY);
        this.maxCharge = maxCharge;
        this.currentCharge = maxCharge;
        this.chargeRate = chargeRate;
        this.drainRate = drainRate;
    }

    // Getters and Setters
    public float getMaxCharge() {
        return maxCharge;
    }

    public void setMaxCharge(float maxCharge) {
        this.maxCharge = maxCharge;
    }

    public float getCurrentCharge() {
        return currentCharge;
    }

    public void setCurrentCharge(float charge) {
        this.currentCharge = Math.max(0, Math.min(maxCharge, charge));
    }

    public float getChargeRate() {
        return chargeRate;
    }

    public void setChargeRate(float rate) {
        this.chargeRate = rate;
    }

    public float getDrainRate() {
        return drainRate;
    }

    public void setDrainRate(float rate) {
        this.drainRate = rate;
    }

    /**
     * Charges the battery.
     */
    public void charge(float deltaTime) {
        currentCharge += chargeRate * deltaTime;
        if (currentCharge > maxCharge) {
            currentCharge = maxCharge;
        }
    }

    /**
     * Drains the battery.
     */
    public void drain(float deltaTime) {
        currentCharge -= drainRate * deltaTime;
        if (currentCharge < 0) {
            currentCharge = 0;
        }
    }

    /**
     * Gets charge as a percentage.
     */
    public float getChargePercentage() {
        return maxCharge > 0 ? currentCharge / maxCharge : 0;
    }

    /**
     * Checks if battery has enough charge to start engine.
     */
    public boolean canStartEngine() {
        return currentCharge >= (maxCharge * 0.1f); // Need at least 10%
    }

    /**
     * Consumes charge for starting engine.
     */
    public boolean consumeStartCharge() {
        float startCost = maxCharge * 0.05f; // 5% to start
        if (currentCharge >= startCost) {
            currentCharge -= startCost;
            return true;
        }
        return false;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putFloat("MaxCharge", maxCharge);
        tag.putFloat("CurrentCharge", currentCharge);
        tag.putFloat("ChargeRate", chargeRate);
        tag.putFloat("DrainRate", drainRate);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.maxCharge = tag.getFloat("MaxCharge");
        this.currentCharge = tag.getFloat("CurrentCharge");
        this.chargeRate = tag.getFloat("ChargeRate");
        this.drainRate = tag.getFloat("DrainRate");
    }

    @Override
    public IVehicleComponent duplicate() {
        BatteryComponent copy = new BatteryComponent(maxCharge, chargeRate, drainRate);
        copy.currentCharge = this.currentCharge;
        return copy;
    }

    @Override
    public boolean isValid() {
        return currentCharge >= 0 && currentCharge <= maxCharge;
    }
}
