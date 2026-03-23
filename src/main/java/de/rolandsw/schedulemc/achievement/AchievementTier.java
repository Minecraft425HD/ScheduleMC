package de.rolandsw.schedulemc.achievement;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Schwierigkeits-Stufen für Achievements
 */
public enum AchievementTier {
    BRONZE("🥉", "§7", 100.0),
    SILVER("🥈", "§f", 500.0),
    GOLD("🥇", "§e", 2000.0),
    DIAMOND("💎", "§b", 10000.0),
    PLATINUM("⭐", "§d", 50000.0);

    private final String emoji;
    private final String colorCode;
    private final double rewardMoney;

    AchievementTier(String emoji, String colorCode, double rewardMoney) {
        this.emoji = emoji;
        this.colorCode = colorCode;
        this.rewardMoney = rewardMoney;
    }

    public String getEmoji() {
        return emoji;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.achievement_tier." + this.name().toLowerCase(Locale.ROOT));
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public Component getFormattedName() {
        return Component.literal(colorCode + emoji + " ").append(getDisplayName());
    }
}
