package de.rolandsw.schedulemc.chocolate;

/**
 * Schokoladen-Reifegrade
 *
 * Längere Reifung = Besserer Geschmack = Höherer Preis
 * Besonders wichtig für dunkle Schokolade
 */
public enum ChocolateAgeLevel {
    FRESH("Fresh", 0, 30, 1.0, "§f"),           // Frisch hergestellt (0-30 Tage)
    AGED("Aged", 31, 90, 1.2, "§e"),            // Gereift (31-90 Tage)
    VINTAGE("Vintage", 91, Integer.MAX_VALUE, 1.5, "§6");  // Vintage (91+ Tage)

    private final String displayName;
    private final int minDays;
    private final int maxDays;
    private final double priceMultiplier;
    private final String colorCode;

    ChocolateAgeLevel(String displayName, int minDays, int maxDays, double priceMultiplier, String colorCode) {
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
     * Bestimmt Reifungsgrad basierend auf verstrichenen Tagen
     */
    public static ChocolateAgeLevel determineAgeLevel(int ageDays) {
        if (ageDays >= VINTAGE.minDays) return VINTAGE;
        if (ageDays >= AGED.minDays) return AGED;
        return FRESH;
    }

    /**
     * Konvertiert Ticks zu Tagen
     */
    public static int ticksToDays(long agingTicks) {
        return (int) (agingTicks / 24000); // 1 MC-Tag = 24000 Ticks
    }

    /**
     * Konvertiert Tage zu Ticks
     */
    public static long daysToTicks(int days) {
        return (long) days * 24000;
    }

    /**
     * Prüft ob der Reifegrad noch steigen kann
     */
    public boolean canAge() {
        return this != VINTAGE;
    }

    /**
     * Gibt den nächsten Reifegrad zurück
     */
    public ChocolateAgeLevel getNext() {
        return switch (this) {
            case FRESH -> AGED;
            case AGED -> VINTAGE;
            case VINTAGE -> VINTAGE; // Already at max
        };
    }

    /**
     * Berechnet verbleibende Tage bis zum nächsten Reifegrad
     */
    public int getDaysUntilNext(int currentDays) {
        if (!canAge()) return 0;
        return getNext().minDays - currentDays;
    }
}
