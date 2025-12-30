package de.rolandsw.schedulemc.mapview.presentation.renderer;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.config.MapOption;
import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.navigation.graph.NavigationOverlay;
import de.rolandsw.schedulemc.mapview.npc.NPCMapRenderer;
import de.rolandsw.schedulemc.mapview.service.data.MapDataManager;
import de.rolandsw.schedulemc.mapview.service.render.ColorCalculationService;
import de.rolandsw.schedulemc.mapview.core.model.AbstractMapData;
import de.rolandsw.schedulemc.mapview.core.event.MapChangeListener;
import de.rolandsw.schedulemc.mapview.integration.DebugRenderState;
import de.rolandsw.schedulemc.mapview.presentation.screen.WorldMapScreen;
import de.rolandsw.schedulemc.mapview.util.BiomeColors;
import de.rolandsw.schedulemc.mapview.util.BlockDatabase;
import de.rolandsw.schedulemc.mapview.service.render.LightingCalculator;
import de.rolandsw.schedulemc.mapview.service.render.ColorUtils;
import de.rolandsw.schedulemc.mapview.service.render.strategy.ChunkScanStrategy;
import de.rolandsw.schedulemc.mapview.service.render.strategy.ChunkScanStrategyFactory;
import de.rolandsw.schedulemc.mapview.util.DimensionContainer;
import de.rolandsw.schedulemc.mapview.util.DynamicMoveableTexture;
import de.rolandsw.schedulemc.mapview.data.repository.MapDataRepository;
import de.rolandsw.schedulemc.mapview.integration.minecraft.MinecraftAccessor;
import de.rolandsw.schedulemc.mapview.util.LayoutVariables;
import de.rolandsw.schedulemc.mapview.util.ChunkCache;
import de.rolandsw.schedulemc.mapview.util.MapViewHelper;
import de.rolandsw.schedulemc.mapview.util.MutableBlockPos;
import de.rolandsw.schedulemc.mapview.data.cache.BlockPositionCache;
import de.rolandsw.schedulemc.mapview.util.ScaledDynamicMutableTexture;
import de.rolandsw.schedulemc.mapview.util.MapViewCachedOrthoProjectionMatrixBuffer;
import de.rolandsw.schedulemc.mapview.util.MapViewGuiGraphics;
import de.rolandsw.schedulemc.mapview.util.MapViewPipelines;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import de.rolandsw.schedulemc.mapview.util.ARGBCompat;
import net.minecraft.util.Mth;
// EnvironmentAttributes doesn't exist in 1.20.1
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Random;
import java.util.TreeSet;

public class MapViewRenderer implements Runnable, MapChangeListener {
    private final Minecraft minecraft = Minecraft.getInstance();
    // private final float[] lastLightBrightnessTable = new float[16];
    private final Object coordinateLock = new Object();
    private final ResourceLocation resourceArrow = new ResourceLocation("schedulemc", "mapview/images/mmarrow.png");
    private final ResourceLocation resourceSquareMap = new ResourceLocation("schedulemc", "mapview/images/squaremap.png");
    private final ResourceLocation resourceRoundMap = new ResourceLocation("schedulemc", "mapview/images/roundmap.png");
    private final ResourceLocation squareStencil = new ResourceLocation("schedulemc", "mapview/images/square.png");
    private final ResourceLocation circleStencil = new ResourceLocation("schedulemc", "mapview/images/circle.png");
    private ClientLevel world;
    private final MapViewConfiguration options;
    private final LayoutVariables layoutVariables;
    private final ColorCalculationService colorManager;
    private final NPCMapRenderer npcMapRenderer = new NPCMapRenderer();
    private final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private final boolean multicore = this.availableProcessors > 1;
    private final int heightMapResetHeight = this.multicore ? 2 : 5;
    private final int heightMapResetTime = this.multicore ? 300 : 3000;
    private final boolean threading = this.multicore;
    private final MapDataRepository[] mapData = new MapDataRepository[5];
    private final ChunkCache[] chunkCache = new ChunkCache[5];
    private DynamicMoveableTexture[] mapImages;
    private ResourceLocation[] mapResources;
    private final DynamicMoveableTexture[] mapImagesFiltered = new DynamicMoveableTexture[5];
    private final DynamicMoveableTexture[] mapImagesUnfiltered = new DynamicMoveableTexture[5];
    private BlockState transparentBlockState;
    private BlockState surfaceBlockState;
    private boolean imageChanged = true;
    private LightTexture lightmapTexture;
    private boolean needLightmapRefresh = true;
    private int tickWithLightChange;
    private boolean lastPaused = true;
    private double lastGamma;
    private float lastSunBrightness;
    private float lastLightning;
    private float lastPotion;
    private final int[] lastLightmapValues = { -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216 };
    private boolean needSkyColor;
    private boolean lastAboveHorizon = true;
    private int lastBiome;
    private int lastSkyColor;
    private final Random generator = new Random();
    private Screen lastGuiScreen;
    private boolean fullscreenMap;
    private int zoom;
    private int scWidth;
    private int scHeight;
    private String error = "";
    private int ztimer;
    private int heightMapFudge;
    private int timer;
    private boolean doFullRender = true;
    private boolean zoomChanged;
    private int lastX;
    private int lastZ;
    private int lastY;
    private int lastImageX;
    private int lastImageZ;
    private boolean lastFullscreen;
    // Performance-Optimierung: Movement-Throttling
    private int lastPlayerX = Integer.MIN_VALUE;
    private int lastPlayerY = Integer.MIN_VALUE;
    private int lastPlayerZ = Integer.MIN_VALUE;
    private int ticksSinceLastUpdate = 0;
    private static final int UPDATE_THROTTLE_TICKS = 2; // Update nur alle 2 Ticks (10 FPS statt 20 FPS)
    private int biomeSegmentationCounter = 0;
    // Chunk cache to avoid redundant getChunkAt() calls
    private LevelChunk cachedChunk = null;
    private int cachedChunkX = Integer.MIN_VALUE;
    private int cachedChunkZ = Integer.MIN_VALUE;
    private float direction;
    private float percentX;
    private float percentY;
    private int northRotate;
    private Thread zCalc = new Thread(this, "MapDataManager LiveMap Calculation Thread");
    private int zCalcTicker;
    private int[] lightmapColors = new int[256];
    private double zoomScale = 1.0;
    private double zoomScaleAdjusted = 1.0;
    private static double minTablistOffset;
    private static float statusIconOffset = 0.0F;

    private final ResourceLocation[] resourceMapImageFiltered = new ResourceLocation[5];
    private final ResourceLocation[] resourceMapImageUnfiltered = new ResourceLocation[5];
    // private GpuTexture fboTexture;
    // private GpuTextureView fboTextureView;
    private Tesselator fboTessellator = new Tesselator(4096);
    private MapViewCachedOrthoProjectionMatrixBuffer projection;

