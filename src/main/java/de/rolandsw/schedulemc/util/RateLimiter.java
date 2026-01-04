package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiter für DoS-Protection
 *
 * Verhindert dass Spieler den Server mit zu vielen Operationen pro Sekunde überfluten.
 *
 * Features:
 * - Sliding Window Rate Limiting
 * - Thread-Safe mit ConcurrentHashMap
 * - Automatische Cleanup alter Einträge
 * - Konfigurierbare Limits pro Operation
 *
 * @author ScheduleMC Team
 * @since 3.1.0
 */
public class RateLimiter {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<UUID, OperationWindow> windows = new ConcurrentHashMap<>();
    private final int maxOperationsPerWindow;
    private final long windowSizeMs;
    private final String operationName;

    /**
     * Erstellt einen neuen Rate Limiter
     *
     * @param operationName Name der Operation (für Logging)
     * @param maxOperationsPerWindow Maximale Anzahl Operationen pro Zeitfenster
     * @param windowSizeMs Größe des Zeitfensters in Millisekunden
     */
    public RateLimiter(String operationName, int maxOperationsPerWindow, long windowSizeMs) {
        this.operationName = operationName;
        this.maxOperationsPerWindow = maxOperationsPerWindow;
        this.windowSizeMs = windowSizeMs;
    }

    /**
     * Erstellt einen Rate Limiter mit Standard-Fenster von 1 Sekunde
     *
     * @param operationName Name der Operation
     * @param maxOperationsPerSecond Max Operationen pro Sekunde
     */
    public RateLimiter(String operationName, int maxOperationsPerSecond) {
        this(operationName, maxOperationsPerSecond, 1000L);
    }

    /**
     * Prüft ob eine Operation erlaubt ist
     *
     * @param playerUUID UUID des Spielers
     * @return true wenn Operation erlaubt, false wenn Rate Limit erreicht
     */
    public boolean allowOperation(UUID playerUUID) {
        long now = System.currentTimeMillis();

        OperationWindow window = windows.computeIfAbsent(
            playerUUID,
            k -> new OperationWindow(now)
        );

        // Prüfe ob Fenster abgelaufen ist
        if (now - window.windowStart >= windowSizeMs) {
            // Neues Fenster starten
            window.reset(now);
        }

        int currentCount = window.count.incrementAndGet();

        if (currentCount > maxOperationsPerWindow) {
            // Rate Limit erreicht
            if (currentCount == maxOperationsPerWindow + 1) {
                // Logge nur beim ersten Überschreiten
                LOGGER.warn("Rate limit exceeded for player {} on operation '{}': {} ops/{}ms (limit: {})",
                    playerUUID, operationName, currentCount - 1, windowSizeMs, maxOperationsPerWindow);
            }
            return false;
        }

        return true;
    }

    /**
     * Setzt den Rate Limiter für einen Spieler zurück
     *
     * @param playerUUID UUID des Spielers
     */
    public void reset(UUID playerUUID) {
        windows.remove(playerUUID);
    }

    /**
     * Setzt alle Rate Limiter zurück
     */
    public void resetAll() {
        windows.clear();
    }

    /**
     * Entfernt alte Einträge (für Spieler die offline sind)
     * Sollte periodisch aufgerufen werden
     *
     * @param maxAgeMs Maximales Alter in Millisekunden
     * @return Anzahl entfernter Einträge
     */
    public int cleanupOldEntries(long maxAgeMs) {
        long now = System.currentTimeMillis();
        int removed = 0;

        windows.entrySet().removeIf(entry -> {
            if (now - entry.getValue().windowStart > maxAgeMs) {
                removed++;
                return true;
            }
            return false;
        });

        if (removed > 0) {
            LOGGER.debug("Cleaned up {} old rate limiter entries for '{}'", removed, operationName);
        }

        return removed;
    }

    /**
     * Gibt aktuelle Statistiken zurück
     */
    public RateLimiterStats getStats() {
        return new RateLimiterStats(
            windows.size(),
            maxOperationsPerWindow,
            windowSizeMs,
            operationName
        );
    }

    /**
     * Operation Window für Sliding Window Rate Limiting
     */
    private static class OperationWindow {
        volatile long windowStart;
        final AtomicInteger count;

        OperationWindow(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(0);
        }

        void reset(long newWindowStart) {
            this.windowStart = newWindowStart;
            this.count.set(0);
        }
    }

    /**
     * Statistik-Datenklasse
     */
    public static class RateLimiterStats {
        public final int activeWindows;
        public final int maxOpsPerWindow;
        public final long windowSizeMs;
        public final String operationName;

        public RateLimiterStats(int activeWindows, int maxOpsPerWindow,
                               long windowSizeMs, String operationName) {
            this.activeWindows = activeWindows;
            this.maxOpsPerWindow = maxOpsPerWindow;
            this.windowSizeMs = windowSizeMs;
            this.operationName = operationName;
        }

        @Override
        public String toString() {
            return String.format("RateLimiter[%s]: %d active, limit=%d/%dms",
                operationName, activeWindows, maxOpsPerWindow, windowSizeMs);
        }
    }
}
