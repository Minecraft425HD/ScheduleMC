package de.rolandsw.schedulemc.npc.crime;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Repräsentiert einen einzelnen Crime-Eintrag
 * Permanente Historie (bleibt auch nach Strafe)
 */
public class CrimeRecord {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());

    @SerializedName("recordId")
    private final String recordId;

    @SerializedName("playerUUID")
    private final UUID playerUUID;

    @SerializedName("type")
    private final CrimeType type;

    @SerializedName("timestamp")
    private final long timestamp;

    @SerializedName("location")
    @Nullable
    private final BlockPos location;

    @SerializedName("wantedLevelAdded")
    private final int wantedLevelAdded;

    @SerializedName("fineAmount")
    private final double fineAmount;

    @SerializedName("prisonDays")
    private final int prisonDays;

    @SerializedName("served")
    private boolean served;                 // Strafe abgesessen?

    @SerializedName("servedTimestamp")
    private long servedTimestamp;           // Wann abgesessen?

    public CrimeRecord(UUID playerUUID, CrimeType type, @Nullable BlockPos location) {
        this.recordId = UUID.randomUUID().toString();
        this.playerUUID = playerUUID;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.location = location;
        this.wantedLevelAdded = type.getWantedStars();
        this.fineAmount = type.getFine();
        this.prisonDays = type.getPrisonDays();
        this.served = false;
        this.servedTimestamp = 0;
    }

    // Getters
    public String getRecordId() { return recordId; }
    public UUID getPlayerUUID() { return playerUUID; }
    public CrimeType getType() { return type; }
    public long getTimestamp() { return timestamp; }
    @Nullable public BlockPos getLocation() { return location; }
    public int getWantedLevelAdded() { return wantedLevelAdded; }
    public double getFineAmount() { return fineAmount; }
    public int getPrisonDays() { return prisonDays; }
    public boolean isServed() { return served; }
    public long getServedTimestamp() { return servedTimestamp; }

    /**
     * Markiert Strafe als abgesessen
     */
    public void markServed() {
        this.served = true;
        this.servedTimestamp = System.currentTimeMillis();
    }

    /**
     * Gibt formatiertes Datum zurück
     */
    public String getFormattedDate() {
        return FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    /**
     * Gibt formatiertes Served-Datum zurück
     */
    public String getFormattedServedDate() {
        if (!served) return "§cNicht abgesessen";
        return FORMATTER.format(Instant.ofEpochMilli(servedTimestamp));
    }

    /**
     * Gibt formatierte Location zurück
     */
    public String getFormattedLocation() {
        if (location == null) return "§7Unbekannt";
        return String.format("§7(%d, %d, %d)", location.getX(), location.getY(), location.getZ());
    }

    /**
     * Gibt formatierte Beschreibung zurück
     */
    public String getFormattedDescription() {
        String statusColor = served ? "§a" : "§c";
        String status = served ? "✓ Abgesessen" : "✗ Offen";

        return String.format(
            "§7[%s] %s%s\n" +
            "  §7Ort: %s\n" +
            "  §7Strafe: §c%.2f€ §7/ §e%d Tage\n" +
            "  §7Status: %s%s",
            getFormattedDate(),
            type.getColorCode(),
            type.getDisplayName(),
            getFormattedLocation(),
            fineAmount,
            prisonDays,
            statusColor,
            status
        );
    }

    @Override
    public String toString() {
        return String.format("CrimeRecord[%s, %s, %s, served=%s]",
            recordId, type, getFormattedDate(), served);
    }
}
