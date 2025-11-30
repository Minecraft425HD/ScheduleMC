package de.rolandsw.schedulemc.client.screen.apps;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    // Karte: 192x192 statt 128x128 für bessere Abdeckung
    private static final int MAP_SIZE = 192;
    private byte[] mapColors = new byte[MAP_SIZE * MAP_SIZE];
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
        renderMapPixels(guiGraphics, x, y, width, height);

        // Spieler-Position (Zentrum) - ROTER Punkt
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        guiGraphics.fill(centerX - 4, centerY - 4, centerX + 4, centerY + 4, 0xFFFF0000);
    }

    /**
     * Update Map-Daten (wie Minecraft Maps) - jetzt 192x192
     */
    private void updateMapData(Level level, BlockPos center) {
        int range = MAP_SIZE / 2; // 96 Blöcke in jede Richtung

        for (int x = 0; x < MAP_SIZE; x++) {
            for (int z = 0; z < MAP_SIZE; z++) {
                int worldX = center.getX() - range + x;
                int worldZ = center.getZ() - range + z;

                int topY = level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    worldX, worldZ
                );

                BlockPos topPos = new BlockPos(worldX, topY - 1, worldZ);
                int color = getMapColor(level, topPos);

                mapColors[x + z * MAP_SIZE] = (byte) color;
            }
        }
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
     * Rendert Map-Pixel auf den Bildschirm mit Zoom & Pan
     */
    private void renderMapPixels(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        float zoom = getCurrentZoom();

        // Skalierung basierend auf Zoom
        float baseScale = Math.min((float) width / MAP_SIZE, (float) height / MAP_SIZE);
        float scale = baseScale * zoom;

        int renderWidth = (int) (MAP_SIZE * scale);
        int renderHeight = (int) (MAP_SIZE * scale);

        // Pan-Offset anwenden
        int offsetX = (width - renderWidth) / 2 + panOffsetX;
        int offsetY = (height - renderHeight) / 2 + panOffsetY;

        int pixelSize = Math.max(1, (int) scale);

        for (int mapX = 0; mapX < MAP_SIZE; mapX++) {
            for (int mapZ = 0; mapZ < MAP_SIZE; mapZ++) {
                byte colorId = mapColors[mapX + mapZ * MAP_SIZE];

                if (colorId == 0) continue;

                int color = getMaterialColor(colorId);

                int screenX = x + offsetX + (int) (mapX * scale);
                int screenY = y + offsetY + (int) (mapZ * scale);

                // Clipping: Nur rendern wenn sichtbar
                if (screenX + pixelSize < x || screenX > x + width ||
                    screenY + pixelSize < y || screenY > y + height) {
                    continue;
                }

                guiGraphics.fill(screenX, screenY,
                               screenX + pixelSize, screenY + pixelSize, color);
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
        // Linke Maustaste: Start Dragging
        if (button == 0) {
            isDragging = true;
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        // Scroll zum Zoomen
        if (delta > 0) {
            zoomIn();
            return true;
        } else if (delta < 0) {
            zoomOut();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

