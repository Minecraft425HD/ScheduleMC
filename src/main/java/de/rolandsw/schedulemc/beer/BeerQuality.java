package de.rolandsw.schedulemc.beer;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Bier-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - POOR (Level 0)
 * - GOOD (Level 1)
 * - VERY_GOOD (Level 2)
 * - LEGENDARY (Level 3)
 */
public enum BeerQuality implements ProductionQuality {
    POOR("§c", 0, 0.7),
    GOOD("§e", 1, 1.0),
    VERY_GOOD("§a", 2, 1.5),
    LEGENDARY("§6§l", 3, 2.5);

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
        return Component.translatable("enum.quality." + this.name().toLowerCase(Locale.ROOT)).getString();
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
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    public String getColoredName() {
        return colorCode + getDisplayName();
    }

    @Override
    public BeerQuality upgrade() {
        return switch (this) {
            case POOR -> GOOD;
            case GOOD -> VERY_GOOD;
            case VERY_GOOD, LEGENDARY -> LEGENDARY;
        };
    }

    @Override
    public BeerQuality downgrade() {
        return switch (this) {
            case POOR, GOOD -> POOR;
            case VERY_GOOD -> GOOD;
            case LEGENDARY -> VERY_GOOD;
        };
    }

    /**
     * Ermittelt Qualität basierend auf Random-Roll und Quality-Factor
     */
    public static BeerQuality determineQuality(double qualityFactor, java.util.Random random) {
        double roll = random.nextDouble() * qualityFactor;

        if (roll >= 0.95) return LEGENDARY;
        if (roll >= 0.75) return VERY_GOOD;
        if (roll >= 0.45) return GOOD;
        return POOR;
    }

    public static BeerQuality fromLevel(int level) {
        for (BeerQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return POOR;
    }
}
