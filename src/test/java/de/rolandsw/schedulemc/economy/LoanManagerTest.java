package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

/**
 * Unit Tests für LoanManager
 */
public class LoanManagerTest {

    private LoanManager loanManager;
    private MinecraftServer mockServer;
    private UUID testPlayer;

    @BeforeEach
    public void setUp() {
        // Mock MinecraftServer
        mockServer = Mockito.mock(MinecraftServer.class);

        // Create test player
        testPlayer = UUID.randomUUID();

        // Setup EconomyManager with sufficient balance
        if (!EconomyManager.hasAccount(testPlayer)) {
            EconomyManager.createAccount(testPlayer);
        }
        EconomyManager.setBalance(testPlayer, 10000.0); // Start with 10k

        // Reset LoanManager instance (using reflection if needed)
        try {
            java.lang.reflect.Field instanceField = LoanManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Ignore if field doesn't exist or can't be reset
        }

        loanManager = LoanManager.getInstance(mockServer);
    }

    @Test
    public void testApplyForLoan_Small() {
        boolean success = loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);

        assertThat(success).isTrue();
        assertThat(loanManager.hasActiveLoan(testPlayer)).isTrue();

        // Verify loan amount was disbursed (10000 + 5000 = 15000)
        assertThat(EconomyManager.getBalance(testPlayer)).isEqualTo(15000.0);
    }

    @Test
    public void testApplyForLoan_Medium() {
        boolean success = loanManager.applyForLoan(testPlayer, Loan.LoanType.MEDIUM);

        assertThat(success).isTrue();
        assertThat(loanManager.hasActiveLoan(testPlayer)).isTrue();

        // Verify loan amount was disbursed (10000 + 25000 = 35000)
        assertThat(EconomyManager.getBalance(testPlayer)).isEqualTo(35000.0);
    }

    @Test
    public void testApplyForLoan_Large() {
        boolean success = loanManager.applyForLoan(testPlayer, Loan.LoanType.LARGE);

        assertThat(success).isTrue();
        assertThat(loanManager.hasActiveLoan(testPlayer)).isTrue();

        // Verify loan amount was disbursed (10000 + 100000 = 110000)
        assertThat(EconomyManager.getBalance(testPlayer)).isEqualTo(110000.0);
    }

    @Test
    public void testApplyForLoan_InsufficientBalance() {
        // Set balance below minimum (1000)
        EconomyManager.setBalance(testPlayer, 500.0);

        boolean success = loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);

        assertThat(success).isFalse();
        assertThat(loanManager.hasActiveLoan(testPlayer)).isFalse();

        // Balance should be unchanged
        assertThat(EconomyManager.getBalance(testPlayer)).isEqualTo(500.0);
    }

    @Test
    public void testApplyForLoan_AlreadyHasLoan() {
        // Apply for first loan
        loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);
        double balanceAfterFirst = EconomyManager.getBalance(testPlayer);

        // Try to apply for second loan
        boolean success = loanManager.applyForLoan(testPlayer, Loan.LoanType.MEDIUM);

        assertThat(success).isFalse();

        // Balance should be unchanged (no second disbursement)
        assertThat(EconomyManager.getBalance(testPlayer)).isEqualTo(balanceAfterFirst);
    }

    @Test
    public void testLoanInterestCalculation_Small() {
        loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);

        Loan loan = loanManager.getLoan(testPlayer);

        assertThat(loan).isNotNull();
        assertThat(loan.getPrincipal()).isEqualTo(5000.0);
        assertThat(loan.getInterestRate()).isEqualTo(0.10);

        // Total with interest: 5000 * 1.10 = 5500
        double expectedTotal = 5000.0 * 1.10;
        assertThat(loan.getRemaining()).isCloseTo(expectedTotal, within(0.01));

        // Daily payment: 5500 / 14 days = ~392.86
        double expectedDaily = expectedTotal / 14.0;
        assertThat(loan.getDailyPayment()).isCloseTo(expectedDaily, within(0.01));
    }

    @Test
    public void testLoanInterestCalculation_Medium() {
        loanManager.applyForLoan(testPlayer, Loan.LoanType.MEDIUM);

        Loan loan = loanManager.getLoan(testPlayer);

        assertThat(loan).isNotNull();
        assertThat(loan.getPrincipal()).isEqualTo(25000.0);
        assertThat(loan.getInterestRate()).isEqualTo(0.15);

        // Total with interest: 25000 * 1.15 = 28750
        double expectedTotal = 25000.0 * 1.15;
        assertThat(loan.getRemaining()).isCloseTo(expectedTotal, within(0.01));

        // Daily payment: 28750 / 28 days = ~1026.79
        double expectedDaily = expectedTotal / 28.0;
        assertThat(loan.getDailyPayment()).isCloseTo(expectedDaily, within(0.01));
    }

    @Test
    public void testLoanInterestCalculation_Large() {
        loanManager.applyForLoan(testPlayer, Loan.LoanType.LARGE);

        Loan loan = loanManager.getLoan(testPlayer);

        assertThat(loan).isNotNull();
        assertThat(loan.getPrincipal()).isEqualTo(100000.0);
        assertThat(loan.getInterestRate()).isEqualTo(0.20);

        // Total with interest: 100000 * 1.20 = 120000
        double expectedTotal = 100000.0 * 1.20;
        assertThat(loan.getRemaining()).isCloseTo(expectedTotal, within(0.01));

        // Daily payment: 120000 / 56 days = ~2142.86
        double expectedDaily = expectedTotal / 56.0;
        assertThat(loan.getDailyPayment()).isCloseTo(expectedDaily, within(0.01));
    }

    @Test
    public void testDailyPayment_ProcessesCorrectly() {
        loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);

        Loan loan = loanManager.getLoan(testPlayer);
        double initialRemaining = loan.getRemaining();
        double dailyPayment = loan.getDailyPayment();
        double initialBalance = EconomyManager.getBalance(testPlayer);

        // Simulate one day passing (tick)
        loanManager.tick(24000); // Day 1

        // Verify payment was deducted from balance
        double expectedBalance = initialBalance - dailyPayment;
        assertThat(EconomyManager.getBalance(testPlayer)).isCloseTo(expectedBalance, within(0.01));

        // Verify remaining loan decreased
        double expectedRemaining = initialRemaining - dailyPayment;
        assertThat(loan.getRemaining()).isCloseTo(expectedRemaining, within(0.01));
    }

    @Test
    public void testDailyPayment_InsufficientFunds_LoanStaysActive() {
        loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);

        // Drain balance to below daily payment
        EconomyManager.setBalance(testPlayer, 100.0);

        Loan loan = loanManager.getLoan(testPlayer);
        double remainingBefore = loan.getRemaining();

        // Simulate one day passing
        loanManager.tick(24000);

        // Loan should still be active (not removed)
        assertThat(loanManager.hasActiveLoan(testPlayer)).isTrue();

        // Remaining should be unchanged (payment failed)
        assertThat(loan.getRemaining()).isEqualTo(remainingBefore);
    }

    @Test
    public void testFullLoanRepayment_AutomaticallyRemovesLoan() {
        loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);

        Loan loan = loanManager.getLoan(testPlayer);
        int duration = loan.getDurationDays();

        // Ensure sufficient balance
        EconomyManager.setBalance(testPlayer, 100000.0);

        // Simulate all days of the loan
        for (int day = 1; day <= duration; day++) {
            loanManager.tick(day * 24000L);
        }

        // Loan should be fully repaid and removed
        assertThat(loanManager.hasActiveLoan(testPlayer)).isFalse();
        assertThat(loanManager.getLoan(testPlayer)).isNull();
    }

    @Test
    public void testEarlyRepayment_Success() {
        loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);

        Loan loan = loanManager.getLoan(testPlayer);
        double remaining = loan.getRemaining();
        double balanceBefore = EconomyManager.getBalance(testPlayer);

        // Repay loan early
        boolean success = loanManager.repayLoan(testPlayer);

        assertThat(success).isTrue();

        // Verify full amount was withdrawn
        double expectedBalance = balanceBefore - remaining;
        assertThat(EconomyManager.getBalance(testPlayer)).isCloseTo(expectedBalance, within(0.01));

        // Verify loan is removed
        assertThat(loanManager.hasActiveLoan(testPlayer)).isFalse();
        assertThat(loanManager.getLoan(testPlayer)).isNull();
    }

    @Test
    public void testEarlyRepayment_InsufficientFunds() {
        loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);

        // Drain balance
        EconomyManager.setBalance(testPlayer, 100.0);

        double balanceBefore = EconomyManager.getBalance(testPlayer);

        // Try to repay loan
        boolean success = loanManager.repayLoan(testPlayer);

        assertThat(success).isFalse();

        // Loan should still be active
        assertThat(loanManager.hasActiveLoan(testPlayer)).isTrue();

        // Balance should be unchanged
        assertThat(EconomyManager.getBalance(testPlayer)).isEqualTo(balanceBefore);
    }

    @Test
    public void testEarlyRepayment_NoActiveLoan() {
        boolean success = loanManager.repayLoan(testPlayer);

        assertThat(success).isFalse();
    }

    @Test
    public void testGetLoan_NoLoan_ReturnsNull() {
        Loan loan = loanManager.getLoan(testPlayer);

        assertThat(loan).isNull();
    }

    @Test
    public void testSequentialLoans_CanApplyAfterRepayment() {
        // Apply for first loan
        loanManager.applyForLoan(testPlayer, Loan.LoanType.SMALL);
        assertThat(loanManager.hasActiveLoan(testPlayer)).isTrue();

        // Repay it
        EconomyManager.setBalance(testPlayer, 100000.0);
        loanManager.repayLoan(testPlayer);
        assertThat(loanManager.hasActiveLoan(testPlayer)).isFalse();

        // Apply for second loan
        boolean success = loanManager.applyForLoan(testPlayer, Loan.LoanType.MEDIUM);

        assertThat(success).isTrue();
        assertThat(loanManager.hasActiveLoan(testPlayer)).isTrue();

        Loan newLoan = loanManager.getLoan(testPlayer);
        assertThat(newLoan.getType()).isEqualTo(Loan.LoanType.MEDIUM);
    }

    @Test
    public void testLoanDuration_AllTypes() {
        assertThat(Loan.LoanType.SMALL.getDurationDays()).isEqualTo(14);
        assertThat(Loan.LoanType.MEDIUM.getDurationDays()).isEqualTo(28);
        assertThat(Loan.LoanType.LARGE.getDurationDays()).isEqualTo(56);
    }

    @Test
    public void testLoanAmounts_AllTypes() {
        assertThat(Loan.LoanType.SMALL.getAmount()).isEqualTo(5000.0);
        assertThat(Loan.LoanType.MEDIUM.getAmount()).isEqualTo(25000.0);
        assertThat(Loan.LoanType.LARGE.getAmount()).isEqualTo(100000.0);
    }

    @Test
    public void testLoanInterestRates_AllTypes() {
        assertThat(Loan.LoanType.SMALL.getInterestRate()).isEqualTo(0.10);
        assertThat(Loan.LoanType.MEDIUM.getInterestRate()).isEqualTo(0.15);
        assertThat(Loan.LoanType.LARGE.getInterestRate()).isEqualTo(0.20);
    }
}
