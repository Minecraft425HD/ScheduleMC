package de.rolandsw.schedulemc.tobacco;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Tabak-Qualitätsstufen
 */
public enum TobaccoQuality implements ProductionQuality {
    SCHLECHT(Component.translatable("enum.tobacco_quality.schlecht").getString(), "§c", 0, 1.0),
    GUT(Component.translatable("enum.tobacco_quality.gut").getString(), "§e", 1, 1.5),
    SEHR_GUT(Component.translatable("enum.tobacco_quality.sehr_gut").getString(), "§a", 2, 2.5),
    LEGENDAER(Component.translatable("enum.tobacco_quality.legendaer").getString(), "§6§l", 3, 5.0);
    
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
            case SCHLECHT -> Component.translatable("enum.tobacco_quality.desc.schlecht").getString();
            case GUT -> Component.translatable("enum.tobacco_quality.desc.gut").getString();
            case SEHR_GUT -> Component.translatable("enum.tobacco_quality.desc.sehr_gut").getString();
            case LEGENDAER -> Component.translatable("enum.tobacco_quality.desc.legendaer").getString();
        };
    }

    /**
     * Verbessert die Qualität um eine Stufe
     */
    @Override
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
    @Override
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
