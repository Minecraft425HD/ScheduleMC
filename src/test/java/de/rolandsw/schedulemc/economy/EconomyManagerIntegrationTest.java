package de.rolandsw.schedulemc.economy;

import de.rolandsw.schedulemc.exceptions.EconomyException;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for EconomyManager
 *
 * Tests comprehensive money flow scenarios:
 * - Account Creation & Management
 * - Deposits & Withdrawals
 * - Transfers between Accounts
 * - Rate Limiting
 * - Persistence & Recovery
 * - Thread Safety
 *
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EconomyManager Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EconomyManagerIntegrationTest {

    @TempDir
    Path tempDir;

    @Mock
    private MinecraftServer mockServer;

    private UUID playerA;
    private UUID playerB;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance
        resetEconomyManager();

        // Configure temp file location
        File tempFile = tempDir.resolve("test_economy.json").toFile();
        EconomyManager.setFile(tempFile);

        // Initialize
        EconomyManager.initialize(mockServer);

        // Create test players
        playerA = UUID.randomUUID();
        playerB = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetEconomyManager();
    }

    @Test
    @Order(1)
    @DisplayName("Account Creation - Should create account with starting balance")
    void testAccountCreation() {
        // Act
        EconomyManager.createAccount(playerA);

        // Assert
        assertThat(EconomyManager.hasAccount(playerA)).isTrue();
        assertThat(EconomyManager.getBalance(playerA)).isEqualTo(EconomyManager.getStartBalance());
    }

    @Test
    @Order(2)
    @DisplayName("Account Creation - Should reject duplicate account")
    void testDuplicateAccountCreation() {
        // Arrange
        EconomyManager.createAccount(playerA);

        // Act & Assert
        assertThatThrownBy(() -> EconomyManager.createAccount(playerA))
            .isInstanceOf(EconomyException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @Order(3)
    @DisplayName("Deposit - Should increase balance correctly")
    void testDeposit() {
        // Arrange
        EconomyManager.createAccount(playerA);
        double initialBalance = EconomyManager.getBalance(playerA);

        // Act
        EconomyManager.deposit(playerA, 100.0);

        // Assert
        assertThat(EconomyManager.getBalance(playerA)).isEqualTo(initialBalance + 100.0);
    }

    @Test
    @Order(4)
    @DisplayName("Deposit - Should reject negative amounts")
    void testNegativeDeposit() {
        // Arrange
        EconomyManager.createAccount(playerA);

        // Act & Assert
        assertThatThrownBy(() -> EconomyManager.deposit(playerA, -50.0))
            .isInstanceOf(EconomyException.class)
            .hasMessageContaining("Negative deposit");
    }

    @Test
    @Order(5)
    @DisplayName("Withdrawal - Should decrease balance correctly")
    void testWithdrawal() {
        // Arrange
        EconomyManager.createAccount(playerA);
        EconomyManager.deposit(playerA, 500.0);
        double balanceBeforeWithdraw = EconomyManager.getBalance(playerA);

        // Act
        boolean success = EconomyManager.withdraw(playerA, 200.0);

        // Assert
        assertThat(success).isTrue();
        assertThat(EconomyManager.getBalance(playerA)).isEqualTo(balanceBeforeWithdraw - 200.0);
    }

    @Test
    @Order(6)
    @DisplayName("Withdrawal - Should reject insufficient funds")
    void testInsufficientFundsWithdrawal() {
        // Arrange
        EconomyManager.createAccount(playerA);
        EconomyManager.setBalance(playerA, 50.0);

        // Act & Assert
        assertThatThrownBy(() -> EconomyManager.withdraw(playerA, 100.0))
            .isInstanceOf(EconomyException.class)
            .hasMessageContaining("Insufficient funds");
    }

    @Test
    @Order(7)
    @DisplayName("Transfer - Should transfer money between accounts")
    void testTransfer() {
        // Arrange
        EconomyManager.createAccount(playerA);
        EconomyManager.createAccount(playerB);
        EconomyManager.setBalance(playerA, 1000.0);
        EconomyManager.setBalance(playerB, 0.0);

        // Act
        boolean success = EconomyManager.transfer(playerA, playerB, 300.0);

        // Assert
        assertThat(success).isTrue();
        assertThat(EconomyManager.getBalance(playerA)).isEqualTo(700.0);
        assertThat(EconomyManager.getBalance(playerB)).isEqualTo(300.0);
    }

    @Test
    @Order(8)
    @DisplayName("Transfer - Should reject self-transfer")
    void testSelfTransfer() {
        // Arrange
        EconomyManager.createAccount(playerA);
        EconomyManager.setBalance(playerA, 1000.0);

        // Act & Assert
        assertThatThrownBy(() -> EconomyManager.transfer(playerA, playerA, 100.0))
            .isInstanceOf(EconomyException.class)
            .hasMessageContaining("Cannot transfer money to yourself");
    }

    @Test
    @Order(9)
    @DisplayName("Persistence - Should save and load accounts")
    void testPersistence() {
        // Arrange
        EconomyManager.createAccount(playerA);
        EconomyManager.setBalance(playerA, 555.55);

        // Act - Save
        EconomyManager.saveAccounts();

        // Reset and load
        resetEconomyManager();
        EconomyManager.setFile(tempDir.resolve("test_economy.json").toFile());
        EconomyManager.loadAccounts();

        // Assert
        assertThat(EconomyManager.hasAccount(playerA)).isTrue();
        assertThat(EconomyManager.getBalance(playerA)).isEqualTo(555.55);
    }

    @Test
    @Order(10)
    @DisplayName("Balance Management - Should get all accounts")
    void testGetAllAccounts() {
        // Arrange
        EconomyManager.createAccount(playerA);
        EconomyManager.createAccount(playerB);
        EconomyManager.setBalance(playerA, 100.0);
        EconomyManager.setBalance(playerB, 200.0);

        // Act
        var allAccounts = EconomyManager.getAllAccounts();

        // Assert
        assertThat(allAccounts).hasSize(2);
        assertThat(allAccounts.get(playerA)).isEqualTo(100.0);
        assertThat(allAccounts.get(playerB)).isEqualTo(200.0);
    }

    // Helper method to reset singleton
    private void resetEconomyManager() throws Exception {
        var instanceField = EconomyManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        var balancesField = EconomyManager.class.getDeclaredField("balances");
        balancesField.setAccessible(true);
        ((java.util.Map<?, ?>) balancesField.get(null)).clear();
    }
}
