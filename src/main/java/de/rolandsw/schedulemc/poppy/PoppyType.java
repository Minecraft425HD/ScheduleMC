package de.rolandsw.schedulemc.poppy;

import de.rolandsw.schedulemc.economy.ItemCategory;
import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Mohn-Sorten mit unterschiedlichen Eigenschaften
 */
public enum PoppyType implements ProductionType {
    AFGHANISCH("§4", 50.0, 160, 1.2, 6, 1.5),  // Höchste Potenz, langsam
    TUERKISCH("§6", 35.0, 120, 1.0, 6, 1.0),   // Ausgewogen
    INDISCH("§5", 20.0, 80, 0.8, 6, 0.8);      // Schnell, niedrige Potenz

    private final String colorCode;
    private final double seedPrice;
    private final int growthTicks;
    private final double waterConsumption;
    private final int baseYield;
    private final double potencyMultiplier;

    PoppyType(String colorCode, double seedPrice, int growthTicks,
              double waterConsumption, int baseYield, double potencyMultiplier) {
        this.colorCode = colorCode;
        this.seedPrice = seedPrice;
        this.growthTicks = growthTicks;
        this.waterConsumption = waterConsumption;
        this.baseYield = baseYield;
        this.potencyMultiplier = potencyMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.poppy_type." + this.name().toLowerCase()).getString();
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getColoredName() {
        return colorCode + getDisplayName();
    }

    public double getSeedPrice() {
        return seedPrice;
    }

    public int getGrowthTicks() {
        return growthTicks;
    }

    public double getWaterConsumption() {
        return waterConsumption;
    }

    public int getBaseYield() {
        return baseYield;
    }

    public double getPotencyMultiplier() {
        return potencyMultiplier;
    }

    @Override
    public String getProductId() {
        return "POPPY_" + name();
    }

    @Override
    public ItemCategory getItemCategory() {
        return ItemCategory.HEROIN;
    }

    @Override
    public double getBasePrice() {
        return seedPrice / 10.0;
    }

    @Override
    public double calculatePrice(ProductionQuality quality, int amount) {
        double basePrice = seedPrice * 3.5;
        return basePrice * potencyMultiplier * quality.getPriceMultiplier() * amount;
    }
}
