package de.rolandsw.schedulemc.messaging;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for MessageManager
 *
 * Tests cover:
 * - Message Sending (bidirectional, player-to-player, player-to-NPC)
 * - Conversation Retrieval (sorted by time)
 * - Conversation Creation (getOrCreate pattern)
 * - Persistence (load/save with JSON)
 * - Health Monitoring
 * - Thread Safety
 * - Edge Cases
 */
@DisplayName("MessageManager Tests")
class MessageManagerTest {

    @TempDir
    Path tempDir;

    private File configFile;

    @BeforeEach
    void setUp() throws Exception {
        // Setup temp config file
        configFile = tempDir.resolve("plotmod_messages.json").toFile();

        // Update MessageManager to use temp file
        Field fileField = MessageManager.class.getDeclaredField("file");
        fileField.setAccessible(true);
        fileField.set(null, configFile);

        // Clear static state
        clearMessageManagerState();

        // Reinitialize persistence with new file
        Field persistenceField = MessageManager.class.getDeclaredField("persistence");
        persistenceField.setAccessible(true);
        Object persistence = persistenceField.get(null);

        Field dataFileField = persistence.getClass().getSuperclass().getDeclaredField("dataFile");
        dataFileField.setAccessible(true);
        dataFileField.set(persistence, configFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        clearMessageManagerState();
    }

    private void clearMessageManagerState() throws Exception {
        Field conversationsField = MessageManager.class.getDeclaredField("playerConversations");
        conversationsField.setAccessible(true);
        Map<UUID, Map<UUID, Conversation>> conversations =
            (Map<UUID, Map<UUID, Conversation>>) conversationsField.get(null);
        conversations.clear();

        // Reset dirty flag
        Field persistenceField = MessageManager.class.getDeclaredField("persistence");
        persistenceField.setAccessible(true);
        Object persistence = persistenceField.get(null);

        Field dirtyField = persistence.getClass().getSuperclass().getDeclaredField("dirty");
        dirtyField.setAccessible(true);
        dirtyField.setBoolean(persistence, false);
    }

    // ═══════════════════════════════════════════════════════════
    // MESSAGE SENDING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Message Sending Tests")
    class MessageSendingTests {

        @Test
        @DisplayName("Should send message from player to player")
        void testSendMessagePlayerToPlayer() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            MessageManager.sendMessage(
                player1UUID, "Player1", true,
                player2UUID, "Player2", true,
                "Hello Player2!"
            );

            // Check sender's conversation
            Conversation senderConv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(senderConv).isNotNull();
            assertThat(senderConv.getMessages()).hasSize(1);
            assertThat(senderConv.getMessages().get(0).getContent()).isEqualTo("Hello Player2!");

            // Check receiver's conversation
            Conversation receiverConv = MessageManager.getConversation(player2UUID, player1UUID);
            assertThat(receiverConv).isNotNull();
            assertThat(receiverConv.getMessages()).hasSize(1);
            assertThat(receiverConv.getMessages().get(0).getContent()).isEqualTo("Hello Player2!");
        }

        @Test
        @DisplayName("Should send message from player to NPC")
        void testSendMessagePlayerToNPC() {
            UUID playerUUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();

            MessageManager.sendMessage(
                playerUUID, "Player", true,
                npcUUID, "NPC Guard", false,
                "Hello NPC!"
            );

            // Check player's conversation with NPC
            Conversation playerConv = MessageManager.getConversation(playerUUID, npcUUID);
            assertThat(playerConv).isNotNull();
            assertThat(playerConv.getParticipantName()).isEqualTo("NPC Guard");
            assertThat(playerConv.isPlayerParticipant()).isFalse();
            assertThat(playerConv.getMessages()).hasSize(1);

            // Check NPC's conversation with player
            Conversation npcConv = MessageManager.getConversation(npcUUID, playerUUID);
            assertThat(npcConv).isNotNull();
            assertThat(npcConv.getParticipantName()).isEqualTo("Player");
            assertThat(npcConv.isPlayerParticipant()).isTrue();
        }

        @Test
        @DisplayName("Should send message from NPC to player")
        void testSendMessageNPCToPlayer() {
            UUID npcUUID = UUID.randomUUID();
            UUID playerUUID = UUID.randomUUID();

            MessageManager.sendMessage(
                npcUUID, "NPC Guard", false,
                playerUUID, "Player", true,
                "Halt! Who goes there?"
            );

            Conversation playerConv = MessageManager.getConversation(playerUUID, npcUUID);
            assertThat(playerConv).isNotNull();
            assertThat(playerConv.getMessages()).hasSize(1);
            assertThat(playerConv.getMessages().get(0).getSenderName()).isEqualTo("NPC Guard");
            assertThat(playerConv.getMessages().get(0).isPlayerSender()).isFalse();
        }

        @Test
        @DisplayName("Should send multiple messages in same conversation")
        void testSendMultipleMessages() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Hello!");
            MessageManager.sendMessage(player2UUID, "Bob", true, player1UUID, "Alice", true, "Hi Alice!");
            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "How are you?");

