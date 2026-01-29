package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.sounds.SoundEvent;

public class PartNormalMotor extends PartEngine {

    public PartNormalMotor() {
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorFuelEfficiency.get().floatValue();
    }

    @Override
    public int getUpgradeLevel() {
        return 0;
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
        return ModSounds.VEHICLE_CRASH.get();
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
        return ModSounds.VEHICLE_HORN.get();
    }

}
