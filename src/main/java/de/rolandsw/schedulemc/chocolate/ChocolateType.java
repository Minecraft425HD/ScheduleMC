package de.rolandsw.schedulemc.chocolate;

import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;

/**
 * Schokoladentypen mit verschiedenen Eigenschaften
 *
 * Eigenschaften:
 * - Name und Color Code (für Display)
 * - Basis-Preis pro Kilogramm
 * - Reifungszeit in Tagen (Minecraft-Tage)
 * - Qualitätsfaktor (Chance auf höhere Qualität)
 */
public enum ChocolateType implements ProductionType {
    // Verschiedene Schokoladensorten
    DARK("Dark Chocolate", "§6", 20.0, 60, 1.3),
    MILK("Milk Chocolate", "§e", 15.0, 30, 1.0),
    WHITE("White Chocolate", "§f", 12.0, 20, 0.9),
    RUBY("Ruby Chocolate", "§d", 30.0, 40, 1.5);

    private final String displayName;
    private final String colorCode;
    private final double basePricePerKg;
    private final int agingTimeDays;
    private final double qualityFactor;

    ChocolateType(String displayName, String colorCode, double basePricePerKg,
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
        return 10; // Base yield in kg
    }

    @Override
    public double calculatePrice(ProductionQuality quality, int amount) {
        return basePricePerKg * quality.getPriceMultiplier() * amount;
    }

    /**
     * Berechnet Temperatur-Bonus basierend auf aktueller Biom-Temperatur
     * Kakao bevorzugt tropische Temperaturen (20-30°C)
     */
    public double getTemperatureBonus(float biomeTemperature) {
        // biomeTemperature ist 0.0 bis 2.0 (kalt bis heiß)
        float tempCelsius = biomeTemperature * 20; // Ungefähre Celsius-Konvertierung

        // Kakao bevorzugt tropische Temperaturen (20-30°C)
        if (tempCelsius >= 20 && tempCelsius <= 30) return 1.3; // Perfekte Bedingungen: +30%
        if (tempCelsius >= 15 && tempCelsius <= 35) return 1.15; // Gute Bedingungen: +15%
        if (tempCelsius >= 10 && tempCelsius <= 40) return 1.0; // Normale Bedingungen
        return 0.7; // Schlechte Bedingungen: -30%
    }

    /**
     * Prüft ob dieser Schokoladentyp dunkle Schokolade ist
     */
    public boolean isDarkChocolate() {
        return this == DARK;
    }

    /**
     * Prüft ob dieser Schokoladentyp Milchschokolade ist
     */
    public boolean isMilkChocolate() {
        return this == MILK;
    }

    /**
     * Prüft ob dieser Schokoladentyp weiße Schokolade ist
     */
    public boolean isWhiteChocolate() {
        return this == WHITE;
    }

    /**
     * Prüft ob dieser Schokoladentyp Ruby Schokolade ist (selten & wertvoll)
     */
    public boolean isRubyChocolate() {
        return this == RUBY;
    }

    /**
     * Gibt den Kakaoanteil zurück (in Prozent)
     */
    public int getCocoaPercentage() {
        return switch (this) {
            case DARK -> 70;
            case MILK -> 30;
            case WHITE -> 0;
            case RUBY -> 47;
        };
    }
}
