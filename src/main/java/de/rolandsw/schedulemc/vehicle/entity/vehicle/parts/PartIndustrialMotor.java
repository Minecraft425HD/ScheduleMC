package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.sounds.SoundEvent;

public class PartIndustrialMotor extends PartEngine {

    public PartIndustrialMotor() {
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.industrialMotorMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.VEHICLE_SERVER.industrialMotorMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.industrialMotorAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.industrialMotorFuelEfficiency.get().floatValue();
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
