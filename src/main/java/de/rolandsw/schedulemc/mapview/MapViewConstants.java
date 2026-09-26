package de.rolandsw.schedulemc.mapview;

import de.rolandsw.schedulemc.mapview.service.data.MapDataManager;
import de.rolandsw.schedulemc.mapview.integration.PacketBridge;
import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.util.BiomeColors;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class MapViewConstants {
    private static final Logger LOGGER = LogManager.getLogger("MapDataManager");
    private static final MapDataManager LIGHTMAP_INSTANCE = new MapDataManager();
    private static int elapsedTicks;
    private static final ResourceLocation OPTIONS_BACKGROUND_TEXTURE = ResourceLocation.parse("textures/block/dirt.png");
    public static final boolean DEBUG = false;
    private static boolean initialized;
    private static PacketBridge packetBridge;

    private MapViewConstants() {}

    @NotNull
    public static Minecraft getMinecraft() { return Minecraft.getInstance(); }

    public static boolean isSystemMacOS() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac"); }

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
    public static MapDataManager getLightMapInstance() { return LIGHTMAP_INSTANCE; }

    public static void tick() { elapsedTicks = elapsedTicks == Integer.MAX_VALUE ? 1 : elapsedTicks + 1; }

    public static int getElapsedTicks() { return elapsedTicks; }

    static { elapsedTicks = 0; }

    public static ResourceLocation getOptionsBackgroundTexture() {
        return OPTIONS_BACKGROUND_TEXTURE;
    }

    public static void lateInit() {
        initialized = true;
        MapViewConstants.getLightMapInstance().lateInit(true, false);
    }

    // DIAGNOSTIC (2026-09-27, Teil 26/27): Temporärer Kill-Switch, um bei einem gemeldeten,
    // bisher trotz zweier behobener Bugs weiterhin bestehenden FPS-Einbruch auf einer
    // konkreten Nutzer-Weltkarte per Bisektion festzustellen, welcher Teil des mapview-
    // Moduls die Ursache ist - ohne dass der Nutzer einen Profiler installieren muss.
    // Teil 26 (ein einziges Flag für BEIDE Pfade zusammen) hat bereits bestätigt: mapview
    // ist die Ursache ("fps ist normal mit dem flag"). Teil 27 splittet das Flag in zwei
    // unabhängige Hälften, damit die nächste Testrunde ohne erneuten Build zwischen
    // Datenaufbau (clientTick -> WorldMapData.onTick(), inkl. periodischer Rescans) und
    // Rendering (renderOverlay -> MapViewRenderer.onTickInGame(), inkl. drawMinimap +
    // eigener periodischer Rescan) unterscheiden kann. Aktivierung: JVM-Argumente
    // -Dschedulemc.disableMapviewTick=true und/oder -Dschedulemc.disableMapviewRender=true
    // (z. B. in PrismLauncher unter Instance Settings -> Java -> JVM Args). Siehe CLAUDE.md
    // Teil 26/27. **Nicht als dauerhaftes Feature gedacht** - nach Abschluss der Diagnose
    // wieder entfernen, falls nicht explizit als dauerhafte Option gewünscht.
    private static final boolean MAPVIEW_TICK_DISABLED = Boolean.getBoolean("schedulemc.disableMapviewTick");
    private static final boolean MAPVIEW_RENDER_DISABLED = Boolean.getBoolean("schedulemc.disableMapviewRender");

    public static void clientTick() {
        if (MAPVIEW_TICK_DISABLED) {
            return;
        }

        if (!initialized) {
            lateInit();
        }

        if (initialized) {
            MapViewConstants.getLightMapInstance().onTick();
        }

    }

    public static void renderOverlay(GuiGraphics guiGraphics) {
        if (MAPVIEW_RENDER_DISABLED) {
            return;
        }

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

    public static void onShutDown() {
        MapViewConstants.getLogger().info("Saving all world maps");
        MapViewConstants.getLightMapInstance().getWorldMapData().purgeRegionCaches();
        MapViewConstants.getLightMapInstance().getMapOptions().saveAll();
        BiomeColors.saveBiomeColors();
        long shutdownTime = System.currentTimeMillis();

        while (de.rolandsw.schedulemc.util.ThreadPoolManager.getComputationPoolQueueSize() + de.rolandsw.schedulemc.util.ThreadPoolManager.getComputationPoolActiveCount() > 0 && System.currentTimeMillis() - shutdownTime < 10000L) {
            Thread.onSpinWait();
        }
    }

    public static void playerRunTeleportCommand(double x, double y, double z) {
        MapViewConfiguration mapSettingsManager = MapViewConstants.getLightMapInstance().getMapOptions();
        String cmd = mapSettingsManager.serverTeleportCommand == null ? mapSettingsManager.teleportCommand : mapSettingsManager.serverTeleportCommand;
        cmd = cmd.replace("%p", MapViewConstants.getPlayer().getName().getString()).replace("%x", String.valueOf(x + 0.5)).replace("%y", String.valueOf(y)).replace("%z", String.valueOf(z + 0.5));
        MapViewConstants.getPlayer().connection.sendCommand(cmd);
    }

    public static PacketBridge getPacketBridge() {
        return packetBridge;
    }

    public static void setPacketBridge(PacketBridge packetBridge) {
        MapViewConstants.packetBridge = packetBridge;
    }
}