package de.rolandsw.schedulemc.mapview.navigation.graph;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;

import java.util.List;

/**
 * RoadPathRenderer - Rendert den Navigationspfad auf der Karte
 *
 * Features:
 * - Farbige Pfadlinie mit Farbverlauf
 * - Richtungspfeile entlang des Pfads
 * - Zielmarker mit Animation
 * - Distanzanzeige
 */
public class RoadPathRenderer {

    // Farben
    private static final int PATH_COLOR_START = 0xFF4CAF50;  // Grün (Start)
    private static final int PATH_COLOR_END = 0xFFFF5722;    // Orange (Ziel)
    private static final int PATH_COLOR_PASSED = 0x80808080;  // Grau (bereits passiert)
    private static final int TARGET_COLOR = 0xFFFF0000;      // Rot (Zielmarker)
    private static final int ARROW_COLOR = 0xFFFFFFFF;       // Weiß (Pfeile)

    // Konfiguration
    private static final float PATH_WIDTH = 3.0f;
    private static final float ARROW_SIZE = 6.0f;
    private static final int ARROW_INTERVAL = 5; // Jeder n-te Punkt bekommt einen Pfeil
    private static final float TARGET_PULSE_SPEED = 0.003f;

    // Animation
    private float targetPulse = 0;

    /**
     * Rendert den kompletten Navigationspfad
     *
     * @param graphics GuiGraphics-Kontext
     * @param path Der zu rendernde Pfad
     * @param currentIndex Aktueller Fortschritt auf dem Pfad
     * @param target Das Navigationsziel
     * @param mapCenterX Karten-Zentrum X (Spielerposition)
     * @param mapCenterZ Karten-Zentrum Z
     * @param mapSize Kartengröße in Pixeln
     * @param zoom Zoom-Faktor
     * @param rotation Kartenrotation in Grad
     */
    public void render(GuiGraphics graphics, List<BlockPos> path, int currentIndex,
                       NavigationTarget target, int mapCenterX, int mapCenterZ,
                       int mapSize, float zoom, float rotation) {

        if (path == null || path.isEmpty()) {
            return;
        }

        PoseStack poseStack = graphics.pose();

        // Update Animation
        targetPulse += TARGET_PULSE_SPEED;
        if (targetPulse > 1.0f) targetPulse = 0;

        // Rendere Pfadlinie
        renderPathLine(poseStack, path, currentIndex, mapCenterX, mapCenterZ, mapSize, zoom, rotation);

        // Rendere Richtungspfeile
        renderArrows(poseStack, path, currentIndex, mapCenterX, mapCenterZ, mapSize, zoom, rotation);

        // Rendere Zielmarker
        if (target != null) {
            BlockPos targetPos = target.getCurrentPosition();
            if (targetPos != null) {
                renderTargetMarker(poseStack, targetPos, mapCenterX, mapCenterZ, mapSize, zoom, rotation);
            }
        }
    }

