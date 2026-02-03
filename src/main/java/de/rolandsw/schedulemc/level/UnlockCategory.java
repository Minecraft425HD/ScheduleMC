package de.rolandsw.schedulemc.level;

/**
 * Kategorien von freischaltbaren Inhalten im Level-System.
 */
public enum UnlockCategory {

    PRODUCTION_CHAIN("Produktionskette", "§a", "Neue Produkte herstellen"),
    MACHINE("Maschine", "§b", "Bessere Maschinen nutzen"),
    POT("Topf/Behälter", "§6", "Größere Anbau-Kapazität"),
    QUALITY("Qualitätsstufe", "§d", "Höhere Qualität erreichen"),
    STRAIN("Sorte/Variante", "§e", "Neue Sorten anbauen"),
    PROCESSING("Verarbeitungsmethode", "§5", "Neue Verarbeitungsarten"),
    VEHICLE("Fahrzeug", "§7", "Bessere Fahrzeuge kaufen"),
    SHOP_FEATURE("Shop-Feature", "§f", "Erweiterte Shop-Funktionen"),
    ECONOMY_FEATURE("Wirtschafts-Feature", "§6", "Erweiterte Wirtschafts-Features");

    private final String displayName;
    private final String colorCode;
    private final String description;

    UnlockCategory(String displayName, String colorCode, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedName() {
        return colorCode + displayName + "§r";
    }
}
