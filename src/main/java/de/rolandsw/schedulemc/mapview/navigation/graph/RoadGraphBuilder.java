package de.rolandsw.schedulemc.mapview.navigation.graph;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.mapview.service.data.WorldMapData;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.util.*;

/**
 * RoadGraphBuilder - Scannt die Welt und baut einen Straßen-Graphen auf
 *
 * Funktionsweise:
 * 1. Scannt alle Blöcke im angegebenen Bereich nach Straßenblöcken
 * 2. Erkennt Kreuzungen (3+ Nachbarn) und Sackgassen (1 Nachbar)
 * 3. Verfolgt Straßen zwischen Kreuzungen und erstellt Segmente
 * 4. Baut einen navigierbaren Graphen auf
 */
public class RoadGraphBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();

    // 8 Richtungen für Nachbar-Suche
    private static final int[][] DIRECTIONS_8 = {
        {0, 1}, {1, 0}, {0, -1}, {-1, 0},   // Kardinal
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // Diagonal
    };

    // 4 Richtungen (nur kardinal) für striktere Straßenverfolgung
    private static final int[][] DIRECTIONS_4 = {
        {0, 1}, {1, 0}, {0, -1}, {-1, 0}
    };

    private final WorldMapData mapData;
    private final Map<BlockPos, RoadNode> nodesByPosition = new HashMap<>();
    private final List<RoadSegment> segments = new ArrayList<>();
    private final Set<BlockPos> allRoadBlocks = new HashSet<>();

    // Konfiguration
    private boolean useDiagonalConnections = true;
    private int defaultY = 64;

    public RoadGraphBuilder(WorldMapData mapData) {
        this.mapData = mapData;
    }

    /**
     * Aktiviert/Deaktiviert diagonale Verbindungen
     */
    public RoadGraphBuilder withDiagonalConnections(boolean enabled) {
        this.useDiagonalConnections = enabled;
        return this;
    }

    /**
     * Setzt die Standard-Y-Koordinate für Nodes
     */
    public RoadGraphBuilder withDefaultY(int y) {
        this.defaultY = y;
        return this;
    }

    /**
     * Baut den Straßen-Graphen für einen Bereich auf
     *
     * @param centerX X-Koordinate des Zentrums
     * @param centerZ Z-Koordinate des Zentrums
     * @param radius Suchradius in Blöcken
     * @return Der fertige RoadGraph
     */
    public RoadGraph buildGraph(int centerX, int centerZ, int radius) {
        long startTime = System.currentTimeMillis();

        // Reset
        nodesByPosition.clear();
        segments.clear();
        allRoadBlocks.clear();

        // Schritt 1: Finde alle Straßenblöcke
        LOGGER.debug("[RoadGraphBuilder] Scanning area ({}, {}) radius {}", centerX, centerZ, radius);
        scanRoadBlocks(centerX, centerZ, radius);
        LOGGER.debug("[RoadGraphBuilder] Found {} road blocks", allRoadBlocks.size());

        if (allRoadBlocks.isEmpty()) {
            return new RoadGraph(Collections.emptyMap(), Collections.emptyList());
        }

        // Schritt 2: Finde Kreuzungen und Endpunkte
        findNodes();
        LOGGER.debug("[RoadGraphBuilder] Found {} nodes", nodesByPosition.size());

        // Schritt 3: Verbinde Nodes durch Segmente
        buildSegments();
        LOGGER.debug("[RoadGraphBuilder] Created {} segments", segments.size());

        long elapsed = System.currentTimeMillis() - startTime;
        LOGGER.info("[RoadGraphBuilder] Graph built in {}ms: {} nodes, {} segments",
                elapsed, nodesByPosition.size(), segments.size());

        return new RoadGraph(new HashMap<>(nodesByPosition), new ArrayList<>(segments));
    }

    /**
     * Scannt den Bereich nach Straßenblöcken
     */
    private void scanRoadBlocks(int centerX, int centerZ, int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                if (RoadBlockDetector.isRoadAt(mapData, x, z)) {
                    allRoadBlocks.add(new BlockPos(x, defaultY, z));
                }
            }
        }
    }

    /**
     * Findet alle Kreuzungen und Endpunkte
     */
    private void findNodes() {
        for (BlockPos pos : allRoadBlocks) {
            int neighbors = countRoadNeighbors(pos);

            if (neighbors >= 3) {
                // Kreuzung
                RoadNode node = new RoadNode(pos, RoadNode.NodeType.INTERSECTION);
                nodesByPosition.put(pos, node);
            } else if (neighbors == 1) {
                // Sackgasse / Endpunkt
                RoadNode node = new RoadNode(pos, RoadNode.NodeType.ENDPOINT);
                nodesByPosition.put(pos, node);
            }
            // neighbors == 2 sind normale Straßenpunkte (kein Node nötig)
        }
    }

    /**
     * Zählt die Straßen-Nachbarn einer Position
     */
    private int countRoadNeighbors(BlockPos pos) {
        int count = 0;
        int[][] directions = useDiagonalConnections ? DIRECTIONS_8 : DIRECTIONS_4;

        for (int[] dir : directions) {
            BlockPos neighbor = pos.offset(dir[0], 0, dir[1]);
            if (allRoadBlocks.contains(neighbor)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Baut Segmente zwischen allen Nodes
     */
    private void buildSegments() {
        Set<BlockPos> processedStartPoints = new HashSet<>();

        for (RoadNode node : nodesByPosition.values()) {
            traceSegmentsFrom(node, processedStartPoints);
        }
    }

    /**
     * Verfolgt alle Straßen von einem Node aus
     */
    private void traceSegmentsFrom(RoadNode startNode, Set<BlockPos> processedStartPoints) {
        int[][] directions = useDiagonalConnections ? DIRECTIONS_8 : DIRECTIONS_4;

        for (int[] dir : directions) {
            BlockPos nextPos = startNode.getPosition().offset(dir[0], 0, dir[1]);

            // Prüfe ob diese Richtung bereits verarbeitet wurde
            String segmentKey = createSegmentKey(startNode.getPosition(), nextPos);
            if (processedStartPoints.contains(nextPos) && !nodesByPosition.containsKey(nextPos)) {
                continue;
            }

            if (!allRoadBlocks.contains(nextPos)) {
                continue;
            }

            // Verfolge die Straße bis zum nächsten Node
            List<BlockPos> pathPoints = new ArrayList<>();
            pathPoints.add(startNode.getPosition());

            BlockPos current = nextPos;
            BlockPos previous = startNode.getPosition();
            RoadNode endNode = null;

            while (current != null) {
                pathPoints.add(current);

                // Prüfe ob wir einen anderen Node erreicht haben
                RoadNode foundNode = nodesByPosition.get(current);
                if (foundNode != null && !foundNode.equals(startNode)) {
                    endNode = foundNode;
                    break;
                }

                // Finde nächsten Block auf der Straße
                BlockPos next = findNextRoadBlock(current, previous);
                previous = current;
                current = next;
            }

            // Nur Segment erstellen wenn wir einen End-Node gefunden haben
            if (endNode != null) {
                // Prüfe ob dieses Segment bereits existiert
                if (!segmentExists(startNode, endNode)) {
                    RoadSegment segment = new RoadSegment(startNode, endNode, pathPoints);
                    segments.add(segment);
                    startNode.addSegment(segment);
                    endNode.addSegment(segment);
                }
            }
        }
    }

    /**
     * Findet den nächsten Straßenblock in der Verfolgung
     */
    private BlockPos findNextRoadBlock(BlockPos current, BlockPos previous) {
        int[][] directions = useDiagonalConnections ? DIRECTIONS_8 : DIRECTIONS_4;
        List<BlockPos> candidates = new ArrayList<>();

        for (int[] dir : directions) {
            BlockPos neighbor = current.offset(dir[0], 0, dir[1]);

            // Nicht zurückgehen
            if (neighbor.equals(previous)) {
                continue;
            }

            if (allRoadBlocks.contains(neighbor)) {
                candidates.add(neighbor);
            }
        }

        // Wenn genau 1 Kandidat, folge diesem
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        // Mehrere Kandidaten = Kreuzung (sollte ein Node sein)
        // Keine Kandidaten = Sackgasse
        return null;
    }

    /**
     * Prüft ob ein Segment zwischen zwei Nodes bereits existiert
     */
    private boolean segmentExists(RoadNode nodeA, RoadNode nodeB) {
        for (RoadSegment segment : segments) {
            if (segment.connects(nodeA, nodeB)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Erstellt einen eindeutigen Key für eine Segment-Richtung
     */
    private String createSegmentKey(BlockPos from, BlockPos to) {
        return from.getX() + "," + from.getZ() + "->" + to.getX() + "," + to.getZ();
    }

    // ═══════════════════════════════════════════════════════════
    // INKREMENTELLE UPDATES
    // ═══════════════════════════════════════════════════════════

    /**
     * Aktualisiert den Graph für einen geänderten Chunk
     * (Für spätere Implementierung von Live-Updates)
     *
     * @param graph Der bestehende Graph
     * @param chunkX Chunk X-Koordinate
     * @param chunkZ Chunk Z-Koordinate
     * @return Aktualisierter Graph
     */
    public RoadGraph updateGraphForChunk(RoadGraph graph, int chunkX, int chunkZ) {
        // Chunk-Koordinaten zu Block-Koordinaten
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;

        // Scan nur den Chunk-Bereich plus 1 Block Rand
        // (um Verbindungen zu Nachbar-Chunks zu erkennen)
        int radius = 9; // 16/2 + 1
        int centerX = blockX + 8;
        int centerZ = blockZ + 8;

        // Entferne alte Nodes/Segments im Bereich
        // ... (komplexe Logik für inkrementelle Updates)

        // Für jetzt: kompletter Rebuild
        // TODO: Implementiere echtes inkrementelles Update
        return graph;
    }
}
