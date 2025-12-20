package de.rolandsw.schedulemc.economy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

/**
 * Unit Tests für WalletManager
 */
public class WalletManagerTest {

    private UUID player1;
    private UUID player2;
    private UUID player3;

    @BeforeEach
    public void setUp() {
        player1 = UUID.randomUUID();
        player2 = UUID.randomUUID();
        player3 = UUID.randomUUID();

        // Reset wallets by setting all to 0
        WalletManager.setBalance(player1, 0.0);
        WalletManager.setBalance(player2, 0.0);
        WalletManager.setBalance(player3, 0.0);
    }

    @Test
    public void testGetBalance_NewWallet_ReturnsZero() {
        UUID newPlayer = UUID.randomUUID();

        double balance = WalletManager.getBalance(newPlayer);

        assertThat(balance).isEqualTo(0.0);
    }

    @Test
    public void testSetBalance_Basic() {
        WalletManager.setBalance(player1, 1000.0);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(1000.0);
    }

    @Test
    public void testSetBalance_NegativeValue_ClampsToZero() {
        WalletManager.setBalance(player1, -500.0);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(0.0);
    }

    @Test
    public void testSetBalance_Zero() {
        WalletManager.setBalance(player1, 1000.0);
        WalletManager.setBalance(player1, 0.0);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(0.0);
    }

    @Test
    public void testSetBalance_LargeValue() {
        double largeAmount = 1_000_000.0;
        WalletManager.setBalance(player1, largeAmount);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(largeAmount);
    }

    @Test
    public void testSetBalance_Overwrite() {
        WalletManager.setBalance(player1, 500.0);
        assertThat(WalletManager.getBalance(player1)).isEqualTo(500.0);

        WalletManager.setBalance(player1, 1500.0);
        assertThat(WalletManager.getBalance(player1)).isEqualTo(1500.0);
    }

    @Test
    public void testAddMoney_ToEmptyWallet() {
        WalletManager.addMoney(player1, 250.0);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(250.0);
    }

    @Test
    public void testAddMoney_ToExistingWallet() {
        WalletManager.setBalance(player1, 500.0);

        WalletManager.addMoney(player1, 250.0);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(750.0);
    }

    @Test
    public void testAddMoney_Multiple() {
        WalletManager.addMoney(player1, 100.0);
        WalletManager.addMoney(player1, 200.0);
        WalletManager.addMoney(player1, 50.0);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(350.0);
    }

    @Test
    public void testAddMoney_Zero_DoesNothing() {
        WalletManager.setBalance(player1, 500.0);

        WalletManager.addMoney(player1, 0.0);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(500.0);
    }

    @Test
    public void testRemoveMoney_Success() {
        WalletManager.setBalance(player1, 1000.0);

        boolean success = WalletManager.removeMoney(player1, 300.0);

        assertThat(success).isTrue();
        assertThat(WalletManager.getBalance(player1)).isEqualTo(700.0);
    }

    @Test
    public void testRemoveMoney_InsufficientFunds() {
        WalletManager.setBalance(player1, 100.0);

        boolean success = WalletManager.removeMoney(player1, 500.0);

        assertThat(success).isFalse();
        assertThat(WalletManager.getBalance(player1)).isEqualTo(100.0); // Unchanged
    }

    @Test
    public void testRemoveMoney_ExactAmount() {
        WalletManager.setBalance(player1, 500.0);

        boolean success = WalletManager.removeMoney(player1, 500.0);

        assertThat(success).isTrue();
        assertThat(WalletManager.getBalance(player1)).isEqualTo(0.0);
    }

    @Test
    public void testRemoveMoney_FromEmptyWallet() {
        boolean success = WalletManager.removeMoney(player1, 100.0);

        assertThat(success).isFalse();
        assertThat(WalletManager.getBalance(player1)).isEqualTo(0.0);
    }

    @Test
    public void testRemoveMoney_Zero_AlwaysSucceeds() {
        WalletManager.setBalance(player1, 500.0);

        boolean success = WalletManager.removeMoney(player1, 0.0);

        assertThat(success).isTrue();
        assertThat(WalletManager.getBalance(player1)).isEqualTo(500.0);
    }

