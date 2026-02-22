package de.rolandsw.schedulemc.mission;

/**
 * Kategorie einer Spieler-Mission.
 */
public enum MissionCategory {
    HAUPT("Hauptmissionen", "gui.missions.tab_haupt"),
    NEBEN("Nebenmissionen", "gui.missions.tab_neben");

    private final String displayName;
    private final String translationKey;

    MissionCategory(String displayName, String translationKey) {
        this.displayName = displayName;
        this.translationKey = translationKey;
    }

    public String getDisplayName() { return displayName; }
    public String getTranslationKey() { return translationKey; }
}
