package de.rolandsw.schedulemc.chocolate;

/**
 * Schokoladen-Verarbeitungsmethoden
 *
 * Beeinflusst Geschmack, Textur und Marktpreis
 */
public enum ChocolateProcessingMethod {
    PLAIN("Plain", 1.0, "§6", "chocolate.processing.plain"),
    FILLED("Filled", 1.3, "§d", "chocolate.processing.filled"),
    MIXED("Mixed", 1.2, "§e", "chocolate.processing.mixed");

    private final String displayName;
    private final double priceMultiplier;
    private final String colorCode;
    private final String translationKey;

    ChocolateProcessingMethod(String displayName, double priceMultiplier, String colorCode, String translationKey) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
        this.colorCode = colorCode;
        this.translationKey = translationKey;
    }

    public String getDisplayName() {
        return colorCode + displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * Prüft ob diese Methode eine Füllung verwendet
     */
    public boolean isFilled() {
        return this == FILLED;
    }

    /**
     * Prüft ob diese Methode gemischt ist (mit Nüssen, Früchten, etc.)
     */
    public boolean isMixed() {
        return this == MIXED;
    }

    /**
     * Prüft ob diese Methode einfach/pur ist
     */
    public boolean isPlain() {
        return this == PLAIN;
    }

    /**
     * Gibt die Beschreibung der Verarbeitungsmethode zurück
     */
    public String getDescription() {
        return switch (this) {
            case PLAIN -> "Pure chocolate without any additions";
            case FILLED -> "Chocolate with filling (caramel, nougat, etc.)";
            case MIXED -> "Chocolate mixed with nuts, fruits, or other ingredients";
        };
    }

    /**
     * Gibt die empfohlenen Zutaten für diese Methode zurück
     */
    public String[] getRecommendedIngredients() {
        return switch (this) {
            case PLAIN -> new String[]{"Cocoa Mass", "Cocoa Butter", "Sugar"};
            case FILLED -> new String[]{"Cocoa Mass", "Cocoa Butter", "Sugar", "Caramel", "Nougat"};
            case MIXED -> new String[]{"Cocoa Mass", "Cocoa Butter", "Sugar", "Nuts", "Dried Fruits"};
        };
    }
}
