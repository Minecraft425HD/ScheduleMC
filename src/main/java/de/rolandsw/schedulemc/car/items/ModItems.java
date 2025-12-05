package de.rolandsw.schedulemc.car.items;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.BlockPaint;
import de.rolandsw.schedulemc.car.blocks.ModBlocks;
import de.rolandsw.schedulemc.car.entity.car.parts.PartRegistry;
import de.rolandsw.schedulemc.car.fluids.ModFluids;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

    public static final RegistryObject<ItemPainter> PAINTER = ITEM_REGISTER.register("painter", () -> new ItemPainter(false));
    public static final RegistryObject<ItemPainter> PAINTER_YELLOW = ITEM_REGISTER.register("painter_yellow", () -> new ItemPainter(true));
    public static final RegistryObject<ItemCanister> CANISTER = ITEM_REGISTER.register("canister", () -> new ItemCanister());
    public static final RegistryObject<ItemRepairKit> REPAIR_KIT = ITEM_REGISTER.register("repair_kit", () -> new ItemRepairKit());
    public static final RegistryObject<ItemRepairTool> WRENCH = ITEM_REGISTER.register("wrench", () -> new ItemRepairTool());
    public static final RegistryObject<ItemRepairTool> SCREW_DRIVER = ITEM_REGISTER.register("screw_driver", () -> new ItemRepairTool());
    public static final RegistryObject<ItemRepairTool> HAMMER = ITEM_REGISTER.register("hammer", () -> new ItemRepairTool());
    public static final RegistryObject<ItemKey> KEY = ITEM_REGISTER.register("key", () -> new ItemKey());
    public static final RegistryObject<ItemBattery> BATTERY = ITEM_REGISTER.register("battery", () -> new ItemBattery());

    public static final RegistryObject<ItemLicensePlate> LICENSE_PLATE = ITEM_REGISTER.register("license_plate", () -> new ItemLicensePlate());

    public static final RegistryObject<ItemCarPart> ENGINE_3_CYLINDER = ITEM_REGISTER.register("engine_3_cylinder", () -> new ItemCarPart(PartRegistry.ENGINE_3_CYLINDER));
    public static final RegistryObject<ItemCarPart> ENGINE_6_CYLINDER = ITEM_REGISTER.register("engine_6_cylinder", () -> new ItemCarPart(PartRegistry.ENGINE_6_CYLINDER));
    public static final RegistryObject<ItemCarPart> ENGINE_TRUCK = ITEM_REGISTER.register("engine_truck", () -> new ItemCarPart(PartRegistry.ENGINE_TRUCK));

    public static final RegistryObject<ItemCarPart> WHEEL = ITEM_REGISTER.register("wheel", () -> new ItemCarPart(PartRegistry.WHEEL));
    public static final RegistryObject<ItemCarPart> BIG_WHEEL = ITEM_REGISTER.register("big_wheel", () -> new ItemCarPart(PartRegistry.BIG_WHEEL));

    public static final RegistryObject<ItemCarPart> OAK_BODY = ITEM_REGISTER.register("oak_body", () -> new ItemCarPart(PartRegistry.OAK_BODY));

    public static final RegistryObject<ItemCarPart>[] WOOD_BODIES = new RegistryObject[]{
            OAK_BODY
    };

    public static final RegistryObject<ItemCarPart> BIG_OAK_BODY = ITEM_REGISTER.register("big_oak_body", () -> new ItemCarPart(PartRegistry.BIG_OAK_BODY));

    public static final RegistryObject<ItemCarPart>[] BIG_WOOD_BODIES = new RegistryObject[]{
            BIG_OAK_BODY
    };

    public static final RegistryObject<ItemCarPart> WHITE_TRANSPORTER_BODY = ITEM_REGISTER.register("white_transporter_body", () -> new ItemCarPart(PartRegistry.WHITE_TRANSPORTER_BODY));

    public static final RegistryObject<ItemCarPart>[] TRANSPORTER_BODIES = new RegistryObject[]{
            WHITE_TRANSPORTER_BODY
    };

    public static final RegistryObject<ItemCarPart> WHITE_SUV_BODY = ITEM_REGISTER.register("white_suv_body", () -> new ItemCarPart(PartRegistry.WHITE_SUV_BODY));

    public static final RegistryObject<ItemCarPart>[] SUV_BODIES = new RegistryObject[]{
            WHITE_SUV_BODY
    };

    public static final RegistryObject<ItemCarPart> WHITE_SPORT_BODY = ITEM_REGISTER.register("white_sport_body", () -> new ItemCarPart(PartRegistry.WHITE_SPORT_BODY));

    public static final RegistryObject<ItemCarPart>[] SPORT_BODIES = new RegistryObject[]{
            WHITE_SPORT_BODY
    };

    public static final RegistryObject<ItemCarPart> WHITE_CONTAINER = ITEM_REGISTER.register("white_container", () -> new ItemCarPart(PartRegistry.WHITE_CONTAINER));

    public static final RegistryObject<ItemCarPart>[] CONTAINERS = new RegistryObject[]{
            WHITE_CONTAINER
    };

    public static final RegistryObject<ItemCarPart> WHITE_TANK_CONTAINER = ITEM_REGISTER.register("white_tank_container", () -> new ItemCarPart(PartRegistry.WHITE_TANK_CONTAINER));

    public static final RegistryObject<ItemCarPart>[] TANK_CONTAINERS = new RegistryObject[]{
            WHITE_TANK_CONTAINER
    };

    public static final RegistryObject<ItemCarPart> OAK_BUMPER = ITEM_REGISTER.register("oak_bumper", () -> new ItemCarPart(PartRegistry.OAK_BUMPER));

    public static final RegistryObject<ItemCarPart>[] BUMPERS = new RegistryObject[]{
            OAK_BUMPER
    };

    public static final RegistryObject<ItemCarPart>[] WOODEN_LICENSE_PLATE_HOLDERS = new RegistryObject[]{
    };

    public static final RegistryObject<ItemCarPart> IRON_LICENSE_PLATE_HOLDER = ITEM_REGISTER.register("iron_license_plate_holder", () -> new ItemCarPart(PartRegistry.IRON_LICENSE_PLATE_HOLDER));

    public static final RegistryObject<ItemCarPart> SMALL_TANK = ITEM_REGISTER.register("small_tank", () -> new ItemCarPart(PartRegistry.SMALL_TANK));
    public static final RegistryObject<ItemCarPart> MEDIUM_TANK = ITEM_REGISTER.register("medium_tank", () -> new ItemCarPart(PartRegistry.MEDIUM_TANK));
    public static final RegistryObject<ItemCarPart> LARGE_TANK = ITEM_REGISTER.register("large_tank", () -> new ItemCarPart(PartRegistry.LARGE_TANK));

    // Pre-built cars
    public static final RegistryObject<ItemSpawnCar> SPAWN_CAR_OAK = ITEM_REGISTER.register("spawn_car_oak", () -> new ItemSpawnCar(PartRegistry.OAK_BODY));
    public static final RegistryObject<ItemSpawnCar> SPAWN_CAR_BIG_OAK = ITEM_REGISTER.register("spawn_car_big_oak", () -> new ItemSpawnCar(PartRegistry.BIG_OAK_BODY));
    public static final RegistryObject<ItemSpawnCar> SPAWN_CAR_WHITE_TRANSPORTER = ITEM_REGISTER.register("spawn_car_white_transporter", () -> new ItemSpawnCar(PartRegistry.WHITE_TRANSPORTER_BODY));
    public static final RegistryObject<ItemSpawnCar> SPAWN_CAR_WHITE_SUV = ITEM_REGISTER.register("spawn_car_white_suv", () -> new ItemSpawnCar(PartRegistry.WHITE_SUV_BODY));
    public static final RegistryObject<ItemSpawnCar> SPAWN_CAR_WHITE_SPORT = ITEM_REGISTER.register("spawn_car_white_sport", () -> new ItemSpawnCar(PartRegistry.WHITE_SPORT_BODY));

    public static final RegistryObject<CarBucketItem> BIO_DIESEL_BUCKET = ITEM_REGISTER.register("bio_diesel_bucket", () -> new CarBucketItem(ModFluids.BIO_DIESEL.get()));

    public static final RegistryObject<Item> GAS_STATION = ITEM_REGISTER.register("gas_station", () -> ModBlocks.GAS_STATION.get().toItem());
    public static final RegistryObject<Item> CAR_WORKSHOP = ITEM_REGISTER.register("car_workshop", () -> ModBlocks.CAR_WORKSHOP.get().toItem());
    public static final RegistryObject<Item> CAR_WORKSHOP_OUTTER = ITEM_REGISTER.register("car_workshop_outter", () -> ModBlocks.CAR_WORKSHOP_OUTTER.get().toItem());
    public static final RegistryObject<Item> CAR_PRESSURE_PLATE = ITEM_REGISTER.register("car_pressure_plate", () -> ModBlocks.CAR_PRESSURE_PLATE.get().toItem());

    public static final RegistryObject<Item>[] PAINTS;
    public static final RegistryObject<Item>[] YELLOW_PAINTS;

    static {
        PAINTS = new RegistryObject[BlockPaint.EnumPaintType.values().length];
        for (int i = 0; i < PAINTS.length; i++) {
            int paintIndex = i;
            PAINTS[i] = ITEM_REGISTER.register(BlockPaint.EnumPaintType.values()[i].getPaintName(), () -> ModBlocks.PAINTS[paintIndex].get().toItem());
        }

        YELLOW_PAINTS = new RegistryObject[BlockPaint.EnumPaintType.values().length];
        for (int i = 0; i < YELLOW_PAINTS.length; i++) {
            int paintIndex = i;
            YELLOW_PAINTS[i] = ITEM_REGISTER.register(BlockPaint.EnumPaintType.values()[i].getPaintName() + "_yellow", () -> ModBlocks.PAINTS[paintIndex].get().toItem());
        }
    }

    public static void init() {
        ITEM_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
