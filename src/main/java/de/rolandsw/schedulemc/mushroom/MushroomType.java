package de.rolandsw.schedulemc.mushroom;

/**
 * Pilz-Sorten mit unterschiedlichen Eigenschaften
 */
public enum MushroomType {
    // Name, Farbe, Preis, Wachstum, Wasser, Ertrag, Potenz, Flushes, MaxLicht(Inkub), MaxLicht(Frucht)
    CUBENSIS("Psilocybe Cubensis", "§6", 30.0, 100, 1.0, 20, 1.0, 4, 4, 7),       // Klassiker, ausgewogen
    AZURESCENS("Psilocybe Azurescens", "§9", 60.0, 180, 1.5, 12, 2.0, 3, 3, 5),   // Höchste Potenz, schwer
    MEXICANA("Psilocybe Mexicana", "§e", 20.0, 60, 0.7, 16, 0.6, 5, 5, 8);        // Schnell, tolerant

    private final String displayName;
    private final String colorCode;
    private final double sporePrice;
    private final int growthTicks;
    private final double waterConsumption;
    private final int baseYield;
    private final double potencyMultiplier;
    private final int maxFlushes;
    private final int maxLightIncubation;  // Maximales Lichtlevel für Inkubation
    private final int maxLightFruiting;    // Maximales Lichtlevel für Fruchtung

    MushroomType(String displayName, String colorCode, double sporePrice,
                 int growthTicks, double waterConsumption, int baseYield,
                 double potencyMultiplier, int maxFlushes, int maxLightIncubation, int maxLightFruiting) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.sporePrice = sporePrice;
        this.growthTicks = growthTicks;
        this.waterConsumption = waterConsumption;
        this.baseYield = baseYield;
        this.potencyMultiplier = potencyMultiplier;
        this.maxFlushes = maxFlushes;
        this.maxLightIncubation = maxLightIncubation;
        this.maxLightFruiting = maxLightFruiting;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    public double getSporePrice() {
        return sporePrice;
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

    public double getPotencyMultiplier() {
        return potencyMultiplier;
    }

    public int getMaxFlushes() {
        return maxFlushes;
    }

    public int getMaxLightIncubation() {
        return maxLightIncubation;
    }

    public int getMaxLightFruiting() {
        return maxLightFruiting;
    }

    public String getRegistryName() {
        return name().toLowerCase();
    }
}
