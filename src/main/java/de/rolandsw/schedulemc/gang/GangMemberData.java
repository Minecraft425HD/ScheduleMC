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
        // lastFeePaid als "unbekannt" (0) übergeben → Fallback im Voll-Konstruktor greift (GM-7).
        this(playerUUID, rank, contributedXP, joinTimestamp, 0, 0);
    }

    /** Vollstaendige Deserialisierung */
    public GangMemberData(UUID playerUUID, GangRank rank, int contributedXP,
                          long joinTimestamp, long lastFeePaid, int missedFeePayments) {
        this.playerUUID = playerUUID;
        this.rank = rank;
        this.contributedXP = contributedXP;
        this.joinTimestamp = joinTimestamp;
        // GM-7: Fehlt lastFeePaid (alter Save, 0), NICHT auf joinTimestamp zurückfallen — sonst
        // wäre der Beitrag für monatealte Mitglieder sofort fällig. Frisches Intervall ab jetzt.
        this.lastFeePaid = lastFeePaid > 0 ? lastFeePaid : System.currentTimeMillis();
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
        if (xp <= 0) return;
        this.contributedXP = (int) Math.min((long) this.contributedXP + xp, Integer.MAX_VALUE);
    }

    public void resetFeePaid() {
        this.lastFeePaid = System.currentTimeMillis();
        this.missedFeePayments = 0;
    }

    public void incrementMissedFeePayments() {
        this.missedFeePayments++;
    }

    /**
     * Wertet einen fälligen, aber nicht gezahlten Beitrag als verpasst (GM-2): erhöht den
     * Zähler UND stellt die Beitrags-Uhr um ein Intervall weiter. Dadurch zählt ein Miss pro
     * Fälligkeits-Intervall (statt pro Einzug-Zyklus alle 60 s) — für Online- wie Offline-Spieler.
     * Aufeinanderfolgende Einzüge holen mehrere überfällige Intervalle nach und nach auf.
     */
    public void markFeeMissed() {
        this.missedFeePayments++;
        this.lastFeePaid += FEE_INTERVAL_MS;
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
