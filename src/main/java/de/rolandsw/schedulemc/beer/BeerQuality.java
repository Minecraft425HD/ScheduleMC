package de.rolandsw.schedulemc.beer;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Bier-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - SCHLECHT (Level 0)
 * - GUT (Level 1)
 * - SEHR_GUT (Level 2)
 * - LEGENDAER (Level 3)
 */
public enum BeerQuality implements ProductionQuality {
    SCHLECHT("§c", 0, 0.7),
    GUT("§e", 1, 1.0),
    SEHR_GUT("§a", 2, 1.5),
    LEGENDAER("§6§l", 3, 2.5);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    BeerQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    @Override
    public String getDisplayName() {
        return Component.translatable("enum.quality." + this.name().toLowerCase()).getString();
    }

    @Override
    public String getColorCode() {
        return colorCode;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    @Override
    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase()).getString();
    }

    public String getColoredName() {
        return colorCode + getDisplayName();
    }

    @Override
    public BeerQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }

    @Override
    public BeerQuality downgrade() {
        return switch (this) {
            case SCHLECHT, GUT -> SCHLECHT;
            case SEHR_GUT -> GUT;
            case LEGENDAER -> SEHR_GUT;
        };
    }

    /**
     * Ermittelt Qualität basierend auf Random-Roll und Quality-Factor
     */
    public static BeerQuality determineQuality(double qualityFactor, java.util.Random random) {
        double roll = random.nextDouble() * qualityFactor;

        if (roll >= 0.95) return LEGENDAER;
        if (roll >= 0.75) return SEHR_GUT;
        if (roll >= 0.45) return GUT;
        return SCHLECHT;
    }

    public static BeerQuality fromLevel(int level) {
        for (BeerQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return SCHLECHT;
    }
}
