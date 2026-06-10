package de.rolandsw.schedulemc.coffee;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Kaffee-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - POOR (Level 0)
 * - GOOD (Level 1)
 * - VERY_GOOD (Level 2)
 * - LEGENDARY (Level 3)
 */
public enum CoffeeQuality implements ProductionQuality {
    POOR("§c", 0, 0.7),
    GOOD("§e", 1, 1.0),
    VERY_GOOD("§a", 2, 2.0),
    LEGENDARY("§6§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CoffeeQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.quality." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    public String getColorCode() {
        return colorCode;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public double getYieldMultiplier() {
        return priceMultiplier;
    }

    public String getColoredName() {
        return colorCode + getDisplayName();
    }

    @Override
    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    @Override
    public CoffeeQuality upgrade() {
        return switch (this) {
            case POOR -> GOOD;
            case GOOD -> VERY_GOOD;
            case VERY_GOOD, LEGENDARY -> LEGENDARY;
        };
    }

    @Override
    public CoffeeQuality downgrade() {
        return switch (this) {
            case POOR, GOOD -> POOR;
            case VERY_GOOD -> GOOD;
            case LEGENDARY -> VERY_GOOD;
        };
    }

    public static CoffeeQuality fromLevel(int level) {
        for (CoffeeQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return POOR;
    }
}
