package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TaxManager
 *
 * Tests cover:
 * - Progressive income tax bracket calculation (pure math, no config dependency)
 * - Edge cases at bracket boundaries
 */
class TaxManagerTest {

    @TempDir
    Path tempDir;

    private TaxManager taxManager;

    @BeforeEach
    void setUp() throws Exception {
        // Reset TaxManager singleton
        Field instanceField = TaxManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // Mock MinecraftServer - only getServerDirectory() is needed for construction
        MinecraftServer mockServer = Mockito.mock(MinecraftServer.class);
        Mockito.when(mockServer.getServerDirectory()).thenReturn(tempDir.toFile());

        taxManager = TaxManager.getInstance(mockServer);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field instanceField = TaxManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    // ==================== Income Tax Bracket Tests ====================

    @Test
    @DisplayName("Balance below tax-free threshold (5.000€) should result in zero tax")
    void testIncomeTax_BelowThreshold_Zero() {
        assertThat(taxManager.calculateIncomeTax(5_000.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Balance exactly at tax-free threshold (10.000€) should result in zero tax")
    void testIncomeTax_ExactlyAtThreshold_Zero() {
        assertThat(taxManager.calculateIncomeTax(10_000.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Balance of 0€ should result in zero tax")
    void testIncomeTax_Zero_Zero() {
        assertThat(taxManager.calculateIncomeTax(0.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Balance in first bracket (30.000€) → 10% on 20k taxable = 2.000€")
    void testIncomeTax_FirstBracket_30k() {
        // taxable = 30.000 - 10.000 = 20.000
        // tax = 20.000 × 10% = 2.000€
        assertThat(taxManager.calculateIncomeTax(30_000.0)).isCloseTo(2_000.0, within(0.01));
    }

    @Test
    @DisplayName("Balance exactly at first bracket limit (50.000€) → 10% on 40k = 4.000€")
    void testIncomeTax_AtFirstBracketLimit_50k() {
        // taxable = 50.000 - 10.000 = 40.000
        // tax = 40.000 × 10% = 4.000€
        assertThat(taxManager.calculateIncomeTax(50_000.0)).isCloseTo(4_000.0, within(0.01));
    }

    @Test
    @DisplayName("Balance in second bracket (75.000€) → 4.000 + 15% on 25k = 7.750€")
    void testIncomeTax_SecondBracket_75k() {
        // 10% on 40k = 4.000
        // 15% on (65.000 - 40.000) = 15% on 25.000 = 3.750
        // total = 7.750€
        assertThat(taxManager.calculateIncomeTax(75_000.0)).isCloseTo(7_750.0, within(0.01));
    }

    @Test
    @DisplayName("Balance exactly at second bracket limit (100.000€) → 4.000 + 7.500 = 11.500€")
    void testIncomeTax_AtSecondBracketLimit_100k() {
        // 10% on 40k = 4.000
        // 15% on 50k = 7.500
        // total = 11.500€
        assertThat(taxManager.calculateIncomeTax(100_000.0)).isCloseTo(11_500.0, within(0.01));
    }

    @Test
    @DisplayName("Balance above second bracket (200.000€) → 4.000 + 7.500 + 20% on 100k = 31.500€")
    void testIncomeTax_ThirdBracket_200k() {
        // 10% on 40k = 4.000
        // 15% on 50k = 7.500
        // 20% on (190.000 - 90.000) = 20% on 100.000 = 20.000
        // total = 31.500€
        assertThat(taxManager.calculateIncomeTax(200_000.0)).isCloseTo(31_500.0, within(0.01));
    }

    @Test
    @DisplayName("Tax should always be non-negative")
    void testIncomeTax_AlwaysNonNegative() {
        assertThat(taxManager.calculateIncomeTax(0.0)).isGreaterThanOrEqualTo(0.0);
        assertThat(taxManager.calculateIncomeTax(5_000.0)).isGreaterThanOrEqualTo(0.0);
        assertThat(taxManager.calculateIncomeTax(50_000.0)).isGreaterThanOrEqualTo(0.0);
        assertThat(taxManager.calculateIncomeTax(500_000.0)).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("Tax should increase monotonically with balance")
    void testIncomeTax_Monotonic() {
        double tax10k = taxManager.calculateIncomeTax(10_000.0);
        double tax30k = taxManager.calculateIncomeTax(30_000.0);
        double tax100k = taxManager.calculateIncomeTax(100_000.0);
        double tax500k = taxManager.calculateIncomeTax(500_000.0);

        assertThat(tax10k).isLessThanOrEqualTo(tax30k);
        assertThat(tax30k).isLessThan(tax100k);
        assertThat(tax100k).isLessThan(tax500k);
    }
}
