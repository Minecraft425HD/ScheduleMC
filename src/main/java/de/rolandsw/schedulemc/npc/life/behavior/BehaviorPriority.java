package de.rolandsw.schedulemc.npc.life.behavior;

/**
 * BehaviorPriority - Prioritätsstufen für NPC-Verhalten
 *
 * Höhere Priorität überschreibt niedrigere.
 * Ermöglicht feingranulare Steuerung welches Verhalten
 * aktiv wird.
 */
public enum BehaviorPriority {

    /**
     * Niedrigste Priorität - Standard-Verhalten
     * Beispiel: Zufälliges Umherschauen, Idle-Animationen
     */
    LOWEST(0, "Minimal"),

    /**
     * Niedrige Priorität - Routinen
     * Beispiel: Zum Arbeitsplatz gehen, Tagesablauf
     */
    LOW(100, "Niedrig"),

    /**
     * Normale Priorität - Reguläre Aktivitäten
     * Beispiel: Arbeiten, Handeln, Gespräche
     */
    NORMAL(200, "Normal"),

    /**
     * Hohe Priorität - Wichtige Ereignisse
     * Beispiel: Auf Spieler reagieren, Verdächtig werden
     */
    HIGH(300, "Hoch"),

    /**
     * Höchste Priorität - Notfälle
     * Beispiel: Fliehen, Polizei alarmieren, Verstecken
     */
    CRITICAL(400, "Kritisch"),

    /**
     * Override-Priorität - Nur für Systembefehle
     * Beispiel: Admin-Teleport, Quest-erzwungenes Verhalten
     */
    OVERRIDE(500, "Override");

    private final int value;
    private final String displayName;

    BehaviorPriority(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Numerischer Wert für Vergleiche
     */
    public int getValue() {
        return value;
    }

    /**
     * Anzeigename für UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Prüft ob diese Priorität höher ist als eine andere
     */
    public boolean isHigherThan(BehaviorPriority other) {
        return this.value > other.value;
    }

    /**
     * Prüft ob diese Priorität gleich oder höher ist als eine andere
     */
    public boolean isAtLeast(BehaviorPriority other) {
        return this.value >= other.value;
    }

    /**
     * Gibt die höhere von zwei Prioritäten zurück
     */
    public static BehaviorPriority max(BehaviorPriority a, BehaviorPriority b) {
        return a.value >= b.value ? a : b;
    }

    /**
     * Gibt BehaviorPriority aus Ordinal zurück (mit Fallback)
     */
    public static BehaviorPriority fromOrdinal(int ordinal) {
        BehaviorPriority[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return NORMAL;
    }

    /**
     * Gibt BehaviorPriority aus numerischem Wert zurück (nächste Stufe)
     */
    public static BehaviorPriority fromValue(int value) {
        for (BehaviorPriority priority : values()) {
            if (priority.value >= value) {
                return priority;
            }
        }
        return OVERRIDE;
    }
}
