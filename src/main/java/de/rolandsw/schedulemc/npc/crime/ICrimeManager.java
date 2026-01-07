package de.rolandsw.schedulemc.npc.crime;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Service Interface für Crime-Management
 *
 * Dieses Interface definiert den Contract für das Verbrechen- und Wanted-Level-System:
 * - Wanted-Level-Verwaltung
 * - Escape-Timer-Mechanik
 * - Client-Server-Synchronisation
 * - Persistence-Operations
 *
 * @author ScheduleMC Team
 * @since 3.2.0
 */
public interface ICrimeManager {

    // ========== Wanted Level Management ==========

    /**
     * Retrieves the current wanted level for a player
     *
     * @param playerUUID The player's unique identifier
     * @return Wanted level (0-5), 0 if no criminal record
     */
    int getWantedLevel(@Nonnull UUID playerUUID);

    /**
     * Adds wanted level to a player for committing a crime
     *
     * @param playerUUID The player's UUID
     * @param amount Amount to add to wanted level
     * @param currentDay Current game day for persistence
     */
    void addWantedLevel(@Nonnull UUID playerUUID, int amount, long currentDay);

    /**
     * Clears all wanted level for a player (arrest, payment, etc.)
     *
     * @param playerUUID The player's UUID
     */
    void clearWantedLevel(@Nonnull UUID playerUUID);

    /**
     * Sets wanted level to a specific value (admin command)
     *
     * @param playerUUID The player's UUID
     * @param level New wanted level
     */
    void setWantedLevel(@Nonnull UUID playerUUID, int level);

    /**
     * Decays wanted level over time (called periodically)
     *
     * @param playerUUID The player's UUID
     * @param currentDay Current game day
     */
    void decayWantedLevel(UUID playerUUID, long currentDay);

    // ========== Escape Timer Mechanics ==========

    /**
     * Starts the escape timer for a player trying to evade police
     *
     * @param playerUUID The player's UUID
     * @param currentTick Current game tick
     */
    void startEscapeTimer(@Nonnull UUID playerUUID, long currentTick);

    /**
     * Stops the escape timer (player caught or gave up)
     *
     * @param playerUUID The player's UUID
     */
    void stopEscapeTimer(UUID playerUUID);

    /**
     * Checks if player is currently hiding from police
     *
     * @param playerUUID The player's UUID
     * @return true if player has active escape timer
     */
    boolean isHiding(UUID playerUUID);

    /**
     * Gets remaining time on escape timer
     *
     * @param playerUUID The player's UUID
     * @param currentTick Current game tick
     * @return Remaining ticks, or 0 if not hiding
     */
    long getEscapeTimeRemaining(UUID playerUUID, long currentTick);

    /**
     * Checks if player successfully escaped (timer expired while hidden)
     *
     * @param playerUUID The player's UUID
     * @param currentTick Current game tick
     * @return true if escape successful
     */
    boolean checkEscapeSuccess(UUID playerUUID, long currentTick);

    // ========== Client Synchronization ==========

    /**
     * Sets wanted level on client side (packet sync)
     *
     * @param level Wanted level from server
     */
    void setClientWantedLevel(int level);

    /**
     * Sets escape time remaining on client (packet sync)
     *
     * @param timeRemaining Time remaining in ticks
     */
    void setClientEscapeTime(long timeRemaining);

    /**
     * Gets client-side wanted level (for HUD rendering)
     *
     * @return Client wanted level
     */
    int getClientWantedLevel();

    /**
     * Gets client-side escape time (for HUD rendering)
     *
     * @return Client escape time
     */
    long getClientEscapeTime();

    // ========== Persistence ==========

    /**
     * Loads crime data from persistent storage
     */
    void load();

    /**
     * Saves crime data to persistent storage immediately
     */
    void save();

    /**
     * Saves crime data only if there are unsaved changes
     */
    void saveIfNeeded();

    // ========== System Health ==========

    /**
     * Checks if the crime management system is healthy
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
