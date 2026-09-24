package de.rolandsw.schedulemc.mapview.data.persistence;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.util.ThreadPoolManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public final class AsyncPersistenceManager {
    // MIGRATED: Now using ThreadPoolManager for centralized thread management
    // executorService → ThreadPoolManager.getComputationPool() (map calculations)
    // saveExecutorService → ThreadPoolManager.getIOPool() (save operations)
    // DEPRECATED: Use getExecutorService() instead - kept for backward compatibility
    @Deprecated
    public static ExecutorService executorService = ThreadPoolManager.getComputationPool();  // NOPMD
    @Deprecated
    public static ExecutorService saveExecutorService = ThreadPoolManager.getIOPool();  // NOPMD

    // Legacy queue reference (for emptyQueue compatibility)
    static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private AsyncPersistenceManager() {}

    /**
     * Gets the executor service for map calculations (computation pool)
     * This method ensures the pool is reinitialized if it was shut down
     *
     * @return ExecutorService for computations
     */
    public static ExecutorService getExecutorService() {  // NOPMD
        return ThreadPoolManager.getComputationPool();
    }

    /**
     * Gets the executor service for save operations (IO pool)
     * This method ensures the pool is reinitialized if it was shut down
     *
     * @return ExecutorService for I/O operations
     */
    public static ExecutorService getSaveExecutorService() {  // NOPMD
        return ThreadPoolManager.getIOPool();
    }

    public static void emptyQueue() {
        // MIGRATED: ThreadPoolManager handles purging internally
        // Legacy method kept for compatibility but is now a no-op
        MapViewConstants.getLogger().debug("emptyQueue() called - managed by ThreadPoolManager");
    }

    public static void flushSaveQueue() {
        // MIGRATED: ThreadPoolManager handles shutdown globally
        // This method is now a no-op - flush happens in ScheduleMC.onServerStopping
        MapViewConstants.getLogger().info("flushSaveQueue() called - handled by ThreadPoolManager.shutdown()");
    }
}