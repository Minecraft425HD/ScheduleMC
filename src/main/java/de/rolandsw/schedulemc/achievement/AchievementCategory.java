package de.rolandsw.schedulemc.achievement;

/**
 * Kategorien für Achievements
 */
public enum AchievementCategory {
    ECONOMY("💰", "Wirtschaft", "Geld verdienen und verwalten"),
    CRIME("🚔", "Verbrechen", "Kriminelle Aktivitäten"),
    PRODUCTION("🌿", "Produktion", "Drogen und Waren produzieren"),
    SOCIAL("👥", "Sozial", "Plots und soziale Interaktionen"),
    EXPLORATION("🗺️", "Erkundung", "Die Welt erkunden");

    private final String emoji;
    private final String displayName;
    private final String description;

    AchievementCategory(String emoji, String displayName, String description) {
        this.emoji = emoji;
        this.displayName = displayName;
        this.description = description;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedName() {
        return emoji + " " + displayName;
    }
}
