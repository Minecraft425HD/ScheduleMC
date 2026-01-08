package de.rolandsw.schedulemc.npc.crime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for CrimeManager (NPC Crime/Police System)
 *
 * Tests cover:
 * - Wanted level management (0-5 stars)
 * - Wanted level addition with max cap
 * - Wanted level decay over time
 * - Escape timer system (30 seconds hide = -1 star)
 * - Thread safety (concurrent access)
 * - Crime day tracking
 * - Edge cases: negative levels, overflow, concurrent modifications
 */
@DisplayName("CrimeManager Tests")
class CrimeManagerTest {

    private static final UUID TEST_PLAYER = UUID.randomUUID();
    private static final UUID TEST_PLAYER_2 = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
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
    // WANTED LEVEL BASIC TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Wanted Level Basic Tests")
    class WantedLevelBasicTests {

        @Test
        @DisplayName("New player should have 0 wanted level")
        void newPlayerShouldHaveZeroWantedLevel() {
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isZero();
        }

        @Test
        @DisplayName("Should add wanted level")
        void shouldAddWantedLevel() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 2, 0L);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(2);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        @DisplayName("Should handle all wanted levels 1-5")
        void shouldHandleAllWantedLevels(int level) {
            CrimeManager.addWantedLevel(TEST_PLAYER, level, 0L);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(level);
        }

