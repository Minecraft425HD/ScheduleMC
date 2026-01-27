package de.rolandsw.schedulemc.wine;

import de.rolandsw.schedulemc.production.core.ProductionType;

/**
 * Weintypen mit verschiedenen Eigenschaften
 *
 * Eigenschaften:
 * - Name und Color Code (für Display)
 * - Basis-Preis pro Liter
 * - Wachstumszeit in Tagen (Minecraft-Tage)
 * - Qualitätsfaktor (Chance auf höhere Qualität)
 * - Ertrag pro Pflanze
 * - Optimale Temperatur (Klima-Bonus)
 */
public enum WineType implements ProductionType {
    // Weißweine
    RIESLING("Riesling", "§e", 15.0, 120, 0.8, 10, 18),
    CHARDONNAY("Chardonnay", "§6", 22.0, 110, 0.9, 8, 20),

    // Rotweine
    SPAETBURGUNDER("Spätburgunder", "§c", 28.0, 140, 0.85, 9, 16),
    MERLOT("Merlot", "§4", 35.0, 130, 0.95, 7, 22);

    private final String displayName;
    private final String colorCode;
    private final double basePricePerLiter;
    private final int growthTimeDays;
    private final double qualityFactor;
    private final int yieldPerPlant;
    private final int optimalTemperature;

    WineType(String displayName, String colorCode, double basePricePerLiter,
             int growthTimeDays, double qualityFactor, int yieldPerPlant, int optimalTemperature) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.basePricePerLiter = basePricePerLiter;
        this.growthTimeDays = growthTimeDays;
        this.qualityFactor = qualityFactor;
        this.yieldPerPlant = yieldPerPlant;
        this.optimalTemperature = optimalTemperature;
    }

    @Override
    public String getDisplayName() {
        return colorCode + displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getBasePricePerLiter() {
        return basePricePerLiter;
    }

    public int getGrowthTimeDays() {
        return growthTimeDays;
    }

    public double getQualityFactor() {
        return qualityFactor;
    }

    public int getYieldPerPlant() {
        return yieldPerPlant;
    }

    public int getOptimalTemperature() {
        return optimalTemperature;
    }

    public boolean isRedWine() {
        return this == SPAETBURGUNDER || this == MERLOT;
    }

    public boolean isWhiteWine() {
        return this == RIESLING || this == CHARDONNAY;
    }

    /**
     * Berechnet Temperatur-Bonus basierend auf aktueller Biom-Temperatur
     */
    public double getTemperatureBonus(float biomeTemperature) {
        // biomeTemperature ist 0.0 bis 2.0 (kalt bis heiß)
        float tempCelsius = biomeTemperature * 20; // Ungefähre Celsius-Konvertierung
        float diff = Math.abs(tempCelsius - optimalTemperature);

        if (diff <= 3) return 1.2; // Perfekte Bedingungen: +20%
        if (diff <= 6) return 1.1; // Gute Bedingungen: +10%
        if (diff <= 10) return 1.0; // Normale Bedingungen
        return 0.8; // Schlechte Bedingungen: -20%
    }
}
