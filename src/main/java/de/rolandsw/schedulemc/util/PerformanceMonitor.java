package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance Monitoring & Metrics System
 *
 * Ermöglicht das Tracking von Operation-Zeiten und Häufigkeiten
 * für Performance-Analyse und Bottleneck-Identifikation.
 *
 * Features:
 * - Thread-safe Operation Tracking
 * - Min/Max/Average Berechnungen
 * - Automatische Statistik-Aggregation
 * - Periodische Reports
 * - Low Overhead (AtomicLong operations)
 *
 * Usage:
 * ```java
 * long start = System.nanoTime();
 * // ... operation ...
 * long duration = System.nanoTime() - start;
 * PerformanceMonitor.recordOperation("plot_lookup", duration);
 * ```
 *
 * @author ScheduleMC Team
 * @since 3.1.0
 */
public class PerformanceMonitor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<String, OperationStats> stats = new ConcurrentHashMap<>();
    private static volatile boolean enabled = true;

    /**
     * Aktiviert/Deaktiviert Performance Monitoring
     *
     * @param enabled true = aktiviert, false = deaktiviert
     */
    public static void setEnabled(boolean enabled) {
        PerformanceMonitor.enabled = enabled;
        LOGGER.info("Performance Monitoring {}", enabled ? "enabled" : "disabled");
    }

    /**
     * Prüft ob Monitoring aktiviert ist
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Zeichnet eine Operation auf (in Nanosekunden)
     *
     * @param operationName Name der Operation
     * @param durationNanos Dauer in Nanosekunden
     */
    public static void recordOperation(String operationName, long durationNanos) {
        if (!enabled) {
            return;
        }

        OperationStats stat = stats.computeIfAbsent(operationName, k -> new OperationStats());
        stat.record(durationNanos);
    }

    /**
     * Zeichnet eine Operation auf (in Millisekunden)
     *
     * @param operationName Name der Operation
     * @param durationMs Dauer in Millisekunden
     */
    public static void recordOperationMs(String operationName, double durationMs) {
        recordOperation(operationName, (long) (durationMs * 1_000_000));
    }

    /**
     * Wrapper für automatisches Timing
     *
     * Usage:
     * ```java
     * try (var timer = PerformanceMonitor.startTimer("my_operation")) {
     *     // ... operation ...
     * }
     * ```
     */
    public static AutoCloseable startTimer(String operationName) {
        if (!enabled) {
            return () -> {}; // No-op
        }

        long startTime = System.nanoTime();
        return () -> {
            long duration = System.nanoTime() - startTime;
            recordOperation(operationName, duration);
        };
    }

    /**
     * Gibt Statistiken für eine Operation zurück
     *
     * @param operationName Name der Operation
     * @return OperationStats oder null wenn nicht gefunden
     */
    public static OperationStats getStats(String operationName) {
        return stats.get(operationName);
    }

    /**
     * Gibt alle Statistiken zurück
     */
    public static Map<String, OperationStats> getAllStats() {
        return new ConcurrentHashMap<>(stats);
    }

    /**
     * Setzt alle Statistiken zurück
     */
    public static void resetAll() {
        stats.clear();
        LOGGER.info("Performance statistics reset");
    }

    /**
     * Setzt Statistiken für eine Operation zurück
     */
    public static void reset(String operationName) {
        stats.remove(operationName);
        LOGGER.info("Performance statistics reset for: {}", operationName);
    }

    /**
     * Gibt einen formatierten Performance Report aus
     */
    public static void printReport() {
        if (stats.isEmpty()) {
            LOGGER.info("No performance data collected");
            return;
        }

        LOGGER.info("╔═══════════════════════════════════════════════════════════════════╗");
        LOGGER.info("║              PERFORMANCE MONITORING REPORT                        ║");
        LOGGER.info("╠═══════════════════════════════════════════════════════════════════╣");
        LOGGER.info("║ Operation                   │  Count │   Avg  │   Min  │   Max    ║");
        LOGGER.info("╠═══════════════════════════════════════════════════════════════════╣");

        stats.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().totalTime.get(), a.getValue().totalTime.get()))
            .forEach(entry -> {
                String name = entry.getKey();
                OperationStats stat = entry.getValue();

                LOGGER.info("║ {:27} │ {:6} │ {:5.2f}ms │ {:5.2f}ms │ {:5.2f}ms ║",
                    truncate(name, 27),
                    stat.count.get(),
                    stat.getAverageMs(),
                    stat.getMinMs(),
                    stat.getMaxMs()
                );
            });

        LOGGER.info("╚═══════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Gibt einen kompakten Performance Report zurück
     */
    public static String getCompactReport() {
        if (stats.isEmpty()) {
            return "No performance data";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Performance Report:\n");

        stats.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().totalTime.get(), a.getValue().totalTime.get()))
            .limit(10)  // Top 10
            .forEach(entry -> {
                String name = entry.getKey();
                OperationStats stat = entry.getValue();

                sb.append(String.format("  %s: %.2fms avg (count=%d)\n",
                    name, stat.getAverageMs(), stat.count.get()));
            });

        return sb.toString();
    }

    private static String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Operation Statistics Klasse
     * Thread-safe mit AtomicLong
     */
    public static class OperationStats {
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(0);

        /**
         * Zeichnet eine Messung auf
         *
         * @param durationNanos Dauer in Nanosekunden
         */
        public void record(long durationNanos) {
            count.incrementAndGet();
            totalTime.addAndGet(durationNanos);

            // Update min
            long currentMin;
            do {
                currentMin = minTime.get();
                if (durationNanos >= currentMin) {
                    break;
                }
            } while (!minTime.compareAndSet(currentMin, durationNanos));

            // Update max
            long currentMax;
            do {
                currentMax = maxTime.get();
                if (durationNanos <= currentMax) {
                    break;
                }
            } while (!maxTime.compareAndSet(currentMax, durationNanos));
        }

        public long getCount() {
            return count.get();
        }

        public double getAverageMs() {
            long c = count.get();
            if (c == 0) return 0;
            return (totalTime.get() / (double) c) / 1_000_000.0;
        }

        public double getMinMs() {
            long min = minTime.get();
            if (min == Long.MAX_VALUE) return 0;
            return min / 1_000_000.0;
        }

        public double getMaxMs() {
            return maxTime.get() / 1_000_000.0;
        }

        public double getTotalMs() {
            return totalTime.get() / 1_000_000.0;
        }

        @Override
        public String toString() {
            return String.format("OperationStats{count=%d, avg=%.2fms, min=%.2fms, max=%.2fms}",
                count.get(), getAverageMs(), getMinMs(), getMaxMs());
        }
    }
}
