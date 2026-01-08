package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Centralized cache management system with TTL support.
 * <p>
 * Provides a unified interface for caching across the entire mod with:
 * <ul>
 *   <li>Multiple cache instances with different TTL policies</li>
 *   <li>Automatic expiration of stale entries</li>
 *   <li>Thread-safe operations</li>
 *   <li>Cache statistics for monitoring</li>
 *   <li>Automatic cleanup of expired entries</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Get or create a named cache with 5-second TTL
 * CacheManager.Cache<String, PlayerData> playerCache = CacheManager.getCache(
 *     "players",
 *     GameConstants.DEFAULT_CACHE_TTL_MS
 * );
 *
 * // Get with automatic computation if missing
 * PlayerData data = playerCache.computeIfAbsent(
 *     playerUUID.toString(),
 *     key -> loadPlayerData(playerUUID)
 * );
 *
 * // Get with manual check
 * PlayerData cached = playerCache.get(playerUUID.toString());
 * if (cached == null) {
 *     cached = loadPlayerData(playerUUID);
 *     playerCache.put(playerUUID.toString(), cached);
 * }
 * }</pre>
 *
 * @author ScheduleMC Development Team
 * @since 3.2.0
 */
public class CacheManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Global cache registry
    private static final Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();

    // Cleanup thread for expired entries
    private static volatile boolean cleanupRunning = false;
    private static final long CLEANUP_INTERVAL_MS = 60_000L; // 1 minute

    /**
     * Gets or creates a named cache with specified TTL.
     *
     * @param name Unique cache name
     * @param ttlMs Time-to-live in milliseconds
     * @param <K> Key type
     * @param <V> Value type
     * @return Cache instance
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> getCache(@Nonnull String name, long ttlMs) {
        return (Cache<K, V>) caches.computeIfAbsent(name, k -> new Cache<>(name, ttlMs));
    }

    /**
     * Gets or creates a named cache with default TTL.
     *
     * @param name Unique cache name
     * @param <K> Key type
     * @param <V> Value type
     * @return Cache instance
     */
    public static <K, V> Cache<K, V> getCache(@Nonnull String name) {
        return getCache(name, GameConstants.DEFAULT_CACHE_TTL_MS);
    }

    /**
     * Clears all caches.
     */
    public static void clearAll() {
        LOGGER.info("Clearing all caches ({} caches)", caches.size());
        for (Cache<?, ?> cache : caches.values()) {
            cache.clear();
        }
    }

    /**
     * Gets global cache statistics.
     *
     * @return Statistics summary
     */
    public static String getGlobalStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("═══ Cache Statistics ═══\n");
        stats.append(String.format("Total caches: %d\n", caches.size()));

        long totalEntries = 0;
        long totalHits = 0;
        long totalMisses = 0;

        for (Cache<?, ?> cache : caches.values()) {
            totalEntries += cache.size();
            totalHits += cache.getHitCount();
            totalMisses += cache.getMissCount();
        }

        stats.append(String.format("Total entries: %d\n", totalEntries));
        stats.append(String.format("Total hits: %d\n", totalHits));
        stats.append(String.format("Total misses: %d\n", totalMisses));

        if (totalHits + totalMisses > 0) {
            double hitRate = (totalHits * 100.0) / (totalHits + totalMisses);
            stats.append(String.format("Hit rate: %.2f%%\n", hitRate));
        }

        return stats.toString();
    }

    /**
     * Starts automatic cleanup thread for expired entries.
     */
    public static synchronized void startCleanup() {
        if (cleanupRunning) {
            return;
        }

        cleanupRunning = true;
        Thread cleanupThread = new Thread(() -> {
            LOGGER.info("Cache cleanup thread started");
            while (cleanupRunning) {
                try {
                    Thread.sleep(CLEANUP_INTERVAL_MS);
                    cleanupExpired();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            LOGGER.info("Cache cleanup thread stopped");
        }, "CacheManager-Cleanup");

        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    /**
     * Stops automatic cleanup thread.
     */
    public static synchronized void stopCleanup() {
        cleanupRunning = false;
    }

    /**
     * Manually triggers cleanup of expired entries across all caches.
     */
    private static void cleanupExpired() {
        long totalRemoved = 0;
        for (Cache<?, ?> cache : caches.values()) {
            totalRemoved += cache.cleanupExpired();
        }
        if (totalRemoved > 0) {
            LOGGER.debug("Cleaned up {} expired cache entries", totalRemoved);
        }
    }

    /**
     * TTL-based cache implementation.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    public static class Cache<K, V> {
        private final String name;
        private final long ttlMs;
        private final Map<K, CacheEntry<V>> entries;

        // Statistics
        private long hitCount = 0;
        private long missCount = 0;

        public Cache(String name, long ttlMs) {
            this.name = name;
            this.ttlMs = ttlMs;
            this.entries = new ConcurrentHashMap<>();
        }

        /**
         * Gets a value from cache if not expired.
         *
         * @param key Cache key
         * @return Cached value or null if not found/expired
         */
        @Nullable
        public V get(@Nonnull K key) {
            CacheEntry<V> entry = entries.get(key);
            if (entry == null) {
                missCount++;
                return null;
            }

            if (entry.isExpired()) {
                entries.remove(key);
                missCount++;
                return null;
            }

            hitCount++;
            return entry.value;
        }

        /**
         * Puts a value into cache with current timestamp.
         *
         * @param key Cache key
         * @param value Value to cache
         */
        public void put(@Nonnull K key, @Nonnull V value) {
            entries.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
        }

        /**
         * Computes value if absent and caches it.
         *
         * @param key Cache key
         * @param mappingFunction Function to compute value
         * @return Cached or computed value
         */
        @Nonnull
        public V computeIfAbsent(@Nonnull K key, @Nonnull Function<K, V> mappingFunction) {
            V value = get(key);
            if (value != null) {
                return value;
            }

            value = mappingFunction.apply(key);
            put(key, value);
            return value;
        }

        /**
         * Gets or computes value with supplier.
         *
         * @param key Cache key
         * @param supplier Supplier to compute value
         * @return Cached or computed value
         */
        @Nonnull
        public V getOrCompute(@Nonnull K key, @Nonnull Supplier<V> supplier) {
            V value = get(key);
            if (value != null) {
                return value;
            }

            value = supplier.get();
            put(key, value);
            return value;
        }

        /**
         * Removes a key from cache.
         *
         * @param key Cache key
         */
        public void remove(@Nonnull K key) {
            entries.remove(key);
        }

        /**
         * Clears all entries from cache.
         */
        public void clear() {
            entries.clear();
            hitCount = 0;
            missCount = 0;
        }

        /**
         * Removes all expired entries.
         *
         * @return Number of entries removed
         */
        public long cleanupExpired() {
            long removed = entries.entrySet().removeIf(entry -> entry.getValue().isExpired());
            return removed;
        }

        /**
         * Gets number of entries in cache (including expired).
         *
         * @return Entry count
         */
        public int size() {
            return entries.size();
        }

        /**
         * Gets hit count.
         *
         * @return Number of cache hits
         */
        public long getHitCount() {
            return hitCount;
        }

        /**
         * Gets miss count.
         *
         * @return Number of cache misses
         */
        public long getMissCount() {
            return missCount;
        }

        /**
         * Gets hit rate as percentage.
         *
         * @return Hit rate (0.0 - 100.0)
         */
        public double getHitRate() {
            long total = hitCount + missCount;
            if (total == 0) return 0.0;
            return (hitCount * 100.0) / total;
        }

        /**
         * Gets cache name.
         *
         * @return Cache name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets cache statistics.
         *
         * @return Statistics string
         */
        public String getStats() {
            return String.format("Cache[%s]: size=%d, hits=%d, misses=%d, hitRate=%.2f%%",
                name, size(), hitCount, missCount, getHitRate());
        }

        /**
         * Cache entry with timestamp.
         */
        private class CacheEntry<T> {
            final T value;
            final long timestamp;

            CacheEntry(T value, long timestamp) {
                this.value = value;
                this.timestamp = timestamp;
            }

            boolean isExpired() {
                return (System.currentTimeMillis() - timestamp) > ttlMs;
            }
        }
    }

    private CacheManager() {
        throw new UnsupportedOperationException("Utility class");
    }
}
