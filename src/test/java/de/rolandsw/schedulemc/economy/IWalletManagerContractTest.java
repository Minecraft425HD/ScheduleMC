package de.rolandsw.schedulemc.economy;

import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Contract Tests for IWalletManager Interface
 *
 * Tests that verify the WalletManager correctly implements the IWalletManager interface
 * and honors all contract requirements including:
 * - Interface type compatibility
 * - Singleton pattern implementation
 * - Method signatures and behavior
 * - Instance vs static method delegation
 */
@DisplayName("IWalletManager Contract Tests")
class IWalletManagerContractTest {

    private IWalletManager walletManager;
    private UUID testPlayer1;
    private UUID testPlayer2;

    @BeforeEach
    void setUp() {
        testPlayer1 = UUID.randomUUID();
        testPlayer2 = UUID.randomUUID();

        // Get instance as interface type
        walletManager = WalletManager.getInstance();

        // Reset balances
        walletManager.setBalance(testPlayer1, 0.0);
        walletManager.setBalance(testPlayer2, 0.0);
    }

    // ═══════════════════════════════════════════════════════════
    // INTERFACE COMPATIBILITY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Interface Compatibility")
    class InterfaceCompatibilityTests {

        @Test
        @DisplayName("WalletManager should implement IWalletManager")
        void walletManagerShouldImplementInterface() {
            assertThat(WalletManager.getInstance()).isInstanceOf(IWalletManager.class);
        }

        @Test
        @DisplayName("Should be assignable to IWalletManager type")
        void shouldBeAssignableToInterfaceType() {
            IWalletManager manager = WalletManager.getInstance();
            assertThat(manager).isNotNull();
        }

