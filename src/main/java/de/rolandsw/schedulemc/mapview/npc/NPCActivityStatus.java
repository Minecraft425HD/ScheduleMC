package de.rolandsw.schedulemc.mapview.npc;

/**
 * NPCActivityStatus - Status des NPCs für Kartenanzeige
 *
 * Wird zum Client synchronisiert um zu bestimmen,
 * ob der NPC auf der Karte angezeigt werden soll.
 */
public enum NPCActivityStatus {
    /** NPC ist unterwegs (Freizeit, Patrouille, etc.) - SICHTBAR */
    ROAMING(true),

    /** NPC ist auf der Arbeit - NICHT sichtbar */
    AT_WORK(false),

    /** NPC ist zuhause - NICHT sichtbar */
    AT_HOME(false),

    /** NPC ist auf Patrouille (Polizei) - Polizei wird ohnehin gefiltert */
    ON_PATROL(true),

    /** NPC ist an der Polizeistation - Polizei wird ohnehin gefiltert */
    AT_STATION(true),

    /** Unbekannter Status - SICHTBAR (Fallback) */
    UNKNOWN(true);

    private final boolean visibleOnMap;

    NPCActivityStatus(boolean visibleOnMap) {
        this.visibleOnMap = visibleOnMap;
    }

    /**
     * Prüft ob NPCs mit diesem Status auf der Karte angezeigt werden sollen
     */
    public boolean isVisibleOnMap() {
        return visibleOnMap;
    }

    /**
     * Konvertiert Ordinal zu Enum
     */
    public static NPCActivityStatus fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return UNKNOWN;
    }
}
