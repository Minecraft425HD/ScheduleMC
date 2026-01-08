package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

/**
 * Unit Tests für PlotManager
 */
public class PlotManagerTest {

    private UUID testOwner;
    private BlockPos pos1;
    private BlockPos pos2;

    @BeforeEach
    public void setUp() {
        testOwner = UUID.randomUUID();
        pos1 = new BlockPos(0, 64, 0);
        pos2 = new BlockPos(10, 70, 10);

        // Clear plots before each test
        PlotManager.clearAllPlots();
    }

    @Test
    public void testCreatePlot_Basic() {
        PlotRegion plot = PlotManager.createPlot(pos1, pos2, 1000.0);

        assertThat(plot).isNotNull();
        assertThat(plot.getPrice()).isEqualTo(1000.0);
        assertThat(plot.getType()).isEqualTo(PlotType.RESIDENTIAL);
    }

    @Test
    public void testCreatePlot_WithCustomName() {
        PlotRegion plot = PlotManager.createPlot(pos1, pos2, "TestPlot", PlotType.COMMERCIAL, 5000.0);

        assertThat(plot).isNotNull();
        assertThat(plot.getPlotName()).contains("TestPlot");
        assertThat(plot.getType()).isEqualTo(PlotType.COMMERCIAL);
        assertThat(plot.getPrice()).isEqualTo(5000.0);
    }

    @Test
    public void testCreatePlot_CalculatesVolume() {
        PlotRegion plot = PlotManager.createPlot(pos1, pos2, 1000.0);

        int expectedVolume = 11 * 7 * 11; // (10-0+1) * (70-64+1) * (10-0+1)
        assertThat(plot.getVolume()).isEqualTo(expectedVolume);
    }

    @Test
    public void testGetPlotAt_ExistingPlot() {
        PlotRegion createdPlot = PlotManager.createPlot(pos1, pos2, 1000.0);

        BlockPos insidePos = new BlockPos(5, 67, 5);
        PlotRegion foundPlot = PlotManager.getPlotAt(insidePos);

        assertThat(foundPlot).isNotNull();
        assertThat(foundPlot.getPlotId()).isEqualTo(createdPlot.getPlotId());
    }

    @Test
    public void testGetPlotAt_NonExistingPlot() {
        PlotManager.createPlot(pos1, pos2, 1000.0);

        BlockPos outsidePos = new BlockPos(100, 67, 100);
        PlotRegion foundPlot = PlotManager.getPlotAt(outsidePos);

        assertThat(foundPlot).isNull();
    }

    @Test
    public void testGetPlotById_Existing() {
        PlotRegion createdPlot = PlotManager.createPlot(pos1, pos2, 1000.0);

        PlotRegion foundPlot = PlotManager.getPlot(createdPlot.getPlotId());

        assertThat(foundPlot).isNotNull();
        assertThat(foundPlot).isEqualTo(createdPlot);
    }

    @Test
    public void testGetPlotById_NonExisting() {
        PlotRegion foundPlot = PlotManager.getPlot("non_existent_plot");

        assertThat(foundPlot).isNull();
    }

    @Test
    public void testRemovePlot() {
        PlotRegion plot = PlotManager.createPlot(pos1, pos2, 1000.0);
        String plotId = plot.getPlotId();

        boolean removed = PlotManager.removePlot(plotId);

        assertThat(removed).isTrue();
        assertThat(PlotManager.getPlot(plotId)).isNull();
    }

    @Test
    public void testRemovePlot_NonExisting() {
        boolean removed = PlotManager.removePlot("non_existent");

        assertThat(removed).isFalse();
    }

    @Test
    public void testGetAllPlots() {
        PlotManager.createPlot(pos1, pos2, 1000.0);
        PlotManager.createPlot(new BlockPos(20, 64, 20), new BlockPos(30, 70, 30), 2000.0);
        PlotManager.createPlot(new BlockPos(40, 64, 40), new BlockPos(50, 70, 50), 3000.0);

        assertThat(PlotManager.getPlots()).hasSize(3);
    }

    @Test
    public void testPlotOverlap_ShouldNotInterfere() {
        // Create two non-overlapping plots
        PlotRegion plot1 = PlotManager.createPlot(pos1, pos2, 1000.0);
        PlotRegion plot2 = PlotManager.createPlot(
            new BlockPos(20, 64, 20),
            new BlockPos(30, 70, 30),
            2000.0
        );

        // Verify both exist independently
        assertThat(PlotManager.getPlotAt(new BlockPos(5, 67, 5))).isEqualTo(plot1);
        assertThat(PlotManager.getPlotAt(new BlockPos(25, 67, 25))).isEqualTo(plot2);
    }

    @Test
    public void testPlotCache_Performance() {
        // Create a plot
        PlotRegion plot = PlotManager.createPlot(pos1, pos2, 1000.0);
        BlockPos testPos = new BlockPos(5, 67, 5);

        // Access multiple times - should use cache
        PlotRegion found1 = PlotManager.getPlotAt(testPos);
        PlotRegion found2 = PlotManager.getPlotAt(testPos);
        PlotRegion found3 = PlotManager.getPlotAt(testPos);

        assertThat(found1).isSameAs(found2).isSameAs(found3);
    }

    @Test
    public void testPlotTypes() {
        PlotRegion residential = PlotManager.createPlot(pos1, pos2, "Home", PlotType.RESIDENTIAL, 1000.0);
        PlotRegion shop = PlotManager.createPlot(
            new BlockPos(20, 64, 20),
            new BlockPos(30, 70, 30),
            "Shop",
            PlotType.SHOP,
            0.0
        );

        assertThat(residential.getType()).isEqualTo(PlotType.RESIDENTIAL);
        assertThat(shop.getType()).isEqualTo(PlotType.SHOP);
        assertThat(residential.getType().canBePurchased()).isTrue();
        assertThat(shop.getType().canBePurchased()).isFalse(); // Shops can't be purchased
    }
}
