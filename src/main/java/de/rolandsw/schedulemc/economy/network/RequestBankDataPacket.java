package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.*;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.bank.TransferLimitTracker;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from client to server to request full bank data synchronization
 * Used by both ATM and Banker screens
 */
public class RequestBankDataPacket {

    public RequestBankDataPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
        // No data to encode
    }

    public static RequestBankDataPacket decode(FriendlyByteBuf buf) {
        return new RequestBankDataPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            sendBankDataToClient(player);
        });
    }

    private void sendBankDataToClient(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        MinecraftServer server = player.getServer();

        // ═══════════════════════════════════════════════════════════════════════════
        // Gather all bank data
        // ═══════════════════════════════════════════════════════════════════════════

        // Girokonto Balance
        double balance = EconomyManager.getBalance(playerUUID);

        // Wallet Balance (Bargeld) - Aus WalletManager (UUID-basiert)
        double walletBalance = de.rolandsw.schedulemc.economy.WalletManager.getBalance(playerUUID);

        // Sparkonto Balance
        double savingsBalance = 0.0;
        if (server != null) {
            SavingsAccountManager savingsManager = SavingsAccountManager.getInstance(server);
            if (savingsManager != null) {
                List<SavingsAccount> accounts = savingsManager.getAccounts(playerUUID);
                savingsBalance = accounts.stream()
                    .mapToDouble(SavingsAccount::getBalance)
                    .sum();
            }
        }

        // Transfer Limit
        double remainingTransferLimit = ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();
        double maxTransferLimit = ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();
        if (server != null) {
            TransferLimitTracker tracker = TransferLimitTracker.getInstance(server);
            if (tracker != null) {
                remainingTransferLimit = tracker.getRemainingLimit(playerUUID);
            }
        }

        // Transaction History
        List<Transaction> transactions = new ArrayList<>();
        double totalIncome = 0.0;
        double totalExpenses = 0.0;
        TransactionHistory history = TransactionHistory.getInstance();
        if (history != null) {
            transactions = history.getRecentTransactions(playerUUID, 100);  // Erhöht von 20 auf 100
            totalIncome = history.getTotalIncome(playerUUID);
            totalExpenses = history.getTotalExpenses(playerUUID);
        }

        // Recurring Payments
        List<ClientBankDataCache.RecurringPaymentData> recurringPayments = new ArrayList<>();
        if (server != null) {
            RecurringPaymentManager paymentManager = RecurringPaymentManager.getInstance(server);
            if (paymentManager != null) {
                List<RecurringPayment> payments = paymentManager.getPayments(playerUUID);
                for (RecurringPayment payment : payments) {
                    // Get recipient name
                    String recipientName = payment.getToPlayer().toString().substring(0, 8) + "...";
                    recurringPayments.add(new ClientBankDataCache.RecurringPaymentData(
                        payment.getPaymentId(),
                        recipientName,
                        payment.getAmount(),
                        payment.getIntervalDays(),
                        payment.isActive(),
                        payment.getNextExecutionDay()
                    ));
                }
            }
        }

        // Active Credit Loan
        ClientBankDataCache.CreditLoanData activeLoan = null;
        if (server != null) {
            CreditLoanManager loanManager = CreditLoanManager.getInstance(server);
            if (loanManager != null) {
                CreditLoan loan = loanManager.getLoan(playerUUID);
                if (loan != null) {
                    // Calculate current day for remaining days calculation
                    long currentDay = server.overworld() != null ?
                        server.overworld().getDayTime() / 24000L : 0;
                    activeLoan = new ClientBankDataCache.CreditLoanData(
                        loan.getType().name(),
                        loan.getPrincipal() + loan.getTotalInterest(),
                        loan.getRemaining(),
                        loan.getDailyPayment(),
                        loan.getRemainingDays(currentDay)
                    );
                }
            }
        }

        // Overdraft (Dispo) Data
        double overdraftAmount = 0.0;
        int debtDaysPassed = 0;
        int daysUntilAutoRepay = 0;
        int daysUntilPrison = 0;
        double potentialPrisonMinutes = 0.0;

        if (server != null) {
            OverdraftManager overdraftManager = OverdraftManager.getInstance(server);
            if (overdraftManager != null) {
                overdraftAmount = OverdraftManager.getOverdraftAmount(balance);
                if (overdraftAmount > 0) {
                    debtDaysPassed = overdraftManager.getDaysSinceDebtStart(playerUUID);
                    daysUntilAutoRepay = overdraftManager.getDaysUntilAutoRepay(playerUUID);
                    daysUntilPrison = overdraftManager.getDaysUntilPrison(playerUUID);
                    potentialPrisonMinutes = overdraftManager.getPotentialPrisonMinutes(overdraftAmount);
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // Send comprehensive data to client
        // ═══════════════════════════════════════════════════════════════════════════

        // Send full bank data (includes everything)
        SyncFullBankDataPacket fullPacket = new SyncFullBankDataPacket(
            balance,
            walletBalance,
            savingsBalance,
            remainingTransferLimit,
            maxTransferLimit,
            transactions,
            recurringPayments,
            activeLoan,
            totalIncome,
            totalExpenses,
            overdraftAmount,
            debtDaysPassed,
            daysUntilAutoRepay,
            daysUntilPrison,
            potentialPrisonMinutes
        );
        de.rolandsw.schedulemc.npc.network.NPCNetworkHandler.INSTANCE.sendTo(
            fullPacket,
            player.connection.connection,
            net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
        );
    }
}
