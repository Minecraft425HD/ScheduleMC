package de.rolandsw.schedulemc.level.network;

import de.rolandsw.schedulemc.level.UnlockCategory;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Serializable Unlockable Data fuer Network Transfer.
 * Enthaelt alle Informationen ueber ein Unlockable fuer die Client-Anzeige.
 */
public class UnlockableData {
    private final String id;
    private final String description;
    private final int requiredLevel;
    private final int categoryOrdinal;
    private final boolean unlocked;

    private static final UnlockCategory[] CATEGORIES = UnlockCategory.values();

    public UnlockableData(String id, String description, int requiredLevel,
                          int categoryOrdinal, boolean unlocked) {
        this.id = id;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.categoryOrdinal = categoryOrdinal;
        this.unlocked = unlocked;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(description);
        buf.writeInt(requiredLevel);
        buf.writeInt(categoryOrdinal);
        buf.writeBoolean(unlocked);
    }

    /**
     * SICHERHEIT: Max-Laenge fuer Strings gegen DoS/Memory-Angriffe
     */
    public static UnlockableData decode(FriendlyByteBuf buf) {
        String id = buf.readUtf(64);
        String description = buf.readUtf(256);
        int requiredLevel = buf.readInt();
        int categoryOrdinal = buf.readInt();
        boolean unlocked = buf.readBoolean();
        return new UnlockableData(id, description, requiredLevel, categoryOrdinal, unlocked);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public UnlockCategory getCategory() {
        if (categoryOrdinal >= 0 && categoryOrdinal < CATEGORIES.length) {
            return CATEGORIES[categoryOrdinal];
        }
        return CATEGORIES[0];
    }

    public int getCategoryOrdinal() {
        return categoryOrdinal;
    }

    public boolean isUnlocked() {
        return unlocked;
    }
}
