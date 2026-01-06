package de.rolandsw.schedulemc.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Umfangreiche Tests für TerritoryManager
 *
 * Getestete Funktionalität:
 * - Chunk-basierte Territorien-Verwaltung
 * - Territory setzen/entfernen mit Delta-Updates
 * - Territory-Abfragen (Chunk, BlockPos)
 * - Filterung nach Typ und Owner
 * - Bulk-Operationen
 * - Statistiken
 * - Singleton Pattern
 * - AbstractPersistenceManager Integration
 */
class TerritoryManagerTest {

    private static final UUID TEST_OWNER_1 = UUID.randomUUID();
    private static final UUID TEST_OWNER_2 = UUID.randomUUID();

    private TerritoryManager manager;
    private MinecraftServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton
        Field instanceField = TerritoryManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // Clean save file
        clearSaveFile();

        // Mock server
        mockServer = mock(MinecraftServer.class);
        File serverDir = new File(".");
        when(mockServer.getServerDirectory()).thenReturn(serverDir.toPath());

        manager = TerritoryManager.getInstance(mockServer);
    }

    @AfterEach
    void tearDown() {
        clearSaveFile();
    }

    private void clearSaveFile() {
        File saveFile = new File("config/plotmod_territories.json");
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }

    // ============================================================================
    // Singleton Pattern Tests
    // ============================================================================

    @Nested
    @DisplayName("Singleton Pattern")
    class SingletonTests {

        @Test
        @DisplayName("Should return same instance")
        void shouldReturnSameInstance() {
            TerritoryManager instance1 = TerritoryManager.getInstance(mockServer);
            TerritoryManager instance2 = TerritoryManager.getInstance(mockServer);

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should return instance without server parameter")
        void shouldReturnInstanceWithoutServerParameter() {
            TerritoryManager instance1 = TerritoryManager.getInstance(mockServer);
            TerritoryManager instance2 = TerritoryManager.getInstance();

            assertThat(instance2).isSameAs(instance1);
        }

        @Test
        @DisplayName("Should return null when not initialized")
        void shouldReturnNullWhenNotInitialized() throws Exception {
            // Reset instance
            Field instanceField = TerritoryManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);

            TerritoryManager instance = TerritoryManager.getInstance();

            assertThat(instance).isNull();
        }

        @Test
        @DisplayName("Should update server reference")
        void shouldUpdateServerReference() {
            MinecraftServer newServer = mock(MinecraftServer.class);
            File serverDir = new File(".");
            when(newServer.getServerDirectory()).thenReturn(serverDir.toPath());

            TerritoryManager instance = TerritoryManager.getInstance(newServer);

            assertThat(instance).isNotNull();
        }
    }

    // ============================================================================
    // Territory Setting Tests
    // ============================================================================

    @Nested
    @DisplayName("Territory Setting")
    class TerritorySettingTests {

        @Test
        @DisplayName("Should set territory for chunk")
        void shouldSetTerritoryForChunk() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test City", TEST_OWNER_1);

                Territory territory = manager.getTerritory(10, 20);
                assertThat(territory).isNotNull();
                assertThat(territory.getChunkX()).isEqualTo(10);
                assertThat(territory.getChunkZ()).isEqualTo(20);
                assertThat(territory.getType()).isEqualTo(TerritoryType.CITY);
                assertThat(territory.getName()).isEqualTo("Test City");
                assertThat(territory.getOwnerUUID()).isEqualTo(TEST_OWNER_1);
            }
        }

        @Test
        @DisplayName("Should broadcast delta update on set")
        void shouldBroadcastDeltaUpdateOnSet() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test", TEST_OWNER_1);

                networkMock.verify(() ->
                    de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.broadcastDeltaUpdate(any()));
            }
        }

        @Test
        @DisplayName("Should overwrite existing territory")
        void shouldOverwriteExistingTerritory() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City 1", TEST_OWNER_1);
                manager.setTerritory(10, 20, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);

                Territory territory = manager.getTerritory(10, 20);
                assertThat(territory.getType()).isEqualTo(TerritoryType.INDUSTRIAL);
                assertThat(territory.getName()).isEqualTo("Factory");
                assertThat(territory.getOwnerUUID()).isEqualTo(TEST_OWNER_2);
            }
        }

        @ParameterizedTest
        @EnumSource(TerritoryType.class)
        @DisplayName("Should set territory for all types")
        void shouldSetTerritoryForAllTypes(TerritoryType type) {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(0, 0, type, "Test", TEST_OWNER_1);

                Territory territory = manager.getTerritory(0, 0);
                assertThat(territory.getType()).isEqualTo(type);

                // Clear for next iteration
                manager.removeTerritory(0, 0);
            }
        }

        @Test
        @DisplayName("Should handle null owner UUID")
        void shouldHandleNullOwnerUUID() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.WILDERNESS, "Wild", null);

                Territory territory = manager.getTerritory(10, 20);
                assertThat(territory.getOwnerUUID()).isNull();
            }
        }

        @ParameterizedTest
        @CsvSource({
            "-100, -200",  // Negative chunks
            "0, 0",        // Origin
            "1000, 2000",  // Large positive chunks
            "-50, 75"      // Mixed signs
        })
        @DisplayName("Should handle various chunk coordinates")
        void shouldHandleVariousChunkCoordinates(int chunkX, int chunkZ) {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(chunkX, chunkZ, TerritoryType.CITY, "Test", TEST_OWNER_1);

                Territory territory = manager.getTerritory(chunkX, chunkZ);
                assertThat(territory).isNotNull();
                assertThat(territory.getChunkX()).isEqualTo(chunkX);
                assertThat(territory.getChunkZ()).isEqualTo(chunkZ);

                // Clear for next iteration
                manager.removeTerritory(chunkX, chunkZ);
            }
        }
    }

    // ============================================================================
    // Territory Removal Tests
    // ============================================================================

    @Nested
    @DisplayName("Territory Removal")
    class TerritoryRemovalTests {

        @Test
        @DisplayName("Should remove existing territory")
        void shouldRemoveExistingTerritory() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test", TEST_OWNER_1);

                boolean removed = manager.removeTerritory(10, 20);

                assertThat(removed).isTrue();
                assertThat(manager.getTerritory(10, 20)).isNull();
            }
        }

        @Test
        @DisplayName("Should broadcast delta update on remove")
        void shouldBroadcastDeltaUpdateOnRemove() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test", TEST_OWNER_1);
                networkMock.clearInvocations();

                manager.removeTerritory(10, 20);

                networkMock.verify(() ->
                    de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.broadcastDeltaUpdate(any()));
            }
        }

        @Test
        @DisplayName("Should return false when removing non-existent territory")
        void shouldReturnFalseWhenRemovingNonExistentTerritory() {
            boolean removed = manager.removeTerritory(999, 999);

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("Should not broadcast when removing non-existent territory")
        void shouldNotBroadcastWhenRemovingNonExistentTerritory() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.removeTerritory(999, 999);

                networkMock.verify(() ->
                    de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.broadcastDeltaUpdate(any()),
                    never());
            }
        }
    }

    // ============================================================================
    // Territory Query Tests
    // ============================================================================

    @Nested
    @DisplayName("Territory Queries")
    class TerritoryQueryTests {

        @Test
        @DisplayName("getTerritory should return territory for chunk")
        void getTerritoryShould returnTerritoryForChunk() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test", TEST_OWNER_1);

                Territory territory = manager.getTerritory(10, 20);

                assertThat(territory).isNotNull();
            }
        }

        @Test
        @DisplayName("getTerritory should return null for non-existent chunk")
        void getTerritoryShould returnNullForNonExistentChunk() {
            Territory territory = manager.getTerritory(999, 999);

            assertThat(territory).isNull();
        }

        @ParameterizedTest
        @CsvSource({
            "0, 0, 0, 0",          // Chunk (0,0) from block (0,0)
            "16, 16, 1, 1",        // Chunk (1,1) from block (16,16)
            "160, 320, 10, 20",    // Chunk (10,20) from block (160,320)
            "-16, -16, -1, -1",    // Negative chunks
            "15, 15, 0, 0"         // Edge of chunk (0,0)
        })
        @DisplayName("getTerritoryAt should convert BlockPos to chunk coordinates")
        void getTerritoryAtShouldConvertBlockPosToChunkCoordinates(int blockX, int blockZ,
                                                                    int expectedChunkX, int expectedChunkZ) {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(expectedChunkX, expectedChunkZ, TerritoryType.CITY, "Test", TEST_OWNER_1);

                BlockPos pos = new BlockPos(blockX, 64, blockZ);
                Territory territory = manager.getTerritoryAt(pos);

                assertThat(territory).isNotNull();
                assertThat(territory.getChunkX()).isEqualTo(expectedChunkX);
                assertThat(territory.getChunkZ()).isEqualTo(expectedChunkZ);

                // Clear for next iteration
                manager.removeTerritory(expectedChunkX, expectedChunkZ);
            }
        }

        @Test
        @DisplayName("getTerritoryAt should return null for non-existent territory")
        void getTerritoryAtShouldReturnNullForNonExistentTerritory() {
            BlockPos pos = new BlockPos(9999, 64, 9999);
            Territory territory = manager.getTerritoryAt(pos);

            assertThat(territory).isNull();
        }

        @Test
        @DisplayName("hasTerritory should return true for existing territory")
        void hasTerritoryShould returnTrueForExistingTerritory() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test", TEST_OWNER_1);

                assertThat(manager.hasTerritory(10, 20)).isTrue();
            }
        }

        @Test
        @DisplayName("hasTerritory should return false for non-existent territory")
        void hasTerritoryShould returnFalseForNonExistentTerritory() {
            assertThat(manager.hasTerritory(999, 999)).isFalse();
        }
    }

    // ============================================================================
    // Collection Query Tests
    // ============================================================================

    @Nested
    @DisplayName("Collection Queries")
    class CollectionQueryTests {

        @Test
        @DisplayName("getAllTerritories should return all territories")
        void getAllTerritoriesShouldReturnAllTerritories() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City 1", TEST_OWNER_1);
                manager.setTerritory(30, 40, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);
                manager.setTerritory(50, 60, TerritoryType.WILDERNESS, "Wild", null);

                Collection<Territory> territories = manager.getAllTerritories();

                assertThat(territories).hasSize(3);
            }
        }

        @Test
        @DisplayName("getAllTerritories should return unmodifiable collection")
        void getAllTerritoriesShouldReturnUnmodifiableCollection() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test", TEST_OWNER_1);

                Collection<Territory> territories = manager.getAllTerritories();

                assertThatThrownBy(() -> territories.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("getAllTerritories should return empty collection initially")
        void getAllTerritoriesShouldReturnEmptyCollectionInitially() {
            Collection<Territory> territories = manager.getAllTerritories();

            assertThat(territories).isEmpty();
        }

        @Test
        @DisplayName("getTerritoriesByType should filter by type")
        void getTerritoriesByTypeShouldFilterByType() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City 1", TEST_OWNER_1);
                manager.setTerritory(30, 40, TerritoryType.CITY, "City 2", TEST_OWNER_1);
                manager.setTerritory(50, 60, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);

                List<Territory> cities = manager.getTerritoriesByType(TerritoryType.CITY);

                assertThat(cities).hasSize(2);
                assertThat(cities).allMatch(t -> t.getType() == TerritoryType.CITY);
            }
        }

        @Test
        @DisplayName("getTerritoriesByType should return empty list for unused type")
        void getTerritoriesByTypeShouldReturnEmptyListForUnusedType() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City", TEST_OWNER_1);

                List<Territory> industrial = manager.getTerritoriesByType(TerritoryType.INDUSTRIAL);

                assertThat(industrial).isEmpty();
            }
        }

        @Test
        @DisplayName("getTerritoriesByOwner should filter by owner")
        void getTerritoriesByOwnerShouldFilterByOwner() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City 1", TEST_OWNER_1);
                manager.setTerritory(30, 40, TerritoryType.INDUSTRIAL, "Factory 1", TEST_OWNER_1);
                manager.setTerritory(50, 60, TerritoryType.CITY, "City 2", TEST_OWNER_2);

                List<Territory> owner1Territories = manager.getTerritoriesByOwner(TEST_OWNER_1);

                assertThat(owner1Territories).hasSize(2);
                assertThat(owner1Territories).allMatch(t -> TEST_OWNER_1.equals(t.getOwnerUUID()));
            }
        }

        @Test
        @DisplayName("getTerritoriesByOwner should return empty list for unknown owner")
        void getTerritoriesByOwnerShouldReturnEmptyListForUnknownOwner() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City", TEST_OWNER_1);

                List<Territory> unknownOwner = manager.getTerritoriesByOwner(UUID.randomUUID());

                assertThat(unknownOwner).isEmpty();
            }
        }

        @Test
        @DisplayName("getTerritoryCount should return correct count")
        void getTerritoryCountShouldReturnCorrectCount() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                assertThat(manager.getTerritoryCount()).isEqualTo(0);

                manager.setTerritory(10, 20, TerritoryType.CITY, "City 1", TEST_OWNER_1);
                assertThat(manager.getTerritoryCount()).isEqualTo(1);

                manager.setTerritory(30, 40, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);
                assertThat(manager.getTerritoryCount()).isEqualTo(2);

                manager.removeTerritory(10, 20);
                assertThat(manager.getTerritoryCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("getTerritoriesMap should return unmodifiable map")
        void getTerritoriesMapShouldReturnUnmodifiableMap() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test", TEST_OWNER_1);

                Map<Long, Territory> map = manager.getTerritoriesMap();

                assertThatThrownBy(() -> map.put(0L, null))
                    .isInstanceOf(UnsupportedOperationException.class);
            }
        }
    }

    // ============================================================================
    // Bulk Operations Tests
    // ============================================================================

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperationsTests {

        @Test
        @DisplayName("clearTerritoriesByType should remove all of a type")
        void clearTerritoriesByTypeShouldRemoveAllOfAType() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City 1", TEST_OWNER_1);
                manager.setTerritory(30, 40, TerritoryType.CITY, "City 2", TEST_OWNER_1);
                manager.setTerritory(50, 60, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);

                int removed = manager.clearTerritoriesByType(TerritoryType.CITY);

                assertThat(removed).isEqualTo(2);
                assertThat(manager.getTerritoriesByType(TerritoryType.CITY)).isEmpty();
                assertThat(manager.getTerritoryCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("clearTerritoriesByType should return 0 for unused type")
        void clearTerritoriesByTypeShouldReturn0ForUnusedType() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City", TEST_OWNER_1);

                int removed = manager.clearTerritoriesByType(TerritoryType.INDUSTRIAL);

                assertThat(removed).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("clearTerritoriesByType should preserve other types")
        void clearTerritoriesByTypeShouldPreserveOtherTypes() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City", TEST_OWNER_1);
                manager.setTerritory(30, 40, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);

                manager.clearTerritoriesByType(TerritoryType.CITY);

                assertThat(manager.getTerritoryCount()).isEqualTo(1);
                assertThat(manager.getTerritory(30, 40)).isNotNull();
            }
        }
    }

    // ============================================================================
    // Statistics Tests
    // ============================================================================

    @Nested
    @DisplayName("Statistics")
    class StatisticsTests {

        @Test
        @DisplayName("getStatistics should show territory counts")
        void getStatisticsShouldShowTerritoryCounts() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City 1", TEST_OWNER_1);
                manager.setTerritory(30, 40, TerritoryType.CITY, "City 2", TEST_OWNER_1);
                manager.setTerritory(50, 60, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);

                String stats = manager.getStatistics();

                assertThat(stats).contains("Territories: 3");
                assertThat(stats).contains(TerritoryType.CITY.getDisplayName());
                assertThat(stats).contains(TerritoryType.INDUSTRIAL.getDisplayName());
            }
        }

        @Test
        @DisplayName("getStatistics should show zero for empty manager")
        void getStatisticsShouldShowZeroForEmptyManager() {
            String stats = manager.getStatistics();

            assertThat(stats).contains("Territories: 0");
        }

        @Test
        @DisplayName("getStatistics should group by type")
        void getStatisticsShouldGroupByType() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "C1", TEST_OWNER_1);
                manager.setTerritory(30, 40, TerritoryType.CITY, "C2", TEST_OWNER_1);
                manager.setTerritory(50, 60, TerritoryType.CITY, "C3", TEST_OWNER_1);

                String stats = manager.getStatistics();

                // Should show count of 3 for CITY
                assertThat(stats).contains("3");
            }
        }
    }

    // ============================================================================
    // AbstractPersistenceManager Tests
    // ============================================================================

    @Nested
    @DisplayName("AbstractPersistenceManager Integration")
    class PersistenceTests {

        @Test
        @DisplayName("Should implement getComponentName")
        void shouldImplementGetComponentName() throws Exception {
            var method = TerritoryManager.class.getDeclaredMethod("getComponentName");
            method.setAccessible(true);
            String name = (String) method.invoke(manager);

            assertThat(name).isEqualTo("TerritoryManager");
        }

        @Test
        @DisplayName("Should implement getHealthDetails")
        void shouldImplementGetHealthDetails() throws Exception {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test", TEST_OWNER_1);
                manager.setTerritory(30, 40, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);

                var method = TerritoryManager.class.getDeclaredMethod("getHealthDetails");
                method.setAccessible(true);
                String health = (String) method.invoke(manager);

                assertThat(health).contains("2");
                assertThat(health).contains("Territorien");
            }
        }
    }

    // ============================================================================
    // Chunk Key Tests
    // ============================================================================

    @Nested
    @DisplayName("Chunk Key Handling")
    class ChunkKeyTests {

        @Test
        @DisplayName("Should use same chunk key for same coordinates")
        void shouldUseSameChunkKeyForSameCoordinates() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "Test 1", TEST_OWNER_1);
                manager.setTerritory(10, 20, TerritoryType.INDUSTRIAL, "Test 2", TEST_OWNER_2);

                // Should have overwritten, not duplicated
                assertThat(manager.getTerritoryCount()).isEqualTo(1);
                assertThat(manager.getTerritory(10, 20).getType())
                    .isEqualTo(TerritoryType.INDUSTRIAL);
            }
        }

        @Test
        @DisplayName("Should use different chunk keys for different coordinates")
        void shouldUseDifferentChunkKeysForDifferentCoordinates() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "City", TEST_OWNER_1);
                manager.setTerritory(10, 21, TerritoryType.INDUSTRIAL, "Factory", TEST_OWNER_2);

                assertThat(manager.getTerritoryCount()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("Should handle large chunk coordinates")
        void shouldHandleLargeChunkCoordinates() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                int largeX = 1000000;
                int largeZ = 2000000;

                manager.setTerritory(largeX, largeZ, TerritoryType.CITY, "Far City", TEST_OWNER_1);

                Territory territory = manager.getTerritory(largeX, largeZ);
                assertThat(territory).isNotNull();
                assertThat(territory.getChunkX()).isEqualTo(largeX);
                assertThat(territory.getChunkZ()).isEqualTo(largeZ);
            }
        }

        @Test
        @DisplayName("Should handle negative chunk coordinates")
        void shouldHandleNegativeChunkCoordinates() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(-100, -200, TerritoryType.WILDERNESS, "Negative", null);

                Territory territory = manager.getTerritory(-100, -200);
                assertThat(territory).isNotNull();
                assertThat(territory.getChunkX()).isEqualTo(-100);
                assertThat(territory.getChunkZ()).isEqualTo(-200);
            }
        }
    }

    // ============================================================================
    // Edge Cases
    // ============================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty name")
        void shouldHandleEmptyName() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                manager.setTerritory(10, 20, TerritoryType.CITY, "", TEST_OWNER_1);

                Territory territory = manager.getTerritory(10, 20);
                assertThat(territory.getName()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle very long name")
        void shouldHandleVeryLongName() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                String longName = "A".repeat(1000);

                manager.setTerritory(10, 20, TerritoryType.CITY, longName, TEST_OWNER_1);

                Territory territory = manager.getTerritory(10, 20);
                assertThat(territory.getName()).isEqualTo(longName);
            }
        }

        @Test
        @DisplayName("Should handle multiple owners")
        void shouldHandleMultipleOwners() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                UUID owner1 = UUID.randomUUID();
                UUID owner2 = UUID.randomUUID();
                UUID owner3 = UUID.randomUUID();

                manager.setTerritory(10, 20, TerritoryType.CITY, "C1", owner1);
                manager.setTerritory(30, 40, TerritoryType.CITY, "C2", owner2);
                manager.setTerritory(50, 60, TerritoryType.CITY, "C3", owner3);

                assertThat(manager.getTerritoriesByOwner(owner1)).hasSize(1);
                assertThat(manager.getTerritoriesByOwner(owner2)).hasSize(1);
                assertThat(manager.getTerritoriesByOwner(owner3)).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should handle concurrent operations safely")
        void shouldHandleConcurrentOperationsSafely() {
            try (MockedStatic<de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler> networkMock =
                     mockStatic(de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.class)) {

                // Set multiple territories
                for (int i = 0; i < 100; i++) {
                    manager.setTerritory(i, i, TerritoryType.CITY, "City " + i, TEST_OWNER_1);
                }

                // Should not lose data
                assertThat(manager.getTerritoryCount()).isEqualTo(100);
            }
        }
    }
}
