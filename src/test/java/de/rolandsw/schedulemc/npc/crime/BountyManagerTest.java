package de.rolandsw.schedulemc.npc.crime;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for BountyManager (Bounty System Manager)
 *
 * Tests cover:
 * - Auto-bounty creation (3+ stars, 2000€ per star)
 * - Manual bounty placement (economy integration, validation)
 * - Bounty claiming (reward payout, once-only)
 * - Active bounty retrieval
 * - Top bounties listing (sorted by amount)
 * - Bounty history tracking
 * - Expired bounty cleanup
 * - Statistics calculation
 * - Edge cases: self-bounties, insufficient funds, duplicate claims
 *
 * NOTE: These tests use reflection to reset singleton state and mock economy operations
 */
@DisplayName("BountyManager Tests")
class BountyManagerTest {

    private static final UUID TEST_CRIMINAL = UUID.randomUUID();
    private static final UUID TEST_PLACER = UUID.randomUUID();
    private static final UUID TEST_HUNTER = UUID.randomUUID();
    private static final UUID TEST_TARGET = UUID.randomUUID();

    private BountyManager bountyManager;
    private MinecraftServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance
        resetBountyManagerInstance();

        // Create mock server
        mockServer = createMockServer();

        // Get BountyManager instance (will create new one)
        bountyManager = BountyManager.getInstance(mockServer);

