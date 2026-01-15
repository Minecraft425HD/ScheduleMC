package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet to synchronize full bank data from server to client
 * Includes: balance, wallet, savings, transfer limits, recurring payments, and active loan
 */
public class SyncFullBankDataPacket {
    private final double balance;
    private final double walletBalance;
    private final double savingsBalance;
    private final double remainingTransferLimit;
    private final double maxTransferLimit;
    private final List<TransactionData> transactions;
    private final List<RecurringPaymentData> recurringPayments;
    private final CreditLoanData activeLoan; // null if no active loan

    // Dispo-Daten
    private final double overdraftAmount;
    private final int debtDaysPassed;
    private final int daysUntilAutoRepay;
    private final int daysUntilPrison;
    private final double potentialPrisonMinutes;

    public SyncFullBankDataPacket(double balance, double walletBalance, double savingsBalance,
                                  double remainingTransferLimit, double maxTransferLimit,
                                  List<Transaction> transactions,
                                  List<ClientBankDataCache.RecurringPaymentData> recurringPayments,
                                  ClientBankDataCache.CreditLoanData activeLoan,
                                  double overdraftAmount, int debtDaysPassed,
                                  int daysUntilAutoRepay, int daysUntilPrison,
                                  double potentialPrisonMinutes) {
        this.balance = balance;
        this.walletBalance = walletBalance;
        this.savingsBalance = savingsBalance;
        this.remainingTransferLimit = remainingTransferLimit;
        this.maxTransferLimit = maxTransferLimit;

        // Dispo-Daten
        this.overdraftAmount = overdraftAmount;
        this.debtDaysPassed = debtDaysPassed;
        this.daysUntilAutoRepay = daysUntilAutoRepay;
        this.daysUntilPrison = daysUntilPrison;
        this.potentialPrisonMinutes = potentialPrisonMinutes;

        // Convert transactions
        this.transactions = new ArrayList<>();
        for (Transaction t : transactions) {
            this.transactions.add(new TransactionData(
                t.getTransactionId(),
                t.getType(),
                t.getFromPlayer(),
                t.getToPlayer(),
                t.getAmount(),
                t.getTimestamp(),
                t.getDescription(),
                t.getBalanceAfter()
            ));
        }

        // Convert recurring payments
        this.recurringPayments = new ArrayList<>();
        for (ClientBankDataCache.RecurringPaymentData rp : recurringPayments) {
            this.recurringPayments.add(new RecurringPaymentData(
                rp.paymentId,
                rp.recipientName,
                rp.amount,
                rp.intervalDays,
                rp.isActive,
                rp.nextExecutionTime
            ));
        }

        // Convert credit loan
        if (activeLoan != null) {
            this.activeLoan = new CreditLoanData(
                activeLoan.loanType,
                activeLoan.totalAmount,
                activeLoan.remainingAmount,
                activeLoan.dailyPayment,
                activeLoan.remainingDays
            );
        } else {
            this.activeLoan = null;
        }
    }

    // Internal constructor for decoding
    private SyncFullBankDataPacket(double balance, double walletBalance, double savingsBalance,
                                   double remainingTransferLimit, double maxTransferLimit,
                                   List<TransactionData> transactions,
                                   List<RecurringPaymentData> recurringPayments,
                                   CreditLoanData activeLoan,
                                   double overdraftAmount, int debtDaysPassed,
                                   int daysUntilAutoRepay, int daysUntilPrison,
                                   double potentialPrisonMinutes) {
        this.balance = balance;
        this.walletBalance = walletBalance;
        this.savingsBalance = savingsBalance;
        this.remainingTransferLimit = remainingTransferLimit;
        this.maxTransferLimit = maxTransferLimit;
        this.transactions = transactions;
        this.recurringPayments = recurringPayments;
        this.activeLoan = activeLoan;

        // Dispo-Daten
        this.overdraftAmount = overdraftAmount;
        this.debtDaysPassed = debtDaysPassed;
        this.daysUntilAutoRepay = daysUntilAutoRepay;
        this.daysUntilPrison = daysUntilPrison;
        this.potentialPrisonMinutes = potentialPrisonMinutes;
    }

