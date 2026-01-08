package de.rolandsw.schedulemc.managers;
nimport de.rolandsw.schedulemc.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet tägliche Belohnungen für Spieler
 *
 * Nutzt AbstractPersistenceManager für robuste Datenpersistenz
 */
public class DailyRewardManager {

    private static final File file = new File("config/plotmod_daily.json");
    private static final Gson gson = GsonHelper.get();
    private static final Map<String, DailyReward> rewards = new ConcurrentHashMap<>();

    // Persistence-Manager (eliminiert ~150 Zeilen Duplikation)
    private static final DailyRewardPersistenceManager persistence =
        new DailyRewardPersistenceManager(file, gson);

    /**
     * Lädt alle Daily Rewards
     */
    public static void load() {
        persistence.load();
    }

    /**
     * Speichert alle Daily Rewards
     */
    public static void save() {
        persistence.save();
    }

    /**
     * Speichert nur wenn nötig
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

        return totalAmount;
    }

    /**
     * Automatische Belohnung beim Login
     * Prüft ob Spieler heute bereits geclaimed hat und zahlt ggf. automatisch aus
     *
     * @param player Der eingeloggte Spieler
     * @return true wenn Belohnung ausgezahlt wurde
     */
    public static boolean claimOnLogin(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        // Prüfe ob heute bereits geclaimed
        if (!canClaim(playerUUID)) {
            return false;
        }

        // Claim durchführen
        double amount = claimDaily(playerUUID);

        // Auf Konto einzahlen
        EconomyManager.deposit(playerUUID, amount, TransactionType.DAILY_REWARD,
            "Tägliche Login-Belohnung");

        int streak = getStreak(playerUUID);

        // Nachricht an Spieler senden
        player.sendSystemMessage(Component.literal(
            "§a§l═══════════════════════════════\n" +
            "§a§l✓ TÄGLICHE LOGIN-BELOHNUNG!\n" +
            "§a§l═══════════════════════════════\n" +
            "§a+§e" + StringUtils.formatMoney(amount) + " §7aufs Konto überwiesen!\n" +
            "§7Streak: §e" + streak + " Tag" + (streak == 1 ? "" : "e") + " §6🔥\n" +
            (streak >= 7 ? "§d★ Bonus-Woche! ★\n" : "") +
            "§7Komm morgen wieder für mehr!\n" +
            "§a═══════════════════════════════"
        ));

        save();
        return true;
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

    /**
     * Speichert tägliche Belohnungs-Daten eines Spielers
     */
    public static class DailyReward {

        private final String playerUUID;
        private long lastClaimTime;
        private int currentStreak;
        private int longestStreak;
        private int totalClaims;

        public DailyReward(String playerUUID) {
            this.playerUUID = playerUUID;
            this.lastClaimTime = 0;
            this.currentStreak = 0;
            this.longestStreak = 0;
            this.totalClaims = 0;
        }

        // Getter
        public String getPlayerUUID() { return playerUUID; }
        public long getLastClaimTime() { return lastClaimTime; }
        public int getCurrentStreak() { return currentStreak; }
        public int getLongestStreak() { return longestStreak; }
        public int getTotalClaims() { return totalClaims; }

        // Setter
        public void setLastClaimTime(long time) { this.lastClaimTime = time; }
        public void setCurrentStreak(int streak) { this.currentStreak = streak; }
        public void setLongestStreak(int streak) { this.longestStreak = streak; }
        public void setTotalClaims(int claims) { this.totalClaims = claims; }

        /**
         * Prüft ob heute bereits geclaimt wurde
         */
        public boolean hasClaimedToday() {
            long now = System.currentTimeMillis();
            long dayInMillis = 24 * 60 * 60 * 1000;
            return (now - lastClaimTime) < dayInMillis;
        }

        /**
         * Prüft ob Streak noch gültig ist (innerhalb 48h)
         */
        public boolean isStreakValid() {
            if (lastClaimTime == 0) return false;
            long now = System.currentTimeMillis();
            long maxGap = 48 * 60 * 60 * 1000; // 48 Stunden
            return (now - lastClaimTime) < maxGap;
        }

        /**
         * Claimen der täglichen Belohnung
         */
        public void claim() {
            long now = System.currentTimeMillis();

            // Streak aktualisieren
            if (isStreakValid()) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }

            // Längsten Streak aktualisieren
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }

            lastClaimTime = now;
            totalClaims++;
        }

        /**
         * Gibt verbleibende Zeit bis zum nächsten Claim zurück (in Sekunden)
         */
        public long getTimeUntilNextClaim() {
            if (!hasClaimedToday()) return 0;

            long dayInMillis = 24 * 60 * 60 * 1000;
            long timeSinceClaim = System.currentTimeMillis() - lastClaimTime;
            long remaining = dayInMillis - timeSinceClaim;

            return Math.max(0, remaining / 1000);
        }

        /**
         * Formatiert Zeit bis zum nächsten Claim (HH:MM:SS)
         */
        public String getFormattedTimeUntilNext() {
            long seconds = getTimeUntilNextClaim();

            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;

            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
    }

    /**
     * Innere Persistence-Manager-Klasse
     */
    private static class DailyRewardPersistenceManager extends AbstractPersistenceManager<Map<String, DailyReward>> {

        public DailyRewardPersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return new TypeToken<Map<String, DailyReward>>(){}.getType();
        }

        @Override
        protected void onDataLoaded(Map<String, DailyReward> data) {
            rewards.clear();
            rewards.putAll(data);
        }

        @Override
        protected Map<String, DailyReward> getCurrentData() {
            return new HashMap<>(rewards);
        }

        @Override
        protected String getComponentName() {
            return "Daily Reward System";
        }

        @Override
        protected String getHealthDetails() {
            return String.format("%d Daily Rewards", rewards.size());
        }

        @Override
        protected void onCriticalLoadFailure() {
            rewards.clear();
        }
    }
}
