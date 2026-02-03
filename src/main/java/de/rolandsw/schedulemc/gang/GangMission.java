package de.rolandsw.schedulemc.gang;

import java.util.UUID;

/**
 * Eine taegliche Gang-Mission.
 *
 * Missionen werden taeglich generiert und koennen von der Gang gemeinsam abgeschlossen werden.
 */
public class GangMission {

    public enum MissionType {
        SELL_ITEMS("Verkaufe %d Einheiten", GangXPSource.MISSION_COMPLETED),
        HOLD_TERRITORY("Halte %d Territory-Chunks fuer 24h", GangXPSource.MISSION_COMPLETED),
        EARN_REVENUE("Erwirtschafte %d\u20AC Umsatz", GangXPSource.MISSION_COMPLETED),
        AVOID_ARREST("Vermeide Verhaftungen fuer %d Stunden", GangXPSource.MISSION_COMPLETED),
        RECRUIT_MEMBER("Rekrutiere %d neue Mitglieder", GangXPSource.MISSION_COMPLETED),
        PRODUCE_ITEMS("Produziere %d Einheiten", GangXPSource.MISSION_COMPLETED);

        private final String descriptionFormat;
        private final GangXPSource xpSource;

        MissionType(String descriptionFormat, GangXPSource xpSource) {
            this.descriptionFormat = descriptionFormat;
            this.xpSource = xpSource;
        }

        public String getDescriptionFormat() { return descriptionFormat; }
        public GangXPSource getXpSource() { return xpSource; }
    }

    private final UUID missionId;
    private final MissionType type;
    private final int targetAmount;
    private volatile int currentProgress;
    private final int xpReward;
    private final int moneyReward;
    private volatile boolean completed;
    private final long createdTimestamp;
    private final long expiryTimestamp;

    public GangMission(MissionType type, int targetAmount, int xpReward, int moneyReward) {
        this.missionId = UUID.randomUUID();
        this.type = type;
        this.targetAmount = targetAmount;
        this.currentProgress = 0;
        this.xpReward = xpReward;
        this.moneyReward = moneyReward;
        this.completed = false;
        this.createdTimestamp = System.currentTimeMillis();
        this.expiryTimestamp = createdTimestamp + 24 * 60 * 60 * 1000L; // 24h
    }

    /** Deserialisierung */
    public GangMission(UUID missionId, MissionType type, int targetAmount, int currentProgress,
                       int xpReward, int moneyReward, boolean completed,
                       long createdTimestamp, long expiryTimestamp) {
        this.missionId = missionId;
        this.type = type;
        this.targetAmount = targetAmount;
        this.currentProgress = currentProgress;
        this.xpReward = xpReward;
        this.moneyReward = moneyReward;
        this.completed = completed;
        this.createdTimestamp = createdTimestamp;
        this.expiryTimestamp = expiryTimestamp;
    }

    public boolean addProgress(int amount) {
        if (completed || isExpired()) return false;
        currentProgress = Math.min(targetAmount, currentProgress + amount);
        if (currentProgress >= targetAmount) {
            completed = true;
            return true; // Mission abgeschlossen
        }
        return false;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTimestamp;
    }

    public String getDescription() {
        return String.format(type.getDescriptionFormat(), targetAmount);
    }

    public double getProgressPercent() {
        return targetAmount > 0 ? (double) currentProgress / targetAmount : 0;
    }

    // Getters
    public UUID getMissionId() { return missionId; }
    public MissionType getType() { return type; }
    public int getTargetAmount() { return targetAmount; }
    public int getCurrentProgress() { return currentProgress; }
    public int getXpReward() { return xpReward; }
    public int getMoneyReward() { return moneyReward; }
    public boolean isCompleted() { return completed; }
    public long getCreatedTimestamp() { return createdTimestamp; }
    public long getExpiryTimestamp() { return expiryTimestamp; }
}
