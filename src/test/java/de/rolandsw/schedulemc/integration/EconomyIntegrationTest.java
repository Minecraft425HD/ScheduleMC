package de.rolandsw.schedulemc.integration;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Tests für Economy-Transaktionsflüsse
 *
 * Testet komplette End-to-End Szenarien:
 * - Spieler erstellt Account → Deposit → Withdraw → Transfer
 * - Mehrere Spieler interagieren
 * - Save/Load mit realen Daten
 * - Transaktions-Historie
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EconomyIntegrationTest {

    private static UUID player1;
    private static UUID player2;
    private static UUID player3;

    @BeforeAll
    static void setUpAll() {
        player1 = UUID.randomUUID();
        player2 = UUID.randomUUID();
        player3 = UUID.randomUUID();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Reset EconomyManager state
        resetEconomyManager();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetEconomyManager();
    }

    // ==================== Scenario 1: New Player Journey ====================

    @Test
    @Order(1)
    @DisplayName("Scenario: New player creates account and receives money")
    void scenarioNewPlayerJourney() {
        // Act - Player joins and gets account
        EconomyManager.createAccount(player1);
        double startBalance = EconomyManager.getBalance(player1);

        // Player receives welcome bonus
        EconomyManager.deposit(player1, 500.0, TransactionType.ATM_DEPOSIT, "Welcome bonus");

        // Player buys something
        boolean success = EconomyManager.withdraw(player1, 100.0, TransactionType.NPC_PURCHASE, "Bought plot");

        // Assert
        assertThat(EconomyManager.hasAccount(player1)).isTrue();
        assertThat(EconomyManager.getBalance(player1)).isEqualTo(startBalance + 500.0 - 100.0);
        assertThat(success).isTrue();
    }

    // ==================== Scenario 2: Player-to-Player Trading ====================

    @Test
    @Order(2)
    @DisplayName("Scenario: Two players trade money")
    void scenarioPlayerTrading() {
        // Arrange - Both players have accounts
        EconomyManager.createAccount(player1);
        EconomyManager.createAccount(player2);
        EconomyManager.deposit(player1, 1000.0);
        EconomyManager.deposit(player2, 500.0);

        double balance1Before = EconomyManager.getBalance(player1);
        double balance2Before = EconomyManager.getBalance(player2);

        // Act - Player1 buys item from Player2 for 250
        boolean transferSuccess = EconomyManager.transfer(player1, player2, 250.0, "Bought tobacco");

        // Assert
        assertThat(transferSuccess).isTrue();
        assertThat(EconomyManager.getBalance(player1)).isEqualTo(balance1Before - 250.0);
        assertThat(EconomyManager.getBalance(player2)).isEqualTo(balance2Before + 250.0);
    }

    // ==================== Scenario 3: Multi-Player Economy ====================

    @Test
    @Order(3)
    @DisplayName("Scenario: Three players in complex economy")
    void scenarioMultiPlayerEconomy() {
        // Arrange - Server economy simulation
        EconomyManager.createAccount(player1); // Rich player
        EconomyManager.createAccount(player2); // Medium player
        EconomyManager.createAccount(player3); // Poor player

        EconomyManager.deposit(player1, 10000.0, TransactionType.ADMIN_GIVE, "Admin gift");
        EconomyManager.deposit(player2, 5000.0, TransactionType.ATM_DEPOSIT, "Salary");
        EconomyManager.deposit(player3, 100.0, TransactionType.ATM_DEPOSIT, "Start bonus");

        // Act - Complex transaction chain
        // Player1 buys from Player2
        EconomyManager.transfer(player1, player2, 500.0, "Plot purchase");

        // Player2 pays Player3 for service
        EconomyManager.transfer(player2, player3, 200.0, "Farming service");

        // Player3 buys from Player1
        EconomyManager.transfer(player3, player1, 50.0, "Item purchase");

        // Player1 withdraws cash
        EconomyManager.withdraw(player1, 1000.0, TransactionType.ATM_WITHDRAW, "ATM withdrawal");

        // Assert - Verify all balances
        assertThat(EconomyManager.getBalance(player1)).isEqualTo(10000.0 - 500.0 + 50.0 - 1000.0);
        assertThat(EconomyManager.getBalance(player2)).isEqualTo(5000.0 + 500.0 - 200.0);
        assertThat(EconomyManager.getBalance(player3)).isEqualTo(100.0 + 200.0 - 50.0);
    }

    // ==================== Scenario 4: Save and Load Persistence ====================

    @Test
    @Order(4)
    @DisplayName("Scenario: Economy persists through save/load")
    void scenarioSaveLoadPersistence() throws Exception {
        // Arrange - Create economy state
        EconomyManager.createAccount(player1);
        EconomyManager.createAccount(player2);
        EconomyManager.deposit(player1, 1500.0);
        EconomyManager.deposit(player2, 2500.0);

        double balance1 = EconomyManager.getBalance(player1);
        double balance2 = EconomyManager.getBalance(player2);

        // Act - Save
        EconomyManager.saveAccounts();

        // Reset and reload
        resetEconomyManager();
        EconomyManager.loadAccounts();

        // Assert - Data should be restored
        assertThat(EconomyManager.getBalance(player1)).isEqualTo(balance1);
        assertThat(EconomyManager.getBalance(player2)).isEqualTo(balance2);
    }

    // ==================== Scenario 5: Insufficient Funds Protection ====================

    @Test
    @Order(5)
    @DisplayName("Scenario: System prevents overdraft")
    void scenarioOverdraftPrevention() {
        // Arrange
        EconomyManager.createAccount(player1);
        EconomyManager.deposit(player1, 100.0);

        double balanceBefore = EconomyManager.getBalance(player1);

        // Act - Try to withdraw more than balance
        boolean withdrawSuccess = EconomyManager.withdraw(player1, 200.0);

        // Try to transfer more than balance
        EconomyManager.createAccount(player2);
        boolean transferSuccess = EconomyManager.transfer(player1, player2, 200.0, "Overspend");

        // Assert - Both should fail, balance unchanged
        assertThat(withdrawSuccess).isFalse();
        assertThat(transferSuccess).isFalse();
        assertThat(EconomyManager.getBalance(player1)).isEqualTo(balanceBefore);
    }

    // ==================== Scenario 6: Mass Transactions ====================

    @Test
    @Order(6)
    @DisplayName("Scenario: System handles many transactions")
    void scenarioMassTransactions() {
        // Arrange
        EconomyManager.createAccount(player1);
        EconomyManager.deposit(player1, 100000.0);

        // Act - 1000 small transactions
        for (int i = 0; i < 1000; i++) {
            if (i % 2 == 0) {
                EconomyManager.deposit(player1, 10.0);
            } else {
                EconomyManager.withdraw(player1, 5.0);
            }
        }

        // Assert - Balance should be correct
        double expected = 100000.0 + (500 * 10.0) - (500 * 5.0); // +5000 - 2500 = +2500
        double startBalance = EconomyManager.getStartBalance();
        assertThat(EconomyManager.getBalance(player1)).isEqualTo(expected);
    }

    // ==================== Scenario 7: Admin Operations ====================

    @Test
    @Order(7)
    @DisplayName("Scenario: Admin manages economy")
    void scenarioAdminOperations() {
        // Arrange
        EconomyManager.createAccount(player1);

        // Act - Admin sets balance directly
        EconomyManager.setBalance(player1, 5000.0, TransactionType.ADMIN_SET, "Admin correction");

        // Admin gives bonus
        EconomyManager.deposit(player1, 1000.0, TransactionType.ADMIN_GIVE, "Event reward");

        // Admin removes funds (penalty)
        EconomyManager.withdraw(player1, 500.0, TransactionType.ADMIN_TAKE, "Rule violation penalty");

        // Assert
        assertThat(EconomyManager.getBalance(player1)).isEqualTo(5500.0);
    }

    // ==================== Scenario 8: Account Lifecycle ====================

    @Test
    @Order(8)
    @DisplayName("Scenario: Complete account lifecycle")
    void scenarioAccountLifecycle() {
        // Act - Create
        EconomyManager.createAccount(player1);
        assertThat(EconomyManager.hasAccount(player1)).isTrue();

        // Use account
        EconomyManager.deposit(player1, 1000.0);
        assertThat(EconomyManager.getBalance(player1)).isGreaterThan(0);

        // Delete account
        EconomyManager.deleteAccount(player1);

        // Assert - Account gone
        assertThat(EconomyManager.hasAccount(player1)).isFalse();
        assertThat(EconomyManager.getBalance(player1)).isEqualTo(0.0);
    }

    // ==================== Helper Methods ====================

    private void resetEconomyManager() throws Exception {
        Field balancesField = EconomyManager.class.getDeclaredField("balances");
        balancesField.setAccessible(true);
        java.util.Map<UUID, Double> balances = (java.util.Map<UUID, Double>) balancesField.get(null);
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
}
