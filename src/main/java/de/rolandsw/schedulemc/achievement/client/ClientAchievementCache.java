package de.rolandsw.schedulemc.achievement.client;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.achievement.AchievementCategory;
import de.rolandsw.schedulemc.achievement.network.AchievementData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Client-seitiger Cache für Achievement-Daten
 *
 * Dieser Cache speichert Achievement-Daten, die vom Server synchronisiert werden,
 * sodass die Achievement-App sie anzeigen kann ohne direkten Server-Zugriff.
 * SICHERHEIT: Thread-safe Collections und volatile Felder für concurrent access
 */
@OnlyIn(Dist.CLIENT)
public class ClientAchievementCache {
    private static final Logger LOGGER = LogUtils.getLogger();

    // MEMORY LEAK PREVENTION: Size limits for all collections
    /**
     * Maximum achievements cache size (5000 entries)
     * Prevents unbounded growth - reasonable limit for achievement system
     */
    private static final int MAX_ACHIEVEMENTS_SIZE = 5000;

    /**
     * Maximum categories in category map (50 categories)
     * Prevents unbounded growth - reasonable limit for achievement categories
     */
    private static final int MAX_CATEGORY_MAP_SIZE = 50;

    // SICHERHEIT: Thread-safe Collections für concurrent access vom Network-Thread und Client-Thread
    private static final List<AchievementData> achievements = new CopyOnWriteArrayList<>();
    private static final Map<String, AchievementData> achievementMap = new ConcurrentHashMap<>();
    private static final Map<AchievementCategory, List<AchievementData>> categoryMap = new ConcurrentHashMap<>();

    // SICHERHEIT: volatile für Memory Visibility zwischen Threads
    private static volatile int totalAchievements = 0;
    private static volatile int unlockedCount = 0;
    private static volatile double totalEarned = 0.0;
    private static volatile boolean initialized = false;

    // Listener für Cache-Updates
    private static volatile Runnable updateListener = null;

    /**
     * Aktualisiert den Cache mit neuen Daten vom Server
     * MEMORY LEAK PREVENTION: Enforces size limits on all collections
     */
    public static void updateCache(List<AchievementData> newAchievements, int total, int unlocked, double earned) {
        LOGGER.info("ClientAchievementCache: Updating cache with {} achievements (unlocked: {}, earned: {}€)",
            total, unlocked, earned);

        // SIZE CHECK: Validate incoming data size
        if (newAchievements.size() > MAX_ACHIEVEMENTS_SIZE) {
            LOGGER.warn("Achievement cache size ({}) exceeds limit ({}), truncating to most recent entries",
                newAchievements.size(), MAX_ACHIEVEMENTS_SIZE);
            // Keep only the first MAX_ACHIEVEMENTS_SIZE entries
            newAchievements = newAchievements.subList(0, MAX_ACHIEVEMENTS_SIZE);
        }

        achievements.clear();
        achievementMap.clear();
        categoryMap.clear();

        achievements.addAll(newAchievements);
        totalAchievements = total;
        unlockedCount = unlocked;
        totalEarned = earned;

        // Build maps for quick access
        for (AchievementData data : newAchievements) {
            // SIZE CHECK: Limit achievementMap size
            if (achievementMap.size() >= MAX_ACHIEVEMENTS_SIZE) {
                LOGGER.warn("Achievement map size limit reached ({}), skipping additional entries",
                    MAX_ACHIEVEMENTS_SIZE);
                break;
            }
            achievementMap.put(data.getId(), data);

            AchievementCategory category = data.getCategory();

            // SIZE CHECK: Limit categoryMap size
            if (!categoryMap.containsKey(category) && categoryMap.size() >= MAX_CATEGORY_MAP_SIZE) {
                LOGGER.warn("Category map size limit reached ({}), skipping category: {}",
                    MAX_CATEGORY_MAP_SIZE, category);
                continue;
            }

            // SICHERHEIT: Thread-safe list creation für categoryMap
            categoryMap.computeIfAbsent(category, k -> new CopyOnWriteArrayList<>()).add(data);
        }

        initialized = true;

        LOGGER.info("ClientAchievementCache: Cache initialized. Categories: {}, Total achievements: {}",
            categoryMap.keySet(), achievementMap.size());

        // Notify listener (Achievement App) that data has been updated
        if (updateListener != null) {
            LOGGER.info("ClientAchievementCache: Notifying update listener");
            updateListener.run();
        }
    }

    /**
     * Löscht den Cache (z.B. beim Disconnect)
     * MEMORY LEAK PREVENTION: Ensures no data persists across sessions.
     * Clears all 3 static collections and resets all counters.
     */
    public static void clear() {
        int achievementCount = achievements.size();
        int mapCount = achievementMap.size();
        int categoryCount = categoryMap.size();

        achievements.clear();
        achievementMap.clear();
        categoryMap.clear();
        totalAchievements = 0;
        unlockedCount = 0;
        totalEarned = 0.0;
        initialized = false;
        updateListener = null;

        if (achievementCount > 0 || mapCount > 0 || categoryCount > 0) {
            LOGGER.info("ClientAchievementCache cleared: {} achievements, {} map entries, {} categories",
                achievementCount, mapCount, categoryCount);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt alle Achievements zurück
     */
    public static List<AchievementData> getAllAchievements() {
        return new ArrayList<>(achievements);
    }

    /**
     * Gibt Achievements einer Kategorie zurück (sortiert nach Tier)
     */
    public static List<AchievementData> getAchievementsByCategory(AchievementCategory category) {
        List<AchievementData> list = categoryMap.getOrDefault(category, new ArrayList<>());
        // Sort by tier (Bronze → Platinum)
        return list.stream()
            .sorted(Comparator.comparing(a -> a.getTier().ordinal()))
            .collect(Collectors.toList());
    }

    /**
     * Gibt ein Achievement nach ID zurück
     */
    public static AchievementData getAchievement(String id) {
        return achievementMap.get(id);
    }

    /**
     * Gibt Gesamtanzahl der Achievements zurück
     */
    public static int getTotalAchievements() {
        return totalAchievements;
    }

    /**
     * Gibt Anzahl freigeschalteter Achievements zurück
     */
    public static int getUnlockedCount() {
        return unlockedCount;
    }

    /**
     * Gibt insgesamt verdiente Belohnung zurück
     */
    public static double getTotalEarned() {
        return totalEarned;
    }

    /**
     * Prüft ob Cache initialisiert ist
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Gibt Fortschritt für ein Achievement zurück
     */
    public static double getProgress(String achievementId) {
        AchievementData data = achievementMap.get(achievementId);
        return data != null ? data.getProgress() : 0.0;
    }

    /**
     * Prüft ob Achievement freigeschaltet ist
     */
    public static boolean isUnlocked(String achievementId) {
        AchievementData data = achievementMap.get(achievementId);
        return data != null && data.isUnlocked();
    }

    /**
     * Registriert einen Listener, der benachrichtigt wird, wenn der Cache aktualisiert wird
     */
    public static void setUpdateListener(Runnable listener) {
        updateListener = listener;
    }

    /**
     * Entfernt den Update-Listener
     */
    public static void removeUpdateListener() {
        updateListener = null;
    }
}
