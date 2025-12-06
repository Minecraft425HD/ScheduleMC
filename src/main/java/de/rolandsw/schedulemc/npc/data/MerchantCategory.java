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
    AUTOHAENDLER("Autohändler", "Car Dealer");

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

    public String getDisplayName() {
        // TODO: Kann später auf Client-Locale basieren
        return displayNameDE;
    }

    public static MerchantCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BAUMARKT; // Default
    }
}
