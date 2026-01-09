package de.rolandsw.schedulemc.coca;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Crack-Qualitätsstufen
 * Abhängig vom Kochprozess
 */
public enum CrackQuality implements ProductionQuality {
    SCHLECHT(Component.translatable("enum.crack_quality.schlecht").getString(), "§c", 0, 0.6),        // Überkokt oder unterkokt
    STANDARD(Component.translatable("enum.crack_quality.standard").getString(), "§7", 1, 1.0),        // Normaler Cook
    GUT(Component.translatable("enum.crack_quality.gut").getString(), "§a", 2, 1.5),                  // Guter Cook
    FISHSCALE(Component.translatable("enum.crack_quality.fishscale").getString(), "§b§l", 3, 2.5);    // Perfekter Cook, glänzend

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;

    CrackQuality(String displayName, String colorCode, int level, double priceMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getColoredName() { return colorCode + displayName; }
    public int getLevel() { return level; }
    public double getPriceMultiplier() { return priceMultiplier; }

    @Override
    public String getDescription() {
        return switch (this) {
            case SCHLECHT -> Component.translatable("enum.crack_quality.desc.schlecht").getString();
            case STANDARD -> Component.translatable("enum.crack_quality.desc.standard").getString();
            case GUT -> Component.translatable("enum.crack_quality.desc.gut").getString();
            case FISHSCALE -> Component.translatable("enum.crack_quality.desc.fishscale").getString();
        };
    }

    @Override
    public CrackQuality upgrade() {
        return switch (this) {
            case SCHLECHT -> STANDARD;
            case STANDARD -> GUT;
            case GUT, FISHSCALE -> FISHSCALE;
        };
    }

    @Override
    public CrackQuality downgrade() {
        return switch (this) {
            case SCHLECHT, STANDARD -> SCHLECHT;
            case GUT -> STANDARD;
            case FISHSCALE -> GUT;
        };
    }

    /**
     * Berechnet Qualität basierend auf Kochzeit-Präzision
     * @param timingScore 0.0 (schlecht) bis 1.0 (perfekt)
     */
    public static CrackQuality fromTimingScore(double timingScore) {
        if (timingScore >= 0.95) return FISHSCALE;
        if (timingScore >= 0.80) return GUT;
        if (timingScore >= 0.50) return STANDARD;
        return SCHLECHT;
    }

    public static CrackQuality fromLevel(int level) {
        for (CrackQuality quality : values()) {
            if (quality.level == level) return quality;
        }
        return SCHLECHT;
    }
}
