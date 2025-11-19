package de.rolandsw.schedulemc.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.data.DailyReward;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet tägliche Belohnungen für Spieler
 */
public class DailyRewardManager {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File file = new File("config/plotmod_daily.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, DailyReward> rewards = new HashMap<>();
    private static boolean needsSave = false;
    
    /**
     * Lädt alle Daily Rewards
     */
    public static void load() {
        if (!file.exists()) {
            LOGGER.info("Keine Daily Reward Datei gefunden");
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, DailyReward>>(){}.getType();
            Map<String, DailyReward> loaded = gson.fromJson(reader, type);
            
            if (loaded != null) {
                rewards.clear();
                rewards.putAll(loaded);
                LOGGER.info("Daily Rewards geladen: " + rewards.size() + " Spieler");
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Laden der Daily Rewards", e);
        }
    }
    
    /**
     * Speichert alle Daily Rewards
     */
    public static void save() {
        try {
            file.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(rewards, writer);
                needsSave = false;
                LOGGER.info("Daily Rewards gespeichert: " + rewards.size() + " Spieler");
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Daily Rewards", e);
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
        
        LOGGER.info("Daily Reward geclaimed: " + playerUUID + " - " + totalAmount + "€ (Streak: " + streak + ")");
        
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
        LOGGER.info("Streak resettet für: " + playerUUID);
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
}
