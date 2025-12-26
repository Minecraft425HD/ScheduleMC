package de.rolandsw.schedulemc.lightmap.persistent;

import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

public final class ThreadManager {
    // Performance-Optimierung: Nutze bis zu 75% der CPU-Cores (min 2, max 16)
    // Vorher: Hard-Cap bei 4 Threads → unterausgelastet auf modernen CPUs
    static final int concurrentThreads = Math.min(Math.max(Runtime.getRuntime().availableProcessors() * 3 / 4, 2), 16);
    // Performance-Optimierung: Core-Threads für bessere Responsiveness (50% der max Threads)
    static final int coreThreads = Math.max(concurrentThreads / 2, 1);

    static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    // Performance-Optimierung: corePoolSize > 0 für sofortige Verfügbarkeit (vorher: 0)
    public static final ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreThreads, concurrentThreads, 60L, TimeUnit.SECONDS, queue);
    // Performance-Optimierung: Bounded queue für Save-Operations (10000 Tasks max)
    public static ThreadPoolExecutor saveExecutorService = new ThreadPoolExecutor(coreThreads, concurrentThreads, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000));

    private ThreadManager() {}

    public static void emptyQueue() {
        for (Runnable runnable : queue) {
            if (runnable instanceof FutureTask) {
                ((FutureTask<?>) runnable).cancel(false);
            }
        }

        executorService.purge();
    }

    public static void flushSaveQueue() {
        saveExecutorService.shutdown();
        try {
            while (!saveExecutorService.awaitTermination(240, TimeUnit.SECONDS)) {
                LightMapConstants.getLogger().info("Waiting for map save... (" + saveExecutorService.getQueue().size() + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Performance-Optimierung: Nutze optimierte ThreadPool-Parameter
        saveExecutorService = new ThreadPoolExecutor(coreThreads, concurrentThreads, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000));
        saveExecutorService.setThreadFactory(new NamedThreadFactory("LightMap WorldMap Saver Thread"));
        LightMapConstants.getLogger().info("Save queue flushed!");
    }

    static {
        executorService.setThreadFactory(new NamedThreadFactory("LightMap WorldMap Calculation Thread"));
        saveExecutorService.setThreadFactory(new NamedThreadFactory("LightMap WorldMap Saver Thread"));
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final String name;
        private final AtomicInteger threadCount = new AtomicInteger(1);

        private NamedThreadFactory(String name) { this.name = name; }

        @Override
        public Thread newThread(@NotNull Runnable r) { return new Thread(r, this.name + " " + this.threadCount.getAndIncrement()); }
    }
}