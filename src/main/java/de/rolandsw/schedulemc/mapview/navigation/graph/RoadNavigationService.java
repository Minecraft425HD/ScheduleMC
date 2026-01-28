package de.rolandsw.schedulemc.mapview.navigation.graph;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.mapview.service.data.WorldMapData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * RoadNavigationService - Koordiniert die Straßen-Navigation
 *
 * Hauptfunktionen:
 * - Verwaltet den Straßen-Graphen
 * - Berechnet Pfade zu Zielen
 * - Aktualisiert Pfade für bewegliche Ziele
 * - Bietet API für UI-Integration
 */
public class RoadNavigationService {

    private static final Logger LOGGER = LogUtils.getLogger();

    // SICHERHEIT: volatile + Lock für Thread-safe Singleton
    private static volatile RoadNavigationService instance;
    private static final Object INSTANCE_LOCK = new Object();

    // Konfiguration
    private static final int DEFAULT_SCAN_RADIUS = 150; // Reduziert für bessere Performance
    private static final int PATH_UPDATE_INTERVAL_MS = 1000; // Pfad-Update jede Sekunde
    private static final double MOVEMENT_THRESHOLD = 10.0; // Mindestbewegung für Neuberechnung
    private static final double ARRIVAL_DISTANCE = 5.0; // Distanz für "angekommen"
    private static final double PATH_DEVIATION_THRESHOLD = 15.0; // Pfad-Neuberechnung wenn zu weit vom Pfad

    // Services
    private final WorldMapData mapData;

    // Zustand
    private RoadGraph currentGraph;
    private NavigationTarget currentTarget;
    private List<BlockPos> currentPath;
    private List<BlockPos> simplifiedPath;
    private boolean isNavigationActive;
    private long lastPathUpdate;
    private int currentPathIndex;           // Index auf simplifiedPath
    private int currentFullPathIndex;       // Index auf currentPath (für Rendering)
    private BlockPos lastPathStartPos;      // Letzte Position von der aus der Pfad berechnet wurde

