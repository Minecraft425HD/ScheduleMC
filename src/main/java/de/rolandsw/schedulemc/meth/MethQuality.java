package de.rolandsw.schedulemc.meth;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Meth-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - SCHLECHT (Level 0)
 * - GUT (Level 1)
 * - SEHR_GUT (Level 2)
 * - LEGENDAER (Level 3) - Blue Sky
 */
public enum MethQuality implements ProductionQuality {
    SCHLECHT("§c", 0, 0.7),
    GUT("§e", 1, 1.0),
    SEHR_GUT("§a", 2, 2.0),
    LEGENDAER("§b§l", 3, 5.0);  // Blue Sky Farbe beibehalten

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    MethQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.quality." + this.name().toLowerCase()).getString();
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
        return colorCode + getDisplayName();
    }

    @Override
    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase()).getString();
    }

    @Override
    public MethQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }

    @Override
    public MethQuality downgrade() {
        return switch (this) {
            case SCHLECHT, GUT -> SCHLECHT;
            case SEHR_GUT -> GUT;
            case LEGENDAER -> SEHR_GUT;
        };
    }

    public static MethQuality fromLevel(int level) {
        for (MethQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return SCHLECHT;
    }

    /**
     * Berechnet Qualität basierend auf Temperatur-Performance im Reduktionskessel
     * @param optimalTimePercent Prozentsatz der Zeit im optimalen Temperaturbereich (0.0 - 1.0)
     */
    public static MethQuality fromTemperaturePerformance(double optimalTimePercent) {
        if (optimalTimePercent >= 0.95) return LEGENDAER;
        if (optimalTimePercent >= 0.80) return SEHR_GUT;
        if (optimalTimePercent >= 0.60) return GUT;
        return SCHLECHT;
    }
}
