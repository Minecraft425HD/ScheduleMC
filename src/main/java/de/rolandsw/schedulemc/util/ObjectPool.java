package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Thread-safe object pool for expensive-to-create objects.
 * <p>
 * Reduces object allocation overhead by reusing instances. Particularly useful for:
 * <ul>
 *   <li>StringBuilder instances (frequent string concatenation)</li>
 *   <li>Byte arrays (packet handling)</li>
 *   <li>Collections (temporary data structures)</li>
 *   <li>Any expensive-to-create objects</li>
 * </ul>
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Thread-safe borrowing and returning</li>
 *   <li>Automatic object reset before reuse</li>
 *   <li>Configurable min/max pool size</li>
 *   <li>Lazy object creation</li>
 *   <li>Pool statistics for monitoring</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Create pool for StringBuilders with capacity 256
 * ObjectPool<StringBuilder> stringBuilderPool = new ObjectPool<>(
 *     () -> new StringBuilder(256),  // Factory
 *     sb -> sb.setLength(0),         // Reset function
 *     10,  // Min size
 *     50   // Max size
 * );
 *
 * // Borrow object
 * StringBuilder sb = stringBuilderPool.borrow();
 * try {
 *     sb.append("Hello ");
 *     sb.append("World");
 *     String result = sb.toString();
 * } finally {
 *     // Always return in finally block
 *     stringBuilderPool.returnObject(sb);
 * }
 *
 * // Or use try-with-resources pattern
 * try (PooledObject<StringBuilder> pooled = stringBuilderPool.borrowAutoReturn()) {
 *     StringBuilder sb = pooled.get();
 *     sb.append("Hello World");
 * } // Automatically returned
 * }</pre>
 *
 * @param <T> Type of pooled objects
 * @author ScheduleMC Development Team
 * @since 3.2.0
 */
