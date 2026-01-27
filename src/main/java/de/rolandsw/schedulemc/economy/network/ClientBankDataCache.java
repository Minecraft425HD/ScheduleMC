package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side cache for bank data received from server
 * Stores ALL bank-related data needed by ATM and Banker screens
 */
public class ClientBankDataCache {

    /**
     * Maximum stock market history size (90 days)
     * Prevents unbounded memory growth - daily updates would grow infinitely otherwise
     */
    private static final int MAX_HISTORY_SIZE = 90;

    // Girokonto
    private static double balance = 0.0;
    private static List<Transaction> transactions = new CopyOnWriteArrayList<>();
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
    private static List<RecurringPaymentData> recurringPayments = new CopyOnWriteArrayList<>();

    // Kredit
    private static CreditLoanData activeLoan = null;

    // Überziehung (Dispo)
    private static double overdraftAmount = 0.0;           // Wie viel überzogen?
    private static int debtDaysPassed = 0;                 // Tage seit Überziehung
    private static int daysUntilAutoRepay = 0;             // Tage bis Auto-Ausgleich (Tag 7)
    private static int daysUntilPrison = 0;                // Tage bis Gefängnis (Tag 28)
    private static double potentialPrisonMinutes = 0.0;    // Gefängniszeit in Minuten

    // Börsen-Daten
    private static double goldPrice = 150.0;
    private static double diamondPrice = 800.0;
    private static double emeraldPrice = 100.0;
    private static int goldTrend = 0;
    private static int diamondTrend = 0;
    private static int emeraldTrend = 0;

    // Börsen-Historie (7 Tage)
    private static List<Double> goldHistory = new CopyOnWriteArrayList<>();
    private static List<Double> diamondHistory = new CopyOnWriteArrayList<>();
    private static List<Double> emeraldHistory = new CopyOnWriteArrayList<>();