    /**
     * Rendert die Pfadlinie
     */
    private void renderPathLine(PoseStack poseStack, List<BlockPos> path, int currentIndex,
                                 int mapCenterX, int mapCenterZ, int mapSize, float zoom, float rotation) {

        if (path.size() < 2) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float halfSize = mapSize / 2.0f;

        for (int i = 0; i < path.size() - 1; i++) {
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);

            // Konvertiere Weltkoordinaten zu Kartenkoordinaten
            float x1 = worldToMap(current.getX(), mapCenterX, zoom, halfSize);
            float z1 = worldToMap(current.getZ(), mapCenterZ, zoom, halfSize);
            float x2 = worldToMap(next.getX(), mapCenterX, zoom, halfSize);
            float z2 = worldToMap(next.getZ(), mapCenterZ, zoom, halfSize);

            // Rotation anwenden
            if (rotation != 0) {
                float[] rotated1 = rotatePoint(x1, z1, halfSize, halfSize, rotation);
                float[] rotated2 = rotatePoint(x2, z2, halfSize, halfSize, rotation);
                x1 = rotated1[0];
                z1 = rotated1[1];
                x2 = rotated2[0];
                z2 = rotated2[1];
            }

            // Farbe basierend auf Fortschritt
            int color;
            if (i < currentIndex) {
                color = PATH_COLOR_PASSED;
            } else {
                float progress = (float) (i - currentIndex) / (path.size() - currentIndex);
                color = interpolateColor(PATH_COLOR_START, PATH_COLOR_END, progress);
            }

            // Berechne Normale für Linienbreite
            float dx = x2 - x1;
            float dz = z2 - z1;
            float length = (float) Math.sqrt(dx * dx + dz * dz);
            if (length < 0.001f) continue;

            float nx = -dz / length * PATH_WIDTH / 2;
            float nz = dx / length * PATH_WIDTH / 2;

            // Extrahiere RGBA
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = ((color >> 24) & 0xFF) / 255.0f;

            // Zeichne Liniensegment als Quad
            buffer.addVertex(matrix, x1 - nx, z1 - nz, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1 + nx, z1 + nz, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x2 - nx, z2 - nz, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x2 + nx, z2 + nz, 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    /**
     * Rendert Richtungspfeile entlang des Pfads
     */
    private void renderArrows(PoseStack poseStack, List<BlockPos> path, int currentIndex,
                              int mapCenterX, int mapCenterZ, int mapSize, float zoom, float rotation) {

        if (path.size() < 2) return;

        float halfSize = mapSize / 2.0f;

        for (int i = currentIndex + 1; i < path.size() - 1; i += ARROW_INTERVAL) {
            BlockPos prev = path.get(i - 1);
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);

            // Position
            float x = worldToMap(current.getX(), mapCenterX, zoom, halfSize);
            float z = worldToMap(current.getZ(), mapCenterZ, zoom, halfSize);

            // Richtung (Durchschnitt von prev->current und current->next)
            float dx = (next.getX() - prev.getX()) / 2.0f;
            float dz = (next.getZ() - prev.getZ()) / 2.0f;
            float angle = (float) Math.toDegrees(Math.atan2(-dx, dz));

            // Rotation anwenden
            if (rotation != 0) {
                float[] rotated = rotatePoint(x, z, halfSize, halfSize, rotation);
                x = rotated[0];
                z = rotated[1];
                angle -= rotation;
            }

            renderArrow(poseStack, x, z, angle, ARROW_SIZE);
        }
    }

    /**
     * Rendert einen einzelnen Pfeil
     */
    private void renderArrow(PoseStack poseStack, float x, float y, float angle, float size) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float r = ((ARROW_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((ARROW_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (ARROW_COLOR & 0xFF) / 255.0f;
        float a = 0.9f;

        // Dreieck nach oben
        buffer.addVertex(matrix, 0, -size / 2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, -size / 3, size / 3, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, size / 3, size / 3, 0).setColor(r, g, b, a);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();
    }

    /**
     * Rendert den Zielmarker
     */
    private void renderTargetMarker(PoseStack poseStack, BlockPos targetPos,
                                     int mapCenterX, int mapCenterZ, int mapSize, float zoom, float rotation) {

        float halfSize = mapSize / 2.0f;
        float x = worldToMap(targetPos.getX(), mapCenterX, zoom, halfSize);
        float z = worldToMap(targetPos.getZ(), mapCenterZ, zoom, halfSize);

        // Rotation anwenden
        if (rotation != 0) {
            float[] rotated = rotatePoint(x, z, halfSize, halfSize, rotation);
            x = rotated[0];
            z = rotated[1];
        }

        // Pulsierende Animation
        float pulse = (float) (1.0 + 0.3 * Math.sin(targetPulse * Math.PI * 2));
        float markerSize = 8 * pulse;

        poseStack.pushPose();
        poseStack.translate(x, z, 0);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float r = ((TARGET_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((TARGET_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (TARGET_COLOR & 0xFF) / 255.0f;

        // Zentrierter Punkt
        buffer.addVertex(matrix, 0, 0, 0).setColor(r, g, b, 1.0f);

        // Kreis um den Punkt
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float px = (float) (Math.cos(angle) * markerSize);
            float pz = (float) (Math.sin(angle) * markerSize);
            buffer.addVertex(matrix, px, pz, 0).setColor(r, g, b, 0.5f);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();

        poseStack.popPose();

        // Äußerer Ring
        renderRing(poseStack, x, z, markerSize * 1.5f, TARGET_COLOR, 0.3f);
    }

    /**
     * Rendert einen Ring
     */
    private void renderRing(PoseStack poseStack, float x, float y, float radius, int color, float alpha) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        int segments = 24;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float px = (float) (Math.cos(angle) * radius);
            float py = (float) (Math.sin(angle) * radius);
            buffer.addVertex(matrix, px, py, 0).setColor(r, g, b, alpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    /**
     * Rendert die Distanzanzeige
     */
    public void renderDistanceOverlay(GuiGraphics graphics, double distance, int x, int y) {
        String distanceText;
        if (distance >= 1000) {
            distanceText = String.format("%.1f km", distance / 1000);
        } else {
            distanceText = String.format("%.0f m", distance);
        }

        // Hintergrund
        int textWidth = Minecraft.getInstance().font.width(distanceText);
        graphics.fill(x - 2, y - 2, x + textWidth + 2, y + 10, 0x80000000);

        // Text
        graphics.drawString(Minecraft.getInstance().font, distanceText, x, y, 0xFFFFFFFF, false);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Konvertiert Weltkoordinaten zu Kartenkoordinaten
     */
    private float worldToMap(int worldCoord, int mapCenter, float zoom, float halfSize) {
        return halfSize + (worldCoord - mapCenter) / zoom;
    }

    /**
     * Rotiert einen Punkt um ein Zentrum
     */
    private float[] rotatePoint(float x, float y, float centerX, float centerY, float angleDegrees) {
        float angleRad = (float) Math.toRadians(angleDegrees);
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);

        float dx = x - centerX;
        float dy = y - centerY;

        return new float[] {
                centerX + dx * cos - dy * sin,
                centerY + dx * sin + dy * cos
        };
    }

    /**
     * Interpoliert zwischen zwei Farben
     */
    private int interpolateColor(int color1, int color2, float progress) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * progress);
        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
