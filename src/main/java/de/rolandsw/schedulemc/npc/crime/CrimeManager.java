package de.rolandsw.schedulemc.npc.crime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Wanted-Level (Fahndungsstufen) für Spieler
 * 0 Sterne = Sauber
 * 1-2 Sterne = Kleinkriminalität
 * 3-4 Sterne = Schwere Straftaten
 * 5 Sterne = Höchste Fahndungsstufe
 *
 * SICHERHEIT: Verwendet ConcurrentHashMap für Thread-Sicherheit bei parallelen Zugriffen
 * Nutzt AbstractPersistenceManager für robuste Datenpersistenz
 */
public class CrimeManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = GsonHelper.get();
    private static final File CRIME_FILE = new File("config/plotmod_crimes.json");
    private static final int MAX_WANTED_LEVEL = 5;

    // Escape Timer: 30 Sekunden verstecken = Polizei gibt auf
    public static final long ESCAPE_DURATION = 30 * 20; // 30 Sekunden in Ticks
    public static final double ESCAPE_DISTANCE = 40.0; // Mindestabstand zur Polizei

    // SICHERHEIT: ConcurrentHashMap für Thread-Sicherheit
    // UUID -> Wanted Level
    private static final Map<UUID, Integer> wantedLevels = new ConcurrentHashMap<>();
    // UUID -> Last Crime Day (für automatischen Abbau)
    private static final Map<UUID, Long> lastCrimeDay = new ConcurrentHashMap<>();
    // UUID -> Escape Timer Start (in Ticks) - NOT PERSISTED
    private static final Map<UUID, Long> escapeTimers = new ConcurrentHashMap<>();

    // Client-side data (nur für HUD Overlay)
    private static int clientWantedLevel = 0;
    private static long clientEscapeTime = 0;

    // Persistence-Manager (eliminiert ~100 Zeilen Duplikation)
    private static final CrimePersistenceManager persistence =
        new CrimePersistenceManager(CRIME_FILE, GSON);

    /**
     * Lädt Crime-Daten vom Disk
     */
    public static void load() {
        persistence.load();
    }

    /**
     * Speichert Crime-Daten auf Disk
     */
    public static void save() {
        persistence.save();
    }

    /**
     * Speichert nur wenn Änderungen vorhanden
     */
    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    /**
     * Markiert als geändert
     */
    private static void markDirty() {
        persistence.markDirty();
    }

    /**
     * Gibt aktuelles Wanted-Level zurück
     */
    public static int getWantedLevel(UUID playerUUID) {
        return wantedLevels.getOrDefault(playerUUID, 0);
    }

    /**
     * Fügt Wanted-Level hinzu (max 5)
     * SICHERHEIT: Atomare Operation für Thread-Sicherheit
     */
    public static void addWantedLevel(UUID playerUUID, int amount, long currentDay) {
        wantedLevels.compute(playerUUID, (key, current) -> {
            int currentLevel = current != null ? current : 0;
            return Math.min(MAX_WANTED_LEVEL, currentLevel + amount);
        });
        lastCrimeDay.put(playerUUID, currentDay);

        markDirty();
    }

    /**
     * Setzt Wanted-Level auf 0 (z.B. nach Festnahme)
     */
    public static void clearWantedLevel(UUID playerUUID) {
        wantedLevels.remove(playerUUID);
        lastCrimeDay.remove(playerUUID);
        markDirty();
    }

    /**
     * Setzt Wanted-Level auf einen bestimmten Wert
     */
    public static void setWantedLevel(UUID playerUUID, int level) {
        if (level <= 0) {
            clearWantedLevel(playerUUID);
        } else {
            wantedLevels.put(playerUUID, Math.min(MAX_WANTED_LEVEL, level));
            markDirty();
        }
    }

    /**
     * Reduziert Wanted-Level über Zeit
     * Sollte täglich aufgerufen werden (pro Minecraft-Tag)
     * SICHERHEIT: Atomare Operation für Thread-Sicherheit
     */
    public static void decayWantedLevel(UUID playerUUID, long currentDay) {
        Long lastCrime = lastCrimeDay.get(playerUUID);
        if (lastCrime == null) return;

        // Pro Tag ohne Verbrechen: -1 Stern
        long daysPassed = currentDay - lastCrime;
        if (daysPassed > 0) {
            final int decay = (int) daysPassed;

            wantedLevels.compute(playerUUID, (key, current) -> {
                if (current == null) return null;
                int newLevel = Math.max(0, current - decay);
                return newLevel <= 0 ? null : newLevel;
            });

            // Wenn Level auf 0 gefallen, entferne auch lastCrimeDay
            if (!wantedLevels.containsKey(playerUUID)) {
                lastCrimeDay.remove(playerUUID);
            } else {
                lastCrimeDay.put(playerUUID, currentDay);
            }
            markDirty();
        }
    }

    /**
     * Startet Escape-Timer (Spieler versteckt sich vor Polizei)
     */
    public static void startEscapeTimer(UUID playerUUID, long currentTick) {
        escapeTimers.put(playerUUID, currentTick);
        LOGGER.info("Player {} Escape-Timer gestartet (Tick {})", playerUUID, currentTick);
    }

    /**
     * Stoppt Escape-Timer (Polizei hat Spieler wieder entdeckt)
     */
    public static void stopEscapeTimer(UUID playerUUID) {
        escapeTimers.remove(playerUUID);
    }

    /**
     * Prüft, ob Spieler sich versteckt (aktiver Escape-Timer)
     */
    public static boolean isHiding(UUID playerUUID) {
        return escapeTimers.containsKey(playerUUID);
    }

    /**
     * Gibt verbleibende Escape-Zeit in Ticks zurück (0 = kein Timer aktiv)
     */
    public static long getEscapeTimeRemaining(UUID playerUUID, long currentTick) {
        if (!escapeTimers.containsKey(playerUUID)) {
            return 0;
        }

        long startTick = escapeTimers.get(playerUUID);
        long elapsed = currentTick - startTick;
        long remaining = ESCAPE_DURATION - elapsed;

        return Math.max(0, remaining);
    }

    /**
     * Prüft, ob Escape erfolgreich war (Timer abgelaufen)
     * Reduziert Wanted-Level um 1 Stern
     * SICHERHEIT: Atomare Operation für Thread-Sicherheit
     */
    public static boolean checkEscapeSuccess(UUID playerUUID, long currentTick) {
        if (!isHiding(playerUUID)) return false;

        long remaining = getEscapeTimeRemaining(playerUUID, currentTick);
        if (remaining <= 0) {
            // Escape erfolgreich! -1 Stern (atomar)
            wantedLevels.compute(playerUUID, (key, current) -> {
                if (current == null) return null;
                int newLevel = Math.max(0, current - 1);
                return newLevel <= 0 ? null : newLevel;
            });

            // Wenn Level auf 0, entferne auch lastCrimeDay
            if (!wantedLevels.containsKey(playerUUID)) {
                lastCrimeDay.remove(playerUUID);
            }
            markDirty();

            stopEscapeTimer(playerUUID);
            return true;
        }

        return false;
    }

    // ========== HEALTH MONITORING ==========

    /**
     * Gibt Health-Status zurück
     */
    public static boolean isHealthy() {
        return persistence.isHealthy();
    }

    /**
     * Gibt letzte Fehlermeldung zurück
     */
    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    /**
     * Gibt Health-Info zurück
     */
    public static String getHealthInfo() {
        return persistence.getHealthInfo();
    }

    // ========== CLIENT-SIDE METHODS (nur für HUD) ==========

    /**
     * Setzt Client-seitigen Wanted-Level (wird vom Server gesynct)
     */
    public static void setClientWantedLevel(int level) {
        clientWantedLevel = level;
    }

    /**
     * Setzt Client-seitige Escape-Zeit (wird vom Server gesynct)
     */
    public static void setClientEscapeTime(long timeRemaining) {
        clientEscapeTime = timeRemaining;
    }

    /**
     * Gibt Client-seitigen Wanted-Level zurück (für HUD Overlay)
     */
    public static int getClientWantedLevel() {
        return clientWantedLevel;
    }

    /**
     * Gibt Client-seitige Escape-Zeit zurück (für HUD Overlay)
     */
    public static long getClientEscapeTime() {
        return clientEscapeTime;
    }

    /**
     * Innere Persistence-Manager-Klasse
     */
    private static class CrimePersistenceManager extends AbstractPersistenceManager<CrimeData> {

        public CrimePersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return CrimeData.class;
        }

        @Override
        protected void onDataLoaded(CrimeData data) {
            wantedLevels.clear();
            lastCrimeDay.clear();

            int invalidCount = 0;
            int correctedCount = 0;

            // NULL CHECK
            if (data == null) {
                LOGGER.warn("Null data loaded for crime manager");
                invalidCount++;
                return;
            }

            // Load wantedLevels
            if (data.wantedLevels != null) {
                for (Map.Entry<String, Integer> entry : data.wantedLevels.entrySet()) {
                    try {
                        // VALIDATE UUID STRING
                        if (entry.getKey() == null || entry.getKey().isEmpty()) {
                            LOGGER.warn("Null/empty UUID string in wanted levels, skipping");
                            invalidCount++;
                            continue;
                        }

                        UUID playerUUID = UUID.fromString(entry.getKey());
                        Integer level = entry.getValue();

                        // NULL CHECK
                        if (level == null) {
                            LOGGER.warn("Null wanted level for player {}, setting to 0", playerUUID);
                            level = 0;
                            correctedCount++;
                        }

                        // VALIDATE WANTED LEVEL (>= 0)
                        if (level < 0) {
                            LOGGER.warn("Player {} has negative wanted level {}, resetting to 0",
                                playerUUID, level);
                            level = 0;
                            correctedCount++;
                        }

                        wantedLevels.put(playerUUID, level);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid UUID in wanted levels: {}", entry.getKey(), e);
                        invalidCount++;
                    }
                }
            }

            // Load lastCrimeDay
            if (data.lastCrimeDay != null) {
                for (Map.Entry<String, Long> entry : data.lastCrimeDay.entrySet()) {
                    try {
                        // VALIDATE UUID STRING
                        if (entry.getKey() == null || entry.getKey().isEmpty()) {
                            LOGGER.warn("Null/empty UUID string in last crime day, skipping");
                            invalidCount++;
                            continue;
                        }

                        UUID playerUUID = UUID.fromString(entry.getKey());
                        Long day = entry.getValue();

                        // NULL CHECK
                        if (day == null) {
                            LOGGER.warn("Null last crime day for player {}, setting to 0", playerUUID);
                            day = 0L;
                            correctedCount++;
                        }

                        // VALIDATE DAY (>= 0)
                        if (day < 0) {
                            LOGGER.warn("Player {} has negative last crime day {}, resetting to 0",
                                playerUUID, day);
                            day = 0L;
                            correctedCount++;
                        }

                        lastCrimeDay.put(playerUUID, day);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid UUID in last crime day: {}", entry.getKey(), e);
                        invalidCount++;
                    }
                }
            }

            // SUMMARY
            if (invalidCount > 0 || correctedCount > 0) {
                LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                    invalidCount, correctedCount);
                if (correctedCount > 0) {
                    markDirty(); // Re-save corrected data
                }
            }
        }

        @Override
        protected CrimeData getCurrentData() {
            CrimeData data = new CrimeData();

            for (Map.Entry<UUID, Integer> entry : wantedLevels.entrySet()) {
                data.wantedLevels.put(entry.getKey().toString(), entry.getValue());
            }

            for (Map.Entry<UUID, Long> entry : lastCrimeDay.entrySet()) {
                data.lastCrimeDay.put(entry.getKey().toString(), entry.getValue());
            }

            return data;
        }

        @Override
        protected String getComponentName() {
            return "Crime System";
        }

        @Override
        protected String getHealthDetails() {
            return String.format("%d Wanted Players", wantedLevels.size());
        }

        @Override
        protected void onCriticalLoadFailure() {
            wantedLevels.clear();
            lastCrimeDay.clear();
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
