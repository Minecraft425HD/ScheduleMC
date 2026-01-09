package de.rolandsw.schedulemc.mdma;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * MDMA/Ecstasy-Qualitätsstufen
 */
public enum MDMAQuality implements ProductionQuality {
    SCHLECHT("§7", 0, 0.5),
    STANDARD("§f", 1, 1.0),
    GUT("§e", 2, 2.0),
    PREMIUM("§d§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    MDMAQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.mdma_quality." + this.name().toLowerCase()).getString();
    }

    public String getDescription() {
        return Component.translatable("enum.mdma_quality.desc." + this.name().toLowerCase()).getString();
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
            case SCHLECHT -> STANDARD;
            case STANDARD -> GUT;
            case GUT, PREMIUM -> PREMIUM;
        };
    }

    @Override
    public MDMAQuality downgrade() {
        return switch (this) {
            case SCHLECHT, STANDARD -> SCHLECHT;
            case GUT -> STANDARD;
            case PREMIUM -> GUT;
        };
    }

    public static MDMAQuality fromLevel(int level) {
        for (MDMAQuality q : values()) {
            if (q.level == level) return q;
        }
        return STANDARD;
    }

    /**
     * Berechnet Qualität basierend auf Timing-Performance
     * @param timingScore 0.0 (schlecht) bis 1.0 (perfekt)
     */
    public static MDMAQuality fromTimingScore(double timingScore) {
        if (timingScore >= 0.95) return PREMIUM;  // Fast perfekt
        if (timingScore >= 0.8) return GUT;       // Gut getroffen
        if (timingScore >= 0.5) return STANDARD;  // Akzeptabel
        return SCHLECHT;                          // Zu früh/spät
    }
}
