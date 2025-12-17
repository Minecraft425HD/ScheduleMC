package de.rolandsw.schedulemc.meth;

/**
 * Meth-Qualitätsstufen (Breaking Bad inspiriert)
 */
public enum MethQuality {
    STANDARD("Standard", "§f", 0, 1.0, "Weiß"),      // Weißes Meth (niedrigste Qualität)
    GUT("Gut", "§e", 1, 2.0, "Gelblich"),            // Gelbliches Meth
    BLUE_SKY("Blue Sky", "§b§l", 2, 5.0, "Blau");   // Blaues Meth (höchste Qualität - Heisenberg Style)

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;
    private final String colorDescription;

    MethQuality(String displayName, String colorCode, int level, double priceMultiplier, String colorDescription) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
        this.colorDescription = colorDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public int getLevel() {
        return level;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public String getColorDescription() {
        return colorDescription;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    /**
     * Verbessert die Qualität um eine Stufe
     */
    public MethQuality upgrade() {
        return switch (this) {
            case STANDARD -> GUT;
            case GUT, BLUE_SKY -> BLUE_SKY;
        };
    }

    /**
     * Verschlechtert die Qualität um eine Stufe
     */
    public MethQuality downgrade() {
        return switch (this) {
            case STANDARD, GUT -> STANDARD;
            case BLUE_SKY -> GUT;
        };
    }

    public static MethQuality fromLevel(int level) {
        for (MethQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return STANDARD;
    }

    /**
     * Berechnet Qualität basierend auf Temperatur-Performance im Reduktionskessel
     * @param optimalTimePercent Prozentsatz der Zeit im optimalen Temperaturbereich (0.0 - 1.0)
     */
    public static MethQuality fromTemperaturePerformance(double optimalTimePercent) {
        if (optimalTimePercent >= 0.9) {
            return BLUE_SKY; // 90%+ optimal = Blue Sky
        } else if (optimalTimePercent >= 0.6) {
            return GUT;      // 60-89% optimal = Gut
        } else {
            return STANDARD; // < 60% optimal = Standard
        }
    }
}
