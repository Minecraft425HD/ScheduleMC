package de.rolandsw.schedulemc.economy;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.common.ForgeConfigSpec;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Umfangreiche Tests für SavingsAccountManager
 *
 * Getestete Funktionalität:
 * - Sparkonto-Erstellung mit Mindesteinlage
 * - Einzahlungen mit Limit-Prüfung
 * - Abhebungen mit Sperrfrist (4 Wochen)
 * - Vorzeitige Abhebung mit 10% Strafe
 * - Sparkonto-Schließung
 * - Zinsen (5% pro Woche)
 * - Singleton Pattern
 * - AbstractPersistenceManager Integration
 */
class SavingsAccountManagerTest {

    private static final UUID TEST_PLAYER = UUID.randomUUID();
    private static final String TEST_PLAYER_NAME = "TestSaver";
    private static final double MIN_DEPOSIT = 5000.0;
    private static final double MAX_PER_PLAYER = 500000.0;

    private SavingsAccountManager manager;
    private MinecraftServer mockServer;
    private ServerPlayer mockPlayer;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton
        Field instanceField = SavingsAccountManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // Clean save file
        clearSaveFile();

        // Mock server
        mockServer = mock(MinecraftServer.class);
        File serverDir = new File(".");
        when(mockServer.getServerDirectory()).thenReturn(serverDir.toPath());

        PlayerList mockPlayerList = mock(PlayerList.class);
        when(mockServer.getPlayerList()).thenReturn(mockPlayerList);

        // Mock player
        mockPlayer = mock(ServerPlayer.class);
        when(mockPlayer.getUUID()).thenReturn(TEST_PLAYER);
        when(mockPlayer.getName()).thenReturn(Component.literal(TEST_PLAYER_NAME));
        when(mockPlayerList.getPlayer(TEST_PLAYER)).thenReturn(mockPlayer);

