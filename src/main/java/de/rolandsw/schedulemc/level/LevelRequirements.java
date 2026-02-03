package de.rolandsw.schedulemc.level;

/**
 * XP-Anforderungen für jedes Level.
 *
 * Progression-Kurve:
 * - Level 1-10: Schneller Aufstieg (niedrige XP-Anforderungen)
 * - Level 11-20: Mittlerer Aufstieg
 * - Level 21-30: Langsamer Aufstieg (hohe XP-Anforderungen)
 *
 * Formel: xpForLevel = BASE_XP × (level ^ EXPONENT)
 * Das bedeutet: Jedes Level braucht mehr XP als das vorherige.
 */
public class LevelRequirements {

    /**
     * Maximales Level
     */
    public static final int MAX_LEVEL = 30;

    /**
     * Basis-XP für Level 1
     */
    private static final int BASE_XP = 100;

    /**
     * Exponent für die Progression-Kurve.
     * 1.5 = moderate Steigerung
     * 2.0 = starke Steigerung
     */
    private static final double EXPONENT = 1.8;

    /**
     * Vorberechnete XP-Tabelle für schnellen Lookup
     */
    private static final int[] XP_TABLE = new int[MAX_LEVEL + 1];

    static {
        XP_TABLE[0] = 0;
        for (int level = 1; level <= MAX_LEVEL; level++) {
            XP_TABLE[level] = calculateXPForLevel(level);
        }
    }

    private LevelRequirements() {
        // Utility class
    }

    /**
     * Berechnet die XP die für ein bestimmtes Level benötigt werden.
     *
     * @param level Ziel-Level (1-30)
     * @return Benötigte Gesamt-XP um dieses Level zu erreichen
     */
    public static int getRequiredXP(int level) {
        level = Math.max(0, Math.min(MAX_LEVEL, level));
        return XP_TABLE[level];
    }

    /**
     * Berechnet die XP die noch fehlen um das nächste Level zu erreichen.
     *
     * @param currentLevel Aktuelles Level
     * @param currentXP    Aktuelle XP
     * @return Fehlende XP bis zum nächsten Level
     */
    public static int getXPToNextLevel(int currentLevel, int currentXP) {
        if (currentLevel >= MAX_LEVEL) return 0;
        return Math.max(0, XP_TABLE[currentLevel + 1] - currentXP);
    }

    /**
     * Berechnet das Level basierend auf der Gesamt-XP.
     *
     * @param totalXP Gesamt-XP
     * @return Erreichtes Level (0-30)
     */
    public static int getLevelForXP(int totalXP) {
        for (int level = MAX_LEVEL; level >= 1; level--) {
            if (totalXP >= XP_TABLE[level]) {
                return level;
            }
        }
        return 0;
    }

    /**
     * Berechnet den Fortschritt zum nächsten Level (0.0-1.0).
     *
     * @param currentLevel Aktuelles Level
     * @param currentXP    Aktuelle XP
     * @return Progress (0.0 = gerade Level erreicht, 1.0 = fast nächstes Level)
     */
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
     * Gibt eine formatierte Level-Leiste zurück.
     *
     * @param currentLevel Aktuelles Level
     * @param currentXP    Aktuelle XP
     * @return Formatierter Progress-Bar-String
     */
    public static String getProgressBar(int currentLevel, int currentXP) {
        double progress = getProgress(currentLevel, currentXP);
        int filled = (int) (progress * 20);
        int empty = 20 - filled;

        StringBuilder sb = new StringBuilder();
        sb.append("§a");
        for (int i = 0; i < filled; i++) sb.append("█");
        sb.append("§7");
        for (int i = 0; i < empty; i++) sb.append("░");

        sb.append(String.format(" §f%d%%", (int) (progress * 100)));

        return sb.toString();
    }

    /**
     * Gibt eine formatierte Level-Übersicht zurück.
     */
    public static String getLevelOverview(int currentLevel, int currentXP) {
        int nextLevelXP = currentLevel < MAX_LEVEL ? XP_TABLE[currentLevel + 1] : XP_TABLE[MAX_LEVEL];
        int xpToNext = getXPToNextLevel(currentLevel, currentXP);

        return String.format(
                "§6Level §f%d §7/ %d\n" +
                "§7XP: §f%d §7/ §f%d\n" +
                "%s\n" +
                "§7Nächstes Level: §f%d XP fehlen",
                currentLevel, MAX_LEVEL,
                currentXP, nextLevelXP,
                getProgressBar(currentLevel, currentXP),
                xpToNext
        );
    }

    /**
     * Interne Berechnung der XP für ein Level.
     */
    private static int calculateXPForLevel(int level) {
        return (int) (BASE_XP * Math.pow(level, EXPONENT));
    }

    /**
     * Gibt die vollständige XP-Tabelle zurück (für Debug/Admin).
     */
    public static String getFullXPTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6═══ XP-Tabelle ═══\n");

        for (int level = 1; level <= MAX_LEVEL; level++) {
            int xp = XP_TABLE[level];
            int diff = level > 1 ? xp - XP_TABLE[level - 1] : xp;
            sb.append(String.format("§7Level §f%2d§7: §f%,6d XP §7(+%,d)\n", level, xp, diff));
        }

        return sb.toString();
    }
}
