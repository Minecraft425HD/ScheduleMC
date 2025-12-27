package de.rolandsw.schedulemc.mapview.navigation.graph;

import de.rolandsw.schedulemc.managers.NPCEntityRegistry;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

/**
 * NavigationTarget - Repräsentiert ein Navigationsziel
 *
 * Unterstützt verschiedene Zieltypen:
 * - BLOCK_POS: Statische Position (Haus, markierter Ort)
 * - ENTITY: Bewegliches Entity (NPC, Spieler)
 * - POI: Point of Interest mit Namen
 */
public class NavigationTarget {

    /**
     * Typ des Navigationsziels
     */
    public enum TargetType {
        /** Statische Block-Position */
        BLOCK_POS,
        /** Bewegliches Entity (NPC, Spieler) */
        ENTITY,
        /** Benannter Point of Interest */
        POI
    }

    private final TargetType type;
    private final BlockPos staticPosition;
    private final UUID entityUUID;
    private final String poiId;
    private final String displayName;

    // Letzte bekannte Position (für bewegliche Ziele)
    private BlockPos lastKnownPosition;
    private long lastPositionUpdate;

    // ═══════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt ein statisches Positions-Ziel
     */
    public static NavigationTarget atPosition(BlockPos position, String name) {
        return new NavigationTarget(TargetType.BLOCK_POS, position, null, null, name);
    }

    /**
     * Erstellt ein statisches Positions-Ziel ohne Namen
     */
    public static NavigationTarget atPosition(BlockPos position) {
        return atPosition(position, "Position (" + position.getX() + ", " + position.getZ() + ")");
    }

    /**
     * Erstellt ein Entity-Ziel (z.B. NPC)
     */
    public static NavigationTarget forEntity(UUID entityUUID, String name) {
        return new NavigationTarget(TargetType.ENTITY, null, entityUUID, null, name);
    }

    /**
     * Erstellt ein Entity-Ziel aus einem Entity
     */
    public static NavigationTarget forEntity(Entity entity) {
        String name = entity.getName().getString();
        return forEntity(entity.getUUID(), name);
    }

    /**
     * Erstellt ein NPC-Ziel
     */
    public static NavigationTarget forNPC(CustomNPCEntity npc) {
        return forEntity(npc.getUUID(), npc.getNpcName());
    }

    /**
     * Erstellt ein POI-Ziel
     */
    public static NavigationTarget forPOI(String poiId, BlockPos position, String name) {
        NavigationTarget target = new NavigationTarget(TargetType.POI, position, null, poiId, name);
        target.lastKnownPosition = position;
        return target;
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private NavigationTarget(TargetType type, BlockPos position, UUID entityUUID, String poiId, String displayName) {
        this.type = type;
        this.staticPosition = position;
        this.entityUUID = entityUUID;
        this.poiId = poiId;
        this.displayName = displayName;
        this.lastKnownPosition = position;
        this.lastPositionUpdate = System.currentTimeMillis();
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER
    // ═══════════════════════════════════════════════════════════

    public TargetType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }

    public String getPoiId() {
        return poiId;
    }

    /**
     * Gibt die aktuelle Position des Ziels zurück
     *
     * Für statische Ziele wird die fixe Position zurückgegeben.
     * Für Entities wird die Live-Position abgerufen.
     *
     * @return Aktuelle Position oder null wenn Entity nicht gefunden
     */
    public BlockPos getCurrentPosition() {
        switch (type) {
            case BLOCK_POS:
            case POI:
                return staticPosition;

            case ENTITY:
                return getEntityPosition();

            default:
                return lastKnownPosition;
        }
    }

    /**
     * Gibt die letzte bekannte Position zurück
     * Nützlich wenn das Entity temporär nicht verfügbar ist
     */
    public BlockPos getLastKnownPosition() {
        return lastKnownPosition;
    }

    /**
     * Holt die aktuelle Position eines Entities
     */
    private BlockPos getEntityPosition() {
        if (entityUUID == null) {
            return lastKnownPosition;
        }

        // Versuche NPC über Registry zu finden
        CustomNPCEntity npc = NPCEntityRegistry.getNPCByUUID(entityUUID);
        if (npc != null && npc.isAlive()) {
            lastKnownPosition = npc.blockPosition();
            lastPositionUpdate = System.currentTimeMillis();
            return lastKnownPosition;
        }

        // Fallback auf letzte bekannte Position
        return lastKnownPosition;
    }

    // ═══════════════════════════════════════════════════════════
    // STATUS-CHECKS
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob das Ziel noch gültig ist
     */
    public boolean isValid() {
        switch (type) {
            case BLOCK_POS:
            case POI:
                return staticPosition != null;

            case ENTITY:
                if (entityUUID == null) return false;
                CustomNPCEntity npc = NPCEntityRegistry.getNPCByUUID(entityUUID);
                return npc != null && npc.isAlive();

            default:
                return false;
        }
    }

    /**
     * Prüft ob das Ziel beweglich ist (Entity)
     */
    public boolean isMoving() {
        return type == TargetType.ENTITY;
    }

    /**
     * Prüft ob sich das Ziel signifikant bewegt hat
     *
     * @param threshold Mindestdistanz in Blöcken
     * @return true wenn Neuberechnung des Pfads empfohlen
     */
    public boolean hasMovedSignificantly(double threshold) {
        if (!isMoving()) {
            return false;
        }

        BlockPos current = getCurrentPosition();
        if (current == null || lastKnownPosition == null) {
            return false;
        }

        double dx = current.getX() - lastKnownPosition.getX();
        double dz = current.getZ() - lastKnownPosition.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        return distance >= threshold;
    }

    /**
     * Gibt die Zeit seit dem letzten Positions-Update zurück
     */
    public long getMillisSinceLastUpdate() {
        return System.currentTimeMillis() - lastPositionUpdate;
    }

    // ═══════════════════════════════════════════════════════════
    // DISTANZ-BERECHNUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet die Distanz zu einer Position
     */
    public double distanceTo(BlockPos pos) {
        BlockPos targetPos = getCurrentPosition();
        if (targetPos == null || pos == null) {
            return Double.MAX_VALUE;
        }

        double dx = targetPos.getX() - pos.getX();
        double dz = targetPos.getZ() - pos.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Prüft ob eine Position nahe am Ziel ist
     */
    public boolean isNear(BlockPos pos, double radius) {
        return distanceTo(pos) <= radius;
    }

    // ═══════════════════════════════════════════════════════════
    // EQUALS & HASHCODE
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        NavigationTarget other = (NavigationTarget) obj;

        if (type != other.type) return false;

        switch (type) {
            case BLOCK_POS:
                return staticPosition != null && staticPosition.equals(other.staticPosition);
            case ENTITY:
                return entityUUID != null && entityUUID.equals(other.entityUUID);
            case POI:
                return poiId != null && poiId.equals(other.poiId);
            default:
                return false;
        }
    }

    @Override
    public int hashCode() {
        switch (type) {
            case BLOCK_POS:
                return staticPosition != null ? staticPosition.hashCode() : 0;
            case ENTITY:
                return entityUUID != null ? entityUUID.hashCode() : 0;
            case POI:
                return poiId != null ? poiId.hashCode() : 0;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        BlockPos pos = getCurrentPosition();
        String posStr = pos != null ?
                String.format("(%d, %d)", pos.getX(), pos.getZ()) : "unknown";

        return String.format("NavigationTarget[%s, %s, %s]", type, displayName, posStr);
    }
}
