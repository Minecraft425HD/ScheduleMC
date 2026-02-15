package de.rolandsw.schedulemc.mdma;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * MDMA/Ecstasy-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - SCHLECHT (Level 0)
 * - GUT (Level 1)
 * - SEHR_GUT (Level 2)
 * - LEGENDAER (Level 3)
 */
public enum MDMAQuality implements ProductionQuality {
    SCHLECHT("§c", 0, 0.7),
    GUT("§e", 1, 1.0),
    SEHR_GUT("§a", 2, 2.0),
    LEGENDAER("§6§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    MDMAQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.quality." + this.name().toLowerCase()).getString();
    }

    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase()).getString();
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
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }

    @Override
    public MDMAQuality downgrade() {
        return switch (this) {
            case SCHLECHT, GUT -> SCHLECHT;
            case SEHR_GUT -> GUT;
            case LEGENDAER -> SEHR_GUT;
        };
    }

    public static MDMAQuality fromLevel(int level) {
        for (MDMAQuality q : values()) {
            if (q.level == level) return q;
        }
        return SCHLECHT;
    }

    /**
     * Berechnet Qualität basierend auf Timing-Performance
     * @param timingScore 0.0 (schlecht) bis 1.0 (perfekt)
     */
    public static MDMAQuality fromTimingScore(double timingScore) {
        if (timingScore >= 0.95) return LEGENDAER;
        if (timingScore >= 0.80) return SEHR_GUT;
        if (timingScore >= 0.50) return GUT;
        return SCHLECHT;
    }
}
