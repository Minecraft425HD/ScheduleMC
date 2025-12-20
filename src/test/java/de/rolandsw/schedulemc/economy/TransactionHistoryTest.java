package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;

/**
 * Unit Tests für TransactionHistory
 */
public class TransactionHistoryTest {

    private TransactionHistory history;
    private MinecraftServer mockServer;
    private UUID player1;
    private UUID player2;

    @BeforeEach
    public void setUp() {
        // Mock MinecraftServer
        mockServer = Mockito.mock(MinecraftServer.class);

        player1 = UUID.randomUUID();
        player2 = UUID.randomUUID();

        // Reset TransactionHistory instance
        try {
            java.lang.reflect.Field instanceField = TransactionHistory.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Ignore if field doesn't exist
        }

        history = TransactionHistory.getInstance(mockServer);

        // Clear any existing transactions
        history.clearTransactions(player1);
        history.clearTransactions(player2);
    }

    @Test
    public void testAddTransaction_Single() {
        Transaction tx = new Transaction(
            TransactionType.DEPOSIT,
            null,
            player1,
            1000.0,
            "Test deposit",
            1000.0
        );

        history.addTransaction(player1, tx);

        assertThat(history.getTransactionCount(player1)).isEqualTo(1);
    }

    @Test
    public void testAddTransaction_Multiple() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        history.addTransaction(player1, createTransaction(TransactionType.WITHDRAWAL, -50.0));
        history.addTransaction(player1, createTransaction(TransactionType.PURCHASE, -25.0));

