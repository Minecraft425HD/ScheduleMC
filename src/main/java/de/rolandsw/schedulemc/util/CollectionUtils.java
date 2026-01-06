package de.rolandsw.schedulemc.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * High-performance utility methods for collection operations.
 *
 * <p>Provides optimized alternatives to common collection operations
 * that are frequently used in hot code paths (tick handlers, event handlers).</p>
 *
 * <p><strong>Performance benefits:</strong></p>
 * <ul>
 *   <li>Pre-sized collections to avoid resizing</li>
 *   <li>Optimized filtering without intermediate collections</li>
 *   <li>Batch operations with minimal allocations</li>
 *   <li>Specialized methods for common use cases</li>
 * </ul>
 *
 * @since 1.0
 */
public class CollectionUtils {

    /**
     * Creates a pre-sized ArrayList to avoid resizing overhead.
     *
     * <p>Use when you know the approximate size in advance:</p>
     * <pre>{@code
     * List<Player> players = CollectionUtils.newArrayListWithCapacity(
     *     server.getPlayerCount()
     * );
     * }</pre>
     *
     * @param initialCapacity Expected number of elements
     * @param <T> Element type
     * @return Pre-sized ArrayList
     */
    public static <T> ArrayList<T> newArrayListWithCapacity(int initialCapacity) {
        return new ArrayList<>(Math.max(1, initialCapacity));
    }

    /**
     * Creates a pre-sized HashMap to avoid resizing overhead.
     *
     * <p>Calculates optimal capacity to avoid rehashing:</p>
     * <pre>{@code
     * Map<UUID, Player> playerMap = CollectionUtils.newHashMapWithCapacity(100);
     * }</pre>
     *
     * @param expectedSize Expected number of entries
     * @param <K> Key type
     * @param <V> Value type
     * @return Pre-sized HashMap
     */
    public static <K, V> HashMap<K, V> newHashMapWithCapacity(int expectedSize) {
        return new HashMap<>(calculateHashMapCapacity(expectedSize));
    }

    /**
     * Creates a pre-sized HashSet to avoid resizing overhead.
     *
     * @param expectedSize Expected number of elements
     * @param <T> Element type
     * @return Pre-sized HashSet
     */
    public static <T> HashSet<T> newHashSetWithCapacity(int expectedSize) {
        return new HashSet<>(calculateHashMapCapacity(expectedSize));
    }

    /**
     * Calculates optimal HashMap capacity to avoid rehashing.
     *
     * <p>Formula: capacity = expectedSize / loadFactor + 1</p>
     *
     * @param expectedSize Expected number of entries
     * @return Optimal initial capacity
     */
    private static int calculateHashMapCapacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        }
        // Default load factor is 0.75, so capacity = size / 0.75 + 1
        return (int) ((expectedSize / 0.75f) + 1);
    }

    /**
     * Filters a collection in-place without creating intermediate collections.
     *
     * <p>Performance optimization: Avoids stream overhead for simple filtering:</p>
     * <pre>{@code
     * List<Entity> entities = new ArrayList<>(world.getAllEntities());
     * CollectionUtils.filterInPlace(entities, entity -> entity.isAlive());
     * }</pre>
     *
     * @param collection Collection to filter (modified in-place)
     * @param predicate Filter condition
     * @param <T> Element type
     */
    public static <T> void filterInPlace(List<T> collection, Predicate<T> predicate) {
        collection.removeIf(predicate.negate());
    }

    /**
     * Finds first element matching predicate without stream overhead.
     *
     * <p>Faster than {@code stream().filter().findFirst()} for small collections:</p>
     * <pre>{@code
     * Player targetPlayer = CollectionUtils.findFirst(
     *     players,
     *     p -> p.getName().equals("Steve")
     * );
     * }</pre>
     *
     * @param collection Collection to search
     * @param predicate Match condition
     * @param <T> Element type
     * @return First matching element or null
     */
    public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
        for (T element : collection) {
            if (predicate.test(element)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Checks if any element matches predicate without stream overhead.
     *
     * <p>Faster than {@code stream().anyMatch()} for small collections:</p>
     *
     * @param collection Collection to search
     * @param predicate Match condition
     * @param <T> Element type
     * @return true if any element matches
     */
    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        for (T element : collection) {
            if (predicate.test(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts elements matching predicate without stream overhead.
     *
     * @param collection Collection to count in
     * @param predicate Match condition
     * @param <T> Element type
     * @return Number of matching elements
     */
    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        long count = 0;
        for (T element : collection) {
            if (predicate.test(element)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Partitions collection into two lists based on predicate.
     *
     * <p>Single-pass partitioning without intermediate collections:</p>
     * <pre>{@code
     * PartitionResult<Player> partition = CollectionUtils.partition(
     *     players,
     *     player -> player.isOp()
     * );
     * List<Player> ops = partition.matching;
     * List<Player> nonOps = partition.nonMatching;
     * }</pre>
     *
     * @param collection Collection to partition
     * @param predicate Partition condition
     * @param <T> Element type
     * @return Partition result with matching and non-matching elements
     */
    public static <T> PartitionResult<T> partition(Collection<T> collection, Predicate<T> predicate) {
        List<T> matching = newArrayListWithCapacity(collection.size() / 2);
        List<T> nonMatching = newArrayListWithCapacity(collection.size() / 2);

        for (T element : collection) {
            if (predicate.test(element)) {
                matching.add(element);
            } else {
                nonMatching.add(element);
            }
        }

        return new PartitionResult<>(matching, nonMatching);
    }

    /**
     * Result of partitioning operation.
     *
     * @param <T> Element type
     */
    public static class PartitionResult<T> {
        /** Elements matching the predicate */
        public final List<T> matching;

        /** Elements not matching the predicate */
        public final List<T> nonMatching;

        PartitionResult(List<T> matching, List<T> nonMatching) {
            this.matching = matching;
            this.nonMatching = nonMatching;
        }
    }

    /**
     * Checks if collection is null or empty.
     *
     * @param collection Collection to check
     * @return true if null or empty
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if map is null or empty.
     *
     * @param map Map to check
     * @return true if null or empty
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Gets first element of list or default value.
     *
     * @param list List to get from
     * @param defaultValue Value to return if list is empty
     * @param <T> Element type
     * @return First element or default
     */
    public static <T> T getFirstOrDefault(List<T> list, T defaultValue) {
        return isNullOrEmpty(list) ? defaultValue : list.get(0);
    }

    /**
     * Gets last element of list or default value.
     *
     * @param list List to get from
     * @param defaultValue Value to return if list is empty
     * @param <T> Element type
     * @return Last element or default
     */
    public static <T> T getLastOrDefault(List<T> list, T defaultValue) {
        return isNullOrEmpty(list) ? defaultValue : list.get(list.size() - 1);
    }

    private CollectionUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
}