    @Test
    public void testMultipleWallets_Independent() {
        WalletManager.setBalance(player1, 1000.0);
        WalletManager.setBalance(player2, 2000.0);
        WalletManager.setBalance(player3, 3000.0);

        assertThat(WalletManager.getBalance(player1)).isEqualTo(1000.0);
        assertThat(WalletManager.getBalance(player2)).isEqualTo(2000.0);
        assertThat(WalletManager.getBalance(player3)).isEqualTo(3000.0);

        // Modify one wallet
        WalletManager.addMoney(player2, 500.0);

        // Others should be unchanged
        assertThat(WalletManager.getBalance(player1)).isEqualTo(1000.0);
        assertThat(WalletManager.getBalance(player2)).isEqualTo(2500.0);
        assertThat(WalletManager.getBalance(player3)).isEqualTo(3000.0);
    }

    @Test
    public void testComplexTransactions() {
        // Start with 1000
        WalletManager.setBalance(player1, 1000.0);

        // Add 500 -> 1500
        WalletManager.addMoney(player1, 500.0);
        assertThat(WalletManager.getBalance(player1)).isEqualTo(1500.0);

        // Remove 300 -> 1200
        WalletManager.removeMoney(player1, 300.0);
        assertThat(WalletManager.getBalance(player1)).isEqualTo(1200.0);

        // Add 800 -> 2000
        WalletManager.addMoney(player1, 800.0);
        assertThat(WalletManager.getBalance(player1)).isEqualTo(2000.0);

        // Remove 2000 -> 0
        WalletManager.removeMoney(player1, 2000.0);
        assertThat(WalletManager.getBalance(player1)).isEqualTo(0.0);
    }

    @Test
    public void testDecimalPrecision() {
        WalletManager.setBalance(player1, 123.45);
        WalletManager.addMoney(player1, 67.89);

        assertThat(WalletManager.getBalance(player1)).isCloseTo(191.34, within(0.001));
    }

    @Test
    public void testVerySmallAmounts() {
        WalletManager.setBalance(player1, 0.01);
        WalletManager.addMoney(player1, 0.01);

        assertThat(WalletManager.getBalance(player1)).isCloseTo(0.02, within(0.001));
    }

    @Test
    public void testRemoveMoney_SmallDifference_InsufficientFunds() {
        WalletManager.setBalance(player1, 100.0);

        // Try to remove slightly more
        boolean success = WalletManager.removeMoney(player1, 100.01);

        assertThat(success).isFalse();
        assertThat(WalletManager.getBalance(player1)).isEqualTo(100.0);
    }

    @Test
    public void testHealthStatus() {
        // Health status should be available
        assertThat(WalletManager.isHealthy()).isNotNull();
        assertThat(WalletManager.getHealthInfo()).isNotNull();
    }

    @Test
    public void testSequentialOperations() {
        // Simulate a player's wallet lifecycle
        UUID player = UUID.randomUUID();

        // Start empty
        assertThat(WalletManager.getBalance(player)).isEqualTo(0.0);

        // Receive starting money
        WalletManager.addMoney(player, 500.0);
        assertThat(WalletManager.getBalance(player)).isEqualTo(500.0);

        // Make a purchase
        boolean purchase1 = WalletManager.removeMoney(player, 150.0);
        assertThat(purchase1).isTrue();
        assertThat(WalletManager.getBalance(player)).isEqualTo(350.0);

        // Try to buy something too expensive
        boolean purchase2 = WalletManager.removeMoney(player, 500.0);
        assertThat(purchase2).isFalse();
        assertThat(WalletManager.getBalance(player)).isEqualTo(350.0);

        // Earn more money
        WalletManager.addMoney(player, 250.0);
        assertThat(WalletManager.getBalance(player)).isEqualTo(600.0);

        // Now can afford the expensive item
        boolean purchase3 = WalletManager.removeMoney(player, 500.0);
        assertThat(purchase3).isTrue();
        assertThat(WalletManager.getBalance(player)).isEqualTo(100.0);
    }
}
