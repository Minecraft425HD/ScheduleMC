package de.rolandsw.schedulemc.beer;

/**
 * Bier-Verarbeitungsmethoden
 *
 * Beeinflusst Geschmack, Frische und Marktpreis
 */
public enum BeerProcessingMethod {
    DRAFT("Draft", "§6", 1.2, "beer.processing.draft"),        // Fass/Keg (frischeste Form)
    BOTTLED("Bottled", "§e", 1.0, "beer.processing.bottled"),  // Flasche (Standard)
    CANNED("Canned", "§7", 0.9, "beer.processing.canned");     // Dose (praktisch, aber weniger wertvoll)

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;
    private final String translationKey;

    BeerProcessingMethod(String displayName, String colorCode, double priceMultiplier, String translationKey) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.priceMultiplier = priceMultiplier;
        this.translationKey = translationKey;
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

    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * Prüft ob das Bier als Fass-Bier serviert wird
     */
    public boolean isDraft() {
        return this == DRAFT;
    }

    /**
     * Prüft ob das Bier in Flaschen abgefüllt ist
     */
    public boolean isBottled() {
        return this == BOTTLED;
    }

    /**
     * Prüft ob das Bier in Dosen abgefüllt ist
     */
    public boolean isCanned() {
        return this == CANNED;
    }
}
