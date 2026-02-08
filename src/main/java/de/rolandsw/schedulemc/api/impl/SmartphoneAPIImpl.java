package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.smartphone.ISmartphoneAPI;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of ISmartphoneAPI
 *
 * Thread-safe tracking von Smartphone-GUI-Zuständen.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class SmartphoneAPIImpl implements ISmartphoneAPI {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Set<UUID> playersWithSmartphoneOpen = ConcurrentHashMap.newKeySet();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSmartphoneOpen(UUID playerUUID, boolean open) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }

        if (open) {
            playersWithSmartphoneOpen.add(playerUUID);
        } else {
            playersWithSmartphoneOpen.remove(playerUUID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSmartphoneOpen(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return playersWithSmartphoneOpen.contains(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePlayer(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        playersWithSmartphoneOpen.remove(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<UUID> getPlayersWithSmartphoneOpen() {
        return Collections.unmodifiableSet(playersWithSmartphoneOpen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAllTracking() {
        playersWithSmartphoneOpen.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOpenSmartphoneCount() {
        return playersWithSmartphoneOpen.size();
    }

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerApp(String appId, String appName, String iconColor) {
        if (appId == null || appName == null || iconColor == null) {
            throw new IllegalArgumentException("appId, appName and iconColor cannot be null");
        }
        LOGGER.debug("Stub: registerApp not fully implemented - app registration system not directly accessible");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterApp(String appId) {
        if (appId == null) {
            throw new IllegalArgumentException("appId cannot be null");
        }
        LOGGER.debug("Stub: unregisterApp not fully implemented - app registration system not directly accessible");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getRegisteredApps() {
        LOGGER.debug("Stub: getRegisteredApps not fully implemented - app registration system not directly accessible");
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNotification(UUID playerUUID, String appId, String message) {
        if (playerUUID == null || appId == null || message == null) {
            throw new IllegalArgumentException("playerUUID, appId and message cannot be null");
        }
        LOGGER.debug("Stub: sendNotification not fully implemented - notification system not directly accessible");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSmartphone(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        LOGGER.debug("Stub: hasSmartphone not fully implemented - inventory check not directly accessible");
        return true;
    }
}
