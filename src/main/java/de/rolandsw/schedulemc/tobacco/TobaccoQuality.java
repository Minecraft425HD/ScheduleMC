package de.rolandsw.schedulemc.tobacco;

import de.rolandsw.schedulemc.production.core.ProductionQuality;

/**
 * Tabak-Qualitätsstufen
 */
public enum TobaccoQuality implements ProductionQuality {
    SCHLECHT("Schlecht", "§c", 0, 1.0),
    GUT("Gut", "§e", 1, 1.5),
    SEHR_GUT("Sehr Gut", "§a", 2, 2.5),
    LEGENDAER("Legendär", "§6§l", 3, 5.0);
    
    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;
    
    TobaccoQuality(String displayName, String colorCode, int level, double priceMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
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

    /**
     * Returns the yield multiplier (same as price multiplier)
     */
    public double getYieldMultiplier() {
        return priceMultiplier;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    @Override
    public String getDescription() {
        return switch (this) {
            case SCHLECHT -> "Niedrige Qualität";
            case GUT -> "Gute Qualität";
            case SEHR_GUT -> "Sehr gute Qualität";
            case LEGENDAER -> "Legendäre Qualität";
        };
    }

    /**
     * Verbessert die Qualität um eine Stufe
     */
    @Override
    public ProductionQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }
    
    /**
     * Verschlechtert die Qualität um eine Stufe
     */
    @Override
    public ProductionQuality downgrade() {
        return switch (this) {
            case SCHLECHT, GUT -> SCHLECHT;
            case SEHR_GUT -> GUT;
            case LEGENDAER -> SEHR_GUT;
        };
    }
    
    public static TobaccoQuality fromLevel(int level) {
        for (TobaccoQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return SCHLECHT;
    }
}