        // Clear all bounties
        clearActiveBounties();
        clearBountyHistory();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetBountyManagerInstance();
    }

    // ═══════════════════════════════════════════════════════════
    // AUTO-BOUNTY CREATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Auto-Bounty Creation Tests")
    class AutoBountyCreationTests {

        @ParameterizedTest
        @ValueSource(ints = {3, 4, 5})
        @DisplayName("Auto-bounty should be created for wanted level 3+")
        void autoBountyShouldBeCreatedForHighWantedLevel(int wantedLevel) {
            bountyManager.createAutoBounty(TEST_CRIMINAL, wantedLevel);

            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);

            assertThat(bounty).isNotNull();
            assertThat(bounty.getAmount()).isEqualTo(wantedLevel * 2000.0);
            assertThat(bounty.isActive()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2})
        @DisplayName("Auto-bounty should NOT be created for wanted level below 3")
        void autoBountyShouldNotBeCreatedForLowWantedLevel(int wantedLevel) {
            bountyManager.createAutoBounty(TEST_CRIMINAL, wantedLevel);

            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);

            assertThat(bounty).isNull();
        }

        @ParameterizedTest
        @CsvSource({
            "3, 6000.0",   // 3 stars = 6000€
            "4, 8000.0",   // 4 stars = 8000€
            "5, 10000.0"   // 5 stars = 10000€
        })
        @DisplayName("Auto-bounty amount should be 2000€ per star")
        void autoBountyAmountShouldBeCorrect(int stars, double expectedAmount) {
            bountyManager.createAutoBounty(TEST_CRIMINAL, stars);

            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);

            assertThat(bounty.getAmount()).isEqualTo(expectedAmount);
        }

        @Test
        @DisplayName("Auto-bounty should have null placer (police bounty)")
        void autoBountyShouldHaveNullPlacer() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);

            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);

            assertThat(bounty.getPlacedBy()).isNull();
        }

        @Test
        @DisplayName("Auto-bounty should include wanted level in reason")
        void autoBountyShouldIncludeWantedLevelInReason() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 4);

            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);

            assertThat(bounty.getReason())
                .contains("Wanted Level")
                .contains("4");
        }

        @Test
        @DisplayName("Creating auto-bounty when one exists should increase amount")
        void creatingAutoBountyWhenExistsShouldIncrease() {
            // Create initial bounty
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
            BountyData first = bountyManager.getActiveBounty(TEST_CRIMINAL);
            double initialAmount = first.getAmount();

            // Create another auto-bounty
            bountyManager.createAutoBounty(TEST_CRIMINAL, 4);
            BountyData second = bountyManager.getActiveBounty(TEST_CRIMINAL);

            assertThat(second.getAmount()).isGreaterThan(initialAmount);
            assertThat(second.getBountyId()).isEqualTo(first.getBountyId()); // Same bounty
        }

        @Test
        @DisplayName("Auto-bounty increase should be 2000€ regardless of new wanted level")
        void autoBountyIncreaseShouldBeConstant() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
            double initial = bountyManager.getActiveBounty(TEST_CRIMINAL).getAmount();

            bountyManager.createAutoBounty(TEST_CRIMINAL, 5);
            double after = bountyManager.getActiveBounty(TEST_CRIMINAL).getAmount();

            assertThat(after - initial).isEqualTo(2000.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MANUAL BOUNTY PLACEMENT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Manual Bounty Placement Tests")
    class ManualBountyPlacementTests {

        @Test
        @DisplayName("Manual bounty should be created successfully with sufficient funds")
        void manualBountyShouldBeCreated() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.withdraw(
                    eq(TEST_PLACER), eq(5000.0), any(), anyString()
                )).thenReturn(true);

                boolean result = bountyManager.placeBounty(TEST_PLACER, TEST_TARGET, 5000.0, "Revenge");

                assertThat(result).isTrue();
                BountyData bounty = bountyManager.getActiveBounty(TEST_TARGET);
                assertThat(bounty).isNotNull();
                assertThat(bounty.getAmount()).isEqualTo(5000.0);
                assertThat(bounty.getReason()).isEqualTo("Revenge");
            }
        }

        @Test
        @DisplayName("Manual bounty should fail with insufficient funds")
        void manualBountyShouldFailWithInsufficientFunds() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.withdraw(
                    eq(TEST_PLACER), eq(5000.0), any(), anyString()
                )).thenReturn(false);

                boolean result = bountyManager.placeBounty(TEST_PLACER, TEST_TARGET, 5000.0, "Test");

                assertThat(result).isFalse();
                assertThat(bountyManager.getActiveBounty(TEST_TARGET)).isNull();
            }
        }

        @Test
        @DisplayName("Self-bounty should be rejected")
        void selfBountyShouldBeRejected() {
            boolean result = bountyManager.placeBounty(TEST_PLACER, TEST_PLACER, 1000.0, "Self");

            assertThat(result).isFalse();
            assertThat(bountyManager.getActiveBounty(TEST_PLACER)).isNull();
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0, -100.0})
        @DisplayName("Bounty with zero or negative amount should be rejected")
        void bountyWithInvalidAmountShouldBeRejected(double invalidAmount) {
            boolean result = bountyManager.placeBounty(TEST_PLACER, TEST_TARGET, invalidAmount, "Test");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Manual bounty should have placer UUID")
        void manualBountyShouldHavePlacer() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.withdraw(any(), anyDouble(), any(), anyString()))
                    .thenReturn(true);

                bountyManager.placeBounty(TEST_PLACER, TEST_TARGET, 1000.0, "Test");

                BountyData bounty = bountyManager.getActiveBounty(TEST_TARGET);
                assertThat(bounty.getPlacedBy()).isEqualTo(TEST_PLACER);
            }
        }

        @Test
        @DisplayName("Placing bounty on existing target should increase amount")
        void placingBountyOnExistingTargetShouldIncrease() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.withdraw(any(), anyDouble(), any(), anyString()))
                    .thenReturn(true);

                bountyManager.placeBounty(TEST_PLACER, TEST_TARGET, 1000.0, "First");
                bountyManager.placeBounty(UUID.randomUUID(), TEST_TARGET, 500.0, "Second");

                BountyData bounty = bountyManager.getActiveBounty(TEST_TARGET);
                assertThat(bounty.getAmount()).isEqualTo(1500.0);
            }
        }

        @Test
        @DisplayName("Economy withdraw should be called with correct parameters")
        void economyWithdrawShouldBeCalledCorrectly() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.withdraw(
                    eq(TEST_PLACER), eq(2500.0), eq(TransactionType.OTHER), anyString()
                )).thenReturn(true);

                bountyManager.placeBounty(TEST_PLACER, TEST_TARGET, 2500.0, "Test");

                economyMock.verify(() -> EconomyManager.withdraw(
                    eq(TEST_PLACER), eq(2500.0), eq(TransactionType.OTHER), anyString()
                ));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BOUNTY CLAIMING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Bounty Claiming Tests")
    class BountyClaimingTests {

        @Test
        @DisplayName("Bounty should be claimed successfully")
        void bountyShouldBeClaimedSuccessfully() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                // Create bounty
                bountyManager.createAutoBounty(TEST_CRIMINAL, 3);

                // Claim bounty
                boolean result = bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);

                assertThat(result).isTrue();
                assertThat(bountyManager.getActiveBounty(TEST_CRIMINAL)).isNull();
            }
        }

        @Test
        @DisplayName("Claiming should deposit reward to hunter")
        void claimingShouldDepositRewardToHunter() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
                double bountyAmount = bountyManager.getActiveBounty(TEST_CRIMINAL).getAmount();

                bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);

                economyMock.verify(() -> EconomyManager.deposit(
                    eq(TEST_HUNTER), eq(bountyAmount), eq(TransactionType.OTHER), anyString()
                ));
            }
        }

        @Test
        @DisplayName("Claiming non-existent bounty should fail")
        void claimingNonExistentBountyShouldFail() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                boolean result = bountyManager.claimBounty(TEST_HUNTER, UUID.randomUUID());

                assertThat(result).isFalse();
                economyMock.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("Claiming same bounty twice should fail")
        void claimingSameBountyTwiceShouldFail() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                bountyManager.createAutoBounty(TEST_CRIMINAL, 3);

                bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);
                boolean secondClaim = bountyManager.claimBounty(UUID.randomUUID(), TEST_CRIMINAL);

                assertThat(secondClaim).isFalse();
            }
        }

        @Test
        @DisplayName("Claimed bounty should be moved to history")
        void claimedBountyShouldBeMovedToHistory() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
                String bountyId = bountyManager.getActiveBounty(TEST_CRIMINAL).getBountyId();

                bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);

                List<BountyData> history = bountyManager.getBountyHistory(TEST_CRIMINAL);
                assertThat(history).hasSize(1);
                assertThat(history.get(0).getBountyId()).isEqualTo(bountyId);
            }
        }

        @Test
        @DisplayName("Claimed bounty should be removed from active list")
        void claimedBountyShouldBeRemovedFromActiveList() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
                bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);

                List<BountyData> active = bountyManager.getAllActiveBounties();
                assertThat(active).isEmpty();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ACTIVE BOUNTY RETRIEVAL TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Active Bounty Retrieval Tests")
    class ActiveBountyRetrievalTests {

        @Test
        @DisplayName("Should return active bounty for player")
        void shouldReturnActiveBountyForPlayer() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);

            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);

            assertThat(bounty).isNotNull();
            assertThat(bounty.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should return null for player without bounty")
        void shouldReturnNullForPlayerWithoutBounty() {
            BountyData bounty = bountyManager.getActiveBounty(UUID.randomUUID());

            assertThat(bounty).isNull();
        }

        @Test
        @DisplayName("Should return null for claimed bounty")
        void shouldReturnNullForClaimedBounty() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
                bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);

                BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);

                assertThat(bounty).isNull();
            }
        }

        @Test
        @DisplayName("Should return null for expired bounty")
        void shouldReturnNullForExpiredBounty() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);
            bounty.setExpiresAt(System.currentTimeMillis() - 1000); // Expire it

            BountyData retrieved = bountyManager.getActiveBounty(TEST_CRIMINAL);

            assertThat(retrieved).isNull(); // Should filter out expired
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ALL ACTIVE BOUNTIES TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("All Active Bounties Tests")
    class AllActiveBountiesTests {

        @Test
        @DisplayName("Should return all active bounties")
        void shouldReturnAllActiveBounties() {
            bountyManager.createAutoBounty(UUID.randomUUID(), 3);
            bountyManager.createAutoBounty(UUID.randomUUID(), 4);
            bountyManager.createAutoBounty(UUID.randomUUID(), 5);

            List<BountyData> active = bountyManager.getAllActiveBounties();

            assertThat(active).hasSize(3);
        }

        @Test
        @DisplayName("Should return bounties sorted by amount (highest first)")
        void shouldReturnBountiesSortedByAmount() {
            UUID low = UUID.randomUUID();
            UUID medium = UUID.randomUUID();
            UUID high = UUID.randomUUID();

            bountyManager.createAutoBounty(low, 3);    // 6000€
            bountyManager.createAutoBounty(high, 5);   // 10000€
            bountyManager.createAutoBounty(medium, 4); // 8000€

            List<BountyData> active = bountyManager.getAllActiveBounties();

            assertThat(active.get(0).getTargetUUID()).isEqualTo(high);
            assertThat(active.get(1).getTargetUUID()).isEqualTo(medium);
            assertThat(active.get(2).getTargetUUID()).isEqualTo(low);
        }

        @Test
        @DisplayName("Should exclude claimed bounties")
        void shouldExcludeClaimedBounties() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                UUID active = UUID.randomUUID();
                UUID claimed = UUID.randomUUID();

                bountyManager.createAutoBounty(active, 3);
                bountyManager.createAutoBounty(claimed, 4);
                bountyManager.claimBounty(TEST_HUNTER, claimed);

                List<BountyData> activeBounties = bountyManager.getAllActiveBounties();

                assertThat(activeBounties).hasSize(1);
                assertThat(activeBounties.get(0).getTargetUUID()).isEqualTo(active);
            }
        }

        @Test
        @DisplayName("Should return empty list when no bounties exist")
        void shouldReturnEmptyListWhenNoBountiesExist() {
            List<BountyData> active = bountyManager.getAllActiveBounties();

            assertThat(active).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TOP BOUNTIES TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Top Bounties Tests")
    class TopBountiesTests {

        @Test
        @DisplayName("Should return top N bounties")
        void shouldReturnTopNBounties() {
            for (int i = 0; i < 10; i++) {
                bountyManager.createAutoBounty(UUID.randomUUID(), 3 + (i % 3));
            }

            List<BountyData> top5 = bountyManager.getTopBounties(5);

            assertThat(top5).hasSize(5);
        }

        @Test
        @DisplayName("Top bounties should be sorted by amount (highest first)")
        void topBountiesShouldBeSortedByAmount() {
            bountyManager.createAutoBounty(UUID.randomUUID(), 3); // 6000€
            bountyManager.createAutoBounty(UUID.randomUUID(), 5); // 10000€
            bountyManager.createAutoBounty(UUID.randomUUID(), 4); // 8000€

            List<BountyData> top = bountyManager.getTopBounties(3);

            assertThat(top.get(0).getAmount()).isEqualTo(10000.0);
            assertThat(top.get(1).getAmount()).isEqualTo(8000.0);
            assertThat(top.get(2).getAmount()).isEqualTo(6000.0);
        }

        @Test
        @DisplayName("Should handle limit greater than available bounties")
        void shouldHandleLimitGreaterThanAvailable() {
            bountyManager.createAutoBounty(UUID.randomUUID(), 3);
            bountyManager.createAutoBounty(UUID.randomUUID(), 4);

            List<BountyData> top = bountyManager.getTopBounties(10);

            assertThat(top).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no bounties exist")
        void shouldReturnEmptyListWhenNoBounties() {
            List<BountyData> top = bountyManager.getTopBounties(5);

            assertThat(top).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10, 100})
        @DisplayName("Should respect limit parameter")
        void shouldRespectLimitParameter(int limit) {
            for (int i = 0; i < 20; i++) {
                bountyManager.createAutoBounty(UUID.randomUUID(), 3);
            }

            List<BountyData> top = bountyManager.getTopBounties(limit);

            assertThat(top.size()).isLessThanOrEqualTo(limit);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BOUNTY HISTORY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Bounty History Tests")
    class BountyHistoryTests {

        @Test
        @DisplayName("Should return empty history for player without bounties")
        void shouldReturnEmptyHistoryForPlayerWithoutBounties() {
            List<BountyData> history = bountyManager.getBountyHistory(UUID.randomUUID());

            assertThat(history).isEmpty();
        }

        @Test
        @DisplayName("Claimed bounty should appear in history")
        void claimedBountyShouldAppearInHistory() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
                bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);

                List<BountyData> history = bountyManager.getBountyHistory(TEST_CRIMINAL);

                assertThat(history).hasSize(1);
                assertThat(history.get(0).isClaimed()).isTrue();
            }
        }

        @Test
        @DisplayName("Multiple claimed bounties should all appear in history")
        void multipleClaimedBountiesShouldAppearInHistory() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                // Create and claim first bounty
                bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
                bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);

                // Create and claim second bounty
                bountyManager.createAutoBounty(TEST_CRIMINAL, 4);
                bountyManager.claimBounty(TEST_HUNTER, TEST_CRIMINAL);

                List<BountyData> history = bountyManager.getBountyHistory(TEST_CRIMINAL);

                assertThat(history).hasSize(2);
            }
        }

        @Test
        @DisplayName("Active bounties should not appear in history")
        void activeBountiesShouldNotAppearInHistory() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);

            List<BountyData> history = bountyManager.getBountyHistory(TEST_CRIMINAL);

            assertThat(history).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EXPIRED BOUNTY CLEANUP TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Expired Bounty Cleanup Tests")
    class ExpiredBountyCleanupTests {

        @Test
        @DisplayName("Expired bounties should be removed from active list")
        void expiredBountiesShouldBeRemoved() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);
            bounty.setExpiresAt(System.currentTimeMillis() - 1000);

            bountyManager.cleanupExpiredBounties();

            assertThat(bountyManager.getActiveBounty(TEST_CRIMINAL)).isNull();
        }

        @Test
        @DisplayName("Expired bounties should be moved to history")
        void expiredBountiesShouldBeMovedToHistory() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);
            bounty.setExpiresAt(System.currentTimeMillis() - 1000);

            bountyManager.cleanupExpiredBounties();

            List<BountyData> history = bountyManager.getBountyHistory(TEST_CRIMINAL);
            assertThat(history).hasSize(1);
        }

        @Test
        @DisplayName("Non-expired bounties should remain active")
        void nonExpiredBountiesShouldRemainActive() {
            bountyManager.createAutoBounty(TEST_CRIMINAL, 3);
            BountyData bounty = bountyManager.getActiveBounty(TEST_CRIMINAL);
            bounty.setExpiresAt(System.currentTimeMillis() + 100000);

            bountyManager.cleanupExpiredBounties();

            assertThat(bountyManager.getActiveBounty(TEST_CRIMINAL)).isNotNull();
        }

        @Test
        @DisplayName("Cleanup should handle mix of expired and active bounties")
        void cleanupShouldHandleMixedBounties() {
            UUID expired1 = UUID.randomUUID();
            UUID expired2 = UUID.randomUUID();
            UUID active = UUID.randomUUID();

            bountyManager.createAutoBounty(expired1, 3);
            bountyManager.createAutoBounty(expired2, 4);
            bountyManager.createAutoBounty(active, 5);

            bountyManager.getActiveBounty(expired1).setExpiresAt(System.currentTimeMillis() - 1000);
            bountyManager.getActiveBounty(expired2).setExpiresAt(System.currentTimeMillis() - 1000);

            bountyManager.cleanupExpiredBounties();

            assertThat(bountyManager.getAllActiveBounties()).hasSize(1);
            assertThat(bountyManager.getActiveBounty(active)).isNotNull();
        }

        @Test
        @DisplayName("Cleanup should do nothing when no bounties exist")
        void cleanupShouldHandleEmptyList() {
            assertThatCode(() -> bountyManager.cleanupExpiredBounties())
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Statistics should show correct active bounty count")
        void statisticsShouldShowCorrectCount() {
            bountyManager.createAutoBounty(UUID.randomUUID(), 3);
            bountyManager.createAutoBounty(UUID.randomUUID(), 4);
            bountyManager.createAutoBounty(UUID.randomUUID(), 5);

            String stats = bountyManager.getStatistics();

            assertThat(stats).contains("Active Bounties: 3");
        }

        @Test
        @DisplayName("Statistics should show correct total amount")
        void statisticsShouldShowCorrectTotal() {
            bountyManager.createAutoBounty(UUID.randomUUID(), 3); // 6000€
            bountyManager.createAutoBounty(UUID.randomUUID(), 4); // 8000€
            bountyManager.createAutoBounty(UUID.randomUUID(), 5); // 10000€

            String stats = bountyManager.getStatistics();

            assertThat(stats).contains("Total: 24000"); // 6000 + 8000 + 10000
        }

        @Test
        @DisplayName("Statistics should show zero when no bounties exist")
        void statisticsShouldShowZeroWhenNoBounties() {
            String stats = bountyManager.getStatistics();

            assertThat(stats)
                .contains("Active Bounties: 0")
                .contains("Total: 0.00");
        }

        @Test
        @DisplayName("Statistics should exclude claimed bounties")
        void statisticsShouldExcludeClaimedBounties() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                    .thenAnswer(invocation -> null);

                bountyManager.createAutoBounty(UUID.randomUUID(), 3);
                UUID claimed = UUID.randomUUID();
                bountyManager.createAutoBounty(claimed, 4);
                bountyManager.claimBounty(TEST_HUNTER, claimed);

                String stats = bountyManager.getStatistics();

                assertThat(stats).contains("Active Bounties: 1");
                assertThat(stats).contains("Total: 6000");
            }
        }

        @Test
        @DisplayName("Statistics should use correct format")
        void statisticsShouldUseCorrectFormat() {
            bountyManager.createAutoBounty(UUID.randomUUID(), 3);

            String stats = bountyManager.getStatistics();

            assertThat(stats).matches(".*Active Bounties: \\d+, Total: \\d+\\.\\d{2}€.*");
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

            BountyManager instance1 = BountyManager.getInstance(server);
            BountyManager instance2 = BountyManager.getInstance(server);

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("getInstance without parameter should return existing instance")
        void getInstanceWithoutParameterShouldReturnExisting() throws Exception {
            resetBountyManagerInstance();
            MinecraftServer server = createMockServer();

            BountyManager.getInstance(server); // Initialize
            BountyManager retrieved = BountyManager.getInstance();

            assertThat(retrieved).isNotNull();
        }

        @Test
        @DisplayName("getInstance without parameter should return null if not initialized")
        void getInstanceWithoutParameterShouldReturnNullIfNotInitialized() throws Exception {
            resetBountyManagerInstance();

            BountyManager retrieved = BountyManager.getInstance();

            assertThat(retrieved).isNull();
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

    private void resetBountyManagerInstance() throws Exception {
        Field instanceField = BountyManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private void clearActiveBounties() throws Exception {
        Field activeBountiesField = BountyManager.class.getDeclaredField("activeBounties");
        activeBountiesField.setAccessible(true);
        Map<UUID, BountyData> activeBounties = (Map<UUID, BountyData>) activeBountiesField.get(bountyManager);
        activeBounties.clear();
    }

    private void clearBountyHistory() throws Exception {
        Field historyField = BountyManager.class.getDeclaredField("bountyHistory");
        historyField.setAccessible(true);
        Map<UUID, List<BountyData>> history = (Map<UUID, List<BountyData>>) historyField.get(bountyManager);
        history.clear();
    }
}
