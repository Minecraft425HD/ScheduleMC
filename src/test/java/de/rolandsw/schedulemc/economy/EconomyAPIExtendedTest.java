package de.rolandsw.schedulemc.economy;

import de.rolandsw.schedulemc.api.impl.EconomyAPIImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for EconomyAPIImpl - Extended API (v3.2.0)
 *
 * Tests cover:
 * - canAfford()
 * - batchTransfer()
 * - getTopBalances()
 * - getAccountCount()
 * - getTotalMoneyInCirculation()
 * - getTransactionHistory() (null/invalid parameter guards)
 */
class EconomyAPIExtendedTest {

    @TempDir
    Path tempDir;

    private EconomyAPIImpl api;
    private UUID player1;
    private UUID player2;
    private UUID player3;

    @BeforeEach
    void setUp() throws Exception {
        player1 = UUID.randomUUID();
        player2 = UUID.randomUUID();
        player3 = UUID.randomUUID();

        // Reset EconomyManager state
        resetEconomyManager();

        // Point EconomyManager to temp dir to avoid real file I/O
        File tempFile = tempDir.resolve("test_economy_api.json").toFile();
        EconomyManager.setFile(tempFile);

        // Set up accounts directly
        createAccountDirectly(player1, 10_000.0);
        createAccountDirectly(player2, 5_000.0);
        createAccountDirectly(player3, 1_000.0);

        api = new EconomyAPIImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetEconomyManager();
    }

    private void resetEconomyManager() throws Exception {
        Field balancesField = EconomyManager.class.getDeclaredField("balances");
        balancesField.setAccessible(true);
        Map<UUID, Double> balances = (Map<UUID, Double>) balancesField.get(null);
        balances.clear();

        Field needsSaveField = EconomyManager.class.getDeclaredField("needsSave");
        needsSaveField.setAccessible(true);
        needsSaveField.set(null, false);
    }

    private void createAccountDirectly(UUID uuid, double balance) throws Exception {
        Field balancesField = EconomyManager.class.getDeclaredField("balances");
        balancesField.setAccessible(true);
        Map<UUID, Double> balances = (Map<UUID, Double>) balancesField.get(null);
        balances.put(uuid, balance);
    }

    // ==================== canAfford() ====================

    @Test
    @DisplayName("canAfford should return true when balance >= amount")
    void testCanAfford_SufficientBalance_ReturnsTrue() {
        assertThat(api.canAfford(player1, 5_000.0)).isTrue();
    }

    @Test
    @DisplayName("canAfford should return true for exact balance")
    void testCanAfford_ExactBalance_ReturnsTrue() {
        assertThat(api.canAfford(player1, 10_000.0)).isTrue();
    }

    @Test
    @DisplayName("canAfford should return false when balance < amount")
    void testCanAfford_InsufficientBalance_ReturnsFalse() {
        assertThat(api.canAfford(player1, 15_000.0)).isFalse();
    }

    @Test
    @DisplayName("canAfford with zero amount should always return true")
    void testCanAfford_ZeroAmount_ReturnsTrue() {
        assertThat(api.canAfford(player3, 0.0)).isTrue();
    }

