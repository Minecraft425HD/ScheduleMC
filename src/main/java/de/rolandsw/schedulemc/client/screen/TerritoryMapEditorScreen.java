package de.rolandsw.schedulemc.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import de.rolandsw.schedulemc.territory.Territory;
import de.rolandsw.schedulemc.territory.TerritoryManager;
import de.rolandsw.schedulemc.territory.TerritoryType;
import de.rolandsw.schedulemc.territory.network.SetTerritoryPacket;
import de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Fullscreen Territory Map Editor
 * Allows OPs to paint territories chunk-by-chunk
 */
public class TerritoryMapEditorScreen extends Screen {
    private static final int CHUNK_SIZE = 16;
    private static final int PALETTE_WIDTH = 150;
    private static final int PALETTE_BUTTON_HEIGHT = 30;
    private static final int PALETTE_PADDING = 5;

    // Map data (shared with MapAppScreen)
    private static final Map<Long, byte[]> exploredChunks = new HashMap<>();
    private static int viewCenterWorldX = Integer.MAX_VALUE;
    private static int viewCenterWorldZ = Integer.MAX_VALUE;

    // Editor state
    @Nullable
    private TerritoryType selectedType = TerritoryType.COLOR_RED;
    private float currentZoom = 1.0f;
    private int offsetX = 0;
    private int offsetZ = 0;

    // Drag state
    private boolean isDragging = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    // Color palette buttons
    private final Map<TerritoryType, Button> paletteButtons = new HashMap<>();

    // Territory name input
    private EditBox territoryNameInput;
    private String currentTerritoryName = "";

    public TerritoryMapEditorScreen() {
        super(Component.literal("Territory Map Editor"));
    }

    @Override
    protected void init() {
        super.init();

        // Initialize view center on first open
        if (viewCenterWorldX == Integer.MAX_VALUE && minecraft != null && minecraft.player != null) {
            BlockPos pos = minecraft.player.blockPosition();
            viewCenterWorldX = pos.getX();
            viewCenterWorldZ = pos.getZ();
        }

        // Create color palette buttons (left side)
        int buttonY = 10;
        for (TerritoryType type : TerritoryType.values()) {
            Button button = Button.builder(
                Component.literal(type.getDisplayName()),
                btn -> selectedType = type
            )
            .bounds(10, buttonY, PALETTE_WIDTH, PALETTE_BUTTON_HEIGHT)
            .build();

            paletteButtons.put(type, button);
            addRenderableWidget(button);
            buttonY += PALETTE_BUTTON_HEIGHT + PALETTE_PADDING;
        }

        // Add Clear Territory button
        addRenderableWidget(Button.builder(
            Component.literal("Clear Territory"),
            btn -> selectedType = null
        )
        .bounds(10, buttonY, PALETTE_WIDTH, PALETTE_BUTTON_HEIGHT)
        .build());

        buttonY += PALETTE_BUTTON_HEIGHT + PALETTE_PADDING * 2;

        // Add Zoom buttons
        addRenderableWidget(Button.builder(
            Component.literal("+ Zoom In"),
            btn -> {
                if (currentZoom < 4.0f) {
                    currentZoom *= 2.0f;
                }
            }
        )
        .bounds(10, buttonY, PALETTE_WIDTH / 2 - 3, 20)
        .build());

        addRenderableWidget(Button.builder(
            Component.literal("- Zoom Out"),
            btn -> {
                if (currentZoom > 0.5f) {
                    currentZoom /= 2.0f;
                }
            }
        )
        .bounds(10 + PALETTE_WIDTH / 2 + 3, buttonY, PALETTE_WIDTH / 2 - 3, 20)
        .build());

        buttonY += 30;

        // Add Territory Name Input
        territoryNameInput = new EditBox(minecraft.font, 10, buttonY, PALETTE_WIDTH, 20,
            Component.literal("Territory Name"));
        territoryNameInput.setHint(Component.literal("Gebietsname..."));
        territoryNameInput.setMaxLength(32);
        territoryNameInput.setValue(currentTerritoryName);
        territoryNameInput.setResponder(value -> currentTerritoryName = value);
        addRenderableWidget(territoryNameInput);

        // Add Close button
        addRenderableWidget(Button.builder(
            Component.literal("Close & Save"),
            btn -> onClose()
        )
        .bounds(this.width - 120, this.height - 30, 110, 20)
        .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render dark background
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF1a1a1a);

        // Calculate map area (full screen minus palette)
        int mapX = PALETTE_WIDTH + 20;
        int mapY = 10;
        int mapWidth = this.width - mapX - 20;
        int mapHeight = this.height - 20;

        // Render base map
        renderBaseMap(guiGraphics, mapX, mapY, mapWidth, mapHeight);

        // Render territory overlay
        renderTerritoryOverlay(guiGraphics, mapX, mapY, mapWidth, mapHeight);

        // Render chunk grid
        renderChunkGrid(guiGraphics, mapX, mapY, mapWidth, mapHeight);

        // Highlight selected territory type button
        for (Map.Entry<TerritoryType, Button> entry : paletteButtons.entrySet()) {
            if (entry.getKey() == selectedType) {
                Button btn = entry.getValue();
                guiGraphics.fill(btn.getX() - 2, btn.getY() - 2,
                    btn.getX() + btn.getWidth() + 2, btn.getY() + btn.getHeight() + 2,
                    0xFF44FF44);
            }
        }

        // Render UI elements
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render hover info
        renderHoverInfo(guiGraphics, mouseX, mouseY, mapX, mapY, mapWidth, mapHeight);
    }

