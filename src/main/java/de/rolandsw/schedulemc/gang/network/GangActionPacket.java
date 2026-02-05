package de.rolandsw.schedulemc.gang.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.gang.*;
import de.rolandsw.schedulemc.gang.mission.GangMissionManager;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.level.ProducerLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client-zu-Server Packet fuer Gang-Aktionen.
 */
public class GangActionPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    public enum ActionType {
        CREATE, INVITE, ACCEPT_INVITE, LEAVE, KICK, PROMOTE, DISBAND, UNLOCK_PERK, SET_FEE, CLAIM_MISSION
    }

    private final ActionType action;
    private final String stringParam;  // Name/Tag/PerkName
    private final String stringParam2; // Tag/Color
    private final UUID targetUUID;     // Ziel-Spieler fuer Invite/Kick/Promote

    public GangActionPacket(ActionType action, String param1, String param2, UUID target) {
        this.action = action;
        this.stringParam = param1;
        this.stringParam2 = param2;
        this.targetUUID = target;
    }

    public static GangActionPacket create(String name, String tag, String colorName) {
        return new GangActionPacket(ActionType.CREATE, name, tag + "|" + colorName, UUID.randomUUID());
    }

    public static GangActionPacket invite(UUID targetUUID) {
        return new GangActionPacket(ActionType.INVITE, "", "", targetUUID);
    }

    public static GangActionPacket inviteByName(String playerName) {
        return new GangActionPacket(ActionType.INVITE, playerName, "", UUID.randomUUID());
    }

    public static GangActionPacket acceptInvite(UUID gangId) {
        return new GangActionPacket(ActionType.ACCEPT_INVITE, "", "", gangId);
    }

    public static GangActionPacket leave() {
        return new GangActionPacket(ActionType.LEAVE, "", "", UUID.randomUUID());
    }

    public static GangActionPacket kick(UUID targetUUID) {
        return new GangActionPacket(ActionType.KICK, "", "", targetUUID);
    }

    public static GangActionPacket promote(UUID targetUUID, GangRank rank) {
        return new GangActionPacket(ActionType.PROMOTE, rank.name(), "", targetUUID);
    }

    public static GangActionPacket disband() {
        return new GangActionPacket(ActionType.DISBAND, "", "", UUID.randomUUID());
    }

    public static GangActionPacket unlockPerk(String perkName) {
        return new GangActionPacket(ActionType.UNLOCK_PERK, perkName, "", UUID.randomUUID());
    }

    public static GangActionPacket setFee(int fee) {
        return new GangActionPacket(ActionType.SET_FEE, String.valueOf(fee), "", UUID.randomUUID());
    }

    public static GangActionPacket claimMission(String missionId) {
        return new GangActionPacket(ActionType.CLAIM_MISSION, missionId, "", UUID.randomUUID());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(stringParam, 64);
        buf.writeUtf(stringParam2, 64);
        buf.writeUUID(targetUUID);
    }

    public static GangActionPacket decode(FriendlyByteBuf buf) {
        ActionType action = buf.readEnum(ActionType.class);
        String param1 = buf.readUtf(64);
        String param2 = buf.readUtf(64);
        UUID target = buf.readUUID();
        return new GangActionPacket(action, param1, param2, target);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            GangManager manager = GangManager.getInstance();
            if (manager == null) {
                sendError(player, "Gang-System nicht verfuegbar.");
                return;
            }

            switch (action) {
                case CREATE -> handleCreate(player, manager);
                case INVITE -> handleInvite(player, manager);
                case ACCEPT_INVITE -> handleAcceptInvite(player, manager);
                case LEAVE -> handleLeave(player, manager);
                case KICK -> handleKick(player, manager);
                case PROMOTE -> handlePromote(player, manager);
                case DISBAND -> handleDisband(player, manager);
                case UNLOCK_PERK -> handleUnlockPerk(player, manager);
                case SET_FEE -> handleSetFee(player, manager);
                case CLAIM_MISSION -> handleClaimMission(player, manager);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static final int GANG_CREATE_LEVEL = 15;
    private static final double GANG_CREATE_COST = 25000.0;
    private static final int GANG_JOIN_LEVEL = 5;
    private static final double GANG_JOIN_COST = 2500.0;

    private void handleCreate(ServerPlayer player, GangManager manager) {
        // Level-Check
        int playerLevel = ProducerLevel.getInstance().getPlayerLevel(player.getUUID());
        if (playerLevel < GANG_CREATE_LEVEL) {
            sendError(player, "Du brauchst Level " + GANG_CREATE_LEVEL + " um eine Gang zu gruenden (aktuell: " + playerLevel + ").");
            return;
        }

        // Geld-Check
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < GANG_CREATE_COST) {
            sendError(player, "Du brauchst " + (int) GANG_CREATE_COST + "\u20AC um eine Gang zu gruenden (aktuell: " + (int) balance + "\u20AC).");
            return;
        }

        String[] parts = stringParam2.split("\\|", 2);
        String tag = parts.length > 0 ? parts[0] : "";
        String colorName = parts.length > 1 ? parts[1] : "WHITE";

        ChatFormatting color = ChatFormatting.getByName(colorName);
        if (color == null) color = ChatFormatting.WHITE;

        Gang gang = manager.createGang(stringParam, tag, player.getUUID(), color);
        if (gang != null) {
            EconomyManager.withdraw(player.getUUID(), GANG_CREATE_COST);
            sendSuccess(player, "Gang '" + gang.getName() + "' [" + gang.getTag() + "] gegruendet! (-" + (int) GANG_CREATE_COST + "\u20AC)");
            GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
        } else {
            sendError(player, "Gang konnte nicht erstellt werden. Name/Tag bereits vergeben oder ungueltig.");
        }
    }

    private void handleInvite(ServerPlayer player, GangManager manager) {
        Gang gang = manager.getPlayerGang(player.getUUID());
        if (gang == null) { sendError(player, "Du bist in keiner Gang."); return; }

        GangRank rank = gang.getRank(player.getUUID());
        if (rank == null || !rank.canInvite()) { sendError(player, "Keine Berechtigung."); return; }

        // Name-basierte Einladung (aus App) oder UUID-basiert (aus Command)
        ServerPlayer target;
        if (!stringParam.isEmpty()) {
            target = player.getServer().getPlayerList().getPlayerByName(stringParam);
        } else {
            target = player.getServer().getPlayerList().getPlayer(targetUUID);
        }
        if (target == null) { sendError(player, "Spieler nicht online."); return; }

        if (gang.invite(target.getUUID())) {
            sendSuccess(player, target.getGameProfile().getName() + " eingeladen.");
            target.sendSystemMessage(Component.literal(
                    "\u00A76Gang-Einladung: \u00A7f" + gang.getName() +
                    " \u00A77| Nutze \u00A7e/gang accept\u00A77 zum Beitreten (5 Min.)"));
        } else {
            sendError(player, "Einladung fehlgeschlagen (bereits Mitglied oder Gang voll).");
        }
    }

    private void handleAcceptInvite(ServerPlayer player, GangManager manager) {
        // Level-Check
        int playerLevel = ProducerLevel.getInstance().getPlayerLevel(player.getUUID());
        if (playerLevel < GANG_JOIN_LEVEL) {
            sendError(player, "Du brauchst Level " + GANG_JOIN_LEVEL + " um einer Gang beizutreten (aktuell: " + playerLevel + ").");
            return;
        }

        // Geld-Check
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < GANG_JOIN_COST) {
            sendError(player, "Du brauchst " + (int) GANG_JOIN_COST + "\u20AC um beizutreten (aktuell: " + (int) balance + "\u20AC).");
            return;
        }

        for (Gang gang : manager.getAllGangs()) {
            if (gang.hasValidInvite(player.getUUID())) {
                if (manager.joinGang(player.getUUID(), gang.getGangId())) {
                    EconomyManager.withdraw(player.getUUID(), GANG_JOIN_COST);
                    sendSuccess(player, "Du bist der Gang '" + gang.getName() + "' beigetreten! (-" + (int) GANG_JOIN_COST + "\u20AC)");
                    GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
                    return;
                }
            }
        }
        sendError(player, "Keine gueltige Einladung gefunden.");
    }

    private void handleLeave(ServerPlayer player, GangManager manager) {
        Gang gang = manager.getPlayerGang(player.getUUID());
        if (gang == null) { sendError(player, "Du bist in keiner Gang."); return; }

        if (manager.leaveGang(player.getUUID())) {
            sendSuccess(player, "Du hast die Gang verlassen.");
            GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
        } else {
            sendError(player, "Konnte Gang nicht verlassen (als Boss zuerst /gang disband oder Boss uebertragen).");
        }
    }

    private void handleKick(ServerPlayer player, GangManager manager) {
        if (manager.kickMember(player.getUUID(), targetUUID)) {
            sendSuccess(player, "Spieler aus der Gang entfernt.");
            GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
        } else {
            sendError(player, "Kick fehlgeschlagen (keine Berechtigung oder Spieler nicht in Gang).");
        }
    }

    private void handlePromote(ServerPlayer player, GangManager manager) {
        try {
            GangRank newRank = GangRank.valueOf(stringParam);
            if (manager.promoteMember(player.getUUID(), targetUUID, newRank)) {
                sendSuccess(player, "Spieler zu " + newRank.getDisplayName() + " befoerdert.");
                GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
            } else {
                sendError(player, "Befoerderung fehlgeschlagen.");
            }
        } catch (IllegalArgumentException e) {
            sendError(player, "Ungueltiger Rang.");
        }
    }

    private void handleDisband(ServerPlayer player, GangManager manager) {
        UUID gangId = manager.getPlayerGangId(player.getUUID());
        if (gangId == null) { sendError(player, "Du bist in keiner Gang."); return; }

        if (manager.disbandGang(gangId, player.getUUID())) {
            sendSuccess(player, "Gang aufgeloest.");
            GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
        } else {
            sendError(player, "Nur der Boss kann die Gang aufloesen.");
        }
    }

    private void handleUnlockPerk(ServerPlayer player, GangManager manager) {
        Gang gang = manager.getPlayerGang(player.getUUID());
        if (gang == null) { sendError(player, "Du bist in keiner Gang."); return; }

        GangRank rank = gang.getRank(player.getUUID());
        if (rank == null || !rank.canManagePerks()) { sendError(player, "Keine Berechtigung."); return; }

        try {
            GangPerk perk = GangPerk.valueOf(stringParam);
            if (gang.unlockPerk(perk)) {
                manager.markDirty();
                sendSuccess(player, "Perk '" + perk.getDisplayName() + "' freigeschaltet!");
            } else {
                sendError(player, "Perk kann nicht freigeschaltet werden (Level/Punkte nicht ausreichend).");
            }
        } catch (IllegalArgumentException e) {
            sendError(player, "Ungueltiger Perk.");
        }
    }

    private void handleSetFee(ServerPlayer player, GangManager manager) {
        Gang gang = manager.getPlayerGang(player.getUUID());
        if (gang == null) { sendError(player, "Du bist in keiner Gang."); return; }

        GangRank rank = gang.getRank(player.getUUID());
        if (rank != GangRank.BOSS) { sendError(player, "Nur der Boss kann den Beitrag aendern."); return; }

        try {
            int fee = Integer.parseInt(stringParam.trim());
            if (fee < 0 || fee > 10000) {
                sendError(player, "Beitrag muss zwischen 0 und 10.000\u20AC liegen.");
                return;
            }
            gang.setWeeklyFee(fee);
            manager.markDirty();
            if (fee == 0) {
                sendSuccess(player, "Wochenbeitrag deaktiviert.");
            } else {
                sendSuccess(player, "Wochenbeitrag auf " + fee + "\u20AC/Woche gesetzt.");
            }
        } catch (NumberFormatException e) {
            sendError(player, "Ungueltiger Betrag.");
        }
    }

    private void handleClaimMission(ServerPlayer player, GangManager manager) {
        Gang gang = manager.getPlayerGang(player.getUUID());
        if (gang == null) { sendError(player, "Du bist in keiner Gang."); return; }

        GangMissionManager mm = GangMissionManager.getInstance();
        if (mm == null) { sendError(player, "Missions-System nicht verfuegbar."); return; }

        int[] reward = mm.claimReward(gang.getGangId(), stringParam);
        if (reward != null) {
            if (reward[0] > 0) {
                manager.awardGangXP(player.getUUID(), de.rolandsw.schedulemc.gang.GangXPSource.MISSION_COMPLETED, reward[0]);
            }
            if (reward[1] > 0) {
                gang.deposit(reward[1]);
                manager.markDirty();
            }
            sendSuccess(player, "Belohnung: +" + reward[0] + " XP" + (reward[1] > 0 ? ", +" + reward[1] + "\u20AC in Gang-Kasse" : ""));
        } else {
            sendError(player, "Belohnung kann nicht eingeloest werden.");
        }
    }

    private static void sendSuccess(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("\u00A7a[Gang] \u00A7f" + message));
    }

    private static void sendError(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("\u00A7c[Gang] \u00A7f" + message));
    }
}
