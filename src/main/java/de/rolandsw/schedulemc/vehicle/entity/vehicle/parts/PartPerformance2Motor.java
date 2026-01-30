package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.sounds.SoundEvent;

public class PartPerformance2Motor extends PartEngine {

    public PartPerformance2Motor() {
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorFuelEfficiency.get().floatValue();
        this.fuelConsumptionPer10km = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorFuelConsumption.get();
    }

    @Override
    public int getUpgradeLevel() {
        return 2;
    }

    @Override
    public SoundEvent getStopSound() {
        return ModSounds.TRUCK_ENGINE_STOP.get();
    }

    @Override
    public SoundEvent getFailSound() {
        return ModSounds.TRUCK_ENGINE_FAIL.get();
    }

    @Override
    public SoundEvent getCrashSound() {
        return ModSounds.VEHICLE_CRASH.get();
    }

    @Override
    public SoundEvent getStartSound() {
        return ModSounds.TRUCK_ENGINE_START.get();
    }

    @Override
    public SoundEvent getStartingSound() {
        return ModSounds.TRUCK_ENGINE_STARTING.get();
    }

    @Override
    public SoundEvent getIdleSound() {
        return ModSounds.TRUCK_ENGINE_IDLE.get();
    }

    @Override
    public SoundEvent getHighSound() {
        return ModSounds.TRUCK_ENGINE_HIGH.get();
    }

    @Override
    public SoundEvent getHornSound() {
        return ModSounds.VEHICLE_HORN.get();
    }

}