    private void renderBaseMap(GuiGraphics guiGraphics, int mapX, int mapY, int mapWidth, int mapHeight) {
        if (minecraft == null || minecraft.level == null) return;

        Level level = minecraft.level;

        // Explore chunks around view center
        exploreChunksAround(level, viewCenterWorldX, viewCenterWorldZ, 64);

        // Calculate chunk rendering parameters
        int chunksWide = (int) Math.ceil(mapWidth / (CHUNK_SIZE * currentZoom));
        int chunksHigh = (int) Math.ceil(mapHeight / (CHUNK_SIZE * currentZoom));

        int centerChunkX = (viewCenterWorldX + offsetX) >> 4;
        int centerChunkZ = (viewCenterWorldZ + offsetZ) >> 4;

        int startChunkX = centerChunkX - chunksWide / 2;
        int startChunkZ = centerChunkZ - chunksHigh / 2;

        int pixelsPerChunk = (int) (CHUNK_SIZE * currentZoom);

        // Render chunks with fallback background
        for (int cz = 0; cz < chunksHigh; cz++) {
            for (int cx = 0; cx < chunksWide; cx++) {
                int chunkX = startChunkX + cx;
                int chunkZ = startChunkZ + cz;

                long chunkKey = getChunkKey(chunkX, chunkZ);
                byte[] chunkData = exploredChunks.get(chunkKey);

                int screenX = mapX + cx * pixelsPerChunk;
                int screenY = mapY + cz * pixelsPerChunk;

                if (chunkData != null) {
                    renderChunk(guiGraphics, chunkData, cx, cz, mapX, mapY, currentZoom);
                } else {
                    // Fallback: Render gray background for unexplored chunks
                    guiGraphics.fill(screenX, screenY,
                        screenX + pixelsPerChunk, screenY + pixelsPerChunk,
                        0xFF3a3a3a);
                }
            }
        }
    }

