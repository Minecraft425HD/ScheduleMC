package de.rolandsw.schedulemc.npc.data;

/**
 * Aktivitätstypen für NPCs
 */
public enum ActivityType {
    WORK("Arbeit"),
    HOME("Zuhause"),
    FREETIME("Freizeit");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ActivityType fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return HOME; // Default
    }
}
