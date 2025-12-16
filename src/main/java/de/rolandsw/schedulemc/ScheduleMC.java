package de.rolandsw.schedulemc;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.commands.*;
import de.rolandsw.schedulemc.economy.commands.HospitalCommand;
import de.rolandsw.schedulemc.economy.commands.ShopInvestCommand;
import de.rolandsw.schedulemc.economy.commands.StateCommand;
import de.rolandsw.schedulemc.npc.commands.NPCCommand;
import de.rolandsw.schedulemc.tobacco.commands.TobaccoCommand;
import de.rolandsw.schedulemc.warehouse.commands.WarehouseCommand;
import de.rolandsw.schedulemc.economy.PlayerJoinHandler;
import de.rolandsw.schedulemc.events.BlockProtectionHandler;
import de.rolandsw.schedulemc.events.InventoryRestrictionHandler;
import de.rolandsw.schedulemc.npc.events.NPCStealingHandler;
import de.rolandsw.schedulemc.tobacco.events.TobaccoBottleHandler;
import de.rolandsw.schedulemc.economy.events.CashSlotRestrictionHandler;
import de.rolandsw.schedulemc.economy.network.EconomyNetworkHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.client.network.SmartphoneNetworkHandler;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.messaging.network.MessageNetworkHandler;
import de.rolandsw.schedulemc.economy.events.RespawnHandler;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.managers.*;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.items.PlotSelectionTool;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import de.rolandsw.schedulemc.tobacco.business.BusinessMetricsUpdateHandler;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks;
import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoBlockEntities;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.coca.blocks.CocaBlocks;
import de.rolandsw.schedulemc.coca.blockentity.CocaBlockEntities;
import de.rolandsw.schedulemc.poppy.items.PoppyItems;
import de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks;
import de.rolandsw.schedulemc.poppy.blockentity.PoppyBlockEntities;
import de.rolandsw.schedulemc.mushroom.items.MushroomItems;
import de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks;
import de.rolandsw.schedulemc.mushroom.blockentity.MushroomBlockEntities;
import de.rolandsw.schedulemc.tobacco.menu.ModMenuTypes;
import de.rolandsw.schedulemc.tobacco.entity.ModEntities;
import de.rolandsw.schedulemc.economy.blocks.EconomyBlocks;
import de.rolandsw.schedulemc.economy.menu.EconomyMenuTypes;
import de.rolandsw.schedulemc.warehouse.WarehouseBlocks;
import de.rolandsw.schedulemc.warehouse.WarehouseManager;
import de.rolandsw.schedulemc.config.DeliveryPriceConfig;
import de.rolandsw.schedulemc.warehouse.menu.WarehouseMenuTypes;
import de.rolandsw.schedulemc.warehouse.network.WarehouseNetworkHandler;
import de.rolandsw.schedulemc.economy.StateAccount;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.npc.entity.NPCEntities;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.items.NPCItems;
import de.rolandsw.schedulemc.npc.menu.NPCMenuTypes;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.vehicle.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ScheduleMC.MOD_ID)
public class ScheduleMC {
    
    public static final String MOD_ID = "schedulemc";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final int SAVE_INTERVAL = 6000;
    private int tickCounter = 0;

    // Vehicle Mod integration
    private static Main vehicleMod;

