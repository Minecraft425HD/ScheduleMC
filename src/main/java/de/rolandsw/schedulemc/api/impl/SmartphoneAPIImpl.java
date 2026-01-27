package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.smartphone.ISmartphoneAPI;

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
}
