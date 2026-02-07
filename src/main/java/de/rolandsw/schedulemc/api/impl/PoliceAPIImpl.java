package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.police.IPoliceAPI;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private static final Logger LOGGER = LogUtils.getLogger();

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

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Integer> getAllWantedPlayers() {
        Map<UUID, Integer> wantedPlayers = CrimeManager.getAllWantedPlayers();
        if (wantedPlayers != null) {
            return Collections.unmodifiableMap(wantedPlayers);
        }
        LOGGER.debug("Stub: getAllWantedPlayers - CrimeManager.getAllWantedPlayers() not available, returning empty map");
        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<UUID> getPlayersAtWantedLevel(int level) {
        if (level < 0 || level > 5) {
            throw new IllegalArgumentException("level must be between 0 and 5, got: " + level);
        }
        return getAllWantedPlayers().entrySet().stream()
            .filter(entry -> entry.getValue() == level)
            .map(Map.Entry::getKey)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWantedPlayerCount() {
        return getAllWantedPlayers().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isImprisoned(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        LOGGER.debug("Stub: isImprisoned not fully implemented - prison system not directly accessible");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRemainingJailTime(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        LOGGER.debug("Stub: getRemainingJailTime not fully implemented - prison system not directly accessible");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean releaseFromPrison(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        LOGGER.debug("Stub: releaseFromPrison not fully implemented - prison system not directly accessible");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getBailAmount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        int wantedLevel = getWantedLevel(playerUUID);
        if (wantedLevel <= 0) {
            return 0;
        }
        // Calculate bail based on wanted level: level * 500 Euro
        return wantedLevel * 500.0;
    }
}
