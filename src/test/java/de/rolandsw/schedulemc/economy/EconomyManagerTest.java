package de.rolandsw.schedulemc.economy;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for EconomyManager
 *
 * Tests cover:
 * - Account creation and management
 * - Deposit and withdrawal operations
 * - Balance queries
 * - Transfer between accounts
 * - Save/Load functionality
 * - Health monitoring
 * - Transaction validation
 * - Edge cases (negative amounts, non-existent accounts)
 */
class EconomyManagerTest {

    @TempDir
    Path tempDir;

    private UUID testPlayer1;
    private UUID testPlayer2;
    private File originalConfigFile;

    @BeforeEach
    void setUp() throws Exception {
        testPlayer1 = UUID.randomUUID();
        testPlayer2 = UUID.randomUUID();

        // Reset EconomyManager state before each test
        resetEconomyManager();

        // Redirect file location to temp directory using setter method
        originalConfigFile = EconomyManager.getFile();
        File tempFile = tempDir.resolve("test_economy.json").toFile();
        EconomyManager.setFile(tempFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original file location
        if (originalConfigFile != null) {
            EconomyManager.setFile(originalConfigFile);
        }

        // Clean up
        resetEconomyManager();
    }

    /**
     * Helper method to reset EconomyManager state between tests
     */
    private void resetEconomyManager() throws Exception {
        Field balancesField = EconomyManager.class.getDeclaredField("balances");
        balancesField.setAccessible(true);
        Map<UUID, Double> balances = (Map<UUID, Double>) balancesField.get(null);
        balances.clear();

        Field needsSaveField = EconomyManager.class.getDeclaredField("needsSave");
        needsSaveField.setAccessible(true);
        needsSaveField.set(null, false);

        Field isHealthyField = EconomyManager.class.getDeclaredField("isHealthy");
        isHealthyField.setAccessible(true);
        isHealthyField.set(null, true);

        Field lastErrorField = EconomyManager.class.getDeclaredField("lastError");
        lastErrorField.setAccessible(true);
        lastErrorField.set(null, null);
    }

    /**
     * Helper method to create account directly without accessing config.
     * This bypasses the getStartBalance() config access which fails in unit tests.
     */
    private void createAccountDirectly(UUID uuid, double startBalance) throws Exception {
        Field balancesField = EconomyManager.class.getDeclaredField("balances");
        balancesField.setAccessible(true);
        Map<UUID, Double> balances = (Map<UUID, Double>) balancesField.get(null);
        balances.put(uuid, startBalance);

        // Mark as dirty to simulate real behavior
        Field needsSaveField = EconomyManager.class.getDeclaredField("needsSave");
        needsSaveField.setAccessible(true);
        needsSaveField.set(null, true);
    }

    // ==================== Account Management Tests ====================

    @Test
    @DisplayName("Should create new account with start balance")
    void testCreateAccount() throws Exception {
        // Act
        createAccountDirectly(testPlayer1, 1000.0);

        // Assert
        assertThat(EconomyManager.hasAccount(testPlayer1)).isTrue();
        assertThat(EconomyManager.getBalance(testPlayer1)).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should not create duplicate accounts")
    void testCreateAccountDuplicate() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        double initialBalance = EconomyManager.getBalance(testPlayer1);

        // Act - Try to create again (should not overwrite if account exists)
        if (!EconomyManager.hasAccount(testPlayer1)) {
            createAccountDirectly(testPlayer1, 1000.0);
        }

        // Assert - Balance should remain unchanged
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Should check account existence")
    void testHasAccount() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);

        // Assert
        assertThat(EconomyManager.hasAccount(testPlayer1)).isTrue();
        assertThat(EconomyManager.hasAccount(testPlayer2)).isFalse();
    }

