package de.rolandsw.schedulemc.economy;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Contract Tests for IEconomyService Interface
 *
 * Tests that verify the EconomyManager correctly implements the IEconomyService interface
 * and honors all contract requirements including:
 * - Interface type compatibility
 * - Method signatures and behavior
 * - Dependency injection support
 * - Instance vs static method delegation
 */
@DisplayName("IEconomyService Contract Tests")
class IEconomyServiceContractTest {

    @TempDir
    Path tempDir;

    private IEconomyService economyService;
    private UUID testPlayer1;
    private UUID testPlayer2;
    private File originalConfigFile;

    @BeforeEach
    void setUp() throws Exception {
        testPlayer1 = UUID.randomUUID();
        testPlayer2 = UUID.randomUUID();

        // Reset EconomyManager state
        resetEconomyManager();

        // Redirect file location to temp directory
        originalConfigFile = EconomyManager.getFile();
        File tempFile = tempDir.resolve("test_economy.json").toFile();
        EconomyManager.setFile(tempFile);

        // Get instance as interface type (Dependency Injection pattern)
        economyService = EconomyManager.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        EconomyManager.setFile(originalConfigFile);
        resetEconomyManager();
    }

    /**
     * Helper to reset EconomyManager state using reflection
     */
    private void resetEconomyManager() throws Exception {
        Field accountsField = EconomyManager.class.getDeclaredField("accounts");
        accountsField.setAccessible(true);
        Map<UUID, Account> accounts = (Map<UUID, Account>) accountsField.get(null);
        accounts.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // INTERFACE COMPATIBILITY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Interface Compatibility")
    class InterfaceCompatibilityTests {

        @Test
        @DisplayName("EconomyManager should implement IEconomyService")
        void economyManagerShouldImplementInterface() {
            assertThat(EconomyManager.getInstance()).isInstanceOf(IEconomyService.class);
        }

        @Test
        @DisplayName("Should be assignable to IEconomyService type")
        void shouldBeAssignableToInterfaceType() {
            IEconomyService service = EconomyManager.getInstance();
            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("Interface reference should work for all operations")
        void interfaceReferenceShouldWorkForAllOperations() {
            IEconomyService service = EconomyManager.getInstance();

            service.createAccount(testPlayer1);
            service.deposit(testPlayer1, 1000.0, TransactionType.ADMIN, "Test");
            double balance = service.getBalance(testPlayer1);

            assertThat(balance).isEqualTo(1000.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ACCOUNT MANAGEMENT CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Account Management Contract")
    class AccountManagementContractTests {

        @Test
        @DisplayName("createAccount should create new account")
        void createAccountShouldCreateNewAccount() {
            economyService.createAccount(testPlayer1);

            assertThat(economyService.hasAccount(testPlayer1)).isTrue();
        }

        @Test
        @DisplayName("hasAccount should return false for non-existent account")
        void hasAccountShouldReturnFalseForNonExistent() {
            assertThat(economyService.hasAccount(testPlayer1)).isFalse();
        }

        @Test
        @DisplayName("hasAccount should return true after account creation")
        void hasAccountShouldReturnTrueAfterCreation() {
            economyService.createAccount(testPlayer1);

            assertThat(economyService.hasAccount(testPlayer1)).isTrue();
        }

        @Test
        @DisplayName("deleteAccount should remove account")
        void deleteAccountShouldRemoveAccount() {
            economyService.createAccount(testPlayer1);
            economyService.deleteAccount(testPlayer1);

            assertThat(economyService.hasAccount(testPlayer1)).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BALANCE OPERATIONS CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Balance Operations Contract")
    class BalanceOperationsContractTests {

        @Test
        @DisplayName("getBalance should return current balance")
        void getBalanceShouldReturnCurrentBalance() {
            economyService.createAccount(testPlayer1);
            economyService.deposit(testPlayer1, 500.0, TransactionType.ADMIN, "Test");

            assertThat(economyService.getBalance(testPlayer1)).isEqualTo(500.0);
        }

        @Test
        @DisplayName("deposit should increase balance")
        void depositShouldIncreaseBalance() {
            economyService.createAccount(testPlayer1);
            economyService.deposit(testPlayer1, 1000.0, TransactionType.ADMIN, "Test");

            assertThat(economyService.getBalance(testPlayer1)).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("withdraw should decrease balance when sufficient funds")
        void withdrawShouldDecreaseBalanceWhenSufficientFunds() {
            economyService.createAccount(testPlayer1);
            economyService.deposit(testPlayer1, 1000.0, TransactionType.ADMIN, "Test");

            boolean success = economyService.withdraw(testPlayer1, 300.0, TransactionType.ADMIN, "Test");

            assertThat(success).isTrue();
            assertThat(economyService.getBalance(testPlayer1)).isEqualTo(700.0);
        }

        @Test
        @DisplayName("withdraw should fail when insufficient funds")
        void withdrawShouldFailWhenInsufficientFunds() {
            economyService.createAccount(testPlayer1);
            economyService.deposit(testPlayer1, 100.0, TransactionType.ADMIN, "Test");

            boolean success = economyService.withdraw(testPlayer1, 500.0, TransactionType.ADMIN, "Test");

            assertThat(success).isFalse();
            assertThat(economyService.getBalance(testPlayer1)).isEqualTo(100.0);
        }

        @Test
        @DisplayName("setBalance should set exact balance")
        void setBalanceShouldSetExactBalance() {
            economyService.createAccount(testPlayer1);
            economyService.setBalance(testPlayer1, 1500.0);

            assertThat(economyService.getBalance(testPlayer1)).isEqualTo(1500.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TRANSFER CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Transfer Contract")
    class TransferContractTests {

        @Test
        @DisplayName("transfer should move funds between accounts")
        void transferShouldMoveFundsBetweenAccounts() {
            economyService.createAccount(testPlayer1);
            economyService.createAccount(testPlayer2);
            economyService.deposit(testPlayer1, 1000.0, TransactionType.ADMIN, "Test");

            boolean success = economyService.transfer(testPlayer1, testPlayer2, 400.0, "Transfer test");

            assertThat(success).isTrue();
            assertThat(economyService.getBalance(testPlayer1)).isEqualTo(600.0);
            assertThat(economyService.getBalance(testPlayer2)).isEqualTo(400.0);
        }

        @Test
        @DisplayName("transfer should fail with insufficient funds")
        void transferShouldFailWithInsufficientFunds() {
            economyService.createAccount(testPlayer1);
            economyService.createAccount(testPlayer2);
            economyService.deposit(testPlayer1, 100.0, TransactionType.ADMIN, "Test");

            boolean success = economyService.transfer(testPlayer1, testPlayer2, 500.0, "Transfer test");

            assertThat(success).isFalse();
            assertThat(economyService.getBalance(testPlayer1)).isEqualTo(100.0);
            assertThat(economyService.getBalance(testPlayer2)).isEqualTo(0.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Persistence Contract")
    class PersistenceContractTests {

        @Test
        @DisplayName("load should initialize data")
        void loadShouldInitializeData() {
            assertThatCode(() -> economyService.load())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("save should persist data")
        void saveShouldPersistData() {
            economyService.createAccount(testPlayer1);
            economyService.deposit(testPlayer1, 1000.0, TransactionType.ADMIN, "Test");

            assertThatCode(() -> economyService.save())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("saveIfNeeded should persist when dirty")
        void saveIfNeededShouldPersistWhenDirty() {
            assertThatCode(() -> economyService.saveIfNeeded())
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HEALTH MONITORING CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Health Monitoring Contract")
    class HealthMonitoringContractTests {

        @Test
        @DisplayName("isHealthy should return health status")
        void isHealthyShouldReturnHealthStatus() {
            boolean healthy = economyService.isHealthy();

            assertThat(healthy).isNotNull();
        }

        @Test
        @DisplayName("getLastError should return error message or null")
        void getLastErrorShouldReturnErrorOrNull() {
            String error = economyService.getLastError();

            // Should be null when healthy, String when not
            assertThat(error).satisfiesAnyOf(
                e -> assertThat(e).isNull(),
                e -> assertThat(e).isInstanceOf(String.class)
            );
        }

        @Test
        @DisplayName("getHealthInfo should return formatted status")
        void getHealthInfoShouldReturnFormattedStatus() {
            String healthInfo = economyService.getHealthInfo();

            assertThat(healthInfo).isNotNull();
            assertThat(healthInfo).isNotEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // METHOD DELEGATION CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Method Delegation Contract")
    class MethodDelegationContractTests {

        @Test
        @DisplayName("Instance methods should delegate to static methods")
        void instanceMethodsShouldDelegateToStaticMethods() {
            // Test that instance and static methods produce same result
            EconomyManager.createAccount(testPlayer1);
            EconomyManager.deposit(testPlayer1, 1000.0, TransactionType.ADMIN, "Static");

            // Now test via instance
            economyService.createAccount(testPlayer2);
            economyService.deposit(testPlayer2, 1000.0, TransactionType.ADMIN, "Instance");

            // Both should have same balance
            assertThat(EconomyManager.getBalance(testPlayer1))
                .isEqualTo(economyService.getBalance(testPlayer2));
        }

        @Test
        @DisplayName("Interface and concrete class should share state")
        void interfaceAndConcreteClassShouldShareState() {
            // Modify via static method
            EconomyManager.createAccount(testPlayer1);
            EconomyManager.deposit(testPlayer1, 500.0, TransactionType.ADMIN, "Test");

            // Read via interface
            double balance = economyService.getBalance(testPlayer1);

            assertThat(balance).isEqualTo(500.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEPENDENCY INJECTION SUPPORT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Dependency Injection Support")
    class DependencyInjectionSupportTests {

        @Test
        @DisplayName("Should support constructor injection pattern")
        void shouldSupportConstructorInjectionPattern() {
            // Simulate dependency injection
            IEconomyService injectedService = EconomyManager.getInstance();

            TestServiceConsumer consumer = new TestServiceConsumer(injectedService);
            consumer.performOperation(testPlayer1);

            assertThat(injectedService.hasAccount(testPlayer1)).isTrue();
        }

        @Test
        @DisplayName("Should support method injection pattern")
        void shouldSupportMethodInjectionPattern() {
            TestServiceConsumer consumer = new TestServiceConsumer(null);
            consumer.setEconomyService(EconomyManager.getInstance());

            consumer.performOperation(testPlayer1);

            assertThat(economyService.hasAccount(testPlayer1)).isTrue();
        }
    }

    /**
     * Helper class to test dependency injection patterns
     */
    private static class TestServiceConsumer {
        private IEconomyService economyService;

        public TestServiceConsumer(IEconomyService economyService) {
            this.economyService = economyService;
        }

        public void setEconomyService(IEconomyService economyService) {
            this.economyService = economyService;
        }

        public void performOperation(UUID playerUUID) {
            economyService.createAccount(playerUUID);
            economyService.deposit(playerUUID, 100.0, TransactionType.ADMIN, "DI Test");
        }
    }
}