    public MapViewRenderer() {
        resourceMapImageFiltered[0] = new ResourceLocation("schedulemc", "mapview/map/filtered/0");
        resourceMapImageFiltered[1] = new ResourceLocation("schedulemc", "mapview/map/filtered/1");
        resourceMapImageFiltered[2] = new ResourceLocation("schedulemc", "mapview/map/filtered/2");
        resourceMapImageFiltered[3] = new ResourceLocation("schedulemc", "mapview/map/filtered/3");
        resourceMapImageFiltered[4] = new ResourceLocation("schedulemc", "mapview/map/filtered/4");
        resourceMapImageUnfiltered[0] = new ResourceLocation("schedulemc", "mapview/map/unfiltered/0");
        resourceMapImageUnfiltered[1] = new ResourceLocation("schedulemc", "mapview/map/unfiltered/1");
        resourceMapImageUnfiltered[2] = new ResourceLocation("schedulemc", "mapview/map/unfiltered/2");
        resourceMapImageUnfiltered[3] = new ResourceLocation("schedulemc", "mapview/map/unfiltered/3");
        resourceMapImageUnfiltered[4] = new ResourceLocation("schedulemc", "mapview/map/unfiltered/4");

        this.options = MapViewConstants.getLightMapInstance().getMapOptions();
        this.colorManager = MapViewConstants.getLightMapInstance().getColorManager();
        this.layoutVariables = new LayoutVariables();
        ArrayList<KeyMapping> tempBindings = new ArrayList<>();
        tempBindings.addAll(Arrays.asList(minecraft.options.keyMappings));
        tempBindings.addAll(Arrays.asList(this.options.keyBindings));
        minecraft.options.keyMappings = tempBindings.toArray(new KeyMapping[0]);

        this.zCalc.start();
        this.mapData[0] = new MapDataRepository(32, 32);
        this.mapData[1] = new MapDataRepository(64, 64);
        this.mapData[2] = new MapDataRepository(128, 128);
        this.mapData[3] = new MapDataRepository(256, 256);
        this.mapData[4] = new MapDataRepository(512, 512);
        this.chunkCache[0] = new ChunkCache(3, 3, this);
        this.chunkCache[1] = new ChunkCache(5, 5, this);
        this.chunkCache[2] = new ChunkCache(9, 9, this);
        this.chunkCache[3] = new ChunkCache(17, 17, this);
        this.chunkCache[4] = new ChunkCache(33, 33, this);
        this.mapImagesFiltered[0] = new DynamicMoveableTexture(32, 32, true);
        this.mapImagesFiltered[1] = new DynamicMoveableTexture(64, 64, true);
        this.mapImagesFiltered[2] = new DynamicMoveableTexture(128, 128, true);
        this.mapImagesFiltered[3] = new DynamicMoveableTexture(256, 256, true);
        this.mapImagesFiltered[4] = new DynamicMoveableTexture(512, 512, true);
        minecraft.getTextureManager().register(resourceMapImageFiltered[0], this.mapImagesFiltered[0]);
        minecraft.getTextureManager().register(resourceMapImageFiltered[1], this.mapImagesFiltered[1]);
        minecraft.getTextureManager().register(resourceMapImageFiltered[2], this.mapImagesFiltered[2]);
        minecraft.getTextureManager().register(resourceMapImageFiltered[3], this.mapImagesFiltered[3]);
        minecraft.getTextureManager().register(resourceMapImageFiltered[4], this.mapImagesFiltered[4]);
        this.mapImagesUnfiltered[0] = new ScaledDynamicMutableTexture(32, 32, true);
        this.mapImagesUnfiltered[1] = new ScaledDynamicMutableTexture(64, 64, true);
        this.mapImagesUnfiltered[2] = new ScaledDynamicMutableTexture(128, 128, true);
        this.mapImagesUnfiltered[3] = new ScaledDynamicMutableTexture(256, 256, true);
        this.mapImagesUnfiltered[4] = new ScaledDynamicMutableTexture(512, 512, true);
        minecraft.getTextureManager().register(resourceMapImageUnfiltered[0], this.mapImagesUnfiltered[0]);
        minecraft.getTextureManager().register(resourceMapImageUnfiltered[1], this.mapImagesUnfiltered[1]);
        minecraft.getTextureManager().register(resourceMapImageUnfiltered[2], this.mapImagesUnfiltered[2]);
        minecraft.getTextureManager().register(resourceMapImageUnfiltered[3], this.mapImagesUnfiltered[3]);
        minecraft.getTextureManager().register(resourceMapImageUnfiltered[4], this.mapImagesUnfiltered[4]);

        if (this.options.filtering) {
            this.mapImages = this.mapImagesFiltered;
            this.mapResources = resourceMapImageFiltered;
        } else {
            this.mapImages = this.mapImagesUnfiltered;
            this.mapResources = resourceMapImageUnfiltered;
        }

        this.zoom = this.options.zoom;
        this.setZoomScale();

        this.projection = new MapViewCachedOrthoProjectionMatrixBuffer("MapDataManager MapViewRenderer To Screen Proj", -256.0F, 256.0F, 256.0F, -256.0F, 1000.0F, 21000.0F);

        try {
            var arrowResourceOpt = Minecraft.getInstance().getResourceManager().getResource(resourceArrow);
            if (arrowResourceOpt.isPresent()) {
                DynamicTexture arrowTexture = new DynamicTexture(NativeImage.read(arrowResourceOpt.get().open()));
                minecraft.getTextureManager().register(resourceArrow, arrowTexture);
            } else {
                MapViewConstants.getLogger().warn("Arrow texture not found: " + resourceArrow);
            }

            var squareMapResourceOpt = Minecraft.getInstance().getResourceManager().getResource(resourceSquareMap);
            if (squareMapResourceOpt.isPresent()) {
                DynamicTexture squareMapTexture = new DynamicTexture(NativeImage.read(squareMapResourceOpt.get().open()));
                minecraft.getTextureManager().register(resourceSquareMap, squareMapTexture);
            } else {
                MapViewConstants.getLogger().warn("Square map texture not found: " + resourceSquareMap);
            }

            var roundMapResourceOpt = Minecraft.getInstance().getResourceManager().getResource(resourceRoundMap);
            if (roundMapResourceOpt.isPresent()) {
                DynamicTexture roundMapTexture = new DynamicTexture(NativeImage.read(roundMapResourceOpt.get().open()));
                minecraft.getTextureManager().register(resourceRoundMap, roundMapTexture);
            } else {
                MapViewConstants.getLogger().warn("Round map texture not found: " + resourceRoundMap);
            }
        } catch (Exception exception) {
            MapViewConstants.getLogger().error("Failed getting map images " + exception.getLocalizedMessage(), exception);
        }
    }

    public void forceFullRender(boolean forceFullRender) {
        this.doFullRender = forceFullRender;
        MapViewConstants.getLightMapInstance().getSettingsAndLightingChangeNotifier().notifyOfChanges();
    }

    public float getPercentX() {
        return this.percentX;
    }

    public float getPercentY() {
        return this.percentY;
    }

    @Override
    public void run() {
        if (minecraft != null) {
            while (true) {
                if (this.world != null) {
                    if (this.options.minimapAllowed) {
                        try {
                            this.mapCalc(this.doFullRender);
                            if (!this.doFullRender) {
                                MutableBlockPos blockPos = BlockPositionCache.get();
                                this.chunkCache[this.zoom].centerChunks(blockPos.withXYZ(this.lastX, 0, this.lastZ));
                                BlockPositionCache.release(blockPos);
                                this.chunkCache[this.zoom].checkIfChunksChanged();
                            }
                        } catch (Exception exception) {
                            MapViewConstants.getLogger().error("MapDataManager LiveMap Calculation Thread", exception);
                        }
                    }

                    this.doFullRender = this.zoomChanged;
                    this.zoomChanged = false;
                }

                this.zCalcTicker = 0;
                synchronized (this.zCalc) {
                    try {
                        this.zCalc.wait(0L);
                    } catch (InterruptedException exception) {
                        MapViewConstants.getLogger().error("MapDataManager LiveMap Calculation Thread", exception);
                    }
                }
            }
        }

    }

    public void newWorld(ClientLevel world) {
        this.world = world;
        this.lightmapTexture = this.getLightmapTexture();
        this.mapData[this.zoom].blank();
        this.doFullRender = true;
        MapViewConstants.getLightMapInstance().getSettingsAndLightingChangeNotifier().notifyOfChanges();
    }

    public void newWorldName() {
        // Waypoints removed - no subworld name display
        this.error = "";
    }

    public void onTickInGame(GuiGraphics drawContext) {
        this.northRotate = this.options.oldNorth ? 90 : 0;

        if (this.lightmapTexture == null) {
            this.lightmapTexture = this.getLightmapTexture();
        }

        if (minecraft.screen == null && this.options.keyBindMenu.consumeClick()) {
            minecraft.setScreen(new WorldMapScreen(null));
        }

        if (minecraft.screen == null && this.options.keyBindZoom.consumeClick()) {
            this.cycleZoomLevel();
        }

        if (minecraft.screen == null && this.options.keyBindFullscreen.consumeClick()) {
            this.fullscreenMap = !this.fullscreenMap;
            if (this.zoom == 4) {
                this.error = I18n.get("mapview.ui.zoomLevel") + " (0.25x)";
            } else if (this.zoom == 3) {
                this.error = I18n.get("mapview.ui.zoomLevel") + " (0.5x)";
            } else if (this.zoom == 2) {
                this.error = I18n.get("mapview.ui.zoomLevel") + " (1.0x)";
            } else if (this.zoom == 1) {
                this.error = I18n.get("mapview.ui.zoomLevel") + " (2.0x)";
            } else {
                this.error = I18n.get("mapview.ui.zoomLevel") + " (4.0x)";
            }
        }

        // Performance-Optimierung: Throttle checkForChanges - nur alle 20 Ticks (1x/Sek)
        if (this.timer % 20 == 0) {
            this.checkForChanges();
        }
        this.lastGuiScreen = minecraft.screen;
        this.calculateCurrentLightAndSkyColor();

        // Performance-Optimierung: Throttle map updates - nur wenn sich Player bewegt hat oder throttle-Intervall erreicht
        int currentX = MinecraftAccessor.xCoord();
        int currentY = MinecraftAccessor.yCoord();
        int currentZ = MinecraftAccessor.zCoord();
        boolean playerMoved = currentX != lastPlayerX || currentY != lastPlayerY || currentZ != lastPlayerZ;
        ticksSinceLastUpdate++;

        boolean shouldUpdate = playerMoved || ticksSinceLastUpdate >= UPDATE_THROTTLE_TICKS;

        if (shouldUpdate) {
            lastPlayerX = currentX;
            lastPlayerY = currentY;
            lastPlayerZ = currentZ;
            ticksSinceLastUpdate = 0;
        }

        if (this.threading) {
            if (!this.zCalc.isAlive()) {
                this.zCalc = new Thread(this, "MapDataManager LiveMap Calculation Thread");
                this.zCalc.start();
                this.zCalcTicker = 0;
            }

            if (!(minecraft.screen instanceof DeathScreen) && !(minecraft.screen instanceof OutOfMemoryScreen)) {
                ++this.zCalcTicker;
                if (this.zCalcTicker > 2000) {
                    this.zCalcTicker = 0;
                    Exception ex = new Exception();
                    ex.setStackTrace(this.zCalc.getStackTrace());
                    DebugRenderState.print();
                    MapViewConstants.getLogger().error("MapDataManager LiveMap Calculation Thread is hanging?", ex);
                }
                // Performance-Optimierung: Notify nur wenn Update nötig
                if (shouldUpdate) {
                    synchronized (this.zCalc) {
                        this.zCalc.notify();
                    }
                }
            }
        } else {
            // Performance-Optimierung: Nur recalculieren wenn Update nötig
            if (shouldUpdate && this.options.minimapAllowed && this.world != null) {
                this.mapCalc(this.doFullRender);
                if (!this.doFullRender) {
                    MutableBlockPos blockPos = BlockPositionCache.get();
                    this.chunkCache[this.zoom].centerChunks(blockPos.withXYZ(this.lastX, 0, this.lastZ));
                    BlockPositionCache.release(blockPos);
                    this.chunkCache[this.zoom].checkIfChunksChanged();
                }
            }

            this.doFullRender = false;
        }

        boolean enabled = !minecraft.options.hideGui && (this.options.showUnderMenus || minecraft.screen == null) && !minecraft.options.renderDebug;

        this.direction = MinecraftAccessor.rotationYaw() + 180.0F;

        while (this.direction >= 360.0F) {
            this.direction -= 360.0F;
        }

        while (this.direction < 0.0F) {
            this.direction += 360.0F;
        }

        if (!this.error.isEmpty() && this.ztimer == 0) {
            this.ztimer = 500;
        }

        if (this.ztimer > 0) {
            --this.ztimer;
        }

        if (this.ztimer == 0 && !this.error.isEmpty()) {
            this.error = "";
        }

        if (enabled && MapDataManager.mapOptions.minimapAllowed) {
            this.drawMinimap(drawContext);
        }

        this.timer = this.timer > 5000 ? 0 : this.timer + 1;
    }

