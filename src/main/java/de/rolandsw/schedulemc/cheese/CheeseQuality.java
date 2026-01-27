package de.rolandsw.schedulemc.cheese;

/**
 * Kase-Qualitatsstufen
 *
 * Hohere Qualitat = Hoherer Preis-Multiplikator
 */
public enum CheeseQuality {
    POOR("Minderwertig", "§7", 0.7),
    STANDARD("Standard", "§f", 1.0),
    GOOD("Gut", "§a", 1.5),
    PREMIUM("Premium", "§b", 2.5),
    ARTISAN("Handwerklich", "§d§l", 4.0);

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;

    CheeseQuality(String displayName, String colorCode, double priceMultiplier) {
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
     * Ermittelt Qualitat basierend auf Random-Roll und Quality-Factor
     */
    public static CheeseQuality determineQuality(double qualityFactor, java.util.Random random) {
        double roll = random.nextDouble() * qualityFactor;

        if (roll >= 0.95) return ARTISAN;      // 5% Chance (mit perfektem Factor)
        if (roll >= 0.80) return PREMIUM;      // 15% Chance
        if (roll >= 0.55) return GOOD;         // 25% Chance
        if (roll >= 0.25) return STANDARD;     // 30% Chance
        return POOR;                           // 25% Chance
    }
}
