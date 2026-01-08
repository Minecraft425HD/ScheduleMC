package de.rolandsw.schedulemc.production;

import de.rolandsw.schedulemc.production.config.ProductionConfig;
import de.rolandsw.schedulemc.production.config.ProductionRegistry;
import de.rolandsw.schedulemc.production.core.GenericPlantData;
import de.rolandsw.schedulemc.production.core.GenericQuality;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für Generic Production System
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GenericProductionSystemTest {

    // ═══════════════════════════════════════════════════════════
    // TEST PRODUCTION TYPE
    // ═══════════════════════════════════════════════════════════

    static class TestProductionType implements ProductionType {
        private final String name;
        private final int growthTicks;
        private final int baseYield;

        public TestProductionType(String name, int growthTicks, int baseYield) {
            this.name = name;
            this.growthTicks = growthTicks;
            this.baseYield = baseYield;
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public String getColorCode() {
            return "§a";
        }

        @Override
        public double getBasePrice() {
            return 10.0;
        }

        @Override
        public int getGrowthTicks() {
            return growthTicks;
        }

        @Override
        public int getBaseYield() {
            return baseYield;
        }

        @Override
        public double calculatePrice(ProductionQuality quality, int amount) {
            return getBasePrice() * quality.getPriceMultiplier() * amount;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GENERIC QUALITY TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("GenericQuality: Standard 4-Tier System")
    public void testStandard4TierSystem() {
        GenericQuality[] tiers = GenericQuality.createStandard4TierSystem();

        assertEquals(4, tiers.length, "Should have 4 tiers");

        // Tier 0: Schlecht
        assertEquals("Schlecht", tiers[0].getDisplayName());
        assertEquals(0, tiers[0].getLevel());
        assertEquals(0.7, tiers[0].getPriceMultiplier(), 0.01);

        // Tier 3: Legendär
        assertEquals("Legendär", tiers[3].getDisplayName());
        assertEquals(3, tiers[3].getLevel());
        assertEquals(2.5, tiers[3].getPriceMultiplier(), 0.01);
    }

    @Test
    @Order(2)
    @DisplayName("GenericQuality: Cannabis 5-Tier System")
    public void testCannabis5TierSystem() {
        GenericQuality[] tiers = GenericQuality.createCannabis5TierSystem();

        assertEquals(5, tiers.length, "Should have 5 tiers");

        // Tier 0: Schwag
        assertEquals("Schwag", tiers[0].getDisplayName());
        assertEquals(0.5, tiers[0].getPriceMultiplier(), 0.01);

        // Tier 4: Exotic
        assertEquals("Exotic", tiers[4].getDisplayName());
        assertEquals(5.0, tiers[4].getPriceMultiplier(), 0.01);
    }

    @Test
    @Order(3)
    @DisplayName("GenericQuality: Upgrade/Downgrade")
    public void testQualityUpgradeDowngrade() {
        GenericQuality[] tiers = GenericQuality.createStandard4TierSystem();

        GenericQuality middle = tiers[1];  // Gut (Level 1)

        // Upgrade
        ProductionQuality upgraded = middle.upgrade();
        assertEquals(2, upgraded.getLevel(), "Should upgrade to level 2");

        // Downgrade
        ProductionQuality downgraded = middle.downgrade();
        assertEquals(0, downgraded.getLevel(), "Should downgrade to level 0");

        // Can't downgrade below min
        GenericQuality min = tiers[0];
        ProductionQuality stillMin = min.downgrade();
        assertEquals(0, stillMin.getLevel(), "Should stay at level 0");

        // Can't upgrade above max
        GenericQuality max = tiers[3];
        ProductionQuality stillMax = max.upgrade();
        assertEquals(3, stillMax.getLevel(), "Should stay at level 3");
    }

    @Test
    @Order(4)
    @DisplayName("GenericQuality: Custom Tier System")
    public void testCustomTierSystem() {
        // Create 6-tier system with custom multipliers
        GenericQuality[] tiers = GenericQuality.createCustomTierSystem(6, 0.5, 10.0);

        assertEquals(6, tiers.length, "Should have 6 tiers");

        // Check linear interpolation
        assertEquals(0.5, tiers[0].getPriceMultiplier(), 0.01, "First tier should have base multiplier");
        assertEquals(10.0, tiers[5].getPriceMultiplier(), 0.01, "Last tier should have max multiplier");

        // Middle tier should be roughly halfway
        double expectedMiddle = 0.5 + (10.0 - 0.5) / 2.0;
        assertEquals(expectedMiddle, tiers[3].getPriceMultiplier(), 1.0, "Middle tier should be halfway");
    }

    @Test
    @Order(5)
    @DisplayName("GenericQuality: Builder Pattern")
    public void testQualityBuilder() {
        GenericQuality[] tiers = new GenericQuality.Builder(3)
            .names("Low", "Medium", "High")
            .colorCodes("§c", "§e", "§a")
            .priceMultipliers(0.8, 1.5, 3.0)
            .descriptions("Low quality", "Medium quality", "High quality")
            .build();

        assertEquals(3, tiers.length);
        assertEquals("Medium", tiers[1].getDisplayName());
        assertEquals(1.5, tiers[1].getPriceMultiplier(), 0.01);
        assertEquals("§e", tiers[1].getColorCode());
    }

    // ═══════════════════════════════════════════════════════════
    // GENERIC PLANT DATA TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("GenericPlantData: Construction")
    public void testPlantDataConstruction() {
        GenericQuality[] qualities = GenericQuality.createStandard4TierSystem();
        TestProductionType type = new TestProductionType("Test Plant", 3600, 3);

        GenericPlantData<TestProductionType, GenericQuality> plant =
            new GenericPlantData<>(type, qualities[1]);

        assertEquals(0, plant.getGrowthStage(), "Should start at stage 0");
        assertEquals(0, plant.getTicksGrown(), "Should have 0 ticks");
        assertFalse(plant.isFullyGrown(), "Should not be fully grown");
        assertFalse(plant.hasFertilizer(), "Should not have fertilizer");
    }

    @Test
    @Order(11)
    @DisplayName("GenericPlantData: Growth Progression")
    public void testPlantGrowth() {
        GenericQuality[] qualities = GenericQuality.createStandard4TierSystem();
        TestProductionType type = new TestProductionType("Test Plant", 800, 3);  // 800 ticks = 40 seconds

        GenericPlantData<TestProductionType, GenericQuality> plant =
            new GenericPlantData<>(type, qualities[1]);

        // Simulate growth
        assertEquals(0, plant.getGrowthStage());

        // Tick until Stage 1 (100 ticks)
        for (int i = 0; i < 100; i++) {
            plant.tick();
        }
        assertEquals(1, plant.getGrowthStage(), "Should reach stage 1 after 100 ticks");

        // Tick until Stage 7 (800 ticks total)
        for (int i = 0; i < 700; i++) {
            plant.tick();
        }
        assertTrue(plant.getGrowthStage() >= 7, "Should reach stage 7 after 800 ticks");
        assertTrue(plant.isFullyGrown(), "Should be fully grown");
        assertTrue(plant.canHarvest(), "Should be harvestable");
    }

    @Test
    @Order(12)
    @DisplayName("GenericPlantData: Growth Booster Effect")
    public void testGrowthBooster() {
        GenericQuality[] qualities = GenericQuality.createStandard4TierSystem();
        TestProductionType type = new TestProductionType("Test Plant", 1000, 3);

        // Plant without booster
        GenericPlantData<TestProductionType, GenericQuality> plantA =
            new GenericPlantData<>(type, qualities[1]);

        // Plant with booster
        GenericPlantData<TestProductionType, GenericQuality> plantB =
            new GenericPlantData<>(type, qualities[1]);
        plantB.applyGrowthBooster();

        // Grow both for 500 ticks
        for (int i = 0; i < 500; i++) {
            plantA.tick();
            plantB.tick();
        }

        // Plant B should be further ahead (30% faster)
        assertTrue(plantB.getGrowthStage() > plantA.getGrowthStage(),
            "Plant with booster should grow faster");

        assertEquals(700, plantB.getGrowthSpeed(), "Booster should reduce growth time by 30%");
    }

    @Test
    @Order(13)
    @DisplayName("GenericPlantData: Harvest Yield Calculation")
    public void testHarvestYield() {
        GenericQuality[] qualities = GenericQuality.createStandard4TierSystem();
        TestProductionType type = new TestProductionType("Test Plant", 800, 5);  // Base yield 5

        // Test without fertilizer
        GenericPlantData<TestProductionType, GenericQuality> plantA =
            new GenericPlantData<>(type, qualities[0]);  // Schlecht quality (0.7x)
        int yieldA = plantA.getHarvestYield();
        assertEquals(3, yieldA, 1, "Schlecht quality should give ~3 (5 * 0.7)");

        // Test with fertilizer
        GenericPlantData<TestProductionType, GenericQuality> plantB =
            new GenericPlantData<>(type, qualities[3]);  // Legendär quality (1.6x)
        plantB.applyFertilizer();
        int yieldB = plantB.getHarvestYield();
        assertTrue(yieldB >= 8, "Legendär + fertilizer should give >=8 (5*1.6 + 1-2)");
    }

    @Test
    @Order(14)
    @DisplayName("GenericPlantData: Quality Yield Multipliers")
    public void testQualityYieldMultipliers() {
        GenericQuality[] qualities = GenericQuality.createStandard4TierSystem();
        TestProductionType type = new TestProductionType("Test Plant", 800, 10);

        // Test each quality tier
        int[] expectedYields = {7, 10, 13, 16};  // 10 * [0.7, 1.0, 1.3, 1.6]

        for (int i = 0; i < 4; i++) {
            GenericPlantData<TestProductionType, GenericQuality> plant =
                new GenericPlantData<>(type, qualities[i]);
            int yield = plant.getHarvestYield();
            assertEquals(expectedYields[i], yield, 2,
                String.format("Quality level %d should give yield ~%d", i, expectedYields[i]));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PRODUCTION REGISTRY TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(20)
    @DisplayName("ProductionRegistry: Default Productions")
    public void testRegistryDefaultProductions() {
        ProductionRegistry registry = ProductionRegistry.getInstance();

        // Should have default productions
        assertTrue(registry.getCount() > 0, "Registry should have productions");

        // Test specific productions
        assertNotNull(registry.get("tobacco_virginia"), "Should have Virginia tobacco");
        assertNotNull(registry.get("cannabis_indica"), "Should have Indica cannabis");
        assertNotNull(registry.get("coca_colombian"), "Should have Colombian coca");
        assertNotNull(registry.get("poppy_afghan"), "Should have Afghan poppy");
        assertNotNull(registry.get("mushroom_cubensis"), "Should have Cubensis mushroom");
    }

    @Test
    @Order(21)
    @DisplayName("ProductionRegistry: Category Filtering")
    public void testRegistryCategoryFiltering() {
        ProductionRegistry registry = ProductionRegistry.getInstance();

        // Get all plant productions
        var plants = registry.getByCategory(ProductionConfig.ProductionCategory.PLANT);
        assertTrue(plants.size() >= 4, "Should have at least 4 plant productions");

        // Get all chemical productions
        var chemicals = registry.getByCategory(ProductionConfig.ProductionCategory.CHEMICAL);
        assertTrue(chemicals.size() >= 3, "Should have at least 3 chemical productions");
    }

    @Test
    @Order(22)
    @DisplayName("ProductionRegistry: Custom Production Registration")
    public void testCustomProductionRegistration() {
        ProductionRegistry registry = ProductionRegistry.getInstance();

        // Create custom production
        ProductionConfig custom = new ProductionConfig.Builder("test_custom", "Test Custom Plant")
            .basePrice(99.99)
            .growthTicks(1234)
            .baseYield(7)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build();

        // Register
        registry.register(custom);

        // Verify
        assertTrue(registry.has("test_custom"), "Should have registered custom production");
        ProductionConfig retrieved = registry.get("test_custom");
        assertNotNull(retrieved);
        assertEquals("Test Custom Plant", retrieved.getDisplayName());
        assertEquals(99.99, retrieved.getBasePrice(), 0.01);

        // Cleanup
        registry.unregister("test_custom");
        assertFalse(registry.has("test_custom"), "Should have unregistered custom production");
    }

    @Test
    @Order(23)
    @DisplayName("ProductionConfig: Processing Stages")
    public void testProductionConfigProcessingStages() {
        ProductionRegistry registry = ProductionRegistry.getInstance();
        ProductionConfig tobacco = registry.get("tobacco_virginia");

        assertNotNull(tobacco, "Virginia tobacco should exist");

        var stages = tobacco.getProcessingStages();
        assertTrue(stages.containsKey("drying"), "Should have drying stage");
        assertTrue(stages.containsKey("fermentation"), "Should have fermentation stage");
        assertTrue(stages.containsKey("packaging"), "Should have packaging stage");

        // Check drying stage
        var dryingStage = stages.get("drying");
        assertEquals("Trocknung", dryingStage.getStageName());
        assertEquals(1200, dryingStage.getProcessingTime());
        assertTrue(dryingStage.preservesQuality(), "Drying should preserve quality");
    }

    @Test
    @Order(24)
    @DisplayName("ProductionConfig: Resource Requirements")
    public void testProductionConfigResourceRequirements() {
        ProductionRegistry registry = ProductionRegistry.getInstance();
        ProductionConfig coca = registry.get("coca_colombian");

        assertNotNull(coca, "Colombian coca should exist");

        var stages = coca.getProcessingStages();
        var extractionStage = stages.get("extraction");

        assertNotNull(extractionStage, "Should have extraction stage");
        assertTrue(extractionStage.requiresResource(), "Extraction should require resource");
        assertEquals("diesel", extractionStage.getRequiredResource(), "Should require diesel");
        assertEquals(100, extractionStage.getResourceAmount(), "Should require 100 units");
    }

    // ═══════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(30)
    @DisplayName("Integration: Full Production Cycle")
    public void testFullProductionCycle() {
        GenericQuality[] qualities = GenericQuality.createStandard4TierSystem();
        TestProductionType type = new TestProductionType("Test Tobacco", 800, 4);

        // 1. Plant
        GenericPlantData<TestProductionType, GenericQuality> plant =
            new GenericPlantData<>(type, qualities[1]);

        // 2. Apply boosters
        plant.applyFertilizer();
        plant.applyGrowthBooster();
        plant.applyQualityBooster();

        // 3. Grow to maturity
        for (int i = 0; i < 1000; i++) {
            plant.tick();
        }

        assertTrue(plant.isFullyGrown(), "Plant should be fully grown");
        assertTrue(plant.canHarvest(), "Plant should be harvestable");

        // 4. Harvest
        int yield = plant.getHarvestYield();
        assertTrue(yield >= 4, "Yield should be at least base yield");

        boolean harvested = plant.harvest();
        assertTrue(harvested, "Harvest should succeed");
    }

    @Test
    @Order(31)
    @DisplayName("Integration: Quality System Consistency")
    public void testQualitySystemConsistency() {
        GenericQuality[] tiers = GenericQuality.createStandard4TierSystem();

        // Test consistency across all tiers
        for (int i = 0; i < tiers.length; i++) {
            assertEquals(i, tiers[i].getLevel(), "Level should match index");
            assertEquals(i, tiers[i].getTierIndex(), "Tier index should match");

            // Test upgrade/downgrade consistency
            if (i < tiers.length - 1) {
                assertTrue(tiers[i].canUpgrade(), "Should be able to upgrade");
                assertEquals(tiers[i + 1], tiers[i].upgrade(), "Upgrade should return next tier");
            } else {
                assertFalse(tiers[i].canUpgrade(), "Max tier should not be able to upgrade");
            }

            if (i > 0) {
                assertTrue(tiers[i].canDowngrade(), "Should be able to downgrade");
                assertEquals(tiers[i - 1], tiers[i].downgrade(), "Downgrade should return previous tier");
            } else {
                assertFalse(tiers[i].canDowngrade(), "Min tier should not be able to downgrade");
            }
        }
    }
}
