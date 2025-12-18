package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-Limiting für Economy-Commands
 * Verhindert Spam und potentielle Exploits
 */
public class RateLimiter {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Maximale Anzahl an Transaktionen pro Minute
    private static final int MAX_TRANSACTIONS_PER_MINUTE = 10;
    private static final long MINUTE_IN_MILLIS = 60000;

    // Spieler -> Liste von Timestamps der letzten Transaktionen
    private static final Map<UUID, TransactionTracker> trackers = new ConcurrentHashMap<>();

    /**
     * Prüft ob Spieler eine weitere Transaktion durchführen darf
     */
    public static boolean canPerformTransaction(UUID playerUUID) {
        TransactionTracker tracker = trackers.computeIfAbsent(playerUUID, k -> new TransactionTracker());
        return tracker.canPerformTransaction();
    }

    /**
     * Registriert eine Transaktion
     */
    public static void recordTransaction(UUID playerUUID) {
        TransactionTracker tracker = trackers.computeIfAbsent(playerUUID, k -> new TransactionTracker());
        tracker.recordTransaction();
    }

    /**
     * Gibt verbleibende Zeit bis zur nächsten Transaktion zurück (in Sekunden)
     */
    public static int getSecondsUntilNextTransaction(UUID playerUUID) {
        TransactionTracker tracker = trackers.get(playerUUID);
        if (tracker == null) {
            return 0;
        }
        return tracker.getSecondsUntilNext();
    }

    /**
     * Gibt Anzahl der Transaktionen in der letzten Minute zurück
     */
    public static int getTransactionCount(UUID playerUUID) {
        TransactionTracker tracker = trackers.get(playerUUID);
        if (tracker == null) {
            return 0;
        }
        return tracker.getTransactionCount();
    }

    /**
     * Löscht alle Tracking-Daten eines Spielers
     */
    public static void clearPlayer(UUID playerUUID) {
        trackers.remove(playerUUID);
    }

    /**
     * Innere Klasse für das Tracking einzelner Spieler
     */
    private static class TransactionTracker {
        private final java.util.Deque<Long> timestamps = new java.util.ArrayDeque<>();

        public boolean canPerformTransaction() {
            cleanOldTimestamps();
            return timestamps.size() < MAX_TRANSACTIONS_PER_MINUTE;
        }

        public void recordTransaction() {
            cleanOldTimestamps();
            timestamps.add(System.currentTimeMillis());
        }

        public int getTransactionCount() {
            cleanOldTimestamps();
            return timestamps.size();
        }

        public int getSecondsUntilNext() {
            cleanOldTimestamps();
            if (timestamps.size() < MAX_TRANSACTIONS_PER_MINUTE) {
                return 0;
            }

            // Ältester Timestamp + 1 Minute = wann nächste Transaktion möglich
            Long oldest = timestamps.peekFirst();
            if (oldest == null) {
                return 0;
            }

            long nextAllowed = oldest + MINUTE_IN_MILLIS;
            long now = System.currentTimeMillis();
            long diff = nextAllowed - now;

            return (int) Math.ceil(diff / 1000.0);
        }

        private void cleanOldTimestamps() {
            long now = System.currentTimeMillis();
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - MINUTE_IN_MILLIS) {
                timestamps.removeFirst();
            }
        }
    }
}
