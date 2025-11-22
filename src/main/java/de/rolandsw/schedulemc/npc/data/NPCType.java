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

    public String getDisplayName() {
        // TODO: Kann später auf Client-Locale basieren
        return displayNameDE;
    }

    public static NPCType fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BEWOHNER; // Default
    }
}
