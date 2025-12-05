package de.rolandsw.schedulemc.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversation {
    private final UUID participantUUID;
    private final String participantName;
    private final boolean isPlayerParticipant;
    private final List<Message> messages;
    private long lastMessageTime;

    public Conversation(UUID participantUUID, String participantName, boolean isPlayerParticipant) {
        this.participantUUID = participantUUID;
        this.participantName = participantName;
        this.isPlayerParticipant = isPlayerParticipant;
        this.messages = new ArrayList<>();
        this.lastMessageTime = System.currentTimeMillis();
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
}
