package de.rolandsw.schedulemc.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a conversation thread between a player and another entity (player or NPC).
 * <p>
 * Each conversation maintains a chronological list of messages, tracks the last
 * message timestamp, and stores reputation data for NPC conversations. Conversations
 * are stored per-player and per-participant, allowing each entity to have their own
 * view of the conversation history.
 * </p>
 * <p>
 * <strong>Reputation System:</strong> The reputation field (0-100) is only relevant
 * for player-to-NPC conversations. Higher reputation unlocks better dialogue options
 * and NPC interactions.
 * </p>
 * <p>
 * <strong>Thread Safety:</strong> NOT thread-safe. Access should be synchronized
 * by the calling code (typically MessageManager).
 * </p>
 *
 * @see Message
 * @see MessageManager
 */
public class Conversation {
    private final UUID participantUUID;
    private final String participantName;
    private final boolean isPlayerParticipant;
    private final List<Message> messages;
    private long lastMessageTime;
    private int reputation; // 0-100, nur für NPC-Konversationen relevant

    public Conversation(UUID participantUUID, String participantName, boolean isPlayerParticipant) {
        this.participantUUID = participantUUID;
        this.participantName = participantName;
        this.isPlayerParticipant = isPlayerParticipant;
        this.messages = new ArrayList<>();
        this.lastMessageTime = System.currentTimeMillis();
        this.reputation = 0; // Anfänger-Reputation
    }

    public void addMessage(Message message) {
        messages.add(message);
        lastMessageTime = message.getTimestamp();
    }

    public UUID getParticipantUUID() {
        return participantUUID;
    }

    public String getParticipantName() {
        return participantName;
    }

    public boolean isPlayerParticipant() {
        return isPlayerParticipant;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public String getPreviewText() {
        Message lastMsg = getLastMessage();
        if (lastMsg == null) {
            return "Keine Nachrichten";
        }
        String content = lastMsg.getContent();
        return content.length() > 30 ? content.substring(0, 27) + "..." : content;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = Math.max(0, Math.min(100, reputation));
    }
}
