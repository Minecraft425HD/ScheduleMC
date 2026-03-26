package de.rolandsw.schedulemc.mapview.service.render;

import com.google.common.collect.UnmodifiableIterator;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.core.model.BlockModel;
import de.rolandsw.schedulemc.mapview.util.BlockDatabase;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles all OptiFine CTM and color.properties loading.
 * Owns the biome tint tables and block color override data produced during resource-pack loading.
 */
class OptiFineColorLoader {

    private static final Pattern BLOCK_PROPERTIES_PATTERN = Pattern.compile(".*/block_(.+).properties");

    private final ColorCalculationService ccs;
    private final boolean optifineInstalled;

    private final HashSet<Integer> biomeTintsAvailable = new HashSet<>();
    private final HashMap<Integer, int[][]> blockTintTables = new HashMap<>();
    private final HashSet<Integer> biomeTextureAvailable = new HashSet<>();
    private final HashMap<String, Integer> blockBiomeSpecificColors = new HashMap<>();
    private String renderPassThreeBlendMode = "alpha";

    OptiFineColorLoader(ColorCalculationService ccs) {
        this.ccs = ccs;
        boolean installed = false;
        Field ofProfiler = null;
        try {
            ofProfiler = Options.class.getDeclaredField("ofProfiler");
        } catch (SecurityException | NoSuchFieldException ignored) {
        } finally {
            if (ofProfiler != null) {
                installed = true;
            }
        }
        this.optifineInstalled = installed;
    }

    boolean isInstalled() {
        return optifineInstalled;
    }

    HashSet<Integer> getBiomeTintsAvailable() {
        return biomeTintsAvailable;
    }

    HashSet<Integer> getBiomeTextureAvailable() {
        return biomeTextureAvailable;
    }

    HashMap<String, Integer> getBlockBiomeSpecificColors() {
        return blockBiomeSpecificColors;
    }

    HashMap<Integer, int[][]> getBlockTintTables() {
        return blockTintTables;
    }

    void clear() {
        biomeTintsAvailable.clear();
        biomeTextureAvailable.clear();
        blockBiomeSpecificColors.clear();
        blockTintTables.clear();
    }

