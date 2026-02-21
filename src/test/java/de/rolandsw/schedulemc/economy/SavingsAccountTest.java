package de.rolandsw.schedulemc.economy;

import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for SavingsAccount (data model)
 *
 * Tests cover deposit and withdraw operations that do NOT require
 * ModConfigHandler.COMMON (Forge config). Methods that rely on config
 * (isUnlocked, calculateAndPayInterest, close) are excluded from unit tests
 * as Forge config is not available in the test environment.
 */
class SavingsAccountTest {

    private UUID testPlayer;
    private SavingsAccount account;

    @BeforeEach
    void setUp() {
        testPlayer = UUID.randomUUID();
        account = new SavingsAccount(testPlayer, 5000.0, 0L);
    }

    // ==================== Initial State Tests ====================

    @Test
    @DisplayName("Initial balance should equal constructor deposit")
    void testInitialBalance() {
        assertThat(account.getBalance()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("Player UUID should be stored correctly")
    void testPlayerUUID() {
        assertThat(account.getPlayerUUID()).isEqualTo(testPlayer);
    }

    @Test
    @DisplayName("Account ID should not be null or empty")
    void testAccountIdNotEmpty() {
        assertThat(account.getAccountId()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Created day should be stored correctly")
    void testCreatedDay() {
        assertThat(account.getCreatedDay()).isEqualTo(0L);
    }

    // ==================== Deposit Tests ====================

    @Test
    @DisplayName("Positive deposit should increase balance")
    void testDeposit_PositiveAmount_IncreasesBalance() {
        account.deposit(2000.0);
        assertThat(account.getBalance()).isEqualTo(7000.0);
    }

    @Test
    @DisplayName("Multiple deposits should accumulate correctly")
    void testDeposit_Multiple_Accumulates() {
        account.deposit(1000.0);
        account.deposit(500.0);
        account.deposit(250.0);
        assertThat(account.getBalance()).isEqualTo(6750.0);
    }

    @Test
    @DisplayName("Zero deposit should not change balance")
    void testDeposit_Zero_NoChange() {
        account.deposit(0.0);
        assertThat(account.getBalance()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("Negative deposit should be ignored")
    void testDeposit_NegativeAmount_Ignored() {
        account.deposit(-500.0);
        assertThat(account.getBalance()).isEqualTo(5000.0);
    }

    // ==================== Withdraw Tests ====================

    @Test
    @DisplayName("Withdraw of exact balance when unlocked should return true and zero balance")
    void testWithdraw_ExactBalance_Unlocked_ReturnsTrue() {
        // day=999 ensures account is unlocked (lock period in weeks, but we bypass with forced)
        boolean result = account.withdraw(5000.0, 0L, true);
        assertThat(result).isTrue();
        assertThat(account.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Withdraw more than balance should return false")
    void testWithdraw_MoreThanBalance_ReturnsFalse() {
        boolean result = account.withdraw(6000.0, 0L, true);
        assertThat(result).isFalse();
        assertThat(account.getBalance()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("Withdraw zero amount should return false")
    void testWithdraw_ZeroAmount_ReturnsFalse() {
        boolean result = account.withdraw(0.0, 0L, true);
        assertThat(result).isFalse();
        assertThat(account.getBalance()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("Withdraw negative amount should return false")
    void testWithdraw_NegativeAmount_ReturnsFalse() {
        boolean result = account.withdraw(-100.0, 0L, true);
        assertThat(result).isFalse();
        assertThat(account.getBalance()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("Partial withdraw when forced should reduce balance correctly")
    void testWithdraw_Partial_Forced_ReducesBalance() {
        boolean result = account.withdraw(2000.0, 0L, true);
        assertThat(result).isTrue();
        assertThat(account.getBalance()).isEqualTo(3000.0);
    }

    @Test
    @DisplayName("Second account instance should be independent")
    void testTwoAccountsAreIndependent() {
        SavingsAccount other = new SavingsAccount(testPlayer, 1000.0, 0L);
        account.deposit(500.0);
        assertThat(account.getBalance()).isEqualTo(5500.0);
        assertThat(other.getBalance()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Account IDs for two different instances should be unique")
    void testAccountIdUniqueness() {
        SavingsAccount other = new SavingsAccount(testPlayer, 1000.0, 0L);
        assertThat(account.getAccountId()).isNotEqualTo(other.getAccountId());
    }
}
