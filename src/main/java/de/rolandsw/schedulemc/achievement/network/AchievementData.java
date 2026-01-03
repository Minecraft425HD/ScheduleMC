package de.rolandsw.schedulemc.achievement.network;

import de.rolandsw.schedulemc.achievement.AchievementCategory;
import de.rolandsw.schedulemc.achievement.AchievementTier;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Serializable Achievement Data für Network Transfer
 */
public class AchievementData {
    private final String id;
    private final String name;
    private final String description;
    private final String categoryName;
    private final String tierName;
    private final double requirement;
    private final boolean hidden;
    private final double progress;
    private final boolean unlocked;

    public AchievementData(String id, String name, String description, String categoryName,
                           String tierName, double requirement, boolean hidden,
                           double progress, boolean unlocked) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.tierName = tierName;
        this.requirement = requirement;
        this.hidden = hidden;
        this.progress = progress;
        this.unlocked = unlocked;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(name);
        buf.writeUtf(description);
        buf.writeUtf(categoryName);
        buf.writeUtf(tierName);
        buf.writeDouble(requirement);
        buf.writeBoolean(hidden);
        buf.writeDouble(progress);
        buf.writeBoolean(unlocked);
    }

    public static AchievementData decode(FriendlyByteBuf buf) {
        return new AchievementData(
            buf.readUtf(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readDouble(),
            buf.readBoolean(),
            buf.readDouble(),
            buf.readBoolean()
        );
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AchievementCategory getCategory() { return AchievementCategory.valueOf(categoryName); }
    public AchievementTier getTier() { return AchievementTier.valueOf(tierName); }
    public double getRequirement() { return requirement; }
    public boolean isHidden() { return hidden; }
    public double getProgress() { return progress; }
    public boolean isUnlocked() { return unlocked; }
}