    private void cycleZoomLevel() {
        if (this.options.zoom == 4) {
            this.options.zoom = 3;
            this.error = I18n.get("mapview.ui.zoomLevel") + " (0.5x)";
        } else if (this.options.zoom == 3) {
            this.options.zoom = 2;
            this.error = I18n.get("mapview.ui.zoomLevel") + " (1.0x)";
        } else if (this.options.zoom == 2) {
            this.options.zoom = 1;
            this.error = I18n.get("mapview.ui.zoomLevel") + " (2.0x)";
        } else if (this.options.zoom == 1) {
            this.options.zoom = 0;
            this.error = I18n.get("mapview.ui.zoomLevel") + " (4.0x)";
        } else if (this.options.zoom == 0) {
            this.options.zoom = 4;
            this.error = I18n.get("mapview.ui.zoomLevel") + " (0.25x)";
        }

        this.options.saveAll();
        this.zoomChanged = true;
        this.zoom = this.options.zoom;
        this.setZoomScale();
        this.doFullRender = true;
    }

    private void setZoomScale() {
        this.zoomScale = Math.pow(2.0, this.zoom) / 2.0;
        if (this.options.squareMap && this.options.rotates) {
            this.zoomScaleAdjusted = this.zoomScale / 1.4142F;
        } else {
            this.zoomScaleAdjusted = this.zoomScale;
        }

    }

    private LightTexture getLightmapTexture() {
        return minecraft.gameRenderer.lightTexture();
    }

    public void calculateCurrentLightAndSkyColor() {
        try {
            if (this.world != null) {
                if (this.needLightmapRefresh && MapViewConstants.getElapsedTicks() != this.tickWithLightChange && !minecraft.isPaused() || this.options.isRealTimeTorches()) {
                    this.needLightmapRefresh = false;
                    LightingCalculator lightmap = LightingCalculator.getInstance();
                    lightmap.setup();
                    for (int blockLight = 0; blockLight < 16; blockLight++) {
                        for (int skyLight = 0; skyLight < 16; skyLight++) {
                            this.lightmapColors[blockLight + skyLight * 16] = lightmap.getLight(blockLight, skyLight);
                        }
                    }
                }

                boolean lightChanged = false;
                if (minecraft.options.gamma().get() != this.lastGamma) {
                    lightChanged = true;
                    this.lastGamma = minecraft.options.gamma().get();
                }

                float sunBrightness = 1 - (this.world.getSkyDarken() / 15f);
                if (Math.abs(this.lastSunBrightness - sunBrightness) > 0.01 || sunBrightness == 1.0 && sunBrightness != this.lastSunBrightness || sunBrightness == 0.0 && sunBrightness != this.lastSunBrightness) {
                    lightChanged = true;
                    this.needSkyColor = true;
                    this.lastSunBrightness = sunBrightness;
                }

                float potionEffect = 0.0F;
                if (MapViewConstants.getPlayer().hasEffect(MobEffects.NIGHT_VISION)) {
                    int duration = MapViewConstants.getPlayer().getEffect(MobEffects.NIGHT_VISION).getDuration();
                    potionEffect = duration > 200 ? 1.0F : 0.7F + Mth.sin((duration - 1.0F) * (float) Math.PI * 0.2F) * 0.3F;
                }

                if (this.lastPotion != potionEffect) {
                    this.lastPotion = potionEffect;
                    lightChanged = true;
                }

                int lastLightningBolt = this.world.getSkyFlashTime();
                if (this.lastLightning != lastLightningBolt) {
                    this.lastLightning = lastLightningBolt;
                    lightChanged = true;
                }

                if (this.lastPaused != minecraft.isPaused()) {
                    this.lastPaused = !this.lastPaused;
                    lightChanged = true;
                }

                boolean scheduledUpdate = (this.timer - 50) % 50 == 0;
                if (lightChanged || scheduledUpdate) {
                    this.tickWithLightChange = MapViewConstants.getElapsedTicks();
                    this.needLightmapRefresh = true;
                }

                boolean aboveHorizon = MapViewConstants.getPlayer().getEyePosition(0.0F).y >= this.world.getLevelData().getHorizonHeight(this.world);
                if (this.world.dimension().location().toString().toLowerCase().contains("ether")) {
                    aboveHorizon = true;
                }

                if (aboveHorizon != this.lastAboveHorizon) {
                    this.needSkyColor = true;
                    this.lastAboveHorizon = aboveHorizon;
                }

                MutableBlockPos blockPos = BlockPositionCache.get();
                int biomeID = this.world.registryAccess().registryOrThrow(Registries.BIOME).getId(this.world.getBiome(blockPos.withXYZ(MinecraftAccessor.xCoord(), MinecraftAccessor.yCoord(), MinecraftAccessor.zCoord())).value());
                BlockPositionCache.release(blockPos);
                if (biomeID != this.lastBiome) {
                    this.needSkyColor = true;
                    this.lastBiome = biomeID;
                }

                if (this.needSkyColor || scheduledUpdate) {
                    this.colorManager.setSkyColor(this.getSkyColor());
                }
            }
        } catch (NullPointerException ignore) {

        }
    }

    private int getSkyColor() {
        this.needSkyColor = false;
        boolean aboveHorizon = this.lastAboveHorizon;
        // Original: Vector4f color = Minecraft.getInstance().gameRenderer.fogRenderer.computeFogColor(minecraft.gameRenderer.getMainCamera(), 0.0F, this.world, minecraft.options.renderDistance().get(), minecraft.gameRenderer.getDarkenWorldAmount(0.0F));
        // Fallback: Get biome sky color
        net.minecraft.world.phys.Vec3 skyColorVec = this.world.getSkyColor(minecraft.gameRenderer.getMainCamera().getPosition(), 0.0F);
        int skyColorInt = ((int)(skyColorVec.x * 255) << 16) | ((int)(skyColorVec.y * 255) << 8) | (int)(skyColorVec.z * 255);
        Vector4f color = new Vector4f(((skyColorInt >> 16) & 0xFF) / 255.0F, ((skyColorInt >> 8) & 0xFF) / 255.0F, (skyColorInt & 0xFF) / 255.0F, 1.0F);
        float r = color.x;
        float g = color.y;
        float b = color.z;
        if (!aboveHorizon) {
            return 0x0A000000 + (int) (r * 255.0F) * 65536 + (int) (g * 255.0F) * 256 + (int) (b * 255.0F);
        } else {
            int backgroundColor = 0xFF000000 + (int) (r * 255.0F) * 65536 + (int) (g * 255.0F) * 256 + (int) (b * 255.0F);
            // In 1.20.1, EnvironmentAttributes doesn't exist - skipping sunset color overlay
            return backgroundColor;
        }
    }

    public int[] getLightmapArray() {
        return this.lightmapColors;
    }

    public int getLightmapColor(int skyLight, int blockLight) {
        if (this.lightmapColors == null) {
            return 0;
        }
        return ARGBCompat.toABGR(this.lightmapColors[blockLight + skyLight * 16]);
    }

    public void drawMinimap(GuiGraphics drawContext) {
        int scScaleOrig = 1;

        while (minecraft.getWindow().getWidth() / (scScaleOrig + 1) >= 320 && minecraft.getWindow().getHeight() / (scScaleOrig + 1) >= 240) {
            ++scScaleOrig;
        }

        int scScale = Math.max(1, scScaleOrig + (this.fullscreenMap ? 0 : this.options.sizeModifier));
        double scaledWidthD = (double) minecraft.getWindow().getWidth() / scScale;
        double scaledHeightD = (double) minecraft.getWindow().getHeight() / scScale;
        this.scWidth = Mth.ceil(scaledWidthD);
        this.scHeight = Mth.ceil(scaledHeightD);
        float scaleProj = (float) (scScale) / (float) minecraft.getWindow().getGuiScale();

        int mapX;
        if (this.options.mapCorner != 0 && this.options.mapCorner != 3) {
            mapX = this.scWidth - 37;
        } else {
            mapX = 37;
        }

        int mapY;
        if (this.options.mapCorner != 0 && this.options.mapCorner != 1) {
            mapY = this.scHeight - 37;
        } else {
            mapY = 37;
        }

        float statusIconOffset = 0.0F;
        if (MapDataManager.mapOptions.moveMapDownWhileStatusEffect) {
            if (this.options.mapCorner == 1 && !MapViewConstants.getPlayer().getActiveEffects().isEmpty()) {

                for (MobEffectInstance statusEffectInstance : MapViewConstants.getPlayer().getActiveEffects()) {
                    if (statusEffectInstance.showIcon()) {
                        if (statusEffectInstance.getEffect().isBeneficial()) {
                            statusIconOffset = Math.max(statusIconOffset, 24.0F);
                        } else {
                            statusIconOffset = 50.0F;
                        }
                    }
                }
                int scHeight = minecraft.getWindow().getGuiScaledHeight();
                float resFactor = (float) this.scHeight / scHeight;
                mapY += (int) (statusIconOffset * resFactor);
            }
        }
        MapViewRenderer.statusIconOffset = statusIconOffset;

        if (this.fullscreenMap) {
            this.renderMapFull(drawContext, this.scWidth, this.scHeight, scaleProj);
            // Render Navigation Overlay für Fullscreen
            renderNavigationOverlay(drawContext, this.scWidth / 2, this.scHeight / 2,
                    Math.min(this.scWidth, this.scHeight), (float) this.zoomScale, true);
            // Render NPCs auf Fullscreen-Karte
            renderNPCMarkers(drawContext, this.scWidth / 2, this.scHeight / 2,
                    Math.min(this.scWidth, this.scHeight), (float) this.zoomScale, true);
            this.drawArrow(drawContext, this.scWidth / 2, this.scHeight / 2, scaleProj);
        } else {
            this.renderMap(drawContext, mapX, mapY, scScale, scaleProj);
            this.drawDirections(drawContext, mapX, mapY, scaleProj);
            // Render Navigation Overlay für Minimap
            renderNavigationOverlay(drawContext, mapX, mapY, 64, (float) this.zoomScale, false);
            // Render NPCs auf Minimap
            renderNPCMarkers(drawContext, mapX, mapY, 64, (float) this.zoomScale, false);
            this.drawArrow(drawContext, mapX, mapY, scaleProj);
        }
    }

