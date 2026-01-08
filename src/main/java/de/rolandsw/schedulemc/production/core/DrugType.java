package de.rolandsw.schedulemc.production.core;

/**
 * Enum für alle Drogentypen im Mod
 * Wird für das universelle Packaging-System verwendet
 */
public enum DrugType {
    TOBACCO("Tabak", "§6"),
    COCAINE("Kokain", "§f"),
    HEROIN("Heroin", "§7"),
    METH("Meth", "§b"),
    MUSHROOM("Pilze", "§d"),
    CANNABIS("Cannabis", "§a");

    private final String displayName;
    private final String colorCode;

    DrugType(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    /**
     * Parse DrugType from string (case-insensitive)
     */
    public static DrugType fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TOBACCO; // Fallback
        }
    }
}
