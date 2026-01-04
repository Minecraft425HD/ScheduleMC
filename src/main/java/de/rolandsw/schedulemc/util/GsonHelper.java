package de.rolandsw.schedulemc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Zentrale Gson-Instanz für alle Manager
 *
 * OPTIMIERT: Automatische Erkennung von Produktions-Umgebung
 * - Entwicklung: Pretty Printing für lesbare JSON-Dateien
 * - Produktion: Kompaktes JSON für ~30% weniger Dateigröße
 *
 * Vermeidet mehrfache Instanz-Erstellung und bietet konsistente Serialisierung
 */
public class GsonHelper {

    /**
     * Erkennt ob wir in einer Produktionsumgebung laufen
     * (keine IDE-Eigenschaften, Forge DevLaunch nicht aktiv)
     */
    private static final boolean IS_PRODUCTION = detectProductionEnvironment();

    /**
     * Shared Gson-Instanz mit Pretty Printing für lesbares JSON
     */
    private static final Gson GSON_PRETTY = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Shared Gson-Instanz ohne Pretty Printing für kompaktes JSON
     */
    private static final Gson GSON_COMPACT = new GsonBuilder()
            .create();

    /**
     * Prüft ob wir in einer Entwicklungsumgebung laufen
     */
    private static boolean detectProductionEnvironment() {
        // Forge/FML setzt diese Property im Dev-Modus
        String fmlDevLaunch = System.getProperty("fml.devLaunch");
        if ("true".equalsIgnoreCase(fmlDevLaunch)) {
            return false; // Entwicklung
        }

        // IDE-spezifische Erkennung
        String javaClassPath = System.getProperty("java.class.path", "");
        if (javaClassPath.contains("idea_rt.jar") ||
            javaClassPath.contains("eclipse.launcher") ||
            javaClassPath.contains("gradle-worker")) {
            return false; // Entwicklung via IDE
        }

        // Standard: Produktion
        return true;
    }

    /**
     * Gibt die umgebungsabhängige Gson-Instanz zurück
     * - Entwicklung: Pretty Printing
     * - Produktion: Kompakt
     */
    public static Gson get() {
        return IS_PRODUCTION ? GSON_COMPACT : GSON_PRETTY;
    }

    /**
     * Gibt explizit die Pretty-Print Gson-Instanz zurück
     * (für Fälle wo Lesbarkeit wichtiger ist als Größe)
     */
    public static Gson getPretty() {
        return GSON_PRETTY;
    }

    /**
     * Gibt explizit die kompakte Gson-Instanz zurück
     */
    public static Gson getCompact() {
        return GSON_COMPACT;
    }

    /**
     * Gibt zurück ob wir in Produktion laufen
     */
    public static boolean isProduction() {
        return IS_PRODUCTION;
    }

    // Prevent instantiation
    private GsonHelper() {
        throw new UnsupportedOperationException("Utility class");
    }
}
