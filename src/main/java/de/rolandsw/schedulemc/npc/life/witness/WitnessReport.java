package de.rolandsw.schedulemc.npc.life.witness;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * WitnessReport - Ein Zeugenbericht über ein beobachtetes Verbrechen
 *
 * Enthält alle Details die ein NPC über ein Verbrechen beobachtet hat.
 */
public class WitnessReport {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Eindeutige ID des Reports */
    private final UUID reportId;

    /** Wer wurde beim Verbrechen beobachtet? */
    private final UUID criminalUUID;

    /** Welcher NPC hat das Verbrechen beobachtet? */
    private final UUID witnessNPCUUID;

    /** Typ des Verbrechens */
    private final CrimeType crimeType;

    /** Wo fand das Verbrechen statt? */
    private final BlockPos location;

    /** Wann wurde das Verbrechen beobachtet? (Game Time in Ticks) */
    private final long timestamp;

    /** Spieltag des Verbrechens */
    private final long gameDay;

    /** Wurde das Verbrechen bereits gemeldet? */
    private boolean reported = false;

    /** Wurde der Zeuge bestochen? */
    private boolean bribed = false;

    /** Höhe der Bestechung */
    private int bribeAmount = 0;

    /** Optional: Opfer des Verbrechens */
    @Nullable
    private UUID victimUUID;

    /** Optional: Zusätzliche Details */
    private String details = "";

    /** Glaubwürdigkeit des Zeugen (0-100) */
    private float witnessCredibility = 100.0f;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public WitnessReport(UUID criminalUUID, UUID witnessNPCUUID, CrimeType crimeType,
                        BlockPos location, long timestamp, long gameDay) {
        this.reportId = UUID.randomUUID();
        this.criminalUUID = criminalUUID;
        this.witnessNPCUUID = witnessNPCUUID;
        this.crimeType = crimeType;
        this.location = location;
        this.timestamp = timestamp;
        this.gameDay = gameDay;
    }

    // ═══════════════════════════════════════════════════════════
    // REPORT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Markiert den Report als an die Polizei gemeldet
     */
    public void markAsReported() {
        this.reported = true;
    }

    /**
     * Markiert den Zeugen als bestochen
     */
    public void markAsBribed(int amount) {
        this.bribed = true;
        this.bribeAmount = amount;
    }

    /**
     * Prüft ob der Report noch gültig ist (nicht zu alt)
     */
    public boolean isValid(long currentGameDay) {
        // Reports sind für (Schwere * 2) Tage gültig
        long validDays = crimeType.getWantedDuration();
        return (currentGameDay - gameDay) <= validDays;
    }

    /**
     * Prüft ob der Report vor Gericht verwendet werden kann
     */
    public boolean isUsableAsEvidence() {
        if (bribed) return false;
        if (witnessCredibility < 50) return false;
        return !reported; // Nur einmal verwendbar
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULATED VALUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den Beweiswert dieses Reports
     */
    public float getEvidenceValue() {
        if (bribed) return 0.0f;

        float value = crimeType.getSeverity() * 10.0f;
        value *= (witnessCredibility / 100.0f);

        return value;
    }

    /**
     * Berechnet das Kopfgeld basierend auf diesem Report
     */
    public int calculateBounty(boolean repeatOffender) {
        if (bribed) return 0;
        return crimeType.calculateBounty(repeatOffender, false);
    }

    /**
     * Berechnet den minimalen Bestechungsbetrag
     */
    public int getMinimumBribeAmount() {
        // Basis: Kopfgeld * Glaubwürdigkeit * Schwere-Faktor
        int base = crimeType.getBaseBounty();
        float credibilityFactor = witnessCredibility / 100.0f;
        float severityFactor = 1.0f + (crimeType.getSeverity() / 10.0f);

        return (int) (base * credibilityFactor * severityFactor);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getReportId() {
        return reportId;
    }

    public UUID getCriminalUUID() {
        return criminalUUID;
    }

    public UUID getWitnessNPCUUID() {
        return witnessNPCUUID;
    }

    public CrimeType getCrimeType() {
        return crimeType;
    }

    public BlockPos getLocation() {
        return location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getGameDay() {
        return gameDay;
    }

    public boolean isReported() {
        return reported;
    }

    public boolean isBribed() {
        return bribed;
    }

    public int getBribeAmount() {
        return bribeAmount;
    }

    @Nullable
    public UUID getVictimUUID() {
        return victimUUID;
    }

    public void setVictimUUID(@Nullable UUID victimUUID) {
        this.victimUUID = victimUUID;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public float getWitnessCredibility() {
        return witnessCredibility;
    }

    public void setWitnessCredibility(float witnessCredibility) {
        this.witnessCredibility = Math.max(0, Math.min(100, witnessCredibility));
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ReportId", reportId);
        tag.putUUID("CriminalUUID", criminalUUID);
        tag.putUUID("WitnessNPCUUID", witnessNPCUUID);
        tag.putString("CrimeType", crimeType.name());
        tag.putInt("LocationX", location.getX());
        tag.putInt("LocationY", location.getY());
        tag.putInt("LocationZ", location.getZ());
        tag.putLong("Timestamp", timestamp);
        tag.putLong("GameDay", gameDay);
        tag.putBoolean("Reported", reported);
        tag.putBoolean("Bribed", bribed);
        tag.putInt("BribeAmount", bribeAmount);
        if (victimUUID != null) {
            tag.putUUID("VictimUUID", victimUUID);
        }
        tag.putString("Details", details);
        tag.putFloat("WitnessCredibility", witnessCredibility);
        return tag;
    }

    public static WitnessReport load(CompoundTag tag) {
        UUID criminalUUID = tag.getUUID("CriminalUUID");
        UUID witnessNPCUUID = tag.getUUID("WitnessNPCUUID");
        CrimeType crimeType = CrimeType.fromName(tag.getString("CrimeType"));
        BlockPos location = new BlockPos(
            tag.getInt("LocationX"),
            tag.getInt("LocationY"),
            tag.getInt("LocationZ")
        );
        long timestamp = tag.getLong("Timestamp");
        long gameDay = tag.getLong("GameDay");

        WitnessReport report = new WitnessReport(
            criminalUUID, witnessNPCUUID, crimeType, location, timestamp, gameDay
        );

        report.reported = tag.getBoolean("Reported");
        report.bribed = tag.getBoolean("Bribed");
        report.bribeAmount = tag.getInt("BribeAmount");
        if (tag.contains("VictimUUID")) {
            report.victimUUID = tag.getUUID("VictimUUID");
        }
        report.details = tag.getString("Details");
        report.witnessCredibility = tag.getFloat("WitnessCredibility");

        return report;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("WitnessReport{%s, day=%d, reported=%s, bribed=%s}",
            crimeType.getDisplayName(), gameDay, reported, bribed);
    }

    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Zeugenbericht ===\n");
        sb.append("Verbrechen: ").append(crimeType.getDisplayName()).append("\n");
        sb.append("Ort: ").append(location.toShortString()).append("\n");
        sb.append("Tag: ").append(gameDay).append("\n");
        sb.append("Schwere: ").append(crimeType.getSeverity()).append("/10\n");
        sb.append("Status: ").append(reported ? "Gemeldet" : "Nicht gemeldet");
        if (bribed) {
            sb.append(" (Bestochen: ").append(bribeAmount).append(")");
        }
        if (!details.isEmpty()) {
            sb.append("\nDetails: ").append(details);
        }
        return sb.toString();
    }
}
