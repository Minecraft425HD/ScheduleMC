package de.rolandsw.schedulemc;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.EventHelper;
import de.rolandsw.schedulemc.util.ThreadPoolManager;
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
import de.rolandsw.schedulemc.player.PlayerTracker;
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
import de.rolandsw.schedulemc.coffee.items.CoffeeItems;
import de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks;
import de.rolandsw.schedulemc.coffee.blockentity.CoffeeBlockEntities;
import de.rolandsw.schedulemc.coffee.menu.CoffeeMenuTypes;
import de.rolandsw.schedulemc.coffee.network.CoffeeNetworking;
import de.rolandsw.schedulemc.wine.items.WineItems;
import de.rolandsw.schedulemc.wine.blocks.WineBlocks;
import de.rolandsw.schedulemc.wine.blockentity.WineBlockEntities;
import de.rolandsw.schedulemc.wine.menu.WineMenuTypes;
import de.rolandsw.schedulemc.wine.network.WineNetworking;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks;
import de.rolandsw.schedulemc.cheese.blockentity.CheeseBlockEntities;
import de.rolandsw.schedulemc.cheese.menu.CheeseMenuTypes;
import de.rolandsw.schedulemc.cheese.network.CheeseNetworking;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.honey.blocks.HoneyBlocks;
import de.rolandsw.schedulemc.honey.blockentity.HoneyBlockEntities;
import de.rolandsw.schedulemc.honey.menu.HoneyMenuTypes;
import de.rolandsw.schedulemc.honey.network.HoneyNetworking;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
import de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks;
import de.rolandsw.schedulemc.chocolate.blockentity.ChocolateBlockEntities;
import de.rolandsw.schedulemc.chocolate.menu.ChocolateMenuTypes;
import de.rolandsw.schedulemc.chocolate.network.ChocolateNetworking;
import de.rolandsw.schedulemc.beer.items.BeerItems;
import de.rolandsw.schedulemc.beer.blocks.BeerBlocks;
import de.rolandsw.schedulemc.beer.blockentity.BeerBlockEntities;
import de.rolandsw.schedulemc.beer.menu.BeerMenuTypes;
import de.rolandsw.schedulemc.beer.network.BeerNetworking;
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
    private static final int SAVE_INTERVAL = 6000; // 5 Minuten (6000 Ticks)
    private int tickCounter = 0; // Für periodische Saves der noch nicht migrierten Manager

    // Incremental Save Manager - Optimized Data Persistence
    private IncrementalSaveManager saveManager;

    // Vehicle Mod integration
    private static Main vehicleMod;

    public ScheduleMC() {
        this(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public ScheduleMC(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onEntityAttributeCreation);

        // Register client-side packet handlers only on client
        net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::clientSetup);
        });

        // Initialize Vehicle Mod
        vehicleMod = new Main(modEventBus);

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

        // Coffee-System registrieren
        CoffeeItems.ITEMS.register(modEventBus);
        CoffeeBlocks.BLOCKS.register(modEventBus);
        CoffeeBlocks.ITEMS.register(modEventBus);
        CoffeeBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CoffeeMenuTypes.MENUS.register(modEventBus);

        // Wine-System registrieren
        WineItems.ITEMS.register(modEventBus);
        WineBlocks.BLOCKS.register(modEventBus);
        WineBlocks.ITEMS.register(modEventBus);
        WineBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        WineMenuTypes.MENUS.register(modEventBus);

        // Cheese-System registrieren
        CheeseItems.ITEMS.register(modEventBus);
        CheeseBlocks.BLOCKS.register(modEventBus);
        CheeseBlocks.ITEMS.register(modEventBus);
        CheeseBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CheeseMenuTypes.MENUS.register(modEventBus);

        // Honey-System registrieren
        HoneyItems.ITEMS.register(modEventBus);
        HoneyBlocks.BLOCKS.register(modEventBus);
        HoneyBlocks.ITEMS.register(modEventBus);
        HoneyBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        HoneyMenuTypes.MENUS.register(modEventBus);

        // Chocolate-System registrieren
        ChocolateItems.ITEMS.register(modEventBus);
        ChocolateBlocks.BLOCKS.register(modEventBus);
        ChocolateBlocks.ITEMS.register(modEventBus);
        ChocolateBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ChocolateMenuTypes.MENUS.register(modEventBus);

        // Beer-System registrieren
        BeerItems.ITEMS.register(modEventBus);
        BeerBlocks.BLOCKS.register(modEventBus);
        BeerBlocks.ITEMS.register(modEventBus);
        BeerBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        BeerMenuTypes.MENUS.register(modEventBus);

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
        de.rolandsw.schedulemc.towing.menu.TowingMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, ModConfigHandler.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ModConfigHandler.CLIENT_SPEC);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BlockProtectionHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerJoinHandler());
        MinecraftForge.EVENT_BUS.register(new TobaccoBottleHandler());
        MinecraftForge.EVENT_BUS.register(new CashSlotRestrictionHandler());
        MinecraftForge.EVENT_BUS.register(new InventoryRestrictionHandler());
        MinecraftForge.EVENT_BUS.register(new NPCStealingHandler());
        MinecraftForge.EVENT_BUS.register(new de.rolandsw.schedulemc.npc.events.EntityRemoverHandler());
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
            CoffeeNetworking.register();
            WineNetworking.register();
            CheeseNetworking.register();
            HoneyNetworking.register();
            ChocolateNetworking.register();
            BeerNetworking.register();
            SmartphoneNetworkHandler.register();
            MessageNetworkHandler.register();
            WarehouseNetworkHandler.register();
            AchievementNetworkHandler.register();
            de.rolandsw.schedulemc.region.network.PlotNetworkHandler.register();
            de.rolandsw.schedulemc.npc.crime.prison.network.PrisonNetworkHandler.register();
            de.rolandsw.schedulemc.player.network.PlayerSettingsNetworkHandler.register();
            de.rolandsw.schedulemc.towing.network.TowingNetworkHandler.register();
            de.rolandsw.schedulemc.npc.crime.prison.PrisonManager.init();
            de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.register();

            // MapView (LightMap) network packets - must be registered on both client and server
            de.rolandsw.schedulemc.mapview.integration.forge.ForgeEvents.registerNetworkPackets();

            // Utility-System initialisieren
            UtilityRegistry.registerDefaults();
            UtilityRegistry.resolveBlockReferences();
            LOGGER.info("Utility-System initialisiert");
        });

        // Vehicle Mod handles its own setup via event bus (registered in Main constructor)
    }

    private void clientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register client-bound packets that reference Screen classes
            // These must be registered client-side only to avoid loading Screen classes on the server
            de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.registerClientPackets();
            de.rolandsw.schedulemc.npc.crime.prison.network.PrisonNetworkHandler.registerClientPackets();
            LOGGER.info("Client-side packets registered");
        });
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
            de.rolandsw.schedulemc.npc.commands.AdminToolsCommand.register(event.getDispatcher());
            WarehouseCommand.register(event.getDispatcher(), event.getBuildContext());
            ShopInvestCommand.register(event.getDispatcher());
            StateCommand.register(event.getDispatcher());
            UtilityCommand.register(event.getDispatcher());
            HealthCommand.register(event.getDispatcher());
            de.rolandsw.schedulemc.npc.crime.prison.PrisonCommand.register(event.getDispatcher());
            de.rolandsw.schedulemc.territory.MapCommand.register(event.getDispatcher());
            de.rolandsw.schedulemc.npc.crime.BountyCommand.register(event.getDispatcher());
            de.rolandsw.schedulemc.market.MarketCommand.register(event.getDispatcher());

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
            PlayerTracker.load();
            de.rolandsw.schedulemc.player.PlayerSettingsManager.load();

            // Towing Service System
            de.rolandsw.schedulemc.towing.MembershipManager.load();
            de.rolandsw.schedulemc.towing.TowingYardManager.load();
            de.rolandsw.schedulemc.towing.TowingServiceRegistry.initializeDefaultServices();

            // NEU: Warehouse & Shop System initialisieren
            StateAccount.load();
            MinecraftForge.EVENT_BUS.register(ShopAccountManager.class);
            WarehouseManager.load(event.getServer());

            // Crime & Territory Systems - Initialize managers with persistence
            de.rolandsw.schedulemc.npc.crime.BountyManager.getInstance(event.getServer());
            de.rolandsw.schedulemc.territory.TerritoryManager.getInstance(event.getServer());

            // Market System - Load market data
            de.rolandsw.schedulemc.market.DynamicMarketManager.getInstance().load();
            LOGGER.info("Crime, Territory, and Market Systems initialized");

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

            // NPC Life System Manager - Implemented managers with JSON persistence
            LOGGER.info("Initializing NPC Life System Managers...");
            de.rolandsw.schedulemc.npc.life.social.FactionManager.getInstance(event.getServer());
            de.rolandsw.schedulemc.npc.life.witness.WitnessManager.getInstance(event.getServer());
            de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager.getInstance(event.getServer());
            // TODO: Remaining managers (CompanionManager, QuestManager, DialogueManager,
            // NPCInteractionManager, WorldEventManager, DynamicPriceManager) need persistence
            // implementation - see NPC_LIFE_PERSISTENCE_IMPLEMENTATION_GUIDE.md for details
            LOGGER.info("NPC Life System Managers initialized (3/9 completed)");

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

            // Crime & Territory Systems (Priority 2)
            saveManager.register(de.rolandsw.schedulemc.npc.crime.BountyManager.getInstance(event.getServer()));
            saveManager.register(de.rolandsw.schedulemc.territory.TerritoryManager.getInstance(event.getServer()));

            // Market System (Priority 3)
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "DynamicMarketManager",
                () -> de.rolandsw.schedulemc.market.DynamicMarketManager.getInstance().save(),
                3
            ));

            // Player Systems (Priority 4)
            saveManager.register(de.rolandsw.schedulemc.player.PlayerTracker.getInstance());
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "PlayerSettingsManager",
                de.rolandsw.schedulemc.player.PlayerSettingsManager::saveIfNeeded,
                4
            ));

            // Wallet & Daily Rewards (Priority 4)
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "WalletManager",
                WalletManager::saveIfNeeded,
                4
            ));
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "DailyRewardManager",
                DailyRewardManager::saveIfNeeded,
                4
            ));

            // Messaging System (Priority 5)
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "MessageManager",
                MessageManager::saveIfNeeded,
                5
            ));

            // NPC Life System Managers (Priority 5) - Completed: 3/9
            saveManager.register(de.rolandsw.schedulemc.npc.life.social.FactionManager.getInstance());
            saveManager.register(de.rolandsw.schedulemc.npc.life.witness.WitnessManager.getInstance());
            saveManager.register(de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager.getInstance());
            // TODO: Remaining 6 managers need AbstractPersistenceManager implementation:
            // - CompanionManager, QuestManager, DialogueManager, NPCInteractionManager,
            // - WorldEventManager, DynamicPriceManager
            // See NPC_LIFE_PERSISTENCE_IMPLEMENTATION_GUIDE.md for implementation pattern

            // NPC System (Priority 5)
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "NPCNameRegistry",
                de.rolandsw.schedulemc.managers.NPCNameRegistry::saveIfNeeded,
                5
            ));

            // Towing Service (Priority 6)
            saveManager.register(de.rolandsw.schedulemc.towing.MembershipManager.getInstance());
            saveManager.register(de.rolandsw.schedulemc.towing.TowingYardManager.getInstance());

            // Warehouse System (Priority 6)
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "WarehouseManager",
                () -> de.rolandsw.schedulemc.warehouse.WarehouseManager.save(event.getServer()),
                6
            ));

            // Utility System (Priority 6)
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "PlotUtilityManager",
                de.rolandsw.schedulemc.utility.PlotUtilityManager::save,
                6
            ));

            // State Account (Priority 7)
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "StateAccount",
                StateAccount::save,
                7
            ));

            // Economy Advanced Systems (Priority 3)
            saveManager.register(InterestManager.getInstance(event.getServer()));
            saveManager.register(LoanManager.getInstance(event.getServer()));
            saveManager.register(TaxManager.getInstance(event.getServer()));
            saveManager.register(SavingsAccountManager.getInstance(event.getServer()));
            saveManager.register(OverdraftManager.getInstance(event.getServer()));
            saveManager.register(RecurringPaymentManager.getInstance(event.getServer()));
            saveManager.register(CreditScoreManager.getInstance(event.getServer()));
            saveManager.register(CreditLoanManager.getInstance(event.getServer()));

            // Vehicle Systems (Priority 5)
            saveManager.register(de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager.getInstance());
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "VehicleSpawnRegistry",
                de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry::save,
                5
            ));
            saveManager.register(new de.rolandsw.schedulemc.util.SaveableWrapper(
                "FuelStationRegistry",
                de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry::save,
                5
            ));

            LOGGER.info("Registered {} managers with IncrementalSaveManager", saveManager.getRegisteredCount());

            // SaveManager starten
            saveManager.start();
            LOGGER.info("IncrementalSaveManager started - automatic background saves active");

            // Health-Check nach Start
            LOGGER.info("Performing initial health check...");
            HealthCheckManager.logHealthCheck();

            // ═══════════════════════════════════════════════════════════
            // TODO: SCHEDULEMC API - Initialize public API for external mods
            // ═══════════════════════════════════════════════════════════
            // ScheduleMCAPI needs 11 interface implementations:
            // 1. IEconomyAPI - Economy operations (EconomyManager wrapper)
            // 2. IPlotAPI - Plot management (PlotManager wrapper)
            // 3. IProductionAPI - Production system access
            // 4. INPCAPI - NPC management and interaction
            // 5. IPoliceAPI - Crime and wanted level system
            // 6. IWarehouseAPI - Warehouse management
            // 7. IMessagingAPI - Message system
            // 8. ISmartphoneAPI - Smartphone app integration
            // 9. IVehicleAPI - Vehicle system access
            // 10. IAchievementAPI - Achievement tracking
            // 11. IMarketAPI - Dynamic market system
            //
            // After implementing all 11, call:
            // ScheduleMCAPI.initialize(economyAPI, plotAPI, productionAPI, npcAPI,
            //     policeAPI, warehouseAPI, messagingAPI, smartphoneAPI,
            //     vehicleAPI, achievementAPI, marketAPI);
            //
            // This is CRITICAL for external mod compatibility!
            // ═══════════════════════════════════════════════════════════

        }, "onServerStarted");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, server -> {
            tickCounter++;

            // OPTIMIERUNG: Spieler-Cache für Polizei-KI aktualisieren (einmal pro Tick)
            long currentTick = server.overworld() != null ? server.overworld().getGameTime() : 0;
            de.rolandsw.schedulemc.npc.events.PoliceAIHandler.updatePlayerCache(server, currentTick);

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
                // ALLE Manager werden jetzt via IncrementalSaveManager gespeichert
                // Nur Business Logic bleibt hier
                RentManager.checkExpiredRents();
                // HINWEIS: Alle Saves werden jetzt automatisch vom IncrementalSaveManager gehandhabt
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

            // ═══════════════════════════════════════════════════════════
            // THREAD POOL MANAGER - Shutdown all managed thread pools
            // ═══════════════════════════════════════════════════════════
            LOGGER.info("Shutting down ThreadPoolManager...");
            ThreadPoolManager.shutdown();
            LOGGER.info("ThreadPoolManager shutdown completed");

            // HINWEIS: Alle Manager wurden bereits von saveManager.saveAll() gespeichert (Zeile 592)
            // Keine manuellen Saves mehr nötig - IncrementalSaveManager handled alles!
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
