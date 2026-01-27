package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.achievement.IAchievementAPI;
import de.rolandsw.schedulemc.achievement.Achievement;
import de.rolandsw.schedulemc.achievement.AchievementCategory;
import de.rolandsw.schedulemc.achievement.AchievementManager;
import de.rolandsw.schedulemc.achievement.PlayerAchievements;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of IAchievementAPI
 *
 * Wrapper für AchievementManager mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class AchievementAPIImpl implements IAchievementAPI {

    private final AchievementManager achievementManager;

    public AchievementAPIImpl() {
        // Get singleton instance via MinecraftServer
        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("Server not started yet");
        }
        this.achievementManager = AchievementManager.getInstance(server);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerAchievements getPlayerAchievements(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return achievementManager.getPlayerAchievements(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProgress(UUID playerUUID, String achievementId, double amount) {
        if (playerUUID == null || achievementId == null) {
            throw new IllegalArgumentException("playerUUID and achievementId cannot be null");
        }
        achievementManager.addProgress(playerUUID, achievementId, amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(UUID playerUUID, String achievementId, double value) {
        if (playerUUID == null || achievementId == null) {
            throw new IllegalArgumentException("playerUUID and achievementId cannot be null");
        }
        achievementManager.setProgress(playerUUID, achievementId, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unlockAchievement(UUID playerUUID, String achievementId) {
        if (playerUUID == null || achievementId == null) {
            throw new IllegalArgumentException("playerUUID and achievementId cannot be null");
        }
        return achievementManager.unlockAchievement(playerUUID, achievementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Achievement getAchievement(String achievementId) {
        if (achievementId == null) {
            throw new IllegalArgumentException("achievementId cannot be null");
        }
        return achievementManager.getAchievement(achievementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Achievement> getAllAchievements() {
        return achievementManager.getAllAchievements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
        return getAllAchievements().stream()
            .filter(achievement -> achievement.getCategory() == category)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatistics(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        PlayerAchievements playerAch = getPlayerAchievements(playerUUID);
        int unlocked = playerAch.getUnlockedCount();
        int total = getTotalAchievementCount();
        double percentage = total > 0 ? (unlocked * 100.0 / total) : 0.0;
        double totalRewards = playerAch.getTotalRewards();

        return String.format("Achievements: %d/%d (%.1f%%) - €%.2f verdient",
            unlocked, total, percentage, totalRewards);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalAchievementCount() {
        return getAllAchievements().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnlockedCount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return getPlayerAchievements(playerUUID).getUnlockedCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getProgress(UUID playerUUID, String achievementId) {
        if (playerUUID == null || achievementId == null) {
            throw new IllegalArgumentException("playerUUID and achievementId cannot be null");
        }
        return getPlayerAchievements(playerUUID).getProgress(achievementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnlocked(UUID playerUUID, String achievementId) {
        if (playerUUID == null || achievementId == null) {
            throw new IllegalArgumentException("playerUUID and achievementId cannot be null");
        }
        return getPlayerAchievements(playerUUID).isUnlocked(achievementId);
    }
}
