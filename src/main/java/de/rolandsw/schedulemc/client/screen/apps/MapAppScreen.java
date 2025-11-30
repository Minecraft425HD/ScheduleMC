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

    private int leftPos;
    private int topPos;
    private int currentZoomIndex = 1; // Standard: 1.0x Zoom

    // Map-Marker-System (Vorbereitung für interaktive Symbole)
    private java.util.List<MapMarker> markers = new java.util.ArrayList<>();

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

            // Berechne Kartenbereich basierend auf Zoom
            float zoom = getCurrentZoom();
            int viewRange = (int)(50 / zoom); // Angepasster Bereich

            // Rendere Karte
            renderWorldMap(guiGraphics, level, playerPos, x, y, width, height, viewRange);

            // Spieler-Position (Zentrum)
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            guiGraphics.fill(centerX - 3, centerY - 3, centerX + 3, centerY + 3, 0xFFFFFF00); // Gelber Punkt

            // Weißer Rand um Spieler
            guiGraphics.fill(centerX - 4, centerY - 4, centerX + 4, centerY - 3, 0xFFFFFFFF);
            guiGraphics.fill(centerX - 4, centerY + 3, centerX + 4, centerY + 4, 0xFFFFFFFF);
            guiGraphics.fill(centerX - 4, centerY - 3, centerX - 3, centerY + 3, 0xFFFFFFFF);
            guiGraphics.fill(centerX + 3, centerY - 3, centerX + 4, centerY + 3, 0xFFFFFFFF);

            // Render Marker
            renderMapMarkers(guiGraphics, playerPos, x, y, width, height, viewRange);
        } else {
            guiGraphics.drawCenteredString(this.font, "§7Lade Karte...",
                                          x + width / 2, y + height / 2, 0xFFFFFF);
        }
    }

    /**
     * Rendert eine vereinfachte Weltkarte
     */
    private void renderWorldMap(GuiGraphics guiGraphics, Level level, BlockPos center,
                                int x, int y, int width, int height, int range) {
        int pixelSize = Math.max(1, Math.min(width, height) / (range * 2));

        for (int dx = -range; dx < range; dx++) {
            for (int dz = -range; dz < range; dz++) {
                int color = getTerrainColor(level, center.offset(dx, 0, dz));

                int screenX = x + (dx + range) * pixelSize;
                int screenY = y + (dz + range) * pixelSize;

                guiGraphics.fill(screenX, screenY, screenX + pixelSize, screenY + pixelSize, color);
            }
        }
    }

    /**
     * Holt die Terrain-Farbe für eine Position
     */
    private int getTerrainColor(Level level, BlockPos pos) {
        // Finde obersten Block
        int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        BlockPos topPos = new BlockPos(pos.getX(), topY - 1, pos.getZ());

        BlockState state = level.getBlockState(topPos);

        // Falls Luft, gehe weiter runter
        if (state.isAir()) {
            for (int i = 1; i < 10; i++) {
                BlockPos checkPos = topPos.below(i);
                BlockState checkState = level.getBlockState(checkPos);
                if (!checkState.isAir()) {
                    state = checkState;
                    topPos = checkPos;
                    break;
                }
            }
        }

        // Nutze MapColor
        MapColor mapColor = state.getMapColor(level, topPos);

        // Fallback zu manueller Farbe
        if (mapColor == MapColor.NONE || mapColor.col == 0) {
            return getBlockTypeColor(state);
        }

        return getColorFromMapColor(mapColor.col);
    }

    /**
     * Konvertiert MapColor ID zu RGB
     */
    private int getColorFromMapColor(int colorId) {
        switch (colorId) {
            case 0: return 0xFF000000;
            case 1: return 0xFF7FB238; // Gras
            case 2: return 0xFFB5651D; // Sand
            case 3: return 0xFFC7C7C7; // Wolle
            case 4: return 0xFFFF0000; // Feuer
            case 5: return 0xFFA0A0FF; // Eis
            case 6: return 0xFFA7A7A7; // Metall
            case 7: return 0xFF007C00; // Pflanzen
            case 8: return 0xFFFFFFFF; // Schnee
            case 9: return 0xFFA4A8B8; // Lehm
            case 10: return 0xFF976D4D; // Erde
            case 11: return 0xFF707070; // Stein
            case 12: return 0xFF4040FF; // Wasser
            case 13: return 0xFF8B7653; // Holz
            case 14: return 0xFFFFFFFF; // Quarz
            case 15: return 0xFFFF9800; // Orange
            case 16: return 0xFFE91E63; // Magenta
            case 17: return 0xFF2196F3; // Hellblau
            case 18: return 0xFFFDD835; // Gelb
            case 19: return 0xFF8BC34A; // Lime
            case 20: return 0xFFF48FB1; // Rosa
            case 21: return 0xFF757575; // Grau
            case 22: return 0xFFBDBDBD; // Hellgrau
            case 23: return 0xFF00BCD4; // Cyan
            case 24: return 0xFF9C27B0; // Lila
            case 25: return 0xFF3F51B5; // Blau
            case 26: return 0xFF795548; // Braun
            case 27: return 0xFF4CAF50; // Grün
            case 28: return 0xFFFF5722; // Rot
            case 29: return 0xFF212121; // Schwarz
            default: return 0xFF808080;
        }
    }

    /**
     * Fallback-Farbe basierend auf Block-Typ
     */
    private int getBlockTypeColor(BlockState state) {
        if (state.is(Blocks.WATER) || state.is(Blocks.FLOWING_WATER)) {
            return 0xFF4040FF;
        }
        if (state.is(Blocks.LAVA) || state.is(Blocks.FLOWING_LAVA)) {
            return 0xFFFF4400;
        }
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) {
            return 0xFF7FB238;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.ANDESITE)
            || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
            return 0xFF808080;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) {
            return 0xFF976D4D;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.SANDSTONE)) {
            return 0xFFB5651D;
        }
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
            return 0xFFFFFFFF;
        }
        if (state.is(Blocks.OAK_LOG) || state.is(Blocks.OAK_PLANKS) || state.is(Blocks.SPRUCE_LOG)
            || state.is(Blocks.BIRCH_LOG) || state.is(Blocks.JUNGLE_LOG)) {
            return 0xFF8B7653;
        }
        if (state.is(Blocks.OAK_LEAVES) || state.is(Blocks.SPRUCE_LEAVES)
            || state.is(Blocks.BIRCH_LEAVES) || state.is(Blocks.JUNGLE_LEAVES)) {
            return 0xFF007C00;
        }
        return 0xFF404040;
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
