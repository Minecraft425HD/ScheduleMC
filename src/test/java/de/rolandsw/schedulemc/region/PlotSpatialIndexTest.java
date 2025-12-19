package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlotSpatialIndex
 *
 * Tests cover:
 * - Adding and removing plots
 * - Spatial queries (getPlotsNear)
 * - Index rebuilding
 * - Chunk-based indexing correctness
 * - Performance characteristics
 * - Edge cases (overlapping plots, large plots)
 */
class PlotSpatialIndexTest {

    private PlotSpatialIndex index;

    @BeforeEach
    void setUp() {
        index = new PlotSpatialIndex();
    }

    // ==================== Add/Remove Tests ====================

    @Test
    @DisplayName("Should add plot to index")
    void testAddPlot() {
        // Arrange
        PlotRegion plot = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));

        // Act
        index.addPlot(plot);
        Set<String> plots = index.getPlotsNear(new BlockPos(5, 5, 5));

        // Assert
        assertThat(plots).contains("plot1");
    }

    @Test
    @DisplayName("Should remove plot from index")
    void testRemovePlot() {
        // Arrange
        PlotRegion plot = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        index.addPlot(plot);

        // Act
        index.removePlot("plot1");
        Set<String> plots = index.getPlotsNear(new BlockPos(5, 5, 5));

        // Assert
        assertThat(plots).isEmpty();
    }

    @Test
    @DisplayName("Should update plot when re-adding")
    void testUpdatePlot() {
        // Arrange
        PlotRegion oldPlot = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        index.addPlot(oldPlot);

        // Act - Re-add with different position
        PlotRegion newPlot = createPlot("plot1", new BlockPos(100, 0, 0), new BlockPos(115, 15, 15));
        index.addPlot(newPlot);

        // Assert - Should only be found at new position
        assertThat(index.getPlotsNear(new BlockPos(5, 5, 5))).isEmpty();
        assertThat(index.getPlotsNear(new BlockPos(105, 5, 5))).contains("plot1");
    }

    @Test
    @DisplayName("Should handle removing non-existent plot")
    void testRemoveNonExistentPlot() {
        // Act & Assert - Should not throw
        assertThatCode(() -> index.removePlot("nonexistent")).doesNotThrowAnyException();
    }

    // ==================== Spatial Query Tests ====================

    @Test
    @DisplayName("Should find plot at position within bounds")
    void testGetPlotsNearWithinBounds() {
        // Arrange
        PlotRegion plot = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(31, 31, 31));
        index.addPlot(plot);

        // Act & Assert - Check various positions
        assertThat(index.getPlotsNear(new BlockPos(0, 0, 0))).contains("plot1");
        assertThat(index.getPlotsNear(new BlockPos(15, 15, 15))).contains("plot1");
        assertThat(index.getPlotsNear(new BlockPos(31, 31, 31))).contains("plot1");
    }

    @Test
    @DisplayName("Should not find plot at position outside bounds")
    void testGetPlotsNearOutsideBounds() {
        // Arrange
        PlotRegion plot = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        index.addPlot(plot);

        // Act & Assert
        assertThat(index.getPlotsNear(new BlockPos(100, 0, 0))).doesNotContain("plot1");
        assertThat(index.getPlotsNear(new BlockPos(-100, 0, 0))).doesNotContain("plot1");
    }

    @Test
    @DisplayName("Should return empty set for empty chunk")
    void testGetPlotsNearEmptyChunk() {
        // Act
        Set<String> plots = index.getPlotsNear(new BlockPos(1000, 1000, 1000));

        // Assert
        assertThat(plots).isEmpty();
    }

    @Test
    @DisplayName("Should find multiple plots in same chunk")
    void testMultiplePlotsInSameChunk() {
        // Arrange - Two plots in same 16x16x16 chunk
        PlotRegion plot1 = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(7, 7, 7));
        PlotRegion plot2 = createPlot("plot2", new BlockPos(8, 0, 0), new BlockPos(15, 7, 7));
        index.addPlot(plot1);
        index.addPlot(plot2);

        // Act
        Set<String> plots = index.getPlotsNear(new BlockPos(5, 5, 5));

        // Assert - Both plots should be found
        assertThat(plots).containsExactlyInAnyOrder("plot1", "plot2");
    }

    // ==================== Chunk Boundary Tests ====================

    @Test
    @DisplayName("Should handle plots crossing chunk boundaries")
    void testPlotCrossingChunkBoundary() {
        // Arrange - Plot crosses from chunk (0,0,0) to (1,0,0)
        PlotRegion plot = createPlot("plot1", new BlockPos(10, 0, 0), new BlockPos(25, 15, 15));
        index.addPlot(plot);

        // Act & Assert - Should be found in both chunks
        assertThat(index.getPlotsNear(new BlockPos(12, 5, 5))).contains("plot1");  // Chunk 0
        assertThat(index.getPlotsNear(new BlockPos(20, 5, 5))).contains("plot1");  // Chunk 1
    }

    @Test
    @DisplayName("Should handle plots spanning multiple chunks")
    void testLargePlotSpanningMultipleChunks() {
        // Arrange - Large plot spanning 4x4x4 = 64 chunks
        PlotRegion plot = createPlot("bigplot", new BlockPos(0, 0, 0), new BlockPos(63, 63, 63));
        index.addPlot(plot);

        // Act & Assert - Should be found in all corners
        assertThat(index.getPlotsNear(new BlockPos(0, 0, 0))).contains("bigplot");
        assertThat(index.getPlotsNear(new BlockPos(63, 63, 63))).contains("bigplot");
        assertThat(index.getPlotsNear(new BlockPos(32, 32, 32))).contains("bigplot");
    }

    @Test
    @DisplayName("Should correctly calculate chunk coordinates for negative positions")
    void testNegativeCoordinates() {
        // Arrange
        PlotRegion plot = createPlot("plot1", new BlockPos(-16, 0, 0), new BlockPos(-1, 15, 15));
        index.addPlot(plot);

        // Act & Assert
        assertThat(index.getPlotsNear(new BlockPos(-10, 5, 5))).contains("plot1");
        assertThat(index.getPlotsNear(new BlockPos(10, 5, 5))).doesNotContain("plot1");
    }

    // ==================== Clear and Rebuild Tests ====================

    @Test
    @DisplayName("Should clear all plots from index")
    void testClear() {
        // Arrange
        PlotRegion plot1 = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        PlotRegion plot2 = createPlot("plot2", new BlockPos(100, 0, 0), new BlockPos(115, 15, 15));
        index.addPlot(plot1);
        index.addPlot(plot2);

        // Act
        index.clear();

        // Assert
        assertThat(index.getPlotsNear(new BlockPos(5, 5, 5))).isEmpty();
        assertThat(index.getPlotsNear(new BlockPos(105, 5, 5))).isEmpty();
    }

    @Test
    @DisplayName("Should rebuild index from collection")
    void testRebuild() {
        // Arrange
        List<PlotRegion> plots = Arrays.asList(
            createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15)),
            createPlot("plot2", new BlockPos(100, 0, 0), new BlockPos(115, 15, 15)),
            createPlot("plot3", new BlockPos(200, 0, 0), new BlockPos(215, 15, 15))
        );

        // Act
        index.rebuild(plots);

        // Assert - All plots should be findable
        assertThat(index.getPlotsNear(new BlockPos(5, 5, 5))).contains("plot1");
        assertThat(index.getPlotsNear(new BlockPos(105, 5, 5))).contains("plot2");
        assertThat(index.getPlotsNear(new BlockPos(205, 5, 5))).contains("plot3");
    }

    @Test
    @DisplayName("Should rebuild over existing index")
    void testRebuildOverExisting() {
        // Arrange - Initial state
        index.addPlot(createPlot("old1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15)));
        index.addPlot(createPlot("old2", new BlockPos(100, 0, 0), new BlockPos(115, 15, 15)));

        // Act - Rebuild with new plots
        List<PlotRegion> newPlots = Arrays.asList(
            createPlot("new1", new BlockPos(200, 0, 0), new BlockPos(215, 15, 15))
        );
        index.rebuild(newPlots);

        // Assert - Old plots gone, new plot present
        assertThat(index.getPlotsNear(new BlockPos(5, 5, 5))).isEmpty();
        assertThat(index.getPlotsNear(new BlockPos(105, 5, 5))).isEmpty();
        assertThat(index.getPlotsNear(new BlockPos(205, 5, 5))).contains("new1");
    }

    // ==================== Statistics Tests ====================

    @Test
    @DisplayName("Should provide statistics")
    void testGetStats() {
        // Arrange
        index.addPlot(createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15)));
        index.addPlot(createPlot("plot2", new BlockPos(100, 0, 0), new BlockPos(115, 15, 15)));

        // Act
        String stats = index.getStats();

        // Assert
        assertThat(stats).contains("2 Plots");
    }

    @Test
    @DisplayName("Should handle empty index statistics")
    void testGetStatsEmpty() {
        // Act
        String stats = index.getStats();

        // Assert
        assertThat(stats).contains("0 Plots");
        assertThat(stats).doesNotContain("NaN");
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Should handle many plots efficiently")
    void testManyPlots() {
        // Arrange - Add 1000 plots
        for (int i = 0; i < 1000; i++) {
            int x = i * 20;
            PlotRegion plot = createPlot("plot" + i, new BlockPos(x, 0, 0), new BlockPos(x + 15, 15, 15));
            index.addPlot(plot);
        }

        // Act & Assert - Queries should still be fast
        long start = System.nanoTime();
        Set<String> plots = index.getPlotsNear(new BlockPos(505, 5, 5));
        long duration = System.nanoTime() - start;

        assertThat(plots).contains("plot25");
        assertThat(duration).isLessThan(1_000_000); // Less than 1ms
    }

    @Test
    @DisplayName("Should handle overlapping plots")
    void testOverlappingPlots() {
        // Arrange - Multiple plots overlapping same space
        PlotRegion plot1 = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(31, 31, 31));
        PlotRegion plot2 = createPlot("plot2", new BlockPos(10, 10, 10), new BlockPos(41, 41, 41));
        PlotRegion plot3 = createPlot("plot3", new BlockPos(20, 20, 20), new BlockPos(51, 51, 51));

        index.addPlot(plot1);
        index.addPlot(plot2);
        index.addPlot(plot3);

        // Act - Query overlapping region
        Set<String> plots = index.getPlotsNear(new BlockPos(25, 25, 25));

        // Assert - All three should be found (index returns candidates)
        assertThat(plots).containsExactlyInAnyOrder("plot1", "plot2", "plot3");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle single-block plot")
    void testSingleBlockPlot() {
        // Arrange
        PlotRegion plot = createPlot("tiny", new BlockPos(5, 5, 5), new BlockPos(5, 5, 5));
        index.addPlot(plot);

        // Act & Assert
        assertThat(index.getPlotsNear(new BlockPos(5, 5, 5))).contains("tiny");
        assertThat(index.getPlotsNear(new BlockPos(6, 5, 5))).doesNotContain("tiny");
    }

    @Test
    @DisplayName("Should handle plot at world origin")
    void testPlotAtOrigin() {
        // Arrange
        PlotRegion plot = createPlot("origin", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        index.addPlot(plot);

        // Act & Assert
        assertThat(index.getPlotsNear(new BlockPos(0, 0, 0))).contains("origin");
    }

    @Test
    @DisplayName("Should handle extreme coordinates")
    void testExtremeCoordinates() {
        // Arrange
        PlotRegion plot = createPlot("extreme",
            new BlockPos(1000000, 0, 1000000),
            new BlockPos(1000015, 15, 1000015));
        index.addPlot(plot);

        // Act & Assert
        assertThat(index.getPlotsNear(new BlockPos(1000005, 5, 1000005))).contains("extreme");
    }

    @Test
    @DisplayName("Should return unmodifiable set")
    void testUnmodifiableResult() {
        // Arrange
        PlotRegion plot = createPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        index.addPlot(plot);

        // Act
        Set<String> plots = index.getPlotsNear(new BlockPos(5, 5, 5));

        // Assert - Should throw when trying to modify
        assertThatThrownBy(() -> plots.add("newplot"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a mock PlotRegion for testing
     */
    private PlotRegion createPlot(String plotId, BlockPos min, BlockPos max) {
        PlotRegion plot = mock(PlotRegion.class);
        when(plot.getPlotId()).thenReturn(plotId);
        when(plot.getMin()).thenReturn(min);
        when(plot.getMax()).thenReturn(max);
        return plot;
    }
}
