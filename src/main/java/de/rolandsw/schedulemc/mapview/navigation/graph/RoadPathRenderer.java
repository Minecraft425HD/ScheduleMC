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
 * Design: Leuchtende Linie mit Rand
 * - Weißer/heller Kern
 * - Cyan Außenrand
 * - 1 Block dick
 * - Fahnen-Symbol am Ziel
 * - Linie verschwindet sofort hinter dem Spieler
 */
public class RoadPathRenderer {

    // Farben für leuchtende Linie
    private static final int GLOW_CORE_COLOR = 0xFFFFFFFF;    // Weißer Kern
    private static final int GLOW_EDGE_COLOR = 0xFF00DDFF;    // Cyan Rand
    private static final int FLAG_COLOR = 0xFFFF3333;         // Rot für Fahne
    private static final int FLAG_POLE_COLOR = 0xFF8B4513;    // Braun für Fahnenstange

    // Konfiguration
    private static final float LINE_WIDTH_BLOCKS = 1.0f;      // 1 Block breit

    /**
     * Setzt den Fortschritt zurück (bei neuer Navigation)
     */
    public void resetProgress() {
        // Nicht mehr benötigt, aber für API-Kompatibilität beibehalten
    }

    // ═══════════════════════════════════════════════════════════
    // MINIMAP RENDERING
    // ═══════════════════════════════════════════════════════════

    public void renderMinimapAccurate(GuiGraphics graphics, List<BlockPos> path, int currentIndex,
                                       NavigationTarget target, int worldCenterX, int worldCenterZ,
                                       int screenCenterX, int screenCenterY, int mapSize,
                                       float scale, float rotation) {

        if (path == null || path.isEmpty()) {
            return;
        }

        PoseStack poseStack = graphics.pose();

        // Rendere leuchtende Pfadlinie - startet direkt ab currentIndex (sofort verschwindend)
        renderGlowingLineMinimap(poseStack, path, currentIndex, worldCenterX, worldCenterZ,
                screenCenterX, screenCenterY, mapSize, scale, rotation);

        // Rendere Fahnen-Zielmarker
        if (target != null) {
            BlockPos targetPos = target.getCurrentPosition();
            if (targetPos != null) {
                renderFlagMarkerMinimap(poseStack, targetPos, worldCenterX, worldCenterZ,
                        screenCenterX, screenCenterY, mapSize, scale, rotation);
            }
        }
    }

    /**
     * Rendert die leuchtende Linie mit Rand für Minimap
     */
    private void renderGlowingLineMinimap(PoseStack poseStack, List<BlockPos> path, int visibleStart,
                                           int worldCenterX, int worldCenterZ,
                                           int screenCenterX, int screenCenterY, int mapSize,
                                           float scale, float rotation) {

        if (path.size() < 2) return;

        float halfMap = mapSize / 2.0f;
        float lineWidth = LINE_WIDTH_BLOCKS * scale;  // 1 Block in Pixel

        // Zeichne zuerst den äußeren Rand (Cyan)
        renderLineSegments(poseStack, path, visibleStart, worldCenterX, worldCenterZ,
                screenCenterX, screenCenterY, halfMap, scale, rotation,
                lineWidth, GLOW_EDGE_COLOR, 0.9f);

        // Dann den inneren Kern (Weiß) - etwas dünner
        renderLineSegments(poseStack, path, visibleStart, worldCenterX, worldCenterZ,
                screenCenterX, screenCenterY, halfMap, scale, rotation,
                lineWidth * 0.5f, GLOW_CORE_COLOR, 1.0f);
    }

