package de.rolandsw.schedulemc.mdma;

/**
 * MDMA/Ecstasy-Qualitätsstufen
 */
public enum MDMAQuality {
    SCHLECHT("Schlecht", "§7", 0, 0.5, "Verunreinigt"),
    STANDARD("Standard", "§f", 1, 1.0, "Normale Qualität"),
    GUT("Gut", "§e", 2, 2.0, "Hohe Reinheit"),
    PREMIUM("Premium", "§d§l", 3, 4.0, "Laborqualität");

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;
    private final String description;

    MDMAQuality(String displayName, String colorCode, int level, double priceMultiplier, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public int getLevel() { return level; }
    public double getPriceMultiplier() { return priceMultiplier; }
    public String getDescription() { return description; }
    public String getColoredName() { return colorCode + displayName; }

    public MDMAQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> STANDARD;
            case STANDARD -> GUT;
            case GUT, PREMIUM -> PREMIUM;
        };
    }

    public MDMAQuality downgrade() {
        return switch (this) {
            case SCHLECHT, STANDARD -> SCHLECHT;
            case GUT -> STANDARD;
            case PREMIUM -> GUT;
        };
    }

    public static MDMAQuality fromLevel(int level) {
        for (MDMAQuality q : values()) {
            if (q.level == level) return q;
        }
        return STANDARD;
    }

    /**
     * Berechnet Qualität basierend auf Timing-Performance
     * @param timingScore 0.0 (schlecht) bis 1.0 (perfekt)
     */
    public static MDMAQuality fromTimingScore(double timingScore) {
        if (timingScore >= 0.95) return PREMIUM;  // Fast perfekt
        if (timingScore >= 0.8) return GUT;       // Gut getroffen
        if (timingScore >= 0.5) return STANDARD;  // Akzeptabel
        return SCHLECHT;                          // Zu früh/spät
    }
}
