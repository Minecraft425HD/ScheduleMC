package de.rolandsw.schedulemc.economy;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.chat.Component;

import java.util.UUID;

/**
 * Repräsentiert die Kreditwürdigkeit (Bonität) eines Spielers
 *
 * Kredit-Score basiert auf:
 * - Spielzeit (Tage seit erstem Login)
 * - Kontostand (durchschnittlich)
 * - Kredit-Historie (erfolgreiche Rückzahlungen)
 * - Zahlungsausfälle
 */
public class CreditScore {

    @SerializedName("playerUUID")
    private final UUID playerUUID;

    @SerializedName("totalLoansCompleted")
    private int totalLoansCompleted;

    @SerializedName("totalLoansDefaulted")
    private int totalLoansDefaulted;

    @SerializedName("missedPayments")
    private int missedPayments;

    @SerializedName("onTimePayments")
    private int onTimePayments;

    @SerializedName("firstLoginDay")
    private long firstLoginDay;

    @SerializedName("totalAmountRepaid")
    private double totalAmountRepaid;

    @SerializedName("averageBalance")
    private double averageBalance;

    @SerializedName("balanceSamples")
    private int balanceSamples;

    public CreditScore(UUID playerUUID, long currentDay) {
        this.playerUUID = playerUUID;
        this.totalLoansCompleted = 0;
        this.totalLoansDefaulted = 0;
        this.missedPayments = 0;
        this.onTimePayments = 0;
        this.firstLoginDay = currentDay;
        this.totalAmountRepaid = 0;
        this.averageBalance = 0;
        this.balanceSamples = 0;
    }

    /**
     * Berechnet den Kredit-Score (0-1000 Punkte)
     *
     * Komponenten:
     * - Spielzeit: max 200 Punkte (1 Punkt pro Tag, max 200)
     * - Kontostand: max 200 Punkte (1 Punkt pro 500€, max 200)
     * - Erfolgreiche Kredite: max 300 Punkte (50 pro Kredit, max 6)
     * - Zahlungspünktlichkeit: max 200 Punkte
     * - Ausfälle/Verpasste Zahlungen: Abzüge bis -100 Punkte
     */
    public int calculateScore(long currentDay) {
        int score = 0;

        // 1. Spielzeit-Bonus (max 200 Punkte)
        long daysPlayed = currentDay - firstLoginDay;
        int playtimeBonus = (int) Math.min(200, daysPlayed);
        score += playtimeBonus;

        // 2. Kontostand-Bonus (max 200 Punkte)
        int balanceBonus = (int) Math.min(200, averageBalance / 500);
        score += balanceBonus;

        // 3. Erfolgreiche Kredite (max 300 Punkte)
        int loanBonus = Math.min(300, totalLoansCompleted * 50);
        score += loanBonus;

        // 4. Zahlungspünktlichkeit (max 200 Punkte)
        int totalPayments = onTimePayments + missedPayments;
        if (totalPayments > 0) {
            double onTimeRate = (double) onTimePayments / totalPayments;
            int punctualityBonus = (int) Math.round(onTimeRate * 200);
            score += punctualityBonus;
        } else {
            // Keine Zahlungshistorie = neutraler Bonus
            score += 100;
        }

        // 5. Abzüge für Ausfälle
        int defaultPenalty = totalLoansDefaulted * 100;
        score -= defaultPenalty;

        // 6. Abzüge für verpasste Zahlungen
        int missedPenalty = missedPayments * 10;
        score -= missedPenalty;

        // Score zwischen 0 und 1000 begrenzen
        return Math.max(0, Math.min(1000, score));
    }

    /**
     * Gibt das Kredit-Rating zurück (AAA bis D)
     */
    public CreditRating getRating(long currentDay) {
        int score = calculateScore(currentDay);

        if (score >= 900) return CreditRating.AAA;
        if (score >= 800) return CreditRating.AA;
        if (score >= 700) return CreditRating.A;
        if (score >= 600) return CreditRating.BBB;
        if (score >= 500) return CreditRating.BB;
        if (score >= 400) return CreditRating.B;
        if (score >= 300) return CreditRating.CCC;
        if (score >= 200) return CreditRating.CC;
        if (score >= 100) return CreditRating.C;
        return CreditRating.D;
    }