    // Börsen-Statistiken
    private static double goldHigh = 150.0;
    private static double goldLow = 150.0;
    private static double goldAvg = 150.0;
    private static double diamondHigh = 800.0;
    private static double diamondLow = 800.0;
    private static double diamondAvg = 800.0;
    private static double emeraldHigh = 100.0;
    private static double emeraldLow = 100.0;
    private static double emeraldAvg = 100.0;

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
        transactions = new CopyOnWriteArrayList<>(newTransactions);
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
        transactions = new CopyOnWriteArrayList<>(newTransactions);
        recurringPayments = new CopyOnWriteArrayList<>(newRecurringPayments);
        activeLoan = newActiveLoan;
        hasData = true;
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Update full bank data including overdraft info
     */
    public static void updateFullData(double newBalance, double newWalletBalance,
                                      double newSavingsBalance, double newRemainingLimit,
                                      double newMaxLimit, List<Transaction> newTransactions,
                                      List<RecurringPaymentData> newRecurringPayments,
                                      CreditLoanData newActiveLoan,
                                      double newOverdraftAmount, int newDebtDaysPassed,
                                      int newDaysUntilAutoRepay, int newDaysUntilPrison,
                                      double newPotentialPrisonMinutes) {
        balance = newBalance;
        walletBalance = newWalletBalance;
        savingsBalance = newSavingsBalance;
        remainingTransferLimit = newRemainingLimit;
        maxTransferLimit = newMaxLimit;
        transactions = new CopyOnWriteArrayList<>(newTransactions);
        recurringPayments = new CopyOnWriteArrayList<>(newRecurringPayments);
        activeLoan = newActiveLoan;

        // Dispo-Daten
        overdraftAmount = newOverdraftAmount;
        debtDaysPassed = newDebtDaysPassed;
        daysUntilAutoRepay = newDaysUntilAutoRepay;
        daysUntilPrison = newDaysUntilPrison;
        potentialPrisonMinutes = newPotentialPrisonMinutes;

        hasData = true;
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Update stock market data (legacy method - ohne Historie)
     */
    public static void updateStockData(double gold, int goldT, double diamond, int diamondT, double emerald, int emeraldT) {
        goldPrice = gold;
        goldTrend = goldT;
        diamondPrice = diamond;
        diamondTrend = diamondT;
        emeraldPrice = emerald;
        emeraldTrend = emeraldT;
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Update stock market data with full statistics
     */
    public static void updateStockDataFull(
            double gold, int goldT, List<Double> goldHist, double goldH, double goldL, double goldA,
            double diamond, int diamondT, List<Double> diamondHist, double diamondH, double diamondL, double diamondA,
            double emerald, int emeraldT, List<Double> emeraldHist, double emeraldH, double emeraldL, double emeraldA) {

        // Prices & Trends
        goldPrice = gold;
        goldTrend = goldT;
        diamondPrice = diamond;
        diamondTrend = diamondT;
        emeraldPrice = emerald;
        emeraldTrend = emeraldT;

        // History - with size limit to prevent unbounded growth
        goldHistory = limitHistorySize(goldHist);
        diamondHistory = limitHistorySize(diamondHist);
        emeraldHistory = limitHistorySize(emeraldHist);

        // Statistics
        goldHigh = goldH;
        goldLow = goldL;
        goldAvg = goldA;
        diamondHigh = diamondH;
        diamondLow = diamondL;
        diamondAvg = diamondA;
        emeraldHigh = emeraldH;
        emeraldLow = emeraldL;
        emeraldAvg = emeraldA;

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

    // Overdraft Getters
    public static double getOverdraftAmount() {
        return overdraftAmount;
    }

    public static int getDebtDaysPassed() {
        return debtDaysPassed;
    }

    public static int getDaysUntilAutoRepay() {
        return daysUntilAutoRepay;
    }

    public static int getDaysUntilPrison() {
        return daysUntilPrison;
    }

    public static double getPotentialPrisonMinutes() {
        return potentialPrisonMinutes;
    }

    public static boolean isOverdrawn() {
        return overdraftAmount > 0;
    }

    // Stock Market Getters
    public static double getGoldPrice() {
        return goldPrice;
    }

    public static double getDiamondPrice() {
        return diamondPrice;
    }

    public static double getEmeraldPrice() {
        return emeraldPrice;
    }

    public static int getGoldTrend() {
        return goldTrend;
    }

    public static int getDiamondTrend() {
        return diamondTrend;
    }

    public static int getEmeraldTrend() {
        return emeraldTrend;
    }

    // History Getters
    public static List<Double> getGoldHistory() {
        return new ArrayList<>(goldHistory);
    }

    public static List<Double> getDiamondHistory() {
        return new ArrayList<>(diamondHistory);
    }

    public static List<Double> getEmeraldHistory() {
        return new ArrayList<>(emeraldHistory);
    }

    // Statistics Getters
    public static double getGoldHigh() {
        return goldHigh;
    }

    public static double getGoldLow() {
        return goldLow;
    }

    public static double getGoldAvg() {
        return goldAvg;
    }

    public static double getDiamondHigh() {
        return diamondHigh;
    }

    public static double getDiamondLow() {
        return diamondLow;
    }

    public static double getDiamondAvg() {
        return diamondAvg;
    }

    public static double getEmeraldHigh() {
        return emeraldHigh;
    }

    public static double getEmeraldLow() {
        return emeraldLow;
    }

    public static double getEmeraldAvg() {
        return emeraldAvg;
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

        // Clear overdraft data
        overdraftAmount = 0.0;
        debtDaysPassed = 0;
        daysUntilAutoRepay = 0;
        daysUntilPrison = 0;
        potentialPrisonMinutes = 0.0;

        // Clear stock data
        goldHistory.clear();
        diamondHistory.clear();
        emeraldHistory.clear();

        hasData = false;
        lastUpdateTime = 0;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Limits history list size to MAX_HISTORY_SIZE (90 days).
     * Keeps only the most recent entries.
     * Prevents unbounded memory growth from daily stock price updates.
     */
    private static List<Double> limitHistorySize(List<Double> history) {
        if (history.size() <= MAX_HISTORY_SIZE) {
            return new CopyOnWriteArrayList<>(history);
        }

        // Keep only last MAX_HISTORY_SIZE entries
        int startIndex = history.size() - MAX_HISTORY_SIZE;
        List<Double> limited = history.subList(startIndex, history.size());
        return new CopyOnWriteArrayList<>(limited);
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
