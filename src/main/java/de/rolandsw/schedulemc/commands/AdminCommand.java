package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.rolandsw.schedulemc.level.LevelRequirements;
import de.rolandsw.schedulemc.level.ProducerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Admin-Befehle fuer Spieler-Level (ProducerLevel).
 *
 * /admin setlevel <spieler> <level>   - Setzt das Level eines Spielers
 * /admin addxp <spieler> <xp>         - Fuegt einem Spieler XP hinzu
 * /admin getlevel <spieler>           - Zeigt das Level eines Spielers
 *
 * Erfordert OP Level 2.
 */
public class AdminCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("admin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 30))
                                        .executes(ctx -> setLevel(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("addxp")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("xp", IntegerArgumentType.integer(1, 100000))
                                        .executes(ctx -> addXP(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "xp"))))))
                .then(Commands.literal("getlevel")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> getLevel(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player")))))
        );
    }

    private static int setLevel(CommandSourceStack source, ServerPlayer target, int level) {
        ProducerLevel pl = ProducerLevel.getInstance();
        int oldLevel = pl.getPlayerLevel(target.getUUID());

        pl.setLevel(target.getUUID(), level);

        int newLevel = pl.getPlayerLevel(target.getUUID());
        source.sendSystemMessage(Component.literal(
                "\u00A7a[Admin] " + target.getGameProfile().getName() +
                " Level: " + oldLevel + " \u2192 " + newLevel));

        // Spieler informieren
        target.sendSystemMessage(Component.literal(
                "\u00A76\u00A7l\u2605 \u00A7fDein Level wurde auf \u00A76" + newLevel + "\u00A7f gesetzt!"));

        return 1;
    }

    private static int addXP(CommandSourceStack source, ServerPlayer target, int xp) {
        ProducerLevel pl = ProducerLevel.getInstance();
        int oldLevel = pl.getPlayerLevel(target.getUUID());

        // XP-Quelle fuer Admin-Vergabe
        pl.awardXP(target.getUUID(), de.rolandsw.schedulemc.level.XPSource.ADMIN_GRANT, xp, 1.0);

        int newLevel = pl.getPlayerLevel(target.getUUID());
        String msg = "\u00A7a[Admin] " + target.getGameProfile().getName() + " +" + xp + " XP";
        if (newLevel > oldLevel) {
            msg += " \u00A76Level-Up! " + oldLevel + " \u2192 " + newLevel;
        }
        source.sendSystemMessage(Component.literal(msg));

        return 1;
    }

    private static int getLevel(CommandSourceStack source, ServerPlayer target) {
        ProducerLevel pl = ProducerLevel.getInstance();
        int level = pl.getPlayerLevel(target.getUUID());

        var data = pl.getPlayerData(target.getUUID());
        int totalXP = data != null ? data.getTotalXP() : 0;
        int xpToNext = LevelRequirements.getXPToNextLevel(level, totalXP);

        source.sendSystemMessage(Component.literal(
                "\u00A76[Admin] " + target.getGameProfile().getName() + ": Level \u00A7f" + level +
                "\u00A76/" + LevelRequirements.MAX_LEVEL +
                " \u00A77(XP: " + totalXP + ", naechstes Level: " + xpToNext + " XP)"));

        return 1;
    }
}
