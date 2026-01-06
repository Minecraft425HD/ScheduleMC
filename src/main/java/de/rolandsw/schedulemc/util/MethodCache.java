package de.rolandsw.schedulemc.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Generic method-level caching utility for expensive operations.
 *
 * <p>Performance optimization: Caches results of expensive computations
 * with configurable TTL (time-to-live) to avoid redundant calculations.</p>
 *
 * <p><strong>Use cases:</strong></p>
 * <ul>
 *   <li>Database queries</li>
 *   <li>Complex calculations</li>
 *   <li>Network operations</li>
 *   <li>Expensive lookups</li>
 * </ul>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * // Cache expensive calculation for 5 seconds
 * double result = MethodCache.get("expensiveCalc", 5000L, () -> {
 *     return performExpensiveCalculation();
 * });
 * }</pre>
 *
 * @since 1.0
 */
public class MethodCache {

    /**
     * Cache entry holding value and expiration time
     */
    private static class CacheEntry<T> {
        final T value;
        final long expiresAt;

        CacheEntry(T value, long ttlMillis) {
            this.value = value;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    // Thread-safe cache storage
    private static final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    // Cleanup threshold - cleanup every 1000 cache accesses
    private static final int CLEANUP_THRESHOLD = 1000;
    private static volatile int accessCount = 0;

    /**
     * Gets a value from cache or computes it if missing/expired.
     *
     * <p>Thread-safe: Multiple threads can safely access the cache concurrently.</p>
     *
     * @param key Unique cache key
     * @param ttlMillis Time-to-live in milliseconds
     * @param supplier Function to compute value if not cached
     * @param <T> Type of cached value
     * @return Cached or freshly computed value
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, long ttlMillis, Supplier<T> supplier) {
        // Periodic cleanup of expired entries
        if (++accessCount >= CLEANUP_THRESHOLD) {
            cleanupExpired();
            accessCount = 0;
        }

        CacheEntry<?> entry = cache.get(key);

        // Cache hit - valid entry
        if (entry != null && !entry.isExpired()) {
            return (T) entry.value;
        }

        // Cache miss or expired - compute new value
        T value = supplier.get();

        // Store in cache with TTL
        cache.put(key, new CacheEntry<>(value, ttlMillis));

        return value;
    }

    /**
     * Gets a value with default TTL of 5 seconds.
     *
     * @param key Unique cache key
     * @param supplier Function to compute value if not cached
     * @param <T> Type of cached value
     * @return Cached or freshly computed value
     */
    public static <T> T get(String key, Supplier<T> supplier) {
        return get(key, GameConstants.DEFAULT_CACHE_TTL_MS, supplier);
    }

    /**
     * Invalidates (removes) a specific cache entry.
     *
     * @param key Cache key to invalidate
     */
    public static void invalidate(String key) {
        cache.remove(key);
    }

    /**
     * Invalidates all cache entries matching a prefix.
     *
     * <p>Useful for invalidating related cache entries:</p>
     * <pre>{@code
     * MethodCache.invalidatePrefix("player_" + uuid);
     * }</pre>
     *
     * @param prefix Key prefix to match
     */
    public static void invalidatePrefix(String prefix) {
        cache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    /**
     * Clears all cache entries.
     */
    public static void clear() {
        cache.clear();
    }

    /**
     * Removes all expired cache entries.
     *
     * <p>Called automatically every {@link #CLEANUP_THRESHOLD} accesses
     * to prevent memory leaks from expired entries.</p>
     */
    private static void cleanupExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Gets current cache size (number of entries).
     *
     * @return Number of cached entries
     */
    public static int size() {
        return cache.size();
    }

    /**
     * Gets cache statistics for monitoring.
     *
     * @return Statistics string
     */
    public static String getStatistics() {
        long expired = cache.values().stream()
            .filter(CacheEntry::isExpired)
            .count();

        return String.format("MethodCache: %d total entries, %d expired",
            cache.size(), expired);
    }

    private MethodCache() {
        throw new UnsupportedOperationException("Utility class");
    }
}
