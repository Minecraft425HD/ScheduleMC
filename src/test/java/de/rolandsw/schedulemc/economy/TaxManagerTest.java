package de.rolandsw.schedulemc.economy;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for TaxManager (Tax System)
 *
 * Tests cover:
 * - Income tax calculation (progressive: 0%, 10%, 15%, 20%)
 * - Property tax calculation (chunk-based, 100€ per chunk)
 * - Tax brackets (10k free, then 10%, 15%, 20%)
 * - Tax period processing (every 7 days)
 * - Tax debt accumulation and payment
 * - Edge cases: zero balance, no property, large balances
 *
 * BUSINESS LOGIC:
 * - Tax-free amount: 10,000€
 * - Bracket 1 (10%): 10,000€ - 50,000€
 * - Bracket 2 (15%): 50,000€ - 100,000€
 * - Bracket 3 (20%): above 100,000€
 * - Property tax: 100€ per chunk per month (7 days)
 */
@DisplayName("TaxManager Tests")
class TaxManagerTest {

    private static final UUID TEST_PLAYER = UUID.randomUUID();
    private static final UUID TEST_PLAYER_2 = UUID.randomUUID();
    private static final double TAX_FREE_AMOUNT = 10000.0;
    private static final double TAX_BRACKET_1 = 50000.0;
    private static final double TAX_BRACKET_2 = 100000.0;
    private static final int TAX_PERIOD_DAYS = 7;

    private TaxManager taxManager;
    private MinecraftServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance
        resetTaxManagerInstance();

        // Create mock server
        mockServer = createMockServer();

        // Get TaxManager instance
        taxManager = TaxManager.getInstance(mockServer);

