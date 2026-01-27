package de.rolandsw.schedulemc.cheese;

/**
 * Kase-Reifegrade
 *
 * Langere Reifung = Besserer Geschmack = Hoherer Preis
 */
public enum CheeseAgeLevel {
    FRESH("Frisch", "§e", 1.0, 0),           // 0 days
    YOUNG("Jung", "§6", 1.3, 600),           // 30 days = 600 ticks
    MATURE("Gereift", "§c", 1.8, 1800),      // 90 days = 1800 ticks
    AGED("Alt", "§4", 2.5, 3600);            // 180 days = 3600 ticks

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;
    private final int requiredAgingTicks;

    CheeseAgeLevel(String displayName, String colorCode, double priceMultiplier, int requiredAgingTicks) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.priceMultiplier = priceMultiplier;
        this.requiredAgingTicks = requiredAgingTicks;
    }

    public String getDisplayName() {
        return colorCode + displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public int getRequiredAgingTicks() {
        return requiredAgingTicks;
    }

    /**
     * Bestimmt Reifungsgrad basierend auf verstrichenen Ticks
     */
    public static CheeseAgeLevel determineAgeLevel(int agingTicks) {
        if (agingTicks >= AGED.requiredAgingTicks) return AGED;
        if (agingTicks >= MATURE.requiredAgingTicks) return MATURE;
        if (agingTicks >= YOUNG.requiredAgingTicks) return YOUNG;
        return FRESH;
    }
}
