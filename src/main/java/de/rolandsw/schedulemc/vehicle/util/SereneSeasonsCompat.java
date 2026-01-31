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
     *
     * PERFORMANCE: Reflection-Ergebnisse (Class, Method) werden gecacht.
     * Vorher: Class.forName() + getMethod() bei JEDEM Aufruf (potenziell jeden Tick).
     * Nachher: Einmaliges Lookup, danach nur noch Method.invoke().
     */
    private static class SereneSeasonsHelper {

        // PERFORMANCE: Gecachte Reflection-Referenzen (einmaliges Lookup)
        private static java.lang.reflect.Method cachedGetCurrentSeason;
        private static java.lang.reflect.Method cachedGetSubSeason;
        private static boolean reflectionInitialized = false;
        private static boolean reflectionFailed = false;

        private static void initReflection() {
            if (reflectionInitialized) return;
            reflectionInitialized = true;
            try {
                Class<?> seasonHelperClass = Class.forName("sereneseasons.api.season.SeasonHelper");
                cachedGetCurrentSeason = seasonHelperClass.getMethod("getCurrentSeason", Level.class);
            } catch (Exception e) {
                reflectionFailed = true;
            }
        }

        private static String getSubSeasonName(Level level) {
            initReflection();
            if (reflectionFailed) return "";
            try {
                Object season = cachedGetCurrentSeason.invoke(null, level);
                if (season == null) return "";

                // Cache getSubSeason beim ersten erfolgreichen Aufruf
                if (cachedGetSubSeason == null) {
                    cachedGetSubSeason = season.getClass().getMethod("getSubSeason");
                }
                Object subSeason = cachedGetSubSeason.invoke(season);
                return subSeason != null ? subSeason.toString() : "";
            } catch (Exception e) {
                return "";
            }
        }

        static boolean isWinterSeason(Level level) {
            String name = getSubSeasonName(level);
            return "LATE_AUTUMN".equals(name)
                || "EARLY_WINTER".equals(name)
                || "MID_WINTER".equals(name)
                || "LATE_WINTER".equals(name);
        }

        static boolean isSummerSeason(Level level) {
            String name = getSubSeasonName(level);
            return "LATE_SPRING".equals(name)
                || "EARLY_SUMMER".equals(name)
                || "MID_SUMMER".equals(name)
                || "LATE_SUMMER".equals(name);
        }
    }
}
