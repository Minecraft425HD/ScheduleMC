package de.rolandsw.schedulemc.coffee;

/**
 * Mahlgrade für Kaffee
 */
public enum CoffeeGrindSize {
    COARSE("Coarse Grind", "§e", "French Press, Cold Brew"),
    MEDIUM("Medium Grind", "§6", "Drip Coffee, Pour Over"),
    FINE("Fine Grind", "§c", "Espresso Machines"),
    EXTRA_FINE("Extra Fine Grind", "§4", "Turkish Coffee");

    private final String displayName;
    private final String colorCode;
    private final String bestFor;

    CoffeeGrindSize(String displayName, String colorCode, String bestFor) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.bestFor = bestFor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getBestFor() {
        return bestFor;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }
}
