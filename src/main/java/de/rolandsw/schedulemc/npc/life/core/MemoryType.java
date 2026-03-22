package de.rolandsw.schedulemc.npc.life.core;

/**
 * Enum für die verschiedenen Erinnerungstypen eines NPCs.
 */
public enum MemoryType {

    /**
     * Transaktion - Kauf oder Verkauf mit dem Spieler
     */
    TRANSACTION("Transaktion", 3, false),

    /**
     * Verbrechen gesehen - NPC war Zeuge eines Verbrechens
     */
    CRIME_WITNESSED("Verbrechen gesehen", 8, true),

    /**
     * Opfer eines Verbrechens - NPC wurde selbst Opfer
     */
    CRIME_VICTIM("Opfer", 10, true),

    /**
     * Gespräch - Unterhaltung mit dem Spieler
     */
    CONVERSATION("Gespräch", 2, false),

    /**
     * Geschenk erhalten - Spieler hat NPC etwas geschenkt
     */
    GIFT_RECEIVED("Geschenk", 6, false),

    /**
     * Hilfe erhalten - Spieler hat NPC geholfen
     */
    HELP_RECEIVED("Hilfe", 7, false),

    /**
     * Bedrohung erhalten - Spieler hat NPC bedroht
     */
    THREAT_RECEIVED("Bedrohung", 9, true),

    /**
     * Versprechen - Spieler hat NPC etwas versprochen
     */
    PROMISE_MADE("Versprechen", 5, false),

    /**
     * Gerücht gehört - NPC hat ein Gerücht über den Spieler gehört
     */
    RUMOR_HEARD("Gerücht", 4, false),

    /**
     * Quest-bezogen - Interaktion im Rahmen einer Quest
     */
    QUEST_RELATED("Quest", 6, false),

    /**
     * Handel - Spieler hat mit NPC gehandelt (Alias für TRANSACTION)
     */
    TRADED("Handel", 3, false),

    /**
     * Quest abgeschlossen - Spieler hat eine Quest für NPC abgeschlossen
     */
    QUEST_COMPLETED("Quest abgeschlossen", 7, false),

    /**
     * Geholfen - Spieler hat NPC geholfen (Alias für HELP_RECEIVED)
     */
    HELPED("Geholfen", 7, false),

    /**
     * Bestechung angeboten - Spieler hat versucht den NPC zu bestechen
     */
    BRIBE_OFFERED("Bestechung", 6, true);

    private final String displayName;
    private final int defaultImportance;
    private final boolean isNegative;

    MemoryType(String displayName, int defaultImportance, boolean isNegative) {
        this.displayName = displayName;
        this.defaultImportance = defaultImportance;
        this.isNegative = isNegative;
    }

    /**
     * Anzeigename der Erinnerung
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Standard-Wichtigkeit (1-10)
     * Beeinflusst wie lange die Erinnerung behalten wird
     */
    public int getDefaultImportance() {
        return defaultImportance;
    }

    /**
     * Prüft ob dies eine negative Erinnerung ist
     */
    public boolean isNegative() {
        return isNegative;
    }

    /**
     * Berechnet wie viele Tage die Erinnerung behalten wird
     * Formel: Wichtigkeit × 7 Tage
     * @param importance Aktuelle Wichtigkeit (1-10)
     * @return Anzahl der Tage
     */
    public int getRetentionDays(int importance) {
        return importance * 7;
    }

    /**
     * Übersetzungsschlüssel für Lokalisierung
     */
    public String getTranslationKey() {
        return "npc.memory." + name().toLowerCase();
    }

    /**
     * Gibt MemoryType aus Ordinal zurück (mit Fallback)
     */
    public static MemoryType fromOrdinal(int ordinal) {
        MemoryType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return CONVERSATION;
    }

    /**
     * Gibt MemoryType aus Name zurück (mit Fallback)
     */
    public static MemoryType fromName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CONVERSATION;
        }
    }
}
