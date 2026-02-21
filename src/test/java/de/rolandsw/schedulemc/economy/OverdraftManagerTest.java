package de.rolandsw.schedulemc.economy;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for OverdraftManager
 *
 * Tests cover:
 * - Static helper: getOverdraftAmount()
 * - Prison time calculation: getPotentialPrisonMinutes()
 * - Day countdown methods: getDaysSinceDebtStart(), getDaysUntilPrison(), getDaysUntilAutoRepay()
 */
class OverdraftManagerTest {

    @TempDir
    Path tempDir;

    private OverdraftManager overdraftManager;

    @BeforeEach
    void setUp() throws Exception {
        // Reset OverdraftManager singleton
        Field instanceField = OverdraftManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        MinecraftServer mockServer = Mockito.mock(MinecraftServer.class);
        Mockito.when(mockServer.getServerDirectory()).thenReturn(tempDir.toFile());

        overdraftManager = OverdraftManager.getInstance(mockServer);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field instanceField = OverdraftManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    // ==================== Static: getOverdraftAmount() ====================

    @Test
    @DisplayName("Positive balance should have zero overdraft amount")
    void testGetOverdraftAmount_PositiveBalance_ReturnsZero() {
        assertThat(OverdraftManager.getOverdraftAmount(1000.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Zero balance should have zero overdraft amount")
    void testGetOverdraftAmount_ZeroBalance_ReturnsZero() {
        assertThat(OverdraftManager.getOverdraftAmount(0.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Negative balance should return absolute value as overdraft amount")
    void testGetOverdraftAmount_NegativeBalance_ReturnsAbsolute() {
        assertThat(OverdraftManager.getOverdraftAmount(-500.0)).isEqualTo(500.0);
    }

    @Test
    @DisplayName("Large negative balance should return correct absolute value")
    void testGetOverdraftAmount_LargeNegative() {
        assertThat(OverdraftManager.getOverdraftAmount(-99999.99)).isCloseTo(99999.99, within(0.001));
    }

    // ==================== Prison Minutes Calculation ====================

    @Test
    @DisplayName("1.000€ debt should result in exactly 1 prison minute")
    void testPrisonMinutes_1000Debt_Is1Minute() {
        assertThat(overdraftManager.getPotentialPrisonMinutes(1000.0)).isEqualTo(1.0);
    }

    @Test
    @DisplayName("1.001€ debt should round up to 2 prison minutes (ceil)")
    void testPrisonMinutes_1001Debt_Is2Minutes() {
        assertThat(overdraftManager.getPotentialPrisonMinutes(1001.0)).isEqualTo(2.0);
    }

    @Test
    @DisplayName("1.500€ debt should round up to 2 prison minutes (ceil of 1.5)")
    void testPrisonMinutes_1500Debt_Is2Minutes() {
        assertThat(overdraftManager.getPotentialPrisonMinutes(1500.0)).isEqualTo(2.0);
    }

    @Test
    @DisplayName("2.000€ debt should be exactly 2 prison minutes")
    void testPrisonMinutes_2000Debt_Is2Minutes() {
        assertThat(overdraftManager.getPotentialPrisonMinutes(2000.0)).isEqualTo(2.0);
    }

    @Test
    @DisplayName("10.000€ debt should be 10 prison minutes")
    void testPrisonMinutes_10000Debt_Is10Minutes() {
        assertThat(overdraftManager.getPotentialPrisonMinutes(10_000.0)).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Zero debt should result in zero prison minutes")
    void testPrisonMinutes_ZeroDebt_IsZero() {
        assertThat(overdraftManager.getPotentialPrisonMinutes(0.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Negative debt should result in zero prison minutes")
    void testPrisonMinutes_NegativeDebt_IsZero() {
        assertThat(overdraftManager.getPotentialPrisonMinutes(-500.0)).isEqualTo(0.0);
    }

    // ==================== Day Countdown (via Reflection) ====================

    /**
     * Helper: sets currentDay field via reflection
     */
    private void setCurrentDay(long day) throws Exception {
        Field currentDayField = OverdraftManager.class.getDeclaredField("currentDay");
        currentDayField.setAccessible(true);
        currentDayField.set(overdraftManager, day);
    }

    /**
     * Helper: sets debtStartDay for a player via reflection
     */
    private void setDebtStartDay(UUID playerUUID, long day) throws Exception {
        Field debtStartDayField = OverdraftManager.class.getDeclaredField("debtStartDay");
        debtStartDayField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, Long> debtStartDay = (Map<UUID, Long>) debtStartDayField.get(overdraftManager);
        debtStartDay.put(playerUUID, day);
    }

    @Test
    @DisplayName("Player with no debt should have 0 days since debt start")
    void testDaysSinceDebtStart_NoDebt_ReturnsZero() {
        UUID player = UUID.randomUUID();
        assertThat(overdraftManager.getDaysSinceDebtStart(player)).isEqualTo(0);
    }

    @Test
    @DisplayName("Player with debt started on day 0, current day 10 → 10 days elapsed")
    void testDaysSinceDebtStart_10DaysElapsed() throws Exception {
        UUID player = UUID.randomUUID();
        setCurrentDay(10L);
        setDebtStartDay(player, 0L);
        assertThat(overdraftManager.getDaysSinceDebtStart(player)).isEqualTo(10);
    }

    @Test
    @DisplayName("Player with no debt should have 28 days until prison")
    void testDaysUntilPrison_NoDebt_Returns28() {
        UUID player = UUID.randomUUID();
        assertThat(overdraftManager.getDaysUntilPrison(player)).isEqualTo(28);
    }

    @Test
    @DisplayName("After 10 days of debt → 18 days until prison")
    void testDaysUntilPrison_After10Days_Returns18() throws Exception {
        UUID player = UUID.randomUUID();
        setCurrentDay(10L);
        setDebtStartDay(player, 0L);
        assertThat(overdraftManager.getDaysUntilPrison(player)).isEqualTo(18);
    }

    @Test
    @DisplayName("After 28 days of debt → 0 days until prison (should already be in prison)")
    void testDaysUntilPrison_After28Days_ReturnsZero() throws Exception {
        UUID player = UUID.randomUUID();
        setCurrentDay(28L);
        setDebtStartDay(player, 0L);
        assertThat(overdraftManager.getDaysUntilPrison(player)).isEqualTo(0);
    }

    @Test
    @DisplayName("Player with no debt should have 7 days until auto-repay")
    void testDaysUntilAutoRepay_NoDebt_Returns7() {
        UUID player = UUID.randomUUID();
        assertThat(overdraftManager.getDaysUntilAutoRepay(player)).isEqualTo(7);
    }

    @Test
    @DisplayName("After 3 days of debt → 4 days until auto-repay")
    void testDaysUntilAutoRepay_After3Days_Returns4() throws Exception {
        UUID player = UUID.randomUUID();
        setCurrentDay(3L);
        setDebtStartDay(player, 0L);
        assertThat(overdraftManager.getDaysUntilAutoRepay(player)).isEqualTo(4);
    }

    @Test
    @DisplayName("After 7 days of debt → 0 days until auto-repay")
    void testDaysUntilAutoRepay_After7Days_ReturnsZero() throws Exception {
        UUID player = UUID.randomUUID();
        setCurrentDay(7L);
        setDebtStartDay(player, 0L);
        assertThat(overdraftManager.getDaysUntilAutoRepay(player)).isEqualTo(0);
    }
}
