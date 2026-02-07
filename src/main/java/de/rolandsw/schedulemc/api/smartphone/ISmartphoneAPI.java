package de.rolandsw.schedulemc.api.smartphone;

import java.util.Set;
import java.util.UUID;

/**
 * Public Smartphone API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Smartphone-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Smartphone-GUI-Tracking (welche Spieler haben Smartphone geöffnet)</li>
 *   <li>Schutz-Mechanismus während Smartphone-Nutzung</li>
 *   <li>Integration mit Achievement-System</li>
 *   <li>Integration mit Messaging-System</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch ConcurrentHashMap.newKeySet()-basiertes Tracking.
 *
 * <h2>Schutz-Mechanismus:</h2>
 * Spieler mit geöffnetem Smartphone können nicht:
 * - Von NPCs angegriffen werden
 * - Von anderen Spielern angegriffen werden
 * - Items verlieren
 * <p>
 * Dies verhindert unfaire Todesfälle während der GUI-Nutzung.
 *
 * <h2>Smartphone-Apps:</h2>
 * <ul>
 *   <li>Messaging - Nachrichten an andere Spieler senden</li>
 *   <li>Achievements - Fortschritt und freigeschaltete Erfolge anzeigen</li>
 *   <li>Market - Marktpreise und Trends einsehen</li>
 *   <li>Plots - Eigene Grundstücke verwalten</li>
 *   <li>Bank - Kontostand und Transaktionen anzeigen</li>
 * </ul>
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * ISmartphoneAPI smartphoneAPI = ScheduleMCAPI.getSmartphoneAPI();
 *
 * // Smartphone öffnen (Schutz aktivieren)
 * smartphoneAPI.setSmartphoneOpen(playerUUID, true);
 *
 * // Prüfen ob Smartphone geöffnet ist
 * if (smartphoneAPI.hasSmartphoneOpen(playerUUID)) {
 *     // Schutz ist aktiv - kein Schaden möglich
 * }
 *
 * // Smartphone schließen (Schutz deaktivieren)
 * smartphoneAPI.setSmartphoneOpen(playerUUID, false);
 *
 * // Alle Spieler mit geöffnetem Smartphone
 * Set<UUID> playersWithSmartphones = smartphoneAPI.getPlayersWithSmartphoneOpen();
 *
 * // Bei Disconnect: Spieler aus Tracking entfernen
 * smartphoneAPI.removePlayer(playerUUID);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface ISmartphoneAPI {

    /**
     * Setzt ob ein Spieler das Smartphone geöffnet hat.
     * <p>
     * Bei true: Schutz-Mechanismus wird aktiviert
     * Bei false: Schutz-Mechanismus wird deaktiviert
     *
     * @param playerUUID Die UUID des Spielers
     * @param open true wenn Smartphone geöffnet, false wenn geschlossen
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void setSmartphoneOpen(UUID playerUUID, boolean open);

    /**
     * Prüft ob ein Spieler das Smartphone geöffnet hat.
     *
     * @param playerUUID Die UUID des Spielers
     * @return true wenn Smartphone geöffnet (Schutz aktiv)
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    boolean hasSmartphoneOpen(UUID playerUUID);

    /**
     * Entfernt einen Spieler aus dem Tracking.
     * <p>
     * Sollte beim Disconnect aufgerufen werden um Speicher freizugeben.
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void removePlayer(UUID playerUUID);

    /**
     * Gibt alle Spieler zurück die aktuell das Smartphone geöffnet haben.
     *
     * @return Set aller UUIDs mit geöffnetem Smartphone (unveränderbare Kopie)
     */
    Set<UUID> getPlayersWithSmartphoneOpen();

    /**
     * Löscht alle Tracking-Daten.
     * <p>
     * WARNUNG: Sollte nur bei Server-Shutdown oder für Tests verwendet werden.
     */
    void clearAllTracking();

    /**
     * Gibt die Anzahl der Spieler mit geöffnetem Smartphone zurück.
     *
     * @return Anzahl der Spieler
     */
    int getOpenSmartphoneCount();

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * Registers a custom app for the smartphone.
     *
     * @param appId Unique app identifier
     * @param appName Display name
     * @param iconColor Color code (e.g. "§aGreen")
     * @return true if registered successfully
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    boolean registerApp(String appId, String appName, String iconColor);

    /**
     * Unregisters a custom app.
     *
     * @param appId The app identifier
     * @return true if unregistered successfully
     * @throws IllegalArgumentException if appId is null
     * @since 3.2.0
     */
    boolean unregisterApp(String appId);

    /**
     * Returns all registered app IDs.
     *
     * @return Set of app identifiers
     * @since 3.2.0
     */
    java.util.Set<String> getRegisteredApps();

    /**
     * Sends a notification to a player's smartphone.
     *
     * @param playerUUID The player's UUID
     * @param appId The app sending the notification
     * @param message Notification message
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    void sendNotification(UUID playerUUID, String appId, String message);

    /**
     * Checks if a player has the smartphone item.
     *
     * @param playerUUID The player's UUID
     * @return true if player has smartphone
     * @throws IllegalArgumentException if playerUUID is null
     * @since 3.2.0
     */
    boolean hasSmartphone(UUID playerUUID);
}
