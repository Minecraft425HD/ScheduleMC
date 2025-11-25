package de.rolandsw.schedulemc.tobacco;

/**
 * Tabak-Qualitätsstufen
 */
public enum TobaccoQuality {
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
    
    public String getColoredName() {
        return colorCode + displayName;
    }
    
    /**
     * Verbessert die Qualität um eine Stufe
     */
    public TobaccoQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }
    
    /**
     * Verschlechtert die Qualität um eine Stufe
     */
    public TobaccoQuality downgrade() {
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
