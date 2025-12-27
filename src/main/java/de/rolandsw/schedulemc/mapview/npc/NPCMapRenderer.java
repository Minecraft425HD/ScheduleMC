package de.rolandsw.schedulemc.mapview.npc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * NPCMapRenderer - Rendert NPCs auf der Minimap und Worldmap
 *
 * Filterregeln:
 * - Polizei-NPCs werden NICHT angezeigt
 * - NPCs auf der Arbeit werden NICHT angezeigt
 * - NPCs zuhause werden NICHT angezeigt
 * - Alle anderen NPCs werden als Marker angezeigt
 */
public class NPCMapRenderer {

    // Marker-Farben nach NPC-Typ
    private static final int COLOR_BEWOHNER = 0xFF4CAF50;    // Grün für Bewohner
    private static final int COLOR_VERKAEUFER = 0xFFFF9800;  // Orange für Verkäufer
    private static final int COLOR_DEFAULT = 0xFF2196F3;     // Blau als Default

    // Marker-Größe
    private static final float MARKER_SIZE = 4.0f;
    private static final float MARKER_BORDER = 1.0f;

    // Animation
    private float pulseAnimation = 0;
    private static final float PULSE_SPEED = 0.05f;

    /**
     * Rendert alle sichtbaren NPCs auf der Minimap
     *
     * @param graphics GuiGraphics-Kontext
     * @param centerX Kartenzentrum X (Spieler-Position)
     * @param centerZ Kartenzentrum Z
     * @param mapSize Kartengröße in Pixeln
     * @param zoom Zoom-Faktor (Blöcke pro Pixel)
     * @param mapX Karten-Position X auf Screen
     * @param mapY Karten-Position Y auf Screen
     * @param rotation Kartenrotation in Grad
     */
    public void renderOnMinimap(GuiGraphics graphics, int centerX, int centerZ,
                                 int mapSize, float zoom, int mapX, int mapY, float rotation) {
        List<CustomNPCEntity> visibleNPCs = getVisibleNPCs(centerX, centerZ, (int)(mapSize * zoom / 2));

        if (visibleNPCs.isEmpty()) {
            return;
        }

        // Update Animation
        pulseAnimation += PULSE_SPEED;
        if (pulseAnimation > Math.PI * 2) {
            pulseAnimation = 0;
        }

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // Verschiebe zum Kartenzentrum
        poseStack.translate(mapX, mapY, 0);

        // Rotation anwenden (falls Karte rotiert)
        if (rotation != 0) {
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-rotation));
        }

        // Rendere jeden NPC
        for (CustomNPCEntity npc : visibleNPCs) {
            renderNPCMarker(poseStack, npc, centerX, centerZ, mapSize, zoom);
        }

        poseStack.popPose();
    }

    /**
     * Rendert alle sichtbaren NPCs auf der Worldmap
     *
     * @param graphics GuiGraphics-Kontext
     * @param centerX Kartenzentrum X
     * @param centerZ Kartenzentrum Z
     * @param screenWidth Bildschirmbreite
     * @param screenHeight Bildschirmhöhe
     * @param zoom Zoom-Faktor
     * @param offsetX Karten-Offset X
     * @param offsetY Karten-Offset Y
     */
    public void renderOnWorldmap(GuiGraphics graphics, int centerX, int centerZ,
                                  int screenWidth, int screenHeight, float zoom,
                                  int offsetX, int offsetY) {
        // Größerer Suchradius für Worldmap
        int searchRadius = (int)(Math.max(screenWidth, screenHeight) * zoom);
        List<CustomNPCEntity> visibleNPCs = getVisibleNPCs(centerX, centerZ, searchRadius);

        if (visibleNPCs.isEmpty()) {
            return;
        }

        pulseAnimation += PULSE_SPEED;
        if (pulseAnimation > Math.PI * 2) {
            pulseAnimation = 0;
        }

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        int mapSize = Math.min(screenWidth, screenHeight);

        for (CustomNPCEntity npc : visibleNPCs) {
            // Berechne Position auf Screen
            float relX = (npc.getBlockX() - centerX) / zoom;
            float relZ = (npc.getBlockZ() - centerZ) / zoom;

            float screenX = screenWidth / 2.0f + relX + offsetX;
            float screenY = screenHeight / 2.0f + relZ + offsetY;

            // Prüfe ob auf Screen sichtbar
            if (screenX >= 0 && screenX <= screenWidth && screenY >= 0 && screenY <= screenHeight) {
                renderNPCMarkerAt(poseStack, npc, screenX, screenY);
            }
        }

        poseStack.popPose();
    }

    /**
     * Rendert einen einzelnen NPC-Marker
     */
    private void renderNPCMarker(PoseStack poseStack, CustomNPCEntity npc,
                                  int centerX, int centerZ, int mapSize, float zoom) {
        // Berechne relative Position
        float relX = (npc.getBlockX() - centerX) / zoom;
        float relZ = (npc.getBlockZ() - centerZ) / zoom;

        // Prüfe ob innerhalb der Karte
        float halfSize = mapSize / 2.0f;
        if (Math.abs(relX) > halfSize || Math.abs(relZ) > halfSize) {
            return;
        }

        renderNPCMarkerAt(poseStack, npc, relX, relZ);
    }

    /**
     * Rendert einen NPC-Marker an einer bestimmten Position
     */
    private void renderNPCMarkerAt(PoseStack poseStack, CustomNPCEntity npc, float x, float y) {
        int color = getColorForNPC(npc);

        // Pulsierender Effekt
        float pulse = 1.0f + 0.2f * (float) Math.sin(pulseAnimation);
        float size = MARKER_SIZE * pulse;

        poseStack.pushPose();
        poseStack.translate(x, y, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Äußerer Rand (schwarz)
        drawCircle(matrix, 0, 0, size + MARKER_BORDER, 0xFF000000, 0.8f);

        // Innerer Kreis (Farbe nach Typ)
        drawCircle(matrix, 0, 0, size, color, 1.0f);

        // Highlight in der Mitte
        drawCircle(matrix, 0, 0, size * 0.4f, 0xFFFFFFFF, 0.5f);

        RenderSystem.disableBlend();

        poseStack.popPose();

        // Name-Tooltip (optional - bei Hover)
        // Wird später implementiert wenn gewünscht
    }

    /**
     * Zeichnet einen gefüllten Kreis
     */
    private void drawCircle(Matrix4f matrix, float x, float y, float radius, int color, float alpha) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Zentrum
        buffer.addVertex(matrix, x, y, 0).setColor(r, g, b, alpha);

        // Kreispunkte
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float px = x + (float) Math.cos(angle) * radius;
            float py = y + (float) Math.sin(angle) * radius;
            buffer.addVertex(matrix, px, py, 0).setColor(r, g, b, alpha * 0.8f);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /**
     * Gibt die Farbe für einen NPC basierend auf seinem Typ zurück
     */
    private int getColorForNPC(CustomNPCEntity npc) {
        NPCType type = npc.getNpcType();

        switch (type) {
            case BEWOHNER:
                return COLOR_BEWOHNER;
            case VERKAEUFER:
                return COLOR_VERKAEUFER;
            case POLIZEI:
                // Sollte nie erreicht werden (gefiltert)
                return 0xFF0000FF;
            default:
                return COLOR_DEFAULT;
        }
    }

    /**
     * Findet alle sichtbaren NPCs in einem Radius
     * Filtert Polizei und NPCs auf Arbeit/Zuhause
     */
    private List<CustomNPCEntity> getVisibleNPCs(int centerX, int centerZ, int radius) {
        List<CustomNPCEntity> result = new ArrayList<>();

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return result;
        }

        // Suche in AABB
        AABB searchBox = new AABB(
                centerX - radius, -64, centerZ - radius,
                centerX + radius, 320, centerZ + radius
        );

        for (Entity entity : level.getEntities(null, searchBox, e -> e instanceof CustomNPCEntity)) {
            CustomNPCEntity npc = (CustomNPCEntity) entity;

            // Filter: Keine Polizei-NPCs
            if (npc.getNpcType() == NPCType.POLIZEI) {
                continue;
            }

            // Filter: NPCs auf Arbeit oder Zuhause
            NPCActivityStatus status = npc.getActivityStatus();
            if (!status.isVisibleOnMap()) {
                continue;
            }

            result.add(npc);
        }

        return result;
    }

    /**
     * Rendert NPC-Namen bei Hover (für Worldmap)
     */
    public void renderNPCTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                                  int centerX, int centerZ, float zoom,
                                  int screenWidth, int screenHeight) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        // Berechne Weltposition unter Maus
        float worldX = centerX + (mouseX - screenWidth / 2.0f) * zoom;
        float worldZ = centerZ + (mouseY - screenHeight / 2.0f) * zoom;

        // Suche NPC in der Nähe
        int searchRadius = (int)(Math.max(screenWidth, screenHeight) * zoom);
        AABB searchBox = new AABB(
                worldX - 3, -64, worldZ - 3,
                worldX + 3, 320, worldZ + 3
        );

        for (Entity entity : level.getEntities(null, searchBox, e -> e instanceof CustomNPCEntity)) {
            CustomNPCEntity npc = (CustomNPCEntity) entity;

            if (npc.getNpcType() == NPCType.POLIZEI) {
                continue;
            }

            NPCActivityStatus status = npc.getActivityStatus();
            if (!status.isVisibleOnMap()) {
                continue;
            }

            // Zeige Tooltip
            String name = npc.getNpcName();
            String typeStr = npc.getNpcType().getDisplayName();

            graphics.renderTooltip(
                    Minecraft.getInstance().font,
                    net.minecraft.network.chat.Component.literal(name + " (" + typeStr + ")"),
                    mouseX, mouseY
            );

            break; // Nur ersten NPC anzeigen
        }
    }
}
