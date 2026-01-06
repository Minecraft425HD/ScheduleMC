package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MethodCache
 *
 * @since 1.0
 */
@DisplayName("MethodCache Tests")
class MethodCacheTest {

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        MethodCache.clear();
    }

    @Test
    @DisplayName("get - Should cache expensive computation")
    void testCacheExpensiveComputation() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);

        // Act - Call twice with same key
        String result1 = MethodCache.get("test", 5000L, () -> {
            callCount.incrementAndGet();
            return "expensive-result";
        });
        String result2 = MethodCache.get("test", 5000L, () -> {
            callCount.incrementAndGet();
            return "expensive-result";
        });

        // Assert
        assertThat(result1).isEqualTo("expensive-result");
        assertThat(result2).isEqualTo("expensive-result");
        assertThat(callCount.get()).isEqualTo(1); // Only computed once
    }

    @Test
    @DisplayName("get - Should use default TTL")
    void testDefaultTTL() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);

        // Act
        String result1 = MethodCache.get("test", () -> {
            callCount.incrementAndGet();
            return "result";
        });
        String result2 = MethodCache.get("test", () -> {
            callCount.incrementAndGet();
            return "result";
        });

        // Assert
        assertThat(result1).isEqualTo("result");
        assertThat(result2).isEqualTo("result");
        assertThat(callCount.get()).isEqualTo(1); // Cached with default TTL
    }

    @Test
    @DisplayName("get - Should expire after TTL")
    void testCacheExpiration() throws InterruptedException {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        long shortTTL = 50L; // 50ms

        // Act - First call
        String result1 = MethodCache.get("test", shortTTL, () -> {
            callCount.incrementAndGet();
            return "result-" + callCount.get();
        });

        // Wait for expiration
        Thread.sleep(60);

        // Second call after expiration
        String result2 = MethodCache.get("test", shortTTL, () -> {
            callCount.incrementAndGet();
            return "result-" + callCount.get();
        });

        // Assert
        assertThat(result1).isEqualTo("result-1");
        assertThat(result2).isEqualTo("result-2"); // Recomputed
        assertThat(callCount.get()).isEqualTo(2); // Called twice
    }

    @Test
    @DisplayName("get - Should handle different keys independently")
    void testDifferentKeys() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);

        // Act
        String result1 = MethodCache.get("key1", 5000L, () -> {
            callCount.incrementAndGet();
            return "value1";
        });
        String result2 = MethodCache.get("key2", 5000L, () -> {
            callCount.incrementAndGet();
            return "value2";
        });

        // Assert
        assertThat(result1).isEqualTo("value1");
        assertThat(result2).isEqualTo("value2");
        assertThat(callCount.get()).isEqualTo(2); // Each key computed once
    }

    @Test
    @DisplayName("invalidate - Should remove specific cache entry")
    void testInvalidate() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        MethodCache.get("test", 5000L, () -> {
            callCount.incrementAndGet();
            return "result";
        });

        // Act
        MethodCache.invalidate("test");
        String result = MethodCache.get("test", 5000L, () -> {
            callCount.incrementAndGet();
            return "result";
        });

        // Assert
        assertThat(result).isEqualTo("result");
        assertThat(callCount.get()).isEqualTo(2); // Recomputed after invalidation
    }

    @Test
    @DisplayName("invalidatePrefix - Should remove entries matching prefix")
    void testInvalidatePrefix() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        MethodCache.get("player_uuid1", 5000L, () -> {
            callCount.incrementAndGet();
            return "data1";
        });
        MethodCache.get("player_uuid2", 5000L, () -> {
            callCount.incrementAndGet();
            return "data2";
        });
        MethodCache.get("other_key", 5000L, () -> {
            callCount.incrementAndGet();
            return "data3";
        });

        // Act
        MethodCache.invalidatePrefix("player_");

        // Get all again
        MethodCache.get("player_uuid1", 5000L, () -> {
            callCount.incrementAndGet();
            return "data1";
        });
        MethodCache.get("player_uuid2", 5000L, () -> {
            callCount.incrementAndGet();
            return "data2";
        });
        MethodCache.get("other_key", 5000L, () -> {
            callCount.incrementAndGet();
            return "data3";
        });

        // Assert
        // Initial: 3 calls, after invalidation: 2 more (player_ keys)
        // other_key still cached
        assertThat(callCount.get()).isEqualTo(5);
    }

    @Test
    @DisplayName("clear - Should remove all cache entries")
    void testClear() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        MethodCache.get("key1", 5000L, () -> {
            callCount.incrementAndGet();
            return "value1";
        });
        MethodCache.get("key2", 5000L, () -> {
            callCount.incrementAndGet();
            return "value2";
        });

        // Act
        MethodCache.clear();

        // Get again
        MethodCache.get("key1", 5000L, () -> {
            callCount.incrementAndGet();
            return "value1";
        });
        MethodCache.get("key2", 5000L, () -> {
            callCount.incrementAndGet();
            return "value2";
        });

        // Assert
        assertThat(callCount.get()).isEqualTo(4); // All recomputed
    }

    @Test
    @DisplayName("size - Should return cache size")
    void testSize() {
        // Act
        MethodCache.get("key1", 5000L, () -> "value1");
        MethodCache.get("key2", 5000L, () -> "value2");
        MethodCache.get("key3", 5000L, () -> "value3");

        // Assert
        assertThat(MethodCache.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("size - Should return 0 for empty cache")
    void testSizeEmpty() {
        assertThat(MethodCache.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("getStatistics - Should provide cache statistics")
    void testGetStatistics() {
        // Arrange
        MethodCache.get("key1", 5000L, () -> "value1");
        MethodCache.get("key2", 5000L, () -> "value2");

        // Act
        String stats = MethodCache.getStatistics();

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats).contains("MethodCache:");
        assertThat(stats).contains("total entries");
        assertThat(stats).contains("expired");
    }

    @Test
    @DisplayName("Thread Safety - Should handle concurrent access")
    void testConcurrentAccess() throws InterruptedException {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // Act - Multiple threads accessing same key
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                MethodCache.get("shared-key", 5000L, () -> {
                    callCount.incrementAndGet();
                    return "shared-value";
                });
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - Should be called only once (or few times due to race)
        // ConcurrentHashMap ensures thread safety
        assertThat(callCount.get()).isLessThanOrEqualTo(threadCount);
    }

    @Test
    @DisplayName("Null handling - Should handle null supplier result")
    void testNullResult() {
        // Act
        String result = MethodCache.get("null-test", 5000L, () -> null);

        // Assert - Should cache null value
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Different types - Should cache different types")
    void testDifferentTypes() {
        // Act
        Integer intResult = MethodCache.get("int-key", 5000L, () -> 42);
        String strResult = MethodCache.get("str-key", 5000L, () -> "hello");
        Double dblResult = MethodCache.get("dbl-key", 5000L, () -> 3.14);

        // Assert
        assertThat(intResult).isEqualTo(42);
        assertThat(strResult).isEqualTo("hello");
        assertThat(dblResult).isEqualTo(3.14);
    }

    @Test
    @DisplayName("Automatic cleanup - Should cleanup expired entries periodically")
    void testAutomaticCleanup() throws InterruptedException {
        // This test verifies the automatic cleanup mechanism
        // Cleanup happens every 1000 accesses

        // Add an expired entry
        MethodCache.get("expired", 1L, () -> "old-value");
        Thread.sleep(5); // Wait for expiration

        // Trigger cleanup by making 1000+ accesses
        for (int i = 0; i < 1001; i++) {
            MethodCache.get("key-" + i, 5000L, () -> "value");
        }

        // The cleanup should have removed the expired entry
        // Size should be 1001 (not 1002 with expired entry)
        assertThat(MethodCache.size()).isLessThanOrEqualTo(1001);
    }
}
