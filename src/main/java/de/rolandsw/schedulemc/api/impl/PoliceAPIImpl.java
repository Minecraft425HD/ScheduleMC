package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.police.IPoliceAPI;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;

import java.util.UUID;

/**
 * Implementation of IPoliceAPI
 *
 * Wrapper für CrimeManager mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class PoliceAPIImpl implements IPoliceAPI {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWantedLevel(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return CrimeManager.getWantedLevel(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWantedLevel(UUID playerUUID, int amount) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1, got: " + amount);
        }
        // Use current game day (0 as default if not available)
        CrimeManager.addWantedLevel(playerUUID, amount, 0L);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWantedLevel(UUID playerUUID, int level) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        CrimeManager.setWantedLevel(playerUUID, Math.max(0, Math.min(5, level)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearWantedLevel(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        CrimeManager.clearWantedLevel(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decayWantedLevel(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        int currentLevel = getWantedLevel(playerUUID);
        if (currentLevel > 0) {
            setWantedLevel(playerUUID, currentLevel - 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startEscape(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        // Use current tick (0 as default if not available)
        CrimeManager.startEscapeTimer(playerUUID, 0L);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopEscape(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        CrimeManager.stopEscapeTimer(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHiding(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return CrimeManager.isHiding(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getEscapeTimeRemaining(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        // Use current tick (0 as default if not available)
        return CrimeManager.getEscapeTimeRemaining(playerUUID, 0L);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkEscapeSuccess(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }

        if (isHiding(playerUUID) && CrimeManager.getEscapeTimeRemaining(playerUUID, 0L) <= 0) {
            // Escape successful - reduce wanted level by 1
            int currentLevel = getWantedLevel(playerUUID);
            if (currentLevel > 0) {
                setWantedLevel(playerUUID, currentLevel - 1);
                stopEscape(playerUUID);
                return true;
            }
        }
        return false;
    }
}
