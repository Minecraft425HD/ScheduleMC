package de.rolandsw.schedulemc.economy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for FeeManager
 *
 * Tests cover:
 * - ATM fee calculations
 * - Transfer fee calculations (1% with min 10€)
 * - Edge cases: zero amounts, negative amounts, very large amounts
 * - Boundary value testing
 * - Fee info formatting
 */
@DisplayName("FeeManager Tests")
class FeeManagerTest {

    // ═══════════════════════════════════════════════════════════
    // ATM FEE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ATM Fee Tests")
    class ATMFeeTests {

        @Test
        @DisplayName("ATM fee should be constant at 5.0€")
        void atmFeeShouldBeConstant() {
            assertThat(FeeManager.getATMFee()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("ATM fee should be positive")
        void atmFeeShouldBePositive() {
            assertThat(FeeManager.getATMFee()).isPositive();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TRANSFER FEE CALCULATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Transfer Fee Calculation Tests")
    class TransferFeeCalculationTests {

        @Test
        @DisplayName("Transfer fee should be 1% for amounts above minimum")
        void transferFeeShouldBeOnePercent() {
            // For 10,000€, fee should be 100€ (1%)
            assertThat(FeeManager.getTransferFee(10_000.0))
                .isEqualTo(100.0);
        }

        @Test
        @DisplayName("Transfer fee should have minimum of 10€")
        void transferFeeShouldHaveMinimum() {
            // For 100€, 1% = 1€, but minimum is 10€
            assertThat(FeeManager.getTransferFee(100.0))
                .isEqualTo(10.0);

            // For 500€, 1% = 5€, but minimum is 10€
            assertThat(FeeManager.getTransferFee(500.0))
                .isEqualTo(10.0);
        }

        @ParameterizedTest
        @CsvSource({
            "0.0, 10.0",           // Zero amount → minimum fee
            "1.0, 10.0",           // Tiny amount → minimum fee
            "100.0, 10.0",         // 1€ fee → minimum enforced
            "999.0, 10.0",         // 9.99€ fee → minimum enforced
            "1000.0, 10.0",        // Exactly at threshold
            "1001.0, 10.01",       // Just above threshold
            "2000.0, 20.0",        // 1% = 20€
            "5000.0, 50.0",        // 1% = 50€
            "10000.0, 100.0",      // 1% = 100€
            "100000.0, 1000.0",    // Large amount
            "1000000.0, 10000.0"   // Very large amount
        })
        @DisplayName("Transfer fee should calculate correctly for various amounts")
        void transferFeeShouldCalculateCorrectly(double amount, double expectedFee) {
            assertThat(FeeManager.getTransferFee(amount))
                .isEqualTo(expectedFee);
        }

        @Test
        @DisplayName("Transfer fee should handle very large amounts without overflow")
        void transferFeeShouldHandleVeryLargeAmounts() {
            double amount = Double.MAX_VALUE / 100; // Very large but safe from overflow
            double fee = FeeManager.getTransferFee(amount);

            assertThat(fee).isPositive();
            assertThat(fee).isLessThan(Double.MAX_VALUE);
        }

        @Test
        @DisplayName("Transfer fee for zero should return minimum")
        void transferFeeForZeroShouldReturnMinimum() {
            assertThat(FeeManager.getTransferFee(0.0))
                .isEqualTo(10.0);
        }

        @Test
        @DisplayName("Transfer fee should be consistent with formula: max(1% of amount, 10€)")
        void transferFeeShouldBeConsistentWithFormula() {
            for (double amount : new double[]{10, 50, 100, 500, 1000, 5000, 10000, 50000}) {
                double fee = FeeManager.getTransferFee(amount);
                double calculatedFee = Math.max(amount * 0.01, 10.0);

                assertThat(fee).isEqualTo(calculatedFee);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @ParameterizedTest
        @ValueSource(doubles = {-1.0, -10.0, -100.0, -1000.0})
        @DisplayName("Negative transfer amounts should still calculate minimum fee")
        void negativeTransferAmountsShouldStillCalculateFee(double negativeAmount) {
            // Note: The manager doesn't reject negative values,
            // it just applies the formula which results in min fee
            double fee = FeeManager.getTransferFee(negativeAmount);
            assertThat(fee).isEqualTo(10.0); // Should return minimum fee
        }

        @Test
        @DisplayName("Very small positive transfer amount should return minimum fee")
        void verySmallPositiveAmountShouldReturnMinimumFee() {
            assertThat(FeeManager.getTransferFee(0.01))
                .isEqualTo(10.0);

            assertThat(FeeManager.getTransferFee(1.0))
                .isEqualTo(10.0);
        }

        @Test
        @DisplayName("Transfer fee should handle boundary at 1000€ correctly")
        void transferFeeShouldHandleBoundaryCorrectly() {
            // At exactly 1000€, 1% = 10€ (equals minimum)
            assertThat(FeeManager.getTransferFee(1000.0))
                .isEqualTo(10.0);

            // Just below threshold
            assertThat(FeeManager.getTransferFee(999.99))
                .isEqualTo(10.0);

            // Just above threshold
            assertThat(FeeManager.getTransferFee(1000.01))
                .isCloseTo(10.0, within(0.01));
        }

        @Test
        @DisplayName("Transfer fee should not lose precision for typical amounts")
        void transferFeeShouldNotLosePrecision() {
            // Test that common amounts calculate precisely
            assertThat(FeeManager.getTransferFee(1234.56))
                .isEqualTo(12.3456);

            assertThat(FeeManager.getTransferFee(9999.99))
                .isCloseTo(99.9999, within(0.0001));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FEE INFO FORMATTING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Fee Info Formatting Tests")
    class FeeInfoFormattingTests {

        @Test
        @DisplayName("Fee info should contain all required information")
        void feeInfoShouldContainRequiredInfo() {
            String info = FeeManager.getFeeInfo();

            assertThat(info)
                .contains("GEBÜHREN-ÜBERSICHT")
                .contains("ATM-Gebühr")
                .contains("5")  // ATM fee value
                .contains("Transfer-Gebühr")
                .contains("1.0%")  // Transfer percentage
                .contains("10");  // Minimum transfer fee
        }

        @Test
        @DisplayName("Fee info should be non-null and non-empty")
        void feeInfoShouldBeNonNullAndNonEmpty() {
            String info = FeeManager.getFeeInfo();

            assertThat(info).isNotNull();
            assertThat(info).isNotEmpty();
        }

        @Test
        @DisplayName("Fee info should use consistent formatting")
        void feeInfoShouldUseConsistentFormatting() {
            String info = FeeManager.getFeeInfo();

            // Should contain Minecraft color codes
            assertThat(info).contains("§e");  // Yellow color code
            assertThat(info).contains("§l");  // Bold formatting
            assertThat(info).contains("§7");  // Gray color code
            assertThat(info).contains("§c");  // Red color code
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULATION ACCURACY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Calculation Accuracy Tests")
    class CalculationAccuracyTests {

        @Test
        @DisplayName("Transfer fee should be monotonically increasing with amount")
        void transferFeeShouldBeMonotonicallyIncreasing() {
            double previousFee = 0;
            for (double amount = 0; amount <= 100000; amount += 1000) {
                double currentFee = FeeManager.getTransferFee(amount);
                assertThat(currentFee).isGreaterThanOrEqualTo(previousFee);
                previousFee = currentFee;
            }
        }

        @Test
        @DisplayName("Transfer fee should scale linearly above minimum threshold")
        void transferFeeShouldScaleLinearly() {
            // Above 1000€, fee should scale at 1%
            double amount1 = 2000.0;
            double amount2 = 4000.0;

            double fee1 = FeeManager.getTransferFee(amount1);
            double fee2 = FeeManager.getTransferFee(amount2);

            // Doubling the amount should double the fee
            assertThat(fee2).isCloseTo(fee1 * 2, within(0.01));
        }

        @Test
        @DisplayName("ATM fee should never change during runtime")
        void atmFeeShouldBeConstant() {
            double fee1 = FeeManager.getATMFee();
            double fee2 = FeeManager.getATMFee();
            double fee3 = FeeManager.getATMFee();

            assertThat(fee1).isEqualTo(fee2);
            assertThat(fee2).isEqualTo(fee3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS LOGIC VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Minimum transfer fee should prevent free transfers")
        void minimumFeeShouldPreventFreeTransfers() {
            // Even for tiny amounts, there should be a fee
            assertThat(FeeManager.getTransferFee(0.01))
                .isGreaterThan(0.0);
        }

        @Test
        @DisplayName("ATM fee should be economically meaningful (not zero)")
        void atmFeeShouldBeMeaningful() {
            assertThat(FeeManager.getATMFee())
                .isGreaterThan(0.0);
        }

        @Test
        @DisplayName("Transfer fee percentage should be reasonable (1-10%)")
        void transferFeePercentageShouldBeReasonable() {
            double largeAmount = 100_000.0;
            double fee = FeeManager.getTransferFee(largeAmount);
            double percentage = fee / largeAmount;

            assertThat(percentage)
                .isGreaterThan(0.0)  // Not free
                .isLessThan(0.10);    // Less than 10%
        }

        @Test
        @DisplayName("Fee calculation should be deterministic")
        void feeCalculationShouldBeDeterministic() {
            double amount = 5000.0;

            // Calculate fee multiple times
            double fee1 = FeeManager.getTransferFee(amount);
            double fee2 = FeeManager.getTransferFee(amount);
            double fee3 = FeeManager.getTransferFee(amount);

            // All should be identical
            assertThat(fee1).isEqualTo(fee2);
            assertThat(fee2).isEqualTo(fee3);
        }
    }
}
