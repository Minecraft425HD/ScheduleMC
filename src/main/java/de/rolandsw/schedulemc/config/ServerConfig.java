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

    // Distance-based fuel consumption (L per 10 km, where 500 blocks = 1 km)
    public final ForgeConfigSpec.DoubleValue normalMotorFuelConsumption;
    public final ForgeConfigSpec.DoubleValue performanceMotorFuelConsumption;
    public final ForgeConfigSpec.DoubleValue performance2MotorFuelConsumption;

    public final ForgeConfigSpec.DoubleValue performanceMotorFuelEfficiency;
    public final ForgeConfigSpec.DoubleValue normalMotorFuelEfficiency;
    public final ForgeConfigSpec.DoubleValue performance2MotorFuelEfficiency;

    public final ForgeConfigSpec.DoubleValue performanceMotorAcceleration;
    public final ForgeConfigSpec.DoubleValue normalMotorAcceleration;
    public final ForgeConfigSpec.DoubleValue performance2MotorAcceleration;

    public final ForgeConfigSpec.DoubleValue performanceMotorMaxSpeed;
    public final ForgeConfigSpec.DoubleValue normalMotorMaxSpeed;
    public final ForgeConfigSpec.DoubleValue performance2MotorMaxSpeed;

    public final ForgeConfigSpec.DoubleValue performanceMotorMaxReverseSpeed;
    public final ForgeConfigSpec.DoubleValue normalMotorMaxReverseSpeed;
    public final ForgeConfigSpec.DoubleValue performance2MotorMaxReverseSpeed;

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

    // Chassis Inventory Slots
    public final ForgeConfigSpec.IntValue limousineChassisInventorySlots;
    public final ForgeConfigSpec.IntValue vanChassisInventorySlots;
    public final ForgeConfigSpec.IntValue truckChassisInventorySlots;
    public final ForgeConfigSpec.IntValue offroadChassisInventorySlots;
    public final ForgeConfigSpec.IntValue luxusChassisInventorySlots;

    // Container Configuration
    public final ForgeConfigSpec.IntValue itemContainerSlots;
    public final ForgeConfigSpec.IntValue fluidContainerCapacity;
    public final ForgeConfigSpec.DoubleValue containerReinstallationCost;

    // Towing Service Configuration
    public final ForgeConfigSpec.DoubleValue towingBaseFee;
    public final ForgeConfigSpec.DoubleValue towingDistanceFeePerBlock;
    public final ForgeConfigSpec.IntValue towingDamageReductionPercent;

    // Membership Configuration
    public final ForgeConfigSpec.IntValue membershipPaymentIntervalDays;
    public final ForgeConfigSpec.DoubleValue membershipBronzeFee;
    public final ForgeConfigSpec.IntValue membershipBronzeCoveragePercent;
    public final ForgeConfigSpec.DoubleValue membershipSilverFee;
    public final ForgeConfigSpec.IntValue membershipSilverCoveragePercent;
    public final ForgeConfigSpec.DoubleValue membershipGoldFee;
    public final ForgeConfigSpec.IntValue membershipGoldCoveragePercent;

    // Vehicle Aging (Odometer)
    public final ForgeConfigSpec.BooleanValue vehicleAgingEnabled;
    public final ForgeConfigSpec.LongValue odometerTier1;
    public final ForgeConfigSpec.LongValue odometerTier2;
    public final ForgeConfigSpec.LongValue odometerTier3;
    public final ForgeConfigSpec.LongValue odometerTier4;
    public final ForgeConfigSpec.DoubleValue agingMaxHealthTier0;
    public final ForgeConfigSpec.DoubleValue agingMaxHealthTier1;
    public final ForgeConfigSpec.DoubleValue agingMaxHealthTier2;
    public final ForgeConfigSpec.DoubleValue agingMaxHealthTier3;

    // Tire Season System (Serene Seasons Integration)
    public final ForgeConfigSpec.BooleanValue tireSeasonEnabled;
    public final ForgeConfigSpec.DoubleValue tireSeasonCorrectModifier;
    public final ForgeConfigSpec.DoubleValue tireSeasonWrongModifier;
    public final ForgeConfigSpec.DoubleValue tireSeasonAllSeasonModifier;

    public List<Tag<Fluid>> fuelStationValidFuelList = new ArrayList<>();
    public List<Tag<Block>> vehicleDriveBlockList = new ArrayList<>();

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);

        fuelStationTransferRate = builder.defineInRange("machines.fuel_station.transfer_rate", 5, 1, Short.MAX_VALUE);
        fuelStationValidFuels = builder.comment("If it starts with '#' it is a tag").defineList("machines.fuel_station.valid_fuels", Collections.singletonList("#vehicle:fuel_station"), Objects::nonNull);
        fuelStationMorningPricePer10mb = builder.comment("Price per 10 mb of fuel during morning (0-12000 ticks, 6:00-18:00)").defineInRange("machines.fuel_station.morning_price_per_10mb", 10, 0, Integer.MAX_VALUE);
        fuelStationEveningPricePer10mb = builder.comment("Price per 10 mb of fuel during evening (12000-24000 ticks, 18:00-6:00)").defineInRange("machines.fuel_station.evening_price_per_10mb", 5, 0, Integer.MAX_VALUE);

        repairKitRepairAmount = builder.defineInRange("items.repair_kit.repair_amount", 5F, 0.1F, 100F);

        canisterMaxFuel = builder.defineInRange("items.canister.max_fuel", 1000, 1, 10000);

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

        tankSmallMaxFuel = builder.comment("Default tank: 11 Liter = 11000 mB").defineInRange("vehicle.parts.small_tank.max_fuel", 11000, 100, 100_000);
        tankMediumMaxFuel = builder.comment("1st upgrade: 15 Liter = 15000 mB").defineInRange("vehicle.parts.medium_tank.max_fuel", 15000, 100, 100_000);
        tankLargeMaxFuel = builder.comment("2nd upgrade: 20 Liter = 20000 mB").defineInRange("vehicle.parts.large_tank.max_fuel", 20000, 100, 100_000);

        normalMotorFuelConsumption = builder.comment("Fuel consumption of the normal motor in liters per 10 km (500 blocks = 1 km)").defineInRange("vehicle.fuel.normal_motor_consumption_per_10km", 5.5D, 0.1D, 100.0D);
        performanceMotorFuelConsumption = builder.comment("Fuel consumption of the performance motor in liters per 10 km").defineInRange("vehicle.fuel.performance_motor_consumption_per_10km", 7.0D, 0.1D, 100.0D);
        performance2MotorFuelConsumption = builder.comment("Fuel consumption of the performance 2 motor in liters per 10 km").defineInRange("vehicle.fuel.performance_2_motor_consumption_per_10km", 8.5D, 0.1D, 100.0D);

        normalMotorFuelEfficiency = builder.defineInRange("vehicle.parts.normal_motor.fuel_efficiency", 0.5D, 0.001D, 10D);
        performanceMotorFuelEfficiency = builder.defineInRange("vehicle.parts.performance_motor.fuel_efficiency", 0.25D, 0.001D, 10D);
        performance2MotorFuelEfficiency = builder.defineInRange("vehicle.parts.performance_2_motor.fuel_efficiency", 0.15D, 0.001D, 10D);
        normalMotorAcceleration = builder.defineInRange("vehicle.parts.normal_motor.acceleration", 0.030D, 0.001D, 10D);
        performanceMotorAcceleration = builder.defineInRange("vehicle.parts.performance_motor.acceleration", 0.035D, 0.001D, 10D);
        performance2MotorAcceleration = builder.defineInRange("vehicle.parts.performance_2_motor.acceleration", 0.040D, 0.001D, 10D);
        normalMotorMaxSpeed = builder.defineInRange("vehicle.parts.normal_motor.max_speed", 0.55D, 0.001D, 10D);
        performanceMotorMaxSpeed = builder.defineInRange("vehicle.parts.performance_motor.max_speed", 0.65D, 0.001D, 10D);
        performance2MotorMaxSpeed = builder.defineInRange("vehicle.parts.performance_2_motor.max_speed", 0.75D, 0.001D, 10D);
        normalMotorMaxReverseSpeed = builder.defineInRange("vehicle.parts.normal_motor.max_reverse_speed", 0.15D, 0.001D, 10D);
        performanceMotorMaxReverseSpeed = builder.defineInRange("vehicle.parts.performance_motor.max_reverse_speed", 0.18D, 0.001D, 10D);
        performance2MotorMaxReverseSpeed = builder.defineInRange("vehicle.parts.performance_2_motor.max_reverse_speed", 0.20D, 0.001D, 10D);

        vanChassisFuelEfficiency = builder.defineInRange("vehicle.parts.van_chassis.fuel_efficiency", 0.7D, 0.001D, 10D);
        vanChassisAcceleration = builder.defineInRange("vehicle.parts.van_chassis.acceleration", 0.95D, 0.001D, 10D);
        vanChassisMaxSpeed = builder.defineInRange("vehicle.parts.van_chassis.max_speed", 0.85D, 0.001D, 10D);
        limousineChassisEfficiency = builder.defineInRange("vehicle.parts.limousine_chassis.fuel_efficiency", 0.8D, 0.001D, 10D);
        limousineChassisAcceleration = builder.defineInRange("vehicle.parts.limousine_chassis.acceleration", 1D, 0.001D, 10D);
        limousineChassisMaxSpeed = builder.defineInRange("vehicle.parts.limousine_chassis.max_speed", 0.9D, 0.001D, 10D);
        luxusChassisFuelEfficiency = builder.defineInRange("vehicle.parts.luxus_chassis.fuel_efficiency", 0.9D, 0.001D, 10D);
        luxusChassisAcceleration = builder.defineInRange("vehicle.parts.luxus_chassis.acceleration", 1D, 0.001D, 10D);
        luxusChassisMaxSpeed = builder.defineInRange("vehicle.parts.luxus_chassis.max_speed", 1D, 0.001D, 10D);
        offroadChassisFuelEfficiency = builder.defineInRange("vehicle.parts.offroad_chassis.fuel_efficiency", 0.6D, 0.001D, 10D);
        offroadChassisAcceleration = builder.defineInRange("vehicle.parts.offroad_chassis.acceleration", 0.8D, 0.001D, 10D);
        offroadChassisMaxSpeed = builder.defineInRange("vehicle.parts.offroad_chassis.max_speed", 0.7D, 0.001D, 10D);
        truckChassisFuelEfficiency = builder.defineInRange("vehicle.parts.truck_chassis.fuel_efficiency", 0.6D, 0.001D, 10D);
        truckChassisAcceleration = builder.defineInRange("vehicle.parts.truck_chassis.acceleration", 0.8D, 0.001D, 10D);
        truckChassisMaxSpeed = builder.defineInRange("vehicle.parts.truck_chassis.max_speed", 0.765D, 0.001D, 10D);

        // Chassis Inventory Slots
        limousineChassisInventorySlots = builder
            .comment("Number of inventory slots for Limousine chassis")
            .defineInRange("vehicle.parts.limousine_chassis.inventory_slots", 4, 0, 54);
        vanChassisInventorySlots = builder
            .comment("Number of inventory slots for Van chassis")
            .defineInRange("vehicle.parts.van_chassis.inventory_slots", 6, 0, 54);
        truckChassisInventorySlots = builder
            .comment("Number of inventory slots for Truck chassis (base, without containers)")
            .defineInRange("vehicle.parts.truck_chassis.inventory_slots", 0, 0, 54);
        offroadChassisInventorySlots = builder
            .comment("Number of inventory slots for Offroad/SUV chassis")
            .defineInRange("vehicle.parts.offroad_chassis.inventory_slots", 6, 0, 54);
        luxusChassisInventorySlots = builder
            .comment("Number of inventory slots for Luxus/Sport chassis")
            .defineInRange("vehicle.parts.luxus_chassis.inventory_slots", 3, 0, 54);

        // Container Configuration
        itemContainerSlots = builder
            .comment("Number of inventory slots provided by Item Container (truck-only)")
            .defineInRange("vehicle.parts.item_container.slots", 12, 0, 54);
        fluidContainerCapacity = builder
            .comment("Fluid capacity of Fluid Container in mB (truck-only). 100000 mB = 100 Buckets")
            .defineInRange("vehicle.parts.fluid_container.capacity", 100000, 0, 1000000);
        containerReinstallationCost = builder
            .comment("Cost to reinstall a container after it was removed (in €). First installation is free.")
            .defineInRange("vehicle.parts.container.reinstallation_cost", 7500.0D, 0.0D, 1000000.0D);

        // Towing Service
        towingBaseFee = builder
            .comment("Base fee for towing a vehicle (in €)")
            .defineInRange("towing.base_fee", 100.0D, 0.0D, 10000.0D);
        towingDistanceFeePerBlock = builder
            .comment("Additional fee per block distance (in €)")
            .defineInRange("towing.distance_fee_per_block", 0.5D, 0.0D, 100.0D);
        towingDamageReductionPercent = builder
            .comment("Percentage of damage reduction when collecting vehicle from towing yard")
            .defineInRange("towing.damage_reduction_percent", 10, 0, 100);

        // Membership System
        membershipPaymentIntervalDays = builder
            .comment("How often membership fees are charged (in Minecraft days)")
            .defineInRange("towing.membership.payment_interval_days", 7, 1, 30);

        membershipBronzeFee = builder
            .comment("Monthly fee for Bronze membership (in €)")
            .defineInRange("towing.membership.bronze.fee", 50.0D, 0.0D, 10000.0D);
        membershipBronzeCoveragePercent = builder
            .comment("Percentage of towing costs covered by Bronze membership")
            .defineInRange("towing.membership.bronze.coverage_percent", 33, 0, 100);

        membershipSilverFee = builder
            .comment("Monthly fee for Silver membership (in €)")
            .defineInRange("towing.membership.silver.fee", 150.0D, 0.0D, 10000.0D);
        membershipSilverCoveragePercent = builder
            .comment("Percentage of towing costs covered by Silver membership")
            .defineInRange("towing.membership.silver.coverage_percent", 66, 0, 100);

        membershipGoldFee = builder
            .comment("Monthly fee for Gold membership (in €)")
            .defineInRange("towing.membership.gold.fee", 300.0D, 0.0D, 10000.0D);
        membershipGoldCoveragePercent = builder
            .comment("Percentage of towing costs covered by Gold membership (100 = free towing)")
            .defineInRange("towing.membership.gold.coverage_percent", 100, 0, 100);

        // Vehicle Aging (Odometer)
        vehicleAgingEnabled = builder
            .comment("Enable vehicle aging system. Vehicles degrade as they accumulate distance.")
            .define("vehicle.aging.enabled", true);
        odometerTier1 = builder
            .comment("Blocks driven until max health drops to tier 1 (default: 250000 = 250km)")
            .defineInRange("vehicle.aging.odometer_tier_1", 250_000L, 1L, 10_000_000L);
        odometerTier2 = builder
            .comment("Blocks driven until max health drops to tier 2 (default: 500000 = 500km)")
            .defineInRange("vehicle.aging.odometer_tier_2", 500_000L, 1L, 10_000_000L);
        odometerTier3 = builder
            .comment("Blocks driven until max health drops to tier 3 (default: 750000 = 750km)")
            .defineInRange("vehicle.aging.odometer_tier_3", 750_000L, 1L, 10_000_000L);
        odometerTier4 = builder
            .comment("Blocks driven until max health drops to tier 4 (default: 1000000 = 1000km)")
            .defineInRange("vehicle.aging.odometer_tier_4", 1_000_000L, 1L, 10_000_000L);
        agingMaxHealthTier0 = builder
            .comment("Max health percentage for tier 0 (0-tier1 blocks). 1.0 = 100%")
            .defineInRange("vehicle.aging.max_health_tier_0", 1.0D, 0.01D, 1.0D);
        agingMaxHealthTier1 = builder
            .comment("Max health percentage for tier 1 (tier1-tier2 blocks). 0.75 = 75%")
            .defineInRange("vehicle.aging.max_health_tier_1", 0.75D, 0.01D, 1.0D);
        agingMaxHealthTier2 = builder
            .comment("Max health percentage for tier 2 (tier2-tier3 blocks). 0.5 = 50%")
            .defineInRange("vehicle.aging.max_health_tier_2", 0.5D, 0.01D, 1.0D);
        agingMaxHealthTier3 = builder
            .comment("Max health percentage for tier 3 (tier3+ blocks). 0.25 = 25%")
            .defineInRange("vehicle.aging.max_health_tier_3", 0.25D, 0.01D, 1.0D);

        // Tire Season System
        tireSeasonEnabled = builder
            .comment("Enable tire season system. Requires Serene Seasons mod. Wrong tires reduce speed.")
            .define("vehicle.tire_season.enabled", true);
        tireSeasonCorrectModifier = builder
            .comment("Speed modifier when correct tires for the season are equipped (1.0 = 100%)")
            .defineInRange("vehicle.tire_season.correct_modifier", 1.0D, 0.1D, 2.0D);
        tireSeasonWrongModifier = builder
            .comment("Speed modifier when wrong tires for the season are equipped (0.5 = 50%)")
            .defineInRange("vehicle.tire_season.wrong_modifier", 0.5D, 0.1D, 2.0D);
        tireSeasonAllSeasonModifier = builder
            .comment("Speed modifier for all-season tires (0.85 = 85%)")
            .defineInRange("vehicle.tire_season.all_season_modifier", 0.85D, 0.1D, 2.0D);
    }

    @Override
    public void onReload(ModConfigEvent event) {
        super.onReload(event);
        fuelStationValidFuelList = fuelStationValidFuels.get().stream().map(TagUtils::getFluid).filter(Objects::nonNull).collect(Collectors.toList());
        vehicleDriveBlockList = vehicleDriveBlocks.get().stream().map(TagUtils::getBlock).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
