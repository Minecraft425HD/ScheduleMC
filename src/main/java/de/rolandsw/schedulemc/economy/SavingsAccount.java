package de.rolandsw.schedulemc.economy;

import com.google.gson.annotations.SerializedName;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.UUIDHelper;

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

    @SerializedName("createdDay")
    private final long createdDay;

    @SerializedName("lastInterestDay")
    private long lastInterestDay;

    public SavingsAccount(UUID playerUUID, double initialDeposit, long currentDay) {
        // OPTIMIERT: UUIDHelper vermeidet redundante toString() Aufrufe
        this.accountId = UUIDHelper.randomUUIDString();
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
        int lockPeriodWeeks = ModConfigHandler.COMMON.SAVINGS_LOCK_PERIOD_WEEKS.get();
        return daysElapsed >= (lockPeriodWeeks * 7);
    }

    /**
     * Gibt verbleibende Tage bis Entsperrung zurück
     */
    public int getDaysUntilUnlock(long currentDay) {
        if (isUnlocked(currentDay)) {
            return 0;
        }
        long daysElapsed = currentDay - createdDay;
        int lockPeriodWeeks = ModConfigHandler.COMMON.SAVINGS_LOCK_PERIOD_WEEKS.get();
        return (int) ((lockPeriodWeeks * 7) - daysElapsed);
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

        // Wenn gesperrt und erzwungen: Strafe aus Config
        if (!isUnlocked(currentDay) && forcedWithdrawal) {
            double penaltyRate = ModConfigHandler.COMMON.SAVINGS_EARLY_WITHDRAWAL_PENALTY.get();
            double penalty = amount * penaltyRate;
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
            double interestRate = ModConfigHandler.COMMON.SAVINGS_INTEREST_RATE.get();
            double interest = balance * interestRate;
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
            // Frühe Schließung: Strafe aus Config
            double penaltyRate = ModConfigHandler.COMMON.SAVINGS_EARLY_WITHDRAWAL_PENALTY.get();
            double penalty = balance * penaltyRate;
            double remaining = balance - penalty;
            balance = 0;
            return remaining;
        }

        double remaining = balance;
        balance = 0;
        return remaining;
    }

    public static double getInterestRate() {
        return ModConfigHandler.COMMON.SAVINGS_INTEREST_RATE.get();
    }

    public static int getLockPeriodWeeks() {
        return ModConfigHandler.COMMON.SAVINGS_LOCK_PERIOD_WEEKS.get();
    }
}