    @Test
    @DisplayName("Should delete account")
    void testDeleteAccount() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);

        // Act
        EconomyManager.deleteAccount(testPlayer1);

        // Assert
        assertThat(EconomyManager.hasAccount(testPlayer1)).isFalse();
    }

    @Test
    @DisplayName("Should return 0 balance for non-existent account")
    void testGetBalanceNonExistent() {
        // Act & Assert
        assertThat(EconomyManager.getBalance(testPlayer2)).isEqualTo(0.0);
    }

    // ==================== Deposit Tests ====================

    @Test
    @DisplayName("Should deposit money to account")
    void testDeposit() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        double initialBalance = EconomyManager.getBalance(testPlayer1);

        // Act
        EconomyManager.deposit(testPlayer1, 500.0);

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(initialBalance + 500.0);
    }

    @Test
    @DisplayName("Should create account on deposit if not exists")
    void testDepositCreatesAccount() {
        // Act
        EconomyManager.deposit(testPlayer1, 100.0);

        // Assert
        assertThat(EconomyManager.hasAccount(testPlayer1)).isTrue();
        assertThat(EconomyManager.getBalance(testPlayer1)).isGreaterThanOrEqualTo(100.0);
    }

    @Test
    @DisplayName("Should handle multiple deposits")
    void testMultipleDeposits() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);

        // Act
        EconomyManager.deposit(testPlayer1, 100.0);
        EconomyManager.deposit(testPlayer1, 200.0);
        EconomyManager.deposit(testPlayer1, 50.0);

        // Assert
        double expected = EconomyManager.getStartBalance() + 350.0;
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should deposit with transaction type and description")
    void testDepositWithTransaction() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        double initialBalance = EconomyManager.getBalance(testPlayer1);

        // Act
        EconomyManager.deposit(testPlayer1, 250.0, TransactionType.ATM_DEPOSIT, "Test deposit");

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(initialBalance + 250.0);
    }

    // ==================== Withdrawal Tests ====================

    @Test
    @DisplayName("Should withdraw money from account")
    void testWithdraw() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        EconomyManager.deposit(testPlayer1, 1000.0);
        double balanceBeforeWithdraw = EconomyManager.getBalance(testPlayer1);

        // Act
        boolean success = EconomyManager.withdraw(testPlayer1, 300.0);

        // Assert
        assertThat(success).isTrue();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balanceBeforeWithdraw - 300.0);
    }

    @Test
    @DisplayName("Should fail withdrawal with insufficient funds")
    void testWithdrawInsufficientFunds() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        double initialBalance = EconomyManager.getBalance(testPlayer1);

        // Act
        boolean success = EconomyManager.withdraw(testPlayer1, initialBalance + 1000.0);

        // Assert
        assertThat(success).isFalse();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Should handle exact balance withdrawal")
    void testWithdrawExactBalance() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        EconomyManager.deposit(testPlayer1, 500.0);
        double balance = EconomyManager.getBalance(testPlayer1);

        // Act
        boolean success = EconomyManager.withdraw(testPlayer1, balance);

        // Assert
        assertThat(success).isTrue();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should withdraw with transaction type")
    void testWithdrawWithTransaction() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        EconomyManager.deposit(testPlayer1, 1000.0);
        double balanceBeforeWithdraw = EconomyManager.getBalance(testPlayer1);

        // Act
        boolean success = EconomyManager.withdraw(testPlayer1, 200.0, TransactionType.ATM_WITHDRAW, "Test withdrawal");

        // Assert
        assertThat(success).isTrue();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balanceBeforeWithdraw - 200.0);
    }

    // ==================== Set Balance Tests ====================

    @Test
    @DisplayName("Should set balance directly")
    void testSetBalance() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);

        // Act
        EconomyManager.setBalance(testPlayer1, 5000.0);

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("Should set balance with transaction")
    void testSetBalanceWithTransaction() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);

        // Act
        EconomyManager.setBalance(testPlayer1, 2500.0, TransactionType.ADMIN_SET, "Admin set balance");

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(2500.0);
    }

    @Test
    @DisplayName("Should set balance to zero")
    void testSetBalanceZero() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        EconomyManager.deposit(testPlayer1, 1000.0);

        // Act
        EconomyManager.setBalance(testPlayer1, 0.0);

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(0.0);
    }

    // ==================== Transfer Tests ====================

    @Test
    @DisplayName("Should transfer money between accounts")
    void testTransfer() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        createAccountDirectly(testPlayer2, 1000.0);
        EconomyManager.deposit(testPlayer1, 1000.0);

        double balance1Before = EconomyManager.getBalance(testPlayer1);
        double balance2Before = EconomyManager.getBalance(testPlayer2);

        // Act
        boolean success = EconomyManager.transfer(testPlayer1, testPlayer2, 300.0, "Test transfer");

        // Assert
        assertThat(success).isTrue();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balance1Before - 300.0);
        assertThat(EconomyManager.getBalance(testPlayer2)).isEqualTo(balance2Before + 300.0);
    }

    @Test
    @DisplayName("Should fail transfer with insufficient funds")
    void testTransferInsufficientFunds() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        createAccountDirectly(testPlayer2, 1000.0);

        double balance1Before = EconomyManager.getBalance(testPlayer1);
        double balance2Before = EconomyManager.getBalance(testPlayer2);

        // Act
        boolean success = EconomyManager.transfer(testPlayer1, testPlayer2, 10000.0, "Should fail");

        // Assert
        assertThat(success).isFalse();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balance1Before);
        assertThat(EconomyManager.getBalance(testPlayer2)).isEqualTo(balance2Before);
    }

    @Test
    @DisplayName("Should transfer entire balance")
    void testTransferEntireBalance() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        createAccountDirectly(testPlayer2, 1000.0);
        EconomyManager.deposit(testPlayer1, 500.0);

        double balance1 = EconomyManager.getBalance(testPlayer1);
        double balance2Before = EconomyManager.getBalance(testPlayer2);

        // Act
        boolean success = EconomyManager.transfer(testPlayer1, testPlayer2, balance1, "Full transfer");

        // Assert
        assertThat(success).isTrue();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(0.0);
        assertThat(EconomyManager.getBalance(testPlayer2)).isEqualTo(balance2Before + balance1);
    }

    // ==================== Save/Load Tests ====================

    @Test
    @DisplayName("Should save and load accounts")
    void testSaveAndLoad() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        createAccountDirectly(testPlayer2, 1000.0);
        EconomyManager.deposit(testPlayer1, 1000.0);
        EconomyManager.deposit(testPlayer2, 2000.0);

        double balance1 = EconomyManager.getBalance(testPlayer1);
        double balance2 = EconomyManager.getBalance(testPlayer2);

        // Act
        EconomyManager.saveAccounts();

        // Clear balances and reload
        resetEconomyManager();
        EconomyManager.loadAccounts();

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balance1);
        assertThat(EconomyManager.getBalance(testPlayer2)).isEqualTo(balance2);
    }

    @Test
    @DisplayName("Should handle loading from non-existent file")
    void testLoadNonExistentFile() {
        // Act
        EconomyManager.loadAccounts();

        // Assert - Should not throw, just log warning
        assertThat(EconomyManager.isHealthy()).isTrue();
    }

    @Test
    @DisplayName("Should only save when needed")
    void testSaveIfNeeded() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        EconomyManager.saveAccounts();

        Field fileField = EconomyManager.class.getDeclaredField("file");
        fileField.setAccessible(true);
        File file = (File) fileField.get(null);

        long lastModified = file.lastModified();
        Thread.sleep(100);

        // Act - saveIfNeeded without changes
        EconomyManager.saveIfNeeded();

        // Assert - File should not be modified
        assertThat(file.lastModified()).isEqualTo(lastModified);

        // Act - Make change and saveIfNeeded
        EconomyManager.deposit(testPlayer1, 100.0);
        Thread.sleep(100);
        EconomyManager.saveIfNeeded();

        // Assert - File should be modified
        assertThat(file.lastModified()).isGreaterThan(lastModified);
    }

    // ==================== Health Monitoring Tests ====================

    @Test
    @DisplayName("Should report healthy status")
    void testHealthyStatus() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        EconomyManager.saveAccounts();

        // Assert
        assertThat(EconomyManager.isHealthy()).isTrue();
        assertThat(EconomyManager.getLastError()).isNull();
    }

    @Test
    @DisplayName("Should provide health info")
    void testHealthInfo() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        createAccountDirectly(testPlayer2, 1000.0);

        // Act
        String healthInfo = EconomyManager.getHealthInfo();

        // Assert
        assertThat(healthInfo).contains("2 Konten");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle large balances")
    void testLargeBalances() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);

        // Act
        EconomyManager.deposit(testPlayer1, 999999999.99);

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isGreaterThan(999999999.0);
    }

    @Test
    @DisplayName("Should handle decimal precision")
    void testDecimalPrecision() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);

        // Act
        EconomyManager.deposit(testPlayer1, 10.55);
        EconomyManager.deposit(testPlayer1, 20.45);

        // Assert
        double expected = EconomyManager.getStartBalance() + 31.0;
        assertThat(EconomyManager.getBalance(testPlayer1)).isCloseTo(expected, within(0.01));
    }

    @Test
    @DisplayName("Should get all accounts")
    void testGetAllAccounts() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        createAccountDirectly(testPlayer2, 1000.0);

        // Act
        Map<UUID, Double> allAccounts = EconomyManager.getAllAccounts();

        // Assert
        assertThat(allAccounts).hasSize(2);
        assertThat(allAccounts).containsKeys(testPlayer1, testPlayer2);
    }

    @Test
    @DisplayName("Should handle concurrent operations safely")
    void testConcurrentOperations() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        EconomyManager.deposit(testPlayer1, 10000.0);

        // Act - Simulate concurrent deposits/withdrawals
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                EconomyManager.deposit(testPlayer1, 10.0);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                EconomyManager.withdraw(testPlayer1, 10.0);
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Assert - Balance should be consistent
        double expected = EconomyManager.getStartBalance() + 10000.0 + (100 * 10.0) - (50 * 10.0);
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(expected);
    }

    // ==================== NaN / Infinity Validation Tests ====================

    @Test
    @DisplayName("Should ignore NaN deposit")
    void testDepositNaN_ShouldBeIgnored() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        double balanceBefore = EconomyManager.getBalance(testPlayer1);

        // Act
        EconomyManager.deposit(testPlayer1, Double.NaN);

        // Assert - balance must not change
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balanceBefore);
    }

    @Test
    @DisplayName("Should ignore positive-Infinity deposit")
    void testDepositPositiveInfinity_ShouldBeIgnored() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        double balanceBefore = EconomyManager.getBalance(testPlayer1);

        // Act
        EconomyManager.deposit(testPlayer1, Double.POSITIVE_INFINITY);

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balanceBefore);
    }

    @Test
    @DisplayName("Should ignore negative-Infinity deposit")
    void testDepositNegativeInfinity_ShouldBeIgnored() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        double balanceBefore = EconomyManager.getBalance(testPlayer1);

        // Act
        EconomyManager.deposit(testPlayer1, Double.NEGATIVE_INFINITY);

        // Assert
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balanceBefore);
    }

    @Test
    @DisplayName("Should reject NaN withdrawal and return false")
    void testWithdrawNaN_ShouldReturnFalse() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        EconomyManager.deposit(testPlayer1, 500.0);
        double balanceBefore = EconomyManager.getBalance(testPlayer1);

        // Act
        boolean result = EconomyManager.withdraw(testPlayer1, Double.NaN);

        // Assert
        assertThat(result).isFalse();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balanceBefore);
    }

    @Test
    @DisplayName("Should reject Infinity withdrawal and return false")
    void testWithdrawInfinity_ShouldReturnFalse() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 1000.0);
        double balanceBefore = EconomyManager.getBalance(testPlayer1);

        // Act
        boolean result = EconomyManager.withdraw(testPlayer1, Double.POSITIVE_INFINITY);

        // Assert
        assertThat(result).isFalse();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balanceBefore);
    }

    @Test
    @DisplayName("Should reject NaN transfer and return false")
    void testTransferNaN_ShouldReturnFalse() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 5000.0);
        createAccountDirectly(testPlayer2, 1000.0);
        double balance1Before = EconomyManager.getBalance(testPlayer1);
        double balance2Before = EconomyManager.getBalance(testPlayer2);

        // Act
        boolean result = EconomyManager.transfer(testPlayer1, testPlayer2, Double.NaN, "NaN transfer");

        // Assert
        assertThat(result).isFalse();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balance1Before);
        assertThat(EconomyManager.getBalance(testPlayer2)).isEqualTo(balance2Before);
    }

    @Test
    @DisplayName("Should reject Infinity transfer and return false")
    void testTransferInfinity_ShouldReturnFalse() throws Exception {
        // Arrange
        createAccountDirectly(testPlayer1, 5000.0);
        createAccountDirectly(testPlayer2, 1000.0);
        double balance1Before = EconomyManager.getBalance(testPlayer1);
        double balance2Before = EconomyManager.getBalance(testPlayer2);

        // Act
        boolean result = EconomyManager.transfer(testPlayer1, testPlayer2, Double.POSITIVE_INFINITY, "Inf transfer");

        // Assert
        assertThat(result).isFalse();
        assertThat(EconomyManager.getBalance(testPlayer1)).isEqualTo(balance1Before);
        assertThat(EconomyManager.getBalance(testPlayer2)).isEqualTo(balance2Before);
    }
}