    public ScheduleMC() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onEntityAttributeCreation);

        // Initialize Vehicle Mod
        vehicleMod = new Main();

        ModItems.ITEMS.register(modEventBus);
        TobaccoItems.ITEMS.register(modEventBus);
        TobaccoBlocks.BLOCKS.register(modEventBus);
        TobaccoBlocks.ITEMS.register(modEventBus);

        // Koka-System registrieren
        CocaItems.ITEMS.register(modEventBus);
        CocaBlocks.BLOCKS.register(modEventBus);
        CocaBlocks.ITEMS.register(modEventBus);
        CocaBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // Mohn-System registrieren
        PoppyItems.ITEMS.register(modEventBus);
        PoppyBlocks.BLOCKS.register(modEventBus);
        PoppyBlocks.ITEMS.register(modEventBus);
        PoppyBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // Pilz-System registrieren
        MushroomItems.ITEMS.register(modEventBus);
        MushroomBlocks.BLOCKS.register(modEventBus);
        MushroomBlocks.ITEMS.register(modEventBus);
        MushroomBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        EconomyBlocks.BLOCKS.register(modEventBus);
        EconomyBlocks.ITEMS.register(modEventBus);
        de.rolandsw.schedulemc.region.blocks.PlotBlocks.BLOCKS.register(modEventBus);
        de.rolandsw.schedulemc.region.blocks.PlotBlocks.ITEMS.register(modEventBus);
        TobaccoBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        EconomyBlocks.BLOCK_ENTITIES.register(modEventBus);
        WarehouseBlocks.BLOCKS.register(modEventBus);
        WarehouseBlocks.ITEMS.register(modEventBus);
        WarehouseBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        EconomyMenuTypes.MENUS.register(modEventBus);
        WarehouseMenuTypes.MENUS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        NPCItems.ITEMS.register(modEventBus);
        NPCEntities.ENTITIES.register(modEventBus);
        NPCMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigHandler.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModConfigHandler.CLIENT_SPEC);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BlockProtectionHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerJoinHandler());
        MinecraftForge.EVENT_BUS.register(new TobaccoBottleHandler());
        MinecraftForge.EVENT_BUS.register(new CashSlotRestrictionHandler());
        MinecraftForge.EVENT_BUS.register(new InventoryRestrictionHandler());
        MinecraftForge.EVENT_BUS.register(new NPCStealingHandler());
        MinecraftForge.EVENT_BUS.register(new de.rolandsw.schedulemc.npc.events.NPCKnockoutHandler());
        MinecraftForge.EVENT_BUS.register(new de.rolandsw.schedulemc.npc.events.PoliceAIHandler());
        MinecraftForge.EVENT_BUS.register(new de.rolandsw.schedulemc.npc.events.PoliceDoorBlockHandler());
        MinecraftForge.EVENT_BUS.register(de.rolandsw.schedulemc.npc.events.NPCNameSyncHandler.class);
        MinecraftForge.EVENT_BUS.register(RespawnHandler.class);
        MinecraftForge.EVENT_BUS.register(BusinessMetricsUpdateHandler.class);
        MinecraftForge.EVENT_BUS.register(WarehouseManager.class);


        LOGGER.info("ScheduleMC initialized");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize delivery price config from main config (after config is loaded)
            DeliveryPriceConfig.setDefaultPrice(ModConfigHandler.COMMON.WAREHOUSE_DEFAULT_DELIVERY_PRICE.get());

            EconomyNetworkHandler.register();
            NPCNetworkHandler.register();
            ModNetworking.register();
            SmartphoneNetworkHandler.register();
            MessageNetworkHandler.register();
            WarehouseNetworkHandler.register();
        });

        // Vehicle Mod handles its own setup via event bus (registered in Main constructor)
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(NPCEntities.CUSTOM_NPC.get(), CustomNPCEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PlotCommand.register(event.getDispatcher());
        MoneyCommand.register(event.getDispatcher());
        DailyCommand.register(event.getDispatcher());
        TobaccoCommand.register(event.getDispatcher());
        HospitalCommand.register(event.getDispatcher());
        NPCCommand.register(event.getDispatcher(), event.getBuildContext());
        WarehouseCommand.register(event.getDispatcher(), event.getBuildContext());
        ShopInvestCommand.register(event.getDispatcher());
        StateCommand.register(event.getDispatcher());

        // Vehicle Mod handles its own commands via event bus (registered in Main.commonSetup)
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        PlotManager.loadPlots();
        EconomyManager.loadAccounts();
        DailyRewardManager.load();
        WalletManager.load();
        de.rolandsw.schedulemc.npc.crime.CrimeManager.load();
        NPCNameRegistry.loadRegistry();
        MessageManager.loadMessages();

        // NEU: Warehouse & Shop System initialisieren
        StateAccount.load();
        MinecraftForge.EVENT_BUS.register(ShopAccountManager.class);
        WarehouseManager.load(event.getServer());

        // Vehicle System - Vehicle Spawn Registry, Gas Station Registry, Fuel Bills
        de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry.load();
        de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry.load();
        de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager.load();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter >= SAVE_INTERVAL) {
            tickCounter = 0;
            PlotManager.saveIfNeeded();
            EconomyManager.saveIfNeeded();
            DailyRewardManager.saveIfNeeded();
            RentManager.checkExpiredRents();
            WalletManager.saveIfNeeded();
            NPCNameRegistry.saveIfNeeded();
            MessageManager.saveIfNeeded();
            // Vehicle System periodic saves
            de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry.save();
            de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry.save();
            de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager.save();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PlotManager.savePlots();
        EconomyManager.saveAccounts();
        DailyRewardManager.save();
        WalletManager.save();
        NPCNameRegistry.saveRegistry();
        MessageManager.saveMessages();
        WarehouseManager.save(event.getServer());
        // Vehicle System final saves
        de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry.save();
        de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry.save();
        de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager.save();
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide) return;
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();

        // Plot Selection Tool
        if (heldItem.getItem() instanceof PlotSelectionTool) {
            BlockPos pos = event.getPos();
            PlotSelectionTool.setPosition1(player.getUUID(), pos);
            player.displayClientMessage(Component.literal(
                "§a✓ Position 1 gesetzt!\n§7Koordinaten: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
            ), true);
            player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            event.setCanceled(true);
        }

        // Vehicle Spawn Tool (Linksklick)
        if (heldItem.getItem() instanceof de.rolandsw.schedulemc.vehicle.items.VehicleSpawnTool) {
            de.rolandsw.schedulemc.vehicle.items.VehicleSpawnTool.handleLeftClick(player, heldItem, event.getPos().above());
            event.setCanceled(true);
        }
    }
}
