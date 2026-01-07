package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link BatchTransactionManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Fluent Builder API for deposits, withdrawals, and transfers</li>
 *   <li>Bulk operations with Map-based API</li>
 *   <li>Batch execution with success/failure tracking</li>
 *   <li>Query methods (isEmpty, getQueuedCount)</li>
 *   <li>Statistics tracking (total, successful, failed)</li>
 *   <li>Negative amount validation</li>
 * </ul>
 */
@DisplayName("BatchTransactionManager Tests")
class BatchTransactionManagerTest {

    private static final UUID ACCOUNT_A = UUID.randomUUID();
    private static final UUID ACCOUNT_B = UUID.randomUUID();
    private static final UUID ACCOUNT_C = UUID.randomUUID();

    private BatchTransactionManager batchManager;
    private MockedStatic<EconomyManager> economyMock;

    @BeforeEach
    void setUp() {
        // Mock EconomyManager static methods
        economyMock = mockStatic(EconomyManager.class);

        // Mock getInstance to avoid initialization
        MinecraftServer mockServer = mock(MinecraftServer.class);
        EconomyManager mockInstance = mock(EconomyManager.class);
        economyMock.when(EconomyManager::getInstance).thenReturn(mockInstance);

        // Default: all transactions succeed
        economyMock.when(() -> EconomyManager.deposit(any(UUID.class), anyDouble()))
                   .then(invocation -> null);
        economyMock.when(() -> EconomyManager.deposit(any(UUID.class), anyDouble(), any(TransactionType.class), anyString()))
                   .then(invocation -> null);
        economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble()))
                   .thenReturn(true);
        economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(), any(TransactionType.class), anyString()))
                   .thenReturn(true);
        economyMock.when(() -> EconomyManager.transfer(any(UUID.class), any(UUID.class), anyDouble(), anyString()))
                   .thenReturn(true);

        batchManager = new BatchTransactionManager();
    }

    @AfterEach
    void tearDown() {
        economyMock.close();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FLUENT BUILDER API TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Fluent Builder API")
    class FluentBuilderTests {

        @Test
        @DisplayName("deposit() should return BatchTransactionManager for chaining")
        void depositShouldReturnBatchManager() {
            BatchTransactionManager result = batchManager.deposit(ACCOUNT_A, 100.0);

            assertThat(result).isSameAs(batchManager);
        }

        @Test
        @DisplayName("withdraw() should return BatchTransactionManager for chaining")
        void withdrawShouldReturnBatchManager() {
            BatchTransactionManager result = batchManager.withdraw(ACCOUNT_A, 50.0);

            assertThat(result).isSameAs(batchManager);
        }

        @Test
        @DisplayName("transfer() should return BatchTransactionManager for chaining")
        void transferShouldReturnBatchManager() {
            BatchTransactionManager result = batchManager.transfer(ACCOUNT_A, ACCOUNT_B, 75.0);

            assertThat(result).isSameAs(batchManager);
        }

        @Test
        @DisplayName("should support method chaining for multiple operations")
        void shouldSupportMethodChaining() {
            BatchTransactionManager result = batchManager
                .deposit(ACCOUNT_A, 100.0)
                .withdraw(ACCOUNT_B, 50.0)
                .transfer(ACCOUNT_A, ACCOUNT_C, 25.0);

            assertThat(result).isSameAs(batchManager);
            assertThat(batchManager.getQueuedCount()).isEqualTo(3);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BULK OPERATIONS TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperationsTests {

        @Test
        @DisplayName("batchDeposit() should add multiple deposits")
        void batchDepositShouldAddMultipleDeposits() {
            Map<UUID, Double> amounts = new HashMap<>();
            amounts.put(ACCOUNT_A, 100.0);
            amounts.put(ACCOUNT_B, 200.0);
            amounts.put(ACCOUNT_C, 300.0);

            batchManager.batchDeposit(amounts);

            assertThat(batchManager.getQueuedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("batchWithdraw() should add multiple withdrawals")
        void batchWithdrawShouldAddMultipleWithdrawals() {
            Map<UUID, Double> amounts = new HashMap<>();
            amounts.put(ACCOUNT_A, 50.0);
            amounts.put(ACCOUNT_B, 75.0);

            batchManager.batchWithdraw(amounts);

            assertThat(batchManager.getQueuedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("bulk operations should support chaining")
        void bulkOperationsShouldSupportChaining() {
            Map<UUID, Double> deposits = Map.of(ACCOUNT_A, 100.0);
            Map<UUID, Double> withdrawals = Map.of(ACCOUNT_B, 50.0);

            BatchTransactionManager result = batchManager
                .batchDeposit(deposits)
                .batchWithdraw(withdrawals);

            assertThat(result).isSameAs(batchManager);
            assertThat(batchManager.getQueuedCount()).isEqualTo(2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXECUTION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Batch Execution")
    class ExecutionTests {

        @Test
        @DisplayName("execute() should process all deposits successfully")
        void executeShouldProcessDepositsSuccessfully() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .deposit(ACCOUNT_B, 200.0);

            BatchTransactionManager.BatchResult result = batchManager.execute();

            assertThat(result.total).isEqualTo(2);
            assertThat(result.successful).isEqualTo(2);
            assertThat(result.failed).isEqualTo(0);
            assertThat(result.durationMs).isGreaterThanOrEqualTo(0.0);

            economyMock.verify(() -> EconomyManager.deposit(eq(ACCOUNT_A), eq(100.0), any(), any()), times(1));
            economyMock.verify(() -> EconomyManager.deposit(eq(ACCOUNT_B), eq(200.0), any(), any()), times(1));
        }

        @Test
        @DisplayName("execute() should handle withdrawal failures (insufficient funds)")
        void executeShouldHandleWithdrawalFailures() {
            // Mock: ACCOUNT_A withdrawal fails (insufficient funds)
            economyMock.when(() -> EconomyManager.withdraw(eq(ACCOUNT_A), anyDouble(), any(), any()))
                       .thenReturn(false);
            economyMock.when(() -> EconomyManager.withdraw(eq(ACCOUNT_B), anyDouble(), any(), any()))
                       .thenReturn(true);

            batchManager
                .withdraw(ACCOUNT_A, 1000.0)  // Will fail
                .withdraw(ACCOUNT_B, 50.0);    // Will succeed

            BatchTransactionManager.BatchResult result = batchManager.execute();

            assertThat(result.total).isEqualTo(2);
            assertThat(result.successful).isEqualTo(1);
            assertThat(result.failed).isEqualTo(1);
        }

        @Test
        @DisplayName("execute() should handle transfer failures")
        void executeShouldHandleTransferFailures() {
            // Mock: Transfer from ACCOUNT_A fails
            economyMock.when(() -> EconomyManager.transfer(eq(ACCOUNT_A), any(), anyDouble(), any()))
                       .thenReturn(false);
            economyMock.when(() -> EconomyManager.transfer(eq(ACCOUNT_B), any(), anyDouble(), any()))
                       .thenReturn(true);

            batchManager
                .transfer(ACCOUNT_A, ACCOUNT_C, 1000.0)  // Will fail
                .transfer(ACCOUNT_B, ACCOUNT_C, 50.0);    // Will succeed

            BatchTransactionManager.BatchResult result = batchManager.execute();

            assertThat(result.total).isEqualTo(2);
            assertThat(result.successful).isEqualTo(1);
            assertThat(result.failed).isEqualTo(1);
        }

        @Test
        @DisplayName("execute() should clear batch after execution")
        void executeShouldClearBatchAfterExecution() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .withdraw(ACCOUNT_B, 50.0);

            assertThat(batchManager.getQueuedCount()).isEqualTo(2);

            batchManager.execute();

            assertThat(batchManager.isEmpty()).isTrue();
            assertThat(batchManager.getQueuedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("executeAll() should return true when all transactions succeed")
        void executeAllShouldReturnTrueWhenAllSucceed() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .withdraw(ACCOUNT_B, 50.0);

            boolean result = batchManager.executeAll();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("executeAll() should return false when any transaction fails")
        void executeAllShouldReturnFalseWhenAnyFails() {
            economyMock.when(() -> EconomyManager.withdraw(eq(ACCOUNT_A), anyDouble(), any(), any()))
                       .thenReturn(false);

            batchManager
                .deposit(ACCOUNT_B, 100.0)
                .withdraw(ACCOUNT_A, 1000.0);  // Will fail

            boolean result = batchManager.executeAll();

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("execute() should handle mixed deposit/withdraw/transfer batch")
        void executeShouldHandleMixedBatch() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .withdraw(ACCOUNT_B, 50.0)
                .transfer(ACCOUNT_A, ACCOUNT_C, 25.0)
                .deposit(ACCOUNT_C, 200.0);

            BatchTransactionManager.BatchResult result = batchManager.execute();

            assertThat(result.total).isEqualTo(4);
            assertThat(result.successful).isEqualTo(4);
            assertThat(result.failed).isEqualTo(0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // QUERY METHODS TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodsTests {

        @Test
        @DisplayName("isEmpty() should return true for new batch")
        void isEmptyShouldReturnTrueForNewBatch() {
            assertThat(batchManager.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty() should return false after adding transactions")
        void isEmptyShouldReturnFalseAfterAddingTransactions() {
            batchManager.deposit(ACCOUNT_A, 100.0);

            assertThat(batchManager.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("getQueuedCount() should return correct count")
        void getQueuedCountShouldReturnCorrectCount() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .withdraw(ACCOUNT_B, 50.0)
                .transfer(ACCOUNT_A, ACCOUNT_C, 25.0);

            assertThat(batchManager.getQueuedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("clear() should empty the batch without execution")
        void clearShouldEmptyBatchWithoutExecution() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .withdraw(ACCOUNT_B, 50.0);

            assertThat(batchManager.getQueuedCount()).isEqualTo(2);

            batchManager.clear();

            assertThat(batchManager.isEmpty()).isTrue();
            assertThat(batchManager.getQueuedCount()).isEqualTo(0);

            // Verify no EconomyManager calls were made
            economyMock.verifyNoInteractions();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STATISTICS TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Statistics Tracking")
    class StatisticsTests {

        @Test
        @DisplayName("should track total transactions correctly")
        void shouldTrackTotalTransactionsCorrectly() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .withdraw(ACCOUNT_B, 50.0)
                .transfer(ACCOUNT_A, ACCOUNT_C, 25.0);

            batchManager.execute();

            assertThat(batchManager.getTotalTransactions()).isEqualTo(3);
        }

        @Test
        @DisplayName("should track successful transactions correctly")
        void shouldTrackSuccessfulTransactionsCorrectly() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .deposit(ACCOUNT_B, 200.0);

            batchManager.execute();

            assertThat(batchManager.getSuccessfulTransactions()).isEqualTo(2);
            assertThat(batchManager.getFailedTransactions()).isEqualTo(0);
        }

        @Test
        @DisplayName("should track failed transactions correctly")
        void shouldTrackFailedTransactionsCorrectly() {
            economyMock.when(() -> EconomyManager.withdraw(eq(ACCOUNT_A), anyDouble(), any(), any()))
                       .thenReturn(false);

            batchManager
                .deposit(ACCOUNT_B, 100.0)
                .withdraw(ACCOUNT_A, 1000.0);  // Will fail

            batchManager.execute();

            assertThat(batchManager.getTotalTransactions()).isEqualTo(2);
            assertThat(batchManager.getSuccessfulTransactions()).isEqualTo(1);
            assertThat(batchManager.getFailedTransactions()).isEqualTo(1);
        }

        @Test
        @DisplayName("statistics should reset between executions")
        void statisticsShouldResetBetweenExecutions() {
            // First execution
            batchManager.deposit(ACCOUNT_A, 100.0);
            batchManager.execute();
            assertThat(batchManager.getTotalTransactions()).isEqualTo(1);

            // Second execution
            batchManager
                .deposit(ACCOUNT_B, 200.0)
                .deposit(ACCOUNT_C, 300.0);
            batchManager.execute();

            assertThat(batchManager.getTotalTransactions()).isEqualTo(2);
            assertThat(batchManager.getSuccessfulTransactions()).isEqualTo(2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Input Validation")
    class ValidationTests {

        @Test
        @DisplayName("deposit() should reject negative amounts")
        void depositShouldRejectNegativeAmounts() {
            batchManager.deposit(ACCOUNT_A, -100.0);

            assertThat(batchManager.getQueuedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("withdraw() should reject negative amounts")
        void withdrawShouldRejectNegativeAmounts() {
            batchManager.withdraw(ACCOUNT_A, -50.0);

            assertThat(batchManager.getQueuedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("transfer() should reject negative amounts")
        void transferShouldRejectNegativeAmounts() {
            batchManager.transfer(ACCOUNT_A, ACCOUNT_B, -25.0);

            assertThat(batchManager.getQueuedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("should continue chaining after rejected negative amounts")
        void shouldContinueChainingAfterRejectedNegativeAmounts() {
            BatchTransactionManager result = batchManager
                .deposit(ACCOUNT_A, -100.0)  // Rejected
                .deposit(ACCOUNT_B, 200.0);   // Accepted

            assertThat(result).isSameAs(batchManager);
            assertThat(batchManager.getQueuedCount()).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BATCH RESULT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("BatchResult")
    class BatchResultTests {

        @Test
        @DisplayName("BatchResult should contain correct fields")
        void batchResultShouldContainCorrectFields() {
            batchManager.deposit(ACCOUNT_A, 100.0);

            BatchTransactionManager.BatchResult result = batchManager.execute();

            assertThat(result.total).isEqualTo(1);
            assertThat(result.successful).isEqualTo(1);
            assertThat(result.failed).isEqualTo(0);
            assertThat(result.durationMs).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("BatchResult should measure execution duration")
        void batchResultShouldMeasureExecutionDuration() {
            batchManager
                .deposit(ACCOUNT_A, 100.0)
                .deposit(ACCOUNT_B, 200.0)
                .deposit(ACCOUNT_C, 300.0);

            BatchTransactionManager.BatchResult result = batchManager.execute();

            assertThat(result.durationMs).isGreaterThan(0.0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should handle complex batch with 100+ transactions")
        void shouldHandleComplexBatchWith100Transactions() {
            // Add 100 deposits
            for (int i = 0; i < 100; i++) {
                batchManager.deposit(UUID.randomUUID(), 100.0);
            }

            assertThat(batchManager.getQueuedCount()).isEqualTo(100);

            BatchTransactionManager.BatchResult result = batchManager.execute();

            assertThat(result.total).isEqualTo(100);
            assertThat(result.successful).isEqualTo(100);
            assertThat(result.failed).isEqualTo(0);
        }

        @Test
        @DisplayName("should handle realistic salary payment scenario")
        void shouldHandleRealisticSalaryPaymentScenario() {
            Map<UUID, Double> salaries = new HashMap<>();
            for (int i = 0; i < 50; i++) {
                salaries.put(UUID.randomUUID(), 1000.0 + (i * 100.0));
            }

            batchManager.batchDeposit(salaries, TransactionType.SALARY, "Weekly salary payment");

            BatchTransactionManager.BatchResult result = batchManager.execute();

            assertThat(result.total).isEqualTo(50);
            assertThat(result.successful).isEqualTo(50);
            assertThat(result.failed).isEqualTo(0);
        }
    }
}
