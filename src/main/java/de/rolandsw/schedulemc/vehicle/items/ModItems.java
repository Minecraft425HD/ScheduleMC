package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.ModBlocks;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import de.rolandsw.schedulemc.vehicle.fluids.ModFluids;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

    public static final RegistryObject<ItemCanister> CANISTER = ITEM_REGISTER.register("canister", () -> new ItemCanister());
    public static final RegistryObject<ItemBioDieselCanister> BIO_DIESEL_CANISTER = ITEM_REGISTER.register("bio_diesel_canister", () -> new ItemBioDieselCanister());
    public static final RegistryObject<ItemRepairKit> REPAIR_KIT = ITEM_REGISTER.register("repair_kit", () -> new ItemRepairKit());
    public static final RegistryObject<ItemKey> KEY = ITEM_REGISTER.register("key", () -> new ItemKey());
    public static final RegistryObject<ItemBattery> BATTERY = ITEM_REGISTER.register("battery", () -> new ItemBattery());

    public static final RegistryObject<ItemLicensePlate> LICENSE_PLATE = ITEM_REGISTER.register("license_plate", () -> new ItemLicensePlate());

    public static final RegistryObject<VehicleSpawnTool> VEHICLE_SPAWN_TOOL = ITEM_REGISTER.register("vehicle_spawn_tool", () -> new VehicleSpawnTool());

    public static final RegistryObject<ItemVehiclePart> ENGINE_3_CYLINDER = ITEM_REGISTER.register("engine_3_cylinder", () -> new ItemVehiclePart(PartRegistry.ENGINE_3_CYLINDER));
    public static final RegistryObject<ItemVehiclePart> ENGINE_6_CYLINDER = ITEM_REGISTER.register("engine_6_cylinder", () -> new ItemVehiclePart(PartRegistry.ENGINE_6_CYLINDER));
    public static final RegistryObject<ItemVehiclePart> ENGINE_TRUCK = ITEM_REGISTER.register("engine_truck", () -> new ItemVehiclePart(PartRegistry.ENGINE_TRUCK));

    public static final RegistryObject<ItemVehiclePart> WHEEL = ITEM_REGISTER.register("wheel", () -> new ItemVehiclePart(PartRegistry.WHEEL));
    public static final RegistryObject<ItemVehiclePart> BIG_WHEEL = ITEM_REGISTER.register("big_wheel", () -> new ItemVehiclePart(PartRegistry.BIG_WHEEL));

    public static final RegistryObject<ItemVehiclePart> OAK_BODY = ITEM_REGISTER.register("oak_body", () -> new ItemVehiclePart(PartRegistry.OAK_BODY));

    public static final RegistryObject<ItemVehiclePart>[] WOOD_BODIES = new RegistryObject[]{
            OAK_BODY
    };

    public static final RegistryObject<ItemVehiclePart> BIG_OAK_BODY = ITEM_REGISTER.register("big_oak_body", () -> new ItemVehiclePart(PartRegistry.BIG_OAK_BODY));

    public static final RegistryObject<ItemVehiclePart>[] BIG_WOOD_BODIES = new RegistryObject[]{
            BIG_OAK_BODY
    };

    public static final RegistryObject<ItemVehiclePart> WHITE_TRANSPORTER_BODY = ITEM_REGISTER.register("white_transporter_body", () -> new ItemVehiclePart(PartRegistry.WHITE_TRANSPORTER_BODY));

    public static final RegistryObject<ItemVehiclePart>[] TRANSPORTER_BODIES = new RegistryObject[]{
            WHITE_TRANSPORTER_BODY
    };

    public static final RegistryObject<ItemVehiclePart> WHITE_SUV_BODY = ITEM_REGISTER.register("white_suv_body", () -> new ItemVehiclePart(PartRegistry.WHITE_SUV_BODY));

    public static final RegistryObject<ItemVehiclePart>[] SUV_BODIES = new RegistryObject[]{
            WHITE_SUV_BODY
    };

    public static final RegistryObject<ItemVehiclePart> WHITE_SPORT_BODY = ITEM_REGISTER.register("white_sport_body", () -> new ItemVehiclePart(PartRegistry.WHITE_SPORT_BODY));

    public static final RegistryObject<ItemVehiclePart>[] SPORT_BODIES = new RegistryObject[]{
            WHITE_SPORT_BODY
    };

    public static final RegistryObject<ItemVehiclePart> WHITE_CONTAINER = ITEM_REGISTER.register("white_container", () -> new ItemVehiclePart(PartRegistry.WHITE_CONTAINER));

    public static final RegistryObject<ItemVehiclePart>[] CONTAINERS = new RegistryObject[]{
            WHITE_CONTAINER
    };

    public static final RegistryObject<ItemVehiclePart> WHITE_TANK_CONTAINER = ITEM_REGISTER.register("white_tank_container", () -> new ItemVehiclePart(PartRegistry.WHITE_TANK_CONTAINER));

    public static final RegistryObject<ItemVehiclePart>[] TANK_CONTAINERS = new RegistryObject[]{
            WHITE_TANK_CONTAINER
    };

    public static final RegistryObject<ItemVehiclePart> OAK_BUMPER = ITEM_REGISTER.register("oak_bumper", () -> new ItemVehiclePart(PartRegistry.OAK_BUMPER));

    public static final RegistryObject<ItemVehiclePart>[] BUMPERS = new RegistryObject[]{
            OAK_BUMPER
    };

    public static final RegistryObject<ItemVehiclePart> IRON_LICENSE_PLATE_HOLDER = ITEM_REGISTER.register("iron_license_plate_holder", () -> new ItemVehiclePart(PartRegistry.IRON_LICENSE_PLATE_HOLDER));

    public static final RegistryObject<ItemVehiclePart> SMALL_TANK = ITEM_REGISTER.register("small_tank", () -> new ItemVehiclePart(PartRegistry.SMALL_TANK));
    public static final RegistryObject<ItemVehiclePart> MEDIUM_TANK = ITEM_REGISTER.register("medium_tank", () -> new ItemVehiclePart(PartRegistry.MEDIUM_TANK));
    public static final RegistryObject<ItemVehiclePart> LARGE_TANK = ITEM_REGISTER.register("large_tank", () -> new ItemVehiclePart(PartRegistry.LARGE_TANK));

    // Pre-built vehicles
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_OAK = ITEM_REGISTER.register("spawn_car_oak", () -> new ItemSpawnVehicle(PartRegistry.OAK_BODY));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_BIG_OAK = ITEM_REGISTER.register("spawn_car_big_oak", () -> new ItemSpawnVehicle(PartRegistry.BIG_OAK_BODY));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_TRANSPORTER = ITEM_REGISTER.register("spawn_car_white_transporter", () -> new ItemSpawnVehicle(PartRegistry.WHITE_TRANSPORTER_BODY));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_SUV = ITEM_REGISTER.register("spawn_car_white_suv", () -> new ItemSpawnVehicle(PartRegistry.WHITE_SUV_BODY));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_SPORT = ITEM_REGISTER.register("spawn_car_white_sport", () -> new ItemSpawnVehicle(PartRegistry.WHITE_SPORT_BODY));

    public static final RegistryObject<VehicleBucketItem> BIO_DIESEL_BUCKET = ITEM_REGISTER.register("bio_diesel_bucket", () -> new VehicleBucketItem(ModFluids.BIO_DIESEL.get()));

    public static final RegistryObject<Item> GAS_STATION = ITEM_REGISTER.register("gas_station", () -> ModBlocks.GAS_STATION.get().toItem());

    public static void init() {
        ITEM_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
