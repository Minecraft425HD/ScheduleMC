package de.rolandsw.schedulemc.mapview.navigation.graph;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * RoadNode - Repräsentiert einen Knotenpunkt im Straßen-Graphen
 *
 * Nodes können sein:
 * - INTERSECTION: Kreuzung mit 3+ Verbindungen
 * - ENDPOINT: Sackgasse mit 1 Verbindung
 * - WAYPOINT: Punkt auf einer Kurve (2 Verbindungen, Richtungsänderung)
 */
public class RoadNode {

    /**
     * Typ des Knotenpunkts
     */
    public enum NodeType {
        /** Kreuzung - 3 oder mehr verbundene Straßen */
        INTERSECTION,
        /** Sackgasse - nur 1 verbundene Straße */
        ENDPOINT,
        /** Wegpunkt auf Kurve - 2 Verbindungen mit Richtungsänderung */
        WAYPOINT
    }

    private final UUID id;
    private final BlockPos position;
    private final NodeType type;
    private final List<RoadSegment> connectedSegments;

    /**
     * Erstellt einen neuen RoadNode
     *
     * @param position Die Blockposition des Knotens
     * @param type Der Typ des Knotens
     */
    public RoadNode(BlockPos position, NodeType type) {
        this.id = UUID.randomUUID();
        this.position = position;
        this.type = type;
        this.connectedSegments = new ArrayList<>();
    }

    /**
     * Erstellt einen RoadNode mit vorgegebener ID (für Deserialisierung)
     */
    public RoadNode(UUID id, BlockPos position, NodeType type) {
        this.id = id;
        this.position = position;
        this.type = type;
        this.connectedSegments = new ArrayList<>();
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER
    // ═══════════════════════════════════════════════════════════

    public UUID getId() {
        return id;
    }

    public BlockPos getPosition() {
        return position;
    }

    public NodeType getType() {
        return type;
    }

    public int getX() {
        return position.getX();
    }

    public int getZ() {
        return position.getZ();
    }

    // ═══════════════════════════════════════════════════════════
    // SEGMENT-VERWALTUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt ein verbundenes Segment hinzu
     *
     * @param segment Das zu verbindende Segment
     */
    public void addSegment(RoadSegment segment) {
        if (segment != null && !connectedSegments.contains(segment)) {
            connectedSegments.add(segment);
        }
    }

    /**
     * Entfernt ein verbundenes Segment
     *
     * @param segment Das zu entfernende Segment
     */
    public void removeSegment(RoadSegment segment) {
        connectedSegments.remove(segment);
    }

    /**
     * Gibt alle verbundenen Segmente zurück
     *
     * @return Unmodifiable Liste der Segmente
     */
    public List<RoadSegment> getConnectedSegments() {
        return Collections.unmodifiableList(connectedSegments);
    }

    /**
     * Gibt die Anzahl der verbundenen Segmente zurück
     *
     * @return Anzahl der Verbindungen
     */
    public int getConnectionCount() {
        return connectedSegments.size();
    }

    /**
     * Prüft ob dieser Node mit einem anderen verbunden ist
     *
     * @param other Der andere Node
     * @return true wenn eine direkte Verbindung besteht
     */
    public boolean isConnectedTo(RoadNode other) {
        for (RoadSegment segment : connectedSegments) {
            if (segment.getOtherNode(this).equals(other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Findet das Segment zu einem anderen Node
     *
     * @param other Der Ziel-Node
     * @return Das verbindende Segment oder null
     */
    @Nullable
    public RoadSegment getSegmentTo(RoadNode other) {
        for (RoadSegment segment : connectedSegments) {
            if (segment.getOtherNode(this).equals(other)) {
                return segment;
            }
        }
        return null;
    }

    /**
     * Gibt alle verbundenen Nodes zurück
     *
     * @return Liste der Nachbar-Nodes
     */
    public List<RoadNode> getNeighbors() {
        List<RoadNode> neighbors = new ArrayList<>();
        for (RoadSegment segment : connectedSegments) {
            neighbors.add(segment.getOtherNode(this));
        }
        return neighbors;
    }

    // ═══════════════════════════════════════════════════════════
    // DISTANZ-BERECHNUNGEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet die euklidische Distanz zu einer Position
     *
     * @param pos Die Zielposition
     * @return Distanz in Blöcken
     */
    public double distanceTo(BlockPos pos) {
        double dx = position.getX() - pos.getX();
        double dz = position.getZ() - pos.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Berechnet die euklidische Distanz zu einem anderen Node
     *
     * @param other Der andere Node
     * @return Distanz in Blöcken
     */
    public double distanceTo(RoadNode other) {
        return distanceTo(other.getPosition());
    }

    /**
     * Berechnet die Manhattan-Distanz zu einer Position
     *
     * @param pos Die Zielposition
     * @return Manhattan-Distanz in Blöcken
     */
    public int manhattanDistanceTo(BlockPos pos) {
        return Math.abs(position.getX() - pos.getX()) +
               Math.abs(position.getZ() - pos.getZ());
    }

    // ═══════════════════════════════════════════════════════════
    // EQUALS & HASHCODE
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RoadNode other = (RoadNode) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("RoadNode[%s @ (%d, %d), type=%s, connections=%d]",
                id.toString().substring(0, 8),
                position.getX(), position.getZ(),
                type, connectedSegments.size());
    }
}
