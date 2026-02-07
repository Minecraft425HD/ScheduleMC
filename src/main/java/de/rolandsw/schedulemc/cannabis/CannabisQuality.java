package de.rolandsw.schedulemc.cannabis;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Cannabis-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - SCHLECHT (Level 0)
 * - GUT (Level 1)
 * - SEHR_GUT (Level 2)
 * - LEGENDAER (Level 3)
 */
public enum CannabisQuality implements ProductionQuality {
    SCHLECHT("§c", 0, 0.7),
    GUT("§e", 1, 1.0),
    SEHR_GUT("§a", 2, 2.0),
    LEGENDAER("§6§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CannabisQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.quality." + this.name().toLowerCase()).getString();
    }

    public String getColorCode() { return colorCode; }
    public String getColoredName() { return colorCode + getDisplayName(); }
    public int getLevel() { return level; }
    public double getPriceMultiplier() { return priceMultiplier; }

    @Override
    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase()).getString();
    }

    @Override
    public CannabisQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }

    @Override
    public CannabisQuality downgrade() {
        return switch (this) {
            case SCHLECHT, GUT -> SCHLECHT;
            case SEHR_GUT -> GUT;
            case LEGENDAER -> SEHR_GUT;
        };
    }

    public static CannabisQuality fromLevel(int level) {
        for (CannabisQuality quality : values()) {
            if (quality.level == level) return quality;
        }
        return SCHLECHT;
    }

    /**
     * Berechnet Qualität basierend auf Trim-Score (0.0 - 1.0)
     */
    public static CannabisQuality fromTrimScore(double score) {
        if (score >= 0.90) return LEGENDAER;
        if (score >= 0.70) return SEHR_GUT;
        if (score >= 0.40) return GUT;
        return SCHLECHT;
    }

    /**
     * Berechnet Qualität basierend auf Curing-Zeit
     */
    public static CannabisQuality fromCuringTime(int days, CannabisQuality baseQuality) {
        if (days >= 28 && baseQuality.level < LEGENDAER.level) {
            return baseQuality.upgrade().upgrade();
        } else if (days >= 14 && baseQuality.level < LEGENDAER.level) {
            return baseQuality.upgrade();
        }
        return baseQuality;
    }
}
