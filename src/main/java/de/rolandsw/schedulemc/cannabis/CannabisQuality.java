package de.rolandsw.schedulemc.cannabis;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Cannabis-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - POOR (Level 0)
 * - GOOD (Level 1)
 * - VERY_GOOD (Level 2)
 * - LEGENDARY (Level 3)
 */
public enum CannabisQuality implements ProductionQuality {
    POOR("§c", 0, 0.7),
    GOOD("§e", 1, 1.0),
    VERY_GOOD("§a", 2, 2.0),
    LEGENDARY("§6§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CannabisQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.quality." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    public String getColorCode() { return colorCode; }
    public String getColoredName() { return colorCode + getDisplayName(); }
    public int getLevel() { return level; }
    public double getPriceMultiplier() { return priceMultiplier; }

    @Override
    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    @Override
    public CannabisQuality upgrade() {
        return switch (this) {
            case POOR -> GOOD;
            case GOOD -> VERY_GOOD;
            case VERY_GOOD, LEGENDARY -> LEGENDARY;
        };
    }

    @Override
    public CannabisQuality downgrade() {
        return switch (this) {
            case POOR, GOOD -> POOR;
            case VERY_GOOD -> GOOD;
            case LEGENDARY -> VERY_GOOD;
        };
    }

    public static CannabisQuality fromLevel(int level) {
        for (CannabisQuality quality : values()) {
            if (quality.level == level) return quality;
        }
        return POOR;
    }

    /**
     * Berechnet Qualität basierend auf Trim-Score (0.0 - 1.0)
     */
    public static CannabisQuality fromTrimScore(double score) {
        if (score >= 0.90) return LEGENDARY;
        if (score >= 0.70) return VERY_GOOD;
        if (score >= 0.40) return GOOD;
        return POOR;
    }

    /**
     * Berechnet Qualität basierend auf Curing-Zeit: 1 Tag = +1 Qualitätsstufe.
     */
    public static CannabisQuality fromCuringTime(int days, CannabisQuality baseQuality) {
        int newLevel = Math.min(LEGENDARY.level, baseQuality.level + days);
        return fromLevel(newLevel);
    }
}
