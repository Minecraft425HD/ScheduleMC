package de.rolandsw.schedulemc.messaging;

import java.util.UUID;

/**
 * Represents a single message in a conversation between players and/or NPCs.
 * <p>
 * Messages are immutable once created and contain all necessary information
 * about the sender, content, and timestamp. This class is used by both
 * player-to-player and player-to-NPC messaging systems.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> Immutable and thread-safe.
 * </p>
 *
 * @see Conversation
 * @see MessageManager
 */
public class Message {
    private final UUID senderUUID;
    private final String senderName;
    private final String content;
    private final long timestamp;
    private final boolean isPlayerSender;

    public Message(UUID senderUUID, String senderName, String content, long timestamp, boolean isPlayerSender) {
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
        this.isPlayerSender = isPlayerSender;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPlayerSender() {
        return isPlayerSender;
    }
}
