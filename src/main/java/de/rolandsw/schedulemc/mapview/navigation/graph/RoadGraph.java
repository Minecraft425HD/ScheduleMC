package de.rolandsw.schedulemc.mapview.navigation.graph;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.util.*;

/**
 * RoadGraph - Der navigierbare Straßen-Graph
 *
 * Enthält alle Nodes (Kreuzungen, Endpunkte) und Segmente (Straßenabschnitte).
 * Bietet Dijkstra-basiertes Routing für kürzeste Pfade.
 */
public class RoadGraph {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<BlockPos, RoadNode> nodesByPosition;
    private final List<RoadSegment> segments;
    private final Map<UUID, RoadNode> nodesById;

    // Cache für häufige Routen
    private final Map<String, List<BlockPos>> pathCache = new LinkedHashMap<String, List<BlockPos>>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<BlockPos>> eldest) {
            return size() > 100;
        }
    };

    public RoadGraph(Map<BlockPos, RoadNode> nodesByPosition, List<RoadSegment> segments) {
        this.nodesByPosition = new HashMap<>(nodesByPosition);
        this.segments = new ArrayList<>(segments);

        // Baue ID-Index auf
        this.nodesById = new HashMap<>();
        for (RoadNode node : nodesByPosition.values()) {
            nodesById.put(node.getId(), node);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER
    // ═══════════════════════════════════════════════════════════

    public int getNodeCount() {
        return nodesByPosition.size();
    }

    public int getSegmentCount() {
        return segments.size();
    }

    public Collection<RoadNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodesByPosition.values());
    }

    public List<RoadSegment> getAllSegments() {
        return Collections.unmodifiableList(segments);
    }

    public RoadNode getNodeAt(BlockPos pos) {
        return nodesByPosition.get(pos);
    }

    public RoadNode getNodeById(UUID id) {
        return nodesById.get(id);
    }

    public boolean isEmpty() {
        return nodesByPosition.isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    // NÄCHSTEN NODE FINDEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Findet den nächsten Node zu einer Position
     *
     * @param pos Die Referenzposition
     * @return Der nächste Node oder null wenn Graph leer
     */
    public RoadNode findNearestNode(BlockPos pos) {
        if (nodesByPosition.isEmpty()) {
            return null;
        }

        RoadNode nearest = null;
        double minDist = Double.MAX_VALUE;

        for (RoadNode node : nodesByPosition.values()) {
            double dist = node.distanceTo(pos);
            if (dist < minDist) {
                minDist = dist;
                nearest = node;
            }
        }

        return nearest;
    }

    /**
     * Findet den nächsten Node innerhalb eines Maximalradius
     *
     * @param pos Die Referenzposition
     * @param maxRadius Maximaler Suchradius
     * @return Der nächste Node oder null wenn keiner im Radius
     */
    public RoadNode findNearestNode(BlockPos pos, double maxRadius) {
        RoadNode nearest = findNearestNode(pos);
        if (nearest != null && nearest.distanceTo(pos) <= maxRadius) {
            return nearest;
        }
        return null;
    }

    /**
     * Findet den nächsten Punkt auf irgendeinem Segment
     *
     * @param pos Die Referenzposition
     * @return Der nächste Punkt auf der Straße
     */
    public BlockPos findNearestRoadPoint(BlockPos pos) {
        BlockPos nearest = null;
        double minDist = Double.MAX_VALUE;

        for (RoadSegment segment : segments) {
            BlockPos segmentNearest = segment.findNearestPoint(pos);
            double dist = distance(pos, segmentNearest);
            if (dist < minDist) {
                minDist = dist;
                nearest = segmentNearest;
            }
        }

        return nearest;
    }

    // ═══════════════════════════════════════════════════════════
    // PATHFINDING (DIJKSTRA)
    // ═══════════════════════════════════════════════════════════

    /**
     * Findet den kürzesten Pfad zwischen zwei Positionen
     *
     * @param start Startposition
     * @param end Zielposition
     * @return Liste der Wegpunkte oder leere Liste wenn kein Pfad existiert
     */
    public List<BlockPos> findPath(BlockPos start, BlockPos end) {
        // Prüfe Cache
        String cacheKey = start.getX() + "," + start.getZ() + "->" + end.getX() + "," + end.getZ();
        List<BlockPos> cached = pathCache.get(cacheKey);
        if (cached != null) {
            return new ArrayList<>(cached);
        }

        // Finde nächste Nodes zu Start und Ziel
        RoadNode startNode = findNearestNode(start);
        RoadNode endNode = findNearestNode(end);

        if (startNode == null || endNode == null) {
            LOGGER.debug("[RoadGraph] No path: start or end node not found");
            return Collections.emptyList();
        }

        // Wenn Start und Ziel der gleiche Node sind
        if (startNode.equals(endNode)) {
            List<BlockPos> path = Arrays.asList(start, startNode.getPosition(), end);
            pathCache.put(cacheKey, path);
            return path;
        }

        // Dijkstra-Algorithmus
        List<RoadNode> nodePath = dijkstra(startNode, endNode);

        if (nodePath.isEmpty()) {
            LOGGER.debug("[RoadGraph] No path found between nodes");
            return Collections.emptyList();
        }

        // Expandiere Node-Pfad zu vollständigem Block-Pfad
        List<BlockPos> fullPath = expandNodePath(nodePath, start, end);

        // Cache das Ergebnis
        pathCache.put(cacheKey, fullPath);

        return fullPath;
    }

    /**
     * Dijkstra-Algorithmus für kürzesten Pfad zwischen Nodes
     */
    private List<RoadNode> dijkstra(RoadNode start, RoadNode end) {
        // Distanzen zu allen Nodes
        Map<RoadNode, Double> distances = new HashMap<>();
        Map<RoadNode, RoadNode> previous = new HashMap<>();
        Set<RoadNode> visited = new HashSet<>();

        // Priority Queue sortiert nach Distanz
        PriorityQueue<RoadNode> queue = new PriorityQueue<>(
                Comparator.comparingDouble(node -> distances.getOrDefault(node, Double.MAX_VALUE))
        );

        // Initialisierung
        distances.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            RoadNode current = queue.poll();

            // Bereits besucht?
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            // Ziel erreicht?
            if (current.equals(end)) {
                return reconstructNodePath(previous, start, end);
            }

            // Nachbarn prüfen
            for (RoadSegment segment : current.getConnectedSegments()) {
                RoadNode neighbor = segment.getOtherNode(current);

                if (visited.contains(neighbor)) {
                    continue;
                }

                double newDist = distances.get(current) + segment.getLength();
                double oldDist = distances.getOrDefault(neighbor, Double.MAX_VALUE);

                if (newDist < oldDist) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Kein Pfad gefunden
        return Collections.emptyList();
    }

    /**
     * Rekonstruiert den Node-Pfad aus der previous-Map
     */
    private List<RoadNode> reconstructNodePath(Map<RoadNode, RoadNode> previous, RoadNode start, RoadNode end) {
        List<RoadNode> path = new ArrayList<>();
        RoadNode current = end;

        while (current != null) {
            path.add(0, current);
            if (current.equals(start)) {
                break;
            }
            current = previous.get(current);
        }

        return path;
    }

    /**
     * Expandiert einen Node-Pfad zu einem vollständigen Block-Pfad
     */
    private List<BlockPos> expandNodePath(List<RoadNode> nodePath, BlockPos originalStart, BlockPos originalEnd) {
        List<BlockPos> fullPath = new ArrayList<>();

        // Füge Startpunkt hinzu (wenn nicht schon auf der Straße)
        if (!originalStart.equals(nodePath.get(0).getPosition())) {
            fullPath.add(originalStart);
        }

        // Füge alle Segment-Punkte hinzu
        for (int i = 0; i < nodePath.size() - 1; i++) {
            RoadNode current = nodePath.get(i);
            RoadNode next = nodePath.get(i + 1);

            RoadSegment segment = findSegmentBetween(current, next);
            if (segment != null) {
                List<BlockPos> segmentPoints = segment.getPathFrom(current);

                // Vermeide Duplikate am Anfang
                for (BlockPos point : segmentPoints) {
                    if (fullPath.isEmpty() || !fullPath.get(fullPath.size() - 1).equals(point)) {
                        fullPath.add(point);
                    }
                }
            }
        }

        // Füge Endpunkt hinzu (wenn nicht schon auf der Straße)
        if (!originalEnd.equals(nodePath.get(nodePath.size() - 1).getPosition())) {
            fullPath.add(originalEnd);
        }

        return fullPath;
    }

    /**
     * Findet das Segment zwischen zwei Nodes
     */
    private RoadSegment findSegmentBetween(RoadNode nodeA, RoadNode nodeB) {
        for (RoadSegment segment : nodeA.getConnectedSegments()) {
            if (segment.connects(nodeA, nodeB)) {
                return segment;
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // PFAD-VEREINFACHUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Vereinfacht einen Pfad durch Entfernen kollinearer Punkte
     *
     * @param path Der ursprüngliche Pfad
     * @return Vereinfachter Pfad mit nur Richtungsänderungen
     */
    public static List<BlockPos> simplifyPath(List<BlockPos> path) {
        if (path.size() <= 2) {
            return new ArrayList<>(path);
        }

        List<BlockPos> simplified = new ArrayList<>();
        simplified.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i++) {
            BlockPos prev = path.get(i - 1);
            BlockPos curr = path.get(i);
            BlockPos next = path.get(i + 1);

            // Prüfe ob Richtung sich ändert
            int dx1 = Integer.signum(curr.getX() - prev.getX());
            int dz1 = Integer.signum(curr.getZ() - prev.getZ());
            int dx2 = Integer.signum(next.getX() - curr.getX());
            int dz2 = Integer.signum(next.getZ() - curr.getZ());

            if (dx1 != dx2 || dz1 != dz2) {
                simplified.add(curr);
            }
        }

        simplified.add(path.get(path.size() - 1));
        return simplified;
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
     * Leert den Pfad-Cache
     */
    public void clearCache() {
        pathCache.clear();
    }

    /**
     * Gibt Statistiken über den Graph zurück
     */
    public String getStatistics() {
        double totalLength = 0;
        for (RoadSegment segment : segments) {
            totalLength += segment.getLength();
        }

        int intersections = 0;
        int endpoints = 0;
        for (RoadNode node : nodesByPosition.values()) {
            if (node.getType() == RoadNode.NodeType.INTERSECTION) {
                intersections++;
            } else if (node.getType() == RoadNode.NodeType.ENDPOINT) {
                endpoints++;
            }
        }

        return String.format(
                "RoadGraph: %d nodes (%d intersections, %d endpoints), %d segments, %.1f total length",
                nodesByPosition.size(), intersections, endpoints, segments.size(), totalLength
        );
    }

    @Override
    public String toString() {
        return getStatistics();
    }
}
