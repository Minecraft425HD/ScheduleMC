package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.VehicleMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for all vehicle-related items.
 */
public class VehicleItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VehicleMod.MOD_ID);

    // Spawn items for vehicles (simple spawn items)
    public static final RegistryObject<Item> SEDAN_SPAWN_ITEM = ITEMS.register("vehicle_sedan",
            () -> new VehicleSpawnItem("sedan", new Item.Properties()));

    public static final RegistryObject<Item> SPORT_SPAWN_ITEM = ITEMS.register("vehicle_sport",
            () -> new VehicleSpawnItem("sport", new Item.Properties()));

    public static final RegistryObject<Item> SUV_SPAWN_ITEM = ITEMS.register("vehicle_suv",
            () -> new VehicleSpawnItem("suv", new Item.Properties()));

    public static final RegistryObject<Item> TRUCK_SPAWN_ITEM = ITEMS.register("vehicle_truck",
            () -> new VehicleSpawnItem("truck", new Item.Properties()));

    public static final RegistryObject<Item> TRANSPORTER_SPAWN_ITEM = ITEMS.register("vehicle_transporter",
            () -> new VehicleSpawnItem("transporter", new Item.Properties()));

    // Vehicle Spawn Tools (for dealer shops)
    public static final RegistryObject<Item> VEHICLE_SPAWN_SEDAN = ITEMS.register("vehicle_spawn_tool_sedan",
            () -> new VehicleSpawnTool(new Item.Properties().stacksTo(1), VehicleSpawnTool.VehicleType.SEDAN));

    public static final RegistryObject<Item> VEHICLE_SPAWN_SPORT = ITEMS.register("vehicle_spawn_tool_sport",
            () -> new VehicleSpawnTool(new Item.Properties().stacksTo(1), VehicleSpawnTool.VehicleType.SPORT));

    public static final RegistryObject<Item> VEHICLE_SPAWN_SUV = ITEMS.register("vehicle_spawn_tool_suv",
            () -> new VehicleSpawnTool(new Item.Properties().stacksTo(1), VehicleSpawnTool.VehicleType.SUV));

    public static final RegistryObject<Item> VEHICLE_SPAWN_TRUCK = ITEMS.register("vehicle_spawn_tool_truck",
            () -> new VehicleSpawnTool(new Item.Properties().stacksTo(1), VehicleSpawnTool.VehicleType.TRUCK));

    public static final RegistryObject<Item> VEHICLE_SPAWN_TRANSPORTER = ITEMS.register("vehicle_spawn_tool_transporter",
            () -> new VehicleSpawnTool(new Item.Properties().stacksTo(1), VehicleSpawnTool.VehicleType.TRANSPORTER));

    // Component items - Engines
    public static final RegistryObject<Item> ENGINE_INLINE_THREE = ITEMS.register("engine_inline_three",
            () -> new ComponentItem(ComponentItem.ComponentItemType.ENGINE, "inline_three",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ENGINE_INLINE_SIX = ITEMS.register("engine_inline_six",
            () -> new ComponentItem(ComponentItem.ComponentItemType.ENGINE, "inline_six",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ENGINE_TRUCK_V8 = ITEMS.register("engine_truck_v8",
            () -> new ComponentItem(ComponentItem.ComponentItemType.ENGINE, "truck_v8",
                    new Item.Properties().stacksTo(1)));

    // Component items - Wheels
    public static final RegistryObject<Item> WHEELS_STANDARD = ITEMS.register("wheels_standard",
            () -> new ComponentItem(ComponentItem.ComponentItemType.WHEELS, "standard",
                    new Item.Properties().stacksTo(4)));

    public static final RegistryObject<Item> WHEELS_SPORT = ITEMS.register("wheels_sport",
            () -> new ComponentItem(ComponentItem.ComponentItemType.WHEELS, "sport",
                    new Item.Properties().stacksTo(4)));

    public static final RegistryObject<Item> WHEELS_OFFROAD = ITEMS.register("wheels_offroad",
            () -> new ComponentItem(ComponentItem.ComponentItemType.WHEELS, "offroad",
                    new Item.Properties().stacksTo(4)));

    public static final RegistryObject<Item> WHEELS_TRUCK = ITEMS.register("wheels_truck",
            () -> new ComponentItem(ComponentItem.ComponentItemType.WHEELS, "truck",
                    new Item.Properties().stacksTo(4)));

    // Component items - Body
    public static final RegistryObject<Item> BODY_SEDAN = ITEMS.register("body_sedan",
            () -> new ComponentItem(ComponentItem.ComponentItemType.BODY, "sedan",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BODY_SPORT = ITEMS.register("body_sport",
            () -> new ComponentItem(ComponentItem.ComponentItemType.BODY, "sport",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BODY_SUV = ITEMS.register("body_suv",
            () -> new ComponentItem(ComponentItem.ComponentItemType.BODY, "suv",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BODY_TRUCK = ITEMS.register("body_truck",
            () -> new ComponentItem(ComponentItem.ComponentItemType.BODY, "truck",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BODY_TRANSPORTER = ITEMS.register("body_transporter",
            () -> new ComponentItem(ComponentItem.ComponentItemType.BODY, "transporter",
                    new Item.Properties().stacksTo(1)));

    // Component items - Fuel tanks
    public static final RegistryObject<Item> TANK_SMALL = ITEMS.register("fuel_tank_small",
            () -> new ComponentItem(ComponentItem.ComponentItemType.FUEL_TANK, "small",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TANK_MEDIUM = ITEMS.register("fuel_tank_medium",
            () -> new ComponentItem(ComponentItem.ComponentItemType.FUEL_TANK, "medium",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TANK_LARGE = ITEMS.register("fuel_tank_large",
            () -> new ComponentItem(ComponentItem.ComponentItemType.FUEL_TANK, "large",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TANK_TRUCK = ITEMS.register("fuel_tank_truck",
            () -> new ComponentItem(ComponentItem.ComponentItemType.FUEL_TANK, "truck",
                    new Item.Properties().stacksTo(1)));

    // Utility items
    public static final RegistryObject<Item> VEHICLE_KEY = ITEMS.register("vehicle_key",
            () -> new VehicleKeyItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LICENSE_PLATE = ITEMS.register("license_plate",
            () -> new LicensePlateItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> REPAIR_KIT = ITEMS.register("repair_kit",
            () -> new RepairKitItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> BATTERY = ITEMS.register("vehicle_battery",
            () -> new BatteryItem(new Item.Properties().stacksTo(1)));

    // Wrench for vehicle modification
    public static final RegistryObject<Item> WRENCH = ITEMS.register("vehicle_wrench",
            () -> new WrenchItem(new Item.Properties().stacksTo(1).durability(250)));
}
