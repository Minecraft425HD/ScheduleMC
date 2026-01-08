package de.rolandsw.schedulemc.mapview.data.persistence;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.util.ThreadPoolManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

public final class AsyncPersistenceManager {
    // MIGRATED: Now using ThreadPoolManager for centralized thread management
    // executorService → ThreadPoolManager.getComputationPool() (map calculations)
    // saveExecutorService → ThreadPoolManager.getIOPool() (save operations)
    public static final ExecutorService executorService = ThreadPoolManager.getComputationPool();
    public static final ExecutorService saveExecutorService = ThreadPoolManager.getIOPool();

    // Legacy queue reference (for emptyQueue compatibility)
    static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private AsyncPersistenceManager() {}

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