package de.rolandsw.schedulemc.honey;

/**
 * Honig-Reifegrade
 *
 * Längere Reifung = Besserer Geschmack = Höherer Preis
 * Honig kristallisiert und entwickelt komplexere Aromen mit der Zeit
 */
public enum HoneyAgeLevel {
    FRESH("Fresh", 0, 30, 1.0, "§f"),           // Frisch geerntet (0-30 Tage)
    MATURE("Mature", 31, 90, 1.2, "§e"),        // Gereift (31-90 Tage)
    AGED("Aged", 91, 180, 1.4, "§6"),           // Gut gereift (91-180 Tage)
    VINTAGE("Vintage", 181, Integer.MAX_VALUE, 1.7, "§d"); // Jahrgangshonig (>180 Tage)

    private final String displayName;
    private final int minDays;
    private final int maxDays;
    private final double priceMultiplier;
    private final String colorCode;

    HoneyAgeLevel(String displayName, int minDays, int maxDays, double priceMultiplier, String colorCode) {
        this.displayName = displayName;
        this.minDays = minDays;
        this.maxDays = maxDays;
        this.priceMultiplier = priceMultiplier;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return colorCode + displayName;
    }

    public int getMinDays() {
        return minDays;
    }

    public int getMaxDays() {
        return maxDays;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public String getColorCode() {
        return colorCode;
    }

    /**
     * Berechnet benötigte Ticks für dieses Reifungslevel
     */
    public int getRequiredAgingTicks() {
        return minDays * 24000; // Convert days to ticks (1 MC day = 24000 ticks)
    }

    /**
     * Bestimmt Reifungsgrad basierend auf verstrichenen Tagen
     */
    public static HoneyAgeLevel determineAgeLevel(int agingDays) {
        if (agingDays >= VINTAGE.minDays) return VINTAGE;
        if (agingDays >= AGED.minDays) return AGED;
        if (agingDays >= MATURE.minDays) return MATURE;
        return FRESH;
    }

    /**
     * Bestimmt Reifungsgrad basierend auf verstrichenen Ticks
     */
    public static HoneyAgeLevel determineAgeLevelFromTicks(int agingTicks) {
        int days = agingTicks / 24000; // Convert ticks to days
        return determineAgeLevel(days);
    }

    /**
     * Gibt die Beschreibung des Reifungsgrades zurück
     */
    public String getDescription() {
        return switch (this) {
            case FRESH -> "Frisch geerntet mit leichtem Blütenaroma";
            case MATURE -> "Gereift mit ausgewogenen Aromen";
            case AGED -> "Gut gereift mit komplexen Geschmacksnoten";
            case VINTAGE -> "Jahrgangshonig mit außergewöhnlicher Tiefe";
        };
    }
}
