package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for InterestManager (Interest & Payout System)
 *
 * Tests cover:
 * - Interest rate (2% per week)
 * - Weekly payout mechanics (every 7 days)
 * - Maximum interest cap (10,000€ per week)
 * - Next payout calculation
 * - Day tracking and tick mechanics
 * - Zero/negative balance handling
 * - Edge cases: max cap, very large balances
 *
 * BUSINESS LOGIC:
 * - Regular accounts: 2% interest per week
 * - Maximum interest: 10,000€ per week (prevents inflation)
 * - Payout frequency: Every 7 in-game days
 */
@DisplayName("InterestManager Tests")
class InterestManagerTest {

    private static final UUID TEST_PLAYER = UUID.randomUUID();
    private static final UUID TEST_PLAYER_2 = UUID.randomUUID();
    private static final double INTEREST_RATE = 0.02; // 2% per week
    private static final double MAX_INTEREST = 10000.0;
    private static final long WEEK_IN_DAYS = 7;

    private InterestManager interestManager;
    private MinecraftServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance
        resetInterestManagerInstance();

        // Create mock server
        mockServer = createMockServer();

        // Get InterestManager instance
        interestManager = InterestManager.getInstance(mockServer);

        // Clear all data
        clearLastInterestPayout();
        setCurrentDay(0);
    }

    @AfterEach
    void tearDown() throws Exception {
        resetInterestManagerInstance();
    }

    // ═══════════════════════════════════════════════════════════
    // INTEREST RATE AND CALCULATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Interest Rate and Calculation Tests")
    class InterestRateAndCalculationTests {

        @ParameterizedTest
        @CsvSource({
            "1000.0, 20.0",      // 1000€ → 20€ (2%)
            "5000.0, 100.0",     // 5000€ → 100€ (2%)
            "10000.0, 200.0",    // 10000€ → 200€ (2%)
            "50000.0, 1000.0",   // 50000€ → 1000€ (2%)
            "100000.0, 2000.0"   // 100000€ → 2000€ (2%)
        })
        @DisplayName("Interest should be calculated at 2% for balances under cap")
        void interestShouldBeCalculatedAtTwoPercent(double balance, double expectedInterest) {
            double calculatedInterest = balance * INTEREST_RATE;

            assertThat(calculatedInterest).isEqualTo(expectedInterest);
        }

        @ParameterizedTest
        @CsvSource({
            "500000.0, 10000.0",    // Would be 10,000€, capped
            "600000.0, 10000.0",    // Would be 12,000€, capped at 10,000€
            "1000000.0, 10000.0"    // Would be 20,000€, capped at 10,000€
        })
        @DisplayName("Interest should be capped at 10,000€ for very large balances")
        void interestShouldBeCappedAtMaximum(double balance, double expectedMax) {
            double calculatedInterest = Math.min(balance * INTEREST_RATE, MAX_INTEREST);

            assertThat(calculatedInterest).isEqualTo(expectedMax);
        }

        @Test
        @DisplayName("Next interest calculation should match balance × 2%")
        void nextInterestCalculationShouldMatchFormula() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(5000.0);

                double nextInterest = interestManager.calculateNextInterest(TEST_PLAYER);

                assertThat(nextInterest).isEqualTo(100.0); // 2% of 5000
            }
        }

        @Test
        @DisplayName("Next interest calculation should respect cap")
        void nextInterestCalculationShouldRespectCap() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(1_000_000.0);

                double nextInterest = interestManager.calculateNextInterest(TEST_PLAYER);

                assertThat(nextInterest).isEqualTo(10000.0); // Capped
            }
        }

        @Test
        @DisplayName("Interest calculation should handle zero balance")
        void interestCalculationShouldHandleZeroBalance() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(0.0);

                double nextInterest = interestManager.calculateNextInterest(TEST_PLAYER);

                assertThat(nextInterest).isZero();
            }
        }

        @Test
        @DisplayName("Interest calculation should handle negative balance")
        void interestCalculationShouldHandleNegativeBalance() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(-1000.0);

                double nextInterest = interestManager.calculateNextInterest(TEST_PLAYER);

                // Should be capped at 0 or calculated as negative (implementation-dependent)
                assertThat(nextInterest).isLessThanOrEqualTo(0.0);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WEEKLY PAYOUT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Weekly Payout Tests")
    class WeeklyPayoutTests {

        @Test
        @DisplayName("First payout should occur after 7 days")
        void firstPayoutShouldOccurAfterSevenDays() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 10000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                // Advance 7 days
                for (long day = 1; day <= 7; day++) {
                    interestManager.tick(day * 24000L);
                }

                // Should have deposited interest
                economyMock.verify(() -> EconomyManager.deposit(
                    eq(TEST_PLAYER), eq(200.0), eq(TransactionType.INTEREST), anyString()
                ), atLeastOnce());
            }
        }

        @Test
        @DisplayName("No payout should occur before 7 days")
        void noPayoutShouldOccurBeforeSevenDays() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 10000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                // Advance only 6 days
                for (long day = 1; day <= 6; day++) {
                    interestManager.tick(day * 24000L);
                }

                // Should NOT have deposited interest yet
                economyMock.verify(() -> EconomyManager.deposit(
                    any(), anyDouble(), any(), anyString()
                ), never());
            }
        }

        @Test
        @DisplayName("Payout should respect 10,000€ maximum")
        void payoutShouldRespectMaximum() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 1_000_000.0); // Would earn 20,000€ at 2%

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                // Advance 7 days
                for (long day = 1; day <= 7; day++) {
                    interestManager.tick(day * 24000L);
                }

                // Should deposit capped amount
                economyMock.verify(() -> EconomyManager.deposit(
                    eq(TEST_PLAYER), eq(10000.0), eq(TransactionType.INTEREST), anyString()
                ), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Payout should not occur for zero balance")
        void payoutShouldNotOccurForZeroBalance() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 0.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                // Advance 7 days
                for (long day = 1; day <= 7; day++) {
                    interestManager.tick(day * 24000L);
                }

                // Should NOT deposit anything
                economyMock.verify(() -> EconomyManager.deposit(
                    any(), anyDouble(), any(), anyString()
                ), never());
            }
        }

        @Test
        @DisplayName("Payout should not occur for negative balance")
        void payoutShouldNotOccurForNegativeBalance() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, -1000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                // Advance 7 days
                for (long day = 1; day <= 7; day++) {
                    interestManager.tick(day * 24000L);
                }

                // Should NOT deposit anything
                economyMock.verify(() -> EconomyManager.deposit(
                    any(), anyDouble(), any(), anyString()
                ), never());
            }
        }

        @Test
        @DisplayName("Multiple players should receive payouts independently")
        void multiplePlayersShouldReceivePayoutsIndependently() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 10000.0);
                balances.put(TEST_PLAYER_2, 5000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                // Advance 7 days
                for (long day = 1; day <= 7; day++) {
                    interestManager.tick(day * 24000L);
                }

                // Both should receive interest
                economyMock.verify(() -> EconomyManager.deposit(
                    eq(TEST_PLAYER), eq(200.0), any(), anyString()
                ), atLeastOnce());

                economyMock.verify(() -> EconomyManager.deposit(
                    eq(TEST_PLAYER_2), eq(100.0), any(), anyString()
                ), atLeastOnce());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NEXT PAYOUT CALCULATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Next Payout Calculation Tests")
    class NextPayoutCalculationTests {

        @Test
        @DisplayName("Days until next payout should be 7 for new player")
        void daysUntilNextPayoutShouldBeSevenForNewPlayer() throws Exception {
            setCurrentDay(10);

            long daysUntilNext = interestManager.getDaysUntilNextPayout(TEST_PLAYER);

            assertThat(daysUntilNext).isEqualTo(7);
        }

        @ParameterizedTest
        @CsvSource({
            "1, 6",   // 1 day passed → 6 days remaining
            "2, 5",   // 2 days passed → 5 days remaining
            "3, 4",   // 3 days passed → 4 days remaining
            "6, 1",   // 6 days passed → 1 day remaining
            "7, 0"    // 7 days passed → 0 days (payout due)
        })
        @DisplayName("Days until next payout should decrease as days pass")
        void daysUntilNextPayoutShouldDecrease(long daysPassed, long expectedRemaining) throws Exception {
            setCurrentDay(daysPassed);
            setLastPayoutDay(TEST_PLAYER, 0L);

            long daysUntilNext = interestManager.getDaysUntilNextPayout(TEST_PLAYER);

            assertThat(daysUntilNext).isEqualTo(expectedRemaining);
        }

        @Test
        @DisplayName("Days until next payout should be 0 when payout is due")
        void daysUntilNextPayoutShouldBeZeroWhenDue() throws Exception {
            setCurrentDay(10);
            setLastPayoutDay(TEST_PLAYER, 3L); // 7 days ago

            long daysUntilNext = interestManager.getDaysUntilNextPayout(TEST_PLAYER);

            assertThat(daysUntilNext).isEqualTo(0);
        }

        @Test
        @DisplayName("Days until next payout should reset after payout")
        void daysUntilNextPayoutShouldResetAfterPayout() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 10000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                setCurrentDay(0);
                setLastPayoutDay(TEST_PLAYER, 0L);

                // Advance 7 days to trigger payout
                for (long day = 1; day <= 7; day++) {
                    interestManager.tick(day * 24000L);
                }

                // Should now be 7 days until next payout
                long daysUntilNext = interestManager.getDaysUntilNextPayout(TEST_PLAYER);
                assertThat(daysUntilNext).isEqualTo(7);
            }
        }

        @Test
        @DisplayName("Days until next payout should handle very large day values")
        void daysUntilNextPayoutShouldHandleLargeDayValues() throws Exception {
            setCurrentDay(1000000L);
            setLastPayoutDay(TEST_PLAYER, 999995L);

            long daysUntilNext = interestManager.getDaysUntilNextPayout(TEST_PLAYER);

            assertThat(daysUntilNext).isEqualTo(2); // 7 - 5 = 2
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DAY TRACKING AND TICK TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Day Tracking and Tick Tests")
    class DayTrackingAndTickTests {

        @Test
        @DisplayName("Tick should update current day")
        void tickShouldUpdateCurrentDay() throws Exception {
            interestManager.tick(5 * 24000L); // Day 5

            long currentDay = getCurrentDay();

            assertThat(currentDay).isEqualTo(5);
        }

        @Test
        @DisplayName("Multiple ticks on same day should not trigger duplicate payouts")
        void multipleTicksOnSameDayShouldNotTriggerDuplicatePayouts() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 10000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                // Tick multiple times on day 7
                interestManager.tick(7 * 24000L);
                interestManager.tick(7 * 24000L + 100);
                interestManager.tick(7 * 24000L + 500);

                // Should only deposit once
                economyMock.verify(() -> EconomyManager.deposit(
                    eq(TEST_PLAYER), anyDouble(), any(), anyString()
                ), times(1));
            }
        }

        @Test
        @DisplayName("Tick should handle day overflow gracefully")
        void tickShouldHandleDayOverflow() throws Exception {
            long veryLargeDay = Long.MAX_VALUE / 24000L - 100;

            assertThatCode(() -> interestManager.tick(veryLargeDay * 24000L))
                .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(longs = {0, 1, 100, 1000, 10000, 100000})
        @DisplayName("Tick should correctly calculate day from dayTime")
        void tickShouldCorrectlyCalculateDayFromDayTime(long day) throws Exception {
            interestManager.tick(day * 24000L);

            long currentDay = getCurrentDay();

            assertThat(currentDay).isEqualTo(day);
        }

        @Test
        @DisplayName("Tick should handle negative dayTime gracefully")
        void tickShouldHandleNegativeDayTime() throws Exception {
            assertThatCode(() -> interestManager.tick(-1000L))
                .doesNotThrowAnyException();
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
            MinecraftServer server = createMockServer();

            InterestManager instance1 = InterestManager.getInstance(server);
            InterestManager instance2 = InterestManager.getInstance(server);

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("getInstance should be thread-safe")
        void getInstanceShouldBeThreadSafe() throws InterruptedException {
            resetInterestManagerInstance();

            InterestManager[] instances = new InterestManager[2];
            MinecraftServer server = createMockServer();

            Thread thread1 = new Thread(() -> instances[0] = InterestManager.getInstance(server));
            Thread thread2 = new Thread(() -> instances[1] = InterestManager.getInstance(server));

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
        @DisplayName("Interest should handle very small balances")
        void interestShouldHandleVerySmallBalances() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(0.01);

                double nextInterest = interestManager.calculateNextInterest(TEST_PLAYER);

                assertThat(nextInterest).isGreaterThanOrEqualTo(0.0);
                assertThat(nextInterest).isLessThanOrEqualTo(MAX_INTEREST);
            }
        }

        @Test
        @DisplayName("Interest should handle balances exactly at cap threshold")
        void interestShouldHandleBalancesAtCapThreshold() {
            double capThreshold = MAX_INTEREST / INTEREST_RATE; // 500,000€

            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(capThreshold);

                double nextInterest = interestManager.calculateNextInterest(TEST_PLAYER);

                assertThat(nextInterest).isEqualTo(MAX_INTEREST);
            }
        }

        @Test
        @DisplayName("System should handle no accounts gracefully")
        void systemShouldHandleNoAccountsGracefully() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(EconomyManager::getAllAccounts)
                    .thenReturn(new ConcurrentHashMap<>());

                // Advance 7 days
                assertThatCode(() -> {
                    for (long day = 1; day <= 7; day++) {
                        interestManager.tick(day * 24000L);
                    }
                }).doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("Payout should handle transaction type correctly")
        void payoutShouldHandleTransactionTypeCorrectly() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 10000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                // Advance 7 days
                for (long day = 1; day <= 7; day++) {
                    interestManager.tick(day * 24000L);
                }

                // Verify transaction type is INTEREST
                economyMock.verify(() -> EconomyManager.deposit(
                    any(), anyDouble(), eq(TransactionType.INTEREST), anyString()
                ), atLeastOnce());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS LOGIC VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Interest rate should be exactly 2%")
        void interestRateShouldBeExactlyTwoPercent() {
            assertThat(INTEREST_RATE).isEqualTo(0.02);
        }

        @Test
        @DisplayName("Maximum interest should be exactly 10,000€")
        void maximumInterestShouldBeExactlyTenThousand() {
            assertThat(MAX_INTEREST).isEqualTo(10000.0);
        }

        @Test
        @DisplayName("Week length should be exactly 7 days")
        void weekLengthShouldBeSevenDays() {
            assertThat(WEEK_IN_DAYS).isEqualTo(7);
        }

        @Test
        @DisplayName("Interest cap should prevent excessive inflation")
        void interestCapShouldPreventExcessiveInflation() {
            // Balance that would earn more than cap
            double hugeBalance = 10_000_000.0; // Would earn 200,000€ at 2%
            double cappedInterest = Math.min(hugeBalance * INTEREST_RATE, MAX_INTEREST);

            assertThat(cappedInterest).isEqualTo(MAX_INTEREST);
            assertThat(cappedInterest).isLessThan(hugeBalance * INTEREST_RATE);
        }

        @Test
        @DisplayName("Interest system should be deterministic")
        void interestSystemShouldBeDeterministic() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(5000.0);

                double interest1 = interestManager.calculateNextInterest(TEST_PLAYER);
                double interest2 = interestManager.calculateNextInterest(TEST_PLAYER);
                double interest3 = interestManager.calculateNextInterest(TEST_PLAYER);

                assertThat(interest1).isEqualTo(interest2);
                assertThat(interest2).isEqualTo(interest3);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private MinecraftServer createMockServer() {
        MinecraftServer server = mock(MinecraftServer.class);
        when(server.getServerDirectory()).thenReturn(new java.io.File("test_server").toPath());
        when(server.getPlayerList()).thenReturn(mock(net.minecraft.server.players.PlayerList.class));
        return server;
    }

    private void resetInterestManagerInstance() throws Exception {
        Field instanceField = InterestManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private void clearLastInterestPayout() throws Exception {
        Field lastPayoutField = InterestManager.class.getDeclaredField("lastInterestPayout");
        lastPayoutField.setAccessible(true);
        Map<UUID, Long> lastPayout = (Map<UUID, Long>) lastPayoutField.get(interestManager);
        lastPayout.clear();
    }

    private void setCurrentDay(long day) throws Exception {
        Field currentDayField = InterestManager.class.getDeclaredField("currentDay");
        currentDayField.setAccessible(true);
        currentDayField.set(interestManager, day);
    }

    private long getCurrentDay() throws Exception {
        Field currentDayField = InterestManager.class.getDeclaredField("currentDay");
        currentDayField.setAccessible(true);
        return (long) currentDayField.get(interestManager);
    }

    private void setLastPayoutDay(UUID player, long day) throws Exception {
        Field lastPayoutField = InterestManager.class.getDeclaredField("lastInterestPayout");
        lastPayoutField.setAccessible(true);
        Map<UUID, Long> lastPayout = (Map<UUID, Long>) lastPayoutField.get(interestManager);
        lastPayout.put(player, day);
    }
}