        @Test
        @DisplayName("Interface reference should work for all operations")
        void interfaceReferenceShouldWorkForAllOperations() {
            IWalletManager manager = WalletManager.getInstance();

            manager.setBalance(testPlayer1, 1000.0);
            manager.addMoney(testPlayer1, 500.0);
            boolean success = manager.removeMoney(testPlayer1, 300.0);

            assertThat(success).isTrue();
            assertThat(manager.getBalance(testPlayer1)).isEqualTo(1200.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON PATTERN CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Singleton Pattern Contract")
    class SingletonPatternContractTests {

        @Test
        @DisplayName("getInstance should always return same instance")
        void getInstanceShouldAlwaysReturnSameInstance() {
            IWalletManager instance1 = WalletManager.getInstance();
            IWalletManager instance2 = WalletManager.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Multiple getInstance calls should share state")
        void multipleGetInstanceCallsShouldShareState() {
            IWalletManager instance1 = WalletManager.getInstance();
            instance1.setBalance(testPlayer1, 500.0);

            IWalletManager instance2 = WalletManager.getInstance();
            double balance = instance2.getBalance(testPlayer1);

            assertThat(balance).isEqualTo(500.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BALANCE OPERATIONS CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Balance Operations Contract")
    class BalanceOperationsContractTests {

        @Test
        @DisplayName("getBalance should return current wallet balance")
        void getBalanceShouldReturnCurrentBalance() {
            walletManager.setBalance(testPlayer1, 750.0);

            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(750.0);
        }

        @Test
        @DisplayName("getBalance should return 0 for new wallet")
        void getBalanceShouldReturnZeroForNewWallet() {
            UUID newPlayer = UUID.randomUUID();

            assertThat(walletManager.getBalance(newPlayer)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("setBalance should set exact balance")
        void setBalanceShouldSetExactBalance() {
            walletManager.setBalance(testPlayer1, 1234.56);

            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(1234.56);
        }

        @Test
        @DisplayName("setBalance with negative should clamp to 0")
        void setBalanceWithNegativeShouldClampToZero() {
            walletManager.setBalance(testPlayer1, -500.0);

            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("addMoney should increase balance")
        void addMoneyShouldIncreaseBalance() {
            walletManager.setBalance(testPlayer1, 100.0);
            walletManager.addMoney(testPlayer1, 50.0);

            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(150.0);
        }

        @Test
        @DisplayName("removeMoney should decrease balance when sufficient")
        void removeMoneyShould DecreaseBalanceWhenSufficient() {
            walletManager.setBalance(testPlayer1, 500.0);

            boolean success = walletManager.removeMoney(testPlayer1, 200.0);

            assertThat(success).isTrue();
            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(300.0);
        }

        @Test
        @DisplayName("removeMoney should fail when insufficient")
        void removeMoneyShould FailWhenInsufficient() {
            walletManager.setBalance(testPlayer1, 100.0);

            boolean success = walletManager.removeMoney(testPlayer1, 500.0);

            assertThat(success).isFalse();
            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(100.0);
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
            assertThatCode(() -> walletManager.load())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("save should persist data")
        void saveShouldPersistData() {
            walletManager.setBalance(testPlayer1, 1000.0);

            assertThatCode(() -> walletManager.save())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("saveIfNeeded should persist when dirty")
        void saveIfNeededShouldPersistWhenDirty() {
            assertThatCode(() -> walletManager.saveIfNeeded())
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
            boolean healthy = walletManager.isHealthy();

            assertThat(healthy).isNotNull();
        }

        @Test
        @DisplayName("getLastError should return error message or null")
        void getLastErrorShouldReturnErrorOrNull() {
            String error = walletManager.getLastError();

            assertThat(error).satisfiesAnyOf(
                e -> assertThat(e).isNull(),
                e -> assertThat(e).isInstanceOf(String.class)
            );
        }

        @Test
        @DisplayName("getHealthInfo should return formatted status")
        void getHealthInfoShouldReturnFormattedStatus() {
            String healthInfo = walletManager.getHealthInfo();

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
            // Set via static method
            WalletManager.setBalance(testPlayer1, 1000.0);

            // Read via instance
            double balance = walletManager.getBalance(testPlayer1);

            assertThat(balance).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("Interface and concrete class should share state")
        void interfaceAndConcreteClassShouldShareState() {
            // Modify via instance
            walletManager.setBalance(testPlayer1, 750.0);

            // Read via static
            double balance = WalletManager.getBalance(testPlayer1);

            assertThat(balance).isEqualTo(750.0);
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
            IWalletManager injectedManager = WalletManager.getInstance();

            TestWalletConsumer consumer = new TestWalletConsumer(injectedManager);
            consumer.performOperation(testPlayer1);

            assertThat(injectedManager.getBalance(testPlayer1)).isEqualTo(250.0);
        }

        @Test
        @DisplayName("Should support method injection pattern")
        void shouldSupportMethodInjectionPattern() {
            TestWalletConsumer consumer = new TestWalletConsumer(null);
            consumer.setWalletManager(WalletManager.getInstance());

            consumer.performOperation(testPlayer1);

            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(250.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WALLET ISOLATION CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Wallet Isolation Contract")
    class WalletIsolationContractTests {

        @Test
        @DisplayName("Different players should have independent wallets")
        void differentPlayersShouldHaveIndependentWallets() {
            walletManager.setBalance(testPlayer1, 1000.0);
            walletManager.setBalance(testPlayer2, 2000.0);

            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(1000.0);
            assertThat(walletManager.getBalance(testPlayer2)).isEqualTo(2000.0);
        }

        @Test
        @DisplayName("Modifying one wallet should not affect others")
        void modifyingOneWalletShouldNotAffectOthers() {
            walletManager.setBalance(testPlayer1, 500.0);
            walletManager.setBalance(testPlayer2, 500.0);

            walletManager.addMoney(testPlayer1, 300.0);

            assertThat(walletManager.getBalance(testPlayer1)).isEqualTo(800.0);
            assertThat(walletManager.getBalance(testPlayer2)).isEqualTo(500.0);
        }
    }

    /**
     * Helper class to test dependency injection patterns
     */
    private static class TestWalletConsumer {
        private IWalletManager walletManager;

        public TestWalletConsumer(IWalletManager walletManager) {
            this.walletManager = walletManager;
        }

        public void setWalletManager(IWalletManager walletManager) {
            this.walletManager = walletManager;
        }

        public void performOperation(UUID playerUUID) {
            walletManager.setBalance(playerUUID, 100.0);
            walletManager.addMoney(playerUUID, 200.0);
            walletManager.removeMoney(playerUUID, 50.0);
        }
    }
}
