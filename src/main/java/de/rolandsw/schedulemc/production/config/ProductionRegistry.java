package de.rolandsw.schedulemc.production.config;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.production.core.GenericQuality;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Production Registry - Zentrale Verwaltung aller Produktionstypen
 *
 * Erlaubt:
 * - Registrierung neuer Produktionen zur Laufzeit
 * - Lookup nach ID
 * - Dynamische Erweiterung ohne Code-Änderungen
 */
public class ProductionRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Singleton
    private static ProductionRegistry instance;

    // Registry
    private final Map<String, ProductionConfig> productions = new ConcurrentHashMap<>();

    // Kategorisierung
    private final Map<ProductionConfig.ProductionCategory, List<ProductionConfig>> byCategory = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private ProductionRegistry() {
        registerDefaultProductions();
    }

    public static ProductionRegistry getInstance() {
        if (instance == null) {
            synchronized (ProductionRegistry.class) {
                if (instance == null) {
                    instance = new ProductionRegistry();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTRATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert eine neue Production
     */
    public void register(ProductionConfig config) {
        String id = config.getId();

        if (productions.containsKey(id)) {
            LOGGER.warn("Production '{}' is already registered, overwriting...", id);
        }

        productions.put(id, config);

        // Kategorisierung
        ProductionConfig.ProductionCategory category = config.getCategory();
        byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(config);

        LOGGER.info("Registered production: {} ({}) - {}",
            id, config.getDisplayName(), category.getDisplayName());
    }

    /**
     * Entfernt eine Production
     */
    public boolean unregister(String id) {
        ProductionConfig removed = productions.remove(id);
        if (removed != null) {
            byCategory.get(removed.getCategory()).remove(removed);
            LOGGER.info("Unregistered production: {}", id);
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════
    // LOOKUP
    // ═══════════════════════════════════════════════════════════

    @Nullable
    public ProductionConfig get(String id) {
        return productions.get(id);
    }

    public boolean has(String id) {
        return productions.containsKey(id);
    }

    public Collection<ProductionConfig> getAll() {
        return new ArrayList<>(productions.values());
    }

    public List<ProductionConfig> getByCategory(ProductionConfig.ProductionCategory category) {
        return new ArrayList<>(byCategory.getOrDefault(category, Collections.emptyList()));
    }

    public int getCount() {
        return productions.size();
    }

    // ═══════════════════════════════════════════════════════════
    // DEFAULT PRODUCTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert Standard-Produktionen (bestehende Systeme)
     */
    private void registerDefaultProductions() {
        LOGGER.info("Registering default production types...");

        // TOBACCO VARIANTS
        registerTobacco();

        // CANNABIS STRAINS
        registerCannabis();

        // COCA VARIANTS
        registerCoca();

        // POPPY VARIANTS
        registerPoppy();

        // MUSHROOM TYPES
        registerMushroom();

        // MDMA
        registerMDMA();

        // LSD
        registerLSD();

        // METH
        registerMeth();

        LOGGER.info("Registered {} production types", productions.size());
    }

    // ═══════════════════════════════════════════════════════════
    // TOBACCO
    // ═══════════════════════════════════════════════════════════

    private void registerTobacco() {
        // Virginia
        register(new ProductionConfig.Builder("tobacco_virginia", "Virginia Tabak")
            .colorCode("§6")
            .basePrice(15.0)
            .growthTicks(3600)  // 3 Minuten
            .baseYield(3)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .requiresLight(true)
            .minLightLevel(8)
            .addProcessingStage("drying", new ProductionConfig.ProcessingStageConfig(
                "Trocknung", 1200, "fresh_tobacco_leaf", "dried_tobacco_leaf", true
            ))
            .addProcessingStage("fermentation", new ProductionConfig.ProcessingStageConfig(
                "Fermentation", 2400, "dried_tobacco_leaf", "fermented_tobacco_leaf", true
            ))
            .addProcessingStage("packaging", new ProductionConfig.ProcessingStageConfig(
                "Verpackung", 600, "fermented_tobacco_leaf", "packaged_tobacco", true
            ))
            .qualityTiers(GenericQuality.createStandard4TierSystem())
            .build()
        );

        // Burley
        register(new ProductionConfig.Builder("tobacco_burley", "Burley Tabak")
            .colorCode("§e")
            .basePrice(18.0)
            .growthTicks(4200)
            .baseYield(4)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build()
        );

        // Oriental
        register(new ProductionConfig.Builder("tobacco_oriental", "Oriental Tabak")
            .colorCode("§d")
            .basePrice(25.0)
            .growthTicks(4800)
            .baseYield(2)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build()
        );

        // Havana
        register(new ProductionConfig.Builder("tobacco_havana", "Havana Tabak")
            .colorCode("§5")
            .basePrice(35.0)
            .growthTicks(6000)
            .baseYield(2)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // CANNABIS
    // ═══════════════════════════════════════════════════════════

    private void registerCannabis() {
        GenericQuality[] cannabis5Tier = GenericQuality.createCannabis5TierSystem();

        // Indica
        register(new ProductionConfig.Builder("cannabis_indica", "Indica")
            .colorCode("§5")
            .basePrice(25.0)
            .growthTicks(2400)  // 120 Sekunden = 2 Minuten
            .baseYield(6)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .qualityTiers(cannabis5Tier)
            .addProcessingStage("drying", new ProductionConfig.ProcessingStageConfig(
                "Trocknung", 72000, "fresh_bud", "dried_bud", true
            ))
            .addProcessingStage("curing", new ProductionConfig.ProcessingStageConfig(
                "Curing", 144000, "dried_bud", "cured_bud", true
            ))
            .build()
        );

        // Sativa
        register(new ProductionConfig.Builder("cannabis_sativa", "Sativa")
            .colorCode("§a")
            .basePrice(30.0)
            .growthTicks(3200)
            .baseYield(6)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .qualityTiers(cannabis5Tier)
            .build()
        );

        // Hybrid
        register(new ProductionConfig.Builder("cannabis_hybrid", "Hybrid")
            .colorCode("§e")
            .basePrice(35.0)
            .growthTicks(2800)
            .baseYield(6)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .qualityTiers(cannabis5Tier)
            .build()
        );

        // Autoflower
        register(new ProductionConfig.Builder("cannabis_autoflower", "Autoflower")
            .colorCode("§b")
            .basePrice(20.0)
            .growthTicks(1400)  // 70 Sekunden
            .baseYield(6)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .requiresLight(false)  // Autoflower braucht kein Licht
            .qualityTiers(cannabis5Tier)
            .build()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // COCA
    // ═══════════════════════════════════════════════════════════

    private void registerCoca() {
        // Colombian
        register(new ProductionConfig.Builder("coca_colombian", "Colombian Coca")
            .colorCode("§a")
            .basePrice(30.0)
            .growthTicks(3000)
            .baseYield(4)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .addProcessingStage("extraction", new ProductionConfig.ProcessingStageConfig(
                "Extraktion", 2400, "coca_leaf", "coca_paste", true, "diesel", 100
            ))
            .addProcessingStage("refinement", new ProductionConfig.ProcessingStageConfig(
                "Raffination", 3600, "coca_paste", "cocaine", true, "fuel", 50
            ))
            .build()
        );

        // Bolivian
        register(new ProductionConfig.Builder("coca_bolivian", "Bolivian Coca")
            .colorCode("§2")
            .basePrice(35.0)
            .growthTicks(3600)
            .baseYield(3)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // POPPY
    // ═══════════════════════════════════════════════════════════

    private void registerPoppy() {
        // Afghan
        register(new ProductionConfig.Builder("poppy_afghan", "Afghan Poppy")
            .colorCode("§c")
            .basePrice(40.0)
            .growthTicks(4000)
            .baseYield(3)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .addProcessingStage("scoring", new ProductionConfig.ProcessingStageConfig(
                "Anritzen", 1200, "poppy_pod", "raw_opium", true
            ))
            .addProcessingStage("pressing", new ProductionConfig.ProcessingStageConfig(
                "Pressen", 1800, "raw_opium", "opium_block", true
            ))
            .addProcessingStage("cooking", new ProductionConfig.ProcessingStageConfig(
                "Kochen", 2400, "opium_block", "morphine", true, "water", 1
            ))
            .addProcessingStage("refinement", new ProductionConfig.ProcessingStageConfig(
                "Raffination", 3600, "morphine", "heroin", true
            ))
            .build()
        );

        // Turkish
        register(new ProductionConfig.Builder("poppy_turkish", "Turkish Poppy")
            .colorCode("§6")
            .basePrice(45.0)
            .growthTicks(4800)
            .baseYield(2)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build()
        );

        // Indian
        register(new ProductionConfig.Builder("poppy_indian", "Indian Poppy")
            .colorCode("§d")
            .basePrice(38.0)
            .growthTicks(3600)
            .baseYield(4)
            .category(ProductionConfig.ProductionCategory.PLANT)
            .build()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // MUSHROOM
    // ═══════════════════════════════════════════════════════════

    private void registerMushroom() {
        // Psilocybe Cubensis
        register(new ProductionConfig.Builder("mushroom_cubensis", "Psilocybe Cubensis")
            .colorCode("§d")
            .basePrice(50.0)
            .growthTicks(2400)
            .baseYield(4)
            .category(ProductionConfig.ProductionCategory.MUSHROOM)
            .requiresLight(false)  // Darkness for incubation
            .addProcessingStage("drying", new ProductionConfig.ProcessingStageConfig(
                "Trocknung", 1200, "fresh_mushroom", "dried_mushroom", true
            ))
            .build()
        );

        // Golden Teacher
        register(new ProductionConfig.Builder("mushroom_golden_teacher", "Golden Teacher")
            .colorCode("§6")
            .basePrice(60.0)
            .growthTicks(2800)
            .baseYield(3)
            .category(ProductionConfig.ProductionCategory.MUSHROOM)
            .build()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // MDMA
    // ═══════════════════════════════════════════════════════════

    private void registerMDMA() {
        register(new ProductionConfig.Builder("mdma_standard", "MDMA")
            .colorCode("§b")
            .basePrice(80.0)
            .growthTicks(0)  // No growth, synthesized
            .baseYield(0)
            .category(ProductionConfig.ProductionCategory.CHEMICAL)
            .addProcessingStage("synthesis", new ProductionConfig.ProcessingStageConfig(
                "Synthese", 4800, "safrol", "mdma_base", false
            ))
            .addProcessingStage("crystallization", new ProductionConfig.ProcessingStageConfig(
                "Kristallisation", 2400, "mdma_base", "mdma_crystal", true
            ))
            .addProcessingStage("pressing", new ProductionConfig.ProcessingStageConfig(
                "Pressen", 1200, "mdma_crystal", "ecstasy_pill", true
            ))
            .build()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // LSD
    // ═══════════════════════════════════════════════════════════

    private void registerLSD() {
        register(new ProductionConfig.Builder("lsd_standard", "LSD")
            .colorCode("§5")
            .basePrice(100.0)
            .growthTicks(0)
            .baseYield(0)
            .category(ProductionConfig.ProductionCategory.CHEMICAL)
            .addProcessingStage("fermentation", new ProductionConfig.ProcessingStageConfig(
                "Fermentation", 6000, "ergot_culture", "lysergic_acid", false
            ))
            .addProcessingStage("distillation", new ProductionConfig.ProcessingStageConfig(
                "Destillation", 4800, "lysergic_acid", "lsd_solution", true
            ))
            .addProcessingStage("blotting", new ProductionConfig.ProcessingStageConfig(
                "Blotting", 1200, "lsd_solution", "lsd_blotter", true
            ))
            .build()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // METH
    // ═══════════════════════════════════════════════════════════

    private void registerMeth() {
        register(new ProductionConfig.Builder("meth_standard", "Methamphetamine")
            .colorCode("§b")
            .basePrice(120.0)
            .growthTicks(0)
            .baseYield(0)
            .category(ProductionConfig.ProductionCategory.CHEMICAL)
            .addProcessingStage("cooking", new ProductionConfig.ProcessingStageConfig(
                "Kochen", 7200, "precursor", "meth_crystal", false
            ))
            .build()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    public void printRegistry() {
        LOGGER.info("═══════════════════════════════════════");
        LOGGER.info("PRODUCTION REGISTRY - {} Productions", productions.size());
        LOGGER.info("═══════════════════════════════════════");

        for (ProductionConfig.ProductionCategory category : ProductionConfig.ProductionCategory.values()) {
            List<ProductionConfig> configs = getByCategory(category);
            if (!configs.isEmpty()) {
                LOGGER.info("");
                LOGGER.info("{}{} ({} types)", category.getColorCode(), category.getDisplayName(), configs.size());
                for (ProductionConfig config : configs) {
                    LOGGER.info("  - {} ({})", config.getId(), config.getDisplayName());
                }
            }
        }

        LOGGER.info("═══════════════════════════════════════");
    }

    /**
     * Für Tests: Registry zurücksetzen
     */
    public void clear() {
        productions.clear();
        byCategory.clear();
        LOGGER.warn("Production registry cleared!");
    }
}
