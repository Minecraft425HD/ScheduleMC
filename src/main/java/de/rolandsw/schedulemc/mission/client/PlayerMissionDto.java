package de.rolandsw.schedulemc.mission.client;

import de.rolandsw.schedulemc.mission.MissionCategory;
import de.rolandsw.schedulemc.mission.MissionStatus;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Serialisierbares Data-Transfer-Object für Spieler-Missionen.
 * Wird via Netzwerk vom Server zum Client übertragen.
 */
public class PlayerMissionDto {

    private final String missionId;
    private final String definitionId;
    private final String title;
    private final String description;
    private final MissionCategory category;
    private final MissionStatus status;
    private final int currentProgress;
    private final int targetAmount;
    private final int xpReward;
    private final int moneyReward;
    private final String npcGiverName;

    public PlayerMissionDto(String missionId, String definitionId, String title, String description,
                            MissionCategory category, MissionStatus status,
                            int currentProgress, int targetAmount,
                            int xpReward, int moneyReward, String npcGiverName) {
        this.missionId = missionId;
        this.definitionId = definitionId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.currentProgress = currentProgress;
        this.targetAmount = targetAmount;
        this.xpReward = xpReward;
        this.moneyReward = moneyReward;
        this.npcGiverName = npcGiverName != null ? npcGiverName : "";
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(missionId);
        buf.writeUtf(definitionId);
        buf.writeUtf(title);
        buf.writeUtf(description);
        buf.writeEnum(category);
        buf.writeEnum(status);
        buf.writeInt(currentProgress);
        buf.writeInt(targetAmount);
        buf.writeInt(xpReward);
        buf.writeInt(moneyReward);
        buf.writeUtf(npcGiverName);
    }

    public static PlayerMissionDto decode(FriendlyByteBuf buf) {
        return new PlayerMissionDto(
            buf.readUtf(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readEnum(MissionCategory.class),
            buf.readEnum(MissionStatus.class),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readUtf()
        );
    }

    public String getMissionId() { return missionId; }
    public String getDefinitionId() { return definitionId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MissionCategory getCategory() { return category; }
    public MissionStatus getStatus() { return status; }
    public int getCurrentProgress() { return currentProgress; }
    public int getTargetAmount() { return targetAmount; }
    public int getXpReward() { return xpReward; }
    public int getMoneyReward() { return moneyReward; }
    public String getNpcGiverName() { return npcGiverName; }

    public double getProgressPercent() {
        return targetAmount > 0 ? (double) currentProgress / targetAmount : 0;
    }

    public boolean isClaimable() {
        return status == MissionStatus.COMPLETED;
    }
}
