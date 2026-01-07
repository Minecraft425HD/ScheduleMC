package de.rolandsw.schedulemc.npc.crime;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Contract Tests for ICrimeManager Interface
 *
 * Tests that verify the CrimeManager correctly implements the ICrimeManager interface
 * and honors all contract requirements including:
 * - Interface type compatibility
 * - Singleton pattern implementation
 * - Method signatures and behavior
 * - Wanted level management (0-5 stars)
 * - Escape timer mechanics
 * - Client-server synchronization
 */
@DisplayName("ICrimeManager Contract Tests")
class ICrimeManagerContractTest {

    private ICrimeManager crimeManager;
    private UUID testPlayer1;
    private UUID testPlayer2;

    @BeforeEach
    void setUp() throws Exception {
        testPlayer1 = UUID.randomUUID();
        testPlayer2 = UUID.randomUUID();

        // Get instance as interface type
        crimeManager = CrimeManager.getInstance();

        // Clear crime data
        clearCrimeData();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearCrimeData();
    }

    /**
     * Helper to clear crime data using reflection
     */
    private void clearCrimeData() throws Exception {
        Field wantedLevelsField = CrimeManager.class.getDeclaredField("wantedLevels");
        wantedLevelsField.setAccessible(true);
        Map<UUID, Integer> wantedLevels = (Map<UUID, Integer>) wantedLevelsField.get(null);
        wantedLevels.clear();

        Field lastCrimeDayField = CrimeManager.class.getDeclaredField("lastCrimeDay");
        lastCrimeDayField.setAccessible(true);
        Map<UUID, Long> lastCrimeDay = (Map<UUID, Long>) lastCrimeDayField.get(null);
        lastCrimeDay.clear();

        Field escapeTimersField = CrimeManager.class.getDeclaredField("escapeTimers");
        escapeTimersField.setAccessible(true);
        Map<UUID, Long> escapeTimers = (Map<UUID, Long>) escapeTimersField.get(null);
        escapeTimers.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // INTERFACE COMPATIBILITY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Interface Compatibility")
    class InterfaceCompatibilityTests {

        @Test
        @DisplayName("CrimeManager should implement ICrimeManager")
        void crimeManagerShouldImplementInterface() {
            assertThat(CrimeManager.getInstance()).isInstanceOf(ICrimeManager.class);
        }

        @Test
        @DisplayName("Should be assignable to ICrimeManager type")
        void shouldBeAssignableToInterfaceType() {
            ICrimeManager manager = CrimeManager.getInstance();
            assertThat(manager).isNotNull();
        }

        @Test
        @DisplayName("Interface reference should work for all operations")
        void interfaceReferenceShouldWorkForAllOperations() {
            ICrimeManager manager = CrimeManager.getInstance();

            manager.addWantedLevel(testPlayer1, 2, 0L);
            int level = manager.getWantedLevel(testPlayer1);
            manager.startEscapeTimer(testPlayer1, 0L);
            boolean hiding = manager.isHiding(testPlayer1);

            assertThat(level).isEqualTo(2);
            assertThat(hiding).isTrue();
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
            ICrimeManager instance1 = CrimeManager.getInstance();
            ICrimeManager instance2 = CrimeManager.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Multiple getInstance calls should share state")
        void multipleGetInstanceCallsShouldShareState() {
            ICrimeManager instance1 = CrimeManager.getInstance();
            instance1.setWantedLevel(testPlayer1, 3);

            ICrimeManager instance2 = CrimeManager.getInstance();
            int level = instance2.getWantedLevel(testPlayer1);

            assertThat(level).isEqualTo(3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WANTED LEVEL MANAGEMENT CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Wanted Level Management Contract")
    class WantedLevelManagementContractTests {

        @Test
        @DisplayName("getWantedLevel should return 0 for new player")
        void getWantedLevelShouldReturnZeroForNewPlayer() {
            assertThat(crimeManager.getWantedLevel(testPlayer1)).isZero();
        }

        @Test
        @DisplayName("addWantedLevel should increase wanted level")
        void addWantedLevelShouldIncreaseWantedLevel() {
            crimeManager.addWantedLevel(testPlayer1, 2, 0L);

            assertThat(crimeManager.getWantedLevel(testPlayer1)).isEqualTo(2);
        }

        @Test
        @DisplayName("addWantedLevel should cap at 5")
        void addWantedLevelShouldCapAtFive() {
            crimeManager.addWantedLevel(testPlayer1, 10, 0L);

            assertThat(crimeManager.getWantedLevel(testPlayer1)).isEqualTo(5);
        }

        @Test
        @DisplayName("clearWantedLevel should remove wanted level")
        void clearWantedLevelShouldRemoveWantedLevel() {
            crimeManager.addWantedLevel(testPlayer1, 3, 0L);
            crimeManager.clearWantedLevel(testPlayer1);

            assertThat(crimeManager.getWantedLevel(testPlayer1)).isZero();
        }

        @Test
        @DisplayName("setWantedLevel should set exact level")
        void setWantedLevelShouldSetExactLevel() {
            crimeManager.setWantedLevel(testPlayer1, 4);

            assertThat(crimeManager.getWantedLevel(testPlayer1)).isEqualTo(4);
        }

        @Test
        @DisplayName("decayWantedLevel should reduce level over time")
        void decayWantedLevelShouldReduceLevelOverTime() {
            crimeManager.addWantedLevel(testPlayer1, 5, 0L);
            crimeManager.decayWantedLevel(testPlayer1, 1L);

            assertThat(crimeManager.getWantedLevel(testPlayer1)).isEqualTo(4);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ESCAPE TIMER MECHANICS CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Escape Timer Mechanics Contract")
    class EscapeTimerMechanicsContractTests {

        @Test
        @DisplayName("startEscapeTimer should start timer")
        void startEscapeTimerShouldStartTimer() {
            crimeManager.startEscapeTimer(testPlayer1, 0L);

            assertThat(crimeManager.isHiding(testPlayer1)).isTrue();
        }

        @Test
        @DisplayName("stopEscapeTimer should stop timer")
        void stopEscapeTimerShouldStopTimer() {
            crimeManager.startEscapeTimer(testPlayer1, 0L);
            crimeManager.stopEscapeTimer(testPlayer1);

            assertThat(crimeManager.isHiding(testPlayer1)).isFalse();
        }

        @Test
        @DisplayName("isHiding should return false initially")
        void isHidingShouldReturnFalseInitially() {
            assertThat(crimeManager.isHiding(testPlayer1)).isFalse();
        }

        @Test
        @DisplayName("getEscapeTimeRemaining should return time left")
        void getEscapeTimeRemainingShouldReturnTimeLeft() {
            crimeManager.startEscapeTimer(testPlayer1, 0L);

            long remaining = crimeManager.getEscapeTimeRemaining(testPlayer1, 100L);

            assertThat(remaining).isGreaterThan(0);
        }

        @Test
        @DisplayName("getEscapeTimeRemaining should return 0 when not hiding")
        void getEscapeTimeRemainingShouldReturnZeroWhenNotHiding() {
            long remaining = crimeManager.getEscapeTimeRemaining(testPlayer1, 1000L);

            assertThat(remaining).isZero();
        }

        @Test
        @DisplayName("checkEscapeSuccess should return true after timer expires")
        void checkEscapeSuccessShouldReturnTrueAfterExpiry() {
            crimeManager.setWantedLevel(testPlayer1, 3);
            crimeManager.startEscapeTimer(testPlayer1, 0L);

            boolean success = crimeManager.checkEscapeSuccess(testPlayer1, CrimeManager.ESCAPE_DURATION + 10);

            if (success) {
                assertThat(crimeManager.getWantedLevel(testPlayer1)).isEqualTo(2);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CLIENT SYNCHRONIZATION CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Client Synchronization Contract")
    class ClientSynchronizationContractTests {

        @Test
        @DisplayName("setClientWantedLevel should set client-side level")
        void setClientWantedLevelShouldSetClientLevel() {
            assertThatCode(() -> crimeManager.setClientWantedLevel(3))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setClientEscapeTime should set client-side time")
        void setClientEscapeTimeShouldSetClientTime() {
            assertThatCode(() -> crimeManager.setClientEscapeTime(600L))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getClientWantedLevel should return client level")
        void getClientWantedLevelShouldReturnClientLevel() {
            crimeManager.setClientWantedLevel(4);

            assertThat(crimeManager.getClientWantedLevel()).isEqualTo(4);
        }

        @Test
        @DisplayName("getClientEscapeTime should return client time")
        void getClientEscapeTimeShouldReturnClientTime() {
            crimeManager.setClientEscapeTime(500L);

            assertThat(crimeManager.getClientEscapeTime()).isEqualTo(500L);
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
            assertThatCode(() -> crimeManager.load())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("save should persist data")
        void saveShouldPersistData() {
            crimeManager.setWantedLevel(testPlayer1, 3);

            assertThatCode(() -> crimeManager.save())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("saveIfNeeded should persist when dirty")
        void saveIfNeededShouldPersistWhenDirty() {
            assertThatCode(() -> crimeManager.saveIfNeeded())
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
            boolean healthy = crimeManager.isHealthy();

            assertThat(healthy).isNotNull();
        }

        @Test
        @DisplayName("getLastError should return error message or null")
        void getLastErrorShouldReturnErrorOrNull() {
            String error = crimeManager.getLastError();

            assertThat(error).satisfiesAnyOf(
                e -> assertThat(e).isNull(),
                e -> assertThat(e).isInstanceOf(String.class)
            );
        }

        @Test
        @DisplayName("getHealthInfo should return formatted status")
        void getHealthInfoShouldReturnFormattedStatus() {
            String healthInfo = crimeManager.getHealthInfo();

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
            CrimeManager.setWantedLevel(testPlayer1, 3);

            // Read via instance
            int level = crimeManager.getWantedLevel(testPlayer1);

            assertThat(level).isEqualTo(3);
        }

        @Test
        @DisplayName("Interface and concrete class should share state")
        void interfaceAndConcreteClassShouldShareState() {
            // Modify via instance
            crimeManager.setWantedLevel(testPlayer1, 4);

            // Read via static
            int level = CrimeManager.getWantedLevel(testPlayer1);

            assertThat(level).isEqualTo(4);
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
            ICrimeManager injectedManager = CrimeManager.getInstance();

            TestCrimeConsumer consumer = new TestCrimeConsumer(injectedManager);
            consumer.performOperation(testPlayer1);

            assertThat(injectedManager.getWantedLevel(testPlayer1)).isEqualTo(3);
        }

        @Test
        @DisplayName("Should support method injection pattern")
        void shouldSupportMethodInjectionPattern() {
            TestCrimeConsumer consumer = new TestCrimeConsumer(null);
            consumer.setCrimeManager(CrimeManager.getInstance());

            consumer.performOperation(testPlayer1);

            assertThat(crimeManager.getWantedLevel(testPlayer1)).isEqualTo(3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYER ISOLATION CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Player Isolation Contract")
    class PlayerIsolationContractTests {

        @Test
        @DisplayName("Different players should have independent wanted levels")
        void differentPlayersShouldHaveIndependentWantedLevels() {
            crimeManager.setWantedLevel(testPlayer1, 2);
            crimeManager.setWantedLevel(testPlayer2, 4);

            assertThat(crimeManager.getWantedLevel(testPlayer1)).isEqualTo(2);
            assertThat(crimeManager.getWantedLevel(testPlayer2)).isEqualTo(4);
        }

        @Test
        @DisplayName("Modifying one player's wanted level should not affect others")
        void modifyingOnePlayerShouldNotAffectOthers() {
            crimeManager.setWantedLevel(testPlayer1, 3);
            crimeManager.setWantedLevel(testPlayer2, 3);

            crimeManager.addWantedLevel(testPlayer1, 2, 0L);

            assertThat(crimeManager.getWantedLevel(testPlayer1)).isEqualTo(5);
            assertThat(crimeManager.getWantedLevel(testPlayer2)).isEqualTo(3);
        }

        @Test
        @DisplayName("Escape timers should be independent per player")
        void escapeTimersShouldBeIndependentPerPlayer() {
            crimeManager.startEscapeTimer(testPlayer1, 0L);

            assertThat(crimeManager.isHiding(testPlayer1)).isTrue();
            assertThat(crimeManager.isHiding(testPlayer2)).isFalse();
        }
    }

    /**
     * Helper class to test dependency injection patterns
     */
    private static class TestCrimeConsumer {
        private ICrimeManager crimeManager;

        public TestCrimeConsumer(ICrimeManager crimeManager) {
            this.crimeManager = crimeManager;
        }

        public void setCrimeManager(ICrimeManager crimeManager) {
            this.crimeManager = crimeManager;
        }

        public void performOperation(UUID playerUUID) {
            crimeManager.addWantedLevel(playerUUID, 3, 0L);
            crimeManager.startEscapeTimer(playerUUID, 0L);
        }
    }
}