        assertThat(history.getTransactionCount(player1)).isEqualTo(3);
    }

    @Test
    public void testGetAllTransactions_EmptyForNewPlayer() {
        UUID newPlayer = UUID.randomUUID();

        List<Transaction> transactions = history.getAllTransactions(newPlayer);

        assertThat(transactions).isEmpty();
    }

    @Test
    public void testGetAllTransactions_ReturnsAllAdded() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 200.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 300.0));

        List<Transaction> transactions = history.getAllTransactions(player1);

        assertThat(transactions).hasSize(3);
    }

    @Test
    public void testGetRecentTransactions_LimitWorks() {
        // Add 5 transactions
        for (int i = 0; i < 5; i++) {
            history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0 * i));
        }

        // Get only 3 most recent
        List<Transaction> recent = history.getRecentTransactions(player1, 3);

        assertThat(recent).hasSize(3);
    }

    @Test
    public void testGetRecentTransactions_SortedByNewest() throws InterruptedException {
        Transaction old = createTransaction(TransactionType.DEPOSIT, 100.0);
        Thread.sleep(10); // Ensure different timestamps
        Transaction middle = createTransaction(TransactionType.DEPOSIT, 200.0);
        Thread.sleep(10);
        Transaction newest = createTransaction(TransactionType.DEPOSIT, 300.0);

        history.addTransaction(player1, old);
        history.addTransaction(player1, middle);
        history.addTransaction(player1, newest);

        List<Transaction> recent = history.getRecentTransactions(player1, 3);

        // Newest should be first
        assertThat(recent.get(0).getAmount()).isEqualTo(300.0);
        assertThat(recent.get(1).getAmount()).isEqualTo(200.0);
        assertThat(recent.get(2).getAmount()).isEqualTo(100.0);
    }

    @Test
    public void testGetRecentTransactions_EmptyForNewPlayer() {
        UUID newPlayer = UUID.randomUUID();

        List<Transaction> recent = history.getRecentTransactions(newPlayer, 10);

        assertThat(recent).isEmpty();
    }

    @Test
    public void testGetTransactionsByType_FiltersCorrectly() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        history.addTransaction(player1, createTransaction(TransactionType.WITHDRAWAL, -50.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 200.0));
        history.addTransaction(player1, createTransaction(TransactionType.PURCHASE, -25.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 300.0));

        List<Transaction> deposits = history.getTransactionsByType(player1, TransactionType.DEPOSIT);

        assertThat(deposits).hasSize(3);
        assertThat(deposits).allMatch(tx -> tx.getType() == TransactionType.DEPOSIT);
    }

    @Test
    public void testGetTransactionsByType_EmptyWhenNoMatch() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));

        List<Transaction> withdrawals = history.getTransactionsByType(player1, TransactionType.WITHDRAWAL);

        assertThat(withdrawals).isEmpty();
    }

    @Test
    public void testGetTransactionsBetween_FiltersByTime() {
        long now = System.currentTimeMillis();
        long oneHourAgo = now - 3600000;
        long twoHoursAgo = now - 7200000;

        // Create transactions with specific timestamps
        Transaction old = createTransactionWithTime(TransactionType.DEPOSIT, 100.0, twoHoursAgo);
        Transaction middle = createTransactionWithTime(TransactionType.DEPOSIT, 200.0, oneHourAgo);
        Transaction recent = createTransactionWithTime(TransactionType.DEPOSIT, 300.0, now);

        history.addTransaction(player1, old);
        history.addTransaction(player1, middle);
        history.addTransaction(player1, recent);

        // Get transactions from last hour
        List<Transaction> lastHour = history.getTransactionsBetween(player1, oneHourAgo - 1000, now + 1000);

        assertThat(lastHour).hasSize(2); // middle and recent
    }

    @Test
    public void testGetTotalIncome_SumsPositiveAmounts() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 200.0));
        history.addTransaction(player1, createTransaction(TransactionType.WITHDRAWAL, -50.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 150.0));

        double totalIncome = history.getTotalIncome(player1);

        assertThat(totalIncome).isEqualTo(450.0); // 100 + 200 + 150
    }

    @Test
    public void testGetTotalIncome_ZeroForNewPlayer() {
        UUID newPlayer = UUID.randomUUID();

        double totalIncome = history.getTotalIncome(newPlayer);

        assertThat(totalIncome).isEqualTo(0.0);
    }

    @Test
    public void testGetTotalExpenses_SumsNegativeAmounts() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        history.addTransaction(player1, createTransaction(TransactionType.WITHDRAWAL, -50.0));
        history.addTransaction(player1, createTransaction(TransactionType.PURCHASE, -25.0));
        history.addTransaction(player1, createTransaction(TransactionType.WITHDRAWAL, -75.0));

        double totalExpenses = history.getTotalExpenses(player1);

        assertThat(totalExpenses).isEqualTo(150.0); // 50 + 25 + 75 (absolute values)
    }

    @Test
    public void testGetTotalExpenses_ZeroForNewPlayer() {
        UUID newPlayer = UUID.randomUUID();

        double totalExpenses = history.getTotalExpenses(newPlayer);

        assertThat(totalExpenses).isEqualTo(0.0);
    }

    @Test
    public void testGetTransactionCount_AccurateCount() {
        assertThat(history.getTransactionCount(player1)).isEqualTo(0);

        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        assertThat(history.getTransactionCount(player1)).isEqualTo(1);

        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 200.0));
        assertThat(history.getTransactionCount(player1)).isEqualTo(2);

        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 300.0));
        assertThat(history.getTransactionCount(player1)).isEqualTo(3);
    }

    @Test
    public void testClearTransactions_RemovesAll() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 200.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 300.0));

        assertThat(history.getTransactionCount(player1)).isEqualTo(3);

        history.clearTransactions(player1);

        assertThat(history.getTransactionCount(player1)).isEqualTo(0);
        assertThat(history.getAllTransactions(player1)).isEmpty();
    }

    @Test
    public void testMultiplePlayers_IndependentHistories() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 200.0));

        history.addTransaction(player2, createTransaction(TransactionType.DEPOSIT, 500.0));
        history.addTransaction(player2, createTransaction(TransactionType.DEPOSIT, 600.0));
        history.addTransaction(player2, createTransaction(TransactionType.DEPOSIT, 700.0));

        assertThat(history.getTransactionCount(player1)).isEqualTo(2);
        assertThat(history.getTransactionCount(player2)).isEqualTo(3);

        // Verify amounts
        assertThat(history.getTotalIncome(player1)).isEqualTo(300.0);
        assertThat(history.getTotalIncome(player2)).isEqualTo(1800.0);
    }

    @Test
    public void testTransactionLimit_EnforcesMaximum() {
        // Add more than MAX (1000) transactions
        int maxTransactions = 1000;
        int excess = 50;

        for (int i = 0; i < maxTransactions + excess; i++) {
            history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, i * 10.0));
        }

        // Should be capped at 1000
        assertThat(history.getTransactionCount(player1)).isLessThanOrEqualTo(maxTransactions);
    }

    @Test
    public void testTransactionLimit_KeepsNewest() {
        int maxTransactions = 1000;
        int excess = 50;

        // Add transactions with identifiable amounts
        for (int i = 0; i < maxTransactions + excess; i++) {
            history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, i));
        }

        // Get all transactions
        List<Transaction> all = history.getAllTransactions(player1);

        // The oldest (0-49) should be removed, keeping 50-1049
        // So the smallest amount should be around 50, not 0
        double minAmount = all.stream()
            .mapToDouble(Transaction::getAmount)
            .min()
            .orElse(0);

        assertThat(minAmount).isGreaterThanOrEqualTo(excess);
    }

    @Test
    public void testGetStatistics_ProvidesInfo() {
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 100.0));
        history.addTransaction(player2, createTransaction(TransactionType.DEPOSIT, 200.0));

        String stats = history.getStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats).contains("2 players");
        assertThat(stats).contains("2 total transactions");
    }

    @Test
    public void testComplexScenario_MixedOperations() {
        // Simulate a player's transaction history
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 1000.0));
        history.addTransaction(player1, createTransaction(TransactionType.PURCHASE, -150.0));
        history.addTransaction(player1, createTransaction(TransactionType.PURCHASE, -200.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 500.0));
        history.addTransaction(player1, createTransaction(TransactionType.WITHDRAWAL, -100.0));
        history.addTransaction(player1, createTransaction(TransactionType.DEPOSIT, 250.0));

        // Verify counts
        assertThat(history.getTransactionCount(player1)).isEqualTo(6);

        // Verify totals
        assertThat(history.getTotalIncome(player1)).isEqualTo(1750.0); // 1000 + 500 + 250
        assertThat(history.getTotalExpenses(player1)).isEqualTo(450.0); // 150 + 200 + 100

        // Verify filtering
        List<Transaction> deposits = history.getTransactionsByType(player1, TransactionType.DEPOSIT);
        assertThat(deposits).hasSize(3);

        List<Transaction> purchases = history.getTransactionsByType(player1, TransactionType.PURCHASE);
        assertThat(purchases).hasSize(2);
    }

    // Helper methods

    private Transaction createTransaction(TransactionType type, double amount) {
        return new Transaction(type, null, player1, amount, "Test transaction", 0.0);
    }

    private Transaction createTransactionWithTime(TransactionType type, double amount, long timestamp) {
        // Create transaction and use reflection to set timestamp
        Transaction tx = new Transaction(type, null, player1, amount, "Test", 0.0);
        try {
            java.lang.reflect.Field timestampField = Transaction.class.getDeclaredField("timestamp");
            timestampField.setAccessible(true);
            timestampField.set(tx, timestamp);
        } catch (Exception e) {
            // Fallback: return transaction with current timestamp
        }
        return tx;
    }
}
