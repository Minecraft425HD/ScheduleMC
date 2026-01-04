package de.rolandsw.schedulemc.api.messaging;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Public Messaging API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Nachrichten-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Spieler-zu-Spieler Nachrichten</li>
 *   <li>Nachrichtenverlauf</li>
 *   <li>Gelesen/Ungelesen Status</li>
 *   <li>Nachrichtenfilterung</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch ConcurrentHashMap.
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IMessagingAPI messagingAPI = ScheduleMCAPI.getMessagingAPI();
 *
 * // Nachricht senden
 * messagingAPI.sendMessage(fromUUID, toUUID, "Hallo!");
 *
 * // Ungelesene Nachrichten
 * int unread = messagingAPI.getUnreadMessageCount(playerUUID);
 *
 * // Nachrichten abrufen
 * List<Message> messages = messagingAPI.getMessages(playerUUID, 10);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IMessagingAPI {

    /**
     * Sendet eine Nachricht an einen Spieler.
     *
     * @param fromUUID UUID des Absenders
     * @param toUUID UUID des Empfängers
     * @param message Der Nachrichtentext
     * @return true wenn erfolgreich gesendet
     * @throws IllegalArgumentException wenn Parameter null oder message leer
     */
    boolean sendMessage(UUID fromUUID, UUID toUUID, String message);

    /**
     * Gibt die Anzahl ungelesener Nachrichten zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @return Anzahl ungelesener Nachrichten
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    int getUnreadMessageCount(UUID playerUUID);

    /**
     * Gibt die letzten N Nachrichten eines Spielers zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @param limit Maximale Anzahl Nachrichten
     * @return Liste der Nachrichten (neueste zuerst)
     * @throws IllegalArgumentException wenn playerUUID null oder limit < 1
     */
    List<String> getMessages(UUID playerUUID, int limit);

    /**
     * Markiert alle Nachrichten als gelesen.
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void markAllAsRead(UUID playerUUID);

    /**
     * Löscht eine Nachricht.
     *
     * @param playerUUID Die UUID des Spielers
     * @param messageId Die Nachrichten-ID
     * @return true wenn erfolgreich gelöscht
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    boolean deleteMessage(UUID playerUUID, String messageId);

    /**
     * Löscht alle Nachrichten eines Spielers.
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void deleteAllMessages(UUID playerUUID);

    /**
     * Gibt die Gesamtanzahl aller Nachrichten zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @return Gesamtanzahl Nachrichten
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    int getTotalMessageCount(UUID playerUUID);
}
