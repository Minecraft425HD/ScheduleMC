package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

/**
 * Reifentyp mit Jahreszeit-Zuordnung und Rädergröße.
 * largeWheel = true → Reifen für Offroad/LKW-Chassis (big_wheel.obj)
 * largeWheel = false → Reifen für normale Fahrzeuge (wheel.obj)
 */
public enum TireType {
    STANDARD (TireSeasonType.SUMMER,    false),
    SPORT    (TireSeasonType.SUMMER,    false),
    PREMIUM  (TireSeasonType.SUMMER,    false),
    WINTER   (TireSeasonType.WINTER,    false),
    OFFROAD  (TireSeasonType.ALL_SEASON, true),
    ALLTERRAIN(TireSeasonType.ALL_SEASON, true),
    HEAVYDUTY(TireSeasonType.ALL_SEASON, true);

    private final TireSeasonType seasonType;
    private final boolean largeWheel;

    TireType(TireSeasonType seasonType, boolean largeWheel) {
        this.seasonType = seasonType;
        this.largeWheel = largeWheel;
    }

    public TireSeasonType getSeasonType() {
        return seasonType;
    }

    public boolean isLargeWheel() {
        return largeWheel;
    }
}
