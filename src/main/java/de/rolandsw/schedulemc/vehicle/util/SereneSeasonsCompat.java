package de.rolandsw.schedulemc.vehicle.util;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.TireSeasonType;
import net.minecraft.world.level.Level;

/**
 * Reifenhaftungs-spezifischer Adapter auf {@link de.rolandsw.schedulemc.util.SereneSeasonsCompat},
 * der zentralen Kompatibilitäts-Schicht für die Serene Seasons Mod.
 *
 * Sommerreifen (SUMMER): Optimal in SPRING, SUMMER
 * Winterreifen (WINTER): Optimal in AUTUMN, WINTER
 * Allwetterreifen (ALL_SEASON): Akzeptabel in allen Jahreszeiten
 */
public final class SereneSeasonsCompat {

    private SereneSeasonsCompat() {
        // Utility class
    }

    /**
     * Prüft ob Serene Seasons installiert ist
     */
    public static boolean isSereneSeasonsLoaded() {
        return de.rolandsw.schedulemc.util.SereneSeasonsCompat.isLoaded();
    }

    /**
     * Ermittelt ob aktuell Winter-Bedingungen herrschen.
     * Winter-Bedingungen = LATE_AUTUMN, EARLY_WINTER, MID_WINTER, LATE_WINTER
     *
     * Wenn Serene Seasons nicht installiert ist, gibt es keine Winterbedingungen.
     */
    public static boolean isWinterConditions(Level level) {
        String name = de.rolandsw.schedulemc.util.SereneSeasonsCompat.getSubSeasonName(level);
        return "LATE_AUTUMN".equals(name)
            || "EARLY_WINTER".equals(name)
            || "MID_WINTER".equals(name)
            || "LATE_WINTER".equals(name);
    }

    /**
     * Ermittelt ob aktuell Sommer-Bedingungen herrschen.
     * Sommer-Bedingungen = LATE_SPRING, EARLY_SUMMER, MID_SUMMER, LATE_SUMMER
     */
    public static boolean isSummerConditions(Level level) {
        String name = de.rolandsw.schedulemc.util.SereneSeasonsCompat.getSubSeasonName(level);
        return "LATE_SPRING".equals(name)
            || "EARLY_SUMMER".equals(name)
            || "MID_SUMMER".equals(name)
            || "LATE_SUMMER".equals(name);
    }

    /**
     * Berechnet den Speed-Modifier basierend auf Reifentyp und aktueller Jahreszeit.
     *
     * @param tireType Der Reifentyp (SUMMER, WINTER, ALL_SEASON)
     * @param level Das aktuelle Level
     * @return Speed-Modifier (0.5 - 1.0)
     */
    public static float getTireSeasonModifier(TireSeasonType tireType, Level level) {
        if (!ModConfigHandler.VEHICLE_SERVER.tireSeasonEnabled.get()) {
            return 1.0F;
        }

        if (!isSereneSeasonsLoaded()) {
            return 1.0F; // Ohne Serene Seasons keine Auswirkung
        }

        float correctMod = ModConfigHandler.VEHICLE_SERVER.tireSeasonCorrectModifier.get().floatValue();
        float wrongMod = ModConfigHandler.VEHICLE_SERVER.tireSeasonWrongModifier.get().floatValue();
        float allSeasonMod = ModConfigHandler.VEHICLE_SERVER.tireSeasonAllSeasonModifier.get().floatValue();

        if (tireType == TireSeasonType.ALL_SEASON) {
            return allSeasonMod;
        }

        boolean isWinter = isWinterConditions(level);
        boolean isSummer = isSummerConditions(level);

        if (tireType == TireSeasonType.SUMMER) {
            if (isWinter) {
                return wrongMod; // Sommerreifen im Winter = schlecht
            }
            return correctMod; // Sommerreifen im Sommer/Frühling = gut
        }

        if (tireType == TireSeasonType.WINTER) {
            if (isSummer) {
                return wrongMod; // Winterreifen im Sommer = schlecht
            }
            return correctMod; // Winterreifen im Winter/Herbst = gut
        }

        return correctMod;
    }
}
