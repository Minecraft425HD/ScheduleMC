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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

            int invalidCount = 0;
            int correctedCount = 0;

            // NULL CHECK
            if (data == null) {
                LOGGER.warn("Null data loaded for messages");
                invalidCount++;
                return;
            }

            // Check collection size
            if (data.size() > 10000) {
                LOGGER.warn("Message data map size ({}) exceeds limit, potential corruption",
                    data.size());
                correctedCount++;
            }

            data.forEach((playerKey, conversations) -> {
                try {
                    // VALIDATE UUID STRING
                    if (playerKey == null || playerKey.isEmpty()) {
                        LOGGER.warn("Null/empty player UUID string in messages, skipping");
                        return;
                    }

                    // NULL CHECK
                    if (conversations == null) {
                        LOGGER.warn("Null conversations for player {}, skipping", playerKey);
                        return;
                    }

                    UUID playerUUID;
                    try {
                        playerUUID = UUID.fromString(playerKey);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid player UUID: {}", playerKey, e);
                        return;
                    }

                    Map<UUID, Conversation> convMap = new ConcurrentHashMap<>();

                    conversations.forEach((participantKey, convData) -> {
                        try {
                            // VALIDATE UUID STRING
                            if (participantKey == null || participantKey.isEmpty()) {
                                LOGGER.warn("Null/empty participant UUID string for player {}, skipping",
                                    playerUUID);
                                return;
                            }

                            // NULL CHECK
                            if (convData == null) {
                                LOGGER.warn("Null conversation data for player {} participant {}, skipping",
                                    playerUUID, participantKey);
                                return;
                            }

                            UUID participantUUID;
                            try {
                                participantUUID = UUID.fromString(participantKey);
                            } catch (IllegalArgumentException e) {
                                LOGGER.error("Invalid participant UUID: {}", participantKey, e);
                                return;
                            }

                            // VALIDATE PARTICIPANT NAME
                            String participantName = convData.participantName;
                            if (participantName == null || participantName.isEmpty()) {
                                participantName = "Unknown";
                            } else if (participantName.length() > 100) {
                                participantName = participantName.substring(0, 100);
                            }

                            Conversation conv = new Conversation(
                                participantUUID,
                                participantName,
                                convData.isPlayerParticipant
                            );

                            // VALIDATE REPUTATION (-100 to 100)
                            int reputation = convData.reputation;
                            if (reputation < -100 || reputation > 100) {
                                reputation = Math.max(-100, Math.min(100, reputation));
                            }
                            conv.setReputation(reputation);

                            // VALIDATE MESSAGES
                            if (convData.messages != null) {
                                convData.messages.forEach(msgData -> {
                                    try {
                                        if (msgData == null) return;

                                        // VALIDATE SENDER UUID
                                        if (msgData.senderUUID == null || msgData.senderUUID.isEmpty()) {
                                            return;
                                        }

                                        UUID senderUUID;
                                        try {
                                            senderUUID = UUID.fromString(msgData.senderUUID);
                                        } catch (IllegalArgumentException e) {
                                            return;
                                        }

                                        // VALIDATE MESSAGE CONTENT
                                        String content = msgData.content;
                                        if (content == null) content = "";
                                        if (content.length() > 500) {
                                            content = content.substring(0, 500);
                                        }

                                        // VALIDATE SENDER NAME
                                        String senderName = msgData.senderName;
                                        if (senderName == null || senderName.isEmpty()) {
                                            senderName = "Unknown";
                                        } else if (senderName.length() > 100) {
                                            senderName = senderName.substring(0, 100);
                                        }

                                        // VALIDATE TIMESTAMP (>= 0)
                                        long timestamp = msgData.timestamp;
                                        if (timestamp < 0) {
                                            timestamp = 0;
                                        }

                                        Message msg = new Message(
                                            senderUUID,
                                            senderName,
                                            content,
                                            timestamp,
                                            msgData.isPlayerSender
                                        );
                                        conv.addMessage(msg);
                                    } catch (Exception e) {
                                        LOGGER.error("Error loading message", e);
                                    }
                                });
                            }

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

            // SUMMARY
            if (invalidCount > 0 || correctedCount > 0) {
                LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                    invalidCount, correctedCount);
                if (correctedCount > 0) {
                    markDirty(); // Re-save corrected data
                }
            }
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
