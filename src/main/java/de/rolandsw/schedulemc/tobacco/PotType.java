package de.rolandsw.schedulemc.tobacco;

/**
 * Verschiedene Topf-Typen mit gleichen Kapazitäten (nur optische Unterschiede)
 */
public enum PotType {
    TERRACOTTA("Terracotta-Topf", "§6", 20.0, 100, 50, 1.0),
    CERAMIC("Keramik-Topf", "§f", 40.0, 100, 50, 1.0),
    IRON("Eisen-Topf", "§7", 80.0, 100, 50, 1.0),
    GOLDEN("Gold-Topf", "§e§l", 150.0, 100, 50, 1.0);
    
    private final String displayName;
    private final String colorCode;
    private final double price;
    private final int waterCapacity; // Maximale Wasserkapazität
    private final int soilCapacity; // Maximale Erdekapazität
    private final double consumptionMultiplier; // Verbrauchsrate (1.0 = normal, 0.5 = halbe Rate)
    
    PotType(String displayName, String colorCode, double price, int waterCapacity, 
            int soilCapacity, double consumptionMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.price = price;
        this.waterCapacity = waterCapacity;
        this.soilCapacity = soilCapacity;
        this.consumptionMultiplier = consumptionMultiplier;
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
