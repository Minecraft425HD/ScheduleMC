package de.rolandsw.schedulemc.production.core;

/**
 * Verschiedene Topf-Typen mit unterschiedlichen Erde-Kapazitäten
 *
 * Erde-System:
 * - Jede Pflanze benötigt 33 Erde zum Wachsen
 * - TERRACOTTA: 99 Erde = 3 Pflanzen
 * - CERAMIC: 132 Erde = 4 Pflanzen
 * - IRON: 165 Erde = 5 Pflanzen
 * - GOLDEN: 165 Erde = 5 Pflanzen + automatischer Qualitäts-Boost
 */
public enum PotType {
    TERRACOTTA("Terracotta-Topf", "§6", 20.0, 100, 99, 1.0, false),   // 3 Säcke = 3 Pflanzen
    CERAMIC("Keramik-Topf", "§f", 40.0, 100, 132, 1.0, false),        // 4 Säcke = 4 Pflanzen
    IRON("Eisen-Topf", "§7", 80.0, 100, 165, 1.0, false),             // 5 Säcke = 5 Pflanzen
    GOLDEN("Gold-Topf", "§e§l", 150.0, 100, 165, 1.0, true);          // 5 Säcke = 5 Pflanzen + Qualität+1

    /** Erde die pro Pflanze verbraucht wird */
    public static final int SOIL_PER_PLANT = 33;

    private final String displayName;
    private final String colorCode;
    private final double price;
    private final int waterCapacity;
    private final int soilCapacity;
    private final double consumptionMultiplier;
    private final boolean hasQualityBoost;

    PotType(String displayName, String colorCode, double price, int waterCapacity,
            int soilCapacity, double consumptionMultiplier, boolean hasQualityBoost) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.price = price;
        this.waterCapacity = waterCapacity;
        this.soilCapacity = soilCapacity;
        this.consumptionMultiplier = consumptionMultiplier;
        this.hasQualityBoost = hasQualityBoost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getPrice() {
        return price;
    }

    public int getWaterCapacity() {
        return waterCapacity;
    }

    public int getSoilCapacity() {
        return soilCapacity;
    }

    public double getConsumptionMultiplier() {
        return consumptionMultiplier;
    }

    /**
     * Gibt zurück ob dieser Topf automatisch +1 Qualität bei Ernte gibt
     */
    public boolean hasQualityBoost() {
        return hasQualityBoost;
    }

    /**
     * Berechnet wie viele Pflanzen in diesem Topf wachsen können
     */
    public int getMaxPlants() {
        return soilCapacity / SOIL_PER_PLANT;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    /**
     * Berechnet tatsächlichen Wasserverbrauch
     */
    public double calculateWaterConsumption(double baseConsumption) {
        return baseConsumption * consumptionMultiplier;
    }

    /**
     * Berechnet tatsächlichen Erdeverbrauch
     */
    public double calculateSoilConsumption(double baseConsumption) {
        return baseConsumption * consumptionMultiplier;
    }
}
