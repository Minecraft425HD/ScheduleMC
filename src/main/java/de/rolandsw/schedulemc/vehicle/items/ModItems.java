package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.ModBlocks;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import de.rolandsw.schedulemc.vehicle.fluids.ModFluids;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

    public static final RegistryObject<ItemCanister> CANISTER = ITEM_REGISTER.register("empty_diesel_can", () -> new ItemCanister());
    public static final RegistryObject<ItemBioDieselCanister> BIO_DIESEL_CANISTER = ITEM_REGISTER.register("full_diesel_can", () -> new ItemBioDieselCanister());
    public static final RegistryObject<ItemRepairKit> REPAIR_KIT = ITEM_REGISTER.register("maintenance_kit", () -> new ItemRepairKit());
    public static final RegistryObject<ItemKey> KEY = ITEM_REGISTER.register("key", () -> new ItemKey());
    public static final RegistryObject<ItemBattery> BATTERY = ITEM_REGISTER.register("starter_battery", () -> new ItemBattery());

    public static final RegistryObject<ItemLicensePlate> LICENSE_PLATE = ITEM_REGISTER.register("license_sign", () -> new ItemLicensePlate());

    public static final RegistryObject<VehicleSpawnTool> VEHICLE_SPAWN_TOOL = ITEM_REGISTER.register("spawn_tool", () -> new VehicleSpawnTool());

    public static final RegistryObject<ItemVehiclePart> NORMAL_MOTOR = ITEM_REGISTER.register("normal_motor", () -> new ItemVehiclePart(PartRegistry.NORMAL_MOTOR));
    public static final RegistryObject<ItemVehiclePart> PERFORMANCE_MOTOR = ITEM_REGISTER.register("performance_motor", () -> new ItemVehiclePart(PartRegistry.PERFORMANCE_MOTOR));
    public static final RegistryObject<ItemVehiclePart> INDUSTRIAL_MOTOR = ITEM_REGISTER.register("industrial_motor", () -> new ItemVehiclePart(PartRegistry.INDUSTRIAL_MOTOR));

    // Normal vehicle tires
    public static final RegistryObject<ItemVehiclePart> STANDARD_TIRE = ITEM_REGISTER.register("standard_tire", () -> new ItemVehiclePart(PartRegistry.STANDARD_TIRE));
    public static final RegistryObject<ItemVehiclePart> SPORT_TIRE = ITEM_REGISTER.register("sport_tire", () -> new ItemVehiclePart(PartRegistry.SPORT_TIRE));
    public static final RegistryObject<ItemVehiclePart> PREMIUM_TIRE = ITEM_REGISTER.register("premium_tire", () -> new ItemVehiclePart(PartRegistry.PREMIUM_TIRE));

    // Truck tires
    public static final RegistryObject<ItemVehiclePart> OFFROAD_TIRE = ITEM_REGISTER.register("offroad_tire", () -> new ItemVehiclePart(PartRegistry.OFFROAD_TIRE));
    public static final RegistryObject<ItemVehiclePart> ALLTERRAIN_TIRE = ITEM_REGISTER.register("allterrain_tire", () -> new ItemVehiclePart(PartRegistry.ALLTERRAIN_TIRE));
    public static final RegistryObject<ItemVehiclePart> HEAVY_DUTY_TIRE = ITEM_REGISTER.register("heavyduty_tire", () -> new ItemVehiclePart(PartRegistry.HEAVY_DUTY_TIRE));

    public static final RegistryObject<ItemVehiclePart> LIMOUSINE_CHASSIS = ITEM_REGISTER.register("limousine_chassis", () -> new ItemVehiclePart(PartRegistry.LIMOUSINE_CHASSIS));

    public static final RegistryObject<ItemVehiclePart>[] WOOD_BODIES = new RegistryObject[]{
            LIMOUSINE_CHASSIS
    };

    public static final RegistryObject<ItemVehiclePart> VAN_CHASSIS = ITEM_REGISTER.register("van_chassis", () -> new ItemVehiclePart(PartRegistry.VAN_CHASSIS));

    public static final RegistryObject<ItemVehiclePart>[] BIG_WOOD_BODIES = new RegistryObject[]{
            VAN_CHASSIS
    };

    public static final RegistryObject<ItemVehiclePart> TRUCK_CHASSIS = ITEM_REGISTER.register("truck_chassis", () -> new ItemVehiclePart(PartRegistry.TRUCK_CHASSIS));

    public static final RegistryObject<ItemVehiclePart>[] TRANSPORTER_BODIES = new RegistryObject[]{
            TRUCK_CHASSIS
    };

    public static final RegistryObject<ItemVehiclePart> OFFROAD_CHASSIS = ITEM_REGISTER.register("offroad_chassis", () -> new ItemVehiclePart(PartRegistry.OFFROAD_CHASSIS));

    public static final RegistryObject<ItemVehiclePart>[] SUV_BODIES = new RegistryObject[]{
            OFFROAD_CHASSIS
    };

    public static final RegistryObject<ItemVehiclePart> LUXUS_CHASSIS = ITEM_REGISTER.register("luxus_chassis", () -> new ItemVehiclePart(PartRegistry.LUXUS_CHASSIS));

    public static final RegistryObject<ItemVehiclePart>[] SPORT_BODIES = new RegistryObject[]{
            LUXUS_CHASSIS
    };

    public static final RegistryObject<ItemVehiclePart> CARGO_MODULE = ITEM_REGISTER.register("cargo_module", () -> new ItemVehiclePart(PartRegistry.CARGO_MODULE));

    public static final RegistryObject<ItemVehiclePart>[] CONTAINERS = new RegistryObject[]{
            CARGO_MODULE
    };

    public static final RegistryObject<ItemVehiclePart> FLUID_MODULE = ITEM_REGISTER.register("fluid_module", () -> new ItemVehiclePart(PartRegistry.FLUID_MODULE));

    public static final RegistryObject<ItemVehiclePart>[] TANK_CONTAINERS = new RegistryObject[]{
            FLUID_MODULE
    };

    // Fenders (Bumpers)
    public static final RegistryObject<ItemVehiclePart> FENDER_BASIC = ITEM_REGISTER.register("fender_basic", () -> new ItemVehiclePart(PartRegistry.FENDER_BASIC));
    public static final RegistryObject<ItemVehiclePart> FENDER_CHROME = ITEM_REGISTER.register("fender_chrome", () -> new ItemVehiclePart(PartRegistry.FENDER_CHROME));
    public static final RegistryObject<ItemVehiclePart> FENDER_SPORT = ITEM_REGISTER.register("fender_sport", () -> new ItemVehiclePart(PartRegistry.FENDER_SPORT));

    public static final RegistryObject<ItemVehiclePart>[] BUMPERS = new RegistryObject[]{
            FENDER_BASIC, FENDER_CHROME, FENDER_SPORT
    };

    public static final RegistryObject<ItemVehiclePart> LICENSE_PLATE_HOLDER = ITEM_REGISTER.register("license_sign_mount", () -> new ItemVehiclePart(PartRegistry.LICENSE_PLATE_HOLDER));

    public static final RegistryObject<ItemVehiclePart> TANK_15L = ITEM_REGISTER.register("tank_15l", () -> new ItemVehiclePart(PartRegistry.TANK_15L));
    public static final RegistryObject<ItemVehiclePart> TANK_30L = ITEM_REGISTER.register("tank_30l", () -> new ItemVehiclePart(PartRegistry.TANK_30L));
    public static final RegistryObject<ItemVehiclePart> TANK_50L = ITEM_REGISTER.register("tank_50l", () -> new ItemVehiclePart(PartRegistry.TANK_50L));

    // Pre-built vehicles
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_OAK = ITEM_REGISTER.register("limousine", () -> new ItemSpawnVehicle(PartRegistry.LIMOUSINE_CHASSIS));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_BIG_OAK = ITEM_REGISTER.register("van", () -> new ItemSpawnVehicle(PartRegistry.VAN_CHASSIS));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_TRANSPORTER = ITEM_REGISTER.register("truck", () -> new ItemSpawnVehicle(PartRegistry.TRUCK_CHASSIS));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_SUV = ITEM_REGISTER.register("suv", () -> new ItemSpawnVehicle(PartRegistry.OFFROAD_CHASSIS));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_SPORT = ITEM_REGISTER.register("sports_car", () -> new ItemSpawnVehicle(PartRegistry.LUXUS_CHASSIS));

    public static final RegistryObject<Item> FUEL_STATION = ITEM_REGISTER.register("fuel_station", () -> ModBlocks.FUEL_STATION.get().toItem());
    public static final RegistryObject<Item> GARAGE = ITEM_REGISTER.register("garage", () -> ModBlocks.GARAGE.get().toItem());

    public static void init(IEventBus modEventBus) {
        ITEM_REGISTER.register(modEventBus);
    }

}
