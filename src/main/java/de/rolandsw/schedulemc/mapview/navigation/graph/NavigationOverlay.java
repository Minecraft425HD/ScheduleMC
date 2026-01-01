package de.rolandsw.schedulemc.mapview.navigation.graph;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.service.data.WorldMapData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.util.List;

/**
 * NavigationOverlay - Rendert die Navigation auf der Minimap/Worldmap
 *
 * Integriert sich in den MapViewRenderer und zeigt:
 * - Navigationspfad mit Farbverlauf
 * - Zielmarker mit Animation
 * - Distanzanzeige
 * - Richtungspfeile
 */
public class NavigationOverlay {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static NavigationOverlay instance;

    private final RoadPathRenderer pathRenderer;
    private RoadNavigationService navigationService;
    private boolean initialized = false;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    public static NavigationOverlay getInstance() {
        if (instance == null) {
            instance = new NavigationOverlay();
        }
        return instance;
    }

    private NavigationOverlay() {
        this.pathRenderer = new RoadPathRenderer();
    }

    // ═══════════════════════════════════════════════════════════
    // INITIALISIERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Initialisiert das Overlay mit WorldMapData
     * Sollte aufgerufen werden sobald die Welt geladen ist
     */
    public void initialize(WorldMapData mapData) {
        if (mapData == null) {
            LOGGER.warn("[NavigationOverlay] Cannot initialize with null mapData");
            return;
        }

        this.navigationService = RoadNavigationService.getInstance(mapData);
        this.initialized = true;

        LOGGER.info("[NavigationOverlay] Initialized");
    }

    /**
     * Prüft ob das Overlay initialisiert ist
     */
    public boolean isInitialized() {
        return initialized && navigationService != null;
    }

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION STARTEN/STOPPEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet die Navigation zu einer Position
     */
    public boolean navigateTo(BlockPos position, String name) {
        if (!isInitialized()) {
            LOGGER.warn("[NavigationOverlay] Not initialized, cannot start navigation");
            return false;
        }

        // Reset path progress when starting new navigation
        pathRenderer.resetProgress();

        NavigationTarget target = NavigationTarget.atPosition(position, name);
        return navigationService.startNavigation(target);
    }

    /**
     * Startet die Navigation zu einer Position
     */
    public boolean navigateTo(BlockPos position) {
        return navigateTo(position, "Ziel");
    }

    /**
     * Startet die Navigation zu einem NPC
     */
    public boolean navigateToNPC(java.util.UUID npcUUID, String name) {
        if (!isInitialized()) {
            LOGGER.warn("[NavigationOverlay] Not initialized, cannot start navigation");
            return false;
        }

        // Reset path progress when starting new navigation
        pathRenderer.resetProgress();

        NavigationTarget target = NavigationTarget.forEntity(npcUUID, name);
        return navigationService.startNavigation(target);
    }

    /**
     * Stoppt die Navigation
     */
    public void stopNavigation() {
        if (navigationService != null) {
            navigationService.stopNavigation();
        }
    }

    /**
     * Prüft ob Navigation aktiv ist
     */
    public boolean isNavigating() {
        return navigationService != null && navigationService.isNavigationActive();
    }

