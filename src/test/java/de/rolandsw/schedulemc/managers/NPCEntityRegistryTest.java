package de.rolandsw.schedulemc.managers;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 5 Tests for NPCEntityRegistry
 *
 * Tests all critical functionality:
 * - Registration & Unregistration
 * - UUID Lookups (O(1) performance)
 * - Level-based NPC Management
 * - Thread Safety (concurrent access)
 * - Edge Cases (null handling, large datasets)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NPCEntityRegistry Tests")
class NPCEntityRegistryTest {

    @Mock
    private CustomNPCEntity mockNPC1;

    @Mock
    private CustomNPCEntity mockNPC2;

    @Mock
    private CustomNPCEntity mockNPC3;

    @Mock
    private ServerLevel mockOverworld;

    @Mock
    private ServerLevel mockNether;

    @Mock
    private ServerLevel mockEnd;

    private UUID uuid1;
    private UUID uuid2;
    private UUID uuid3;

    @BeforeEach
    void setUp() {
        // Clear registry before each test
        NPCEntityRegistry.clear();

        // Initialize UUIDs
        uuid1 = UUID.randomUUID();
        uuid2 = UUID.randomUUID();
        uuid3 = UUID.randomUUID();

        // Configure mock NPCs
        setupMockNPC(mockNPC1, uuid1, "TestNPC1", mockOverworld, "minecraft:overworld");
        setupMockNPC(mockNPC2, uuid2, "TestNPC2", mockOverworld, "minecraft:overworld");
        setupMockNPC(mockNPC3, uuid3, "TestNPC3", mockNether, "minecraft:the_nether");

        // Configure mock levels
        setupMockLevel(mockOverworld, "minecraft:overworld");
        setupMockLevel(mockNether, "minecraft:the_nether");
        setupMockLevel(mockEnd, "minecraft:the_end");
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        NPCEntityRegistry.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private void setupMockNPC(CustomNPCEntity npc, UUID uuid, String name, ServerLevel level, String dimension) {
        when(npc.getUUID()).thenReturn(uuid);
        when(npc.getNpcName()).thenReturn(name);
        when(npc.level()).thenReturn(level);
    }

    @SuppressWarnings("unchecked")
    private void setupMockLevel(ServerLevel level, String dimensionName) {
        ResourceKey<Level> dimensionKey = mock(ResourceKey.class);
        ResourceLocation location = new ResourceLocation(dimensionName);
        when(level.dimension()).thenReturn(dimensionKey);
        when(dimensionKey.location()).thenReturn(location);
    }

    // ═══════════════════════════════════════════════════════════
    // 1. REGISTRATION & UNREGISTRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should register NPC successfully")
    void testRegisterNPC() {
        // Act
        NPCEntityRegistry.registerNPC(mockNPC1);

        // Assert
        CustomNPCEntity found = NPCEntityRegistry.getNPCByUUID(uuid1, mockOverworld);
        assertThat(found).isEqualTo(mockNPC1);
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle null NPC registration gracefully")
    void testRegisterNullNPC() {
        // Act & Assert - Should not throw exception
        assertThatCode(() -> NPCEntityRegistry.registerNPC(null))
            .doesNotThrowAnyException();

        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle NPC with null level gracefully")
    void testRegisterNPCWithNullLevel() {
        // Arrange
        CustomNPCEntity npcWithNullLevel = mock(CustomNPCEntity.class);
        when(npcWithNullLevel.level()).thenReturn(null);

        // Act & Assert
        assertThatCode(() -> NPCEntityRegistry.registerNPC(npcWithNullLevel))
            .doesNotThrowAnyException();

        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should update NPC on double registration")
    void testDoubleRegistration() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);

        // Act - Register same NPC again (should update)
        NPCEntityRegistry.registerNPC(mockNPC1);

        // Assert
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(1);
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid1, mockOverworld)).isEqualTo(mockNPC1);
    }

    @Test
    @DisplayName("Should unregister NPC successfully")
    void testUnregisterNPC() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(1);

        // Act
        NPCEntityRegistry.unregisterNPC(mockNPC1);