    /**
     * Berechnet den Zinssatz-Modifikator basierend auf dem Score
     * Besserer Score = niedrigere Zinsen
     *
     * @return Multiplikator (0.5 bis 1.5)
     */
    public double getInterestRateModifier(long currentDay) {
        int score = calculateScore(currentDay);

        // Score 1000 = 0.5x Zinsen (50% Rabatt)
        // Score 500 = 1.0x Zinsen (normal)
        // Score 0 = 1.5x Zinsen (50% Aufschlag)
        return 1.5 - (score / 1000.0);
    }

    /**
     * Gibt den maximalen Kreditbetrag zurück, den der Spieler aufnehmen kann
     * Basiert auf dem Kredit-Score
     */
    public double getMaxLoanAmount(long currentDay) {
        CreditRating rating = getRating(currentDay);
        return rating.getMaxLoanAmount();
    }

    // ========== Update Methoden ==========

    public void recordOnTimePayment() {
        onTimePayments++;
    }

    public void recordMissedPayment() {
        missedPayments++;
    }

    public void recordLoanCompleted(double amountRepaid) {
        totalLoansCompleted++;
        totalAmountRepaid += amountRepaid;
    }

    public void recordLoanDefaulted() {
        totalLoansDefaulted++;
    }

    public void updateAverageBalance(double currentBalance) {
        // Gleitender Durchschnitt
        balanceSamples++;
        averageBalance = ((averageBalance * (balanceSamples - 1)) + currentBalance) / balanceSamples;
    }

    // ========== Getters ==========

    public UUID getPlayerUUID() { return playerUUID; }
    public int getTotalLoansCompleted() { return totalLoansCompleted; }
    public int getTotalLoansDefaulted() { return totalLoansDefaulted; }
    public int getMissedPayments() { return missedPayments; }
    public int getOnTimePayments() { return onTimePayments; }
    public long getFirstLoginDay() { return firstLoginDay; }
    public double getTotalAmountRepaid() { return totalAmountRepaid; }
    public double getAverageBalance() { return averageBalance; }

    /**
     * Gibt die Anzahl der Sterne (1-5) für die GUI-Anzeige zurück
     */
    public int getStars(long currentDay) {
        CreditRating rating = getRating(currentDay);
        return rating.getStars();
    }

    /**
     * Kredit-Rating Enum
     */
    public enum CreditRating {
        AAA(5, 500000, 0xFFD700),  // Gold
        AA(5, 250000, 0x00FF00),   // Grün
        A(4, 150000, 0x55FF55),    // Hellgrün
        BBB(4, 100000, 0xAAAA00),  // Olive
        BB(3, 75000, 0xFFFF00),    // Gelb
        B(3, 50000, 0xFFAA00),     // Orange
        CCC(2, 25000, 0xFF5500),   // Dunkelorange
        CC(2, 10000, 0xFF0000),    // Rot
        C(1, 5000, 0xAA0000),      // Dunkelrot
        D(1, 0, 0x550000);         // Sehr dunkelrot (kein Kredit möglich)

        private final int stars;
        private final double maxLoanAmount;
        private final int color;

        CreditRating(int stars, double maxLoanAmount, int color) {
            this.stars = stars;
            this.maxLoanAmount = maxLoanAmount;
            this.color = color;
        }

        public String getDisplayName() {
            return Component.translatable("enum.credit_rating." + this.name().toLowerCase()).getString();
        }
        public int getStars() { return stars; }
        public double getMaxLoanAmount() { return maxLoanAmount; }
        public int getColor() { return color; }

        public String getStarsString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                sb.append(i < stars ? "\u2605" : "\u2606"); // ★ oder ☆
            }
            return sb.toString();
        }
    }
}