    // ═══════════════════════════════════════════════════════════
    // TICK UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Aktualisiert die Navigation (sollte jeden Tick aufgerufen werden)
     */
    public void tick() {
        if (navigationService != null) {
            navigationService.tick();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════

    /**
     * Rendert das Navigations-Overlay auf der Minimap
     *
     * @param graphics GuiGraphics-Kontext
     * @param mapCenterX Kartenzentrum X (Spielerposition)
     * @param mapCenterZ Kartenzentrum Z
     * @param mapSize Kartengröße in Pixeln
     * @param zoom Zoom-Faktor
     * @param rotation Kartenrotation (0 wenn Norden oben ist)
     */
    public void render(GuiGraphics graphics, int mapCenterX, int mapCenterZ,
                       int mapSize, float zoom, float rotation) {

        if (!isInitialized() || !isNavigating()) {
            return;
        }

        List<BlockPos> path = navigationService.getSimplifiedPath();
        int currentIndex = navigationService.getCurrentPathIndex();
        NavigationTarget target = navigationService.getCurrentTarget();

        if (path.isEmpty()) {
            return;
        }

        // Rendere Pfad und Zielmarker
        pathRenderer.render(
                graphics,
                path,
                currentIndex,
                target,
                mapCenterX,
                mapCenterZ,
                mapSize,
                zoom,
                rotation
        );

        // Rendere Distanzanzeige (oben links auf der Karte)
        double distance = navigationService.getRemainingDistance();
        int distanceX = (int) (mapCenterX - mapSize / 2.0 + 5);
        int distanceY = (int) (mapCenterZ - mapSize / 2.0 + 5);
        pathRenderer.renderDistanceOverlay(graphics, distance, distanceX, distanceY);
    }

    /**
     * Rendert das Overlay für die Minimap mit pixelgenauer Positionierung
     *
     * @param graphics GuiGraphics-Kontext
     * @param worldCenterX Weltzentrum X (Spielerposition)
     * @param worldCenterZ Weltzentrum Z
     * @param screenCenterX Bildschirmzentrum X der Minimap
     * @param screenCenterY Bildschirmzentrum Y der Minimap
     * @param mapSize Kartengröße in Pixeln
     * @param scale Pixel pro Block
     * @param rotation Kartenrotation in Grad
     */
    public void renderMinimapAccurate(GuiGraphics graphics, int worldCenterX, int worldCenterZ,
                                       int screenCenterX, int screenCenterY, int mapSize,
                                       float scale, float rotation) {

        if (!isInitialized() || !isNavigating()) {
            return;
        }

        List<BlockPos> path = navigationService.getSimplifiedPath();
        int currentIndex = navigationService.getCurrentPathIndex();
        NavigationTarget target = navigationService.getCurrentTarget();

        if (path.isEmpty()) {
            return;
        }

        pathRenderer.renderMinimapAccurate(
                graphics,
                path,
                currentIndex,
                target,
                worldCenterX,
                worldCenterZ,
                screenCenterX,
                screenCenterY,
                mapSize,
                scale,
                rotation
        );

        // Distanzanzeige neben der Minimap
        double distance = navigationService.getRemainingDistance();
        int distanceX = screenCenterX - mapSize / 2;
        int distanceY = screenCenterY + mapSize / 2 + 5;
        pathRenderer.renderDistanceOverlay(graphics, distance, distanceX, distanceY);
    }

    /**
     * Rendert das Overlay für die Fullscreen-Worldmap
     */
    public void renderFullscreen(GuiGraphics graphics, int mapCenterX, int mapCenterZ,
                                  int screenWidth, int screenHeight, float zoom) {

        if (!isInitialized() || !isNavigating()) {
            return;
        }

        List<BlockPos> path = navigationService.getSimplifiedPath();
        int currentIndex = navigationService.getCurrentPathIndex();
        NavigationTarget target = navigationService.getCurrentTarget();

        if (path.isEmpty()) {
            return;
        }

        // Für Fullscreen ist die Größe der Screen
        int mapSize = Math.min(screenWidth, screenHeight);

        pathRenderer.render(
                graphics,
                path,
                currentIndex,
                target,
                mapCenterX,
                mapCenterZ,
                mapSize,
                zoom,
                0 // Keine Rotation in Fullscreen
        );

        // Distanzanzeige oben links
        double distance = navigationService.getRemainingDistance();
        pathRenderer.renderDistanceOverlay(graphics, distance, 10, 10);
    }

    /**
     * Rendert das Overlay für die Fullscreen-Worldmap mit pixelgenauer Positionierung
     *
     * @param graphics GuiGraphics-Kontext
     * @param mapCenterX Weltzentrum X (Spielerposition)
     * @param mapCenterZ Weltzentrum Z
     * @param screenCenterX Bildschirmzentrum X
     * @param screenCenterY Bildschirmzentrum Y
     * @param mapToGui Skalierungsfaktor (Welt -> Bildschirm)
     */
    public void renderFullscreenAccurate(GuiGraphics graphics, int mapCenterX, int mapCenterZ,
                                          int screenCenterX, int screenCenterY, float mapToGui) {

        if (!isInitialized() || !isNavigating()) {
            return;
        }

        List<BlockPos> path = navigationService.getSimplifiedPath();
        int currentIndex = navigationService.getCurrentPathIndex();
        NavigationTarget target = navigationService.getCurrentTarget();

        if (path.isEmpty()) {
            return;
        }

        pathRenderer.renderAccurate(
                graphics,
                path,
                currentIndex,
                target,
                mapCenterX,
                mapCenterZ,
                screenCenterX,
                screenCenterY,
                mapToGui
        );

        // Distanzanzeige oben links
        double distance = navigationService.getRemainingDistance();
        pathRenderer.renderDistanceOverlay(graphics, distance, 10, 40);
    }

    // ═══════════════════════════════════════════════════════════
    // GRAPH MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Baut den Straßen-Graphen für den aktuellen Bereich auf
     */
    public void buildGraph() {
        if (navigationService != null) {
            navigationService.buildGraph();
        }
    }

    /**
     * Gibt Statistiken über den Graphen zurück
     */
    public String getGraphStats() {
        if (navigationService != null && navigationService.hasGraph()) {
            return navigationService.getGraph().getStatistics();
        }
        return "Kein Graph geladen";
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER
    // ═══════════════════════════════════════════════════════════

    public RoadNavigationService getNavigationService() {
        return navigationService;
    }

    public NavigationTarget getCurrentTarget() {
        return navigationService != null ? navigationService.getCurrentTarget() : null;
    }

    public double getRemainingDistance() {
        return navigationService != null ? navigationService.getRemainingDistance() : 0;
    }

    /**
     * Gibt die Richtung zum nächsten Wegpunkt zurück
     */
    public float getDirectionToNextWaypoint() {
        return navigationService != null ? navigationService.getDirectionToNextWaypoint() : 0;
    }

    // ═══════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt Ressourcen frei
     */
    public void shutdown() {
        if (navigationService != null) {
            navigationService.shutdown();
        }
        initialized = false;
        instance = null;
    }
}
