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

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns a map of all player UUIDs to their balances.
     * Useful for leaderboards and statistics.
     *
     * @return Unmodifiable map of UUID to balance
     * @since 3.2.0
     */
    java.util.Map<UUID, Double> getAllBalances();

    /**
     * Returns the total money in circulation across all accounts.
     *
     * @return Total money in Euro
     * @since 3.2.0
     */
    double getTotalMoneyInCirculation();

    /**
     * Returns the number of registered accounts.
     *
     * @return Account count
     * @since 3.2.0
     */
    int getAccountCount();

    /**
     * Returns the top N richest players.
     *
     * @param limit Maximum number of entries
     * @return Sorted list of UUID-balance pairs (richest first)
     * @throws IllegalArgumentException if limit < 1
     * @since 3.2.0
     */
    java.util.List<java.util.Map.Entry<UUID, Double>> getTopBalances(int limit);

    /**
     * Checks if a player can afford a specific amount.
     *
     * @param playerUUID The player's UUID
     * @param amount The amount to check
     * @return true if balance >= amount
     * @throws IllegalArgumentException if playerUUID is null or amount negative
     * @since 3.2.0
     */
    boolean canAfford(UUID playerUUID, double amount);

    /**
     * Performs a batch transfer to multiple recipients.
     *
     * @param fromUUID Sender UUID
     * @param recipients Map of recipient UUID to amount
     * @param description Optional transaction description
     * @return true if all transfers succeeded, false if insufficient balance
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    boolean batchTransfer(UUID fromUUID, java.util.Map<UUID, Double> recipients, @Nullable String description);

    /**
     * Returns the transaction history for a player.
     *
     * @param playerUUID The player's UUID
     * @param limit Maximum number of entries
     * @return List of transaction descriptions (newest first)
     * @throws IllegalArgumentException if playerUUID is null or limit < 1
     * @since 3.2.0
     */
    java.util.List<String> getTransactionHistory(UUID playerUUID, int limit);
}
