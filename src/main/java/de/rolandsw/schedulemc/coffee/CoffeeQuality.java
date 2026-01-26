package de.rolandsw.schedulemc.coffee;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Kaffee-Qualitätsstufen
 *
 * Qualität wird beeinflusst durch:
 * - Höhenlage (Altitude)
 * - Röstgrad
 * - Processing-Methode (Wet/Dry)
 * - Fermentation-Dauer
 */
public enum CoffeeQuality implements ProductionQuality {
    POOR(Component.translatable("enum.coffee_quality.poor").getString(), "§7", 0, 0.8),
    GOOD(Component.translatable("enum.coffee_quality.good").getString(), "§e", 1, 1.5),
    VERY_GOOD(Component.translatable("enum.coffee_quality.very_good").getString(), "§a", 2, 2.5),
    EXCELLENT(Component.translatable("enum.coffee_quality.excellent").getString(), "§b", 3, 4.0),
    LEGENDARY(Component.translatable("enum.coffee_quality.legendary").getString(), "§6§l", 4, 6.0);

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CoffeeQuality(String displayName, String colorCode, int level, double priceMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
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

    @Override
    public double getYieldMultiplier() {
        return priceMultiplier;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    @Override
    public String getDescription() {
        return switch (this) {
            case POOR -> Component.translatable("enum.coffee_quality.desc.poor").getString();
            case GOOD -> Component.translatable("enum.coffee_quality.desc.good").getString();
            case VERY_GOOD -> Component.translatable("enum.coffee_quality.desc.very_good").getString();
            case EXCELLENT -> Component.translatable("enum.coffee_quality.desc.excellent").getString();
            case LEGENDARY -> Component.translatable("enum.coffee_quality.desc.legendary").getString();
        };
    }

    /**
     * Verbessert die Qualität um eine Stufe
     */
    @Override
    public CoffeeQuality upgrade() {
        return switch (this) {
            case POOR -> GOOD;
            case GOOD -> VERY_GOOD;
            case VERY_GOOD -> EXCELLENT;
            case EXCELLENT, LEGENDARY -> LEGENDARY;
        };
    }

    /**
     * Verschlechtert die Qualität um eine Stufe
     */
    @Override
    public CoffeeQuality downgrade() {
        return switch (this) {
            case POOR, GOOD -> POOR;
            case VERY_GOOD -> GOOD;
            case EXCELLENT -> VERY_GOOD;
            case LEGENDARY -> EXCELLENT;
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
