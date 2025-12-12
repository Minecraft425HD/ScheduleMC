package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.sounds.SoundEvent;

public class PartEngineTruck extends PartEngine {

    public PartEngineTruck() {
        this.maxSpeed = () -> ModConfigHandler.CAR_SERVER.engineTruckMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.CAR_SERVER.engineTruckMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.CAR_SERVER.engineTruckAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.CAR_SERVER.engineTruckFuelEfficiency.get().floatValue();
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
        return ModSounds.CAR_CRASH.get();
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
        return ModSounds.CAR_HORN.get();
    }

}
