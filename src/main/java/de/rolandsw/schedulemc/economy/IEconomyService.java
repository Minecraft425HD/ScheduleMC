package de.rolandsw.schedulemc.economy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * Service Interface für Economy-Operationen
 *
 * Dieses Interface definiert den Contract für das Economy-System und ermöglicht:
 * - Bessere Testbarkeit durch Mock-Implementierungen
 * - Dependency Injection
 * - Multiple Implementierungen (z.B. Test-Economy, Production-Economy)
 * - Loose Coupling zwischen Komponenten
 *
 * @since 1.0
 */
public interface IEconomyService {

    // ========== Account Management ==========

    /**
     * Creates a new economy account with the configured starting balance
     *
     * @param uuid The player's unique identifier
     * @throws de.rolandsw.schedulemc.exceptions.EconomyException if account already exists
     */
    void createAccount(@Nonnull UUID uuid);

    /**
     * Checks if an account exists for the given player
     *
     * @param uuid The player's unique identifier
     * @return true if account exists, false otherwise
     */
    boolean hasAccount(@Nonnull UUID uuid);

    /**
     * Deletes an account from the system
     *
     * @param uuid The player's unique identifier
     */
    void deleteAccount(@Nonnull UUID uuid);

    // ========== Balance Operations ==========

    /**
     * Retrieves the current balance for a player
     *
     * @param uuid The player's unique identifier
     * @return The current balance, or 0.0 if no account exists
     */
    double getBalance(@Nonnull UUID uuid);

    /**
     * Sets a player's balance to a specific amount (administrative operation)
     *
     * @param uuid The player's unique identifier
     * @param amount The new balance
     */
    void setBalance(@Nonnull UUID uuid, double amount);

    /**
     * Sets a player's balance with transaction tracking
     *
     * @param uuid The player's unique identifier
     * @param amount The new balance
     * @param type The transaction type
     * @param description Optional description
     */
    void setBalance(@Nonnull UUID uuid, double amount, @Nonnull TransactionType type, @Nullable String description);

    // ========== Transaction Operations ==========

    /**
     * Deposits money into a player's account
     *
     * @param uuid The player's unique identifier
     * @param amount The amount to deposit (must be non-negative)
     * @throws de.rolandsw.schedulemc.exceptions.EconomyException if amount is invalid
     */
    void deposit(@Nonnull UUID uuid, double amount);

    /**
     * Deposits money with transaction tracking
     *
     * @param uuid The player's unique identifier
     * @param amount The amount to deposit
     * @param type The transaction type
     * @param description Optional description
     * @throws de.rolandsw.schedulemc.exceptions.EconomyException if amount is invalid or rate limit exceeded
     */
    void deposit(@Nonnull UUID uuid, double amount, @Nonnull TransactionType type, @Nullable String description);

    /**
     * Withdraws money from a player's account
     *
     * @param uuid The player's unique identifier
     * @param amount The amount to withdraw (must be non-negative)
     * @return true if successful, false if insufficient funds
     * @throws de.rolandsw.schedulemc.exceptions.EconomyException if amount is invalid
     */
    boolean withdraw(@Nonnull UUID uuid, double amount);

    /**
     * Withdraws money with transaction tracking
     *
     * @param uuid The player's unique identifier
     * @param amount The amount to withdraw
     * @param type The transaction type
     * @param description Optional description
     * @return true if successful, false if insufficient funds
     * @throws de.rolandsw.schedulemc.exceptions.EconomyException if amount is invalid or rate limit exceeded
     */
    boolean withdraw(@Nonnull UUID uuid, double amount, @Nonnull TransactionType type, @Nullable String description);

    /**
     * Transfers money from one player to another
     *
     * @param from The sender's UUID
     * @param to The recipient's UUID
     * @param amount The amount to transfer (must be positive)
     * @param description Optional description
     * @return true if successful, false if insufficient funds
     * @throws de.rolandsw.schedulemc.exceptions.EconomyException if players are same, amount invalid, or rate limit exceeded
     */
    boolean transfer(@Nonnull UUID from, @Nonnull UUID to, double amount, @Nullable String description);

    // ========== System Operations ==========

    /**
     * Retrieves all accounts in the system
     *
     * @return A defensive copy of all accounts
     */
    Map<UUID, Double> getAllAccounts();

    /**
     * Returns the configured starting balance for new accounts
     *
     * @return The starting balance
     */
    double getStartBalance();

    /**
     * Checks if the economy system is healthy
     *
     * @return true if healthy, false if there are issues
     */
    boolean isHealthy();

    /**
     * Returns the last error message if system is unhealthy
     *
     * @return The error message, or null if healthy
     */
    @Nullable
    String getLastError();

    /**
     * Returns formatted health information for monitoring
     *
     * @return Formatted health status string
     */
    String getHealthInfo();

    // ========== Persistence Operations ==========

    /**
     * Loads account data from persistent storage
     */
    void loadAccounts();

    /**
     * Saves account data to persistent storage
     */
    void saveAccounts();

    /**
     * Saves account data only if changes have been made
     */
    void saveIfNeeded();
}
