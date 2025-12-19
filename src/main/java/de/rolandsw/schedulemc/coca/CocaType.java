package de.rolandsw.schedulemc.coca;

import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;

/**
 * Koka-Sorten mit unterschiedlichen Eigenschaften
 */
public enum CocaType implements ProductionType {
    BOLIVIANISCH("Bolivianisch", "§a", 20.0, 100, 0.8, 20),
    KOLUMBIANISCH("Kolumbianisch", "§2", 35.0, 140, 1.0, 30);

    private final String displayName;
    private final String colorCode;
    private final double seedPrice;
    private final int growthTicks; // Basis-Wachstumszeit in Ticks
    private final double waterConsumption; // Wasserverbrauch pro Wachstumsstufe
    private final int baseYield; // Basis-Ertrag beim Ernten

    CocaType(String displayName, String colorCode, double seedPrice, int growthTicks,
             double waterConsumption, int baseYield) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.seedPrice = seedPrice;
        this.growthTicks = growthTicks;
        this.waterConsumption = waterConsumption;
        this.baseYield = baseYield;
    }

    public String getDisplayName() {
        return displayName;
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
        return colorCode + displayName;
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
