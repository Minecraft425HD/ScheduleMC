package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side cache for bank data received from server
 */
public class ClientBankDataCache {
    private static double balance = 0.0;
    private static List<Transaction> transactions = new ArrayList<>();
    private static double totalIncome = 0.0;
    private static double totalExpenses = 0.0;
    private static boolean hasData = false;

    public static void updateData(double newBalance, List<Transaction> newTransactions, double newTotalIncome, double newTotalExpenses) {
        balance = newBalance;
        transactions = new ArrayList<>(newTransactions);
        totalIncome = newTotalIncome;
        totalExpenses = newTotalExpenses;
        hasData = true;
    }

    public static double getBalance() {
        return balance;
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

    public static boolean hasData() {
        return hasData;
    }

    public static void clear() {
        balance = 0.0;
        transactions.clear();
        totalIncome = 0.0;
        totalExpenses = 0.0;
        hasData = false;
    }
}
