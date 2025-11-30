package de.rolandsw.schedulemc.client.screen.apps;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

/**
 * Map App - Zeigt Karten wie Minecraft Maps (von oben fotografiert)
 * Horizontale Ausrichtung für bessere Kartenansicht
 */
@OnlyIn(Dist.CLIENT)
public class MapAppScreen extends Screen {

    private final Screen parentScreen;

    // Horizontale Ausrichtung (Landschaft-Modus)
    private static final int WIDTH = 320;
    private static final int HEIGHT = 180;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;

    private int leftPos;
    private int topPos;

    // Chunk-basiertes Map-System für unbegrenzte Exploration
    // Jeder Chunk speichert 16x16 Block-Farben
    private static final int CHUNK_SIZE = 16;
    private final Map<Long, byte[]> exploredChunks = new HashMap<>(); // ChunkPos -> 16x16 Farben

    private int updateCounter = 0;
    private static final int UPDATE_INTERVAL = 20; // Update alle 20 Ticks (1 Sekunde)

    // Zoom & Pan
    private static final float[] ZOOM_LEVELS = {0.5f, 1.0f, 2.0f, 4.0f};
    private int currentZoomIndex = 1; // Standard: 1.0x
    private int panOffsetX = 0;
    private int panOffsetY = 0;

