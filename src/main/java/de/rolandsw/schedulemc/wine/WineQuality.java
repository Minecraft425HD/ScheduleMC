package de.rolandsw.schedulemc.wine;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Wein-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - SCHLECHT (Level 0)
 * - GUT (Level 1)
 * - SEHR_GUT (Level 2)
 * - LEGENDAER (Level 3)
 */
public enum WineQuality implements ProductionQuality {
    SCHLECHT("§c", 0, 0.7),
    GUT("§e", 1, 1.0),
    SEHR_GUT("§a", 2, 2.0),
    LEGENDAER("§d§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    WineQuality(String colorCode, int level, double priceMultiplier) {
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

    @Override
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
    public WineQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }

    @Override
    public WineQuality downgrade() {
        return switch (this) {
            case SCHLECHT, GUT -> SCHLECHT;
            case SEHR_GUT -> GUT;
            case LEGENDAER -> SEHR_GUT;
        };
    }

    /**
     * Ermittelt Qualität basierend auf Random-Roll und Quality-Factor
     */
    public static WineQuality determineQuality(double qualityFactor, java.util.Random random) {
        double roll = random.nextDouble() * qualityFactor;

        if (roll >= 0.95) return LEGENDAER;
        if (roll >= 0.75) return SEHR_GUT;
        if (roll >= 0.45) return GUT;
        return SCHLECHT;
    }

    public static WineQuality fromLevel(int level) {
        for (WineQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return SCHLECHT;
    }
}
