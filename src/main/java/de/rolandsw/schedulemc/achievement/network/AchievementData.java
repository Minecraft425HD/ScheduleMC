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
        // Enum-Ordinal statt String: spart ~20 Bytes pro Achievement
        try {
            buf.writeInt(AchievementCategory.valueOf(categoryName).ordinal());
        } catch (IllegalArgumentException e) {
            buf.writeInt(0);
        }
        try {
            buf.writeInt(AchievementTier.valueOf(tierName).ordinal());
        } catch (IllegalArgumentException e) {
            buf.writeInt(0);
        }
        buf.writeDouble(requirement);
        buf.writeBoolean(hidden);
        buf.writeDouble(progress);
        buf.writeBoolean(unlocked);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    private static final AchievementCategory[] CATEGORIES = AchievementCategory.values();
    private static final AchievementTier[] TIERS = AchievementTier.values();

    public static AchievementData decode(FriendlyByteBuf buf) {
        String id = buf.readUtf(64);
        String name = buf.readUtf(64);
        String description = buf.readUtf(256);
        int categoryOrdinal = buf.readInt();
        int tierOrdinal = buf.readInt();
        String categoryName = (categoryOrdinal >= 0 && categoryOrdinal < CATEGORIES.length)
            ? CATEGORIES[categoryOrdinal].name() : AchievementCategory.values()[0].name();
        String tierName = (tierOrdinal >= 0 && tierOrdinal < TIERS.length)
            ? TIERS[tierOrdinal].name() : AchievementTier.values()[0].name();
        return new AchievementData(
            id, name, description, categoryName, tierName,
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
    public AchievementCategory getCategory() {
        try { return AchievementCategory.valueOf(categoryName); }
        catch (IllegalArgumentException e) { return CATEGORIES[0]; }
    }
    public AchievementTier getTier() {
        try { return AchievementTier.valueOf(tierName); }
        catch (IllegalArgumentException e) { return TIERS[0]; }
    }
    public double getRequirement() { return requirement; }
    public boolean isHidden() { return hidden; }
    public double getProgress() { return progress; }
    public boolean isUnlocked() { return unlocked; }
}
