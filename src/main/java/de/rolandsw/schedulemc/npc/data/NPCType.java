package de.rolandsw.schedulemc.npc.data;

/**
 * Enum für verschiedene NPC-Typen
 */
public enum NPCType {
    BEWOHNER("Bewohner", "Resident"),
    VERKAEUFER("Verkäufer", "Merchant"),
    POLIZEI("Polizei", "Police");

    private final String displayNameDE;
    private final String displayNameEN;

    NPCType(String displayNameDE, String displayNameEN) {
        this.displayNameDE = displayNameDE;
        this.displayNameEN = displayNameEN;
    }

    public String getDisplayNameDE() {
        return displayNameDE;
    }

    public String getDisplayNameEN() {
        return displayNameEN;
    }

    /**
     * Gibt den Display-Name basierend auf der Client-Locale zurück
     * Falls Client-Side: Automatische Sprachwahl
     * Falls Server-Side: Deutsch (Standard)
     *
     * @return Lokalisierter Display-Name
     */
    public String getDisplayName() {
        try {
            // Versuche Client-Locale zu verwenden
            return de.rolandsw.schedulemc.util.LocaleHelper.selectClientLocalized(displayNameDE, displayNameEN);
        } catch (Exception e) {
            // Server-Side Fallback: Deutsch
            return de.rolandsw.schedulemc.util.LocaleHelper.selectServerLocalized(displayNameDE, displayNameEN);
        }
    }

    public static NPCType fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BEWOHNER; // Default
    }
}
