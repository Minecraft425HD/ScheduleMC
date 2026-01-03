package de.rolandsw.schedulemc;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.EventHelper;
import de.rolandsw.schedulemc.commands.*;
import de.rolandsw.schedulemc.economy.commands.HospitalCommand;
import de.rolandsw.schedulemc.util.HealthCheckManager;
import de.rolandsw.schedulemc.economy.commands.ShopInvestCommand;
import de.rolandsw.schedulemc.economy.commands.StateCommand;
import de.rolandsw.schedulemc.npc.commands.NPCCommand;
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
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.economy.InterestManager;
import de.rolandsw.schedulemc.economy.LoanManager;
import de.rolandsw.schedulemc.economy.CreditScoreManager;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.TaxManager;
import de.rolandsw.schedulemc.economy.SavingsAccountManager;
import de.rolandsw.schedulemc.economy.OverdraftManager;
import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.client.network.SmartphoneNetworkHandler;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.messaging.network.MessageNetworkHandler;
import de.rolandsw.schedulemc.achievement.network.AchievementNetworkHandler;
import de.rolandsw.schedulemc.achievement.AchievementManager;
import de.rolandsw.schedulemc.economy.events.RespawnHandler;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.managers.*;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
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
import de.rolandsw.schedulemc.meth.items.MethItems;
import de.rolandsw.schedulemc.meth.blocks.MethBlocks;
import de.rolandsw.schedulemc.meth.blockentity.MethBlockEntities;
import de.rolandsw.schedulemc.meth.menu.MethMenuTypes;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.lsd.blocks.LSDBlocks;
import de.rolandsw.schedulemc.lsd.blockentity.LSDBlockEntities;
import de.rolandsw.schedulemc.lsd.menu.LSDMenuTypes;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.mdma.blocks.MDMABlocks;
import de.rolandsw.schedulemc.mdma.blockentity.MDMABlockEntities;
import de.rolandsw.schedulemc.mdma.menu.MDMAMenuTypes;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks;
import de.rolandsw.schedulemc.cannabis.blockentity.CannabisBlockEntities;
import de.rolandsw.schedulemc.cannabis.menu.CannabisMenuTypes;
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
import de.rolandsw.schedulemc.utility.UtilityRegistry;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import de.rolandsw.schedulemc.utility.commands.UtilityCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
    private static final int SAVE_INTERVAL = 6000; // Wird durch IncrementalSaveManager ersetzt
    private int tickCounter = 0;

    // Incremental Save Manager - Optimized Data Persistence
    private IncrementalSaveManager saveManager;

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

        // Meth-System registrieren
        MethItems.ITEMS.register(modEventBus);
        MethBlocks.BLOCKS.register(modEventBus);
        MethBlocks.ITEMS.register(modEventBus);
        MethBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        MethMenuTypes.MENUS.register(modEventBus);

        // LSD-System registrieren
        LSDItems.ITEMS.register(modEventBus);
        LSDBlocks.BLOCKS.register(modEventBus);
        LSDBlocks.ITEMS.register(modEventBus);
        LSDBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        LSDMenuTypes.MENUS.register(modEventBus);

        // MDMA-System registrieren
        MDMAItems.ITEMS.register(modEventBus);
        MDMABlocks.BLOCKS.register(modEventBus);
        MDMABlocks.ITEMS.register(modEventBus);
        MDMABlockEntities.BLOCK_ENTITIES.register(modEventBus);
        MDMAMenuTypes.MENUS.register(modEventBus);

        // Cannabis-System registrieren
        CannabisItems.ITEMS.register(modEventBus);
        CannabisBlocks.BLOCKS.register(modEventBus);
        CannabisBlocks.ITEMS.register(modEventBus);
        CannabisBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CannabisMenuTypes.MENUS.register(modEventBus);

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
        MinecraftForge.EVENT_BUS.register(UtilityEventHandler.class);

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
            AchievementNetworkHandler.register();
            de.rolandsw.schedulemc.region.network.PlotNetworkHandler.register();
            de.rolandsw.schedulemc.npc.crime.prison.network.PrisonNetworkHandler.register();
            de.rolandsw.schedulemc.player.network.PlayerSettingsNetworkHandler.register();
            de.rolandsw.schedulemc.npc.crime.prison.PrisonManager.init();
            de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.register();

            // Utility-System initialisieren
            UtilityRegistry.registerDefaults();
            UtilityRegistry.resolveBlockReferences();
            LOGGER.info("Utility-System initialisiert");
        });

        // Vehicle Mod handles its own setup via event bus (registered in Main constructor)
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(NPCEntities.CUSTOM_NPC.get(), CustomNPCEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        EventHelper.handleEvent(() -> {
            PlotCommand.register(event.getDispatcher());
            MoneyCommand.register(event.getDispatcher());
            // LoanCommand removed - now handled via CreditAdvisor NPC
            // DailyCommand removed - now automatic on login
            HospitalCommand.register(event.getDispatcher());
            NPCCommand.register(event.getDispatcher(), event.getBuildContext());
            WarehouseCommand.register(event.getDispatcher(), event.getBuildContext());
            ShopInvestCommand.register(event.getDispatcher());
            StateCommand.register(event.getDispatcher());
            UtilityCommand.register(event.getDispatcher());
            HealthCommand.register(event.getDispatcher());
            de.rolandsw.schedulemc.npc.crime.prison.PrisonCommand.register(event.getDispatcher());
            de.rolandsw.schedulemc.territory.MapCommand.register(event.getDispatcher());

            // Vehicle Mod handles its own commands via event bus (registered in Main.commonSetup)
        }, "onRegisterCommands");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        EventHelper.handleEvent(() -> {
            PlotManager.loadPlots();
            EconomyManager.loadAccounts();
            DailyRewardManager.load();
            WalletManager.load();
            de.rolandsw.schedulemc.npc.crime.CrimeManager.load();
            NPCNameRegistry.loadRegistry();
            MessageManager.loadMessages();
            de.rolandsw.schedulemc.player.PlayerSettingsManager.load();

            // NEU: Warehouse & Shop System initialisieren
            StateAccount.load();
            MinecraftForge.EVENT_BUS.register(ShopAccountManager.class);
            WarehouseManager.load(event.getServer());

            // Economy System - Advanced Features
            EconomyManager.initialize(event.getServer());
            TransactionHistory.getInstance(event.getServer());
            InterestManager.getInstance(event.getServer());
            LoanManager.getInstance(event.getServer());
            TaxManager.getInstance(event.getServer());
            SavingsAccountManager.getInstance(event.getServer());
            OverdraftManager.getInstance(event.getServer());
            RecurringPaymentManager.getInstance(event.getServer());

            // Credit System - NPC-based loans with credit score
            CreditScoreManager.getInstance(event.getServer());
            CreditLoanManager.getInstance(event.getServer());
            LOGGER.info("Advanced Economy Systems initialized (Transaction History, Interest, Loans, Taxes, Savings, Overdraft, Recurring Payments, Credit Score)");

            // Achievement System
            AchievementManager.getInstance(event.getServer());
            LOGGER.info("Achievement System initialized");

            // Vehicle System - Vehicle Spawn Registry, Gas Station Registry, Fuel Bills
            de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry.load();
            de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry.load();
            de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager.load();

            // Utility-System laden
            PlotUtilityManager.load();

            // ═══════════════════════════════════════════════════════════
            // INCREMENTAL SAVE MANAGER - Performance Optimization
            // ═══════════════════════════════════════════════════════════
            LOGGER.info("Initializing IncrementalSaveManager...");
            saveManager = new IncrementalSaveManager();

            // Kritische Manager registrieren (Priority 0-2)
            saveManager.register(EconomyManager.getInstance());
            saveManager.register(PlotManager.getInstance());

            // SaveManager starten
            saveManager.start();
            LOGGER.info("IncrementalSaveManager started - automatic background saves active");

            // Health-Check nach Start
            LOGGER.info("Performing initial health check...");
            HealthCheckManager.logHealthCheck();
        }, "onServerStarted");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, server -> {
            tickCounter++;

            // Economy Systems - Tick every server tick for day tracking
            if (server.overworld() != null) {
                long dayTime = server.overworld().getDayTime();
                InterestManager.getInstance(server).tick(dayTime);
                LoanManager.getInstance(server).tick(dayTime);
                TaxManager.getInstance(server).tick(dayTime);
                SavingsAccountManager.getInstance(server).tick(dayTime);
                OverdraftManager.getInstance(server).tick(dayTime);
                RecurringPaymentManager.getInstance(server).tick(dayTime);

                // Credit System
                CreditScoreManager.getInstance(server).tick(dayTime);
                CreditLoanManager.getInstance(server).tick(dayTime);

                // Bank Systems
                de.rolandsw.schedulemc.npc.bank.StockMarketData.getInstance(server).tick(dayTime);
                de.rolandsw.schedulemc.npc.bank.TransferLimitTracker.getInstance(server).tick(dayTime);
            }

            if (tickCounter >= SAVE_INTERVAL) {
                tickCounter = 0;
                // PlotManager.saveIfNeeded();  // Jetzt via IncrementalSaveManager
                // EconomyManager.saveIfNeeded();  // Jetzt via IncrementalSaveManager
                DailyRewardManager.saveIfNeeded();
                RentManager.checkExpiredRents();
                WalletManager.saveIfNeeded();
                NPCNameRegistry.saveIfNeeded();
                MessageManager.saveIfNeeded();
                // Vehicle System periodic saves
                de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry.save();
                de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry.save();
                de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager.save();

                // Economy Systems periodic saves
                InterestManager.getInstance(server).save();
                LoanManager.getInstance(server).save();
                TaxManager.getInstance(server).save();
                SavingsAccountManager.getInstance(server).save();
                OverdraftManager.getInstance(server).save();
                RecurringPaymentManager.getInstance(server).save();

                // Credit System periodic saves
                CreditScoreManager.getInstance(server).save();
                CreditLoanManager.getInstance(server).save();
            }
        });
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        EventHelper.handleEvent(() -> {
            // ═══════════════════════════════════════════════════════════
            // INCREMENTAL SAVE MANAGER - Final Save & Shutdown
            // ═══════════════════════════════════════════════════════════
            if (saveManager != null) {
                LOGGER.info("Stopping IncrementalSaveManager and performing final save...");
                saveManager.saveAll(); // Finaler Save aller registrierten Manager
                saveManager.stop();
                LOGGER.info("IncrementalSaveManager stopped successfully");
            }

            // Manuelle Saves für noch nicht migrierte Manager
            // PlotManager.savePlots();  // Jetzt via IncrementalSaveManager
            // EconomyManager.saveAccounts();  // Jetzt via IncrementalSaveManager
            DailyRewardManager.save();
            WalletManager.save();
            NPCNameRegistry.saveRegistry();
            MessageManager.saveMessages();
            WarehouseManager.save(event.getServer());

            // Economy Systems final saves
            InterestManager.getInstance(event.getServer()).save();
            LoanManager.getInstance(event.getServer()).save();
            TaxManager.getInstance(event.getServer()).save();
            SavingsAccountManager.getInstance(event.getServer()).save();
            OverdraftManager.getInstance(event.getServer()).save();
            RecurringPaymentManager.getInstance(event.getServer()).save();

            // Credit System final saves
            CreditScoreManager.getInstance(event.getServer()).save();
            CreditLoanManager.getInstance(event.getServer()).save();

            TransactionHistory history = TransactionHistory.getInstance();
            if (history != null) {
                history.save();
            }
            LOGGER.info("Advanced Economy Systems saved (including Savings, Overdraft, Recurring Payments, and Credit Score)");

            // Vehicle System final saves
            de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry.save();
            de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry.save();
            de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager.save();

            // Utility-System speichern
            PlotUtilityManager.save();
        }, "onServerStopping");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EventHelper.handleEvent(() -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                // Automatische Daily-Belohnung beim Login
                DailyRewardManager.claimOnLogin(serverPlayer);
            }
        }, "onPlayerLoggedIn");
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        EventHelper.handleLeftClickBlock(event, player -> {
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
        });
    }
}
