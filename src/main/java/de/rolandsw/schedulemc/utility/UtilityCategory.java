package de.rolandsw.schedulemc.utility;

/**
 * Kategorien für Utility-Verbraucher
 * Wird für Statistiken und Aufschlüsselung verwendet
 */
public enum UtilityCategory {

    // Beleuchtung
    LIGHTING("Beleuchtung", "💡"),

    // Klimatisierung (Heizung, Kühlung, Belüftung)
    CLIMATE("Klimatisierung", "🌡️"),

    // Pflanzentöpfe und Bewässerung
    IRRIGATION("Bewässerung", "💧"),

    // Trocknungs-Equipment
    DRYING("Trocknung", "🌬️"),

    // Fermentierung
    FERMENTATION("Fermentierung", "🍺"),

    // Chemische Verarbeitung (Meth, LSD, MDMA, etc.)
    CHEMICAL("Chemie-Labor", "⚗️"),

    // Mechanische Verarbeitung (Pressen, Mühlen)
    MECHANICAL("Mechanisch", "⚙️"),

    // Verpackung
    PACKAGING("Verpackung", "📦"),

    // Sonstiges
    OTHER("Sonstiges", "📊");

    private final String displayName;
    private final String icon;

    UtilityCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getFormattedName() {
        return icon + " " + displayName;
    }
}
