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
        return MessageManager.sendMessage(fromUUID, toUUID, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnreadMessageCount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return MessageManager.getUnreadMessageCount(playerUUID);
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

        List<Message> messages = MessageManager.getMessages(playerUUID, limit);
        if (messages == null) {
            return Collections.emptyList();
        }

        return messages.stream()
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
        return MessageManager.deleteMessage(playerUUID, messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllMessages(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        MessageManager.deleteAllMessages(playerUUID);
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
}
