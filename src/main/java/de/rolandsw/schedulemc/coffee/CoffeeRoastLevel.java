package de.rolandsw.schedulemc.coffee;

import net.minecraft.network.chat.Component;

/**
 * Röstgrade für Kaffee
 */
public enum CoffeeRoastLevel {
    LIGHT("coffee.roast.light.name", "§e", 1.0, 200, "coffee.roast.light.flavor"),
    MEDIUM("coffee.roast.medium.name", "§6", 1.2, 220, "coffee.roast.medium.flavor"),
    DARK("coffee.roast.dark.name", "§c", 1.4, 240, "coffee.roast.dark.flavor"),
    ESPRESSO("coffee.roast.espresso.name", "§4§l", 1.6, 260, "coffee.roast.espresso.flavor");

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;
    private final int roastTemperature;  // In °C
    private final String flavorProfile;

    CoffeeRoastLevel(String displayName, String colorCode, double priceMultiplier,
                     int roastTemperature, String flavorProfile) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.priceMultiplier = priceMultiplier;
        this.roastTemperature = roastTemperature;
        this.flavorProfile = flavorProfile;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public int getRoastTemperature() {
        return roastTemperature;
    }

    public String getFlavorProfile() {
        return flavorProfile;
    }

    public String getColoredName() {
        return colorCode + Component.translatable(displayName).getString();
    }
}
