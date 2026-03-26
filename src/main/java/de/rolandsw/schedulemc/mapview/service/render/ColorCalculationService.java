package de.rolandsw.schedulemc.mapview.service.render;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.core.model.AbstractMapData;
import de.rolandsw.schedulemc.mapview.core.model.BlockModel;
import de.rolandsw.schedulemc.mapview.integration.DebugRenderState;
import de.rolandsw.schedulemc.mapview.util.BlockDatabase;
import de.rolandsw.schedulemc.mapview.service.render.ColorUtils;
import de.rolandsw.schedulemc.mapview.util.GLUtils;
import de.rolandsw.schedulemc.mapview.util.MessageUtils;
import de.rolandsw.schedulemc.mapview.util.MutableBlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import de.rolandsw.schedulemc.mapview.util.ARGBCompat;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColorCalculationService {
    private boolean resourcePacksChanged;
    ClientLevel world;
    private BufferedImage terrainBuff;
    private BufferedImage colorPicker;
    int sizeOfBiomeArray;
    int[] blockColors = new int[16384];
    int[] blockColorsWithDefaultTint = new int[16384];
    float failedToLoadX;
    float failedToLoadY;
    final RandomSource random = RandomSource.create();
    private boolean loaded;
    private boolean loadedTerrainImage;
    final MutableBlockPos dummyBlockPos = new MutableBlockPos(BlockPos.ZERO.getX(), BlockPos.ZERO.getY(), BlockPos.ZERO.getZ());
    private final ColorResolver spruceColorResolver = (blockState, biomex, blockPos) -> FoliageColor.getEvergreenColor();
    private final ColorResolver birchColorResolver = (blockState, biomex, blockPos) -> FoliageColor.getBirchColor();
    private final ColorResolver mangroveColorResolver = (blockState, biomex, blockPos) -> FoliageColor.getMangroveColor();
    private final ColorResolver grassColorResolver = (blockState, biomex, blockPos) -> biomex.getGrassColor(blockPos.getX(), blockPos.getZ());
    private final ColorResolver foliageColorResolver = (blockState, biomex, blockPos) -> biomex.getFoliageColor();
    private final ColorResolver dryFoliageColorResolver = (blockState, biomex, blockPos) -> biomex.getFoliageColor();
    // Fixed water color (Minecraft default blue) - ignore biome tinting to prevent brown water in swamps
    private final ColorResolver waterColorResolver = (blockState, biomex, blockPos) -> 0x3F76E4;
    // Fixed lava color (Minecraft default orange/red)
    private final ColorResolver lavaColorResolver = (blockState, biomex, blockPos) -> 0xFF5A00;

    // Performance-Optimierung: LRU Cache für Biome Tints (reduziert 9 Biome-Lookups pro Block)
    // Cache-Size: 4096 Einträge = ~32KB Memory (genug für typische Spieler-Umgebung)
    private final Map<Long, Integer> biomeTintCache = new LinkedHashMap<Long, Integer>(4096, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
            return size() > 4096;
        }
    };
    private final ColorResolver redstoneColorResolver = (blockState, biomex, blockPos) -> RedStoneWireBlock.getColorForPower(blockState.getValue(RedStoneWireBlock.POWER));

    private final OptiFineColorLoader optifineLoader;

    public ColorCalculationService() {
        this.optifineLoader = new OptiFineColorLoader(this);
        ++this.sizeOfBiomeArray;
    }

    public int getAirColor() {
        return this.blockColors[BlockDatabase.airID];
    }

    public BufferedImage getColorPicker() {
        return this.colorPicker;
    }

    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.resourcePacksChanged = true;
    }

    public boolean checkForChanges() {
        boolean biomesChanged = false;

        if (MapViewConstants.getClientWorld() != this.world) {
            this.world = MapViewConstants.getClientWorld();
            int largestBiomeID = 0;

            Registry<Biome> biomeRegistry = this.world.registryAccess().registryOrThrow(Registries.BIOME);
            for (Map.Entry<ResourceKey<Biome>, Biome> entry : biomeRegistry.entrySet()) {
                Biome biome = entry.getValue();
                int biomeID = biomeRegistry.getId(biome);
                if (biomeID > largestBiomeID) {
                    largestBiomeID = biomeID;
                }
            }

            if (this.sizeOfBiomeArray != largestBiomeID + 1) {
                this.sizeOfBiomeArray = largestBiomeID + 1;
                biomesChanged = true;
            }
        }

        boolean changed = this.resourcePacksChanged || biomesChanged;
        this.resourcePacksChanged = false;
        if (changed) {
            this.loadColors();
        }

        return changed;
    }

    private void loadColors() {
        this.loadedTerrainImage = false;
        BlockDatabase.getBlocks();
        this.loadColorPicker();
        this.loadTexturePackTerrainImage();
        TextureAtlasSprite missing = MapViewConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(ResourceLocation.parse("missingno"));
        this.failedToLoadX = missing.getU0();
        this.failedToLoadY = missing.getV0();
        this.loaded = false;  // NOPMD

        try {
            Arrays.fill(this.blockColors, 0xFEFF00FF);
            Arrays.fill(this.blockColorsWithDefaultTint, 0xFEFF00FF);
            this.loadSpecialColors();
            this.optifineLoader.clear();
            if (this.optifineLoader.isInstalled()) {
                try {
                    this.optifineLoader.processCTM();
                } catch (Exception var4) {
                    MapViewConstants.getLogger().error("error loading CTM " + var4.getLocalizedMessage(), var4);
                }

                try {
                    this.optifineLoader.processColorProperties();
                } catch (Exception var3) {
                    MapViewConstants.getLogger().error("error loading custom color properties " + var3.getLocalizedMessage(), var3);
                }
            }

            MapViewConstants.getLightMapInstance().getMap().forceFullRender(true);
        } catch (Exception var5) {
            MapViewConstants.getLogger().error("error loading pack", var5);
        }

        this.loaded = true;
    }

    private void loadColorPicker() {
        try (InputStream is = MapViewConstants.getMinecraft().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("schedulemc", "mapview/images/colorpicker.png")).orElseThrow(() -> new IOException("Resource not found: schedulemc:mapview/images/colorpicker.png")).open()) {
            Image picker = ImageIO.read(is);
            this.colorPicker = new BufferedImage(picker.getWidth(null), picker.getHeight(null), 2);
            Graphics gfx = this.colorPicker.createGraphics();
            gfx.drawImage(picker, 0, 0, null);
            gfx.dispose();
        } catch (Exception var4) {
            MapViewConstants.getLogger().error("Error loading color picker: " + var4.getLocalizedMessage());
        }

    }

    public void setSkyColor(int skyColor) {
        this.blockColors[BlockDatabase.airID] = skyColor;
        this.blockColors[BlockDatabase.voidAirID] = skyColor;
        this.blockColors[BlockDatabase.caveAirID] = skyColor;
    }

    private void loadTexturePackTerrainImage() {
        GLUtils.readTextureContentsToBufferedImage(MapViewConstants.getMinecraft().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getId(), image -> {
            terrainBuff = image;
            loadedTerrainImage = true;
        });
    }

    private void loadSpecialColors() {
        int blockStateID;
        for (Iterator<BlockState> blockStateIterator = BlockDatabase.pistonTechBlock.getStateDefinition().getPossibleStates().iterator(); blockStateIterator.hasNext(); this.blockColors[blockStateID] = 0) {
            BlockState blockState = blockStateIterator.next();
            blockStateID = BlockDatabase.getStateId(blockState);
        }

        for (Iterator<BlockState> var6 = BlockDatabase.barrier.getStateDefinition().getPossibleStates().iterator(); var6.hasNext(); this.blockColors[blockStateID] = 0) {
            BlockState blockState = var6.next();
            blockStateID = BlockDatabase.getStateId(blockState);
        }

    }

    public final int getBlockColorWithDefaultTint(MutableBlockPos blockPos, int blockStateID) {
        if (this.loaded && loadedTerrainImage) {
            int col = 0x1B000000;  // NOPMD

            try {
                col = this.blockColorsWithDefaultTint[blockStateID];
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            return ARGBCompat.toABGR(col != 0xFEFF00FF ? col : this.getBlockColor(blockPos, blockStateID));
        } else {
            return 0;
        }
    }

    public final int getBlockColor(MutableBlockPos blockPos, int blockStateID, Biome biomeID) {
        if (this.loaded && loadedTerrainImage) {
            if (this.optifineLoader.isInstalled() && this.optifineLoader.getBiomeTextureAvailable().contains(blockStateID)) {
                Integer col = this.optifineLoader.getBlockBiomeSpecificColors().get(blockStateID + " " + biomeID);
                if (col != null) {
                    return ARGBCompat.toABGR(col);
                }
            }

            return ARGBCompat.toABGR(this.getBlockColor(blockPos, blockStateID));
        } else {
            return 0;
        }
    }

    int getBlockColor(int blockStateID) {
        return this.getBlockColor(this.dummyBlockPos, blockStateID);
    }

    /**
     * SICHERHEIT: Synchronized für Thread-safe Array-Zugriff während Resize
     */
    private int getBlockColor(MutableBlockPos blockPos, int blockStateID) {
        synchronized (this) {
            int col = 0x1B000000;  // NOPMD

            // Nach Synchronisierung ist Array-Zugriff sicher
            if (blockStateID >= this.blockColors.length) {
                this.resizeColorArrays(blockStateID);
            }

            col = this.blockColors[blockStateID];

            if (col == 0xFEFF00FF || col == 0x1B000000) {
                BlockState blockState = BlockDatabase.getStateById(blockStateID);
                col = this.blockColors[blockStateID] = this.getColor(blockPos, blockState);
            }

            return col;
        }
    }

    private void resizeColorArrays(int queriedID) {
        if (queriedID >= this.blockColors.length) {
            // Performance-Optimierung: Wachse direkt auf benötigte Größe + Puffer
            // Vorher: Verdoppelte Größe jedes Mal → mehrere Resizes nötig
            // Jetzt: Wachse auf queriedID + 1024 Puffer → weniger Resizes
            int newSize = Math.max(queriedID + 1024, this.blockColors.length * 2);
            int[] newBlockColors = new int[newSize];
            int[] newBlockColorsWithDefaultTint = new int[newSize];
            System.arraycopy(this.blockColors, 0, newBlockColors, 0, this.blockColors.length);
            System.arraycopy(this.blockColorsWithDefaultTint, 0, newBlockColorsWithDefaultTint, 0, this.blockColorsWithDefaultTint.length);
            Arrays.fill(newBlockColors, this.blockColors.length, newBlockColors.length, 0xFEFF00FF);
            Arrays.fill(newBlockColorsWithDefaultTint, this.blockColorsWithDefaultTint.length, newBlockColorsWithDefaultTint.length, 0xFEFF00FF);
            this.blockColors = newBlockColors;
            this.blockColorsWithDefaultTint = newBlockColorsWithDefaultTint;
        }

    }

    private int getColor(MutableBlockPos blockPos, BlockState state) {
        try {
            int color = this.getColorForBlockPosBlockStateAndFacing(blockPos, state, Direction.UP);
            if (color == 0x1B000000) {
                BlockRenderDispatcher blockRendererDispatcher = MapViewConstants.getMinecraft().getBlockRenderer();
                color = this.getColorForTerrainSprite(state, blockRendererDispatcher);
            }

            Block block = state.getBlock();
            if (block == BlockDatabase.cobweb) {
                color |= -16777216;
            }

            if (block == BlockDatabase.redstone) {
                color = ColorUtils.colorMultiplier(color, MapViewConstants.getMinecraft().getBlockColors().getColor(state, null, null, 0) | 0xFF000000);
            }

            if (BlockDatabase.biomeBlocks.contains(block)) {
                this.applyDefaultBuiltInShading(state, color);
            } else {
                this.checkForBiomeTinting(blockPos, state, color);
            }

            if (BlockDatabase.shapedBlocks.contains(block)) {
                color = this.applyShape(block, color);
            }

            if ((color >> 24 & 0xFF) < 27) {
                color |= 0x1B000000;
            }
            return color;
        } catch (Exception var5) {
            MapViewConstants.getLogger().error("failed getting color: " + state.getBlock().getName().getString(), var5);
            return 0x1B000000;
        }
    }

    private int getColorForBlockPosBlockStateAndFacing(BlockPos _blockPos, BlockState blockState, Direction facing) {
        int color = 0x1B000000;

        try {
            RenderShape blockRenderType = blockState.getRenderShape();
            BlockRenderDispatcher blockRendererDispatcher = MapViewConstants.getMinecraft().getBlockRenderer();
            if (blockRenderType == RenderShape.MODEL) {
                BakedModel iBakedModel = blockRendererDispatcher.getBlockModel(blockState);
                List<BakedQuad> quads = new ArrayList<>();

                // In 1.20.1, get quads directly from BakedModel
                quads.addAll(iBakedModel.getQuads(blockState, facing, this.random));
                quads.addAll(iBakedModel.getQuads(blockState, null, this.random));
                BlockModel model = new BlockModel(quads, this.failedToLoadX, this.failedToLoadY);
                if (model.numberOfFaces() > 0) {
                    BufferedImage modelImage = model.getImage(this.terrainBuff);
                    if (modelImage != null) {
                        color = this.getColorForCoordinatesAndImage(new float[]{0.0F, 1.0F, 0.0F, 1.0F}, modelImage);
                    } else {
                        MapViewConstants.getLogger().warn(String.format("Block texture for block %s is missing!", BuiltInRegistries.BLOCK.getKey(blockState.getBlock())));
                    }
                }
            }
        } catch (Exception var11) {
            MapViewConstants.getLogger().error(var11.getMessage(), var11);
        }

        return color;
    }

    private int getColorForTerrainSprite(BlockState blockState, BlockRenderDispatcher blockRendererDispatcher) {
        BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
        TextureAtlasSprite icon = blockModelShapes.getParticleIcon(blockState);
        if (icon == blockModelShapes.getModelManager().getMissingModel().getParticleIcon()) {
            Block block = blockState.getBlock();
            Block material = blockState.getBlock();
            if (block instanceof LiquidBlock) {
                if (material == Blocks.WATER) {
                    icon = MapViewConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(ResourceLocation.parse("minecraft:blocks/water_flow"));
                } else if (material == Blocks.LAVA) {
                    icon = MapViewConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(ResourceLocation.parse("minecraft:blocks/lava_flow"));
                }
            } else if (material == Blocks.WATER) {
                icon = MapViewConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(ResourceLocation.parse("minecraft:blocks/water_still"));
            } else if (material == Blocks.LAVA) {
                icon = MapViewConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(ResourceLocation.parse("minecraft:blocks/lava_still"));
            }
        }

        return this.getColorForIcon(icon);
    }

    private int getColorForIcon(TextureAtlasSprite icon) {
        int color = 0x1B000000;
        if (icon != null) {
            float left = icon.getU0();
            float right = icon.getU1();
            float top = icon.getV0();
            float bottom = icon.getV1();
            color = this.getColorForCoordinatesAndImage(new float[]{left, right, top, bottom}, this.terrainBuff);
        }

        return color;
    }

    private int getColorForCoordinatesAndImage(float[] uv, BufferedImage imageBuff) {
        int color = 0x1B000000;
        if (uv[0] != this.failedToLoadX || uv[2] != this.failedToLoadY) {
            int left = (int) (uv[0] * imageBuff.getWidth());
            int right = (int) Math.ceil(uv[1] * imageBuff.getWidth());
            int top = (int) (uv[2] * imageBuff.getHeight());
            int bottom = (int) Math.ceil(uv[3] * imageBuff.getHeight());

            try {
                BufferedImage blockTexture = imageBuff.getSubimage(left, top, right - left, bottom - top);
                Image singlePixel = blockTexture.getScaledInstance(1, 1, 4);
                BufferedImage singlePixelBuff = new BufferedImage(1, 1, imageBuff.getType());
                Graphics gfx = singlePixelBuff.createGraphics();
                gfx.drawImage(singlePixel, 0, 0, null);
                gfx.dispose();
                color = singlePixelBuff.getRGB(0, 0);
            } catch (RasterFormatException var12) {
                MapViewConstants.getLogger().warn("error getting color");
                MapViewConstants.getLogger().warn(IntStream.of(left, right, top, bottom).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
            }
        }

        return color;
    }

    private void applyDefaultBuiltInShading(BlockState blockState, int color) {
        Block block = blockState.getBlock();
        int blockStateID = BlockDatabase.getStateId(blockState);
        if (block != BlockDatabase.largeFern && block != BlockDatabase.tallGrass && block != BlockDatabase.reeds) {
            if (block == BlockDatabase.water) {
                // Fixed blue color for water (Minecraft default: #3F76E4)
                this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, 0xFF3F76E4);
            } else if (block == Blocks.LAVA) {
                // Fixed orange/red color for lava (Minecraft default: #FF5A00)
                this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, 0xFFFF5A00);
            } else {
                this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, MapViewConstants.getMinecraft().getBlockColors().getColor(blockState, null, null, 0) | 0xFF000000);
            }
        } else {
            this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, GrassColor.get(0.7, 0.8) | 0xFF000000);
        }

    }

    void checkForBiomeTinting(MutableBlockPos blockPos, BlockState blockState, int color) {
        try {
            Block block = blockState.getBlock();
            String blockName = String.valueOf(BuiltInRegistries.BLOCK.getKey(block));
            if (BlockDatabase.biomeBlocks.contains(block) || !blockName.startsWith("minecraft:")) {
                int tint;
                MutableBlockPos tempBlockPos = new MutableBlockPos(0, 0, 0);
                if (blockPos == this.dummyBlockPos) {
                    tint = this.tintFromFakePlacedBlock(blockState, tempBlockPos, null); // Biome 4?
                } else {
                    ClientLevel clientWorld = MapViewConstants.getClientWorld();

                    ChunkAccess chunk = clientWorld.getChunk(blockPos);
                    if (chunk != null && !((LevelChunk) chunk).isEmpty() && clientWorld.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
                        tint = MapViewConstants.getMinecraft().getBlockColors().getColor(blockState, clientWorld, blockPos, 1) | 0xFF000000;
                    } else {
                        tint = this.tintFromFakePlacedBlock(blockState, tempBlockPos, null); // Biome 4?
                    }
                }

                if (tint != 16777215 && tint != -1) {
                    int blockStateID = BlockDatabase.getStateId(blockState);
                    this.optifineLoader.getBiomeTintsAvailable().add(blockStateID);
                    this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, tint);
                } else {
                    this.blockColorsWithDefaultTint[BlockDatabase.getStateId(blockState)] = 0x1B000000;
                }
            }
        } catch (Exception ignored) {
        }

    }

    private int tintFromFakePlacedBlock(BlockState _blockState, MutableBlockPos _loopBlockPos, Biome _biomeID) {
        return -1;
    }

    public int getBiomeTint(AbstractMapData mapData, Level world, BlockState blockState, int blockStateID, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ) {
        // IMPORTANT: Water and lava should ALWAYS use fixed colors, ignoring biome tints
        // This prevents brown water in swamps and ensures consistent fluid colors
        Block block = blockState.getBlock();
        if (block == BlockDatabase.water) {
            return ARGBCompat.toABGR(0xFF3F76E4); // Fixed Minecraft default blue water color
        }
        if (block == Blocks.LAVA) {
            return ARGBCompat.toABGR(0xFFFF5A00); // Fixed Minecraft default orange/red lava color
        }

        // Performance-Optimierung: LRU Cache für Biome Tints
        // Cache-Key: ChunkX (16 bit) | ChunkZ (16 bit) | BlockStateID (32 bit)
        long cacheKey = ((long)(blockPos.getX() >> 4) << 48) | ((long)(blockPos.getZ() >> 4) << 32) | (blockStateID & 0xFFFFFFFFL);
        Integer cachedTint = biomeTintCache.get(cacheKey);
        if (cachedTint != null) {
            return cachedTint; // Cache Hit - spart 9 Biome-Lookups!
        }

        ChunkAccess chunk = world.getChunk(blockPos);
        boolean live = chunk != null && !((LevelChunk) chunk).isEmpty() && MapViewConstants.getPlayer().level().hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        int tint = -2;
        if (this.optifineLoader.isInstalled() || !live && this.optifineLoader.getBiomeTintsAvailable().contains(blockStateID)) {
            try {
                int[][] tints = this.optifineLoader.getBlockTintTables().get(blockStateID);
                if (tints != null) {
                    int r = 0;
                    int g = 0;
                    int b = 0;

                    for (int t = blockPos.getX() - 1; t <= blockPos.getX() + 1; ++t) {
                        for (int s = blockPos.getZ() - 1; s <= blockPos.getZ() + 1; ++s) {
                            Biome biome;
                            if (live) {
                                biome = world.getBiome(loopBlockPos.withXYZ(t, blockPos.getY(), s)).value();
                            } else {
                                int dataX = t - startX;
                                int dataZ = s - startZ;
                                dataX = Math.max(dataX, 0);
                                dataX = Math.min(dataX, mapData.getWidth() - 1);
                                dataZ = Math.max(dataZ, 0);
                                dataZ = Math.min(dataZ, mapData.getHeight() - 1);
                                biome = mapData.getBiome(dataX, dataZ);
                            }

                            if (biome == null) {
                                biome = world.registryAccess().registryOrThrow(Registries.BIOME).get(Biomes.PLAINS);
                            }
                            int biomeID = world.registryAccess().registryOrThrow(Registries.BIOME).getId(biome);
                            int biomeTint = tints[biomeID][loopBlockPos.y / 8];
                            r += (biomeTint & 0xFF0000) >> 16;
                            g += (biomeTint & 0xFF00) >> 8;
                            b += biomeTint & 0xFF;
                        }
                    }

                    tint = 0xFF000000 | (r / 9 & 0xFF) << 16 | (g / 9 & 0xFF) << 8 | b / 9 & 0xFF;
                }
            } catch (Exception ignored) {
            }
        }

        if (tint == -2) {
            tint = this.getBuiltInBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ, live);
        }

        // Performance-Optimierung: Speichere berechneten Wert im Cache
        int result = ARGBCompat.toABGR(tint);
        biomeTintCache.put(cacheKey, result);
        return result;
    }

    private int getBuiltInBiomeTint(AbstractMapData mapData, Level world, BlockState blockState, int blockStateID, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ, boolean live) {
        int tint = -1;
        Block block = blockState.getBlock();

        // IMPORTANT: Water and lava should ALWAYS use fixed colors, ignoring biome tints
        // This prevents brown water in swamps and ensures consistent fluid colors
        if (block == BlockDatabase.water) {
            return 0xFF3F76E4; // Fixed Minecraft default blue water color
        }
        if (block == Blocks.LAVA) {
            return 0xFFFF5A00; // Fixed Minecraft default orange/red lava color
        }

        if (BlockDatabase.biomeBlocks.contains(block) || this.optifineLoader.getBiomeTintsAvailable().contains(blockStateID)) {
            if (live) {
                try {
                    DebugRenderState.blockX = blockPos.x;
                    DebugRenderState.blockY = blockPos.y;
                    DebugRenderState.blockZ = blockPos.z;
                    tint = MapViewConstants.getMinecraft().getBlockColors().getColor(blockState, world, blockPos, 0) | 0xFF000000;
                } catch (Exception ignored) {
                }
            }

            if (tint == -1) {
                tint = this.getBuiltInBiomeTintFromUnloadedChunk(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ) | 0xFF000000;
            }
        }

        return tint;
    }

    private int getBuiltInBiomeTintFromUnloadedChunk(AbstractMapData mapData, Level world, BlockState blockState, int blockStateID, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ) {
        int tint = -1;
        Block block = blockState.getBlock();
        ColorResolver colorResolver = null;
        if (block == BlockDatabase.water) {
            colorResolver = this.waterColorResolver;
        } else if (block == Blocks.LAVA) {
            colorResolver = this.lavaColorResolver;
        } else if (block == BlockDatabase.spruceLeaves) {
            colorResolver = this.spruceColorResolver;
        } else if (block == BlockDatabase.birchLeaves) {
            colorResolver = this.birchColorResolver;
        } else if (block == BlockDatabase.mangroveLeaves) {
            colorResolver = this.mangroveColorResolver;
        } else {
            boolean isFoliage = block == BlockDatabase.oakLeaves || block == BlockDatabase.jungleLeaves  || block == BlockDatabase.acaciaLeaves || block == BlockDatabase.darkOakLeaves || block == BlockDatabase.vine;
            boolean isDryFoliage = block == BlockDatabase.leafLitter;
            if (isFoliage) {
                colorResolver = this.foliageColorResolver;
            } else if (isDryFoliage) {
                colorResolver = this.dryFoliageColorResolver;
            } else if (block == BlockDatabase.redstone) {
                colorResolver = this.redstoneColorResolver;
            } else if (BlockDatabase.biomeBlocks.contains(block)) {
                colorResolver = this.grassColorResolver;
            }
        }

        if (colorResolver != null) {
            int r = 0;
            int g = 0;
            int b = 0;

            for (int t = blockPos.getX() - 1; t <= blockPos.getX() + 1; ++t) {
                for (int s = blockPos.getZ() - 1; s <= blockPos.getZ() + 1; ++s) {
                    int dataX = t - startX;
                    int dataZ = s - startZ;
                    dataX = Math.max(dataX, 0);
                    dataX = Math.min(dataX, 255);
                    dataZ = Math.max(dataZ, 0);
                    dataZ = Math.min(dataZ, 255);
                    Biome biome = mapData.getBiome(dataX, dataZ);
                    if (biome == null) {
                        MessageUtils.printDebug("Null biome ID! " + " at " + t + "," + s);
                        MessageUtils.printDebug("block: " + mapData.getBlockstate(dataX, dataZ) + ", height: " + mapData.getHeight(dataX, dataZ));
                        MessageUtils.printDebug("Mapdata: " + mapData);
                    }

                    int biomeTint = biome == null ? 0 : colorResolver.getColorAtPos(blockState, biome, loopBlockPos.withXYZ(t, blockPos.getY(), s));
                    r += (biomeTint & 0xFF0000) >> 16;
                    g += (biomeTint & 0xFF00) >> 8;
                    b += biomeTint & 0xFF;
                }
            }

            tint = (r / 9 & 0xFF) << 16 | (g / 9 & 0xFF) << 8 | b / 9 & 0xFF;
        } else if (this.optifineLoader.getBiomeTintsAvailable().contains(blockStateID)) {
            tint = this.getCustomBlockBiomeTintFromUnloadedChunk(mapData, world, blockState, blockPos, loopBlockPos, startX, startZ);
        }

        return tint;
    }

    private int getCustomBlockBiomeTintFromUnloadedChunk(AbstractMapData mapData, Level _world, BlockState blockState, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ) {
        int tint;

        try {
            int dataX = blockPos.getX() - startX;
            int dataZ = blockPos.getZ() - startZ;
            dataX = Math.max(dataX, 0);
            dataX = Math.min(dataX, mapData.getWidth() - 1);
            dataZ = Math.max(dataZ, 0);
            dataZ = Math.min(dataZ, mapData.getHeight() - 1);
            Biome biome = mapData.getBiome(dataX, dataZ);
            tint = this.tintFromFakePlacedBlock(blockState, loopBlockPos, biome);
        } catch (Exception var12) {
            tint = -1;
        }

        return tint;
    }

    int applyShape(Block block, int color) {
        int alpha = color >> 24 & 0xFF;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        if (block instanceof SignBlock) {
            alpha = 31;
        } else if (block instanceof DoorBlock) {
            alpha = 47;
        } else if (block == BlockDatabase.ladder || block == BlockDatabase.vine) {
            alpha = 15;
        }

        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    @FunctionalInterface
    private interface ColorResolver {
        int getColorAtPos(BlockState state, Biome biome, BlockPos pos);
    }
}
