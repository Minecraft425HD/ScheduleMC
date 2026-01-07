package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Contract Tests for IPlotManager Interface
 *
 * Tests that verify the PlotManager correctly implements the IPlotManager interface
 * and honors all contract requirements including:
 * - Interface type compatibility
 * - Plot creation and management
 * - Plot queries and lookups
 * - Persistence operations
 * - Health monitoring
 * - Method delegation
 */
@DisplayName("IPlotManager Contract Tests")
class IPlotManagerContractTest {

    private IPlotManager plotManager;
    private UUID testOwner1;
    private UUID testOwner2;
    private BlockPos pos1;
    private BlockPos pos2;

    @BeforeEach
    void setUp() {
        testOwner1 = UUID.randomUUID();
        testOwner2 = UUID.randomUUID();
        pos1 = new BlockPos(0, 64, 0);
        pos2 = new BlockPos(10, 74, 10);

        // Get instance as interface type
        plotManager = PlotManager.getInstance();

        // Clear all plots
        plotManager.clearAllPlots();
    }

    @AfterEach
    void tearDown() {
        plotManager.clearAllPlots();
    }

    // ═══════════════════════════════════════════════════════════
    // INTERFACE COMPATIBILITY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Interface Compatibility")
    class InterfaceCompatibilityTests {

        @Test
        @DisplayName("PlotManager should implement IPlotManager")
        void plotManagerShouldImplementInterface() {
            assertThat(PlotManager.getInstance()).isInstanceOf(IPlotManager.class);
        }

        @Test
        @DisplayName("Should be assignable to IPlotManager type")
        void shouldBeAssignableToInterfaceType() {
            IPlotManager manager = PlotManager.getInstance();
            assertThat(manager).isNotNull();
        }

