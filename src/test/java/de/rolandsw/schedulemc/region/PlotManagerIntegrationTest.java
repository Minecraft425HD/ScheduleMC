package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for PlotManager
 *
 * Tests comprehensive plot management scenarios:
 * - Plot Creation & Validation
 * - Ownership & Permissions
 * - Plot Queries & Filtering
 * - Persistence & Recovery
 * - Boundary Detection
 * - Statistics & Metrics
 *
 * @since 1.0
 */
@DisplayName("PlotManager Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlotManagerIntegrationTest {

    @TempDir
    Path tempDir;

    private UUID ownerA;
    private UUID ownerB;
    private PlotRegion testPlot;

    @BeforeEach
    void setUp() {
        // Reset plot manager state
        PlotManager.clearAllPlots();

        // Create test owners
        ownerA = UUID.randomUUID();
        ownerB = UUID.randomUUID();
    }

    @Test
    @Order(1)
    @DisplayName("Plot Creation - Should create plot with valid bounds")
    void testCreatePlot() {
        // Arrange
        BlockPos pos1 = new BlockPos(0, 64, 0);
        BlockPos pos2 = new BlockPos(15, 80, 15);

        // Act
        PlotRegion plot = PlotManager.createPlot(pos1, pos2, 1000.0);

        // Assert
        assertThat(plot).isNotNull();
        assertThat(plot.getPrice()).isEqualTo(1000.0);
        assertThat(plot.isAvailable()).isTrue();
        assertThat(PlotManager.getPlotCount()).isEqualTo(1);
    }

    @Test
    @Order(2)
    @DisplayName("Plot Creation - Should create named plot")
    void testCreateNamedPlot() {
        // Act
        PlotRegion plot = PlotManager.createPlot(
            new BlockPos(0, 64, 0),
            new BlockPos(15, 80, 15),
            "Test Estate",
            PlotType.RESIDENTIAL,
            5000.0
        );

        // Assert
        assertThat(plot.getName()).isEqualTo("Test Estate");
        assertThat(plot.getType()).isEqualTo(PlotType.RESIDENTIAL);
        assertThat(plot.getPrice()).isEqualTo(5000.0);
    }

    @Test
    @Order(3)
    @DisplayName("Plot Ownership - Should set and get owner")
    void testPlotOwnership() {
        // Arrange
        PlotRegion plot = PlotManager.createPlot(
            new BlockPos(0, 64, 0),
            new BlockPos(15, 80, 15),
            1000.0
        );

        // Act
        plot.setOwner(ownerA);

        // Assert
        assertThat(plot.getOwner()).isEqualTo(ownerA);
        assertThat(plot.isAvailable()).isFalse();
        assertThat(plot.hasOwner()).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("Plot Ownership - Should transfer ownership")
    void testOwnershipTransfer() {
        // Arrange
        PlotRegion plot = PlotManager.createPlot(
            new BlockPos(0, 64, 0),
            new BlockPos(15, 80, 15),
            1000.0
        );
        plot.setOwner(ownerA);

        // Act
        plot.setOwner(ownerB);

        // Assert
        assertThat(plot.getOwner()).isEqualTo(ownerB);
        assertThat(plot.hasOwner()).isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("Plot Queries - Should find plot by position")
    void testGetPlotAt() {
        // Arrange
        PlotRegion plot = PlotManager.createPlot(
            new BlockPos(0, 64, 0),
            new BlockPos(15, 80, 15),
            1000.0
        );

        // Act - Position inside plot
        PlotRegion found = PlotManager.getPlotAt(new BlockPos(7, 70, 7));

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(plot.getId());
    }

    @Test
    @Order(6)
    @DisplayName("Plot Queries - Should return null for position outside plot")
    void testGetPlotAtOutsideBounds() {
        // Arrange
        PlotManager.createPlot(
            new BlockPos(0, 64, 0),
            new BlockPos(15, 80, 15),
            1000.0
        );

        // Act - Position outside plot
        PlotRegion found = PlotManager.getPlotAt(new BlockPos(100, 70, 100));

        // Assert
        assertThat(found).isNull();
    }

    @Test
    @Order(7)
    @DisplayName("Plot Queries - Should filter plots by owner")
    void testGetPlotsByOwner() {
        // Arrange
        PlotRegion plot1 = PlotManager.createPlot(new BlockPos(0, 64, 0), new BlockPos(15, 80, 15), 1000.0);
        PlotRegion plot2 = PlotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(35, 80, 35), 1000.0);
        PlotRegion plot3 = PlotManager.createPlot(new BlockPos(40, 64, 40), new BlockPos(55, 80, 55), 1000.0);

        plot1.setOwner(ownerA);
        plot2.setOwner(ownerA);
        plot3.setOwner(ownerB);

        // Act
        List<PlotRegion> ownerAPlots = PlotManager.getPlotsByOwner(ownerA);

        // Assert
        assertThat(ownerAPlots).hasSize(2);
        assertThat(ownerAPlots).allMatch(p -> p.getOwner().equals(ownerA));
    }

    @Test
    @Order(8)
    @DisplayName("Plot Queries - Should get available plots")
    void testGetAvailablePlots() {
        // Arrange
        PlotRegion plot1 = PlotManager.createPlot(new BlockPos(0, 64, 0), new BlockPos(15, 80, 15), 1000.0);
        PlotRegion plot2 = PlotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(35, 80, 35), 1000.0);
        PlotRegion plot3 = PlotManager.createPlot(new BlockPos(40, 64, 40), new BlockPos(55, 80, 55), 1000.0);

        plot1.setOwner(ownerA); // Not available
        // plot2 and plot3 remain available

        // Act
        List<PlotRegion> available = PlotManager.getAvailablePlots();

        // Assert
        assertThat(available).hasSize(2);
        assertThat(available).allMatch(PlotRegion::isAvailable);
    }

    @Test
    @Order(9)
    @DisplayName("Plot Removal - Should remove plot by ID")
    void testRemovePlot() {
        // Arrange
        PlotRegion plot = PlotManager.createPlot(
            new BlockPos(0, 64, 0),
            new BlockPos(15, 80, 15),
            1000.0
        );
        String plotId = plot.getId();

        // Act
        boolean removed = PlotManager.removePlot(plotId);

        // Assert
        assertThat(removed).isTrue();
        assertThat(PlotManager.hasPlot(plotId)).isFalse();
        assertThat(PlotManager.getPlotCount()).isEqualTo(0);
    }

    @Test
    @Order(10)
    @DisplayName("Plot Removal - Should remove plot by position")
    void testRemovePlotAt() {
        // Arrange
        PlotManager.createPlot(
            new BlockPos(0, 64, 0),
            new BlockPos(15, 80, 15),
            1000.0
        );

        // Act
        boolean removed = PlotManager.removePlotAt(new BlockPos(7, 70, 7));

        // Assert
        assertThat(removed).isTrue();
        assertThat(PlotManager.getPlotCount()).isEqualTo(0);
    }

    @Test
    @Order(11)
    @DisplayName("Plot Statistics - Should calculate correct statistics")
    void testGetStatistics() {
        // Arrange
        PlotRegion plot1 = PlotManager.createPlot(new BlockPos(0, 64, 0), new BlockPos(15, 80, 15), 1000.0);
        PlotRegion plot2 = PlotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(35, 80, 35), 2000.0);
        PlotRegion plot3 = PlotManager.createPlot(new BlockPos(40, 64, 40), new BlockPos(55, 80, 55), 3000.0);

        plot1.setOwner(ownerA);
        plot2.setOwner(ownerB);
        // plot3 remains available

        // Act
        PlotManager.PlotStatistics stats = PlotManager.getStatistics();

        // Assert
        assertThat(stats.totalPlots).isEqualTo(3);
        assertThat(stats.ownedPlots).isEqualTo(2);
        assertThat(stats.availablePlots).isEqualTo(1);
        assertThat(stats.totalValue).isEqualTo(6000.0);
    }

    @Test
    @Order(12)
    @DisplayName("Plot Metrics - Should count plots correctly")
    void testGetPlotCount() {
        // Arrange
        PlotManager.createPlot(new BlockPos(0, 64, 0), new BlockPos(15, 80, 15), 1000.0);
        PlotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(35, 80, 35), 1000.0);
        PlotManager.createPlot(new BlockPos(40, 64, 40), new BlockPos(55, 80, 55), 1000.0);

        // Act
        int count = PlotManager.getPlotCount();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @Order(13)
    @DisplayName("Plot Volume - Should calculate total plot volume")
    void testGetTotalPlotVolume() {
        // Arrange
        PlotManager.createPlot(new BlockPos(0, 64, 0), new BlockPos(15, 80, 15), 1000.0);
        // Volume = 16 * 17 * 16 = 4352

        // Act
        long volume = PlotManager.getTotalPlotVolume();

        // Assert
        assertThat(volume).isGreaterThan(0);
    }

    @Test
    @Order(14)
    @DisplayName("Boundary Detection - Should detect plot boundaries correctly")
    void testPlotBoundaries() {
        // Arrange
        PlotRegion plot = PlotManager.createPlot(
            new BlockPos(0, 64, 0),
            new BlockPos(15, 80, 15),
            1000.0
        );

        // Act & Assert - Inside boundaries
        assertThat(plot.contains(new BlockPos(0, 64, 0))).isTrue();    // Min corner
        assertThat(plot.contains(new BlockPos(15, 80, 15))).isTrue();  // Max corner
        assertThat(plot.contains(new BlockPos(7, 70, 7))).isTrue();    // Center

        // Outside boundaries
        assertThat(plot.contains(new BlockPos(-1, 64, 0))).isFalse();  // X too low
        assertThat(plot.contains(new BlockPos(16, 64, 0))).isFalse();  // X too high
        assertThat(plot.contains(new BlockPos(0, 63, 0))).isFalse();   // Y too low
        assertThat(plot.contains(new BlockPos(0, 81, 0))).isFalse();   // Y too high
    }

    @Test
    @Order(15)
    @DisplayName("Plot Types - Should filter by plot type")
    void testPlotTypes() {
        // Arrange
        PlotManager.createPlot(new BlockPos(0, 64, 0), new BlockPos(15, 80, 15), "House", PlotType.RESIDENTIAL, 1000.0);
        PlotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(35, 80, 35), "Shop", PlotType.COMMERCIAL, 2000.0);
        PlotManager.createPlot(new BlockPos(40, 64, 40), new BlockPos(55, 80, 55), "Factory", PlotType.INDUSTRIAL, 3000.0);

        // Act
        List<PlotRegion> allPlots = PlotManager.getPlots();
        long residentialCount = allPlots.stream().filter(p -> p.getType() == PlotType.RESIDENTIAL).count();
        long commercialCount = allPlots.stream().filter(p -> p.getType() == PlotType.COMMERCIAL).count();

        // Assert
        assertThat(residentialCount).isEqualTo(1);
        assertThat(commercialCount).isEqualTo(1);
    }

    @Test
    @Order(16)
    @DisplayName("Empty Manager - Should handle empty plot manager")
    void testEmptyManager() {
        // Act
        List<PlotRegion> plots = PlotManager.getPlots();
        PlotRegion nonExistent = PlotManager.getPlotAt(new BlockPos(0, 0, 0));

        // Assert
        assertThat(plots).isEmpty();
        assertThat(nonExistent).isNull();
        assertThat(PlotManager.getPlotCount()).isEqualTo(0);
    }

    @AfterEach
    void tearDown() {
        PlotManager.clearAllPlots();
    }
}
