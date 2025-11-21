package de.rolandsw.schedulemc;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.commands.*;
import de.rolandsw.schedulemc.economy.commands.HospitalCommand;
import de.rolandsw.schedulemc.tobacco.commands.TobaccoCommand;
import de.rolandsw.schedulemc.economy.PlayerJoinHandler;
import de.rolandsw.schedulemc.events.BlockProtectionHandler;
import de.rolandsw.schedulemc.tobacco.events.TobaccoBottleHandler;
import de.rolandsw.schedulemc.economy.events.CashSlotRestrictionHandler;
import de.rolandsw.schedulemc.economy.network.EconomyNetworkHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.events.RespawnHandler;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.managers.*;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.items.PlotSelectionTool;
import de.rolandsw.schedulemc.tobacco.TobaccoShopIntegration;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks;
import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoBlockEntities;
import de.rolandsw.schedulemc.tobacco.menu.ModMenuTypes;
import de.rolandsw.schedulemc.tobacco.entity.ModEntities;
import de.rolandsw.schedulemc.economy.blocks.EconomyBlocks;
import de.rolandsw.schedulemc.economy.menu.EconomyMenuTypes;
import de.rolandsw.schedulemc.npc.entity.NPCEntities;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.items.NPCItems;
import de.rolandsw.schedulemc.npc.menu.NPCMenuTypes;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
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

    public ScheduleMC() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onEntityAttributeCreation);

        ModItems.ITEMS.register(modEventBus);
        TobaccoItems.ITEMS.register(modEventBus);
        TobaccoBlocks.BLOCKS.register(modEventBus);
        TobaccoBlocks.ITEMS.register(modEventBus);
        EconomyBlocks.BLOCKS.register(modEventBus);
        EconomyBlocks.ITEMS.register(modEventBus);
        TobaccoBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        EconomyBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        EconomyMenuTypes.MENUS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        NPCItems.ITEMS.register(modEventBus);
        NPCEntities.ENTITIES.register(modEventBus);
        NPCMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigHandler.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BlockProtectionHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerJoinHandler());
        MinecraftForge.EVENT_BUS.register(new TobaccoBottleHandler());
        MinecraftForge.EVENT_BUS.register(new CashSlotRestrictionHandler());
        MinecraftForge.EVENT_BUS.register(RespawnHandler.class);


        LOGGER.info("ScheduleMC initialized");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EconomyNetworkHandler.register();
            NPCNetworkHandler.register();
        });
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(NPCEntities.CUSTOM_NPC.get(), CustomNPCEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PlotCommand.register(event.getDispatcher());
        MoneyCommand.register(event.getDispatcher());
        DailyCommand.register(event.getDispatcher());
        ShopCommand.register(event.getDispatcher());
        TobaccoCommand.register(event.getDispatcher());
        HospitalCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        PlotManager.loadPlots();
        EconomyManager.loadAccounts();
        DailyRewardManager.load();
        ShopManager.load();
        TobaccoShopIntegration.registerShopItems();
        WalletManager.load();
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
            ShopManager.saveIfNeeded();
            RentManager.checkExpiredRents();
            WalletManager.saveIfNeeded();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PlotManager.savePlots();
        EconomyManager.saveAccounts();
        DailyRewardManager.save();
        ShopManager.save();
        WalletManager.save();
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide) return;
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();
        if (heldItem.getItem() instanceof PlotSelectionTool) {
            BlockPos pos = event.getPos();
            PlotSelectionTool.setPosition1(player.getUUID(), pos);
            player.displayClientMessage(Component.literal(
                "§a✓ Position 1 gesetzt!\n§7Koordinaten: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
            ), true);
            player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            event.setCanceled(true);
        }
    }
}
