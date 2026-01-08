package de.rolandsw.schedulemc.lsd;

import de.rolandsw.schedulemc.production.core.ProductionQuality;

/**
 * LSD-Dosierungsstufen
 * Mikrogramm-basiert für realistische Dosierung
 */
public enum LSDDosage implements ProductionQuality {
    // Dosierungs-Konstanten (in Mikrogramm)
    private static final int STANDARD_DOSAGE_UG = 100; // Standard-Dosierung
    private static final int STRONG_DOSAGE_UG = 200; // Starke Dosierung

    SCHWACH("Schwach", "§7", 0, 50, 1.0, "Leichte Effekte"),
    STANDARD("Standard", "§a", 1, STANDARD_DOSAGE_UG, 2.0, "Normale Dosis"),
    STARK("Stark", "§e", 2, STRONG_DOSAGE_UG, 3.5, "Intensive Erfahrung"),
    BICYCLE_DAY("Bicycle Day", "§d§l", 3, 300, 6.0, "Hofmann-Dosis");

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final int micrograms;
    private final double priceMultiplier;
    private final String description;

    LSDDosage(String displayName, String colorCode, int level, int micrograms, double priceMultiplier, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.micrograms = micrograms;
        this.priceMultiplier = priceMultiplier;
        this.description = description;
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

    public int getMicrograms() {
        return micrograms;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public String getDescription() {
        return description;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    public String getDosageString() {
        return micrograms + "μg";
    }

    @Override
    public LSDDosage upgrade() {
        return switch (this) {
            case SCHWACH -> STANDARD;
            case STANDARD -> STARK;
            case STARK, BICYCLE_DAY -> BICYCLE_DAY;
        };
    }

    @Override
    public LSDDosage downgrade() {
        return switch (this) {
            case SCHWACH, STANDARD -> SCHWACH;
            case STARK -> STANDARD;
            case BICYCLE_DAY -> STARK;
        };
    }

    public static LSDDosage fromLevel(int level) {
        for (LSDDosage dosage : values()) {
            if (dosage.level == level) {
                return dosage;
            }
        }
        return STANDARD;
    }

    /**
     * Berechnet Dosierung basierend auf Slider-Wert (0-100)
     */
    public static LSDDosage fromSliderValue(int sliderValue) {
        if (sliderValue < 25) {
            return SCHWACH;
        } else if (sliderValue < 50) {
            return STANDARD;
        } else if (sliderValue < 75) {
            return STARK;
        } else {
            return BICYCLE_DAY;
        }
    }

    /**
     * Gibt Mikrogramm-Wert basierend auf Slider (0-100) zurück
     * Interpoliert zwischen 50 und 300 μg
     */
    public static int getMicrogramsFromSlider(int sliderValue) {
        return 50 + (int) ((sliderValue / 100.0) * 250);
    }
}
