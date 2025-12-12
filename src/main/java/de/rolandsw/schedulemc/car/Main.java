package de.rolandsw.schedulemc.car;

import com.google.common.collect.ImmutableSet;
import de.rolandsw.schedulemc.car.blocks.ModBlocks;
import de.rolandsw.schedulemc.car.blocks.tileentity.*;
import de.rolandsw.schedulemc.car.blocks.tileentity.render.TileentitySpecialRendererGasStation;
import de.rolandsw.schedulemc.config.ClientConfig;
import de.rolandsw.schedulemc.config.FuelConfig;
import de.rolandsw.schedulemc.config.ServerConfig;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.entity.model.GenericCarModel;
import de.rolandsw.schedulemc.car.events.*;
import de.rolandsw.schedulemc.car.fluids.ModFluids;
import de.rolandsw.schedulemc.car.gui.*;
import de.rolandsw.schedulemc.car.items.ItemLicensePlate;
import de.rolandsw.schedulemc.car.items.ModItems;
import de.rolandsw.schedulemc.car.net.*;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.corelib.CommonRegistry;
import de.maxhenkel.corelib.config.DynamicConfig;
import de.maxhenkel.corelib.dataserializers.DataSerializerItemList;
import de.maxhenkel.tools.EntityTools;
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

    public static final String MODID = "car";

    public static final Logger LOGGER = LogManager.getLogger(Main.MODID);

    public static SimpleChannel SIMPLE_CHANNEL;

    private static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Main.MODID);
    public static final RegistryObject<EntityType<EntityGenericCar>> CAR_ENTITY_TYPE = ENTITY_REGISTER.register("car", Main::createCarEntityType);

    private static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Main.MODID);
    public static final RegistryObject<EntityDataSerializer<NonNullList<ItemStack>>> ITEM_LIST = ENTITY_DATA_SERIALIZER_REGISTER.register("serializer_item_list", () -> DataSerializerItemList.create());

    public static ServerConfig SERVER_CONFIG;
    public static FuelConfig FUEL_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.COMMON, ServerConfig.class, true);
        FUEL_CONFIG = CommonRegistry.registerDynamicConfig(DynamicConfig.DynamicConfigType.SERVER, Main.MODID, "fuel", FuelConfig.class);
        CLIENT_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.COMMON, ClientConfig.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::onRegisterKeyBinds);
        });

        ModFluids.init();
        ModBlocks.init();
        ModItems.init();
        ModSounds.init();
        ModCreativeTabs.init();
        de.rolandsw.schedulemc.car.recipes.ModRecipes.init();

        MENU_TYPE_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITY_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITY_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_SERIALIZER_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITY_DATA_SERIALIZER_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        // DISABLED: CommandCarDemo requires JEI integration which was removed
        // CommandCarDemo.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BlockEvents());

        SIMPLE_CHANNEL = CommonRegistry.registerChannel(Main.MODID, "default");
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 0, MessageControlCar.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 1, MessageCarGui.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 2, MessageStarting.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 3, MessageCrash.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 4, MessageStartFuel.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 6, MessageSyncTileEntity.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 7, MessageSpawnCar.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 9, MessageRepairCar.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 10, MessageCarHorn.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 12, MessageGasStationAdminAmount.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 13, MessageCenterCar.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 14, MessageCenterCarClient.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 15, MessageEditLicensePlate.class);
    }

    public static KeyMapping FORWARD_KEY;
    public static KeyMapping BACK_KEY;
    public static KeyMapping LEFT_KEY;
    public static KeyMapping RIGHT_KEY;

    public static KeyMapping CAR_GUI_KEY;
    public static KeyMapping START_KEY;
    public static KeyMapping HORN_KEY;
    public static KeyMapping CENTER_KEY;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(GAS_STATION_TILE_ENTITY_TYPE.get(), TileentitySpecialRendererGasStation::new);

        ClientRegistry.<ContainerCar, GuiCar>registerScreen(Main.CAR_CONTAINER_TYPE.get(), GuiCar::new);
        ClientRegistry.<ContainerCarInventory, GuiCarInventory>registerScreen(Main.CAR_INVENTORY_CONTAINER_TYPE.get(), GuiCarInventory::new);
        ClientRegistry.<ContainerGasStation, GuiGasStation>registerScreen(Main.GAS_STATION_CONTAINER_TYPE.get(), GuiGasStation::new);
        ClientRegistry.<ContainerGasStationAdmin, GuiGasStationAdmin>registerScreen(Main.GAS_STATION_ADMIN_CONTAINER_TYPE.get(), GuiGasStationAdmin::new);
        ClientRegistry.<ContainerLicensePlate, GuiLicensePlate>registerScreen(Main.LICENSE_PLATE_CONTAINER_TYPE.get(), GuiLicensePlate::new);

        MinecraftForge.EVENT_BUS.register(new RenderEvents());
        MinecraftForge.EVENT_BUS.register(new SoundEvents());
        MinecraftForge.EVENT_BUS.register(new KeyEvents());
        MinecraftForge.EVENT_BUS.register(new PlayerEvents());

        EntityRenderers.register(CAR_ENTITY_TYPE.get(), GenericCarModel::new);

        ItemBlockRenderTypes.setRenderLayer(ModFluids.BIO_DIESEL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.BIO_DIESEL_FLOWING.get(), RenderType.translucent());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        FORWARD_KEY = new KeyMapping("key.car_forward", GLFW.GLFW_KEY_W, "category.car");
        BACK_KEY = new KeyMapping("key.car_back", GLFW.GLFW_KEY_S, "category.car");
        LEFT_KEY = new KeyMapping("key.car_left", GLFW.GLFW_KEY_A, "category.car");
        RIGHT_KEY = new KeyMapping("key.car_right", GLFW.GLFW_KEY_D, "category.car");
        CAR_GUI_KEY = new KeyMapping("key.car_gui", GLFW.GLFW_KEY_I, "category.car");
        START_KEY = new KeyMapping("key.car_start", GLFW.GLFW_KEY_R, "category.car");
        HORN_KEY = new KeyMapping("key.car_horn", GLFW.GLFW_KEY_H, "category.car");
        CENTER_KEY = new KeyMapping("key.center_car", GLFW.GLFW_KEY_SPACE, "category.car");

        event.register(FORWARD_KEY);
        event.register(BACK_KEY);
        event.register(LEFT_KEY);
        event.register(RIGHT_KEY);
        event.register(CAR_GUI_KEY);
        event.register(START_KEY);
        event.register(HORN_KEY);
        event.register(CENTER_KEY);
    }

    private static EntityType<EntityGenericCar> createCarEntityType() {
        return CommonRegistry.registerEntity(Main.MODID, "car", MobCategory.MISC, EntityGenericCar.class, builder -> {
            builder.setTrackingRange(128)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(1F, 1F)
                    .setCustomClientFactory((spawnEntity, world) -> new EntityGenericCar(world));
        });
    }

    private static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Main.MODID);

    public static final RegistryObject<MenuType<ContainerCar>> CAR_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("car", () ->
            IForgeMenuType.create((IContainerFactory<ContainerCar>) (windowId, inv, data) -> {
                EntityGenericCar car = EntityTools.getCarByUUID(inv.player, data.readUUID());
                if (car == null) {
                    return null;
                }
                return new ContainerCar(windowId, car, inv);
            })
    );
    public static final RegistryObject<MenuType<ContainerCarInventory>> CAR_INVENTORY_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("car_inventory", () ->
            IForgeMenuType.create((IContainerFactory<ContainerCarInventory>) (windowId, inv, data) -> {
                EntityGenericCar car = EntityTools.getCarByUUID(inv.player, data.readUUID());
                if (car == null) {
                    return null;
                }
                return new ContainerCarInventory(windowId, car, inv);
            })
    );

    public static final RegistryObject<MenuType<ContainerGasStation>> GAS_STATION_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("gas_station", () ->
            IForgeMenuType.create(new ContainerFactoryTileEntity((ContainerFactoryTileEntity.ContainerCreator<ContainerGasStation, TileEntityGasStation>) ContainerGasStation::new))
    );
    public static final RegistryObject<MenuType<ContainerGasStationAdmin>> GAS_STATION_ADMIN_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("gas_station_admin", () ->
            IForgeMenuType.create(new ContainerFactoryTileEntity((ContainerFactoryTileEntity.ContainerCreator<ContainerGasStationAdmin, TileEntityGasStation>) ContainerGasStationAdmin::new))
    );
    public static final RegistryObject<MenuType<ContainerLicensePlate>> LICENSE_PLATE_CONTAINER_TYPE = MENU_TYPE_REGISTER.register("license_plate", () ->
            IForgeMenuType.create((windowId, inv, data) -> {
                ItemStack licensePlate = null;
                for (InteractionHand hand : InteractionHand.values()) {
                    ItemStack stack = inv.player.getItemInHand(hand);
                    if (stack.getItem() instanceof ItemLicensePlate) {
                        licensePlate = stack;
                        break;
                    }
                }
                if (licensePlate != null) {
                    return new ContainerLicensePlate(windowId, licensePlate);
                }
                return null;
            })
    );

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MODID);

    public static final RegistryObject<BlockEntityType<TileEntityGasStation>> GAS_STATION_TILE_ENTITY_TYPE = BLOCK_ENTITY_REGISTER.register("gas_station", () ->
            BlockEntityType.Builder.of(TileEntityGasStation::new, ModBlocks.GAS_STATION.get()).build(null)
    );

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Main.MODID);

}
