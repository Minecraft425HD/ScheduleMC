package de.rolandsw.schedulemc.util;

import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

/**
 * Zentrale Kompatibilitäts-Schicht für die Serene Seasons Mod.
 * Erkennt ob Serene Seasons installiert ist und ermittelt die aktuelle Jahreszeit
 * per Reflection (Soft-Dependency, kein compileOnly-Zwang).
 *
 * Wird sowohl von der Reifenhaftung (vehicle.util.SereneSeasonsCompat) als auch
 * vom saisonalen Preissystem (market.SeasonalPriceModifier) genutzt, damit beide
 * Systeme dieselbe echte Jahreszeit verwenden statt eigener, potenziell
 * abweichender Berechnungen.
 */
public final class SereneSeasonsCompat {

    private static final String SERENE_SEASONS_MOD_ID = "sereneseasons";

    private static Boolean cachedModPresent;

    // PERFORMANCE: Reflection-Ergebnisse gecacht (einmaliges Lookup statt pro Aufruf).
    private static java.lang.reflect.Method cachedGetCurrentSeason;
    private static java.lang.reflect.Method cachedGetSubSeason;
    private static boolean reflectionInitialized = false;
    private static boolean reflectionFailed = false;

    private SereneSeasonsCompat() {
        // Utility class
    }

    public enum MainSeason {
        SPRING, SUMMER, AUTUMN, WINTER
    }

    public static boolean isLoaded() {
        synchronized (SereneSeasonsCompat.class) {
            if (cachedModPresent == null) {
                cachedModPresent = ModList.get().isLoaded(SERENE_SEASONS_MOD_ID);
            }
            return cachedModPresent;
        }
    }

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

    /**
     * Gibt den rohen Serene-Seasons-Sub-Season-Namen zurueck (z.B. "MID_WINTER"),
     * oder "" wenn Serene Seasons nicht installiert ist oder die API fehlschlaegt.
     */
    public static String getSubSeasonName(Level level) {
        if (!isLoaded()) return "";
        initReflection();
        if (reflectionFailed) return "";
        try {
            Object season = cachedGetCurrentSeason.invoke(null, level);
            if (season == null) return "";

            synchronized (SereneSeasonsCompat.class) {
                if (cachedGetSubSeason == null) {
                    cachedGetSubSeason = season.getClass().getMethod("getSubSeason");
                }
            }
            Object subSeason = cachedGetSubSeason.invoke(season);
            return subSeason != null ? subSeason.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Ordnet die aktuelle Serene-Seasons-Sub-Season einer der 4 Hauptjahreszeiten zu.
     *
     * @return die Hauptjahreszeit, oder {@code null} wenn Serene Seasons nicht
     *         installiert ist oder die aktuelle Sub-Season nicht ermittelt werden konnte.
     */
    public static MainSeason getMainSeason(Level level) {
        return mainSeasonFromSubSeasonName(getSubSeasonName(level));
    }

    /**
     * Reine Zuordnungsfunktion Sub-Season-Name -> Hauptjahreszeit, ohne erneuten
     * Reflection-Aufruf. Nuetzlich wenn der Sub-Season-Name bereits vorliegt.
     */
    public static MainSeason mainSeasonFromSubSeasonName(String subSeasonName) {
        if (subSeasonName == null || subSeasonName.isEmpty()) return null;
        if (subSeasonName.contains("SPRING")) return MainSeason.SPRING;
        if (subSeasonName.contains("SUMMER")) return MainSeason.SUMMER;
        if (subSeasonName.contains("AUTUMN")) return MainSeason.AUTUMN;
        if (subSeasonName.contains("WINTER")) return MainSeason.WINTER;
        return null;
    }

    /**
     * Ordnet eine Sub-Season ihrer Phase innerhalb der Hauptjahreszeit zu
     * (0 = frueh, 1 = Mitte, 2 = spaet). Unbekannte Namen ergeben 1 (Mitte).
     */
    public static int phaseIndexFromSubSeasonName(String subSeasonName) {
        if (subSeasonName == null) return 1;
        if (subSeasonName.startsWith("EARLY")) return 0;
        if (subSeasonName.startsWith("LATE")) return 2;
        return 1; // MID_* oder unbekannt
    }
}
