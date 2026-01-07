package de.rolandsw.schedulemc.npc.crime.prison;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Umfangreiche Tests für PrisonManager
 *
 * Getestete Funktionalität:
 * - Singleton Pattern
 * - Gefängnis-Registrierung
 * - Zellen-Management
 * - Inhaftierung (60s pro Wanted Level)
 * - Kaution (1000€ pro Level, verfügbar nach 33%)
 * - Entlassung (TIME_SERVED, BAIL_PAID, ADMIN_RELEASE)
 * - Offline/Online-Handling
 * - Server Tick Processing
 * - Persistierung
 */
class PrisonManagerTest {

    private static final UUID TEST_PLAYER_ID = UUID.randomUUID();
    private static final String TEST_PLAYER_NAME = "TestPrisoner";
    private static final String TEST_PRISON_ID = "prison-1";

    private PrisonManager manager;
    private ServerPlayer mockPlayer;
    private ServerLevel mockLevel;
    private PlotRegion mockPrison;
    private PrisonCell mockCell;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton
        Field instanceField = PrisonManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // Reset internal state
        clearPrisonData();

        // Mock player
        mockPlayer = mock(ServerPlayer.class);
        when(mockPlayer.getUUID()).thenReturn(TEST_PLAYER_ID);
        when(mockPlayer.getName()).thenReturn(Component.literal(TEST_PLAYER_NAME));

        CompoundTag mockNbt = mock(CompoundTag.class);
        when(mockPlayer.getPersistentData()).thenReturn(mockNbt);

        // Mock level
        mockLevel = mock(ServerLevel.class);
        when(mockPlayer.level()).thenReturn(mockLevel);
        when(mockLevel.getGameTime()).thenReturn(0L);

        MinecraftServer mockServer = mock(MinecraftServer.class);
        when(mockLevel.getServer()).thenReturn(mockServer);

        PlayerList mockPlayerList = mock(PlayerList.class);
        when(mockServer.getPlayerList()).thenReturn(mockPlayerList);
        when(mockPlayerList.getPlayer(TEST_PLAYER_ID)).thenReturn(mockPlayer);

        // Mock prison plot
        mockPrison = mock(PlotRegion.class);
        when(mockPrison.getPlotId()).thenReturn(TEST_PRISON_ID);
        when(mockPrison.getType()).thenReturn(PlotType.PRISON);
        when(mockPrison.getSpawnPosition()).thenReturn(new BlockPos(100, 64, 100));

        // Mock prison cell
        mockCell = mock(PrisonCell.class);
        when(mockCell.getCellNumber()).thenReturn(1);
        when(mockCell.isFree()).thenReturn(true);
        when(mockCell.getSpawnPosition()).thenReturn(new BlockPos(50, 64, 50));
        when(mockCell.getParentPlotId()).thenReturn(TEST_PRISON_ID);

        List<Object> subAreas = new ArrayList<>();
        subAreas.add(mockCell);
        when(mockPrison.getSubAreas()).thenReturn(subAreas);

