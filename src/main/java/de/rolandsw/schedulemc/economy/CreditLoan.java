package de.rolandsw.schedulemc.economy;
nimport de.rolandsw.schedulemc.util.StringUtils;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Erweitertes Kredit-System mit 4 Kredittypen und Bonitätsprüfung
 * Ersetzt das alte Loan-System mit dynamischen Zinsen basierend auf Kredit-Score
 */
public class CreditLoan {

    @SerializedName("id")
    private final String loanId;

    @SerializedName("playerUUID")
    private final UUID playerUUID;

    @SerializedName("type")
    private final CreditLoanType type;

    @SerializedName("principal")
    private final double principal; // Kreditsumme

    @SerializedName("effectiveInterestRate")
    private final double effectiveInterestRate; // Effektiver Zinssatz (nach Score-Anpassung)

    @SerializedName("durationDays")
    private final int durationDays;

    @SerializedName("startDay")
    private final long startDay;

    @SerializedName("remaining")
    private double remaining; // Verbleibender Betrag

    @SerializedName("dailyPayment")
    private final double dailyPayment;

    @SerializedName("totalInterest")
    private final double totalInterest; // Gesamte Zinsen

    @SerializedName("guarantorUUID")
    private UUID guarantorUUID; // Optional: Bürge

    /**
     * Erstellt einen neuen Kredit mit dynamischem Zinssatz
     *
     * @param playerUUID Spieler-UUID
     * @param type Kredittyp
     * @param effectiveInterestRate Effektiver Zinssatz (nach Score-Anpassung)
     * @param currentDay Aktueller Spieltag
     */
    public CreditLoan(UUID playerUUID, CreditLoanType type, double effectiveInterestRate, long currentDay) {
        this.loanId = UUID.randomUUID().toString();
        this.playerUUID = playerUUID;
        this.type = type;
        this.principal = type.getBaseAmount();
        this.effectiveInterestRate = effectiveInterestRate;
        this.durationDays = type.getDurationDays();
        this.startDay = currentDay;

        // Berechne Gesamtbetrag mit Zinsen
        this.totalInterest = principal * effectiveInterestRate;
        double totalWithInterest = principal + totalInterest;
        this.remaining = totalWithInterest;
        this.dailyPayment = totalWithInterest / durationDays;
    }

    /**
     * Erstellt einen neuen Kredit mit Basis-Zinssatz (ohne Score-Anpassung)
     * Für Abwärtskompatibilität
     */
    public CreditLoan(UUID playerUUID, CreditLoanType type, long currentDay) {
        this(playerUUID, type, type.getBaseInterestRate(), currentDay);
    }

    // ========== Kredit-Operationen ==========

    /**
     * Zahlt tägliche Rate
     * @return true wenn Zahlung erfolgreich, false wenn Kredit bereits abbezahlt
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
     * @return Der zurückgezahlte Betrag
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
        return remaining <= 0.01; // Float precision
    }

    /**
     * Verbleibende Tage
     */
    public int getRemainingDays(long currentDay) {
        long elapsed = currentDay - startDay;
        return Math.max(0, durationDays - (int)elapsed);
    }

    /**
     * Fortschritt in Prozent (0-100)
     */
    public int getProgressPercent() {
        double total = principal + totalInterest;
        double paid = total - remaining;
        return (int) ((paid / total) * 100);
    }

    // ========== Getters ==========

    public String getLoanId() { return loanId; }
    public UUID getPlayerUUID() { return playerUUID; }
    public CreditLoanType getType() { return type; }
    public double getPrincipal() { return principal; }
    public double getEffectiveInterestRate() { return effectiveInterestRate; }
    public int getDurationDays() { return durationDays; }
    public long getStartDay() { return startDay; }
    public double getRemaining() { return remaining; }
    public double getDailyPayment() { return dailyPayment; }
    public double getTotalInterest() { return totalInterest; }
    public UUID getGuarantorUUID() { return guarantorUUID; }

    public void setGuarantorUUID(UUID guarantorUUID) {
        this.guarantorUUID = guarantorUUID;
    }

    /**
     * Kredittypen mit erweiterten Optionen
     */
    public enum CreditLoanType {
        // Name, Basis-Betrag, Basis-Zinsen, Laufzeit, Min-Rating, Beschreibung DE, Beschreibung EN
        STARTER(
            "Starter-Kredit",
            "Starter Loan",
            5000,
            0.08,
            14,
            CreditScore.CreditRating.C,
            "Ideal für Einsteiger"
        ),
        STANDARD(
            "Standard-Kredit",
            "Standard Loan",
            25000,
            0.12,
            28,
            CreditScore.CreditRating.BB,
            "Für mittlere Investitionen"
        ),
        PREMIUM(
            "Premium-Kredit",
            "Premium Loan",
            100000,
            0.15,
            56,
            CreditScore.CreditRating.BBB,
            "Für größere Projekte"
        ),
        VIP(
            "VIP-Kredit",
            "VIP Loan",
            500000,
            0.10,
            90,
            CreditScore.CreditRating.A,
            "Exklusiv für Top-Kunden"
        );

        private final String displayNameDE;
        private final String displayNameEN;
        private final double baseAmount;
        private final double baseInterestRate;
        private final int durationDays;
        private final CreditScore.CreditRating requiredRating;
        private final String description;

        CreditLoanType(String displayNameDE, String displayNameEN, double baseAmount,
                       double baseInterestRate, int durationDays,
                       CreditScore.CreditRating requiredRating, String description) {
            this.displayNameDE = displayNameDE;
            this.displayNameEN = displayNameEN;
            this.baseAmount = baseAmount;
            this.baseInterestRate = baseInterestRate;
            this.durationDays = durationDays;
            this.requiredRating = requiredRating;
            this.description = description;
        }

        public String getDisplayNameDE() { return displayNameDE; }
        public String getDisplayNameEN() { return displayNameEN; }
        public double getBaseAmount() { return baseAmount; }
        public double getBaseInterestRate() { return baseInterestRate; }
        public int getDurationDays() { return durationDays; }
        public CreditScore.CreditRating getRequiredRating() { return requiredRating; }
        public String getDescription() { return description; }

        /**
         * Gibt den Display-Name basierend auf der Client-Locale zurück
         */
        public String getDisplayName() {
            try {
                return de.rolandsw.schedulemc.util.LocaleHelper.selectClientLocalized(displayNameDE, displayNameEN);
            } catch (NullPointerException e) {
                // Client context not available (server-side) - use German default
                return displayNameDE;
            } catch (IllegalStateException e) {
                // Locale helper not initialized - use German default
                return displayNameDE;
            } catch (Exception e) {
                // Fallback for unexpected localization errors
                return displayNameDE;
            }
        }

        /**
         * Formatiert den Zinssatz für die Anzeige
         */
        public String getInterestRateString() {
            return String.format("%.0f%%", baseInterestRate * 100);
        }

        /**
         * Formatiert die tägliche Rate für die Anzeige
         */
        public String getDailyPaymentString() {
            double totalWithInterest = baseAmount * (1 + baseInterestRate);
            double daily = totalWithInterest / durationDays;
            return StringUtils.formatMoney(daily);
        }
    }
}
