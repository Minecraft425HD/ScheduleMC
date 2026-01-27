package de.rolandsw.schedulemc.honey;

import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;

/**
 * Honigtypen mit verschiedenen Eigenschaften
 *
 * Eigenschaften:
 * - Name und Color Code (für Display)
 * - Basis-Preis pro Kilogramm
 * - Reifungszeit in Tagen (Minecraft-Tage)
 * - Qualitätsfaktor (Chance auf höhere Qualität)
 */
public enum HoneyType implements ProductionType {
    // Verschiedene Honigsorten basierend auf Blütenarten
    ACACIA("Acacia", "§e", 12.0, 30, 0.9),
    WILDFLOWER("Wildflower", "§6", 15.0, 60, 1.0),
    FOREST("Forest", "§c", 20.0, 90, 1.2),
    MANUKA("Manuka", "§d", 35.0, 120, 1.5);

    private final String displayName;
    private final String colorCode;
    private final double basePricePerKg;
    private final int agingTimeDays;
    private final double qualityFactor;

    HoneyType(String displayName, String colorCode, double basePricePerKg,
              int agingTimeDays, double qualityFactor) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.basePricePerKg = basePricePerKg;
        this.agingTimeDays = agingTimeDays;
        this.qualityFactor = qualityFactor;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getColorCode() {
        return colorCode;
    }

    public double getBasePricePerKg() {
        return basePricePerKg;
    }

    public int getAgingTimeDays() {
        return agingTimeDays;
    }

    public double getQualityFactor() {
        return qualityFactor;
    }

    @Override
    public double getBasePrice() {
        return basePricePerKg;
    }

    @Override
    public int getGrowthTicks() {
        return agingTimeDays * 24000; // Convert days to ticks
    }

    @Override
    public int getBaseYield() {
        return 5; // Base yield in kg
    }

    @Override
    public double calculatePrice(ProductionQuality quality, int amount) {
        return basePricePerKg * quality.getPriceMultiplier() * amount;
    }

    /**
     * Berechnet Temperatur-Bonus basierend auf aktueller Biom-Temperatur
     */
    public double getTemperatureBonus(float biomeTemperature) {
        // biomeTemperature ist 0.0 bis 2.0 (kalt bis heiß)
        float tempCelsius = biomeTemperature * 20; // Ungefähre Celsius-Konvertierung

        // Bienen bevorzugen moderate Temperaturen (15-25°C)
        if (tempCelsius >= 15 && tempCelsius <= 25) return 1.2; // Perfekte Bedingungen: +20%
        if (tempCelsius >= 10 && tempCelsius <= 30) return 1.1; // Gute Bedingungen: +10%
        if (tempCelsius >= 5 && tempCelsius <= 35) return 1.0; // Normale Bedingungen
        return 0.8; // Schlechte Bedingungen: -20%
    }

    /**
     * Prüft ob dieser Honigtyp von Akazienbäumen stammt
     */
    public boolean isAcaciaHoney() {
        return this == ACACIA;
    }

    /**
     * Prüft ob dieser Honigtyp von Wildblumen stammt
     */
    public boolean isWildflowerHoney() {
        return this == WILDFLOWER;
    }

    /**
     * Prüft ob dieser Honigtyp aus dem Wald stammt
     */
    public boolean isForestHoney() {
        return this == FOREST;
    }

    /**
     * Prüft ob dieser Honigtyp Manuka ist (selten & wertvoll)
     */
    public boolean isManukaHoney() {
        return this == MANUKA;
    }
}
