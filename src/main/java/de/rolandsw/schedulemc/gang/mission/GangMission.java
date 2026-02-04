package de.rolandsw.schedulemc.gang.mission;

/**
 * Eine konkrete Auftrags-Instanz fuer eine Gang.
 *
 * Wird aus einem {@link MissionTemplate} generiert mit zufaelligen Zielwerten.
 * Tracking: Fortschritt wird via trackingKey vom MissionManager aktualisiert.
 */
public class GangMission {

    private final String missionId;
    private final MissionTemplate template;
    private final String description;
    private final int targetAmount;
    private final int xpReward;
    private final int moneyReward;
    private final long createdAt;

    private int currentProgress;
    private boolean completed;
    private boolean claimed;

    public GangMission(String missionId, MissionTemplate template,
                       String description, int targetAmount,
                       int xpReward, int moneyReward) {
        this.missionId = missionId;
        this.template = template;
        this.description = description;
        this.targetAmount = targetAmount;
        this.xpReward = xpReward;
        this.moneyReward = moneyReward;
        this.createdAt = System.currentTimeMillis();
        this.currentProgress = 0;
        this.completed = false;
        this.claimed = false;
    }

    /**
     * Deserialisierungs-Konstruktor.
     */
    public GangMission(String missionId, MissionTemplate template,
                       String description, int targetAmount,
                       int xpReward, int moneyReward, long createdAt,
                       int currentProgress, boolean completed, boolean claimed) {
        this.missionId = missionId;
        this.template = template;
        this.description = description;
        this.targetAmount = targetAmount;
        this.xpReward = xpReward;
        this.moneyReward = moneyReward;
        this.createdAt = createdAt;
        this.currentProgress = currentProgress;
        this.completed = completed;
        this.claimed = claimed;
    }

    /**
     * Addiert Fortschritt (fuer INCREMENTAL Tracking).
     * @return true wenn die Mission durch diesen Aufruf abgeschlossen wurde
     */
    public boolean addProgress(int amount) {
        if (completed) return false;
        currentProgress = Math.min(currentProgress + amount, targetAmount);
        if (currentProgress >= targetAmount) {
            completed = true;
            return true;
        }
        return false;
    }

    /**
     * Setzt den Fortschritt auf einen absoluten Wert (fuer THRESHOLD Tracking).
     * @return true wenn die Mission durch diesen Aufruf abgeschlossen wurde
     */
    public boolean setProgress(int value) {
        if (completed) return false;
        currentProgress = Math.min(value, targetAmount);
        if (currentProgress >= targetAmount) {
            completed = true;
            return true;
        }
        return false;
    }

    /**
     * Markiert die Belohnung als abgeholt.
     * @return true wenn erfolgreich (war completed und noch nicht claimed)
     */
    public boolean claim() {
        if (completed && !claimed) {
            claimed = true;
            return true;
        }
        return false;
    }

    public double getProgressPercent() {
        return targetAmount > 0 ? (double) currentProgress / targetAmount : 0;
    }

    public boolean isClaimable() {
        return completed && !claimed;
    }

    // Getters
    public String getMissionId() { return missionId; }
    public MissionTemplate getTemplate() { return template; }
    public MissionType getType() { return template.getType(); }
    public String getTrackingKey() { return template.getTrackingKey(); }
    public MissionTemplate.TrackingMode getTrackingMode() { return template.getTrackingMode(); }
    public String getDescription() { return description; }
    public int getTargetAmount() { return targetAmount; }
    public int getXpReward() { return xpReward; }
    public int getMoneyReward() { return moneyReward; }
    public long getCreatedAt() { return createdAt; }
    public int getCurrentProgress() { return currentProgress; }
    public boolean isCompleted() { return completed; }
    public boolean isClaimed() { return claimed; }
}
