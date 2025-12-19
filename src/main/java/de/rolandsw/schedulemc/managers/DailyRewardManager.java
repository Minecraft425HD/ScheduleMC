package de.rolandsw.schedulemc.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.data.DailyReward;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet tägliche Belohnungen für Spieler
 *
 * Features:
 * - Thread-safe durch ConcurrentHashMap
 * - Automatische Backup-Rotation
 * - Atomic file writes
 * - Backup-Wiederherstellung bei Korruption
 * - Health-Status-Tracking
 */
public class DailyRewardManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File file = new File("config/plotmod_daily.json");
    private static final Gson gson = GsonHelper.get();
    private static final Map<String, DailyReward> rewards = new ConcurrentHashMap<>();
    private static boolean needsSave = false;
    private static boolean isHealthy = true;
    private static String lastError = null;

    /**
     * Lädt alle Daily Rewards mit Backup-Wiederherstellung
     */
    public static void load() {
        if (!file.exists()) {
            LOGGER.info("Keine Daily Reward Datei gefunden");
            isHealthy = true;
            return;
        }

        try {
            loadFromFile(file);
            isHealthy = true;
            lastError = null;
            LOGGER.info("Daily Rewards geladen: {} Spieler", rewards.size());
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Daily Rewards", e);
            lastError = "Failed to load: " + e.getMessage();

            // Backup-Wiederherstellung
            if (BackupManager.restoreFromBackup(file)) {
                LOGGER.warn("Daily Rewards Datei korrupt, versuche Backup wiederherzustellen...");
                try {
                    loadFromFile(file);
                    LOGGER.info("Daily Rewards erfolgreich von Backup wiederhergestellt: {} Spieler", rewards.size());
                    isHealthy = true;
                    lastError = "Recovered from backup";
                } catch (Exception backupError) {
                    LOGGER.error("KRITISCH: Backup-Wiederherstellung fehlgeschlagen!", backupError);
                    handleCriticalLoadFailure();
                }
            } else {
                LOGGER.error("KRITISCH: Kein Backup verfügbar!");
                handleCriticalLoadFailure();
            }
        }
    }

    private static void loadFromFile(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, DailyReward>>(){}.getType();
            Map<String, DailyReward> loaded = gson.fromJson(reader, type);

            if (loaded == null) {
                throw new IOException("Geladene Daily Reward Daten sind null");
            }

            rewards.clear();
            rewards.putAll(loaded);
        }
    }

    private static void handleCriticalLoadFailure() {
        LOGGER.error("KRITISCH: Daily Reward System konnte nicht geladen werden!");
        LOGGER.error("Starte mit leerem Daily Reward System als Fallback");
        rewards.clear();
        isHealthy = false;
        lastError = "Critical load failure - running with empty data";

        // Preserve corrupt file for forensics
        if (file.exists()) {
            File corruptBackup = new File(file.getParent(),
                file.getName() + ".CORRUPT_" + System.currentTimeMillis());
            try {
                java.nio.file.Files.copy(file.toPath(), corruptBackup.toPath());
                LOGGER.info("Korrupte Datei gesichert nach: {}", corruptBackup.getName());
            } catch (IOException e) {
                LOGGER.error("Konnte korrupte Datei nicht sichern", e);
            }
        }
    }

    /**
     * Speichert alle Daily Rewards mit Backup und atomic writes
     */
    public static void save() {
        try {
            file.getParentFile().mkdirs();

            // Create backup before overwriting
            if (file.exists() && file.length() > 0) {
                BackupManager.createBackup(file);
            }

            // Temporary file for atomic writing
            File tempFile = new File(file.getParent(), file.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(rewards, writer);
                writer.flush();
            }

            // Atomic replace
            java.nio.file.Files.move(tempFile.toPath(), file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);

            needsSave = false;
            isHealthy = true;
            lastError = null;
            LOGGER.info("Daily Rewards gespeichert: {} Spieler", rewards.size());

        } catch (Exception e) {
            LOGGER.error("KRITISCH: Fehler beim Speichern der Daily Rewards!", e);
            isHealthy = false;
            lastError = "Save failed: " + e.getMessage();
            needsSave = true; // Keep dirty flag for retry
        }
    }

    /**
     * Speichert nur wenn nötig
     */
    public static void saveIfNeeded() {
        if (needsSave) {
            save();
        }
    }

    /**
     * Markiert als geändert
     */
    private static void markDirty() {
        needsSave = true;
    }

    /**
     * Gibt DailyReward eines Spielers zurück (erstellt bei Bedarf)
     */
    public static DailyReward getReward(UUID playerUUID) {
        String uuid = playerUUID.toString();
        return rewards.computeIfAbsent(uuid, k -> new DailyReward(uuid));
    }

    /**
     * Prüft ob Spieler heute claimen kann
     */
    public static boolean canClaim(UUID playerUUID) {
        DailyReward reward = getReward(playerUUID);
        return !reward.hasClaimedToday();
    }

    /**
     * Claimed die tägliche Belohnung
     * @return Betrag der ausgezahlt wurde
     */
    public static double claimDaily(UUID playerUUID) {
        DailyReward reward = getReward(playerUUID);

        if (reward.hasClaimedToday()) {
            return 0;
        }

        // Basis-Belohnung
        double amount = ModConfigHandler.COMMON.DAILY_REWARD.get();

        // Streak-Bonus
        int streak = reward.getCurrentStreak() + 1;
        double streakBonus = ModConfigHandler.COMMON.DAILY_REWARD_STREAK_BONUS.get() * (streak - 1);

        // Maximaler Streak-Bonus
        int maxStreak = ModConfigHandler.COMMON.MAX_STREAK_DAYS.get();
        if (streak > maxStreak) {
            streakBonus = ModConfigHandler.COMMON.DAILY_REWARD_STREAK_BONUS.get() * (maxStreak - 1);
        }

        double totalAmount = amount + streakBonus;

        // Claim durchführen
        reward.claim();
        markDirty();

        LOGGER.info("Daily Reward geclaimed: {} - {}€ (Streak: {})", playerUUID, totalAmount, streak);

        return totalAmount;
    }

    /**
     * Gibt Streak eines Spielers zurück
     */
    public static int getStreak(UUID playerUUID) {
        return getReward(playerUUID).getCurrentStreak();
    }

    /**
     * Gibt längsten Streak zurück
     */
    public static int getLongestStreak(UUID playerUUID) {
        return getReward(playerUUID).getLongestStreak();
    }

    /**
     * Gibt Zeit bis zum nächsten Claim zurück
     */
    public static long getTimeUntilNextClaim(UUID playerUUID) {
        return getReward(playerUUID).getTimeUntilNextClaim();
    }

    /**
     * Gibt formatierte Zeit bis zum nächsten Claim zurück
     */
    public static String getFormattedTimeUntilNext(UUID playerUUID) {
        return getReward(playerUUID).getFormattedTimeUntilNext();
    }

    /**
     * Resettet Streak eines Spielers (Admin)
     */
    public static void resetStreak(UUID playerUUID) {
        DailyReward reward = getReward(playerUUID);
        reward.setCurrentStreak(0);
        markDirty();
        LOGGER.info("Streak resettet für: {}", playerUUID);
    }

    /**
     * Gibt Statistiken zurück
     */
    public static Map<String, Object> getStats(UUID playerUUID) {
        DailyReward reward = getReward(playerUUID);
        Map<String, Object> stats = new HashMap<>();

        stats.put("currentStreak", reward.getCurrentStreak());
        stats.put("longestStreak", reward.getLongestStreak());
        stats.put("totalClaims", reward.getTotalClaims());
        stats.put("canClaim", !reward.hasClaimedToday());
        stats.put("timeUntilNext", reward.getFormattedTimeUntilNext());

        return stats;
    }

    /**
     * Gibt Health-Status zurück
     */
    public static boolean isHealthy() {
        return isHealthy;
    }

    /**
     * Gibt letzte Fehlermeldung zurück
     */
    @Nullable
    public static String getLastError() {
        return lastError;
    }

    /**
     * Gibt Health-Info zurück
     */
    public static String getHealthInfo() {
        if (isHealthy) {
            return String.format("§aGESUND§r - %d Daily Rewards, %d Backups verfügbar",
                rewards.size(), BackupManager.getBackupCount(file));
        } else {
            return String.format("§cUNGESUND§r - Letzter Fehler: %s, %d Daily Rewards geladen",
                lastError != null ? lastError : "Unknown", rewards.size());
        }
    }
}
