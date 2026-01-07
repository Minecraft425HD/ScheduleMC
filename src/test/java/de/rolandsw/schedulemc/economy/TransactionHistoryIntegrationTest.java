package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for TransactionHistory
 *
 * Tests transaction logging and querying:
 * - Transaction Recording
 * - Transaction Retrieval
 * - Filtering by Type
 * - Persistence
 * - Transaction Limits
 *
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionHistory Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionHistoryIntegrationTest {

    @TempDir
    Path tempDir;

    @Mock
    private MinecraftServer mockServer;

    private TransactionHistory history;
    private UUID playerUUID;

    @BeforeEach
    void setUp() {
        // Configure temp directory
        when(mockServer.getServerDirectory()).thenReturn(tempDir.toFile());

        // Create history instance
        history = TransactionHistory.getInstance(mockServer);

        // Create test player
        playerUUID = UUID.randomUUID();
    }

    @Test
    @Order(1)
    @DisplayName("Transaction Recording - Should record transaction")
    void testRecordTransaction() {
        // Arrange
        Transaction transaction = new Transaction(
            TransactionType.DEPOSIT,
            null,
            playerUUID,
            100.0,
            "Test deposit",
            100.0
        );

        // Act
        history.addTransaction(playerUUID, transaction);

        // Assert
        List<Transaction> transactions = history.getTransactions(playerUUID);
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transactions.get(0).getAmount()).isEqualTo(100.0);
    }

    @Test
    @Order(2)
    @DisplayName("Transaction Recording - Should record multiple transactions")
    void testRecordMultipleTransactions() {
        // Arrange & Act
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.DEPOSIT, null, playerUUID, 100.0, "Deposit 1", 100.0
        ));
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.WITHDRAW, playerUUID, null, -50.0, "Withdrawal 1", 50.0
        ));
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.DEPOSIT, null, playerUUID, 25.0, "Deposit 2", 75.0
        ));

        // Assert
        List<Transaction> transactions = history.getTransactions(playerUUID);
        assertThat(transactions).hasSize(3);
    }

    @Test
    @Order(3)
    @DisplayName("Transaction Retrieval - Should retrieve transactions in order")
    void testRetrieveTransactionsInOrder() {
        // Arrange
        Transaction t1 = new Transaction(TransactionType.DEPOSIT, null, playerUUID, 10.0, "First", 10.0);
        Transaction t2 = new Transaction(TransactionType.DEPOSIT, null, playerUUID, 20.0, "Second", 30.0);
        Transaction t3 = new Transaction(TransactionType.DEPOSIT, null, playerUUID, 30.0, "Third", 60.0);

        history.addTransaction(playerUUID, t1);
        history.addTransaction(playerUUID, t2);
        history.addTransaction(playerUUID, t3);

        // Act
        List<Transaction> transactions = history.getTransactions(playerUUID);

        // Assert - Should be in chronological order (oldest first)
        assertThat(transactions.get(0).getDescription()).isEqualTo("First");
        assertThat(transactions.get(1).getDescription()).isEqualTo("Second");
        assertThat(transactions.get(2).getDescription()).isEqualTo("Third");
    }

    @Test
    @Order(4)
    @DisplayName("Transaction Filtering - Should filter by type")
    void testFilterByType() {
        // Arrange
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.DEPOSIT, null, playerUUID, 100.0, "Deposit", 100.0
        ));
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.WITHDRAW, playerUUID, null, -50.0, "Withdraw", 50.0
        ));
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.TRANSFER, playerUUID, UUID.randomUUID(), -25.0, "Transfer", 25.0
        ));

        // Act
        List<Transaction> deposits = history.getTransactionsByType(playerUUID, TransactionType.DEPOSIT);

        // Assert
        assertThat(deposits).hasSize(1);
        assertThat(deposits.get(0).getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    @Order(5)
    @DisplayName("Transaction Limits - Should limit transaction history size")
    void testTransactionLimit() {
        // Arrange - Add more than limit (assuming limit is 1000)
        for (int i = 0; i < 1100; i++) {
            history.addTransaction(playerUUID, new Transaction(
                TransactionType.DEPOSIT, null, playerUUID, 1.0, "Tx " + i, i + 1.0
            ));
        }

        // Act
        List<Transaction> transactions = history.getTransactions(playerUUID);

        // Assert - Should not exceed limit
        assertThat(transactions).hasSizeLessThanOrEqualTo(1000);
    }

    @Test
    @Order(6)
    @DisplayName("Transaction Query - Should get recent transactions")
    void testGetRecentTransactions() {
        // Arrange
        for (int i = 0; i < 50; i++) {
            history.addTransaction(playerUUID, new Transaction(
                TransactionType.DEPOSIT, null, playerUUID, 1.0, "Tx " + i, i + 1.0
            ));
        }

        // Act
        List<Transaction> recent = history.getRecentTransactions(playerUUID, 10);

        // Assert
        assertThat(recent).hasSize(10);
        // Recent transactions should be last 10
        assertThat(recent.get(recent.size() - 1).getDescription()).isEqualTo("Tx 49");
    }

    @Test
    @Order(7)
    @DisplayName("Persistence - Should save and load transaction history")
    void testPersistence() {
        // Arrange
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.DEPOSIT, null, playerUUID, 123.45, "Persistent", 123.45
        ));

        // Act - Save
        history.save();

        // Create new instance (simulating server restart)
        TransactionHistory newHistory = TransactionHistory.getInstance(mockServer);

        // Assert
        List<Transaction> loaded = newHistory.getTransactions(playerUUID);
        assertThat(loaded).hasSize(1);
        assertThat(loaded.get(0).getAmount()).isEqualTo(123.45);
        assertThat(loaded.get(0).getDescription()).isEqualTo("Persistent");
    }

    @Test
    @Order(8)
    @DisplayName("Empty History - Should handle empty transaction history")
    void testEmptyHistory() {
        // Act
        List<Transaction> transactions = history.getTransactions(UUID.randomUUID());

        // Assert
        assertThat(transactions).isNotNull();
        assertThat(transactions).isEmpty();
    }

    @Test
    @Order(9)
    @DisplayName("Transaction Statistics - Should calculate transaction totals")
    void testTransactionTotals() {
        // Arrange
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.DEPOSIT, null, playerUUID, 100.0, "D1", 100.0
        ));
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.DEPOSIT, null, playerUUID, 50.0, "D2", 150.0
        ));
        history.addTransaction(playerUUID, new Transaction(
            TransactionType.WITHDRAW, playerUUID, null, -30.0, "W1", 120.0
        ));

        // Act
        double totalDeposits = history.getTotalDeposits(playerUUID);
        double totalWithdrawals = history.getTotalWithdrawals(playerUUID);

        // Assert
        assertThat(totalDeposits).isEqualTo(150.0);
        assertThat(totalWithdrawals).isEqualTo(30.0);
    }
}
