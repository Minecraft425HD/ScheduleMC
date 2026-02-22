package de.rolandsw.schedulemc.mission;

/**
 * Status einer Spieler-Mission.
 */
public enum MissionStatus {
    /** Verfügbar, aber noch nicht angenommen */
    AVAILABLE,
    /** Aktiv (angenommen, in Bearbeitung) */
    ACTIVE,
    /** Ziel erreicht, Belohnung noch nicht abgeholt */
    COMPLETED,
    /** Belohnung abgeholt */
    CLAIMED
}
