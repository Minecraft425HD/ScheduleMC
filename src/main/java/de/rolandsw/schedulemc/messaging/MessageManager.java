package de.rolandsw.schedulemc.messaging;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages all messaging conversations and persistence
 *
 * Features:
 * - Automatische Backup-Rotation
 * - Atomic file writes
 * - Backup-Wiederherstellung bei Korruption
 * - Health-Status-Tracking
 */
public class MessageManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Map<UUID, Conversation>> playerConversations = new ConcurrentHashMap<>();
    private static final File file = new File("config/plotmod_messages.json");
    private static final Gson gson = GsonHelper.get();
    private static boolean needsSave = false;
    private static boolean isHealthy = true;
    private static String lastError = null;

    /**
     * Loads all conversations from JSON file with backup recovery
     */
    public static void loadMessages() {
        if (!file.exists()) {
            LOGGER.info("No messages file found, starting with empty database");
            isHealthy = true;
            return;
        }

        try {
            loadFromFile(file);
            isHealthy = true;
            lastError = null;
            LOGGER.info("Messages loaded: {} players", playerConversations.size());
        } catch (Exception e) {
            LOGGER.error("Error loading messages", e);
            lastError = "Failed to load: " + e.getMessage();

            // Backup-Wiederherstellung
            if (BackupManager.restoreFromBackup(file)) {
                LOGGER.warn("Messages file corrupt, attempting backup recovery...");
                try {
                    loadFromFile(file);
                    LOGGER.info("Messages successfully recovered from backup: {} players", playerConversations.size());
                    isHealthy = true;
                    lastError = "Recovered from backup";
                } catch (Exception backupError) {
                    LOGGER.error("CRITICAL: Backup recovery failed!", backupError);
                    handleCriticalLoadFailure();
                }
            } else {
                LOGGER.error("CRITICAL: No backup available!");
                handleCriticalLoadFailure();
            }
        }
    }

    private static void loadFromFile(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            Map<String, Map<String, ConversationData>> loaded = gson.fromJson(reader,
                new TypeToken<Map<String, Map<String, ConversationData>>>(){}.getType());

            if (loaded == null) {
                throw new IOException("Loaded message data is null");
            }

            playerConversations.clear();

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

                            // Load reputation (default 0 for old data)
                            conv.setReputation(data.reputation);

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
        }
    }

    private static void handleCriticalLoadFailure() {
        LOGGER.error("CRITICAL: Message system could not be loaded!");
        LOGGER.error("Starting with empty message system as fallback");
        playerConversations.clear();
        isHealthy = false;
        lastError = "Critical load failure - running with empty data";

        // Preserve corrupt file for forensics
        if (file.exists()) {
            File corruptBackup = new File(file.getParent(),
                file.getName() + ".CORRUPT_" + System.currentTimeMillis());
            try {
                java.nio.file.Files.copy(file.toPath(), corruptBackup.toPath());
                LOGGER.info("Corrupt file saved to: {}", corruptBackup.getName());
            } catch (IOException e) {
                LOGGER.error("Could not save corrupt file", e);
            }
        }
    }

    /**
     * Saves all conversations to JSON file with backup and atomic writes
     */
    public static void saveMessages() {
        try {
            file.getParentFile().mkdirs();

            // Create backup before overwriting
            if (file.exists() && file.length() > 0) {
                BackupManager.createBackup(file);
            }

            // Temporary file for atomic writing
            File tempFile = new File(file.getParent(), file.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
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

                gson.toJson(saveMap, writer);
                writer.flush();
            }

            // Atomic replace
            java.nio.file.Files.move(tempFile.toPath(), file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);

            needsSave = false;
            isHealthy = true;
            lastError = null;
            LOGGER.info("Messages saved: {} players", playerConversations.size());

        } catch (Exception e) {
            LOGGER.error("CRITICAL: Error saving messages!", e);
            isHealthy = false;
            lastError = "Save failed: " + e.getMessage();
            needsSave = true; // Keep dirty flag for retry
        }
    }

    /**
     * Saves only if changes are present
     */
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
        return isHealthy;
    }

    /**
     * Returns last error message
     */
    @Nullable
    public static String getLastError() {
        return lastError;
    }

    /**
     * Returns health info for monitoring
     */
    public static String getHealthInfo() {
        int totalConversations = playerConversations.values().stream()
            .mapToInt(Map::size)
            .sum();

        if (isHealthy) {
            return String.format("§aGESUND§r - %d Spieler, %d Conversations, %d Backups verfügbar",
                playerConversations.size(), totalConversations, BackupManager.getBackupCount(file));
        } else {
            return String.format("§cUNGESUND§r - Letzter Fehler: %s, %d Spieler, %d Conversations geladen",
                lastError != null ? lastError : "Unknown", playerConversations.size(), totalConversations);
        }
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
}
