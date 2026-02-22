package de.rolandsw.schedulemc.gang;

/**
 * Reputationsstufen einer Gang.
 * Steigt mit Gang-Level und beeinflusst NPC-Verhalten.
 */
public enum GangReputation {

    UNKNOWN("Unbekannt", "\u00A77", 0, 0),
    BEKANNT("Bekannt", "\u00A7e", 5, 1),
    RESPEKTIERT("Respektiert", "\u00A76", 12, 2),
    GEFUERCHTET("Gefuerchtet", "\u00A7c", 20, 3),
    LEGENDAER("Legendaer", "\u00A74", 27, 4);

    private static final int LEVELS_PER_STAR = 6;

    private final String displayName;
    private final String colorCode;
    private final int requiredLevel;
    private final int starCount;

    GangReputation(String displayName, String colorCode, int requiredLevel, int starCount) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.requiredLevel = requiredLevel;
        this.starCount = starCount;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public int getRequiredLevel() { return requiredLevel; }
    public int getStarCount() { return starCount; }

    /**
     * Gibt die Reputation fuer ein bestimmtes Gang-Level zurueck.
     */
    public static GangReputation getForLevel(int gangLevel) {
        GangReputation result = UNKNOWN;
        for (GangReputation rep : values()) {
            if (gangLevel >= rep.requiredLevel) {
                result = rep;
            }
        }
        return result;
    }

    /**
     * Gibt die Sterne als formatierter String zurueck.
     * z.B. Level 15 → "★★★" (GEFUERCHTET = 3 Sterne)
     */
    public static String getStarsForLevel(int gangLevel) {
        GangReputation rep = getForLevel(gangLevel);
        if (rep.starCount == 0) return "";
        StringBuilder sb = new StringBuilder("\u00A74");
        for (int i = 0; i < rep.starCount; i++) {
            sb.append("\u2605");
        }
        return sb.toString();
    }

    /**
     * Gibt 1-5 Sterne basierend auf Gang-Level (je 6 Level = 1 Stern).
     */
    public static String getLevelStars(int gangLevel) {
        int stars = Math.min(5, Math.max(0, (gangLevel - 1) / LEVELS_PER_STAR + 1));
        if (stars == 0) return "";
        StringBuilder sb = new StringBuilder("\u00A74");
        for (int i = 0; i < stars; i++) {
            sb.append("\u2605");
        }
        return sb.toString();
    }

    public String getFormattedName() {
        return colorCode + displayName;
    }
}
