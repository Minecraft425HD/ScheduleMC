package de.rolandsw.schedulemc.achievement;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Speichert Achievement-Fortschritt eines Spielers
 */
public class PlayerAchievements {
    @SerializedName("playerUUID")
    private final UUID playerUUID;

    @SerializedName("unlockedAchievements")
    private final Set<String> unlockedAchievements;

    @SerializedName("progress")
    private final Map<String, Double> progress;

    @SerializedName("unlockTimestamps")
    private final Map<String, Long> unlockTimestamps;

    @SerializedName("totalPointsEarned")
    private double totalPointsEarned;

    public PlayerAchievements(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.unlockedAchievements = ConcurrentHashMap.newKeySet();
        this.progress = new ConcurrentHashMap<>();
        this.unlockTimestamps = new ConcurrentHashMap<>();
        this.totalPointsEarned = 0.0;
    }

    // Getters
    public UUID getPlayerUUID() { return playerUUID; }
    public Set<String> getUnlockedAchievements() { return new HashSet<>(unlockedAchievements); }
    public Map<String, Double> getProgress() { return new HashMap<>(progress); }
    public Map<String, Long> getUnlockTimestamps() { return new HashMap<>(unlockTimestamps); }
    public double getTotalPointsEarned() { return totalPointsEarned; }

    /**
     * Prüft ob Achievement freigeschaltet ist
     */
    public boolean isUnlocked(String achievementId) {
        return unlockedAchievements.contains(achievementId);
    }

    /**
     * Gibt Fortschritt für Achievement zurück
     */
    public double getProgress(String achievementId) {
        return progress.getOrDefault(achievementId, 0.0);
    }

    /**
     * Setzt Fortschritt für Achievement
     */
    public void setProgress(String achievementId, double value) {
        progress.put(achievementId, value);
    }

    /**
     * Addiert Fortschritt für Achievement
     */
    public void addProgress(String achievementId, double amount) {
        double current = getProgress(achievementId);
        setProgress(achievementId, current + amount);
    }

    /**
     * Schaltet Achievement frei
     */
    public boolean unlock(String achievementId, double rewardMoney) {
        if (unlockedAchievements.add(achievementId)) {
            unlockTimestamps.put(achievementId, System.currentTimeMillis());
            totalPointsEarned += rewardMoney;
            return true;
        }
        return false;
    }

    /**
     * Gibt Anzahl freigeschalteter Achievements zurück
     */
    public int getUnlockedCount() {
        return unlockedAchievements.size();
    }

    /**
     * Gibt Unlock-Timestamp zurück
     */
    public long getUnlockTimestamp(String achievementId) {
        return unlockTimestamps.getOrDefault(achievementId, 0L);
    }

    /**
     * Gibt Fortschritts-Prozentsatz zurück (0.0 - 1.0)
     */
    public double getProgressPercentage(String achievementId, double requirement) {
        if (isUnlocked(achievementId)) {
            return 1.0;
        }
        double current = getProgress(achievementId);
        return Math.min(1.0, current / requirement);
    }
}
