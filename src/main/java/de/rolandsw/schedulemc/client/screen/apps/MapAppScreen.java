package de.rolandsw.schedulemc.client.screen.apps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

/**
 * Map App - Zeigt Karten und Standorte mit Minimap
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

    // Minimap-Konstanten
    private static final int MINIMAP_SIZE = 80;
    private static final int MINIMAP_MARGIN = 10;
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
        int mapAreaHeight = HEIGHT - 75;

        renderMainMap(guiGraphics, mapAreaX, mapAreaY, mapAreaWidth, mapAreaHeight);

        // Minimap (oben rechts)
        int minimapX = leftPos + WIDTH - MINIMAP_SIZE - MINIMAP_MARGIN;
        int minimapY = topPos + 35;
        renderMinimap(guiGraphics, minimapX, minimapY, partialTick);

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
            int viewRange = (int)(width / (4 * zoom)); // Angepasster Bereich

            // Rendere vereinfachte Karte
            renderWorldMap(guiGraphics, level, playerPos, x, y, width, height, viewRange);

            // Spieler-Position (Zentrum)
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            guiGraphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFF00); // Gelber Punkt

            // Render Marker
            renderMapMarkers(guiGraphics, playerPos, x, y, width, height, viewRange);
        } else {
            guiGraphics.drawCenteredString(this.font, "§7Lade Karte...",
                                          x + width / 2, y + height / 2, 0xFFFFFF);
        }
    }

    /**
     * Rendert die runde Minimap mit Himmelsrichtungen
     */
    private void renderMinimap(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        // Hintergrund-Quadrat
        guiGraphics.fill(x - 2, y - 2, x + MINIMAP_SIZE + 2, y + MINIMAP_SIZE + 2, 0xFF1A1A1A);

        if (minecraft != null && minecraft.player != null) {
            Level level = minecraft.player.level();
            BlockPos playerPos = minecraft.player.blockPosition();
            float playerYaw = minecraft.player.getYRot();

            // Berechne Minimap-Bereich
            int minimapRange = 32; // Fester Bereich für Minimap

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();

            // Transformiere für Rotation um Zentrum
            int centerX = x + MINIMAP_SIZE / 2;
            int centerY = y + MINIMAP_SIZE / 2;
            poseStack.translate(centerX, centerY, 0);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(playerYaw));
            poseStack.translate(-centerX, -centerY, 0);

            // Rendere rotierende Karte
            renderWorldMapCircular(guiGraphics, level, playerPos, x, y, MINIMAP_SIZE, minimapRange);

            poseStack.popPose();

            // Runde Maske (darüber, nicht rotiert)
            renderCircularMask(guiGraphics, centerX, centerY, MINIMAP_SIZE / 2);

            // Himmelsrichtungen (fixiert, nicht rotiert)
            renderCardinalDirections(guiGraphics, centerX, centerY, MINIMAP_SIZE / 2);

            // Spieler-Marker (Zentrum, nicht rotiert)
            guiGraphics.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, 0xFFFFFF00);
        }
    }

    /**
     * Rendert eine vereinfachte Weltkarte
     */
    private void renderWorldMap(GuiGraphics guiGraphics, Level level, BlockPos center,
                                int x, int y, int width, int height, int range) {
        int pixelSize = Math.max(1, width / (range * 2));

        for (int dx = -range; dx < range; dx++) {
            for (int dz = -range; dz < range; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
                BlockPos topPos = new BlockPos(pos.getX(), topY, pos.getZ());

                BlockState state = level.getBlockState(topPos);
                int color = getBlockColor(state, level, topPos);

                int screenX = x + (dx + range) * pixelSize;
                int screenY = y + (dz + range) * pixelSize;

                guiGraphics.fill(screenX, screenY, screenX + pixelSize, screenY + pixelSize, color);
            }
        }
    }

    /**
     * Rendert eine kreisförmige Weltkarte für die Minimap
     */
    private void renderWorldMapCircular(GuiGraphics guiGraphics, Level level, BlockPos center,
                                       int x, int y, int size, int range) {
        int pixelSize = Math.max(1, size / (range * 2));
        int radius = size / 2;

        for (int dx = -range; dx < range; dx++) {
            for (int dz = -range; dz < range; dz++) {
                // Prüfe ob Punkt im Kreis liegt
                int screenX = (dx + range) * pixelSize;
                int screenY = (dz + range) * pixelSize;
                int distFromCenter = (int)Math.sqrt(
                    Math.pow(screenX - radius, 2) + Math.pow(screenY - radius, 2)
                );

                if (distFromCenter > radius) continue;

                BlockPos pos = center.offset(dx, 0, dz);
                int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
                BlockPos topPos = new BlockPos(pos.getX(), topY, pos.getZ());

                BlockState state = level.getBlockState(topPos);
                int color = getBlockColor(state, level, topPos);

                guiGraphics.fill(x + screenX, y + screenY,
                               x + screenX + pixelSize, y + screenY + pixelSize, color);
            }
        }
    }

    /**
     * Erstellt eine kreisförmige Maske für die Minimap
     */
    private void renderCircularMask(GuiGraphics guiGraphics, int centerX, int centerY, int radius) {
        // Zeichne schwarzen Rand um den Kreis
        int size = radius * 2;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                int distSq = x * x + y * y;
                if (distSq > radius * radius) {
                    guiGraphics.fill(centerX + x, centerY + y,
                                   centerX + x + 1, centerY + y + 1, 0xFF2A2A2A);
                }
            }
        }

        // Äußerer Ring
        drawCircleOutline(guiGraphics, centerX, centerY, radius, 0xFF4A4A4A);
    }

    /**
     * Zeichnet einen Kreis-Umriss
     */
    private void drawCircleOutline(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
        for (int angle = 0; angle < 360; angle += 2) {
            double rad = Math.toRadians(angle);
            int x = centerX + (int)(Math.cos(rad) * radius);
            int y = centerY + (int)(Math.sin(rad) * radius);
            guiGraphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    /**
     * Rendert Himmelsrichtungen um die Minimap
     */
    private void renderCardinalDirections(GuiGraphics guiGraphics, int centerX, int centerY, int radius) {
        int offset = radius + 8;

        // N (Norden - oben)
        guiGraphics.drawCenteredString(this.font, "§fN", centerX, centerY - offset, 0xFFFFFF);

        // S (Süden - unten)
        guiGraphics.drawCenteredString(this.font, "§7S", centerX, centerY + offset - 8, 0xFFFFFF);

        // O (Osten - rechts)
        guiGraphics.drawString(this.font, "§7O", centerX + offset - 4, centerY - 4, 0xFFFFFF);

        // W (Westen - links)
        guiGraphics.drawString(this.font, "§7W", centerX - offset - 4, centerY - 4, 0xFFFFFF);
    }

    /**
     * Konvertiert einen BlockState zu einer Farbe
     */
    private int getBlockColor(BlockState state, Level level, BlockPos pos) {
        // Nutze Minecraft's MapColor System
        MapColor mapColor = state.getMapColor(level, pos);
        if (mapColor == MapColor.NONE) {
            return 0xFF1A1A1A; // Dunkelgrau für unbekannt
        }

        // Extrahiere Farbe aus MapColor
        int colorId = mapColor.col;

        // Basis-Farben (vereinfacht)
        switch (colorId) {
            case 2: return 0xFF7FB238; // Gras
            case 4: return 0xFF8C8C8C; // Stein
            case 8: return 0xFF3C76DD; // Wasser
            case 12: return 0xFFA0522D; // Erde
            case 14: return 0xFF707070; // Grau
            case 16: return 0xFFFF0000; // Rot
            case 20: return 0xFF00FF00; // Grün
            default: return 0xFF606060; // Standard Grau
        }
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
