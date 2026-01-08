package de.rolandsw.schedulemc.api.economy;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Public Economy API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Economy-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Spieler-Guthaben abfragen und ändern</li>
 *   <li>Transfers zwischen Spielern</li>
 *   <li>Konto-Verwaltung</li>
 *   <li>Transaktions-Tracking</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe und können von beliebigen Threads aufgerufen werden.
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IEconomyAPI economy = ScheduleMCAPI.getEconomyAPI();
 *
 * // Guthaben abfragen
 * double balance = economy.getBalance(playerUUID);
 *
 * // Geld einzahlen
 * economy.deposit(playerUUID, 100.0);
 *
 * // Transfer zwischen Spielern
 * boolean success = economy.transfer(fromUUID, toUUID, 50.0);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IEconomyAPI {

    /**
     * Gibt das aktuelle Guthaben eines Spielers zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @return Das Guthaben in Euro (0.0 wenn kein Konto existiert)
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    double getBalance(UUID playerUUID);

    /**
     * Prüft ob ein Spieler ein Konto besitzt.
     *
     * @param playerUUID Die UUID des Spielers
     * @return true wenn Konto existiert, false sonst
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    boolean hasAccount(UUID playerUUID);

    /**
     * Erstellt ein neues Konto mit Startguthaben.
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     * @throws IllegalStateException wenn bereits ein Konto existiert
     */
    void createAccount(UUID playerUUID);

    /**
     * Zahlt Geld auf ein Konto ein.
     *
     * @param playerUUID Die UUID des Spielers
     * @param amount Der Betrag (muss positiv sein)
     * @throws IllegalArgumentException wenn playerUUID null oder amount negativ
     */
    void deposit(UUID playerUUID, double amount);

    /**
     * Zahlt Geld auf ein Konto ein mit Beschreibung.
     *
     * @param playerUUID Die UUID des Spielers
     * @param amount Der Betrag (muss positiv sein)
     * @param description Optionale Beschreibung für Transaktions-Log
     * @throws IllegalArgumentException wenn playerUUID null oder amount negativ
     */
    void deposit(UUID playerUUID, double amount, @Nullable String description);

    /**
     * Hebt Geld von einem Konto ab.
     *
     * @param playerUUID Die UUID des Spielers
     * @param amount Der Betrag (muss positiv sein)
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     * @throws IllegalArgumentException wenn playerUUID null oder amount negativ
     */
    boolean withdraw(UUID playerUUID, double amount);

    /**
     * Hebt Geld von einem Konto ab mit Beschreibung.
     *
     * @param playerUUID Die UUID des Spielers
     * @param amount Der Betrag (muss positiv sein)
     * @param description Optionale Beschreibung für Transaktions-Log
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     * @throws IllegalArgumentException wenn playerUUID null oder amount negativ
     */
    boolean withdraw(UUID playerUUID, double amount, @Nullable String description);

    /**
     * Transferiert Geld zwischen zwei Spielern.
     *
     * @param fromUUID UUID des Absenders
     * @param toUUID UUID des Empfängers
     * @param amount Der Betrag (muss positiv sein)
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     * @throws IllegalArgumentException wenn UUIDs null oder amount negativ
     */
    boolean transfer(UUID fromUUID, UUID toUUID, double amount);

    /**
     * Transferiert Geld zwischen zwei Spielern mit Beschreibung.
     *
     * @param fromUUID UUID des Absenders
     * @param toUUID UUID des Empfängers
     * @param amount Der Betrag (muss positiv sein)
     * @param description Optionale Beschreibung für Transaktions-Log
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     * @throws IllegalArgumentException wenn UUIDs null oder amount negativ
     */
    boolean transfer(UUID fromUUID, UUID toUUID, double amount, @Nullable String description);

    /**
     * Setzt das Guthaben eines Spielers direkt (Admin-Funktion).
     * <p>
     * <b>WARNUNG:</b> Diese Methode sollte nur für Admin-Commands verwendet werden!
     *
     * @param playerUUID Die UUID des Spielers
     * @param amount Der neue Betrag (wird auf 0 gesetzt wenn negativ)
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void setBalance(UUID playerUUID, double amount);

    /**
     * Löscht ein Konto (Admin-Funktion).
     * <p>
     * <b>WARNUNG:</b> Alle Transaktionen und das Guthaben gehen verloren!
     *
     * @param playerUUID Die UUID des Spielers
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    void deleteAccount(UUID playerUUID);

    /**
     * Gibt das konfigurierte Startguthaben zurück.
     *
     * @return Das Startguthaben für neue Konten in Euro
     */
    double getStartBalance();
}
