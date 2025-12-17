package de.rolandsw.schedulemc.coca;

/**
 * Crack-Qualitätsstufen
 * Abhängig vom Kochprozess
 */
public enum CrackQuality {
    SCHLECHT("Schlecht", "§c", 0, 0.6),        // Überkokt oder unterkokt
    STANDARD("Standard", "§7", 1, 1.0),        // Normaler Cook
    GUT("Gut", "§a", 2, 1.5),                  // Guter Cook
    FISHSCALE("Fishscale", "§b§l", 3, 2.5);    // Perfekter Cook, glänzend

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CrackQuality(String displayName, String colorCode, int level, double priceMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getColoredName() { return colorCode + displayName; }
    public int getLevel() { return level; }
    public double getPriceMultiplier() { return priceMultiplier; }

    /**
     * Berechnet Qualität basierend auf Kochzeit-Präzision
     * @param timingScore 0.0 (schlecht) bis 1.0 (perfekt)
     */
    public static CrackQuality fromTimingScore(double timingScore) {
        if (timingScore >= 0.95) return FISHSCALE;
        if (timingScore >= 0.80) return GUT;
        if (timingScore >= 0.50) return STANDARD;
        return SCHLECHT;
    }

    public static CrackQuality fromLevel(int level) {
        for (CrackQuality quality : values()) {
            if (quality.level == level) return quality;
        }
        return SCHLECHT;
    }
}
