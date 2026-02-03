package de.rolandsw.schedulemc.gang;

import java.util.UUID;

/**
 * Daten eines Gang-Mitglieds.
 */
public class GangMemberData {

    private final UUID playerUUID;
    private GangRank rank;
    private int contributedXP;
    private long joinTimestamp;

    public GangMemberData(UUID playerUUID, GangRank rank) {
        this.playerUUID = playerUUID;
        this.rank = rank;
        this.contributedXP = 0;
        this.joinTimestamp = System.currentTimeMillis();
    }

    /** Deserialisierung */
    public GangMemberData(UUID playerUUID, GangRank rank, int contributedXP, long joinTimestamp) {
        this.playerUUID = playerUUID;
        this.rank = rank;
        this.contributedXP = contributedXP;
        this.joinTimestamp = joinTimestamp;
    }

    public UUID getPlayerUUID() { return playerUUID; }
    public GangRank getRank() { return rank; }
    public int getContributedXP() { return contributedXP; }
    public long getJoinTimestamp() { return joinTimestamp; }

    public void setRank(GangRank rank) { this.rank = rank; }

    public void addContributedXP(int xp) {
        this.contributedXP += xp;
    }
}
