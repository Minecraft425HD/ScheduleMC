package de.rolandsw.schedulemc.mapview.presentation.renderer;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.data.cache.BlockPositionCache;
import de.rolandsw.schedulemc.mapview.integration.minecraft.MinecraftAccessor;
import de.rolandsw.schedulemc.mapview.service.render.ColorCalculationService;
import de.rolandsw.schedulemc.mapview.service.render.LightingCalculator;
import de.rolandsw.schedulemc.mapview.util.ARGBCompat;
import de.rolandsw.schedulemc.mapview.util.MutableBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.biome.Biome;

/**
 * Manages lightmap and sky-color state for the map renderer.
 * Extracted from {@link MapViewRenderer} to separate lighting concerns.
 */
public class MapLightingState {

    private final Minecraft minecraft = Minecraft.getInstance();
    private final MapViewConfiguration options;
    private final ColorCalculationService colorManager;

    private ClientLevel world;

    // SICHERHEIT: volatile für Thread-Safety zwischen Render und Game Thread
    private volatile boolean needLightmapRefresh = true;
    private volatile int tickWithLightChange = -1;
    private volatile boolean lastPaused = true;
    private double lastGamma;
    private float lastSunBrightness;
    private float lastLightning;
    private float lastPotion;
    private final int[] lastLightmapValues = {
        -16777216, -16777216, -16777216, -16777216,
        -16777216, -16777216, -16777216, -16777216,
        -16777216, -16777216, -16777216, -16777216,
        -16777216, -16777216, -16777216, -16777216
    };
    private boolean needSkyColor;
    private boolean lastAboveHorizon = true;
    private int lastBiome;
    private final int[] lightmapColors = new int[256];
    // PERFORMANCE: Cache biome registry reference (avoid registryAccess().registryOrThrow() per frame)
    private net.minecraft.core.Registry<Biome> cachedBiomeRegistry;

    public MapLightingState(MapViewConfiguration options, ColorCalculationService colorManager) {
        this.options = options;
        this.colorManager = colorManager;
    }

    /** Called when the world changes to invalidate caches. */
    public void setWorld(ClientLevel world) {
        this.world = world;
        this.cachedBiomeRegistry = null;
    }

    /**
     * Updates the lightmap colors and sky color based on current environment.
     * Must be called once per tick. {@code timer} is the renderer's tick counter
     * used for periodic forced updates.
     */
    public void calculateCurrentLightAndSkyColor(int timer) {
        if (this.world == null || this.colorManager == null || MapViewConstants.getPlayer() == null) return;
        try {
            if (this.needLightmapRefresh && MapViewConstants.getElapsedTicks() != this.tickWithLightChange
                    && !minecraft.isPaused() || this.options.isRealTimeTorches()) {
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
            if (Math.abs(this.lastSunBrightness - sunBrightness) > 0.01
                    || sunBrightness == 1.0 && sunBrightness != this.lastSunBrightness
                    || sunBrightness == 0.0 && sunBrightness != this.lastSunBrightness) {
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

            boolean scheduledUpdate = (timer - 50) % 50 == 0;
            if (lightChanged || scheduledUpdate) {
                this.tickWithLightChange = MapViewConstants.getElapsedTicks();
                this.needLightmapRefresh = true;
            }

            boolean aboveHorizon = MapViewConstants.getPlayer().getEyePosition(0.0F).y
                    >= this.world.getLevelData().getHorizonHeight(this.world);
            if (this.world.dimension().location().toString().toLowerCase().contains("ether")) {
                aboveHorizon = true;
            }

            if (aboveHorizon != this.lastAboveHorizon) {
                this.needSkyColor = true;
                this.lastAboveHorizon = aboveHorizon;
            }

            MutableBlockPos blockPos = BlockPositionCache.get();
            if (cachedBiomeRegistry == null) {
                cachedBiomeRegistry = this.world.registryAccess().registryOrThrow(Registries.BIOME);
            }
            int biomeID = cachedBiomeRegistry.getId(this.world.getBiome(
                    blockPos.withXYZ(MinecraftAccessor.xCoord(), MinecraftAccessor.yCoord(), MinecraftAccessor.zCoord())
            ).value());
            BlockPositionCache.release(blockPos);
            if (biomeID != this.lastBiome) {
                this.needSkyColor = true;
                this.lastBiome = biomeID;
            }

            if (this.needSkyColor || scheduledUpdate) {
                this.colorManager.setSkyColor(this.getSkyColor());
            }
        } catch (RuntimeException ignored) {
            // colorManager oder getSkyColor() noch nicht initialisiert
        }
    }

    private int getSkyColor() {
        this.needSkyColor = false;
        boolean aboveHorizon = this.lastAboveHorizon;
        net.minecraft.world.phys.Vec3 skyColorVec =
                this.world.getSkyColor(minecraft.gameRenderer.getMainCamera().getPosition(), 0.0F);
        float r = (float) skyColorVec.x;
        float g = (float) skyColorVec.y;
        float b = (float) skyColorVec.z;
        if (!aboveHorizon) {
            return 0x0A000000 + (int) (r * 255.0F) * 65536 + (int) (g * 255.0F) * 256 + (int) (b * 255.0F);
        } else {
            int backgroundColor = 0xFF000000 + (int) (r * 255.0F) * 65536 + (int) (g * 255.0F) * 256 + (int) (b * 255.0F);
            // In 1.20.1, EnvironmentAttributes doesn't exist - skipping sunset color overlay
            return backgroundColor;
        }
    }

    public int[] getLightmapArray() {
        return this.lightmapColors;  // NOPMD
    }

    public int getLightmapColor(int skyLight, int blockLight) {
        if (this.lightmapColors == null) {
            return 0;
        }
        return ARGBCompat.toABGR(this.lightmapColors[blockLight + skyLight * 16]);
    }
}
