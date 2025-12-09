package de.rolandsw.schedulemc.vehicle;

import de.rolandsw.schedulemc.vehicle.component.attribute.BatteryComponent;
import de.rolandsw.schedulemc.vehicle.component.attribute.DurabilityComponent;
import de.rolandsw.schedulemc.vehicle.component.attribute.TemperatureComponent;
import de.rolandsw.schedulemc.vehicle.component.body.BodyComponent;
import de.rolandsw.schedulemc.vehicle.component.control.ControlComponent;
import de.rolandsw.schedulemc.vehicle.component.control.LicensePlateComponent;
import de.rolandsw.schedulemc.vehicle.component.control.OwnershipComponent;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineComponent;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankComponent;
import de.rolandsw.schedulemc.vehicle.component.mobility.WheelComponent;
import de.rolandsw.schedulemc.vehicle.component.storage.InventoryComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.core.registry.ComponentRegistry;
import de.rolandsw.schedulemc.vehicle.core.registry.SystemRegistry;
import de.rolandsw.schedulemc.vehicle.core.system.SystemManager;
import de.rolandsw.schedulemc.vehicle.items.VehicleItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import de.rolandsw.schedulemc.vehicle.network.VehicleNetwork;
import de.rolandsw.schedulemc.vehicle.system.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main initialization class for the new Vehicle system.
 * Handles registration and setup of the ECS architecture.
 */
public class VehicleMod {

    public static final String MOD_ID = "schedulemc";
    public static final Logger LOGGER = LogManager.getLogger("VehicleMod");

    // Entity registration
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<EntityType<VehicleEntity>> VEHICLE_ENTITY = ENTITIES.register(
            "vehicle",
            () -> EntityType.Builder.of(VehicleEntity::new, MobCategory.MISC)
                    .sized(2.0f, 1.5f)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("vehicle")
    );

    // Public accessors for vehicle spawn tools (for merchant shops)
    public static final RegistryObject<Item> VEHICLE_SPAWN_SEDAN = VehicleItems.VEHICLE_SPAWN_SEDAN;
    public static final RegistryObject<Item> VEHICLE_SPAWN_SPORT = VehicleItems.VEHICLE_SPAWN_SPORT;
    public static final RegistryObject<Item> VEHICLE_SPAWN_SUV = VehicleItems.VEHICLE_SPAWN_SUV;
    public static final RegistryObject<Item> VEHICLE_SPAWN_TRUCK = VehicleItems.VEHICLE_SPAWN_TRUCK;
    public static final RegistryObject<Item> VEHICLE_SPAWN_TRANSPORTER = VehicleItems.VEHICLE_SPAWN_TRANSPORTER;

    /**
     * Initializes the vehicle mod.
     */
    public static void init() {
        LOGGER.info("Initializing Vehicle Mod (ECS Architecture)...");

        // Register entities
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Register items
        VehicleItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Register components
        registerComponents();

        // Register systems
        registerSystems();

        // Register network
        VehicleNetwork.register();

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new VehicleMod());

        LOGGER.info("Vehicle Mod initialized successfully!");
    }

    /**
     * Registers all component types.
     */
    private static void registerComponents() {
        LOGGER.info("Registering vehicle components...");

        ComponentRegistry.register(ComponentType.ENGINE, EngineComponent::new);
        ComponentRegistry.register(ComponentType.FUEL_TANK, FuelTankComponent::new);
        ComponentRegistry.register(ComponentType.WHEELS, WheelComponent::new);
        ComponentRegistry.register(ComponentType.BODY, BodyComponent::new);
        ComponentRegistry.register(ComponentType.DURABILITY, DurabilityComponent::new);
        ComponentRegistry.register(ComponentType.BATTERY, BatteryComponent::new);
        ComponentRegistry.register(ComponentType.TEMPERATURE, TemperatureComponent::new);
        ComponentRegistry.register(ComponentType.CONTROLS, ControlComponent::new);
        ComponentRegistry.register(ComponentType.INVENTORY, InventoryComponent::new);
        ComponentRegistry.register(ComponentType.SECURITY, OwnershipComponent::new);
        ComponentRegistry.register(ComponentType.LICENSE_PLATE, LicensePlateComponent::new);

        LOGGER.info("Registered {} component types", ComponentRegistry.getRegisteredTypes().size());
    }

    /**
     * Registers all vehicle systems.
     */
    private static void registerSystems() {
        LOGGER.info("Registering vehicle systems...");

        SystemRegistry.register(new MovementSystem());
        SystemRegistry.register(new FuelSystem());
        SystemRegistry.register(new BatterySystem());
        SystemRegistry.register(new TemperatureSystem());
        SystemRegistry.register(new DamageSystem());

        LOGGER.info("Registered {} systems", SystemRegistry.getSortedSystems().size());
    }

    /**
     * Called when server starts.
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Initializing vehicle systems...");
        SystemRegistry.initializeAll();
    }

    /**
     * Called when server stops.
     */
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Shutting down vehicle systems...");
        SystemRegistry.shutdownAll();
    }

    /**
     * Ticks all vehicle systems.
     */
    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SystemManager.tickWorld(event.level);
        }
    }
}
