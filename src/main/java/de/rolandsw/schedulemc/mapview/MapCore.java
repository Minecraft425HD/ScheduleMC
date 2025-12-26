package de.rolandsw.schedulemc.mapview;

import de.rolandsw.schedulemc.mapview.persistent.WorldMapData;
import de.rolandsw.schedulemc.mapview.config.WorldMapConfiguration;
import de.rolandsw.schedulemc.mapview.data.persistence.AsyncPersistenceManager;
import de.rolandsw.schedulemc.mapview.util.BiomeColors;
import de.rolandsw.schedulemc.mapview.util.DimensionManager;
import de.rolandsw.schedulemc.mapview.util.GameVariableAccessShim;
import de.rolandsw.schedulemc.mapview.util.MapViewHelper;
import de.rolandsw.schedulemc.mapview.util.TextUtils;
import de.rolandsw.schedulemc.mapview.util.WorldUpdateListener;
import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;

public class MapCore implements PreparableReloadListener {
    public static MapViewConfiguration mapOptions;
    private WorldMapConfiguration persistentMapOptions;
    private MapViewRenderer map;
    private WorldMapData persistentMap;
    private ConfigurationChangeNotifier settingsAndLightingChangeNotifier;
    private WorldUpdateListener worldUpdateListener;
    private BlockColorCache colorManager;
    private DimensionManager dimensionManager;
    private ClientLevel world;
    private static String passMessage;
    private ArrayDeque<Runnable> runOnWorldSet = new ArrayDeque<>();
    private String worldSeed = "";
    MapCore() {}

    public void lateInit(boolean showUnderMenus, boolean isFair) {
        mapOptions = new MapViewConfiguration();
        mapOptions.showUnderMenus = showUnderMenus;
        this.persistentMapOptions = new WorldMapConfiguration();
        mapOptions.addSecondaryOptionsManager(this.persistentMapOptions);
        BiomeColors.loadBiomeColors();
        this.colorManager = new BlockColorCache();
        this.dimensionManager = new DimensionManager();
        this.persistentMap = new WorldMapData();
        mapOptions.loadAll();

        // Event listeners are now registered separately during mod construction
        this.map = new MapViewRenderer();
        this.settingsAndLightingChangeNotifier = new ConfigurationChangeNotifier();
        this.worldUpdateListener = new WorldUpdateListener();
        this.worldUpdateListener.addListener(this.map);
        this.worldUpdateListener.addListener(this.persistentMap);
        ReloadableResourceManager resourceManager = (ReloadableResourceManager) MapViewConstants.getMinecraft().getResourceManager();
        resourceManager.registerReloadListener(this);
        this.apply(resourceManager);
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, net.minecraft.util.profiling.ProfilerFiller preparationsProfiler, net.minecraft.util.profiling.ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return preparationBarrier.wait((Object) Unit.INSTANCE).thenRunAsync(() -> this.apply(resourceManager), gameExecutor);
    }

    private void apply(ResourceManager resourceManager) {
        this.colorManager.onResourceManagerReload(resourceManager);
    }

    public void onTickInGame(GuiGraphics guiGraphics) {
        if (this.map != null) {
            this.map.onTickInGame(guiGraphics);
        }
        if (passMessage != null) {
            MapViewConstants.getMinecraft().gui.getChat().addMessage(Component.literal(passMessage));
            passMessage = null;
        }
    }

    public void onTick() {
        ClientLevel newWorld = GameVariableAccessShim.getWorld();
        if (this.world != newWorld) {
            this.world = newWorld;
            this.persistentMap.newWorld(this.world);
            if (this.world != null) {
                MapViewHelper.reset();
                MapViewConstants.getPacketBridge().sendWorldIDPacket();
                this.map.newWorld(this.world);
                while (!runOnWorldSet.isEmpty()) {
                    runOnWorldSet.removeFirst().run();
                }
            }
        }

        MapViewConstants.tick();
        this.persistentMap.onTick();
    }

    public static void checkPermissionMessages(Component message) {
        // Cave mode removed - no permission messages to check
    }

    public MapViewConfiguration getMapOptions() {
        return mapOptions;
    }

    public WorldMapConfiguration getWorldMapDataOptions() {
        return this.persistentMapOptions;
    }

    public MapViewRenderer getMap() {
        return this.map;
    }

    public ConfigurationChangeNotifier getSettingsAndLightingChangeNotifier() {
        return this.settingsAndLightingChangeNotifier;
    }

    public BlockColorCache getColorManager() {
        return this.colorManager;
    }

    public DimensionManager getDimensionManager() {
        return this.dimensionManager;
    }

    public WorldMapData getWorldMapData() {
        return this.persistentMap;
    }

    public void setPermissions(boolean hasCavemodePermission) {
        // Cave mode removed - no permissions to set
    }

    public void sendPlayerMessageOnMainThread(String s) {
        passMessage = s;
    }

    public WorldUpdateListener getWorldUpdateListener() {
        return this.worldUpdateListener;
    }

    public void clearServerSettings() {
        mapOptions.serverTeleportCommand = null;
        mapOptions.worldmapAllowed = true;
        mapOptions.minimapAllowed = true;
    }

    public void onPlayInit() {
        // registries are ready, but no world
    }

    public void onJoinServer() {
        // No-op after radar removal
    }

    public void onDisconnect() {
        clearServerSettings();
    }

    public void onConfigurationInit() {
        clearServerSettings();
    }

    public void onClientStopping() {
        MapViewConstants.onShutDown();
        AsyncPersistenceManager.flushSaveQueue();
    }

    /**
     * Gets the current world name for caching purposes.
     * Returns the singleplayer world name or multiplayer server name.
     */
    public String getCurrentWorldName() {
        if (MapViewConstants.isSinglePlayer()) {
            return MapViewConstants.getIntegratedServer()
                    .map(server -> server.getWorldData().getLevelName())
                    .filter(name -> name != null && !name.isBlank())
                    .orElse("Singleplayer World");
        } else {
            ServerData info = MapViewConstants.getMinecraft().getCurrentServer();
            if (info != null && info.name != null && !info.name.isBlank()) {
                return info.name;
            }
            if (MapViewConstants.isRealmServer()) {
                return "Realms";
            }
            return "Multiplayer Server";
        }
    }

    /**
     * Placeholder for subworld name functionality that was removed.
     * Always returns empty string since waypoint/subworld system was removed.
     */
    public void newSubWorldName(String name, boolean fromServer) {
        // No-op after waypoint system removal
    }

    /**
     * Gets the world seed used for slime chunk calculation.
     * For singleplayer, automatically retrieves from server.
     */
    public String getWorldSeed() {
        if (MapViewConstants.isSinglePlayer()) {
            return MapViewConstants.getIntegratedServer()
                    .map(server -> String.valueOf(server.getWorldData().worldGenOptions().seed()))
                    .orElse("");
        }
        return this.worldSeed;
    }

    /**
     * Sets the world seed for multiplayer slime chunk calculation.
     */
    public void setWorldSeed(String seed) {
        this.worldSeed = seed != null ? seed : "";
    }
}
