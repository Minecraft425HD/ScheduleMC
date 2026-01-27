package de.rolandsw.schedulemc.beer;

import de.rolandsw.schedulemc.production.core.ProductionQuality;

/**
 * Bier-Qualitätsstufen
 *
 * Höhere Qualität = Höherer Preis-Multiplikator
 * Basierend auf Brauqualität, Reinheit und Geschmack
 */
public enum BeerQuality implements ProductionQuality {
    RAW("Raw", "§7", 0, 0.5, "Unreifes Bier mit schlechter Qualität"),
    BASIC("Basic", "§f", 1, 0.8, "Einfaches Bier mit Standard-Qualität"),
    GOOD("Good", "§a", 2, 1.0, "Gutes Bier mit sauberem Geschmack"),
    PREMIUM("Premium", "§b", 3, 1.3, "Hochwertiges Bier mit ausgezeichnetem Geschmack"),
    EXCEPTIONAL("Exceptional", "§6", 4, 1.6, "Außergewöhnliches Bier von perfekter Qualität");

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;
    private final String description;

    BeerQuality(String displayName, String colorCode, int level, double priceMultiplier, String description) {
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
    public static BeerQuality determineQuality(double qualityFactor, java.util.Random random) {
        double roll = random.nextDouble() * qualityFactor;

        if (roll >= 1.2) return EXCEPTIONAL;    // ~8% Chance (mit perfektem Factor)
        if (roll >= 1.0) return PREMIUM;        // ~15% Chance
        if (roll >= 0.7) return GOOD;           // ~30% Chance
        if (roll >= 0.4) return BASIC;          // ~30% Chance
        return RAW;                             // ~17% Chance
    }
}
