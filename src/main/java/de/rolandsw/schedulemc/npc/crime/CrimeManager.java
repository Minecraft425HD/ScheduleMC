package de.rolandsw.schedulemc.npc.crime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet Wanted-Level (Fahndungsstufen) für Spieler
 * 0 Sterne = Sauber
 * 1-2 Sterne = Kleinkriminalität
 * 3-4 Sterne = Schwere Straftaten
 * 5 Sterne = Höchste Fahndungsstufe
 */
public class CrimeManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CRIME_FILE = new File("plotmod_crimes.json");
    private static final int MAX_WANTED_LEVEL = 5;

    // UUID -> Wanted Level
    private static Map<UUID, Integer> wantedLevels = new HashMap<>();
    // UUID -> Last Crime Day (für automatischen Abbau)
    private static Map<UUID, Long> lastCrimeDay = new HashMap<>();

    /**
     * Lädt Crime-Daten vom Disk
     */
    public static void load() {
        if (!CRIME_FILE.exists()) {
            LOGGER.info("Keine Crime-Daten gefunden, starte mit leerer Datenbank");
            return;
        }

        try (FileReader reader = new FileReader(CRIME_FILE)) {
            CrimeData data = GSON.fromJson(reader, CrimeData.class);

            if (data != null) {
                wantedLevels.clear();
                lastCrimeDay.clear();

                for (Map.Entry<String, Integer> entry : data.wantedLevels.entrySet()) {
                    wantedLevels.put(UUID.fromString(entry.getKey()), entry.getValue());
                }

                for (Map.Entry<String, Long> entry : data.lastCrimeDay.entrySet()) {
                    lastCrimeDay.put(UUID.fromString(entry.getKey()), entry.getValue());
                }

                LOGGER.info("Crime-Daten geladen: {} Spieler", wantedLevels.size());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Crime-Daten!", e);
        }
    }

    /**
     * Speichert Crime-Daten auf Disk
     */
    public static void save() {
        try (FileWriter writer = new FileWriter(CRIME_FILE)) {
            CrimeData data = new CrimeData();

            for (Map.Entry<UUID, Integer> entry : wantedLevels.entrySet()) {
                data.wantedLevels.put(entry.getKey().toString(), entry.getValue());
            }

            for (Map.Entry<UUID, Long> entry : lastCrimeDay.entrySet()) {
                data.lastCrimeDay.put(entry.getKey().toString(), entry.getValue());
            }

            GSON.toJson(data, writer);
            LOGGER.info("Crime-Daten gespeichert: {} Spieler", wantedLevels.size());
        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der Crime-Daten!", e);
        }
    }

    /**
     * Gibt aktuelles Wanted-Level zurück
     */
    public static int getWantedLevel(UUID playerUUID) {
        return wantedLevels.getOrDefault(playerUUID, 0);
    }

    /**
     * Fügt Wanted-Level hinzu (max 5)
     */
    public static void addWantedLevel(UUID playerUUID, int amount, long currentDay) {
        int current = getWantedLevel(playerUUID);
        int newLevel = Math.min(MAX_WANTED_LEVEL, current + amount);

        wantedLevels.put(playerUUID, newLevel);
        lastCrimeDay.put(playerUUID, currentDay);

        save();

        LOGGER.info("Player {} Wanted-Level: {} -> {} (Verbrechen an Tag {})",
            playerUUID, current, newLevel, currentDay);
    }

    /**
     * Setzt Wanted-Level auf 0 (z.B. nach Festnahme)
     */
    public static void clearWantedLevel(UUID playerUUID) {
        wantedLevels.remove(playerUUID);
        lastCrimeDay.remove(playerUUID);
        save();

        LOGGER.info("Player {} Wanted-Level gelöscht", playerUUID);
    }

    /**
     * Reduziert Wanted-Level über Zeit
     * Sollte täglich aufgerufen werden (pro Minecraft-Tag)
     */
    public static void decayWantedLevel(UUID playerUUID, long currentDay) {
        if (!wantedLevels.containsKey(playerUUID)) return;

        Long lastCrime = lastCrimeDay.get(playerUUID);
        if (lastCrime == null) return;

        // Pro Tag ohne Verbrechen: -1 Stern
        long daysPassed = currentDay - lastCrime;
        if (daysPassed > 0) {
            int current = getWantedLevel(playerUUID);
            int newLevel = Math.max(0, current - (int)daysPassed);

            if (newLevel <= 0) {
                clearWantedLevel(playerUUID);
            } else {
                wantedLevels.put(playerUUID, newLevel);
                lastCrimeDay.put(playerUUID, currentDay);
                save();
            }

            LOGGER.info("Player {} Wanted-Level Decay: {} -> {} ({} Tage vergangen)",
                playerUUID, current, newLevel, daysPassed);
        }
    }

    /**
     * Datenklasse für JSON-Serialisierung
     */
    private static class CrimeData {
        Map<String, Integer> wantedLevels = new HashMap<>();
        Map<String, Long> lastCrimeDay = new HashMap<>();
    }
}
