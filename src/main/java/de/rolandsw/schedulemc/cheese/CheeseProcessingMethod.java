package de.rolandsw.schedulemc.cheese;

/**
 * Kase-Verarbeitungsmethoden
 *
 * Beeinflusst Geschmack und Marktpreis
 */
public enum CheeseProcessingMethod {
    NATURAL("Natur", "§f", 1.0),
    SMOKED("Geruchert", "§6", 1.4),
    HERB("Krauterkase", "§a", 1.6);

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;

    CheeseProcessingMethod(String displayName, String colorCode, double priceMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.priceMultiplier = priceMultiplier;
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
}