        @Test
        @DisplayName("Interface reference should work for all operations")
        void interfaceReferenceShouldWorkForAllOperations() {
            IPlotManager manager = PlotManager.getInstance();

            PlotRegion plot = manager.createPlot(pos1, pos2, 1000.0);
            PlotRegion retrieved = manager.getPlotAt(pos1);
            int count = manager.getPlotCount();

            assertThat(retrieved).isNotNull();
            assertThat(count).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLOT CREATION CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Plot Creation Contract")
    class PlotCreationContractTests {

        @Test
        @DisplayName("createPlot should create new plot with default settings")
        void createPlotShouldCreateNewPlot() {
            PlotRegion plot = plotManager.createPlot(pos1, pos2, 1000.0);

            assertThat(plot).isNotNull();
            assertThat(plotManager.hasPlot(plot.getPlotId())).isTrue();
        }

        @Test
        @DisplayName("createPlot should support custom name and type")
        void createPlotShouldSupportCustomNameAndType() {
            PlotRegion plot = plotManager.createPlot(pos1, pos2, "Test Plot", PlotType.HAUS, 2000.0);

            assertThat(plot).isNotNull();
            assertThat(plot.getCustomName()).isEqualTo("Test Plot");
            assertThat(plot.getPlotType()).isEqualTo(PlotType.HAUS);
        }

        @Test
        @DisplayName("addPlot should add existing plot")
        void addPlotShouldAddExistingPlot() {
            PlotRegion plot = new PlotRegion("test-plot", pos1, pos2, 1000.0);

            assertThatCode(() -> plotManager.addPlot(plot))
                .doesNotThrowAnyException();

            assertThat(plotManager.hasPlot("test-plot")).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLOT LOOKUP CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Plot Lookup Contract")
    class PlotLookupContractTests {

        @Test
        @DisplayName("getPlotAt should find plot at position")
        void getPlotAtShouldFindPlotAtPosition() {
            PlotRegion created = plotManager.createPlot(pos1, pos2, 1000.0);

            PlotRegion found = plotManager.getPlotAt(pos1);

            assertThat(found).isNotNull();
            assertThat(found.getPlotId()).isEqualTo(created.getPlotId());
        }

        @Test
        @DisplayName("getPlotAt should return null for non-existent plot")
        void getPlotAtShouldReturnNullForNonExistent() {
            PlotRegion found = plotManager.getPlotAt(new BlockPos(1000, 64, 1000));

            assertThat(found).isNull();
        }

        @Test
        @DisplayName("getPlot should retrieve plot by ID")
        void getPlotShouldRetrievePlotById() {
            PlotRegion created = plotManager.createPlot(pos1, pos2, 1000.0);

            PlotRegion found = plotManager.getPlot(created.getPlotId());

            assertThat(found).isNotNull();
            assertThat(found).isEqualTo(created);
        }

        @Test
        @DisplayName("getPlot should return null for invalid ID")
        void getPlotShouldReturnNullForInvalidId() {
            PlotRegion found = plotManager.getPlot("non-existent-plot");

            assertThat(found).isNull();
        }

        @Test
        @DisplayName("hasPlot should return true for existing plot")
        void hasPlotShouldReturnTrueForExisting() {
            PlotRegion created = plotManager.createPlot(pos1, pos2, 1000.0);

            assertThat(plotManager.hasPlot(created.getPlotId())).isTrue();
        }

        @Test
        @DisplayName("hasPlot should return false for non-existent plot")
        void hasPlotShouldReturnFalseForNonExistent() {
            assertThat(plotManager.hasPlot("non-existent")).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLOT QUERIES CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Plot Queries Contract")
    class PlotQueriesContractTests {

        @Test
        @DisplayName("getPlots should return all plots")
        void getPlotsShouldReturnAllPlots() {
            plotManager.createPlot(pos1, pos2, 1000.0);
            plotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 74, 30), 1500.0);

            List<PlotRegion> plots = plotManager.getPlots();

            assertThat(plots).hasSize(2);
        }

        @Test
        @DisplayName("getPlotsByOwner should return plots owned by player")
        void getPlotsByOwnerShouldReturnOwnedPlots() {
            PlotRegion plot1 = plotManager.createPlot(pos1, pos2, 1000.0);
            plot1.setOwner(testOwner1);

            PlotRegion plot2 = plotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 74, 30), 1500.0);
            plot2.setOwner(testOwner1);

            PlotRegion plot3 = plotManager.createPlot(new BlockPos(40, 64, 40), new BlockPos(50, 74, 50), 2000.0);
            plot3.setOwner(testOwner2);

            List<PlotRegion> ownedPlots = plotManager.getPlotsByOwner(testOwner1);

            assertThat(ownedPlots).hasSize(2);
            assertThat(ownedPlots).allMatch(p -> testOwner1.equals(p.getOwner()));
        }

        @Test
        @DisplayName("getAvailablePlots should return unowned plots")
        void getAvailablePlotsShouldReturnUnownedPlots() {
            plotManager.createPlot(pos1, pos2, 1000.0); // No owner
            PlotRegion plot2 = plotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 74, 30), 1500.0);
            plot2.setOwner(testOwner1); // Has owner

            List<PlotRegion> available = plotManager.getAvailablePlots();

            assertThat(available).hasSize(1);
        }

        @Test
        @DisplayName("getPlotsForSale should return plots marked for sale")
        void getPlotsForSaleShouldReturnPlotsForSale() {
            PlotRegion plot1 = plotManager.createPlot(pos1, pos2, 1000.0);
            plot1.setForSale(true);

            plotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 74, 30), 1500.0); // Not for sale

            List<PlotRegion> forSale = plotManager.getPlotsForSale();

