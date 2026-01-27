package de.rolandsw.schedulemc.wine;

/**
 * Wein-Verarbeitungsmethoden
 *
 * Beeinflusst Geschmack und Marktpreis
 */
public enum WineProcessingMethod {
    DRY("Trocken", "§f", 1.0),          // Standard (0-9g Restzucker/Liter)
    SEMI_DRY("Halbtrocken", "§e", 1.1), // Leicht süßlich (9-18g)
    SWEET("Süß", "§6", 1.3),            // Deutlich süß (18-45g)
    DESSERT("Dessertwein", "§d", 1.8);  // Sehr süß (>45g)

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;

    WineProcessingMethod(String displayName, String colorCode, double priceMultiplier) {
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
