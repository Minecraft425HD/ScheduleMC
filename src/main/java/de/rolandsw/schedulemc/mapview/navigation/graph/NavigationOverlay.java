package de.rolandsw.schedulemc.mapview.navigation.graph;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.mapview.service.data.WorldMapData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.util.List;

/**
 * NavigationOverlay - Verwaltet die Navigation und aktualisiert das Pfad-Overlay
 *
 * Der Pfad wird jetzt direkt in die Kartenfarben integriert (via NavigationPathOverlay),
 * nicht mehr als separates Overlay gerendert. Dadurch bewegt sich der Pfad perfekt mit der Karte.
 */
public class NavigationOverlay {

    private static final Logger LOGGER = LogUtils.getLogger();

    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile NavigationOverlay instance;

    private final RoadPathRenderer pathRenderer;
    private RoadNavigationService navigationService;
    private boolean initialized = false;

    // Letzter bekannter Pfad-Index für Change-Detection
    private int lastPathIndex = -1;
    private int lastPathSize = 0;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static NavigationOverlay getInstance() {
        NavigationOverlay localRef = instance;
        if (localRef == null) {
            synchronized (NavigationOverlay.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new NavigationOverlay();
                }
            }
        }
        return localRef;
    }

    private NavigationOverlay() {
        this.pathRenderer = new RoadPathRenderer();
    }

    // ═══════════════════════════════════════════════════════════
    // INITIALISIERUNG
    // ═══════════════════════════════════════════════════════════

    public void initialize(WorldMapData mapData) {
        if (mapData == null) {
            LOGGER.warn("[NavigationOverlay] Cannot initialize with null mapData");
            return;
        }

        this.navigationService = RoadNavigationService.getInstance(mapData);
        this.initialized = true;

        LOGGER.info("[NavigationOverlay] Initialized");
    }

    public boolean isInitialized() {
        return initialized && navigationService != null;
    }

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION STARTEN/STOPPEN
    // ═══════════════════════════════════════════════════════════

    public boolean navigateTo(BlockPos position, String name) {
        if (!isInitialized()) {
            LOGGER.warn("[NavigationOverlay] Not initialized, cannot start navigation");
            return false;
        }

        pathRenderer.resetProgress();
        NavigationTarget target = NavigationTarget.atPosition(position, name);
        boolean result = navigationService.startNavigation(target);

        if (result) {
            // Pfad-Overlay aktualisieren
            updatePathOverlay();
        }

        return result;
    }

    public boolean navigateTo(BlockPos position) {
        return navigateTo(position, "Ziel");
    }

    public boolean navigateToNPC(java.util.UUID npcUUID, String name) {
        if (!isInitialized()) {
            LOGGER.warn("[NavigationOverlay] Not initialized, cannot start navigation");
            return false;
        }

        pathRenderer.resetProgress();
        NavigationTarget target = NavigationTarget.forEntity(npcUUID, name);
        boolean result = navigationService.startNavigation(target);

        if (result) {
            updatePathOverlay();
        }

        return result;
    }

    public void stopNavigation() {
        if (navigationService != null) {
            navigationService.stopNavigation();
        }

        // Pfad-Overlay leeren
        NavigationPathOverlay.getInstance().clearPath();
        lastPathIndex = -1;
        lastPathSize = 0;
    }

    public boolean isNavigating() {
        return navigationService != null && navigationService.isNavigationActive();
    }

    // ═══════════════════════════════════════════════════════════
    // TICK UPDATE
    // ═══════════════════════════════════════════════════════════

    public void tick() {
        if (navigationService != null) {
            navigationService.tick();

            // Prüfe ob sich der Pfad-Fortschritt geändert hat
            if (isNavigating()) {
                int currentIndex = navigationService.getCurrentFullPathIndex();
                List<BlockPos> path = navigationService.getCurrentPath();

                // Wenn sich Index oder Pfadgröße geändert hat -> Overlay aktualisieren
                if (currentIndex != lastPathIndex || path.size() != lastPathSize) {
                    updatePathOverlay();
                    lastPathIndex = currentIndex;
                    lastPathSize = path.size();
                }
            } else if (lastPathSize > 0) {
                // Navigation wurde beendet (Ziel erreicht) - Pfad komplett löschen
                NavigationPathOverlay.getInstance().clearPath();
                lastPathIndex = -1;
                lastPathSize = 0;
            }
        }
    }

    /**
     * Aktualisiert das NavigationPathOverlay mit dem aktuellen Pfad
     */
    private void updatePathOverlay() {
        if (!isNavigating()) {
            NavigationPathOverlay.getInstance().clearPath();
            return;
        }

        List<BlockPos> path = navigationService.getCurrentPath();
        int currentIndex = navigationService.getCurrentFullPathIndex();

        NavigationPathOverlay.getInstance().setPath(path, currentIndex);
    }

    // ═══════════════════════════════════════════════════════════
    // RENDERING - Nur noch für Zielmarker und Distanzanzeige
    // Der Pfad wird jetzt direkt in der Karte gerendert
    // ═══════════════════════════════════════════════════════════

    /**
     * Rendert nur den Zielmarker und die Distanzanzeige (Pfad ist in der Karte)
     */
    public void renderMinimapAccurate(GuiGraphics graphics, int worldCenterX, int worldCenterZ,
                                       int screenCenterX, int screenCenterY, int mapSize,
                                       float scale, float rotation) {

        if (!isInitialized() || !isNavigating()) {
            return;
        }

        NavigationTarget target = navigationService.getCurrentTarget();
        if (target == null) {
            return;
        }

        // Nur Zielmarker rendern (Pfad ist in der Karte integriert)
        BlockPos targetPos = target.getCurrentPosition();
        if (targetPos != null) {
            pathRenderer.renderFlagMarkerMinimapPublic(graphics.pose(), targetPos, worldCenterX, worldCenterZ,
                    screenCenterX, screenCenterY, mapSize, scale, rotation);
        }

        // Distanzanzeige
        double distance = navigationService.getRemainingDistance();
        int distanceX = screenCenterX - mapSize / 2;
        int distanceY = screenCenterY + mapSize / 2 + 5;
        pathRenderer.renderDistanceOverlay(graphics, distance, distanceX, distanceY);
    }

    /**
     * Rendert nur den Zielmarker und die Distanzanzeige für Worldmap
     */
    public void renderFullscreenAccurate(GuiGraphics graphics, int mapCenterX, int mapCenterZ,
                                          int screenCenterX, int screenCenterY, float mapToGui) {

        if (!isInitialized() || !isNavigating()) {
            return;
        }

        NavigationTarget target = navigationService.getCurrentTarget();
        if (target == null) {
            return;
        }

        // Nur Zielmarker rendern
        BlockPos targetPos = target.getCurrentPosition();
        if (targetPos != null) {
            pathRenderer.renderFlagMarkerWorldmapPublic(graphics.pose(), targetPos, mapCenterX, mapCenterZ,
                    screenCenterX, screenCenterY, mapToGui);
        }

        // Distanzanzeige
        double distance = navigationService.getRemainingDistance();
        pathRenderer.renderDistanceOverlay(graphics, distance, 10, 40);
    }

    // Legacy-Methoden für Kompatibilität
    public void render(GuiGraphics graphics, int mapCenterX, int mapCenterZ,
                       int mapSize, float zoom, float rotation) {
        // Nicht mehr benötigt - Pfad ist in Karte integriert
    }

    public void renderFullscreen(GuiGraphics graphics, int mapCenterX, int mapCenterZ,
                                  int screenWidth, int screenHeight, float zoom) {
        // Nicht mehr benötigt - Pfad ist in Karte integriert
    }

    // ═══════════════════════════════════════════════════════════
    // GRAPH MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    public void buildGraph() {
        if (navigationService != null) {
            navigationService.buildGraph();
        }
    }

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

    public float getDirectionToNextWaypoint() {
        return navigationService != null ? navigationService.getDirectionToNextWaypoint() : 0;
    }

    // ═══════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════

    public void shutdown() {
        NavigationPathOverlay.getInstance().clearPath();

        if (navigationService != null) {
            navigationService.shutdown();
        }
        initialized = false;
        instance = null;
    }
}
