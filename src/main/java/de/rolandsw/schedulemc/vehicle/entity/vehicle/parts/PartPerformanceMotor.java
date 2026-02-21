package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;

public class PartPerformanceMotor extends PartEngine {

    public PartPerformanceMotor() {
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorFuelEfficiency.get().floatValue();
        this.fuelConsumptionPer10km = () -> ModConfigHandler.VEHICLE_SERVER.performanceMotorFuelConsumption.get();
        this.stopSound    = ModSounds.SPORT_ENGINE_STOP;
        this.failSound    = ModSounds.SPORT_ENGINE_FAIL;
        this.crashSound   = ModSounds.VEHICLE_CRASH;
        this.startSound   = ModSounds.SPORT_ENGINE_START;
        this.startingSound= ModSounds.SPORT_ENGINE_STARTING;
        this.idleSound    = ModSounds.SPORT_ENGINE_IDLE;
        this.highSound    = ModSounds.SPORT_ENGINE_HIGH;
        this.hornSound    = ModSounds.VEHICLE_HORN;
    }

    @Override
    public int getUpgradeLevel() {
        return 1;
    }

}
