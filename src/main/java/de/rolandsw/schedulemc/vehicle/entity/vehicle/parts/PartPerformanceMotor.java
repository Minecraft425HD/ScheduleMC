package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.sounds.SoundEvent;

public class PartPerformanceMotor extends PartEngine {

    public PartPerformanceMotor() {
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorFuelEfficiency.get().floatValue();
    }

    @Override
    public int getUpgradeLevel() {
        return 1;
    }

    @Override
    public SoundEvent getStopSound() {
        return ModSounds.SPORT_ENGINE_STOP.get();
    }

    @Override
    public SoundEvent getFailSound() {
        return ModSounds.SPORT_ENGINE_FAIL.get();
    }

    @Override
    public SoundEvent getCrashSound() {
        return ModSounds.VEHICLE_CRASH.get();
    }

    @Override
    public SoundEvent getStartSound() {
        return ModSounds.SPORT_ENGINE_START.get();
    }

    @Override
    public SoundEvent getStartingSound() {
        return ModSounds.SPORT_ENGINE_STARTING.get();
    }

    @Override
    public SoundEvent getIdleSound() {
        return ModSounds.SPORT_ENGINE_IDLE.get();
    }

    @Override
    public SoundEvent getHighSound() {
        return ModSounds.SPORT_ENGINE_HIGH.get();
    }

    @Override
    public SoundEvent getHornSound() {
        return ModSounds.VEHICLE_HORN.get();
    }

}