    /**
     * Rendert das Navigations-Overlay (Pfad zum Ziel) auf der Karte
     */
    private void renderNavigationOverlay(GuiGraphics graphics, int mapX, int mapY,
                                          int mapSize, float zoom, boolean fullscreen) {
        NavigationOverlay overlay = NavigationOverlay.getInstance();

        // Initialisiere falls nötig
        if (!overlay.isInitialized()) {
            var mapData = MapViewConstants.getLightMapInstance().getWorldMapData();
            if (mapData != null) {
                overlay.initialize(mapData);
            }
        }

        if (!overlay.isInitialized() || !overlay.isNavigating()) {
            return;
        }

        // Tick für Updates (Position, Pfad-Neuberechnung)
        overlay.tick();

        // Berechne Kartenrotation
        float rotation = 0;
        if (this.options.rotates && !fullscreen) {
            rotation = this.direction;
        }

        if (fullscreen) {
            // Für Fullscreen: Nutze pixelgenaue Positionierung
            // screenCenter = Bildschirmmitte, zoom = Pixel pro Block
            int screenCenterX = this.scWidth / 2;
            int screenCenterY = this.scHeight / 2;
            overlay.renderFullscreenAccurate(graphics, this.lastX, this.lastZ,
                    screenCenterX, screenCenterY, zoom);
        } else {
            // Für Minimap: Nutze pixelgenaue Positionierung
            // mapX, mapY = Bildschirmposition des Kartenzentrums
            // lastX, lastZ = Weltkoordinaten des Kartenzentrums (Spielerposition)
            // zoomScale: Bei 1.0 zeigt die 64px Minimap ca. 64 Blöcke (1 Block = 1 Pixel)
            // Bei 2.0: 1 Block = 2 Pixel, bei 0.5: 1 Block = 0.5 Pixel
            // zoom IS already pixels per block (zoomScale)
            float scale = zoom;
            overlay.renderMinimapAccurate(graphics, this.lastX, this.lastZ,
                    mapX, mapY, mapSize, scale, rotation);
        }
    }

    /**
     * Rendert NPC-Marker auf der Karte
     * Filtert automatisch Polizei-NPCs und NPCs auf Arbeit/Zuhause
     */
    private void renderNPCMarkers(GuiGraphics graphics, int mapX, int mapY,
                                   int mapSize, float zoom, boolean fullscreen) {
        // Berechne Kartenrotation (nur für Minimap)
        float rotation = 0;
        if (this.options.rotates && !fullscreen) {
            rotation = this.direction;
        }

        if (fullscreen) {
            npcMapRenderer.renderOnWorldmap(graphics, this.lastX, this.lastZ,
                    this.scWidth, this.scHeight, zoom, 0, 0);
        } else {
            npcMapRenderer.renderOnMinimap(graphics, this.lastX, this.lastZ,
                    mapSize, zoom, mapX, mapY, rotation);
        }
    }

    private void checkForChanges() {
        boolean changed = false;
        if (this.colorManager.checkForChanges()) {
            changed = true;
        }

        if (this.options.isChanged()) {
            if (this.options.filtering) {
                this.mapImages = this.mapImagesFiltered;
                this.mapResources = resourceMapImageFiltered;
            } else {
                this.mapImages = this.mapImagesUnfiltered;
                this.mapResources = resourceMapImageUnfiltered;
            }

            changed = true;
            this.setZoomScale();
        }

        if (changed) {
            this.doFullRender = true;
            MapViewConstants.getLightMapInstance().getSettingsAndLightingChangeNotifier().notifyOfChanges();
        }

    }

    private void mapCalc(boolean full) {
        int currentX = MinecraftAccessor.xCoord();
        int currentZ = MinecraftAccessor.zCoord();
        int currentY = MinecraftAccessor.yCoord();
        int offsetX = currentX - this.lastX;
        int offsetZ = currentZ - this.lastZ;
        int offsetY = currentY - this.lastY;
        int zoom = this.zoom;
        int multi = (int) Math.pow(2.0, zoom);
        ClientLevel world = this.world;
        boolean needHeightAndID;
        boolean needHeightMap = false;
        boolean needLight = false;
        boolean skyColorChanged = false;
        int skyColor = this.colorManager.getAirColor();
        if (this.lastSkyColor != skyColor) {
            skyColorChanged = true;
            this.lastSkyColor = skyColor;
        }

        if (this.options.lightmap) {
            int torchOffset = this.options.isRealTimeTorches() ? 8 : 0;
            for (int t = 0; t < 16; ++t) {
                int newValue = getLightmapColor(t, torchOffset);
                if (this.lastLightmapValues[t] != newValue) {
                    needLight = true;
                    this.lastLightmapValues[t] = newValue;
                }
            }
        }

        if (offsetY != 0) {
            ++this.heightMapFudge;
        } else if (this.heightMapFudge != 0) {
            ++this.heightMapFudge;
        }

        if (full || Math.abs(offsetY) >= this.heightMapResetHeight || this.heightMapFudge > this.heightMapResetTime) {
            if (this.lastY != currentY) {
                needHeightMap = true;
            }

            this.lastY = currentY;
            this.heightMapFudge = 0;
        }

        if (Math.abs(offsetX) > 32 * multi || Math.abs(offsetZ) > 32 * multi) {
            full = true;
        }

        boolean nether = false;
        boolean caves = false;
        needHeightAndID = false;
        int color24;
        synchronized (this.coordinateLock) {
            if (!full) {
                this.mapImages[zoom].moveY(offsetZ);
                this.mapImages[zoom].moveX(offsetX);
            }

            this.lastX = currentX;
            this.lastZ = currentZ;
        }
        int startX = currentX - 16 * multi;
        int startZ = currentZ - 16 * multi;
        if (!full) {
            this.mapData[zoom].moveZ(offsetZ);
            this.mapData[zoom].moveX(offsetX);

            // Optimized Y movement (N/S): recalculate new rows
            if (offsetZ != 0) {
                // Clear chunk cache for new recalculation
                cachedChunk = null;
                int startY, endY;
                if (offsetZ > 0) {
                    // Moved south: recalculate bottom rows
                    startY = 32 * multi - offsetZ;
                    endY = 32 * multi;
                } else {
                    // Moved north: recalculate top rows
                    startY = 0;
                    endY = -offsetZ;
                }
                for (int imageY = startY; imageY < endY; ++imageY) {
                    for (int imageX = 0; imageX < 32 * multi; ++imageX) {
                        color24 = this.getPixelColor(true, true, true, true, nether, caves, world, zoom, multi, startX, startZ, imageX, imageY);
                        this.mapImages[zoom].setRGB(imageX, imageY, color24);
                    }
                }
            }

            // Optimized X movement (E/W): recalculate new columns
            if (offsetX != 0) {
                // Clear chunk cache for new recalculation
                cachedChunk = null;
                int colStartX, colEndX;
                if (offsetX > 0) {
                    // Moved east: recalculate right columns
                    colStartX = 32 * multi - offsetX;
                    colEndX = 32 * multi;
                } else {
                    // Moved west: recalculate left columns
                    colStartX = 0;
                    colEndX = -offsetX;
                }
                for (int imageX = colStartX; imageX < colEndX; ++imageX) {
                    for (int imageY = 0; imageY < 32 * multi; ++imageY) {
                        color24 = this.getPixelColor(true, true, true, true, nether, caves, world, zoom, multi, startX, startZ, imageX, imageY);
                        this.mapImages[zoom].setRGB(imageX, imageY, color24);
                    }
                }
            }
        }

        if (full || this.options.heightmap && needHeightMap || needHeightAndID || this.options.lightmap && needLight || skyColorChanged) {
            for (int imageY = 32 * multi - 1; imageY >= 0; --imageY) {
                for (int imageX = 0; imageX < 32 * multi; ++imageX) {
                    color24 = this.getPixelColor(full, full || needHeightAndID, full, full || needLight || needHeightAndID, nether, caves, world, zoom, multi, startX, startZ, imageX, imageY);
                    this.mapImages[zoom].setRGB(imageX, imageY, color24);
                }
            }
        }

        // OPTIMIZED: Throttle biome segmentation - only run every 4 pixels of movement
        // Running on EVERY pixel movement was causing massive stuttering on N/S movement!
        if ((full || offsetX != 0 || offsetZ != 0 || !this.lastFullscreen) && this.fullscreenMap && this.options.biomeOverlay != 0) {
            biomeSegmentationCounter += Math.abs(offsetX) + Math.abs(offsetZ);
            if (full || !this.lastFullscreen || biomeSegmentationCounter >= 4) {
                this.mapData[zoom].segmentBiomes();
                this.mapData[zoom].findCenterOfSegments(!this.options.oldNorth);
                biomeSegmentationCounter = 0;
            }
        }

        this.lastFullscreen = this.fullscreenMap;
        if (full || offsetX != 0 || offsetZ != 0 || needHeightMap || needLight || skyColorChanged) {
            this.imageChanged = true;
        }

        if (needLight || skyColorChanged) {
            MapViewConstants.getLightMapInstance().getSettingsAndLightingChangeNotifier().notifyOfChanges();
        }

    }

    @Override
    public void handleChangeInWorld(int chunkX, int chunkZ) {
        try {
            this.chunkCache[this.zoom].registerChangeAt(chunkX, chunkZ);
        } catch (Exception e) {
            MapViewConstants.getLogger().warn(e);
        }
    }

    @Override
    public void processChunk(LevelChunk chunk) {
        this.rectangleCalc(chunk.getPos().x * 16, chunk.getPos().z * 16, chunk.getPos().x * 16 + 15, chunk.getPos().z * 16 + 15);
    }

