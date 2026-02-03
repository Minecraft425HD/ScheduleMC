package de.rolandsw.schedulemc.coca;

import de.rolandsw.schedulemc.economy.ItemCategory;
import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.network.chat.Component;

/**
 * Koka-Sorten mit unterschiedlichen Eigenschaften
 */
public enum CocaType implements ProductionType {
    BOLIVIANISCH("§a", 20.0, 100, 0.8, 6),
    KOLUMBIANISCH("§2", 35.0, 140, 1.0, 6),
    PERUANISCH("§6", 27.5, 120, 0.9, 6);

    private final String colorCode;
    private final double seedPrice;
    private final int growthTicks; // Basis-Wachstumszeit in Ticks
    private final double waterConsumption; // Wasserverbrauch pro Wachstumsstufe
    private final int baseYield; // Basis-Ertrag beim Ernten

    CocaType(String colorCode, double seedPrice, int growthTicks,
             double waterConsumption, int baseYield) {
        this.colorCode = colorCode;
        this.seedPrice = seedPrice;
        this.growthTicks = growthTicks;
        this.waterConsumption = waterConsumption;
        this.baseYield = baseYield;
    }

    public String getDisplayName() {
        return Component.translatable("enum.coca_type." + this.name().toLowerCase()).getString();
    }

    public String getColorCode() {
        return colorCode;
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

    public String getColoredName() {
        return colorCode + getDisplayName();
    }

    @Override
    public String getProductId() {
        return "COCA_" + name();
    }

    @Override
    public ItemCategory getItemCategory() {
        return ItemCategory.COCAINE;
    }

    /**
     * Berechnet Verkaufspreis für Kokain
     */
    public double calculatePrice(TobaccoQuality quality, int amount) {
        double basePrice = seedPrice * 3.0; // Basis = 3x Saatgutpreis (höher als Tabak)
        return basePrice * quality.getPriceMultiplier() * amount;
    }

    /**
     * Gibt den Basispreis pro Gramm für verpacktes Kokain zurück
     */
    @Override
    public double getBasePrice() {
        // Bolivianisch: 20.0 / 10 = 2.00€/g
        // Kolumbianisch: 35.0 / 10 = 3.50€/g
        return seedPrice / 10.0;
    }

    @Override
    public double calculatePrice(ProductionQuality quality, int amount) {
        double basePrice = seedPrice * 3.0;
        return basePrice * quality.getPriceMultiplier() * amount;
    }
}
