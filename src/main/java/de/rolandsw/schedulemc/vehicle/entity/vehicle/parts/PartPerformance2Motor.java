package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;

public class PartPerformance2Motor extends PartEngine {

    public PartPerformance2Motor() {
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorFuelEfficiency.get().floatValue();
        this.fuelConsumptionPer10km = () -> ModConfigHandler.VEHICLE_SERVER.performance2MotorFuelConsumption.get();
        this.stopSound    = ModSounds.TRUCK_ENGINE_STOP;
        this.failSound    = ModSounds.TRUCK_ENGINE_FAIL;
        this.crashSound   = ModSounds.VEHICLE_CRASH;
        this.startSound   = ModSounds.TRUCK_ENGINE_START;
        this.startingSound= ModSounds.TRUCK_ENGINE_STARTING;
        this.idleSound    = ModSounds.TRUCK_ENGINE_IDLE;
        this.highSound    = ModSounds.TRUCK_ENGINE_HIGH;
        this.hornSound    = ModSounds.VEHICLE_HORN;
    }

    @Override
    public int getUpgradeLevel() {
        return 2;
    }

}
