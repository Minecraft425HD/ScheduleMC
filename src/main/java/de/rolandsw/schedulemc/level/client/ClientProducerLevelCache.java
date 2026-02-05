package de.rolandsw.schedulemc.level.client;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.level.UnlockCategory;
import de.rolandsw.schedulemc.level.network.UnlockableData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Client-seitiger Cache fuer ProducerLevel-Daten.
 *
 * Speichert Level-Daten und Unlockables die vom Server synchronisiert werden,
 * sodass die ProducerLevel-App sie anzeigen kann ohne direkten Server-Zugriff.
 *
 * SICHERHEIT: Thread-safe Collections und volatile Felder fuer concurrent access
 */
@OnlyIn(Dist.CLIENT)
public class ClientProducerLevelCache {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int MAX_UNLOCKABLES_SIZE = 500;

    // Level-Daten
    private static volatile int currentLevel = 0;
    private static volatile int totalXP = 0;
    private static volatile int xpToNextLevel = 0;
    private static volatile double progress = 0.0;
    private static volatile int totalUnlocked = 0;
    private static volatile int totalUnlockables = 0;

    // Statistiken
    private static volatile int totalItemsSold = 0;
    private static volatile int totalIllegalSold = 0;
    private static volatile int totalLegalSold = 0;
    private static volatile double totalRevenue = 0.0;

    // Unlockable-Daten
    private static final List<UnlockableData> unlockables = new CopyOnWriteArrayList<>();
    private static final Map<UnlockCategory, List<UnlockableData>> categoryMap = new ConcurrentHashMap<>();

    private static volatile boolean initialized = false;
    private static volatile Runnable updateListener = null;

    /**
     * Aktualisiert den Cache mit neuen Daten vom Server.
     */
    public static void updateCache(int level, int xp, int xpNext, double prog,
                                   int unlocked, int totalUnlock,
                                   List<UnlockableData> newUnlockables,
                                   int itemsSold, int illegalSold,
                                   int legalSold, double revenue) {
        LOGGER.debug("ClientProducerLevelCache: Updating - Level {}, {}/{} unlocked",
                level, unlocked, totalUnlock);

        // Size check
        List<UnlockableData> safeList = newUnlockables;
        if (newUnlockables.size() > MAX_UNLOCKABLES_SIZE) {
            LOGGER.warn("Unlockable cache size ({}) exceeds limit ({}), truncating",
                    newUnlockables.size(), MAX_UNLOCKABLES_SIZE);
            safeList = newUnlockables.subList(0, MAX_UNLOCKABLES_SIZE);
        }

        currentLevel = level;
        totalXP = xp;
        xpToNextLevel = xpNext;
        progress = prog;
        totalUnlocked = unlocked;
        totalUnlockables = totalUnlock;
        totalItemsSold = itemsSold;
        totalIllegalSold = illegalSold;
        totalLegalSold = legalSold;
        totalRevenue = revenue;

        unlockables.clear();
        categoryMap.clear();
        unlockables.addAll(safeList);

        // Category-Map aufbauen
        for (UnlockableData data : safeList) {
            UnlockCategory category = data.getCategory();
            categoryMap.computeIfAbsent(category, k -> new CopyOnWriteArrayList<>()).add(data);
        }

        initialized = true;

        if (updateListener != null) {
            updateListener.run();
        }
    }

    /**
     * Loescht den Cache (z.B. beim Disconnect).
     */
    public static void clear() {
        unlockables.clear();
        categoryMap.clear();
        currentLevel = 0;
        totalXP = 0;
        xpToNextLevel = 0;
        progress = 0.0;
        totalUnlocked = 0;
        totalUnlockables = 0;
        totalItemsSold = 0;
        totalIllegalSold = 0;
        totalLegalSold = 0;
        totalRevenue = 0.0;
        initialized = false;
        updateListener = null;
    }

    // === GETTERS ===

    public static int getCurrentLevel() {
        return currentLevel;
    }

    public static int getTotalXP() {
        return totalXP;
    }

    public static int getXpToNextLevel() {
        return xpToNextLevel;
    }

    public static double getProgress() {
        return progress;
    }

    public static int getTotalUnlocked() {
        return totalUnlocked;
    }

    public static int getTotalUnlockables() {
        return totalUnlockables;
    }

    public static int getTotalItemsSold() {
        return totalItemsSold;
    }

    public static int getTotalIllegalSold() {
        return totalIllegalSold;
    }

    public static int getTotalLegalSold() {
        return totalLegalSold;
    }

    public static double getTotalRevenue() {
        return totalRevenue;
    }

    public static List<UnlockableData> getAllUnlockables() {
        return new ArrayList<>(unlockables);
    }

    /**
     * Gibt Unlockables einer Kategorie zurueck, sortiert nach Level-Anforderung.
     */
    public static List<UnlockableData> getUnlockablesByCategory(UnlockCategory category) {
        List<UnlockableData> list = categoryMap.getOrDefault(category, new ArrayList<>());
        return list.stream()
                .sorted(Comparator.comparingInt(UnlockableData::getRequiredLevel))
                .collect(Collectors.toList());
    }

    /**
     * Zaehlt freigeschaltete Unlockables in einer Kategorie.
     */
    public static int getUnlockedCountForCategory(UnlockCategory category) {
        List<UnlockableData> list = categoryMap.get(category);
        if (list == null) return 0;
        return (int) list.stream().filter(UnlockableData::isUnlocked).count();
    }

    /**
     * Zaehlt gesamte Unlockables in einer Kategorie.
     */
    public static int getTotalCountForCategory(UnlockCategory category) {
        List<UnlockableData> list = categoryMap.get(category);
        return list != null ? list.size() : 0;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void setUpdateListener(Runnable listener) {
        updateListener = listener;
    }

    public static void removeUpdateListener() {
        updateListener = null;
    }
}
