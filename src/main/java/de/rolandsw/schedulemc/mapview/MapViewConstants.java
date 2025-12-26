package de.rolandsw.schedulemc.mapview;

import de.rolandsw.schedulemc.mapview.data.persistence.AsyncPersistenceManager;
import de.rolandsw.schedulemc.mapview.util.BiomeColors;
import java.util.Optional;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class MapViewConstants {
    private static final Logger LOGGER = LogManager.getLogger("MapCore");
    private static final MapCore LIGHTMAP_INSTANCE = new MapCore();
    private static int elapsedTicks;
    private static final ResourceLocation OPTIONS_BACKGROUND_TEXTURE = new ResourceLocation("textures/block/dirt.png");
    public static final boolean DEBUG = false;
    private static boolean initialized;
    private static Events events;
    private static PacketBridge packetBridge;
    private static ModApiBridge modApiBridge;

    private MapViewConstants() {}

    @NotNull
    public static Minecraft getMinecraft() { return Minecraft.getInstance(); }

    public static boolean isSystemMacOS() { return System.getProperty("os.name").toLowerCase().contains("mac"); }

    public static boolean isFabulousGraphicsOrBetter() { return Minecraft.useShaderTransparency(); }

    public static boolean isSinglePlayer() { return getMinecraft().isLocalServer(); }
    public static boolean isRealmServer() {
        // Realms detection not available in 1.20.1 - ServerData.type field doesn't exist
        // Always return false as Realms check is not critical for functionality
        return false;
    }

    @NotNull
    public static Logger getLogger() { return LOGGER; }

    @NotNull
    public static Optional<IntegratedServer> getIntegratedServer() { return Optional.ofNullable(getMinecraft().getSingleplayerServer()); }

    @NotNull
    public static Optional<Level> getWorldByKey(ResourceKey<Level> key) { return getIntegratedServer().map(integratedServer -> integratedServer.getLevel(key)); }

    @NotNull
    public static ClientLevel getClientWorld() { return (ClientLevel) getPlayer().level(); }

    @NotNull
    public static LocalPlayer getPlayer() {
        LocalPlayer player = getMinecraft().player;

        if (player == null) {
            String error = "Attempted to fetch player entity while not in-game!";

            getLogger().fatal(error);
            throw new IllegalStateException(error);
        }

        return player;
    }

    @NotNull
    public static MapCore getLightMapInstance() { return LIGHTMAP_INSTANCE; }

    static void tick() { elapsedTicks = elapsedTicks == Integer.MAX_VALUE ? 1 : elapsedTicks + 1; }

    public static int getElapsedTicks() { return elapsedTicks; }

    static { elapsedTicks = 0; }

    public static ResourceLocation getOptionsBackgroundTexture() {
        return OPTIONS_BACKGROUND_TEXTURE;
    }

    public static void lateInit() {
        initialized = true;
        MapViewConstants.getLightMapInstance().lateInit(true, false);
    }

    public static void clientTick() {
        if (!initialized) {
            lateInit();
        }

        if (initialized) {
            MapViewConstants.getLightMapInstance().onTick();
        }

    }

    public static void renderOverlay(GuiGraphics guiGraphics) {
        if (!initialized) {
            lateInit();
        }

        if (initialized) {
            try {
                MapViewConstants.getLightMapInstance().onTickInGame(guiGraphics);
            } catch (RuntimeException e) {
                MapViewConstants.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Error while render overlay", e);
            }
        }
    }

    public static boolean onChat(Component chat, GuiMessageTag indicator) {
        return true;
    }

    public static boolean onSendChatMessage(String message) {
        return true;
    }

    public static void onShutDown() {
        MapViewConstants.getLogger().info("Saving all world maps");
        MapViewConstants.getLightMapInstance().getWorldMapData().purgeRegionCaches();
        MapViewConstants.getLightMapInstance().getMapOptions().saveAll();
        BiomeColors.saveBiomeColors();
        long shutdownTime = System.currentTimeMillis();

        while (AsyncPersistenceManager.executorService.getQueue().size() + AsyncPersistenceManager.executorService.getActiveCount() > 0 && System.currentTimeMillis() - shutdownTime < 10000L) {
            Thread.onSpinWait();
        }
    }

    public static void playerRunTeleportCommand(double x, double y, double z) {
        MapViewConfiguration mapSettingsManager = MapViewConstants.getLightMapInstance().getMapOptions();
        String cmd = mapSettingsManager.serverTeleportCommand == null ? mapSettingsManager.teleportCommand : mapSettingsManager.serverTeleportCommand;
        cmd = cmd.replace("%p", MapViewConstants.getPlayer().getName().getString()).replace("%x", String.valueOf(x + 0.5)).replace("%y", String.valueOf(y)).replace("%z", String.valueOf(z + 0.5));
        MapViewConstants.getPlayer().connection.sendCommand(cmd);
    }

    public static int moveScoreboard(int bottomX, int entriesHeight) {
        double unscaledHeight = MapViewRenderer.getMinTablistOffset(); // / scaleFactor;
        if (!MapCore.mapOptions.minimapAllowed || MapCore.mapOptions.mapCorner != 1 || !MapCore.mapOptions.moveScoreBoardDown || !Double.isFinite(unscaledHeight)) {
            return bottomX;
        }
        double scaleFactor = Minecraft.getInstance().getWindow().getGuiScale(); // 1x 2x 3x, ...
        double mapHeightScaled = unscaledHeight * 1.37 / scaleFactor; // * 1.37 because unscaledHeight is just the map without the text around it

        int fontHeight = Minecraft.getInstance().font.lineHeight; // height of the title line
        float statusIconOffset = MapViewRenderer.getStatusIconOffset();
        int statusIconOffsetInt = Float.isFinite(statusIconOffset) ? (int) statusIconOffset : 0;
        int minBottom = (int) (mapHeightScaled + entriesHeight + fontHeight + statusIconOffsetInt);

        return Math.max(bottomX, minBottom);
    }

    public static void setEvents(Events events) {
        MapViewConstants.events = events;
    }

    public static Events getEvents() {
        return events;
    }

    public static PacketBridge getPacketBridge() {
        return packetBridge;
    }

    public static void setPacketBridge(PacketBridge packetBridge) {
        MapViewConstants.packetBridge = packetBridge;
    }

    public static void setModApiBride(ModApiBridge modApiBridge) {
        MapViewConstants.modApiBridge = modApiBridge;
    }

    public static ModApiBridge getModApiBridge() {
        return modApiBridge;
    }
}