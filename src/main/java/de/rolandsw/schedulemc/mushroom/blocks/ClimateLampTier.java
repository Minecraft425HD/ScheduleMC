package de.rolandsw.schedulemc.mushroom.blocks;

/**
 * ClimateLamp-Stufen mit verschiedenen Eigenschaften
 */
public enum ClimateLampTier {
    SMALL("ClimateLamp", "§7", false, 0.0, 0.0),
    MEDIUM("Auto-ClimateLamp", "§e", true, 0.10, 0.0),
    LARGE("Premium-ClimateLamp", "§6", true, 0.25, 0.10);

    private final String displayName;
    private final String colorCode;
    private final boolean automatic;
    private final double growthBonus;
    private final double qualityBonus;

    ClimateLampTier(String displayName, String colorCode, boolean automatic, double growthBonus, double qualityBonus) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.automatic = automatic;
        this.growthBonus = growthBonus;
        this.qualityBonus = qualityBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public double getGrowthBonus() {
        return growthBonus;
    }

    public double getQualityBonus() {
        return qualityBonus;
    }

    public String getRegistryName() {
        return name().toLowerCase();
    }
}
