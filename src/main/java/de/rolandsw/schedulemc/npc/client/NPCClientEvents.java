package de.rolandsw.schedulemc.npc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.client.model.CustomNPCModel;
import de.rolandsw.schedulemc.npc.client.renderer.CustomNPCRenderer;
import de.rolandsw.schedulemc.npc.client.screen.NPCInteractionScreen;
import de.rolandsw.schedulemc.npc.client.screen.NPCSpawnerScreen;
import de.rolandsw.schedulemc.npc.client.screen.MerchantShopScreen;
import de.rolandsw.schedulemc.npc.client.screen.ShopEditorScreen;
import de.rolandsw.schedulemc.npc.client.screen.StealingScreen;
import de.rolandsw.schedulemc.npc.entity.NPCEntities;
import de.rolandsw.schedulemc.npc.menu.NPCMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-Side Events für NPC System
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NPCClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EventHelper.handleEvent(() -> {
            event.enqueueWork(() -> {
                // Register Screens
                MenuScreens.register(NPCMenuTypes.NPC_SPAWNER_MENU.get(), NPCSpawnerScreen::new);
                MenuScreens.register(NPCMenuTypes.NPC_INTERACTION_MENU.get(), NPCInteractionScreen::new);
                MenuScreens.register(NPCMenuTypes.MERCHANT_SHOP_MENU.get(), MerchantShopScreen::new);
                MenuScreens.register(NPCMenuTypes.SHOP_EDITOR_MENU.get(), ShopEditorScreen::new);
                MenuScreens.register(NPCMenuTypes.STEALING_MENU.get(), StealingScreen::new);

                // Register Entity Renderer
                EntityRenderers.register(NPCEntities.CUSTOM_NPC.get(), CustomNPCRenderer::new);
            });
        }, "onClientSetup");
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        EventHelper.handleEvent(() -> {
            event.registerLayerDefinition(CustomNPCModel.LAYER_LOCATION, CustomNPCModel::createBodyLayer);
        }, "registerLayerDefinitions");
    }
}
