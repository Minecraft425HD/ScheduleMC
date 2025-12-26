package de.rolandsw.schedulemc.lightmap;

import com.google.common.collect.UnmodifiableIterator;
import de.rolandsw.schedulemc.lightmap.interfaces.AbstractMapData;
import de.rolandsw.schedulemc.lightmap.util.BlockModel;
import de.rolandsw.schedulemc.lightmap.util.BlockDatabase;
import de.rolandsw.schedulemc.lightmap.util.ColorUtils;
import de.rolandsw.schedulemc.lightmap.util.GLUtils;
import de.rolandsw.schedulemc.lightmap.util.MessageUtils;
import de.rolandsw.schedulemc.lightmap.util.MutableBlockPos;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import de.rolandsw.schedulemc.lightmap.util.ARGBCompat;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BlockColorCache {
    private boolean resourcePacksChanged;
    private ClientLevel world;
    private BufferedImage terrainBuff;
    private BufferedImage colorPicker;
    private int sizeOfBiomeArray;
    private int[] blockColors = new int[16384];
    private int[] blockColorsWithDefaultTint = new int[16384];
    private final HashSet<Integer> biomeTintsAvailable = new HashSet<>();
    private boolean optifineInstalled;
    private final HashMap<Integer, int[][]> blockTintTables = new HashMap<>();
    private final HashSet<Integer> biomeTextureAvailable = new HashSet<>();
    private final HashMap<String, Integer> blockBiomeSpecificColors = new HashMap<>();
    private float failedToLoadX;
    private float failedToLoadY;
    private String renderPassThreeBlendMode;
    private final RandomSource random = RandomSource.create();
    private boolean loaded;
    private boolean loadedTerrainImage;
    private final MutableBlockPos dummyBlockPos = new MutableBlockPos(BlockPos.ZERO.getX(), BlockPos.ZERO.getY(), BlockPos.ZERO.getZ());
    private final ColorResolver spruceColorResolver = (blockState, biomex, blockPos) -> FoliageColor.getEvergreenColor();
    private final ColorResolver birchColorResolver = (blockState, biomex, blockPos) -> FoliageColor.getBirchColor();
    private final ColorResolver mangroveColorResolver = (blockState, biomex, blockPos) -> FoliageColor.getMangroveColor();
    private final ColorResolver grassColorResolver = (blockState, biomex, blockPos) -> biomex.getGrassColor(blockPos.getX(), blockPos.getZ());
    private final ColorResolver foliageColorResolver = (blockState, biomex, blockPos) -> biomex.getFoliageColor();
    private final ColorResolver dryFoliageColorResolver = (blockState, biomex, blockPos) -> biomex.getFoliageColor();
    private final ColorResolver waterColorResolver = (blockState, biomex, blockPos) -> biomex.getWaterColor();
    private final ColorResolver redstoneColorResolver = (blockState, biomex, blockPos) -> RedStoneWireBlock.getColorForPower(blockState.getValue(RedStoneWireBlock.POWER));

    public BlockColorCache() {
        this.optifineInstalled = false;
        Field ofProfiler = null;

        try {
            ofProfiler = Options.class.getDeclaredField("ofProfiler");
        } catch (SecurityException | NoSuchFieldException ignored) {
        } finally {
            if (ofProfiler != null) {
                this.optifineInstalled = true;
            }

        }

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

        if (LightMapConstants.getClientWorld() != this.world) {
            this.world = LightMapConstants.getClientWorld();
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
        TextureAtlasSprite missing = LightMapConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(new ResourceLocation("missingno"));
        this.failedToLoadX = missing.getU0();
        this.failedToLoadY = missing.getV0();
        this.loaded = false;

        try {
            Arrays.fill(this.blockColors, 0xFEFF00FF);
            Arrays.fill(this.blockColorsWithDefaultTint, 0xFEFF00FF);
            this.loadSpecialColors();
            this.biomeTintsAvailable.clear();
            this.biomeTextureAvailable.clear();
            this.blockBiomeSpecificColors.clear();
            this.blockTintTables.clear();
            if (this.optifineInstalled) {
                try {
                    this.processCTM();
                } catch (Exception var4) {
                    LightMapConstants.getLogger().error("error loading CTM " + var4.getLocalizedMessage(), var4);
                }

                try {
                    this.processColorProperties();
                } catch (Exception var3) {
                    LightMapConstants.getLogger().error("error loading custom color properties " + var3.getLocalizedMessage(), var3);
                }
            }

            LightMapConstants.getLightMapInstance().getMap().forceFullRender(true);
        } catch (Exception var5) {
            LightMapConstants.getLogger().error("error loading pack", var5);
        }

        this.loaded = true;
    }

    private void loadColorPicker() {
        try {
            InputStream is = LightMapConstants.getMinecraft().getResourceManager().getResource(new ResourceLocation("schedulemc", "lightmap/images/colorpicker.png")).get().open();
            Image picker = ImageIO.read(is);
            is.close();
            this.colorPicker = new BufferedImage(picker.getWidth(null), picker.getHeight(null), 2);
            Graphics gfx = this.colorPicker.createGraphics();
            gfx.drawImage(picker, 0, 0, null);
            gfx.dispose();
        } catch (Exception var4) {
            LightMapConstants.getLogger().error("Error loading color picker: " + var4.getLocalizedMessage());
        }

    }

    public void setSkyColor(int skyColor) {
        this.blockColors[BlockDatabase.airID] = skyColor;
        this.blockColors[BlockDatabase.voidAirID] = skyColor;
        this.blockColors[BlockDatabase.caveAirID] = skyColor;
    }

    private void loadTexturePackTerrainImage() {
        GLUtils.readTextureContentsToBufferedImage(LightMapConstants.getMinecraft().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getId(), image -> {
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
            int col = 0x1B000000;

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
            if (this.optifineInstalled && this.biomeTextureAvailable.contains(blockStateID)) {
                Integer col = this.blockBiomeSpecificColors.get(blockStateID + " " + biomeID);
                if (col != null) {
                    return ARGBCompat.toABGR(col);
                }
            }

            return ARGBCompat.toABGR(this.getBlockColor(blockPos, blockStateID));
        } else {
            return 0;
        }
    }

    private int getBlockColor(int blockStateID) {
        return this.getBlockColor(this.dummyBlockPos, blockStateID);
    }

    private int getBlockColor(MutableBlockPos blockPos, int blockStateID) {
        int col = 0x1B000000;

        try {
            col = this.blockColors[blockStateID];
        } catch (ArrayIndexOutOfBoundsException var5) {
            this.resizeColorArrays(blockStateID);
        }

        if (col == 0xFEFF00FF || col == 0x1B000000) {
            BlockState blockState = BlockDatabase.getStateById(blockStateID);
            col = this.blockColors[blockStateID] = this.getColor(blockPos, blockState);
        }

        return col;
    }

    private synchronized void resizeColorArrays(int queriedID) {
        if (queriedID >= this.blockColors.length) {
            int[] newBlockColors = new int[this.blockColors.length * 2];
            int[] newBlockColorsWithDefaultTint = new int[this.blockColors.length * 2];
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
                BlockRenderDispatcher blockRendererDispatcher = LightMapConstants.getMinecraft().getBlockRenderer();
                color = this.getColorForTerrainSprite(state, blockRendererDispatcher);
            }

            Block block = state.getBlock();
            if (block == BlockDatabase.cobweb) {
                color |= -16777216;
            }

            if (block == BlockDatabase.redstone) {
                color = ColorUtils.colorMultiplier(color, LightMapConstants.getMinecraft().getBlockColors().getColor(state, null, null, 0) | 0xFF000000);
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
            LightMapConstants.getLogger().error("failed getting color: " + state.getBlock().getName().getString(), var5);
            return 0x1B000000;
        }
    }

    private int getColorForBlockPosBlockStateAndFacing(BlockPos blockPos, BlockState blockState, Direction facing) {
        int color = 0x1B000000;

        try {
            RenderShape blockRenderType = blockState.getRenderShape();
            BlockRenderDispatcher blockRendererDispatcher = LightMapConstants.getMinecraft().getBlockRenderer();
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
                        LightMapConstants.getLogger().warn(String.format("Block texture for block %s is missing!", BuiltInRegistries.BLOCK.getKey(blockState.getBlock())));
                    }
                }
            }
        } catch (Exception var11) {
            LightMapConstants.getLogger().error(var11.getMessage(), var11);
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
                    icon = LightMapConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(new ResourceLocation("minecraft:blocks/water_flow"));
                } else if (material == Blocks.LAVA) {
                    icon = LightMapConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(new ResourceLocation("minecraft:blocks/lava_flow"));
                }
            } else if (material == Blocks.WATER) {
                icon = LightMapConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(new ResourceLocation("minecraft:blocks/water_still"));
            } else if (material == Blocks.LAVA) {
                icon = LightMapConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(new ResourceLocation("minecraft:blocks/lava_still"));
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
                LightMapConstants.getLogger().warn("error getting color");
                LightMapConstants.getLogger().warn(IntStream.of(left, right, top, bottom).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
            }
        }

        return color;
    }

    private void applyDefaultBuiltInShading(BlockState blockState, int color) {
        Block block = blockState.getBlock();
        int blockStateID = BlockDatabase.getStateId(blockState);
        if (block != BlockDatabase.largeFern && block != BlockDatabase.tallGrass && block != BlockDatabase.reeds) {
            if (block == BlockDatabase.water) {
                this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, 0xFF3F76E4);
            } else {
                this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, LightMapConstants.getMinecraft().getBlockColors().getColor(blockState, null, null, 0) | 0xFF000000);
            }
        } else {
            this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, GrassColor.get(0.7, 0.8) | 0xFF000000);
        }

    }

    private void checkForBiomeTinting(MutableBlockPos blockPos, BlockState blockState, int color) {
        try {
            Block block = blockState.getBlock();
            String blockName = String.valueOf(BuiltInRegistries.BLOCK.getKey(block));
            if (BlockDatabase.biomeBlocks.contains(block) || !blockName.startsWith("minecraft:")) {
                int tint;
                MutableBlockPos tempBlockPos = new MutableBlockPos(0, 0, 0);
                if (blockPos == this.dummyBlockPos) {
                    tint = this.tintFromFakePlacedBlock(blockState, tempBlockPos, null); // Biome 4?
                } else {
                    ClientLevel clientWorld = LightMapConstants.getClientWorld();

                    ChunkAccess chunk = clientWorld.getChunk(blockPos);
                    if (chunk != null && !((LevelChunk) chunk).isEmpty() && clientWorld.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
                        tint = LightMapConstants.getMinecraft().getBlockColors().getColor(blockState, clientWorld, blockPos, 1) | 0xFF000000;
                    } else {
                        tint = this.tintFromFakePlacedBlock(blockState, tempBlockPos, null); // Biome 4?
                    }
                }

                if (tint != 16777215 && tint != -1) {
                    int blockStateID = BlockDatabase.getStateId(blockState);
                    this.biomeTintsAvailable.add(blockStateID);
                    this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, tint);
                } else {
                    this.blockColorsWithDefaultTint[BlockDatabase.getStateId(blockState)] = 0x1B000000;
                }
            }
        } catch (Exception ignored) {
        }

    }

    private int tintFromFakePlacedBlock(BlockState blockState, MutableBlockPos loopBlockPos, Biome biomeID) {
        return -1;
    }

    public int getBiomeTint(AbstractMapData mapData, Level world, BlockState blockState, int blockStateID, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ) {
        ChunkAccess chunk = world.getChunk(blockPos);
        boolean live = chunk != null && !((LevelChunk) chunk).isEmpty() && LightMapConstants.getPlayer().level().hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        int tint = -2;
        if (this.optifineInstalled || !live && this.biomeTintsAvailable.contains(blockStateID)) {
            try {
                int[][] tints = this.blockTintTables.get(blockStateID);
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

        return ARGBCompat.toABGR(tint);
    }

    private int getBuiltInBiomeTint(AbstractMapData mapData, Level world, BlockState blockState, int blockStateID, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ, boolean live) {
        int tint = -1;
        Block block = blockState.getBlock();
        if (BlockDatabase.biomeBlocks.contains(block) || this.biomeTintsAvailable.contains(blockStateID)) {
            if (live) {
                try {
                    DebugRenderState.blockX = blockPos.x;
                    DebugRenderState.blockY = blockPos.y;
                    DebugRenderState.blockZ = blockPos.z;
                    tint = LightMapConstants.getMinecraft().getBlockColors().getColor(blockState, world, blockPos, 0) | 0xFF000000;
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
        } else if (this.biomeTintsAvailable.contains(blockStateID)) {
            tint = this.getCustomBlockBiomeTintFromUnloadedChunk(mapData, world, blockState, blockPos, loopBlockPos, startX, startZ);
        }

        return tint;
    }

    private int getCustomBlockBiomeTintFromUnloadedChunk(AbstractMapData mapData, Level world, BlockState blockState, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ) {
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

    private int applyShape(Block block, int color) {
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

    private void processCTM() {
        this.renderPassThreeBlendMode = "alpha";
        Properties properties = new Properties();
        ResourceLocation propertiesFile = new ResourceLocation("minecraft", "optifine/renderpass.properties");

        try {
            InputStream input = LightMapConstants.getMinecraft().getResourceManager().getResource(propertiesFile).get().open();
            if (input != null) {
                properties.load(input);
                input.close();
                this.renderPassThreeBlendMode = properties.getProperty("blend.3", "alpha");
            }
        } catch (IOException var9) {
            this.renderPassThreeBlendMode = "alpha";
        }

        String namespace = "minecraft";

        for (ResourceLocation s : this.findResources(namespace, "/optifine/ctm", ".properties", true, false, true)) {
            try {
                this.loadCTM(s);
            } catch (IllegalArgumentException ignored) {
            }
        }

        for (int t = 0; t < this.blockColors.length; ++t) {
            if (this.blockColors[t] != 0x1B000000 && this.blockColors[t] != 0xFEFF00FF) {
                if ((this.blockColors[t] >> 24 & 0xFF) < 27) {
                    this.blockColors[t] |= 0x1B000000;
                }

                this.checkForBiomeTinting(this.dummyBlockPos, BlockDatabase.getStateById(t), this.blockColors[t]);
            }
        }

    }

    private void loadCTM(ResourceLocation propertiesFile) {
        if (propertiesFile != null) {
            BlockRenderDispatcher blockRendererDispatcher = LightMapConstants.getMinecraft().getBlockRenderer();
            BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
            Properties properties = new Properties();

            try {
                InputStream input = LightMapConstants.getMinecraft().getResourceManager().getResource(propertiesFile).get().open();
                if (input != null) {
                    properties.load(input);
                    input.close();
                }
            } catch (IOException var39) {
                return;
            }

            String filePath = propertiesFile.getPath();
            String method = properties.getProperty("method", "").trim().toLowerCase();
            String faces = properties.getProperty("faces", "").trim().toLowerCase();
            String matchBlocks = properties.getProperty("matchBlocks", "").trim().toLowerCase();
            String matchTiles = properties.getProperty("matchTiles", "").trim().toLowerCase();
            String metadata = properties.getProperty("metadata", "").trim().toLowerCase();
            String tiles = properties.getProperty("tiles", "").trim();
            String biomes = properties.getProperty("biomes", "").trim().toLowerCase();
            String renderPass = properties.getProperty("renderPass", "").trim().toLowerCase();
            metadata = metadata.replaceAll("\\s+", ",");
            Set<BlockState> blockStates = new HashSet<>(this.parseBlocksList(matchBlocks, metadata));
            String directory = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            String[] tilesParsed = this.parseStringList(tiles);
            String tilePath = directory + "0";
            if (tilesParsed.length > 0) {
                tilePath = tilesParsed[0].trim();
            }

            if (tilePath.startsWith("~")) {
                tilePath = tilePath.replace("~", "optifine");
            } else if (!tilePath.contains("/")) {
                tilePath = directory + tilePath;
            }

            if (!tilePath.toLowerCase().endsWith(".png")) {
                tilePath = tilePath + ".png";
            }

            String[] biomesArray = biomes.split(" ");
            if (blockStates.isEmpty()) {
                Block block;
                Pattern pattern = Pattern.compile(".*/block_(.+).properties");
                Matcher matcher = pattern.matcher(filePath);
                if (matcher.find()) {
                    block = this.getBlockFromName(matcher.group(1));
                    if (block != null) {
                        Set<BlockState> matching = this.parseBlockMetadata(block, metadata);
                        if (matching.isEmpty()) {
                            matching.addAll(block.getStateDefinition().getPossibleStates());
                        }

                        blockStates.addAll(matching);
                    }
                } else {
                    if (matchTiles.isEmpty()) {
                        matchTiles = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf(".properties"));
                    }

                    if (!matchTiles.contains(":")) {
                        matchTiles = "minecraft:blocks/" + matchTiles;
                    }

                    ResourceLocation matchID = new ResourceLocation(matchTiles);
                    TextureAtlasSprite compareIcon = LightMapConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(matchID);
                    if (compareIcon.atlasLocation() != MissingTextureAtlasSprite.getLocation()) {
                        ArrayList<BlockState> tmpList = new ArrayList<>();

                        for (Block testBlock : BuiltInRegistries.BLOCK) {

                            for (BlockState blockState : testBlock.getStateDefinition().getPossibleStates()) {
                                try {
                                    BakedModel bakedModel = blockModelShapes.getBlockModel(blockState);
                                    List<BakedQuad> quads = new ArrayList<>();
                                    // In 1.20.1, get quads directly from BakedModel
                                    quads.addAll(bakedModel.getQuads(blockState, Direction.UP, this.random));
                                    quads.addAll(bakedModel.getQuads(blockState, null, this.random));
                                    BlockModel model = new BlockModel(quads, this.failedToLoadX, this.failedToLoadY);
                                    if (model.numberOfFaces() > 0) {
                                        ArrayList<BlockModel.BlockFace> blockFaces = model.getFaces();

                                        for (int i = 0; i < blockFaces.size(); ++i) {
                                            BlockModel.BlockFace face = model.getFaces().get(i);
                                            float minU = face.getMinU();
                                            float maxU = face.getMaxU();
                                            float minV = face.getMinV();
                                            float maxV = face.getMaxV();
                                            if (this.similarEnough(minU, maxU, minV, maxV, compareIcon.getU0(), compareIcon.getU1(), compareIcon.getV0(), compareIcon.getV1())) {
                                                tmpList.add(blockState);
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }

                        blockStates.addAll(tmpList);
                    }
                }
            }

            if (!blockStates.isEmpty()) {
                if (!method.equals("horizontal") && !method.startsWith("overlay") && (method.equals("sandstone") || method.equals("top") || faces.contains("top") || faces.contains("all") || faces.isEmpty())) {
                    try {
                        ResourceLocation pngResource = new ResourceLocation(propertiesFile.getNamespace(), tilePath);
                        InputStream is = LightMapConstants.getMinecraft().getResourceManager().getResource(pngResource).get().open();
                        Image top = ImageIO.read(is);
                        is.close();
                        top = top.getScaledInstance(1, 1, 4);
                        BufferedImage topBuff = new BufferedImage(top.getWidth(null), top.getHeight(null), 6);
                        Graphics gfx = topBuff.createGraphics();
                        gfx.drawImage(top, 0, 0, null);
                        gfx.dispose();
                        int topRGB = topBuff.getRGB(0, 0);
                        if ((topRGB >> 24 & 0xFF) == 0) {
                            return;
                        }

                        for (BlockState blockState : blockStates) {
                            topRGB = topBuff.getRGB(0, 0);
                            if (blockState.getBlock() == BlockDatabase.cobweb) {
                                topRGB |= 0xFF000000;
                            }

                            if (renderPass.equals("3")) {
                                topRGB = this.processRenderPassThree(topRGB);
                                int blockStateID = BlockDatabase.getStateId(blockState);
                                int baseRGB = this.blockColors[blockStateID];
                                if (baseRGB != 0x1B000000 && baseRGB != 0xFEFF00FF) {
                                    topRGB = ColorUtils.colorMultiplier(baseRGB, topRGB);
                                }
                            }

                            if (BlockDatabase.shapedBlocks.contains(blockState.getBlock())) {
                                topRGB = this.applyShape(blockState.getBlock(), topRGB);
                            }

                            int blockStateID = BlockDatabase.getStateId(blockState);
                            if (!biomes.isEmpty()) {
                                this.biomeTextureAvailable.add(blockStateID);

                                for (String s : biomesArray) {
                                    int biomeInt = this.parseBiomeName(s);
                                    if (biomeInt != -1) {
                                        this.blockBiomeSpecificColors.put(blockStateID + " " + biomeInt, topRGB);
                                    }
                                }
                            } else {
                                this.blockColors[blockStateID] = topRGB;
                            }
                        }
                    } catch (IOException var40) {
                        LightMapConstants.getLogger().error("error getting CTM block from " + propertiesFile.getPath() + ": " + filePath + " " + BuiltInRegistries.BLOCK.getKey(blockStates.iterator().next().getBlock()) + " " + tilePath, var40);
                    }
                }

            }
        }
    }

    private boolean similarEnough(float a, float b, float c, float d, float one, float two, float three, float four) {
        boolean similar = Math.abs(a - one) < 1.0E-4;
        similar = similar && Math.abs(b - two) < 1.0E-4;
        similar = similar && Math.abs(c - three) < 1.0E-4;
        return similar && Math.abs(d - four) < 1.0E-4;
    }

    private int processRenderPassThree(int rgb) {
        if (this.renderPassThreeBlendMode.equals("color") || this.renderPassThreeBlendMode.equals("overlay")) {
            int red = rgb >> 16 & 0xFF;
            int green = rgb >> 8 & 0xFF;
            int blue = rgb & 0xFF;
            float colorAverage = (red + blue + green) / 3.0F;
            float lighteningFactor = (colorAverage - 127.5F) * 2.0F;
            red += (int) (red * (lighteningFactor / 255.0F));
            blue += (int) (red * (lighteningFactor / 255.0F));
            green += (int) (red * (lighteningFactor / 255.0F));
            int newAlpha = (int) Math.abs(lighteningFactor);
            rgb = newAlpha << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }

        return rgb;
    }

    private String[] parseStringList(String list) {
        ArrayList<String> tmpList = new ArrayList<>();

        for (String token : list.split("\\s+")) {
            token = token.trim();

            try {
                if (token.matches("^\\d+$")) {
                    tmpList.add(String.valueOf(Integer.parseInt(token)));
                } else if (token.matches("^\\d+-\\d+$")) {
                    String[] t = token.split("-");
                    int min = Integer.parseInt(t[0]);
                    int max = Integer.parseInt(t[1]);

                    for (int i = min; i <= max; ++i) {
                        tmpList.add(String.valueOf(i));
                    }
                } else if (!token.isEmpty()) {
                    tmpList.add(token);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return tmpList.toArray(String[]::new);
    }

    private Set<BlockState> parseBlocksList(String blocks, String metadataLine) {
        Set<BlockState> blockStates = new HashSet<>();

        for (String blockString : blocks.split("\\s+")) {
            StringBuilder metadata = new StringBuilder(metadataLine);
            blockString = blockString.trim();
            String[] blockComponents = blockString.split(":");
            int tokensUsed = 0;
            Block block;
            block = this.getBlockFromName(blockComponents[0]);
            if (block != null) {
                tokensUsed = 1;
            } else if (blockComponents.length > 1) {
                block = this.getBlockFromName(blockComponents[0] + ":" + blockComponents[1]);
                if (block != null) {
                    tokensUsed = 2;
                }
            }

            if (block != null) {
                if (blockComponents.length > tokensUsed) {
                    metadata = new StringBuilder(blockComponents[tokensUsed]);

                    for (int t = tokensUsed + 1; t < blockComponents.length; ++t) {
                        metadata.append(":").append(blockComponents[t]);
                    }
                }

                blockStates.addAll(this.parseBlockMetadata(block, metadata.toString()));
            }
        }

        return blockStates;
    }

    private Set<BlockState> parseBlockMetadata(Block block, String metadataList) {
        Set<BlockState> blockStates = new HashSet<>();
        if (metadataList.isEmpty()) {
            blockStates.addAll(block.getStateDefinition().getPossibleStates());
        } else {
            Set<String> valuePairs = Arrays.stream(metadataList.split(":")).map(String::trim).filter(metadata -> metadata.contains("=")).collect(Collectors.toSet());

            if (!valuePairs.isEmpty()) {

                for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
                    boolean matches = true;

                    for (String pair : valuePairs) {
                        String[] propertyAndValues = pair.split("\\s*=\\s*", 5);
                        if (propertyAndValues.length == 2) {
                            Property<?> property = block.getStateDefinition().getProperty(propertyAndValues[0]);
                            if (property != null) {
                                boolean valueIncluded = false;
                                String[] values = propertyAndValues[1].split(",");

                                for (String value : values) {
                                    if (property.getValueClass() == Integer.class && value.matches("^\\d+-\\d+$")) {
                                        String[] range = value.split("-");
                                        int min = Integer.parseInt(range[0]);
                                        int max = Integer.parseInt(range[1]);
                                        int intValue = (Integer) blockState.getValue(property);
                                        if (intValue >= min && intValue <= max) {
                                            valueIncluded = true;
                                        }
                                    } else if (!blockState.getValue(property).equals(property.getValue(value).orElse(null))) {
                                        valueIncluded = true;
                                    }
                                }

                                matches = matches && valueIncluded;
                            }
                        }
                    }

                    if (matches) {
                        blockStates.add(blockState);
                    }
                }
            }
        }

        return blockStates;
    }

    private int parseBiomeName(String name) {
        Biome biome = this.world.registryAccess().registryOrThrow(Registries.BIOME).get(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(name)));
        return biome != null ? this.world.registryAccess().registryOrThrow(Registries.BIOME).getId(biome) : -1;
    }

    private List<ResourceLocation> findResources(String namespace, String startingPath, String suffixMaybeNull, boolean recursive, boolean directories, boolean sortByFilename) {
        if (startingPath == null) {
            startingPath = "";
        }

        if (!startingPath.isEmpty() && startingPath.charAt(0) == '/') {
            startingPath = startingPath.substring(1);
        }

        String suffix = suffixMaybeNull == null ? "" : suffixMaybeNull;
        ArrayList<ResourceLocation> resources;

        Map<ResourceLocation, Resource> resourceMap = LightMapConstants.getMinecraft().getResourceManager().listResources(startingPath, asset -> asset.getPath().endsWith(suffix));
        resources = resourceMap.keySet().stream().filter(candidate -> candidate.getNamespace().equals(namespace)).collect(Collectors.toCollection(ArrayList::new));

        if (sortByFilename) {
            resources.sort((o1, o2) -> {
                String f1 = o1.getPath().replaceAll(".*/", "").replaceFirst("\\.properties", "");
                String f2 = o2.getPath().replaceAll(".*/", "").replaceFirst("\\.properties", "");
                int result = f1.compareTo(f2);
                return result != 0 ? result : o1.getPath().compareTo(o2.getPath());
            });
        } else {
            resources.sort(Comparator.comparing(ResourceLocation::getPath));
        }

        return resources;
    }

    private void processColorProperties() {
        Properties properties = new Properties();

        try {
            InputStream input = LightMapConstants.getMinecraft().getResourceManager().getResource(new ResourceLocation("optifine/color.properties")).get().open();
            if (input != null) {
                properties.load(input);
                input.close();
            }
        } catch (IOException exception) {
            LightMapConstants.getLogger().error(exception);
        }

        BlockState blockState = BlockDatabase.lilypad.defaultBlockState();
        int blockStateID = BlockDatabase.getStateId(blockState);
        int lilyRGB = this.getBlockColor(blockStateID);
        int lilypadMultiplier = 2129968;
        String lilypadMultiplierString = properties.getProperty("lilypad");
        if (lilypadMultiplierString != null) {
            lilypadMultiplier = Integer.parseInt(lilypadMultiplierString, 16);
        }

        for (UnmodifiableIterator<BlockState> defaultFormat = BlockDatabase.lilypad.getStateDefinition().getPossibleStates().iterator(); defaultFormat.hasNext(); this.blockColorsWithDefaultTint[blockStateID] = this.blockColors[blockStateID]) {
            BlockState padBlockState = defaultFormat.next();
            blockStateID = BlockDatabase.getStateId(padBlockState);
            this.blockColors[blockStateID] = ColorUtils.colorMultiplier(lilyRGB, lilypadMultiplier | 0xFF000000);
        }

        String defaultFormat = properties.getProperty("palette.format");
        boolean globalGrid = defaultFormat != null && defaultFormat.equalsIgnoreCase("grid");
        Enumeration<?> e = properties.propertyNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith("palette.block")) {
                String filename = key.substring("palette.block.".length());
                filename = filename.replace("~", "optifine");
                this.processColorPropertyHelper(new ResourceLocation(filename), properties.getProperty(key), globalGrid);
            }
        }

        for (ResourceLocation resource : this.findResources("minecraft", "/optifine/colormap/blocks", ".properties", true, false, true)) {
            Properties colorProperties = new Properties();

            try {
                InputStream input = LightMapConstants.getMinecraft().getResourceManager().getResource(resource).get().open();
                if (input != null) {
                    colorProperties.load(input);
                    input.close();
                }
            } catch (IOException var21) {
                break;
            }

            String names = colorProperties.getProperty("blocks");
            if (names == null) {
                String name = resource.getPath();
                name = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf(".properties"));
                names = name;
            }

            String source = colorProperties.getProperty("source");
            ResourceLocation resourcePNG;
            if (source != null) {
                resourcePNG = new ResourceLocation(resource.getNamespace(), source);

                LightMapConstants.getMinecraft().getResourceManager().getResource(resourcePNG);
            } else {
                resourcePNG = new ResourceLocation(resource.getNamespace(), resource.getPath().replace(".properties", ".png"));
            }

            String format = colorProperties.getProperty("format");
            boolean grid;
            if (format != null) {
                grid = format.equalsIgnoreCase("grid");
            } else {
                grid = globalGrid;
            }

            String yOffsetString = colorProperties.getProperty("yOffset");
            int yOffset = 0;
            if (yOffsetString != null) {
                yOffset = Integer.parseInt(yOffsetString);
            }

            this.processColorProperty(resourcePNG, names, grid, yOffset);
        }

        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/water.png"), "water", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/watercolorx.png"), "water", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/swampgrass.png"), "grass_block grass fern tall_grass large_fern", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/swampgrasscolor.png"), "grass_block grass fern tall_grass large_fern", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/swampfoliage.png"), "oak_leaves vine", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/swampfoliagecolor.png"), "oak_leaves vine", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/pine.png"), "spruce_leaves", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/pinecolor.png"), "spruce_leaves", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/birch.png"), "birch_leaves", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/birchcolor.png"), "birch_leaves", globalGrid);
    }

    private void processColorPropertyHelper(ResourceLocation resource, String list, boolean grid) {
        ResourceLocation resourceProperties = new ResourceLocation(resource.getNamespace(), resource.getPath().replace(".png", ".properties"));
        Properties colorProperties = new Properties();
        int yOffset = 0;

        try {
            InputStream input = LightMapConstants.getMinecraft().getResourceManager().getResource(resourceProperties).get().open();
            if (input != null) {
                colorProperties.load(input);
                input.close();
            }

            String format = colorProperties.getProperty("format");
            if (format != null) {
                grid = format.equalsIgnoreCase("grid");
            }

            String yOffsetString = colorProperties.getProperty("yOffset");
            if (yOffsetString != null) {
                yOffset = Integer.parseInt(yOffsetString);
            }
        } catch (IOException ignored) {
        }

        this.processColorProperty(resource, list, grid, yOffset);
    }

    private void processColorProperty(ResourceLocation resource, String list, boolean grid, int yOffset) {
        int[][] tints = new int[this.sizeOfBiomeArray][32];

        for (int[] row : tints) {
            Arrays.fill(row, -1);
        }

        boolean swamp = resource.getPath().contains("/swamp");
        Image tintColors;

        try {
            InputStream is = LightMapConstants.getMinecraft().getResourceManager().getResource(resource).get().open();
            tintColors = ImageIO.read(is);
            is.close();
        } catch (IOException var21) {
            return;
        }

        BufferedImage tintColorsBuff = new BufferedImage(tintColors.getWidth(null), tintColors.getHeight(null), 1);
        Graphics gfx = tintColorsBuff.createGraphics();
        gfx.drawImage(tintColors, 0, 0, null);
        gfx.dispose();
        int numBiomesToCheck = grid ? Math.min(tintColorsBuff.getWidth(), this.sizeOfBiomeArray) : this.sizeOfBiomeArray;

        for (int t = 0; t < numBiomesToCheck; ++t) {
            Biome biome = this.world.registryAccess().registryOrThrow(Registries.BIOME).byId(t);
            if (biome != null) {
                int tintMult;
                int heightMultiplier = tintColorsBuff.getHeight() / 32;

                for (int s = 0; s < 32; ++s) {
                    if (grid) {
                        tintMult = tintColorsBuff.getRGB(t, Math.max(0, s * heightMultiplier - yOffset)) & 16777215;
                    } else {
                        double var1 = Mth.clamp(biome.getBaseTemperature(), 0.0F, 1.0F);
                        double var2 = Mth.clamp(biome.getModifiedClimateSettings().downfall(), 0.0F, 1.0F);

                        var2 *= var1;
                        var1 = 1.0 - var1;
                        var2 = 1.0 - var2;
                        tintMult = tintColorsBuff.getRGB((int) ((tintColorsBuff.getWidth() - 1) * var1), (int) ((tintColorsBuff.getHeight() - 1) * var2)) & 16777215;
                    }

                    if (tintMult != 0 && !swamp) {
                        tints[t][s] = tintMult;
                    }
                }
            }
        }

        Set<BlockState> blockStates = new HashSet<>(this.parseBlocksList(list, ""));

        for (BlockState blockState : blockStates) {
            int blockStateID = BlockDatabase.getStateId(blockState);
            int[][] previousTints = this.blockTintTables.get(blockStateID);
            if (swamp && previousTints == null) {
                ResourceLocation defaultResource;
                if (resource.getPath().contains("grass")) {
                    defaultResource = new ResourceLocation("textures/colormap/grass.png");
                } else {
                    defaultResource = new ResourceLocation("textures/colormap/foliage.png");
                }

                String stateString = blockState.toString().toLowerCase();
                stateString = stateString.replaceAll("^block", "");
                stateString = stateString.replace("{", "");
                stateString = stateString.replace("}", "");
                stateString = stateString.replace("[", ":");
                stateString = stateString.replace("]", "");
                stateString = stateString.replace(",", ":");
                this.processColorProperty(defaultResource, stateString, false, 0);
                previousTints = this.blockTintTables.get(blockStateID);
            }

            if (previousTints != null) {
                for (int t = 0; t < this.sizeOfBiomeArray; ++t) {
                    for (int s = 0; s < 32; ++s) {
                        if (tints[t][s] == -1) {
                            tints[t][s] = previousTints[t][s];
                        }
                    }
                }
            }

            this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(this.getBlockColor(blockStateID), tints[4][8] | 0xFF000000);
            this.blockTintTables.put(blockStateID, tints);
            this.biomeTintsAvailable.add(blockStateID);
        }

    }

    private Block getBlockFromName(String name) {
        try {
            ResourceLocation identifier = new ResourceLocation(name);
            return BuiltInRegistries.BLOCK.containsKey(identifier) ? BuiltInRegistries.BLOCK.get(identifier) : null;
        } catch (ResourceLocationException | NumberFormatException var3) {
            return null;
        }
    }

    @FunctionalInterface
    private interface ColorResolver {
        int getColorAtPos(BlockState state, Biome biome, BlockPos pos);
    }
}