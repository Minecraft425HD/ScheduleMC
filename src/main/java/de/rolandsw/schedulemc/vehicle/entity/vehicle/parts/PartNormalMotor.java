package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;

public class PartNormalMotor extends PartEngine {

    public PartNormalMotor() {
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorMaxSpeed.get().floatValue();
        this.maxReverseSpeed = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorMaxReverseSpeed.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorAcceleration.get().floatValue();
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorFuelEfficiency.get().floatValue();
        this.fuelConsumptionPer10km = () -> ModConfigHandler.VEHICLE_SERVER.normalMotorFuelConsumption.get();
        this.stopSound    = ModSounds.ENGINE_STOP;
        this.failSound    = ModSounds.ENGINE_FAIL;
        this.crashSound   = ModSounds.VEHICLE_CRASH;
        this.startSound   = ModSounds.ENGINE_START;
        this.startingSound= ModSounds.ENGINE_STARTING;
        this.idleSound    = ModSounds.ENGINE_IDLE;
        this.highSound    = ModSounds.ENGINE_HIGH;
        this.hornSound    = ModSounds.VEHICLE_HORN;
    }

    @Override
    public int getUpgradeLevel() {
        return 0;
    }

}
