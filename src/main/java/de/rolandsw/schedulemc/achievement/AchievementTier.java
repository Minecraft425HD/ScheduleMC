package de.rolandsw.schedulemc.achievement;

/**
 * Schwierigkeits-Stufen für Achievements
 */
public enum AchievementTier {
    BRONZE("🥉", "Bronze", "§7", 100.0),
    SILVER("🥈", "Silber", "§f", 500.0),
    GOLD("🥇", "Gold", "§e", 2000.0),
    DIAMOND("💎", "Diamant", "§b", 10000.0),
    PLATINUM("⭐", "Platin", "§d", 50000.0);

    private final String emoji;
    private final String displayName;
    private final String colorCode;
    private final double rewardMoney;

    AchievementTier(String emoji, String displayName, String colorCode, double rewardMoney) {
        this.emoji = emoji;
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.rewardMoney = rewardMoney;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public String getFormattedName() {
        return colorCode + emoji + " " + displayName;
    }
}
