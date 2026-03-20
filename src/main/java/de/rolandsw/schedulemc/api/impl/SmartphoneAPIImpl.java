package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.smartphone.ISmartphoneAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;
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

    // In-memory app registry: appId -> appName
    private static final Map<String, String> registeredApps = new ConcurrentHashMap<>();

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
        registeredApps.put(appId, appName);
        LOGGER.debug("Registered smartphone app: {} ({})", appId, appName);
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
        return registeredApps.remove(appId) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getRegisteredApps() {
        return Collections.unmodifiableSet(registeredApps.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNotification(UUID playerUUID, String appId, String message) {
        if (playerUUID == null || appId == null || message == null) {
            throw new IllegalArgumentException("playerUUID, appId and message cannot be null");
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            String appName = registeredApps.getOrDefault(appId, appId);
            player.sendSystemMessage(Component.literal("[" + appName + "] " + message));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSmartphone(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        // In ScheduleMC all players have a smartphone (it's a roleplay feature, not an inventory item)
        return true;
    }
}
