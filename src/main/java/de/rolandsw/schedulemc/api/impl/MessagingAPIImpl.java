package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.messaging.IMessagingAPI;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.messaging.Message;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendMessage(UUID fromUUID, UUID toUUID, String message) {
        if (fromUUID == null || toUUID == null) {
            throw new IllegalArgumentException("fromUUID and toUUID cannot be null");
        }
        if (message == null || message.trim().isEmpty()) {
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
        // Note: Conversation class doesn't track read/unread status
        // This would need to be implemented in the messaging system
        return 0;
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

        // Stub: Get messages from all conversations
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
        // Note: Conversation class doesn't support marking as read
        // This would need to be implemented in the messaging system
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMessage(UUID playerUUID, String messageId) {
        if (playerUUID == null || messageId == null) {
            throw new IllegalArgumentException("playerUUID and messageId cannot be null");
        }
        // Stub: Message deletion not supported in current MessageManager implementation
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllMessages(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        // Stub: Bulk message deletion not supported in current MessageManager implementation
        // Would need to be implemented in MessageManager if required
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMessageCount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        // Stub: Count total messages from all conversations
        List<de.rolandsw.schedulemc.messaging.Conversation> convs = MessageManager.getConversations(playerUUID);
        return convs.stream()
            .mapToInt(c -> c.getMessages().size())
            .sum();
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
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("message cannot be null or empty");
        }
        LOGGER.debug("Stub: broadcastMessage not fully implemented - broadcast system not directly accessible");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendSystemMessage(UUID toUUID, String message) {
        if (toUUID == null) {
            throw new IllegalArgumentException("toUUID cannot be null");
        }
        if (message == null || message.trim().isEmpty()) {
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
        LOGGER.debug("Stub: getConversation not fully implemented - direct conversation retrieval not accessible");
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlocked(UUID playerUUID, UUID blockedUUID) {
        if (playerUUID == null || blockedUUID == null) {
            throw new IllegalArgumentException("playerUUID and blockedUUID cannot be null");
        }
        LOGGER.debug("Stub: isBlocked not fully implemented - block system not directly accessible");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void blockPlayer(UUID playerUUID, UUID blockedUUID) {
        if (playerUUID == null || blockedUUID == null) {
            throw new IllegalArgumentException("playerUUID and blockedUUID cannot be null");
        }
        LOGGER.debug("Stub: blockPlayer not fully implemented - block system not directly accessible");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unblockPlayer(UUID playerUUID, UUID blockedUUID) {
        if (playerUUID == null || blockedUUID == null) {
            throw new IllegalArgumentException("playerUUID and blockedUUID cannot be null");
        }
        LOGGER.debug("Stub: unblockPlayer not fully implemented - block system not directly accessible");
    }
}
