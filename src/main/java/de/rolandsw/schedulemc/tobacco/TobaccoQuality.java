package de.rolandsw.schedulemc.tobacco;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Tabak-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - SCHLECHT (Level 0)
 * - GUT (Level 1)
 * - SEHR_GUT (Level 2)
 * - LEGENDAER (Level 3)
 */
public enum TobaccoQuality implements ProductionQuality {
    SCHLECHT("§c", 0, 0.7),
    GUT("§e", 1, 1.0),
    SEHR_GUT("§a", 2, 2.0),
    LEGENDAER("§6§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    TobaccoQuality(String colorCode, int level, double priceMultiplier) {
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

    /**
     * Returns the yield multiplier (same as price multiplier)
     */
    public double getYieldMultiplier() {
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
    public TobaccoQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }

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
