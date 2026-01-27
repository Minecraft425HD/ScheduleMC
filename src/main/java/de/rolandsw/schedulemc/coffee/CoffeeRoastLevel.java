package de.rolandsw.schedulemc.coffee;

/**
 * Röstgrade für Kaffee
 */
public enum CoffeeRoastLevel {
    LIGHT("Light Roast", "§e", 1.0, 200, "Sauer, fruchtig, heller Körper"),
    MEDIUM("Medium Roast", "§6", 1.2, 220, "Balanced, süß, karamellisiert"),
    DARK("Dark Roast", "§c", 1.4, 240, "Kräftig, bitter, voller Körper"),
    ESPRESSO("Espresso Roast", "§4§l", 1.6, 260, "Sehr dunkel, ölig, intensiv");

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
        return colorCode + displayName;
    }
}
