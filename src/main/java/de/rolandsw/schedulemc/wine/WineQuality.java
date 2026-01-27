package de.rolandsw.schedulemc.wine;

/**
 * Wein-Qualitätsstufen nach deutschem Weingesetz
 *
 * Höhere Qualität = Höherer Preis-Multiplikator
 */
public enum WineQuality {
    TAFELWEIN("Tafelwein", "§7", 0.7),              // Einfachste Qualität
    LANDWEIN("Landwein", "§f", 1.0),                // Standard-Qualität
    QUALITAETSWEIN("Qualitätswein", "§a", 1.5),     // Gute Qualität
    PRAEDIKATSWEIN("Prädikatswein", "§b", 2.5),     // Sehr gute Qualität
    EISWEIN("Eiswein", "§d§l", 5.0);                // Legendäre Qualität (sehr selten)

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;

    WineQuality(String displayName, String colorCode, double priceMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return colorCode + displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    /**
     * Ermittelt Qualität basierend auf Random-Roll und Quality-Factor
     */
    public static WineQuality determineQuality(double qualityFactor, java.util.Random random) {
        double roll = random.nextDouble() * qualityFactor;

        if (roll >= 0.95) return EISWEIN;           // 5% Chance (mit perfektem Factor)
        if (roll >= 0.80) return PRAEDIKATSWEIN;    // 15% Chance
        if (roll >= 0.55) return QUALITAETSWEIN;    // 25% Chance
        if (roll >= 0.25) return LANDWEIN;          // 30% Chance
        return TAFELWEIN;                           // 25% Chance
    }
}
