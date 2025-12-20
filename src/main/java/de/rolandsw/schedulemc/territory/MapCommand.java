package de.rolandsw.schedulemc.territory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.territory.network.OpenMapEditorPacket;
import de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command für Territory Map Editor
 *
 * Commands:
 * - /map edit - Öffnet den Territory Map Editor (OP-only)
 * - /map info - Zeigt Territory-Statistiken
 */
public class MapCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("map")
            .then(Commands.literal("edit")
                .requires(source -> source.hasPermission(2)) // OP-Level 2
                .executes(MapCommand::openEditor))
            .then(Commands.literal("info")
                .executes(MapCommand::showInfo))
        );
    }

    /**
     * Öffnet den Territory Map Editor
     */
    private static int openEditor(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§cNur Spieler können diesen Command nutzen!"));
            return 0;
        }

        // Sende Packet an Client zum Öffnen des Editors
        TerritoryNetworkHandler.sendToPlayer(new OpenMapEditorPacket(), player);

        context.getSource().sendSuccess(() ->
            Component.literal("§a§lTerritory Map Editor§r\n" +
                "§7Öffne Editor...\n" +
                "§7Steuerung:\n" +
                "  §e• Linksklick: §fTerritory setzen\n" +
                "  §e• Rechtsklick + Ziehen: §fKarte verschieben\n" +
                "  §e• Mausrad: §fZoom\n" +
                "  §e• WASD/Pfeiltasten: §fKarte verschieben"), false);

        return 1;
    }

    /**
     * Zeigt Territory-Statistiken
     */
    private static int showInfo(CommandContext<CommandSourceStack> context) {
        TerritoryManager manager = TerritoryManager.getInstance();

        if (manager == null) {
            context.getSource().sendFailure(Component.literal("§cTerritoryManager nicht verfügbar!"));
            return 0;
        }

        context.getSource().sendSuccess(() ->
            Component.literal("§6§l═══ TERRITORY INFO ═══§r"), false);

        context.getSource().sendSuccess(() ->
            Component.literal(manager.getStatistics()), false);

        context.getSource().sendSuccess(() ->
            Component.literal("§6§l════════════════════════§r"), false);

        return 1;
    }
}
