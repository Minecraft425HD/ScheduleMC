package de.rolandsw.schedulemc.coffee;

/**
 * Verarbeitungsmethoden für Kaffee
 */
public enum CoffeeProcessingMethod {
    WET("Wet Process", "§b", 1.3, "Clean, bright, acidic"),
    DRY("Dry Process", "§e", 1.1, "Fruity, full-bodied, sweet");

    private final String displayName;
    private final String colorCode;
    private final double qualityMultiplier;
    private final String flavorProfile;

    CoffeeProcessingMethod(String displayName, String colorCode,
                           double qualityMultiplier, String flavorProfile) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.qualityMultiplier = qualityMultiplier;
        this.flavorProfile = flavorProfile;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getQualityMultiplier() {
        return qualityMultiplier;
    }

    public String getFlavorProfile() {
        return flavorProfile;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }
}
