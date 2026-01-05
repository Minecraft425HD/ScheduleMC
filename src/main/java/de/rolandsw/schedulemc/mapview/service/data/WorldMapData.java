package de.rolandsw.schedulemc.mapview.service.data;

import de.rolandsw.schedulemc.mapview.service.render.ColorCalculationService;
import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.config.WorldMapConfiguration;
import de.rolandsw.schedulemc.mapview.data.cache.RegionCache;
import de.rolandsw.schedulemc.mapview.data.persistence.AsyncPersistenceManager;
import de.rolandsw.schedulemc.mapview.data.persistence.CompressedMapData;
import de.rolandsw.schedulemc.mapview.presentation.screen.WorldMapScreen;
import de.rolandsw.schedulemc.mapview.service.data.ConfigNotificationService;
import de.rolandsw.schedulemc.mapview.navigation.graph.NavigationPathOverlay;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.service.data.MapDataManager;
import de.rolandsw.schedulemc.mapview.core.model.AbstractMapData;
import de.rolandsw.schedulemc.mapview.core.event.MapChangeListener;
import de.rolandsw.schedulemc.mapview.util.BiomeColors;
import de.rolandsw.schedulemc.mapview.util.BlockDatabase;
import de.rolandsw.schedulemc.mapview.service.render.ColorUtils;
import de.rolandsw.schedulemc.mapview.integration.minecraft.MinecraftAccessor;
import de.rolandsw.schedulemc.mapview.util.ChunkCache;
import de.rolandsw.schedulemc.mapview.util.MapViewHelper;
import de.rolandsw.schedulemc.mapview.util.MutableBlockPos;
import de.rolandsw.schedulemc.mapview.util.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import de.rolandsw.schedulemc.mapview.util.ARGBCompat;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldMapData implements MapChangeListener {
    final MutableBlockPos blockPos = new MutableBlockPos(0, 0, 0);
    final ColorCalculationService colorManager;
    final MapViewConfiguration mapOptions;
    WorldMapConfiguration options;
    final int[] lightmapColors;
    ClientLevel world;
    String subworldName = "";
    // OPTIMIZATION: CopyOnWriteArrayList is lock-free for reads (most common operation)
    protected final List<RegionCache> cachedRegionsPool = new CopyOnWriteArrayList<>();
    protected final ConcurrentHashMap<String, RegionCache> cachedRegions = new ConcurrentHashMap<>(150, 0.9F, 2);
    int lastLeft;
    int lastRight;
    int lastTop;
    int lastBottom;
    // OPTIMIZATION: AtomicReference for lock-free reads
    private final AtomicReference<RegionCache[]> lastRegionsArray = new AtomicReference<>(new RegionCache[0]);
    final Comparator<RegionCache> ageThenDistanceSorter = (region1, region2) -> {
        long mostRecentAccess1 = region1.getMostRecentView();
        long mostRecentAccess2 = region2.getMostRecentView();
        if (mostRecentAccess1 < mostRecentAccess2) {
            return 1;
        } else if (mostRecentAccess1 > mostRecentAccess2) {
            return -1;
        } else {
            double distance1sq = (region1.getX() * 256 + region1.getWidth() / 2f - WorldMapData.this.options.getMapX()) * (region1.getX() * 256 + region1.getWidth() / 2f - WorldMapData.this.options.getMapX()) + (region1.getZ() * 256 + region1.getWidth() / 2f - WorldMapData.this.options.getMapZ()) * (region1.getZ() * 256 + region1.getWidth() / 2f - WorldMapData.this.options.getMapZ());
            double distance2sq = (region2.getX() * 256 + region2.getWidth() / 2f - WorldMapData.this.options.getMapX()) * (region2.getX() * 256 + region2.getWidth() / 2f - WorldMapData.this.options.getMapX()) + (region2.getZ() * 256 + region2.getWidth() / 2f - WorldMapData.this.options.getMapZ()) * (region2.getZ() * 256 + region2.getWidth() / 2f - WorldMapData.this.options.getMapZ());
            return Double.compare(distance1sq, distance2sq);
        }
    };
    final Comparator<RegionCoordinates> distanceSorter = (coordinates1, coordinates2) -> {
        double distance1sq = (coordinates1.x * 256 + 128 - WorldMapData.this.options.getMapX()) * (coordinates1.x * 256 + 128 - WorldMapData.this.options.getMapX()) + (coordinates1.z * 256 + 128 - WorldMapData.this.options.getMapZ()) * (coordinates1.z * 256 + 128 - WorldMapData.this.options.getMapZ());
        double distance2sq = (coordinates2.x * 256 + 128 - WorldMapData.this.options.getMapX()) * (coordinates2.x * 256 + 128 - WorldMapData.this.options.getMapX()) + (coordinates2.z * 256 + 128 - WorldMapData.this.options.getMapZ()) * (coordinates2.z * 256 + 128 - WorldMapData.this.options.getMapZ());
        return Double.compare(distance1sq, distance2sq);
    };
    private boolean queuedChangedChunks;
    private ChunkCache chunkCache;
    private final ConcurrentLinkedQueue<ChunkWithAge> chunkUpdateQueue = new ConcurrentLinkedQueue<>();
    private long lastPeriodicRefresh = 0;
    private static final long PERIODIC_REFRESH_INTERVAL_MS = 2000; // Alle 2 Sekunden

    public WorldMapData() {
        this.colorManager = MapViewConstants.getLightMapInstance().getColorManager();
        mapOptions = MapViewConstants.getLightMapInstance().getMapOptions();
        this.options = MapViewConstants.getLightMapInstance().getWorldMapDataOptions();
        this.lightmapColors = new int[256];
        Arrays.fill(this.lightmapColors, -16777216);
    }

    public void newWorld(ClientLevel world) {
        this.subworldName = "";
        this.purgeRegionCaches();
        this.queuedChangedChunks = false;
        this.chunkUpdateQueue.clear();
        this.world = world;

        if (world != null) {
            this.newWorldStuff();
        } else {
            // Pause for 2 seconds before checking world again (using ThreadPoolManager)
            de.rolandsw.schedulemc.util.ThreadPoolManager.executeScheduled(() -> {
                if (WorldMapData.this.world != null) {
                    WorldMapData.this.newWorldStuff();
                }
            }, 2000, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

    }

    private void newWorldStuff() {
        String worldName = TextUtils.scrubNameFile(MapViewConstants.getLightMapInstance().getCurrentWorldName());
        File oldCacheDir = new File(MapViewConstants.getMinecraft().gameDirectory, "/mods/mapview/cache/" + worldName + "/");
        if (oldCacheDir.exists() && oldCacheDir.isDirectory()) {
            File newCacheDir = new File(MapViewConstants.getMinecraft().gameDirectory, "/mapview/cache/" + worldName + "/");
            newCacheDir.getParentFile().mkdirs();
            boolean success = oldCacheDir.renameTo(newCacheDir);
            if (!success) {
                MapViewConstants.getLogger().warn("Failed moving MapDataManager cache files.  Please move " + oldCacheDir.getPath() + " to " + newCacheDir.getPath());
            } else {
                MapViewConstants.getLogger().warn("Moved MapDataManager cache files from " + oldCacheDir.getPath() + " to " + newCacheDir.getPath());
            }
        }

        // Multiworld detection removed with waypoint system

        this.chunkCache = new ChunkCache(33, 33, this);
    }

    public void onTick() {
        if (MapViewConstants.getMinecraft().getCameraEntity() == null) {
            return;
        }
        if (MapViewConstants.getMinecraft().screen == null) {
            this.options.setMapX(MinecraftAccessor.xCoord());
            this.options.setMapZ(MinecraftAccessor.zCoord());
        }

        // Subworld detection removed with waypoint system

        if (this.queuedChangedChunks) {
            this.queuedChangedChunks = false;
            this.prunePool();
        }

        if (this.world != null) {
            this.chunkCache.centerChunks(this.blockPos.withXYZ(MinecraftAccessor.xCoord(), 0, MinecraftAccessor.zCoord()));
            this.chunkCache.checkIfChunksBecameSurroundedByLoaded();

            // Periodischer Refresh um Block-Änderungen zu erkennen
            long now = System.currentTimeMillis();
            if (now - lastPeriodicRefresh >= PERIODIC_REFRESH_INTERVAL_MS) {
                lastPeriodicRefresh = now;
                refreshNearbyChunks();
            }

            while (!this.chunkUpdateQueue.isEmpty() && Math.abs(MapViewConstants.getElapsedTicks() - this.chunkUpdateQueue.peek().tick) >= 20) {
                this.doProcessChunk(this.chunkUpdateQueue.remove().chunk);
            }
        }

    }

    public WorldMapConfiguration getOptions() {
        return this.options;
    }

    /**
     * Aktualisiert Chunks in der Nähe des Spielers um Block-Änderungen zu erkennen
     */
    private void refreshNearbyChunks() {
        if (this.world == null) {
            return;
        }

        int playerChunkX = MinecraftAccessor.xCoord() >> 4;
        int playerChunkZ = MinecraftAccessor.zCoord() >> 4;

        // Aktualisiere Chunks im Umkreis von 4 Chunks (64 Blöcke)
        int radius = 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int chunkX = playerChunkX + dx;
                int chunkZ = playerChunkZ + dz;

                LevelChunk chunk = this.world.getChunk(chunkX, chunkZ);
                if (chunk != null && !chunk.isEmpty() && this.world.hasChunk(chunkX, chunkZ)) {
                    // Prüfe ob Chunk von geladenen umgeben ist
                    if (this.isChunkReady(this.world, chunk)) {
                        this.processChunk(chunk);
                    }
                }
            }
        }
    }

    public void purgeRegionCaches() {
        // OPTIMIZATION: No synchronization needed - CopyOnWriteArrayList is thread-safe
        for (RegionCache cachedRegion : this.cachedRegionsPool) {
            cachedRegion.cleanup();
        }

        this.cachedRegions.clear();
        this.cachedRegionsPool.clear();
        this.getRegions(0, -1, 0, -1);
    }

    public void renameSubworld(String oldName, String newName) {
        // OPTIMIZATION: No synchronization needed - CopyOnWriteArrayList is thread-safe
        for (RegionCache cachedRegion : this.cachedRegionsPool) {
            cachedRegion.renameSubworld(oldName, newName);
        }
    }

    public ConfigNotificationService getSettingsAndLightingChangeNotifier() {
        return MapViewConstants.getLightMapInstance().getSettingsAndLightingChangeNotifier();
    }

    public void setLightMapArray(int[] lights) {
        boolean changed;
        int torchOffset = 0;
        int skylightMultiplier = 16;

        changed = IntStream.range(0, 16).anyMatch(t -> lights[t * skylightMultiplier + torchOffset] != this.lightmapColors[t * skylightMultiplier + torchOffset]);

        System.arraycopy(lights, 0, this.lightmapColors, 0, 256);
        if (changed) {
            this.getSettingsAndLightingChangeNotifier().notifyOfChanges();
        }

    }

    public void getAndStoreData(AbstractMapData mapData, Level world, LevelChunk chunk, MutableBlockPos pos, boolean underground, int startX, int startZ, int imageX, int imageY) {
        int bottomY = world.getMinBuildHeight();
        int surfaceHeight;
        int seafloorHeight = bottomY;
        int transparentHeight = bottomY;
        int foliageHeight = bottomY;
        BlockState surfaceBlockState;
        BlockState transparentBlockState = BlockDatabase.air.defaultBlockState();
        BlockState foliageBlockState = BlockDatabase.air.defaultBlockState();
        BlockState seafloorBlockState = BlockDatabase.air.defaultBlockState();
        pos.setXYZ(startX + imageX, 64, startZ + imageY);
        Biome biome;
        if (!chunk.isEmpty()) {
            biome = world.getBiome(pos).value();
        } else {
            biome = null;
        }

        mapData.setBiome(imageX, imageY, biome);
        if (biome != null) {
            boolean solid = false;
            if (underground) {
                surfaceHeight = this.getNetherHeight(chunk, startX + imageX, startZ + imageY);
                surfaceBlockState = chunk.getBlockState(pos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                if (surfaceHeight != Short.MIN_VALUE) {
                    foliageHeight = surfaceHeight + 1;
                    pos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
                    foliageBlockState = chunk.getBlockState(pos);
                    Block material = foliageBlockState.getBlock();
                    if (material == Blocks.SNOW || material instanceof AirBlock || material == Blocks.LAVA || material == Blocks.WATER) {
                        foliageHeight = 0;
                    }
                }
            } else {
                transparentHeight = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() & 15, pos.getZ() & 15) + 1;
                transparentBlockState = chunk.getBlockState(pos.withXYZ(startX + imageX, transparentHeight - 1, startZ + imageY));
                FluidState fluidState = transparentBlockState.getFluidState();
                if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                    transparentBlockState = fluidState.createLegacyBlock();
                }

                surfaceHeight = transparentHeight;
                surfaceBlockState = transparentBlockState;
                VoxelShape voxelShape;
                boolean hasOpacity = transparentBlockState.getLightBlock(chunk, pos) > 0;
                if (!hasOpacity && transparentBlockState.canOcclude() && transparentBlockState.useShapeForLightOcclusion()) {
                    voxelShape = transparentBlockState.getFaceOcclusionShape(chunk, pos, Direction.DOWN);
                    hasOpacity = Shapes.faceShapeOccludes(voxelShape, Shapes.empty());
                    voxelShape = transparentBlockState.getFaceOcclusionShape(chunk, pos, Direction.UP);
                    hasOpacity = hasOpacity || Shapes.faceShapeOccludes(Shapes.empty(), voxelShape);
                }

                while (!hasOpacity && surfaceHeight > bottomY) {
                    foliageBlockState = surfaceBlockState;
                    --surfaceHeight;
                    surfaceBlockState = chunk.getBlockState(pos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                    fluidState = surfaceBlockState.getFluidState();
                    if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                        surfaceBlockState = fluidState.createLegacyBlock();
                    }

                    hasOpacity = surfaceBlockState.getLightBlock(chunk, pos) > 0;
                    if (!hasOpacity && surfaceBlockState.canOcclude() && surfaceBlockState.useShapeForLightOcclusion()) {
                        voxelShape = surfaceBlockState.getFaceOcclusionShape(chunk, pos, Direction.DOWN);
                        hasOpacity = Shapes.faceShapeOccludes(voxelShape, Shapes.empty());
                        voxelShape = surfaceBlockState.getFaceOcclusionShape(chunk, pos, Direction.UP);
                        hasOpacity = hasOpacity || Shapes.faceShapeOccludes(Shapes.empty(), voxelShape);
                    }
                }

                if (surfaceHeight == transparentHeight) {
                    transparentHeight = bottomY;
                    transparentBlockState = BlockDatabase.air.defaultBlockState();
                    foliageBlockState = chunk.getBlockState(pos.withXYZ(startX + imageX, surfaceHeight, startZ + imageY));
                }

                if (foliageBlockState.getBlock() == Blocks.SNOW) {
                    surfaceBlockState = foliageBlockState;
                    foliageBlockState = BlockDatabase.air.defaultBlockState();
                }

                if (foliageBlockState == transparentBlockState) {
                    foliageBlockState = BlockDatabase.air.defaultBlockState();
                }

                if (foliageBlockState != null && !(foliageBlockState.getBlock() instanceof AirBlock)) {
                    foliageHeight = surfaceHeight + 1;
                } else {
                    foliageBlockState = BlockDatabase.air.defaultBlockState();
                }

                Block material = surfaceBlockState.getBlock();
                if (material == Blocks.WATER || material == Blocks.ICE) {
                    seafloorHeight = surfaceHeight;

                    for (seafloorBlockState = chunk.getBlockState(pos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY)); seafloorBlockState.getLightBlock(chunk, pos) < 5 && !(seafloorBlockState.getBlock() instanceof LeavesBlock) && seafloorHeight > bottomY + 1; seafloorBlockState = chunk.getBlockState(pos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY))) {
                        material = seafloorBlockState.getBlock();
                        if (transparentHeight == bottomY && material != Blocks.ICE && material != Blocks.WATER && seafloorBlockState.blocksMotion()) {
                            transparentHeight = seafloorHeight;
                            transparentBlockState = seafloorBlockState;
                        }

                        if (foliageHeight == bottomY && seafloorHeight != transparentHeight && transparentBlockState != seafloorBlockState && material != Blocks.ICE && material != Blocks.WATER && !(material instanceof AirBlock) && material != Blocks.BUBBLE_COLUMN) {
                            foliageHeight = seafloorHeight;
                            foliageBlockState = seafloorBlockState;
                        }

                        --seafloorHeight;
                    }

                    if (seafloorBlockState.getBlock() == Blocks.WATER) {
                        seafloorBlockState = BlockDatabase.air.defaultBlockState();
                    }
                }
            }

            mapData.setHeight(imageX, imageY, surfaceHeight);
            mapData.setBlockstate(imageX, imageY, surfaceBlockState);
            mapData.setTransparentHeight(imageX, imageY, transparentHeight);
            mapData.setTransparentBlockstate(imageX, imageY, transparentBlockState);
            mapData.setFoliageHeight(imageX, imageY, foliageHeight);
            mapData.setFoliageBlockstate(imageX, imageY, foliageBlockState);
            mapData.setOceanFloorHeight(imageX, imageY, seafloorHeight);
            mapData.setOceanFloorBlockstate(imageX, imageY, seafloorBlockState);
            if (surfaceHeight < bottomY) {
                surfaceHeight = 80;
                solid = true;
            }

            if (surfaceBlockState.getBlock() == Blocks.LAVA) {
                solid = false;
            }

            int light = solid ? 0 : 255;
            if (!solid) {
                light = this.getLight(surfaceBlockState, world, pos, startX + imageX, startZ + imageY, surfaceHeight, solid);
            }

            mapData.setLight(imageX, imageY, light);
            int seafloorLight = 0;
            if (seafloorBlockState != null && seafloorBlockState != BlockDatabase.air.defaultBlockState()) {
                seafloorLight = this.getLight(seafloorBlockState, world, pos, startX + imageX, startZ + imageY, seafloorHeight, solid);
            }

            mapData.setOceanFloorLight(imageX, imageY, seafloorLight);
            int transparentLight = 0;
            if (transparentBlockState != null && transparentBlockState != BlockDatabase.air.defaultBlockState()) {
                transparentLight = this.getLight(transparentBlockState, world, pos, startX + imageX, startZ + imageY, transparentHeight, solid);
            }

            mapData.setTransparentLight(imageX, imageY, transparentLight);
            int foliageLight = 0;
            if (foliageBlockState != null && foliageBlockState != BlockDatabase.air.defaultBlockState()) {
                foliageLight = this.getLight(foliageBlockState, world, pos, startX + imageX, startZ + imageY, foliageHeight, solid);
            }

            mapData.setFoliageLight(imageX, imageY, foliageLight);
        }
    }

    private int getNetherHeight(LevelChunk chunk, int x, int z) {
        int bottomY = chunk.getMinBuildHeight();
        int y = 80;
        this.blockPos.setXYZ(x, y, z);
        BlockState blockState = chunk.getBlockState(this.blockPos);
        if (blockState.getLightBlock(chunk, this.blockPos) == 0 && blockState.getBlock() != Blocks.LAVA) {
            while (y > bottomY) {
                --y;
                this.blockPos.setXYZ(x, y, z);
                blockState = chunk.getBlockState(this.blockPos);
                if (blockState.getLightBlock(chunk, this.blockPos) > 0 || blockState.getBlock() == Blocks.LAVA) {
                    return y + 1;
                }
            }

            return y;
        } else {
            while (y <= 90) {
                ++y;
                this.blockPos.setXYZ(x, y, z);
                blockState = chunk.getBlockState(this.blockPos);
                if (blockState.getLightBlock(chunk, this.blockPos) == 0 && blockState.getBlock() != Blocks.LAVA) {
                    return y;
                }
            }

            return Short.MIN_VALUE;
        }
    }

    private int getLight(BlockState blockState, Level world, MutableBlockPos blockPos, int x, int z, int height, boolean solid) {
        int lightCombined = 255;
        if (solid) {
            lightCombined = 0;
        } else if (blockState != null && !(blockState.getBlock() instanceof AirBlock)) {
            blockPos.setXYZ(x, Math.max(Math.min(height, world.getMaxBuildHeight()), world.getMinBuildHeight()), z);
            int blockLight = world.getBrightness(LightLayer.BLOCK, blockPos) & 15;
            int skyLight = world.getBrightness(LightLayer.SKY, blockPos);
            if (blockState.getBlock() == Blocks.LAVA || blockState.getBlock() == Blocks.MAGMA_BLOCK) {
                blockLight = 14;
            }

            lightCombined = blockLight + skyLight * 16;
        }

        return lightCombined;
    }

    public int getPixelColor(AbstractMapData mapData, ClientLevel world, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, boolean underground, int multi, int startX, int startZ, int imageX, int imageY) {
        int bottomY = world.getMinBuildHeight();
        int mcX = startX + imageX;
        int mcZ = startZ + imageY;
        BlockState surfaceBlockState;
        BlockState transparentBlockState;
        BlockState foliageBlockState;
        BlockState seafloorBlockState;
        int surfaceHeight;
        int seafloorHeight = bottomY;
        int transparentHeight = bottomY;
        int foliageHeight = bottomY;
        int surfaceColor;
        int seafloorColor = 0;
        int transparentColor = 0;
        int foliageColor = 0;
        blockPos.setXYZ(mcX, 0, mcZ);
        int color24;
        Biome biome = mapData.getBiome(imageX, imageY);
        surfaceBlockState = mapData.getBlockstate(imageX, imageY);
        if (surfaceBlockState != null && (surfaceBlockState.getBlock() != BlockDatabase.air || mapData.getLight(imageX, imageY) != 0 || mapData.getHeight(imageX, imageY) != Short.MIN_VALUE) && biome != null) {
            if (mapOptions.biomeOverlay == 1) {
                color24 = ARGBCompat.toABGR(BiomeColors.getBiomeColor(biome) | 0xFF000000);
            } else {
                boolean solid = false;
                int blockStateID;
                surfaceHeight = mapData.getHeight(imageX, imageY);
                blockStateID = BlockDatabase.getStateId(surfaceBlockState);
                if (surfaceHeight < bottomY || surfaceHeight == world.getMaxBuildHeight()) {
                    surfaceHeight = 80;
                    solid = true;
                }

                blockPos.setXYZ(mcX, surfaceHeight - 1, mcZ);
                if (surfaceBlockState.getBlock() == Blocks.LAVA) {
                    solid = false;
                }

                if (mapOptions.biomes) {
                    surfaceColor = this.colorManager.getBlockColor(blockPos, blockStateID, biome);
                    int tint;
                    tint = this.colorManager.getBiomeTint(mapData, world, surfaceBlockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
                    if (tint != -1) {
                        surfaceColor = ColorUtils.colorMultiplier(surfaceColor, tint);
                    }
                } else {
                    surfaceColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
                }

                surfaceColor = this.applyHeight(mapData, surfaceColor, underground, multi, imageX, imageY, surfaceHeight, solid, 1);
                int light = mapData.getLight(imageX, imageY);
                if (solid) {
                    surfaceColor = 0;
                } else if (mapOptions.lightmap) {
                    int lightValue = this.getLight(light);
                    surfaceColor = ColorUtils.colorMultiplier(surfaceColor, lightValue);
                }

                if (mapOptions.waterTransparency && !solid) {
                    seafloorHeight = mapData.getOceanFloorHeight(imageX, imageY);
                    if (seafloorHeight > bottomY) {
                        blockPos.setXYZ(mcX, seafloorHeight - 1, mcZ);
                        seafloorBlockState = mapData.getOceanFloorBlockstate(imageX, imageY);
                        if (seafloorBlockState != null && seafloorBlockState != BlockDatabase.air.defaultBlockState()) {
                            blockStateID = BlockDatabase.getStateId(seafloorBlockState);
                            if (mapOptions.biomes) {
                                seafloorColor = this.colorManager.getBlockColor(blockPos, blockStateID, biome);
                                int tint;
                                tint = this.colorManager.getBiomeTint(mapData, world, seafloorBlockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
                                if (tint != -1) {
                                    seafloorColor = ColorUtils.colorMultiplier(seafloorColor, tint);
                                }
                            } else {
                                seafloorColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
                            }

                            seafloorColor = this.applyHeight(mapData, seafloorColor, underground, multi, imageX, imageY, seafloorHeight, solid, 0);
                            int seafloorLight;
                            seafloorLight = mapData.getOceanFloorLight(imageX, imageY);
                            if (mapOptions.lightmap) {
                                int lightValue = this.getLight(seafloorLight);
                                seafloorColor = ColorUtils.colorMultiplier(seafloorColor, lightValue);
                            }
                        }
                    }
                }

                if (mapOptions.blockTransparency && !solid) {
                    transparentHeight = mapData.getTransparentHeight(imageX, imageY);
                    if (transparentHeight > bottomY) {
                        blockPos.setXYZ(mcX, transparentHeight - 1, mcZ);
                        transparentBlockState = mapData.getTransparentBlockstate(imageX, imageY);
                        if (transparentBlockState != null && transparentBlockState != BlockDatabase.air.defaultBlockState()) {
                            blockStateID = BlockDatabase.getStateId(transparentBlockState);
                            if (mapOptions.biomes) {
                                transparentColor = this.colorManager.getBlockColor(blockPos, blockStateID, biome);
                                int tint;
                                tint = this.colorManager.getBiomeTint(mapData, world, transparentBlockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
                                if (tint != -1) {
                                    transparentColor = ColorUtils.colorMultiplier(transparentColor, tint);
                                }
                            } else {
                                transparentColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
                            }

                            transparentColor = this.applyHeight(mapData, transparentColor, underground, multi, imageX, imageY, transparentHeight, solid, 3);
                            int transparentLight;
                            transparentLight = mapData.getTransparentLight(imageX, imageY);
                            if (mapOptions.lightmap) {
                                int lightValue = this.getLight(transparentLight);
                                transparentColor = ColorUtils.colorMultiplier(transparentColor, lightValue);
                            }
                        }
                    }

                    foliageHeight = mapData.getFoliageHeight(imageX, imageY);
                    if (foliageHeight > bottomY) {
                        blockPos.setXYZ(mcX, foliageHeight - 1, mcZ);
                        foliageBlockState = mapData.getFoliageBlockstate(imageX, imageY);
                        if (foliageBlockState != null && foliageBlockState != BlockDatabase.air.defaultBlockState()) {
                            blockStateID = BlockDatabase.getStateId(foliageBlockState);
                            if (mapOptions.biomes) {
                                foliageColor = this.colorManager.getBlockColor(blockPos, blockStateID, biome);
                                int tint;
                                tint = this.colorManager.getBiomeTint(mapData, world, foliageBlockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
                                if (tint != -1) {
                                    foliageColor = ColorUtils.colorMultiplier(foliageColor, tint);
                                }
                            } else {
                                foliageColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
                            }

                            foliageColor = this.applyHeight(mapData, foliageColor, underground, multi, imageX, imageY, foliageHeight, solid, 2);
                            int foliageLight;
                            foliageLight = mapData.getFoliageLight(imageX, imageY);
                            if (mapOptions.lightmap) {
                                int lightValue = this.getLight(foliageLight);
                                foliageColor = ColorUtils.colorMultiplier(foliageColor, lightValue);
                            }
                        }
                    }
                }

                if (mapOptions.waterTransparency && seafloorHeight > bottomY) {
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

                if (mapOptions.biomeOverlay == 2) {
                    int bc = 0;
                    if (biome != null) {
                        bc = ARGBCompat.toABGR(BiomeColors.getBiomeColor(biome));
                    }

                    bc = 0x7F000000 | bc;
                    color24 = ColorUtils.colorAdder(bc, color24);
                }

            }

            // Navigation Pfad in die Kartenfarbe mischen
            color24 = NavigationPathOverlay.getInstance().blendWithPath(color24, mcX, mcZ);

            return MapViewHelper.doSlimeAndGrid(ARGBCompat.toABGR(color24), world, mcX, mcZ);
        } else {
            return 0;
        }
    }

    private int applyHeight(AbstractMapData mapData, int color24, boolean underground, int multi, int imageX, int imageY, int height, boolean solid, int layer) {
        if (color24 != this.colorManager.getAirColor() && color24 != 0) {
            int heightComp = Short.MIN_VALUE;
            if ((mapOptions.heightmap || mapOptions.slopemap) && !solid) {
                int diff;
                double sc = 0.0;
                boolean invert = false;
                if (!mapOptions.slopemap) {
                    diff = height - 80;
                    sc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 1.8;
                    if (diff < 0) {
                        sc = 0.0 - sc;
                    }
                } else {
                    if (imageX > 0 && imageY < 32 * multi - 1) {
                        if (layer == 0) {
                            heightComp = mapData.getOceanFloorHeight(imageX - 1, imageY + 1);
                        }

                        if (layer == 1) {
                            heightComp = mapData.getHeight(imageX - 1, imageY + 1);
                        }

                        if (layer == 2) {
                            heightComp = height;
                        }

                        if (layer == 3) {
                            heightComp = mapData.getTransparentHeight(imageX - 1, imageY + 1);
                            if (heightComp == Short.MIN_VALUE) {
                                BlockState transparentBlockState = mapData.getTransparentBlockstate(imageX, imageY);
                                if (transparentBlockState != null && transparentBlockState != BlockDatabase.air.defaultBlockState()) {
                                    Block block = transparentBlockState.getBlock();
                                    if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
                                        heightComp = mapData.getHeight(imageX - 1, imageY + 1);
                                    }
                                }
                            }
                        }
                    } else if (imageX < 32 * multi - 1 && imageY > 0) {
                        if (layer == 0) {
                            heightComp = mapData.getOceanFloorHeight(imageX + 1, imageY - 1);
                        }

                        if (layer == 1) {
                            heightComp = mapData.getHeight(imageX + 1, imageY - 1);
                        }

                        if (layer == 2) {
                            heightComp = height;
                        }

                        if (layer == 3) {
                            heightComp = mapData.getTransparentHeight(imageX + 1, imageY - 1);
                            if (heightComp == Short.MIN_VALUE) {
                                BlockState transparentBlockState = mapData.getTransparentBlockstate(imageX, imageY);
                                if (transparentBlockState != null && transparentBlockState != BlockDatabase.air.defaultBlockState()) {
                                    Block block = transparentBlockState.getBlock();
                                    if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
                                        heightComp = mapData.getHeight(imageX + 1, imageY - 1);
                                    }
                                }
                            }
                        }

                        invert = true;
                    } else {
                        heightComp = height;
                    }

                    if (heightComp == Short.MIN_VALUE) {
                        heightComp = height;
                    }

                    if (!invert) {
                        diff = heightComp - height;
                    } else {
                        diff = height - heightComp;
                    }

                    if (diff != 0) {
                        sc = diff > 0 ? 1.0 : -1.0;
                        sc /= 8.0;
                    }

                    if (mapOptions.heightmap) {
                        diff = height - 80;
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
        }

        return color24;
    }

    private int getLight(int light) {
        return this.lightmapColors[light];
    }

    public RegionCache[] getRegions(int left, int right, int top, int bottom) {
        if (left == this.lastLeft && right == this.lastRight && top == this.lastTop && bottom == this.lastBottom) {
            return this.lastRegionsArray.get();
        } else {
            AsyncPersistenceManager.emptyQueue();
            RegionCache[] visibleRegionCachesArray = new RegionCache[(right - left + 1) * (bottom - top + 1)];
            String worldName = MapViewConstants.getLightMapInstance().getCurrentWorldName();
            String subWorldName = "";
            List<RegionCoordinates> regionsToDisplay = new ArrayList<>();

            for (int t = left; t <= right; ++t) {
                for (int s = top; s <= bottom; ++s) {
                    RegionCoordinates regionCoordinates = new RegionCoordinates(t, s);
                    regionsToDisplay.add(regionCoordinates);
                }
            }

            regionsToDisplay.sort(this.distanceSorter);

            for (RegionCoordinates regionCoordinates : regionsToDisplay) {
                int x = regionCoordinates.x;
                int z = regionCoordinates.z;
                String key = x + "," + z;
                // OPTIMIZATION: ConcurrentHashMap.computeIfAbsent is atomic and lock-free
                RegionCache cachedRegion = this.cachedRegions.computeIfAbsent(key, k -> {
                    RegionCache newRegion = new RegionCache(this, k, this.world, worldName, subWorldName, x, z);
                    // OPTIMIZATION: CopyOnWriteArrayList.add is thread-safe without explicit synchronization
                    this.cachedRegionsPool.add(newRegion);
                    return newRegion;
                });

                cachedRegion.refresh(true);
                visibleRegionCachesArray[(z - top) * (right - left + 1) + (x - left)] = cachedRegion;
            }

            this.prunePool();
            // OPTIMIZATION: AtomicReference for lock-free update
            this.lastLeft = left;
            this.lastRight = right;
            this.lastTop = top;
            this.lastBottom = bottom;
            this.lastRegionsArray.set(visibleRegionCachesArray);
            return visibleRegionCachesArray;
        }
    }

    private void prunePool() {
        // OPTIMIZATION: CopyOnWriteArrayList doesn't support iterator.remove(), use removeIf()
        this.cachedRegionsPool.removeIf(region -> {
            if (region.isLoaded() && region.isEmpty()) {
                this.cachedRegions.put(region.getKey(), RegionCache.emptyRegion);
                region.cleanup();
                return true;
            }
            return false;
        });

        if (this.cachedRegionsPool.size() > this.options.getCacheSize()) {
            this.cachedRegionsPool.sort(this.ageThenDistanceSorter);
            List<RegionCache> toRemove = this.cachedRegionsPool.subList(this.options.getCacheSize(), this.cachedRegionsPool.size());

            for (RegionCache cachedRegion : toRemove) {
                this.cachedRegions.remove(cachedRegion.getKey());
                cachedRegion.cleanup();
            }

            toRemove.clear();
        }

        this.compress();
    }

    public void compress() {
        // OPTIMIZATION: No synchronization needed - CopyOnWriteArrayList is thread-safe
        for (RegionCache cachedRegion : this.cachedRegionsPool) {
            if (System.currentTimeMillis() - cachedRegion.getMostRecentChange() > 5000L) {
                cachedRegion.compress();
            }
        }
    }

    @Override
    public void handleChangeInWorld(int chunkX, int chunkZ) {
        if (this.world != null) {
            LevelChunk chunk = this.world.getChunk(chunkX, chunkZ);
            if (chunk != null && !chunk.isEmpty()) {
                if (this.isChunkReady(this.world, chunk)) {
                    this.processChunk(chunk);
                }

            }
        }
    }

    @Override
    public void processChunk(LevelChunk chunk) {
        if (MapDataManager.mapOptions.worldmapAllowed) {
            this.chunkUpdateQueue.add(new ChunkWithAge(chunk, MapViewConstants.getElapsedTicks()));
        }
    }

    private void doProcessChunk(LevelChunk chunk) {
        this.queuedChangedChunks = true;

        try {
            if (this.world == null) {
                return;
            }

            if (chunk == null || chunk.isEmpty()) {
                return;
            }

            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;
            int regionX = (int) Math.floor(chunkX / 16.0);
            int regionZ = (int) Math.floor(chunkZ / 16.0);
            String key = regionX + "," + regionZ;
            // OPTIMIZATION: ConcurrentHashMap.computeIfAbsent is atomic and lock-free
            RegionCache cachedRegion = this.cachedRegions.computeIfAbsent(key, k -> {
                String worldName = MapViewConstants.getLightMapInstance().getCurrentWorldName();
                String subWorldName = "";
                RegionCache newRegion = new RegionCache(this, k, this.world, worldName, subWorldName, regionX, regionZ);
                // OPTIMIZATION: CopyOnWriteArrayList.add is thread-safe
                this.cachedRegionsPool.add(newRegion);

                // OPTIMIZATION: AtomicReference for lock-free array access
                RegionCache[] currentArray = this.lastRegionsArray.get();
                if (regionX >= this.lastLeft && regionX <= this.lastRight && regionZ >= this.lastTop && regionZ <= this.lastBottom) {
                    currentArray[(regionZ - this.lastTop) * (this.lastRight - this.lastLeft + 1) + (regionX - this.lastLeft)] = newRegion;
                }
                return newRegion;
            });

            if (MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen instanceof WorldMapScreen) {
                cachedRegion.registerChangeAt(chunkX, chunkZ);
                cachedRegion.refresh(false);
            } else {
                cachedRegion.handleChangedChunk(chunk);
            }
        } catch (Exception var19) {
            MapViewConstants.getLogger().error(var19.getMessage(), var19);
        }

    }

    private boolean isChunkReady(ClientLevel world, LevelChunk chunk) {
        return this.chunkCache.isChunkSurroundedByLoaded(chunk.getPos().x, chunk.getPos().z);
    }

    public boolean isRegionLoaded(int blockX, int blockZ) {
        int x = (int) Math.floor(blockX / 256.0F);
        int z = (int) Math.floor(blockZ / 256.0F);
        RegionCache cachedRegion = this.cachedRegions.get(x + "," + z);
        return cachedRegion != null && cachedRegion.isLoaded();
    }

    public boolean isGroundAt(int blockX, int blockZ) {
        int x = (int) Math.floor(blockX / 256.0F);
        int z = (int) Math.floor(blockZ / 256.0F);
        RegionCache cachedRegion = this.cachedRegions.get(x + "," + z);
        return cachedRegion != null && cachedRegion.isGroundAt(blockX, blockZ);
    }

    public int getHeightAt(int blockX, int blockZ) {
        int x = (int) Math.floor(blockX / 256.0F);
        int z = (int) Math.floor(blockZ / 256.0F);
        RegionCache cachedRegion = this.cachedRegions.get(x + "," + z);
        return cachedRegion == null ? Short.MIN_VALUE : cachedRegion.getHeightAt(blockX, blockZ);
    }

    /**
     * Gibt den BlockState an einer Welt-Position zurück
     * Für Navigation/Pathfinding auf Straßenblöcken
     *
     * @param blockX Welt X-Koordinate
     * @param blockZ Welt Z-Koordinate
     * @return BlockState oder null wenn Region nicht geladen
     */
    public BlockState getBlockStateAt(int blockX, int blockZ) {
        int x = (int) Math.floor(blockX / 256.0F);
        int z = (int) Math.floor(blockZ / 256.0F);
        RegionCache cachedRegion = this.cachedRegions.get(x + "," + z);
        return cachedRegion == null ? null : cachedRegion.getBlockStateAt(blockX, blockZ);
    }

    public void debugLog(int blockX, int blockZ) {
        int x = (int) Math.floor(blockX / 256.0F);
        int z = (int) Math.floor(blockZ / 256.0F);
        RegionCache cachedRegion = this.cachedRegions.get(x + "," + z);
        if (cachedRegion == null) {
            MapViewConstants.getLogger().info("No Region " + x + "," + z + " at " + blockX + "," + blockZ);
        } else {
            MapViewConstants.getLogger().info("Info for region " + x + "," + z + " block " + blockX + "," + blockZ);
            int localx = blockX - x * 256;
            int localz = blockZ - z * 256;
            CompressedMapData data = cachedRegion.getMapData();
            if (data == null) {
                MapViewConstants.getLogger().info("  No map data!");
            } else {
                MapViewConstants.getLogger().info("  Base: " + data.getHeight(localx, localz) + " Block: " + data.getBlockstate(localx, localz) + " Light: " + Integer.toHexString(data.getLight(localx, localz)));
                MapViewConstants.getLogger().info("  Foilage: " + data.getFoliageHeight(localx, localz) + " Block: " + data.getFoliageBlockstate(localx, localz) + " Light: " + Integer.toHexString(data.getFoliageLight(localx, localz)));
                MapViewConstants.getLogger().info("  Ocean Floor: " + data.getOceanFloorHeight(localx, localz) + " Block: " + data.getOceanFloorBlockstate(localx, localz) + " Light: " + Integer.toHexString(data.getOceanFloorLight(localx, localz)));
                MapViewConstants.getLogger().info("  Transparent: " + data.getTransparentHeight(localx, localz) + " Block: " + data.getTransparentBlockstate(localx, localz) + " Light: " + Integer.toHexString(data.getTransparentLight(localx, localz)));
                MapViewConstants.getLogger().info("  Biome: " + world.registryAccess().registryOrThrow(Registries.BIOME).getKey(data.getBiome(localx, localz)) + " (" + data.getBiomeId(localx, localz) + ")");
            }
        }
    }

    /**
     * Invalidiert alle Regions entlang eines Pfades, um ein Neurendern zu erzwingen
     * Wird benötigt wenn der Navigationspfad gesetzt oder gelöscht wird
     *
     * @param path Liste von BlockPos entlang des Pfades
     */
    public void invalidateRegionsAlongPath(List<BlockPos> path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        // Sammle alle betroffenen Region-Koordinaten (ohne Duplikate)
        java.util.Set<String> affectedRegionKeys = new java.util.HashSet<>();

        for (BlockPos pos : path) {
            int regionX = (int) Math.floor(pos.getX() / 256.0);
            int regionZ = (int) Math.floor(pos.getZ() / 256.0);
            String key = regionX + "," + regionZ;
            affectedRegionKeys.add(key);
        }

        MapViewConstants.getLogger().info("[WorldMapData] Invalidating {} regions along navigation path", affectedRegionKeys.size());

        // Invalidiere alle betroffenen Regions
        ConfigNotificationService notifier = new ConfigNotificationService();
        for (String key : affectedRegionKeys) {
            RegionCache cachedRegion = this.cachedRegions.get(key);
            if (cachedRegion != null && cachedRegion != RegionCache.emptyRegion) {
                cachedRegion.notifyOfActionableChange(notifier);
            }
        }
    }

    private record ChunkWithAge(LevelChunk chunk, int tick) {}
    private record RegionCoordinates(int x, int z) {}
}
