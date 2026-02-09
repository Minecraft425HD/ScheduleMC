package de.rolandsw.schedulemc.messaging;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Conversation {
    private static final int MAX_MESSAGES = 200;

    private final UUID participantUUID;
    private final String participantName;
    private final boolean isPlayerParticipant;
    private final List<Message> messages;
    private volatile long lastMessageTime;
    private int reputation; // 0-100, nur für NPC-Konversationen relevant

    public Conversation(UUID participantUUID, String participantName, boolean isPlayerParticipant) {
        this.participantUUID = participantUUID;
        this.participantName = participantName;
        this.isPlayerParticipant = isPlayerParticipant;
        this.messages = new CopyOnWriteArrayList<>();
        this.lastMessageTime = System.currentTimeMillis();
        this.reputation = 0; // Anfänger-Reputation
    }

    public void addMessage(Message message) {
        messages.add(message);
        lastMessageTime = message.getTimestamp();
        // Cap bei MAX_MESSAGES um unbegrenztes Wachstum zu verhindern
        while (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
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
        return Collections.unmodifiableList(messages);
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
