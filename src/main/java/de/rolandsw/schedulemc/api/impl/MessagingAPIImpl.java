package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.messaging.IMessagingAPI;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.messaging.Message;

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
        // Stub: Count unread messages from conversations
        List<de.rolandsw.schedulemc.messaging.Conversation> convs = MessageManager.getConversations(playerUUID);
        return (int) convs.stream().filter(c -> c.hasUnreadMessages()).count();
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
        // Stub: Mark all conversations as read
        List<de.rolandsw.schedulemc.messaging.Conversation> convs = MessageManager.getConversations(playerUUID);
        convs.forEach(c -> c.markAsRead());
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
}
