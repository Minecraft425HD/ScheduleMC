package de.rolandsw.schedulemc.economy;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link CreditLoanManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Loan application with credit score validation</li>
 *   <li>Dynamic interest rate calculation based on creditworthiness</li>
 *   <li>Daily payment processing and automatic deductions</li>
 *   <li>Loan completion and credit score updates</li>
 *   <li>Early repayment functionality</li>
 *   <li>Missed payment penalties</li>
 *   <li>Persistence integration</li>
 * </ul>
 */
@DisplayName("CreditLoanManager Tests")
class CreditLoanManagerTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final double STARTING_BALANCE = 5000.0;

    private MinecraftServer mockServer;
    private CreditScoreManager mockScoreManager;
    private MockedStatic<EconomyManager> economyMock;
    private MockedStatic<CreditScoreManager> scoreMock;
    private CreditLoanManager loanManager;

    @BeforeEach
    void setUp() {
        // Mock MinecraftServer
        mockServer = mock(MinecraftServer.class);
        when(mockServer.getServerDirectory()).thenReturn(java.nio.file.Paths.get("test_data").toFile());

        // Mock PlayerList for notifications
        PlayerList mockPlayerList = mock(PlayerList.class);
        when(mockServer.getPlayerList()).thenReturn(mockPlayerList);

        // Mock CreditScoreManager
        mockScoreManager = mock(CreditScoreManager.class);
        scoreMock = mockStatic(CreditScoreManager.class);
        scoreMock.when(() -> CreditScoreManager.getInstance(any())).thenReturn(mockScoreManager);

        // Default credit score behavior: good credit (can take loan)
        when(mockScoreManager.canTakeLoan(any(), any())).thenReturn(true);
        when(mockScoreManager.getEffectiveInterestRate(any(), any())).thenReturn(0.05); // 5% default

        // Mock EconomyManager
        economyMock = mockStatic(EconomyManager.class);
        economyMock.when(() -> EconomyManager.getBalance(any())).thenReturn(STARTING_BALANCE);
        economyMock.when(() -> EconomyManager.deposit(any(), anyDouble(), any(), anyString()))
                   .then(invocation -> null);
        economyMock.when(() -> EconomyManager.withdraw(any(), anyDouble(), any(), anyString()))
                   .thenReturn(true);

        // Create manager instance
        loanManager = CreditLoanManager.getInstance(mockServer);
    }

    @AfterEach
    void tearDown() {
        economyMock.close();
        scoreMock.close();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOAN APPLICATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Loan Application")
    class LoanApplicationTests {

        @Test
        @DisplayName("should successfully apply for small loan with good credit")
        void shouldApplyForSmallLoanWithGoodCredit() {
            boolean result = loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(result).isTrue();
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isTrue();

            // Verify loan amount was deposited
            economyMock.verify(() -> EconomyManager.deposit(
                eq(PLAYER_UUID),
                eq(CreditLoan.CreditLoanType.SMALL.getBaseAmount()),
                eq(TransactionType.LOAN_DISBURSEMENT),
                anyString()
            ), times(1));
        }

        @Test
        @DisplayName("should reject loan if balance below minimum (1000.0)")
        void shouldRejectLoanIfBalanceBelowMinimum() {
            economyMock.when(() -> EconomyManager.getBalance(PLAYER_UUID)).thenReturn(500.0);

            boolean result = loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(result).isFalse();
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isFalse();
        }

        @Test
        @DisplayName("should reject loan if player already has active loan")
        void shouldRejectLoanIfAlreadyHasActiveLoan() {
            // First loan succeeds
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            // Second loan should fail
            boolean result = loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should reject loan if credit score insufficient")
        void shouldRejectLoanIfCreditScoreInsufficient() {
            when(mockScoreManager.canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE))
                .thenReturn(false);

            boolean result = loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE);

            assertThat(result).isFalse();
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isFalse();
        }

        @Test
        @DisplayName("should apply dynamic interest rate based on credit score")
        void shouldApplyDynamicInterestRateBasedOnCreditScore() {
            // Mock: Poor credit → higher interest rate
            when(mockScoreManager.getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM))
                .thenReturn(0.15); // 15% interest

            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            CreditLoan loan = loanManager.getLoan(PLAYER_UUID);
            assertThat(loan).isNotNull();
            assertThat(loan.getInterestRate()).isEqualTo(0.15);
        }

        @Test
        @DisplayName("should create loan with correct initial values")
        void shouldCreateLoanWithCorrectInitialValues() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            CreditLoan loan = loanManager.getLoan(PLAYER_UUID);

            assertThat(loan).isNotNull();
            assertThat(loan.getPlayerUUID()).isEqualTo(PLAYER_UUID);
            assertThat(loan.getType()).isEqualTo(CreditLoan.CreditLoanType.SMALL);
            assertThat(loan.getPrincipal()).isEqualTo(CreditLoan.CreditLoanType.SMALL.getBaseAmount());
            assertThat(loan.getRemaining()).isGreaterThan(loan.getPrincipal()); // includes interest
            assertThat(loan.isRepaid()).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DAILY PAYMENT PROCESSING TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Daily Payment Processing")
    class DailyPaymentTests {

        @Test
        @DisplayName("tick() should not process payments on same day")
        void tickShouldNotProcessPaymentsOnSameDay() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            long dayTime = 24000L; // Day 1
            loanManager.tick(dayTime);
            loanManager.tick(dayTime + 1000L); // Still day 1

            // Only 1 deposit (initial loan), no daily payments yet
            economyMock.verify(() -> EconomyManager.withdraw(any(), anyDouble(), any(), anyString()), never());
        }

        @Test
        @DisplayName("should process daily payment when day changes")
        void shouldProcessDailyPaymentWhenDayChanges() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            // Advance to next day
            loanManager.tick(24000L);  // Day 1
            loanManager.tick(48000L);  // Day 2 → triggers daily payment

            // Verify daily payment was withdrawn
            economyMock.verify(() -> EconomyManager.withdraw(
                eq(PLAYER_UUID),
                anyDouble(),
                eq(TransactionType.LOAN_REPAYMENT),
                anyString()
            ), atLeastOnce());
        }

        @Test
        @DisplayName("should record on-time payment in credit score")
        void shouldRecordOnTimePaymentInCreditScore() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            loanManager.tick(24000L);  // Day 1
            loanManager.tick(48000L);  // Day 2 → payment

            verify(mockScoreManager, atLeastOnce()).recordOnTimePayment(PLAYER_UUID);
        }

        @Test
        @DisplayName("should record missed payment when withdrawal fails")
        void shouldRecordMissedPaymentWhenWithdrawalFails() {
            // Mock: Withdrawal fails (insufficient funds)
            economyMock.when(() -> EconomyManager.withdraw(any(), anyDouble(), any(), anyString()))
                       .thenReturn(false);

            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            loanManager.tick(24000L);  // Day 1
            loanManager.tick(48000L);  // Day 2 → payment attempt fails

            verify(mockScoreManager, atLeastOnce()).recordMissedPayment(PLAYER_UUID);
        }

        @Test
        @DisplayName("should complete loan when fully repaid")
        void shouldCompleteLoanWhenFullyRepaid() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            CreditLoan loan = loanManager.getLoan(PLAYER_UUID);
            int requiredDays = loan.getLoanPeriod();

            // Simulate daily payments until completion
            for (int day = 1; day <= requiredDays + 1; day++) {
                loanManager.tick(24000L * day);
            }

            // Loan should be removed
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isFalse();

            // Credit score should be updated
            verify(mockScoreManager).recordLoanCompleted(eq(PLAYER_UUID), anyDouble());
        }

        @Test
        @DisplayName("should notify player when loan is completed")
        void shouldNotifyPlayerWhenLoanIsCompleted() {
            ServerPlayer mockPlayer = mock(ServerPlayer.class);
            when(mockServer.getPlayerList().getPlayer(PLAYER_UUID)).thenReturn(mockPlayer);

            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            CreditLoan loan = loanManager.getLoan(PLAYER_UUID);
            int requiredDays = loan.getLoanPeriod();

            // Complete loan
            for (int day = 1; day <= requiredDays + 1; day++) {
                loanManager.tick(24000L * day);
            }

            // Verify notification was sent
            verify(mockPlayer, atLeastOnce()).sendSystemMessage(any(Component.class));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EARLY REPAYMENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Early Repayment")
    class EarlyRepaymentTests {

        @Test
        @DisplayName("should allow early loan repayment")
        void shouldAllowEarlyLoanRepayment() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            boolean result = loanManager.repayLoan(PLAYER_UUID);

            assertThat(result).isTrue();
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isFalse();
        }

        @Test
        @DisplayName("should withdraw full remaining amount on early repayment")
        void shouldWithdrawFullRemainingAmountOnEarlyRepayment() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            CreditLoan loan = loanManager.getLoan(PLAYER_UUID);
            double remaining = loan.getRemaining();

            loanManager.repayLoan(PLAYER_UUID);

            economyMock.verify(() -> EconomyManager.withdraw(
                eq(PLAYER_UUID),
                eq(remaining),
                eq(TransactionType.LOAN_REPAYMENT),
                anyString()
            ), times(1));
        }

        @Test
        @DisplayName("should fail early repayment if insufficient funds")
        void shouldFailEarlyRepaymentIfInsufficientFunds() {
            economyMock.when(() -> EconomyManager.withdraw(any(), anyDouble(), any(), anyString()))
                       .thenReturn(false);

            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            boolean result = loanManager.repayLoan(PLAYER_UUID);

            assertThat(result).isFalse();
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isTrue();
        }

        @Test
        @DisplayName("should record loan completion on early repayment")
        void shouldRecordLoanCompletionOnEarlyRepayment() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            loanManager.repayLoan(PLAYER_UUID);

            verify(mockScoreManager).recordLoanCompleted(eq(PLAYER_UUID), anyDouble());
        }

        @Test
        @DisplayName("should fail repayment if no active loan")
        void shouldFailRepaymentIfNoActiveLoan() {
            boolean result = loanManager.repayLoan(PLAYER_UUID);

            assertThat(result).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // QUERY METHODS TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodsTests {

        @Test
        @DisplayName("hasActiveLoan() should return false when no loan")
        void hasActiveLoanShouldReturnFalseWhenNoLoan() {
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isFalse();
        }

        @Test
        @DisplayName("hasActiveLoan() should return true after loan application")
        void hasActiveLoanShouldReturnTrueAfterLoanApplication() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);

            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isTrue();
        }

        @Test
        @DisplayName("getLoan() should return null when no loan")
        void getLoanShouldReturnNullWhenNoLoan() {
            assertThat(loanManager.getLoan(PLAYER_UUID)).isNull();
        }

        @Test
        @DisplayName("getLoan() should return loan object after application")
        void getLoanShouldReturnLoanObjectAfterApplication() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            CreditLoan loan = loanManager.getLoan(PLAYER_UUID);

            assertThat(loan).isNotNull();
            assertThat(loan.getPlayerUUID()).isEqualTo(PLAYER_UUID);
            assertThat(loan.getType()).isEqualTo(CreditLoan.CreditLoanType.MEDIUM);
        }

        @Test
        @DisplayName("getCurrentDay() should track current game day")
        void getCurrentDayShouldTrackCurrentGameDay() {
            assertThat(loanManager.getCurrentDay()).isEqualTo(0);

            loanManager.tick(24000L); // Day 1

            assertThat(loanManager.getCurrentDay()).isEqualTo(1);

            loanManager.tick(72000L); // Day 3

            assertThat(loanManager.getCurrentDay()).isEqualTo(3);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should handle complete loan lifecycle")
        void shouldHandleCompleteLoanLifecycle() {
            // 1. Apply for loan
            boolean applied = loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);
            assertThat(applied).isTrue();

            // 2. Make some daily payments
            loanManager.tick(24000L);  // Day 1
            loanManager.tick(48000L);  // Day 2
            loanManager.tick(72000L);  // Day 3

            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isTrue();

            // 3. Early repayment
            boolean repaid = loanManager.repayLoan(PLAYER_UUID);
            assertThat(repaid).isTrue();

            // 4. Loan should be gone
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isFalse();
        }

        @Test
        @DisplayName("should handle multiple consecutive loans")
        void shouldHandleMultipleConsecutiveLoans() {
            // First loan
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.SMALL);
            loanManager.repayLoan(PLAYER_UUID);

            // Second loan (should succeed now)
            boolean result = loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.MEDIUM);

            assertThat(result).isTrue();
            assertThat(loanManager.hasActiveLoan(PLAYER_UUID)).isTrue();
        }

        @Test
        @DisplayName("should interact correctly with CreditScoreManager")
        void shouldInteractCorrectlyWithCreditScoreManager() {
            loanManager.applyForLoan(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE);

            // Verify credit score checks
            verify(mockScoreManager).canTakeLoan(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE);
            verify(mockScoreManager).getEffectiveInterestRate(PLAYER_UUID, CreditLoan.CreditLoanType.LARGE);

            // Make payment
            loanManager.tick(24000L);
            loanManager.tick(48000L);

            verify(mockScoreManager, atLeastOnce()).recordOnTimePayment(PLAYER_UUID);
        }
    }
}
