package de.rolandsw.schedulemc.mission;

import de.rolandsw.schedulemc.mission.client.PlayerMissionDto;

import java.util.UUID;

/**
 * Spieler-Instanz einer Mission.
 *
 * Thread-safe: compound read-modify-write Operationen sind synchronized,
 * mutable Felder sind volatile (wie GangMission-Pattern).
 */
public class PlayerMission {

    private final String missionId;
    private final String definitionId;
    private final UUID playerUUID;
    private final MissionDefinition definition;

    private volatile int currentProgress;
    private volatile MissionStatus status;
    private final long acceptedAt;
    private volatile long completedAt;
    private volatile long claimedAt;

    public PlayerMission(String missionId, MissionDefinition definition, UUID playerUUID) {
        this.missionId = missionId;
        this.definitionId = definition.getId();
        this.definition = definition;
        this.playerUUID = playerUUID;
        this.currentProgress = 0;
        this.status = MissionStatus.ACTIVE;
        this.acceptedAt = System.currentTimeMillis();
        this.completedAt = 0;
        this.claimedAt = 0;
    }

    /** Deserialisierungs-Konstruktor */
    public PlayerMission(String missionId, String definitionId, MissionDefinition definition,
                         UUID playerUUID, int currentProgress, MissionStatus status,
                         long acceptedAt, long completedAt, long claimedAt) {
        this.missionId = missionId;
        this.definitionId = definitionId;
        this.definition = definition;
        this.playerUUID = playerUUID;
        this.currentProgress = currentProgress;
        this.status = status;
        this.acceptedAt = acceptedAt;
        this.completedAt = completedAt;
        this.claimedAt = claimedAt;
    }

    /**
     * Erhöht den Fortschritt (für inkrementelles Tracking).
     * @return true wenn die Mission durch diesen Aufruf abgeschlossen wurde
     */
    public synchronized boolean addProgress(int amount) {
        if (status != MissionStatus.ACTIVE) return false;
        currentProgress = Math.min(currentProgress + amount, definition.getTargetAmount());
        if (currentProgress >= definition.getTargetAmount()) {
            status = MissionStatus.COMPLETED;
            completedAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Setzt den Fortschritt auf einen absoluten Wert (für Schwellwert-Tracking).
     * @return true wenn die Mission durch diesen Aufruf abgeschlossen wurde
     */
    public synchronized boolean setProgress(int value) {
        if (status != MissionStatus.ACTIVE) return false;
        currentProgress = Math.min(value, definition.getTargetAmount());
        if (currentProgress >= definition.getTargetAmount()) {
            status = MissionStatus.COMPLETED;
            completedAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Markiert die Belohnung als abgeholt.
     * @return true wenn erfolgreich (war COMPLETED und noch nicht CLAIMED)
     */
    public synchronized boolean claim() {
        if (status == MissionStatus.COMPLETED) {
            status = MissionStatus.CLAIMED;
            claimedAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public double getProgressPercent() {
        return definition.getTargetAmount() > 0
            ? (double) currentProgress / definition.getTargetAmount()
            : 0;
    }

    public boolean isClaimable() {
        return status == MissionStatus.COMPLETED;
    }

    public PlayerMissionDto toDto() {
        return new PlayerMissionDto(
            missionId, definitionId,
            definition.getTitle(), definition.getDescription(),
            definition.getCategory(), status,
            currentProgress, definition.getTargetAmount(),
            definition.getXpReward(), definition.getMoneyReward(),
            definition.getNpcGiverName()
        );
    }

    // Getters
    public String getMissionId() { return missionId; }
    public String getDefinitionId() { return definitionId; }
    public MissionDefinition getDefinition() { return definition; }
    public UUID getPlayerUUID() { return playerUUID; }
    public int getCurrentProgress() { return currentProgress; }
    public MissionStatus getStatus() { return status; }
    public long getAcceptedAt() { return acceptedAt; }
    public long getCompletedAt() { return completedAt; }
    public long getClaimedAt() { return claimedAt; }
}
