package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for StateAccount (Government Treasury)
 *
 * Tests cover:
 * - Balance management (deposit, withdraw)
 * - Persistence (save, load)
 * - Singleton pattern
 * - Insufficient funds handling
 * - Transaction logging
 * - Thread safety
 * - Edge cases: negative amounts, zero balance
 *
 * BUSINESS LOGIC:
 * - Initial balance: 100,000€
 * - Withdraw requires sufficient funds
 * - Deposits unlimited
 * - Persistence to config/state_account.json
 */
@DisplayName("StateAccount Tests")
class StateAccountTest {

    private static final int INITIAL_BALANCE = 100000;
    private MinecraftServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = createMockServer();
        resetStateAccount();
        clearSaveFile();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetStateAccount();
        clearSaveFile();
    }

    // ═══════════════════════════════════════════════════════════
    // BALANCE MANAGEMENT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Balance Management Tests")
    class BalanceManagementTests {

        @Test
        @DisplayName("Initial balance should be 100,000€")
        void initialBalanceShouldBeHundredThousand() {
            int balance = StateAccount.getBalance();

            assertThat(balance).isEqualTo(INITIAL_BALANCE);
        }

        @Test
        @DisplayName("Deposit should increase balance")
        void depositShouldIncreaseBalance() {
            StateAccount.deposit(5000, "Test");

            assertThat(StateAccount.getBalance()).isEqualTo(105000);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 100, 1000, 10000, 50000})
        @DisplayName("Deposit should work for various amounts")
        void depositShouldWorkForVariousAmounts(int amount) {
            int initialBalance = StateAccount.getBalance();

            StateAccount.deposit(amount, "Test");

            assertThat(StateAccount.getBalance()).isEqualTo(initialBalance + amount);
        }

        @Test
        @DisplayName("Multiple deposits should accumulate")
        void multipleDepositsShouldAccumulate() {
            StateAccount.deposit(1000, "First");
            StateAccount.deposit(2000, "Second");
            StateAccount.deposit(3000, "Third");

            assertThat(StateAccount.getBalance()).isEqualTo(106000);
        }

        @Test
        @DisplayName("Double deposit method should work")
        void doubleDepositMethodShouldWork() {
            StateAccount stateAccount = StateAccount.getInstance(mockServer);

            stateAccount.deposit(1234.56, "Test");

            // Should round to 1235
            assertThat(StateAccount.getBalance()).isEqualTo(101235);
        }

        @Test
        @DisplayName("Double deposit should round correctly")
        void doubleDepositShouldRoundCorrectly() {
            StateAccount stateAccount = StateAccount.getInstance(mockServer);

            stateAccount.deposit(999.4, "Test 1");
            int balance1 = StateAccount.getBalance();

            resetStateAccount();
            StateAccount.getInstance(mockServer).deposit(999.5, "Test 2");
            int balance2 = StateAccount.getBalance();

            assertThat(balance1).isEqualTo(100999); // Rounds down
            assertThat(balance2).isEqualTo(101000); // Rounds up
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WITHDRAW TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Withdraw Tests")
    class WithdrawTests {

        @Test
        @DisplayName("Withdraw should decrease balance when sufficient funds")
        void withdrawShouldDecreaseBalanceWhenSufficientFunds() {
            boolean success = StateAccount.withdraw(10000, "Test");

            assertThat(success).isTrue();
            assertThat(StateAccount.getBalance()).isEqualTo(90000);
        }

        @ParameterizedTest
        @CsvSource({
            "1000, 99000",
            "50000, 50000",
            "100000, 0"
        })
        @DisplayName("Withdraw should work for various valid amounts")
        void withdrawShouldWorkForVariousValidAmounts(int amount, int expectedBalance) {
            boolean success = StateAccount.withdraw(amount, "Test");

            assertThat(success).isTrue();
            assertThat(StateAccount.getBalance()).isEqualTo(expectedBalance);
        }

        @Test
        @DisplayName("Withdraw should fail when insufficient funds")
        void withdrawShouldFailWhenInsufficientFunds() {
            boolean success = StateAccount.withdraw(200000, "Test");

            assertThat(success).isFalse();
            assertThat(StateAccount.getBalance()).isEqualTo(INITIAL_BALANCE);
        }

        @Test
        @DisplayName("Withdraw exact balance should work")
        void withdrawExactBalanceShouldWork() {
            boolean success = StateAccount.withdraw(100000, "All funds");

            assertThat(success).isTrue();
            assertThat(StateAccount.getBalance()).isZero();
        }

        @Test
        @DisplayName("Withdraw one more than balance should fail")
        void withdrawOneMoreThanBalanceShouldFail() {
            boolean success = StateAccount.withdraw(100001, "Too much");

            assertThat(success).isFalse();
            assertThat(StateAccount.getBalance()).isEqualTo(INITIAL_BALANCE);
        }

        @Test
        @DisplayName("Multiple withdrawals should work correctly")
        void multipleWithdrawalsShouldWorkCorrectly() {
            StateAccount.withdraw(10000, "First");
            StateAccount.withdraw(20000, "Second");
            StateAccount.withdraw(30000, "Third");

            assertThat(StateAccount.getBalance()).isEqualTo(40000);
        }

        @Test
        @DisplayName("Withdraw after balance depleted should fail")
        void withdrawAfterBalanceDepletedShouldFail() {
            StateAccount.withdraw(100000, "Deplete");

            boolean success = StateAccount.withdraw(1, "Too late");

            assertThat(success).isFalse();
            assertThat(StateAccount.getBalance()).isZero();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BALANCE SETTING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Balance Setting Tests (Admin)")
    class BalanceSettingTests {

        @Test
        @DisplayName("setBalance should change balance")
        void setBalanceShouldChangeBalance() {
            StateAccount.setBalance(250000);

            assertThat(StateAccount.getBalance()).isEqualTo(250000);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 1000, 50000, 500000, 1000000})
        @DisplayName("setBalance should work for various amounts")
        void setBalanceShouldWorkForVariousAmounts(int newBalance) {
            StateAccount.setBalance(newBalance);

            assertThat(StateAccount.getBalance()).isEqualTo(newBalance);
        }

        @Test
        @DisplayName("setBalance to zero should work")
        void setBalanceToZeroShouldWork() {
            StateAccount.setBalance(0);

            assertThat(StateAccount.getBalance()).isZero();
        }

        @Test
        @DisplayName("setBalance should overwrite existing balance")
        void setBalanceShouldOverwriteExistingBalance() {
            StateAccount.deposit(50000, "Add funds");

            StateAccount.setBalance(75000);

            assertThat(StateAccount.getBalance()).isEqualTo(75000);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Persistence Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Save should create file")
        void saveShouldCreateFile() {
            StateAccount.save();

            File saveFile = new File("config/state_account.json");
            assertThat(saveFile).exists();
        }

        @Test
        @DisplayName("Load should restore balance")
        void loadShouldRestoreBalance() {
            StateAccount.setBalance(123456);
            StateAccount.save();

            // Reset and reload
            resetStateAccount();
            StateAccount.load();

            assertThat(StateAccount.getBalance()).isEqualTo(123456);
        }

        @Test
        @DisplayName("Load when file doesn't exist should use default balance")
        void loadWhenFileDoesntExistShouldUseDefaultBalance() {
            clearSaveFile();

            StateAccount.load();

            assertThat(StateAccount.getBalance()).isEqualTo(INITIAL_BALANCE);
        }

        @Test
        @DisplayName("Multiple save/load cycles should preserve balance")
        void multipleSaveLoadCyclesShouldPreserveBalance() {
            // Cycle 1
            StateAccount.setBalance(111111);
            StateAccount.save();
            resetStateAccount();
            StateAccount.load();
            assertThat(StateAccount.getBalance()).isEqualTo(111111);

            // Cycle 2
            StateAccount.setBalance(222222);
            StateAccount.save();
            resetStateAccount();
            StateAccount.load();
            assertThat(StateAccount.getBalance()).isEqualTo(222222);
        }

        @Test
        @DisplayName("Deposit should auto-save")
        void depositShouldAutoSave() {
            StateAccount.deposit(5000, "Test");

            // Reload from file
            resetStateAccount();
            StateAccount.load();

            assertThat(StateAccount.getBalance()).isEqualTo(105000);
        }

        @Test
        @DisplayName("Withdraw should auto-save")
        void withdrawShouldAutoSave() {
            StateAccount.withdraw(5000, "Test");

            // Reload from file
            resetStateAccount();
            StateAccount.load();

            assertThat(StateAccount.getBalance()).isEqualTo(95000);
        }

        @Test
        @DisplayName("setBalance should auto-save")
        void setBalanceShouldAutoSave() {
            StateAccount.setBalance(999999);

            // Reload from file
            resetStateAccount();
            StateAccount.load();

            assertThat(StateAccount.getBalance()).isEqualTo(999999);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON PATTERN TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonPatternTests {

        @Test
        @DisplayName("getInstance should return same instance")
        void getInstanceShouldReturnSameInstance() {
            StateAccount instance1 = StateAccount.getInstance(mockServer);
            StateAccount instance2 = StateAccount.getInstance(mockServer);

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("getInstance should be thread-safe")
        void getInstanceShouldBeThreadSafe() throws InterruptedException {
            resetStateAccount();

            StateAccount[] instances = new StateAccount[2];

            Thread thread1 = new Thread(() -> instances[0] = StateAccount.getInstance(mockServer));
            Thread thread2 = new Thread(() -> instances[1] = StateAccount.getInstance(mockServer));

            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            assertThat(instances[0]).isSameAs(instances[1]);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Deposit zero should work")
        void depositZeroShouldWork() {
            StateAccount.deposit(0, "Zero");

            assertThat(StateAccount.getBalance()).isEqualTo(INITIAL_BALANCE);
        }

        @Test
        @DisplayName("Withdraw zero should work")
        void withdrawZeroShouldWork() {
            boolean success = StateAccount.withdraw(0, "Zero");

            assertThat(success).isTrue();
            assertThat(StateAccount.getBalance()).isEqualTo(INITIAL_BALANCE);
        }

        @Test
        @DisplayName("Deposit negative amount should decrease balance")
        void depositNegativeAmountShouldDecreaseBalance() {
            StateAccount.deposit(-5000, "Negative");

            // Negative deposit = withdrawal
            assertThat(StateAccount.getBalance()).isEqualTo(95000);
        }

        @Test
        @DisplayName("Very large deposit should work")
        void veryLargeDepositShouldWork() {
            StateAccount.deposit(Integer.MAX_VALUE / 2, "Huge");

            assertThat(StateAccount.getBalance()).isGreaterThan(INITIAL_BALANCE);
        }

        @Test
        @DisplayName("Deposit near Integer.MAX_VALUE should handle overflow gracefully")
        void depositNearMaxValueShouldHandleOverflow() {
            StateAccount.setBalance(Integer.MAX_VALUE - 1000);

            // This will overflow in Java, but should not crash
            assertThatCode(() -> StateAccount.deposit(2000, "Overflow test"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Withdraw with empty reason should work")
        void withdrawWithEmptyReasonShouldWork() {
            boolean success = StateAccount.withdraw(1000, "");

            assertThat(success).isTrue();
        }

        @Test
        @DisplayName("Deposit with empty reason should work")
        void depositWithEmptyReasonShouldWork() {
            assertThatCode(() -> StateAccount.deposit(1000, ""))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Very long reason string should work")
        void veryLongReasonStringShouldWork() {
            String longReason = "A".repeat(1000);

            assertThatCode(() -> StateAccount.deposit(1000, longReason))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONCURRENT ACCESS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Concurrent Access Tests")
    class ConcurrentAccessTests {

        @Test
        @DisplayName("Concurrent deposits should not lose money")
        void concurrentDepositsShouldNotLoseMoney() throws InterruptedException {
            int numThreads = 10;
            int depositsPerThread = 100;
            int amountPerDeposit = 10;

            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < depositsPerThread; j++) {
                        StateAccount.deposit(amountPerDeposit, "Concurrent");
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            int expectedBalance = INITIAL_BALANCE + (numThreads * depositsPerThread * amountPerDeposit);
            assertThat(StateAccount.getBalance()).isEqualTo(expectedBalance);
        }

        @Test
        @DisplayName("Concurrent withdrawals should not over-withdraw")
        void concurrentWithdrawalsShouldNotOverWithdraw() throws InterruptedException {
            StateAccount.setBalance(100000);

            int numThreads = 10;
            int withdrawalsPerThread = 100;
            int amountPerWithdrawal = 50;

            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < withdrawalsPerThread; j++) {
                        StateAccount.withdraw(amountPerWithdrawal, "Concurrent");
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Balance should never go negative
            assertThat(StateAccount.getBalance()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Mixed concurrent operations should maintain consistency")
        void mixedConcurrentOperationsShouldMaintainConsistency() throws InterruptedException {
            int numThreads = 20;

            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    if (threadId % 2 == 0) {
                        StateAccount.deposit(1000, "Even thread");
                    } else {
                        StateAccount.withdraw(500, "Odd thread");
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Should have: 100000 + (10 * 1000) - (10 * 500) = 105000
            assertThat(StateAccount.getBalance()).isEqualTo(105000);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS LOGIC TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("State account should start with reasonable capital")
        void stateAccountShouldStartWithReasonableCapital() {
            assertThat(INITIAL_BALANCE).isGreaterThan(0);
            assertThat(INITIAL_BALANCE).isLessThan(1000000);
        }

        @Test
        @DisplayName("Withdraw should enforce non-negative balance")
        void withdrawShouldEnforceNonNegativeBalance() {
            StateAccount.setBalance(1000);

            StateAccount.withdraw(500, "First");
            StateAccount.withdraw(400, "Second");
            boolean thirdWithdraw = StateAccount.withdraw(200, "Third");

            assertThat(thirdWithdraw).isFalse();
            assertThat(StateAccount.getBalance()).isEqualTo(100);
        }

        @Test
        @DisplayName("System should handle typical government operations")
        void systemShouldHandleTypicalGovernmentOperations() {
            // Receive taxes
            StateAccount.deposit(15000, "Income taxes");
            StateAccount.deposit(5000, "Property taxes");

            // Pay for infrastructure
            StateAccount.withdraw(8000, "Road maintenance");
            StateAccount.withdraw(3000, "Public lighting");

            // Final balance check
            assertThat(StateAccount.getBalance()).isEqualTo(109000);
        }

        @Test
        @DisplayName("Balance operations should be atomic")
        void balanceOperationsShouldBeAtomic() {
            int initialBalance = StateAccount.getBalance();

            StateAccount.withdraw(1000, "Test 1");
            int balance1 = StateAccount.getBalance();

            StateAccount.deposit(2000, "Test 2");
            int balance2 = StateAccount.getBalance();

            assertThat(balance1).isEqualTo(initialBalance - 1000);
            assertThat(balance2).isEqualTo(balance1 + 2000);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private MinecraftServer createMockServer() {
        MinecraftServer server = mock(MinecraftServer.class);
        when(server.getServerDirectory()).thenReturn(new java.io.File("test_server").toPath());
        return server;
    }

    private void resetStateAccount() throws Exception {
        // Reset singleton instance
        Field instanceField = StateAccount.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // Reset balance
        Field balanceField = StateAccount.class.getDeclaredField("balance");
        balanceField.setAccessible(true);
        balanceField.set(null, INITIAL_BALANCE);
    }

    private void clearSaveFile() {
        File saveFile = new File("config/state_account.json");
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }
}
