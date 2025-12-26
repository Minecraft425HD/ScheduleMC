package de.rolandsw.schedulemc.mapview.service.data;

import de.rolandsw.schedulemc.mapview.service.data.WorldMapService;
import de.rolandsw.schedulemc.mapview.config.WorldMapConfiguration;
import de.rolandsw.schedulemc.mapview.data.persistence.AsyncPersistenceManager;
import de.rolandsw.schedulemc.mapview.util.BiomeColors;
import de.rolandsw.schedulemc.mapview.service.data.DimensionService;
import de.rolandsw.schedulemc.mapview.service.coordination.RenderCoordinationService;
import de.rolandsw.schedulemc.mapview.service.coordination.WorldStateService;
import de.rolandsw.schedulemc.mapview.service.coordination.LifecycleService;
import de.rolandsw.schedulemc.mapview.integration.minecraft.MinecraftAccessor;
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

/**
 * Orchestrator for map data management operations.
 * Coordinates multiple specialized services to handle rendering, world state,
 * lifecycle, and data management in a modular architecture.
 *
 * Phase 2 refactoring: Reduced god-class anti-pattern by delegating
 * responsibilities to specialized service classes.
 */
public class MapDataManager implements PreparableReloadListener {
    public static MapViewConfiguration mapOptions;
    private WorldMapConfiguration persistentMapOptions;

    // Specialized coordination services (Phase 2)
    private final RenderCoordinationService renderService;
    private final WorldStateService worldStateService;
    private LifecycleService lifecycleServiceInstance;

    // Data and business logic services
    private WorldMapData persistentMap;
    private ConfigNotificationService settingsAndLightingChangeNotifier;
    private WorldUpdateListener worldUpdateListener;
    private ColorCalculationService colorManager;
    private DimensionService dimensionManager;

    private ArrayDeque<Runnable> runOnWorldSet = new ArrayDeque<>();

    MapDataManager() {
        // Initialize coordination services
        this.renderService = new RenderCoordinationService();
        this.worldStateService = new WorldStateService();
        this.lifecycleServiceInstance = null; // Initialized in lateInit when config is ready
    }

    public void lateInit(boolean showUnderMenus, boolean isFair) {
        mapOptions = new MapViewConfiguration();
        mapOptions.showUnderMenus = showUnderMenus;
        this.persistentMapOptions = new WorldMapConfiguration();
        mapOptions.addSecondaryOptionsManager(this.persistentMapOptions);
        BiomeColors.loadBiomeColors();
        this.colorManager = new ColorCalculationService();
        this.dimensionManager = new DimensionService();
        this.persistentMap = new WorldMapData();
        mapOptions.loadAll();

        // Initialize lifecycle service with configuration
        this.lifecycleServiceInstance = new LifecycleService(mapOptions);

        // Event listeners are now registered separately during mod construction
        this.settingsAndLightingChangeNotifier = new ConfigNotificationService();
        this.worldUpdateListener = new WorldUpdateListener();
        this.worldUpdateListener.addListener(this.renderService.getRenderer());
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
        // Delegate to render coordination service
        this.renderService.onTickInGame(guiGraphics);
    }

    public void onTick() {
        ClientLevel newWorld = MinecraftAccessor.getWorld();

        // Delegate world change detection to worldStateService
        if (this.worldStateService.hasWorldChanged(newWorld)) {
            this.persistentMap.newWorld(newWorld);
            if (newWorld != null) {
                MapViewHelper.reset();
                MapViewConstants.getPacketBridge().sendWorldIDPacket();
                this.renderService.onWorldChanged(newWorld);
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
        return this.renderService.getRenderer();
    }

    public ConfigNotificationService getSettingsAndLightingChangeNotifier() {
        return this.settingsAndLightingChangeNotifier;
    }

    public ColorCalculationService getColorManager() {
        return this.colorManager;
    }

    public DimensionService getDimensionManager() {
        return this.dimensionManager;
    }

    public WorldMapData getWorldMapData() {
        return this.persistentMap;
    }

    public void setPermissions(boolean hasCavemodePermission) {
        // Delegate to lifecycle service
        this.lifecycleServiceInstance.setPermissions(hasCavemodePermission);
    }

    public void sendPlayerMessageOnMainThread(String s) {
        // Delegate to render service
        this.renderService.sendMessageOnMainThread(s);
    }

    public WorldUpdateListener getWorldUpdateListener() {
        return this.worldUpdateListener;
    }

    public void clearServerSettings() {
        // Delegate to lifecycle service
        this.lifecycleServiceInstance.clearServerSettings();
    }

    public void onPlayInit() {
        // Delegate to lifecycle service
        this.lifecycleServiceInstance.onPlayInit();
    }

    public void onJoinServer() {
        // Delegate to lifecycle service
        this.lifecycleServiceInstance.onJoinServer();
    }

    public void onDisconnect() {
        // Delegate to lifecycle service
        this.lifecycleServiceInstance.onDisconnect();
        // Also reset world state
        this.worldStateService.reset();
    }

    public void onConfigurationInit() {
        // Delegate to lifecycle service
        this.lifecycleServiceInstance.onConfigurationInit();
    }

    public void onClientStopping() {
        // Delegate to lifecycle service
        this.lifecycleServiceInstance.onClientStopping();
    }

    /**
     * Gets the current world name for caching purposes.
     * Returns the singleplayer world name or multiplayer server name.
     */
    public String getCurrentWorldName() {
        // Delegate to world state service
        return this.worldStateService.getCurrentWorldName();
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
        // Delegate to world state service
        return this.worldStateService.getWorldSeed();
    }

    /**
     * Sets the world seed for multiplayer slime chunk calculation.
     */
    public void setWorldSeed(String seed) {
        // Delegate to world state service
        this.worldStateService.setWorldSeed(seed);
    }
}
