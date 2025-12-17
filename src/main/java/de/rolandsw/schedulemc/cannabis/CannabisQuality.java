package de.rolandsw.schedulemc.cannabis;

/**
 * Cannabis-Qualitätsstufen
 */
public enum CannabisQuality {
    SCHWAG("Schwag", "§8", 0, 0.5),              // Schlechte Qualität, viele Samen
    MIDS("Mids", "§7", 1, 1.0),                  // Durchschnitt
    DANK("Dank", "§a", 2, 2.0),                  // Gute Qualität
    TOP_SHELF("Top Shelf", "§6", 3, 3.5),        // Premium
    EXOTIC("Exotic", "§d§l", 4, 5.0);            // Beste Qualität

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CannabisQuality(String displayName, String colorCode, int level, double priceMultiplier) {
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

    public CannabisQuality upgrade() {
        return switch (this) {
            case SCHWAG -> MIDS;
            case MIDS -> DANK;
            case DANK -> TOP_SHELF;
            case TOP_SHELF, EXOTIC -> EXOTIC;
        };
    }

    public CannabisQuality downgrade() {
        return switch (this) {
            case SCHWAG, MIDS -> SCHWAG;
            case DANK -> MIDS;
            case TOP_SHELF -> DANK;
            case EXOTIC -> TOP_SHELF;
        };
    }

    public static CannabisQuality fromLevel(int level) {
        for (CannabisQuality quality : values()) {
            if (quality.level == level) return quality;
        }
        return SCHWAG;
    }

    /**
     * Berechnet Qualität basierend auf Trim-Score (0.0 - 1.0)
     */
    public static CannabisQuality fromTrimScore(double score) {
        if (score >= 0.95) return EXOTIC;
        if (score >= 0.80) return TOP_SHELF;
        if (score >= 0.60) return DANK;
        if (score >= 0.40) return MIDS;
        return SCHWAG;
    }

    /**
     * Berechnet Qualität basierend auf Curing-Zeit
     */
    public static CannabisQuality fromCuringTime(int days, CannabisQuality baseQuality) {
        // Minimum 14 Tage für Upgrade, 28+ für Maximum
        if (days >= 28 && baseQuality.level < EXOTIC.level) {
            return baseQuality.upgrade().upgrade();
        } else if (days >= 14 && baseQuality.level < EXOTIC.level) {
            return baseQuality.upgrade();
        }
        return baseQuality;
    }
}
