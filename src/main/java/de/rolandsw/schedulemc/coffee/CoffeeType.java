package de.rolandsw.schedulemc.coffee;

import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;

/**
 * Kaffeesorten mit unterschiedlichen Eigenschaften
 *
 * Arabica - mild, aromatisch, säuerlich
 * Robusta - stark, bitter, koffeinreich
 * Liberica - holzig, einzigartig, selten
 * Excelsa - fruchtig, komplex, exotisch
 */
public enum CoffeeType implements ProductionType {
    ARABICA("Arabica", "§e", 12.0, 140, 0.7, 8, 1800),      // Mild, beliebt
    ROBUSTA("Robusta", "§6", 18.0, 120, 0.9, 10, 1600),     // Stark, robust
    LIBERICA("Liberica", "§d", 25.0, 160, 0.8, 6, 2000),    // Selten, holzig
    EXCELSA("Excelsa", "§5§l", 35.0, 180, 1.0, 7, 2200);    // Exotisch, fruchtig

    private final String displayName;
    private final String colorCode;
    private final double seedlingPrice;      // Preis für Setzlinge
    private final int growthTicks;            // Basis-Wachstumszeit in Ticks
    private final double waterConsumption;    // Wasserverbrauch pro Wachstumsstufe
    private final int baseYield;              // Basis-Ertrag beim Ernten (Kirschen)
    private final int optimalAltitude;        // Optimale Höhenlage (Y-Level)

    CoffeeType(String displayName, String colorCode, double seedlingPrice,
               int growthTicks, double waterConsumption, int baseYield, int optimalAltitude) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.seedlingPrice = seedlingPrice;
        this.growthTicks = growthTicks;
        this.waterConsumption = waterConsumption;
        this.baseYield = baseYield;
        this.optimalAltitude = optimalAltitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getSeedlingPrice() {
        return seedlingPrice;
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

    public int getOptimalAltitude() {
        return optimalAltitude;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    /**
     * Berechnet Verkaufspreis für gerösteten Kaffee
     */
    @Override
    public double calculatePrice(ProductionQuality quality, int amount) {
        double basePrice = seedlingPrice * 3.0; // Basis = 3x Setzlingspreis
        return basePrice * quality.getPriceMultiplier() * amount;
    }

    /**
     * Gibt den Basispreis pro Gramm für gerösteten Kaffee zurück
     */
    public double getBasePrice() {
        // Basis: Setzlingspreis / 15 (da baseYield ca. 6-10 Kirschen -> ~30-50g Bohnen)
        // Arabica: 12.0 / 15 = 0.80€/g
        // Robusta: 18.0 / 15 = 1.20€/g
        // Liberica: 25.0 / 15 = 1.67€/g
        // Excelsa: 35.0 / 15 = 2.33€/g
        return seedlingPrice / 15.0;
    }

    /**
     * Berechnet Altitude-Bonus (höher = besser für Qualität)
     */
    public double getAltitudeBonus(int currentY) {
        int difference = Math.abs(currentY - optimalAltitude);
        if (difference <= 10) return 1.2;  // +20% Qualität
        if (difference <= 30) return 1.0;  // Normal
        if (difference <= 50) return 0.8;  // -20% Qualität
        return 0.6; // -40% Qualität
    }
}
