package de.rolandsw.schedulemc.achievement;

import de.rolandsw.schedulemc.economy.EconomyManager;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AchievementManager
 *
 * Tests cover:
 * - Achievement Registration (24 achievements)
 * - Player Achievement Management
 * - Progress Tracking (addProgress, setProgress)
 * - Achievement Unlocking with Economy Integration
 * - Getter Methods (by ID, all, by category)
 * - Statistics Generation
 * - Thread Safety
 * - Edge Cases and Error Handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AchievementManager Tests")
class AchievementManagerTest {

    @Mock
    private MinecraftServer mockServer;

    @TempDir
    Path tempDir;

    private AchievementManager manager;
    private MockedStatic<EconomyManager> economyManagerMock;

    @BeforeEach
    void setUp() {
        // Mock server directory
        when(mockServer.getServerDirectory()).thenReturn(tempDir.toFile());

        // Mock EconomyManager static methods
        economyManagerMock = mockStatic(EconomyManager.class);

        // Create manager instance (will trigger achievement registration)
        manager = AchievementManager.getInstance(mockServer);
    }

    @AfterEach
    void tearDown() {
        // Clean up static mocks
        if (economyManagerMock != null) {
            economyManagerMock.close();
        }

        // Reset singleton instance for next test
        try {
            java.lang.reflect.Field instanceField = AchievementManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ACHIEVEMENT REGISTRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Achievement Registration Tests")
    class AchievementRegistrationTests {

        @Test
        @DisplayName("Should register all 24 achievements on initialization")
        void testAchievementRegistration() {
            assertThat(manager.getAllAchievements()).hasSize(24);
        }

        @Test
        @DisplayName("Should register ECONOMY achievements correctly")
        void testEconomyAchievements() {
            List<Achievement> economyAchievements = manager.getAchievementsByCategory(AchievementCategory.ECONOMY);

            assertThat(economyAchievements).hasSize(7);
            assertThat(economyAchievements)
                .extracting(Achievement::getId)
                .containsExactlyInAnyOrder(
                    "FIRST_EURO", "RICH", "WEALTHY", "MILLIONAIRE",
                    "LOAN_MASTER", "SAVINGS_KING", "BIG_SPENDER"
                );
        }

        @Test
        @DisplayName("Should register CRIME achievements correctly")
        void testCrimeAchievements() {
            List<Achievement> crimeAchievements = manager.getAchievementsByCategory(AchievementCategory.CRIME);

            assertThat(crimeAchievements).hasSize(6);
            assertThat(crimeAchievements)
                .extracting(Achievement::getId)
                .containsExactlyInAnyOrder(
                    "FIRST_CRIME", "WANTED", "MOST_WANTED",
                    "ESCAPE_ARTIST", "PRISON_VETERAN", "CLEAN_RECORD"
                );
        }

        @Test
        @DisplayName("Should register PRODUCTION achievements correctly")
        void testProductionAchievements() {
            List<Achievement> productionAchievements = manager.getAchievementsByCategory(AchievementCategory.PRODUCTION);

            assertThat(productionAchievements).hasSize(5);
            assertThat(productionAchievements)
                .extracting(Achievement::getId)
                .containsExactlyInAnyOrder(
                    "HOBBYIST", "FARMER", "PRODUCER", "DRUG_LORD", "EMPIRE_BUILDER"
                );
        }

        @Test
        @DisplayName("Should register SOCIAL achievements correctly")
        void testSocialAchievements() {
            List<Achievement> socialAchievements = manager.getAchievementsByCategory(AchievementCategory.SOCIAL);

            assertThat(socialAchievements).hasSize(4);
            assertThat(socialAchievements)
                .extracting(Achievement::getId)
                .containsExactlyInAnyOrder(
                    "FIRST_PLOT", "PROPERTY_MOGUL", "LANDLORD", "POPULAR"
                );
        }

        @Test
        @DisplayName("Should have achievements with all tier types")
        void testAchievementTiers() {
            List<Achievement> all = new ArrayList<>(manager.getAllAchievements());

            assertThat(all).extracting(Achievement::getTier)
                .contains(
                    AchievementTier.BRONZE,
                    AchievementTier.SILVER,
                    AchievementTier.GOLD,
                    AchievementTier.DIAMOND,
                    AchievementTier.PLATINUM
                );
        }

        @Test
        @DisplayName("Should retrieve specific achievement by ID")
        void testGetAchievementById() {
            Achievement achievement = manager.getAchievement("FIRST_EURO");

            assertThat(achievement).isNotNull();
            assertThat(achievement.getId()).isEqualTo("FIRST_EURO");
            assertThat(achievement.getName()).isEqualTo("Erster Euro");
            assertThat(achievement.getCategory()).isEqualTo(AchievementCategory.ECONOMY);
            assertThat(achievement.getTier()).isEqualTo(AchievementTier.BRONZE);
        }

        @Test
        @DisplayName("Should return null for unknown achievement ID")
        void testGetUnknownAchievement() {
            Achievement achievement = manager.getAchievement("UNKNOWN_ACHIEVEMENT");
            assertThat(achievement).isNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYER ACHIEVEMENT MANAGEMENT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Player Achievement Management Tests")
    class PlayerAchievementManagementTests {

        @Test
        @DisplayName("Should create new PlayerAchievements for new player")
        void testGetPlayerAchievements_NewPlayer() {
            UUID playerUUID = UUID.randomUUID();

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);

            assertThat(playerAch).isNotNull();
            assertThat(playerAch.getPlayerUUID()).isEqualTo(playerUUID);
            assertThat(playerAch.getUnlockedCount()).isEqualTo(0);
            assertThat(playerAch.getTotalPointsEarned()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return same PlayerAchievements instance for same player")
        void testGetPlayerAchievements_SameInstance() {
            UUID playerUUID = UUID.randomUUID();

            PlayerAchievements first = manager.getPlayerAchievements(playerUUID);
            PlayerAchievements second = manager.getPlayerAchievements(playerUUID);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("Should maintain separate PlayerAchievements for different players")
        void testGetPlayerAchievements_DifferentPlayers() {
            UUID player1 = UUID.randomUUID();
            UUID player2 = UUID.randomUUID();

            PlayerAchievements playerAch1 = manager.getPlayerAchievements(player1);
            PlayerAchievements playerAch2 = manager.getPlayerAchievements(player2);

            assertThat(playerAch1).isNotSameAs(playerAch2);
            assertThat(playerAch1.getPlayerUUID()).isEqualTo(player1);
            assertThat(playerAch2.getPlayerUUID()).isEqualTo(player2);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PROGRESS TRACKING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Progress Tracking Tests")
    class ProgressTrackingTests {

        @Test
        @DisplayName("Should add incremental progress correctly")
        void testAddProgress() {
            UUID playerUUID = UUID.randomUUID();

            manager.addProgress(playerUUID, "RICH", 5000.0);
            manager.addProgress(playerUUID, "RICH", 3000.0);

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.getProgress("RICH")).isEqualTo(8000.0);
        }

        @Test
        @DisplayName("Should set absolute progress correctly")
        void testSetProgress() {
            UUID playerUUID = UUID.randomUUID();

            manager.setProgress(playerUUID, "WEALTHY", 50000.0);

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.getProgress("WEALTHY")).isEqualTo(50000.0);
        }

        @Test
        @DisplayName("Should auto-unlock achievement when progress reaches requirement via addProgress")
        void testAddProgress_AutoUnlock() {
            UUID playerUUID = UUID.randomUUID();

            // FIRST_EURO requires 1.0
            manager.addProgress(playerUUID, "FIRST_EURO", 0.5);
            assertThat(manager.getPlayerAchievements(playerUUID).isUnlocked("FIRST_EURO")).isFalse();

            manager.addProgress(playerUUID, "FIRST_EURO", 0.5);
            assertThat(manager.getPlayerAchievements(playerUUID).isUnlocked("FIRST_EURO")).isTrue();

            // Verify economy deposit was called
            economyManagerMock.verify(() ->
                EconomyManager.deposit(eq(playerUUID), eq(100.0), any(), anyString()),
                times(1)
            );
        }

        @Test
        @DisplayName("Should auto-unlock achievement when progress reaches requirement via setProgress")
        void testSetProgress_AutoUnlock() {
            UUID playerUUID = UUID.randomUUID();

            // MILLIONAIRE requires 1,000,000
            manager.setProgress(playerUUID, "MILLIONAIRE", 1000000.0);

            assertThat(manager.getPlayerAchievements(playerUUID).isUnlocked("MILLIONAIRE")).isTrue();

            // Verify economy deposit with GOLD tier reward (2000.0)
            economyManagerMock.verify(() ->
                EconomyManager.deposit(eq(playerUUID), eq(2000.0), any(), anyString()),
                times(1)
            );
        }

        @Test
        @DisplayName("Should ignore progress for unknown achievement")
        void testAddProgress_UnknownAchievement() {
            UUID playerUUID = UUID.randomUUID();

            // Should not throw exception
            assertThatCode(() -> manager.addProgress(playerUUID, "UNKNOWN", 100.0))
                .doesNotThrowAnyException();

            // No progress should be recorded
            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.getProgress("UNKNOWN")).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should ignore progress for already unlocked achievement")
        void testAddProgress_AlreadyUnlocked() {
            UUID playerUUID = UUID.randomUUID();

            // Unlock achievement
            manager.unlockAchievement(playerUUID, "FIRST_EURO");

            // Try to add progress - should be ignored
            manager.addProgress(playerUUID, "FIRST_EURO", 100.0);

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            // Progress should still be 0 as it was ignored
            assertThat(playerAch.getProgress("FIRST_EURO")).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle negative progress values")
        void testAddProgress_NegativeValue() {
            UUID playerUUID = UUID.randomUUID();

            manager.setProgress(playerUUID, "RICH", 5000.0);
            manager.addProgress(playerUUID, "RICH", -2000.0);

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.getProgress("RICH")).isEqualTo(3000.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ACHIEVEMENT UNLOCKING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Achievement Unlocking Tests")
    class AchievementUnlockingTests {

        @Test
        @DisplayName("Should unlock achievement directly and grant reward")
        void testUnlockAchievement() {
            UUID playerUUID = UUID.randomUUID();

            boolean unlocked = manager.unlockAchievement(playerUUID, "FIRST_EURO");

            assertThat(unlocked).isTrue();

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.isUnlocked("FIRST_EURO")).isTrue();
            assertThat(playerAch.getTotalPointsEarned()).isEqualTo(100.0); // BRONZE tier

            // Verify economy deposit
            economyManagerMock.verify(() ->
                EconomyManager.deposit(
                    eq(playerUUID),
                    eq(100.0),
                    any(),
                    contains("Achievement")
                ),
                times(1)
            );
        }

        @Test
        @DisplayName("Should return false when unlocking already unlocked achievement")
        void testUnlockAchievement_AlreadyUnlocked() {
            UUID playerUUID = UUID.randomUUID();

            manager.unlockAchievement(playerUUID, "FIRST_EURO");
            boolean secondUnlock = manager.unlockAchievement(playerUUID, "FIRST_EURO");

            assertThat(secondUnlock).isFalse();

            // Economy deposit should only be called once
            economyManagerMock.verify(() ->
                EconomyManager.deposit(any(UUID.class), anyDouble(), any(), anyString()),
                times(1)
            );
        }

        @Test
        @DisplayName("Should return false for unknown achievement")
        void testUnlockAchievement_Unknown() {
            UUID playerUUID = UUID.randomUUID();

            boolean unlocked = manager.unlockAchievement(playerUUID, "UNKNOWN_ACHIEVEMENT");

            assertThat(unlocked).isFalse();

            // No economy deposit should be made
            economyManagerMock.verify(() ->
                EconomyManager.deposit(any(UUID.class), anyDouble(), any(), anyString()),
                never()
            );
        }

        @Test
        @DisplayName("Should grant correct reward for each tier")
        void testUnlockAchievement_TierRewards() {
            UUID playerUUID = UUID.randomUUID();

            // BRONZE: 100.0
            manager.unlockAchievement(playerUUID, "FIRST_EURO");
            economyManagerMock.verify(() ->
                EconomyManager.deposit(eq(playerUUID), eq(100.0), any(), anyString())
            );

            // SILVER: 500.0
            manager.unlockAchievement(playerUUID, "ESCAPE_ARTIST");
            economyManagerMock.verify(() ->
                EconomyManager.deposit(eq(playerUUID), eq(500.0), any(), anyString())
            );

            // GOLD: 2000.0
            manager.unlockAchievement(playerUUID, "MILLIONAIRE");
            economyManagerMock.verify(() ->
                EconomyManager.deposit(eq(playerUUID), eq(2000.0), any(), anyString())
            );

            // DIAMOND: 10000.0
            manager.unlockAchievement(playerUUID, "BIG_SPENDER");
            economyManagerMock.verify(() ->
                EconomyManager.deposit(eq(playerUUID), eq(10000.0), any(), anyString())
            );

            // PLATINUM: 50000.0
            manager.unlockAchievement(playerUUID, "EMPIRE_BUILDER");
            economyManagerMock.verify(() ->
                EconomyManager.deposit(eq(playerUUID), eq(50000.0), any(), anyString())
            );
        }

        @Test
        @DisplayName("Should record unlock timestamp")
        void testUnlockAchievement_Timestamp() {
            UUID playerUUID = UUID.randomUUID();
            long beforeUnlock = System.currentTimeMillis();

            manager.unlockAchievement(playerUUID, "FIRST_EURO");

            long afterUnlock = System.currentTimeMillis();

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            long timestamp = playerAch.getUnlockTimestamp("FIRST_EURO");

            assertThat(timestamp)
                .isGreaterThanOrEqualTo(beforeUnlock)
                .isLessThanOrEqualTo(afterUnlock);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should generate correct statistics for new player")
        void testGetStatistics_NewPlayer() {
            UUID playerUUID = UUID.randomUUID();

            String stats = manager.getStatistics(playerUUID);

            assertThat(stats).contains("0/24");
            assertThat(stats).contains("0.0%");
            assertThat(stats).contains("0.00€");
        }

        @Test
        @DisplayName("Should generate correct statistics after unlocking achievements")
        void testGetStatistics_WithUnlocks() {
            UUID playerUUID = UUID.randomUUID();

            manager.unlockAchievement(playerUUID, "FIRST_EURO"); // 100.0€
            manager.unlockAchievement(playerUUID, "RICH"); // 100.0€
            manager.unlockAchievement(playerUUID, "ESCAPE_ARTIST"); // 500.0€

            String stats = manager.getStatistics(playerUUID);

            assertThat(stats).contains("3/24"); // 3 unlocked out of 24
            assertThat(stats).contains("12.5%"); // (3/24)*100 = 12.5%
            assertThat(stats).contains("700.00€"); // 100 + 100 + 500
        }

        @Test
        @DisplayName("Should generate correct statistics at 100% completion")
        void testGetStatistics_AllUnlocked() {
            UUID playerUUID = UUID.randomUUID();

            // Unlock all 24 achievements
            manager.getAllAchievements().forEach(achievement ->
                manager.unlockAchievement(playerUUID, achievement.getId())
            );

            String stats = manager.getStatistics(playerUUID);

            assertThat(stats).contains("24/24");
            assertThat(stats).contains("100.0%");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CATEGORY FILTERING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Category Filtering Tests")
    class CategoryFilteringTests {

        @Test
        @DisplayName("Should filter achievements by ECONOMY category")
        void testGetAchievementsByCategory_Economy() {
            List<Achievement> achievements = manager.getAchievementsByCategory(AchievementCategory.ECONOMY);

            assertThat(achievements)
                .hasSize(7)
                .allMatch(a -> a.getCategory() == AchievementCategory.ECONOMY);
        }

        @Test
        @DisplayName("Should filter achievements by CRIME category")
        void testGetAchievementsByCategory_Crime() {
            List<Achievement> achievements = manager.getAchievementsByCategory(AchievementCategory.CRIME);

            assertThat(achievements)
                .hasSize(6)
                .allMatch(a -> a.getCategory() == AchievementCategory.CRIME);
        }

        @Test
        @DisplayName("Should filter achievements by PRODUCTION category")
        void testGetAchievementsByCategory_Production() {
            List<Achievement> achievements = manager.getAchievementsByCategory(AchievementCategory.PRODUCTION);

            assertThat(achievements)
                .hasSize(5)
                .allMatch(a -> a.getCategory() == AchievementCategory.PRODUCTION);
        }

        @Test
        @DisplayName("Should filter achievements by SOCIAL category")
        void testGetAchievementsByCategory_Social() {
            List<Achievement> achievements = manager.getAchievementsByCategory(AchievementCategory.SOCIAL);

            assertThat(achievements)
                .hasSize(4)
                .allMatch(a -> a.getCategory() == AchievementCategory.SOCIAL);
        }

        @Test
        @DisplayName("Should return empty list for category with no achievements")
        void testGetAchievementsByCategory_Empty() {
            List<Achievement> achievements = manager.getAchievementsByCategory(AchievementCategory.EXPLORATION);

            assertThat(achievements).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // THREAD SAFETY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent player achievement creation")
        @Timeout(10)
        void testConcurrentPlayerAchievementCreation() throws InterruptedException {
            int numThreads = 10;
            int playersPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);
            Set<UUID> allPlayerUUIDs = ConcurrentHashMap.newKeySet();

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < playersPerThread; j++) {
                            UUID playerUUID = UUID.randomUUID();
                            allPlayerUUIDs.add(playerUUID);
                            manager.getPlayerAchievements(playerUUID);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertThat(allPlayerUUIDs).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle concurrent progress updates for same player")
        @Timeout(10)
        void testConcurrentProgressUpdates() throws InterruptedException {
            UUID playerUUID = UUID.randomUUID();
            int numThreads = 10;
            int updatesPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < updatesPerThread; j++) {
                            manager.addProgress(playerUUID, "BIG_SPENDER", 100.0);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            // Each thread adds 100 * 100.0 = 10000.0
            // 10 threads = 100000.0 total
            assertThat(playerAch.getProgress("BIG_SPENDER")).isEqualTo(100000.0);
        }

        @Test
        @DisplayName("Should handle concurrent unlock attempts safely")
        @Timeout(10)
        void testConcurrentUnlockAttempts() throws InterruptedException {
            UUID playerUUID = UUID.randomUUID();
            int numThreads = 20;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        boolean unlocked = manager.unlockAchievement(playerUUID, "FIRST_EURO");
                        if (unlocked) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // Only one thread should successfully unlock
            assertThat(successCount.get()).isEqualTo(1);

            // Achievement should be unlocked
            assertThat(manager.getPlayerAchievements(playerUUID).isUnlocked("FIRST_EURO")).isTrue();

            // Economy deposit should only be called once
            economyManagerMock.verify(() ->
                EconomyManager.deposit(any(UUID.class), anyDouble(), any(), anyString()),
                times(1)
            );
        }

        @Test
        @DisplayName("Should maintain data consistency under concurrent load")
        @Timeout(15)
        void testConcurrentLoadDataConsistency() throws InterruptedException {
            int numPlayers = 100;
            List<UUID> playerUUIDs = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                playerUUIDs.add(UUID.randomUUID());
            }

            ExecutorService executor = Executors.newFixedThreadPool(20);
            CountDownLatch latch = new CountDownLatch(numPlayers);

            // Each player gets concurrent operations
            for (UUID playerUUID : playerUUIDs) {
                executor.submit(() -> {
                    try {
                        manager.addProgress(playerUUID, "BIG_SPENDER", 10000.0);
                        manager.setProgress(playerUUID, "RICH", 5000.0);
                        manager.unlockAchievement(playerUUID, "FIRST_EURO");
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // Verify all players have consistent data
            for (UUID playerUUID : playerUUIDs) {
                PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
                assertThat(playerAch.getProgress("BIG_SPENDER")).isEqualTo(10000.0);
                assertThat(playerAch.getProgress("RICH")).isEqualTo(5000.0);
                assertThat(playerAch.isUnlocked("FIRST_EURO")).isTrue();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON PATTERN TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonPatternTests {

        @Test
        @DisplayName("Should return same instance for multiple getInstance calls")
        void testSingletonInstance() {
            AchievementManager instance1 = AchievementManager.getInstance(mockServer);
            AchievementManager instance2 = AchievementManager.getInstance(mockServer);

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should return instance from getInstance without parameter after initialization")
        void testGetInstance_NoParameter() {
            AchievementManager.getInstance(mockServer);

            AchievementManager instance = AchievementManager.getInstance();

            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("Should handle concurrent singleton initialization")
        @Timeout(10)
        void testConcurrentSingletonInitialization() throws InterruptedException {
            // Reset singleton
            try {
                java.lang.reflect.Field instanceField = AchievementManager.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                instanceField.set(null, null);
            } catch (Exception e) {
                fail("Failed to reset singleton");
            }

            int numThreads = 20;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);
            Set<AchievementManager> instances = ConcurrentHashMap.newKeySet();

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        instances.add(AchievementManager.getInstance(mockServer));
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // All threads should get the same instance
            assertThat(instances).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASES TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large progress values")
        void testVeryLargeProgress() {
            UUID playerUUID = UUID.randomUUID();

            manager.setProgress(playerUUID, "BIG_SPENDER", Double.MAX_VALUE / 2);

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.getProgress("BIG_SPENDER")).isEqualTo(Double.MAX_VALUE / 2);
        }

        @Test
        @DisplayName("Should handle zero progress values")
        void testZeroProgress() {
            UUID playerUUID = UUID.randomUUID();

            manager.setProgress(playerUUID, "RICH", 0.0);

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.getProgress("RICH")).isEqualTo(0.0);
            assertThat(playerAch.isUnlocked("RICH")).isFalse();
        }

        @Test
        @DisplayName("Should handle progress exactly at requirement threshold")
        void testProgressExactlyAtThreshold() {
            UUID playerUUID = UUID.randomUUID();

            // RICH requires exactly 10000.0
            manager.setProgress(playerUUID, "RICH", 10000.0);

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.isUnlocked("RICH")).isTrue();
        }

        @Test
        @DisplayName("Should handle progress above requirement threshold")
        void testProgressAboveThreshold() {
            UUID playerUUID = UUID.randomUUID();

            // RICH requires 10000.0, set to 20000.0
            manager.setProgress(playerUUID, "RICH", 20000.0);

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.isUnlocked("RICH")).isTrue();
            assertThat(playerAch.getProgress("RICH")).isEqualTo(20000.0);
        }

        @Test
        @DisplayName("Should handle getAllAchievements returning defensive copy")
        void testGetAllAchievements_DefensiveCopy() {
            Collection<Achievement> achievements1 = manager.getAllAchievements();
            Collection<Achievement> achievements2 = manager.getAllAchievements();

            assertThat(achievements1).isNotSameAs(achievements2);
            assertThat(achievements1).containsExactlyInAnyOrderElementsOf(achievements2);
        }

        @Test
        @DisplayName("Should handle multiple achievements unlocking simultaneously")
        void testMultipleSimultaneousUnlocks() {
            UUID playerUUID = UUID.randomUUID();

            manager.unlockAchievement(playerUUID, "FIRST_EURO");
            manager.unlockAchievement(playerUUID, "FIRST_CRIME");
            manager.unlockAchievement(playerUUID, "FIRST_PLOT");

            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);
            assertThat(playerAch.getUnlockedCount()).isEqualTo(3);
            assertThat(playerAch.getTotalPointsEarned()).isEqualTo(300.0); // 3 * 100 (all BRONZE)
        }
    }
}
