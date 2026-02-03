package de.rolandsw.schedulemc.beer;

import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.economy.ItemCategory;

/**
 * Biertypen mit verschiedenen Eigenschaften
 *
 * Eigenschaften:
 * - Name und Color Code (für Display)
 * - Basis-Preis pro Liter
 * - Reifungszeit in Tagen (Minecraft-Tage)
 * - Qualitätsfaktor (Chance auf höhere Qualität)
 * - Alkoholgehalt in Prozent
 */
public enum BeerType implements ProductionType {
    // Verschiedene Biersorten
    PILSNER("Pilsner", "§e", 8.0, 30, 1.0, 4.8),
    WEIZEN("Weizen", "§6", 10.0, 35, 1.1, 5.4),
    ALE("Ale", "§c", 12.0, 40, 1.2, 6.5),
    STOUT("Stout", "§8", 15.0, 50, 1.3, 7.2);

    private final String displayName;
    private final String colorCode;
    private final double basePricePerLiter;
    private final int agingTimeDays;
    private final double qualityFactor;
    private final double alcoholPercentage;

    BeerType(String displayName, String colorCode, double basePricePerLiter,
             int agingTimeDays, double qualityFactor, double alcoholPercentage) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.basePricePerLiter = basePricePerLiter;
        this.agingTimeDays = agingTimeDays;
        this.qualityFactor = qualityFactor;
        this.alcoholPercentage = alcoholPercentage;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getColorCode() {
        return colorCode;
    }

    public double getBasePricePerLiter() {
        return basePricePerLiter;
    }

    public int getAgingTimeDays() {
        return agingTimeDays;
    }

    public double getQualityFactor() {
        return qualityFactor;
    }

    public double getAlcoholPercentage() {
        return alcoholPercentage;
    }

    @Override
    public String getProductId() {
        return "BEER_" + name();
    }

    @Override
    public ItemCategory getItemCategory() {
        return ItemCategory.BEER;
    }

    @Override
    public double getBasePrice() {
        return basePricePerLiter;
    }

    @Override
    public int getGrowthTicks() {
        return agingTimeDays * 24000; // Convert days to ticks (1 MC day = 24000 ticks)
    }

    @Override
    public int getBaseYield() {
        return 10; // Base yield in liters
    }

    @Override
    public double calculatePrice(ProductionQuality quality, int amount) {
        return basePricePerLiter * quality.getPriceMultiplier() * amount;
    }

    /**
     * Berechnet Temperatur-Bonus basierend auf aktueller Biom-Temperatur
     * Bier bevorzugt kühle bis moderate Temperaturen (8-15°C)
     */
    public double getTemperatureBonus(float biomeTemperature) {
        // biomeTemperature ist 0.0 bis 2.0 (kalt bis heiß)
        float tempCelsius = biomeTemperature * 20; // Ungefähre Celsius-Konvertierung

        // Bier bevorzugt kühle Lagerung (8-15°C)
        if (tempCelsius >= 8 && tempCelsius <= 15) return 1.3; // Perfekte Bedingungen: +30%
        if (tempCelsius >= 5 && tempCelsius <= 20) return 1.15; // Gute Bedingungen: +15%
        if (tempCelsius >= 0 && tempCelsius <= 25) return 1.0; // Normale Bedingungen
        return 0.7; // Schlechte Bedingungen: -30%
    }

    /**
     * Prüft ob dieser Biertyp ein helles Bier ist
     */
    public boolean isLightBeer() {
        return this == PILSNER || this == WEIZEN;
    }

    /**
     * Prüft ob dieser Biertyp ein dunkles Bier ist
     */
    public boolean isDarkBeer() {
        return this == STOUT;
    }

    /**
     * Prüft ob dieser Biertyp ein Ale ist
     */
    public boolean isAle() {
        return this == ALE;
    }

    /**
     * Prüft ob dieser Biertyp ein Weizenbier ist
     */
    public boolean isWheatBeer() {
        return this == WEIZEN;
    }

    /**
     * Gibt die Bitterkeit zurück (IBU - International Bitterness Units)
     */
    public int getBitterness() {
        return switch (this) {
            case PILSNER -> 35;
            case WEIZEN -> 15;
            case ALE -> 40;
            case STOUT -> 45;
        };
    }

    /**
     * Gibt die Stammwürze zurück (in Grad Plato)
     */
    public double getOriginalGravity() {
        return switch (this) {
            case PILSNER -> 11.5;
            case WEIZEN -> 12.5;
            case ALE -> 13.5;
            case STOUT -> 16.0;
        };
    }
}