    @Test
    @DisplayName("canAfford with null UUID should throw IllegalArgumentException")
    void testCanAfford_NullUUID_Throws() {
        assertThatThrownBy(() -> api.canAfford(null, 100.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("canAfford with negative amount should throw IllegalArgumentException")
    void testCanAfford_NegativeAmount_Throws() {
        assertThatThrownBy(() -> api.canAfford(player1, -1.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== getAccountCount() ====================

    @Test
    @DisplayName("getAccountCount should return number of registered accounts")
    void testGetAccountCount_ReturnsCorrect() {
        assertThat(api.getAccountCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("getAccountCount should reflect deleted accounts")
    void testGetAccountCount_AfterDelete_Decreases() {
        api.deleteAccount(player3);
        assertThat(api.getAccountCount()).isEqualTo(2);
    }

    // ==================== getTotalMoneyInCirculation() ====================

    @Test
    @DisplayName("Total money should equal sum of all balances")
    void testGetTotalMoneyInCirculation_SumsAll() {
        // player1=10k, player2=5k, player3=1k → total=16k
        assertThat(api.getTotalMoneyInCirculation()).isCloseTo(16_000.0, within(0.01));
    }

    // ==================== getAllBalances() ====================

    @Test
    @DisplayName("getAllBalances should contain all registered players")
    void testGetAllBalances_ContainsAllPlayers() {
        Map<UUID, Double> balances = api.getAllBalances();
        assertThat(balances).containsKeys(player1, player2, player3);
    }

    @Test
    @DisplayName("getAllBalances should return unmodifiable view")
    void testGetAllBalances_IsUnmodifiable() {
        Map<UUID, Double> balances = api.getAllBalances();
        assertThatThrownBy(() -> balances.put(UUID.randomUUID(), 999.0))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // ==================== getTopBalances() ====================

    @Test
    @DisplayName("getTopBalances should return entries sorted richest first")
    void testGetTopBalances_CorrectOrder() {
        List<Map.Entry<UUID, Double>> top = api.getTopBalances(3);
        assertThat(top).hasSize(3);
        assertThat(top.get(0).getKey()).isEqualTo(player1); // 10k
        assertThat(top.get(1).getKey()).isEqualTo(player2); // 5k
        assertThat(top.get(2).getKey()).isEqualTo(player3); // 1k
    }

    @Test
    @DisplayName("getTopBalances with limit 1 should return only richest player")
    void testGetTopBalances_Limit1_ReturnsOnlyRichest() {
        List<Map.Entry<UUID, Double>> top = api.getTopBalances(1);
        assertThat(top).hasSize(1);
        assertThat(top.get(0).getKey()).isEqualTo(player1);
    }

    @Test
    @DisplayName("getTopBalances with limit 0 should throw IllegalArgumentException")
    void testGetTopBalances_LimitZero_Throws() {
        assertThatThrownBy(() -> api.getTopBalances(0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== batchTransfer() ====================

    @Test
    @DisplayName("batchTransfer should deduct total from sender and credit recipients")
    void testBatchTransfer_Success() {
        Map<UUID, Double> recipients = new LinkedHashMap<>();
        recipients.put(player2, 500.0);
        recipients.put(player3, 300.0);

        boolean result = api.batchTransfer(player1, recipients, "Test batch");

        assertThat(result).isTrue();
        // player1: 10000 - 500 - 300 = 9200 (but after two separate transfers via transfer())
        assertThat(api.getBalance(player1)).isCloseTo(9_200.0, within(0.01));
        assertThat(api.getBalance(player2)).isCloseTo(5_500.0, within(0.01));
        assertThat(api.getBalance(player3)).isCloseTo(1_300.0, within(0.01));
    }

    @Test
    @DisplayName("batchTransfer should return false when total exceeds sender balance")
    void testBatchTransfer_InsufficientFunds_ReturnsFalse() {
        Map<UUID, Double> recipients = new LinkedHashMap<>();
        recipients.put(player2, 6_000.0);
        recipients.put(player3, 6_000.0); // total 12k > player1 balance 10k

        boolean result = api.batchTransfer(player1, recipients, "Overdraft test");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("batchTransfer with null fromUUID should throw IllegalArgumentException")
    void testBatchTransfer_NullFromUUID_Throws() {
        assertThatThrownBy(() -> api.batchTransfer(null, Map.of(player2, 100.0), null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("batchTransfer with null recipients should throw IllegalArgumentException")
    void testBatchTransfer_NullRecipients_Throws() {
        assertThatThrownBy(() -> api.batchTransfer(player1, null, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== getTransactionHistory() ====================

    @Test
    @DisplayName("getTransactionHistory with null UUID should throw IllegalArgumentException")
    void testGetTransactionHistory_NullUUID_Throws() {
        assertThatThrownBy(() -> api.getTransactionHistory(null, 10))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getTransactionHistory with limit=0 should throw IllegalArgumentException")
    void testGetTransactionHistory_LimitZero_Throws() {
        assertThatThrownBy(() -> api.getTransactionHistory(player1, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getTransactionHistory with limit=-1 should throw IllegalArgumentException")
    void testGetTransactionHistory_LimitNegative_Throws() {
        assertThatThrownBy(() -> api.getTransactionHistory(player1, -1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getTransactionHistory returns empty list when TransactionHistory not initialized")
    void testGetTransactionHistory_NoServer_ReturnsEmpty() {
        // TransactionHistory.getInstance() returns null when no server is running
        List<String> history = api.getTransactionHistory(player1, 10);
        assertThat(history).isNotNull().isEmpty();
    }
}
