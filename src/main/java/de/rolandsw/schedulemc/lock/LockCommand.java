package de.rolandsw.schedulemc.lock;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /lock Befehl fuer Schloss-Verwaltung.
 *
 * /lock code <lock-id> <code>   - Code eingeben fuer Zahlenschloss
 * /lock setcode <lock-id> <code> - Neuen Code setzen (nur Besitzer)
 * /lock authorize <lock-id> <player> - Spieler autorisieren
 * /lock info <lock-id>           - Info ueber ein Schloss
 * /lock remove <lock-id>         - Schloss entfernen (nur Besitzer)
 * /lock list                     - Eigene Schloesser anzeigen
 * /lock admin remove <pos>       - Admin: Schloss entfernen (OP 2)
 */
public class LockCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lock")
                .then(Commands.literal("code")
                        .then(Commands.argument("lockId", StringArgumentType.word())
                                .then(Commands.argument("code", StringArgumentType.word())
                                        .executes(ctx -> enterCode(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "lockId"),
                                                StringArgumentType.getString(ctx, "code"))))))
                .then(Commands.literal("setcode")
                        .then(Commands.argument("lockId", StringArgumentType.word())
                                .then(Commands.argument("code", StringArgumentType.word())
                                        .executes(ctx -> setCode(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "lockId"),
                                                StringArgumentType.getString(ctx, "code"))))))
                .then(Commands.literal("authorize")
                        .then(Commands.argument("lockId", StringArgumentType.word())
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(ctx -> authorize(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "lockId"),
                                                StringArgumentType.getString(ctx, "player"))))))
                .then(Commands.literal("info")
                        .then(Commands.argument("lockId", StringArgumentType.word())
                                .executes(ctx -> lockInfo(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "lockId")))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("lockId", StringArgumentType.word())
                                .executes(ctx -> removeLock(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "lockId")))))
                .then(Commands.literal("list")
                        .executes(ctx -> listLocks(ctx.getSource())))
                .then(Commands.literal("admin")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("lockId", StringArgumentType.word())
                                        .executes(ctx -> adminRemove(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "lockId"))))))
        );
    }

    private static int enterCode(CommandSourceStack src, String lockId, String code) {
        LockManager mgr = LockManager.getInstance();
        if (mgr == null) { src.sendFailure(Component.literal("Lock-System nicht verfuegbar")); return 0; }

        // Schloss mit dieser ID finden
        LockData data = findLockById(mgr, lockId);
        if (data == null) { src.sendFailure(Component.literal("\u00A7cSchloss nicht gefunden: " + lockId)); return 0; }
        if (!data.getType().hasCode()) { src.sendFailure(Component.literal("\u00A7cDieses Schloss hat keinen Code.")); return 0; }

        if (code.length() != 4) { src.sendFailure(Component.literal("\u00A7cCode muss 4-stellig sein!")); return 0; }

        if (data.getCode() != null && data.getCode().equals(code)) {
            src.sendSuccess(() -> Component.literal("\u00A7a\u2714 Code korrekt! Tuer entriegelt."), false);
            // Tuer oeffnen per Server-Logic
            openDoorForPlayer(src, data);
            return 1;
        } else {
            src.sendFailure(Component.literal("\u00A7c\u2716 Falscher Code!"));
            return 0;
        }
    }

    private static int setCode(CommandSourceStack src, String lockId, String code) {
        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return 0;

        LockData data = findLockById(mgr, lockId);
        if (data == null) { src.sendFailure(Component.literal("\u00A7cSchloss nicht gefunden.")); return 0; }
        if (!data.getType().hasCode()) { src.sendFailure(Component.literal("\u00A7cKein Zahlenschloss.")); return 0; }

        ServerPlayer player = src.getPlayer();
        if (player == null || !data.getOwnerUUID().equals(player.getUUID())) {
            src.sendFailure(Component.literal("\u00A7cNur der Besitzer kann den Code aendern!"));
            return 0;
        }

        if (code.length() != 4 || !code.matches("\\d{4}")) {
            src.sendFailure(Component.literal("\u00A7cCode muss 4 Ziffern haben!"));
            return 0;
        }

        data.setCode(code);
        mgr.markDirty();
        src.sendSuccess(() -> Component.literal("\u00A7a\u2714 Neuer Code gesetzt: \u00A7e\u00A7l" + code), false);
        return 1;
    }

    private static int authorize(CommandSourceStack src, String lockId, String playerName) {
        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return 0;

        LockData data = findLockById(mgr, lockId);
        if (data == null) { src.sendFailure(Component.literal("\u00A7cSchloss nicht gefunden.")); return 0; }

        ServerPlayer owner = src.getPlayer();
        if (owner == null || !data.getOwnerUUID().equals(owner.getUUID())) {
            src.sendFailure(Component.literal("\u00A7cNur der Besitzer kann Spieler autorisieren!"));
            return 0;
        }

        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) { src.sendFailure(Component.literal("\u00A7cSpieler nicht gefunden: " + playerName)); return 0; }

        data.addAuthorized(target.getUUID());
        mgr.markDirty();
        src.sendSuccess(() -> Component.literal(
                "\u00A7a\u2714 " + playerName + " kann jetzt Schluessel erstellen fuer Lock " + lockId), false);
        return 1;
    }

    private static int lockInfo(CommandSourceStack src, String lockId) {
        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return 0;

        LockData data = findLockById(mgr, lockId);
        if (data == null) { src.sendFailure(Component.literal("\u00A7cSchloss nicht gefunden.")); return 0; }

        src.sendSuccess(() -> Component.literal(
                "\u00A76\u2550\u2550\u2550 Lock: " + lockId + " \u2550\u2550\u2550"), false);
        src.sendSuccess(() -> Component.literal(
                "\u00A77Typ: \u00A7f" + data.getType().getDisplayName()), false);
        src.sendSuccess(() -> Component.literal(
                "\u00A77Besitzer: \u00A7f" + data.getOwnerName()), false);
        src.sendSuccess(() -> Component.literal(
                "\u00A77Position: \u00A7f" + data.getPosString()), false);
        if (data.getType().hasCode()) {
            ServerPlayer player = src.getPlayer();
            if (player != null && data.getOwnerUUID().equals(player.getUUID())) {
                src.sendSuccess(() -> Component.literal(
                        "\u00A77Code: \u00A7e\u00A7l" + data.getCode()), false);
            } else {
                src.sendSuccess(() -> Component.literal(
                        "\u00A77Code: \u00A78****"), false);
            }
        }
        src.sendSuccess(() -> Component.literal(
                "\u00A77Autorisiert: \u00A7f" + data.getAuthorizedPlayers().size() + " Spieler"), false);
        return 1;
    }

    private static int removeLock(CommandSourceStack src, String lockId) {
        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return 0;

        LockData data = findLockById(mgr, lockId);
        if (data == null) { src.sendFailure(Component.literal("\u00A7cSchloss nicht gefunden.")); return 0; }

        ServerPlayer player = src.getPlayer();
        if (player == null || !data.getOwnerUUID().equals(player.getUUID())) {
            src.sendFailure(Component.literal("\u00A7cNur der Besitzer kann das Schloss entfernen!"));
            return 0;
        }

        String posKey = LockManager.posKey(data.getDimension(), data.getDoorX(), data.getDoorY(), data.getDoorZ());
        mgr.removeLockForce(posKey);
        src.sendSuccess(() -> Component.literal("\u00A7a\u2714 Schloss entfernt."), false);
        return 1;
    }

    private static int listLocks(CommandSourceStack src) {
        LockManager mgr = LockManager.getInstance();
        ServerPlayer player = src.getPlayer();
        if (mgr == null || player == null) return 0;

        var locks = mgr.getPlayerLocks(player.getUUID());
        if (locks.isEmpty()) {
            src.sendSuccess(() -> Component.literal("\u00A77Du hast keine Schloesser."), false);
            return 1;
        }

        src.sendSuccess(() -> Component.literal(
                "\u00A76\u2550\u2550\u2550 Deine Schloesser (" + locks.size() + ") \u2550\u2550\u2550"), false);
        for (LockData d : locks) {
            src.sendSuccess(() -> Component.literal(
                    "\u00A7e" + d.getLockId() + " \u00A78" + d.getType().getDisplayName() +
                            " \u00A77" + d.getPosString()), false);
        }
        return 1;
    }

    private static int adminRemove(CommandSourceStack src, String lockId) {
        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return 0;

        LockData data = findLockById(mgr, lockId);
        if (data == null) { src.sendFailure(Component.literal("\u00A7cSchloss nicht gefunden.")); return 0; }

        String posKey = LockManager.posKey(data.getDimension(), data.getDoorX(), data.getDoorY(), data.getDoorZ());
        mgr.removeLockForce(posKey);
        src.sendSuccess(() -> Component.literal("\u00A7a\u2714 Schloss entfernt (Admin)."), false);
        return 1;
    }

    private static LockData findLockById(LockManager mgr, String lockId) {
        return mgr.findByLockId(lockId);
    }

    private static void openDoorForPlayer(CommandSourceStack src, LockData data) {
        ServerPlayer player = src.getPlayer();
        if (player == null) return;
        try {
            var level = player.serverLevel();
            var pos = new net.minecraft.core.BlockPos(data.getDoorX(), data.getDoorY(), data.getDoorZ());
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door) {
                door.setOpen(null, level, state, pos, !state.getValue(net.minecraft.world.level.block.DoorBlock.OPEN));
            }
        } catch (Exception ignored) {}
    }
}
