package de.rolandsw.schedulemc.mapview.navigation.graph;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * RoadSegment - Repräsentiert eine Straßenverbindung zwischen zwei RoadNodes
 *
 * Ein Segment enthält:
 * - Start- und End-Node
 * - Alle Blockpositionen entlang des Weges
 * - Die berechnete Länge (für Routing)
 */
public class RoadSegment {

    private final UUID id;
    private final RoadNode startNode;
    private final RoadNode endNode;
    private final List<BlockPos> pathPoints;
    private final double length;

    /**
     * Erstellt ein neues RoadSegment
     *
     * @param startNode Der Start-Knotenpunkt
     * @param endNode Der End-Knotenpunkt
     * @param pathPoints Alle Blöcke zwischen den Nodes (inklusive Start/End)
     */
    public RoadSegment(RoadNode startNode, RoadNode endNode, List<BlockPos> pathPoints) {
        this.id = UUID.randomUUID();
        this.startNode = startNode;
        this.endNode = endNode;
        this.pathPoints = new ArrayList<>(pathPoints);
        this.length = calculateLength();
    }

    /**
     * Erstellt ein RoadSegment mit vorgegebener ID (für Deserialisierung)
     */
    public RoadSegment(UUID id, RoadNode startNode, RoadNode endNode, List<BlockPos> pathPoints) {
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        this.pathPoints = new ArrayList<>(pathPoints);
        this.length = calculateLength();
    }

    // ═══════════════════════════════════════════════════════════
    // LÄNGENBERECHNUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet die tatsächliche Länge des Segments
     * Summiert die Distanzen zwischen allen aufeinanderfolgenden Punkten
     */
    private double calculateLength() {
        if (pathPoints.size() < 2) {
            return startNode.distanceTo(endNode);
        }

        double totalLength = 0;
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            BlockPos current = pathPoints.get(i);
            BlockPos next = pathPoints.get(i + 1);

            double dx = next.getX() - current.getX();
            double dz = next.getZ() - current.getZ();
            totalLength += Math.sqrt(dx * dx + dz * dz);
        }

        return totalLength;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER
    // ═══════════════════════════════════════════════════════════

    public UUID getId() {
        return id;
    }

    public RoadNode getStartNode() {
        return startNode;
    }

    public RoadNode getEndNode() {
        return endNode;
    }

    /**
     * Gibt die Länge des Segments zurück
     *
     * @return Länge in Blöcken
     */
    public double getLength() {
        return length;
    }

    /**
     * Gibt alle Wegpunkte des Segments zurück
     *
     * @return Unmodifiable Liste der BlockPos
     */
    public List<BlockPos> getPathPoints() {
        return Collections.unmodifiableList(pathPoints);
    }

    /**
     * Gibt die Anzahl der Wegpunkte zurück
     *
     * @return Anzahl der Punkte
     */
    public int getPointCount() {
        return pathPoints.size();
    }

    // ═══════════════════════════════════════════════════════════
    // NODE-OPERATIONEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt den anderen Node zurück (wenn einer bekannt ist)
     *
     * @param node Der bekannte Node
     * @return Der andere Node oder null wenn node nicht Teil des Segments ist
     */
    public RoadNode getOtherNode(RoadNode node) {
        if (node.equals(startNode)) {
            return endNode;
        } else if (node.equals(endNode)) {
            return startNode;
        }
        return null;
    }

    /**
     * Prüft ob ein Node Teil dieses Segments ist
     *
     * @param node Der zu prüfende Node
     * @return true wenn der Node Start oder End ist
     */
    public boolean containsNode(RoadNode node) {
        return node.equals(startNode) || node.equals(endNode);
    }

    /**
     * Prüft ob dieses Segment zwei bestimmte Nodes verbindet
     *
     * @param nodeA Erster Node
     * @param nodeB Zweiter Node
     * @return true wenn das Segment diese beiden Nodes verbindet
     */
    public boolean connects(RoadNode nodeA, RoadNode nodeB) {
        return (startNode.equals(nodeA) && endNode.equals(nodeB)) ||
               (startNode.equals(nodeB) && endNode.equals(nodeA));
    }

    // ═══════════════════════════════════════════════════════════
    // PFAD-OPERATIONEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt den Pfad in korrekter Richtung zurück
     *
     * @param fromNode Der Startnode für die Richtung
     * @return Liste der Punkte in richtiger Reihenfolge
     */
    public List<BlockPos> getPathFrom(RoadNode fromNode) {
        if (fromNode.equals(startNode)) {
            return new ArrayList<>(pathPoints);
        } else if (fromNode.equals(endNode)) {
            List<BlockPos> reversed = new ArrayList<>(pathPoints);
            Collections.reverse(reversed);
            return reversed;
        }
        return Collections.emptyList();
    }

    /**
     * Findet den nächsten Punkt auf dem Segment zu einer Position
     *
     * @param pos Die Referenzposition
     * @return Der nächste Punkt auf dem Segment
     */
    public BlockPos findNearestPoint(BlockPos pos) {
        BlockPos nearest = pathPoints.get(0);
        double minDist = distance(pos, nearest);

        for (BlockPos point : pathPoints) {
            double dist = distance(pos, point);
            if (dist < minDist) {
                minDist = dist;
                nearest = point;
            }
        }

        return nearest;
    }

    /**
     * Berechnet die minimale Distanz einer Position zum Segment
     *
     * @param pos Die Position
     * @return Distanz in Blöcken
     */
    public double distanceToPoint(BlockPos pos) {
        double minDist = Double.MAX_VALUE;

        for (BlockPos point : pathPoints) {
            double dist = distance(pos, point);
            if (dist < minDist) {
                minDist = dist;
            }
        }

        return minDist;
    }

    /**
     * Prüft ob eine Position auf diesem Segment liegt
     *
     * @param pos Die zu prüfende Position
     * @return true wenn die Position Teil des Segments ist
     */
    public boolean containsPoint(BlockPos pos) {
        for (BlockPos point : pathPoints) {
            if (point.getX() == pos.getX() && point.getZ() == pos.getZ()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt den Index eines Punktes auf dem Pfad zurück
     *
     * @param pos Die zu suchende Position
     * @return Index oder -1 wenn nicht gefunden
     */
    public int getPointIndex(BlockPos pos) {
        for (int i = 0; i < pathPoints.size(); i++) {
            BlockPos point = pathPoints.get(i);
            if (point.getX() == pos.getX() && point.getZ() == pos.getZ()) {
                return i;
            }
        }
        return -1;
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════

    private static double distance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    // ═══════════════════════════════════════════════════════════
    // EQUALS & HASHCODE
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RoadSegment other = (RoadSegment) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("RoadSegment[%s -> %s, length=%.1f, points=%d]",
                startNode.getId().toString().substring(0, 8),
                endNode.getId().toString().substring(0, 8),
                length, pathPoints.size());
    }
}
