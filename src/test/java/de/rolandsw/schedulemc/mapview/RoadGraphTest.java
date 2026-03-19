package de.rolandsw.schedulemc.mapview;

import de.rolandsw.schedulemc.mapview.navigation.graph.RoadGraph;
import de.rolandsw.schedulemc.mapview.navigation.graph.RoadNode;
import de.rolandsw.schedulemc.mapview.navigation.graph.RoadSegment;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für RoadGraph — Pathfinding, Node-Lookup und Graph-Operationen.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoadGraphTest {

    private RoadGraph emptyGraph;
    private RoadGraph simpleGraph;
    private RoadNode nodeA;
    private RoadNode nodeB;
    private RoadNode nodeC;

    @BeforeEach
    void setUp() {
        emptyGraph = new RoadGraph(Collections.emptyMap(), Collections.emptyList());

        // Drei Nodes in einer Linie: A(0,0) -> B(10,0) -> C(20,0)
        nodeA = new RoadNode(new BlockPos(0, 64, 0), RoadNode.NodeType.ENDPOINT);
        nodeB = new RoadNode(new BlockPos(10, 64, 0), RoadNode.NodeType.INTERSECTION);
        nodeC = new RoadNode(new BlockPos(20, 64, 0), RoadNode.NodeType.ENDPOINT);

        // Segmente erstellen
        List<BlockPos> pathAB = new ArrayList<>();
        for (int x = 0; x <= 10; x++) pathAB.add(new BlockPos(x, 64, 0));
        List<BlockPos> pathBC = new ArrayList<>();
        for (int x = 10; x <= 20; x++) pathBC.add(new BlockPos(x, 64, 0));

        RoadSegment segAB = new RoadSegment(nodeA, nodeB, pathAB);
        RoadSegment segBC = new RoadSegment(nodeB, nodeC, pathBC);

        nodeA.addSegment(segAB);
        nodeB.addSegment(segAB);
        nodeB.addSegment(segBC);
        nodeC.addSegment(segBC);

        Map<BlockPos, RoadNode> nodes = new HashMap<>();
        nodes.put(nodeA.getPosition(), nodeA);
        nodes.put(nodeB.getPosition(), nodeB);
        nodes.put(nodeC.getPosition(), nodeC);

        simpleGraph = new RoadGraph(nodes, List.of(segAB, segBC));
    }

    // ── Empty Graph ───────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Leerer Graph ist leer")
    void testEmptyGraph() {
        assertTrue(emptyGraph.isEmpty());
        assertEquals(0, emptyGraph.getNodeCount());
        assertEquals(0, emptyGraph.getSegmentCount());
    }

    @Test
    @Order(2)
    @DisplayName("Leerer Graph: findNearestNode gibt null zurück")
    void testEmptyGraphFindNearest() {
        assertNull(emptyGraph.findNearestNode(new BlockPos(5, 64, 5)));
    }

    @Test
    @Order(3)
    @DisplayName("Leerer Graph: findPath gibt leere Liste zurück")
    void testEmptyGraphFindPath() {
        List<BlockPos> path = emptyGraph.findPath(new BlockPos(0, 64, 0), new BlockPos(10, 64, 0));
        assertTrue(path.isEmpty());
    }

    // ── Simple Graph ──────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Graph hat korrekte Anzahl Nodes und Segmente")
    void testSimpleGraphSize() {
        assertFalse(simpleGraph.isEmpty());
        assertEquals(3, simpleGraph.getNodeCount());
        assertEquals(2, simpleGraph.getSegmentCount());
    }

    @Test
    @Order(5)
    @DisplayName("Node-Lookup nach Position")
    void testNodeLookupByPosition() {
        RoadNode found = simpleGraph.getNodeAt(new BlockPos(0, 64, 0));
        assertNotNull(found);
        assertEquals(nodeA.getId(), found.getId());
    }

    @Test
    @Order(6)
    @DisplayName("Unbekannte Position gibt null zurück")
    void testNodeLookupUnknownPosition() {
        assertNull(simpleGraph.getNodeAt(new BlockPos(999, 64, 999)));
    }

    @Test
    @Order(7)
    @DisplayName("findNearestNode findet nächsten Node")
    void testFindNearestNode() {
        // Position näher an B (10,0) als an A oder C
        RoadNode nearest = simpleGraph.findNearestNode(new BlockPos(9, 64, 1));
        assertNotNull(nearest);
        assertEquals(nodeB.getId(), nearest.getId());
    }

    @Test
    @Order(8)
    @DisplayName("findNearestNode mit Radius: außerhalb gibt null")
    void testFindNearestNodeWithRadiusMiss() {
        RoadNode nearest = simpleGraph.findNearestNode(new BlockPos(999, 64, 999), 5.0);
        assertNull(nearest);
    }

    @Test
    @Order(9)
    @DisplayName("Pfad von A nach C wird gefunden")
    void testPathfindingAtoC() {
        List<BlockPos> path = simpleGraph.findPath(
            new BlockPos(0, 64, 0),
            new BlockPos(20, 64, 0)
        );
        assertFalse(path.isEmpty());
        // Pfad muss A und C enthalten
        assertTrue(path.stream().anyMatch(p -> p.getX() == 0 && p.getZ() == 0));
        assertTrue(path.stream().anyMatch(p -> p.getX() == 20 && p.getZ() == 0));
    }

    @Test
    @Order(10)
    @DisplayName("Pfad von A nach A ist der Start selbst")
    void testPathfindingSameNode() {
        List<BlockPos> path = simpleGraph.findPath(
            new BlockPos(0, 64, 0),
            new BlockPos(0, 64, 0)
        );
        assertFalse(path.isEmpty());
    }

    // ── Path Simplification ───────────────────────────────────────────────────

    @Test
    @Order(11)
    @DisplayName("simplifyPath: Gerade Linie hat nur Start und Ende")
    void testSimplifyPathStraightLine() {
        List<BlockPos> path = new ArrayList<>();
        for (int x = 0; x <= 10; x++) path.add(new BlockPos(x, 64, 0));

        List<BlockPos> simplified = RoadGraph.simplifyPath(path);
        assertEquals(2, simplified.size());
        assertEquals(new BlockPos(0, 64, 0), simplified.get(0));
        assertEquals(new BlockPos(10, 64, 0), simplified.get(1));
    }

    @Test
    @Order(12)
    @DisplayName("simplifyPath: L-Form behält Eckpunkt")
    void testSimplifyPathLShape() {
        List<BlockPos> path = List.of(
            new BlockPos(0, 64, 0),
            new BlockPos(5, 64, 0),
            new BlockPos(5, 64, 5)
        );

        List<BlockPos> simplified = RoadGraph.simplifyPath(path);
        assertEquals(3, simplified.size());
    }

    @Test
    @Order(13)
    @DisplayName("simplifyPath: Kurze Liste bleibt unverändert")
    void testSimplifyPathShort() {
        List<BlockPos> path = List.of(new BlockPos(0, 64, 0), new BlockPos(1, 64, 0));
        List<BlockPos> simplified = RoadGraph.simplifyPath(path);
        assertEquals(2, simplified.size());
    }

    // ── RoadSegment ───────────────────────────────────────────────────────────

    @Test
    @Order(14)
    @DisplayName("Segment verbindet korrekte Nodes")
    void testSegmentConnects() {
        RoadSegment seg = simpleGraph.getAllSegments().get(0);
        assertTrue(seg.containsNode(nodeA) || seg.containsNode(nodeB));
    }

    @Test
    @Order(15)
    @DisplayName("Segment.getOtherNode gibt anderen Node zurück")
    void testSegmentGetOtherNode() {
        List<BlockPos> path = List.of(nodeA.getPosition(), nodeB.getPosition());
        RoadSegment seg = new RoadSegment(nodeA, nodeB, path);

        assertEquals(nodeB, seg.getOtherNode(nodeA));
        assertEquals(nodeA, seg.getOtherNode(nodeB));
        assertNull(seg.getOtherNode(nodeC));
    }

    @Test
    @Order(16)
    @DisplayName("Segment.getLength ist positiv")
    void testSegmentLength() {
        List<BlockPos> path = List.of(nodeA.getPosition(), nodeB.getPosition());
        RoadSegment seg = new RoadSegment(nodeA, nodeB, path);
        assertTrue(seg.getLength() > 0);
    }

    @Test
    @Order(17)
    @DisplayName("Graph.getStatistics gibt nicht-leeren String zurück")
    void testGraphStatistics() {
        String stats = simpleGraph.getStatistics();
        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertTrue(stats.contains("3")); // 3 nodes
    }
}
