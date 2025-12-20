package de.rolandsw.schedulemc.territory;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Repräsentiert ein Chunk-basiertes Territorium
 */
public class Territory {
    @SerializedName("chunkX")
    private final int chunkX;

    @SerializedName("chunkZ")
    private final int chunkZ;

    @SerializedName("type")
    private TerritoryType type;

    @SerializedName("name")
    private String name;

    @SerializedName("ownerUUID")
    @Nullable
    private UUID ownerUUID;  // Gang-UUID oder Player-UUID

    @SerializedName("createdAt")
    private final long createdAt;

    @SerializedName("lastModified")
    private long lastModified;

    public Territory(int chunkX, int chunkZ, TerritoryType type, String name, @Nullable UUID ownerUUID) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.type = type;
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.createdAt = System.currentTimeMillis();
        this.lastModified = this.createdAt;
    }

    // Getters
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public TerritoryType getType() { return type; }
    public String getName() { return name; }
    @Nullable public UUID getOwnerUUID() { return ownerUUID; }
    public long getCreatedAt() { return createdAt; }
    public long getLastModified() { return lastModified; }

    // Setters
    public void setType(TerritoryType type) {
        this.type = type;
        this.lastModified = System.currentTimeMillis();
    }

    public void setName(String name) {
        this.name = name;
        this.lastModified = System.currentTimeMillis();
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.lastModified = System.currentTimeMillis();
    }

    /**
     * Gibt Chunk-Key zurück (für HashMap)
     */
    public long getChunkKey() {
        return getChunkKey(chunkX, chunkZ);
    }

    /**
     * Static helper für Chunk-Key
     */
    public static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    /**
     * Gibt formatierte Info zurück
     */
    public String getFormattedInfo() {
        return String.format(
            "%s§f %s\n" +
            "§7Chunk: (%d, %d)\n" +
            "§7Owner: %s",
            type.getFormattedName(),
            name,
            chunkX,
            chunkZ,
            ownerUUID != null ? ownerUUID.toString().substring(0, 8) : "Kein"
        );
    }

    @Override
    public String toString() {
        return String.format("Territory[%s, chunk=(%d,%d), type=%s]",
            name, chunkX, chunkZ, type);
    }
}
