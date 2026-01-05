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
     * Loads all messaging conversations from persistent JSON storage.
     * <p>
     * Reads conversation data from the JSON file and deserializes it into the
     * in-memory conversation cache. This method delegates to the underlying
     * persistence manager which handles error recovery, health tracking, and
     * atomic file operations.
     * </p>
     * <p>
     * <strong>File Location:</strong> {@code config/plotmod_messages.json}
     * </p>
     * <p>
     * <strong>Error Handling:</strong> Uses robust error handling with backup file
     * support. On critical load failure, clears the conversation cache to prevent
     * corrupted state.
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> Should be called during server initialization
     * before concurrent access begins.
     * </p>
     *
     * @see #saveMessages()
     * @see #isHealthy()
     */
    public static void loadMessages() {
        persistence.load();
    }

    /**
     * Saves all messaging conversations to persistent JSON storage.
     * <p>
     * Writes the current conversation data to JSON file using atomic file operations
     * (write to temp file, then rename) to prevent corruption. This method delegates
     * to the underlying persistence manager which handles error recovery and backup
     * creation.
     * </p>
     * <p>
     * <strong>File Location:</strong> {@code config/plotmod_messages.json}
     * </p>
     * <p>
     * <strong>Atomicity:</strong> Uses temporary file and atomic rename to ensure
     * file integrity even if the process is interrupted.
     * </p>
     * <p>
     * <strong>Error Handling:</strong> Logs errors and updates health status, but
     * does not throw exceptions.
     * </p>
     *
     * @see #loadMessages()
     * @see #saveIfNeeded()
     */
    public static void saveMessages() {
        persistence.save();
    }

    /**
     * Conditionally saves conversations only if changes have been made.
     * <p>
     * Checks the internal dirty flag and only performs the save operation if
     * the conversation data has been modified since the last save. This optimization
     * prevents unnecessary disk I/O during periodic save operations.
     * </p>
     * <p>
     * <strong>Performance:</strong> No-op if no changes have been made, making this
     * safe to call frequently (e.g., on server tick or periodic intervals).
     * </p>
     *
     * @see #saveMessages()
     */
    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    private static void markDirty() {
        persistence.markDirty();
    }

    /**
     * Sends a message from one entity (player or NPC) to another.
     * <p>
     * Creates a new message and adds it to both the sender's and receiver's
     * conversation history. If conversations don't exist, they are created
     * automatically. The message is added with the current timestamp.
     * </p>
     * <p>
     * <strong>Optimization:</strong> Reuses a single Message object for both
     * conversations instead of creating duplicates, as Message is immutable.
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> This method is thread-safe. Uses ConcurrentHashMap
     * for safe concurrent access to the conversation cache.
     * </p>
     * <p>
     * <strong>Persistence:</strong> Marks the manager as dirty, triggering a save
     * on the next saveIfNeeded() call.
     * </p>
     *
     * @param fromUUID the UUID of the message sender
     * @param fromName the display name of the sender
     * @param isFromPlayer true if the sender is a player, false if NPC
     * @param toUUID the UUID of the message recipient
     * @param toName the display name of the recipient
     * @param isToPlayer true if the recipient is a player, false if NPC
     * @param content the message content text
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
     * Retrieves all conversations for a specific player.
     * <p>
     * Returns a list of all conversations the player has participated in,
     * sorted by most recent message first. This is typically used to display
     * a player's conversation list in a messaging UI.
     * </p>
     * <p>
     * <strong>Sorting:</strong> Conversations are sorted by descending last message
     * timestamp (most recent first).
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> Returns a new list that is safe to iterate
     * without synchronization. The list is a snapshot and won't reflect subsequent changes.
     * </p>
     *
     * @param playerUUID the UUID of the player
     * @return a sorted list of conversations, or an empty list if the player has none
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
     * Retrieves a specific conversation between a player and another participant.
     * <p>
     * Looks up the conversation from the player's perspective. The participant
     * can be either another player or an NPC.
     * </p>
     *
     * @param playerUUID the UUID of the player whose conversations to search
     * @param participantUUID the UUID of the other participant in the conversation
     * @return the conversation if it exists, or null if no conversation exists
     * @see #getOrCreateConversation(UUID, UUID, String, boolean)
     */
    public static Conversation getConversation(UUID playerUUID, UUID participantUUID) {
        Map<UUID, Conversation> convMap = playerConversations.get(playerUUID);
        return convMap != null ? convMap.get(participantUUID) : null;
    }

    /**
     * Retrieves an existing conversation or creates a new one if it doesn't exist.
     * <p>
     * This is the primary method for opening a chat interface with an NPC or player.
     * If a conversation already exists, it is returned immediately. If not, a new
     * empty conversation is created and registered in the system.
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> This method is thread-safe using ConcurrentHashMap's
     * atomic computeIfAbsent operations.
     * </p>
     * <p>
     * <strong>Persistence:</strong> Creating a new conversation marks the manager as
     * dirty, triggering a save on the next saveIfNeeded() call.
     * </p>
     *
     * @param playerUUID the UUID of the player
     * @param participantUUID the UUID of the other participant (player or NPC)
     * @param participantName the display name of the participant
     * @param isPlayerParticipant true if the participant is a player, false if NPC
     * @return the existing or newly created conversation
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
     * Checks the health status of the messaging persistence system.
     * <p>
     * Returns false if recent load or save operations have encountered errors.
     * Used for monitoring and alerting in production environments.
     * </p>
     *
     * @return true if the persistence system is operating normally, false if errors
     *         have occurred recently
     * @see #getLastError()
     * @see #getHealthInfo()
     */
    public static boolean isHealthy() {
        return persistence.isHealthy();
    }

    /**
     * Retrieves the most recent error message from the persistence system.
     * <p>
     * Used for diagnostics and troubleshooting when {@link #isHealthy()} returns false.
     * </p>
     *
     * @return the last error message, or null if no errors have occurred
     * @see #isHealthy()
     * @see #getHealthInfo()
     */
    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    /**
     * Retrieves detailed health information for monitoring and diagnostics.
     * <p>
     * Returns a formatted string containing component status, statistics (number
     * of players and conversations), and any error messages. This is useful for
     * admin commands, dashboards, and monitoring systems.
     * </p>
     *
     * @return a formatted health status string including statistics and error information
     * @see #isHealthy()
     * @see #getLastError()
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
