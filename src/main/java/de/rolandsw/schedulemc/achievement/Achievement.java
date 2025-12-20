package de.rolandsw.schedulemc.achievement;

import com.google.gson.annotations.SerializedName;

/**
 * Repräsentiert ein Achievement
 */
public class Achievement {
    @SerializedName("id")
    private final String id;

    @SerializedName("name")
    private final String name;

    @SerializedName("description")
    private final String description;

    @SerializedName("category")
    private final AchievementCategory category;

    @SerializedName("tier")
    private final AchievementTier tier;

    @SerializedName("requirement")
    private final double requirement;

    @SerializedName("hidden")
    private final boolean hidden;

    public Achievement(String id, String name, String description, AchievementCategory category,
                      AchievementTier tier, double requirement, boolean hidden) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.tier = tier;
        this.requirement = requirement;
        this.hidden = hidden;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AchievementCategory getCategory() { return category; }
    public AchievementTier getTier() { return tier; }
    public double getRequirement() { return requirement; }
    public boolean isHidden() { return hidden; }

    /**
     * Gibt formatierten Achievement-Namen zurück
     */
    public String getFormattedName() {
        return tier.getColorCode() + tier.getEmoji() + " §f" + name;
    }

    /**
     * Gibt formatierte Beschreibung zurück
     */
    public String getFormattedDescription() {
        return "§7" + description;
    }

    /**
     * Gibt Fortschritts-String zurück
     */
    public String getProgressString(double currentProgress) {
        if (hidden && currentProgress < requirement) {
            return "§8??? / ???";
        }
        return String.format("§e%.0f §7/ §e%.0f", currentProgress, requirement);
    }

    /**
     * Gibt Belohnungs-String zurück
     */
    public String getRewardString() {
        return String.format("§a+%.2f€", tier.getRewardMoney());
    }

    @Override
    public String toString() {
        return String.format("Achievement[%s, %s, %s]", id, name, tier);
    }
}
