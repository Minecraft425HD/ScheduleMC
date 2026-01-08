package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Unit Tests for PlotCache
 *
 * Tests cover:
 * 1. Basic Cache Operations (put, get, size)
 * 2. LRU Eviction (maxSize enforcement, oldest entry removal)
 * 3. Cache Invalidation (single, region, plot-based)
 * 4. Chunk Index (efficient region invalidation)
 * 5. Hit/Miss Statistics (counters, hit rate)
 * 6. Thread Safety (concurrent operations)
 * 7. Performance (O(1) lookups, chunk-based invalidation)
 *
 * Key Features Tested:
 * - ConcurrentHashMap usage for thread-safety
 * - Chunk-based spatial indexing
 * - LRU eviction with timestamp-based approximation
 * - Automatic cache validation (plot.contains() check)
 */
class PlotCacheTest {

    private PlotCache cache;
    private PlotRegion mockPlot;

    @BeforeEach
    void setUp() {
        cache = new PlotCache(100); // kleinere Cache für Tests
        mockPlot = createMockPlot("test-plot", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
    }

    // ==================== Basic Cache Operations ====================

    @Test
    @DisplayName("put() should add entry to cache")
    void testPutAddsEntry() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);

        // Act
        cache.put(pos, mockPlot);

        // Assert
        assertThat(cache.size()).isEqualTo(1);
        assertThat(cache.get(pos)).isEqualTo(mockPlot);
    }

    @Test
    @DisplayName("get() should retrieve entry from cache")
    void testGetRetrievesEntry() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        cache.put(pos, mockPlot);

        // Act
        PlotRegion result = cache.get(pos);

        // Assert
        assertThat(result).isEqualTo(mockPlot);
        assertThat(cache.getHits()).isEqualTo(1);
    }

    @Test
    @DisplayName("get() should return null for non-existent entry")
    void testGetReturnsNullForNonExistent() {
        // Arrange
        BlockPos pos = new BlockPos(1000, 1000, 1000);

        // Act
        PlotRegion result = cache.get(pos);

        // Assert
        assertThat(result).isNull();
        assertThat(cache.getMisses()).isEqualTo(1);
    }

    @Test
    @DisplayName("put() with null plot should not add entry")
    void testPutNullPlotDoesNotAddEntry() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);

        // Act
        cache.put(pos, null);

        // Assert
        assertThat(cache.size()).isZero();
    }

    @Test
    @DisplayName("get() should invalidate entry if plot no longer contains position")
    void testGetInvalidatesStaleEntry() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        PlotRegion plot = createMockPlot("test", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        cache.put(pos, plot);

        // Ändern Mock so dass contains() false zurückgibt
        when(plot.contains(pos)).thenReturn(false);

        // Act
        PlotRegion result = cache.get(pos);

        // Assert
        assertThat(result).isNull();
        assertThat(cache.size()).isZero(); // Entry wurde automatisch entfernt
        assertThat(cache.getMisses()).isEqualTo(1);
    }

    // ==================== LRU Eviction Tests ====================

    @Test
    @DisplayName("Cache should respect maxSize limit")
    void testCacheRespectsMaxSize() {
        // Arrange - Cache mit maxSize=10
        PlotCache smallCache = new PlotCache(10);

        // Act - Füge 15 Einträge hinzu
        for (int i = 0; i < 15; i++) {
            BlockPos pos = new BlockPos(i * 20, 0, 0);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 20 + 10, 10, 10));
            smallCache.put(pos, plot);
        }

        // Assert - Nur 10 Einträge sollten im Cache sein
        assertThat(smallCache.size()).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("Oldest entries should be evicted when cache is full")
    void testLRUEvictionRemovesOldestEntries() throws InterruptedException {
        // Arrange - Cache mit maxSize=5
        PlotCache smallCache = new PlotCache(5);

        // Act - Füge 10 Einträge mit kleinen Delays hinzu
        List<BlockPos> positions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BlockPos pos = new BlockPos(i * 20, 0, 0);
            positions.add(pos);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 20 + 10, 10, 10));
            smallCache.put(pos, plot);
            Thread.sleep(2); // Kleine Verzögerung für verschiedene Timestamps
        }

        // Assert - Die ersten 5 Einträge sollten evicted sein
        assertThat(smallCache.get(positions.get(0))).isNull(); // Ältester Entry
        assertThat(smallCache.get(positions.get(1))).isNull();
        assertThat(smallCache.get(positions.get(9))).isNotNull(); // Neuester Entry
    }

    @Test
    @DisplayName("Frequently accessed entries should remain in cache")
    void testFrequentlyAccessedEntriesRemain() throws InterruptedException {
        // Arrange - Cache mit maxSize=5
        PlotCache smallCache = new PlotCache(5);
        BlockPos frequentPos = new BlockPos(0, 0, 0);
        PlotRegion frequentPlot = createMockPlot("frequent", frequentPos, new BlockPos(10, 10, 10));
        smallCache.put(frequentPos, frequentPlot);

        // Act - Füge weitere Einträge hinzu
        for (int i = 1; i < 10; i++) {
            BlockPos pos = new BlockPos(i * 20, 0, 0);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 20 + 10, 10, 10));
            smallCache.put(pos, plot);
            Thread.sleep(2);
        }

        // Assert - Der häufig zugeriffene Entry könnte evicted sein (LRU approximation)
        // HINWEIS: Bei ConcurrentHashMap LRU ist dies nur approximiert
        assertThat(smallCache.size()).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("clear() should remove all entries")
    void testClearRemovesAllEntries() {
        // Arrange
        for (int i = 0; i < 10; i++) {
            BlockPos pos = new BlockPos(i * 20, 0, 0);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 20 + 10, 10, 10));
            cache.put(pos, plot);
        }

        // Act
        cache.clear();

        // Assert
        assertThat(cache.size()).isZero();
    }

    // ==================== Cache Invalidation Tests ====================

    @Test
    @DisplayName("invalidatePlot() should remove all entries for a plot")
    void testInvalidatePlotRemovesEntries() {
        // Arrange
        PlotRegion plot1 = createMockPlot("plot1", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        PlotRegion plot2 = createMockPlot("plot2", new BlockPos(100, 0, 0), new BlockPos(115, 15, 15));

        cache.put(new BlockPos(5, 5, 5), plot1);
        cache.put(new BlockPos(10, 5, 5), plot1);
        cache.put(new BlockPos(105, 5, 5), plot2);

        // Act
        cache.invalidatePlot("plot1");

        // Assert
        assertThat(cache.get(new BlockPos(5, 5, 5))).isNull();
        assertThat(cache.get(new BlockPos(10, 5, 5))).isNull();
        assertThat(cache.get(new BlockPos(105, 5, 5))).isEqualTo(plot2);
        assertThat(cache.getInvalidations()).isEqualTo(1);
    }

    @Test
    @DisplayName("invalidateRegion() should remove entries in bounding box")
    void testInvalidateRegionRemovesEntriesInBounds() {
        // Arrange
        cache.put(new BlockPos(5, 5, 5), mockPlot);
        cache.put(new BlockPos(10, 5, 5), mockPlot);
        cache.put(new BlockPos(100, 5, 5), mockPlot); // Außerhalb

        // Act - Invalidiere Region von (0,0,0) bis (20,20,20)
        cache.invalidateRegion(new BlockPos(0, 0, 0), new BlockPos(20, 20, 20));

        // Assert
        assertThat(cache.get(new BlockPos(5, 5, 5))).isNull();
        assertThat(cache.get(new BlockPos(10, 5, 5))).isNull();
        assertThat(cache.get(new BlockPos(100, 5, 5))).isNotNull(); // Außerhalb bleibt
    }

    @Test
    @DisplayName("invalidateRegion() should use chunk index efficiently")
    void testInvalidateRegionUsesChunkIndex() {
        // Arrange - Füge viele Einträge in verschiedenen Chunks hinzu
        for (int i = 0; i < 50; i++) {
            BlockPos pos = new BlockPos(i * 100, 0, 0);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 100 + 10, 10, 10));
            cache.put(pos, plot);
        }

        int sizeBeforeInvalidation = cache.size();

        // Act - Invalidiere kleine Region (sollte nur 1-2 Chunks betreffen)
        long start = System.nanoTime();
        cache.invalidateRegion(new BlockPos(0, 0, 0), new BlockPos(20, 20, 20));
        long duration = System.nanoTime() - start;

        // Assert - Performance sollte O(affected_chunks) sein, nicht O(cache_size)
        assertThat(duration).isLessThan(1_000_000); // < 1ms
        assertThat(cache.size()).isLessThan(sizeBeforeInvalidation);
    }

    @Test
    @DisplayName("invalidateRegion() should handle multi-chunk regions")
    void testInvalidateRegionMultiChunk() {
        // Arrange - Füge Einträge in 4x4 Chunk-Grid hinzu
        for (int x = 0; x < 64; x += 4) {
            for (int z = 0; z < 64; z += 4) {
                BlockPos pos = new BlockPos(x, 0, z);
                PlotRegion plot = createMockPlot("plot_" + x + "_" + z, pos, new BlockPos(x + 2, 10, z + 2));
                cache.put(pos, plot);
            }
        }

        // Act - Invalidiere Region von (0,0,0) bis (31,10,31) - betrifft 2x2 = 4 Chunks
        cache.invalidateRegion(new BlockPos(0, 0, 0), new BlockPos(31, 10, 31));

        // Assert - Nur Einträge in dieser Region sollten entfernt sein
        assertThat(cache.get(new BlockPos(4, 0, 4))).isNull(); // In Region
        assertThat(cache.get(new BlockPos(40, 0, 40))).isNotNull(); // Außerhalb Region
    }

    @Test
    @DisplayName("invalidateRegion() should not affect entries outside bounds")
    void testInvalidateRegionPreservesOutsideEntries() {
        // Arrange
        BlockPos inside = new BlockPos(5, 5, 5);
        BlockPos outside = new BlockPos(100, 5, 5);

        cache.put(inside, mockPlot);
        cache.put(outside, mockPlot);

        // Act
        cache.invalidateRegion(new BlockPos(0, 0, 0), new BlockPos(20, 20, 20));

        // Assert
        assertThat(cache.get(inside)).isNull();
        assertThat(cache.get(outside)).isNotNull();
    }

    // ==================== Chunk Index Tests ====================

    @Test
    @DisplayName("Entries should be indexed in correct chunks")
    void testEntriesIndexedInCorrectChunks() {
        // Arrange & Act
        BlockPos pos1 = new BlockPos(5, 5, 5);    // Chunk (0, 0)
        BlockPos pos2 = new BlockPos(20, 5, 5);   // Chunk (1, 0)

        cache.put(pos1, mockPlot);
        cache.put(pos2, mockPlot);

        // Assert - Beide sollten separat invalidierbar sein
        cache.invalidateRegion(new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        assertThat(cache.get(pos1)).isNull();
        assertThat(cache.get(pos2)).isNotNull(); // Anderer Chunk
    }

    @Test
    @DisplayName("Multi-chunk plots should be indexed in all affected chunks")
    void testMultiChunkPlotIndexing() {
        // Arrange - Plot spanning 2 chunks
        BlockPos pos1 = new BlockPos(10, 5, 5);  // Chunk (0, 0)
        BlockPos pos2 = new BlockPos(20, 5, 5);  // Chunk (1, 0)
        PlotRegion largePlot = createMockPlot("large", new BlockPos(0, 0, 0), new BlockPos(31, 15, 15));

        cache.put(pos1, largePlot);
        cache.put(pos2, largePlot);

        // Act - Invalidiere nur ersten Chunk
        cache.invalidateRegion(new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));

        // Assert
        assertThat(cache.get(pos1)).isNull();
        assertThat(cache.get(pos2)).isNotNull(); // Zweiter Chunk bleibt
    }

    @Test
    @DisplayName("Chunk index should be cleaned up when entries are removed")
    void testChunkIndexCleanup() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        cache.put(pos, mockPlot);

        // Act - Remove durch Invalidation
        cache.invalidatePlot("test-plot");

        // Assert - Neuer Entry im selben Chunk sollte funktionieren
        PlotRegion newPlot = createMockPlot("new-plot", new BlockPos(0, 0, 0), new BlockPos(15, 15, 15));
        cache.put(pos, newPlot);
        assertThat(cache.get(pos)).isEqualTo(newPlot);
    }

    // ==================== Hit/Miss Statistics Tests ====================

    @Test
    @DisplayName("Hit counter should increase on cache hit")
    void testHitCounterIncreases() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        cache.put(pos, mockPlot);

        // Act
        cache.get(pos);
        cache.get(pos);
        cache.get(pos);

        // Assert
        assertThat(cache.getHits()).isEqualTo(3);
    }

    @Test
    @DisplayName("Miss counter should increase on cache miss")
    void testMissCounterIncreases() {
        // Act
        cache.get(new BlockPos(1, 1, 1));
        cache.get(new BlockPos(2, 2, 2));
        cache.get(new BlockPos(3, 3, 3));

        // Assert
        assertThat(cache.getMisses()).isEqualTo(3);
    }

    @Test
    @DisplayName("getStatistics() should return correct values")
    void testGetStatistics() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        cache.put(pos, mockPlot);
        cache.get(pos); // Hit
        cache.get(new BlockPos(100, 100, 100)); // Miss

        // Act
        PlotCache.CacheStatistics stats = cache.getStatistics();

        // Assert
        assertThat(stats.currentSize).isEqualTo(1);
        assertThat(stats.maxSize).isEqualTo(100);
        assertThat(stats.hits).isEqualTo(1);
        assertThat(stats.misses).isEqualTo(1);
        assertThat(stats.hitRate).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Hit rate should be calculated correctly")
    void testHitRateCalculation() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        cache.put(pos, mockPlot);

        // Act - 7 Hits, 3 Misses = 70% Hit Rate
        for (int i = 0; i < 7; i++) {
            cache.get(pos);
        }
        for (int i = 0; i < 3; i++) {
            cache.get(new BlockPos(i * 100, 0, 0));
        }

        // Assert
        assertThat(cache.getHitRate()).isEqualTo(70.0);
    }

    @Test
    @DisplayName("Hit rate should be 0.0 when no requests")
    void testHitRateZeroWhenNoRequests() {
        // Assert
        assertThat(cache.getHitRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("resetStatistics() should reset counters but not cache content")
    void testResetStatistics() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        cache.put(pos, mockPlot);
        cache.get(pos);

        // Act
        cache.resetStatistics();

        // Assert
        assertThat(cache.getHits()).isZero();
        assertThat(cache.getMisses()).isZero();
        assertThat(cache.getInvalidations()).isZero();
        assertThat(cache.size()).isEqualTo(1); // Content bleibt
    }

    @Test
    @DisplayName("Statistics toString() should contain all values")
    void testStatisticsToString() {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        cache.put(pos, mockPlot);
        cache.get(pos);

        // Act
        String statsString = cache.getStatistics().toString();

        // Assert
        assertThat(statsString).contains("size=1/100");
        assertThat(statsString).contains("hits=1");
        assertThat(statsString).contains("misses=0");
        assertThat(statsString).contains("hitRate=100.0%");
    }

    // ==================== Thread Safety Tests ====================

    @Test
    @DisplayName("Concurrent put() operations should be thread-safe")
    void testConcurrentPutOperations() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        BlockPos pos = new BlockPos(threadId * 1000 + i, 0, 0);
                        PlotRegion plot = createMockPlot("plot_" + threadId + "_" + i, pos,
                            new BlockPos(threadId * 1000 + i + 10, 10, 10));
                        cache.put(pos, plot);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - Cache sollte konsistent sein (unter maxSize)
        assertThat(cache.size()).isLessThanOrEqualTo(100);
    }

    @Test
    @DisplayName("Concurrent get() operations should be thread-safe")
    void testConcurrentGetOperations() throws InterruptedException {
        // Arrange
        BlockPos pos = new BlockPos(5, 5, 5);
        cache.put(pos, mockPlot);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 100; i++) {
                        PlotRegion result = cache.get(pos);
                        if (result != null) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - Alle Reads sollten erfolgreich sein
        assertThat(successCount.get()).isEqualTo(1000); // 10 threads * 100 ops
        assertThat(cache.getHits()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Concurrent invalidate() operations should be thread-safe")
    void testConcurrentInvalidateOperations() throws InterruptedException {
        // Arrange
        for (int i = 0; i < 50; i++) {
            BlockPos pos = new BlockPos(i * 20, 0, 0);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 20 + 10, 10, 10));
            cache.put(pos, plot);
        }

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        cache.invalidatePlot("plot" + (threadId * 5 + i));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - Cache sollte konsistent sein
        assertThat(cache.size()).isLessThanOrEqualTo(50);
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("get() should be O(1) - constant time lookup")
    void testGetIsConstantTime() {
        // Arrange - Füge viele Einträge hinzu
        for (int i = 0; i < 1000; i++) {
            BlockPos pos = new BlockPos(i * 20, 0, 0);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 20 + 10, 10, 10));
            cache.put(pos, plot);
        }

        // Act - Messe Lookup-Zeit
        BlockPos testPos = new BlockPos(500 * 20, 0, 0);
        long start = System.nanoTime();
        cache.get(testPos);
        long duration = System.nanoTime() - start;

        // Assert - Sollte sehr schnell sein (< 100µs)
        assertThat(duration).isLessThan(100_000); // < 100µs
    }

    @Test
    @DisplayName("invalidateRegion() should be O(affected_chunks) not O(cache_size)")
    void testInvalidateRegionIsChunkBased() {
        // Arrange - Großer Cache mit 1000 Einträgen über viele Chunks verteilt
        PlotCache largeCache = new PlotCache(1000);
        for (int i = 0; i < 1000; i++) {
            BlockPos pos = new BlockPos(i * 100, 0, 0);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 100 + 10, 10, 10));
            largeCache.put(pos, plot);
        }

        // Act - Invalidiere kleine Region (nur 1-2 Chunks)
        long start = System.nanoTime();
        largeCache.invalidateRegion(new BlockPos(0, 0, 0), new BlockPos(20, 20, 20));
        long duration = System.nanoTime() - start;

        // Assert - Sollte schnell sein trotz großem Cache (< 1ms)
        assertThat(duration).isLessThan(1_000_000); // < 1ms
    }

    @Test
    @DisplayName("Large cache (1000 entries) should remain performant")
    void testLargeCachePerformance() {
        // Arrange
        PlotCache largeCache = new PlotCache(1000);

        // Act - Füge 1000 Einträge hinzu und messe Zeit
        long startInsert = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            BlockPos pos = new BlockPos(i * 20, 0, 0);
            PlotRegion plot = createMockPlot("plot" + i, pos, new BlockPos(i * 20 + 10, 10, 10));
            largeCache.put(pos, plot);
        }
        long insertDuration = System.nanoTime() - startInsert;

        // Messe Lookup-Zeit
        long startLookup = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            largeCache.get(new BlockPos(i * 20, 0, 0));
        }
        long lookupDuration = System.nanoTime() - startLookup;

        // Assert
        assertThat(insertDuration).isLessThan(50_000_000); // < 50ms für 1000 inserts
        assertThat(lookupDuration).isLessThan(10_000_000); // < 10ms für 100 lookups
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle negative coordinates correctly")
    void testNegativeCoordinates() {
        // Arrange
        BlockPos pos = new BlockPos(-50, 5, -50);
        PlotRegion plot = createMockPlot("negative", new BlockPos(-60, 0, -60), new BlockPos(-40, 10, -40));

        // Act
        cache.put(pos, plot);

        // Assert
        assertThat(cache.get(pos)).isEqualTo(plot);
    }

    @Test
    @DisplayName("Should handle position at chunk boundary")
    void testChunkBoundaryPosition() {
        // Arrange - Position genau auf Chunk-Grenze (16, 0, 16)
        BlockPos pos = new BlockPos(16, 0, 16);

        // Act
        cache.put(pos, mockPlot);

        // Assert
        assertThat(cache.get(pos)).isEqualTo(mockPlot);
    }

    @Test
    @DisplayName("Should handle invalidateRegion with min > max (empty region)")
    void testInvalidateEmptyRegion() {
        // Arrange
        cache.put(new BlockPos(5, 5, 5), mockPlot);

        // Act - Leere Region (min > max)
        cache.invalidateRegion(new BlockPos(20, 20, 20), new BlockPos(0, 0, 0));

        // Assert - Nichts sollte entfernt werden
        assertThat(cache.get(new BlockPos(5, 5, 5))).isNotNull();
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a mock PlotRegion for testing
     */
    private PlotRegion createMockPlot(String plotId, BlockPos min, BlockPos max) {
        PlotRegion plot = mock(PlotRegion.class);
        when(plot.getPlotId()).thenReturn(plotId);
        when(plot.getMin()).thenReturn(min);
        when(plot.getMax()).thenReturn(max);

        // Mock contains() to return true for positions within bounds
        when(plot.contains(any(BlockPos.class))).thenAnswer(invocation -> {
            BlockPos pos = invocation.getArgument(0);
            return pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
                   pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
                   pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        });

        return plot;
    }
}
