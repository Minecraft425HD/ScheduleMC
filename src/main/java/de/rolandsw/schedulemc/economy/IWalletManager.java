package de.rolandsw.schedulemc.economy;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Service Interface für Wallet-Management
 *
 * Manages physical cash that players carry in their inventory (Wallet System).
 * This is separate from bank account balances managed by EconomyManager.
 *
 * Features:
 * - Physical cash tracking (wallet balance)
 * - Cash operations (add/remove)
 * - Persistence
 * - Health monitoring
 *
 * @author ScheduleMC Team
 * @since 3.2.0
 */
public interface IWalletManager {

    // ========== Balance Operations ==========

    /**
     * Retrieves the wallet balance for a player
     *
     * @param playerUUID The player's unique identifier
     * @return Wallet balance (physical cash), 0.0 if no wallet exists
     */
    double getBalance(@Nonnull UUID playerUUID);

    /**
     * Sets the wallet balance to a specific amount
     *
     * @param playerUUID The player's UUID
     * @param amount New wallet balance (must be non-negative)
     */
    void setBalance(@Nonnull UUID playerUUID, double amount);

    /**
     * Adds money to a player's wallet
     *
     * @param playerUUID The player's UUID
     * @param amount Amount to add (must be positive)
     */
    void addMoney(@Nonnull UUID playerUUID, double amount);

    /**
     * Removes money from a player's wallet
     *
     * @param playerUUID The player's UUID
     * @param amount Amount to remove (must be positive)
     * @return true if successful, false if insufficient wallet balance
     */
    boolean removeMoney(@Nonnull UUID playerUUID, double amount);

    // ========== Persistence ==========

    /**
     * Loads wallet data from persistent storage
     */
    void load();

    /**
     * Saves wallet data to persistent storage immediately
     */
    void save();

    /**
     * Saves wallet data only if there are unsaved changes
     */
    void saveIfNeeded();

    // ========== System Health ==========

    /**
     * Checks if the wallet management system is healthy
     *
     * @return true if healthy, false if there are issues
     */
    boolean isHealthy();

    /**
     * Returns the last error message if system is unhealthy
     *
     * @return Error message, or null if healthy
     */
    String getLastError();

    /**
     * Returns formatted health information for monitoring
     *
     * @return Formatted health status string
     */
    String getHealthInfo();
}
