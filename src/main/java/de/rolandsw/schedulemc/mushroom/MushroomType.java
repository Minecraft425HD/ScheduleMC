package de.rolandsw.schedulemc.mushroom;

/**
 * Pilz-Sorten mit unterschiedlichen Eigenschaften
 */
public enum MushroomType {
    CUBENSIS("Psilocybe Cubensis", "§6", 30.0, 100, 1.0, 20, 1.0),      // Klassiker, ausgewogen
    AZURESCENS("Psilocybe Azurescens", "§9", 60.0, 180, 1.5, 15, 2.0),  // Höchste Potenz, langsam
    SEMILANCEATA("Psilocybe Semilanceata", "§a", 20.0, 60, 0.7, 25, 0.6); // Schnell, niedrige Potenz

    private final String displayName;
    private final String colorCode;
    private final double sporePrice;
    private final int growthTicks;
    private final double waterConsumption;
    private final int baseYield;
    private final double potencyMultiplier;

    MushroomType(String displayName, String colorCode, double sporePrice,
                 int growthTicks, double waterConsumption, int baseYield, double potencyMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.sporePrice = sporePrice;
        this.growthTicks = growthTicks;
        this.waterConsumption = waterConsumption;
        this.baseYield = baseYield;
        this.potencyMultiplier = potencyMultiplier;
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

    public String getRegistryName() {
        return name().toLowerCase();
    }
}