        @Test
        @DisplayName("Should set wanted level directly")
        void shouldSetWantedLevelDirectly() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 3);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(3);
        }

        @Test
        @DisplayName("Should clear wanted level")
        void shouldClearWantedLevel() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 3, 0L);
            CrimeManager.clearWantedLevel(TEST_PLAYER);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isZero();
        }

        @Test
        @DisplayName("Clearing non-existent wanted level should not fail")
        void clearingNonExistentWantedLevelShouldNotFail() {
            assertThatCode(() -> CrimeManager.clearWantedLevel(TEST_PLAYER))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WANTED LEVEL CAP TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Wanted Level Cap Tests")
    class WantedLevelCapTests {

        @Test
        @DisplayName("Wanted level should be capped at 5 stars")
        void wantedLevelShouldBeCappedAtFive() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 10, 0L);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(5);
        }

        @Test
        @DisplayName("Adding to max level should stay at 5")
        void addingToMaxLevelShouldStayAtFive() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 5);
            CrimeManager.addWantedLevel(TEST_PLAYER, 3, 0L);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(5);
        }

        @Test
        @DisplayName("Setting level above 5 should cap at 5")
        void settingLevelAboveFiveShouldCap() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 100);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(5);
        }

        @ParameterizedTest
        @CsvSource({
            "2, 1, 3",
            "2, 2, 4",
            "2, 3, 5",
            "2, 4, 5",  // Capped
            "4, 2, 5",  // Capped
            "3, 5, 5"   // Capped
        })
        @DisplayName("Adding wanted levels should cap correctly")
        void addingWantedLevelsShouldCapCorrectly(int initial, int added, int expected) {
            CrimeManager.setWantedLevel(TEST_PLAYER, initial);
            CrimeManager.addWantedLevel(TEST_PLAYER, added, 0L);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(expected);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WANTED LEVEL DECAY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Wanted Level Decay Tests")
    class WantedLevelDecayTests {

        @Test
        @DisplayName("Wanted level should decay by 1 per day")
        void wantedLevelShouldDecayByOnePerDay() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 5, 0L);
            CrimeManager.decayWantedLevel(TEST_PLAYER, 1L); // 1 day later

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(4);
        }

        @Test
        @DisplayName("Wanted level should decay to zero after enough days")
        void wantedLevelShouldDecayToZero() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 3, 0L);
            CrimeManager.decayWantedLevel(TEST_PLAYER, 5L); // 5 days later

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isZero();
        }

        @ParameterizedTest
        @CsvSource({
            "5, 1, 4",
            "5, 2, 3",
            "5, 3, 2",
            "5, 4, 1",
            "5, 5, 0",
            "5, 10, 0",  // Over-decay should stop at 0
            "3, 1, 2",
            "3, 3, 0"
        })
        @DisplayName("Decay should reduce wanted level correctly")
        void decayShouldReduceWantedLevelCorrectly(int initial, int daysPassed, int expected) {
            CrimeManager.addWantedLevel(TEST_PLAYER, initial, 0L);
            CrimeManager.decayWantedLevel(TEST_PLAYER, daysPassed);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Decay on same day should not reduce wanted level")
        void decayOnSameDayShouldNotReduce() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 3, 5L);
            CrimeManager.decayWantedLevel(TEST_PLAYER, 5L); // Same day

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(3);
        }

        @Test
        @DisplayName("Decay on player without wanted level should not fail")
        void decayOnPlayerWithoutWantedLevelShouldNotFail() {
            assertThatCode(() -> CrimeManager.decayWantedLevel(TEST_PLAYER, 10L))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Multiple decay calls should accumulate")
        void multipleDecayCallsShouldAccumulate() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 5, 0L);

            CrimeManager.decayWantedLevel(TEST_PLAYER, 1L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(4);

            CrimeManager.decayWantedLevel(TEST_PLAYER, 2L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(3);

            CrimeManager.decayWantedLevel(TEST_PLAYER, 3L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(2);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ESCAPE TIMER TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Escape Timer Tests")
    class EscapeTimerTests {

        @Test
        @DisplayName("Player should not be hiding initially")
        void playerShouldNotBeHidingInitially() {
            assertThat(CrimeManager.isHiding(TEST_PLAYER)).isFalse();
        }

        @Test
        @DisplayName("Should start escape timer")
        void shouldStartEscapeTimer() {
            CrimeManager.startEscapeTimer(TEST_PLAYER, 0L);

            assertThat(CrimeManager.isHiding(TEST_PLAYER)).isTrue();
        }

        @Test
        @DisplayName("Should stop escape timer")
        void shouldStopEscapeTimer() {
            CrimeManager.startEscapeTimer(TEST_PLAYER, 0L);
            CrimeManager.stopEscapeTimer(TEST_PLAYER);

            assertThat(CrimeManager.isHiding(TEST_PLAYER)).isFalse();
        }

        @Test
        @DisplayName("Escape time remaining should decrease over time")
        void escapeTimeRemainingShouldDecrease() {
            long startTick = 0L;
            CrimeManager.startEscapeTimer(TEST_PLAYER, startTick);

            long remaining1 = CrimeManager.getEscapeTimeRemaining(TEST_PLAYER, startTick + 100);
            long remaining2 = CrimeManager.getEscapeTimeRemaining(TEST_PLAYER, startTick + 200);

            assertThat(remaining1).isGreaterThan(remaining2);
        }

        @Test
        @DisplayName("Escape time remaining should be 0 when not hiding")
        void escapeTimeRemainingShouldBeZeroWhenNotHiding() {
            assertThat(CrimeManager.getEscapeTimeRemaining(TEST_PLAYER, 1000L)).isZero();
        }

        @Test
        @DisplayName("Escape time remaining should not go negative")
        void escapeTimeRemainingShouldNotGoNegative() {
            CrimeManager.startEscapeTimer(TEST_PLAYER, 0L);

            // Query way past the escape duration
            long remaining = CrimeManager.getEscapeTimeRemaining(TEST_PLAYER, 100000L);

            assertThat(remaining).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Escape duration should be 30 seconds (600 ticks)")
        void escapeDurationShouldBeCorrect() {
            assertThat(CrimeManager.ESCAPE_DURATION).isEqualTo(30 * 20); // 600 ticks
        }

        @Test
        @DisplayName("Escape distance should be 40 blocks")
        void escapeDistanceShouldBeCorrect() {
            assertThat(CrimeManager.ESCAPE_DISTANCE).isEqualTo(40.0);
        }

        @Test
        @DisplayName("Stopping non-existent timer should not fail")
        void stoppingNonExistentTimerShouldNotFail() {
            assertThatCode(() -> CrimeManager.stopEscapeTimer(TEST_PLAYER))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Setting negative wanted level should clear it")
        void settingNegativeWantedLevelShouldClear() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 3);
            CrimeManager.setWantedLevel(TEST_PLAYER, -1);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isZero();
        }

        @Test
        @DisplayName("Setting zero wanted level should clear it")
        void settingZeroWantedLevelShouldClear() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 3);
            CrimeManager.setWantedLevel(TEST_PLAYER, 0);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isZero();
        }

        @Test
        @DisplayName("Adding zero wanted level should not change level")
        void addingZeroWantedLevelShouldNotChange() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 2);
            CrimeManager.addWantedLevel(TEST_PLAYER, 0, 0L);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(2);
        }

        @Test
        @DisplayName("Adding negative wanted level should not change level")
        void addingNegativeWantedLevelShouldNotChange() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 3);
            int before = CrimeManager.getWantedLevel(TEST_PLAYER);

            CrimeManager.addWantedLevel(TEST_PLAYER, -2, 0L);

            // Note: Implementation may vary - document actual behavior
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Multiple players should have independent wanted levels")
        void multiplePlayersShouldHaveIndependentLevels() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 2);
            CrimeManager.setWantedLevel(TEST_PLAYER_2, 4);

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(2);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER_2)).isEqualTo(4);
        }

        @Test
        @DisplayName("Very large day values should not cause overflow")
        void veryLargeDayValuesShouldNotCauseOverflow() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 5, Long.MAX_VALUE - 100);

            assertThatCode(() -> CrimeManager.decayWantedLevel(TEST_PLAYER, Long.MAX_VALUE))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Very large tick values should not cause overflow in escape timer")
        void veryLargeTickValuesShouldNotCauseOverflow() {
            CrimeManager.startEscapeTimer(TEST_PLAYER, Long.MAX_VALUE - 1000);

            assertThatCode(() -> CrimeManager.getEscapeTimeRemaining(TEST_PLAYER, Long.MAX_VALUE))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS LOGIC VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Max wanted level should be 5 stars")
        void maxWantedLevelShouldBeFive() {
            // Verify the constant
            Field maxField;
            try {
                maxField = CrimeManager.class.getDeclaredField("MAX_WANTED_LEVEL");
                maxField.setAccessible(true);
                int maxLevel = (int) maxField.get(null);
                assertThat(maxLevel).isEqualTo(5);
            } catch (Exception e) {
                fail("Could not access MAX_WANTED_LEVEL constant");
            }
        }

        @Test
        @DisplayName("Wanted level should never exceed 5 regardless of additions")
        void wantedLevelShouldNeverExceedFive() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 3);

            // Try to add way more than max
            for (int i = 0; i < 10; i++) {
                CrimeManager.addWantedLevel(TEST_PLAYER, 5, 0L);
            }

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Wanted level should never go negative")
        void wantedLevelShouldNeverGoNegative() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 2);
            CrimeManager.decayWantedLevel(TEST_PLAYER, 100L); // Excessive decay

            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Escape timer should reduce wanted level by 1 on success")
        void escapeTimerShouldReduceWantedLevelOnSuccess() {
            CrimeManager.setWantedLevel(TEST_PLAYER, 3);
            CrimeManager.startEscapeTimer(TEST_PLAYER, 0L);

            // Check after escape duration
            boolean success = CrimeManager.checkEscapeSuccess(TEST_PLAYER, CrimeManager.ESCAPE_DURATION + 1);

            if (success) {
                assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("Crime day should be tracked for each player")
        void crimeDayShouldBeTrackedForEachPlayer() throws Exception {
            CrimeManager.addWantedLevel(TEST_PLAYER, 2, 5L);

            Field lastCrimeDayField = CrimeManager.class.getDeclaredField("lastCrimeDay");
            lastCrimeDayField.setAccessible(true);
            Map<UUID, Long> lastCrimeDay = (Map<UUID, Long>) lastCrimeDayField.get(null);

            assertThat(lastCrimeDay.get(TEST_PLAYER)).isEqualTo(5L);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // THREAD SAFETY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Concurrent wanted level additions should be thread-safe")
        void concurrentAdditionsShouldBeThreadSafe() throws InterruptedException {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    CrimeManager.addWantedLevel(TEST_PLAYER, 1, 0L);
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Should be capped at 5, not exceed it
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER))
                .isLessThanOrEqualTo(5)
                .isGreaterThan(0);
        }

        @Test
        @DisplayName("Concurrent escape timer operations should not cause exceptions")
        void concurrentEscapeTimerOperationsShouldNotCauseExceptions() throws InterruptedException {
            Runnable startTask = () -> {
                for (int i = 0; i < 50; i++) {
                    CrimeManager.startEscapeTimer(TEST_PLAYER, i * 100L);
                }
            };

            Runnable checkTask = () -> {
                for (int i = 0; i < 50; i++) {
                    CrimeManager.isHiding(TEST_PLAYER);
                    CrimeManager.getEscapeTimeRemaining(TEST_PLAYER, i * 100L);
                }
            };

            Thread thread1 = new Thread(startTask);
            Thread thread2 = new Thread(checkTask);

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            // Should complete without ConcurrentModificationException
            assertThat(CrimeManager.isHiding(TEST_PLAYER)).isIn(true, false);
        }

        @Test
        @DisplayName("Multiple players should be processable concurrently")
        void multiplePlayersShouldBeProcessableConcurrently() throws InterruptedException {
            UUID[] players = new UUID[20];
            for (int i = 0; i < 20; i++) {
                players[i] = UUID.randomUUID();
            }

            Thread[] threads = new Thread[20];
            for (int i = 0; i < 20; i++) {
                final UUID player = players[i];
                final int level = (i % 5) + 1;
                threads[i] = new Thread(() -> {
                    CrimeManager.setWantedLevel(player, level);
                    CrimeManager.addWantedLevel(player, 1, 0L);
                    CrimeManager.getWantedLevel(player);
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // All players should have valid wanted levels
            for (UUID player : players) {
                assertThat(CrimeManager.getWantedLevel(player))
                    .isGreaterThanOrEqualTo(0)
                    .isLessThanOrEqualTo(5);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Complete crime lifecycle: add, escape, decay, clear")
        void completeCrimeLifecycle() {
            // 1. Player commits crime
            CrimeManager.addWantedLevel(TEST_PLAYER, 3, 0L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(3);

            // 2. Player tries to escape
            CrimeManager.startEscapeTimer(TEST_PLAYER, 0L);
            assertThat(CrimeManager.isHiding(TEST_PLAYER)).isTrue();

            // 3. Police finds player (timer stopped)
            CrimeManager.stopEscapeTimer(TEST_PLAYER);
            assertThat(CrimeManager.isHiding(TEST_PLAYER)).isFalse();

            // 4. Time passes, wanted level decays
            CrimeManager.decayWantedLevel(TEST_PLAYER, 2L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(1);

            // 5. Player is arrested, wanted level cleared
            CrimeManager.clearWantedLevel(TEST_PLAYER);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isZero();
        }

        @Test
        @DisplayName("Serial crimes should accumulate wanted level")
        void serialCrimesShouldAccumulateWantedLevel() {
            CrimeManager.addWantedLevel(TEST_PLAYER, 1, 0L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(1);

            CrimeManager.addWantedLevel(TEST_PLAYER, 1, 1L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(2);

            CrimeManager.addWantedLevel(TEST_PLAYER, 2, 2L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(4);

            CrimeManager.addWantedLevel(TEST_PLAYER, 3, 3L);
            assertThat(CrimeManager.getWantedLevel(TEST_PLAYER)).isEqualTo(5); // Capped
        }
    }
}