    // Drag-to-Pan
    private boolean isDragging = false;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    public MapAppScreen(Screen parent) {
        super(Component.literal("Map"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        // Zentriere vertikal mit Margin-Check
        int centeredTop = (this.height - HEIGHT) / 2;
        int minTop = MARGIN_TOP + BORDER_SIZE;
        int maxTop = this.height - HEIGHT - BORDER_SIZE - MARGIN_BOTTOM;
        this.topPos = Math.max(minTop, Math.min(centeredTop, maxTop));

        // Zurück-Button (links unten)
        addRenderableWidget(Button.builder(Component.literal("← Zurück"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Zoom Out Button
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            zoomOut();
        }).bounds(leftPos + WIDTH - 60, topPos + HEIGHT - 30, 25, 20).build());

        // Zoom In Button
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            zoomIn();
        }).bounds(leftPos + WIDTH - 30, topPos + HEIGHT - 30, 25, 20).build());
    }

    private void zoomIn() {
        if (currentZoomIndex < ZOOM_LEVELS.length - 1) {
            currentZoomIndex++;
        }
    }

    private void zoomOut() {
        if (currentZoomIndex > 0) {
            currentZoomIndex--;
        }
    }

    private float getCurrentZoom() {
        return ZOOM_LEVELS[currentZoomIndex];
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund (horizontal)
        guiGraphics.fill(leftPos - BORDER_SIZE, topPos - BORDER_SIZE,
                        leftPos + WIDTH + BORDER_SIZE, topPos + HEIGHT + BORDER_SIZE, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 30, 0xFF1A1A1A);
        guiGraphics.drawString(this.font, "§6§lMap", leftPos + 10, topPos + 12, 0xFFFFFF);

        // Zoom-Anzeige
        String zoomText = String.format("%.1fx", getCurrentZoom());
        guiGraphics.drawString(this.font, "§7Zoom: " + zoomText,
                              leftPos + WIDTH - 70, topPos + 12, 0xFFFFFF);

        // Haupt-Kartenbereich
        int mapAreaX = leftPos + 10;
        int mapAreaY = topPos + 35;
        int mapAreaWidth = WIDTH - 20;
        int mapAreaHeight = HEIGHT - 45;

        renderMap(guiGraphics, mapAreaX, mapAreaY, mapAreaWidth, mapAreaHeight);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Rendert die Karte mit Minecraft-Map-Farben
     */
    private void renderMap(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Karten-Hintergrund
        guiGraphics.fill(x, y, x + width, y + height, 0xFF1A1A1A);

        if (minecraft == null || minecraft.player == null) {
            guiGraphics.drawCenteredString(this.font, "§7Lade Karte...",
                                          x + width / 2, y + height / 2, 0xFFFFFF);
            return;
        }

        Level level = minecraft.player.level();
        BlockPos playerPos = minecraft.player.blockPosition();

        // Update Karte nur alle paar Ticks
        updateCounter++;
        if (updateCounter >= UPDATE_INTERVAL) {
            updateMapData(level, playerPos);
            updateCounter = 0;
        }

        // Rendere die Pixel-Karte
        renderMapPixels(guiGraphics, x, y, width, height, playerPos);

        // Spieler-Position auf der Map - ROTER Punkt
        renderPlayerMarker(guiGraphics, x, y, width, height, playerPos);
    }

    /**
     * Rendert den Spieler-Marker auf der Map (immer im Zentrum)
     */
    private void renderPlayerMarker(GuiGraphics guiGraphics, int x, int y, int width, int height, BlockPos playerPos) {
        // Spieler ist immer im Zentrum der Ansicht
        int centerX = x + width / 2;
        int centerY = y + height / 2;

        // Roter Spieler-Marker (8x8 Pixel)
        guiGraphics.fill(centerX - 4, centerY - 4, centerX + 4, centerY + 4, 0xFFFF0000);
    }

    /**
     * Update Map-Daten - Chunk-basiert für unbegrenzte Exploration
     */
    private void updateMapData(Level level, BlockPos playerPos) {
        // Update-Radius: 10 Chunks = 160 Blöcke um den Spieler
        int chunkRadius = 10;
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;

        // Iteriere über alle Chunks im Radius
        for (int chunkX = playerChunkX - chunkRadius; chunkX <= playerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = playerChunkZ - chunkRadius; chunkZ <= playerChunkZ + chunkRadius; chunkZ++) {

                // Nur geladene Chunks updaten
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue; // Chunk nicht geladen - überspringen
                }

                // Hole oder erstelle Chunk-Daten
                long chunkKey = getChunkKey(chunkX, chunkZ);
                byte[] chunkData = exploredChunks.get(chunkKey);

                if (chunkData == null) {
                    chunkData = new byte[CHUNK_SIZE * CHUNK_SIZE];
                    exploredChunks.put(chunkKey, chunkData);
                }

                // Update alle 16x16 Blöcke in diesem Chunk
                for (int localX = 0; localX < CHUNK_SIZE; localX++) {
                    for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                        int worldX = (chunkX << 4) + localX;
                        int worldZ = (chunkZ << 4) + localZ;

                        int topY = level.getHeight(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                            worldX, worldZ
                        );

                        BlockPos topPos = new BlockPos(worldX, topY - 1, worldZ);
                        int color = getMapColor(level, topPos);

                        chunkData[localX + localZ * CHUNK_SIZE] = (byte) color;
                    }
                }
            }
        }
    }

    /**
     * Erstellt einen eindeutigen Key für Chunk-Koordinaten
     */
    private long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    /**
     * Holt MapColor-ID wie Minecraft es macht
     */
    private int getMapColor(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        var mapColor = state.getMapColor(level, pos);

        if (mapColor == net.minecraft.world.level.material.MapColor.NONE) {
            return 0;
        }

        return mapColor.id;
    }

    /**
     * Rendert Map-Pixel - Chunk-basiert, nur sichtbare Bereiche (Performance!)
     */
    private void renderMapPixels(GuiGraphics guiGraphics, int x, int y, int width, int height, BlockPos playerPos) {
        float zoom = getCurrentZoom();
        float scale = zoom; // 1 Block = 1 Pixel bei Zoom 1.0

        int pixelSize = Math.max(1, (int) scale);

        // Berechne Weltkoordinaten-Bereich der sichtbar ist
        // Spieler ist im Zentrum, Pan-Offset verschiebt die View
        int centerScreenX = x + width / 2 + panOffsetX;
        int centerScreenY = y + height / 2 + panOffsetY;

        // Welcher Weltblock ist an welcher Bildschirmposition?
        int viewMinWorldX = playerPos.getX() - (int) ((centerScreenX - x) / scale);
        int viewMaxWorldX = playerPos.getX() + (int) ((x + width - centerScreenX) / scale);
        int viewMinWorldZ = playerPos.getZ() - (int) ((centerScreenY - y) / scale);
        int viewMaxWorldZ = playerPos.getZ() + (int) ((y + height - centerScreenY) / scale);

        // Konvertiere zu Chunk-Koordinaten
        int minChunkX = viewMinWorldX >> 4;
        int maxChunkX = viewMaxWorldX >> 4;
        int minChunkZ = viewMinWorldZ >> 4;
        int maxChunkZ = viewMaxWorldZ >> 4;

        // Rendere nur sichtbare Chunks
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                long chunkKey = getChunkKey(chunkX, chunkZ);
                byte[] chunkData = exploredChunks.get(chunkKey);

                if (chunkData == null) {
                    continue; // Chunk nie erkundet - schwarz lassen
                }

                // Rendere alle 16x16 Blöcke in diesem Chunk
                for (int localX = 0; localX < CHUNK_SIZE; localX++) {
                    for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                        int worldX = (chunkX << 4) + localX;
                        int worldZ = (chunkZ << 4) + localZ;

                        byte colorId = chunkData[localX + localZ * CHUNK_SIZE];
                        if (colorId == 0) continue; // Keine Farbe

                        int color = getMaterialColor(colorId);

                        // Berechne Bildschirm-Position relativ zum Spieler
                        int dx = worldX - playerPos.getX();
                        int dz = worldZ - playerPos.getZ();

                        int screenX = centerScreenX + (int) (dx * scale);
                        int screenY = centerScreenY + (int) (dz * scale);

                        // Clipping
                        if (screenX + pixelSize < x || screenX > x + width ||
                            screenY + pixelSize < y || screenY > y + height) {
                            continue;
                        }

                        guiGraphics.fill(screenX, screenY,
                                       screenX + pixelSize, screenY + pixelSize, color);
                    }
                }
            }
        }
    }

    /**
     * Konvertiert MapColor ID zu RGB (Minecraft-Farben)
     */
    private int getMaterialColor(int id) {
        // Minecraft MapColor IDs -> RGB
        switch (id) {
            case 0: return 0x00000000; // NONE
            case 1: return 0xFF7FB238; // GRASS
            case 2: return 0xFFF7E9A3; // SAND
            case 3: return 0xFFC7C7C7; // WOOL
            case 4: return 0xFFFF0000; // FIRE
            case 5: return 0xFFA0A0FF; // ICE
            case 6: return 0xFFA7A7A7; // METAL
            case 7: return 0xFF007C00; // PLANT
            case 8: return 0xFFFFFFFF; // SNOW
            case 9: return 0xFFA4A8B8; // CLAY
            case 10: return 0xFF976D4D; // DIRT
            case 11: return 0xFF707070; // STONE
            case 12: return 0xFF4040FF; // WATER
            case 13: return 0xFF8B7653; // WOOD
            case 14: return 0xFFFFFFFF; // QUARTZ
            case 15: return 0xFFD87F33; // COLOR_ORANGE
            case 16: return 0xFFB24CD8; // COLOR_MAGENTA
            case 17: return 0xFF6699D8; // COLOR_LIGHT_BLUE
            case 18: return 0xFFE5E533; // COLOR_YELLOW
            case 19: return 0xFF7FCC19; // COLOR_LIGHT_GREEN
            case 20: return 0xFFF27FA5; // COLOR_PINK
            case 21: return 0xFF4C4C4C; // COLOR_GRAY
            case 22: return 0xFF999999; // COLOR_LIGHT_GRAY
            case 23: return 0xFF4C7F99; // COLOR_CYAN
            case 24: return 0xFF7F3FB2; // COLOR_PURPLE
            case 25: return 0xFF334CB2; // COLOR_BLUE
            case 26: return 0xFF664C33; // COLOR_BROWN
            case 27: return 0xFF667F33; // COLOR_GREEN
            case 28: return 0xFF993333; // COLOR_RED
            case 29: return 0xFF191919; // COLOR_BLACK
            case 30: return 0xFFB76A2C; // GOLD
            case 31: return 0xFF6DBAA1; // DIAMOND
            case 32: return 0xFF4164C0; // LAPIS
            case 33: return 0xFF00A000; // EMERALD
            case 34: return 0xFF603020; // PODZOL
            case 35: return 0xFF805020; // NETHER
            case 36: return 0xFFFFFFFF; // TERRACOTTA_WHITE
            case 37: return 0xFFD87F33; // TERRACOTTA_ORANGE
            case 38: return 0xFFB24CD8; // TERRACOTTA_MAGENTA
            default: return 0xFF808080; // Default grau
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Erst prüfen ob ein Button geklickt wurde
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Wenn kein Button geklickt wurde: Linke Maustaste startet Dragging
        if (button == 0) {
            isDragging = true;
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Stop Dragging
        if (button == 0) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            int deltaX = (int) mouseX - lastMouseX;
            int deltaY = (int) mouseY - lastMouseY;

            panOffsetX += deltaX;
            panOffsetY += deltaY;

            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;

            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Erst prüfen ob ein Widget das Event konsumiert
        if (super.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        // Scroll zum Zoomen
        if (delta > 0) {
            zoomIn();
            return true;
        } else if (delta < 0) {
            zoomOut();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

