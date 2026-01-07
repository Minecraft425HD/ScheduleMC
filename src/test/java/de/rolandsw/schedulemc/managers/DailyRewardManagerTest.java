package de.rolandsw.schedulemc.managers;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.managers.DailyRewardManager.DailyReward;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for DailyRewardManager (Daily Login Reward System)
 *
 * Tests cover:
 * - Daily claim mechanics (24-hour cooldown)
 * - Streak tracking (consecutive days, 48h expiry)
 * - Streak bonus calculation
 * - canClaim checking
 * - Auto-claim on login
 * - Time until next claim formatting
 * - Statistics retrieval
 * - Edge cases: multiple claims, streak expiry
 *
 * BUSINESS LOGIC:
 * - Base reward: configurable (default ~100€)
 * - Streak bonus: configurable per day (default ~10€)
 * - Max streak: configurable (default 7 days)
 * - Claim cooldown: 24 hours
 * - Streak expires: 48 hours without claim
 */
@DisplayName("DailyRewardManager Tests")
class DailyRewardManagerTest {

    private static final UUID TEST_PLAYER = UUID.randomUUID();
    private static final UUID TEST_PLAYER_2 = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        clearRewards();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearRewards();
    }

    // ═══════════════════════════════════════════════════════════
    // DAILY CLAIM MECHANICS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Daily Claim Mechanics Tests")
    class DailyClaimMechanicsTests {

        @Test
        @DisplayName("New player should be able to claim")
        void newPlayerShouldBeAbleToClaim() {
            boolean canClaim = DailyRewardManager.canClaim(TEST_PLAYER);

            assertThat(canClaim).isTrue();
        }

        @Test
        @DisplayName("Player should not be able to claim twice in same day")
        void playerShouldNotBeAbleToClaimTwiceInSameDay() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                // First claim
                DailyRewardManager.claimDaily(TEST_PLAYER);

                // Second claim attempt
                boolean canClaim = DailyRewardManager.canClaim(TEST_PLAYER);

                assertThat(canClaim).isFalse();
            }
        }

        @Test
        @DisplayName("Claiming twice should return 0 on second attempt")
        void claimingTwiceShouldReturnZeroOnSecondAttempt() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyRewardManager.claimDaily(TEST_PLAYER);
                double secondClaim = DailyRewardManager.claimDaily(TEST_PLAYER);

                assertThat(secondClaim).isZero();
            }
        }

        @Test
        @DisplayName("First claim should return base reward")
        void firstClaimShouldReturnBaseReward() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                double amount = DailyRewardManager.claimDaily(TEST_PLAYER);

                assertThat(amount).isEqualTo(100.0); // Base reward only (no streak)
            }
        }

        @Test
        @DisplayName("Claim should update last claim time")
        void claimShouldUpdateLastClaimTime() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);
                long timeBefore = reward.getLastClaimTime();

                DailyRewardManager.claimDaily(TEST_PLAYER);

                long timeAfter = reward.getLastClaimTime();

                assertThat(timeAfter).isGreaterThan(timeBefore);
            }
        }

        @Test
        @DisplayName("Multiple players should track independently")
        void multiplePlayersShouldTrackIndependently() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyRewardManager.claimDaily(TEST_PLAYER);

                assertThat(DailyRewardManager.canClaim(TEST_PLAYER)).isFalse();
                assertThat(DailyRewardManager.canClaim(TEST_PLAYER_2)).isTrue();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STREAK TRACKING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Streak Tracking Tests")
    class StreakTrackingTests {

        @Test
        @DisplayName("First claim should set streak to 1")
        void firstClaimShouldSetStreakToOne() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyRewardManager.claimDaily(TEST_PLAYER);

                assertThat(DailyRewardManager.getStreak(TEST_PLAYER)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("Consecutive claims should increase streak")
        void consecutiveClaimsShouldIncreaseStreak() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

                // Day 1
                DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(reward.getCurrentStreak()).isEqualTo(1);

                // Simulate 25 hours passing (valid streak)
                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);

                // Day 2
                DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(reward.getCurrentStreak()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("Streak should reset after 48 hours")
        void streakShouldResetAfter48Hours() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

                // Day 1
                DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(reward.getCurrentStreak()).isEqualTo(1);

                // Simulate 49 hours passing (streak expired)
                simulateTimePassing(reward, 49 * 60 * 60 * 1000L);

                // Next claim should reset streak
                DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(reward.getCurrentStreak()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("Longest streak should be tracked")
        void longestStreakShouldBeTracked() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

                // Build streak to 5
                for (int i = 0; i < 5; i++) {
                    DailyRewardManager.claimDaily(TEST_PLAYER);
                    if (i < 4) {
                        simulateTimePassing(reward, 25 * 60 * 60 * 1000L);
                    }
                }

                assertThat(reward.getLongestStreak()).isEqualTo(5);

                // Reset streak
                simulateTimePassing(reward, 49 * 60 * 60 * 1000L);
                DailyRewardManager.claimDaily(TEST_PLAYER);

                // Longest should still be 5
                assertThat(reward.getLongestStreak()).isEqualTo(5);
            }
        }

        @Test
        @DisplayName("New player should have zero streak")
        void newPlayerShouldHaveZeroStreak() {
            assertThat(DailyRewardManager.getStreak(TEST_PLAYER)).isZero();
        }

        @Test
        @DisplayName("isStreakValid should return false for new players")
        void isStreakValidShouldReturnFalseForNewPlayers() {
            DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

            assertThat(reward.isStreakValid()).isFalse();
        }

        @Test
        @DisplayName("isStreakValid should return true within 48 hours")
        void isStreakValidShouldReturnTrueWithin48Hours() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);
                DailyRewardManager.claimDaily(TEST_PLAYER);

                // 25 hours later (within 48h window)
                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);

                assertThat(reward.isStreakValid()).isTrue();
            }
        }

        @Test
        @DisplayName("isStreakValid should return false after 48 hours")
        void isStreakValidShouldReturnFalseAfter48Hours() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);
                DailyRewardManager.claimDaily(TEST_PLAYER);

                // 49 hours later
                simulateTimePassing(reward, 49 * 60 * 60 * 1000L);

                assertThat(reward.isStreakValid()).isFalse();
            }
        }

        @Test
        @DisplayName("Streak reset should work")
        void streakResetShouldWork() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(DailyRewardManager.getStreak(TEST_PLAYER)).isEqualTo(1);

                DailyRewardManager.resetStreak(TEST_PLAYER);

                assertThat(DailyRewardManager.getStreak(TEST_PLAYER)).isZero();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STREAK BONUS CALCULATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Streak Bonus Calculation Tests")
    class StreakBonusCalculationTests {

        @Test
        @DisplayName("Second day should add streak bonus")
        void secondDayShouldAddStreakBonus() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

                // Day 1: 100€ (base)
                double day1 = DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(day1).isEqualTo(100.0);

                // Simulate 25 hours
                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);

                // Day 2: 100€ (base) + 10€ (bonus for 2-day streak)
                double day2 = DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(day2).isEqualTo(110.0);
            }
        }

        @Test
        @DisplayName("Streak bonus should scale with consecutive days")
        void streakBonusShouldScaleWithDays() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

                // Day 1: 100€
                double day1 = DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(day1).isEqualTo(100.0);

                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);

                // Day 2: 100€ + 10€
                double day2 = DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(day2).isEqualTo(110.0);

                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);

                // Day 3: 100€ + 20€
                double day3 = DailyRewardManager.claimDaily(TEST_PLAYER);
                assertThat(day3).isEqualTo(120.0);
            }
        }

        @Test
        @DisplayName("Streak bonus should cap at max streak")
        void streakBonusShouldCapAtMaxStreak() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

                // Build 10-day streak (max is 7)
                for (int i = 0; i < 10; i++) {
                    DailyRewardManager.claimDaily(TEST_PLAYER);
                    if (i < 9) {
                        simulateTimePassing(reward, 25 * 60 * 60 * 1000L);
                    }
                }

                // Last claim should be capped at 7-day bonus
                // 100€ + (6 days * 10€) = 160€
                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);
                double cappedAmount = DailyRewardManager.claimDaily(TEST_PLAYER);

                assertThat(cappedAmount).isEqualTo(160.0);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // AUTO-CLAIM ON LOGIN TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Auto-Claim on Login Tests")
    class AutoClaimOnLoginTests {

        @Test
        @DisplayName("Auto-claim should succeed for eligible player")
        void autoClaimShouldSucceedForEligiblePlayer() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockPlayer.getUUID()).thenReturn(TEST_PLAYER);

                boolean result = DailyRewardManager.claimOnLogin(mockPlayer);

                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("Auto-claim should fail if already claimed today")
        void autoClaimShouldFailIfAlreadyClaimed() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockPlayer.getUUID()).thenReturn(TEST_PLAYER);

                // First claim
                DailyRewardManager.claimDaily(TEST_PLAYER);

                // Second attempt should fail
                boolean result = DailyRewardManager.claimOnLogin(mockPlayer);

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Auto-claim should deposit to economy account")
        void autoClaimShouldDepositToEconomyAccount() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockPlayer.getUUID()).thenReturn(TEST_PLAYER);

                DailyRewardManager.claimOnLogin(mockPlayer);

                economyMock.verify(() -> EconomyManager.deposit(
                    eq(TEST_PLAYER), eq(100.0), eq(TransactionType.DAILY_REWARD), anyString()
                ));
            }
        }

        @Test
        @DisplayName("Auto-claim should send message to player")
        void autoClaimShouldSendMessageToPlayer() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockPlayer.getUUID()).thenReturn(TEST_PLAYER);

                DailyRewardManager.claimOnLogin(mockPlayer);

                verify(mockPlayer, atLeastOnce()).sendSystemMessage(any());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TIME UNTIL NEXT CLAIM TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Time Until Next Claim Tests")
    class TimeUntilNextClaimTests {

        @Test
        @DisplayName("Time until next should be 0 for new player")
        void timeUntilNextShouldBeZeroForNewPlayer() {
            long time = DailyRewardManager.getTimeUntilNextClaim(TEST_PLAYER);

            assertThat(time).isZero();
        }

        @Test
        @DisplayName("Time until next should be positive after claiming")
        void timeUntilNextShouldBePositiveAfterClaiming() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyRewardManager.claimDaily(TEST_PLAYER);

                long time = DailyRewardManager.getTimeUntilNextClaim(TEST_PLAYER);

                assertThat(time).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("Formatted time should match HH:MM:SS pattern")
        void formattedTimeShouldMatchPattern() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyRewardManager.claimDaily(TEST_PLAYER);

                String formatted = DailyRewardManager.getFormattedTimeUntilNext(TEST_PLAYER);

                assertThat(formatted).matches("\\d{2}:\\d{2}:\\d{2}");
            }
        }

        @Test
        @DisplayName("hasClaimedToday should return false for new player")
        void hasClaimedTodayShouldReturnFalseForNewPlayer() {
            DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

            assertThat(reward.hasClaimedToday()).isFalse();
        }

        @Test
        @DisplayName("hasClaimedToday should return true after claiming")
        void hasClaimedTodayShouldReturnTrueAfterClaiming() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);
                DailyRewardManager.claimDaily(TEST_PLAYER);

                assertThat(reward.hasClaimedToday()).isTrue();
            }
        }

        @Test
        @DisplayName("hasClaimedToday should return false after 24 hours")
        void hasClaimedTodayShouldReturnFalseAfter24Hours() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);
                DailyRewardManager.claimDaily(TEST_PLAYER);

                // Simulate 25 hours passing
                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);

                assertThat(reward.hasClaimedToday()).isFalse();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Stats should include all required fields")
        void statsShouldIncludeAllFields() {
            Map<String, Object> stats = DailyRewardManager.getStats(TEST_PLAYER);

            assertThat(stats).containsKeys(
                "currentStreak",
                "longestStreak",
                "totalClaims",
                "canClaim",
                "timeUntilNext"
            );
        }

        @Test
        @DisplayName("Stats should show correct current streak")
        void statsShouldShowCorrectCurrentStreak() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyRewardManager.claimDaily(TEST_PLAYER);

                Map<String, Object> stats = DailyRewardManager.getStats(TEST_PLAYER);

                assertThat(stats.get("currentStreak")).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("Stats should show correct total claims")
        void statsShouldShowCorrectTotalClaims() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

                // Claim 3 times
                DailyRewardManager.claimDaily(TEST_PLAYER);
                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);
                DailyRewardManager.claimDaily(TEST_PLAYER);
                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);
                DailyRewardManager.claimDaily(TEST_PLAYER);

                Map<String, Object> stats = DailyRewardManager.getStats(TEST_PLAYER);

                assertThat(stats.get("totalClaims")).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("Stats should show canClaim status")
        void statsShouldShowCanClaimStatus() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                // Before claim
                Map<String, Object> statsBefore = DailyRewardManager.getStats(TEST_PLAYER);
                assertThat(statsBefore.get("canClaim")).isEqualTo(true);

                // After claim
                DailyRewardManager.claimDaily(TEST_PLAYER);
                Map<String, Object> statsAfter = DailyRewardManager.getStats(TEST_PLAYER);
                assertThat(statsAfter.get("canClaim")).isEqualTo(false);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("System should handle simultaneous claims gracefully")
        void systemShouldHandleSimultaneousClaimsGracefully() throws InterruptedException {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                Runnable claimTask = () -> DailyRewardManager.claimDaily(TEST_PLAYER);

                Thread thread1 = new Thread(claimTask);
                Thread thread2 = new Thread(claimTask);

                thread1.start();
                thread2.start();
                thread1.join();
                thread2.join();

                // Only one claim should succeed
                assertThat(DailyRewardManager.canClaim(TEST_PLAYER)).isFalse();
            }
        }

        @Test
        @DisplayName("Very long streak should be capped properly")
        void veryLongStreakShouldBeCappedProperly() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

                // Build 100-day streak
                for (int i = 0; i < 100; i++) {
                    DailyRewardManager.claimDaily(TEST_PLAYER);
                    if (i < 99) {
                        simulateTimePassing(reward, 25 * 60 * 60 * 1000L);
                    }
                }

                // Bonus should still be capped at max streak (7 days)
                simulateTimePassing(reward, 25 * 60 * 60 * 1000L);
                double amount = DailyRewardManager.claimDaily(TEST_PLAYER);

                assertThat(amount).isEqualTo(160.0); // 100 + (6 * 10)
            }
        }

        @Test
        @DisplayName("Zero last claim time should handle gracefully")
        void zeroLastClaimTimeShouldHandleGracefully() {
            DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);

            // Should not throw exception
            assertThatCode(() -> reward.hasClaimedToday()).doesNotThrowAnyException();
            assertThatCode(() -> reward.isStreakValid()).doesNotThrowAnyException();
            assertThatCode(() -> reward.getTimeUntilNextClaim()).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(longs = {1, 12, 23, 24, 47})
        @DisplayName("Claim timing edge cases should work correctly")
        void claimTimingEdgeCasesShouldWorkCorrectly(long hoursLater) throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                DailyReward reward = DailyRewardManager.getReward(TEST_PLAYER);
                DailyRewardManager.claimDaily(TEST_PLAYER);

                simulateTimePassing(reward, hoursLater * 60 * 60 * 1000L);

                if (hoursLater < 24) {
                    // Should not be able to claim
                    assertThat(reward.hasClaimedToday()).isTrue();
                } else {
                    // Should be able to claim
                    assertThat(reward.hasClaimedToday()).isFalse();
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private void clearRewards() throws Exception {
        Field rewardsField = DailyRewardManager.class.getDeclaredField("rewards");
        rewardsField.setAccessible(true);
        Map<String, DailyReward> rewards = (Map<String, DailyReward>) rewardsField.get(null);
        rewards.clear();
    }

    private void simulateTimePassing(DailyReward reward, long millis) throws Exception {
        Field lastClaimField = DailyReward.class.getDeclaredField("lastClaimTime");
        lastClaimField.setAccessible(true);
        long currentTime = lastClaimField.getLong(reward);
        lastClaimField.setLong(reward, currentTime - millis);
    }

    private void mockConfigDefaults(MockedStatic<ModConfigHandler> configMock) {
        ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
        when(mockCommon.DAILY_REWARD).thenReturn(new MockConfigValue<>(100.0));
        when(mockCommon.DAILY_REWARD_STREAK_BONUS).thenReturn(new MockConfigValue<>(10.0));
        when(mockCommon.MAX_STREAK_DAYS).thenReturn(new MockConfigValue<>(7));
        configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);
    }

    // Mock config value class
    private static class MockConfigValue<T> implements net.minecraftforge.common.ForgeConfigSpec.ConfigValue<T> {
        private final T value;

        public MockConfigValue(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public net.minecraftforge.common.ForgeConfigSpec next() { return null; }
        @Override
        public java.util.List<String> getPath() { return java.util.Collections.emptyList(); }
        @Override
        public void save() {}
        @Override
        public void set(T value) {}
        @Override
        public void clearCache() {}
    }
}