    public void encode(FriendlyByteBuf buf) {
        // Basic balances
        buf.writeDouble(balance);
        buf.writeDouble(walletBalance);
        buf.writeDouble(savingsBalance);
        buf.writeDouble(remainingTransferLimit);
        buf.writeDouble(maxTransferLimit);

        // Transactions
        buf.writeInt(transactions.size());
        for (TransactionData t : transactions) {
            buf.writeUtf(t.transactionId);
            buf.writeEnum(t.type);
            buf.writeBoolean(t.fromPlayer != null);
            if (t.fromPlayer != null) {
                buf.writeUUID(t.fromPlayer);
            }
            buf.writeBoolean(t.toPlayer != null);
            if (t.toPlayer != null) {
                buf.writeUUID(t.toPlayer);
            }
            buf.writeDouble(t.amount);
            buf.writeLong(t.timestamp);
            buf.writeUtf(t.description);
            buf.writeDouble(t.balanceAfter);
        }

        // Recurring Payments
        buf.writeInt(recurringPayments.size());
        for (RecurringPaymentData rp : recurringPayments) {
            buf.writeUtf(rp.paymentId);
            buf.writeUtf(rp.recipientName);
            buf.writeDouble(rp.amount);
            buf.writeInt(rp.intervalDays);
            buf.writeBoolean(rp.isActive);
            buf.writeLong(rp.nextExecutionTime);
        }

        // Active Loan (nullable)
        buf.writeBoolean(activeLoan != null);
        if (activeLoan != null) {
            buf.writeUtf(activeLoan.loanType);
            buf.writeDouble(activeLoan.totalAmount);
            buf.writeDouble(activeLoan.remainingAmount);
            buf.writeDouble(activeLoan.dailyPayment);
            buf.writeInt(activeLoan.remainingDays);
        }

        // Dispo-Daten
        buf.writeDouble(overdraftAmount);
        buf.writeInt(debtDaysPassed);
        buf.writeInt(daysUntilAutoRepay);
        buf.writeInt(daysUntilPrison);
        buf.writeDouble(potentialPrisonMinutes);
    }

    public static SyncFullBankDataPacket decode(FriendlyByteBuf buf) {
        // Basic balances
        double balance = buf.readDouble();
        double walletBalance = buf.readDouble();
        double savingsBalance = buf.readDouble();
        double remainingTransferLimit = buf.readDouble();
        double maxTransferLimit = buf.readDouble();

        // Transactions
        int txCount = buf.readInt();
        List<TransactionData> transactions = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            String transactionId = buf.readUtf();
            TransactionType type = buf.readEnum(TransactionType.class);
            UUID fromPlayer = buf.readBoolean() ? buf.readUUID() : null;
            UUID toPlayer = buf.readBoolean() ? buf.readUUID() : null;
            double amount = buf.readDouble();
            long timestamp = buf.readLong();
            String description = buf.readUtf();
            double balanceAfter = buf.readDouble();
            transactions.add(new TransactionData(transactionId, type, fromPlayer, toPlayer,
                amount, timestamp, description, balanceAfter));
        }

        // Recurring Payments
        int rpCount = buf.readInt();
        List<RecurringPaymentData> recurringPayments = new ArrayList<>();
        for (int i = 0; i < rpCount; i++) {
            String paymentId = buf.readUtf();
            String recipientName = buf.readUtf();
            double amount = buf.readDouble();
            int intervalDays = buf.readInt();
            boolean isActive = buf.readBoolean();
            long nextExecutionTime = buf.readLong();
            recurringPayments.add(new RecurringPaymentData(paymentId, recipientName, amount,
                intervalDays, isActive, nextExecutionTime));
        }

        // Active Loan (nullable)
        CreditLoanData activeLoan = null;
        if (buf.readBoolean()) {
            String loanType = buf.readUtf();
            double totalAmount = buf.readDouble();
            double remainingAmount = buf.readDouble();
            double dailyPayment = buf.readDouble();
            int remainingDays = buf.readInt();
            activeLoan = new CreditLoanData(loanType, totalAmount, remainingAmount, dailyPayment, remainingDays);
        }

