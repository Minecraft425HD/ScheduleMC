package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import java.util.List;
import java.util.function.Supplier;

public abstract class PartEngine extends Part {

    protected Supplier<Float> maxSpeed;
    protected Supplier<Float> maxReverseSpeed;
    protected Supplier<Float> acceleration;
    protected Supplier<Float> fuelEfficiency;
    protected Supplier<Double> fuelConsumptionPer10km;

    protected Supplier<SoundEvent> stopSound;
    protected Supplier<SoundEvent> failSound;
    protected Supplier<SoundEvent> crashSound;
    protected Supplier<SoundEvent> startSound;
    protected Supplier<SoundEvent> startingSound;
    protected Supplier<SoundEvent> idleSound;
    protected Supplier<SoundEvent> highSound;
    protected Supplier<SoundEvent> hornSound;

    public PartEngine() {

    }

    public SoundEvent getStopSound()    { return stopSound.get(); }
    public SoundEvent getFailSound()    { return failSound.get(); }
    public SoundEvent getCrashSound()   { return crashSound.get(); }
    public SoundEvent getStartSound()   { return startSound.get(); }
    public SoundEvent getStartingSound(){ return startingSound.get(); }
    public SoundEvent getIdleSound()    { return idleSound.get(); }
    public SoundEvent getHighSound()    { return highSound.get(); }
    public SoundEvent getHornSound()    { return hornSound.get(); }

    public float getMaxSpeed() {
        return maxSpeed.get();
    }

    public float getMaxReverseSpeed() {
        return maxReverseSpeed.get();
    }

    public float getAcceleration() {
        return acceleration.get();
    }

    public float getFuelEfficiency() {
        return fuelEfficiency.get();
    }

    /**
     * Returns fuel consumption in liters per 10 km (500 blocks = 1 km).
     */
    public double getFuelConsumptionPer10km() {
        return fuelConsumptionPer10km.get();
    }

    /**
     * Returns the upgrade level of this engine (0 = Standard, 1 = first upgrade, 2 = second upgrade).
     */
    public abstract int getUpgradeLevel();

    @Override
    public boolean validate(List<Part> parts, List<Component> messages) {
        if (getAmount(parts, part -> part instanceof PartTank) > 1) {
            messages.add(Component.translatable("message.parts.too_many_tanks"));
        } else if (getAmount(parts, part -> part instanceof PartTank) <= 0) {
            messages.add(Component.translatable("message.parts.no_tank"));
        }
        return super.validate(parts, messages);
    }
    /*
    Speeds

    transporter 3 -> 27.54
    transporter 6 -> 35.8

    big wood 3 -> 30.6
    big wood 6 -> 39.78

    wood 3 -> 32.4
    wood 6 -> 42.12

    sport 3 -> 36
    sport 6 -> 46.8
    */
}
