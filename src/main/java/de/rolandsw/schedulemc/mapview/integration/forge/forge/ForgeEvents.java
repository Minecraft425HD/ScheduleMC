package de.rolandsw.schedulemc.mapview.integration.forge;

import de.rolandsw.schedulemc.mapview.integration.Events;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.service.data.MapDataManager;
import de.rolandsw.schedulemc.mapview.integration.network.MapViewSettingsS2C;
import de.rolandsw.schedulemc.mapview.integration.network.WorldIdC2S;
import de.rolandsw.schedulemc.mapview.integration.network.WorldIdS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.GameShuttingDownEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ForgeEvents implements Events {
    private MapDataManager map;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("schedulemc", "lightmap_main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static boolean packetsRegistered = false;

    public ForgeEvents() {
    }

    /**
     * Register network packets for the lightmap channel.
     * Must be called on both client and server to avoid version mismatch.
     */
    public static synchronized void registerNetworkPackets() {
        if (packetsRegistered) {
            return; // Already registered
        }

        int id = 0;
        CHANNEL.registerMessage(id++, MapViewSettingsS2C.class,
            MapViewSettingsS2C::write,
            MapViewSettingsS2C::new,
            (msg, ctx) -> MapViewSettingsChannelHandlerForge.handleDataOnMain(msg, ctx),
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(id++, WorldIdS2C.class,
            WorldIdS2C::write,
            WorldIdS2C::new,
            (msg, ctx) -> MapViewWorldIdChannelHandlerForge.handleDataOnMain(msg, ctx),
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(id++, WorldIdC2S.class,
            WorldIdC2S::write,
            WorldIdC2S::new,
            (msg, ctx) -> { ctx.get().setPacketHandled(true); },
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        packetsRegistered = true;
    }

    @Override
    public void initEvents(MapDataManager map) {
        this.map = map;
        // Event listeners are now registered from ClientModEvents in ScheduleMC
        MinecraftForge.EVENT_BUS.register(new ForgeEventListener(map));
    }

    // Public method to be called from ClientModEvents
    public void preInitClientPublic() {
        MapViewConstants.lateInit();
        if (map != null) {
            map.onConfigurationInit();
        }
    }

    // Public method to be called from ClientModEvents
    public void registerPacketsPublic() {
        registerNetworkPackets();
    }

    private void preInitClient(final FMLClientSetupEvent event) {
        // Initialize MapDataManager on the main thread (required for texture creation)
        event.enqueueWork(() -> {
            MapViewConstants.lateInit();
            map.onConfigurationInit();
        });
    }

    private static class ForgeEventListener {
        private final MapDataManager map;

        public ForgeEventListener(MapDataManager map) {
            this.map = map;
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                MapViewConstants.clientTick();
            }
        }

        @SubscribeEvent
        public void onRenderGui(RenderGuiOverlayEvent.Post event) {
            // In 1.20.1, only render after the hotbar to avoid rendering multiple times per frame
            if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
                MapViewConstants.renderOverlay(event.getGuiGraphics());
            }
        }

        @SubscribeEvent
        public void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
            map.onPlayInit();
        }

        @SubscribeEvent
        public void onQuit(ClientPlayerNetworkEvent.LoggingOut event) {
            map.onDisconnect();
        }

        @SubscribeEvent
        public void onClientShutdown(GameShuttingDownEvent event) {
            map.onClientStopping();
        }
    }
}
