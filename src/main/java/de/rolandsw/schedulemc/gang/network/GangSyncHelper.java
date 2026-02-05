package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.Gang;
import de.rolandsw.schedulemc.gang.GangManager;
import de.rolandsw.schedulemc.gang.GangMemberData;
import de.rolandsw.schedulemc.gang.GangRank;
import de.rolandsw.schedulemc.level.ProducerLevel;
import de.rolandsw.schedulemc.level.ProducerLevelData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hilfsfunktionen fuer die Gang-Datensynchronisierung.
 *
 * Baut PlayerGangInfo-Listen auf und sendet sie an Clients.
 */
public class GangSyncHelper {

    /**
     * Sendet Gang+Level Info aller Online-Spieler an ALLE Clients.
     * Aufrufen bei: Login, Gang-Aenderungen, periodisch (alle 60s).
     */
    public static void broadcastAllPlayerInfos(MinecraftServer server) {
        if (server == null) return;

        GangManager gangManager = GangManager.getInstance();
        ProducerLevel levelManager = ProducerLevel.getInstance();
        List<PlayerGangInfo> infos = new ArrayList<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            infos.add(buildPlayerInfo(player.getUUID(), gangManager, levelManager));
        }

        GangNetworkHandler.sendToAll(new SyncAllPlayerGangInfoPacket(infos));
    }

    /**
     * Sendet Gang+Level Info aller Online-Spieler an einen bestimmten Spieler.
     * Aufrufen bei: Spieler-Login.
     */
    public static void sendAllPlayerInfosToPlayer(ServerPlayer target, MinecraftServer server) {
        if (server == null) return;

        GangManager gangManager = GangManager.getInstance();
        ProducerLevel levelManager = ProducerLevel.getInstance();
        List<PlayerGangInfo> infos = new ArrayList<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            infos.add(buildPlayerInfo(player.getUUID(), gangManager, levelManager));
        }

        GangNetworkHandler.sendToPlayer(new SyncAllPlayerGangInfoPacket(infos), target);
    }

    /**
     * Baut PlayerGangInfo fuer einen einzelnen Spieler auf.
     */
    public static PlayerGangInfo buildPlayerInfo(UUID playerUUID,
                                                  GangManager gangManager,
                                                  ProducerLevel levelManager) {
        // ProducerLevel-Daten
        int playerLevel = 0;
        double playerProgress = 0.0;
        if (levelManager != null) {
            ProducerLevelData data = levelManager.getPlayerData(playerUUID);
            if (data != null) {
                playerLevel = data.getLevel();
                playerProgress = data.getProgress();
            }
        }

        // Gang-Daten
        if (gangManager == null) {
            return PlayerGangInfo.noGang(playerUUID, playerLevel, playerProgress);
        }

        Gang gang = gangManager.getPlayerGang(playerUUID);
        if (gang == null) {
            return PlayerGangInfo.noGang(playerUUID, playerLevel, playerProgress);
        }

        GangMemberData member = gang.getMember(playerUUID);
        GangRank rank = member != null ? member.getRank() : GangRank.RECRUIT;

        return new PlayerGangInfo(
                playerUUID,
                true,
                gang.getTag(),
                gang.getColor().ordinal(),
                gang.getGangLevel(),
                rank.getDisplayName(),
                rank.getColorCode(),
                playerLevel,
                playerProgress
        );
    }
}
