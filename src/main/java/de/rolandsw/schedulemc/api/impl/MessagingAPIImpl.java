package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.messaging.IMessagingAPI;
import de.rolandsw.schedulemc.messaging.Conversation;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.messaging.Message;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of IMessagingAPI
 *
 * Wrapper für MessageManager mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class MessagingAPIImpl implements IMessagingAPI {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final UUID SYSTEM_UUID = new UUID(0L, 0L);

    // In-memory block list: player -> set of blocked UUIDs
    private static final Map<UUID, Set<UUID>> blockedPlayers = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendMessage(UUID fromUUID, UUID toUUID, String message) {
        if (fromUUID == null || toUUID == null) {
            throw new IllegalArgumentException("fromUUID and toUUID cannot be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message cannot be null or empty");
        }
        // Call with default names (API doesn't expose player names)
        MessageManager.sendMessage(fromUUID, fromUUID.toString(), false, toUUID, toUUID.toString(), false, message);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnreadMessageCount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return MessageManager.getUnreadCount(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getMessages(UUID playerUUID, int limit) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1, got: " + limit);
        }

        List<de.rolandsw.schedulemc.messaging.Conversation> convs = MessageManager.getConversations(playerUUID);
        return convs.stream()
            .flatMap(c -> c.getMessages().stream())
            .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
            .limit(limit)
            .map(msg -> msg.getContent())
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAllAsRead(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        MessageManager.markAllAsRead(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMessage(UUID playerUUID, String messageId) {
        if (playerUUID == null || messageId == null) {
            throw new IllegalArgumentException("playerUUID and messageId cannot be null");
        }
        // messageId format: "participantUUID:timestamp" or just a timestamp string
        try {
            String[] parts = messageId.split(":", 2);
            if (parts.length == 2) {
                UUID participantUUID = UUID.fromString(parts[0]);
                long timestamp = Long.parseLong(parts[1]);
                return MessageManager.deleteMessage(playerUUID, participantUUID, timestamp);
            } else {
                long timestamp = Long.parseLong(messageId);
                // Search all conversations for this timestamp
                for (de.rolandsw.schedulemc.messaging.Conversation conv : MessageManager.getConversations(playerUUID)) {
                    if (MessageManager.deleteMessage(playerUUID, conv.getParticipantUUID(), timestamp)) return true;
                }
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllMessages(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        for (de.rolandsw.schedulemc.messaging.Conversation conv : MessageManager.getConversations(playerUUID)) {
            MessageManager.deleteAllMessages(playerUUID, conv.getParticipantUUID());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMessageCount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return MessageManager.getTotalMessageCount(playerUUID);
    }

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public int broadcastMessage(UUID fromUUID, String message) {
        if (fromUUID == null) {
            throw new IllegalArgumentException("fromUUID cannot be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message cannot be null or empty");
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 0;
        int count = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.getUUID().equals(fromUUID)) {
                sendMessage(fromUUID, player.getUUID(), message);
                count++;
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendSystemMessage(UUID toUUID, String message) {
        if (toUUID == null) {
            throw new IllegalArgumentException("toUUID cannot be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message cannot be null or empty");
        }
        return sendMessage(SYSTEM_UUID, toUUID, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getConversation(UUID playerA, UUID playerB, int limit) {
        if (playerA == null || playerB == null) {
            throw new IllegalArgumentException("playerA and playerB cannot be null");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1, got: " + limit);
        }
        Conversation conv = MessageManager.getConversation(playerA, playerB);
        if (conv == null) return Collections.emptyList();
        return conv.getMessages().stream()
            .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
            .limit(limit)
            .map(Message::getContent)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlocked(UUID playerUUID, UUID blockedUUID) {
        if (playerUUID == null || blockedUUID == null) {
            throw new IllegalArgumentException("playerUUID and blockedUUID cannot be null");
        }
        Set<UUID> blocked = blockedPlayers.get(playerUUID);
        return blocked != null && blocked.contains(blockedUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void blockPlayer(UUID playerUUID, UUID blockedUUID) {
        if (playerUUID == null || blockedUUID == null) {
            throw new IllegalArgumentException("playerUUID and blockedUUID cannot be null");
        }
        blockedPlayers.computeIfAbsent(playerUUID, k -> ConcurrentHashMap.newKeySet()).add(blockedUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unblockPlayer(UUID playerUUID, UUID blockedUUID) {
        if (playerUUID == null || blockedUUID == null) {
            throw new IllegalArgumentException("playerUUID and blockedUUID cannot be null");
        }
        Set<UUID> blocked = blockedPlayers.get(playerUUID);
        if (blocked != null) {
            blocked.remove(blockedUUID);
        }
    }
}
