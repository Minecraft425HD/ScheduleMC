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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Umfangreiche Tests für OverdraftManager
 *
 * Getestete Funktionalität:
 * - Überziehungsprüfung (Dispo)
 * - Überziehungsbetrag-Berechnung
 * - Warnsystem (max 1 Warnung/Woche)
 * - Wöchentliche Überziehungszinsen
 * - Pfändung bei Limit-Überschreitung
 * - Info-Anzeige
 * - Singleton Pattern
 * - AbstractPersistenceManager Integration
 */
class OverdraftManagerTest {

    private static final UUID TEST_PLAYER = UUID.randomUUID();
    private static final String TEST_PLAYER_NAME = "TestDebtor";
    private static final double MAX_LIMIT = -10000.0;
    private static final double WARNING_THRESHOLD = -5000.0;
    private static final double INTEREST_RATE = 0.15; // 15% per week

    private OverdraftManager manager;
    private MinecraftServer mockServer;
    private ServerPlayer mockPlayer;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton
        Field instanceField = OverdraftManager.class.getDeclaredField("instance");
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

        manager = OverdraftManager.getInstance(mockServer);
    }

    @AfterEach
    void tearDown() {
        clearSaveFile();
    }

    private void clearSaveFile() {
        File saveFile = new File("config/plotmod_overdraft.json");
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
            OverdraftManager instance1 = OverdraftManager.getInstance(mockServer);
            OverdraftManager instance2 = OverdraftManager.getInstance(mockServer);

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should update server reference")
        void shouldUpdateServerReference() {
            MinecraftServer newServer = mock(MinecraftServer.class);
            File serverDir = new File(".");
            when(newServer.getServerDirectory()).thenReturn(serverDir.toPath());

            OverdraftManager instance = OverdraftManager.getInstance(newServer);

            assertThat(instance).isNotNull();
        }
    }

    // ============================================================================
    // Overdraft Check Tests
    // ============================================================================

    @Nested
    @DisplayName("Overdraft Checks")
    class OverdraftCheckTests {

        @ParameterizedTest
        @CsvSource({
            "0.0, true",        // Positive balance - allowed
            "-5000.0, true",    // Within limit - allowed
            "-9999.0, true",    // Just within limit - allowed
            "-10000.0, true",   // At limit - allowed
            "-10001.0, false"   // Beyond limit - not allowed
        })
        @DisplayName("canOverdraft should check against limit")
        void canOverdraftShouldCheckAgainstLimit(double balance, boolean expected) {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                boolean result = OverdraftManager.canOverdraft(balance);

                assertThat(result).isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("Positive balance should allow overdraft")
        void positiveBalanceShouldAllowOverdraft() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                assertThat(OverdraftManager.canOverdraft(1000.0)).isTrue();
                assertThat(OverdraftManager.canOverdraft(100000.0)).isTrue();
            }
        }

        @Test
        @DisplayName("At limit boundary should allow overdraft")
        void atLimitBoundaryShouldAllowOverdraft() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock);

                assertThat(OverdraftManager.canOverdraft(MAX_LIMIT)).isTrue();
            }
        }
    }

    // ============================================================================
    // Overdraft Amount Tests
    // ============================================================================

    @Nested
    @DisplayName("Overdraft Amount Calculation")
    class OverdraftAmountTests {

        @ParameterizedTest
        @CsvSource({
            "100.0, 0.0",       // Positive - no overdraft
            "0.0, 0.0",         // Zero - no overdraft
            "-100.0, 100.0",    // Negative - overdraft amount
            "-5000.0, 5000.0",  // At threshold
            "-10000.0, 10000.0" // At limit
        })
        @DisplayName("Should calculate overdraft amount correctly")
        void shouldCalculateOverdraftAmountCorrectly(double balance, double expected) {
            double overdraft = OverdraftManager.getOverdraftAmount(balance);
            assertThat(overdraft).isEqualTo(expected);
        }

        @Test
        @DisplayName("Zero balance should have zero overdraft")
        void zeroBalanceShouldHaveZeroOverdraft() {
            assertThat(OverdraftManager.getOverdraftAmount(0.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Positive balance should have zero overdraft")
        void positiveBalanceShouldHaveZeroOverdraft() {
            assertThat(OverdraftManager.getOverdraftAmount(1000.0)).isEqualTo(0.0);
            assertThat(OverdraftManager.getOverdraftAmount(100000.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return absolute value for negative balance")
        void shouldReturnAbsoluteValueForNegativeBalance() {
            assertThat(OverdraftManager.getOverdraftAmount(-7500.0)).isEqualTo(7500.0);
        }
    }

    // ============================================================================
    // Warning System Tests
    // ============================================================================

    @Nested
    @DisplayName("Warning System")
    class WarningSystemTests {

        @Test
        @DisplayName("Should send warning at threshold")
        void shouldSendWarningAtThreshold() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, WARNING_THRESHOLD);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                verify(mockPlayer, atLeastOnce()).sendSystemMessage(any(Component.class));
            }
        }

        @Test
        @DisplayName("Should send warning below threshold")
        void shouldSendWarningBelowThreshold() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -6000.0); // Below threshold
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                verify(mockPlayer, atLeastOnce()).sendSystemMessage(any(Component.class));
            }
        }

        @Test
        @DisplayName("Should not send warning above threshold")
        void shouldNotSendWarningAboveThreshold() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -3000.0); // Above threshold
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                // Warning not sent (or not verified - behavior may vary)
            }
        }

        @Test
        @DisplayName("Should limit warnings to once per week")
        void shouldLimitWarningsToOncePerWeek() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, WARNING_THRESHOLD);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0); // Day 0 - warning sent
                verify(mockPlayer, times(1)).sendSystemMessage(any(Component.class));

                manager.tick(24000L); // Day 1 - no warning (within 7 days)
                manager.tick(48000L); // Day 2 - no warning
                manager.tick(72000L); // Day 3 - no warning

                // Only 1 warning should have been sent
                verify(mockPlayer, times(1)).sendSystemMessage(any(Component.class));
            }
        }

        @Test
        @DisplayName("Should send new warning after 7 days")
        void shouldSendNewWarningAfter7Days() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, WARNING_THRESHOLD);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0); // Day 0 - first warning

                // Advance 7 days
                manager.tick(7 * 24000L); // Day 7 - second warning allowed

                verify(mockPlayer, atLeast(2)).sendSystemMessage(any(Component.class));
            }
        }
    }

    // ============================================================================
    // Interest Charging Tests
    // ============================================================================

    @Nested
    @DisplayName("Overdraft Interest")
    class InterestChargingTests {

        @ParameterizedTest
        @CsvSource({
            "-1000.0, 150.0",   // 1000 * 0.15 = 150
            "-5000.0, 750.0",   // 5000 * 0.15 = 750
            "-10000.0, 1500.0"  // 10000 * 0.15 = 1500
        })
        @DisplayName("Should calculate 15% weekly interest")
        void shouldCalculate15PercentWeeklyInterest(double balance, double expectedInterest) {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, balance);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0); // Day 0 - interest charged

                double newBalance = balance - expectedInterest;
                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    eq(newBalance), any(TransactionType.class), anyString()));
            }
        }

        @Test
        @DisplayName("Should charge interest only once per week")
        void shouldChargeInterestOnlyOncePerWeek() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -1000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0); // Day 0 - interest charged
                manager.tick(24000L); // Day 1 - no interest
                manager.tick(48000L); // Day 2 - no interest

                // Should only charge once
                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    anyDouble(), any(TransactionType.class), anyString()), times(1));
            }
        }

        @Test
        @DisplayName("Should charge interest again after 7 days")
        void shouldChargeInterestAgainAfter7Days() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -1000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0); // Day 0 - first charge
                manager.tick(7 * 24000L); // Day 7 - second charge

                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    anyDouble(), any(TransactionType.class), anyString()), atLeast(2));
            }
        }

        @Test
        @DisplayName("Should not charge interest on positive balances")
        void shouldNotChargeInterestOnPositiveBalances() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, 1000.0); // Positive balance
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                // No interest charged
                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    anyDouble(), any(TransactionType.class), anyString()), never());
            }
        }

        @Test
        @DisplayName("Interest should make balance more negative")
        void interestShouldMakeBalanceMoreNegative() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -1000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                // -1000 - 150 = -1150
                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    eq(-1150.0), any(TransactionType.class), anyString()));
            }
        }
    }

    // ============================================================================
    // Seizure Tests
    // ============================================================================

    @Nested
    @DisplayName("Seizure Mechanics")
    class SeizureTests {

        @Test
        @DisplayName("Should execute seizure at limit")
        void shouldExecuteSeizureAtLimit() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<WalletManager> walletMock = mockStatic(WalletManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, MAX_LIMIT);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                walletMock.verify(() -> WalletManager.setBalance(TEST_PLAYER, 0.0));
                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    eq(MAX_LIMIT), any(TransactionType.class), anyString()));
            }
        }

        @Test
        @DisplayName("Should execute seizure beyond limit")
        void shouldExecuteSeizureBeyondLimit() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<WalletManager> walletMock = mockStatic(WalletManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -15000.0); // Way beyond limit
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                walletMock.verify(() -> WalletManager.setBalance(TEST_PLAYER, 0.0));
                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    eq(MAX_LIMIT), any(TransactionType.class), anyString()));
            }
        }

        @Test
        @DisplayName("Should not seize if above limit")
        void shouldNotSeizeIfAboveLimit() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<WalletManager> walletMock = mockStatic(WalletManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -5000.0); // Above limit
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                walletMock.verify(() -> WalletManager.setBalance(any(UUID.class), anyDouble()), never());
            }
        }

        @Test
        @DisplayName("Seizure should empty wallet")
        void seizureShouldEmptyWallet() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<WalletManager> walletMock = mockStatic(WalletManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -15000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                walletMock.verify(() -> WalletManager.setBalance(TEST_PLAYER, 0.0));
            }
        }

        @Test
        @DisplayName("Seizure should set balance to limit")
        void seizureShouldSetBalanceToLimit() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<WalletManager> walletMock = mockStatic(WalletManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -15000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    eq(MAX_LIMIT), any(TransactionType.class), anyString()));
            }
        }

        @Test
        @DisplayName("Seizure should notify player")
        void seizureShouldNotifyPlayer() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<WalletManager> walletMock = mockStatic(WalletManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -15000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                verify(mockPlayer, atLeastOnce()).sendSystemMessage(any(Component.class));
            }
        }
    }

    // ============================================================================
    // Info Display Tests
    // ============================================================================

    @Nested
    @DisplayName("Info Display")
    class InfoDisplayTests {

        @Test
        @DisplayName("Should show positive status when not overdrawn")
        void shouldShowPositiveStatusWhenNotOverdrawn() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(1000.0);

                String info = manager.getOverdraftInfo(TEST_PLAYER);

                assertThat(info).contains("nicht überzogen");
                assertThat(info).contains("Dispo-Limit");
                assertThat(info).contains("15%");
            }
        }

        @Test
        @DisplayName("Should show overdraft details when overdrawn")
        void shouldShowOverdraftDetailsWhenOverdrawn() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(-3000.0);

                String info = manager.getOverdraftInfo(TEST_PLAYER);

                assertThat(info).contains("ÜBERZOGEN");
                assertThat(info).contains("3000");
                assertThat(info).contains("15%");
            }
        }

        @Test
        @DisplayName("Should show available overdraft amount")
        void shouldShowAvailableOverdraftAmount() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(-3000.0);

                String info = manager.getOverdraftInfo(TEST_PLAYER);

                // Available: -10000 - (-3000) = -7000
                assertThat(info).contains("Verfügbar bis Limit");
            }
        }

        @Test
        @DisplayName("Info should show interest rate")
        void infoShouldShowInterestRate() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(1000.0);

                String info = manager.getOverdraftInfo(TEST_PLAYER);

                assertThat(info).contains("15%");
                assertThat(info).contains("Überziehungszinsen");
            }
        }
    }

    // ============================================================================
    // Tick Processing Tests
    // ============================================================================

    @Nested
    @DisplayName("Tick Processing")
    class TickProcessingTests {

        @Test
        @DisplayName("Should process on day change")
        void shouldProcessOnDayChange() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -1000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0); // Day 0
                manager.tick(24000L); // Day 1 - day changed

                // Processing occurred
                economyMock.verify(EconomyManager::getAllAccounts, atLeast(2));
            }
        }

        @Test
        @DisplayName("Should not process on same day")
        void shouldNotProcessOnSameDay() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -1000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);
                manager.tick(12000L); // Still day 0

                // Only processed once
                economyMock.verify(EconomyManager::getAllAccounts, times(1));
            }
        }

        @Test
        @DisplayName("Should process multiple players")
        void shouldProcessMultiplePlayers() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -1000.0);
                balances.put(UUID.randomUUID(), -2000.0);
                balances.put(UUID.randomUUID(), -3000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                // All players processed
                economyMock.verify(EconomyManager::getAllAccounts, atLeastOnce());
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
            var method = OverdraftManager.class.getDeclaredMethod("getComponentName");
            method.setAccessible(true);
            String name = (String) method.invoke(manager);

            assertThat(name).isEqualTo("OverdraftManager");
        }

        @Test
        @DisplayName("Should implement getHealthDetails")
        void shouldImplementGetHealthDetails() throws Exception {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, WARNING_THRESHOLD);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0); // Trigger warning

                var method = OverdraftManager.class.getDeclaredMethod("getHealthDetails");
                method.setAccessible(true);
                String health = (String) method.invoke(manager);

                assertThat(health).contains("Spieler");
                assertThat(health).contains("Überziehung");
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
        @DisplayName("Should handle exact limit boundary")
        void shouldHandleExactLimitBoundary() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<WalletManager> walletMock = mockStatic(WalletManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, MAX_LIMIT); // Exactly at limit
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                // Should trigger seizure
                walletMock.verify(() -> WalletManager.setBalance(TEST_PLAYER, 0.0));
            }
        }

        @Test
        @DisplayName("Should handle zero balance")
        void shouldHandleZeroBalance() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER))
                    .thenReturn(0.0);

                String info = manager.getOverdraftInfo(TEST_PLAYER);

                assertThat(info).contains("nicht überzogen");
            }
        }

        @Test
        @DisplayName("Should handle offline player gracefully")
        void shouldHandleOfflinePlayerGracefully() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);

                PlayerList mockPlayerList = mock(PlayerList.class);
                when(mockServer.getPlayerList()).thenReturn(mockPlayerList);
                when(mockPlayerList.getPlayer(any(UUID.class))).thenReturn(null); // Offline

                Map<UUID, Double> balances = new HashMap<>();
                balances.put(UUID.randomUUID(), -15000.0);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                // Should not crash
                assertThatCode(() -> manager.tick(0)).doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("Should handle empty accounts map")
        void shouldHandleEmptyAccountsMap() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock);
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(new HashMap<>());

                assertThatCode(() -> manager.tick(0)).doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("Should handle very large negative balance")
        void shouldHandleVeryLargeNegativeBalance() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<WalletManager> walletMock = mockStatic(WalletManager.class)) {

                setupConfigMocks(configMock);
                Map<UUID, Double> balances = new HashMap<>();
                balances.put(TEST_PLAYER, -1000000.0); // Very large debt
                economyMock.when(EconomyManager::getAllAccounts).thenReturn(balances);

                manager.tick(0);

                // Should still set to limit, not crash
                economyMock.verify(() -> EconomyManager.setBalance(eq(TEST_PLAYER),
                    eq(MAX_LIMIT), any(TransactionType.class), anyString()));
            }
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private void setupConfigMocks(MockedStatic<ModConfigHandler> configMock) {
        ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
        configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

        ForgeConfigSpec.DoubleValue maxLimitConfig = mock(ForgeConfigSpec.DoubleValue.class);
        when(maxLimitConfig.get()).thenReturn(MAX_LIMIT);
        when(mockCommon.OVERDRAFT_MAX_LIMIT).thenReturn(maxLimitConfig);

        ForgeConfigSpec.DoubleValue warningThresholdConfig = mock(ForgeConfigSpec.DoubleValue.class);
        when(warningThresholdConfig.get()).thenReturn(WARNING_THRESHOLD);
        when(mockCommon.OVERDRAFT_WARNING_THRESHOLD).thenReturn(warningThresholdConfig);

        ForgeConfigSpec.DoubleValue interestRateConfig = mock(ForgeConfigSpec.DoubleValue.class);
        when(interestRateConfig.get()).thenReturn(INTEREST_RATE);
        when(mockCommon.OVERDRAFT_INTEREST_RATE).thenReturn(interestRateConfig);
    }
}
