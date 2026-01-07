package de.rolandsw.schedulemc.config;

import de.maxhenkel.corelib.config.ConfigBase;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.corelib.tag.TagUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServerConfig extends ConfigBase {

    public final ForgeConfigSpec.IntValue fuelStationTransferRate;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fuelStationValidFuels;
    public final ForgeConfigSpec.IntValue fuelStationMorningPricePer10mb;
    public final ForgeConfigSpec.IntValue fuelStationEveningPricePer10mb;

    public final ForgeConfigSpec.DoubleValue repairKitRepairAmount;

    public final ForgeConfigSpec.IntValue canisterMaxFuel;

    public final ForgeConfigSpec.DoubleValue vehicleOffroadSpeed;
    public final ForgeConfigSpec.DoubleValue vehicleOnroadSpeed;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> vehicleDriveBlocks;

    public final ForgeConfigSpec.BooleanValue collideWithEntities;
    public final ForgeConfigSpec.BooleanValue damageEntities;
    public final ForgeConfigSpec.BooleanValue hornFlee;
    public final ForgeConfigSpec.BooleanValue useBattery;

    // Realistic vehicle simulation settings
    public final ForgeConfigSpec.IntValue engineStartFuelConsumption;
    public final ForgeConfigSpec.IntValue idleFuelConsumptionInterval;
    public final ForgeConfigSpec.IntValue idleBatteryRechargeRate;
    public final ForgeConfigSpec.DoubleValue drivingBatteryRechargeMultiplier;
    public final ForgeConfigSpec.IntValue temperatureUpdateInterval;

    public final ForgeConfigSpec.IntValue tankSmallMaxFuel;
    public final ForgeConfigSpec.IntValue tankMediumMaxFuel;
    public final ForgeConfigSpec.IntValue tankLargeMaxFuel;

    public final ForgeConfigSpec.DoubleValue performanceMotorFuelEfficiency;
    public final ForgeConfigSpec.DoubleValue normalMotorFuelEfficiency;
    public final ForgeConfigSpec.DoubleValue industrialMotorFuelEfficiency;

    public final ForgeConfigSpec.DoubleValue performanceMotorAcceleration;
    public final ForgeConfigSpec.DoubleValue normalMotorAcceleration;
    public final ForgeConfigSpec.DoubleValue industrialMotorAcceleration;

    public final ForgeConfigSpec.DoubleValue performanceMotorMaxSpeed;
    public final ForgeConfigSpec.DoubleValue normalMotorMaxSpeed;
    public final ForgeConfigSpec.DoubleValue industrialMotorMaxSpeed;

    public final ForgeConfigSpec.DoubleValue performanceMotorMaxReverseSpeed;
    public final ForgeConfigSpec.DoubleValue normalMotorMaxReverseSpeed;
    public final ForgeConfigSpec.DoubleValue industrialMotorMaxReverseSpeed;

    public final ForgeConfigSpec.DoubleValue vanChassisFuelEfficiency;
    public final ForgeConfigSpec.DoubleValue vanChassisAcceleration;
    public final ForgeConfigSpec.DoubleValue vanChassisMaxSpeed;

    public final ForgeConfigSpec.DoubleValue limousineChassisEfficiency;
    public final ForgeConfigSpec.DoubleValue limousineChassisAcceleration;
    public final ForgeConfigSpec.DoubleValue limousineChassisMaxSpeed;

    public final ForgeConfigSpec.DoubleValue luxusChassisFuelEfficiency;
    public final ForgeConfigSpec.DoubleValue luxusChassisAcceleration;
    public final ForgeConfigSpec.DoubleValue luxusChassisMaxSpeed;

    public final ForgeConfigSpec.DoubleValue offroadChassisFuelEfficiency;
    public final ForgeConfigSpec.DoubleValue offroadChassisAcceleration;
    public final ForgeConfigSpec.DoubleValue offroadChassisMaxSpeed;

    public final ForgeConfigSpec.DoubleValue truckChassisFuelEfficiency;
    public final ForgeConfigSpec.DoubleValue truckChassisAcceleration;
    public final ForgeConfigSpec.DoubleValue truckChassisMaxSpeed;

    public List<Tag<Fluid>> fuelStationValidFuelList = new ArrayList<>();
    public List<Tag<Block>> vehicleDriveBlockList = new ArrayList<>();

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);

        fuelStationTransferRate = builder.defineInRange("machines.fuel_station.transfer_rate", 5, 1, Short.MAX_VALUE);
        fuelStationValidFuels = builder.comment("If it starts with '#' it is a tag").defineList("machines.fuel_station.valid_fuels", Collections.singletonList("#vehicle:fuel_station"), Objects::nonNull);
        fuelStationMorningPricePer10mb = builder.comment("Price per 10 mb of fuel during morning (0-12000 ticks, 6:00-18:00)").defineInRange("machines.fuel_station.morning_price_per_10mb", 10, 0, Integer.MAX_VALUE);
        fuelStationEveningPricePer10mb = builder.comment("Price per 10 mb of fuel during evening (12000-TICKS_PER_DAY ticks, 18:00-6:00)").defineInRange("machines.fuel_station.evening_price_per_10mb", 5, 0, Integer.MAX_VALUE);

        repairKitRepairAmount = builder.comment("Amount of damage repaired by repair kit (in percentage)").defineInRange("items.repair_kit.repair_amount", 5F, 0.1F, 100F);

        canisterMaxFuel = builder.comment("Maximum fuel capacity for canister (in mb)").defineInRange("items.canister.max_fuel", 100, 1, 1000);

        collideWithEntities = builder.comment("Whether the vehicles should collide with other entities (except vehicles)").define("vehicle.collide_with_entities", true);
        damageEntities = builder.comment("Whether the vehicles should damage other entities on collision").define("vehicle.damage_entities", true);
        hornFlee = builder.comment("Whether animals flee from the vehicle when the horn is activted").define("vehicle.horn_flee", true);
        useBattery = builder.comment("True if starting the vehicle should use battery").define("vehicle.use_battery", true);

        engineStartFuelConsumption = builder.comment("How much fuel is consumed when starting the engine (in mb)").defineInRange("vehicle.engine_start_fuel_consumption", 5, 0, 100);
        idleFuelConsumptionInterval = builder.comment("How often fuel is consumed while idling (in ticks). 600 ticks = 30 seconds. Lower = more frequent consumption.").defineInRange("vehicle.idle_fuel_consumption_interval", 600, 20, 12000);
        idleBatteryRechargeRate = builder.comment("How much battery is recharged per tick while idling (with engine running)").defineInRange("vehicle.idle_battery_recharge_rate", 1, 0, 100);
        drivingBatteryRechargeMultiplier = builder.comment("Battery recharge rate multiplier based on speed while driving. Recharge = idleRate * speed * multiplier").defineInRange("vehicle.driving_battery_recharge_multiplier", 20.0D, 0.0D, 1000.0D);
        temperatureUpdateInterval = builder.comment("How often vehicle temperature is updated (in ticks). 20 ticks = 1 second. Lower = more accurate simulation but higher CPU usage.").defineInRange("vehicle.temperature_update_interval", 20, 1, 200);

        vehicleOffroadSpeed = builder.comment("The speed modifier for vehicles on non road blocks").defineInRange("vehicle.offroad_speed_modifier", 1D, 0.001D, 10D);
        vehicleOnroadSpeed = builder.comment("The speed modifier for vehicles on road blocks", "On road blocks are defined in the config section 'road_blocks'").defineInRange("vehicle.onroad_speed_modifier", 1D, 0.001D, 10D);
        vehicleDriveBlocks = builder.comment("If it starts with '#' it is a tag").defineList("vehicle.road_blocks.blocks", Collections.singletonList("#vehicle:drivable_blocks"), Objects::nonNull);

        tankSmallMaxFuel = builder.comment("Maximum fuel capacity for small tank (in mb)").defineInRange("vehicle.parts.small_tank.max_fuel", 500, 100, 100_000);
        tankMediumMaxFuel = builder.comment("Maximum fuel capacity for medium tank (in mb)").defineInRange("vehicle.parts.medium_tank.max_fuel", 1000, 100, 100_000);
        tankLargeMaxFuel = builder.comment("Maximum fuel capacity for large tank (in mb)").defineInRange("vehicle.parts.large_tank.max_fuel", 1500, 100, 100_000);

        performanceMotorFuelEfficiency = builder.comment("Fuel efficiency multiplier for performance motor (higher = more fuel consumed)").defineInRange("vehicle.parts.performance_motor.fuel_efficiency", 0.25D, 0.001D, 10D);
        normalMotorFuelEfficiency = builder.comment("Fuel efficiency multiplier for normal motor (higher = more fuel consumed)").defineInRange("vehicle.parts.normal_motor.fuel_efficiency", 0.5D, 0.001D, 10D);
        industrialMotorFuelEfficiency = builder.comment("Fuel efficiency multiplier for industrial motor (higher = more fuel consumed)").defineInRange("vehicle.parts.industrial_motor.fuel_efficiency", 0.7D, 0.001D, 10D);
        performanceMotorAcceleration = builder.comment("Acceleration multiplier for performance motor").defineInRange("vehicle.parts.performance_motor.acceleration", 0.04D, 0.001D, 10D);
        normalMotorAcceleration = builder.comment("Acceleration multiplier for normal motor").defineInRange("vehicle.parts.normal_motor.acceleration", 0.035D, 0.001D, 10D);
        industrialMotorAcceleration = builder.comment("Acceleration multiplier for industrial motor").defineInRange("vehicle.parts.industrial_motor.acceleration", 0.032D, 0.001D, 10D);
        performanceMotorMaxSpeed = builder.comment("Maximum speed multiplier for performance motor").defineInRange("vehicle.parts.performance_motor.max_speed", 0.75D, 0.001D, 10D);
        normalMotorMaxSpeed = builder.comment("Maximum speed multiplier for normal motor").defineInRange("vehicle.parts.normal_motor.max_speed", 0.65D, 0.001D, 10D);
        industrialMotorMaxSpeed = builder.comment("Maximum speed multiplier for industrial motor").defineInRange("vehicle.parts.industrial_motor.max_speed", 0.6D, 0.001D, 10D);
        performanceMotorMaxReverseSpeed = builder.comment("Maximum reverse speed for performance motor").defineInRange("vehicle.parts.performance_motor.max_reverse_speed", 0.2D, 0.001D, 10D);
        normalMotorMaxReverseSpeed = builder.comment("Maximum reverse speed for normal motor").defineInRange("vehicle.parts.normal_motor.max_reverse_speed", 0.2D, 0.001D, 10D);
        industrialMotorMaxReverseSpeed = builder.comment("Maximum reverse speed for industrial motor").defineInRange("vehicle.parts.industrial_motor.max_reverse_speed", 0.15D, 0.001D, 10D);

        vanChassisFuelEfficiency = builder.comment("Fuel efficiency multiplier for van chassis").defineInRange("vehicle.parts.van_chassis.fuel_efficiency", 0.7D, 0.001D, 10D);
        vanChassisAcceleration = builder.comment("Acceleration multiplier for van chassis").defineInRange("vehicle.parts.van_chassis.acceleration", 0.95D, 0.001D, 10D);
        vanChassisMaxSpeed = builder.comment("Maximum speed multiplier for van chassis").defineInRange("vehicle.parts.van_chassis.max_speed", 0.85D, 0.001D, 10D);
        limousineChassisEfficiency = builder.comment("Fuel efficiency multiplier for limousine chassis").defineInRange("vehicle.parts.limousine_chassis.fuel_efficiency", 0.8D, 0.001D, 10D);
        limousineChassisAcceleration = builder.comment("Acceleration multiplier for limousine chassis").defineInRange("vehicle.parts.limousine_chassis.acceleration", 1D, 0.001D, 10D);
        limousineChassisMaxSpeed = builder.comment("Maximum speed multiplier for limousine chassis").defineInRange("vehicle.parts.limousine_chassis.max_speed", 0.9D, 0.001D, 10D);
        luxusChassisFuelEfficiency = builder.comment("Fuel efficiency multiplier for luxus chassis").defineInRange("vehicle.parts.luxus_chassis.fuel_efficiency", 0.9D, 0.001D, 10D);
        luxusChassisAcceleration = builder.comment("Acceleration multiplier for luxus chassis").defineInRange("vehicle.parts.luxus_chassis.acceleration", 1D, 0.001D, 10D);
        luxusChassisMaxSpeed = builder.comment("Maximum speed multiplier for luxus chassis").defineInRange("vehicle.parts.luxus_chassis.max_speed", 1D, 0.001D, 10D);
        offroadChassisFuelEfficiency = builder.comment("Fuel efficiency multiplier for offroad chassis").defineInRange("vehicle.parts.offroad_chassis.fuel_efficiency", 0.6D, 0.001D, 10D);
        offroadChassisAcceleration = builder.comment("Acceleration multiplier for offroad chassis").defineInRange("vehicle.parts.offroad_chassis.acceleration", 0.8D, 0.001D, 10D);
        offroadChassisMaxSpeed = builder.comment("Maximum speed multiplier for offroad chassis").defineInRange("vehicle.parts.offroad_chassis.max_speed", 0.7D, 0.001D, 10D);
        truckChassisFuelEfficiency = builder.comment("Fuel efficiency multiplier for truck chassis").defineInRange("vehicle.parts.truck_chassis.fuel_efficiency", 0.6D, 0.001D, 10D);
        truckChassisAcceleration = builder.comment("Acceleration multiplier for truck chassis").defineInRange("vehicle.parts.truck_chassis.acceleration", 0.8D, 0.001D, 10D);
        truckChassisMaxSpeed = builder.comment("Maximum speed multiplier for truck chassis").defineInRange("vehicle.parts.truck_chassis.max_speed", 0.765D, 0.001D, 10D);
    }

    @Override
    public void onReload(ModConfigEvent event) {
        super.onReload(event);
        fuelStationValidFuelList = fuelStationValidFuels.get().stream().map(TagUtils::getFluid).filter(Objects::nonNull).collect(Collectors.toList());
        vehicleDriveBlockList = vehicleDriveBlocks.get().stream().map(TagUtils::getBlock).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
