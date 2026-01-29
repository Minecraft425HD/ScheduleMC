package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

/**
 * Reifentyp basierend auf Jahreszeit.
 *
 * SUMMER: Sommerreifen - optimal bei Frühling/Sommer, schlecht im Winter
 * WINTER: Winterreifen - optimal bei Herbst/Winter, schlecht im Sommer
 * ALL_SEASON: Ganzjahresreifen - akzeptabel in allen Jahreszeiten, aber nie optimal
 */
public enum TireSeasonType {
    SUMMER,
    WINTER,
    ALL_SEASON
}
