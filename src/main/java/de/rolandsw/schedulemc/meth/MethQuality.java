package de.rolandsw.schedulemc.meth;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Meth-Qualitätsstufen (Breaking Bad inspiriert)
 */
public enum MethQuality implements ProductionQuality {
    STANDARD("§f", 0, 1.0),      // Weißes Meth (niedrigste Qualität)
    GUT("§e", 1, 2.0),           // Gelbliches Meth
    BLUE_SKY("§b§l", 2, 5.0);    // Blaues Meth (höchste Qualität - Heisenberg Style)

    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    MethQuality(String colorCode, int level, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return Component.translatable("enum.meth_quality." + this.name().toLowerCase()).getString();
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

    public String getColorDescription() {
        return Component.translatable("enum.meth_quality.color." + this.name().toLowerCase()).getString();
    }

    public String getColoredName() {
        return colorCode + getDisplayName();
    }

    @Override
    public String getDescription() {
        return switch (this) {
            case STANDARD -> Component.translatable("enum.meth_quality.desc.standard").getString();
            case GUT -> Component.translatable("enum.meth_quality.desc.gut").getString();
            case BLUE_SKY -> Component.translatable("enum.meth_quality.desc.blue_sky").getString();
        };
    }

    /**
     * Verbessert die Qualität um eine Stufe
     */
    @Override
    public MethQuality upgrade() {
        return switch (this) {
            case STANDARD -> GUT;
            case GUT, BLUE_SKY -> BLUE_SKY;
        };
    }

    /**
     * Verschlechtert die Qualität um eine Stufe
     */
    @Override
    public MethQuality downgrade() {
        return switch (this) {
            case STANDARD, GUT -> STANDARD;
            case BLUE_SKY -> GUT;
        };
    }

    public static MethQuality fromLevel(int level) {
        for (MethQuality quality : values()) {
            if (quality.level == level) {
                return quality;
            }
        }
        return STANDARD;
    }

    /**
     * Berechnet Qualität basierend auf Temperatur-Performance im Reduktionskessel
     * @param optimalTimePercent Prozentsatz der Zeit im optimalen Temperaturbereich (0.0 - 1.0)
     */
    public static MethQuality fromTemperaturePerformance(double optimalTimePercent) {
        if (optimalTimePercent >= 0.9) {
            return BLUE_SKY; // 90%+ optimal = Blue Sky
        } else if (optimalTimePercent >= 0.6) {
            return GUT;      // 60-89% optimal = Gut
        } else {
            return STANDARD; // < 60% optimal = Standard
        }
    }
}
