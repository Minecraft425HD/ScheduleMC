package de.rolandsw.schedulemc.client.screen.apps;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Map App - Zeigt Karten und Standorte
 * Horizontale Ausrichtung für bessere Kartenansicht
 * Minimap ist als permanentes HUD-Overlay verfügbar (siehe MinimapOverlay.java)
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

    private static final float[] ZOOM_LEVELS = {0.5f, 1.0f, 2.0f, 4.0f}; // Zoom-Stufen
    private static final int UPDATE_INTERVAL = 5; // Update alle 5 Ticks

    private int leftPos;
    private int topPos;
    private int currentZoomIndex = 1; // Standard: 1.0x Zoom

    // Map-Marker-System (Vorbereitung für interaktive Symbole)
    private java.util.List<MapMarker> markers = new java.util.ArrayList<>();

    // Cache für Performance
    private int[][] cachedMapColors;
    private BlockPos lastCachePos = BlockPos.ZERO;
    private int lastCacheZoom = -1;
    private int tickCounter = 0;

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

        // Zoom Out Button (unten rechts)
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            zoomOut();
        }).bounds(leftPos + WIDTH - 60, topPos + HEIGHT - 30, 25, 20).build());

        // Zoom In Button (unten rechts)
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

        // Zoom-Anzeige im Header
        String zoomText = "Zoom: " + String.format("%.1fx", getCurrentZoom());
        guiGraphics.drawString(this.font, "§7" + zoomText,
                              leftPos + WIDTH - 70, topPos + 12, 0xFFFFFF);

        // Haupt-Kartenbereich
        int mapAreaX = leftPos + 10;
        int mapAreaY = topPos + 35;
        int mapAreaWidth = WIDTH - 20;
        int mapAreaHeight = HEIGHT - 45;

        renderMainMap(guiGraphics, mapAreaX, mapAreaY, mapAreaWidth, mapAreaHeight);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Rendert die Hauptkarte im großen Bereich
     */
    private void renderMainMap(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Karten-Hintergrund
        guiGraphics.fill(x, y, x + width, y + height, 0xFF1A1A1A);

        if (minecraft != null && minecraft.player != null) {
            Level level = minecraft.player.level();
            BlockPos playerPos = minecraft.player.blockPosition();

            // ULTRA-VEREINFACHT: Nur ein kleiner Bereich
            int viewRange = 20; // Fest, kein Zoom erstmal

            // Rendere DIREKT ohne Cache (für Testing)
            renderSimpleMap(guiGraphics, level, playerPos, x, y, width, height, viewRange);

            // Spieler-Position (Zentrum)
            int centerX = x + width / 2;
            int centerY = y + height / 2;

            // Roter Punkt für Spieler (größer, besser sichtbar)
            guiGraphics.fill(centerX - 4, centerY - 4, centerX + 4, centerY + 4, 0xFFFF0000);
        } else {
            guiGraphics.drawCenteredString(this.font, "§7Lade Karte...",
                                          x + width / 2, y + height / 2, 0xFFFFFF);
        }
    }

    /**
     * Ultra-einfaches Map-Rendering (kein Cache, direkt)
     */
    private void renderSimpleMap(GuiGraphics guiGraphics, Level level, BlockPos center,
                                  int x, int y, int width, int height, int range) {
        int pixelSize = 3; // Große Pixel für bessere Performance

        for (int dx = -range; dx < range; dx += 1) {
            for (int dz = -range; dz < range; dz += 1) {
                BlockPos pos = center.offset(dx, 0, dz);

                // Direkt Block-Farbe holen
                int color = getSimpleBlockColor(level, pos);

                int screenX = x + (dx + range) * pixelSize;
                int screenY = y + (dz + range) * pixelSize;

                guiGraphics.fill(screenX, screenY, screenX + pixelSize, screenY + pixelSize, color);
            }
        }
    }

    /**
     * Extrem vereinfachte Farb-Erkennung
     */
    private int getSimpleBlockColor(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos.above(64)); // Feste Y-Höhe erstmal

        // Nur die wichtigsten Blöcke
        if (state.is(Blocks.GRASS_BLOCK)) return 0xFF00FF00; // Grün
        if (state.is(Blocks.WATER)) return 0xFF0000FF; // Blau
        if (state.is(Blocks.STONE)) return 0xFF808080; // Grau
        if (state.is(Blocks.DIRT)) return 0xFF8B4513; // Braun
        if (state.is(Blocks.SAND)) return 0xFFFFFF00; // Gelb

        return 0xFF404040; // Dunkelgrau als Standard
    }

    /**
     * Update Map Cache
     */
    private void updateMapCache(Level level, BlockPos center, int range) {
        int size = range * 2;
        cachedMapColors = new int[size][size];

        for (int dx = -range; dx < range; dx++) {
            for (int dz = -range; dz < range; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                cachedMapColors[dx + range][dz + range] = getTerrainColorSimple(level, pos);
            }
        }
    }

    /**
     * Rendert gecachte Weltkarte
     */
    private void renderCachedWorldMap(GuiGraphics guiGraphics, int x, int y, int width, int height, int range) {
        int pixelSize = Math.max(1, Math.min(width, height) / (range * 2));

        for (int dx = 0; dx < range * 2; dx++) {
            for (int dz = 0; dz < range * 2; dz++) {
                int screenX = x + dx * pixelSize;
                int screenY = y + dz * pixelSize;

                int color = cachedMapColors[dx][dz];
                guiGraphics.fill(screenX, screenY, screenX + pixelSize, screenY + pixelSize, color);
            }
        }
    }

    /**
     * Vereinfachte Terrain-Farbe (Performance-optimiert)
     */
    private int getTerrainColorSimple(Level level, BlockPos pos) {
        int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        BlockPos topPos = new BlockPos(pos.getX(), topY - 1, pos.getZ());

        BlockState state = level.getBlockState(topPos);

        // Schnelle Block-Type Checks
        if (state.is(Blocks.WATER)) return 0xFF3030DD;
        if (state.is(Blocks.LAVA)) return 0xFFDD3030;
        if (state.is(Blocks.GRASS_BLOCK)) return 0xFF60A040;
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) return 0xFF8B6914;
        if (state.is(Blocks.SAND)) return 0xFFDDDD88;
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) return 0xFF888888;
        if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW)) return 0xFFFFFFFF;
        if (state.is(Blocks.OAK_LEAVES) || state.is(Blocks.SPRUCE_LEAVES)
            || state.is(Blocks.BIRCH_LEAVES)) return 0xFF228B22;
        if (state.is(Blocks.OAK_LOG) || state.is(Blocks.SPRUCE_LOG)) return 0xFF8B4513;

        // MapColor als Fallback
        MapColor mapColor = state.getMapColor(level, topPos);
        if (mapColor != MapColor.NONE) {
            int id = mapColor.col;
            if (id == 12) return 0xFF3030DD; // Wasser
            if (id == 1 || id == 7) return 0xFF60A040; // Gras/Pflanzen
            if (id == 10) return 0xFF8B6914; // Erde
            if (id == 11) return 0xFF888888; // Stein
            if (id == 2) return 0xFFDDDD88; // Sand
            if (id == 8) return 0xFFFFFFFF; // Schnee
        }

        return 0xFF505050;
    }

    /**
     * Rendert Map-Marker (Vorbereitung für interaktive Symbole)
     */
    private void renderMapMarkers(GuiGraphics guiGraphics, BlockPos playerPos,
                                  int x, int y, int width, int height, int range) {
        for (MapMarker marker : markers) {
            int dx = marker.pos.getX() - playerPos.getX();
            int dz = marker.pos.getZ() - playerPos.getZ();

            if (Math.abs(dx) > range || Math.abs(dz) > range) continue;

            int markerX = x + width / 2 + (dx * width / (range * 2));
            int markerY = y + height / 2 + (dz * height / (range * 2));

            // Render Marker-Symbol
            guiGraphics.fill(markerX - 2, markerY - 2, markerX + 2, markerY + 2, marker.color);

            // Optional: Marker-Label
            if (marker.label != null && !marker.label.isEmpty()) {
                guiGraphics.drawString(this.font, marker.label, markerX + 4, markerY - 4, 0xFFFFFF);
            }
        }
    }

    /**
     * Fügt einen Marker zur Karte hinzu
     */
    public void addMarker(BlockPos pos, int color, String label) {
        markers.add(new MapMarker(pos, color, label));
    }

    /**
     * Entfernt alle Marker
     */
    public void clearMarkers() {
        markers.clear();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Map-Marker Klasse für interaktive Symbole
     */
    public static class MapMarker {
        public final BlockPos pos;
        public final int color;
        public final String label;
        public String type; // Für zukünftige Erweiterungen (z.B. "waypoint", "dealer", "quest")

        public MapMarker(BlockPos pos, int color, String label) {
            this.pos = pos;
            this.color = color;
            this.label = label;
            this.type = "default";
        }

        public MapMarker(BlockPos pos, int color, String label, String type) {
            this.pos = pos;
            this.color = color;
            this.label = label;
            this.type = type;
        }
    }
}
