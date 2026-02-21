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

    // Player-facing items (remain in item system)
    public static final RegistryObject<ItemCanister> CANISTER = ITEM_REGISTER.register("empty_diesel_can", () -> new ItemCanister());
    public static final RegistryObject<ItemDieselCanister> DIESEL_CANISTER = ITEM_REGISTER.register("full_diesel_can", () -> new ItemDieselCanister());
    public static final RegistryObject<ItemRepairKit> REPAIR_KIT = ITEM_REGISTER.register("maintenance_kit", () -> new ItemRepairKit());
    public static final RegistryObject<ItemKey> KEY = ITEM_REGISTER.register("key", () -> new ItemKey());
    public static final RegistryObject<ItemBattery> BATTERY = ITEM_REGISTER.register("starter_battery", () -> new ItemBattery());

    public static final RegistryObject<VehicleSpawnTool> VEHICLE_SPAWN_TOOL = ITEM_REGISTER.register("spawn_tool", () -> new VehicleSpawnTool());

    // Tire items (remain as physical items, shown in creative tab)
    public static final RegistryObject<ItemVehiclePart> STANDARD_TIRE = ITEM_REGISTER.register("standard_tire", () -> new ItemVehiclePart(PartRegistry.STANDARD_TIRE));
    public static final RegistryObject<ItemVehiclePart> SPORT_TIRE = ITEM_REGISTER.register("sport_tire", () -> new ItemVehiclePart(PartRegistry.SPORT_TIRE));
    public static final RegistryObject<ItemVehiclePart> PREMIUM_TIRE = ITEM_REGISTER.register("premium_tire", () -> new ItemVehiclePart(PartRegistry.PREMIUM_TIRE));
    public static final RegistryObject<ItemVehiclePart> OFFROAD_TIRE = ITEM_REGISTER.register("offroad_tire", () -> new ItemVehiclePart(PartRegistry.OFFROAD_TIRE));
    public static final RegistryObject<ItemVehiclePart> ALLTERRAIN_TIRE = ITEM_REGISTER.register("allterrain_tire", () -> new ItemVehiclePart(PartRegistry.ALLTERRAIN_TIRE));
    public static final RegistryObject<ItemVehiclePart> HEAVY_DUTY_TIRE = ITEM_REGISTER.register("heavyduty_tire", () -> new ItemVehiclePart(PartRegistry.HEAVY_DUTY_TIRE));
    public static final RegistryObject<ItemVehiclePart> WINTER_TIRE = ITEM_REGISTER.register("winter_tire", () -> new ItemVehiclePart(PartRegistry.WINTER_TIRE));

    // Internal vehicle part item (replaces all non-tire part items)
    // Not shown in creative tab, no player-facing texture required
    public static final RegistryObject<InternalVehiclePartItem> INTERNAL_VEHICLE_PART = ITEM_REGISTER.register("internal_vehicle_part", () -> new InternalVehiclePartItem());

    // Pre-built vehicles
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_OAK = ITEM_REGISTER.register("limousine", () -> new ItemSpawnVehicle(PartRegistry.LIMOUSINE_CHASSIS));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_BIG_OAK = ITEM_REGISTER.register("van", () -> new ItemSpawnVehicle(PartRegistry.VAN_CHASSIS));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_TRANSPORTER = ITEM_REGISTER.register("truck", () -> new ItemSpawnVehicle(PartRegistry.TRUCK_CHASSIS));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_SUV = ITEM_REGISTER.register("suv", () -> new ItemSpawnVehicle(PartRegistry.OFFROAD_CHASSIS));
    public static final RegistryObject<ItemSpawnVehicle> SPAWN_VEHICLE_WHITE_SPORT = ITEM_REGISTER.register("sports_car", () -> new ItemSpawnVehicle(PartRegistry.LUXUS_CHASSIS));

    public static final RegistryObject<ItemCarJack> WAGON_JACK = ITEM_REGISTER.register("wagon_jack", ItemCarJack::new);

    public static final RegistryObject<Item> FUEL_STATION = ITEM_REGISTER.register("fuel_station", () -> ModBlocks.FUEL_STATION.get().toItem());
    public static final RegistryObject<Item> WERKSTATT = ITEM_REGISTER.register("werkstatt", () -> ModBlocks.WERKSTATT.get().toItem());

    public static void init(IEventBus modEventBus) {
        ITEM_REGISTER.register(modEventBus);
    }

}
