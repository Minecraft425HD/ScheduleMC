package de.rolandsw.schedulemc.economy;

import com.google.gson.annotations.SerializedName;
import de.rolandsw.schedulemc.util.UUIDHelper;

import java.util.UUID;

/**
 * Repräsentiert einen Kredit
 */
public class Loan {
    /** Toleranz für Float-Vergleich beim Abbezahlen (vermeidet 0.00000001-Restbeträge). */
    private static final double REPAYMENT_TOLERANCE = 0.01;

    @SerializedName("id")
    private final String loanId;

    @SerializedName("playerUUID")
    private final UUID playerUUID;

    @SerializedName("type")
    private final LoanType type;

    @SerializedName("principal")
    private final double principal; // Kreditsumme

    @SerializedName("interestRate")
    private final double interestRate; // Zinssatz

    @SerializedName("durationDays")
    private final int durationDays;

    @SerializedName("startDay")
    private final long startDay;

    @SerializedName("remaining")
    private double remaining; // Verbleibender Betrag

    @SerializedName("dailyPayment")
    private final double dailyPayment;

    public Loan(UUID playerUUID, LoanType type, long currentDay) {
        // OPTIMIERT: UUIDHelper vermeidet redundante toString() Aufrufe
        this.loanId = UUIDHelper.randomUUIDString();
        this.playerUUID = playerUUID;
        this.type = type;
        this.principal = type.getAmount();
        this.interestRate = type.getInterestRate();
        this.durationDays = type.getDurationDays();
        this.startDay = currentDay;

        // Berechne Gesamtbetrag mit Zinsen
        double totalWithInterest = principal * (1 + interestRate);
        this.remaining = totalWithInterest;
        this.dailyPayment = totalWithInterest / durationDays;
    }

    public String getLoanId() { return loanId; }
    public UUID getPlayerUUID() { return playerUUID; }
    public LoanType getType() { return type; }
    public double getPrincipal() { return principal; }
    public double getInterestRate() { return interestRate; }
    public int getDurationDays() { return durationDays; }
    public long getStartDay() { return startDay; }
    public double getRemaining() { return remaining; }
    public double getDailyPayment() { return dailyPayment; }

    /**
     * Zahlt tägliche Rate
     */
    public boolean payDailyInstallment() {
        if (remaining <= 0) {
            return false;
        }

        double payment = Math.min(dailyPayment, remaining);
        remaining -= payment;
        return true;
    }

    /**
     * Zahlt Kredit vorzeitig zurück
     */
    public double payOff() {
        double amount = remaining;
        remaining = 0;
        return amount;
    }

    /**
     * Ist Kredit abbezahlt?
     */
    public boolean isRepaid() {
        return remaining <= REPAYMENT_TOLERANCE;
    }

    /**
     * Verbleibende Tage
     */
    public int getRemainingDays(long currentDay) {
        long elapsed = currentDay - startDay;
        return Math.max(0, durationDays - (int)elapsed);
    }

    public enum LoanType {
        SMALL(5000, 0.10, 14),      // 5k, 10%, 2 Wochen
        MEDIUM(25000, 0.15, 28),    // 25k, 15%, 4 Wochen
        LARGE(100000, 0.20, 56);    // 100k, 20%, 8 Wochen

        private final double amount;
        private final double interestRate;
        private final int durationDays;

        LoanType(double amount, double interestRate, int durationDays) {
            this.amount = amount;
            this.interestRate = interestRate;
            this.durationDays = durationDays;
        }

        public double getAmount() { return amount; }
        public double getInterestRate() { return interestRate; }
        public int getDurationDays() { return durationDays; }
    }
}
