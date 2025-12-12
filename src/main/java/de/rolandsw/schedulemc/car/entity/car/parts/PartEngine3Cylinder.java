package de.rolandsw.schedulemc.car.entity.car.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import net.minecraft.sounds.SoundEvent;

public class PartEngine3Cylinder extends PartEngine {

    public PartEngine3Cylinder() {
        this.maxSpeed = () -> ModConfigHandler.CAR_SERVER.engine3CylinderMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.CAR_SERVER.engine3CylinderMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.CAR_SERVER.engine3CylinderAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.CAR_SERVER.engine3CylinderFuelEfficiency.get().floatValue();
    }

    @Override
    public SoundEvent getStopSound() {
        return ModSounds.ENGINE_STOP.get();
    }

    @Override
    public SoundEvent getFailSound() {
        return ModSounds.ENGINE_FAIL.get();
    }

    @Override
    public SoundEvent getCrashSound() {
        return ModSounds.CAR_CRASH.get();
    }

    @Override
    public SoundEvent getStartSound() {
        return ModSounds.ENGINE_START.get();
    }

    @Override
    public SoundEvent getStartingSound() {
        return ModSounds.ENGINE_STARTING.get();
    }

    @Override
    public SoundEvent getIdleSound() {
        return ModSounds.ENGINE_IDLE.get();
    }

    @Override
    public SoundEvent getHighSound() {
        return ModSounds.ENGINE_HIGH.get();
    }

    @Override
    public SoundEvent getHornSound() {
        return ModSounds.CAR_HORN.get();
    }

}
