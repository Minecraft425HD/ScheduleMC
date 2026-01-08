package de.rolandsw.schedulemc.achievement;

import net.minecraft.network.chat.Component;

/**
 * Kategorien für Achievements
 */
public enum AchievementCategory {
    ECONOMY("💰"),
    CRIME("🚔"),
    PRODUCTION("🌿"),
    SOCIAL("👥"),
    EXPLORATION("🗺️");

    private final String emoji;

    AchievementCategory(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.achievement_category." + this.name().toLowerCase());
    }

    public Component getDescription() {
        return Component.translatable("enum.achievement_category.desc." + this.name().toLowerCase());
    }

    public Component getFormattedName() {
        return Component.literal(emoji + " ").append(getDisplayName());
    }
}
