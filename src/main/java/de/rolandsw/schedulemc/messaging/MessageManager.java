package de.rolandsw.schedulemc.messaging;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.GsonHelper;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages all messaging conversations and persistence
 */
public class MessageManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Map<UUID, Conversation>> playerConversations = new ConcurrentHashMap<>();
    private static final File file = new File("config/plotmod_messages.json");
    private static final Gson gson = GsonHelper.get();
    private static boolean needsSave = false;

    /**
     * Loads all conversations from JSON file
     */
    public static void loadMessages() {
        if (!file.exists()) {
            LOGGER.info("No messages file found, starting with empty database");
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Map<String, Map<String, ConversationData>> loaded = gson.fromJson(reader,
                new TypeToken<Map<String, Map<String, ConversationData>>>(){}.getType());

            if (loaded != null) {
                loaded.forEach((playerKey, conversations) -> {
                    try {
                        UUID playerUUID = UUID.fromString(playerKey);
                        Map<UUID, Conversation> convMap = new ConcurrentHashMap<>();

                        conversations.forEach((participantKey, data) -> {
                            try {
                                UUID participantUUID = UUID.fromString(participantKey);
                                Conversation conv = new Conversation(
                                    participantUUID,
                                    data.participantName,
                                    data.isPlayerParticipant
                                );

                                data.messages.forEach(msgData -> {
                                    Message msg = new Message(
                                        UUID.fromString(msgData.senderUUID),
                                        msgData.senderName,
                                        msgData.content,
                                        msgData.timestamp,
                                        msgData.isPlayerSender
                                    );
                                    conv.addMessage(msg);
                                });

                                convMap.put(participantUUID, conv);
                            } catch (IllegalArgumentException e) {
                                LOGGER.error("Invalid participant UUID: {}", participantKey, e);
                            }
                        });

                        playerConversations.put(playerUUID, convMap);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid player UUID: {}", playerKey, e);
                    }
                });
                LOGGER.info("Messages loaded: {} players", playerConversations.size());
            }
        } catch (IOException e) {
            LOGGER.error("Error loading messages", e);
        }
    }

    /**
     * Saves all conversations to JSON file
     */
    public static void saveMessages() {
        try {
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                Map<String, Map<String, ConversationData>> saveMap = new HashMap<>();

                playerConversations.forEach((playerUUID, conversations) -> {
                    Map<String, ConversationData> convMap = new HashMap<>();

                    conversations.forEach((participantUUID, conv) -> {
                        ConversationData data = new ConversationData();
                        data.participantName = conv.getParticipantName();
                        data.isPlayerParticipant = conv.isPlayerParticipant();
                        data.messages = new ArrayList<>();

                        conv.getMessages().forEach(msg -> {
                            MessageData msgData = new MessageData();
                            msgData.senderUUID = msg.getSenderUUID().toString();
                            msgData.senderName = msg.getSenderName();
                            msgData.content = msg.getContent();
                            msgData.timestamp = msg.getTimestamp();
                            msgData.isPlayerSender = msg.isPlayerSender();
                            data.messages.add(msgData);
                        });

                        convMap.put(participantUUID.toString(), data);
                    });

                    saveMap.put(playerUUID.toString(), convMap);
                });

                gson.toJson(saveMap, writer);
                needsSave = false;
                LOGGER.debug("Messages saved: {} players", saveMap.size());
            }
        } catch (IOException e) {
            LOGGER.error("Error saving messages", e);
        }
    }

    public static void saveIfNeeded() {
        if (needsSave) {
            saveMessages();
        }
    }

    private static void markDirty() {
        needsSave = true;
    }

    /**
     * Sends a message from one entity to another
     */
    public static void sendMessage(UUID fromUUID, String fromName, boolean isFromPlayer,
                                   UUID toUUID, String toName, boolean isToPlayer, String content) {
        long timestamp = System.currentTimeMillis();

        // Add message to sender's conversation
        addMessageToConversation(fromUUID, toUUID, toName, isToPlayer,
            new Message(fromUUID, fromName, content, timestamp, isFromPlayer));

        // Add message to receiver's conversation
        addMessageToConversation(toUUID, fromUUID, fromName, isFromPlayer,
            new Message(fromUUID, fromName, content, timestamp, isFromPlayer));

        markDirty();
        LOGGER.debug("Message sent from {} to {}", fromName, toName);
    }

    private static void addMessageToConversation(UUID ownerUUID, UUID participantUUID,
                                                 String participantName, boolean isPlayerParticipant,
                                                 Message message) {
        playerConversations.computeIfAbsent(ownerUUID, k -> new ConcurrentHashMap<>());
        Map<UUID, Conversation> conversations = playerConversations.get(ownerUUID);

        Conversation conv = conversations.get(participantUUID);
        if (conv == null) {
            conv = new Conversation(participantUUID, participantName, isPlayerParticipant);
            conversations.put(participantUUID, conv);
        }

        conv.addMessage(message);
    }

    /**
     * Gets all conversations for a player, sorted by last message time
     */
    public static List<Conversation> getConversations(UUID playerUUID) {
        Map<UUID, Conversation> convMap = playerConversations.get(playerUUID);
        if (convMap == null) {
            return new ArrayList<>();
        }

        return convMap.values().stream()
            .sorted((a, b) -> Long.compare(b.getLastMessageTime(), a.getLastMessageTime()))
            .collect(Collectors.toList());
    }

    /**
     * Gets a specific conversation
     */
    public static Conversation getConversation(UUID playerUUID, UUID participantUUID) {
        Map<UUID, Conversation> convMap = playerConversations.get(playerUUID);
        return convMap != null ? convMap.get(participantUUID) : null;
    }

    /**
     * Helper classes for JSON serialization
     */
    private static class ConversationData {
        String participantName;
        boolean isPlayerParticipant;
        List<MessageData> messages;
    }

    private static class MessageData {
        String senderUUID;
        String senderName;
        String content;
        long timestamp;
        boolean isPlayerSender;
    }
}
