package de.rolandsw.schedulemc.honey;

/**
 * Honig-Verarbeitungsmethoden
 *
 * Beeinflusst Textur, Aussehen und Marktpreis
 */
public enum HoneyProcessingMethod {
    LIQUID("Liquid", "§6", 1.0, "honey.processing.liquid"),           // Flüssiger Honig (Standard)
    CREAMED("Creamed", "§e", 1.2, "honey.processing.creamed"),        // Cremiger Honig (kontrollierte Kristallisation)
    CHUNK("Chunk", "§c", 1.4, "honey.processing.chunk");              // Wabenhonig (mit Wabenstücken)

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;
    private final String translationKey;

    HoneyProcessingMethod(String displayName, String colorCode, double priceMultiplier, String translationKey) {
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
     * Gibt eine Beschreibung der Verarbeitungsmethode zurück
     */
    public String getDescription() {
        return switch (this) {
            case LIQUID -> "Flüssiger Honig, direkt aus der Wabe extrahiert";
            case CREAMED -> "Cremiger Honig durch kontrollierte Kristallisation";
            case CHUNK -> "Wabenhonig mit natürlichen Wabenstücken";
        };
    }

    /**
     * Prüft ob diese Verarbeitungsmethode besondere Ausrüstung benötigt
     */
    public boolean requiresSpecialEquipment() {
        return switch (this) {
            case LIQUID -> false;  // Kann mit Standardausrüstung hergestellt werden
            case CREAMED -> true;  // Benötigt Cremiermaschine
            case CHUNK -> true;    // Benötigt spezielle Verpackung
        };
    }

    /**
     * Gibt die benötigte Verarbeitungszeit in Ticks zurück
     */
    public int getProcessingTicks() {
        return switch (this) {
            case LIQUID -> 200;    // 10 Sekunden
            case CREAMED -> 600;   // 30 Sekunden
            case CHUNK -> 400;     // 20 Sekunden
        };
    }
}
