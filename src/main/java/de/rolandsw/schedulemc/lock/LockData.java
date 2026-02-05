package de.rolandsw.schedulemc.lock;

import java.util.*;

/**
 * Daten eines einzelnen Schlosses an einer Tuer-Position.
 */
public class LockData {

    private String lockId;
    private LockType type;
    private UUID ownerUUID;
    private String ownerName;

    // Zahlenschloss
    private String code;              // 4-stelliger Code (null bei reinen Schluessel-Schloessern)
    private long lastCodeRotation;    // Zeitpunkt der letzten Code-Rotation

    // Tuer-Position (lower half)
    private int doorX, doorY, doorZ;
    private String dimension;

    // Autorisierte Spieler (duerfen Schluessel erstellen)
    private Set<UUID> authorizedPlayers;

    private long placedTime;

    public LockData(String lockId, LockType type, UUID ownerUUID, String ownerName,
                    int doorX, int doorY, int doorZ, String dimension) {
        this.lockId = lockId;
        this.type = type;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.doorX = doorX;
        this.doorY = doorY;
        this.doorZ = doorZ;
        this.dimension = dimension;
        this.authorizedPlayers = new HashSet<>();
        this.placedTime = System.currentTimeMillis();
        this.lastCodeRotation = placedTime;
        this.code = null;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════

    public String getLockId() { return lockId; }
    public LockType getType() { return type; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public int getDoorX() { return doorX; }
    public int getDoorY() { return doorY; }
    public int getDoorZ() { return doorZ; }
    public String getDimension() { return dimension; }
    public long getPlacedTime() { return placedTime; }
    public void setPlacedTime(long t) { this.placedTime = t; }

    public String getCode() { return code; }
    public void setCode(String code) {
        this.code = code;
        this.lastCodeRotation = System.currentTimeMillis();
    }
    public long getLastCodeRotation() { return lastCodeRotation; }
    public void setLastCodeRotation(long t) { this.lastCodeRotation = t; }

    public Set<UUID> getAuthorizedPlayers() { return authorizedPlayers; }
    public void addAuthorized(UUID uuid) { authorizedPlayers.add(uuid); }
    public void removeAuthorized(UUID uuid) { authorizedPlayers.remove(uuid); }
    public boolean isAuthorized(UUID uuid) {
        return ownerUUID.equals(uuid) || authorizedPlayers.contains(uuid);
    }

    /** Prueft ob der Code rotiert werden muss (basierend auf LockType). */
    public boolean needsCodeRotation() {
        if (!type.hasCode() || type.getCodeRotationMs() <= 0 || code == null) return false;
        return System.currentTimeMillis() - lastCodeRotation > type.getCodeRotationMs();
    }

    /** Generiert einen neuen zufaelligen 4-stelligen Code. */
    public String rotateCode() {
        String newCode = String.format("%04d", new Random().nextInt(10000));
        setCode(newCode);
        return newCode;
    }

    /** Position als lesbarer String. */
    public String getPosString() {
        return doorX + ", " + doorY + ", " + doorZ;
    }
}
