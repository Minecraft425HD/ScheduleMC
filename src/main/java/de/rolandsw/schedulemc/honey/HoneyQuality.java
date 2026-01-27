package de.rolandsw.schedulemc.honey;

import de.rolandsw.schedulemc.production.core.ProductionQuality;

/**
 * Honig-Qualitätsstufen
 *
 * Höhere Qualität = Höherer Preis-Multiplikator
 * Basierend auf Reinheit, Farbe und Geschmack
 */
public enum HoneyQuality implements ProductionQuality {
    RAW("Raw", "§7", 0, 0.5, "Ungefilterter Honig mit Verunreinigungen"),
    BASIC("Basic", "§f", 1, 0.8, "Einfacher gefilterter Honig"),
    GOOD("Good", "§a", 2, 1.0, "Guter Honig mit reinem Geschmack"),
    PREMIUM("Premium", "§b", 3, 1.3, "Hochwertiger Honig mit ausgezeichneter Reinheit"),
    EXCEPTIONAL("Exceptional", "§6", 4, 1.6, "Außergewöhnlicher Honig von perfekter Qualität");

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;
    private final String description;

    HoneyQuality(String displayName, String colorCode, int level, double priceMultiplier, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
        this.description = description;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getColorCode() {
        return colorCode;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ProductionQuality upgrade() {
        return switch (this) {
            case RAW -> BASIC;
            case BASIC -> GOOD;
            case GOOD -> PREMIUM;
            case PREMIUM -> EXCEPTIONAL;
            case EXCEPTIONAL -> EXCEPTIONAL; // Already at max
        };
    }

    @Override
    public ProductionQuality downgrade() {
        return switch (this) {
            case RAW -> RAW; // Already at minimum
            case BASIC -> RAW;
            case GOOD -> BASIC;
            case PREMIUM -> GOOD;
            case EXCEPTIONAL -> PREMIUM;
        };
    }

    /**
     * Ermittelt Qualität basierend auf Random-Roll und Quality-Factor
     */
    public static HoneyQuality determineQuality(double qualityFactor, java.util.Random random) {
        double roll = random.nextDouble() * qualityFactor;

        if (roll >= 1.4) return EXCEPTIONAL;    // ~6% Chance (mit perfektem Factor)
        if (roll >= 1.1) return PREMIUM;        // ~20% Chance
        if (roll >= 0.8) return GOOD;           // ~30% Chance
        if (roll >= 0.5) return BASIC;          // ~30% Chance
        return RAW;                             // ~14% Chance
    }
}
