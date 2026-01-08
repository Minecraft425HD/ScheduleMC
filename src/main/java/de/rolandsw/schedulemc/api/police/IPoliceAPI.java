package de.rolandsw.schedulemc.api.police;

import java.util.UUID;

/**
 * Public Police API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Polizei/Wanted-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>5-Sterne Wanted-System (GTA-Style)</li>
 *   <li>Automatischer Wanted-Level Abbau über Zeit</li>
 *   <li>Escape-Mechanismus (30 Sekunden verstecken = -1 Stern)</li>
 *   <li>Persistente Wanted-Level (Server-Restart sicher)</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch ConcurrentHashMap und atomare Operationen.
 *
 * <h2>Wanted-Level System:</h2>
 * <ul>
 *   <li>0 Sterne = Sauber (keine Fahndung)</li>
 *   <li>1-2 Sterne = Kleinkriminalität (Diebstahl, Sachbeschädigung)</li>
 *   <li>3-4 Sterne = Schwere Straftaten (Raub, Körperverletzung)</li>
 *   <li>5 Sterne = Höchste Fahndungsstufe (Mord, Terrorismus)</li>
 * </ul>
 *
 * <h2>Escape-Mechanismus:</h2>
 * Wenn ein Spieler sich 30 Sekunden (40 Blocks Abstand) vor der Polizei versteckt,
 * wird sein Wanted-Level automatisch um 1 Stern reduziert.
 *
 * <h2>Automatischer Abbau:</h2>
 * Pro Minecraft-Tag ohne Verbrechen sinkt der Wanted-Level um 1 Stern.
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IPoliceAPI policeAPI = ScheduleMCAPI.getPoliceAPI();
 *
 * // Wanted-Level abrufen
 * int wantedLevel = policeAPI.getWantedLevel(playerUUID);
 *
 * // Wanted-Level hinzufügen (z.B. für Verbrechen)
 * policeAPI.addWantedLevel(playerUUID, 2); // +2 Sterne
 *
 * // Wanted-Level löschen (z.B. nach Festnahme)
 * policeAPI.clearWantedLevel(playerUUID);
 *
 * // Escape-Timer starten
 * policeAPI.startEscape(playerUUID);
 *
 * // Prüfen ob Spieler sich versteckt
 * if (policeAPI.isHiding(playerUUID)) {
 *     long timeRemaining = policeAPI.getEscapeTimeRemaining(playerUUID);
 * }
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IPoliceAPI {

    /**
     * Gibt das aktuelle Wanted-Level eines Spielers zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @return Wanted-Level (0-5)
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    int getWantedLevel(UUID playerUUID);

    /**
     * Fügt Wanted-Level hinzu (maximal 5 Sterne).
     * <p>
     * Beispiele:
     * - Diebstahl: +1 Stern
     * - Raub: +2 Sterne
     * - Körperverletzung: +2 Sterne
     * - Mord: +3 Sterne
     *
     * @param playerUUID Die UUID des Spielers
     * @param amount Anzahl hinzuzufügender Sterne (1-5)
     * @throws IllegalArgumentException wenn playerUUID null oder amount < 1
     */
    void addWantedLevel(UUID playerUUID, int amount);

    /**
     * Setzt Wanted-Level auf einen bestimmten Wert.
     *
     * @param playerUUID Die UUID des Spielers
     * @param level Das neue Wanted-Level (0-5)
     * @throws IllegalArgumentException wenn playerUUID null
     */
    void setWantedLevel(UUID playerUUID, int level);

    /**
     * Löscht Wanted-Level komplett (auf 0 zurücksetzen).
     * <p>
     * Wird typischerweise nach Festnahme oder Gefängnisstrafe aufgerufen.
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void clearWantedLevel(UUID playerUUID);

    /**
     * Reduziert Wanted-Level über Zeit (pro Minecraft-Tag).
     * <p>
     * Pro Tag ohne Verbrechen: -1 Stern
     * <p>
     * HINWEIS: Sollte automatisch vom Server pro Minecraft-Tag aufgerufen werden.
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void decayWantedLevel(UUID playerUUID);

    /**
     * Startet Escape-Timer für einen Spieler.
     * <p>
     * Wenn der Spieler 30 Sekunden lang mindestens 40 Blocks von der Polizei
     * entfernt bleibt, wird sein Wanted-Level um 1 Stern reduziert.
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void startEscape(UUID playerUUID);

    /**
     * Stoppt Escape-Timer (Polizei hat Spieler wieder entdeckt).
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void stopEscape(UUID playerUUID);

    /**
     * Prüft ob ein Spieler sich gerade vor der Polizei versteckt.
     *
     * @param playerUUID Die UUID des Spielers
     * @return true wenn Escape-Timer aktiv
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    boolean isHiding(UUID playerUUID);

    /**
     * Gibt verbleibende Escape-Zeit in Millisekunden zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @return Verbleibende Zeit in ms, 0 wenn kein Timer aktiv
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    long getEscapeTimeRemaining(UUID playerUUID);

    /**
     * Prüft ob Escape erfolgreich war und reduziert Wanted-Level.
     * <p>
     * Wird automatisch aufgerufen, kann aber auch manuell getriggert werden.
     *
     * @param playerUUID Die UUID des Spielers
     * @return true wenn Escape erfolgreich (Wanted-Level reduziert)
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    boolean checkEscapeSuccess(UUID playerUUID);
}
