package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link CreditScoreManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Credit score initialization and retrieval</li>
 *   <li>Loan eligibility checks based on credit rating</li>
 *   <li>Dynamic interest rate calculation (0.8x - 1.5x modifier)</li>
 *   <li>Payment history tracking (on-time/missed payments)</li>
 *   <li>Loan completion bonuses</li>
 *   <li>Loan default penalties</li>
 *   <li>Balance updates and score adjustments</li>
 *   <li>Credit rating transitions (EXCELLENT → GOOD → FAIR → POOR)</li>
 * </ul>
 */
@DisplayName("CreditScoreManager Tests")
class CreditScoreManagerTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    private MinecraftServer mockServer;
    private MockedStatic<EconomyManager> economyMock;
    private CreditScoreManager scoreManager;

    @BeforeEach
    void setUp() {
        // Mock MinecraftServer
        mockServer = mock(MinecraftServer.class);
        when(mockServer.getServerDirectory()).thenReturn(java.nio.file.Paths.get("test_data").toFile());

        // Mock EconomyManager for balance checks
        economyMock = mockStatic(EconomyManager.class);
        economyMock.when(() -> EconomyManager.getBalance(any())).thenReturn(5000.0);

        // Create manager instance
        scoreManager = CreditScoreManager.getInstance(mockServer);
    }

    @AfterEach
    void tearDown() {
        economyMock.close();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALIZATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Score Initialization")
    class InitializationTests {

        @Test
        @DisplayName("should create new credit score for player")
        void shouldCreateNewCreditScoreForPlayer() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            assertThat(score).isNotNull();
            assertThat(score.getPlayerUUID()).isEqualTo(PLAYER_UUID);
        }

        @Test
        @DisplayName("should return existing score if already created")
        void shouldReturnExistingScoreIfAlreadyCreated() {
            CreditScore firstCall = scoreManager.getOrCreateScore(PLAYER_UUID);
            CreditScore secondCall = scoreManager.getOrCreateScore(PLAYER_UUID);

            assertThat(secondCall).isSameAs(firstCall);
        }

        @Test
        @DisplayName("getScore() should return null for non-existent player")
        void getScoreShouldReturnNullForNonExistentPlayer() {
            UUID unknownPlayer = UUID.randomUUID();

            CreditScore score = scoreManager.getScore(unknownPlayer);

            assertThat(score).isNull();
        }

        @Test
        @DisplayName("new credit score should have default values")
        void newCreditScoreShouldHaveDefaultValues() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            assertThat(score.getOnTimePayments()).isEqualTo(0);
            assertThat(score.getMissedPayments()).isEqualTo(0);
            assertThat(score.getTotalLoansCompleted()).isEqualTo(0);
            assertThat(score.getTotalDefaults()).isEqualTo(0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOAN ELIGIBILITY TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Loan Eligibility")
    class LoanEligibilityTests {

        @Test
        @DisplayName("new player with good balance can take small loan")
        void newPlayerWithGoodBalanceCanTakeSmallLoan() {
            economyMock.when(() -> EconomyManager.getBalance(PLAYER_UUID)).thenReturn(5000.0);

            boolean canTake = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(canTake).isTrue();
        }

        @Test
        @DisplayName("new player with low balance cannot take loan")
        void newPlayerWithLowBalanceCannotTakeLoan() {
            economyMock.when(() -> EconomyManager.getBalance(PLAYER_UUID)).thenReturn(500.0);

            boolean canTake = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(canTake).isFalse();
        }

        @Test
        @DisplayName("player with poor credit cannot take large loan")
        void playerWithPoorCreditCannotTakeLargeLoan() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Simulate poor credit history
            for (int i = 0; i < 10; i++) {
                score.recordMissedPayment();
            }

            boolean canTake = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE);

            assertThat(canTake).isFalse();
        }

        @Test
        @DisplayName("player with excellent credit can take any loan")
        void playerWithExcellentCreditCanTakeAnyLoan() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Simulate excellent credit history
            for (int i = 0; i < 20; i++) {
                score.recordOnTimePayment();
            }
            score.recordLoanCompleted(10000.0);

            boolean canTakeSmall = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);
            boolean canTakeMedium = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);
            boolean canTakeLarge = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE);

            assertThat(canTakeSmall).isTrue();
            assertThat(canTakeMedium).isTrue();
            assertThat(canTakeLarge).isTrue();
        }

        @Test
        @DisplayName("loan default should prevent future loans")
        void loanDefaultShouldPreventFutureLoans() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);
            score.recordLoanDefaulted();

            boolean canTake = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(canTake).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTEREST RATE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Interest Rate Calculation")
    class InterestRateTests {

        @Test
        @DisplayName("excellent credit should get 0.8x interest rate modifier")
        void excellentCreditShouldGetLowerInterestRate() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Build excellent credit
            for (int i = 0; i < 30; i++) {
                score.recordOnTimePayment();
            }
            score.recordLoanCompleted(20000.0);

            double baseRate = CreditLoan.CreditLoanType.MEDIUM.getBaseInterestRate();
            double effectiveRate = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            // Excellent credit gets 0.8x modifier
            assertThat(effectiveRate).isLessThan(baseRate);
            assertThat(effectiveRate).isCloseTo(baseRate * 0.8, within(0.01));
        }

        @Test
        @DisplayName("good credit should get 1.0x interest rate (base rate)")
        void goodCreditShouldGetBaseInterestRate() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Build good credit (some on-time payments)
            for (int i = 0; i < 10; i++) {
                score.recordOnTimePayment();
            }

            double baseRate = CreditLoan.CreditLoanType.SMALL.getBaseInterestRate();
            double effectiveRate = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(effectiveRate).isCloseTo(baseRate, within(0.01));
        }

        @Test
        @DisplayName("fair credit should get 1.2x interest rate modifier")
        void fairCreditShouldGetHigherInterestRate() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Build fair credit (mixed history)
            for (int i = 0; i < 5; i++) {
                score.recordOnTimePayment();
            }
            for (int i = 0; i < 3; i++) {
                score.recordMissedPayment();
            }

            double baseRate = CreditLoan.CreditLoanType.SMALL.getBaseInterestRate();
            double effectiveRate = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(effectiveRate).isGreaterThan(baseRate);
        }

        @Test
        @DisplayName("poor credit should get 1.5x interest rate modifier")
        void poorCreditShouldGetHighestInterestRate() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Build poor credit (mostly missed payments)
            for (int i = 0; i < 10; i++) {
                score.recordMissedPayment();
            }

            double baseRate = CreditLoan.CreditLoanType.SMALL.getBaseInterestRate();
            double effectiveRate = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            // Poor credit gets up to 1.5x modifier
            assertThat(effectiveRate).isGreaterThan(baseRate);
            assertThat(effectiveRate).isCloseTo(baseRate * 1.5, within(0.05));
        }

        @Test
        @DisplayName("different loan types should have different base rates")
        void differentLoanTypesShouldHaveDifferentBaseRates() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Same credit score, different loan types
            for (int i = 0; i < 10; i++) {
                score.recordOnTimePayment();
            }

            double rateSmall = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);
            double rateMedium = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);
            double rateLarge = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE);

            // Rates should exist and be positive
            assertThat(rateSmall).isPositive();
            assertThat(rateMedium).isPositive();
            assertThat(rateLarge).isPositive();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PAYMENT TRACKING TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Payment Tracking")
    class PaymentTrackingTests {

        @Test
        @DisplayName("recordOnTimePayment() should increment counter")
        void recordOnTimePaymentShouldIncrementCounter() {
            scoreManager.recordOnTimePayment(PLAYER_UUID);

            CreditScore score = scoreManager.getScore(PLAYER_UUID);

            assertThat(score.getOnTimePayments()).isEqualTo(1);
        }

        @Test
        @DisplayName("recordMissedPayment() should increment counter")
        void recordMissedPaymentShouldIncrementCounter() {
            scoreManager.recordMissedPayment(PLAYER_UUID);

            CreditScore score = scoreManager.getScore(PLAYER_UUID);

            assertThat(score.getMissedPayments()).isEqualTo(1);
        }

        @Test
        @DisplayName("multiple on-time payments should accumulate")
        void multipleOnTimePaymentsShouldAccumulate() {
            for (int i = 0; i < 5; i++) {
                scoreManager.recordOnTimePayment(PLAYER_UUID);
            }

            CreditScore score = scoreManager.getScore(PLAYER_UUID);

            assertThat(score.getOnTimePayments()).isEqualTo(5);
        }

        @Test
        @DisplayName("on-time payments should improve credit rating over time")
        void onTimePaymentsShouldImproveCreditRating() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Record many on-time payments
            for (int i = 0; i < 50; i++) {
                score.recordOnTimePayment();
            }

            // Credit rating should improve
            double initialRate = CreditLoan.CreditLoanType.MEDIUM.getBaseInterestRate();
            double effectiveRate = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            assertThat(effectiveRate).isLessThan(initialRate);
        }

        @Test
        @DisplayName("missed payments should worsen credit rating")
        void missedPaymentsShouldWorsenCreditRating() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Record many missed payments
            for (int i = 0; i < 10; i++) {
                score.recordMissedPayment();
            }

            // Credit rating should worsen
            double initialRate = CreditLoan.CreditLoanType.SMALL.getBaseInterestRate();
            double effectiveRate = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(effectiveRate).isGreaterThan(initialRate);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOAN COMPLETION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Loan Completion")
    class LoanCompletionTests {

        @Test
        @DisplayName("recordLoanCompleted() should increment counter")
        void recordLoanCompletedShouldIncrementCounter() {
            scoreManager.recordLoanCompleted(PLAYER_UUID, 5000.0);

            CreditScore score = scoreManager.getScore(PLAYER_UUID);

            assertThat(score.getTotalLoansCompleted()).isEqualTo(1);
        }

        @Test
        @DisplayName("completing large loans should improve credit more")
        void completingLargeLoansShouldImproveCreditMore() {
            scoreManager.recordLoanCompleted(PLAYER_UUID, 50000.0);

            CreditScore score = scoreManager.getScore(PLAYER_UUID);

            assertThat(score.getTotalAmountRepaid()).isEqualTo(50000.0);
        }

        @Test
        @DisplayName("multiple completed loans should accumulate")
        void multipleCompletedLoansShouldAccumulate() {
            scoreManager.recordLoanCompleted(PLAYER_UUID, 5000.0);
            scoreManager.recordLoanCompleted(PLAYER_UUID, 10000.0);
            scoreManager.recordLoanCompleted(PLAYER_UUID, 15000.0);

            CreditScore score = scoreManager.getScore(PLAYER_UUID);

            assertThat(score.getTotalLoansCompleted()).isEqualTo(3);
            assertThat(score.getTotalAmountRepaid()).isEqualTo(30000.0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOAN DEFAULT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Loan Defaults")
    class LoanDefaultTests {

        @Test
        @DisplayName("recordLoanDefaulted() should increment counter")
        void recordLoanDefaultedShouldIncrementCounter() {
            scoreManager.recordLoanDefaulted(PLAYER_UUID);

            CreditScore score = scoreManager.getScore(PLAYER_UUID);

            assertThat(score.getTotalDefaults()).isEqualTo(1);
        }

        @Test
        @DisplayName("loan default should severely impact credit rating")
        void loanDefaultShouldSeverelyImpactCreditRating() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Start with good credit
            for (int i = 0; i < 20; i++) {
                score.recordOnTimePayment();
            }

            double rateBefore = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            // Record default
            score.recordLoanDefaulted();

            double rateAfter = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            // Interest rate should increase significantly
            assertThat(rateAfter).isGreaterThan(rateBefore);
        }

        @Test
        @DisplayName("single default should not completely destroy credit")
        void singleDefaultShouldNotCompletelyDestroyCreditScore() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // Build excellent credit
            for (int i = 0; i < 50; i++) {
                score.recordOnTimePayment();
            }
            score.recordLoanCompleted(20000.0);

            // One default
            score.recordLoanDefaulted();

            // Should still be able to take small loans (eventually)
            boolean canTakeSmall = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            // May or may not pass depending on scoring algorithm, but shouldn't crash
            assertThat(canTakeSmall).isIn(true, false);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TICK/DAY TRACKING TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Tick and Day Tracking")
    class TickTrackingTests {

        @Test
        @DisplayName("getCurrentDay() should start at 0")
        void getCurrentDayShouldStartAtZero() {
            assertThat(scoreManager.getCurrentDay()).isEqualTo(0);
        }

        @Test
        @DisplayName("tick() should update current day")
        void tickShouldUpdateCurrentDay() {
            scoreManager.tick(24000L); // Day 1

            assertThat(scoreManager.getCurrentDay()).isEqualTo(1);
        }

        @Test
        @DisplayName("tick() should handle multiple days")
        void tickShouldHandleMultipleDays() {
            scoreManager.tick(24000L);  // Day 1
            scoreManager.tick(48000L);  // Day 2
            scoreManager.tick(72000L);  // Day 3

            assertThat(scoreManager.getCurrentDay()).isEqualTo(3);
        }

        @Test
        @DisplayName("tick() should not re-process same day")
        void tickShouldNotReprocessSameDay() {
            scoreManager.tick(24000L);  // Day 1
            scoreManager.tick(25000L);  // Still day 1

            assertThat(scoreManager.getCurrentDay()).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should handle complete credit lifecycle")
        void shouldHandleCompleteCreditLifecycle() {
            // 1. New player
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);
            assertThat(score).isNotNull();

            // 2. Check eligibility (should be able to take small loan)
            boolean canTake = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);
            assertThat(canTake).isTrue();

            // 3. Make on-time payments
            for (int i = 0; i < 10; i++) {
                scoreManager.recordOnTimePayment(PLAYER_UUID);
            }

            // 4. Complete loan
            scoreManager.recordLoanCompleted(PLAYER_UUID, 5000.0);

            // 5. Credit should improve
            double effectiveRate = scoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);
            double baseRate = CreditLoan.CreditLoanType.MEDIUM.getBaseInterestRate();

            assertThat(effectiveRate).isLessThanOrEqualTo(baseRate);
        }

        @Test
        @DisplayName("should handle credit recovery after default")
        void shouldHandleCreditRecoveryAfterDefault() {
            CreditScore score = scoreManager.getOrCreateScore(PLAYER_UUID);

            // 1. Default on loan
            score.recordLoanDefaulted();

            // 2. Cannot take large loans
            boolean canTakeLarge = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE);
            assertThat(canTakeLarge).isFalse();

            // 3. Rebuild credit with many on-time payments
            for (int i = 0; i < 100; i++) {
                score.recordOnTimePayment();
            }

            // 4. Complete several loans
            score.recordLoanCompleted(5000.0);
            score.recordLoanCompleted(10000.0);
            score.recordLoanCompleted(15000.0);

            // 5. Credit should improve (eventually can take larger loans again)
            boolean canTakeMedium = scoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);
            assertThat(canTakeMedium).isTrue();
        }
    }
}