    // SICHERHEIT: CopyOnWriteArrayList für Thread-safe Listener-Operationen
    private final List<NavigationListener> listeners = new CopyOnWriteArrayList<>();

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    /**
     * SICHERHEIT: Thread-safe Singleton mit Double-Checked Locking
     */
    public static RoadNavigationService getInstance(WorldMapData mapData) {
        RoadNavigationService result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new RoadNavigationService(mapData);
                }
            }
        }
        return result;
    }

    public static RoadNavigationService getInstance() {
        return instance;
    }

    private RoadNavigationService(WorldMapData mapData) {
        this.mapData = mapData;
        this.currentPath = Collections.emptyList();
        this.simplifiedPath = Collections.emptyList();
        this.isNavigationActive = false;

        // Initialisiere RoadBlockDetector
        RoadBlockDetector.initialize();

        LOGGER.info("[RoadNavigationService] Initialized");
    }

    // ═══════════════════════════════════════════════════════════
    // GRAPH-MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Baut den Straßen-Graphen für den aktuellen Bereich auf
     */
    public CompletableFuture<RoadGraph> buildGraph() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }

        return buildGraph(player.getBlockX(), player.getBlockZ(), DEFAULT_SCAN_RADIUS);
    }

    /**
     * Baut den Straßen-Graphen für einen bestimmten Bereich auf
     */
    public CompletableFuture<RoadGraph> buildGraph(int centerX, int centerZ, int radius) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RoadGraphBuilder builder = new RoadGraphBuilder(mapData);
                currentGraph = builder.buildGraph(centerX, centerZ, radius);

                notifyListeners(NavigationEvent.GRAPH_UPDATED);
                LOGGER.info("[RoadNavigationService] Graph built: {}", currentGraph);

                return currentGraph;
            } catch (Exception e) {
                LOGGER.error("[RoadNavigationService] Error building graph", e);
                return null;
            }
        }, executor);
    }

    /**
     * Gibt den aktuellen Graphen zurück
     */
    public RoadGraph getGraph() {
        return currentGraph;
    }

    /**
     * Prüft ob ein Graph verfügbar ist
     */
    public boolean hasGraph() {
        return currentGraph != null && !currentGraph.isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet die Navigation zu einem Ziel
     *
     * @param target Das Navigationsziel
     * @return true wenn Navigation gestartet wurde
     */
    public boolean startNavigation(NavigationTarget target) {
        if (target == null || !target.isValid()) {
            LOGGER.warn("[RoadNavigationService] Invalid navigation target");
            return false;
        }

        // Graph bauen falls nicht vorhanden
        if (!hasGraph()) {
            LOGGER.info("[RoadNavigationService] Building graph for navigation...");
            buildGraph().thenAccept(graph -> {
                if (graph != null) {
                    startNavigationInternal(target);
                } else {
                    notifyListeners(NavigationEvent.PATH_NOT_FOUND);
                }
            });
            return true;
        }

        return startNavigationInternal(target);
    }

    private boolean startNavigationInternal(NavigationTarget target) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            LOGGER.warn("[RoadNavigationService] Player is null, cannot start navigation");
            return false;
        }

        BlockPos start = player.blockPosition();
        BlockPos end = target.getCurrentPosition();

        LOGGER.info("[RoadNavigationService] Starting navigation from {} to {}", start, end);

        if (end == null) {
            LOGGER.warn("[RoadNavigationService] Target position is null");
            notifyListeners(NavigationEvent.PATH_NOT_FOUND);
            return false;
        }

        // Prüfe Graph-Status
        if (currentGraph == null) {
            LOGGER.error("[RoadNavigationService] Current graph is null!");
            notifyListeners(NavigationEvent.PATH_NOT_FOUND);
            return false;
        }

        LOGGER.info("[RoadNavigationService] Graph has {} nodes, {} segments",
                currentGraph.getNodeCount(), currentGraph.getSegmentCount());

        // Berechne Pfad
        List<BlockPos> path = currentGraph.findPath(start, end);

        if (path.isEmpty()) {
            LOGGER.warn("[RoadNavigationService] No path found to {}", target);
            notifyListeners(NavigationEvent.PATH_NOT_FOUND);
            return false;
        }

        // Navigation starten
        currentTarget = target;
        currentPath = path;
        simplifiedPath = RoadGraph.simplifyPath(path);
        currentPathIndex = 0;
        currentFullPathIndex = 0;
        isNavigationActive = true;
        lastPathUpdate = System.currentTimeMillis();
        lastPathStartPos = start; // Merke Startposition für Pfad-Neuberechnung

        LOGGER.info("[RoadNavigationService] Navigation started to {}, {} waypoints",
                target.getDisplayName(), simplifiedPath.size());

        notifyListeners(NavigationEvent.NAVIGATION_STARTED);
        return true;
    }

    /**
     * Stoppt die aktuelle Navigation
     */
    public void stopNavigation() {
        if (!isNavigationActive) {
            return;
        }

        isNavigationActive = false;
        currentTarget = null;
        currentPath = Collections.emptyList();
        simplifiedPath = Collections.emptyList();
        currentPathIndex = 0;
        currentFullPathIndex = 0;
        lastPathStartPos = null;

        LOGGER.info("[RoadNavigationService] Navigation stopped");
        notifyListeners(NavigationEvent.NAVIGATION_STOPPED);
    }

    /**
     * Aktualisiert die Navigation (sollte jeden Tick aufgerufen werden)
     */
    public void tick() {
        if (!isNavigationActive || currentTarget == null) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        BlockPos playerPos = player.blockPosition();

        // Prüfe ob Ziel erreicht
        if (currentTarget.isNear(playerPos, ARRIVAL_DISTANCE)) {
            LOGGER.info("[RoadNavigationService] Destination reached: {}", currentTarget.getDisplayName());
            notifyListeners(NavigationEvent.DESTINATION_REACHED);
            stopNavigation();
            return;
        }

        // Pfad aktualisieren wenn:
        // 1. Spieler sich signifikant von der letzten Pfad-Startposition entfernt hat
        // 2. Oder für bewegliche Ziele wenn das Ziel sich bewegt hat
        long now = System.currentTimeMillis();
        if (now - lastPathUpdate >= PATH_UPDATE_INTERVAL_MS) {
            boolean shouldRecalculate = false;

            // Prüfe ob Spieler sich weit von der letzten Pfad-Startposition entfernt hat
            if (lastPathStartPos != null) {
                double distFromStart = distance(playerPos, lastPathStartPos);
                if (distFromStart >= MOVEMENT_THRESHOLD) {
                    shouldRecalculate = true;
                    LOGGER.debug("[RoadNavigationService] Player moved {} blocks from path start, recalculating", distFromStart);
                }
            }

            // Für bewegliche Ziele: Auch prüfen ob Ziel sich bewegt hat
            if (currentTarget.isMoving() && currentTarget.hasMovedSignificantly(MOVEMENT_THRESHOLD)) {
                shouldRecalculate = true;
            }

            if (shouldRecalculate) {
                recalculatePath(playerPos);
            }
            lastPathUpdate = now;
        }

        // Aktualisiere aktuellen Pfad-Index basierend auf Spielerposition
        updatePathProgress(playerPos);
    }

    /**
     * Berechnet den Pfad neu
     */
    private void recalculatePath(BlockPos playerPos) {
        if (currentTarget == null || currentGraph == null) {
            return;
        }

        BlockPos end = currentTarget.getCurrentPosition();
        if (end == null) {
            return;
        }

        List<BlockPos> newPath = currentGraph.findPath(playerPos, end);
        if (!newPath.isEmpty()) {
            currentPath = newPath;
            simplifiedPath = RoadGraph.simplifyPath(newPath);
            currentPathIndex = 0;
            currentFullPathIndex = 0;
            lastPathStartPos = playerPos; // Merke von wo der Pfad berechnet wurde

            LOGGER.debug("[RoadNavigationService] Path recalculated from {}, {} waypoints", playerPos, simplifiedPath.size());
            notifyListeners(NavigationEvent.PATH_UPDATED);
        }
    }

    /**
     * Aktualisiert den Fortschritt auf dem Pfad
     */
    private void updatePathProgress(BlockPos playerPos) {
        // Update für den vollen Pfad (für Rendering)
        if (!currentPath.isEmpty()) {
            double minDist = Double.MAX_VALUE;
            int nearestFullIndex = currentFullPathIndex;

            // Suche den nächsten Punkt auf dem vollen Pfad
            for (int i = currentFullPathIndex; i < currentPath.size(); i++) {
                BlockPos pathPoint = currentPath.get(i);
                double dist = distance(playerPos, pathPoint);

                if (dist < minDist) {
                    minDist = dist;
                    nearestFullIndex = i;
                }
            }

            // Sofort zum nächsten Punkt wechseln wenn nah genug (2 Blöcke)
            if (minDist < 2 && nearestFullIndex < currentPath.size() - 1) {
                currentFullPathIndex = nearestFullIndex + 1;
            } else if (nearestFullIndex > currentFullPathIndex) {
                // Spieler hat Punkte übersprungen
                currentFullPathIndex = nearestFullIndex;
            }
        }

        // Update für simplified path (für Wegpunkt-Navigation)
        if (!simplifiedPath.isEmpty()) {
            double minDist = Double.MAX_VALUE;
            int nearestIndex = currentPathIndex;

            for (int i = currentPathIndex; i < simplifiedPath.size(); i++) {
                BlockPos pathPoint = simplifiedPath.get(i);
                double dist = distance(playerPos, pathPoint);

                if (dist < minDist) {
                    minDist = dist;
                    nearestIndex = i;
                }
            }

            if (minDist < 5 && nearestIndex < simplifiedPath.size() - 1) {
                currentPathIndex = nearestIndex + 1;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER FÜR UI
    // ═══════════════════════════════════════════════════════════

    public boolean isNavigationActive() {
        return isNavigationActive;
    }

    public NavigationTarget getCurrentTarget() {
        return currentTarget;
    }

    public List<BlockPos> getCurrentPath() {
        return Collections.unmodifiableList(currentPath);
    }

    public List<BlockPos> getSimplifiedPath() {
        return Collections.unmodifiableList(simplifiedPath);
    }

    public int getCurrentPathIndex() {
        return currentPathIndex;
    }

    /**
     * Gibt den aktuellen Index auf dem vollen Pfad zurück (für Rendering)
     */
    public int getCurrentFullPathIndex() {
        return currentFullPathIndex;
    }

    /**
     * Gibt die verbleibende Distanz zum Ziel zurück
     */
    public double getRemainingDistance() {
        if (!isNavigationActive || currentTarget == null) {
            return 0;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }

        return currentTarget.distanceTo(player.blockPosition());
    }

    /**
     * Gibt den nächsten Wegpunkt auf dem Pfad zurück
     */
    public BlockPos getNextWaypoint() {
        if (simplifiedPath.isEmpty() || currentPathIndex >= simplifiedPath.size()) {
            return null;
        }
        return simplifiedPath.get(currentPathIndex);
    }

    /**
     * Gibt die Richtung zum nächsten Wegpunkt zurück (in Grad)
     */
    public float getDirectionToNextWaypoint() {
        BlockPos next = getNextWaypoint();
        if (next == null) {
            return 0;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }

        double dx = next.getX() - player.getX();
        double dz = next.getZ() - player.getZ();

        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    // ═══════════════════════════════════════════════════════════
    // LISTENER
    // ═══════════════════════════════════════════════════════════

    public void addListener(NavigationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NavigationListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(NavigationEvent event) {
        for (NavigationListener listener : listeners) {
            try {
                listener.onNavigationEvent(event, this);
            } catch (Exception e) {
                LOGGER.error("[RoadNavigationService] Error in listener", e);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EVENTS & LISTENER INTERFACE
    // ═══════════════════════════════════════════════════════════

    public enum NavigationEvent {
        GRAPH_UPDATED,
        NAVIGATION_STARTED,
        NAVIGATION_STOPPED,
        PATH_UPDATED,
        PATH_NOT_FOUND,
        DESTINATION_REACHED
    }

    @FunctionalInterface
    public interface NavigationListener {
        void onNavigationEvent(NavigationEvent event, RoadNavigationService service);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════

    private static double distance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Gibt Ressourcen frei
     */
    /**
     * SICHERHEIT: Proper Shutdown mit awaitTermination und Thread-safe Instance-Reset
     */
    public void shutdown() {
        stopNavigation();
        // ThreadPoolManager wird zentral heruntergefahren, keine lokale Aktion nötig
        synchronized (INSTANCE_LOCK) {
            instance = null;
        }
    }
}
