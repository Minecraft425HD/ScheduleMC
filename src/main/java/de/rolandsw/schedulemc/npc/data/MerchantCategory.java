package de.rolandsw.schedulemc.npc.data;

/**
 * Enum für verschiedene Verkäufer-Kategorien
 * Nur relevant wenn NPCType == VERKAEUFER
 */
public enum MerchantCategory {
    BAUMARKT("Baumarkt", "Hardware Store"),
    WAFFENHAENDLER("Waffenhändler", "Gun Shop"),
    TANKSTELLE("Tankstelle", "Gas Station"),
    LEBENSMITTEL("Lebensmittel", "Grocery Store"),
    PERSONALMANAGEMENT("Personalmanagement", "HR Management"),
    ILLEGALER_HAENDLER("Illegaler Händler", "Black Market"),
    AUTOHAENDLER("Autohändler", "Vehicle Dealer");

    private final String displayNameDE;
    private final String displayNameEN;

    MerchantCategory(String displayNameDE, String displayNameEN) {
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

    public static MerchantCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BAUMARKT; // Default
    }
}
