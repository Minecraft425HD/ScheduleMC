package de.rolandsw.schedulemc.mapview.presentation.renderer;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.navigation.graph.NavigationOverlay;
import de.rolandsw.schedulemc.mapview.npc.NPCMapRenderer;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Renders overlays (NPC markers and navigation path) on minimap and fullscreen map.
 * Extracted from {@link MapViewRenderer} to separate overlay rendering concerns.
 */
public class MapOverlayRenderer {

    private final MapViewConfiguration options;
    private final NPCMapRenderer npcMapRenderer = new NPCMapRenderer();

    public MapOverlayRenderer(MapViewConfiguration options) {
        this.options = options;
    }

    /**
     * Renders the navigation path overlay.
     *
     * @param direction   current player yaw (degrees)
     * @param scWidth     scaled screen width (used for fullscreen center)
     * @param scHeight    scaled screen height (used for fullscreen center)
     * @param lastX       last known player X
     * @param lastZ       last known player Z
     */
    public void renderNavigationOverlay(GuiGraphics graphics, int mapX, int mapY,
                                        int mapSize, float zoom, boolean fullscreen, float scaleProj,
                                        float direction, int scWidth, int scHeight, int lastX, int lastZ) {
        NavigationOverlay overlay = NavigationOverlay.getInstance();

        if (!overlay.isInitialized()) {
            var mapData = MapViewConstants.getLightMapInstance().getWorldMapData();
            if (mapData != null) {
                overlay.initialize(mapData);
            }
        }

        // Tick für Updates (Position, Pfad-Neuberechnung) - IMMER aufrufen, auch wenn nicht navigiert wird
        // damit der Pfad gelöscht werden kann wenn Navigation beendet wurde
        if (overlay.isInitialized()) {
            overlay.tick();
        }

        if (!overlay.isInitialized() || !overlay.isNavigating()) {
            return;
        }

        float rotation = 0;
        if (this.options.rotates && !fullscreen) {
            rotation = direction;
        }

        graphics.pose().pushPose();
        graphics.pose().scale(scaleProj, scaleProj, 1.0f);

        if (fullscreen) {
            int screenCenterX = scWidth / 2;
            int screenCenterY = scHeight / 2;
            overlay.renderFullscreenAccurate(graphics, lastX, lastZ, screenCenterX, screenCenterY, zoom);
        } else {
            int halfSize = mapSize / 2;
            int scissorX1 = (int) ((mapX - halfSize) * scaleProj);
            int scissorY1 = (int) ((mapY - halfSize) * scaleProj);
            int scissorX2 = (int) ((mapX + halfSize) * scaleProj);
            int scissorY2 = (int) ((mapY + halfSize) * scaleProj);

            graphics.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
            float scale = zoom;
            overlay.renderMinimapAccurate(graphics, lastX, lastZ, mapX, mapY, mapSize, scale, rotation);
            graphics.disableScissor();
        }

        graphics.pose().popPose();
    }

    /**
     * Renders NPC markers on the map.
     *
     * @param direction current player yaw (degrees)
     * @param scWidth   scaled screen width
     * @param scHeight  scaled screen height
     * @param lastX     last known player X
     * @param lastZ     last known player Z
     */
    public void renderNPCMarkers(GuiGraphics graphics, int mapX, int mapY,
                                  int mapSize, float zoom, boolean fullscreen,
                                  float direction, int scWidth, int scHeight, int lastX, int lastZ) {
        float rotation = 0;
        if (this.options.rotates && !fullscreen) {
            rotation = direction;
        }

        if (fullscreen) {
            npcMapRenderer.renderOnWorldmap(graphics, lastX, lastZ, scWidth, scHeight, zoom, 0, 0);
        } else {
            npcMapRenderer.renderOnMinimap(graphics, lastX, lastZ, mapSize, zoom, mapX, mapY, rotation);
        }
    }
}