public class ObjectPool<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Supplier<T> factory;
    private final Consumer<T> resetFunction;
    private final BlockingQueue<T> pool;
    private final int minSize;
    private final int maxSize;
    private final String poolName;

    // Statistics
    private final AtomicInteger createdCount = new AtomicInteger(0);
    private final AtomicInteger borrowCount = new AtomicInteger(0);
    private final AtomicInteger returnCount = new AtomicInteger(0);

    /**
     * Creates an object pool with specified configuration.
     *
     * @param factory Function to create new instances
     * @param resetFunction Function to reset objects before reuse
     * @param minSize Minimum pool size (pre-created objects)
     * @param maxSize Maximum pool size
     * @throws IllegalArgumentException if minSize > maxSize or sizes < 0
     */
    public ObjectPool(@Nonnull Supplier<T> factory,
                     @Nonnull Consumer<T> resetFunction,
                     int minSize,
                     int maxSize) {
        this("UnnamedPool", factory, resetFunction, minSize, maxSize);
    }

    /**
     * Creates a named object pool with specified configuration.
     *
     * @param poolName Name for logging and monitoring
     * @param factory Function to create new instances
     * @param resetFunction Function to reset objects before reuse
     * @param minSize Minimum pool size (pre-created objects)
     * @param maxSize Maximum pool size
     */
    public ObjectPool(@Nonnull String poolName,
                     @Nonnull Supplier<T> factory,
                     @Nonnull Consumer<T> resetFunction,
                     int minSize,
                     int maxSize) {
        if (minSize < 0 || maxSize < 0 || minSize > maxSize) {
            throw new IllegalArgumentException(
                String.format("Invalid pool sizes: min=%d, max=%d", minSize, maxSize)
            );
        }

        this.poolName = poolName;
        this.factory = factory;
        this.resetFunction = resetFunction;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.pool = new LinkedBlockingQueue<>(maxSize);

        // Pre-create minimum objects
        for (int i = 0; i < minSize; i++) {
            pool.offer(createNewObject());
        }

        LOGGER.debug("ObjectPool[{}] created with min={}, max={}", poolName, minSize, maxSize);
    }

    /**
     * Borrows an object from the pool.
     * <p>
     * If pool is empty, creates a new object (up to maxSize).
     * Blocks if pool is at max capacity.
     * </p>
     *
     * @return Pooled object
     */
    @Nonnull
    public T borrow() {
        borrowCount.incrementAndGet();

        T object = pool.poll();
        if (object != null) {
            return object;
        }

        // Pool empty - create new if under max size
        if (createdCount.get() < maxSize) {
            return createNewObject();
        }

        // At max capacity - wait for available object
        try {
            object = pool.take(); // Blocks until available
            return object;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Pool[{}] interrupted while waiting for object", poolName);
            // Fallback: create new object anyway
            return factory.get();
        }
    }

    /**
     * Borrows an object with timeout.
     *
     * @param timeout Maximum wait time
     * @param unit Time unit
     * @return Pooled object or null if timeout
     */
    @Nonnull
    public T borrow(long timeout, @Nonnull TimeUnit unit) {
        borrowCount.incrementAndGet();

        T object = pool.poll();
        if (object != null) {
            return object;
        }

        // Pool empty - create new if under max size
        if (createdCount.get() < maxSize) {
            return createNewObject();
        }

        // At max capacity - wait with timeout
        try {
            object = pool.poll(timeout, unit);
            if (object != null) {
                return object;
            }
            LOGGER.warn("Pool[{}] timeout waiting for object", poolName);
            return factory.get(); // Fallback
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Pool[{}] interrupted while waiting for object", poolName);
            return factory.get();
        }
    }

    /**
     * Borrows object with automatic return via try-with-resources.
     *
     * @return Pooled object wrapper
     */
    @Nonnull
    public PooledObject<T> borrowAutoReturn() {
        return new PooledObject<>(borrow(), this);
    }

    /**
     * Returns an object to the pool.
     * <p>
     * Object is reset before being returned to pool.
     * If pool is full, object is discarded.
     * </p>
     *
     * @param object Object to return
     */
    public void returnObject(@Nonnull T object) {
        returnCount.incrementAndGet();

        try {
            // Reset object state
            resetFunction.accept(object);

            // Return to pool if not full
            if (!pool.offer(object)) {
                // Pool full - discard object
                LOGGER.trace("Pool[{}] full, discarding object", poolName);
            }
        } catch (Exception e) {
            LOGGER.error("Pool[{}] error resetting object: {}", poolName, e.getMessage());
            // Don't return broken object to pool
        }
    }

    /**
     * Gets current pool size (available objects).
     *
     * @return Number of available objects
     */
    public int getAvailableCount() {
        return pool.size();
    }

    /**
     * Gets total created object count.
     *
     * @return Number of objects created
     */
    public int getCreatedCount() {
        return createdCount.get();
    }

    /**
     * Gets borrow count.
     *
     * @return Number of borrow operations
     */
    public int getBorrowCount() {
        return borrowCount.get();
    }

    /**
     * Gets return count.
     *
     * @return Number of return operations
     */
    public int getReturnCount() {
        return returnCount.get();
    }

    /**
     * Gets pool utilization as percentage.
     *
     * @return Utilization (0.0 - 100.0)
     */
    public double getUtilization() {
        int created = createdCount.get();
        if (created == 0) return 0.0;
        int inUse = created - pool.size();
        return (inUse * 100.0) / created;
    }

    /**
     * Gets pool statistics.
     *
     * @return Statistics string
     */
    public String getStats() {
        return String.format(
            "ObjectPool[%s]: available=%d, created=%d, borrows=%d, returns=%d, utilization=%.1f%%",
            poolName, getAvailableCount(), getCreatedCount(),
            getBorrowCount(), getReturnCount(), getUtilization()
        );
    }

    /**
     * Clears the pool.
     */
    public void clear() {
        pool.clear();
        LOGGER.debug("Pool[{}] cleared", poolName);
    }

    private T createNewObject() {
        T object = factory.get();
        createdCount.incrementAndGet();
        return object;
    }

    /**
     * Auto-closeable wrapper for pooled objects.
     *
     * @param <T> Object type
     */
    public static class PooledObject<T> implements AutoCloseable {
        private final T object;
        private final ObjectPool<T> pool;
        private boolean returned = false;

        public PooledObject(T object, ObjectPool<T> pool) {
            this.object = object;
            this.pool = pool;
        }

        /**
         * Gets the pooled object.
         *
         * @return Pooled object
         */
        public T get() {
            return object;
        }

        @Override
        public void close() {
            if (!returned) {
                pool.returnObject(object);
                returned = true;
            }
        }
    }
}
