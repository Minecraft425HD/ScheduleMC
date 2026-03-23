package de.rolandsw.schedulemc.messaging;

import java.util.UUID;

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
