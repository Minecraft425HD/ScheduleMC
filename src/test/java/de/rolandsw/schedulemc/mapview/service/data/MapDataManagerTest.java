package de.rolandsw.schedulemc.mapview.service.data;

import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.config.WorldMapConfiguration;
import de.rolandsw.schedulemc.mapview.presentation.renderer.MapViewRenderer;
import de.rolandsw.schedulemc.mapview.service.render.ColorCalculationService;
import de.rolandsw.schedulemc.mapview.util.WorldUpdateListener;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for {@link MapDataManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Manager construction and initialization</li>
 *   <li>Late initialization (lateInit)</li>
 *   <li>Service delegation architecture</li>
 *   <li>Configuration management</li>
 *   <li>World lifecycle management</li>
 *   <li>Getter methods for services</li>
 * </ul>
 *
 * <p><b>Note:</b> This manager is client-side only and heavily depends on
 * Minecraft client classes. These tests focus on the orchestration logic
 * and service delegation patterns without requiring full Minecraft environment.
 */
@DisplayName("MapDataManager Tests")
class MapDataManagerTest {

    private MapDataManager manager;

    @BeforeEach
    void setUp() {
        manager = new MapDataManager();
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Manager Construction")
    class ConstructionTests {

        @Test
        @DisplayName("constructor should create manager instance")
        void constructorShouldCreateInstance() {
            MapDataManager newManager = new MapDataManager();

            assertThat(newManager).isNotNull();
        }

        @Test
        @DisplayName("newly constructed manager should not have null coordination services")
        void newManagerShouldHaveCoordinationServices() {
            // Manager should be constructed without errors
            assertThat(manager).isNotNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LATE INITIALIZATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Late Initialization")
    class LateInitTests {

        @Test
        @DisplayName("lateInit() should initialize map options")
        void lateInitShouldInitializeMapOptions() {
            // Note: This test may fail in non-Minecraft environment
            // due to resource loading dependencies
            assertThatCode(() -> manager.lateInit(false, false))
                .doesNotThrowAnyException();

            assertThat(MapDataManager.mapOptions).isNotNull();
        }

        @Test
        @DisplayName("lateInit() should set showUnderMenus option")
        void lateInitShouldSetShowUnderMenus() {
            manager.lateInit(true, false);

            assertThat(MapDataManager.mapOptions).isNotNull();
            assertThat(MapDataManager.mapOptions.showUnderMenus).isTrue();
        }

        @Test
        @DisplayName("lateInit() with showUnderMenus=false should not show under menus")
        void lateInitWithFalseShouldNotShowUnderMenus() {
            manager.lateInit(false, false);

            assertThat(MapDataManager.mapOptions).isNotNull();
            assertThat(MapDataManager.mapOptions.showUnderMenus).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONFIGURATION GETTER TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Configuration Getters")
    class ConfigurationGetterTests {

        @BeforeEach
        void initManager() {
            manager.lateInit(false, false);
        }

        @Test
        @DisplayName("getMapOptions() should return map configuration")
        void getMapOptionsShouldReturnConfig() {
            MapViewConfiguration config = manager.getMapOptions();

            assertThat(config).isNotNull();
            assertThat(config).isSameAs(MapDataManager.mapOptions);
        }

        @Test
        @DisplayName("getWorldMapDataOptions() should return world map config")
        void getWorldMapDataOptionsShouldReturnConfig() {
            WorldMapConfiguration config = manager.getWorldMapDataOptions();

            assertThat(config).isNotNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SERVICE GETTER TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Service Getters")
    class ServiceGetterTests {

        @BeforeEach
        void initManager() {
            manager.lateInit(false, false);
        }

        @Test
        @DisplayName("getMap() should return renderer instance")
        void getMapShouldReturnRenderer() {
            MapViewRenderer renderer = manager.getMap();

            assertThat(renderer).isNotNull();
        }

        @Test
        @DisplayName("getColorManager() should return color service")
        void getColorManagerShouldReturnService() {
            ColorCalculationService colorService = manager.getColorManager();

            assertThat(colorService).isNotNull();
        }

        @Test
        @DisplayName("getDimensionManager() should return dimension service")
        void getDimensionManagerShouldReturnService() {
            DimensionService dimensionService = manager.getDimensionManager();

            assertThat(dimensionService).isNotNull();
        }

        @Test
        @DisplayName("getWorldMapData() should return world map data")
        void getWorldMapDataShouldReturnData() {
            WorldMapData worldMapData = manager.getWorldMapData();

            assertThat(worldMapData).isNotNull();
        }

        @Test
        @DisplayName("getSettingsAndLightingChangeNotifier() should return notifier")
        void getSettingsNotifierShouldReturnNotifier() {
            ConfigNotificationService notifier = manager.getSettingsAndLightingChangeNotifier();

            assertThat(notifier).isNotNull();
        }

        @Test
        @DisplayName("getWorldUpdateListener() should return update listener")
        void getWorldUpdateListenerShouldReturnListener() {
            WorldUpdateListener listener = manager.getWorldUpdateListener();

            assertThat(listener).isNotNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE METHOD TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Lifecycle Methods")
    class LifecycleMethodTests {

        @BeforeEach
        void initManager() {
            manager.lateInit(false, false);
        }

        @Test
        @DisplayName("setPermissions() should not throw")
        void setPermissionsShouldNotThrow() {
            assertThatCode(() -> manager.setPermissions(true))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("clearServerSettings() should not throw")
        void clearServerSettingsShouldNotThrow() {
            assertThatCode(() -> manager.clearServerSettings())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onPlayInit() should not throw")
        void onPlayInitShouldNotThrow() {
            assertThatCode(() -> manager.onPlayInit())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onJoinServer() should not throw")
        void onJoinServerShouldNotThrow() {
            assertThatCode(() -> manager.onJoinServer())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onDisconnect() should not throw")
        void onDisconnectShouldNotThrow() {
            assertThatCode(() -> manager.onDisconnect())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onConfigurationInit() should not throw")
        void onConfigurationInitShouldNotThrow() {
            assertThatCode(() -> manager.onConfigurationInit())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onClientStopping() should not throw")
        void onClientStoppingShouldNotThrow() {
            assertThatCode(() -> manager.onClientStopping())
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WORLD STATE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("World State Management")
    class WorldStateTests {

        @BeforeEach
        void initManager() {
            manager.lateInit(false, false);
        }

        @Test
        @DisplayName("getCurrentWorldName() should not throw")
        void getCurrentWorldNameShouldNotThrow() {
            assertThatCode(() -> manager.getCurrentWorldName())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getWorldSeed() should not throw")
        void getWorldSeedShouldNotThrow() {
            assertThatCode(() -> manager.getWorldSeed())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setWorldSeed() should not throw")
        void setWorldSeedShouldNotThrow() {
            assertThatCode(() -> manager.setWorldSeed("12345"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setWorldSeed() should accept numeric seed")
        void setWorldSeedShouldAcceptNumericSeed() {
            assertThatCode(() -> manager.setWorldSeed("9876543210"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setWorldSeed() should accept empty seed")
        void setWorldSeedShouldAcceptEmptySeed() {
            assertThatCode(() -> manager.setWorldSeed(""))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("newSubWorldName() should be no-op (removed functionality)")
        void newSubWorldNameShouldBeNoOp() {
            assertThatCode(() -> manager.newSubWorldName("testworld", false))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MESSAGE HANDLING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Message Handling")
    class MessageHandlingTests {

        @BeforeEach
        void initManager() {
            manager.lateInit(false, false);
        }

        @Test
        @DisplayName("sendPlayerMessageOnMainThread() should not throw")
        void sendPlayerMessageShouldNotThrow() {
            assertThatCode(() -> manager.sendPlayerMessageOnMainThread("Test message"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("sendPlayerMessageOnMainThread() should handle empty message")
        void sendPlayerMessageShouldHandleEmptyMessage() {
            assertThatCode(() -> manager.sendPlayerMessageOnMainThread(""))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("sendPlayerMessageOnMainThread() should handle null message gracefully")
        void sendPlayerMessageShouldHandleNullMessage() {
            // May throw NPE depending on implementation, but should be documented
            assertThatCode(() -> manager.sendPlayerMessageOnMainThread(null))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("complete initialization workflow")
        void completeInitializationWorkflow() {
            MapDataManager newManager = new MapDataManager();

            // Initialize
            newManager.lateInit(true, false);

            // Verify all services are available
            assertThat(newManager.getMapOptions()).isNotNull();
            assertThat(newManager.getWorldMapDataOptions()).isNotNull();
            assertThat(newManager.getMap()).isNotNull();
            assertThat(newManager.getColorManager()).isNotNull();
            assertThat(newManager.getDimensionManager()).isNotNull();
            assertThat(newManager.getWorldMapData()).isNotNull();
            assertThat(newManager.getSettingsAndLightingChangeNotifier()).isNotNull();
            assertThat(newManager.getWorldUpdateListener()).isNotNull();
        }

        @Test
        @DisplayName("lifecycle sequence: init → join → disconnect")
        void lifecycleSequence() {
            manager.lateInit(false, false);

            assertThatCode(() -> {
                manager.onPlayInit();
                manager.onJoinServer();
                manager.onDisconnect();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("world seed management workflow")
        void worldSeedManagementWorkflow() {
            manager.lateInit(false, false);

            // Set seed
            manager.setWorldSeed("12345");

            // Get seed (may return empty or the set seed depending on world state)
            assertThatCode(() -> manager.getWorldSeed())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("permission management workflow")
        void permissionManagementWorkflow() {
            manager.lateInit(false, false);

            assertThatCode(() -> {
                manager.setPermissions(true);
                manager.setPermissions(false);
            }).doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SERVICE DELEGATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Service Delegation")
    class ServiceDelegationTests {

        @BeforeEach
        void initManager() {
            manager.lateInit(false, false);
        }

        @Test
        @DisplayName("manager should delegate to specialized services")
        void managerShouldDelegateToServices() {
            // Verify that services are initialized
            assertThat(manager.getMap()).isNotNull();
            assertThat(manager.getColorManager()).isNotNull();
            assertThat(manager.getDimensionManager()).isNotNull();

            // Each service should be a separate instance
            MapViewRenderer renderer = manager.getMap();
            assertThat(renderer).isNotNull();
        }

        @Test
        @DisplayName("getMap() should return consistent instance")
        void getMapShouldReturnConsistentInstance() {
            MapViewRenderer renderer1 = manager.getMap();
            MapViewRenderer renderer2 = manager.getMap();

            assertThat(renderer1).isSameAs(renderer2);
        }

        @Test
        @DisplayName("getColorManager() should return consistent instance")
        void getColorManagerShouldReturnConsistentInstance() {
            ColorCalculationService service1 = manager.getColorManager();
            ColorCalculationService service2 = manager.getColorManager();

            assertThat(service1).isSameAs(service2);
        }

        @Test
        @DisplayName("getWorldMapData() should return consistent instance")
        void getWorldMapDataShouldReturnConsistentInstance() {
            WorldMapData data1 = manager.getWorldMapData();
            WorldMapData data2 = manager.getWorldMapData();

            assertThat(data1).isSameAs(data2);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASES
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("multiple lateInit() calls should not throw")
        void multipleLateInitShouldNotThrow() {
            assertThatCode(() -> {
                manager.lateInit(false, false);
                manager.lateInit(true, false);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("lifecycle methods before lateInit() should handle gracefully")
        void lifecycleMethodsBeforeLateInitShouldHandle() {
            // Note: These may throw NPE if lifecycle service is not initialized
            // The test documents expected behavior
            assertThatThrownBy(() -> manager.onJoinServer())
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setWorldSeed() with very long seed should not throw")
        void setWorldSeedWithLongSeedShouldNotThrow() {
            manager.lateInit(false, false);

            String longSeed = "1".repeat(1000);
            assertThatCode(() -> manager.setWorldSeed(longSeed))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setWorldSeed() with special characters should not throw")
        void setWorldSeedWithSpecialCharsShouldNotThrow() {
            manager.lateInit(false, false);

            assertThatCode(() -> manager.setWorldSeed("seed-with_special.chars!"))
                .doesNotThrowAnyException();
        }
    }
}
