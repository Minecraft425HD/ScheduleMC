package de.rolandsw.schedulemc.lightmap.forge;

import de.rolandsw.schedulemc.lightmap.Events;
import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import de.rolandsw.schedulemc.lightmap.LightMap;
import de.rolandsw.schedulemc.lightmap.packets.LightMapSettingsS2C;
import de.rolandsw.schedulemc.lightmap.packets.WorldIdC2S;
import de.rolandsw.schedulemc.lightmap.packets.WorldIdS2C;
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
    private LightMap map;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("lightmap", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public ForgeEvents() {
    }

    @Override
    public void initEvents(LightMap map) {
        this.map = map;
        // Event listeners are now registered from ClientModEvents in ScheduleMC
        MinecraftForge.EVENT_BUS.register(new ForgeEventListener(map));
    }

    // Public method to be called from ClientModEvents
    public void preInitClientPublic() {
        LightMapConstants.lateInit();
        if (map != null) {
            map.onConfigurationInit();
        }
    }

    // Public method to be called from ClientModEvents
    public void registerPacketsPublic() {
        registerPackets(null);
    }

    private void preInitClient(final FMLClientSetupEvent event) {
        // Initialize LightMap on the main thread (required for texture creation)
        event.enqueueWork(() -> {
            LightMapConstants.lateInit();
            map.onConfigurationInit();
        });
    }

    public void registerPackets(final FMLClientSetupEvent event) {
        int id = 0;
        CHANNEL.registerMessage(id++, LightMapSettingsS2C.class,
            LightMapSettingsS2C::write,
            LightMapSettingsS2C::new,
            (msg, ctx) -> LightMapSettingsChannelHandlerForge.handleDataOnMain(msg, ctx),
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(id++, WorldIdS2C.class,
            WorldIdS2C::write,
            WorldIdS2C::new,
            (msg, ctx) -> LightMapWorldIdChannelHandlerForge.handleDataOnMain(msg, ctx),
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(id++, WorldIdC2S.class,
            WorldIdC2S::write,
            WorldIdC2S::new,
            (msg, ctx) -> { ctx.get().setPacketHandled(true); },
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    private static class ForgeEventListener {
        private final LightMap map;

        public ForgeEventListener(LightMap map) {
            this.map = map;
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                LightMapConstants.clientTick();
            }
        }

        @SubscribeEvent
        public void onRenderGui(RenderGuiOverlayEvent.Post event) {
            // In 1.20.1, only render after the hotbar to avoid rendering multiple times per frame
            if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
                LightMapConstants.renderOverlay(event.getGuiGraphics());
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
