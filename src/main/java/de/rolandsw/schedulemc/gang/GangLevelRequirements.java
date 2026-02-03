package de.rolandsw.schedulemc.gang;

/**
 * XP-Anforderungen fuer Gang-Level (1-30).
 *
 * Progression:
 * - Level 1-10: Aufbau-Phase (niedrige XP)
 * - Level 11-20: Etablierung
 * - Level 21-30: Endgame (hohe XP)
 *
 * Formel: BASE_XP * (level ^ EXPONENT)
 */
public class GangLevelRequirements {

    public static final int MAX_LEVEL = 30;
    private static final int BASE_XP = 200;
    private static final double EXPONENT = 1.7;

    /** Maximale Mitglieder pro Gang-Level */
    private static final int BASE_MEMBERS = 5;
    private static final int MAX_MEMBERS = 20;

    /** Basis-Territory-Chunks */
    private static final int BASE_TERRITORY = 1;

    private static final int[] XP_TABLE = new int[MAX_LEVEL + 1];

    static {
        XP_TABLE[0] = 0;
        for (int level = 1; level <= MAX_LEVEL; level++) {
            XP_TABLE[level] = (int) (BASE_XP * Math.pow(level, EXPONENT));
        }
    }

    private GangLevelRequirements() {}

    public static int getRequiredXP(int level) {
        level = Math.max(0, Math.min(MAX_LEVEL, level));
        return XP_TABLE[level];
    }

    public static int getXPToNextLevel(int currentLevel, int currentXP) {
        if (currentLevel >= MAX_LEVEL) return 0;
        return Math.max(0, XP_TABLE[currentLevel + 1] - currentXP);
    }

    public static int getLevelForXP(int totalXP) {
        for (int level = MAX_LEVEL; level >= 1; level--) {
            if (totalXP >= XP_TABLE[level]) return level;
        }
        return 0;
    }

    public static double getProgress(int currentLevel, int currentXP) {
        if (currentLevel >= MAX_LEVEL) return 1.0;
        if (currentLevel < 0) return 0.0;

        int currentLevelXP = XP_TABLE[currentLevel];
        int nextLevelXP = XP_TABLE[currentLevel + 1];
        int range = nextLevelXP - currentLevelXP;
        if (range <= 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, (double) (currentXP - currentLevelXP) / range));
    }

    /**
     * Max Mitglieder fuer ein Gang-Level.
     * Start: 5, alle 5 Level +3, max 20.
     */
    public static int getMaxMembers(int gangLevel) {
        int extra = (gangLevel / 5) * 3;
        return Math.min(MAX_MEMBERS, BASE_MEMBERS + extra);
    }

    /**
     * Max Territory-Chunks (ohne Perks).
     * Basis: 1, steigt mit Level.
     */
    public static int getBaseMaxTerritory(int gangLevel) {
        if (gangLevel < 3) return BASE_TERRITORY;
        if (gangLevel < 8) return 4;
        if (gangLevel < 15) return 9;
        if (gangLevel < 22) return 16;
        return 25;
    }

    /**
     * Perk-Punkte die bei einem bestimmten Level verfuegbar sind.
     * 1 Punkt pro Level ab Level 3.
     */
    public static int getAvailablePerkPoints(int gangLevel) {
        return Math.max(0, gangLevel - 2);
    }
}
