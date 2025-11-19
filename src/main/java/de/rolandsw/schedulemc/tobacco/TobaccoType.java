package de.rolandsw.schedulemc.tobacco;

/**
 * Tabaksorten mit unterschiedlichen Eigenschaften
 */
public enum TobaccoType {
    VIRGINIA("Virginia", "§e", 10.0, 100, 0.8, 20),
    BURLEY("Burley", "§6", 15.0, 120, 0.9, 25),
    ORIENTAL("Oriental", "§d", 20.0, 140, 1.0, 30),
    HAVANA("Havana", "§c§l", 30.0, 160, 1.2, 40);
    
    private final String displayName;
    private final String colorCode;
    private final double seedPrice;
    private final int growthTicks; // Basis-Wachstumszeit in Ticks
    private final double waterConsumption; // Wasserverbrauch pro Wachstumsstufe
    private final int baseYield; // Basis-Ertrag beim Ernten
    
    TobaccoType(String displayName, String colorCode, double seedPrice, int growthTicks, 
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
     * Berechnet Verkaufspreis für fermentierten Tabak
     */
    public double calculatePrice(TobaccoQuality quality, int amount) {
        double basePrice = seedPrice * 2.0; // Basis = 2x Saatgutpreis
        return basePrice * quality.getPriceMultiplier() * amount;
    }
}
