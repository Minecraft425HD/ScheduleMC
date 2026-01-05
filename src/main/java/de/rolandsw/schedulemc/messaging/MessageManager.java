package de.rolandsw.schedulemc.messaging;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.HashMap;

/**
 * Manages all messaging conversations and persistence
 *
 * Nutzt AbstractPersistenceManager für robuste Datenpersistenz
 */
public class MessageManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Map<UUID, Conversation>> playerConversations = new ConcurrentHashMap<>();
    private static final File file = new File("config/plotmod_messages.json");
    private static final Gson gson = GsonHelper.get();

    // Persistence-Manager (eliminiert ~180 Zeilen Duplikation)
    private static final MessagePersistenceManager persistence =
        new MessagePersistenceManager(file, gson);

    /**
     * Loads all conversations from JSON file
     */
    public static void loadMessages() {
        persistence.load();
    }

    /**
     * Saves all conversations to JSON file
     */
    public static void saveMessages() {
        persistence.save();
    }

    /**
     * Saves only if changes are present
     */
    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    private static void markDirty() {
        persistence.markDirty();
    }

    /**
     * Sends a message from one entity to another
     * OPTIMIERT: Wiederverwendung des Message-Objekts statt Duplikation
     */
    public static void sendMessage(UUID fromUUID, String fromName, boolean isFromPlayer,
                                   UUID toUUID, String toName, boolean isToPlayer, String content) {
        long timestamp = System.currentTimeMillis();

        // OPTIMIERT: Ein Message-Objekt für beide Konversationen (Message ist immutable)
        Message message = new Message(fromUUID, fromName, content, timestamp, isFromPlayer);

        // Add message to sender's conversation
        addMessageToConversation(fromUUID, toUUID, toName, isToPlayer, message);

        // Add message to receiver's conversation
        addMessageToConversation(toUUID, fromUUID, fromName, isFromPlayer, message);

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
     * Gets or creates a conversation (used for opening chats with NPCs/players)
     */
    public static Conversation getOrCreateConversation(UUID playerUUID, UUID participantUUID,
                                                      String participantName, boolean isPlayerParticipant) {
        playerConversations.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        Map<UUID, Conversation> conversations = playerConversations.get(playerUUID);

        Conversation conv = conversations.get(participantUUID);
        if (conv == null) {
            conv = new Conversation(participantUUID, participantName, isPlayerParticipant);
            conversations.put(participantUUID, conv);
            markDirty();
            LOGGER.debug("Created new conversation: {} <-> {}", playerUUID, participantName);
        }

        return conv;
    }

    /**
     * Returns health status
     */
    public static boolean isHealthy() {
        return persistence.isHealthy();
    }

    /**
     * Returns last error message
     */
    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    /**
     * Returns health info for monitoring
     */
    public static String getHealthInfo() {
        return persistence.getHealthInfo();
    }

    /**
     * Helper classes for JSON serialization
     */
    private static class ConversationData {
        String participantName;
        boolean isPlayerParticipant;
        int reputation;
        List<MessageData> messages;
    }

    private static class MessageData {
        String senderUUID;
        String senderName;
        String content;
        long timestamp;
        boolean isPlayerSender;
    }

    /**
     * Innere Persistence-Manager-Klasse
     */
    private static class MessagePersistenceManager extends AbstractPersistenceManager<Map<String, Map<String, ConversationData>>> {

        public MessagePersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return new TypeToken<Map<String, Map<String, ConversationData>>>(){}.getType();
        }

        @Override
        protected void onDataLoaded(Map<String, Map<String, ConversationData>> data) {
            playerConversations.clear();

            data.forEach((playerKey, conversations) -> {
                try {
                    UUID playerUUID = UUID.fromString(playerKey);
                    Map<UUID, Conversation> convMap = new ConcurrentHashMap<>();

                    conversations.forEach((participantKey, convData) -> {
                        try {
                            UUID participantUUID = UUID.fromString(participantKey);
                            Conversation conv = new Conversation(
                                participantUUID,
                                convData.participantName,
                                convData.isPlayerParticipant
                            );

                            conv.setReputation(convData.reputation);

                            convData.messages.forEach(msgData -> {
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
        }

        @Override
        protected Map<String, Map<String, ConversationData>> getCurrentData() {
            Map<String, Map<String, ConversationData>> saveMap = new HashMap<>();

            playerConversations.forEach((playerUUID, conversations) -> {
                Map<String, ConversationData> convMap = new HashMap<>();

                conversations.forEach((participantUUID, conv) -> {
                    ConversationData data = new ConversationData();
                    data.participantName = conv.getParticipantName();
                    data.isPlayerParticipant = conv.isPlayerParticipant();
                    data.reputation = conv.getReputation();
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

            return saveMap;
        }

        @Override
        protected String getComponentName() {
            return "Message System";
        }

        @Override
        protected String getHealthDetails() {
            int totalConversations = playerConversations.values().stream()
                .mapToInt(Map::size)
                .sum();
            return String.format("%d Spieler, %d Conversations", playerConversations.size(), totalConversations);
        }

        @Override
        protected void onCriticalLoadFailure() {
            playerConversations.clear();
        }
    }
}