    private void renderChunk(GuiGraphics guiGraphics, byte[] chunkData, int chunkGridX, int chunkGridZ,
                            int mapX, int mapY, float zoom) {
        int pixelsPerBlock = Math.max(1, (int) zoom);

        for (int localX = 0; localX < CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                byte colorId = chunkData[localX + localZ * CHUNK_SIZE];
                int color = getMaterialColor(colorId);

                int screenX = mapX + chunkGridX * CHUNK_SIZE * pixelsPerBlock + localX * pixelsPerBlock;
                int screenY = mapY + chunkGridZ * CHUNK_SIZE * pixelsPerBlock + localZ * pixelsPerBlock;

                guiGraphics.fill(screenX, screenY,
                    screenX + pixelsPerBlock, screenY + pixelsPerBlock,
                    0xFF000000 | color);
            }
        }
    }

    private void renderTerritoryOverlay(GuiGraphics guiGraphics, int mapX, int mapY, int mapWidth, int mapHeight) {
        TerritoryManager manager = TerritoryManager.getInstance();
        if (manager == null) return;

        Map<Long, Territory> territories = manager.getTerritoriesMap();

        int chunksWide = (int) Math.ceil(mapWidth / (CHUNK_SIZE * currentZoom));
        int chunksHigh = (int) Math.ceil(mapHeight / (CHUNK_SIZE * currentZoom));

        int centerChunkX = (viewCenterWorldX + offsetX) >> 4;
        int centerChunkZ = (viewCenterWorldZ + offsetZ) >> 4;

        int startChunkX = centerChunkX - chunksWide / 2;
        int startChunkZ = centerChunkZ - chunksHigh / 2;

        int pixelsPerChunk = (int) (CHUNK_SIZE * currentZoom);

        for (int cz = 0; cz < chunksHigh; cz++) {
            for (int cx = 0; cx < chunksWide; cx++) {
                int chunkX = startChunkX + cx;
                int chunkZ = startChunkZ + cz;

                long chunkKey = getChunkKey(chunkX, chunkZ);
                Territory territory = territories.get(chunkKey);

                if (territory != null) {
                    int screenX = mapX + cx * pixelsPerChunk;
                    int screenY = mapY + cz * pixelsPerChunk;

                    int color = territory.getType().getColor();
                    int alpha = 0xCC; // 80% opacity (more visible)
                    int overlayColor = (alpha << 24) | color;

                    guiGraphics.fill(screenX, screenY,
                        screenX + pixelsPerChunk, screenY + pixelsPerChunk,
                        overlayColor);

                    // Border for better visibility
                    guiGraphics.fill(screenX, screenY, screenX + pixelsPerChunk, screenY + 1, 0xFFFFFFFF); // Top
                    guiGraphics.fill(screenX, screenY, screenX + 1, screenY + pixelsPerChunk, 0xFFFFFFFF); // Left
                }
            }
        }
    }

    private void renderChunkGrid(GuiGraphics guiGraphics, int mapX, int mapY, int mapWidth, int mapHeight) {
        if (currentZoom < 2.0f) return; // Only show grid at higher zoom levels

        int chunksWide = (int) Math.ceil(mapWidth / (CHUNK_SIZE * currentZoom));
        int chunksHigh = (int) Math.ceil(mapHeight / (CHUNK_SIZE * currentZoom));

        int pixelsPerChunk = (int) (CHUNK_SIZE * currentZoom);

        // Vertical lines
        for (int cx = 0; cx <= chunksWide; cx++) {
            int x = mapX + cx * pixelsPerChunk;
            guiGraphics.fill(x, mapY, x + 1, mapY + mapHeight, 0x44FFFFFF);
        }

        // Horizontal lines
        for (int cz = 0; cz <= chunksHigh; cz++) {
            int y = mapY + cz * pixelsPerChunk;
            guiGraphics.fill(mapX, y, mapX + mapWidth, y + 1, 0x44FFFFFF);
        }
    }

    private void renderHoverInfo(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                 int mapX, int mapY, int mapWidth, int mapHeight) {
        if (mouseX < mapX || mouseX >= mapX + mapWidth || mouseY < mapY || mouseY >= mapY + mapHeight) {
            return;
        }

        // Convert mouse to chunk coordinates
        int pixelsPerChunk = (int) (CHUNK_SIZE * currentZoom);
        int chunksWide = (int) Math.ceil(mapWidth / (CHUNK_SIZE * currentZoom));
        int chunksHigh = (int) Math.ceil(mapHeight / (CHUNK_SIZE * currentZoom));

        int centerChunkX = (viewCenterWorldX + offsetX) >> 4;
        int centerChunkZ = (viewCenterWorldZ + offsetZ) >> 4;

        int startChunkX = centerChunkX - chunksWide / 2;
        int startChunkZ = centerChunkZ - chunksHigh / 2;

        int relativeX = mouseX - mapX;
        int relativeY = mouseY - mapY;

        int chunkX = startChunkX + relativeX / pixelsPerChunk;
        int chunkZ = startChunkZ + relativeY / pixelsPerChunk;

        // Get territory info
        TerritoryManager manager = TerritoryManager.getInstance();
        String info = String.format("Chunk: [%d, %d]", chunkX, chunkZ);

        if (manager != null) {
            Territory territory = manager.getTerritory(chunkX, chunkZ);
            if (territory != null) {
                if (territory.getName() != null && !territory.getName().isEmpty()) {
                    info += "\n" + territory.getName();
                } else {
                    info += "\n" + territory.getType().getDisplayName();
                }
            }
        }

        // Render tooltip
        String[] lines = info.split("\n");
        int tooltipWidth = 0;
        for (String line : lines) {
            tooltipWidth = Math.max(tooltipWidth, minecraft.font.width(line) + 8);
        }
        int tooltipHeight = lines.length * 10 + 4;

        int tooltipX = mouseX + 10;
        int tooltipY = mouseY + 10;

        // Keep tooltip on screen
        if (tooltipX + tooltipWidth > this.width) {
            tooltipX = mouseX - tooltipWidth - 10;
        }
        if (tooltipY + tooltipHeight > this.height) {
            tooltipY = mouseY - tooltipHeight - 10;
        }

        // Background
        guiGraphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xE0000000);
        guiGraphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + 1, 0xFFFFFFFF);

        // Text
        int lineY = tooltipY + 2;
        for (String line : lines) {
            guiGraphics.drawString(minecraft.font, line, tooltipX + 4, lineY, 0xFFFFFFFF);
            lineY += 10;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int mapX = PALETTE_WIDTH + 20;
        int mapY = 10;
        int mapWidth = this.width - mapX - 20;
        int mapHeight = this.height - 20;

        if (mouseX >= mapX && mouseX < mapX + mapWidth && mouseY >= mapY && mouseY < mapY + mapHeight) {
            if (button == 0) { // Left click - paint territory
                paintTerritoryAt(mouseX, mouseY, mapX, mapY, mapWidth, mapHeight);
                return true;
            } else if (button == 1) { // Right click - start drag
                isDragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            double deltaX = mouseX - lastMouseX;
            double deltaY = mouseY - lastMouseY;

            int pixelsPerBlock = Math.max(1, (int) currentZoom);
            offsetX -= (int) (deltaX / pixelsPerBlock);
            offsetZ -= (int) (deltaY / pixelsPerBlock);

            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && currentZoom < 4.0f) {
            currentZoom *= 2.0f;
            return true;
        } else if (delta < 0 && currentZoom > 0.5f) {
            currentZoom /= 2.0f;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Arrow keys for panning
        if (keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_A) {
            offsetX -= 16;
            return true;
        } else if (keyCode == InputConstants.KEY_RIGHT || keyCode == InputConstants.KEY_D) {
            offsetX += 16;
            return true;
        } else if (keyCode == InputConstants.KEY_UP || keyCode == InputConstants.KEY_W) {
            offsetZ -= 16;
            return true;
        } else if (keyCode == InputConstants.KEY_DOWN || keyCode == InputConstants.KEY_S) {
            offsetZ += 16;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void paintTerritoryAt(double mouseX, double mouseY, int mapX, int mapY, int mapWidth, int mapHeight) {
        // Convert mouse to chunk coordinates
        int pixelsPerChunk = (int) (CHUNK_SIZE * currentZoom);
        int chunksWide = (int) Math.ceil(mapWidth / (CHUNK_SIZE * currentZoom));
        int chunksHigh = (int) Math.ceil(mapHeight / (CHUNK_SIZE * currentZoom));

        int centerChunkX = (viewCenterWorldX + offsetX) >> 4;
        int centerChunkZ = (viewCenterWorldZ + offsetZ) >> 4;

        int startChunkX = centerChunkX - chunksWide / 2;
        int startChunkZ = centerChunkZ - chunksHigh / 2;

        int relativeX = (int) (mouseX - mapX);
        int relativeY = (int) (mouseY - mapY);

        int chunkX = startChunkX + relativeX / pixelsPerChunk;
        int chunkZ = startChunkZ + relativeY / pixelsPerChunk;

        // Send packet to server to set/remove territory
        if (selectedType == null) {
            TerritoryNetworkHandler.sendToServer(new SetTerritoryPacket(chunkX, chunkZ));
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal("§7Territory entfernt: [" + chunkX + ", " + chunkZ + "]"));
            }
        } else {
            TerritoryNetworkHandler.sendToServer(new SetTerritoryPacket(chunkX, chunkZ, selectedType, currentTerritoryName));
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal("§aTerritory gesetzt: [" + chunkX + ", " + chunkZ + "] - " + selectedType.getDisplayName()));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MAP EXPLORATION (from MapAppScreen)
    // ═══════════════════════════════════════════════════════════

    private static void exploreChunksAround(Level level, int centerX, int centerZ, int radius) {
        int centerChunkX = centerX >> 4;
        int centerChunkZ = centerZ >> 4;
        int chunkRadius = (radius >> 4) + 1;

        for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
            for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
                int chunkX = centerChunkX + cx;
                int chunkZ = centerChunkZ + cz;
                long chunkKey = getChunkKey(chunkX, chunkZ);

                if (!exploredChunks.containsKey(chunkKey)) {
                    byte[] chunkData = exploreChunk(level, chunkX, chunkZ);
                    if (chunkData != null) {
                        exploredChunks.put(chunkKey, chunkData);
                    }
                }
            }
        }
    }

    @Nullable
    private static byte[] exploreChunk(Level level, int chunkX, int chunkZ) {
        if (!level.hasChunk(chunkX, chunkZ)) {
            return null;
        }

        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        byte[] data = new byte[CHUNK_SIZE * CHUNK_SIZE];

        for (int localX = 0; localX < CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                int worldX = (chunkX << 4) + localX;
                int worldZ = (chunkZ << 4) + localZ;

                int topY = chunk.getHeight();
                BlockPos pos = new BlockPos(worldX, topY, worldZ);
                BlockState state = chunk.getBlockState(pos);

                MapColor mapColor = state.getMapColor(level, pos);
                byte colorId = (byte) mapColor.id;

                data[localX + localZ * CHUNK_SIZE] = colorId;
            }
        }

        return data;
    }

    private static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static int getMaterialColor(byte colorId) {
        int id = colorId & 0xFF;
        MapColor mapColor = MapColor.byId(id);
        if (mapColor != null) {
            return mapColor.col;
        }
        return 0x000000;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
