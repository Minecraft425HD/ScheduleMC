package de.rolandsw.schedulemc.warehouse;

import de.rolandsw.schedulemc.util.ConfigCache;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for WarehouseManager
 *
 * Tests cover:
 * - Warehouse Registration/Unregistration
 * - Persistence (Load/Save with NBT)
 * - Multi-level Support
 * - Cache Management
 * - Thread Safety
 * - Edge Cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseManager Tests")
class WarehouseManagerTest {

    @Mock
    private MinecraftServer mockServer;

    @Mock
    private ServerLevel mockOverworld;

    @Mock
    private ServerLevel mockNether;

    @Mock
    private WarehouseBlockEntity mockWarehouse;

    @TempDir
    Path tempDir;

    private File dataDir;
    private File dataFile;

    @BeforeEach
    void setUp() throws Exception {
        // Setup temp directory structure
        dataDir = tempDir.resolve("data").toFile();
        dataDir.mkdirs();
        dataFile = new File(dataDir, "schedulemc_warehouses.dat");

        // Mock server paths
        when(mockServer.getWorldPath(any(LevelResource.class))).thenReturn(tempDir);

        // Mock overworld level
        ResourceKey<Level> overworldKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation("minecraft", "overworld")
        );
        when(mockOverworld.dimension()).thenReturn(overworldKey);
        when(mockOverworld.isLoaded(any(BlockPos.class))).thenReturn(true);
        when(mockOverworld.getDayTime()).thenReturn(0L);