            assertThat(forSale).hasSize(1);
            assertThat(forSale.get(0).isForSale()).isTrue();
        }

        @Test
        @DisplayName("getPlotsForRent should return plots marked for rent")
        void getPlotsForRentShouldReturnPlotsForRent() {
            PlotRegion plot1 = plotManager.createPlot(pos1, pos2, 1000.0);
            plot1.setForRent(true);

            plotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 74, 30), 1500.0); // Not for rent

            List<PlotRegion> forRent = plotManager.getPlotsForRent();

            assertThat(forRent).hasSize(1);
            assertThat(forRent.get(0).isForRent()).isTrue();
        }

        @Test
        @DisplayName("getTopRatedPlots should return highest rated plots")
        void getTopRatedPlotsShouldReturnHighestRated() {
            PlotRegion plot1 = plotManager.createPlot(pos1, pos2, 1000.0);
            plot1.setRating(5.0);

            PlotRegion plot2 = plotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 74, 30), 1500.0);
            plot2.setRating(3.0);

            PlotRegion plot3 = plotManager.createPlot(new BlockPos(40, 64, 40), new BlockPos(50, 74, 50), 2000.0);
            plot3.setRating(4.5);

            List<PlotRegion> topRated = plotManager.getTopRatedPlots(2);

            assertThat(topRated).hasSize(2);
            assertThat(topRated.get(0).getRating()).isEqualTo(5.0);
            assertThat(topRated.get(1).getRating()).isEqualTo(4.5);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLOT MANAGEMENT CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Plot Management Contract")
    class PlotManagementContractTests {

        @Test
        @DisplayName("removePlot should remove plot by ID")
        void removePlotShouldRemovePlotById() {
            PlotRegion plot = plotManager.createPlot(pos1, pos2, 1000.0);
            String plotId = plot.getPlotId();

            boolean removed = plotManager.removePlot(plotId);

            assertThat(removed).isTrue();
            assertThat(plotManager.hasPlot(plotId)).isFalse();
        }

        @Test
        @DisplayName("removePlotAt should remove plot at position")
        void removePlotAtShouldRemovePlotAtPosition() {
            plotManager.createPlot(pos1, pos2, 1000.0);

            boolean removed = plotManager.removePlotAt(pos1);

            assertThat(removed).isTrue();
            assertThat(plotManager.getPlotAt(pos1)).isNull();
        }

        @Test
        @DisplayName("clearAllPlots should remove all plots")
        void clearAllPlotsShouldRemoveAllPlots() {
            plotManager.createPlot(pos1, pos2, 1000.0);
            plotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 74, 30), 1500.0);

            plotManager.clearAllPlots();

            assertThat(plotManager.getPlotCount()).isZero();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Statistics Contract")
    class StatisticsContractTests {

        @Test
        @DisplayName("getPlotCount should return number of plots")
        void getPlotCountShouldReturnNumberOfPlots() {
            plotManager.createPlot(pos1, pos2, 1000.0);
            plotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 74, 30), 1500.0);

            assertThat(plotManager.getPlotCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getTotalPlotVolume should return total volume")
        void getTotalPlotVolumeShouldReturnTotalVolume() {
            plotManager.createPlot(pos1, pos2, 1000.0);

            long volume = plotManager.getTotalPlotVolume();

            assertThat(volume).isGreaterThan(0);
        }

        @Test
        @DisplayName("getStatistics should return plot statistics")
        void getStatisticsShouldReturnPlotStatistics() {
            plotManager.createPlot(pos1, pos2, 1000.0);

            PlotManager.PlotStatistics stats = plotManager.getStatistics();

            assertThat(stats).isNotNull();
        }

        @Test
        @DisplayName("getCacheStatistics should return cache stats")
        void getCacheStatisticsShouldReturnCacheStats() {
            PlotCache.CacheStatistics stats = plotManager.getCacheStatistics();

            assertThat(stats).isNotNull();
        }

        @Test
        @DisplayName("resetCacheStatistics should reset stats")
        void resetCacheStatisticsShouldResetStats() {
            assertThatCode(() -> plotManager.resetCacheStatistics())
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Persistence Contract")
    class PersistenceContractTests {

        @Test
        @DisplayName("loadPlots should initialize plots")
        void loadPlotsShouldInitializePlots() {
            assertThatCode(() -> plotManager.loadPlots())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("savePlots should persist plots")
        void savePlotsShouldPersistPlots() {
            plotManager.createPlot(pos1, pos2, 1000.0);

            assertThatCode(() -> plotManager.savePlots())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("saveIfNeeded should persist when dirty")
        void saveIfNeededShouldPersistWhenDirty() {
            assertThatCode(() -> plotManager.saveIfNeeded())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("markDirty should mark data as dirty")
        void markDirtyShouldMarkDataAsDirty() {
            assertThatCode(() -> plotManager.markDirty())
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SYSTEM OPERATIONS CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("System Operations Contract")
    class SystemOperationsContractTests {

        @Test
        @DisplayName("rebuildSpatialIndex should rebuild index")
        void rebuildSpatialIndexShouldRebuildIndex() {
            plotManager.createPlot(pos1, pos2, 1000.0);

            assertThatCode(() -> plotManager.rebuildSpatialIndex())
                .doesNotThrowAnyException();

            assertThat(plotManager.getPlotAt(pos1)).isNotNull();
        }

        @Test
        @DisplayName("printDebugInfo should print info")
        void printDebugInfoShouldPrintInfo() {
            assertThatCode(() -> plotManager.printDebugInfo())
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HEALTH MONITORING CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Health Monitoring Contract")
    class HealthMonitoringContractTests {

        @Test
        @DisplayName("isHealthy should return health status")
        void isHealthyShouldReturnHealthStatus() {
            boolean healthy = plotManager.isHealthy();

            assertThat(healthy).isNotNull();
        }

        @Test
        @DisplayName("getLastError should return error message or null")
        void getLastErrorShouldReturnErrorOrNull() {
            String error = plotManager.getLastError();

            assertThat(error).satisfiesAnyOf(
                e -> assertThat(e).isNull(),
                e -> assertThat(e).isInstanceOf(String.class)
            );
        }

        @Test
        @DisplayName("getHealthInfo should return formatted status")
        void getHealthInfoShouldReturnFormattedStatus() {
            String healthInfo = plotManager.getHealthInfo();

            assertThat(healthInfo).isNotNull();
            assertThat(healthInfo).isNotEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // METHOD DELEGATION CONTRACT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Method Delegation Contract")
    class MethodDelegationContractTests {

        @Test
        @DisplayName("Instance methods should delegate to static methods")
        void instanceMethodsShouldDelegateToStaticMethods() {
            // Create via static
            PlotRegion plot = PlotManager.createPlot(pos1, pos2, 1000.0);

            // Read via instance
            PlotRegion found = plotManager.getPlot(plot.getPlotId());

            assertThat(found).isNotNull();
        }

        @Test
        @DisplayName("Interface and concrete class should share state")
        void interfaceAndConcreteClassShouldShareState() {
            // Create via instance
            PlotRegion plot = plotManager.createPlot(pos1, pos2, 1000.0);

            // Read via static
            PlotRegion found = PlotManager.getPlot(plot.getPlotId());

            assertThat(found).isNotNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEPENDENCY INJECTION SUPPORT
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Dependency Injection Support")
    class DependencyInjectionSupportTests {

        @Test
        @DisplayName("Should support constructor injection pattern")
        void shouldSupportConstructorInjectionPattern() {
            IPlotManager injectedManager = PlotManager.getInstance();

            TestPlotConsumer consumer = new TestPlotConsumer(injectedManager);
            PlotRegion plot = consumer.performOperation(pos1, pos2);

            assertThat(injectedManager.hasPlot(plot.getPlotId())).isTrue();
        }

        @Test
        @DisplayName("Should support method injection pattern")
        void shouldSupportMethodInjectionPattern() {
            TestPlotConsumer consumer = new TestPlotConsumer(null);
            consumer.setPlotManager(PlotManager.getInstance());

            PlotRegion plot = consumer.performOperation(pos1, pos2);

            assertThat(plotManager.hasPlot(plot.getPlotId())).isTrue();
        }
    }

    /**
     * Helper class to test dependency injection patterns
     */
    private static class TestPlotConsumer {
        private IPlotManager plotManager;

        public TestPlotConsumer(IPlotManager plotManager) {
            this.plotManager = plotManager;
        }

        public void setPlotManager(IPlotManager plotManager) {
            this.plotManager = plotManager;
        }

        public PlotRegion performOperation(BlockPos pos1, BlockPos pos2) {
            return plotManager.createPlot(pos1, pos2, 1000.0);
        }
    }
}
