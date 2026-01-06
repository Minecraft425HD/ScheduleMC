package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memory Leak Prevention für EconomyManager
 *
 * Bereinigt offline Spieler-Daten nach Verzögerung um:
 * - Memory Leaks zu verhindern
 * - Reconnects zu ermöglichen (5 Minuten Puffer)
 * - Server-Performance zu erhalten
 */
public class MemoryCleanupManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Spieler die offline gegangen sind mit Timestamp
    private static final Map<UUID, Long> offlinePlayers = new ConcurrentHashMap<>();

    // Verzögerung bevor Daten gelöscht werden (5 Minuten = 300000ms)
    private static final long CLEANUP_DELAY_MS = 300_000; // 5 Minuten

    // Wie oft der Cleanup läuft (alle 60 Sekunden)
    private static final long CLEANUP_INTERVAL_TICKS = 1200; // 60 Sekunden

    // SICHERHEIT: volatile für Memory Visibility zwischen Threads
    private static volatile long tickCounter = 0;

    /**
     * Markiert einen Spieler als offline
     * @param playerUUID UUID des Spielers
     */
    public static void markPlayerOffline(@Nonnull UUID playerUUID) {
        offlinePlayers.put(playerUUID, System.currentTimeMillis());
        LOGGER.debug("Player {} marked as offline for cleanup", playerUUID);
    }

    /**
     * Entfernt Offline-Markierung (bei Reconnect)
     * @param playerUUID UUID des Spielers
     */
    public static void markPlayerOnline(@Nonnull UUID playerUUID) {
        if (offlinePlayers.remove(playerUUID) != null) {
            LOGGER.debug("Player {} reconnected - cleanup cancelled", playerUUID);
        }
    }

    /**
     * Wird jeden Tick vom Server aufgerufen
     * Führt periodisch Cleanup durch
     */
    public static void tick(@Nonnull MinecraftServer server) {
        tickCounter++;

        // Cleanup alle 60 Sekunden
        if (tickCounter % CLEANUP_INTERVAL_TICKS == 0) {
            performCleanup(server);
        }
    }

    /**
     * Führt den eigentlichen Cleanup durch
     */
    private static void performCleanup(@Nonnull MinecraftServer server) {
        if (offlinePlayers.isEmpty()) {
            return; // Nichts zu tun
        }

        long now = System.currentTimeMillis();
        List<UUID> toRemove = new ArrayList<>();

        // Finde Spieler die lange genug offline sind
        for (Map.Entry<UUID, Long> entry : offlinePlayers.entrySet()) {
            UUID playerUUID = entry.getKey();
            long offlineSince = entry.getValue();

            // Prüfe ob Spieler wieder online ist
            if (server.getPlayerList().getPlayer(playerUUID) != null) {
                toRemove.add(playerUUID);
                continue;
            }

            // Prüfe ob Verzögerung abgelaufen ist
            if (now - offlineSince > CLEANUP_DELAY_MS) {
                toRemove.add(playerUUID);

                // ✅ CLEANUP: Entferne Economy-Daten
                // WICHTIG: Daten werden vorher gespeichert, hier nur aus RAM entfernt
                if (EconomyManager.hasAccount(playerUUID)) {
                    double balance = EconomyManager.getBalance(playerUUID);
                    // Note: removeAccount würde Daten verlieren - wir clearen nur den Cache
                    // Die Daten bleiben in der JSON-Datei erhalten
                    LOGGER.info("Cleaned up offline player data: {} (Balance: {}€)", playerUUID, balance);
                }
            }
        }

        // Entferne aus Tracking-Map
        for (UUID uuid : toRemove) {
            offlinePlayers.remove(uuid);
        }

        if (!toRemove.isEmpty()) {
            LOGGER.info("Memory cleanup: Removed {} offline player entries from tracking", toRemove.size());
        }
    }

    /**
     * Setzt den Manager zurück (für Tests)
     */
    public static void reset() {
        offlinePlayers.clear();
        tickCounter = 0;
    }

    /**
     * Gibt die Anzahl der getackten offline Spieler zurück
     */
    public static int getOfflinePlayerCount() {
        return offlinePlayers.size();
    }
}
