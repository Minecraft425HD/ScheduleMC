package de.rolandsw.schedulemc.lsd;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * LSD-Qualitätsstufen (basierend auf Dosierung)
 *
 * Einheitliches 4-Stufen-System:
 * - SCHLECHT (Level 0) - 50μg
 * - GUT (Level 1) - 100μg
 * - SEHR_GUT (Level 2) - 200μg
 * - LEGENDAER (Level 3) - 300μg (Bicycle Day)
 */
public enum LSDDosage implements ProductionQuality {
    SCHLECHT("§c", 0, 50, 0.7),
    GUT("§e", 1, 100, 1.0),
    SEHR_GUT("§a", 2, 200, 2.0),
    LEGENDAER("§6§l", 3, 300, 4.0);

    private final String colorCode;
    private final int level;
    private final int micrograms;
    private final double priceMultiplier;

    LSDDosage(String colorCode, int level, int micrograms, double priceMultiplier) {
        this.colorCode = colorCode;
        this.level = level;
        this.micrograms = micrograms;
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

    public int getMicrograms() {
        return micrograms;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public String getDescription() {
        return Component.translatable("enum.quality.desc." + this.name().toLowerCase()).getString();
    }

    public String getColoredName() {
        return colorCode + getDisplayName();
    }

    public String getDosageString() {
        return micrograms + "μg";
    }

    @Override
    public LSDDosage upgrade() {
        return switch (this) {
            case SCHLECHT -> GUT;
            case GUT -> SEHR_GUT;
            case SEHR_GUT, LEGENDAER -> LEGENDAER;
        };
    }

    @Override
    public LSDDosage downgrade() {
        return switch (this) {
            case SCHLECHT, GUT -> SCHLECHT;
            case SEHR_GUT -> GUT;
            case LEGENDAER -> SEHR_GUT;
        };
    }

    public static LSDDosage fromLevel(int level) {
        for (LSDDosage dosage : values()) {
            if (dosage.level == level) {
                return dosage;
            }
        }
        return SCHLECHT;
    }

    /**
     * Berechnet Dosierung basierend auf Slider-Wert (0-100)
     */
    public static LSDDosage fromSliderValue(int sliderValue) {
        if (sliderValue >= 75) return LEGENDAER;
        if (sliderValue >= 50) return SEHR_GUT;
        if (sliderValue >= 25) return GUT;
        return SCHLECHT;
    }

    /**
     * Gibt Mikrogramm-Wert basierend auf Slider (0-100) zurück
     * Interpoliert zwischen 50 und 300 μg
     */
    public static int getMicrogramsFromSlider(int sliderValue) {
        return 50 + (int) ((sliderValue / 100.0) * 250);
    }
}
