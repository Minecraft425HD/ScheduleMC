package de.rolandsw.schedulemc.beer;

/**
 * Bier-Reifegrade
 *
 * Längere Reifung = Besserer Geschmack = Höherer Preis
 */
public enum BeerAgeLevel {
    YOUNG("Young", "§f", 1.0, 0, 14),              // Frisch gebraut (0-14 Tage)
    MATURED("Matured", "§e", 1.2, 15, 60),         // Gereift (15-60 Tage)
    AGED("Aged", "§6", 1.4, 61, Integer.MAX_VALUE); // Lange gereift (61+ Tage)

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;
    private final int minDays;
    private final int maxDays;

    BeerAgeLevel(String displayName, String colorCode, double priceMultiplier, int minDays, int maxDays) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.priceMultiplier = priceMultiplier;
        this.minDays = minDays;
        this.maxDays = maxDays;
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

    public int getMinDays() {
        return minDays;
    }

    public int getMaxDays() {
        return maxDays;
    }

    /**
     * Bestimmt Reifungsgrad basierend auf verstrichenen Tagen
     */
    public static BeerAgeLevel determineAgeLevel(int agingDays) {
        if (agingDays >= AGED.minDays) return AGED;
        if (agingDays >= MATURED.minDays) return MATURED;
        return YOUNG;
    }

    /**
     * Prüft ob das Bier innerhalb des Zeitraums dieser Reifestufe liegt
     */
    public boolean isInRange(int days) {
        return days >= minDays && days <= maxDays;
    }
}
