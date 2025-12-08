package de.rolandsw.schedulemc.vehicle.component.attribute;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * Component representing vehicle durability/damage.
 * Replaces the old EntityCarDamageBase functionality.
 */
public class DurabilityComponent extends BaseComponent {

    private float maxDurability = 100.0f;
    private float currentDurability = 100.0f;
    private float crashDamage = 0.0f;
    private float lastImpactForce = 0.0f;

    public DurabilityComponent() {
        super(ComponentType.DURABILITY);
    }

    public DurabilityComponent(float maxDurability) {
        super(ComponentType.DURABILITY);
        this.maxDurability = maxDurability;
        this.currentDurability = maxDurability;
    }

    // Getters and Setters
    public float getMaxDurability() {
        return maxDurability;
    }

    public void setMaxDurability(float maxDurability) {
        this.maxDurability = maxDurability;
    }

    public float getCurrentDurability() {
        return currentDurability;
    }

    public void setCurrentDurability(float durability) {
        this.currentDurability = Math.max(0, Math.min(maxDurability, durability));
    }

    public float getCrashDamage() {
        return crashDamage;
    }

    public void setCrashDamage(float damage) {
        this.crashDamage = Math.max(0, damage);
    }

    public float getLastImpactForce() {
        return lastImpactForce;
    }

    public void setLastImpactForce(float force) {
        this.lastImpactForce = force;
    }

    /**
     * Applies damage to the vehicle.
     * @param amount Damage amount
     * @return true if vehicle is destroyed
     */
    public boolean applyDamage(float amount) {
        currentDurability -= amount;
        crashDamage += amount;

        if (currentDurability <= 0) {
            currentDurability = 0;
            return true; // Vehicle destroyed
        }
        return false;
    }

    /**
     * Repairs the vehicle.
     * @param amount Repair amount
     */
    public void repair(float amount) {
        currentDurability += amount;
        if (currentDurability > maxDurability) {
            currentDurability = maxDurability;
        }
        crashDamage = Math.max(0, crashDamage - amount);
    }

    /**
     * Gets durability as a percentage.
     */
    public float getDurabilityPercentage() {
        return maxDurability > 0 ? currentDurability / maxDurability : 0;
    }

    /**
     * Checks if vehicle is functional.
     */
    public boolean isFunctional() {
        return currentDurability > 0;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putFloat("MaxDurability", maxDurability);
        tag.putFloat("CurrentDurability", currentDurability);
        tag.putFloat("CrashDamage", crashDamage);
        tag.putFloat("LastImpactForce", lastImpactForce);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.maxDurability = tag.getFloat("MaxDurability");
        this.currentDurability = tag.getFloat("CurrentDurability");
        this.crashDamage = tag.getFloat("CrashDamage");
        this.lastImpactForce = tag.getFloat("LastImpactForce");
    }

    @Override
    public IVehicleComponent duplicate() {
        DurabilityComponent copy = new DurabilityComponent(maxDurability);
        copy.currentDurability = this.currentDurability;
        copy.crashDamage = this.crashDamage;
        copy.lastImpactForce = this.lastImpactForce;
        return copy;
    }

    @Override
    public boolean isValid() {
        return currentDurability >= 0 && currentDurability <= maxDurability;
    }
}
