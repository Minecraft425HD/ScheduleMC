package de.rolandsw.schedulemc.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet persistente Spieler-Einstellungen
 */
public class PlayerSettingsManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SETTINGS_FILE = new File("world/data/player_settings.json");

    private static Map<String, PlayerSettings> settingsMap = new HashMap<>();

    /**
     * Lädt alle Einstellungen aus der Datei
     */
    public static void load() {
        if (!SETTINGS_FILE.exists()) {
            LOGGER.info("Player settings file doesn't exist yet, creating new one");
            settingsMap = new HashMap<>();
            save();
            return;
        }

        try (FileReader reader = new FileReader(SETTINGS_FILE)) {
            Type type = new TypeToken<Map<String, PlayerSettings>>(){}.getType();
            settingsMap = GSON.fromJson(reader, type);
            if (settingsMap == null) {
                settingsMap = new HashMap<>();
            }
            LOGGER.info("Loaded settings for {} players", settingsMap.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load player settings", e);
            settingsMap = new HashMap<>();
        }
    }

    /**
     * Speichert alle Einstellungen in die Datei
     */
    public static void save() {
        try {
            SETTINGS_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
                GSON.toJson(settingsMap, writer);
            }
            LOGGER.debug("Saved settings for {} players", settingsMap.size());
        } catch (IOException e) {
            LOGGER.error("Failed to save player settings", e);
        }
    }

    /**
     * Holt die Einstellungen für einen Spieler (erstellt neue wenn nicht vorhanden)
     */
    public static PlayerSettings getSettings(UUID playerUUID) {
        String uuidStr = playerUUID.toString();
        return settingsMap.computeIfAbsent(uuidStr, k -> new PlayerSettings(playerUUID));
    }

    /**
     * Aktualisiert die Einstellungen für einen Spieler
     */
    public static void updateSettings(UUID playerUUID, PlayerSettings settings) {
        settings.setPlayerUUID(playerUUID.toString());
        settingsMap.put(playerUUID.toString(), settings);
        save();
    }

    /**
     * Setzt eine spezifische Einstellung
     */
    public static void setUtilityWarningsEnabled(UUID playerUUID, boolean enabled) {
        PlayerSettings settings = getSettings(playerUUID);
        settings.setUtilityWarningsEnabled(enabled);
        save();
    }

    public static void setElectricityThreshold(UUID playerUUID, double threshold) {
        PlayerSettings settings = getSettings(playerUUID);
        settings.setElectricityWarningThreshold(threshold);
        save();
    }

    public static void setWaterThreshold(UUID playerUUID, double threshold) {
        PlayerSettings settings = getSettings(playerUUID);
        settings.setWaterWarningThreshold(threshold);
        save();
    }
}