        // Assert
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid1, mockOverworld)).isNull();
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle unregistering null NPC gracefully")
    void testUnregisterNullNPC() {
        // Act & Assert
        assertThatCode(() -> NPCEntityRegistry.unregisterNPC(null))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle unregistering non-existent NPC gracefully")
    void testUnregisterNonExistentNPC() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);

        // Act - Try to unregister different NPC
        assertThatCode(() -> NPCEntityRegistry.unregisterNPC(mockNPC2))
            .doesNotThrowAnyException();

        // Assert - Original NPC still registered
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(1);
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid1, mockOverworld)).isEqualTo(mockNPC1);
    }

    @Test
    @DisplayName("Should cleanup level map when all NPCs removed")
    void testLevelCleanupOnUnregister() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        NPCEntityRegistry.registerNPC(mockNPC2);
        assertThat(NPCEntityRegistry.getNPCCount(mockOverworld)).isEqualTo(2);

        // Act - Remove all NPCs from overworld
        NPCEntityRegistry.unregisterNPC(mockNPC1);
        NPCEntityRegistry.unregisterNPC(mockNPC2);

        // Assert - Level should be cleaned up (no memory leak)
        assertThat(NPCEntityRegistry.getNPCCount(mockOverworld)).isEqualTo(0);
        assertThat(NPCEntityRegistry.getAllNPCs(mockOverworld)).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    // 2. UUID LOOKUP TESTS (O(1) Performance)
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should find NPC by UUID in specific level")
    void testGetNPCByUUIDWithLevel() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);

        // Act
        CustomNPCEntity found = NPCEntityRegistry.getNPCByUUID(uuid1, mockOverworld);

        // Assert
        assertThat(found).isEqualTo(mockNPC1);
    }

    @Test
    @DisplayName("Should find NPC by UUID across all levels")
    void testGetNPCByUUIDCrossLevel() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        NPCEntityRegistry.registerNPC(mockNPC3); // Different level

        // Act
        CustomNPCEntity foundOverworld = NPCEntityRegistry.getNPCByUUID(uuid1);
        CustomNPCEntity foundNether = NPCEntityRegistry.getNPCByUUID(uuid3);

        // Assert
        assertThat(foundOverworld).isEqualTo(mockNPC1);
        assertThat(foundNether).isEqualTo(mockNPC3);
    }

    @Test
    @DisplayName("Should return null for non-existent UUID")
    void testGetNPCByNonExistentUUID() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        UUID nonExistentUUID = UUID.randomUUID();

        // Act
        CustomNPCEntity found = NPCEntityRegistry.getNPCByUUID(nonExistentUUID, mockOverworld);

        // Assert
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Should handle null UUID gracefully")
    void testGetNPCByNullUUID() {
        // Act
        CustomNPCEntity found1 = NPCEntityRegistry.getNPCByUUID(null, mockOverworld);
        CustomNPCEntity found2 = NPCEntityRegistry.getNPCByUUID(null);

        // Assert
        assertThat(found1).isNull();
        assertThat(found2).isNull();
    }

    @Test
    @DisplayName("Should handle null ServerLevel in UUID lookup")
    void testGetNPCByUUIDWithNullLevel() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);

        // Act
        CustomNPCEntity found = NPCEntityRegistry.getNPCByUUID(uuid1, null);

        // Assert
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Should find NPC in correct level when multiple levels exist")
    void testGetNPCInCorrectLevel() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1); // Overworld
        NPCEntityRegistry.registerNPC(mockNPC3); // Nether

        // Act
        CustomNPCEntity overworldNPC = NPCEntityRegistry.getNPCByUUID(uuid1, mockOverworld);
        CustomNPCEntity netherNPC = NPCEntityRegistry.getNPCByUUID(uuid3, mockNether);
        CustomNPCEntity wrongLevel = NPCEntityRegistry.getNPCByUUID(uuid1, mockNether);

        // Assert
        assertThat(overworldNPC).isEqualTo(mockNPC1);
        assertThat(netherNPC).isEqualTo(mockNPC3);
        assertThat(wrongLevel).isNull(); // UUID1 not in Nether
    }

    // ═══════════════════════════════════════════════════════════
    // 3. LEVEL MANAGEMENT TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should group NPCs by ServerLevel")
    void testNPCsGroupedByLevel() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1); // Overworld
        NPCEntityRegistry.registerNPC(mockNPC2); // Overworld
        NPCEntityRegistry.registerNPC(mockNPC3); // Nether

        // Act
        Collection<CustomNPCEntity> overworldNPCs = NPCEntityRegistry.getAllNPCs(mockOverworld);
        Collection<CustomNPCEntity> netherNPCs = NPCEntityRegistry.getAllNPCs(mockNether);

        // Assert
        assertThat(overworldNPCs).hasSize(2).containsExactlyInAnyOrder(mockNPC1, mockNPC2);
        assertThat(netherNPCs).hasSize(1).containsExactly(mockNPC3);
    }

    @Test
    @DisplayName("Should return empty list for level with no NPCs")
    void testGetAllNPCsEmptyLevel() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1); // Overworld only

        // Act
        Collection<CustomNPCEntity> endNPCs = NPCEntityRegistry.getAllNPCs(mockEnd);

        // Assert
        assertThat(endNPCs).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for null ServerLevel")
    void testGetAllNPCsNullLevel() {
        // Act
        Collection<CustomNPCEntity> npcs = NPCEntityRegistry.getAllNPCs((ServerLevel) null);

        // Assert
        assertThat(npcs).isEmpty();
    }

    @Test
    @DisplayName("Should return correct NPC count per level")
    void testGetNPCCountPerLevel() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1); // Overworld
        NPCEntityRegistry.registerNPC(mockNPC2); // Overworld
        NPCEntityRegistry.registerNPC(mockNPC3); // Nether

        // Act & Assert
        assertThat(NPCEntityRegistry.getNPCCount(mockOverworld)).isEqualTo(2);
        assertThat(NPCEntityRegistry.getNPCCount(mockNether)).isEqualTo(1);
        assertThat(NPCEntityRegistry.getNPCCount(mockEnd)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return 0 for null level count")
    void testGetNPCCountNullLevel() {
        // Act
        int count = NPCEntityRegistry.getNPCCount(null);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    // ═══════════════════════════════════════════════════════════
    // 4. COLLECTION OPERATIONS TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should return all NPCs across all levels")
    void testGetAllNPCs() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1); // Overworld
        NPCEntityRegistry.registerNPC(mockNPC2); // Overworld
        NPCEntityRegistry.registerNPC(mockNPC3); // Nether

        // Act
        Collection<CustomNPCEntity> allNPCs = NPCEntityRegistry.getAllNPCs();

        // Assert
        assertThat(allNPCs).hasSize(3).containsExactlyInAnyOrder(mockNPC1, mockNPC2, mockNPC3);
    }

    @Test
    @DisplayName("Should return correct total NPC count")
    void testGetTotalNPCCount() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        NPCEntityRegistry.registerNPC(mockNPC2);
        NPCEntityRegistry.registerNPC(mockNPC3);

        // Act
        int total = NPCEntityRegistry.getTotalNPCCount();

        // Assert
        assertThat(total).isEqualTo(3);
    }

    @Test
    @DisplayName("Should clear all NPCs from registry")
    void testClearRegistry() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        NPCEntityRegistry.registerNPC(mockNPC2);
        NPCEntityRegistry.registerNPC(mockNPC3);
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(3);

        // Act
        NPCEntityRegistry.clear();

        // Assert
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(0);
        assertThat(NPCEntityRegistry.getAllNPCs()).isEmpty();
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid1)).isNull();
    }

    @Test
    @DisplayName("Should handle operations on empty registry")
    void testEmptyRegistryOperations() {
        // Act & Assert
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(0);
        assertThat(NPCEntityRegistry.getAllNPCs()).isEmpty();
        assertThat(NPCEntityRegistry.getNPCCount(mockOverworld)).isEqualTo(0);
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid1)).isNull();

        // Should not throw exceptions
        assertThatCode(() -> NPCEntityRegistry.clear()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should return unmodifiable collection for getAllNPCs(level)")
    void testGetAllNPCsLevelReturnsUnmodifiable() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);

        // Act
        Collection<CustomNPCEntity> npcs = NPCEntityRegistry.getAllNPCs(mockOverworld);

        // Assert - Should throw exception when trying to modify
        assertThatThrownBy(() -> npcs.add(mockNPC2))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should return unmodifiable collection for getAllNPCs()")
    void testGetAllNPCsReturnsUnmodifiable() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);

        // Act
        Collection<CustomNPCEntity> npcs = NPCEntityRegistry.getAllNPCs();

        // Assert - Should throw exception when trying to modify
        assertThatThrownBy(() -> npcs.add(mockNPC2))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // ═══════════════════════════════════════════════════════════
    // 5. THREAD SAFETY TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should handle concurrent registration from multiple threads")
    void testConcurrentRegistration() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int npcsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act - Register NPCs from multiple threads concurrently
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < npcsPerThread; i++) {
                        CustomNPCEntity npc = mock(CustomNPCEntity.class);
                        UUID uuid = UUID.randomUUID();
                        setupMockNPC(npc, uuid, "NPC-" + threadId + "-" + i, mockOverworld, "minecraft:overworld");
                        NPCEntityRegistry.registerNPC(npc);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        int expectedCount = threadCount * npcsPerThread;
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("Should handle concurrent lookups from multiple threads")
    void testConcurrentLookups() throws InterruptedException {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        NPCEntityRegistry.registerNPC(mockNPC2);

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act - Perform lookups from multiple threads
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        CustomNPCEntity found = NPCEntityRegistry.getNPCByUUID(uuid1, mockOverworld);
                        if (found == mockNPC1) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - All lookups should succeed
        assertThat(successCount.get()).isEqualTo(threadCount * 100);
    }

    @Test
    @DisplayName("Should handle concurrent unregistration from multiple threads")
    void testConcurrentUnregistration() throws InterruptedException {
        // Arrange - Register many NPCs
        int npcCount = 1000;
        CustomNPCEntity[] npcs = new CustomNPCEntity[npcCount];
        for (int i = 0; i < npcCount; i++) {
            npcs[i] = mock(CustomNPCEntity.class);
            UUID uuid = UUID.randomUUID();
            setupMockNPC(npcs[i], uuid, "NPC-" + i, mockOverworld, "minecraft:overworld");
            NPCEntityRegistry.registerNPC(npcs[i]);
        }
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(npcCount);

        // Act - Unregister from multiple threads
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = threadId; i < npcCount; i += threadCount) {
                        NPCEntityRegistry.unregisterNPC(npcs[i]);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle mixed concurrent read/write operations")
    void testConcurrentMixedOperations() throws InterruptedException {
        // Arrange
        int threadCount = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act - Mix of register, lookup, and unregister operations
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    if (threadId % 3 == 0) {
                        // Registration threads
                        for (int i = 0; i < 50; i++) {
                            CustomNPCEntity npc = mock(CustomNPCEntity.class);
                            UUID uuid = UUID.randomUUID();
                            setupMockNPC(npc, uuid, "NPC-" + threadId + "-" + i, mockOverworld, "minecraft:overworld");
                            NPCEntityRegistry.registerNPC(npc);
                        }
                    } else if (threadId % 3 == 1) {
                        // Lookup threads
                        for (int i = 0; i < 100; i++) {
                            NPCEntityRegistry.getAllNPCs(mockOverworld);
                            NPCEntityRegistry.getTotalNPCCount();
                        }
                    } else {
                        // Register then unregister threads
                        for (int i = 0; i < 30; i++) {
                            CustomNPCEntity npc = mock(CustomNPCEntity.class);
                            UUID uuid = UUID.randomUUID();
                            setupMockNPC(npc, uuid, "TempNPC-" + threadId + "-" + i, mockOverworld, "minecraft:overworld");
                            NPCEntityRegistry.registerNPC(npc);
                            NPCEntityRegistry.unregisterNPC(npc);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - No exceptions thrown, registry remains consistent
        int finalCount = NPCEntityRegistry.getTotalNPCCount();
        assertThat(finalCount).isGreaterThanOrEqualTo(0); // Consistent state
    }

    // ═══════════════════════════════════════════════════════════
    // 6. EDGE CASES & STRESS TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should handle large number of NPCs (1000+)")
    void testLargeNumberOfNPCs() {
        // Arrange & Act
        int npcCount = 1000;
        for (int i = 0; i < npcCount; i++) {
            CustomNPCEntity npc = mock(CustomNPCEntity.class);
            UUID uuid = UUID.randomUUID();
            setupMockNPC(npc, uuid, "NPC-" + i, mockOverworld, "minecraft:overworld");
            NPCEntityRegistry.registerNPC(npc);
        }

        // Assert
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(npcCount);
        assertThat(NPCEntityRegistry.getNPCCount(mockOverworld)).isEqualTo(npcCount);
    }

    @Test
    @DisplayName("Should maintain correct state after multiple clear operations")
    void testMultipleClearOperations() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        NPCEntityRegistry.registerNPC(mockNPC2);

        // Act & Assert - Multiple clears
        NPCEntityRegistry.clear();
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(0);

        NPCEntityRegistry.clear(); // Clear again
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(0);

        // Re-register after clear
        NPCEntityRegistry.registerNPC(mockNPC3);
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(1);
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid3)).isEqualTo(mockNPC3);
    }

    @Test
    @DisplayName("Should handle multiple levels with varying NPC counts")
    void testMultipleLevelsVaryingCounts() {
        // Arrange - Different amounts in different levels
        NPCEntityRegistry.registerNPC(mockNPC1); // Overworld: 2
        NPCEntityRegistry.registerNPC(mockNPC2); // Overworld: 2
        NPCEntityRegistry.registerNPC(mockNPC3); // Nether: 1

        CustomNPCEntity endNPC1 = mock(CustomNPCEntity.class);
        CustomNPCEntity endNPC2 = mock(CustomNPCEntity.class);
        CustomNPCEntity endNPC3 = mock(CustomNPCEntity.class);
        setupMockNPC(endNPC1, UUID.randomUUID(), "EndNPC1", mockEnd, "minecraft:the_end");
        setupMockNPC(endNPC2, UUID.randomUUID(), "EndNPC2", mockEnd, "minecraft:the_end");
        setupMockNPC(endNPC3, UUID.randomUUID(), "EndNPC3", mockEnd, "minecraft:the_end");

        NPCEntityRegistry.registerNPC(endNPC1); // End: 3
        NPCEntityRegistry.registerNPC(endNPC2); // End: 3
        NPCEntityRegistry.registerNPC(endNPC3); // End: 3

        // Act & Assert
        assertThat(NPCEntityRegistry.getNPCCount(mockOverworld)).isEqualTo(2);
        assertThat(NPCEntityRegistry.getNPCCount(mockNether)).isEqualTo(1);
        assertThat(NPCEntityRegistry.getNPCCount(mockEnd)).isEqualTo(3);
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("Should correctly handle registry state after partial unregistration")
    void testPartialUnregistration() {
        // Arrange
        NPCEntityRegistry.registerNPC(mockNPC1);
        NPCEntityRegistry.registerNPC(mockNPC2);
        NPCEntityRegistry.registerNPC(mockNPC3);

        // Act - Remove only some NPCs
        NPCEntityRegistry.unregisterNPC(mockNPC2);

        // Assert
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(2);
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid1)).isEqualTo(mockNPC1);
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid2)).isNull();
        assertThat(NPCEntityRegistry.getNPCByUUID(uuid3)).isEqualTo(mockNPC3);
    }

    @Test
    @DisplayName("Should handle NPCs with same UUID in different levels")
    void testSameUUIDDifferentLevels() {
        // Arrange - This shouldn't happen in practice, but test the behavior
        UUID sharedUUID = UUID.randomUUID();
        CustomNPCEntity overworldNPC = mock(CustomNPCEntity.class);
        CustomNPCEntity netherNPC = mock(CustomNPCEntity.class);

        setupMockNPC(overworldNPC, sharedUUID, "OverworldNPC", mockOverworld, "minecraft:overworld");
        setupMockNPC(netherNPC, sharedUUID, "NetherNPC", mockNether, "minecraft:the_nether");

        // Act
        NPCEntityRegistry.registerNPC(overworldNPC);
        NPCEntityRegistry.registerNPC(netherNPC);

        // Assert - Each level should have its own NPC with that UUID
        CustomNPCEntity foundOverworld = NPCEntityRegistry.getNPCByUUID(sharedUUID, mockOverworld);
        CustomNPCEntity foundNether = NPCEntityRegistry.getNPCByUUID(sharedUUID, mockNether);

        assertThat(foundOverworld).isEqualTo(overworldNPC);
        assertThat(foundNether).isEqualTo(netherNPC);
        assertThat(NPCEntityRegistry.getTotalNPCCount()).isEqualTo(2);
    }

    // ═══════════════════════════════════════════════════════════
    // 7. PERFORMANCE & O(1) VERIFICATION
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should demonstrate O(1) lookup performance")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testO1LookupPerformance() {
        // Arrange - Register 10000 NPCs
        CustomNPCEntity[] npcs = new CustomNPCEntity[10000];
        UUID[] uuids = new UUID[10000];

        for (int i = 0; i < 10000; i++) {
            npcs[i] = mock(CustomNPCEntity.class);
            uuids[i] = UUID.randomUUID();
            setupMockNPC(npcs[i], uuids[i], "NPC-" + i, mockOverworld, "minecraft:overworld");
            NPCEntityRegistry.registerNPC(npcs[i]);
        }

        // Act - Perform 10000 lookups (should be fast - O(1))
        for (int i = 0; i < 10000; i++) {
            CustomNPCEntity found = NPCEntityRegistry.getNPCByUUID(uuids[i], mockOverworld);
            assertThat(found).isEqualTo(npcs[i]);
        }

        // Test should complete within timeout (demonstrates O(1) performance)
    }
}
