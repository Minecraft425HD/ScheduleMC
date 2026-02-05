package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.*;
import de.rolandsw.schedulemc.gang.mission.GangMission;
import de.rolandsw.schedulemc.gang.mission.GangMissionManager;
import de.rolandsw.schedulemc.gang.mission.MissionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Client fordert vollstaendige Gang-Daten an (fuer Gang-App).
 * Client -> Server
 */
public class RequestGangDataPacket {

    public RequestGangDataPacket() {}

    public void encode(FriendlyByteBuf buf) {}

    public static RequestGangDataPacket decode(FriendlyByteBuf buf) {
        return new RequestGangDataPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            GangManager manager = GangManager.getInstance();
            if (manager == null) {
                GangNetworkHandler.sendToPlayer(SyncGangDataPacket.noGang(), player);
                return;
            }

            Gang gang = manager.getPlayerGang(player.getUUID());
            if (gang == null) {
                GangNetworkHandler.sendToPlayer(SyncGangDataPacket.noGang(), player);
                return;
            }

            // Rang des anfragenden Spielers
            GangRank myRank = gang.getRank(player.getUUID());
            int myRankPriority = myRank != null ? myRank.getPriority() : 0;

            // Mitglieder-Info zusammenstellen
            List<SyncGangDataPacket.GangMemberInfo> memberInfos = new ArrayList<>();
            for (Map.Entry<UUID, GangMemberData> entry : gang.getMembers().entrySet()) {
                UUID memberUUID = entry.getKey();
                GangMemberData memberData = entry.getValue();

                String name = "???";
                boolean online = false;
                ServerPlayer memberPlayer = player.getServer().getPlayerList().getPlayer(memberUUID);
                if (memberPlayer != null) {
                    name = memberPlayer.getGameProfile().getName();
                    online = true;
                } else {
                    var profile = player.getServer().getProfileCache();
                    if (profile != null) {
                        var optional = profile.get(memberUUID);
                        if (optional.isPresent()) {
                            name = optional.get().getName();
                        }
                    }
                }

                memberInfos.add(new SyncGangDataPacket.GangMemberInfo(
                        memberUUID, name,
                        memberData.getRank().getDisplayName(),
                        memberData.getRank().getColorCode(),
                        memberData.getRank().getPriority(),
                        memberData.getContributedXP(),
                        online
                ));
            }

            // Perks
            Set<String> unlockedPerks = gang.getUnlockedPerks();

            // Missionen aus dem MissionManager
            List<SyncGangDataPacket.MissionInfo> missionInfos = new ArrayList<>();
            long hourlyResetMs = 0, dailyResetMs = 0, weeklyResetMs = 0;
            int weekXP = 0, weekMoney = 0, weekFees = 0, weekH = 0, weekD = 0, weekW = 0;

            GangMissionManager mm = GangMissionManager.getInstance();
            if (mm != null) {
                UUID gangId = gang.getGangId();
                for (GangMission m : mm.getMissions(gangId)) {
                    missionInfos.add(new SyncGangDataPacket.MissionInfo(
                            m.getMissionId(),
                            m.getType().ordinal(),
                            m.getDescription(),
                            m.getCurrentProgress(),
                            m.getTargetAmount(),
                            m.getXpReward(),
                            m.getMoneyReward(),
                            m.isCompleted(),
                            m.isClaimable()
                    ));
                }

                hourlyResetMs = mm.getResetRemainingMs(gangId, MissionType.HOURLY);
                dailyResetMs = mm.getResetRemainingMs(gangId, MissionType.DAILY);
                weeklyResetMs = mm.getResetRemainingMs(gangId, MissionType.WEEKLY);

                GangMissionManager.WeeklyStats ws = mm.getWeeklyStats(gangId);
                weekXP = ws.xpGained;
                weekMoney = ws.moneyEarned;
                weekFees = ws.feesCollected;
                weekH = ws.hourlyCompleted;
                weekD = ws.dailyCompleted;
                weekW = ws.weeklyCompleted;
            }

            SyncGangDataPacket packet = new SyncGangDataPacket(
                    true,
                    gang.getName(), gang.getTag(),
                    gang.getGangLevel(), gang.getGangXP(), gang.getGangBalance(),
                    gang.getColor().ordinal(),
                    gang.getMemberCount(), gang.getMaxMembers(),
                    gang.getTerritoryCount(), gang.getMaxTerritory(),
                    gang.getAvailablePerkPoints(), gang.getProgress(),
                    myRankPriority, gang.getWeeklyFee(),
                    hourlyResetMs, dailyResetMs, weeklyResetMs,
                    memberInfos, unlockedPerks, missionInfos,
                    weekXP, weekMoney, weekFees, weekH, weekD, weekW
            );

            GangNetworkHandler.sendToPlayer(packet, player);
        });
        ctx.get().setPacketHandled(true);
    }
}
