package de.rolandsw.schedulemc.weapon.upgrade;

public enum FireModeUpgradeType {
    SINGLE_PRECISION("Präzisions-Upgrade", -1),
    BURST("Burst-Modus-Upgrade", 1),
    AUTO("Vollautomatik-Upgrade", 2);

    private final String displayName;
    private final int modeIndex;

    FireModeUpgradeType(String displayName, int modeIndex) {
        this.displayName = displayName;
        this.modeIndex = modeIndex;
    }

    public String getDisplayName() { return displayName; }
    public int getModeIndex() { return modeIndex; }
}
