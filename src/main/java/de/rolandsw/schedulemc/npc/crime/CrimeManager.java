package de.rolandsw.schedulemc.npc.crime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Wanted-Level (Fahndungsstufen) fuer Spieler
 * 0 Sterne = Sauber
 * 1-2 Sterne = Kleinkriminalitaet
 * 3-4 Sterne = Schwere Straftaten
 * 5 Sterne = Hoechste Fahndungsstufe
 *
 * FIX 2: BountyManager-Anbindung bei addWantedLevel
 * FIX 5: Escape-Timer werden jetzt persistiert
 * FIX 8: CrimeRecord-Historie wird gefuehrt
 */
public class CrimeManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = GsonHelper.get();
    private static final File CRIME_FILE = new File("config/plotmod_crimes.json");
    private static final int MAX_WANTED_LEVEL = 5;

    // Escape Timer: Skaliert nach Wanted-Level (nicht mehr pauschal 30 Sek)
    public static final long BASE_ESCAPE_DURATION = 20 * 20; // 20 Sekunden in Ticks (Level 1)
    public static final double ESCAPE_DISTANCE = 40.0; // Mindestabstand zur Polizei

    /**
     * Berechnet Fluchtdauer basierend auf Wanted-Level:
     * Level 1: 20 Sek, Level 2: 40 Sek, Level 3: 60 Sek,
     * Level 4: 90 Sek, Level 5: 120 Sek
     */
    public static long getEscapeDuration(int wantedLevel) {
        return switch (wantedLevel) {
            case 1 -> 20 * 20;   // 20 Sekunden
            case 2 -> 40 * 20;   // 40 Sekunden
            case 3 -> 60 * 20;   // 60 Sekunden
            case 4 -> 90 * 20;   // 90 Sekunden
            case 5 -> 120 * 20;  // 120 Sekunden
            default -> BASE_ESCAPE_DURATION;
        };
    }

    // SICHERHEIT: ConcurrentHashMap fuer Thread-Sicherheit
    private static final Map<UUID, Integer> wantedLevels = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastCrimeDay = new ConcurrentHashMap<>();
    // FIX 5: Escape-Timer werden jetzt persistiert (vorher NOT PERSISTED)
    private static final Map<UUID, Long> escapeTimers = new ConcurrentHashMap<>();

    // FIX 8: Crime-Historie fuer Beweisketten
    private static final Map<UUID, List<CrimeRecord>> crimeHistory = new ConcurrentHashMap<>();
    private static final int MAX_RECORDS_PER_PLAYER = 100;

    // Client-side data (nur fuer HUD Overlay)
    private static int clientWantedLevel = 0;
    private static long clientEscapeTime = 0;

    private static final CrimePersistenceManager persistence =
        new CrimePersistenceManager(CRIME_FILE, GSON);

    public static void load() {
        persistence.load();
    }

    public static void save() {
        persistence.save();
    }

    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    private static void markDirty() {
        persistence.markDirty();
    }

    public static int getWantedLevel(UUID playerUUID) {
        return wantedLevels.getOrDefault(playerUUID, 0);
    }

    /**
     * Gibt alle Spieler mit Wanted-Level > 0 zurueck
     */
    public static Map<UUID, Integer> getAllWantedPlayers() {
        return Collections.unmodifiableMap(new HashMap<>(wantedLevels));
    }

    /**
     * Fuegt Wanted-Level hinzu (max 5)
     * FIX 2: Ruft BountyManager.createAutoBounty() auf wenn Level >= 3
     */
    public static void addWantedLevel(UUID playerUUID, int amount, long currentDay) {
        wantedLevels.compute(playerUUID, (key, current) -> {
            int currentLevel = current != null ? current : 0;
            return Math.min(MAX_WANTED_LEVEL, currentLevel + amount);
        });
        lastCrimeDay.put(playerUUID, currentDay);
        markDirty();

        // FIX 2: BountyManager-Anbindung
        int newLevel = getWantedLevel(playerUUID);
        try {
            BountyManager bountyManager = BountyManager.getInstance();
            if (bountyManager != null && newLevel >= 3) {
                bountyManager.createAutoBounty(playerUUID, newLevel);
            }
        } catch (Exception e) {
            LOGGER.debug("BountyManager not available: {}", e.getMessage());
        }
    }

    /**
     * Fuegt Wanted-Level hinzu UND erstellt CrimeRecord.
     * Nutzt die wantedStars des CrimeType fuer severity-basierte Eskalation.
     * Der amount-Parameter wird ignoriert zugunsten von crimeType.getWantedStars().
     */
    public static void addWantedLevel(UUID playerUUID, int amount, long currentDay,
                                       CrimeType crimeType, @Nullable BlockPos location) {
        // Severity-basiert: nutze die Wanted-Stars des CrimeType statt dem rohen amount
        int severityBasedAmount = crimeType.getWantedStars();
        addWantedLevel(playerUUID, severityBasedAmount, currentDay);
        addCrimeRecord(playerUUID, crimeType, location);
    }

    /**
     * Erstellt einen CrimeRecord in der Historie
     */
    public static void addCrimeRecord(UUID playerUUID, CrimeType crimeType, @Nullable BlockPos location) {
        CrimeRecord record = new CrimeRecord(playerUUID, crimeType, location);
        List<CrimeRecord> records = crimeHistory.computeIfAbsent(playerUUID, k -> new ArrayList<>());

        // Limit einhalten
        while (records.size() >= MAX_RECORDS_PER_PLAYER) {
            records.remove(0);
        }
        records.add(record);
        markDirty();
        LOGGER.debug("CrimeRecord added for {}: {}", playerUUID, crimeType.name());
    }

    /**
     * Gibt die Crime-Historie fuer einen Spieler zurueck
     */
    public static List<CrimeRecord> getCrimeHistory(UUID playerUUID) {
        return crimeHistory.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Gibt die Anzahl ungesuehnter Verbrechen zurueck
     */
    public static int getUnservedCrimeCount(UUID playerUUID) {
        int count = 0;
        for (CrimeRecord record : getCrimeHistory(playerUUID)) {
            if (!record.isServed()) count++;
        }
        return count;
    }

    /**
     * Markiert alle CrimeRecords als abgesessen (nach Gefaengnis)
     */
    public static void markAllCrimesServed(UUID playerUUID) {
        List<CrimeRecord> records = crimeHistory.get(playerUUID);
        if (records != null) {
            for (CrimeRecord record : records) {
                if (!record.isServed()) {
                    record.markServed();
                }
            }
            markDirty();
        }
    }

    public static void clearWantedLevel(UUID playerUUID) {
        wantedLevels.remove(playerUUID);
        lastCrimeDay.remove(playerUUID);
        markAllCrimesServed(playerUUID);
        markDirty();
    }

    public static void setWantedLevel(UUID playerUUID, int level) {
        if (level <= 0) {
            clearWantedLevel(playerUUID);
        } else {
            wantedLevels.put(playerUUID, Math.min(MAX_WANTED_LEVEL, level));
            markDirty();
        }
    }

    /**
     * Beschleunigter Wanted-Decay: 1 Level pro halben Tag (12h Spielzeit).
     * Vorher: 1 Level pro Tag. Jetzt: daysPassed * 2 als Decay-Rate.
     */
    public static void decayWantedLevel(UUID playerUUID, long currentDay) {
        Long lastCrime = lastCrimeDay.get(playerUUID);
        if (lastCrime == null) return;

        long daysPassed = currentDay - lastCrime;
        if (daysPassed > 0) {
            // Beschleunigt: 2x Decay (effektiv 1 Level pro 12h statt 24h)
            final int decay = (int) Math.min(MAX_WANTED_LEVEL, daysPassed * 2);

            wantedLevels.compute(playerUUID, (key, current) -> {
                if (current == null) return null;
                int newLevel = Math.max(0, current - decay);
                return newLevel <= 0 ? null : newLevel;
            });

            if (!wantedLevels.containsKey(playerUUID)) {
                lastCrimeDay.remove(playerUUID);
            } else {
                lastCrimeDay.put(playerUUID, currentDay);
            }
            markDirty();
        }
    }

    // ========== ESCAPE SYSTEM (FIX 5: jetzt persistiert) ==========

    public static void startEscapeTimer(UUID playerUUID, long currentTick) {
        escapeTimers.put(playerUUID, currentTick);
        markDirty(); // FIX 5: Persistieren
        LOGGER.info("Player {} Escape-Timer gestartet (Tick {})", playerUUID, currentTick);
    }

    public static void stopEscapeTimer(UUID playerUUID) {
        if (escapeTimers.remove(playerUUID) != null) {
            markDirty(); // FIX 5: Persistieren
        }
    }

    public static boolean isHiding(UUID playerUUID) {
        return escapeTimers.containsKey(playerUUID);
    }

    public static long getEscapeTimeRemaining(UUID playerUUID, long currentTick) {
        Long startTick = escapeTimers.get(playerUUID);
        if (startTick == null) {
            return 0;
        }
        int level = getWantedLevel(playerUUID);
        long duration = getEscapeDuration(level);
        long elapsed = currentTick - startTick;
        long remaining = duration - elapsed;
        return Math.max(0, remaining);
    }

    public static boolean checkEscapeSuccess(UUID playerUUID, long currentTick) {
        if (!isHiding(playerUUID)) return false;

        long remaining = getEscapeTimeRemaining(playerUUID, currentTick);
        if (remaining <= 0) {
            wantedLevels.compute(playerUUID, (key, current) -> {
                if (current == null) return null;
                int newLevel = Math.max(0, current - 1);
                return newLevel <= 0 ? null : newLevel;
            });

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

    public static boolean isHealthy() {
        return persistence.isHealthy();
    }

    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    public static String getHealthInfo() {
        return persistence.getHealthInfo();
    }

    // ========== CLIENT-SIDE METHODS ==========

    public static void setClientWantedLevel(int level) {
        clientWantedLevel = level;
    }

    public static void setClientEscapeTime(long timeRemaining) {
        clientEscapeTime = timeRemaining;
    }

    public static int getClientWantedLevel() {
        return clientWantedLevel;
    }

    public static long getClientEscapeTime() {
        return clientEscapeTime;
    }

    // ========== PERSISTENCE ==========

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
            escapeTimers.clear();
            crimeHistory.clear();

            int invalidCount = 0;
            int correctedCount = 0;

            if (data == null) {
                LOGGER.warn("Null data loaded for crime manager");
                return;
            }

            // Load wantedLevels
            if (data.wantedLevels != null) {
                for (Map.Entry<String, Integer> entry : data.wantedLevels.entrySet()) {
                    try {
                        if (entry.getKey() == null || entry.getKey().isEmpty()) {
                            invalidCount++;
                            continue;
                        }
                        UUID playerUUID = UUID.fromString(entry.getKey());
                        Integer level = entry.getValue();
                        if (level == null || level < 0) {
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
                        if (entry.getKey() == null || entry.getKey().isEmpty()) {
                            invalidCount++;
                            continue;
                        }
                        UUID playerUUID = UUID.fromString(entry.getKey());
                        Long day = entry.getValue();
                        if (day == null || day < 0) {
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

            // FIX 5: Load escape timers
            if (data.escapeTimers != null) {
                for (Map.Entry<String, Long> entry : data.escapeTimers.entrySet()) {
                    try {
                        if (entry.getKey() == null || entry.getKey().isEmpty()) continue;
                        UUID playerUUID = UUID.fromString(entry.getKey());
                        Long startTick = entry.getValue();
                        if (startTick != null && startTick >= 0) {
                            escapeTimers.put(playerUUID, startTick);
                        }
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid UUID in escape timers: {}", entry.getKey(), e);
                    }
                }
                LOGGER.info("Loaded {} escape timers", escapeTimers.size());
            }

            // FIX 8: Load crime history
            if (data.crimeRecords != null) {
                for (Map.Entry<String, List<CrimeRecord>> entry : data.crimeRecords.entrySet()) {
                    try {
                        if (entry.getKey() == null || entry.getKey().isEmpty()) continue;
                        UUID playerUUID = UUID.fromString(entry.getKey());
                        List<CrimeRecord> records = entry.getValue();
                        if (records != null && !records.isEmpty()) {
                            crimeHistory.put(playerUUID, new ArrayList<>(records));
                        }
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid UUID in crime records: {}", entry.getKey(), e);
                    }
                }
                LOGGER.info("Loaded {} crime history entries", crimeHistory.size());
            }

            if (invalidCount > 0 || correctedCount > 0) {
                LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                    invalidCount, correctedCount);
                if (correctedCount > 0) {
                    markDirty();
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

            // FIX 5: Persist escape timers
            for (Map.Entry<UUID, Long> entry : escapeTimers.entrySet()) {
                data.escapeTimers.put(entry.getKey().toString(), entry.getValue());
            }

            // FIX 8: Persist crime records
            for (Map.Entry<UUID, List<CrimeRecord>> entry : crimeHistory.entrySet()) {
                data.crimeRecords.put(entry.getKey().toString(), entry.getValue());
            }

            return data;
        }

        @Override
        protected String getComponentName() {
            return "Crime System";
        }

        @Override
        protected String getHealthDetails() {
            return String.format("%d Wanted Players, %d Escape Timers, %d Crime Records",
                wantedLevels.size(), escapeTimers.size(), crimeHistory.size());
        }

        @Override
        protected void onCriticalLoadFailure() {
            wantedLevels.clear();
            lastCrimeDay.clear();
            escapeTimers.clear();
            crimeHistory.clear();
        }
    }

    private static class CrimeData {
        Map<String, Integer> wantedLevels = new HashMap<>();
        Map<String, Long> lastCrimeDay = new HashMap<>();
        Map<String, Long> escapeTimers = new HashMap<>(); // FIX 5
        Map<String, List<CrimeRecord>> crimeRecords = new HashMap<>(); // FIX 8
    }
}
