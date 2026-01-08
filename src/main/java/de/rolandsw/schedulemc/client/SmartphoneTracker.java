package de.rolandsw.schedulemc.client;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trackt welche Spieler aktuell das Smartphone-GUI geöffnet haben
 * Server-Side Tracking für Schutz-Mechanismus
 * SICHERHEIT: Thread-safe Set für concurrent access von mehreren Spielern
 */
public class SmartphoneTracker {

    // SICHERHEIT: ConcurrentHashMap.newKeySet() für Thread-safe Set-Operationen
    private static final Set<UUID> playersWithSmartphoneOpen = ConcurrentHashMap.newKeySet();

    /**
     * Registriert dass ein Spieler das Smartphone geöffnet hat
     */
    public static void setSmartphoneOpen(UUID playerUUID, boolean open) {
        if (open) {
            playersWithSmartphoneOpen.add(playerUUID);
        } else {
            playersWithSmartphoneOpen.remove(playerUUID);
        }
    }

    /**
     * Prüft ob ein Spieler das Smartphone geöffnet hat
     */
    public static boolean hasSmartphoneOpen(UUID playerUUID) {
        return playersWithSmartphoneOpen.contains(playerUUID);
    }

    /**
     * Entfernt einen Spieler aus dem Tracking (z.B. bei Disconnect)
     */
    public static void removePlayer(UUID playerUUID) {
        playersWithSmartphoneOpen.remove(playerUUID);
    }

    /**
     * Löscht alle Tracking-Daten
     */
    public static void clear() {
        playersWithSmartphoneOpen.clear();
    }
}