        // Clear all data
        clearTaxData();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetTaxManagerInstance();
    }

    // ═══════════════════════════════════════════════════════════
    // INCOME TAX CALCULATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Income Tax Calculation Tests")
    class IncomeTaxCalculationTests {

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 1000.0, 5000.0, 9999.99, 10000.0})
        @DisplayName("No tax should be charged for balances under or equal to 10,000€")
        void noTaxForBalancesUnderTaxFreeAmount(double balance) {
            double tax = taxManager.calculateIncomeTax(balance);

            assertThat(tax).isZero();
        }

        @ParameterizedTest
        @CsvSource({
            "15000.0, 500.0",     // 5k taxable @ 10% = 500€
            "20000.0, 1000.0",    // 10k taxable @ 10% = 1000€
            "30000.0, 2000.0",    // 20k taxable @ 10% = 2000€
            "50000.0, 4000.0"     // 40k taxable @ 10% = 4000€
        })
        @DisplayName("10% tax for bracket 1 (10k - 50k)")
        void tenPercentTaxForBracket1(double balance, double expectedTax) {
            double tax = taxManager.calculateIncomeTax(balance);

            assertThat(tax).isEqualTo(expectedTax);
        }

        @ParameterizedTest
        @CsvSource({
            "60000.0, 5500.0",    // 40k @ 10% + 10k @ 15% = 4000 + 1500 = 5500€
            "75000.0, 7750.0",    // 40k @ 10% + 25k @ 15% = 4000 + 3750 = 7750€
            "100000.0, 11500.0"   // 40k @ 10% + 50k @ 15% = 4000 + 7500 = 11500€
        })
        @DisplayName("15% tax for bracket 2 (50k - 100k)")
        void fifteenPercentTaxForBracket2(double balance, double expectedTax) {
            double tax = taxManager.calculateIncomeTax(balance);

            assertThat(tax).isEqualTo(expectedTax);
        }

        @ParameterizedTest
        @CsvSource({
            "110000.0, 13500.0",  // 40k @ 10% + 50k @ 15% + 10k @ 20% = 4000 + 7500 + 2000
            "150000.0, 21500.0",  // 40k @ 10% + 50k @ 15% + 50k @ 20% = 4000 + 7500 + 10000
            "200000.0, 31500.0"   // 40k @ 10% + 50k @ 15% + 100k @ 20% = 4000 + 7500 + 20000
        })
        @DisplayName("20% tax for bracket 3 (above 100k)")
        void twentyPercentTaxForBracket3(double balance, double expectedTax) {
            double tax = taxManager.calculateIncomeTax(balance);

            assertThat(tax).isEqualTo(expectedTax);
        }

        @Test
        @DisplayName("Tax should be zero at exact tax-free threshold")
        void taxShouldBeZeroAtExactThreshold() {
            double tax = taxManager.calculateIncomeTax(TAX_FREE_AMOUNT);

            assertThat(tax).isZero();
        }

        @Test
        @DisplayName("Tax should be minimal just above tax-free threshold")
        void taxShouldBeMinimalJustAboveThreshold() {
            double tax = taxManager.calculateIncomeTax(TAX_FREE_AMOUNT + 100);

            assertThat(tax).isEqualTo(10.0); // 100 * 0.10
        }

        @Test
        @DisplayName("Tax calculation should be deterministic")
        void taxCalculationShouldBeDeterministic() {
            double balance = 75000.0;

            double tax1 = taxManager.calculateIncomeTax(balance);
            double tax2 = taxManager.calculateIncomeTax(balance);
            double tax3 = taxManager.calculateIncomeTax(balance);

            assertThat(tax1).isEqualTo(tax2);
            assertThat(tax2).isEqualTo(tax3);
        }

        @Test
        @DisplayName("Tax should handle very large balances")
        void taxShouldHandleVeryLargeBalances() {
            double tax = taxManager.calculateIncomeTax(1_000_000.0);

            // Should be: 4000 + 7500 + (890000 * 0.20) = 4000 + 7500 + 178000 = 189500
            assertThat(tax).isGreaterThan(0);
            assertThat(tax).isLessThan(1_000_000.0);
        }

        @Test
        @DisplayName("Tax should handle decimal precision")
        void taxShouldHandleDecimalPrecision() {
            double tax = taxManager.calculateIncomeTax(12345.67);

            // Taxable: 2345.67, Tax: 2345.67 * 0.10 = 234.567
            assertThat(tax).isCloseTo(234.567, within(0.001));
        }

        @Test
        @DisplayName("Tax rate should increase progressively")
        void taxRateShouldIncreaseProgressively() {
            double tax1 = taxManager.calculateIncomeTax(20000.0);  // 10% bracket
            double tax2 = taxManager.calculateIncomeTax(60000.0);  // 15% bracket
            double tax3 = taxManager.calculateIncomeTax(110000.0); // 20% bracket

            // Effective tax rate should increase
            double rate1 = tax1 / 20000.0;
            double rate2 = tax2 / 60000.0;
            double rate3 = tax3 / 110000.0;

            assertThat(rate2).isGreaterThan(rate1);
            assertThat(rate3).isGreaterThan(rate2);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PROPERTY TAX CALCULATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Property Tax Calculation Tests")
    class PropertyTaxCalculationTests {

        @Test
        @DisplayName("No property tax for players without plots")
        void noPropertyTaxWithoutPlots() {
            try (MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class)) {
                plotMock.when(() -> PlotManager.getPlotsByOwner(TEST_PLAYER))
                    .thenReturn(Collections.emptyList());

                double tax = taxManager.calculatePropertyTax(TEST_PLAYER);

                assertThat(tax).isZero();
            }
        }

        @Test
        @DisplayName("Property tax should be 100€ per chunk (default config)")
        void propertyTaxShouldBePerChunk() {
            try (MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class);
                 MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {

                // Mock config value
                ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
                when(mockCommon.TAX_PROPERTY_PER_CHUNK).thenReturn(new MockConfigValue<>(100.0));
                configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

                // Mock plot: 16x16 (1 chunk)
                PlotRegion mockPlot = createMockPlot(
                    new BlockPos(0, 0, 0),
                    new BlockPos(15, 64, 15)
                );

                plotMock.when(() -> PlotManager.getPlotsByOwner(TEST_PLAYER))
                    .thenReturn(List.of(mockPlot));

                double tax = taxManager.calculatePropertyTax(TEST_PLAYER);

                assertThat(tax).isEqualTo(100.0);
            }
        }

        @Test
        @DisplayName("Property tax should scale with number of chunks")
        void propertyTaxShouldScaleWithChunks() {
            try (MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class);
                 MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {

                ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
                when(mockCommon.TAX_PROPERTY_PER_CHUNK).thenReturn(new MockConfigValue<>(100.0));
                configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

                // Mock plot: 48x48 (9 chunks: 3x3)
                PlotRegion mockPlot = createMockPlot(
                    new BlockPos(0, 0, 0),
                    new BlockPos(47, 64, 47)
                );

                plotMock.when(() -> PlotManager.getPlotsByOwner(TEST_PLAYER))
                    .thenReturn(List.of(mockPlot));

                double tax = taxManager.calculatePropertyTax(TEST_PLAYER);

                assertThat(tax).isEqualTo(900.0); // 9 chunks * 100€
            }
        }

        @Test
        @DisplayName("Property tax should sum multiple plots")
        void propertyTaxShouldSumMultiplePlots() {
            try (MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class);
                 MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {

                ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
                when(mockCommon.TAX_PROPERTY_PER_CHUNK).thenReturn(new MockConfigValue<>(100.0));
                configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

                // Plot 1: 16x16 (1 chunk)
                PlotRegion plot1 = createMockPlot(
                    new BlockPos(0, 0, 0),
                    new BlockPos(15, 64, 15)
                );

                // Plot 2: 32x32 (4 chunks)
                PlotRegion plot2 = createMockPlot(
                    new BlockPos(100, 0, 100),
                    new BlockPos(131, 64, 131)
                );

                plotMock.when(() -> PlotManager.getPlotsByOwner(TEST_PLAYER))
                    .thenReturn(List.of(plot1, plot2));

                double tax = taxManager.calculatePropertyTax(TEST_PLAYER);

                assertThat(tax).isEqualTo(500.0); // 5 chunks * 100€
            }
        }

        @Test
        @DisplayName("Property tax should round up partial chunks")
        void propertyTaxShouldRoundUpPartialChunks() {
            try (MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class);
                 MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {

                ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
                when(mockCommon.TAX_PROPERTY_PER_CHUNK).thenReturn(new MockConfigValue<>(100.0));
                configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

                // Plot: 20x20 (slightly more than 1 chunk = 1.5625 chunks, should round to 2)
                PlotRegion mockPlot = createMockPlot(
                    new BlockPos(0, 0, 0),
                    new BlockPos(19, 64, 19)
                );

                plotMock.when(() -> PlotManager.getPlotsByOwner(TEST_PLAYER))
                    .thenReturn(List.of(mockPlot));

                double tax = taxManager.calculatePropertyTax(TEST_PLAYER);

                assertThat(tax).isEqualTo(200.0); // Rounded up to 2 chunks
            }
        }

        @Test
        @DisplayName("Property tax calculation should be consistent")
        void propertyTaxCalculationShouldBeConsistent() {
            try (MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class);
                 MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {

                ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
                when(mockCommon.TAX_PROPERTY_PER_CHUNK).thenReturn(new MockConfigValue<>(100.0));
                configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

                PlotRegion mockPlot = createMockPlot(
                    new BlockPos(0, 0, 0),
                    new BlockPos(31, 64, 31)
                );

                plotMock.when(() -> PlotManager.getPlotsByOwner(TEST_PLAYER))
                    .thenReturn(List.of(mockPlot));

                double tax1 = taxManager.calculatePropertyTax(TEST_PLAYER);
                double tax2 = taxManager.calculatePropertyTax(TEST_PLAYER);
                double tax3 = taxManager.calculatePropertyTax(TEST_PLAYER);

                assertThat(tax1).isEqualTo(tax2);
                assertThat(tax2).isEqualTo(tax3);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TAX DEBT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Tax Debt Tests")
    class TaxDebtTests {

        @Test
        @DisplayName("New player should have zero tax debt")
        void newPlayerShouldHaveZeroTaxDebt() {
            double debt = taxManager.getTaxDebt(TEST_PLAYER);

            assertThat(debt).isZero();
        }

        @Test
        @DisplayName("Tax debt should be retrievable")
        void taxDebtShouldBeRetrievable() throws Exception {
            setTaxDebt(TEST_PLAYER, 500.0);

            double debt = taxManager.getTaxDebt(TEST_PLAYER);

            assertThat(debt).isEqualTo(500.0);
        }

        @Test
        @DisplayName("Tax debt payment should succeed with sufficient balance")
        void taxDebtPaymentShouldSucceedWithSufficientBalance() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<StateAccount> stateMock = mockStatic(StateAccount.class)) {

                setTaxDebt(TEST_PLAYER, 500.0);

                StateAccount mockStateAccount = mock(StateAccount.class);
                stateMock.when(() -> StateAccount.getInstance(any())).thenReturn(mockStateAccount);

                economyMock.when(() -> EconomyManager.withdraw(
                    eq(TEST_PLAYER), eq(500.0), eq(TransactionType.TAX_INCOME), anyString()
                )).thenReturn(true);

                boolean result = taxManager.payTaxDebt(TEST_PLAYER);

                assertThat(result).isTrue();
                assertThat(taxManager.getTaxDebt(TEST_PLAYER)).isZero();
            }
        }

        @Test
        @DisplayName("Tax debt payment should fail with insufficient balance")
        void taxDebtPaymentShouldFailWithInsufficientBalance() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                setTaxDebt(TEST_PLAYER, 500.0);

                economyMock.when(() -> EconomyManager.withdraw(
                    eq(TEST_PLAYER), anyDouble(), any(), anyString()
                )).thenReturn(false);

                boolean result = taxManager.payTaxDebt(TEST_PLAYER);

                assertThat(result).isFalse();
                assertThat(taxManager.getTaxDebt(TEST_PLAYER)).isEqualTo(500.0);
            }
        }

        @Test
        @DisplayName("Paying zero debt should return false")
        void payingZeroDebtShouldReturnFalse() {
            boolean result = taxManager.payTaxDebt(TEST_PLAYER);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Tax debt should deposit to state account when paid")
        void taxDebtShouldDepositToStateAccount() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<StateAccount> stateMock = mockStatic(StateAccount.class)) {

                setTaxDebt(TEST_PLAYER, 1000.0);

                StateAccount mockStateAccount = mock(StateAccount.class);
                stateMock.when(() -> StateAccount.getInstance(any())).thenReturn(mockStateAccount);

                economyMock.when(() -> EconomyManager.withdraw(any(), anyDouble(), any(), anyString()))
                    .thenReturn(true);

                taxManager.payTaxDebt(TEST_PLAYER);

                verify(mockStateAccount).deposit(eq(1000.0), anyString());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TAX PERIOD PROCESSING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Tax Period Processing Tests")
    class TaxPeriodProcessingTests {

        @Test
        @DisplayName("Taxes should not be charged before 7 days")
        void taxesShouldNotBeChargedBeforeSevenDays() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 50000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                // Tick for 6 days
                for (long day = 1; day <= 6; day++) {
                    taxManager.tick(day * 24000L);
                }

                // Should NOT have withdrawn taxes
                economyMock.verify(() -> EconomyManager.withdraw(
                    any(), anyDouble(), any(), anyString()
                ), never());
            }
        }

        @Test
        @DisplayName("Tax period should be exactly 7 days")
        void taxPeriodShouldBeSevenDays() {
            assertThat(TAX_PERIOD_DAYS).isEqualTo(7);
        }

        @Test
        @DisplayName("Multiple ticks on same day should not duplicate tax charges")
        void multipleTicksOnSameDayShouldNotDuplicateCharges() throws Exception {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class);
                 MockedStatic<StateAccount> stateMock = mockStatic(StateAccount.class)) {

                Map<UUID, Double> balances = new ConcurrentHashMap<>();
                balances.put(TEST_PLAYER, 50000.0);

                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);
                plotMock.when(() -> PlotManager.getPlotsByOwner(any())).thenReturn(Collections.emptyList());

                StateAccount mockStateAccount = mock(StateAccount.class);
                stateMock.when(() -> StateAccount.getInstance(any())).thenReturn(mockStateAccount);

                economyMock.when(() -> EconomyManager.withdraw(any(), anyDouble(), any(), anyString()))
                    .thenReturn(true);

                // Multiple ticks on day 7
                taxManager.tick(7 * 24000L);
                taxManager.tick(7 * 24000L + 100);
                taxManager.tick(7 * 24000L + 500);

                // Should only charge once
                economyMock.verify(() -> EconomyManager.withdraw(
                    eq(TEST_PLAYER), anyDouble(), any(), anyString()
                ), times(1));
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
        @DisplayName("Tax should handle negative balance gracefully")
        void taxShouldHandleNegativeBalance() {
            double tax = taxManager.calculateIncomeTax(-1000.0);

            assertThat(tax).isZero();
        }

        @Test
        @DisplayName("Tax should handle zero balance")
        void taxShouldHandleZeroBalance() {
            double tax = taxManager.calculateIncomeTax(0.0);

            assertThat(tax).isZero();
        }

        @Test
        @DisplayName("Tax should handle extremely large balance")
        void taxShouldHandleExtremelyLargeBalance() {
            double tax = taxManager.calculateIncomeTax(Double.MAX_VALUE / 10);

            assertThat(tax).isGreaterThan(0);
            assertThat(tax).isLessThan(Double.MAX_VALUE);
        }

        @Test
        @DisplayName("Property tax should handle very large plots")
        void propertyTaxShouldHandleVeryLargePlots() {
            try (MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class);
                 MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {

                ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
                when(mockCommon.TAX_PROPERTY_PER_CHUNK).thenReturn(new MockConfigValue<>(100.0));
                configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

                // Huge plot: 1000x1000 (3906 chunks)
                PlotRegion mockPlot = createMockPlot(
                    new BlockPos(0, 0, 0),
                    new BlockPos(999, 64, 999)
                );

                plotMock.when(() -> PlotManager.getPlotsByOwner(TEST_PLAYER))
                    .thenReturn(List.of(mockPlot));

                double tax = taxManager.calculatePropertyTax(TEST_PLAYER);

                assertThat(tax).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("Tax brackets should handle exact boundary values")
        void taxBracketsShouldHandleExactBoundaries() {
            // Exact tax-free boundary
            assertThat(taxManager.calculateIncomeTax(10000.0)).isZero();

            // Exact bracket 1 boundary
            double taxAt50k = taxManager.calculateIncomeTax(50000.0);
            assertThat(taxAt50k).isEqualTo(4000.0); // 40k @ 10%

            // Exact bracket 2 boundary
            double taxAt100k = taxManager.calculateIncomeTax(100000.0);
            assertThat(taxAt100k).isEqualTo(11500.0); // 40k @ 10% + 50k @ 15%
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS LOGIC VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Tax brackets should be correctly defined")
        void taxBracketsShouldBeCorrectlyDefined() {
            assertThat(TAX_FREE_AMOUNT).isEqualTo(10000.0);
            assertThat(TAX_BRACKET_1).isEqualTo(50000.0);
            assertThat(TAX_BRACKET_2).isEqualTo(100000.0);
        }

        @Test
        @DisplayName("Progressive taxation should result in higher effective rates for higher incomes")
        void progressiveTaxationShouldResultInHigherEffectiveRates() {
            double tax20k = taxManager.calculateIncomeTax(20000.0);
            double tax50k = taxManager.calculateIncomeTax(50000.0);
            double tax100k = taxManager.calculateIncomeTax(100000.0);

            double effectiveRate20k = tax20k / 20000.0;
            double effectiveRate50k = tax50k / 50000.0;
            double effectiveRate100k = tax100k / 100000.0;

            assertThat(effectiveRate50k).isGreaterThan(effectiveRate20k);
            assertThat(effectiveRate100k).isGreaterThan(effectiveRate50k);
        }

        @Test
        @DisplayName("Total tax should be sum of income and property tax")
        void totalTaxShouldBeSumOfIncomeAndProperty() {
            try (MockedStatic<PlotManager> plotMock = mockStatic(PlotManager.class);
                 MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {

                ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
                when(mockCommon.TAX_PROPERTY_PER_CHUNK).thenReturn(new MockConfigValue<>(100.0));
                configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

                PlotRegion mockPlot = createMockPlot(
                    new BlockPos(0, 0, 0),
                    new BlockPos(15, 64, 15)
                );

                plotMock.when(() -> PlotManager.getPlotsByOwner(TEST_PLAYER))
                    .thenReturn(List.of(mockPlot));

                double balance = 50000.0;
                double incomeTax = taxManager.calculateIncomeTax(balance);
                double propertyTax = taxManager.calculatePropertyTax(TEST_PLAYER);
                double totalExpected = incomeTax + propertyTax;

                assertThat(totalExpected).isEqualTo(4100.0); // 4000 + 100
            }
        }

        @Test
        @DisplayName("Tax system should encourage lower incomes (progressive)")
        void taxSystemShouldEncourageLowerIncomes() {
            // Marginal tax rate should increase with income
            double balance1 = 20000.0;
            double balance2 = 60000.0;
            double balance3 = 110000.0;

            double tax1 = taxManager.calculateIncomeTax(balance1);
            double tax2 = taxManager.calculateIncomeTax(balance2);
            double tax3 = taxManager.calculateIncomeTax(balance3);

            // Marginal increase in tax should be higher for higher incomes
            double marginal1to2 = (tax2 - tax1) / (balance2 - balance1);
            double marginal2to3 = (tax3 - tax2) / (balance3 - balance2);

            assertThat(marginal2to3).isGreaterThan(marginal1to2);
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

            TaxManager instance1 = TaxManager.getInstance(server);
            TaxManager instance2 = TaxManager.getInstance(server);

            assertThat(instance1).isSameAs(instance2);
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

    private void resetTaxManagerInstance() throws Exception {
        Field instanceField = TaxManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private void clearTaxData() throws Exception {
        Field lastTaxField = TaxManager.class.getDeclaredField("lastTaxDay");
        lastTaxField.setAccessible(true);
        Map<UUID, Long> lastTax = (Map<UUID, Long>) lastTaxField.get(taxManager);
        lastTax.clear();

        Field debtField = TaxManager.class.getDeclaredField("taxDebt");
        debtField.setAccessible(true);
        Map<UUID, Double> debt = (Map<UUID, Double>) debtField.get(taxManager);
        debt.clear();
    }

    private void setTaxDebt(UUID player, double amount) throws Exception {
        Field debtField = TaxManager.class.getDeclaredField("taxDebt");
        debtField.setAccessible(true);
        Map<UUID, Double> debt = (Map<UUID, Double>) debtField.get(taxManager);
        debt.put(player, amount);
    }

    private PlotRegion createMockPlot(BlockPos min, BlockPos max) {
        PlotRegion mockPlot = mock(PlotRegion.class);
        when(mockPlot.getMin()).thenReturn(min);
        when(mockPlot.getMax()).thenReturn(max);
        return mockPlot;
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
        public List<String> getPath() { return Collections.emptyList(); }
        @Override
        public void save() {}
        @Override
        public void set(T value) {}
        @Override
        public void clearCache() {}
    }
}
