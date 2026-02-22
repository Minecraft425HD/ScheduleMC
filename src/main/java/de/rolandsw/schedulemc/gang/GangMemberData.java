package de.rolandsw.schedulemc.gang;

import java.util.UUID;

/**
 * Daten eines Gang-Mitglieds.
 */
public class GangMemberData {

    private static final long FEE_INTERVAL_MS = 7L * 24 * 60 * 60 * 1000; // 7 Tage

    private final UUID playerUUID;
    private GangRank rank;
    private int contributedXP;
    private long joinTimestamp;
    private long lastFeePaid;
    private int missedFeePayments;

    public GangMemberData(UUID playerUUID, GangRank rank) {
        this.playerUUID = playerUUID;
        this.rank = rank;
        this.contributedXP = 0;
        this.joinTimestamp = System.currentTimeMillis();
        this.lastFeePaid = System.currentTimeMillis();
        this.missedFeePayments = 0;
    }

    /** Deserialisierung */
    public GangMemberData(UUID playerUUID, GangRank rank, int contributedXP, long joinTimestamp) {
        this(playerUUID, rank, contributedXP, joinTimestamp, joinTimestamp, 0);
    }

    /** Vollstaendige Deserialisierung */
    public GangMemberData(UUID playerUUID, GangRank rank, int contributedXP,
                          long joinTimestamp, long lastFeePaid, int missedFeePayments) {
        this.playerUUID = playerUUID;
        this.rank = rank;
        this.contributedXP = contributedXP;
        this.joinTimestamp = joinTimestamp;
        this.lastFeePaid = lastFeePaid > 0 ? lastFeePaid : joinTimestamp;
        this.missedFeePayments = missedFeePayments;
    }

    public UUID getPlayerUUID() { return playerUUID; }
    public GangRank getRank() { return rank; }
    public int getContributedXP() { return contributedXP; }
    public long getJoinTimestamp() { return joinTimestamp; }
    public long getLastFeePaid() { return lastFeePaid; }
    public int getMissedFeePayments() { return missedFeePayments; }

    public void setRank(GangRank rank) { this.rank = rank; }

    public void addContributedXP(int xp) {
        this.contributedXP += xp;
    }

    public void resetFeePaid() {
        this.lastFeePaid = System.currentTimeMillis();
        this.missedFeePayments = 0;
    }

    public void incrementMissedFeePayments() {
        this.missedFeePayments++;
    }

    /**
     * Berechnet den individuellen Wochenbeitrag basierend auf Rang.
     */
    public int calculateFee(int baseFee) {
        return (int) Math.ceil(baseFee * rank.getFeeMultiplier());
    }

    /**
     * Prueft ob der Wochenbeitrag faellig ist (7 Tage seit letzter Zahlung).
     */
    public boolean isFeeDue() {
        return System.currentTimeMillis() - lastFeePaid >= FEE_INTERVAL_MS;
    }
}
