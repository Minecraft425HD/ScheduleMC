package de.rolandsw.schedulemc.vehicle.util;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.TireSeasonType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

/**
 * Kompatibilitäts-Schicht für Serene Seasons Mod.
 * Erkennt ob Serene Seasons installiert ist und ermittelt die aktuelle Jahreszeit.
 *
 * Sommerreifen (SUMMER): Optimal in SPRING, SUMMER
 * Winterreifen (WINTER): Optimal in AUTUMN, WINTER
 * Allwetterreifen (ALL_SEASON): Akzeptabel in allen Jahreszeiten
 */
public final class SereneSeasonsCompat {

    private static final String SERENE_SEASONS_MOD_ID = "sereneseasons";

    private static Boolean cachedModPresent;

    private SereneSeasonsCompat() {
        // Utility class
    }

    /**
     * Prüft ob Serene Seasons installiert ist
     */
    public static boolean isSereneSeasonsLoaded() {
        if (cachedModPresent == null) {
            cachedModPresent = ModList.get().isLoaded(SERENE_SEASONS_MOD_ID);
        }
        return cachedModPresent;
    }

    /**
     * Ermittelt ob aktuell Winter-Bedingungen herrschen.
     * Winter-Bedingungen = LATE_AUTUMN, EARLY_WINTER, MID_WINTER, LATE_WINTER
     *
     * Wenn Serene Seasons nicht installiert ist, wird Minecraft-Biom-Temperatur als Fallback verwendet.
     */
    public static boolean isWinterConditions(Level level) {
        if (!isSereneSeasonsLoaded()) {
            return false; // Ohne Serene Seasons keine Winterbedingungen
        }

        try {
            return SereneSeasonsHelper.isWinterSeason(level);
        } catch (Throwable e) {
            // Falls die API sich ändert oder nicht verfügbar ist
            return false;
        }
    }

    /**
     * Ermittelt ob aktuell Sommer-Bedingungen herrschen.
     * Sommer-Bedingungen = LATE_SPRING, EARLY_SUMMER, MID_SUMMER, LATE_SUMMER
     */
    public static boolean isSummerConditions(Level level) {
        if (!isSereneSeasonsLoaded()) {
            return false;
        }

        try {
            return SereneSeasonsHelper.isSummerSeason(level);
        } catch (Throwable e) {
            return false;
        }
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

    /**
     * Isolierte Hilfsklasse um ClassNotFoundException zu vermeiden wenn
     * Serene Seasons nicht installiert ist (Lazy Loading).
     */
    private static class SereneSeasonsHelper {

        static boolean isWinterSeason(Level level) {
            sereneseasons.api.season.Season season = sereneseasons.api.season.SeasonHelper.getCurrentSeason(level);
            if (season == null) return false;

            sereneseasons.api.season.Season.SubSeason subSeason = season.getSubSeason();
            return subSeason == sereneseasons.api.season.Season.SubSeason.LATE_AUTUMN
                || subSeason == sereneseasons.api.season.Season.SubSeason.EARLY_WINTER
                || subSeason == sereneseasons.api.season.Season.SubSeason.MID_WINTER
                || subSeason == sereneseasons.api.season.Season.SubSeason.LATE_WINTER;
        }

        static boolean isSummerSeason(Level level) {
            sereneseasons.api.season.Season season = sereneseasons.api.season.SeasonHelper.getCurrentSeason(level);
            if (season == null) return false;

            sereneseasons.api.season.Season.SubSeason subSeason = season.getSubSeason();
            return subSeason == sereneseasons.api.season.Season.SubSeason.LATE_SPRING
                || subSeason == sereneseasons.api.season.Season.SubSeason.EARLY_SUMMER
                || subSeason == sereneseasons.api.season.Season.SubSeason.MID_SUMMER
                || subSeason == sereneseasons.api.season.Season.SubSeason.LATE_SUMMER;
        }
    }
}
