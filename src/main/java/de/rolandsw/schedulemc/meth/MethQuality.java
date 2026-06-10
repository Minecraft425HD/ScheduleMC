package de.rolandsw.schedulemc.meth;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Meth-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - POOR (Level 0)
 * - GOOD (Level 1)
 * - VERY_GOOD (Level 2)
 * - LEGENDARY (Level 3) - Blue Sky
 */
public enum MethQuality implements ProductionQuality {
    POOR("§c", 0, 0.7),
    GOOD("§e", 1, 1.0),
    VERY_GOOD("§a", 2, 2.0),
    LEGENDARY("§6§l", 3, 5.0);  // Gold-Farbe (Blue Sky entfernt für Konsistenz)

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    MethQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.quality." + this.name().toLowerCase(Locale.ROOT)).getString();
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

    public String getColorDescription() {
        return switch (this) {
            case POOR -> "Weiß";
            case GOOD -> "Gelblich";
            case VERY_GOOD -> "Bläulich";
            case LEGENDARY -> "Blau";
        };
    }

    @Override
    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    @Override
    public MethQuality upgrade() {
        return switch (this) {
            case POOR -> GOOD;
            case GOOD -> VERY_GOOD;
            case VERY_GOOD, LEGENDARY -> LEGENDARY;
        };
    }

    @Override
    public MethQuality downgrade() {
        return switch (this) {
            case POOR, GOOD -> POOR;
            case VERY_GOOD -> GOOD;
            case LEGENDARY -> VERY_GOOD;
        };
    }

    public static MethQuality fromLevel(int level) {
        for (MethQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return POOR;
    }

    /**
     * Berechnet Qualität basierend auf Temperatur-Performance im ReductionKettle
     * @param optimalTimePercent Prozentsatz der Zeit im optimalen Temperaturbereich (0.0 - 1.0)
     */
    public static MethQuality fromTemperaturePerformance(double optimalTimePercent) {
        if (optimalTimePercent >= 0.95) return LEGENDARY;
        if (optimalTimePercent >= 0.80) return VERY_GOOD;
        if (optimalTimePercent >= 0.60) return GOOD;
        return POOR;
    }
}