    private void rectangleCalc(int left, int top, int right, int bottom) {
        boolean nether = false;
        boolean caves = false;

        int zoom = this.zoom;
        int startX = this.lastX;
        int startZ = this.lastZ;
        ClientLevel world = this.world;
        int multi = (int) Math.pow(2.0, zoom);
        startX -= 16 * multi;
        startZ -= 16 * multi;
        left = left - startX - 1;
        right = right - startX + 1;
        top = top - startZ - 1;
        bottom = bottom - startZ + 1;
        left = Math.max(0, left);
        right = Math.min(32 * multi - 1, right);
        top = Math.max(0, top);
        bottom = Math.min(32 * multi - 1, bottom);

        // Phase 2B: Use Strategy Pattern for flexible scanning algorithms
        // Replaces hardcoded nested loop with configurable strategy
        ChunkScanStrategy scanStrategy = ChunkScanStrategyFactory.getDefault();

        final int finalLeft = left;
        final int finalTop = top;
        final int finalRight = right;
        final int finalBottom = bottom;
        final int finalZoom = zoom;
        final int finalMulti = multi;
        final int finalStartX = startX;
        final int finalStartZ = startZ;
        final ClientLevel finalWorld = world;
        final boolean finalNether = nether;
        final boolean finalCaves = caves;

        scanStrategy.scan(finalLeft, finalTop, finalRight, finalBottom, (imageX, imageY) -> {
            int color24 = this.getPixelColor(true, true, true, true, finalNether, finalCaves,
                    finalWorld, finalZoom, finalMulti, finalStartX, finalStartZ, imageX, imageY);
            this.mapImages[finalZoom].setRGB(imageX, imageY, color24);
        });

        this.imageChanged = true;
    }

    private int getPixelColor(boolean needBiome, boolean needHeightAndID, boolean needTint, boolean needLight, boolean nether, boolean caves, ClientLevel world, int zoom, int multi, int startX, int startZ, int imageX, int imageY) {
        int surfaceHeight = Short.MIN_VALUE;
        int seafloorHeight = Short.MIN_VALUE;
        int transparentHeight = Short.MIN_VALUE;
        int foliageHeight = Short.MIN_VALUE;
        int surfaceColor;
        int seafloorColor = 0;
        int transparentColor = 0;
        int foliageColor = 0;
        this.surfaceBlockState = null;
        this.transparentBlockState = BlockDatabase.air.defaultBlockState();
        BlockState foliageBlockState = BlockDatabase.air.defaultBlockState();
        BlockState seafloorBlockState = BlockDatabase.air.defaultBlockState();
        boolean surfaceBlockChangeForcedTint = false;
        boolean transparentBlockChangeForcedTint = false;
        boolean foliageBlockChangeForcedTint = false;
        boolean seafloorBlockChangeForcedTint = false;
        int surfaceBlockStateID;
        int transparentBlockStateID;
        int foliageBlockStateID;
        int seafloorBlockStateID;
        MutableBlockPos blockPos = BlockPositionCache.get();
        MutableBlockPos tempBlockPos = BlockPositionCache.get();
        blockPos.withXYZ(startX + imageX, 64, startZ + imageY);
        int color24;
        Biome biome;
        if (needBiome) {
            biome = world.getBiome(blockPos).value();
            this.mapData[zoom].setBiome(imageX, imageY, biome);
        } else {
            biome = this.mapData[zoom].getBiome(imageX, imageY);
        }

        if (this.options.biomeOverlay == 1) {
            if (biome != null) {
                color24 = ARGBCompat.toABGR(BiomeColors.getBiomeColor(biome) | 0xFF000000);
            } else {
                color24 = 0;
            }

        } else {
            boolean solid = false;
            if (needHeightAndID) {
                if (!nether && !caves) {
                    // OPTIMIZED: Cache chunk lookups - chunks are 16x16, so we access the same chunk multiple times
                    int chunkX = blockPos.getX() >> 4;
                    int chunkZ = blockPos.getZ() >> 4;
                    if (cachedChunk == null || cachedChunkX != chunkX || cachedChunkZ != chunkZ) {
                        cachedChunk = world.getChunkAt(blockPos);
                        cachedChunkX = chunkX;
                        cachedChunkZ = chunkZ;
                    }
                    LevelChunk chunk = cachedChunk;
                    transparentHeight = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX() & 15, blockPos.getZ() & 15) + 1;
                    this.transparentBlockState = world.getBlockState(blockPos.withXYZ(startX + imageX, transparentHeight - 1, startZ + imageY));
                    FluidState fluidState = this.transparentBlockState.getFluidState();
                    if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                        this.transparentBlockState = fluidState.createLegacyBlock();
                    }

                    surfaceHeight = transparentHeight;
                    this.surfaceBlockState = this.transparentBlockState;
                    VoxelShape voxelShape;
                    tempBlockPos.setXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY);
                    boolean hasOpacity = this.surfaceBlockState.getLightBlock(world, tempBlockPos) > 0;
                    if (!hasOpacity && this.surfaceBlockState.canOcclude() && this.surfaceBlockState.useShapeForLightOcclusion()) {
                        voxelShape = this.surfaceBlockState.getFaceOcclusionShape(world, tempBlockPos, Direction.DOWN);
                        hasOpacity = Shapes.faceShapeOccludes(voxelShape, Shapes.empty());
                        voxelShape = this.surfaceBlockState.getFaceOcclusionShape(world, tempBlockPos, Direction.UP);
                        hasOpacity = hasOpacity || Shapes.faceShapeOccludes(Shapes.empty(), voxelShape);
                    }

                    while (!hasOpacity && surfaceHeight > world.getMinBuildHeight()) {
                        foliageBlockState = this.surfaceBlockState;
                        --surfaceHeight;
                        this.surfaceBlockState = world.getBlockState(blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                        fluidState = this.surfaceBlockState.getFluidState();
                        if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                            this.surfaceBlockState = fluidState.createLegacyBlock();
                        }

                        tempBlockPos.setXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY);
                        hasOpacity = this.surfaceBlockState.getLightBlock(world, tempBlockPos) > 0;
                        if (!hasOpacity && this.surfaceBlockState.canOcclude() && this.surfaceBlockState.useShapeForLightOcclusion()) {
                            voxelShape = this.surfaceBlockState.getFaceOcclusionShape(world, tempBlockPos, Direction.DOWN);
                            hasOpacity = Shapes.faceShapeOccludes(voxelShape, Shapes.empty());
                            voxelShape = this.surfaceBlockState.getFaceOcclusionShape(world, tempBlockPos, Direction.UP);
                            hasOpacity = hasOpacity || Shapes.faceShapeOccludes(Shapes.empty(), voxelShape);
                        }
                    }

                    if (surfaceHeight == transparentHeight) {
                        transparentHeight = Short.MIN_VALUE;
                        this.transparentBlockState = BlockDatabase.air.defaultBlockState();
                        foliageBlockState = world.getBlockState(blockPos.withXYZ(startX + imageX, surfaceHeight, startZ + imageY));
                    }

                    if (foliageBlockState.getBlock() == Blocks.SNOW) {
                        this.surfaceBlockState = foliageBlockState;
                        foliageBlockState = BlockDatabase.air.defaultBlockState();
                    }

                    if (foliageBlockState == this.transparentBlockState) {
                        foliageBlockState = BlockDatabase.air.defaultBlockState();
                    }

                    if (foliageBlockState != null && !(foliageBlockState.getBlock() instanceof AirBlock)) {
                        foliageHeight = surfaceHeight + 1;
                    } else {
                        foliageHeight = Short.MIN_VALUE;
                    }

