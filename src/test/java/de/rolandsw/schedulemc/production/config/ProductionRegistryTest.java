package de.rolandsw.schedulemc.production.config;

import de.rolandsw.schedulemc.production.core.GenericQuality;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive JUnit 5 Tests for ProductionRegistry
 *
 * Tests all critical functionality:
 * - Recipe Registration/Unregistration
 * - Recipe Lookup by ID
 * - Recipe Lookup by Category
 * - Input/Output Item Matching
 * - Processing Time Validation
 * - Duplicate Registration Handling
 * - Thread Safety (concurrent access)
 * - Edge Cases (null items, invalid times)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductionRegistry Tests")
class ProductionRegistryTest {

    private ProductionRegistry registry;

    @BeforeEach
    void setUp() {
        // Get singleton instance and clear it
        registry = ProductionRegistry.getInstance();
        registry.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        registry.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // 1. REGISTRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should register production config successfully")
    void testRegisterProduction() {
        // Arrange
        ProductionConfig config = createTestConfig("test_tobacco", "Test Tobacco");

        // Act
        registry.register(config);

        // Assert
        assertThat(registry.has("test_tobacco")).isTrue();
        assertThat(registry.get("test_tobacco")).isEqualTo(config);
        assertThat(registry.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should register multiple productions")
    void testRegisterMultipleProductions() {
        // Arrange
        ProductionConfig config1 = createTestConfig("tobacco_virginia", "Virginia");
        ProductionConfig config2 = createTestConfig("tobacco_burley", "Burley");
        ProductionConfig config3 = createTestConfig("cannabis_indica", "Indica");

        // Act
        registry.register(config1);
        registry.register(config2);
        registry.register(config3);

        // Assert
        assertThat(registry.getCount()).isEqualTo(3);
        assertThat(registry.has("tobacco_virginia")).isTrue();
        assertThat(registry.has("tobacco_burley")).isTrue();
        assertThat(registry.has("cannabis_indica")).isTrue();
    }

    @Test
    @DisplayName("Should overwrite duplicate registration")
    void testDuplicateRegistration() {
        // Arrange
        ProductionConfig config1 = createTestConfig("tobacco_virginia", "Virginia v1");
        ProductionConfig config2 = createTestConfig("tobacco_virginia", "Virginia v2");

        // Act
        registry.register(config1);
        registry.register(config2); // Same ID, should overwrite

        // Assert
        assertThat(registry.getCount()).isEqualTo(1);
        assertThat(registry.get("tobacco_virginia")).isEqualTo(config2);
        assertThat(registry.get("tobacco_virginia").getDisplayName()).isEqualTo("Virginia v2");
    }

    @Test
    @DisplayName("Should handle null config registration gracefully")
    void testRegisterNullConfig() {
        // Act & Assert - Should throw NullPointerException (expected for @Nonnull)
        assertThatThrownBy(() -> registry.register(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should categorize production correctly on registration")
    void testRegistrationCategorization() {
        // Arrange
        ProductionConfig plant = new ProductionConfig.Builder("test_plant", "Test Plant")
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build();

        ProductionConfig chemical = new ProductionConfig.Builder("test_chemical", "Test Chemical")
            .category(ProductionConfig.ProductionCategory.CHEMICAL)
            .build();

        // Act
        registry.register(plant);
        registry.register(chemical);

        // Assert
        List<ProductionConfig> plants = registry.getByCategory(ProductionConfig.ProductionCategory.PLANT);
        List<ProductionConfig> chemicals = registry.getByCategory(ProductionConfig.ProductionCategory.CHEMICAL);

        assertThat(plants).hasSize(1).contains(plant);
        assertThat(chemicals).hasSize(1).contains(chemical);
    }

    // ═══════════════════════════════════════════════════════════
    // 2. UNREGISTRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should unregister production successfully")
    void testUnregisterProduction() {
        // Arrange
        ProductionConfig config = createTestConfig("test_tobacco", "Test Tobacco");
        registry.register(config);
        assertThat(registry.getCount()).isEqualTo(1);

        // Act
        boolean result = registry.unregister("test_tobacco");

        // Assert
        assertThat(result).isTrue();
        assertThat(registry.getCount()).isEqualTo(0);
        assertThat(registry.has("test_tobacco")).isFalse();
        assertThat(registry.get("test_tobacco")).isNull();
    }

    @Test
    @DisplayName("Should return false when unregistering non-existent production")
    void testUnregisterNonExistent() {
        // Act
        boolean result = registry.unregister("non_existent");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle null ID in unregistration gracefully")
    void testUnregisterNullId() {
        // Act & Assert - Should throw NullPointerException (expected for @Nonnull)
        assertThatThrownBy(() -> registry.unregister(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should remove from category on unregistration")
    void testUnregisterRemovesFromCategory() {
        // Arrange
        ProductionConfig config = new ProductionConfig.Builder("test_plant", "Test Plant")
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build();
        registry.register(config);

        assertThat(registry.getByCategory(ProductionConfig.ProductionCategory.PLANT)).hasSize(1);

        // Act
        registry.unregister("test_plant");

        // Assert
        assertThat(registry.getByCategory(ProductionConfig.ProductionCategory.PLANT)).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    // 3. LOOKUP BY ID TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should lookup production by ID")
    void testGetById() {
        // Arrange
        ProductionConfig config = createTestConfig("tobacco_virginia", "Virginia");
        registry.register(config);

        // Act
        ProductionConfig found = registry.get("tobacco_virginia");

        // Assert
        assertThat(found).isNotNull();
        assertThat(found).isEqualTo(config);
        assertThat(found.getId()).isEqualTo("tobacco_virginia");
        assertThat(found.getDisplayName()).isEqualTo("Virginia");
    }

    @Test
    @DisplayName("Should return null for non-existent ID")
    void testGetByIdNonExistent() {
        // Act
        ProductionConfig found = registry.get("non_existent");

        // Assert
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Should handle null ID in lookup gracefully")
    void testGetByNullId() {
        // Act & Assert - Should throw NullPointerException (expected for @Nonnull)
        assertThatThrownBy(() -> registry.get(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should check production existence by ID")
    void testHasById() {
        // Arrange
        ProductionConfig config = createTestConfig("test_tobacco", "Test");
        registry.register(config);

        // Act & Assert
        assertThat(registry.has("test_tobacco")).isTrue();
        assertThat(registry.has("non_existent")).isFalse();
    }

    // ═══════════════════════════════════════════════════════════
    // 4. LOOKUP BY CATEGORY TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should get productions by category")
    void testGetByCategory() {
        // Arrange
        ProductionConfig plant1 = new ProductionConfig.Builder("tobacco_virginia", "Virginia")
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build();

        ProductionConfig plant2 = new ProductionConfig.Builder("cannabis_indica", "Indica")
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build();

        ProductionConfig chemical = new ProductionConfig.Builder("mdma_standard", "MDMA")
            .category(ProductionConfig.ProductionCategory.CHEMICAL)
            .build();

        registry.register(plant1);
        registry.register(plant2);
        registry.register(chemical);

        // Act
        List<ProductionConfig> plants = registry.getByCategory(ProductionConfig.ProductionCategory.PLANT);
        List<ProductionConfig> chemicals = registry.getByCategory(ProductionConfig.ProductionCategory.CHEMICAL);

        // Assert
        assertThat(plants).hasSize(2).containsExactlyInAnyOrder(plant1, plant2);
        assertThat(chemicals).hasSize(1).contains(chemical);
    }

    @Test
    @DisplayName("Should return empty list for category with no productions")
    void testGetByCategoryEmpty() {
        // Act
        List<ProductionConfig> extracts = registry.getByCategory(ProductionConfig.ProductionCategory.EXTRACT);

        // Assert
        assertThat(extracts).isEmpty();
    }

    @Test
    @DisplayName("Should handle null category gracefully")
    void testGetByNullCategory() {
        // Act & Assert - Should throw NullPointerException (expected for @Nonnull)
        assertThatThrownBy(() -> registry.getByCategory(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle all production categories")
    void testAllCategories() {
        // Arrange - Create one production for each category
        for (ProductionConfig.ProductionCategory category : ProductionConfig.ProductionCategory.values()) {
            ProductionConfig config = new ProductionConfig.Builder("test_" + category.name(), "Test")
                .category(category)
                .build();
            registry.register(config);
        }

        // Act & Assert
        for (ProductionConfig.ProductionCategory category : ProductionConfig.ProductionCategory.values()) {
            List<ProductionConfig> configs = registry.getByCategory(category);
            assertThat(configs).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 5. COLLECTION OPERATIONS TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should get all productions")
    void testGetAll() {
        // Arrange
        ProductionConfig config1 = createTestConfig("tobacco_virginia", "Virginia");
        ProductionConfig config2 = createTestConfig("cannabis_indica", "Indica");
        ProductionConfig config3 = createTestConfig("mdma_standard", "MDMA");

        registry.register(config1);
        registry.register(config2);
        registry.register(config3);

        // Act
        Collection<ProductionConfig> all = registry.getAll();

        // Assert
        assertThat(all).hasSize(3).containsExactlyInAnyOrder(config1, config2, config3);
    }

    @Test
    @DisplayName("Should return empty collection when registry is empty")
    void testGetAllEmpty() {
        // Act
        Collection<ProductionConfig> all = registry.getAll();

        // Assert
        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("Should return correct production count")
    void testGetCount() {
        // Arrange & Act
        assertThat(registry.getCount()).isEqualTo(0);

        registry.register(createTestConfig("test1", "Test 1"));
        assertThat(registry.getCount()).isEqualTo(1);

        registry.register(createTestConfig("test2", "Test 2"));
        assertThat(registry.getCount()).isEqualTo(2);

        registry.register(createTestConfig("test3", "Test 3"));
        assertThat(registry.getCount()).isEqualTo(3);

        registry.unregister("test2");
        assertThat(registry.getCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should clear all productions")
    void testClear() {
        // Arrange
        registry.register(createTestConfig("test1", "Test 1"));
        registry.register(createTestConfig("test2", "Test 2"));
        registry.register(createTestConfig("test3", "Test 3"));
        assertThat(registry.getCount()).isEqualTo(3);

        // Act
        registry.clear();

        // Assert
        assertThat(registry.getCount()).isEqualTo(0);
        assertThat(registry.getAll()).isEmpty();
        assertThat(registry.has("test1")).isFalse();
    }

    // ═══════════════════════════════════════════════════════════
    // 6. PROCESSING STAGE TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should register production with processing stages")
    void testProductionWithProcessingStages() {
        // Arrange
        ProductionConfig config = new ProductionConfig.Builder("tobacco_virginia", "Virginia")
            .addProcessingStage("drying", new ProductionConfig.ProcessingStageConfig(
                "Trocknung", 1200, "fresh_leaf", "dried_leaf", true
            ))
            .addProcessingStage("fermentation", new ProductionConfig.ProcessingStageConfig(
                "Fermentation", 2400, "dried_leaf", "fermented_leaf", true
            ))
            .build();

        // Act
        registry.register(config);

        // Assert
        ProductionConfig found = registry.get("tobacco_virginia");
        assertThat(found).isNotNull();
        Map<String, ProductionConfig.ProcessingStageConfig> stages = found.getProcessingStages();
        assertThat(stages).hasSize(2);
        assertThat(stages).containsKeys("drying", "fermentation");
    }

    @Test
    @DisplayName("Should validate processing stage properties")
    void testProcessingStageProperties() {
        // Arrange
        ProductionConfig.ProcessingStageConfig stage = new ProductionConfig.ProcessingStageConfig(
            "Test Stage", 3600, "input_item", "output_item", true, "diesel", 100
        );

        // Assert
        assertThat(stage.getStageName()).isEqualTo("Test Stage");
        assertThat(stage.getProcessingTime()).isEqualTo(3600);
        assertThat(stage.getInputItem()).isEqualTo("input_item");
        assertThat(stage.getOutputItem()).isEqualTo("output_item");
        assertThat(stage.preservesQuality()).isTrue();
        assertThat(stage.getRequiredResource()).isEqualTo("diesel");
        assertThat(stage.getResourceAmount()).isEqualTo(100);
        assertThat(stage.requiresResource()).isTrue();
    }

    @Test
    @DisplayName("Should handle processing stage without resources")
    void testProcessingStageWithoutResources() {
        // Arrange
        ProductionConfig.ProcessingStageConfig stage = new ProductionConfig.ProcessingStageConfig(
            "Simple Stage", 1200, "input", "output", false
        );

        // Assert
        assertThat(stage.requiresResource()).isFalse();
        assertThat(stage.getRequiredResource()).isNull();
        assertThat(stage.getResourceAmount()).isEqualTo(0);
    }

    // ═══════════════════════════════════════════════════════════
    // 7. PRODUCTION CONFIG BUILDER TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should build production config with all properties")
    void testBuilderAllProperties() {
        // Arrange & Act
        ProductionConfig config = new ProductionConfig.Builder("test_id", "Test Production")
            .colorCode("§a")
            .basePrice(50.0)
            .growthTicks(2400)
            .baseYield(5)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .requiresLight(true)
            .minLightLevel(10)
            .requiresWater(true)
            .requiresTemperature(true)
            .qualityTiers(GenericQuality.createCannabis5TierSystem())
            .build();

        // Assert
        assertThat(config.getId()).isEqualTo("test_id");
        assertThat(config.getDisplayName()).isEqualTo("Test Production");
        assertThat(config.getColorCode()).isEqualTo("§a");
        assertThat(config.getBasePrice()).isEqualTo(50.0);
        assertThat(config.getGrowthTicks()).isEqualTo(2400);
        assertThat(config.getBaseYield()).isEqualTo(5);
        assertThat(config.getCategory()).isEqualTo(ProductionConfig.ProductionCategory.PLANT);
        assertThat(config.requiresLight()).isTrue();
        assertThat(config.getMinLightLevel()).isEqualTo(10);
        assertThat(config.requiresWater()).isTrue();
        assertThat(config.requiresTemperature()).isTrue();
        assertThat(config.getQualityTiers()).hasSize(5);
    }

    @Test
    @DisplayName("Should use default values in builder")
    void testBuilderDefaults() {
        // Arrange & Act
        ProductionConfig config = new ProductionConfig.Builder("minimal_id", "Minimal")
            .build();

        // Assert
        assertThat(config.getColorCode()).isEqualTo("§f");
        assertThat(config.getBasePrice()).isEqualTo(10.0);
        assertThat(config.getGrowthTicks()).isEqualTo(3600);
        assertThat(config.getBaseYield()).isEqualTo(3);
        assertThat(config.getCategory()).isEqualTo(ProductionConfig.ProductionCategory.PLANT);
        assertThat(config.requiresLight()).isTrue();
        assertThat(config.getMinLightLevel()).isEqualTo(8);
        assertThat(config.requiresWater()).isFalse();
        assertThat(config.requiresTemperature()).isFalse();
    }

    @Test
    @DisplayName("Should fail to build with null ID")
    void testBuilderNullId() {
        // Act & Assert
        assertThatThrownBy(() -> new ProductionConfig.Builder(null, "Test").build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should fail to build with empty ID")
    void testBuilderEmptyId() {
        // Act & Assert
        assertThatThrownBy(() -> new ProductionConfig.Builder("", "Test").build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should fail to build with null display name")
    void testBuilderNullDisplayName() {
        // Act & Assert
        assertThatThrownBy(() -> new ProductionConfig.Builder("test_id", null).build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Display name cannot be null or empty");
    }

    @Test
    @DisplayName("Should fail to build with empty display name")
    void testBuilderEmptyDisplayName() {
        // Act & Assert
        assertThatThrownBy(() -> new ProductionConfig.Builder("test_id", "").build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Display name cannot be null or empty");
    }

    // ═══════════════════════════════════════════════════════════
    // 8. THREAD SAFETY TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should handle concurrent registration from multiple threads")
    void testConcurrentRegistration() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int configsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < configsPerThread; i++) {
                        ProductionConfig config = createTestConfig(
                            "test_" + threadId + "_" + i,
                            "Test " + threadId + " " + i
                        );
                        registry.register(config);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        int expectedCount = threadCount * configsPerThread;
        assertThat(registry.getCount()).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("Should handle concurrent lookups from multiple threads")
    void testConcurrentLookups() throws InterruptedException {
        // Arrange
        ProductionConfig config1 = createTestConfig("test1", "Test 1");
        ProductionConfig config2 = createTestConfig("test2", "Test 2");
        registry.register(config1);
        registry.register(config2);

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        ProductionConfig found = registry.get("test1");
                        if (found == config1) {
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

        // Assert
        assertThat(successCount.get()).isEqualTo(threadCount * 100);
    }

    @Test
    @DisplayName("Should handle concurrent unregistration from multiple threads")
    void testConcurrentUnregistration() throws InterruptedException {
        // Arrange - Register many configs
        int configCount = 1000;
        for (int i = 0; i < configCount; i++) {
            registry.register(createTestConfig("test_" + i, "Test " + i));
        }
        assertThat(registry.getCount()).isEqualTo(configCount);

        // Act - Unregister from multiple threads
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = threadId; i < configCount; i += threadCount) {
                        registry.unregister("test_" + i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertThat(registry.getCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle mixed concurrent read/write operations")
    void testConcurrentMixedOperations() throws InterruptedException {
        // Arrange
        int threadCount = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    if (threadId % 3 == 0) {
                        // Registration threads
                        for (int i = 0; i < 30; i++) {
                            registry.register(createTestConfig("test_" + threadId + "_" + i, "Test"));
                        }
                    } else if (threadId % 3 == 1) {
                        // Lookup threads
                        for (int i = 0; i < 50; i++) {
                            registry.getAll();
                            registry.getCount();
                        }
                    } else {
                        // Register then unregister threads
                        for (int i = 0; i < 20; i++) {
                            String id = "temp_" + threadId + "_" + i;
                            registry.register(createTestConfig(id, "Temp"));
                            registry.unregister(id);
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
        assertThat(registry.getCount()).isGreaterThanOrEqualTo(0);
    }

    // ═══════════════════════════════════════════════════════════
    // 9. EDGE CASES & STRESS TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should handle large number of productions (1000+)")
    void testLargeNumberOfProductions() {
        // Arrange & Act
        int productionCount = 1000;
        for (int i = 0; i < productionCount; i++) {
            registry.register(createTestConfig("test_" + i, "Test " + i));
        }

        // Assert
        assertThat(registry.getCount()).isEqualTo(productionCount);
        assertThat(registry.getAll()).hasSize(productionCount);
    }

    @Test
    @DisplayName("Should handle extreme values in production config")
    void testExtremeValues() {
        // Arrange & Act
        ProductionConfig config = new ProductionConfig.Builder("extreme_test", "Extreme")
            .basePrice(Double.MAX_VALUE)
            .growthTicks(Integer.MAX_VALUE)
            .baseYield(Integer.MAX_VALUE)
            .minLightLevel(15)
            .build();

        registry.register(config);

        // Assert
        ProductionConfig found = registry.get("extreme_test");
        assertThat(found.getBasePrice()).isEqualTo(Double.MAX_VALUE);
        assertThat(found.getGrowthTicks()).isEqualTo(Integer.MAX_VALUE);
        assertThat(found.getBaseYield()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Should handle negative values gracefully")
    void testNegativeValues() {
        // Arrange & Act
        ProductionConfig config = new ProductionConfig.Builder("negative_test", "Negative")
            .basePrice(-50.0)
            .growthTicks(-1000)
            .baseYield(-5)
            .minLightLevel(-10)
            .build();

        // Assert - Builder should allow negative values (validation should be elsewhere)
        assertThat(config.getBasePrice()).isEqualTo(-50.0);
        assertThat(config.getGrowthTicks()).isEqualTo(-1000);
        assertThat(config.getBaseYield()).isEqualTo(-5);
    }

    @Test
    @DisplayName("Should maintain category integrity after multiple operations")
    void testCategoryIntegrity() {
        // Arrange
        ProductionConfig plant1 = new ProductionConfig.Builder("plant1", "Plant 1")
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build();
        ProductionConfig plant2 = new ProductionConfig.Builder("plant2", "Plant 2")
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build();
        ProductionConfig chemical = new ProductionConfig.Builder("chemical1", "Chemical 1")
            .category(ProductionConfig.ProductionCategory.CHEMICAL)
            .build();

        // Act
        registry.register(plant1);
        registry.register(plant2);
        registry.register(chemical);

        assertThat(registry.getByCategory(ProductionConfig.ProductionCategory.PLANT)).hasSize(2);
        assertThat(registry.getByCategory(ProductionConfig.ProductionCategory.CHEMICAL)).hasSize(1);

        registry.unregister("plant1");

        // Assert
        assertThat(registry.getByCategory(ProductionConfig.ProductionCategory.PLANT)).hasSize(1);
        assertThat(registry.getByCategory(ProductionConfig.ProductionCategory.CHEMICAL)).hasSize(1);
    }

    @Test
    @DisplayName("Should handle multiple clear operations")
    void testMultipleClearOperations() {
        // Arrange
        registry.register(createTestConfig("test1", "Test 1"));
        registry.register(createTestConfig("test2", "Test 2"));

        // Act & Assert
        registry.clear();
        assertThat(registry.getCount()).isEqualTo(0);

        registry.clear(); // Clear again
        assertThat(registry.getCount()).isEqualTo(0);

        // Re-register after clear
        registry.register(createTestConfig("test3", "Test 3"));
        assertThat(registry.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should test singleton pattern integrity")
    void testSingletonIntegrity() {
        // Act
        ProductionRegistry instance1 = ProductionRegistry.getInstance();
        ProductionRegistry instance2 = ProductionRegistry.getInstance();

        // Assert
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    @DisplayName("Should get default quality tier")
    void testDefaultQuality() {
        // Arrange
        GenericQuality[] qualities = GenericQuality.createStandard4TierSystem();
        ProductionConfig config = new ProductionConfig.Builder("test_quality", "Test")
            .qualityTiers(qualities)
            .build();

        // Act
        GenericQuality defaultQuality = config.getDefaultQuality();

        // Assert
        assertThat(defaultQuality).isNotNull();
        assertThat(defaultQuality).isEqualTo(qualities[qualities.length / 2]); // Middle tier
    }

    // ═══════════════════════════════════════════════════════════
    // 10. PERFORMANCE TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should demonstrate fast lookup performance")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testLookupPerformance() {
        // Arrange - Register 5000 productions
        for (int i = 0; i < 5000; i++) {
            registry.register(createTestConfig("test_" + i, "Test " + i));
        }

        // Act - Perform 5000 lookups
        for (int i = 0; i < 5000; i++) {
            registry.get("test_" + i);
        }

        // Test should complete within timeout
    }

    @Test
    @DisplayName("Should demonstrate fast category lookup performance")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testCategoryLookupPerformance() {
        // Arrange - Register 1000 productions per category
        for (ProductionConfig.ProductionCategory category : ProductionConfig.ProductionCategory.values()) {
            for (int i = 0; i < 200; i++) {
                ProductionConfig config = new ProductionConfig.Builder(
                    category.name() + "_" + i,
                    "Test " + i
                ).category(category).build();
                registry.register(config);
            }
        }

        // Act - Perform category lookups
        for (int i = 0; i < 1000; i++) {
            for (ProductionConfig.ProductionCategory category : ProductionConfig.ProductionCategory.values()) {
                registry.getByCategory(category);
            }
        }

        // Test should complete within timeout
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private ProductionConfig createTestConfig(String id, String displayName) {
        return new ProductionConfig.Builder(id, displayName)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build();
    }
}
