package de.rolandsw.schedulemc.managers;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Umfangreiche Tests für RentManager
 *
 * Getestete Funktionalität:
 * - Plot-Vermietung mit Zahlungsabwicklung
 * - Mietverlängerung
 * - Miet-Beendigung
 * - Automatische Räumung abgelaufener Mieten
 * - Abfragen (vermietete/verfügbare Plots)
 * - Benachrichtigungen bei ablaufenden Mieten
 * - Einkommensberechnung für Vermieter
 */
class RentManagerTest {

    private static final UUID TEST_RENTER = UUID.randomUUID();
    private static final UUID TEST_OWNER = UUID.randomUUID();
    private static final String TEST_PLOT_ID = "plot-123";
    private static final double RENT_PRICE_PER_DAY = 100.0;

    private PlotRegion mockPlot;

    @BeforeEach
    void setUp() {
        mockPlot = mock(PlotRegion.class);
        when(mockPlot.getPlotId()).thenReturn(TEST_PLOT_ID);
        when(mockPlot.getPlotName()).thenReturn("Test Plot");
        when(mockPlot.getRentPricePerDay()).thenReturn(RENT_PRICE_PER_DAY);
        when(mockPlot.getOwnerUUID()).thenReturn(TEST_OWNER.toString());
        when(mockPlot.getOwnerUUIDAsUUID()).thenReturn(TEST_OWNER);
    }

    // ============================================================================
    // Plot Rental Tests
    // ============================================================================

    @Nested
    @DisplayName("Plot Rental")
    class PlotRentalTests {

        @Test
        @DisplayName("Should successfully rent plot")
        void shouldSuccessfullyRentPlot() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                setupConfigMocks(configMock, true, true);
                when(mockPlot.isRented()).thenReturn(false);
                when(mockPlot.isForRent()).thenReturn(true);

                economyMock.when(() -> EconomyManager.getBalance(TEST_RENTER))
                    .thenReturn(1000.0);

                boolean result = RentManager.rentPlot(mockPlot, TEST_RENTER, 5);

