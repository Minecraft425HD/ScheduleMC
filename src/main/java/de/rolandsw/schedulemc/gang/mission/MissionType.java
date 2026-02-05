package de.rolandsw.schedulemc.gang.mission;

/**
 * Auftrags-Kategorien mit Reset-Intervallen.
 *
 * HOURLY:  2 Auftraege, reset jede volle Stunde
 * DAILY:   3 Auftraege, reset um Mitternacht
 * WEEKLY:  2 Auftraege, reset Montag 00:00
 */
public enum MissionType {
    HOURLY("Stuendlich", 3_600_000L, 2, "\u00A7e\u23F1", 10, 500),
    DAILY("Taeglich", 86_400_000L, 3, "\u00A7a\u2600", 50, 2_000),
    WEEKLY("Woechentlich", 604_800_000L, 2, "\u00A76\u2605", 150, 10_000);

    private final String displayName;
    private final long intervalMs;
    private final int missionCount;
    private final String icon;
    private final int bonusXP;
    private final int bonusMoney;

    MissionType(String displayName, long intervalMs, int missionCount,
                String icon, int bonusXP, int bonusMoney) {
        this.displayName = displayName;
        this.intervalMs = intervalMs;
        this.missionCount = missionCount;
        this.icon = icon;
        this.bonusXP = bonusXP;
        this.bonusMoney = bonusMoney;
    }

    public String getDisplayName() { return displayName; }
    public long getIntervalMs() { return intervalMs; }
    public int getMissionCount() { return missionCount; }
    public String getIcon() { return icon; }
    public int getBonusXP() { return bonusXP; }
    public int getBonusMoney() { return bonusMoney; }

    /**
     * Berechnet den naechsten Reset-Zeitpunkt nach dem gegebenen Timestamp.
     */
    public long getNextResetAfter(long timestamp) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        switch (this) {
            case HOURLY:
                cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                break;
            case DAILY:
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                break;
            case WEEKLY:
                int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                int daysUntilMon = (java.util.Calendar.MONDAY - dow + 7) % 7;
                if (daysUntilMon == 0) daysUntilMon = 7;
                cal.add(java.util.Calendar.DAY_OF_MONTH, daysUntilMon);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                break;
        }
        return cal.getTimeInMillis();
    }

    /**
     * Formatiert verbleibende Millisekunden als lesbaren Timer-String.
     */
    public static String formatTimer(long remainingMs) {
        if (remainingMs <= 0) return "Jetzt!";
        long seconds = remainingMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "min";
    }
}