            Conversation aliceConv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(aliceConv.getMessages())
                .hasSize(3)
                .extracting(Message::getContent)
                .containsExactly("Hello!", "Hi Alice!", "How are you?");
        }

        @Test
        @DisplayName("Should update lastMessageTime when sending message")
        void testLastMessageTimeUpdate() throws InterruptedException {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "First message");
            long firstTime = MessageManager.getConversation(player1UUID, player2UUID).getLastMessageTime();

            Thread.sleep(10); // Ensure time difference

            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Second message");
            long secondTime = MessageManager.getConversation(player1UUID, player2UUID).getLastMessageTime();

            assertThat(secondTime).isGreaterThan(firstTime);
        }

        @Test
        @DisplayName("Should create conversation automatically if it doesn't exist")
        void testAutoCreateConversation() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            // Before sending, no conversation exists
            assertThat(MessageManager.getConversation(player1UUID, player2UUID)).isNull();

            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Hello!");

            // After sending, conversation exists
            assertThat(MessageManager.getConversation(player1UUID, player2UUID)).isNotNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONVERSATION RETRIEVAL TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Conversation Retrieval Tests")
    class ConversationRetrievalTests {

        @Test
        @DisplayName("Should retrieve all conversations for a player")
        void testGetConversations() {
            UUID playerUUID = UUID.randomUUID();
            UUID npc1UUID = UUID.randomUUID();
            UUID npc2UUID = UUID.randomUUID();
            UUID npc3UUID = UUID.randomUUID();

            MessageManager.sendMessage(playerUUID, "Player", true, npc1UUID, "NPC1", false, "Hi NPC1");
            MessageManager.sendMessage(playerUUID, "Player", true, npc2UUID, "NPC2", false, "Hi NPC2");
            MessageManager.sendMessage(playerUUID, "Player", true, npc3UUID, "NPC3", false, "Hi NPC3");

            List<Conversation> conversations = MessageManager.getConversations(playerUUID);

            assertThat(conversations).hasSize(3);
            assertThat(conversations)
                .extracting(Conversation::getParticipantName)
                .containsExactlyInAnyOrder("NPC1", "NPC2", "NPC3");
        }

        @Test
        @DisplayName("Should return conversations sorted by most recent first")
        void testGetConversationsSorted() throws InterruptedException {
            UUID playerUUID = UUID.randomUUID();
            UUID npc1UUID = UUID.randomUUID();
            UUID npc2UUID = UUID.randomUUID();
            UUID npc3UUID = UUID.randomUUID();

            MessageManager.sendMessage(playerUUID, "Player", true, npc1UUID, "NPC1", false, "Message 1");
            Thread.sleep(10);

            MessageManager.sendMessage(playerUUID, "Player", true, npc2UUID, "NPC2", false, "Message 2");
            Thread.sleep(10);

            MessageManager.sendMessage(playerUUID, "Player", true, npc3UUID, "NPC3", false, "Message 3");

            List<Conversation> conversations = MessageManager.getConversations(playerUUID);

            // Most recent first
            assertThat(conversations)
                .extracting(Conversation::getParticipantName)
                .containsExactly("NPC3", "NPC2", "NPC1");
        }

        @Test
        @DisplayName("Should return empty list for player with no conversations")
        void testGetConversationsEmpty() {
            UUID playerUUID = UUID.randomUUID();

            List<Conversation> conversations = MessageManager.getConversations(playerUUID);

            assertThat(conversations).isEmpty();
        }

        @Test
        @DisplayName("Should retrieve specific conversation")
        void testGetConversation() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Hello Bob!");

            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);

            assertThat(conv).isNotNull();
            assertThat(conv.getParticipantUUID()).isEqualTo(player2UUID);
            assertThat(conv.getParticipantName()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("Should return null for non-existent conversation")
        void testGetConversationNonExistent() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);

            assertThat(conv).isNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GET OR CREATE CONVERSATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Or Create Conversation Tests")
    class GetOrCreateConversationTests {

        @Test
        @DisplayName("Should create new conversation if it doesn't exist")
        void testGetOrCreateConversation_New() {
            UUID playerUUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();

            Conversation conv = MessageManager.getOrCreateConversation(
                playerUUID, npcUUID, "NPC Guard", false
            );

            assertThat(conv).isNotNull();
            assertThat(conv.getParticipantUUID()).isEqualTo(npcUUID);
            assertThat(conv.getParticipantName()).isEqualTo("NPC Guard");
            assertThat(conv.isPlayerParticipant()).isFalse();
            assertThat(conv.getMessages()).isEmpty();
        }

        @Test
        @DisplayName("Should return existing conversation if it exists")
        void testGetOrCreateConversation_Existing() {
            UUID playerUUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();

            // Create conversation with a message
            MessageManager.sendMessage(playerUUID, "Player", true, npcUUID, "NPC", false, "Hello!");

            // Get or create should return existing conversation
            Conversation conv = MessageManager.getOrCreateConversation(
                playerUUID, npcUUID, "NPC", false
            );

            assertThat(conv.getMessages()).hasSize(1);
            assertThat(conv.getMessages().get(0).getContent()).isEqualTo("Hello!");
        }

        @Test
        @DisplayName("Should not create duplicate conversation")
        void testGetOrCreateConversation_NoDuplicates() {
            UUID playerUUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();

            Conversation conv1 = MessageManager.getOrCreateConversation(
                playerUUID, npcUUID, "NPC", false
            );
            Conversation conv2 = MessageManager.getOrCreateConversation(
                playerUUID, npcUUID, "NPC", false
            );

            assertThat(conv1).isSameAs(conv2);

            List<Conversation> conversations = MessageManager.getConversations(playerUUID);
            assertThat(conversations).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Persistence Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Should save conversations to file")
        void testSaveMessages() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Hello!");

            MessageManager.saveMessages();

            assertThat(configFile).exists();
        }

        @Test
        @DisplayName("Should load conversations from file")
        void testLoadMessages() throws Exception {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            // Setup: Save some data
            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Hello!");
            MessageManager.sendMessage(player2UUID, "Bob", true, player1UUID, "Alice", true, "Hi Alice!");
            MessageManager.saveMessages();

            // Clear state
            clearMessageManagerState();

            // Test: Load data
            MessageManager.loadMessages();

            // Verify data restored
            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(conv).isNotNull();
            assertThat(conv.getMessages()).hasSize(2);
            assertThat(conv.getMessages().get(0).getContent()).isEqualTo("Hello!");
            assertThat(conv.getMessages().get(1).getContent()).isEqualTo("Hi Alice!");
        }

        @Test
        @DisplayName("Should preserve all conversation data through save/load cycle")
        void testSaveLoadCycle() throws Exception {
            UUID playerUUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();

            // Create conversation with reputation
            MessageManager.sendMessage(playerUUID, "Player", true, npcUUID, "NPC", false, "Hello!");
            Conversation originalConv = MessageManager.getConversation(playerUUID, npcUUID);
            originalConv.setReputation(75);

            // Save
            MessageManager.saveMessages();

            // Clear
            clearMessageManagerState();

            // Load
            MessageManager.loadMessages();

            // Verify all data preserved
            Conversation loadedConv = MessageManager.getConversation(playerUUID, npcUUID);
            assertThat(loadedConv).isNotNull();
            assertThat(loadedConv.getParticipantName()).isEqualTo("NPC");
            assertThat(loadedConv.isPlayerParticipant()).isFalse();
            assertThat(loadedConv.getReputation()).isEqualTo(75);
            assertThat(loadedConv.getMessages()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle load when file does not exist")
        void testLoadNonExistentFile() {
            if (configFile.exists()) {
                configFile.delete();
            }

            assertThatCode(() -> MessageManager.loadMessages())
                .doesNotThrowAnyException();

            List<Conversation> conversations = MessageManager.getConversations(UUID.randomUUID());
            assertThat(conversations).isEmpty();
        }

        @Test
        @DisplayName("Should only save when dirty flag is set")
        void testSaveIfNeeded() throws Exception {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            // Send message (marks dirty)
            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Hello!");

            // Save
            MessageManager.saveIfNeeded();
            assertThat(configFile).exists();

            long lastModified = configFile.lastModified();

            // Clear dirty flag by doing a full save
            MessageManager.saveMessages();

            Thread.sleep(10);

            // saveIfNeeded should not write when not dirty
            MessageManager.saveIfNeeded();

            // File modification time should not change (approximately)
            assertThat(configFile.lastModified()).isEqualTo(lastModified);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HEALTH MONITORING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Health Monitoring Tests")
    class HealthMonitoringTests {

        @Test
        @DisplayName("Should be healthy after successful operations")
        void testIsHealthy() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Hello!");
            MessageManager.saveMessages();

            assertThat(MessageManager.isHealthy()).isTrue();
        }

        @Test
        @DisplayName("Should return null for lastError when healthy")
        void testGetLastError_Healthy() {
            assertThat(MessageManager.getLastError()).isNull();
        }

        @Test
        @DisplayName("Should return health info with statistics")
        void testGetHealthInfo() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();

            MessageManager.sendMessage(player1UUID, "Alice", true, player2UUID, "Bob", true, "Hello!");
            MessageManager.sendMessage(player1UUID, "Alice", true, npcUUID, "NPC", false, "Hi NPC!");

            String healthInfo = MessageManager.getHealthInfo();

            assertThat(healthInfo)
                .contains("Message System")
                .contains("Spieler")
                .contains("Conversations");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // THREAD SAFETY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent message sending safely")
        @Timeout(10)
        void testConcurrentMessageSending() throws InterruptedException {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();
            int numThreads = 10;
            int messagesPerThread = 100;

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < messagesPerThread; j++) {
                            MessageManager.sendMessage(
                                player1UUID, "Player1", true,
                                player2UUID, "Player2", true,
                                "Message " + threadId + "-" + j
                            );
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(conv.getMessages()).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle concurrent conversation creation safely")
        @Timeout(10)
        void testConcurrentConversationCreation() throws InterruptedException {
            UUID playerUUID = UUID.randomUUID();
            int numThreads = 20;
            int conversationsPerThread = 50;

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < conversationsPerThread; j++) {
                            UUID npcUUID = UUID.randomUUID();
                            MessageManager.getOrCreateConversation(
                                playerUUID, npcUUID, "NPC-" + npcUUID, false
                            );
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            List<Conversation> conversations = MessageManager.getConversations(playerUUID);
            assertThat(conversations).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle concurrent getOrCreate for same conversation safely")
        @Timeout(10)
        void testConcurrentGetOrCreate_SameConversation() throws InterruptedException {
            UUID playerUUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();
            int numThreads = 20;

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);
            Set<Conversation> conversations = ConcurrentHashMap.newKeySet();

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        Conversation conv = MessageManager.getOrCreateConversation(
                            playerUUID, npcUUID, "NPC", false
                        );
                        conversations.add(conv);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // All threads should get the same conversation instance
            assertThat(conversations).hasSize(1);

            // Only one conversation should exist in the manager
            List<Conversation> allConversations = MessageManager.getConversations(playerUUID);
            assertThat(allConversations).hasSize(1);
        }

        @Test
        @DisplayName("Should maintain data consistency under concurrent load")
        @Timeout(15)
        void testConcurrentLoadDataConsistency() throws InterruptedException {
            int numPlayers = 50;
            int numNPCs = 20;
            List<UUID> playerUUIDs = new ArrayList<>();
            List<UUID> npcUUIDs = new ArrayList<>();

            for (int i = 0; i < numPlayers; i++) {
                playerUUIDs.add(UUID.randomUUID());
            }
            for (int i = 0; i < numNPCs; i++) {
                npcUUIDs.add(UUID.randomUUID());
            }

            ExecutorService executor = Executors.newFixedThreadPool(20);
            CountDownLatch latch = new CountDownLatch(numPlayers);

            for (UUID playerUUID : playerUUIDs) {
                executor.submit(() -> {
                    try {
                        for (UUID npcUUID : npcUUIDs) {
                            MessageManager.sendMessage(
                                playerUUID, "Player", true,
                                npcUUID, "NPC", false,
                                "Hello"
                            );
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // Each player should have conversations with all NPCs
            for (UUID playerUUID : playerUUIDs) {
                List<Conversation> conversations = MessageManager.getConversations(playerUUID);
                assertThat(conversations).hasSize(numNPCs);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASES TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long message content")
        void testVeryLongMessage() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();
            String longMessage = "A".repeat(10000);

            MessageManager.sendMessage(
                player1UUID, "Alice", true,
                player2UUID, "Bob", true,
                longMessage
            );

            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(conv.getMessages().get(0).getContent()).hasSize(10000);
        }

        @Test
        @DisplayName("Should handle empty message content")
        void testEmptyMessage() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            MessageManager.sendMessage(
                player1UUID, "Alice", true,
                player2UUID, "Bob", true,
                ""
            );

            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(conv.getMessages().get(0).getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in message content")
        void testSpecialCharacters() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();
            String specialMessage = "§a§lHello! §r<>&\"'{}[]";

            MessageManager.sendMessage(
                player1UUID, "Alice", true,
                player2UUID, "Bob", true,
                specialMessage
            );

            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(conv.getMessages().get(0).getContent()).isEqualTo(specialMessage);
        }

        @Test
        @DisplayName("Should handle large number of conversations per player")
        void testLargeNumberOfConversations() {
            UUID playerUUID = UUID.randomUUID();
            int numConversations = 1000;

            for (int i = 0; i < numConversations; i++) {
                UUID npcUUID = UUID.randomUUID();
                MessageManager.sendMessage(
                    playerUUID, "Player", true,
                    npcUUID, "NPC-" + i, false,
                    "Hello " + i
                );
            }

            List<Conversation> conversations = MessageManager.getConversations(playerUUID);
            assertThat(conversations).hasSize(numConversations);
        }

        @Test
        @DisplayName("Should handle large number of messages in one conversation")
        void testLargeNumberOfMessages() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();
            int numMessages = 10000;

            for (int i = 0; i < numMessages; i++) {
                MessageManager.sendMessage(
                    player1UUID, "Alice", true,
                    player2UUID, "Bob", true,
                    "Message " + i
                );
            }

            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(conv.getMessages()).hasSize(numMessages);
        }

        @Test
        @DisplayName("Should handle Unicode characters in names and messages")
        void testUnicodeCharacters() {
            UUID player1UUID = UUID.randomUUID();
            UUID player2UUID = UUID.randomUUID();

            MessageManager.sendMessage(
                player1UUID, "Alice™", true,
                player2UUID, "Bob😀", true,
                "Hello 世界! 🎮"
            );

            Conversation conv = MessageManager.getConversation(player1UUID, player2UUID);
            assertThat(conv.getParticipantName()).isEqualTo("Bob😀");
            assertThat(conv.getMessages().get(0).getContent()).isEqualTo("Hello 世界! 🎮");
        }

        @Test
        @DisplayName("Should handle getting conversations returns defensive copy")
        void testGetConversationsDefensiveCopy() {
            UUID playerUUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();

            MessageManager.sendMessage(playerUUID, "Player", true, npcUUID, "NPC", false, "Hello!");

            List<Conversation> conversations1 = MessageManager.getConversations(playerUUID);
            List<Conversation> conversations2 = MessageManager.getConversations(playerUUID);

            assertThat(conversations1).isNotSameAs(conversations2);
            assertThat(conversations1).containsExactlyElementsOf(conversations2);
        }

        @Test
        @DisplayName("Should handle modifying returned conversation list without affecting internal state")
        void testModifyReturnedList() {
            UUID playerUUID = UUID.randomUUID();
            UUID npcUUID = UUID.randomUUID();

            MessageManager.sendMessage(playerUUID, "Player", true, npcUUID, "NPC", false, "Hello!");

            List<Conversation> conversations = MessageManager.getConversations(playerUUID);
            conversations.clear();

            // Internal state should remain unchanged
            List<Conversation> actual = MessageManager.getConversations(playerUUID);
            assertThat(actual).hasSize(1);
        }
    }
}
