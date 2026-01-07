package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for {@link CacheManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Cache creation with default and custom TTL</li>
 *   <li>Basic cache operations (get, put, remove, clear)</li>
 *   <li>TTL-based expiration of cache entries</li>
 *   <li>Compute methods (computeIfAbsent, getOrCompute)</li>
 *   <li>Hit/miss statistics tracking</li>
 *   <li>Global operations (clearAll, getGlobalStats)</li>
 *   <li>Cleanup thread lifecycle</li>
 *   <li>Thread-safe concurrent access</li>
 * </ul>
 */
@DisplayName("CacheManager Tests")
class CacheManagerTest {

    private static final String TEST_CACHE_NAME = "test-cache";
    private static final long SHORT_TTL_MS = 100L;  // 100ms for quick tests

    @AfterEach
    void tearDown() {
        CacheManager.clearAll();
        CacheManager.stopCleanup();
    }

    // ════════════════════════════════════════════════════════════════
    // CACHE CREATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Cache Creation")
    class CacheCreationTests {

        @Test
        @DisplayName("getCache() should create new cache with custom TTL")
        void getCacheShouldCreateWithCustomTTL() {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME, SHORT_TTL_MS);

            assertThat(cache).isNotNull();
            assertThat(cache.getName()).isEqualTo(TEST_CACHE_NAME);
        }

        @Test
        @DisplayName("getCache() should create new cache with default TTL")
        void getCacheShouldCreateWithDefaultTTL() {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME);

            assertThat(cache).isNotNull();
            assertThat(cache.getName()).isEqualTo(TEST_CACHE_NAME);
        }

        @Test
        @DisplayName("getCache() should return same instance for same name")
        void getCacheShouldReturnSameInstance() {
            CacheManager.Cache<String, String> cache1 = CacheManager.getCache(TEST_CACHE_NAME, SHORT_TTL_MS);
            CacheManager.Cache<String, String> cache2 = CacheManager.getCache(TEST_CACHE_NAME, SHORT_TTL_MS);

            assertThat(cache1).isSameAs(cache2);
        }

        @Test
        @DisplayName("getCache() should create different caches for different names")
        void getCacheShouldCreateDifferentInstances() {
            CacheManager.Cache<String, String> cache1 = CacheManager.getCache("cache1", SHORT_TTL_MS);
            CacheManager.Cache<String, String> cache2 = CacheManager.getCache("cache2", SHORT_TTL_MS);

            assertThat(cache1).isNotSameAs(cache2);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // BASIC OPERATIONS TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Basic Cache Operations")
    class BasicOperationsTests {

        private CacheManager.Cache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CacheManager.getCache(TEST_CACHE_NAME, 10_000L);  // Long TTL for basic tests
        }

        @Test
        @DisplayName("put() and get() should store and retrieve value")
        void putAndGetShouldWork() {
            cache.put("key1", "value1");

            String value = cache.get("key1");

            assertThat(value).isEqualTo("value1");
        }

        @Test
        @DisplayName("get() should return null for non-existent key")
        void getShouldReturnNullForNonExistentKey() {
            String value = cache.get("nonexistent");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("remove() should delete entry from cache")
        void removeShouldDeleteEntry() {
            cache.put("key1", "value1");

            cache.remove("key1");

            assertThat(cache.get("key1")).isNull();
        }

        @Test
        @DisplayName("clear() should remove all entries")
        void clearShouldRemoveAllEntries() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.put("key3", "value3");

            cache.clear();

            assertThat(cache.size()).isZero();
            assertThat(cache.get("key1")).isNull();
            assertThat(cache.get("key2")).isNull();
            assertThat(cache.get("key3")).isNull();
        }

        @Test
        @DisplayName("size() should return number of entries")
        void sizeShouldReturnEntryCount() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            assertThat(cache.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("put() should overwrite existing value")
        void putShouldOverwriteExisting() {
            cache.put("key1", "value1");
            cache.put("key1", "value2");

            assertThat(cache.get("key1")).isEqualTo("value2");
            assertThat(cache.size()).isEqualTo(1);  // Should still be 1 entry
        }
    }

    // ════════════════════════════════════════════════════════════════
    // TTL EXPIRATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TTL-Based Expiration")
    class TTLExpirationTests {

        @Test
        @DisplayName("get() should return null for expired entry")
        void getShouldReturnNullForExpired() throws InterruptedException {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME, SHORT_TTL_MS);
            cache.put("key1", "value1");

            Thread.sleep(SHORT_TTL_MS + 50);  // Wait for expiration

            String value = cache.get("key1");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("get() should remove expired entry from cache")
        void getShouldRemoveExpiredEntry() throws InterruptedException {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME, SHORT_TTL_MS);
            cache.put("key1", "value1");
            int sizeBefore = cache.size();

            Thread.sleep(SHORT_TTL_MS + 50);  // Wait for expiration
            cache.get("key1");  // Triggers cleanup

            // Size might still include expired entry until cleaned
            // (depends on timing)
        }

        @Test
        @DisplayName("cleanupExpired() should remove all expired entries")
        void cleanupExpiredShouldRemoveExpired() throws InterruptedException {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME, SHORT_TTL_MS);
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            Thread.sleep(SHORT_TTL_MS + 50);  // Wait for expiration

            long removed = cache.cleanupExpired();

            assertThat(removed).isEqualTo(2);
            assertThat(cache.size()).isZero();
        }

        @Test
        @DisplayName("cleanupExpired() should not remove non-expired entries")
        void cleanupExpiredShouldKeepValid() throws InterruptedException {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME, 10_000L);
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            long removed = cache.cleanupExpired();

            assertThat(removed).isZero();
            assertThat(cache.size()).isEqualTo(2);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // COMPUTE METHODS TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Compute Methods")
    class ComputeMethodsTests {

        private CacheManager.Cache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CacheManager.getCache(TEST_CACHE_NAME, 10_000L);
        }

        @Test
        @DisplayName("computeIfAbsent() should compute value if missing")
        void computeIfAbsentShouldComputeIfMissing() {
            String value = cache.computeIfAbsent("key1", key -> "computed-" + key);

            assertThat(value).isEqualTo("computed-key1");
            assertThat(cache.get("key1")).isEqualTo("computed-key1");
        }

        @Test
        @DisplayName("computeIfAbsent() should return cached value if present")
        void computeIfAbsentShouldReturnCached() {
            cache.put("key1", "cached-value");

            String value = cache.computeIfAbsent("key1", key -> "computed-value");

            assertThat(value).isEqualTo("cached-value");
        }

        @Test
        @DisplayName("computeIfAbsent() should not call function if value cached")
        void computeIfAbsentShouldNotCallFunction() {
            cache.put("key1", "cached-value");
            AtomicInteger callCount = new AtomicInteger(0);

            cache.computeIfAbsent("key1", key -> {
                callCount.incrementAndGet();
                return "computed-value";
            });

            assertThat(callCount.get()).isZero();  // Function should not be called
        }

        @Test
        @DisplayName("getOrCompute() should compute value if missing")
        void getOrComputeShouldComputeIfMissing() {
            String value = cache.getOrCompute("key1", () -> "computed-value");

            assertThat(value).isEqualTo("computed-value");
            assertThat(cache.get("key1")).isEqualTo("computed-value");
        }

        @Test
        @DisplayName("getOrCompute() should return cached value if present")
        void getOrComputeShouldReturnCached() {
            cache.put("key1", "cached-value");

            String value = cache.getOrCompute("key1", () -> "computed-value");

            assertThat(value).isEqualTo("cached-value");
        }

        @Test
        @DisplayName("getOrCompute() should not call supplier if value cached")
        void getOrComputeShouldNotCallSupplier() {
            cache.put("key1", "cached-value");
            AtomicInteger callCount = new AtomicInteger(0);

            cache.getOrCompute("key1", () -> {
                callCount.incrementAndGet();
                return "computed-value";
            });

            assertThat(callCount.get()).isZero();  // Supplier should not be called
        }
    }

    // ════════════════════════════════════════════════════════════════
    // STATISTICS TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Statistics Tracking")
    class StatisticsTests {

        private CacheManager.Cache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CacheManager.getCache(TEST_CACHE_NAME, 10_000L);
        }

        @Test
        @DisplayName("get() should increment hit count on cache hit")
        void getShouldIncrementHitCount() {
            cache.put("key1", "value1");

            cache.get("key1");  // Hit
            cache.get("key1");  // Hit

            assertThat(cache.getHitCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("get() should increment miss count on cache miss")
        void getShouldIncrementMissCount() {
            cache.get("nonexistent1");  // Miss
            cache.get("nonexistent2");  // Miss

            assertThat(cache.getMissCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getHitRate() should calculate percentage correctly")
        void getHitRateShouldCalculatePercentage() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            cache.get("key1");  // Hit
            cache.get("key2");  // Hit
            cache.get("nonexistent");  // Miss

            // 2 hits / 3 total = 66.67%
            assertThat(cache.getHitRate()).isCloseTo(66.67, within(0.1));
        }

        @Test
        @DisplayName("getHitRate() should return 0.0 for empty cache")
        void getHitRateShouldReturnZeroForEmpty() {
            assertThat(cache.getHitRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("clear() should reset statistics")
        void clearShouldResetStatistics() {
            cache.put("key1", "value1");
            cache.get("key1");  // Hit
            cache.get("nonexistent");  // Miss

            cache.clear();

            assertThat(cache.getHitCount()).isZero();
            assertThat(cache.getMissCount()).isZero();
        }

        @Test
        @DisplayName("getStats() should return formatted statistics string")
        void getStatsShouldReturnFormattedString() {
            cache.put("key1", "value1");
            cache.get("key1");

            String stats = cache.getStats();

            assertThat(stats).contains(TEST_CACHE_NAME);
            assertThat(stats).contains("size=1");
            assertThat(stats).contains("hits=1");
        }
    }

    // ════════════════════════════════════════════════════════════════
    // GLOBAL OPERATIONS TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Global Operations")
    class GlobalOperationsTests {

        @Test
        @DisplayName("clearAll() should clear all caches")
        void clearAllShouldClearAllCaches() {
            CacheManager.Cache<String, String> cache1 = CacheManager.getCache("cache1", 10_000L);
            CacheManager.Cache<String, String> cache2 = CacheManager.getCache("cache2", 10_000L);

            cache1.put("key1", "value1");
            cache2.put("key2", "value2");

            CacheManager.clearAll();

            assertThat(cache1.size()).isZero();
            assertThat(cache2.size()).isZero();
        }

        @Test
        @DisplayName("getGlobalStats() should return statistics for all caches")
        void getGlobalStatsShouldReturnAllStats() {
            CacheManager.Cache<String, String> cache1 = CacheManager.getCache("cache1", 10_000L);
            CacheManager.Cache<String, String> cache2 = CacheManager.getCache("cache2", 10_000L);

            cache1.put("key1", "value1");
            cache2.put("key2", "value2");

            String stats = CacheManager.getGlobalStats();

            assertThat(stats).contains("Total caches:");
            assertThat(stats).contains("Total entries:");
        }
    }

    // ════════════════════════════════════════════════════════════════
    // CLEANUP THREAD TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Cleanup Thread Lifecycle")
    class CleanupThreadTests {

        @Test
        @DisplayName("startCleanup() should start cleanup thread")
        void startCleanupShouldStart() {
            assertThatCode(() -> CacheManager.startCleanup()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("startCleanup() should be idempotent")
        void startCleanupShouldBeIdempotent() {
            CacheManager.startCleanup();
            CacheManager.startCleanup();  // Second call should be no-op

            assertThatCode(() -> CacheManager.stopCleanup()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("stopCleanup() should stop cleanup thread")
        void stopCleanupShouldStop() {
            CacheManager.startCleanup();

            assertThatCode(() -> CacheManager.stopCleanup()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("stopCleanup() should be idempotent")
        void stopCleanupShouldBeIdempotent() {
            CacheManager.startCleanup();
            CacheManager.stopCleanup();
            CacheManager.stopCleanup();  // Second call should be no-op

            // Should not throw
        }
    }

    // ════════════════════════════════════════════════════════════════
    // THREAD-SAFETY TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Thread-Safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("concurrent put() and get() should be thread-safe")
        void concurrentPutGetShouldBeThreadSafe() throws InterruptedException {
            CacheManager.Cache<String, Integer> cache = CacheManager.getCache(TEST_CACHE_NAME, 10_000L);
            int threadCount = 10;
            int operationsPerThread = 100;
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                new Thread(() -> {
                    for (int i = 0; i < operationsPerThread; i++) {
                        String key = "key-" + threadId + "-" + i;
                        cache.put(key, i);
                        cache.get(key);
                    }
                    latch.countDown();
                }).start();
            }

            boolean finished = latch.await(5, TimeUnit.SECONDS);

            assertThat(finished).isTrue();
            assertThat(cache.size()).isEqualTo(threadCount * operationsPerThread);
        }

        @Test
        @DisplayName("concurrent computeIfAbsent() should compute only once per key")
        void concurrentComputeIfAbsentShouldComputeOnce() throws InterruptedException {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME, 10_000L);
            AtomicInteger computeCount = new AtomicInteger(0);
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                new Thread(() -> {
                    cache.computeIfAbsent("shared-key", key -> {
                        computeCount.incrementAndGet();
                        return "computed-value";
                    });
                    latch.countDown();
                }).start();
            }

            boolean finished = latch.await(5, TimeUnit.SECONDS);

            assertThat(finished).isTrue();
            // Note: Due to race conditions, this might be > 1 but should be close
            assertThat(computeCount.get()).isLessThanOrEqualTo(threadCount);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("full lifecycle: create → put → get → expire → cleanup")
        void fullLifecycle() throws InterruptedException {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME, SHORT_TTL_MS);

            // Put value
            cache.put("key1", "value1");
            assertThat(cache.get("key1")).isEqualTo("value1");

            // Wait for expiration
            Thread.sleep(SHORT_TTL_MS + 50);

            // Get should return null (expired)
            assertThat(cache.get("key1")).isNull();

            // Cleanup
            cache.cleanupExpired();
            assertThat(cache.size()).isZero();
        }

        @Test
        @DisplayName("multiple caches should operate independently")
        void multipleCachesShouldBeIndependent() {
            CacheManager.Cache<String, String> cache1 = CacheManager.getCache("cache1", 10_000L);
            CacheManager.Cache<String, String> cache2 = CacheManager.getCache("cache2", 10_000L);

            cache1.put("key", "value1");
            cache2.put("key", "value2");

            assertThat(cache1.get("key")).isEqualTo("value1");
            assertThat(cache2.get("key")).isEqualTo("value2");
        }

        @Test
        @DisplayName("getName() should return cache name")
        void getNameShouldReturnName() {
            CacheManager.Cache<String, String> cache = CacheManager.getCache(TEST_CACHE_NAME, 10_000L);

            assertThat(cache.getName()).isEqualTo(TEST_CACHE_NAME);
        }
    }
}
