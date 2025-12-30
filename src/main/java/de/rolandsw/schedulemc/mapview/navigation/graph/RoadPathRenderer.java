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
     * Rendert die Pfadlinie (1.20.1 API)
     */
    private void renderPathLine(PoseStack poseStack, List<BlockPos> path, int currentIndex,
                                 int mapCenterX, int mapCenterZ, int mapSize, float zoom, float rotation) {

        if (path.size() < 2) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // Deaktiviere Tiefentest für Overlay
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float halfSize = mapSize / 2.0f;
        float zLevel = 100.0f; // Über der Karte rendern

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

            // Zeichne Liniensegment als Quad mit höherer Z-Ebene
            buffer.vertex(matrix, x1 - nx, z1 - nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x1 + nx, z1 + nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x2 + nx, z2 + nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x2 - nx, z2 - nz, zLevel).color(r, g, b, a).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest(); // Tiefentest wieder aktivieren
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
     * Rendert einen einzelnen Pfeil (1.20.1 API)
     */
    private void renderArrow(PoseStack poseStack, float x, float y, float angle, float size) {
        poseStack.pushPose();
        poseStack.translate(x, y, 100); // Höhere Z-Ebene
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));

        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float r = ((ARROW_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((ARROW_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (ARROW_COLOR & 0xFF) / 255.0f;
        float a = 0.9f;

        // Dreieck nach oben
        buffer.vertex(matrix, 0, -size / 2, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, -size / 3, size / 3, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, size / 3, size / 3, 0).color(r, g, b, a).endVertex();

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();

        poseStack.popPose();
    }

    /**
     * Rendert den Zielmarker (1.20.1 API)
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
        poseStack.translate(x, z, 100); // Höhere Z-Ebene

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float r = ((TARGET_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((TARGET_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (TARGET_COLOR & 0xFF) / 255.0f;

        // Zentrierter Punkt
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, 1.0f).endVertex();

        // Kreis um den Punkt
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            float circleAngle = (float) (i * 2 * Math.PI / segments);
            float px = (float) (Math.cos(circleAngle) * markerSize);
            float pz = (float) (Math.sin(circleAngle) * markerSize);
            buffer.vertex(matrix, px, pz, 0).color(r, g, b, 0.5f).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();

        // Äußerer Ring
        renderRing(poseStack, x, z, markerSize * 1.5f, TARGET_COLOR, 0.3f);
    }

    /**
     * Rendert einen Ring (1.20.1 API)
     */
    private void renderRing(PoseStack poseStack, float x, float y, float radius, int color, float alpha) {
        poseStack.pushPose();
        poseStack.translate(x, y, 100); // Höhere Z-Ebene

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        int segments = 24;
        for (int i = 0; i <= segments; i++) {
            float ringAngle = (float) (i * 2 * Math.PI / segments);
            float px = (float) (Math.cos(ringAngle) * radius);
            float py = (float) (Math.sin(ringAngle) * radius);
            buffer.vertex(matrix, px, py, 0).color(r, g, b, alpha).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
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
    // MINIMAP ACCURATE RENDERING
    // ═══════════════════════════════════════════════════════════

    /**
     * Rendert den Pfad pixelgenau für die Minimap
     *
     * @param graphics GuiGraphics-Kontext
     * @param path Der zu rendernde Pfad
     * @param currentIndex Aktueller Fortschritt auf dem Pfad
     * @param target Das Navigationsziel
     * @param worldCenterX Weltzentrum X (Spielerposition)
     * @param worldCenterZ Weltzentrum Z
     * @param screenCenterX Bildschirmzentrum X der Minimap
     * @param screenCenterY Bildschirmzentrum Y der Minimap
     * @param mapSize Kartengröße in Pixeln
     * @param scale Pixel pro Block
     * @param rotation Kartenrotation in Grad
     */
    public void renderMinimapAccurate(GuiGraphics graphics, List<BlockPos> path, int currentIndex,
                                       NavigationTarget target, int worldCenterX, int worldCenterZ,
                                       int screenCenterX, int screenCenterY, int mapSize,
                                       float scale, float rotation) {

        if (path == null || path.isEmpty()) {
            return;
        }

        PoseStack poseStack = graphics.pose();

        // Update Animation
        targetPulse += TARGET_PULSE_SPEED;
        if (targetPulse > 1.0f) targetPulse = 0;

        // Rendere Pfadlinie
        renderPathLineMinimap(poseStack, path, currentIndex, worldCenterX, worldCenterZ,
                screenCenterX, screenCenterY, mapSize, scale, rotation);

        // Rendere Richtungspfeile
        renderArrowsMinimap(poseStack, path, currentIndex, worldCenterX, worldCenterZ,
                screenCenterX, screenCenterY, mapSize, scale, rotation);

        // Rendere Zielmarker
        if (target != null) {
            BlockPos targetPos = target.getCurrentPosition();
            if (targetPos != null) {
                renderTargetMarkerMinimap(poseStack, targetPos, worldCenterX, worldCenterZ,
                        screenCenterX, screenCenterY, mapSize, scale, rotation);
            }
        }
    }

    /**
     * Rendert die Pfadlinie für die Minimap
     */
    private void renderPathLineMinimap(PoseStack poseStack, List<BlockPos> path, int currentIndex,
                                        int worldCenterX, int worldCenterZ,
                                        int screenCenterX, int screenCenterY, int mapSize,
                                        float scale, float rotation) {

        if (path.size() < 2) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float zLevel = 100.0f;
        float halfMap = mapSize / 2.0f;

        for (int i = 0; i < path.size() - 1; i++) {
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);

            // Konvertiere Weltkoordinaten zu Bildschirmkoordinaten
            float relX1 = (current.getX() - worldCenterX) * scale;
            float relZ1 = (current.getZ() - worldCenterZ) * scale;
            float relX2 = (next.getX() - worldCenterX) * scale;
            float relZ2 = (next.getZ() - worldCenterZ) * scale;

            // Rotation anwenden
            if (rotation != 0) {
                float[] rotated1 = rotatePoint(relX1, relZ1, 0, 0, rotation);
                float[] rotated2 = rotatePoint(relX2, relZ2, 0, 0, rotation);
                relX1 = rotated1[0];
                relZ1 = rotated1[1];
                relX2 = rotated2[0];
                relZ2 = rotated2[1];
            }

            // Zu Bildschirmkoordinaten
            float x1 = screenCenterX + relX1;
            float z1 = screenCenterY + relZ1;
            float x2 = screenCenterX + relX2;
            float z2 = screenCenterY + relZ2;

            // Clipping: Nur rendern wenn innerhalb der Minimap
            if (Math.abs(relX1) > halfMap && Math.abs(relX2) > halfMap) continue;
            if (Math.abs(relZ1) > halfMap && Math.abs(relZ2) > halfMap) continue;

            // Farbe basierend auf Fortschritt
            int color;
            if (i < currentIndex) {
                color = PATH_COLOR_PASSED;
            } else {
                float progress = (float) (i - currentIndex) / Math.max(1, path.size() - currentIndex);
                color = interpolateColor(PATH_COLOR_START, PATH_COLOR_END, progress);
            }

            // Berechne Normale für Linienbreite
            float dx = x2 - x1;
            float dz = z2 - z1;
            float length = (float) Math.sqrt(dx * dx + dz * dz);
            if (length < 0.001f) continue;

            float lineWidth = PATH_WIDTH * 0.7f; // Etwas dünner für Minimap
            float nx = -dz / length * lineWidth;
            float nz = dx / length * lineWidth;

            // Extrahiere RGBA
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = ((color >> 24) & 0xFF) / 255.0f;

            // Zeichne Liniensegment als Quad
            buffer.vertex(matrix, x1 - nx, z1 - nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x1 + nx, z1 + nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x2 + nx, z2 + nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x2 - nx, z2 - nz, zLevel).color(r, g, b, a).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /**
     * Rendert Richtungspfeile für die Minimap
     */
    private void renderArrowsMinimap(PoseStack poseStack, List<BlockPos> path, int currentIndex,
                                      int worldCenterX, int worldCenterZ,
                                      int screenCenterX, int screenCenterY, int mapSize,
                                      float scale, float rotation) {

        if (path.size() < 2) return;

        float halfMap = mapSize / 2.0f;

        for (int i = currentIndex + 1; i < path.size() - 1; i += ARROW_INTERVAL) {
            BlockPos prev = path.get(i - 1);
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);

            // Position relativ zum Zentrum
            float relX = (current.getX() - worldCenterX) * scale;
            float relZ = (current.getZ() - worldCenterZ) * scale;

            // Rotation anwenden
            if (rotation != 0) {
                float[] rotated = rotatePoint(relX, relZ, 0, 0, rotation);
                relX = rotated[0];
                relZ = rotated[1];
            }

            // Nur rendern wenn innerhalb der Minimap
            if (Math.abs(relX) > halfMap || Math.abs(relZ) > halfMap) continue;

            float x = screenCenterX + relX;
            float z = screenCenterY + relZ;

            // Richtung
            float dx = (next.getX() - prev.getX()) / 2.0f;
            float dz = (next.getZ() - prev.getZ()) / 2.0f;
            float angle = (float) Math.toDegrees(Math.atan2(-dx, dz)) - rotation;

            float arrowSize = ARROW_SIZE * 0.6f; // Kleiner für Minimap
            renderArrow(poseStack, x, z, angle, arrowSize);
        }
    }

    /**
     * Rendert den Zielmarker für die Minimap
     */
    private void renderTargetMarkerMinimap(PoseStack poseStack, BlockPos targetPos,
                                            int worldCenterX, int worldCenterZ,
                                            int screenCenterX, int screenCenterY, int mapSize,
                                            float scale, float rotation) {

        float halfMap = mapSize / 2.0f;

        // Position relativ zum Zentrum
        float relX = (targetPos.getX() - worldCenterX) * scale;
        float relZ = (targetPos.getZ() - worldCenterZ) * scale;

        // Rotation anwenden
        if (rotation != 0) {
            float[] rotated = rotatePoint(relX, relZ, 0, 0, rotation);
            relX = rotated[0];
            relZ = rotated[1];
        }

        // Prüfe ob außerhalb der Minimap - zeige Richtungsindikator am Rand
        boolean outsideMap = Math.abs(relX) > halfMap || Math.abs(relZ) > halfMap;

        if (outsideMap) {
            // Normalisiere auf Rand der Minimap
            float dist = (float) Math.sqrt(relX * relX + relZ * relZ);
            if (dist > 0) {
                float edgeDist = halfMap - 5; // Etwas innerhalb des Randes
                relX = relX / dist * edgeDist;
                relZ = relZ / dist * edgeDist;
            }
        }

        float x = screenCenterX + relX;
        float z = screenCenterY + relZ;

        // Pulsierende Animation
        float pulse = (float) (1.0 + 0.3 * Math.sin(targetPulse * Math.PI * 2));
        float markerSize = (outsideMap ? 4 : 6) * pulse; // Kleiner wenn außerhalb

        poseStack.pushPose();
        poseStack.translate(x, z, 100);

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float r = ((TARGET_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((TARGET_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (TARGET_COLOR & 0xFF) / 255.0f;

        // Zentrierter Punkt
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, 1.0f).endVertex();

        // Kreis um den Punkt
        int segments = 12;
        for (int i = 0; i <= segments; i++) {
            float circleAngle = (float) (i * 2 * Math.PI / segments);
            float px = (float) (Math.cos(circleAngle) * markerSize);
            float pz = (float) (Math.sin(circleAngle) * markerSize);
            buffer.vertex(matrix, px, pz, 0).color(r, g, b, 0.5f).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    // ═══════════════════════════════════════════════════════════
    // WORLDMAP ACCURATE RENDERING
    // ═══════════════════════════════════════════════════════════

    /**
     * Rendert den Pfad pixelgenau für die Fullscreen-Worldmap
     *
     * @param graphics GuiGraphics-Kontext
     * @param path Der zu rendernde Pfad
     * @param currentIndex Aktueller Fortschritt auf dem Pfad
     * @param target Das Navigationsziel
     * @param mapCenterX Weltzentrum X
     * @param mapCenterZ Weltzentrum Z
     * @param screenCenterX Bildschirmzentrum X
     * @param screenCenterY Bildschirmzentrum Y
     * @param mapToGui Skalierungsfaktor (Welt -> Bildschirm)
     */
    public void renderAccurate(GuiGraphics graphics, List<BlockPos> path, int currentIndex,
                                NavigationTarget target, int mapCenterX, int mapCenterZ,
                                int screenCenterX, int screenCenterY, float mapToGui) {

        if (path == null || path.isEmpty()) {
            return;
        }

        PoseStack poseStack = graphics.pose();

        // Update Animation
        targetPulse += TARGET_PULSE_SPEED;
        if (targetPulse > 1.0f) targetPulse = 0;

        // Rendere Pfadlinie
        renderPathLineAccurate(poseStack, path, currentIndex, mapCenterX, mapCenterZ,
                screenCenterX, screenCenterY, mapToGui);

        // Rendere Richtungspfeile
        renderArrowsAccurate(poseStack, path, currentIndex, mapCenterX, mapCenterZ,
                screenCenterX, screenCenterY, mapToGui);

        // Rendere Zielmarker
        if (target != null) {
            BlockPos targetPos = target.getCurrentPosition();
            if (targetPos != null) {
                renderTargetMarkerAccurate(poseStack, targetPos, mapCenterX, mapCenterZ,
                        screenCenterX, screenCenterY, mapToGui);
            }
        }
    }

    /**
     * Rendert die Pfadlinie pixelgenau
     */
    private void renderPathLineAccurate(PoseStack poseStack, List<BlockPos> path, int currentIndex,
                                         int mapCenterX, int mapCenterZ,
                                         int screenCenterX, int screenCenterY, float mapToGui) {

        if (path.size() < 2) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float zLevel = 100.0f;

        for (int i = 0; i < path.size() - 1; i++) {
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);

            // Konvertiere Weltkoordinaten zu Bildschirmkoordinaten
            float x1 = screenCenterX + (current.getX() - mapCenterX) * mapToGui;
            float z1 = screenCenterY + (current.getZ() - mapCenterZ) * mapToGui;
            float x2 = screenCenterX + (next.getX() - mapCenterX) * mapToGui;
            float z2 = screenCenterY + (next.getZ() - mapCenterZ) * mapToGui;

            // Farbe basierend auf Fortschritt
            int color;
            if (i < currentIndex) {
                color = PATH_COLOR_PASSED;
            } else {
                float progress = (float) (i - currentIndex) / Math.max(1, path.size() - currentIndex);
                color = interpolateColor(PATH_COLOR_START, PATH_COLOR_END, progress);
            }

            // Berechne Normale für Linienbreite (skaliert mit mapToGui)
            float dx = x2 - x1;
            float dz = z2 - z1;
            float length = (float) Math.sqrt(dx * dx + dz * dz);
            if (length < 0.001f) continue;

            float lineWidth = PATH_WIDTH * Math.max(0.5f, mapToGui * 0.5f);
            float nx = -dz / length * lineWidth;
            float nz = dx / length * lineWidth;

            // Extrahiere RGBA
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = ((color >> 24) & 0xFF) / 255.0f;

            // Zeichne Liniensegment als Quad
            buffer.vertex(matrix, x1 - nx, z1 - nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x1 + nx, z1 + nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x2 + nx, z2 + nz, zLevel).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x2 - nx, z2 - nz, zLevel).color(r, g, b, a).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /**
     * Rendert Richtungspfeile pixelgenau
     */
    private void renderArrowsAccurate(PoseStack poseStack, List<BlockPos> path, int currentIndex,
                                       int mapCenterX, int mapCenterZ,
                                       int screenCenterX, int screenCenterY, float mapToGui) {

        if (path.size() < 2) return;

        for (int i = currentIndex + 1; i < path.size() - 1; i += ARROW_INTERVAL) {
            BlockPos prev = path.get(i - 1);
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);

            // Position in Bildschirmkoordinaten
            float x = screenCenterX + (current.getX() - mapCenterX) * mapToGui;
            float z = screenCenterY + (current.getZ() - mapCenterZ) * mapToGui;

            // Richtung
            float dx = (next.getX() - prev.getX()) / 2.0f;
            float dz = (next.getZ() - prev.getZ()) / 2.0f;
            float angle = (float) Math.toDegrees(Math.atan2(-dx, dz));

            float arrowSize = ARROW_SIZE * Math.max(0.5f, mapToGui * 0.5f);
            renderArrow(poseStack, x, z, angle, arrowSize);
        }
    }

    /**
     * Rendert den Zielmarker pixelgenau
     */
    private void renderTargetMarkerAccurate(PoseStack poseStack, BlockPos targetPos,
                                             int mapCenterX, int mapCenterZ,
                                             int screenCenterX, int screenCenterY, float mapToGui) {

        float x = screenCenterX + (targetPos.getX() - mapCenterX) * mapToGui;
        float z = screenCenterY + (targetPos.getZ() - mapCenterZ) * mapToGui;

        // Pulsierende Animation
        float pulse = (float) (1.0 + 0.3 * Math.sin(targetPulse * Math.PI * 2));
        float markerSize = 8 * pulse * Math.max(0.5f, mapToGui * 0.3f);

        poseStack.pushPose();
        poseStack.translate(x, z, 100);

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float r = ((TARGET_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((TARGET_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (TARGET_COLOR & 0xFF) / 255.0f;

        // Zentrierter Punkt
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, 1.0f).endVertex();

        // Kreis um den Punkt
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            float circleAngle = (float) (i * 2 * Math.PI / segments);
            float px = (float) (Math.cos(circleAngle) * markerSize);
            float pz = (float) (Math.sin(circleAngle) * markerSize);
            buffer.vertex(matrix, px, pz, 0).color(r, g, b, 0.5f).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();

        // Äußerer Ring
        renderRing(poseStack, x, z, markerSize * 1.5f, TARGET_COLOR, 0.3f);
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
