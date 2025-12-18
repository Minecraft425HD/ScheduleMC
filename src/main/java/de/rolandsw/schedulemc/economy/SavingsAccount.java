package de.rolandsw.schedulemc.economy;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Sparkonto mit höheren Zinsen aber Sperrfrist
 */
public class SavingsAccount {
    @SerializedName("id")
    private final String accountId;

    @SerializedName("playerUUID")
    private final UUID playerUUID;

    @SerializedName("balance")
    private double balance;

    @SerializedName("interestRate")
    private static final double INTEREST_RATE = 0.05; // 5% pro Woche

    @SerializedName("lockPeriodWeeks")
    private static final int LOCK_PERIOD_WEEKS = 4; // 4 Wochen gesperrt

    @SerializedName("createdDay")
    private final long createdDay;

    @SerializedName("lastInterestDay")
    private long lastInterestDay;

    public SavingsAccount(UUID playerUUID, double initialDeposit, long currentDay) {
        this.accountId = UUID.randomUUID().toString();
        this.playerUUID = playerUUID;
        this.balance = initialDeposit;
        this.createdDay = currentDay;
        this.lastInterestDay = currentDay;
    }

    public String getAccountId() { return accountId; }
    public UUID getPlayerUUID() { return playerUUID; }
    public double getBalance() { return balance; }
    public long getCreatedDay() { return createdDay; }

    /**
     * Prüft ob Konto entsperrt ist
     */
    public boolean isUnlocked(long currentDay) {
        long daysElapsed = currentDay - createdDay;
        return daysElapsed >= (LOCK_PERIOD_WEEKS * 7);
    }

    /**
     * Gibt verbleibende Tage bis Entsperrung zurück
     */
    public int getDaysUntilUnlock(long currentDay) {
        if (isUnlocked(currentDay)) {
            return 0;
        }
        long daysElapsed = currentDay - createdDay;
        return (int) ((LOCK_PERIOD_WEEKS * 7) - daysElapsed);
    }

    /**
     * Zahlt Geld ein
     */
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    /**
     * Hebt Geld ab (nur wenn entsperrt oder mit Strafe)
     */
    public boolean withdraw(double amount, long currentDay, boolean forcedWithdrawal) {
        if (amount <= 0 || amount > balance) {
            return false;
        }

        // Wenn gesperrt und erzwungen: 10% Strafe
        if (!isUnlocked(currentDay) && forcedWithdrawal) {
            double penalty = amount * 0.10;
            balance -= amount;
            return true; // Strafe wird extern verarbeitet
        }

        // Normal entsperrt
        if (isUnlocked(currentDay)) {
            balance -= amount;
            return true;
        }

        return false;
    }

    /**
     * Berechnet und zahlt wöchentliche Zinsen
     */
    public double calculateAndPayInterest(long currentDay) {
        long daysSinceLastInterest = currentDay - lastInterestDay;

        if (daysSinceLastInterest >= 7) {
            double interest = balance * INTEREST_RATE;
            balance += interest;
            lastInterestDay = currentDay;
            return interest;
        }

        return 0.0;
    }

    /**
     * Schließt das Konto und gibt Balance zurück
     */
    public double close(long currentDay) {
        if (!isUnlocked(currentDay)) {
            // Frühe Schließung: 10% Strafe
            double penalty = balance * 0.10;
            double remaining = balance - penalty;
            balance = 0;
            return remaining;
        }

        double remaining = balance;
        balance = 0;
        return remaining;
    }

    public static double getInterestRate() {
        return INTEREST_RATE;
    }

    public static int getLockPeriodWeeks() {
        return LOCK_PERIOD_WEEKS;
    }
}