        manager = SavingsAccountManager.getInstance(mockServer);
    }

    @AfterEach
    void tearDown() {
        clearSaveFile();
    }

    private void clearSaveFile() {
        File saveFile = new File("config/plotmod_savings.json");
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }

    // ============================================================================
    // Singleton Pattern Tests
    // ============================================================================

    @Nested
    @DisplayName("Singleton Pattern")
    class SingletonTests {

        @Test
        @DisplayName("Should return same instance")
        void shouldReturnSameInstance() {
            SavingsAccountManager instance1 = SavingsAccountManager.getInstance(mockServer);
            SavingsAccountManager instance2 = SavingsAccountManager.getInstance(mockServer);

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should update server reference")
        void shouldUpdateServerReference() {
            MinecraftServer newServer = mock(MinecraftServer.class);
            File serverDir = new File(".");
            when(newServer.getServerDirectory()).thenReturn(serverDir.toPath());

            SavingsAccountManager instance = SavingsAccountManager.getInstance(newServer);

            assertThat(instance).isNotNull();
        }
    }

    // ============================================================================
    // Account Creation Tests
    // ============================================================================

    @Nested
    @DisplayName("Account Creation")
    class AccountCreationTests {

        @Test
        @DisplayName("Should create savings account successfully")
        void shouldCreateSavingsAccountSuccessfully() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                boolean result = manager.createSavingsAccount(TEST_PLAYER, 10000.0);

                assertThat(result).isTrue();
                List<SavingsAccount> accounts = manager.getAccounts(TEST_PLAYER);
                assertThat(accounts).hasSize(1);
                assertThat(accounts.get(0).getBalance()).isEqualTo(10000.0);
            }
        }

        @Test
        @DisplayName("Should fail if deposit below minimum")
        void shouldFailIfDepositBelowMinimum() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                boolean result = manager.createSavingsAccount(TEST_PLAYER, 4000.0);

                assertThat(result).isFalse();
                assertThat(manager.getAccounts(TEST_PLAYER)).isEmpty();
            }
        }

        @Test
        @DisplayName("Should fail if exceeds max per player")
        void shouldFailIfExceedsMaxPerPlayer() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                // Create account with max - 1000
                manager.createSavingsAccount(TEST_PLAYER, MAX_PER_PLAYER - 1000.0);

                // Try to create another with 2000 (would exceed max)
                boolean result = manager.createSavingsAccount(TEST_PLAYER, 2000.0);

                assertThat(result).isFalse();
                assertThat(manager.getAccounts(TEST_PLAYER)).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should fail if insufficient funds")
        void shouldFailIfInsufficientFunds() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(false);

                boolean result = manager.createSavingsAccount(TEST_PLAYER, 10000.0);

                assertThat(result).isFalse();
                assertThat(manager.getAccounts(TEST_PLAYER)).isEmpty();
            }
        }

        @ParameterizedTest
        @ValueSource(doubles = {5000.0, 10000.0, 50000.0, 100000.0})
        @DisplayName("Should create account with various valid amounts")
        void shouldCreateAccountWithVariousValidAmounts(double amount) {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(amount),
                    any(TransactionType.class), anyString())).thenReturn(true);

                boolean result = manager.createSavingsAccount(TEST_PLAYER, amount);

                assertThat(result).isTrue();
                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isEqualTo(amount);

                // Clear for next iteration
                clearSaveFile();
                manager.getAccounts(TEST_PLAYER).clear();
            }
        }
    }

    // ============================================================================
    // Deposit Tests
    // ============================================================================

    @Nested
    @DisplayName("Deposits")
    class DepositTests {

        @Test
        @DisplayName("Should deposit to savings account")
        void shouldDepositToSavingsAccount() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                boolean result = manager.depositToSavings(TEST_PLAYER, accountId, 5000.0);

                assertThat(result).isTrue();
                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isEqualTo(15000.0);
            }
        }

        @Test
        @DisplayName("Should fail deposit if account not found")
        void shouldFailDepositIfAccountNotFound() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                boolean result = manager.depositToSavings(TEST_PLAYER, "invalid-id", 5000.0);

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Should fail deposit if exceeds max per player")
        void shouldFailDepositIfExceedsMaxPerPlayer() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, MAX_PER_PLAYER - 1000.0);
                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                boolean result = manager.depositToSavings(TEST_PLAYER, accountId, 2000.0);

                assertThat(result).isFalse();
                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isEqualTo(MAX_PER_PLAYER - 1000.0);
            }
        }

        @Test
        @DisplayName("Should fail deposit if insufficient main account funds")
        void shouldFailDepositIfInsufficientFunds() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(5000.0),
                    any(TransactionType.class), anyString())).thenReturn(false);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                boolean result = manager.depositToSavings(TEST_PLAYER, accountId, 5000.0);

                assertThat(result).isFalse();
                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isEqualTo(10000.0);
            }
        }

        @Test
        @DisplayName("Should support partial account ID matching")
        void shouldSupportPartialAccountIdMatching() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                String fullAccountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();
                String partialId = fullAccountId.substring(0, 8);

                boolean result = manager.depositToSavings(TEST_PLAYER, partialId, 5000.0);

                assertThat(result).isTrue();
            }
        }
    }

    // ============================================================================
    // Withdrawal Tests
    // ============================================================================

    @Nested
    @DisplayName("Withdrawals")
    class WithdrawalTests {

        @Test
        @DisplayName("Should withdraw after lock period without penalty")
        void shouldWithdrawAfterLockPeriodWithoutPenalty() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<StateAccount> stateAccountMock = mockStatic(StateAccount.class)) {

                setupConfigMocks(configMock);
                StateAccount mockStateAccount = mock(StateAccount.class);
                stateAccountMock.when(() -> StateAccount.getInstance(mockServer))
                    .thenReturn(mockStateAccount);

                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0); // Day 0

                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                // Advance to day 28 (4 weeks = unlocked)
                manager.tick(28 * 24000L);

                boolean result = manager.withdrawFromSavings(TEST_PLAYER, accountId, 5000.0, false);

                assertThat(result).isTrue();
                economyMock.verify(() -> EconomyManager.deposit(eq(TEST_PLAYER), eq(5000.0),
                    any(TransactionType.class), anyString()));
                verify(mockStateAccount, never()).deposit(anyInt(), anyString()); // No penalty
            }
        }

        @Test
        @DisplayName("Should fail withdrawal before lock period without forced flag")
        void shouldFailWithdrawalBeforeLockPeriodWithoutForced() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0);

                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                // Try to withdraw on day 10 (before 28 days)
                manager.tick(10 * 24000L);

                boolean result = manager.withdrawFromSavings(TEST_PLAYER, accountId, 5000.0, false);

                assertThat(result).isFalse();
                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isEqualTo(10000.0);
            }
        }

        @Test
        @DisplayName("Should apply 10% penalty for forced early withdrawal")
        void shouldApply10PercentPenaltyForForcedEarlyWithdrawal() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<StateAccount> stateAccountMock = mockStatic(StateAccount.class)) {

                setupConfigMocks(configMock);
                StateAccount mockStateAccount = mock(StateAccount.class);
                stateAccountMock.when(() -> StateAccount.getInstance(mockServer))
                    .thenReturn(mockStateAccount);

                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0);

                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                // Forced withdrawal on day 10
                manager.tick(10 * 24000L);

                boolean result = manager.withdrawFromSavings(TEST_PLAYER, accountId, 5000.0, true);

                assertThat(result).isTrue();

                // 5000 - 10% = 4500 to player
                economyMock.verify(() -> EconomyManager.deposit(eq(TEST_PLAYER), eq(4500.0),
                    any(TransactionType.class), anyString()));

                // 500 penalty to state
                verify(mockStateAccount).deposit(eq(500), anyString());
            }
        }

        @ParameterizedTest
        @CsvSource({
            "1000.0, 900.0, 100",     // 10% of 1000
            "5000.0, 4500.0, 500",    // 10% of 5000
            "10000.0, 9000.0, 1000"   // 10% of 10000
        })
        @DisplayName("Early withdrawal penalty calculation")
        void earlyWithdrawalPenaltyCalculation(double amount, double expectedPayout, int expectedPenalty) {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<StateAccount> stateAccountMock = mockStatic(StateAccount.class)) {

                setupConfigMocks(configMock);
                StateAccount mockStateAccount = mock(StateAccount.class);
                stateAccountMock.when(() -> StateAccount.getInstance(mockServer))
                    .thenReturn(mockStateAccount);

                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(20000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 20000.0);
                manager.tick(0);

                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();
                manager.tick(10 * 24000L); // Day 10

                manager.withdrawFromSavings(TEST_PLAYER, accountId, amount, true);

                economyMock.verify(() -> EconomyManager.deposit(eq(TEST_PLAYER), eq(expectedPayout),
                    any(TransactionType.class), anyString()));
                verify(mockStateAccount).deposit(eq(expectedPenalty), anyString());

                // Clear for next iteration
                clearSaveFile();
                manager.getAccounts(TEST_PLAYER).clear();
            }
        }

        @Test
        @DisplayName("Should fail withdrawal if account not found")
        void shouldFailWithdrawalIfAccountNotFound() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                boolean result = manager.withdrawFromSavings(TEST_PLAYER, "invalid-id", 5000.0, false);

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Should fail withdrawal if insufficient balance")
        void shouldFailWithdrawalIfInsufficientBalance() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(28 * 24000L); // Unlocked

                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                boolean result = manager.withdrawFromSavings(TEST_PLAYER, accountId, 15000.0, false);

                assertThat(result).isFalse();
            }
        }
    }

    // ============================================================================
    // Account Closure Tests
    // ============================================================================

    @Nested
    @DisplayName("Account Closure")
    class AccountClosureTests {

        @Test
        @DisplayName("Should close account after lock period without penalty")
        void shouldCloseAccountAfterLockPeriodWithoutPenalty() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<StateAccount> stateAccountMock = mockStatic(StateAccount.class)) {

                setupConfigMocks(configMock);
                StateAccount mockStateAccount = mock(StateAccount.class);
                stateAccountMock.when(() -> StateAccount.getInstance(mockServer))
                    .thenReturn(mockStateAccount);

                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0);

                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                // Close after 28 days
                manager.tick(28 * 24000L);
                boolean result = manager.closeSavingsAccount(TEST_PLAYER, accountId);

                assertThat(result).isTrue();
                assertThat(manager.getAccounts(TEST_PLAYER)).isEmpty();

                economyMock.verify(() -> EconomyManager.deposit(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString()));
                verify(mockStateAccount, never()).deposit(anyInt(), anyString());
            }
        }

        @Test
        @DisplayName("Should apply 10% penalty for early account closure")
        void shouldApply10PercentPenaltyForEarlyAccountClosure() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<StateAccount> stateAccountMock = mockStatic(StateAccount.class)) {

                setupConfigMocks(configMock);
                StateAccount mockStateAccount = mock(StateAccount.class);
                stateAccountMock.when(() -> StateAccount.getInstance(mockServer))
                    .thenReturn(mockStateAccount);

                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0);

                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                // Close on day 10 (early)
                manager.tick(10 * 24000L);
                boolean result = manager.closeSavingsAccount(TEST_PLAYER, accountId);

                assertThat(result).isTrue();
                assertThat(manager.getAccounts(TEST_PLAYER)).isEmpty();

                // 10000 - 10% = 9000
                economyMock.verify(() -> EconomyManager.deposit(eq(TEST_PLAYER), eq(9000.0),
                    any(TransactionType.class), anyString()));
                verify(mockStateAccount).deposit(eq(1000), anyString());
            }
        }

        @Test
        @DisplayName("Should fail closure if account not found")
        void shouldFailClosureIfAccountNotFound() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                boolean result = manager.closeSavingsAccount(TEST_PLAYER, "invalid-id");

                assertThat(result).isFalse();
            }
        }
    }

    // ============================================================================
    // Interest Tests
    // ============================================================================

    @Nested
    @DisplayName("Interest Processing")
    class InterestTests {

        @Test
        @DisplayName("Should process 5% weekly interest")
        void shouldProcess5PercentWeeklyInterest() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0);

                // Advance 7 days (1 week)
                manager.tick(7 * 24000L);

                double expectedBalance = 10000.0 * 1.05; // 10500
                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isEqualTo(expectedBalance);
            }
        }

        @ParameterizedTest
        @CsvSource({
            "7, 10500.0",     // 1 week: 10000 * 1.05
            "14, 11025.0",    // 2 weeks: 10000 * 1.05^2
            "21, 11576.25"    // 3 weeks: 10000 * 1.05^3
        })
        @DisplayName("Compound interest over multiple weeks")
        void compoundInterestOverMultipleWeeks(int days, double expectedBalance) {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0);

                manager.tick(days * 24000L);

                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isCloseTo(expectedBalance, within(0.01));

                // Clear for next iteration
                clearSaveFile();
                manager.getAccounts(TEST_PLAYER).clear();
            }
        }

        @Test
        @DisplayName("Should not process interest on same day")
        void shouldNotProcessInterestOnSameDay() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(eq(TEST_PLAYER), eq(10000.0),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0);

                manager.tick(12000L); // Same day (half day)

                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isEqualTo(10000.0);
            }
        }
    }

    // ============================================================================
    // Query Tests
    // ============================================================================

    @Nested
    @DisplayName("Query Methods")
    class QueryTests {

        @Test
        @DisplayName("getAccounts should return player accounts")
        void getAccountsShouldReturnPlayerAccounts() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.createSavingsAccount(TEST_PLAYER, 15000.0);

                List<SavingsAccount> accounts = manager.getAccounts(TEST_PLAYER);

                assertThat(accounts).hasSize(2);
                assertThat(accounts.get(0).getBalance()).isEqualTo(10000.0);
                assertThat(accounts.get(1).getBalance()).isEqualTo(15000.0);
            }
        }

        @Test
        @DisplayName("getAccounts should return empty list for unknown player")
        void getAccountsShouldReturnEmptyListForUnknownPlayer() {
            List<SavingsAccount> accounts = manager.getAccounts(UUID.randomUUID());

            assertThat(accounts).isEmpty();
        }

        @Test
        @DisplayName("getMaxSavingsPerPlayer should return config value")
        void getMaxSavingsPerPlayerShouldReturnConfigValue() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                double max = SavingsAccountManager.getMaxSavingsPerPlayer();

                assertThat(max).isEqualTo(MAX_PER_PLAYER);
            }
        }

        @Test
        @DisplayName("getMinDeposit should return config value")
        void getMinDepositShouldReturnConfigValue() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                double min = SavingsAccountManager.getMinDeposit();

                assertThat(min).isEqualTo(MIN_DEPOSIT);
            }
        }
    }

    // ============================================================================
    // AbstractPersistenceManager Tests
    // ============================================================================

    @Nested
    @DisplayName("AbstractPersistenceManager Integration")
    class PersistenceTests {

        @Test
        @DisplayName("Should implement getComponentName")
        void shouldImplementGetComponentName() throws Exception {
            var method = SavingsAccountManager.class.getDeclaredMethod("getComponentName");
            method.setAccessible(true);
            String name = (String) method.invoke(manager);

            assertThat(name).isEqualTo("SavingsAccountManager");
        }

        @Test
        @DisplayName("Should implement getHealthDetails")
        void shouldImplementGetHealthDetails() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.createSavingsAccount(TEST_PLAYER, 15000.0);

                var method = SavingsAccountManager.class.getDeclaredMethod("getHealthDetails");
                method.setAccessible(true);
                String health = (String) method.invoke(manager);

                assertThat(health).contains("2");
                assertThat(health).contains("Sparkonten");
            }
        }
    }

    // ============================================================================
    // Edge Cases
    // ============================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle multiple accounts for same player")
        void shouldHandleMultipleAccountsForSamePlayer() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.createSavingsAccount(TEST_PLAYER, 20000.0);
                manager.createSavingsAccount(TEST_PLAYER, 30000.0);

                assertThat(manager.getAccounts(TEST_PLAYER)).hasSize(3);
            }
        }

        @Test
        @DisplayName("Should handle exact limit scenarios")
        void shouldHandleExactLimitScenarios() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                // Create account with exactly max amount
                boolean result = manager.createSavingsAccount(TEST_PLAYER, MAX_PER_PLAYER);

                assertThat(result).isTrue();
                assertThat(manager.getAccounts(TEST_PLAYER).get(0).getBalance())
                    .isEqualTo(MAX_PER_PLAYER);
            }
        }

        @Test
        @DisplayName("Should handle day 28 boundary correctly")
        void shouldHandleDay28BoundaryCorrectly() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.withdraw(any(UUID.class), anyDouble(),
                    any(TransactionType.class), anyString())).thenReturn(true);

                manager.createSavingsAccount(TEST_PLAYER, 10000.0);
                manager.tick(0);

                String accountId = manager.getAccounts(TEST_PLAYER).get(0).getAccountId();

                // Day 27 - still locked
                manager.tick(27 * 24000L);
                boolean lockedResult = manager.withdrawFromSavings(TEST_PLAYER, accountId, 1000.0, false);
                assertThat(lockedResult).isFalse();

                // Day 28 - unlocked
                manager.tick(28 * 24000L);
                boolean unlockedResult = manager.withdrawFromSavings(TEST_PLAYER, accountId, 1000.0, false);
                assertThat(unlockedResult).isTrue();
            }
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private void setupConfigMocks(MockedStatic<ModConfigHandler> configMock) {
        ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
        configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

        ForgeConfigSpec.DoubleValue minDepositConfig = mock(ForgeConfigSpec.DoubleValue.class);
        when(minDepositConfig.get()).thenReturn(MIN_DEPOSIT);
        when(mockCommon.SAVINGS_MIN_DEPOSIT).thenReturn(minDepositConfig);

        ForgeConfigSpec.DoubleValue maxPerPlayerConfig = mock(ForgeConfigSpec.DoubleValue.class);
        when(maxPerPlayerConfig.get()).thenReturn(MAX_PER_PLAYER);
        when(mockCommon.SAVINGS_MAX_PER_PLAYER).thenReturn(maxPerPlayerConfig);
    }
}