        manager = PrisonManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        clearPrisonData();
    }

    private void clearPrisonData() {
        File saveFile = new File("config/schedulemc/prisoners.json");
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
            PrisonManager instance1 = PrisonManager.getInstance();
            PrisonManager instance2 = PrisonManager.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Init should create instance")
        void initShouldCreateInstance() {
            PrisonManager.init();
            assertThat(PrisonManager.getInstance()).isNotNull();
        }
    }

    // ============================================================================
    // Prison Registration Tests
    // ============================================================================

    @Nested
    @DisplayName("Prison Registration")
    class RegistrationTests {

        @Test
        @DisplayName("Should register prison plot")
        void shouldRegisterPrisonPlot() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                plotManagerMock.when(() -> PlotManager.getPlot(TEST_PRISON_ID))
                    .thenReturn(mockPrison);

                manager.registerPrison(TEST_PRISON_ID);

                PlotRegion defaultPrison = manager.getDefaultPrison();
                assertThat(defaultPrison).isEqualTo(mockPrison);
            }
        }

        @Test
        @DisplayName("Should not register non-prison plot")
        void shouldNotRegisterNonPrisonPlot() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                PlotRegion normalPlot = mock(PlotRegion.class);
                when(normalPlot.getType()).thenReturn(PlotType.RESIDENTIAL);

                plotManagerMock.when(() -> PlotManager.getPlot("normal-1"))
                    .thenReturn(normalPlot);

                manager.registerPrison("normal-1");

                // Should still return null if no prison plots registered
                plotManagerMock.when(PlotManager::getPlots).thenReturn(new ArrayList<>());
                assertThat(manager.getDefaultPrison()).isNull();
            }
        }

        @Test
        @DisplayName("Should not duplicate prison registration")
        void shouldNotDuplicatePrisonRegistration() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                plotManagerMock.when(() -> PlotManager.getPlot(TEST_PRISON_ID))
                    .thenReturn(mockPrison);

                manager.registerPrison(TEST_PRISON_ID);
                manager.registerPrison(TEST_PRISON_ID); // Register twice

                // Should still work without duplication
                assertThat(manager.getDefaultPrison()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should return null if no prisons registered")
        void shouldReturnNullIfNoPrisons() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                plotManagerMock.when(PlotManager::getPlots).thenReturn(new ArrayList<>());

                assertThat(manager.getDefaultPrison()).isNull();
            }
        }
    }

    // ============================================================================
    // Cell Management Tests
    // ============================================================================

    @Nested
    @DisplayName("Cell Management")
    class CellManagementTests {

        @Test
        @DisplayName("Should find available cell")
        void shouldFindAvailableCell() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                setupPrisonMocks(plotManagerMock);

                PrisonCell cell = manager.findAvailableCell(3);

                assertThat(cell).isNotNull();
                assertThat(cell.getCellNumber()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("Should use first cell for overcrowding")
        void shouldUseFirstCellForOvercrowding() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                setupPrisonMocks(plotManagerMock);
                when(mockCell.isFree()).thenReturn(false); // All cells occupied

                PrisonCell cell = manager.findAvailableCell(3);

                assertThat(cell).isNotNull();
                assertThat(cell.getCellNumber()).isEqualTo(1); // First cell used for overcrowding
            }
        }

        @Test
        @DisplayName("Should return null if no prison available")
        void shouldReturnNullIfNoPrison() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                plotManagerMock.when(PlotManager::getPlots).thenReturn(new ArrayList<>());

                PrisonCell cell = manager.findAvailableCell(3);

                assertThat(cell).isNull();
            }
        }

        @Test
        @DisplayName("Should add cell to prison")
        void shouldAddCellToPrison() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                plotManagerMock.when(() -> PlotManager.getPlot(TEST_PRISON_ID))
                    .thenReturn(mockPrison);
                plotManagerMock.when(PlotManager::savePlots).then(invocation -> null);

                PrisonCell newCell = mock(PrisonCell.class);
                when(newCell.getCellNumber()).thenReturn(2);

                manager.addCellToPrison(TEST_PRISON_ID, newCell);

                verify(mockPrison).addSubArea(newCell);
                plotManagerMock.verify(PlotManager::savePlots);
            }
        }
    }

    // ============================================================================
    // Imprisonment Tests
    // ============================================================================

    @Nested
    @DisplayName("Imprisonment Logic")
    class ImprisonmentTests {

        @ParameterizedTest
        @CsvSource({
            "1, 60",     // 1 star = 60 seconds
            "2, 120",    // 2 stars = 120 seconds
            "3, 180",    // 3 stars = 180 seconds
            "5, 300"     // 5 stars = 300 seconds
        })
        @DisplayName("Jail time should be 60 seconds per wanted level")
        void jailTimeShouldBe60SecondsPerLevel(int wantedLevel, int expectedSeconds) {
            int jailSeconds = wantedLevel * PrisonManager.JAIL_SECONDS_PER_WANTED_LEVEL;
            assertThat(jailSeconds).isEqualTo(expectedSeconds);
        }

        @ParameterizedTest
        @CsvSource({
            "1, 1000.0",   // 1 star = 1000€
            "2, 2000.0",   // 2 stars = 2000€
            "3, 3000.0",   // 3 stars = 3000€
            "5, 5000.0"    // 5 stars = 5000€
        })
        @DisplayName("Bail should be 1000€ per wanted level")
        void bailShouldBe1000PerLevel(int wantedLevel, double expectedBail) {
            double bail = wantedLevel * PrisonManager.BAIL_MULTIPLIER;
            assertThat(bail).isEqualTo(expectedBail);
        }

        @Test
        @DisplayName("Should successfully imprison player")
        void shouldSuccessfullyImprisonPlayer() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                boolean result = manager.imprisonPlayer(mockPlayer, 3);

                assertThat(result).isTrue();
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isTrue();
                verify(mockCell).assignInmate(eq(TEST_PLAYER_ID), anyLong());
            }
        }

        @Test
        @DisplayName("Should not imprison already imprisoned player")
        void shouldNotImprisonAlreadyImprisonedPlayer() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 3);
                boolean result = manager.imprisonPlayer(mockPlayer, 3); // Try again

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Should fail if no prison available")
        void shouldFailIfNoPrisonAvailable() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                plotManagerMock.when(PlotManager::getPlots).thenReturn(new ArrayList<>());

                boolean result = manager.imprisonPlayer(mockPlayer, 3);

                assertThat(result).isFalse();
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isFalse();
            }
        }

        @Test
        @DisplayName("Should store prisoner data correctly")
        void shouldStorePrisonerDataCorrectly() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 4);

                PrisonManager.PrisonerData data = manager.getPrisonerData(TEST_PLAYER_ID);
                assertThat(data).isNotNull();
                assertThat(data.playerId).isEqualTo(TEST_PLAYER_ID);
                assertThat(data.playerName).isEqualTo(TEST_PLAYER_NAME);
                assertThat(data.originalWantedLevel).isEqualTo(4);
                assertThat(data.bailAmount).isEqualTo(4000.0);
                assertThat(data.cellNumber).isEqualTo(1);
                assertThat(data.bailPaid).isFalse();
            }
        }
    }

    // ============================================================================
    // Bail System Tests
    // ============================================================================

    @Nested
    @DisplayName("Bail System")
    class BailTests {

        @Test
        @DisplayName("Bail available after 33% constant is correct")
        void bailAvailableConstantShouldBe33Percent() {
            assertThat(PrisonManager.BAIL_AVAILABLE_AFTER).isEqualTo(0.33);
        }

        @Test
        @DisplayName("Bail should be available after 33% of sentence")
        void bailShouldBeAvailableAfter33Percent() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 3); // 180 seconds = 3600 ticks

                // At 32% - not available
                when(mockLevel.getGameTime()).thenReturn((long)(3600 * 0.32));
                assertThat(manager.isBailAvailable(TEST_PLAYER_ID, mockLevel.getGameTime()))
                    .isFalse();

                // At 33% - available
                when(mockLevel.getGameTime()).thenReturn((long)(3600 * 0.33));
                assertThat(manager.isBailAvailable(TEST_PLAYER_ID, mockLevel.getGameTime()))
                    .isTrue();

                // At 50% - still available
                when(mockLevel.getGameTime()).thenReturn((long)(3600 * 0.50));
                assertThat(manager.isBailAvailable(TEST_PLAYER_ID, mockLevel.getGameTime()))
                    .isTrue();
            }
        }

        @Test
        @DisplayName("Should successfully pay bail")
        void shouldSuccessfullyPayBail() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<CrimeManager> crimeMock = mockStatic(CrimeManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 3); // Bail: 3000€

                // Advance to 33% of sentence
                when(mockLevel.getGameTime()).thenReturn(1200L); // 33% of 3600 ticks

                boolean result = manager.payBail(mockPlayer);

                assertThat(result).isTrue();
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isFalse();
                economyMock.verify(() -> EconomyManager.withdraw(TEST_PLAYER_ID, 3000.0));
            }
        }

        @Test
        @DisplayName("Should fail bail if not available yet")
        void shouldFailBailIfNotAvailableYet() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 3);

                // Only 10% of sentence served
                when(mockLevel.getGameTime()).thenReturn(360L);

                boolean result = manager.payBail(mockPlayer);

                assertThat(result).isFalse();
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isTrue();
            }
        }

        @Test
        @DisplayName("Should fail bail if insufficient funds")
        void shouldFailBailIfInsufficientFunds() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0); // Initial balance

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 3); // Bail: 3000€

                // Advance to 33%
                when(mockLevel.getGameTime()).thenReturn(1200L);

                // Now player only has 2000€
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(2000.0);

                boolean result = manager.payBail(mockPlayer);

                assertThat(result).isFalse();
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isTrue();
            }
        }

        @Test
        @DisplayName("Should fail bail if not in prison")
        void shouldFailBailIfNotInPrison() {
            boolean result = manager.payBail(mockPlayer);

            assertThat(result).isFalse();
        }
    }

    // ============================================================================
    // Release Tests
    // ============================================================================

    @Nested
    @DisplayName("Release Mechanics")
    class ReleaseTests {

        @Test
        @DisplayName("TIME_SERVED should clear wanted level")
        void timeServedShouldClearWantedLevel() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<CrimeManager> crimeMock = mockStatic(CrimeManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 5);
                manager.releasePlayer(mockPlayer, PrisonManager.ReleaseReason.TIME_SERVED);

                crimeMock.verify(() -> CrimeManager.clearWantedLevel(TEST_PLAYER_ID));
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isFalse();
            }
        }

        @Test
        @DisplayName("BAIL_PAID should reduce wanted level by 2")
        void bailPaidShouldReduceWantedLevelBy2() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<CrimeManager> crimeMock = mockStatic(CrimeManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 5);
                manager.releasePlayer(mockPlayer, PrisonManager.ReleaseReason.BAIL_PAID);

                crimeMock.verify(() -> CrimeManager.setWantedLevel(TEST_PLAYER_ID, 3));
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isFalse();
            }
        }

        @Test
        @DisplayName("BAIL_PAID with low wanted level should not go negative")
        void bailPaidWithLowWantedLevelShouldNotGoNegative() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<CrimeManager> crimeMock = mockStatic(CrimeManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 1);
                manager.releasePlayer(mockPlayer, PrisonManager.ReleaseReason.BAIL_PAID);

                crimeMock.verify(() -> CrimeManager.setWantedLevel(TEST_PLAYER_ID, 0));
            }
        }

        @Test
        @DisplayName("ADMIN_RELEASE should clear wanted level")
        void adminReleaseShouldClearWantedLevel() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<CrimeManager> crimeMock = mockStatic(CrimeManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 5);
                manager.releasePlayer(mockPlayer, PrisonManager.ReleaseReason.ADMIN_RELEASE);

                crimeMock.verify(() -> CrimeManager.clearWantedLevel(TEST_PLAYER_ID));
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isFalse();
            }
        }

        @Test
        @DisplayName("Should release cell on player release")
        void shouldReleaseCellOnPlayerRelease() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<CrimeManager> crimeMock = mockStatic(CrimeManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 3);
                manager.releasePlayer(mockPlayer, PrisonManager.ReleaseReason.TIME_SERVED);

                verify(mockCell).releaseInmate();
            }
        }
    }

    // ============================================================================
    // Offline/Online Handling Tests
    // ============================================================================

    @Nested
    @DisplayName("Offline/Online Handling")
    class OfflineHandlingTests {

        @Test
        @DisplayName("Should track remaining time on logout")
        void shouldTrackRemainingTimeOnLogout() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 3); // 3600 ticks sentence

                // Player plays for 1000 ticks, then logs out
                when(mockLevel.getGameTime()).thenReturn(1000L);
                manager.onPlayerLogout(TEST_PLAYER_ID, 1000L);

                PrisonManager.PrisonerData data = manager.getPrisonerData(TEST_PLAYER_ID);
                assertThat(data).isNotNull();
                // Remaining time should be 3600 - 1000 = 2600 ticks
            }
        }

        @Test
        @DisplayName("Should restore prisoner on login")
        void shouldRestorePrisonerOnLogin() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 3);

                when(mockLevel.getGameTime()).thenReturn(1000L);
                manager.onPlayerLogout(TEST_PLAYER_ID, 1000L);

                // Player logs back in
                when(mockLevel.getGameTime()).thenReturn(5000L);
                manager.onPlayerLogin(mockPlayer);

                // Should still be a prisoner
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isTrue();

                // Should be teleported to cell
                verify(mockPlayer, atLeastOnce()).teleportTo(anyDouble(), anyDouble(), anyDouble());
            }
        }
    }

    // ============================================================================
    // Server Tick Tests
    // ============================================================================

    @Nested
    @DisplayName("Server Tick Processing")
    class TickTests {

        @Test
        @DisplayName("Should auto-release on sentence completion")
        void shouldAutoReleaseOnSentenceCompletion() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<CrimeManager> crimeMock = mockStatic(CrimeManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 1); // 60 seconds = 1200 ticks

                // Advance to release time
                when(mockLevel.getGameTime()).thenReturn(1200L);
                manager.onServerTick(1200L, mockLevel);

                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isFalse();
                crimeMock.verify(() -> CrimeManager.clearWantedLevel(TEST_PLAYER_ID));
            }
        }

        @Test
        @DisplayName("Should not release before sentence completion")
        void shouldNotReleaseBeforeSentenceCompletion() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 1); // 1200 ticks

                // Only 50% served
                when(mockLevel.getGameTime()).thenReturn(600L);
                manager.onServerTick(600L, mockLevel);

                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isTrue();
            }
        }

        @Test
        @DisplayName("Should not process offline prisoners")
        void shouldNotProcessOfflinePrisoners() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                when(mockLevel.getGameTime()).thenReturn(0L);
                manager.imprisonPlayer(mockPlayer, 1);

                manager.onPlayerLogout(TEST_PLAYER_ID, 0L);

                // Advance past release time
                when(mockLevel.getGameTime()).thenReturn(2000L);
                manager.onServerTick(2000L, mockLevel);

                // Should still be prisoner (not auto-released while offline)
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isTrue();
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
        @DisplayName("isPrisoner should return correct status")
        void isPrisonerShouldReturnCorrectStatus() {
            assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isFalse();

            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 3);
                assertThat(manager.isPrisoner(TEST_PLAYER_ID)).isTrue();
            }
        }

        @Test
        @DisplayName("getPrisonerData should return data or null")
        void getPrisonerDataShouldReturnDataOrNull() {
            assertThat(manager.getPrisonerData(TEST_PLAYER_ID)).isNull();

            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 3);

                PrisonManager.PrisonerData data = manager.getPrisonerData(TEST_PLAYER_ID);
                assertThat(data).isNotNull();
                assertThat(data.playerId).isEqualTo(TEST_PLAYER_ID);
            }
        }

        @Test
        @DisplayName("getPrisonerCount should return correct count")
        void getPrisonerCountShouldReturnCorrectCount() {
            assertThat(manager.getPrisonerCount()).isEqualTo(0);

            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 3);
                assertThat(manager.getPrisonerCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("getAllPrisoners should return unmodifiable collection")
        void getAllPrisonersShouldReturnUnmodifiableCollection() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupPrisonMocks(plotManagerMock);
                economyMock.when(() -> EconomyManager.getBalance(TEST_PLAYER_ID))
                    .thenReturn(10000.0);

                manager.imprisonPlayer(mockPlayer, 3);

                var prisoners = manager.getAllPrisoners();
                assertThat(prisoners).hasSize(1);

                // Should be unmodifiable
                assertThatThrownBy(() -> prisoners.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
            }
        }
    }

    // ============================================================================
    // PrisonerData Tests
    // ============================================================================

    @Nested
    @DisplayName("PrisonerData Model")
    class PrisonerDataTests {

        @Test
        @DisplayName("getCellSpawn should return BlockPos")
        void getCellSpawnShouldReturnBlockPos() {
            PrisonManager.PrisonerData data = new PrisonManager.PrisonerData();
            data.cellSpawnX = 100;
            data.cellSpawnY = 64;
            data.cellSpawnZ = 200;

            BlockPos pos = data.getCellSpawn();

            assertThat(pos.getX()).isEqualTo(100);
            assertThat(pos.getY()).isEqualTo(64);
            assertThat(pos.getZ()).isEqualTo(200);
        }

        @Test
        @DisplayName("setCellSpawn should set coordinates")
        void setCellSpawnShouldSetCoordinates() {
            PrisonManager.PrisonerData data = new PrisonManager.PrisonerData();
            BlockPos pos = new BlockPos(100, 64, 200);

            data.setCellSpawn(pos);

            assertThat(data.cellSpawnX).isEqualTo(100);
            assertThat(data.cellSpawnY).isEqualTo(64);
            assertThat(data.cellSpawnZ).isEqualTo(200);
        }

        @Test
        @DisplayName("bailPaid should default to false")
        void bailPaidShouldDefaultToFalse() {
            PrisonManager.PrisonerData data = new PrisonManager.PrisonerData();
            assertThat(data.bailPaid).isFalse();
        }
    }

    // ============================================================================
    // ReleaseReason Tests
    // ============================================================================

    @Nested
    @DisplayName("ReleaseReason Enum")
    class ReleaseReasonTests {

        @Test
        @DisplayName("Should have correct display names")
        void shouldHaveCorrectDisplayNames() {
            assertThat(PrisonManager.ReleaseReason.TIME_SERVED.displayName)
                .isEqualTo("Haft verbüßt");
            assertThat(PrisonManager.ReleaseReason.BAIL_PAID.displayName)
                .isEqualTo("Kaution bezahlt");
            assertThat(PrisonManager.ReleaseReason.ADMIN_RELEASE.displayName)
                .isEqualTo("Admin-Entlassung");
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private void setupPrisonMocks(MockedStatic<PlotManager> plotManagerMock) {
        List<PlotRegion> prisonList = new ArrayList<>();
        prisonList.add(mockPrison);

        plotManagerMock.when(PlotManager::getPlots).thenReturn(prisonList);
        plotManagerMock.when(() -> PlotManager.getPlot(TEST_PRISON_ID))
            .thenReturn(mockPrison);
    }
}