                assertThat(result).isTrue();
                economyMock.verify(() -> EconomyManager.withdraw(TEST_RENTER, 500.0)); // 5 days * 100
                economyMock.verify(() -> EconomyManager.deposit(TEST_OWNER, 500.0));
                verify(mockPlot).setRenterUUID(TEST_RENTER.toString());
            }
        }

        @Test
        @DisplayName("Should fail if rent disabled in config")
        void shouldFailIfRentDisabledInConfig() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock, false, true);

                boolean result = RentManager.rentPlot(mockPlot, TEST_RENTER, 5);

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Should fail if plot already rented")
        void shouldFailIfPlotAlreadyRented() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock, true, true);
                when(mockPlot.isRented()).thenReturn(true);

                boolean result = RentManager.rentPlot(mockPlot, TEST_RENTER, 5);

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Should fail if plot not for rent")
        void shouldFailIfPlotNotForRent() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                setupConfigMocks(configMock, true, true);
                when(mockPlot.isRented()).thenReturn(false);
                when(mockPlot.isForRent()).thenReturn(false);

                boolean result = RentManager.rentPlot(mockPlot, TEST_RENTER, 5);

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Should fail if insufficient funds")
        void shouldFailIfInsufficientFunds() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                setupConfigMocks(configMock, true, true);
                when(mockPlot.isRented()).thenReturn(false);
                when(mockPlot.isForRent()).thenReturn(true);

                economyMock.when(() -> EconomyManager.getBalance(TEST_RENTER))
                    .thenReturn(100.0); // Not enough for 5 days

                boolean result = RentManager.rentPlot(mockPlot, TEST_RENTER, 5);

                assertThat(result).isFalse();
            }
        }

        @ParameterizedTest
        @CsvSource({
            "1, 100.0",    // 1 day
            "7, 700.0",    // 1 week
            "30, 3000.0",  // 1 month
            "365, 36500.0" // 1 year
        })
        @DisplayName("Should calculate correct rent for various durations")
        void shouldCalculateCorrectRentForVariousDurations(int days, double expectedCost) {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                setupConfigMocks(configMock, true, true);
                when(mockPlot.isRented()).thenReturn(false);
                when(mockPlot.isForRent()).thenReturn(true);

                economyMock.when(() -> EconomyManager.getBalance(TEST_RENTER))
                    .thenReturn(100000.0);

                RentManager.rentPlot(mockPlot, TEST_RENTER, days);

                economyMock.verify(() -> EconomyManager.withdraw(TEST_RENTER, expectedCost));
                economyMock.verify(() -> EconomyManager.deposit(TEST_OWNER, expectedCost));
            }
        }

        @Test
        @DisplayName("Should handle plot without owner")
        void shouldHandlePlotWithoutOwner() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                setupConfigMocks(configMock, true, true);
                when(mockPlot.isRented()).thenReturn(false);
                when(mockPlot.isForRent()).thenReturn(true);
                when(mockPlot.getOwnerUUIDAsUUID()).thenReturn(null);

                economyMock.when(() -> EconomyManager.getBalance(TEST_RENTER))
                    .thenReturn(1000.0);

                boolean result = RentManager.rentPlot(mockPlot, TEST_RENTER, 5);

                assertThat(result).isTrue();
                economyMock.verify(() -> EconomyManager.withdraw(TEST_RENTER, 500.0));
                economyMock.verify(() -> EconomyManager.deposit(any(UUID.class), anyDouble()), never());
            }
        }

        @Test
        @DisplayName("Should set rent end time correctly")
        void shouldSetRentEndTimeCorrectly() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                setupConfigMocks(configMock, true, true);
                when(mockPlot.isRented()).thenReturn(false);
                when(mockPlot.isForRent()).thenReturn(true);

                economyMock.when(() -> EconomyManager.getBalance(TEST_RENTER))
                    .thenReturn(1000.0);

                long before = System.currentTimeMillis();
                RentManager.rentPlot(mockPlot, TEST_RENTER, 7);
                long after = System.currentTimeMillis();

                // Verify rent end time is set (7 days from now)
                long expectedDuration = 7L * 24 * 60 * 60 * 1000;
                verify(mockPlot).setRentEndTime(longThat(time ->
                    time >= before + expectedDuration && time <= after + expectedDuration + 1000
                ));
            }
        }
    }

    // ============================================================================
    // Rent Extension Tests
    // ============================================================================

    @Nested
    @DisplayName("Rent Extension")
    class RentExtensionTests {

        @Test
        @DisplayName("Should extend existing rent")
        void shouldExtendExistingRent() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                when(mockPlot.isRented()).thenReturn(true);
                when(mockPlot.getRenterUUID()).thenReturn(TEST_RENTER.toString());
                when(mockPlot.getRentEndTime()).thenReturn(System.currentTimeMillis() + 100000);

                economyMock.when(() -> EconomyManager.getBalance(TEST_RENTER))
                    .thenReturn(1000.0);

                boolean result = RentManager.extendRent(mockPlot, TEST_RENTER, 3);

                assertThat(result).isTrue();
                economyMock.verify(() -> EconomyManager.withdraw(TEST_RENTER, 300.0)); // 3 days * 100
                economyMock.verify(() -> EconomyManager.deposit(TEST_OWNER, 300.0));
            }
        }

        @Test
        @DisplayName("Should fail extension if not rented")
        void shouldFailExtensionIfNotRented() {
            when(mockPlot.isRented()).thenReturn(false);

            boolean result = RentManager.extendRent(mockPlot, TEST_RENTER, 3);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail extension if not the renter")
        void shouldFailExtensionIfNotTheRenter() {
            when(mockPlot.isRented()).thenReturn(true);
            when(mockPlot.getRenterUUID()).thenReturn(UUID.randomUUID().toString()); // Different renter

            boolean result = RentManager.extendRent(mockPlot, TEST_RENTER, 3);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail extension if insufficient funds")
        void shouldFailExtensionIfInsufficientFunds() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                when(mockPlot.isRented()).thenReturn(true);
                when(mockPlot.getRenterUUID()).thenReturn(TEST_RENTER.toString());

                economyMock.when(() -> EconomyManager.getBalance(TEST_RENTER))
                    .thenReturn(50.0); // Not enough

                boolean result = RentManager.extendRent(mockPlot, TEST_RENTER, 3);

                assertThat(result).isFalse();
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 7, 14, 30})
        @DisplayName("Should extend rent for various durations")
        void shouldExtendRentForVariousDurations(int days) {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                when(mockPlot.isRented()).thenReturn(true);
                when(mockPlot.getRenterUUID()).thenReturn(TEST_RENTER.toString());
                long currentEndTime = System.currentTimeMillis() + 100000;
                when(mockPlot.getRentEndTime()).thenReturn(currentEndTime);

                economyMock.when(() -> EconomyManager.getBalance(TEST_RENTER))
                    .thenReturn(10000.0);

                RentManager.extendRent(mockPlot, TEST_RENTER, days);

                long expectedExtension = (long) days * 24 * 60 * 60 * 1000;
                verify(mockPlot).setRentEndTime(currentEndTime + expectedExtension);
            }
        }
    }

    // ============================================================================
    // Rent Cancellation Tests
    // ============================================================================

    @Nested
    @DisplayName("Rent Cancellation")
    class RentCancellationTests {

        @Test
        @DisplayName("Should cancel rent")
        void shouldCancelRent() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                when(mockPlot.isRented()).thenReturn(true);

                RentManager.cancelRent(mockPlot);

                verify(mockPlot).endRent();
                plotManagerMock.verify(PlotManager::savePlots);
            }
        }

        @Test
        @DisplayName("Should handle cancellation of non-rented plot gracefully")
        void shouldHandleCancellationOfNonRentedPlotGracefully() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                when(mockPlot.isRented()).thenReturn(false);

                assertThatCode(() -> RentManager.cancelRent(mockPlot))
                    .doesNotThrowAnyException();

                verify(mockPlot, never()).endRent();
            }
        }
    }

    // ============================================================================
    // Expired Rent Check Tests
    // ============================================================================

    @Nested
    @DisplayName("Expired Rent Check")
    class ExpiredRentCheckTests {

        @Test
        @DisplayName("Should evict expired rentals")
        void shouldEvictExpiredRentals() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                setupConfigMocks(configMock, true, true);

                PlotRegion expiredPlot = mock(PlotRegion.class);
                when(expiredPlot.isRentExpired()).thenReturn(true);
                when(expiredPlot.getPlotId()).thenReturn("expired-1");
                when(expiredPlot.getRenterUUID()).thenReturn(TEST_RENTER.toString());

                List<PlotRegion> plots = new ArrayList<>();
                plots.add(expiredPlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(plots);

                RentManager.checkExpiredRents();

                verify(expiredPlot).endRent();
                plotManagerMock.verify(PlotManager::savePlots);
            }
        }

        @Test
        @DisplayName("Should not evict if rent disabled")
        void shouldNotEvictIfRentDisabled() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                setupConfigMocks(configMock, false, true);

                RentManager.checkExpiredRents();

                plotManagerMock.verify(PlotManager::getPlots, never());
            }
        }

        @Test
        @DisplayName("Should not evict if auto-evict disabled")
        void shouldNotEvictIfAutoEvictDisabled() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                setupConfigMocks(configMock, true, false);

                RentManager.checkExpiredRents();

                plotManagerMock.verify(PlotManager::getPlots, never());
            }
        }

        @Test
        @DisplayName("Should handle mixed expired and active rentals")
        void shouldHandleMixedExpiredAndActiveRentals() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {

                setupConfigMocks(configMock, true, true);

                PlotRegion expiredPlot = mock(PlotRegion.class);
                when(expiredPlot.isRentExpired()).thenReturn(true);

                PlotRegion activePlot = mock(PlotRegion.class);
                when(activePlot.isRentExpired()).thenReturn(false);

                List<PlotRegion> plots = new ArrayList<>();
                plots.add(expiredPlot);
                plots.add(activePlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(plots);

                RentManager.checkExpiredRents();

                verify(expiredPlot).endRent();
                verify(activePlot, never()).endRent();
            }
        }
    }

    // ============================================================================
    // Query Method Tests
    // ============================================================================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("getRentedPlots should return rented plots")
        void getRentedPlotsShouldReturnRentedPlots() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                PlotRegion rentedPlot = mock(PlotRegion.class);
                when(rentedPlot.isRented()).thenReturn(true);

                PlotRegion unrentedPlot = mock(PlotRegion.class);
                when(unrentedPlot.isRented()).thenReturn(false);

                List<PlotRegion> allPlots = new ArrayList<>();
                allPlots.add(rentedPlot);
                allPlots.add(unrentedPlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(allPlots);

                List<PlotRegion> rented = RentManager.getRentedPlots();

                assertThat(rented).hasSize(1);
                assertThat(rented).contains(rentedPlot);
            }
        }

        @Test
        @DisplayName("getAvailableRentPlots should return available plots")
        void getAvailableRentPlotsShouldReturnAvailablePlots() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                PlotRegion availablePlot = mock(PlotRegion.class);
                when(availablePlot.isForRent()).thenReturn(true);
                when(availablePlot.isRented()).thenReturn(false);

                PlotRegion rentedPlot = mock(PlotRegion.class);
                when(rentedPlot.isForRent()).thenReturn(true);
                when(rentedPlot.isRented()).thenReturn(true);

                PlotRegion notForRentPlot = mock(PlotRegion.class);
                when(notForRentPlot.isForRent()).thenReturn(false);

                List<PlotRegion> allPlots = new ArrayList<>();
                allPlots.add(availablePlot);
                allPlots.add(rentedPlot);
                allPlots.add(notForRentPlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(allPlots);

                List<PlotRegion> available = RentManager.getAvailableRentPlots();

                assertThat(available).hasSize(1);
                assertThat(available).contains(availablePlot);
            }
        }

        @Test
        @DisplayName("getPlayerRentedPlots should return player's rentals")
        void getPlayerRentedPlotsShouldReturnPlayersRentals() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                PlotRegion playerPlot = mock(PlotRegion.class);
                when(playerPlot.isRented()).thenReturn(true);
                when(playerPlot.getRenterUUID()).thenReturn(TEST_RENTER.toString());

                PlotRegion otherPlot = mock(PlotRegion.class);
                when(otherPlot.isRented()).thenReturn(true);
                when(otherPlot.getRenterUUID()).thenReturn(UUID.randomUUID().toString());

                List<PlotRegion> allPlots = new ArrayList<>();
                allPlots.add(playerPlot);
                allPlots.add(otherPlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(allPlots);

                List<PlotRegion> playerRentals = RentManager.getPlayerRentedPlots(TEST_RENTER);

                assertThat(playerRentals).hasSize(1);
                assertThat(playerRentals).contains(playerPlot);
            }
        }
    }

    // ============================================================================
    // Notification Tests
    // ============================================================================

    @Nested
    @DisplayName("Expiring Rent Notifications")
    class NotificationTests {

        @Test
        @DisplayName("Should notify for rentals expiring in 24 hours")
        void shouldNotifyForRentalsExpiringIn24Hours() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockPlayer.getUUID()).thenReturn(TEST_RENTER);

                PlotRegion expiringPlot = mock(PlotRegion.class);
                when(expiringPlot.isRented()).thenReturn(true);
                when(expiringPlot.getRenterUUID()).thenReturn(TEST_RENTER.toString());
                when(expiringPlot.getRentHoursLeft()).thenReturn(20L); // 20 hours left
                when(expiringPlot.getPlotName()).thenReturn("Expiring Plot");

                List<PlotRegion> allPlots = new ArrayList<>();
                allPlots.add(expiringPlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(allPlots);

                RentManager.notifyExpiringRents(mockPlayer);

                verify(mockPlayer).sendSystemMessage(any(Component.class));
            }
        }

        @Test
        @DisplayName("Should not notify for rentals with more than 24 hours")
        void shouldNotNotifyForRentalsWithMoreThan24Hours() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockPlayer.getUUID()).thenReturn(TEST_RENTER);

                PlotRegion safePlot = mock(PlotRegion.class);
                when(safePlot.isRented()).thenReturn(true);
                when(safePlot.getRenterUUID()).thenReturn(TEST_RENTER.toString());
                when(safePlot.getRentHoursLeft()).thenReturn(48L); // 48 hours left

                List<PlotRegion> allPlots = new ArrayList<>();
                allPlots.add(safePlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(allPlots);

                RentManager.notifyExpiringRents(mockPlayer);

                verify(mockPlayer, never()).sendSystemMessage(any(Component.class));
            }
        }

        @Test
        @DisplayName("Should not notify for expired rentals")
        void shouldNotNotifyForExpiredRentals() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockPlayer.getUUID()).thenReturn(TEST_RENTER);

                PlotRegion expiredPlot = mock(PlotRegion.class);
                when(expiredPlot.isRented()).thenReturn(true);
                when(expiredPlot.getRenterUUID()).thenReturn(TEST_RENTER.toString());
                when(expiredPlot.getRentHoursLeft()).thenReturn(0L);

                List<PlotRegion> allPlots = new ArrayList<>();
                allPlots.add(expiredPlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(allPlots);

                RentManager.notifyExpiringRents(mockPlayer);

                verify(mockPlayer, never()).sendSystemMessage(any(Component.class));
            }
        }
    }

    // ============================================================================
    // Income Calculation Tests
    // ============================================================================

    @Nested
    @DisplayName("Rent Income Calculation")
    class IncomeCalculationTests {

        @Test
        @DisplayName("Should calculate rent income for owner")
        void shouldCalculateRentIncomeForOwner() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                PlotRegion ownedRentedPlot = mock(PlotRegion.class);
                when(ownedRentedPlot.getOwnerUUID()).thenReturn(TEST_OWNER.toString());
                when(ownedRentedPlot.isRented()).thenReturn(true);
                when(ownedRentedPlot.getRentPricePerDay()).thenReturn(100.0);
                when(ownedRentedPlot.getRentEndTime())
                    .thenReturn(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)); // 7 days

                List<PlotRegion> allPlots = new ArrayList<>();
                allPlots.add(ownedRentedPlot);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(allPlots);

                double income = RentManager.calculateRentIncome(TEST_OWNER);

                // 7 days * 100€/day = 700€
                assertThat(income).isCloseTo(700.0, within(50.0)); // Allow some margin for timing
            }
        }

        @Test
        @DisplayName("Should return zero for owner with no rentals")
        void shouldReturnZeroForOwnerWithNoRentals() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                plotManagerMock.when(PlotManager::getPlots).thenReturn(new ArrayList<>());

                double income = RentManager.calculateRentIncome(TEST_OWNER);

                assertThat(income).isEqualTo(0.0);
            }
        }

        @Test
        @DisplayName("Should only count owner's rented plots")
        void shouldOnlyCountOwnersRentedPlots() {
            try (MockedStatic<PlotManager> plotManagerMock = mockStatic(PlotManager.class)) {
                PlotRegion ownedRented = mock(PlotRegion.class);
                when(ownedRented.getOwnerUUID()).thenReturn(TEST_OWNER.toString());
                when(ownedRented.isRented()).thenReturn(true);
                when(ownedRented.getRentPricePerDay()).thenReturn(100.0);
                when(ownedRented.getRentEndTime())
                    .thenReturn(System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000));

                PlotRegion otherOwned = mock(PlotRegion.class);
                when(otherOwned.getOwnerUUID()).thenReturn(UUID.randomUUID().toString());
                when(otherOwned.isRented()).thenReturn(true);

                List<PlotRegion> allPlots = new ArrayList<>();
                allPlots.add(ownedRented);
                allPlots.add(otherOwned);

                plotManagerMock.when(PlotManager::getPlots).thenReturn(allPlots);

                double income = RentManager.calculateRentIncome(TEST_OWNER);

                // Only count ownedRented: 5 days * 100€ = 500€
                assertThat(income).isCloseTo(500.0, within(50.0));
            }
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private void setupConfigMocks(MockedStatic<ModConfigHandler> configMock,
                                   boolean rentEnabled, boolean autoEvict) {
        ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
        configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);

        ForgeConfigSpec.BooleanValue rentEnabledConfig = mock(ForgeConfigSpec.BooleanValue.class);
        when(rentEnabledConfig.get()).thenReturn(rentEnabled);
        when(mockCommon.RENT_ENABLED).thenReturn(rentEnabledConfig);

        ForgeConfigSpec.BooleanValue autoEvictConfig = mock(ForgeConfigSpec.BooleanValue.class);
        when(autoEvictConfig.get()).thenReturn(autoEvict);
        when(mockCommon.AUTO_EVICT_EXPIRED).thenReturn(autoEvictConfig);
    }
}
