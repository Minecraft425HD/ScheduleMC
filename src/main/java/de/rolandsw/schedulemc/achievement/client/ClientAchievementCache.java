package de.rolandsw.schedulemc.achievement.client;

import de.rolandsw.schedulemc.achievement.AchievementCategory;
import de.rolandsw.schedulemc.achievement.network.AchievementData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Client-seitiger Cache für Achievement-Daten
 *
 * Dieser Cache speichert Achievement-Daten, die vom Server synchronisiert werden,
 * sodass die Achievement-App sie anzeigen kann ohne direkten Server-Zugriff.
 */
@OnlyIn(Dist.CLIENT)
public class ClientAchievementCache {
    private static final List<AchievementData> achievements = new ArrayList<>();
    private static final Map<String, AchievementData> achievementMap = new HashMap<>();
    private static final Map<AchievementCategory, List<AchievementData>> categoryMap = new HashMap<>();

    private static int totalAchievements = 0;
    private static int unlockedCount = 0;
    private static double totalEarned = 0.0;
    private static boolean initialized = false;

    /**
     * Aktualisiert den Cache mit neuen Daten vom Server
     */
    public static void updateCache(List<AchievementData> newAchievements, int total, int unlocked, double earned) {
        achievements.clear();
        achievementMap.clear();
        categoryMap.clear();

        achievements.addAll(newAchievements);
        totalAchievements = total;
        unlockedCount = unlocked;
        totalEarned = earned;

        // Build maps for quick access
        for (AchievementData data : newAchievements) {
            achievementMap.put(data.getId(), data);

            AchievementCategory category = data.getCategory();
            categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(data);
        }

        initialized = true;
    }

    /**
     * Löscht den Cache (z.B. beim Disconnect)
     */
    public static void clear() {
        achievements.clear();
        achievementMap.clear();
        categoryMap.clear();
        totalAchievements = 0;
        unlockedCount = 0;
        totalEarned = 0.0;
        initialized = false;
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
}
