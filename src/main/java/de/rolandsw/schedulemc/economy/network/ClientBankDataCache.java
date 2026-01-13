package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side cache for bank data received from server
 * Stores ALL bank-related data needed by ATM and Banker screens
 */
public class ClientBankDataCache {
    // Girokonto
    private static double balance = 0.0;
    private static List<Transaction> transactions = new ArrayList<>();
    private static double totalIncome = 0.0;
    private static double totalExpenses = 0.0;

    // Bargeld (Wallet)
    private static double walletBalance = 0.0;

    // Sparkonto
    private static double savingsBalance = 0.0;

    // Transfer-Limit
    private static double remainingTransferLimit = 0.0;
    private static double maxTransferLimit = 0.0;

    // Daueraufträge
    private static List<RecurringPaymentData> recurringPayments = new ArrayList<>();

    // Kredit
    private static CreditLoanData activeLoan = null;

    // Status
    private static boolean hasData = false;
    private static long lastUpdateTime = 0;

    // ═══════════════════════════════════════════════════════════════════════════
    // Update Methods (called from packets)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Update basic bank data (legacy method for compatibility)
     */
    public static void updateData(double newBalance, List<Transaction> newTransactions,
                                  double newTotalIncome, double newTotalExpenses) {
        balance = newBalance;
        transactions = new ArrayList<>(newTransactions);
        totalIncome = newTotalIncome;
        totalExpenses = newTotalExpenses;
        hasData = true;
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Update ATM-specific data (balance and wallet)
     */
    public static void updateATMData(double newBalance, double newWalletBalance) {
        balance = newBalance;
        walletBalance = newWalletBalance;
        hasData = true;
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Update full bank data (all fields)
     */
    public static void updateFullData(double newBalance, double newWalletBalance,
                                      double newSavingsBalance, double newRemainingLimit,
                                      double newMaxLimit, List<Transaction> newTransactions,
                                      List<RecurringPaymentData> newRecurringPayments,
                                      CreditLoanData newActiveLoan) {
        balance = newBalance;
        walletBalance = newWalletBalance;
        savingsBalance = newSavingsBalance;
        remainingTransferLimit = newRemainingLimit;
        maxTransferLimit = newMaxLimit;
        transactions = new ArrayList<>(newTransactions);
        recurringPayments = new ArrayList<>(newRecurringPayments);
        activeLoan = newActiveLoan;
        hasData = true;
        lastUpdateTime = System.currentTimeMillis();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Getters
    // ═══════════════════════════════════════════════════════════════════════════

    public static double getBalance() {
        return balance;
    }

    public static double getWalletBalance() {
        return walletBalance;
    }

    public static double getSavingsBalance() {
        return savingsBalance;
    }

    public static double getRemainingTransferLimit() {
        return remainingTransferLimit;
    }

    public static double getMaxTransferLimit() {
        return maxTransferLimit;
    }

    public static List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public static double getTotalIncome() {
        return totalIncome;
    }

    public static double getTotalExpenses() {
        return totalExpenses;
    }

    public static List<RecurringPaymentData> getRecurringPayments() {
        return new ArrayList<>(recurringPayments);
    }

    public static CreditLoanData getActiveLoan() {
        return activeLoan;
    }

    public static boolean hasData() {
        return hasData;
    }

    public static long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Check if data is stale (older than 5 seconds)
     */
    public static boolean isDataStale() {
        return System.currentTimeMillis() - lastUpdateTime > 5000;
    }

    public static void clear() {
        balance = 0.0;
        walletBalance = 0.0;
        savingsBalance = 0.0;
        remainingTransferLimit = 0.0;
        maxTransferLimit = 0.0;
        transactions.clear();
        recurringPayments.clear();
        activeLoan = null;
        totalIncome = 0.0;
        totalExpenses = 0.0;
        hasData = false;
        lastUpdateTime = 0;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Data Classes for complex types
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Client-side representation of a recurring payment
     */
    public static class RecurringPaymentData {
        public final String paymentId;
        public final String recipientName;
        public final double amount;
        public final int intervalDays;
        public final boolean isActive;
        public final long nextExecutionTime;

        public RecurringPaymentData(String paymentId, String recipientName, double amount,
                                   int intervalDays, boolean isActive, long nextExecutionTime) {
            this.paymentId = paymentId;
            this.recipientName = recipientName;
            this.amount = amount;
            this.intervalDays = intervalDays;
            this.isActive = isActive;
            this.nextExecutionTime = nextExecutionTime;
        }
    }

    /**
     * Client-side representation of an active credit loan
     */
    public static class CreditLoanData {
        public final String loanType;
        public final double totalAmount;
        public final double remainingAmount;
        public final double dailyPayment;
        public final int remainingDays;

        public CreditLoanData(String loanType, double totalAmount, double remainingAmount,
                             double dailyPayment, int remainingDays) {
            this.loanType = loanType;
            this.totalAmount = totalAmount;
            this.remainingAmount = remainingAmount;
            this.dailyPayment = dailyPayment;
            this.remainingDays = remainingDays;
        }
    }
}
