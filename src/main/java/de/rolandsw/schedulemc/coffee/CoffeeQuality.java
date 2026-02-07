package de.rolandsw.schedulemc.coffee;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Kaffee-Qualitätsstufen
 *
 * Einheitliches 4-Stufen-System:
 * - SCHLECHT (Level 0)
 * - GUT (Level 1)
 * - SEHR_GUT (Level 2)
 * - LEGENDAER (Level 3)
 */
public enum CoffeeQuality implements ProductionQuality {
    SCHLECHT("§c", 0, 0.7),
    GUT("§e", 1, 1.0),
    SEHR_GUT("§a", 2, 2.0),
    LEGENDAER("§6§l", 3, 4.0);

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CoffeeQuality(String colorCode, int level, double priceMultiplier) {
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
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase()).getString();
    }

    @Override
    public CoffeeQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }

    @Override
    public CoffeeQuality downgrade() {
        return switch (this) {
            case SCHLECHT, GUT -> SCHLECHT;
            case SEHR_GUT -> GUT;
            case LEGENDAER -> SEHR_GUT;
        };
    }

    public static CoffeeQuality fromLevel(int level) {
        for (CoffeeQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return SCHLECHT;
    }
}
