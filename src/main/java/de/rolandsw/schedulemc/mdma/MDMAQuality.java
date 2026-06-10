package de.rolandsw.schedulemc.mdma;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * MDMA/Ecstasy-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - POOR (Level 0)
 * - GOOD (Level 1)
 * - VERY_GOOD (Level 2)
 * - LEGENDARY (Level 3)
 */
public enum MDMAQuality implements ProductionQuality {
    POOR("§c", 0, 0.7),
    GOOD("§e", 1, 1.0),
    VERY_GOOD("§a", 2, 2.0),
    LEGENDARY("§6§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    MDMAQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.quality." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    public String getColorCode() { return colorCode; }
    public int getLevel() { return level; }
    public double getPriceMultiplier() { return priceMultiplier; }

    public String getColoredName() {
        return colorCode + getDisplayName();
    }

    @Override
    public MDMAQuality upgrade() {
        return switch (this) {
            case POOR -> GOOD;
            case GOOD -> VERY_GOOD;
            case VERY_GOOD, LEGENDARY -> LEGENDARY;
        };
    }

    @Override
    public MDMAQuality downgrade() {
        return switch (this) {
            case POOR, GOOD -> POOR;
            case VERY_GOOD -> GOOD;
            case LEGENDARY -> VERY_GOOD;
        };
    }

    public static MDMAQuality fromLevel(int level) {
        for (MDMAQuality q : values()) {
            if (q.level == level) return q;
        }
        return POOR;
    }

    /**
     * Berechnet Qualität basierend auf Timing-Performance
     * @param timingScore 0.0 (schlecht) bis 1.0 (perfekt)
     */
    public static MDMAQuality fromTimingScore(double timingScore) {
        if (timingScore >= 0.95) return LEGENDARY;
        if (timingScore >= 0.80) return VERY_GOOD;
        if (timingScore >= 0.50) return GOOD;
        return POOR;
    }
}
