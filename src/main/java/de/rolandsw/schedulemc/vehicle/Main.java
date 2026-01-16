package de.rolandsw.schedulemc.vehicle;

import com.google.common.collect.ImmutableSet;
import de.rolandsw.schedulemc.util.EventHelper;
import de.rolandsw.schedulemc.vehicle.blocks.ModBlocks;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.*;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.render.TileentitySpecialRendererFuelStation;
import de.rolandsw.schedulemc.config.ClientConfig;
import de.rolandsw.schedulemc.config.FuelConfig;
import de.rolandsw.schedulemc.config.ServerConfig;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.model.GenericVehicleModel;
import de.rolandsw.schedulemc.vehicle.events.*;
import de.rolandsw.schedulemc.vehicle.fluids.ModFluids;
import de.rolandsw.schedulemc.vehicle.gui.*;
import de.rolandsw.schedulemc.vehicle.items.ItemLicensePlate;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import de.rolandsw.schedulemc.vehicle.net.*;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.corelib.CommonRegistry;
import de.maxhenkel.corelib.config.DynamicConfig;
import de.maxhenkel.corelib.dataserializers.DataSerializerItemList;
import de.rolandsw.schedulemc.vehicle.util.VehicleUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class Main {

    public static final String MODID = "vehicle";

    public static final Logger LOGGER = LogManager.getLogger(Main.MODID);

    public static SimpleChannel SIMPLE_CHANNEL;

    private static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Main.MODID);
    public static final RegistryObject<EntityType<EntityGenericVehicle>> VEHICLE_ENTITY_TYPE = ENTITY_REGISTER.register("vehicle", Main::createVehicleEntityType);

    private static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Main.MODID);
    public static final RegistryObject<EntityDataSerializer<NonNullList<ItemStack>>> ITEM_LIST = ENTITY_DATA_SERIALIZER_REGISTER.register("serializer_item_list", () -> DataSerializerItemList.create());

    public static FuelConfig FUEL_CONFIG;

    public Main(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);

        FUEL_CONFIG = CommonRegistry.registerDynamicConfig(DynamicConfig.DynamicConfigType.SERVER, Main.MODID, "fuel", FuelConfig.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(Main.this::clientSetup);
            modEventBus.addListener(Main.this::onRegisterKeyBinds);
        });

        ModFluids.init(modEventBus);
        ModBlocks.init(modEventBus);
        ModItems.init(modEventBus);
        ModSounds.init(modEventBus);
        ModCreativeTabs.init(modEventBus);

        MENU_TYPE_REGISTER.register(modEventBus);
        BLOCK_ENTITY_REGISTER.register(modEventBus);
        ENTITY_REGISTER.register(modEventBus);
        RECIPE_SERIALIZER_REGISTER.register(modEventBus);
        ENTITY_DATA_SERIALIZER_REGISTER.register(modEventBus);
    }

    // Legacy constructor for backward compatibility
    public Main() {
        this(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        EventHelper.handleEvent(() -> {
            // DISABLED: CommandVehicleDemo requires JEI integration which was removed
            // CommandVehicleDemo.register(event.getDispatcher());
        }, "onRegisterCommands");
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        EventHelper.handleEvent(() -> {
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(new BlockEvents());

            SIMPLE_CHANNEL = CommonRegistry.registerChannel(Main.MODID, "default");
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 0, MessageControlVehicle.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 1, MessageVehicleGui.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 2, MessageStarting.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 3, MessageCrash.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 4, MessageStartFuel.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 6, MessageSyncTileEntity.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 10, MessageVehicleHorn.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 13, MessageCenterVehicle.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 14, MessageCenterVehicleClient.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 16, MessageGaragePayment.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 17, MessageGarageUpgrade.class);
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, 18, MessageContainerOperation.class);
        }, "commonSetup");
    }

    public static KeyMapping FORWARD_KEY;
    public static KeyMapping BACK_KEY;
    public static KeyMapping LEFT_KEY;
    public static KeyMapping RIGHT_KEY;

    public static KeyMapping VEHICLE_GUI_KEY;
    public static KeyMapping START_KEY;
    public static KeyMapping HORN_KEY;
    public static KeyMapping CENTER_KEY;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        EventHelper.handleEvent(() -> {
            BlockEntityRenderers.register(FUEL_STATION_TILE_ENTITY_TYPE.get(), TileentitySpecialRendererFuelStation::new);

            ClientRegistry.<ContainerVehicle, GuiVehicle>registerScreen(Main.VEHICLE_CONTAINER_TYPE.get(), GuiVehicle::new);

            // Register vehicle-specific inventory screens
            ClientRegistry.<ContainerLimousineInventory, GuiLimousineInventory>registerScreen(Main.LIMOUSINE_INVENTORY_CONTAINER_TYPE.get(), GuiLimousineInventory::new);
            ClientRegistry.<ContainerVanInventory, GuiVanInventory>registerScreen(Main.VAN_INVENTORY_CONTAINER_TYPE.get(), GuiVanInventory::new);
            ClientRegistry.<ContainerLuxusInventory, GuiLuxusInventory>registerScreen(Main.LUXUS_INVENTORY_CONTAINER_TYPE.get(), GuiLuxusInventory::new);
            ClientRegistry.<ContainerOffroadInventory, GuiOffroadInventory>registerScreen(Main.OFFROAD_INVENTORY_CONTAINER_TYPE.get(), GuiOffroadInventory::new);
            ClientRegistry.<ContainerTruckEmpty, GuiTruckEmpty>registerScreen(Main.TRUCK_EMPTY_CONTAINER_TYPE.get(), GuiTruckEmpty::new);
            ClientRegistry.<ContainerTruckWithContainer, GuiTruckWithContainer>registerScreen(Main.TRUCK_CONTAINER_TYPE.get(), GuiTruckWithContainer::new);

            // Legacy vehicle inventory screen (fallback)
            ClientRegistry.<ContainerVehicleInventory, GuiVehicleInventory>registerScreen(Main.VEHICLE_INVENTORY_CONTAINER_TYPE.get(), GuiVehicleInventory::new);

            ClientRegistry.<ContainerFuelStation, GuiFuelStation>registerScreen(Main.FUEL_STATION_CONTAINER_TYPE.get(), GuiFuelStation::new);
            ClientRegistry.<ContainerGarage, GuiGarage>registerScreen(Main.GARAGE_CONTAINER_TYPE.get(), GuiGarage::new);

            MinecraftForge.EVENT_BUS.register(new RenderEvents());
            MinecraftForge.EVENT_BUS.register(new SoundEvents());
            MinecraftForge.EVENT_BUS.register(new KeyEvents());
            MinecraftForge.EVENT_BUS.register(new PlayerEvents());

            EntityRenderers.register(VEHICLE_ENTITY_TYPE.get(), GenericVehicleModel::new);

            ItemBlockRenderTypes.setRenderLayer(ModFluids.BIO_DIESEL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.BIO_DIESEL_FLOWING.get(), RenderType.translucent());
        }, "clientSetup");
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        EventHelper.handleEvent(() -> {
            FORWARD_KEY = new KeyMapping("key.vehicle_forward", GLFW.GLFW_KEY_W, "category.vehicle");
            BACK_KEY = new KeyMapping("key.vehicle_back", GLFW.GLFW_KEY_S, "category.vehicle");
            LEFT_KEY = new KeyMapping("key.vehicle_left", GLFW.GLFW_KEY_A, "category.vehicle");
            RIGHT_KEY = new KeyMapping("key.vehicle_right", GLFW.GLFW_KEY_D, "category.vehicle");
            VEHICLE_GUI_KEY = new KeyMapping("key.vehicle_gui", GLFW.GLFW_KEY_I, "category.vehicle");
            START_KEY = new KeyMapping("key.vehicle_start", GLFW.GLFW_KEY_R, "category.vehicle");
            HORN_KEY = new KeyMapping("key.vehicle_horn", GLFW.GLFW_KEY_H, "category.vehicle");
            CENTER_KEY = new KeyMapping("key.center_vehicle", GLFW.GLFW_KEY_SPACE, "category.vehicle");

            event.register(FORWARD_KEY);
            event.register(BACK_KEY);
            event.register(LEFT_KEY);
            event.register(RIGHT_KEY);
            event.register(VEHICLE_GUI_KEY);
            event.register(START_KEY);
            event.register(HORN_KEY);
            event.register(CENTER_KEY);
        }, "onRegisterKeyBinds");
    }

    private static EntityType<EntityGenericVehicle> createVehicleEntityType() {
        return CommonRegistry.registerEntity(Main.MODID, "vehicle", MobCategory.MISC, EntityGenericVehicle.class, builder -> {
            builder.setTrackingRange(128)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(1F, 1F)
                    .setCustomClientFactory((spawnEntity, world) -> new EntityGenericVehicle(world));
        });
    }

    private static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Main.MODID);

    public static final RegistryObject<MenuType<ContainerVehicle>> VEHICLE_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("vehicle", () ->
            IForgeMenuType.create((IContainerFactory<ContainerVehicle>) (windowId, inv, data) -> {
                EntityGenericVehicle vehicle = VehicleUtils.getVehicleByUUID(inv.player, data.readUUID());
                if (vehicle == null) {
                    return null;
                }
                return new ContainerVehicle(windowId, vehicle, inv);
            })
    );
    // Separate MenuTypes for each vehicle chassis type
    public static final RegistryObject<MenuType<ContainerLimousineInventory>> LIMOUSINE_INVENTORY_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("limousine_inventory", () ->
            IForgeMenuType.create((IContainerFactory<ContainerLimousineInventory>) (windowId, inv, data) -> {
                EntityGenericVehicle vehicle = VehicleUtils.getVehicleByUUID(inv.player, data.readUUID());
                return vehicle != null ? new ContainerLimousineInventory(windowId, vehicle, inv) : null;
            })
    );

    public static final RegistryObject<MenuType<ContainerVanInventory>> VAN_INVENTORY_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("van_inventory", () ->
            IForgeMenuType.create((IContainerFactory<ContainerVanInventory>) (windowId, inv, data) -> {
                EntityGenericVehicle vehicle = VehicleUtils.getVehicleByUUID(inv.player, data.readUUID());
                return vehicle != null ? new ContainerVanInventory(windowId, vehicle, inv) : null;
            })
    );

    public static final RegistryObject<MenuType<ContainerLuxusInventory>> LUXUS_INVENTORY_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("luxus_inventory", () ->
            IForgeMenuType.create((IContainerFactory<ContainerLuxusInventory>) (windowId, inv, data) -> {
                EntityGenericVehicle vehicle = VehicleUtils.getVehicleByUUID(inv.player, data.readUUID());
                return vehicle != null ? new ContainerLuxusInventory(windowId, vehicle, inv) : null;
            })
    );

    public static final RegistryObject<MenuType<ContainerOffroadInventory>> OFFROAD_INVENTORY_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("offroad_inventory", () ->
            IForgeMenuType.create((IContainerFactory<ContainerOffroadInventory>) (windowId, inv, data) -> {
                EntityGenericVehicle vehicle = VehicleUtils.getVehicleByUUID(inv.player, data.readUUID());
                return vehicle != null ? new ContainerOffroadInventory(windowId, vehicle, inv) : null;
            })
    );

    public static final RegistryObject<MenuType<ContainerTruckEmpty>> TRUCK_EMPTY_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("truck_empty", () ->
            IForgeMenuType.create((IContainerFactory<ContainerTruckEmpty>) (windowId, inv, data) -> {
                EntityGenericVehicle vehicle = VehicleUtils.getVehicleByUUID(inv.player, data.readUUID());
                return vehicle != null ? new ContainerTruckEmpty(windowId, vehicle, inv) : null;
            })
    );

    public static final RegistryObject<MenuType<ContainerTruckWithContainer>> TRUCK_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("truck_container", () ->
            IForgeMenuType.create((IContainerFactory<ContainerTruckWithContainer>) (windowId, inv, data) -> {
                EntityGenericVehicle vehicle = VehicleUtils.getVehicleByUUID(inv.player, data.readUUID());
                return vehicle != null ? new ContainerTruckWithContainer(windowId, vehicle, inv) : null;
            })
    );

    // Legacy vehicle inventory container type (kept for backwards compatibility)
    public static final RegistryObject<MenuType<ContainerVehicleInventory>> VEHICLE_INVENTORY_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("vehicle_inventory", () ->
            IForgeMenuType.create((IContainerFactory<ContainerVehicleInventory>) (windowId, inv, data) -> {
                EntityGenericVehicle vehicle = VehicleUtils.getVehicleByUUID(inv.player, data.readUUID());
                if (vehicle == null) {
                    return null;
                }
                return new ContainerVehicleInventory(windowId, vehicle, inv);
            })
    );

    public static final RegistryObject<MenuType<ContainerFuelStation>> FUEL_STATION_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("fuel_station", () ->
            IForgeMenuType.create(new ContainerFactoryTileEntity((ContainerFactoryTileEntity.ContainerCreator<ContainerFuelStation, TileEntityFuelStation>) ContainerFuelStation::new))
    );
    public static final RegistryObject<MenuType<ContainerGarage>> GARAGE_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("garage", () ->
            IForgeMenuType.create((windowId, inv, data) -> new ContainerGarage(windowId, inv, data))
    );

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MODID);

    public static final RegistryObject<BlockEntityType<TileEntityFuelStation>> FUEL_STATION_TILE_ENTITY_TYPE = BLOCK_ENTITY_REGISTER.register("fuel_station", () ->
            BlockEntityType.Builder.of(TileEntityFuelStation::new, ModBlocks.FUEL_STATION.get()).build(null)
    );

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Main.MODID);

}