    /**
     * Rendert Liniensegmente
     */
    private void renderLineSegments(PoseStack poseStack, List<BlockPos> path, int startIndex,
                                     int worldCenterX, int worldCenterZ,
                                     int screenCenterX, int screenCenterY, float halfMap,
                                     float scale, float rotation, float lineWidth, int color, float alpha) {

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float zLevel = 100.0f;

        // Extrahiere RGBA
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        for (int i = startIndex; i < path.size() - 1; i++) {
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

            // Berechne Normale für Linienbreite
            float dx = x2 - x1;
            float dz = z2 - z1;
            float length = (float) Math.sqrt(dx * dx + dz * dz);
            if (length < 0.001f) continue;

            float nx = -dz / length * lineWidth / 2;
            float nz = dx / length * lineWidth / 2;

            // Zeichne Liniensegment als Quad
            buffer.vertex(matrix, x1 - nx, z1 - nz, zLevel).color(r, g, b, alpha).endVertex();
            buffer.vertex(matrix, x1 + nx, z1 + nz, zLevel).color(r, g, b, alpha).endVertex();
            buffer.vertex(matrix, x2 + nx, z2 + nz, zLevel).color(r, g, b, alpha).endVertex();
            buffer.vertex(matrix, x2 - nx, z2 - nz, zLevel).color(r, g, b, alpha).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /**
     * Rendert das Fahnen-Symbol am Ziel für Minimap
     */
    private void renderFlagMarkerMinimap(PoseStack poseStack, BlockPos targetPos,
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

        // Prüfe ob außerhalb der Minimap
        boolean outsideMap = Math.abs(relX) > halfMap || Math.abs(relZ) > halfMap;

        if (outsideMap) {
            // Normalisiere auf Rand der Minimap
            float dist = (float) Math.sqrt(relX * relX + relZ * relZ);
            if (dist > 0) {
                float edgeDist = halfMap - 5;
                relX = relX / dist * edgeDist;
                relZ = relZ / dist * edgeDist;
            }
        }

        float x = screenCenterX + relX;
        float z = screenCenterY + relZ;

        float flagScale = outsideMap ? 0.6f : 1.0f;
        renderFlag(poseStack, x, z, 6 * flagScale, 10 * flagScale);
    }

    // ═══════════════════════════════════════════════════════════
    // WORLDMAP RENDERING
    // ═══════════════════════════════════════════════════════════

    public void renderAccurate(GuiGraphics graphics, List<BlockPos> path, int currentIndex,
                                NavigationTarget target, int mapCenterX, int mapCenterZ,
                                int screenCenterX, int screenCenterY, float mapToGui) {

        if (path == null || path.isEmpty()) {
            return;
        }

        PoseStack poseStack = graphics.pose();

        // Rendere leuchtende Pfadlinie - startet direkt ab currentIndex (sofort verschwindend)
        renderGlowingLineWorldmap(poseStack, path, currentIndex, mapCenterX, mapCenterZ,
                screenCenterX, screenCenterY, mapToGui);

        // Rendere Fahnen-Zielmarker
        if (target != null) {
            BlockPos targetPos = target.getCurrentPosition();
            if (targetPos != null) {
                renderFlagMarkerWorldmap(poseStack, targetPos, mapCenterX, mapCenterZ,
                        screenCenterX, screenCenterY, mapToGui);
            }
        }
    }

    /**
     * Rendert die leuchtende Linie mit Rand für Worldmap
     */
    private void renderGlowingLineWorldmap(PoseStack poseStack, List<BlockPos> path, int visibleStart,
                                            int mapCenterX, int mapCenterZ,
                                            int screenCenterX, int screenCenterY, float mapToGui) {

        if (path.size() < 2) return;

        float lineWidth = LINE_WIDTH_BLOCKS * mapToGui;  // 1 Block in Pixel

        // Zeichne zuerst den äußeren Rand (Cyan)
        renderLineSegmentsWorldmap(poseStack, path, visibleStart, mapCenterX, mapCenterZ,
                screenCenterX, screenCenterY, mapToGui, lineWidth, GLOW_EDGE_COLOR, 0.9f);

        // Dann den inneren Kern (Weiß) - etwas dünner
        renderLineSegmentsWorldmap(poseStack, path, visibleStart, mapCenterX, mapCenterZ,
                screenCenterX, screenCenterY, mapToGui, lineWidth * 0.5f, GLOW_CORE_COLOR, 1.0f);
    }

    /**
     * Rendert Liniensegmente für Worldmap
     */
    private void renderLineSegmentsWorldmap(PoseStack poseStack, List<BlockPos> path, int startIndex,
                                             int mapCenterX, int mapCenterZ,
                                             int screenCenterX, int screenCenterY, float mapToGui,
                                             float lineWidth, int color, float alpha) {

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float zLevel = 100.0f;

        // Extrahiere RGBA
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        for (int i = startIndex; i < path.size() - 1; i++) {
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);

            // Konvertiere Weltkoordinaten zu Bildschirmkoordinaten
            float x1 = screenCenterX + (current.getX() - mapCenterX) * mapToGui;
            float z1 = screenCenterY + (current.getZ() - mapCenterZ) * mapToGui;
            float x2 = screenCenterX + (next.getX() - mapCenterX) * mapToGui;
            float z2 = screenCenterY + (next.getZ() - mapCenterZ) * mapToGui;

            // Berechne Normale für Linienbreite
            float dx = x2 - x1;
            float dz = z2 - z1;
            float length = (float) Math.sqrt(dx * dx + dz * dz);
            if (length < 0.001f) continue;

            float nx = -dz / length * lineWidth / 2;
            float nz = dx / length * lineWidth / 2;

            // Zeichne Liniensegment als Quad
            buffer.vertex(matrix, x1 - nx, z1 - nz, zLevel).color(r, g, b, alpha).endVertex();
            buffer.vertex(matrix, x1 + nx, z1 + nz, zLevel).color(r, g, b, alpha).endVertex();
            buffer.vertex(matrix, x2 + nx, z2 + nz, zLevel).color(r, g, b, alpha).endVertex();
            buffer.vertex(matrix, x2 - nx, z2 - nz, zLevel).color(r, g, b, alpha).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /**
     * Rendert das Fahnen-Symbol am Ziel für Worldmap
     */
    private void renderFlagMarkerWorldmap(PoseStack poseStack, BlockPos targetPos,
                                           int mapCenterX, int mapCenterZ,
                                           int screenCenterX, int screenCenterY, float mapToGui) {

        float x = screenCenterX + (targetPos.getX() - mapCenterX) * mapToGui;
        float z = screenCenterY + (targetPos.getZ() - mapCenterZ) * mapToGui;

        float flagScale = Math.max(0.8f, mapToGui * 0.15f);
        renderFlag(poseStack, x, z, 8 * flagScale, 14 * flagScale);
    }

    // ═══════════════════════════════════════════════════════════
    // FLAG RENDERING
    // ═══════════════════════════════════════════════════════════

    /**
     * Rendert eine Fahne
     */
    private void renderFlag(PoseStack poseStack, float x, float y, float width, float height) {
        poseStack.pushPose();
        poseStack.translate(x, y, 100);

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        // Fahnenstange (vertikal nach oben)
        float poleWidth = width * 0.15f;
        float poleHeight = height;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float pr = ((FLAG_POLE_COLOR >> 16) & 0xFF) / 255.0f;
        float pg = ((FLAG_POLE_COLOR >> 8) & 0xFF) / 255.0f;
        float pb = (FLAG_POLE_COLOR & 0xFF) / 255.0f;

        // Stange
        buffer.vertex(matrix, -poleWidth / 2, 0, 0).color(pr, pg, pb, 1.0f).endVertex();
        buffer.vertex(matrix, poleWidth / 2, 0, 0).color(pr, pg, pb, 1.0f).endVertex();
        buffer.vertex(matrix, poleWidth / 2, -poleHeight, 0).color(pr, pg, pb, 1.0f).endVertex();
        buffer.vertex(matrix, -poleWidth / 2, -poleHeight, 0).color(pr, pg, pb, 1.0f).endVertex();

        Tesselator.getInstance().end();

        // Fahne (Dreieck nach rechts)
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        float fr = ((FLAG_COLOR >> 16) & 0xFF) / 255.0f;
        float fg = ((FLAG_COLOR >> 8) & 0xFF) / 255.0f;
        float fb = (FLAG_COLOR & 0xFF) / 255.0f;

        float flagWidth = width;
        float flagHeight = height * 0.5f;

        // Dreieckige Fahne
        buffer.vertex(matrix, poleWidth / 2, -poleHeight, 0).color(fr, fg, fb, 1.0f).endVertex();
        buffer.vertex(matrix, poleWidth / 2, -poleHeight + flagHeight, 0).color(fr, fg, fb, 1.0f).endVertex();
        buffer.vertex(matrix, poleWidth / 2 + flagWidth, -poleHeight + flagHeight / 2, 0).color(fr, fg, fb, 1.0f).endVertex();

        Tesselator.getInstance().end();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    // ═══════════════════════════════════════════════════════════
    // DISTANCE OVERLAY
    // ═══════════════════════════════════════════════════════════

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
    // LEGACY METHODS (für Kompatibilität)
    // ═══════════════════════════════════════════════════════════

    public void render(GuiGraphics graphics, List<BlockPos> path, int currentIndex,
                       NavigationTarget target, int mapCenterX, int mapCenterZ,
                       int mapSize, float zoom, float rotation) {
        // Leitet an neue Methode weiter
        renderMinimapAccurate(graphics, path, currentIndex, target, mapCenterX, mapCenterZ,
                mapSize / 2, mapSize / 2, mapSize, zoom, rotation);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

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
}