        // Dispo-Daten
        double overdraftAmount = buf.readDouble();
        int debtDaysPassed = buf.readInt();
        int daysUntilAutoRepay = buf.readInt();
        int daysUntilPrison = buf.readInt();
        double potentialPrisonMinutes = buf.readDouble();

        return new SyncFullBankDataPacket(balance, walletBalance, savingsBalance,
            remainingTransferLimit, maxTransferLimit, transactions, recurringPayments, activeLoan,
            overdraftAmount, debtDaysPassed, daysUntilAutoRepay, daysUntilPrison, potentialPrisonMinutes);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Convert TransactionData back to Transaction for the cache
            List<Transaction> transactionList = new ArrayList<>();
            for (TransactionData td : transactions) {
                transactionList.add(new Transaction(
                    td.type,
                    td.fromPlayer,
                    td.toPlayer,
                    td.amount,
                    td.description,
                    td.balanceAfter
                ));
            }

            // Convert RecurringPaymentData back to ClientBankDataCache format
            List<ClientBankDataCache.RecurringPaymentData> recurringList = new ArrayList<>();
            for (RecurringPaymentData rpd : recurringPayments) {
                recurringList.add(new ClientBankDataCache.RecurringPaymentData(
                    rpd.paymentId,
                    rpd.recipientName,
                    rpd.amount,
                    rpd.intervalDays,
                    rpd.isActive,
                    rpd.nextExecutionTime
                ));
            }

            // Convert CreditLoanData back to ClientBankDataCache format
            ClientBankDataCache.CreditLoanData loanData = null;
            if (activeLoan != null) {
                loanData = new ClientBankDataCache.CreditLoanData(
                    activeLoan.loanType,
                    activeLoan.totalAmount,
                    activeLoan.remainingAmount,
                    activeLoan.dailyPayment,
                    activeLoan.remainingDays
                );
            }

            // Update the client cache with full data (including overdraft)
            ClientBankDataCache.updateFullData(
                balance,
                walletBalance,
                savingsBalance,
                remainingTransferLimit,
                maxTransferLimit,
                transactionList,
                recurringList,
                loanData,
                overdraftAmount,
                debtDaysPassed,
                daysUntilAutoRepay,
                daysUntilPrison,
                potentialPrisonMinutes
            );
        });
        ctx.get().setPacketHandled(true);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Data Classes
    // ═══════════════════════════════════════════════════════════════════════════

    private static class TransactionData {
        final String transactionId;
        final TransactionType type;
        final UUID fromPlayer;
        final UUID toPlayer;
        final double amount;
        final long timestamp;
        final String description;
        final double balanceAfter;

        TransactionData(String transactionId, TransactionType type, UUID fromPlayer, UUID toPlayer,
                       double amount, long timestamp, String description, double balanceAfter) {
            this.transactionId = transactionId;
            this.type = type;
            this.fromPlayer = fromPlayer;
            this.toPlayer = toPlayer;
            this.amount = amount;
            this.timestamp = timestamp;
            this.description = description;
            this.balanceAfter = balanceAfter;
        }
    }

    private static class RecurringPaymentData {
        final String paymentId;
        final String recipientName;
        final double amount;
        final int intervalDays;
        final boolean isActive;
        final long nextExecutionTime;

        RecurringPaymentData(String paymentId, String recipientName, double amount,
                           int intervalDays, boolean isActive, long nextExecutionTime) {
            this.paymentId = paymentId;
            this.recipientName = recipientName;
            this.amount = amount;
            this.intervalDays = intervalDays;
            this.isActive = isActive;
            this.nextExecutionTime = nextExecutionTime;
        }
    }

    private static class CreditLoanData {
        final String loanType;
        final double totalAmount;
        final double remainingAmount;
        final double dailyPayment;
        final int remainingDays;

        CreditLoanData(String loanType, double totalAmount, double remainingAmount,
                      double dailyPayment, int remainingDays) {
            this.loanType = loanType;
            this.totalAmount = totalAmount;
            this.remainingAmount = remainingAmount;
            this.dailyPayment = dailyPayment;
            this.remainingDays = remainingDays;
        }
    }
}