        // Mock nether level
        ResourceKey<Level> netherKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation("minecraft", "the_nether")
        );
        when(mockNether.dimension()).thenReturn(netherKey);
        when(mockNether.isLoaded(any(BlockPos.class))).thenReturn(true);
        when(mockNether.getDayTime()).thenReturn(0L);

        when(mockServer.getAllLevels()).thenReturn(Arrays.asList(mockOverworld, mockNether));
        when(mockServer.getPlayerCount()).thenReturn(1); // At least one player online

        // Clear static state before each test
        clearWarehouseManagerState();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up static state after each test
        clearWarehouseManagerState();
    }

    /**
     * Uses reflection to reset WarehouseManager static state between tests
     */
    private void clearWarehouseManagerState() throws Exception {
        Field warehousesField = WarehouseManager.class.getDeclaredField("warehouses");
        warehousesField.setAccessible(true);
        Map<String, Set<BlockPos>> warehouses = (Map<String, Set<BlockPos>>) warehousesField.get(null);
        warehouses.clear();

        Field cacheField = WarehouseManager.class.getDeclaredField("lastDeliveryDayCache");
        cacheField.setAccessible(true);
        Map<BlockPos, Long> cache = (Map<BlockPos, Long>) cacheField.get(null);
        cache.clear();

        Field dirtyField = WarehouseManager.class.getDeclaredField("dirty");
        dirtyField.setAccessible(true);
        dirtyField.setBoolean(null, false);

        Field tickCounterField = WarehouseManager.class.getDeclaredField("tickCounter");
        tickCounterField.setAccessible(true);
        tickCounterField.setInt(null, 0);
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register warehouse successfully")
        void testRegisterWarehouse() {
            BlockPos pos = new BlockPos(100, 64, 200);

            WarehouseManager.registerWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses).containsKey("minecraft:overworld");
            assertThat(allWarehouses.get("minecraft:overworld")).contains(pos);
        }

        @Test
        @DisplayName("Should register multiple warehouses in same level")
        void testRegisterMultipleWarehousesInSameLevel() {
            BlockPos pos1 = new BlockPos(100, 64, 200);
            BlockPos pos2 = new BlockPos(200, 64, 300);
            BlockPos pos3 = new BlockPos(300, 64, 400);

            WarehouseManager.registerWarehouse(mockOverworld, pos1);
            WarehouseManager.registerWarehouse(mockOverworld, pos2);
            WarehouseManager.registerWarehouse(mockOverworld, pos3);

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses.get("minecraft:overworld"))
                .hasSize(3)
                .contains(pos1, pos2, pos3);
        }

        @Test
        @DisplayName("Should register warehouses in multiple levels")
        void testRegisterWarehousesInMultipleLevels() {
            BlockPos overworldPos = new BlockPos(100, 64, 200);
            BlockPos netherPos = new BlockPos(12, 64, 25);

            WarehouseManager.registerWarehouse(mockOverworld, overworldPos);
            WarehouseManager.registerWarehouse(mockNether, netherPos);

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses)
                .hasSize(2)
                .containsKeys("minecraft:overworld", "minecraft:the_nether");

            assertThat(allWarehouses.get("minecraft:overworld")).contains(overworldPos);
            assertThat(allWarehouses.get("minecraft:the_nether")).contains(netherPos);
        }

        @Test
        @DisplayName("Should handle duplicate registration gracefully")
        void testRegisterDuplicateWarehouse() {
            BlockPos pos = new BlockPos(100, 64, 200);

            WarehouseManager.registerWarehouse(mockOverworld, pos);
            WarehouseManager.registerWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            // Set should only contain one instance
            assertThat(allWarehouses.get("minecraft:overworld")).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UNREGISTRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Unregistration Tests")
    class UnregistrationTests {

        @Test
        @DisplayName("Should unregister warehouse successfully")
        void testUnregisterWarehouse() {
            BlockPos pos = new BlockPos(100, 64, 200);

            WarehouseManager.registerWarehouse(mockOverworld, pos);
            WarehouseManager.unregisterWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses).doesNotContainKey("minecraft:overworld");
        }

        @Test
        @DisplayName("Should unregister specific warehouse without affecting others")
        void testUnregisterSpecificWarehouse() {
            BlockPos pos1 = new BlockPos(100, 64, 200);
            BlockPos pos2 = new BlockPos(200, 64, 300);

            WarehouseManager.registerWarehouse(mockOverworld, pos1);
            WarehouseManager.registerWarehouse(mockOverworld, pos2);

            WarehouseManager.unregisterWarehouse(mockOverworld, pos1);

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses.get("minecraft:overworld"))
                .hasSize(1)
                .contains(pos2)
                .doesNotContain(pos1);
        }

        @Test
        @DisplayName("Should remove level entry when last warehouse is unregistered")
        void testUnregisterLastWarehouse() {
            BlockPos pos = new BlockPos(100, 64, 200);

            WarehouseManager.registerWarehouse(mockOverworld, pos);
            WarehouseManager.unregisterWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses).doesNotContainKey("minecraft:overworld");
        }

        @Test
        @DisplayName("Should handle unregistering non-existent warehouse gracefully")
        void testUnregisterNonExistentWarehouse() {
            BlockPos pos = new BlockPos(100, 64, 200);

            // Should not throw exception
            assertThatCode(() -> WarehouseManager.unregisterWarehouse(mockOverworld, pos))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should clear cache when unregistering warehouse")
        void testUnregisterClearCache() throws Exception {
            BlockPos pos = new BlockPos(100, 64, 200);

            WarehouseManager.registerWarehouse(mockOverworld, pos);

            // Manually add to cache
            Field cacheField = WarehouseManager.class.getDeclaredField("lastDeliveryDayCache");
            cacheField.setAccessible(true);
            Map<BlockPos, Long> cache = (Map<BlockPos, Long>) cacheField.get(null);
            cache.put(pos, 5L);

            WarehouseManager.unregisterWarehouse(mockOverworld, pos);

            // Cache should be cleared
            assertThat(cache).doesNotContainKey(pos);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Persistence Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Should save warehouse data to file")
        void testSaveWarehouseData() {
            BlockPos pos1 = new BlockPos(100, 64, 200);
            BlockPos pos2 = new BlockPos(200, 64, 300);

            WarehouseManager.registerWarehouse(mockOverworld, pos1);
            WarehouseManager.registerWarehouse(mockNether, pos2);

            WarehouseManager.save(mockServer);

            assertThat(dataFile).exists();
        }

        @Test
        @DisplayName("Should load warehouse data from file")
        void testLoadWarehouseData() {
            // Setup: Save some data first
            BlockPos pos1 = new BlockPos(100, 64, 200);
            BlockPos pos2 = new BlockPos(200, 64, 300);

            WarehouseManager.registerWarehouse(mockOverworld, pos1);
            WarehouseManager.registerWarehouse(mockNether, pos2);
            WarehouseManager.save(mockServer);

            // Clear state
            try {
                clearWarehouseManagerState();
            } catch (Exception e) {
                fail("Failed to clear state");
            }

            // Test: Load data
            WarehouseManager.load(mockServer);

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses).hasSize(2);
            assertThat(allWarehouses.get("minecraft:overworld")).contains(pos1);
            assertThat(allWarehouses.get("minecraft:the_nether")).contains(pos2);
        }

        @Test
        @DisplayName("Should handle load when file does not exist")
        void testLoadNonExistentFile() {
            // Ensure file doesn't exist
            if (dataFile.exists()) {
                dataFile.delete();
            }

            // Should not throw exception
            assertThatCode(() -> WarehouseManager.load(mockServer))
                .doesNotThrowAnyException();

            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses).isEmpty();
        }

        @Test
        @DisplayName("Should skip save when not dirty")
        void testSaveWhenNotDirty() throws Exception {
            // Ensure clean state
            Field dirtyField = WarehouseManager.class.getDeclaredField("dirty");
            dirtyField.setAccessible(true);
            dirtyField.setBoolean(null, false);

            long lastModified = dataFile.exists() ? dataFile.lastModified() : 0;

            WarehouseManager.save(mockServer);

            // File should not be created/modified
            if (lastModified == 0) {
                assertThat(dataFile).doesNotExist();
            } else {
                assertThat(dataFile.lastModified()).isEqualTo(lastModified);
            }
        }

        @Test
        @DisplayName("Should set dirty flag on registration")
        void testDirtyFlagOnRegistration() throws Exception {
            Field dirtyField = WarehouseManager.class.getDeclaredField("dirty");
            dirtyField.setAccessible(true);
            dirtyField.setBoolean(null, false);

            BlockPos pos = new BlockPos(100, 64, 200);
            WarehouseManager.registerWarehouse(mockOverworld, pos);

            boolean isDirty = dirtyField.getBoolean(null);
            assertThat(isDirty).isTrue();
        }

        @Test
        @DisplayName("Should clear dirty flag after successful save")
        void testClearDirtyFlagAfterSave() throws Exception {
            BlockPos pos = new BlockPos(100, 64, 200);
            WarehouseManager.registerWarehouse(mockOverworld, pos);

            WarehouseManager.save(mockServer);

            Field dirtyField = WarehouseManager.class.getDeclaredField("dirty");
            dirtyField.setAccessible(true);
            boolean isDirty = dirtyField.getBoolean(null);

            assertThat(isDirty).isFalse();
        }

        @Test
        @DisplayName("Should preserve all data through save/load cycle")
        void testSaveLoadCycle() throws Exception {
            // Create diverse warehouse setup
            BlockPos pos1 = new BlockPos(100, 64, 200);
            BlockPos pos2 = new BlockPos(200, 64, 300);
            BlockPos pos3 = new BlockPos(300, 64, 400);
            BlockPos netherPos = new BlockPos(12, 64, 25);

            WarehouseManager.registerWarehouse(mockOverworld, pos1);
            WarehouseManager.registerWarehouse(mockOverworld, pos2);
            WarehouseManager.registerWarehouse(mockOverworld, pos3);
            WarehouseManager.registerWarehouse(mockNether, netherPos);

            // Save
            WarehouseManager.save(mockServer);

            // Clear
            clearWarehouseManagerState();

            // Load
            WarehouseManager.load(mockServer);

            // Verify all data preserved
            Map<String, Set<BlockPos>> allWarehouses = WarehouseManager.getAllWarehouses();
            assertThat(allWarehouses).hasSize(2);
            assertThat(allWarehouses.get("minecraft:overworld"))
                .hasSize(3)
                .contains(pos1, pos2, pos3);
            assertThat(allWarehouses.get("minecraft:the_nether"))
                .hasSize(1)
                .contains(netherPos);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEFENSIVE COPY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Defensive Copy Tests")
    class DefensiveCopyTests {

        @Test
        @DisplayName("getAllWarehouses should return defensive copy")
        void testGetAllWarehousesDefensiveCopy() {
            BlockPos pos = new BlockPos(100, 64, 200);
            WarehouseManager.registerWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> warehouses1 = WarehouseManager.getAllWarehouses();
            Map<String, Set<BlockPos>> warehouses2 = WarehouseManager.getAllWarehouses();

            // Different instances
            assertThat(warehouses1).isNotSameAs(warehouses2);

            // Same content
            assertThat(warehouses1).containsExactlyInAnyOrderEntriesOf(warehouses2);
        }

        @Test
        @DisplayName("Modifying returned map should not affect internal state")
        void testModifyReturnedMap() {
            BlockPos pos = new BlockPos(100, 64, 200);
            WarehouseManager.registerWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();

            // Try to modify - should throw UnsupportedOperationException
            assertThatThrownBy(() -> warehouses.clear())
                .isInstanceOf(UnsupportedOperationException.class);

            // Internal state should remain unchanged
            Map<String, Set<BlockPos>> actual = WarehouseManager.getAllWarehouses();
            assertThat(actual.get("minecraft:overworld")).contains(pos);
        }

        @Test
        @DisplayName("Modifying returned set should not affect internal state")
        void testModifyReturnedSet() {
            BlockPos pos1 = new BlockPos(100, 64, 200);
            BlockPos pos2 = new BlockPos(200, 64, 300);

            WarehouseManager.registerWarehouse(mockOverworld, pos1);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            Set<BlockPos> positions = warehouses.get("minecraft:overworld");

            // Modify the returned set
            positions.add(pos2);

            // Internal state should not be affected
            Map<String, Set<BlockPos>> actual = WarehouseManager.getAllWarehouses();
            assertThat(actual.get("minecraft:overworld"))
                .hasSize(1)
                .contains(pos1)
                .doesNotContain(pos2);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // THREAD SAFETY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent registrations safely")
        @Timeout(10)
        void testConcurrentRegistrations() throws InterruptedException {
            int numThreads = 10;
            int registrationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);
            Set<BlockPos> allPositions = ConcurrentHashMap.newKeySet();

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < registrationsPerThread; j++) {
                            BlockPos pos = new BlockPos(
                                ThreadLocalRandom.current().nextInt(1000),
                                64,
                                ThreadLocalRandom.current().nextInt(1000)
                            );
                            allPositions.add(pos);
                            WarehouseManager.registerWarehouse(mockOverworld, pos);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses.get("minecraft:overworld"))
                .containsAll(allPositions);
        }

        @Test
        @DisplayName("Should handle concurrent reg/unreg operations safely")
        @Timeout(10)
        void testConcurrentRegistrationAndUnregistration() throws InterruptedException {
            int numThreads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);

            BlockPos[] positions = new BlockPos[100];
            for (int i = 0; i < 100; i++) {
                positions[i] = new BlockPos(i * 10, 64, i * 10);
            }

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 50; j++) {
                            BlockPos pos = positions[ThreadLocalRandom.current().nextInt(100)];
                            if (threadId % 2 == 0) {
                                WarehouseManager.registerWarehouse(mockOverworld, pos);
                            } else {
                                WarehouseManager.unregisterWarehouse(mockOverworld, pos);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // Should complete without exceptions
            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses).isNotNull();
        }

        @Test
        @DisplayName("Should handle concurrent multi-level registrations safely")
        @Timeout(10)
        void testConcurrentMultiLevelRegistrations() throws InterruptedException {
            int numThreads = 10;
            int registrationsPerThread = 50;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < registrationsPerThread; j++) {
                            BlockPos pos = new BlockPos(
                                ThreadLocalRandom.current().nextInt(1000),
                                64,
                                ThreadLocalRandom.current().nextInt(1000)
                            );
                            ServerLevel level = ThreadLocalRandom.current().nextBoolean() ?
                                mockOverworld : mockNether;
                            WarehouseManager.registerWarehouse(level, pos);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses).isNotEmpty();

            int totalWarehouses = warehouses.values().stream()
                .mapToInt(Set::size)
                .sum();
            assertThat(totalWarehouses).isGreaterThan(0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASES TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle negative coordinates")
        void testNegativeCoordinates() {
            BlockPos pos = new BlockPos(-100, 64, -200);

            WarehouseManager.registerWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses.get("minecraft:overworld")).contains(pos);
        }

        @Test
        @DisplayName("Should handle extreme coordinates")
        void testExtremeCoordinates() {
            BlockPos pos = new BlockPos(30_000_000, 255, 30_000_000);

            WarehouseManager.registerWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses.get("minecraft:overworld")).contains(pos);
        }

        @Test
        @DisplayName("Should handle BlockPos at origin")
        void testOriginPosition() {
            BlockPos pos = BlockPos.ZERO;

            WarehouseManager.registerWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses.get("minecraft:overworld")).contains(pos);
        }

        @Test
        @DisplayName("Should handle large number of warehouses")
        void testLargeNumberOfWarehouses() {
            int numWarehouses = 10000;

            for (int i = 0; i < numWarehouses; i++) {
                BlockPos pos = new BlockPos(i, 64, i);
                WarehouseManager.registerWarehouse(mockOverworld, pos);
            }

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses.get("minecraft:overworld")).hasSize(numWarehouses);
        }

        @Test
        @DisplayName("Should getAllWarehouses return empty map when no warehouses registered")
        void testGetAllWarehousesEmpty() {
            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();

            assertThat(warehouses).isEmpty();
        }

        @Test
        @DisplayName("Should handle register then immediate unregister")
        void testRegisterThenImmediateUnregister() {
            BlockPos pos = new BlockPos(100, 64, 200);

            WarehouseManager.registerWarehouse(mockOverworld, pos);
            WarehouseManager.unregisterWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses).doesNotContainKey("minecraft:overworld");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LEVEL KEY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Level Key Tests")
    class LevelKeyTests {

        @Test
        @DisplayName("Should generate correct level key for overworld")
        void testOverworldLevelKey() {
            BlockPos pos = new BlockPos(100, 64, 200);
            WarehouseManager.registerWarehouse(mockOverworld, pos);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses).containsKey("minecraft:overworld");
        }

        @Test
        @DisplayName("Should generate correct level key for nether")
        void testNetherLevelKey() {
            BlockPos pos = new BlockPos(12, 64, 25);
            WarehouseManager.registerWarehouse(mockNether, pos);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses).containsKey("minecraft:the_nether");
        }

        @Test
        @DisplayName("Should keep warehouses separate per dimension")
        void testSeparatePerDimension() {
            BlockPos overworldPos = new BlockPos(100, 64, 200);
            BlockPos netherPos = new BlockPos(100, 64, 200); // Same coordinates, different dimension

            WarehouseManager.registerWarehouse(mockOverworld, overworldPos);
            WarehouseManager.registerWarehouse(mockNether, netherPos);

            Map<String, Set<BlockPos>> warehouses = WarehouseManager.getAllWarehouses();
            assertThat(warehouses).hasSize(2);
            assertThat(warehouses.get("minecraft:overworld")).contains(overworldPos);
            assertThat(warehouses.get("minecraft:the_nether")).contains(netherPos);
        }
    }
}
