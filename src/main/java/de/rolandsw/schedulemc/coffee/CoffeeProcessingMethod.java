package de.rolandsw.schedulemc.coffee;

import net.minecraft.network.chat.Component;

/**
 * Verarbeitungsmethoden für Kaffee
 */
public enum CoffeeProcessingMethod {
    WET("coffee.process.wet.name", "§b", 1.3, "coffee.process.wet.flavor"),
    DRY("coffee.process.dry.name", "§e", 1.1, "coffee.process.dry.flavor");

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
        return colorCode + Component.translatable(displayName).getString();
    }
}
