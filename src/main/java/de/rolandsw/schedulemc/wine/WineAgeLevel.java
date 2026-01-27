package de.rolandsw.schedulemc.wine;

/**
 * Wein-Reifegrade
 *
 * Längere Reifung = Besserer Geschmack = Höherer Preis
 */
public enum WineAgeLevel {
    YOUNG("Jung", "§e", 1.0, 0),              // Frisch abgefüllt (0 Tage)
    MEDIUM("Mittel", "§6", 1.3, 2400),        // 2 Tage gereift (2 MC-Tage = 2400 Ticks)
    AGED("Gereift", "§c", 1.8, 7200);         // 6 Tage gereift (6 MC-Tage = 7200 Ticks)

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;
    private final int requiredAgingTicks;

    WineAgeLevel(String displayName, String colorCode, double priceMultiplier, int requiredAgingTicks) {
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
    public static WineAgeLevel determineAgeLevel(int agingTicks) {
        if (agingTicks >= AGED.requiredAgingTicks) return AGED;
        if (agingTicks >= MEDIUM.requiredAgingTicks) return MEDIUM;
        return YOUNG;
    }
}
