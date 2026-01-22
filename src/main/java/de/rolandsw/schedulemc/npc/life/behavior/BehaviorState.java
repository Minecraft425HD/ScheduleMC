package de.rolandsw.schedulemc.npc.life.behavior;

/**
 * BehaviorState - Aktuelle Verhaltens-Zustände eines NPCs
 *
 * Definiert was der NPC gerade tut und beeinflusst
 * welche Aktionen verfügbar sind.
 */
public enum BehaviorState {

    /**
     * Normaler Zustand - NPC folgt Tagesablauf
     */
    IDLE("Ruhend", false, true),

    /**
     * NPC arbeitet am Arbeitsplatz
     */
    WORKING("Arbeitend", false, true),

    /**
     * NPC ist auf dem Weg zu einem Ziel
     */
    TRAVELING("Unterwegs", false, true),

    /**
     * NPC handelt mit einem Spieler
     */
    TRADING("Handelt", false, false),

    /**
     * NPC führt ein Gespräch
     */
    CONVERSING("Im Gespräch", false, false),

    /**
     * NPC untersucht etwas Verdächtiges
     */
    INVESTIGATING("Untersucht", true, true),

    /**
     * NPC flieht vor einer Bedrohung
     */
    FLEEING("Flieht", true, false),

    /**
     * NPC ist alarmiert und sucht Hilfe
     */
    ALERTING("Alarmiert", true, false),

    /**
     * NPC versteckt sich vor Gefahr
     */
    HIDING("Versteckt sich", true, false),

    /**
     * NPC patrouilliert (nur Polizei)
     */
    PATROLLING("Patrouilliert", false, true),

    /**
     * NPC verfolgt einen Verdächtigen (nur Polizei)
     */
    PURSUING("Verfolgt", true, false),

    /**
     * NPC schläft oder ruht
     */
    SLEEPING("Schläft", false, false),

    /**
     * NPC wartet auf etwas
     */
    WAITING("Wartet", false, true),

    /**
     * NPC interagiert mit einem anderen NPC
     */
    SOCIALIZING("Soziale Interaktion", false, true);

    private final String displayName;
    private final boolean isEmergency;
    private final boolean canBeInterrupted;

    BehaviorState(String displayName, boolean isEmergency, boolean canBeInterrupted) {
        this.displayName = displayName;
        this.isEmergency = isEmergency;
        this.canBeInterrupted = canBeInterrupted;
    }

    /**
     * Anzeigename für UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Ist dies ein Notfall-Zustand?
     * Notfall-Zustände haben höhere Priorität
     */
    public boolean isEmergency() {
        return isEmergency;
    }

    /**
     * Kann dieser Zustand von einem anderen Zustand unterbrochen werden?
     */
    public boolean canBeInterrupted() {
        return canBeInterrupted;
    }

    /**
     * Prüft ob der NPC in diesem Zustand handeln kann
     */
    public boolean canTrade() {
        return this == IDLE || this == WORKING || this == WAITING;
    }

    /**
     * Prüft ob der NPC in diesem Zustand sprechen kann
     */
    public boolean canConverse() {
        return !isEmergency && this != SLEEPING && this != TRADING;
    }

    /**
     * Prüft ob der NPC in diesem Zustand seinem Zeitplan folgen kann
     */
    public boolean canFollowSchedule() {
        return !isEmergency && this != CONVERSING && this != TRADING;
    }

    /**
     * Gibt den Zustand mit höherer Priorität zurück
     */
    public static BehaviorState getHigherPriority(BehaviorState a, BehaviorState b) {
        if (a.isEmergency && !b.isEmergency) return a;
        if (b.isEmergency && !a.isEmergency) return b;
        return a.ordinal() > b.ordinal() ? a : b;
    }

    /**
     * Gibt BehaviorState aus Ordinal zurück (mit Fallback)
     */
    public static BehaviorState fromOrdinal(int ordinal) {
        BehaviorState[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return IDLE;
    }

    /**
     * Übersetzungsschlüssel für Lokalisierung
     */
    public String getTranslationKey() {
        return "npc.behavior." + name().toLowerCase();
    }
}
