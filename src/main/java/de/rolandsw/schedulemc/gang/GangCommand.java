package de.rolandsw.schedulemc.gang;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.rolandsw.schedulemc.gang.network.GangSyncHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

/**
 * /gang Befehl mit Unterbefehlen.
 *
 * /gang create <name> <tag> [farbe]
 * /gang invite <spieler>
 * /gang accept
 * /gang leave
 * /gang kick <spieler>
 * /gang promote <spieler> <rang>
 * /gang info
 * /gang list
 * /gang disband
 * /gang perk <perkname>
 *
 * Admin:
 * /gang admin setlevel <gang-name> <level>
 * /gang admin addxp <gang-name> <xp>
 */
public class GangCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gang")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("tag", StringArgumentType.string())
                                        .executes(ctx -> createGang(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                StringArgumentType.getString(ctx, "tag"),
                                                "WHITE"))
                                        .then(Commands.argument("color", StringArgumentType.string())
                                                .executes(ctx -> createGang(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "name"),
                                                        StringArgumentType.getString(ctx, "tag"),
                                                        StringArgumentType.getString(ctx, "color")))))))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> invitePlayer(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("accept")
                        .executes(ctx -> acceptInvite(ctx.getSource())))
                .then(Commands.literal("leave")
                        .executes(ctx -> leaveGang(ctx.getSource())))
                .then(Commands.literal("kick")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> kickPlayer(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("promote")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rank", StringArgumentType.string())
                                        .executes(ctx -> promotePlayer(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "rank"))))))
                .then(Commands.literal("info")
                        .executes(ctx -> showGangInfo(ctx.getSource())))
                .then(Commands.literal("list")
                        .executes(ctx -> listGangs(ctx.getSource())))
                .then(Commands.literal("disband")
                        .executes(ctx -> disbandGang(ctx.getSource())))
                .then(Commands.literal("perk")
                        .then(Commands.argument("perkname", StringArgumentType.string())
                                .executes(ctx -> unlockPerk(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "perkname")))))
                .then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("gangname", StringArgumentType.string())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 30))
                                                .executes(ctx -> adminSetLevel(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "gangname"),
                                                        IntegerArgumentType.getInteger(ctx, "level"))))))
                        .then(Commands.literal("addxp")
                                .then(Commands.argument("gangname", StringArgumentType.string())
                                        .then(Commands.argument("xp", IntegerArgumentType.integer(1, 100000))
                                                .executes(ctx -> adminAddXP(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "gangname"),
                                                        IntegerArgumentType.getInteger(ctx, "xp"))))))
                        .then(Commands.literal("info")
                                .then(Commands.argument("gangname", StringArgumentType.string())
                                        .executes(ctx -> adminGangInfo(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "gangname"))))))
                .then(Commands.literal("task")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("editor")
                                .executes(ctx -> openScenarioEditor(ctx.getSource()))))
        );
    }

    private static int createGang(CommandSourceStack source, String name, String tag, String colorName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Nur Spieler koennen Gangs erstellen."));
            return 0;
        }

        GangManager manager = GangManager.getInstance();
        if (manager == null) { sendError(player, "Gang-System nicht verfuegbar."); return 0; }

        ChatFormatting color = ChatFormatting.getByName(colorName.toUpperCase());
        if (color == null || !color.isColor()) color = ChatFormatting.WHITE;

        Gang gang = manager.createGang(name, tag, player.getUUID(), color);
        if (gang != null) {
            sendSuccess(player, "Gang \u00A7" + color.getChar() + "'" + name + "' [" + gang.getTag() + "]\u00A7a gegruendet!");
            GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
            return 1;
        }
        sendError(player, "Erstellung fehlgeschlagen. Name/Tag vergeben, ungueltig, oder du bist schon in einer Gang.");
        return 0;
    }

    private static int invitePlayer(CommandSourceStack source, ServerPlayer target) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        Gang gang = manager.getPlayerGang(player.getUUID());
        if (gang == null) { sendError(player, "Du bist in keiner Gang."); return 0; }

        GangRank rank = gang.getRank(player.getUUID());
        if (rank == null || !rank.canInvite()) { sendError(player, "Keine Berechtigung."); return 0; }

        if (gang.invite(target.getUUID())) {
            sendSuccess(player, target.getGameProfile().getName() + " eingeladen!");
            target.sendSystemMessage(Component.literal(
                    "\u00A76\u00A7l[Gang-Einladung] \u00A7f" + gang.getName() +
                    " \u00A77| \u00A7e/gang accept \u00A77zum Beitreten (5 Min.)"));
            return 1;
        }
        sendError(player, "Einladung fehlgeschlagen.");
        return 0;
    }

    private static int acceptInvite(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        for (Gang gang : manager.getAllGangs()) {
            if (gang.hasValidInvite(player.getUUID())) {
                if (manager.joinGang(player.getUUID(), gang.getGangId())) {
                    sendSuccess(player, "Du bist '" + gang.getName() + "' beigetreten!");
                    GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
                    return 1;
                }
            }
        }
        sendError(player, "Keine gueltige Einladung.");
        return 0;
    }

    private static int leaveGang(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        if (manager.leaveGang(player.getUUID())) {
            sendSuccess(player, "Gang verlassen.");
            GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
            return 1;
        }
        sendError(player, "Konnte Gang nicht verlassen (als Boss: /gang disband oder Boss uebertragen).");
        return 0;
    }

    private static int kickPlayer(CommandSourceStack source, ServerPlayer target) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        if (manager.kickMember(player.getUUID(), target.getUUID())) {
            sendSuccess(player, target.getGameProfile().getName() + " entfernt.");
            target.sendSystemMessage(Component.literal("\u00A7c[Gang] Du wurdest aus der Gang entfernt."));
            GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
            return 1;
        }
        sendError(player, "Kick fehlgeschlagen.");
        return 0;
    }

    private static int promotePlayer(CommandSourceStack source, ServerPlayer target, String rankName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        try {
            GangRank newRank = GangRank.valueOf(rankName.toUpperCase());
            if (manager.promoteMember(player.getUUID(), target.getUUID(), newRank)) {
                sendSuccess(player, target.getGameProfile().getName() + " zu " + newRank.getDisplayName() + " befoerdert.");
                GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
                return 1;
            }
        } catch (IllegalArgumentException ignored) {}
        sendError(player, "Befoerderung fehlgeschlagen. Gueltige Raenge: RECRUIT, MEMBER, UNDERBOSS, BOSS");
        return 0;
    }

    private static int showGangInfo(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        Gang gang = manager.getPlayerGang(player.getUUID());
        if (gang == null) { sendError(player, "Du bist in keiner Gang."); return 0; }

        player.sendSystemMessage(Component.literal("\u00A76\u2550\u2550\u2550 " + gang.getFormattedTag() + " \u00A76\u2550\u2550\u2550"));
        player.sendSystemMessage(Component.literal("\u00A77Name: \u00A7f" + gang.getName()));
        player.sendSystemMessage(Component.literal("\u00A77Level: \u00A76" + gang.getGangLevel() + " " + gang.getStars()));
        player.sendSystemMessage(Component.literal("\u00A77XP: \u00A7f" + gang.getGangXP() + " \u00A77(" + gang.getXPToNextLevel() + " bis naechstes Level)"));
        player.sendSystemMessage(Component.literal("\u00A77Mitglieder: \u00A7f" + gang.getMemberCount() + "/" + gang.getMaxMembers()));
        player.sendSystemMessage(Component.literal("\u00A77Territory: \u00A7f" + gang.getTerritoryCount() + "/" + gang.getMaxTerritory()));
        player.sendSystemMessage(Component.literal("\u00A77Perks: \u00A7f" + gang.getUsedPerkPoints() + " genutzt, " + gang.getAvailablePerkPoints() + " verfuegbar"));
        player.sendSystemMessage(Component.literal("\u00A77Reputation: " + gang.getReputation().getFormattedName()));

        // Mitglieder
        player.sendSystemMessage(Component.literal("\u00A76Mitglieder:"));
        for (Map.Entry<UUID, GangMemberData> entry : gang.getMembers().entrySet()) {
            GangMemberData md = entry.getValue();
            String memberName = "???";
            var profile = player.getServer().getProfileCache();
            if (profile != null) {
                var opt = profile.get(entry.getKey());
                if (opt.isPresent()) memberName = opt.get().getName();
            }
            player.sendSystemMessage(Component.literal(
                    "  " + md.getRank().getFormattedName() + " \u00A7f" + memberName +
                    " \u00A78(+" + md.getContributedXP() + " XP)"));
        }
        return 1;
    }

    private static int listGangs(CommandSourceStack source) {
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        source.sendSystemMessage(Component.literal("\u00A76\u2550\u2550\u2550 Gangs (" + manager.getGangCount() + ") \u2550\u2550\u2550"));
        for (Gang gang : manager.getAllGangs()) {
            source.sendSystemMessage(Component.literal(
                    gang.getFormattedTag() + " \u00A7f" + gang.getName() +
                    " \u00A77Lv." + gang.getGangLevel() +
                    " \u00A77(" + gang.getMemberCount() + " Mitglieder)"));
        }
        return 1;
    }

    private static int disbandGang(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        UUID gangId = manager.getPlayerGangId(player.getUUID());
        if (gangId == null) { sendError(player, "Du bist in keiner Gang."); return 0; }

        if (manager.disbandGang(gangId, player.getUUID())) {
            sendSuccess(player, "Gang aufgeloest.");
            GangSyncHelper.broadcastAllPlayerInfos(player.getServer());
            return 1;
        }
        sendError(player, "Nur der Boss kann die Gang aufloesen.");
        return 0;
    }

    private static int unlockPerk(CommandSourceStack source, String perkName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        Gang gang = manager.getPlayerGang(player.getUUID());
        if (gang == null) { sendError(player, "Du bist in keiner Gang."); return 0; }

        GangRank rank = gang.getRank(player.getUUID());
        if (rank == null || !rank.canManagePerks()) { sendError(player, "Nur Boss kann Perks freischalten."); return 0; }

        try {
            GangPerk perk = GangPerk.valueOf(perkName.toUpperCase());
            if (gang.unlockPerk(perk)) {
                manager.markDirty();
                sendSuccess(player, "Perk '" + perk.getDisplayName() + "' freigeschaltet!");
                return 1;
            }
            sendError(player, "Perk nicht freischaltbar (Level/Punkte nicht ausreichend oder bereits freigeschaltet).");
        } catch (IllegalArgumentException e) {
            sendError(player, "Unbekannter Perk: " + perkName);
        }
        return 0;
    }

    private static int adminSetLevel(CommandSourceStack source, String gangName, int level) {
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        for (Gang gang : manager.getAllGangs()) {
            if (gang.getName().equalsIgnoreCase(gangName)) {
                int oldLevel = gang.getGangLevel();
                gang.setLevelDirect(level);
                manager.markDirty();
                source.sendSystemMessage(Component.literal(
                        "\u00A7a[Admin] Gang '" + gang.getName() + "' Level: " + oldLevel + " \u2192 " + gang.getGangLevel() +
                        " (XP: " + gang.getGangXP() + ")"));
                GangSyncHelper.broadcastAllPlayerInfos(source.getServer());
                return 1;
            }
        }
        source.sendFailure(Component.literal("Gang nicht gefunden: " + gangName));
        return 0;
    }

    private static int adminAddXP(CommandSourceStack source, String gangName, int xp) {
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        for (Gang gang : manager.getAllGangs()) {
            if (gang.getName().equalsIgnoreCase(gangName)) {
                int oldLevel = gang.getGangLevel();
                boolean leveledUp = gang.addXPDirect(xp);
                manager.markDirty();
                String msg = "\u00A7a[Admin] Gang '" + gang.getName() + "' +" + xp + " XP (Gesamt: " + gang.getGangXP() + ")";
                if (leveledUp) {
                    msg += " \u00A76Level-Up! " + oldLevel + " \u2192 " + gang.getGangLevel();
                }
                source.sendSystemMessage(Component.literal(msg));
                GangSyncHelper.broadcastAllPlayerInfos(source.getServer());
                return 1;
            }
        }
        source.sendFailure(Component.literal("Gang nicht gefunden: " + gangName));
        return 0;
    }

    private static int adminGangInfo(CommandSourceStack source, String gangName) {
        GangManager manager = GangManager.getInstance();
        if (manager == null) return 0;

        for (Gang gang : manager.getAllGangs()) {
            if (gang.getName().equalsIgnoreCase(gangName)) {
                source.sendSystemMessage(Component.literal("\u00A76\u2550\u2550\u2550 [Admin] " + gang.getFormattedTag() + " \u00A76\u2550\u2550\u2550"));
                source.sendSystemMessage(Component.literal("\u00A77Name: \u00A7f" + gang.getName() + " \u00A78(ID: " + gang.getGangId() + ")"));
                source.sendSystemMessage(Component.literal("\u00A77Level: \u00A76" + gang.getGangLevel() + "/" + GangLevelRequirements.MAX_LEVEL));
                source.sendSystemMessage(Component.literal("\u00A77XP: \u00A7f" + gang.getGangXP() + " \u00A77(naechstes Level: " + gang.getXPToNextLevel() + " XP)"));
                source.sendSystemMessage(Component.literal("\u00A77Mitglieder: \u00A7f" + gang.getMemberCount() + "/" + gang.getMaxMembers()));
                source.sendSystemMessage(Component.literal("\u00A77Kasse: \u00A7f" + gang.getGangBalance() + "\u20AC"));
                source.sendSystemMessage(Component.literal("\u00A77Beitrag: \u00A7f" + (gang.getWeeklyFee() > 0 ? gang.getWeeklyFee() + "\u20AC/Woche" : "keiner")));
                source.sendSystemMessage(Component.literal("\u00A77Perks: \u00A7f" + gang.getUsedPerkPoints() + " genutzt, " + gang.getAvailablePerkPoints() + " frei"));
                source.sendSystemMessage(Component.literal("\u00A77Reputation: " + gang.getReputation().getFormattedName()));
                return 1;
            }
        }
        source.sendFailure(Component.literal("Gang nicht gefunden: " + gangName));
        return 0;
    }

    private static int openScenarioEditor(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Nur Spieler koennen den Editor oeffnen."));
            return 0;
        }

        de.rolandsw.schedulemc.gang.scenario.ScenarioManager sm =
                de.rolandsw.schedulemc.gang.scenario.ScenarioManager.getInstance();
        if (sm == null) {
            source.sendFailure(Component.literal("Szenario-System nicht initialisiert!"));
            return 0;
        }

        String scenariosJson = sm.toJson();

        // NPC-Namen vom Server sammeln
        java.util.List<String> npcNames = new java.util.ArrayList<>();
        try {
            npcNames = de.rolandsw.schedulemc.managers.NPCNameRegistry.getAllNamesSorted();
        } catch (Exception ignored) {}

        // Grundstuecke vom Server sammeln
        java.util.List<de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket.PlotInfo> plots =
                new java.util.ArrayList<>();
        try {
            for (de.rolandsw.schedulemc.region.PlotRegion plot : de.rolandsw.schedulemc.region.PlotManager.getPlots()) {
                net.minecraft.core.BlockPos center = plot.getCenter();
                String plotName = plot.getPlotName() != null ? plot.getPlotName() : plot.getPlotId();
                String plotType = plot.getPlotType() != null ? plot.getPlotType().name() : "PUBLIC";
                plots.add(new de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket.PlotInfo(
                        plot.getPlotId(), plotName, plotType,
                        center.getX(), center.getY(), center.getZ()));
            }
        } catch (Exception ignored) {}

        de.rolandsw.schedulemc.gang.network.GangNetworkHandler.sendToPlayer(
                new de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket(
                        scenariosJson, npcNames, plots),
                player
        );
        source.sendSystemMessage(Component.literal(
                "\u00A7a[Szenario-Editor] Editor geoeffnet. ("
                + sm.getScenarioCount() + " Szenarien, " + sm.getActiveCount() + " aktiv, "
                + npcNames.size() + " NPCs, " + plots.size() + " Grundstuecke)"));
        return 1;
    }

    private static void sendSuccess(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("\u00A7a[Gang] \u00A7f" + message));
    }

    private static void sendError(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("\u00A7c[Gang] \u00A7f" + message));
    }
}
