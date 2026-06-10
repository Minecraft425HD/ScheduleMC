package de.rolandsw.schedulemc.coca;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Crack-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - POOR (Level 0)
 * - GOOD (Level 1)
 * - VERY_GOOD (Level 2)
 * - LEGENDARY (Level 3)
 */
public enum CrackQuality implements ProductionQuality {
    POOR("§c", 0, 0.7),
    GOOD("§e", 1, 1.0),
    VERY_GOOD("§a", 2, 1.5),
    LEGENDARY("§6§l", 3, 2.5);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CrackQuality(String colorCode, int level, double priceMultiplier) {
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
    public CrackQuality upgrade() {
        return switch (this) {
            case POOR -> GOOD;
            case GOOD -> VERY_GOOD;
            case VERY_GOOD, LEGENDARY -> LEGENDARY;
        };
    }

    @Override
    public CrackQuality downgrade() {
        return switch (this) {
            case POOR, GOOD -> POOR;
            case VERY_GOOD -> GOOD;
            case LEGENDARY -> VERY_GOOD;
        };
    }

    /**
     * Berechnet Qualität basierend auf Kochzeit-Präzision
     * @param timingScore 0.0 (schlecht) bis 1.0 (perfekt)
     */
    public static CrackQuality fromTimingScore(double timingScore) {
        if (timingScore >= 0.95) return LEGENDARY;
        if (timingScore >= 0.80) return VERY_GOOD;
        if (timingScore >= 0.50) return GOOD;
        return POOR;
    }

    public static CrackQuality fromLevel(int level) {
        for (CrackQuality quality : values()) {
            if (quality.level == level) return quality;
        }
        return POOR;
    }
}
