package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.*;
import de.rolandsw.schedulemc.level.LevelRequirements;
import de.rolandsw.schedulemc.level.ProducerLevel;
import de.rolandsw.schedulemc.level.ProducerLevelData;
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

    public void encode(FriendlyByteBuf buf) {
        // Leer - Spieler-UUID kommt aus dem Context
    }

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

                // Spielername bestimmen
                String name = "???";
                boolean online = false;
                ServerPlayer memberPlayer = player.getServer().getPlayerList().getPlayer(memberUUID);
                if (memberPlayer != null) {
                    name = memberPlayer.getGameProfile().getName();
                    online = true;
                } else {
                    // Offline-Name via GameProfile Cache
                    var profile = player.getServer().getProfileCache();
                    if (profile != null) {
                        var optional = profile.get(memberUUID);
                        if (optional.isPresent()) {
                            name = optional.get().getName();
                        }
                    }
                }

                memberInfos.add(new SyncGangDataPacket.GangMemberInfo(
                        memberUUID,
                        name,
                        memberData.getRank().getDisplayName(),
                        memberData.getRank().getColorCode(),
                        memberData.getRank().getPriority(),
                        memberData.getContributedXP(),
                        online
                ));
            }

            // Perks
            Set<String> unlockedPerks = gang.getUnlockedPerks();

            // Missionen (Platzhalter - wird spaeter durch MissionManager gefuellt)
            List<SyncGangDataPacket.MissionInfo> missions = List.of();

            SyncGangDataPacket packet = new SyncGangDataPacket(
                    true,
                    gang.getName(),
                    gang.getTag(),
                    gang.getGangLevel(),
                    gang.getGangXP(),
                    gang.getGangBalance(),
                    gang.getColor().ordinal(),
                    gang.getMemberCount(),
                    gang.getMaxMembers(),
                    gang.getTerritoryCount(),
                    gang.getMaxTerritory(),
                    gang.getAvailablePerkPoints(),
                    gang.getProgress(),
                    myRankPriority,
                    gang.getWeeklyFee(),
                    memberInfos,
                    unlockedPerks,
                    missions
            );

            GangNetworkHandler.sendToPlayer(packet, player);
        });
        ctx.get().setPacketHandled(true);
    }
}
