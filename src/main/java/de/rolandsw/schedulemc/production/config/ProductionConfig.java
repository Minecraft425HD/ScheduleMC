package de.rolandsw.schedulemc.production.config;

import de.rolandsw.schedulemc.production.core.GenericQuality;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;

import java.util.HashMap;
import java.util.Map;

/**
 * Production Config System - Konfigurationsbasierte Produktionsdefinition
 *
 * Ermöglicht das Hinzufügen neuer Produktionstypen ohne Code-Änderungen
 */
public class ProductionConfig {

    // ═══════════════════════════════════════════════════════════
    // CONFIG DATA
    // ═══════════════════════════════════════════════════════════

    private final String id;                    // Unique identifier (z.B. "tobacco_virginia")
    private final String displayName;           // Display name
    private final String colorCode;             // Minecraft color code
    private final double basePrice;             // Base price per unit
    private final int growthTicks;              // Ticks to grow from 0 to 7
    private final int baseYield;                // Base harvest yield
    private final ProductionCategory category;  // Category (PLANT, CHEMICAL, etc.)

    // Growth Requirements
    private final boolean requiresLight;
    private final int minLightLevel;
    private final boolean requiresWater;
    private final boolean requiresTemperature;

    // Processing Configuration
    private final Map<String, ProcessingStageConfig> processingStages;

    // Quality System
    private final GenericQuality[] qualityTiers;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR (Private - use Builder)
    // ═══════════════════════════════════════════════════════════

    private ProductionConfig(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.colorCode = builder.colorCode;
        this.basePrice = builder.basePrice;
        this.growthTicks = builder.growthTicks;
        this.baseYield = builder.baseYield;
        this.category = builder.category;
        this.requiresLight = builder.requiresLight;
        this.minLightLevel = builder.minLightLevel;
        this.requiresWater = builder.requiresWater;
        this.requiresTemperature = builder.requiresTemperature;
        this.processingStages = new HashMap<>(builder.processingStages);
        this.qualityTiers = builder.qualityTiers;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public int getGrowthTicks() {
        return growthTicks;
    }

    public int getBaseYield() {
        return baseYield;
    }

    public ProductionCategory getCategory() {
        return category;
    }

    public boolean requiresLight() {
        return requiresLight;
    }

    public int getMinLightLevel() {
        return minLightLevel;
    }

    public boolean requiresWater() {
        return requiresWater;
    }

    public boolean requiresTemperature() {
        return requiresTemperature;
    }

    public Map<String, ProcessingStageConfig> getProcessingStages() {
        return new HashMap<>(processingStages);
    }

    public GenericQuality[] getQualityTiers() {
        return qualityTiers;
    }

    public GenericQuality getDefaultQuality() {
        return qualityTiers[qualityTiers.length / 2];  // Middle tier
    }

    // ═══════════════════════════════════════════════════════════
    // PRODUCTION CATEGORY
    // ═══════════════════════════════════════════════════════════

    public enum ProductionCategory {
        PLANT("Pflanze", "§a"),           // Tobacco, Cannabis, Coca, Poppy
        MUSHROOM("Pilz", "§d"),           // Mushroom (special growth)
        CHEMICAL("Chemikalie", "§b"),     // Meth, LSD, MDMA (synthesized)
        EXTRACT("Extrakt", "§e"),         // Cocaine, Heroin (extracted)
        PROCESSED("Verarbeitet", "§6");   // Fermented, dried, etc.

        private final String displayName;
        private final String colorCode;

        ProductionCategory(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColorCode() {
            return colorCode;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PROCESSING STAGE CONFIG
    // ═══════════════════════════════════════════════════════════

    public static class ProcessingStageConfig {
        private final String stageName;
        private final int processingTime;       // Ticks to complete
        private final String inputItem;         // Input item ID
        private final String outputItem;        // Output item ID
        private final boolean preservesQuality; // Quality carries over?

        // Resource Requirements (optional)
        private final String requiredResource;  // e.g., "diesel", "water"
        private final int resourceAmount;       // Amount needed per process

        public ProcessingStageConfig(String stageName, int processingTime,
                                    String inputItem, String outputItem,
                                    boolean preservesQuality) {
            this(stageName, processingTime, inputItem, outputItem, preservesQuality, null, 0);
        }

        public ProcessingStageConfig(String stageName, int processingTime,
                                    String inputItem, String outputItem,
                                    boolean preservesQuality,
                                    String requiredResource, int resourceAmount) {
            this.stageName = stageName;
            this.processingTime = processingTime;
            this.inputItem = inputItem;
            this.outputItem = outputItem;
            this.preservesQuality = preservesQuality;
            this.requiredResource = requiredResource;
            this.resourceAmount = resourceAmount;
        }

        public String getStageName() {
            return stageName;
        }

        public int getProcessingTime() {
            return processingTime;
        }

        public String getInputItem() {
            return inputItem;
        }

        public String getOutputItem() {
            return outputItem;
        }

        public boolean preservesQuality() {
            return preservesQuality;
        }

        public String getRequiredResource() {
            return requiredResource;
        }

        public int getResourceAmount() {
            return resourceAmount;
        }

        public boolean requiresResource() {
            return requiredResource != null && !requiredResource.isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUILDER PATTERN
    // ═══════════════════════════════════════════════════════════

    public static class Builder {
        private String id;
        private String displayName;
        private String colorCode = "§f";
        private double basePrice = 10.0;
        private int growthTicks = 3600;
        private int baseYield = 3;
        private ProductionCategory category = ProductionCategory.PLANT;

        private boolean requiresLight = true;
        private int minLightLevel = 8;
        private boolean requiresWater = false;
        private boolean requiresTemperature = false;

        private Map<String, ProcessingStageConfig> processingStages = new HashMap<>();
        private GenericQuality[] qualityTiers = GenericQuality.createStandard4TierSystem();

        public Builder(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Builder colorCode(String colorCode) {
            this.colorCode = colorCode;
            return this;
        }

        public Builder basePrice(double basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder growthTicks(int growthTicks) {
            this.growthTicks = growthTicks;
            return this;
        }

        public Builder baseYield(int baseYield) {
            this.baseYield = baseYield;
            return this;
        }

        public Builder category(ProductionCategory category) {
            this.category = category;
            return this;
        }

        public Builder requiresLight(boolean requiresLight) {
            this.requiresLight = requiresLight;
            return this;
        }

        public Builder minLightLevel(int minLightLevel) {
            this.minLightLevel = minLightLevel;
            return this;
        }

        public Builder requiresWater(boolean requiresWater) {
            this.requiresWater = requiresWater;
            return this;
        }

        public Builder requiresTemperature(boolean requiresTemperature) {
            this.requiresTemperature = requiresTemperature;
            return this;
        }

        public Builder addProcessingStage(String stageId, ProcessingStageConfig stage) {
            this.processingStages.put(stageId, stage);
            return this;
        }

        public Builder qualityTiers(GenericQuality[] qualityTiers) {
            this.qualityTiers = qualityTiers;
            return this;
        }

        public ProductionConfig build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("ID cannot be null or empty");
            }
            if (displayName == null || displayName.isEmpty()) {
                throw new IllegalStateException("Display name cannot be null or empty");
            }
            return new ProductionConfig(this);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TOSTRING
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("ProductionConfig{id='%s', name='%s', price=%.2f, growth=%d, yield=%d, category=%s}",
            id, displayName, basePrice, growthTicks, baseYield, category);
    }
}
