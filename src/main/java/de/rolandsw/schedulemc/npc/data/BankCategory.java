package de.rolandsw.schedulemc.npc.data;

/**
 * Enum für verschiedene Bank-Kategorien
 * Nur relevant wenn NPCType == BANK
 */
public enum BankCategory {
    BANKER("Banker", "Banker"),
    BOERSE("Börsenmakler", "Stock Broker");

    private final String displayNameDE;
    private final String displayNameEN;

    BankCategory(String displayNameDE, String displayNameEN) {
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

    public static BankCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BANKER; // Default
    }
}
