package de.rolandsw.schedulemc.achievement;

import net.minecraft.network.chat.Component;

import java.util.Locale;

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
        return Component.translatable("enum.achievement_category." + this.name().toLowerCase(Locale.ROOT));
    }

    public Component getDescription() {
        return Component.translatable("enum.achievement_category.desc." + this.name().toLowerCase(Locale.ROOT));
    }

    public Component getFormattedName() {
        return Component.literal(emoji + " ").append(getDisplayName());
    }
}