    void processCTM() {
        this.renderPassThreeBlendMode = "alpha";  // NOPMD
        Properties properties = new Properties();
        ResourceLocation propertiesFile = ResourceLocation.fromNamespaceAndPath("minecraft", "optifine/renderpass.properties");

        try (InputStream input = MapViewConstants.getMinecraft().getResourceManager().getResource(propertiesFile).orElseThrow(() -> new IOException("Resource not found: " + propertiesFile)).open()) {
            properties.load(input);
            this.renderPassThreeBlendMode = properties.getProperty("blend.3", "alpha");
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

        for (int t = 0; t < ccs.blockColors.length; ++t) {
            if (ccs.blockColors[t] != 0x1B000000 && ccs.blockColors[t] != 0xFEFF00FF) {
                if ((ccs.blockColors[t] >> 24 & 0xFF) < 27) {
                    ccs.blockColors[t] |= 0x1B000000;
                }
                ccs.checkForBiomeTinting(ccs.dummyBlockPos, BlockDatabase.getStateById(t), ccs.blockColors[t]);
            }
        }
    }

    private void loadCTM(ResourceLocation propertiesFile) {
        if (propertiesFile != null) {
            BlockRenderDispatcher blockRendererDispatcher = MapViewConstants.getMinecraft().getBlockRenderer();
            BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
            Properties properties = new Properties();

            try (InputStream input = MapViewConstants.getMinecraft().getResourceManager().getResource(propertiesFile).orElseThrow(() -> new IOException("Resource not found: " + propertiesFile)).open()) {
                properties.load(input);
            } catch (IOException var39) {
                return;
            }

            String filePath = propertiesFile.getPath();
            String method = properties.getProperty("method", "").trim().toLowerCase(Locale.ROOT);
            String faces = properties.getProperty("faces", "").trim().toLowerCase(Locale.ROOT);
            String matchBlocks = properties.getProperty("matchBlocks", "").trim().toLowerCase(Locale.ROOT);
            String matchTiles = properties.getProperty("matchTiles", "").trim().toLowerCase(Locale.ROOT);
            String metadata = properties.getProperty("metadata", "").trim().toLowerCase(Locale.ROOT);
            String tiles = properties.getProperty("tiles", "").trim();
            String biomes = properties.getProperty("biomes", "").trim().toLowerCase(Locale.ROOT);
            String renderPass = properties.getProperty("renderPass", "").trim().toLowerCase(Locale.ROOT);
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
                tilePath = directory + tilePath;  // NOPMD
            }

            if (!tilePath.toLowerCase(Locale.ROOT).endsWith(".png")) {
                tilePath = tilePath + ".png";  // NOPMD
            }

            String[] biomesArray = biomes.split(" ");
            if (blockStates.isEmpty()) {
                Block block;
                Matcher matcher = BLOCK_PROPERTIES_PATTERN.matcher(filePath);
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

                    ResourceLocation matchID = ResourceLocation.parse(matchTiles);
                    TextureAtlasSprite compareIcon = MapViewConstants.getMinecraft().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(matchID);
                    if (compareIcon.atlasLocation() != MissingTextureAtlasSprite.getLocation()) {
                        ArrayList<BlockState> tmpList = new ArrayList<>();

                        for (Block testBlock : BuiltInRegistries.BLOCK) {
                            for (BlockState blockState : testBlock.getStateDefinition().getPossibleStates()) {
                                try {
                                    BakedModel bakedModel = blockModelShapes.getBlockModel(blockState);
                                    List<BakedQuad> quads = new ArrayList<>();
                                    // In 1.20.1, get quads directly from BakedModel
                                    quads.addAll(bakedModel.getQuads(blockState, Direction.UP, ccs.random));
                                    quads.addAll(bakedModel.getQuads(blockState, null, ccs.random));
                                    BlockModel model = new BlockModel(quads, ccs.failedToLoadX, ccs.failedToLoadY);
                                    if (model.numberOfFaces() > 0) {
                                        ArrayList<BlockModel.BlockFace> blockFaces = model.getFaces();
                                        for (int i = 0; i < blockFaces.size(); ++i) {
                                            BlockModel.BlockFace face = model.getFaces().get(i);
                                            if (this.similarEnough(face.getMinU(), face.getMaxU(), face.getMinV(), face.getMaxV(), compareIcon.getU0(), compareIcon.getU1(), compareIcon.getV0(), compareIcon.getV1())) {
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
                if (!"horizontal".equals(method) && !method.startsWith("overlay") && ("sandstone".equals(method) || "top".equals(method) || faces.contains("top") || faces.contains("all") || faces.isEmpty())) {
                    try {
                        ResourceLocation pngResource = ResourceLocation.fromNamespaceAndPath(propertiesFile.getNamespace(), tilePath);
                        Image top;
                        try (InputStream is = MapViewConstants.getMinecraft().getResourceManager().getResource(pngResource).orElseThrow(() -> new IOException("Resource not found: " + pngResource)).open()) {
                            top = ImageIO.read(is);
                        }
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

                            if ("3".equals(renderPass)) {
                                topRGB = this.processRenderPassThree(topRGB);
                                int blockStateID = BlockDatabase.getStateId(blockState);
                                int baseRGB = ccs.blockColors[blockStateID];
                                if (baseRGB != 0x1B000000 && baseRGB != 0xFEFF00FF) {
                                    topRGB = ColorUtils.colorMultiplier(baseRGB, topRGB);
                                }
                            }

                            if (BlockDatabase.shapedBlocks.contains(blockState.getBlock())) {
                                topRGB = ccs.applyShape(blockState.getBlock(), topRGB);
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
                                ccs.blockColors[blockStateID] = topRGB;
                            }
                        }
                    } catch (IOException var40) {
                        MapViewConstants.getLogger().error("error getting CTM block from " + propertiesFile.getPath() + ": " + filePath + " " + BuiltInRegistries.BLOCK.getKey(blockStates.iterator().next().getBlock()) + " " + tilePath, var40);
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
        if ("color".equals(this.renderPassThreeBlendMode) || "overlay".equals(this.renderPassThreeBlendMode)) {
            int red = rgb >> 16 & 0xFF;
            int green = rgb >> 8 & 0xFF;
            int blue = rgb & 0xFF;
            float colorAverage = (red + blue + green) / 3.0F;
            float lighteningFactor = (colorAverage - 127.5F) * 2.0F;
            red += (int) (red * (lighteningFactor / 255.0F));
            blue += (int) (red * (lighteningFactor / 255.0F));
            green += (int) (red * (lighteningFactor / 255.0F));
            int newAlpha = (int) Math.abs(lighteningFactor);
            return newAlpha << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }
        return rgb;
    }

    private String[] parseStringList(String list) {
        ArrayList<String> tmpList = new ArrayList<>();
        for (String rawToken : list.split("\\s+")) {
            String token = rawToken.trim();
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
        for (String rawBlock : blocks.split("\\s+")) {
            String blockString = rawBlock.trim();
            StringBuilder metadata = new StringBuilder(metadataLine);
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
                        metadata.append(':').append(blockComponents[t]);
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
        Biome biome = ccs.world.registryAccess().registryOrThrow(Registries.BIOME).get(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(name)));
        return biome != null ? ccs.world.registryAccess().registryOrThrow(Registries.BIOME).getId(biome) : -1;
    }

    private List<ResourceLocation> findResources(String namespace, String startingPath, String suffixMaybeNull, boolean _recursive, boolean _directories, boolean sortByFilename) {
        String effectivePath = startingPath == null ? "" : startingPath;
        if (!effectivePath.isEmpty() && effectivePath.charAt(0) == '/') {
            effectivePath = effectivePath.substring(1);
        }
        String suffix = suffixMaybeNull == null ? "" : suffixMaybeNull;
        ArrayList<ResourceLocation> resources;
        Map<ResourceLocation, Resource> resourceMap = MapViewConstants.getMinecraft().getResourceManager().listResources(effectivePath, asset -> asset.getPath().endsWith(suffix));
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

    void processColorProperties() {
        Properties properties = new Properties();

        try (InputStream input = MapViewConstants.getMinecraft().getResourceManager().getResource(ResourceLocation.parse("optifine/color.properties")).orElseThrow(() -> new IOException("Resource not found: optifine/color.properties")).open()) {
            properties.load(input);
        } catch (IOException exception) {
            MapViewConstants.getLogger().error(exception);
        }

        BlockState blockState = BlockDatabase.lilypad.defaultBlockState();
        int blockStateID = BlockDatabase.getStateId(blockState);
        int lilyRGB = ccs.getBlockColor(blockStateID);
        int lilypadMultiplier = 2129968;
        String lilypadMultiplierString = properties.getProperty("lilypad");
        if (lilypadMultiplierString != null) {
            lilypadMultiplier = Integer.parseInt(lilypadMultiplierString, 16);
        }

        for (UnmodifiableIterator<BlockState> defaultFormat = BlockDatabase.lilypad.getStateDefinition().getPossibleStates().iterator(); defaultFormat.hasNext(); ccs.blockColorsWithDefaultTint[blockStateID] = ccs.blockColors[blockStateID]) {
            BlockState padBlockState = defaultFormat.next();
            blockStateID = BlockDatabase.getStateId(padBlockState);
            ccs.blockColors[blockStateID] = ColorUtils.colorMultiplier(lilyRGB, lilypadMultiplier | 0xFF000000);
        }

        String defaultFormat = properties.getProperty("palette.format");
        boolean globalGrid = defaultFormat != null && "grid".equalsIgnoreCase(defaultFormat);
        Enumeration<?> e = properties.propertyNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith("palette.block")) {
                String filename = key.substring("palette.block.".length());
                filename = filename.replace("~", "optifine");
                this.processColorPropertyHelper(ResourceLocation.parse(filename), properties.getProperty(key), globalGrid);
            }
        }

        for (ResourceLocation resource : this.findResources("minecraft", "/optifine/colormap/blocks", ".properties", true, false, true)) {
            Properties colorProperties = new Properties();

            try (InputStream input = MapViewConstants.getMinecraft().getResourceManager().getResource(resource).orElseThrow(() -> new IOException("Resource not found: " + resource)).open()) {
                colorProperties.load(input);
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
                resourcePNG = ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), source);
                MapViewConstants.getMinecraft().getResourceManager().getResource(resourcePNG);
            } else {
                resourcePNG = ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), resource.getPath().replace(".properties", ".png"));
            }

            String format = colorProperties.getProperty("format");
            boolean grid;
            if (format != null) {
                grid = "grid".equalsIgnoreCase(format);
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

        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/water.png"), "water", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/watercolorx.png"), "water", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/swampgrass.png"), "grass_block grass fern tall_grass large_fern", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/swampgrasscolor.png"), "grass_block grass fern tall_grass large_fern", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/swampfoliage.png"), "oak_leaves vine", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/swampfoliagecolor.png"), "oak_leaves vine", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/pine.png"), "spruce_leaves", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/pinecolor.png"), "spruce_leaves", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/birch.png"), "birch_leaves", globalGrid);
        this.processColorPropertyHelper(ResourceLocation.parse("optifine/colormap/birchcolor.png"), "birch_leaves", globalGrid);
    }

    private void processColorPropertyHelper(ResourceLocation resource, String list, boolean grid) {
        ResourceLocation resourceProperties = ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), resource.getPath().replace(".png", ".properties"));
        Properties colorProperties = new Properties();
        int yOffset = 0;

        try (InputStream input = MapViewConstants.getMinecraft().getResourceManager().getResource(resourceProperties).orElseThrow(() -> new IOException("Resource not found: " + resourceProperties)).open()) {
            colorProperties.load(input);
        } catch (IOException ignored) {
        }

        String format = colorProperties.getProperty("format");
        boolean effectiveGrid = format != null ? "grid".equalsIgnoreCase(format) : grid;

        String yOffsetString = colorProperties.getProperty("yOffset");
        if (yOffsetString != null) {
            yOffset = Integer.parseInt(yOffsetString);
        }

        this.processColorProperty(resource, list, effectiveGrid, yOffset);
    }

    private void processColorProperty(ResourceLocation resource, String list, boolean grid, int yOffset) {
        int[][] tints = new int[ccs.sizeOfBiomeArray][32];

        for (int[] row : tints) {
            Arrays.fill(row, -1);
        }

        boolean swamp = resource.getPath().contains("/swamp");
        Image tintColors;

        try (InputStream is = MapViewConstants.getMinecraft().getResourceManager().getResource(resource).orElseThrow(() -> new IOException("Resource not found: " + resource)).open()) {
            tintColors = ImageIO.read(is);
        } catch (IOException var21) {
            return;
        }

        BufferedImage tintColorsBuff = new BufferedImage(tintColors.getWidth(null), tintColors.getHeight(null), 1);
        Graphics gfx = tintColorsBuff.createGraphics();
        gfx.drawImage(tintColors, 0, 0, null);
        gfx.dispose();
        int numBiomesToCheck = grid ? Math.min(tintColorsBuff.getWidth(), ccs.sizeOfBiomeArray) : ccs.sizeOfBiomeArray;

        for (int t = 0; t < numBiomesToCheck; ++t) {
            Biome biome = ccs.world.registryAccess().registryOrThrow(Registries.BIOME).byId(t);
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
                    defaultResource = ResourceLocation.parse("textures/colormap/grass.png");
                } else {
                    defaultResource = ResourceLocation.parse("textures/colormap/foliage.png");
                }

                String stateString = blockState.toString().toLowerCase(Locale.ROOT);
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
                for (int t = 0; t < ccs.sizeOfBiomeArray; ++t) {
                    for (int s = 0; s < 32; ++s) {
                        if (tints[t][s] == -1) {
                            tints[t][s] = previousTints[t][s];
                        }
                    }
                }
            }

            ccs.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(ccs.getBlockColor(blockStateID), tints[4][8] | 0xFF000000);
            this.blockTintTables.put(blockStateID, tints);
            this.biomeTintsAvailable.add(blockStateID);
        }
    }

    private Block getBlockFromName(String name) {
        try {
            ResourceLocation identifier = ResourceLocation.parse(name);
            return BuiltInRegistries.BLOCK.containsKey(identifier) ? BuiltInRegistries.BLOCK.get(identifier) : null;
        } catch (ResourceLocationException | NumberFormatException var3) {
            return null;
        }
    }
}
