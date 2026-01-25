package de.rolandsw.schedulemc.npc.crime;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Repräsentiert ein Kopfgeld auf einen Spieler
 */
public class BountyData {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());

    @SerializedName("bountyId")
    private final String bountyId;

    @SerializedName("targetUUID")
    private final UUID targetUUID;

    @SerializedName("amount")
    private double amount;

    @SerializedName("placedBy")
    @Nullable
    private final UUID placedBy;           // null = automatic (police)

    @SerializedName("reason")
    private final String reason;

    @SerializedName("timestamp")
    private final long timestamp;

    @SerializedName("expiresAt")
    private long expiresAt;                 // 0 = never expires

    @SerializedName("claimed")
    private boolean claimed;

    @SerializedName("claimedBy")
    @Nullable
    private UUID claimedBy;

    @SerializedName("claimedAt")
    private long claimedAt;

    public BountyData(UUID targetUUID, double amount, @Nullable UUID placedBy, String reason) {
        this.bountyId = UUID.randomUUID().toString();
        this.targetUUID = targetUUID;
        this.amount = amount;
        this.placedBy = placedBy;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.expiresAt = 0; // Never expires by default
        this.claimed = false;
        this.claimedBy = null;
        this.claimedAt = 0;
    }

    // Getters
    public String getBountyId() { return bountyId; }
    public UUID getTargetUUID() { return targetUUID; }
    public double getAmount() { return amount; }
    @Nullable public UUID getPlacedBy() { return placedBy; }
    public String getReason() { return reason; }
    public long getTimestamp() { return timestamp; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isClaimed() { return claimed; }
    @Nullable public UUID getClaimedBy() { return claimedBy; }
    public long getClaimedAt() { return claimedAt; }

    /**
     * Setzt Ablaufdatum (in Millisekunden)
     */
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Erhöht Kopfgeld
     */
    public void increaseAmount(double additionalAmount) {
        this.amount += additionalAmount;
    }

    /**
     * Markiert Kopfgeld als eingelöst
     */
    public boolean claim(UUID claimerUUID) {
        if (claimed) {
            return false;
        }
        this.claimed = true;
        this.claimedBy = claimerUUID;
        this.claimedAt = System.currentTimeMillis();
        return true;
    }

    /**
     * Prüft ob Bounty abgelaufen ist
     */
    public boolean isExpired() {
        if (expiresAt == 0) {
            return false; // Never expires
        }
        return System.currentTimeMillis() > expiresAt;
    }

    /**
     * Prüft ob Bounty aktiv ist (nicht claimed und nicht expired)
     */
    public boolean isActive() {
        return !claimed && !isExpired();
    }

    /**
     * Gibt formatiertes Datum zurück
     */
    public String getFormattedDate() {
        return FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    /**
     * Gibt formatiertes Ablauf-Datum zurück
     */
    public String getFormattedExpiresAt() {
        if (expiresAt == 0) {
            return "§7Nie";
        }
        return FORMATTER.format(Instant.ofEpochMilli(expiresAt));
    }

    /**
     * Gibt Auftraggeber-String zurück (ohne Spielername-Auflösung)
     */
    public String getPlacedByString() {
        if (placedBy == null) {
            return "§6Polizei (Automatisch)";
        }
        return "§eSpieler (UUID: " + placedBy.toString().substring(0, 8) + "...)";
    }

    /**
     * Gibt Auftraggeber-String zurück (mit Spielername-Auflösung)
     */
    public String getPlacedByString(@Nullable net.minecraft.server.MinecraftServer server) {
        if (placedBy == null) {
            return "§6Polizei (Automatisch)";
        }
        if (server != null) {
            var gameProfile = server.getProfileCache().get(placedBy).orElse(null);
            if (gameProfile != null) {
                return "§e" + gameProfile.getName();
            }
            var onlinePlayer = server.getPlayerList().getPlayer(placedBy);
            if (onlinePlayer != null) {
                return "§e" + onlinePlayer.getName().getString();
            }
        }
        return "§eSpieler (UUID: " + placedBy.toString().substring(0, 8) + "...)";
    }

    /**
     * Gibt formatierte Beschreibung zurück
     */
    public String getFormattedDescription() {
        String status = claimed ? "§c✗ Eingelöst" : (isExpired() ? "§7⏱ Abgelaufen" : "§a✓ Aktiv");

        return String.format(
            "§6§l💰 KOPFGELD 💰\n" +
            "§7ID: §f%s\n" +
            "§7Betrag: §a%.2f€\n" +
            "§7Grund: §e%s\n" +
            "§7Platziert von: %s\n" +
            "§7Datum: §f%s\n" +
            "§7Ablauf: §f%s\n" +
            "§7Status: %s",
            bountyId.substring(0, 8),
            amount,
            reason,
            getPlacedByString(),
            getFormattedDate(),
            getFormattedExpiresAt(),
            status
        );
    }

    @Override
    public String toString() {
        return String.format("BountyData[%s, target=%s, amount=%.2f, active=%s]",
            bountyId, targetUUID, amount, isActive());
    }
}
