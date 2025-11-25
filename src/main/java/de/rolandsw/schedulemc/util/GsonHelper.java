package de.rolandsw.schedulemc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Zentrale Gson-Instanz für alle Manager
 *
 * Vermeidet mehrfache Instanz-Erstellung und bietet konsistente Serialisierung
 */
public class GsonHelper {

    /**
     * Shared Gson-Instanz mit Pretty Printing für lesbares JSON
     */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Shared Gson-Instanz ohne Pretty Printing für kompaktes JSON
     */
    private static final Gson GSON_COMPACT = new GsonBuilder()
            .create();

    /**
     * Gibt die Pretty-Print Gson-Instanz zurück
     */
    public static Gson get() {
        return GSON;
    }

    /**
     * Gibt die kompakte Gson-Instanz zurück
     */
    public static Gson getCompact() {
        return GSON_COMPACT;
    }

    // Prevent instantiation
    private GsonHelper() {
        throw new UnsupportedOperationException("Utility class");
    }
}