                    Block material = this.surfaceBlockState.getBlock();
                    if (material == Blocks.WATER || material == Blocks.ICE) {
                        seafloorHeight = surfaceHeight;

                        for (seafloorBlockState = world.getBlockState(blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY)); seafloorBlockState.getLightBlock(world, blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY)) < 5 && !(seafloorBlockState.getBlock() instanceof LeavesBlock)
                                && seafloorHeight > world.getMinBuildHeight() + 1; seafloorBlockState = world.getBlockState(blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY))) {
                            material = seafloorBlockState.getBlock();
                            if (transparentHeight == Short.MIN_VALUE && material != Blocks.ICE && material != Blocks.WATER && Heightmap.Types.MOTION_BLOCKING.isOpaque().test(seafloorBlockState)) {
                                transparentHeight = seafloorHeight;
                                this.transparentBlockState = seafloorBlockState;
                            }

                            if (foliageHeight == Short.MIN_VALUE && seafloorHeight != transparentHeight && this.transparentBlockState != seafloorBlockState && material != Blocks.ICE && material != Blocks.WATER && !(material instanceof AirBlock) && material != Blocks.BUBBLE_COLUMN) {
                                foliageHeight = seafloorHeight;
                                foliageBlockState = seafloorBlockState;
                            }

                            --seafloorHeight;
                        }

                        if (seafloorBlockState.getBlock() == Blocks.WATER) {
                            seafloorBlockState = BlockDatabase.air.defaultBlockState();
                        }
                    }
                } else {
                    surfaceHeight = this.getNetherHeight(startX + imageX, startZ + imageY);
                    this.surfaceBlockState = world.getBlockState(blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                    surfaceBlockStateID = BlockDatabase.getStateId(this.surfaceBlockState);
                    foliageHeight = surfaceHeight + 1;
                    blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
                    foliageBlockState = world.getBlockState(blockPos);
                    Block material = foliageBlockState.getBlock();
                    if (material != Blocks.SNOW && !(material instanceof AirBlock) && material != Blocks.LAVA && material != Blocks.WATER) {
                        foliageBlockStateID = BlockDatabase.getStateId(foliageBlockState);
                    } else {
                        foliageHeight = Short.MIN_VALUE;
                    }
                }

                surfaceBlockStateID = BlockDatabase.getStateId(this.surfaceBlockState);
                if (this.options.biomes && this.surfaceBlockState != this.mapData[zoom].getBlockstate(imageX, imageY)) {
                    surfaceBlockChangeForcedTint = true;
                }

                this.mapData[zoom].setHeight(imageX, imageY, surfaceHeight);
                this.mapData[zoom].setBlockstateID(imageX, imageY, surfaceBlockStateID);
                if (this.options.biomes && this.transparentBlockState != this.mapData[zoom].getTransparentBlockstate(imageX, imageY)) {
                    transparentBlockChangeForcedTint = true;
                }

                this.mapData[zoom].setTransparentHeight(imageX, imageY, transparentHeight);
                transparentBlockStateID = BlockDatabase.getStateId(this.transparentBlockState);
                this.mapData[zoom].setTransparentBlockstateID(imageX, imageY, transparentBlockStateID);
                if (this.options.biomes && foliageBlockState != this.mapData[zoom].getFoliageBlockstate(imageX, imageY)) {
                    foliageBlockChangeForcedTint = true;
                }

                this.mapData[zoom].setFoliageHeight(imageX, imageY, foliageHeight);
                foliageBlockStateID = BlockDatabase.getStateId(foliageBlockState);
                this.mapData[zoom].setFoliageBlockstateID(imageX, imageY, foliageBlockStateID);
                if (this.options.biomes && seafloorBlockState != this.mapData[zoom].getOceanFloorBlockstate(imageX, imageY)) {
                    seafloorBlockChangeForcedTint = true;
                }

                this.mapData[zoom].setOceanFloorHeight(imageX, imageY, seafloorHeight);
                seafloorBlockStateID = BlockDatabase.getStateId(seafloorBlockState);
                this.mapData[zoom].setOceanFloorBlockstateID(imageX, imageY, seafloorBlockStateID);
            } else {
                surfaceHeight = this.mapData[zoom].getHeight(imageX, imageY);
                surfaceBlockStateID = this.mapData[zoom].getBlockstateID(imageX, imageY);
                this.surfaceBlockState = BlockDatabase.getStateById(surfaceBlockStateID);
                transparentHeight = this.mapData[zoom].getTransparentHeight(imageX, imageY);
                transparentBlockStateID = this.mapData[zoom].getTransparentBlockstateID(imageX, imageY);
                this.transparentBlockState = BlockDatabase.getStateById(transparentBlockStateID);
                foliageHeight = this.mapData[zoom].getFoliageHeight(imageX, imageY);
                foliageBlockStateID = this.mapData[zoom].getFoliageBlockstateID(imageX, imageY);
                foliageBlockState = BlockDatabase.getStateById(foliageBlockStateID);
                seafloorHeight = this.mapData[zoom].getOceanFloorHeight(imageX, imageY);
                seafloorBlockStateID = this.mapData[zoom].getOceanFloorBlockstateID(imageX, imageY);
                seafloorBlockState = BlockDatabase.getStateById(seafloorBlockStateID);
            }

            if (surfaceHeight == Short.MIN_VALUE) {
                surfaceHeight = this.lastY + 1;
                solid = true;
            }

            if (this.surfaceBlockState.getBlock() == Blocks.LAVA) {
                solid = false;
            }

            if (this.options.biomes) {
                surfaceColor = this.colorManager.getBlockColor(blockPos, surfaceBlockStateID, biome);
                int tint;
                if (!needTint && !surfaceBlockChangeForcedTint) {
                    tint = this.mapData[zoom].getBiomeTint(imageX, imageY);
                } else {
                    blockPos.setXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY);
                    tint = this.colorManager.getBiomeTint(this.mapData[zoom], world, this.surfaceBlockState, surfaceBlockStateID, blockPos, tempBlockPos, startX, startZ);
                    this.mapData[zoom].setBiomeTint(imageX, imageY, tint);
                }

                if (tint != -1) {
                    surfaceColor = ColorUtils.colorMultiplier(surfaceColor, tint);
                }
            } else {
                surfaceColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, surfaceBlockStateID);
            }

            surfaceColor = this.applyHeight(surfaceColor, nether, caves, world, zoom, multi, startX, startZ, imageX, imageY, surfaceHeight, solid, 1);
            int light;
            if (needLight) {
                light = this.getLight(surfaceColor, this.surfaceBlockState, world, startX + imageX, startZ + imageY, surfaceHeight, solid);
                this.mapData[zoom].setLight(imageX, imageY, light);
            } else {
                light = this.mapData[zoom].getLight(imageX, imageY);
            }

            if (light == 0) {
                surfaceColor = 0;
            } else if (light != 255) {
                surfaceColor = ColorUtils.colorMultiplier(surfaceColor, light);
            }

            if (this.options.waterTransparency && seafloorHeight != Short.MIN_VALUE) {
                if (!this.options.biomes) {
                    seafloorColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, seafloorBlockStateID);
                } else {
                    seafloorColor = this.colorManager.getBlockColor(blockPos, seafloorBlockStateID, biome);
                    int tint;
                    if (!needTint && !seafloorBlockChangeForcedTint) {
                        tint = this.mapData[zoom].getOceanFloorBiomeTint(imageX, imageY);
                    } else {
                        blockPos.setXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY);
                        tint = this.colorManager.getBiomeTint(this.mapData[zoom], world, seafloorBlockState, seafloorBlockStateID, blockPos, tempBlockPos, startX, startZ);
                        this.mapData[zoom].setOceanFloorBiomeTint(imageX, imageY, tint);
                    }

                    if (tint != -1) {
                        seafloorColor = ColorUtils.colorMultiplier(seafloorColor, tint);
                    }
                }

                seafloorColor = this.applyHeight(seafloorColor, nether, caves, world, zoom, multi, startX, startZ, imageX, imageY, seafloorHeight, solid, 0);
                int seafloorLight;
                if (needLight) {
                    seafloorLight = this.getLight(seafloorColor, seafloorBlockState, world, startX + imageX, startZ + imageY, seafloorHeight, solid);
                    blockPos.setXYZ(startX + imageX, seafloorHeight, startZ + imageY);
                    BlockState blockStateAbove = world.getBlockState(blockPos);
                    Block materialAbove = blockStateAbove.getBlock();
                    if (this.options.lightmap && materialAbove == Blocks.ICE) {
                        int multiplier = minecraft.options.ambientOcclusion().get() ? 200 : 120;
                        seafloorLight = ColorUtils.colorMultiplier(seafloorLight, 0xFF000000 | multiplier << 16 | multiplier << 8 | multiplier);
                    }

                    this.mapData[zoom].setOceanFloorLight(imageX, imageY, seafloorLight);
                } else {
                    seafloorLight = this.mapData[zoom].getOceanFloorLight(imageX, imageY);
                }

                if (seafloorLight == 0) {
                    seafloorColor = 0;
                } else if (seafloorLight != 255) {
                    seafloorColor = ColorUtils.colorMultiplier(seafloorColor, seafloorLight);
                }
            }

            if (this.options.blockTransparency) {
                if (transparentHeight != Short.MIN_VALUE && this.transparentBlockState != null && this.transparentBlockState != BlockDatabase.air.defaultBlockState()) {
                    if (this.options.biomes) {
                        transparentColor = this.colorManager.getBlockColor(blockPos, transparentBlockStateID, biome);
                        int tint;
                        if (!needTint && !transparentBlockChangeForcedTint) {
                            tint = this.mapData[zoom].getTransparentBiomeTint(imageX, imageY);
                        } else {
                            blockPos.setXYZ(startX + imageX, transparentHeight - 1, startZ + imageY);
                            tint = this.colorManager.getBiomeTint(this.mapData[zoom], world, this.transparentBlockState, transparentBlockStateID, blockPos, tempBlockPos, startX, startZ);
                            this.mapData[zoom].setTransparentBiomeTint(imageX, imageY, tint);
                        }

                        if (tint != -1) {
                            transparentColor = ColorUtils.colorMultiplier(transparentColor, tint);
                        }
                    } else {
                        transparentColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, transparentBlockStateID);
                    }

                    transparentColor = this.applyHeight(transparentColor, nether, caves, world, zoom, multi, startX, startZ, imageX, imageY, transparentHeight, solid, 3);
                    int transparentLight;
                    if (needLight) {
                        transparentLight = this.getLight(transparentColor, this.transparentBlockState, world, startX + imageX, startZ + imageY, transparentHeight, solid);
                        this.mapData[zoom].setTransparentLight(imageX, imageY, transparentLight);
                    } else {
                        transparentLight = this.mapData[zoom].getTransparentLight(imageX, imageY);
                    }

                    if (transparentLight == 0) {
                        transparentColor = 0;
                    } else if (transparentLight != 255) {
                        transparentColor = ColorUtils.colorMultiplier(transparentColor, transparentLight);
                    }
                }

                if (foliageHeight != Short.MIN_VALUE && foliageBlockState != null && foliageBlockState != BlockDatabase.air.defaultBlockState()) {
                    if (!this.options.biomes) {
                        foliageColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, foliageBlockStateID);
                    } else {
                        foliageColor = this.colorManager.getBlockColor(blockPos, foliageBlockStateID, biome);
                        int tint;
                        if (!needTint && !foliageBlockChangeForcedTint) {
                            tint = this.mapData[zoom].getFoliageBiomeTint(imageX, imageY);
                        } else {
                            blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
                            tint = this.colorManager.getBiomeTint(this.mapData[zoom], world, foliageBlockState, foliageBlockStateID, blockPos, tempBlockPos, startX, startZ);
                            this.mapData[zoom].setFoliageBiomeTint(imageX, imageY, tint);
                        }

                        if (tint != -1) {
                            foliageColor = ColorUtils.colorMultiplier(foliageColor, tint);
                        }
                    }

                    foliageColor = this.applyHeight(foliageColor, nether, caves, world, zoom, multi, startX, startZ, imageX, imageY, foliageHeight, solid, 2);
                    int foliageLight;
                    if (needLight) {
                        foliageLight = this.getLight(foliageColor, foliageBlockState, world, startX + imageX, startZ + imageY, foliageHeight, solid);
                        this.mapData[zoom].setFoliageLight(imageX, imageY, foliageLight);
                    } else {
                        foliageLight = this.mapData[zoom].getFoliageLight(imageX, imageY);
                    }

                    if (foliageLight == 0) {
                        foliageColor = 0;
                    } else if (foliageLight != 255) {
                        foliageColor = ColorUtils.colorMultiplier(foliageColor, foliageLight);
                    }
                }
            }

            if (seafloorColor != 0 && seafloorHeight > Short.MIN_VALUE) {
                color24 = seafloorColor;
                if (foliageColor != 0 && foliageHeight <= surfaceHeight) {
                    color24 = ColorUtils.colorAdder(foliageColor, seafloorColor);
                }

                if (transparentColor != 0 && transparentHeight <= surfaceHeight) {
                    color24 = ColorUtils.colorAdder(transparentColor, color24);
                }

                color24 = ColorUtils.colorAdder(surfaceColor, color24);
            } else {
                color24 = surfaceColor;
            }

            if (foliageColor != 0 && foliageHeight > surfaceHeight) {
                color24 = ColorUtils.colorAdder(foliageColor, color24);
            }

            if (transparentColor != 0 && transparentHeight > surfaceHeight) {
                color24 = ColorUtils.colorAdder(transparentColor, color24);
            }

            if (this.options.biomeOverlay == 2) {
                int bc = 0;
                if (biome != null) {
                    bc = ARGBCompat.toABGR(BiomeColors.getBiomeColor(biome));
                }

                bc = 2130706432 | bc;
                color24 = ColorUtils.colorAdder(bc, color24);
            }

        }
        BlockPositionCache.release(blockPos);
        BlockPositionCache.release(tempBlockPos);
        // ColorUtils methods output ARGB format, convert to ABGR for NativeImage
        return MapViewHelper.doSlimeAndGrid(ARGBCompat.toABGR(color24), world, startX + imageX, startZ + imageY);
    }

    private int getBlockHeight(boolean nether, boolean caves, Level world, int x, int z) {
        MutableBlockPos blockPos = BlockPositionCache.get();
        int playerHeight = MinecraftAccessor.yCoord();
        blockPos.setXYZ(x, playerHeight, z);
        LevelChunk chunk = (LevelChunk) world.getChunk(blockPos);
        int height = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX() & 15, blockPos.getZ() & 15) + 1;
        BlockState blockState = world.getBlockState(blockPos.withXYZ(x, height - 1, z));
        FluidState fluidState = this.transparentBlockState.getFluidState();
        if (fluidState != Fluids.EMPTY.defaultFluidState()) {
            blockState = fluidState.createLegacyBlock();
        }

        while (blockState.getLightBlock(world, blockPos.withXYZ(x, height - 1, z)) == 0 && height > world.getMinBuildHeight()) {
            --height;
            blockState = world.getBlockState(blockPos.withXYZ(x, height - 1, z));
            fluidState = this.surfaceBlockState.getFluidState();
            if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                blockState = fluidState.createLegacyBlock();
            }
        }
        BlockPositionCache.release(blockPos);
        return (nether || caves) && height > playerHeight ? this.getNetherHeight(x, z) : height;
    }

    private int getNetherHeight(int x, int z) {
        MutableBlockPos blockPos = BlockPositionCache.get();
        int y = this.lastY;
        blockPos.setXYZ(x, y, z);
        BlockState blockState = this.world.getBlockState(blockPos);
        if (blockState.getLightBlock(this.world, blockPos) == 0 && blockState.getBlock() != Blocks.LAVA) {
            while (y > world.getMinBuildHeight()) {
                --y;
                blockPos.setXYZ(x, y, z);
                blockState = this.world.getBlockState(blockPos);
                if (blockState.getLightBlock(this.world, blockPos) > 0 || blockState.getBlock() == Blocks.LAVA) {
                    BlockPositionCache.release(blockPos);
                    return y + 1;
                }
            }
            BlockPositionCache.release(blockPos);
            return y;
        } else {
            while (y <= this.lastY + 10 && y < world.getMaxBuildHeight()) {
                ++y;
                blockPos.setXYZ(x, y, z);
                blockState = this.world.getBlockState(blockPos);
                if (blockState.getLightBlock(this.world, blockPos) == 0 && blockState.getBlock() != Blocks.LAVA) {
                    BlockPositionCache.release(blockPos);
                    return y;
                }
            }
            BlockPositionCache.release(blockPos);
            return this.world.getMinBuildHeight() - 1;
        }
    }

    private int getSeafloorHeight(Level world, int x, int z, int height) {
        MutableBlockPos blockPos = BlockPositionCache.get();
        for (BlockState blockState = world.getBlockState(blockPos.withXYZ(x, height - 1, z)); blockState.getLightBlock(world, blockPos.withXYZ(x, height - 1, z)) < 5 && !(blockState.getBlock() instanceof LeavesBlock) && height > world.getMinBuildHeight() + 1; blockState = world.getBlockState(blockPos.withXYZ(x, height - 1, z))) {
            --height;
        }
        BlockPositionCache.release(blockPos);
        return height;
    }

    private int getTransparentHeight(boolean nether, boolean caves, Level world, int x, int z, int height) {
        MutableBlockPos blockPos = BlockPositionCache.get();
        int transHeight;
        if (!caves && !nether) {
            transHeight = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.withXYZ(x, height, z)).getY();
            if (transHeight <= height) {
                transHeight = Short.MIN_VALUE;
            }
        } else {
            transHeight = Short.MIN_VALUE;
        }

        BlockState blockState = world.getBlockState(blockPos.withXYZ(x, transHeight - 1, z));
        Block material = blockState.getBlock();
        if (transHeight == height + 1 && material == Blocks.SNOW) {
            transHeight = Short.MIN_VALUE;
        }

        if (material == Blocks.BARRIER) {
            ++transHeight;
            blockState = world.getBlockState(blockPos.withXYZ(x, transHeight - 1, z));
            material = blockState.getBlock();
            if (material instanceof AirBlock) {
                transHeight = Short.MIN_VALUE;
            }
        }
        BlockPositionCache.release(blockPos);
        return transHeight;
    }

    private int applyHeight(int color24, boolean nether, boolean caves, Level world, int zoom, int multi, int startX, int startZ, int imageX, int imageY, int height, boolean solid, int layer) {
        if (color24 != this.colorManager.getAirColor() && color24 != 0 && (this.options.heightmap || this.options.slopemap) && !solid) {
            int heightComp = -1;
            int diff;
            double sc = 0.0;
            if (!this.options.slopemap) {
                diff = height - this.lastY;
                sc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 1.8;
                if (diff < 0) {
                    sc = 0.0 - sc;
                }
            } else {
                if (imageX > 0 && imageY < 32 * multi - 1) {
                    if (layer == 0) {
                        heightComp = this.mapData[zoom].getOceanFloorHeight(imageX - 1, imageY + 1);
                    }

                    if (layer == 1) {
                        heightComp = this.mapData[zoom].getHeight(imageX - 1, imageY + 1);
                    }

                    if (layer == 2) {
                        heightComp = height;
                    }

                    if (layer == 3) {
                        heightComp = this.mapData[zoom].getTransparentHeight(imageX - 1, imageY + 1);
                        if (heightComp == Short.MIN_VALUE) {
                            Block block = BlockDatabase.getStateById(this.mapData[zoom].getTransparentBlockstateID(imageX, imageY)).getBlock();
                            if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
                                heightComp = this.mapData[zoom].getHeight(imageX - 1, imageY + 1);
                            }
                        }
                    }
                } else {
                    if (layer == 0) {
                        int baseHeight = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                        heightComp = this.getSeafloorHeight(world, startX + imageX - 1, startZ + imageY + 1, baseHeight);
                    }

                    if (layer == 1) {
                        heightComp = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                    }

                    if (layer == 2) {
                        heightComp = height;
                    }

                    if (layer == 3) {
                        int baseHeight = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                        heightComp = this.getTransparentHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1, baseHeight);
                        if (heightComp == Short.MIN_VALUE) {
                            MutableBlockPos blockPos = BlockPositionCache.get();
                            BlockState blockState = world.getBlockState(blockPos.withXYZ(startX + imageX, height - 1, startZ + imageY));
                            BlockPositionCache.release(blockPos);
                            Block block = blockState.getBlock();
                            if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
                                heightComp = baseHeight;
                            }
                        }
                    }
                }

                if (heightComp == Short.MIN_VALUE) {
                    heightComp = height;
                }

                diff = heightComp - height;
                if (diff != 0) {
                    sc = diff > 0 ? 1.0 : -1.0;
                    sc /= 8.0;
                }

                if (this.options.heightmap) {
                    diff = height - this.lastY;
                    double heightsc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 3.0;
                    sc = diff > 0 ? sc + heightsc : sc - heightsc;
                }
            }

            int alpha = color24 >> 24 & 0xFF;
            int r = color24 >> 16 & 0xFF;
            int g = color24 >> 8 & 0xFF;
            int b = color24 & 0xFF;
            if (sc > 0.0) {
                r += (int) (sc * (255 - r));
                g += (int) (sc * (255 - g));
                b += (int) (sc * (255 - b));
            } else if (sc < 0.0) {
                sc = Math.abs(sc);
                r -= (int) (sc * r);
                g -= (int) (sc * g);
                b -= (int) (sc * b);
            }

            color24 = alpha * 16777216 + r * 65536 + g * 256 + b;
        }

        return color24;
    }

    private int getLight(int color24, BlockState blockState, Level world, int x, int z, int height, boolean solid) {
        int combinedLight = 0xffffffff;
        if (solid) {
            combinedLight = 0;
        } else if (color24 != this.colorManager.getAirColor() && color24 != 0 && this.options.lightmap) {
            MutableBlockPos blockPos = BlockPositionCache.get();
            blockPos.setXYZ(x, Math.max(Math.min(height, world.getMaxBuildHeight()), world.getMinBuildHeight()), z);
            int blockLight = world.getBrightness(LightLayer.BLOCK, blockPos);
            int skyLight = world.getBrightness(LightLayer.SKY, blockPos);
            if (blockState.getBlock() == Blocks.LAVA || blockState.getBlock() == Blocks.MAGMA_BLOCK) {
                blockLight = 14;
            }
            BlockPositionCache.release(blockPos);
            combinedLight = getLightmapColor(skyLight, blockLight);
        }

        return ARGBCompat.toABGR(combinedLight);
    }

    private void renderMap(GuiGraphics guiGraphics, int x, int y, int scScale, float scaleProj) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scaleProj, scaleProj, 1.0f);

        float scale = 1.0F;
        if (this.options.squareMap && this.options.rotates) {
            scale = 1.4142F;
        }

        synchronized (this.coordinateLock) {
            if (this.imageChanged) {
                this.imageChanged = false;
                this.mapImages[this.zoom].upload();
                this.lastImageX = this.lastX;
                this.lastImageZ = this.lastZ;
            }
        }
        //
        float multi = (float) (1.0 / this.zoomScale);
        this.percentX = (float) (MinecraftAccessor.xCoordDouble() - this.lastImageX);
        this.percentY = (float) (MinecraftAccessor.zCoordDouble() - this.lastImageZ);
        this.percentX *= multi;
        this.percentY *= multi;

        // Render the minimap texture using 1.20.1 GuiGraphics API
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0f);

        // Apply rotation based on map settings
        if (!this.options.rotates) {
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-this.northRotate));
        } else {
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(this.direction));
        }

        // Apply scaling for square maps
        if (this.options.squareMap && this.options.rotates) {
            guiGraphics.pose().scale(scale, scale, 1.0f);
        }

        // Get the texture size for the current zoom level
        // Zoom 0: 32x32, Zoom 1: 64x64, Zoom 2: 128x128, Zoom 3: 256x256, Zoom 4: 512x512
        int textureSize = 32 * (int) Math.pow(2.0, this.zoom);
        int halfTextureSize = textureSize / 2;

        // Scale the texture down to 64x64 for the minimap
        float textureScale = 64.0f / textureSize;
        guiGraphics.pose().scale(textureScale, textureScale, 1.0f);

        // Apply offset based on player movement within the map
        // percentX/Y are in map coordinates, multiply by textureSize/64 to convert to texture pixels
        // CRITICAL FIX: Both X and Y need NEGATIVE sign for correct direction!
        float offsetMultiplier = textureSize / 64.0F;
        guiGraphics.pose().translate(-this.percentX * offsetMultiplier, -this.percentY * offsetMultiplier, 0.0f);

        // Render the full map texture, which will be scaled to 64x64 by the transforms above
        guiGraphics.blit(mapResources[this.zoom], -halfTextureSize, -halfTextureSize, 0, 0, textureSize, textureSize, textureSize, textureSize);

        guiGraphics.pose().popPose();

        // This entire section from lines 1594-1649 needs to be rewritten for 1.20.1 rendering APIs
        // Commenting out for now to achieve compilation
        /*
        ProjectionType originalProjectionType = RenderSystem.getProjectionType();
        GpuBufferSlice originalProjectionMatrix = RenderSystem.getProjectionMatrixBuffer();
        RenderSystem.setProjectionMatrix(projection.getBuffer(), ProjectionType.ORTHOGRAPHIC);
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();

        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                        new Vector3f(),
                        new Matrix4f());

        Object renderPipeline = MapViewPipelines.GUI_TEXTURED_ANY_DEPTH_PIPELINE;
        try (MeshData meshData = bufferBuilder.build()) {
            GpuBuffer vertexBuffer = null;
            GpuBuffer indexBuffer;
            VertexFormat.IndexType indexType;
            if (meshData.indexBuffer() == null) {
                RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(meshData.drawState().mode());
                indexBuffer = autoStorageIndexBuffer.getBuffer(meshData.drawState().indexCount());
                indexType = autoStorageIndexBuffer.type();
            } else {
                indexBuffer = null;
                indexType = meshData.drawState().indexType();
            }

            AbstractTexture stencilTexture = null;
            if (this.options.squareMap) {
                stencilTexture = Minecraft.getInstance().getTextureManager().getTexture(squareStencil);
            } else {
                stencilTexture = Minecraft.getInstance().getTextureManager().getTexture(circleStencil);
            }

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "MapDataManager: MapViewRenderer to screen", fboTextureView, OptionalInt.of(0x00000000))) {
                renderPass.setPipeline(renderPipeline);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.setIndexBuffer(indexBuffer, indexType);

                renderPass.drawIndexed(0, 0, meshData.drawState().indexCount() / 2, 1);
                renderPass.setPipeline(MapViewPipelines.GUI_TEXTURED_ANY_DEPTH_DST_ALPHA_PIPELINE);

                renderPass.drawIndexed(0, meshData.drawState().indexCount() / 2, meshData.drawState().indexCount() / 2, 1);
            }
        }
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.setProjectionMatrix(originalProjectionMatrix, originalProjectionType);
        fboTessellator.clear();
        */

        double guiScale = (double) minecraft.getWindow().getWidth() / this.scWidth;
        minTablistOffset = guiScale * 63;
        this.drawMapFrame(guiGraphics, x, y, this.options.squareMap);
        guiGraphics.pose().popPose();
    }

    private void drawArrow(GuiGraphics guiGraphics, int x, int y, float scaleProj) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scaleProj, scaleProj, 1.0f);

        guiGraphics.pose().translate(x, y, 0.0f);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(this.options.rotates && !this.fullscreenMap ? 0.0F : this.direction + this.northRotate));
        guiGraphics.pose().translate(-x, -y, 0.0f);


        guiGraphics.blit(resourceArrow, x - 4, y - 4, 0, 0, 8, 8, 8, 8);

        guiGraphics.pose().popPose();
    }

    private void renderMapFull(GuiGraphics guiGraphics, int scWidth, int scHeight, float scaleProj) {
        synchronized (this.coordinateLock) {
            if (this.imageChanged) {
                this.imageChanged = false;
                this.mapImages[this.zoom].upload();
                this.lastImageX = this.lastX;
                this.lastImageZ = this.lastZ;
            }
        }
        PoseStack matrixStack = guiGraphics.pose();
        matrixStack.pushPose();
        matrixStack.scale(scaleProj, scaleProj, 1.0f);
        matrixStack.translate(scWidth / 2.0F, scHeight / 2.0F, 0.0f);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(this.northRotate));
        matrixStack.translate(-(scWidth / 2.0F), -(scHeight / 2.0F), 0.0f);

        // Get the texture size for the current zoom level
        int textureSize = 32 * (int) Math.pow(2.0, this.zoom);
        int halfTextureSize = textureSize / 2;
        int left = scWidth / 2 - halfTextureSize;
        int top = scHeight / 2 - halfTextureSize;

        guiGraphics.blit(mapResources[this.zoom], left, top, 0, 0, textureSize, textureSize, textureSize, textureSize);
        matrixStack.popPose();

        if (this.options.biomeOverlay != 0) {
            double factor = Math.pow(2.0, 3 - this.zoom);
            int minimumSize = (int) Math.pow(2.0, this.zoom);
            minimumSize *= minimumSize;
            ArrayList<AbstractMapData.BiomeLabel> labels = this.mapData[this.zoom].getBiomeLabels();
            matrixStack.pushPose();

            for (AbstractMapData.BiomeLabel o : labels) {
                if (o.segmentSize > minimumSize) {
                    String name = o.name;
                    int nameWidth = this.textWidth(name);
                    float x = (float) (o.x * factor);
                    float z = (float) (o.z * factor);
                    if (this.options.oldNorth) {
                        this.write(guiGraphics, name, (left + textureSize) - z - (nameWidth / 2f), top + x - 3.0F, 0xFFFFFFFF);
                    } else {
                        this.write(guiGraphics, name, left + x - (nameWidth / 2f), top + z - 3.0F, 0xFFFFFFFF);
                    }
                }
            }

            matrixStack.popPose();
        }
    }

    private void drawMapFrame(GuiGraphics guiGraphics, int x, int y, boolean squaremap) {
        ResourceLocation frameResource = squaremap ? resourceSquareMap : resourceRoundMap;
        guiGraphics.blit(frameResource, x - 32, y - 32, 0, 0, 64, 64, 64, 64);
    }

    private void drawDirections(GuiGraphics drawContext, int x, int y, float scaleProj) {
        PoseStack poseStack = drawContext.pose();
        boolean unicode = minecraft.options.forceUnicodeFont().get();
        float scale = unicode ? 0.65F : 0.5F;
        float rotate;
        if (this.options.rotates) {
            rotate = -this.direction - 90.0F - this.northRotate;
        } else {
            rotate = -90.0F;
        }

        float distance;
        if (this.options.squareMap) {
            if (this.options.rotates) {
                float tempdir = this.direction % 90.0F;
                tempdir = 45.0F - Math.abs(45.0F - tempdir);
                distance = (float) (33.5 / scale / Math.cos(Math.toRadians(tempdir)));
            } else {
                distance = 33.5F / scale;
            }
        } else {
            distance = 32.0F / scale;
        }

        poseStack.pushPose();
        poseStack.scale(scaleProj, scaleProj, 1.0f);
        poseStack.scale(scale, scale, 1.0f);

        poseStack.pushPose();
        poseStack.translate((float) (distance * Math.sin(Math.toRadians(-(rotate - 90.0)))), (float) (distance * Math.cos(Math.toRadians(-(rotate - 90.0)))), 0.0f);
        this.write(drawContext, "N", x / scale - 2.0F, y / scale - 4.0F, 0xFFFFFFFF);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate((float) (distance * Math.sin(Math.toRadians(-rotate))), (float) (distance * Math.cos(Math.toRadians(-rotate))), 0.0f);
        this.write(drawContext, "E", x / scale - 2.0F, y / scale - 4.0F, 0xFFFFFFFF);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate((float) (distance * Math.sin(Math.toRadians(-(rotate + 90.0)))), (float) (distance * Math.cos(Math.toRadians(-(rotate + 90.0)))), 0.0f);
        this.write(drawContext, "S", x / scale - 2.0F, y / scale - 4.0F, 0xFFFFFFFF);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate((float) (distance * Math.sin(Math.toRadians(-(rotate + 180.0)))), (float) (distance * Math.cos(Math.toRadians(-(rotate + 180.0)))), 0.0f);
        this.write(drawContext, "W", x / scale - 2.0F, y / scale - 4.0F, 0xFFFFFFFF);
        poseStack.popPose();

        poseStack.popPose();
    }

    private int textWidth(String string) {
        return minecraft.font.width(string);
    }

    private void write(GuiGraphics drawContext, String text, float x, float y, int color) {
        write(drawContext, Component.nullToEmpty(text), x, y, color);
    }

    private int textWidth(Component text) {
        return minecraft.font.width(text);
    }

    private void write(GuiGraphics drawContext, Component text, float x, float y, int color) {
        drawContext.drawString(minecraft.font, text, (int) x, (int) y, color);
    }

    public static double getMinTablistOffset() {
        return minTablistOffset;
    }

    public static float getStatusIconOffset() {
        return statusIconOffset;
    }
}
