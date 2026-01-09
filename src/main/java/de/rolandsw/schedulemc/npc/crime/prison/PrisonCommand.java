package de.rolandsw.schedulemc.npc.crime.prison;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Admin-Commands für Gefängnis-System
 */
public class PrisonCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("prison")
            .requires(source -> source.hasPermission(2))

            // /prison create <plotId>
            .then(Commands.literal("create")
                .then(Commands.argument("plotId", StringArgumentType.word())
                    .executes(ctx -> createPrison(ctx.getSource(),
                        StringArgumentType.getString(ctx, "plotId")))))

            // /prison addcell <cellNumber> <min> <max> [securityLevel]
            .then(Commands.literal("addcell")
                .then(Commands.argument("cellNumber", IntegerArgumentType.integer(1))
                    .then(Commands.argument("min", BlockPosArgument.blockPos())
                        .then(Commands.argument("max", BlockPosArgument.blockPos())
                            .executes(ctx -> addCell(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "cellNumber"),
                                BlockPosArgument.getLoadedBlockPos(ctx, "min"),
                                BlockPosArgument.getLoadedBlockPos(ctx, "max"),
                                1))
                            .then(Commands.argument("securityLevel", IntegerArgumentType.integer(1, 5))
                                .executes(ctx -> addCell(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "cellNumber"),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "min"),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "max"),
                                    IntegerArgumentType.getInteger(ctx, "securityLevel"))))))))

            // /prison removecell <cellNumber>
            .then(Commands.literal("removecell")
                .then(Commands.argument("cellNumber", IntegerArgumentType.integer(1))
                    .executes(ctx -> removeCell(ctx.getSource(),
                        IntegerArgumentType.getInteger(ctx, "cellNumber")))))

            // /prison list
            .then(Commands.literal("list")
                .executes(ctx -> listPrisons(ctx.getSource())))

            // /prison cells
            .then(Commands.literal("cells")
                .executes(ctx -> listCells(ctx.getSource())))

            // /prison inmates
            .then(Commands.literal("inmates")
                .executes(ctx -> listInmates(ctx.getSource())))

            // /prison release <player>
            .then(Commands.literal("release")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> releasePlayer(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "player")))))

            // /prison status <player>
            .then(Commands.literal("status")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> showStatus(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "player")))))
        );

        // Spieler-Command: /bail
        dispatcher.register(Commands.literal("bail")
            .executes(ctx -> payBail(ctx.getSource())));

        // Spieler-Command: /jailtime
        dispatcher.register(Commands.literal("jailtime")
            .executes(ctx -> showJailTime(ctx.getSource())));
    }

    private static int createPrison(CommandSourceStack source, String plotId) {
        PlotRegion plot = PlotManager.getPlot(plotId);

        if (plot == null) {
            source.sendFailure(Component.translatable("command.prison.plot_not_found", plotId));
            return 0;
        }

        plot.setType(PlotType.PRISON);
        PlotManager.savePlots();

        PrisonManager.getInstance().registerPrison(plotId);

        source.sendSuccess(() -> Component.translatable("command.prison.created", plotId), true);
        return 1;
    }

    private static int addCell(CommandSourceStack source, int cellNumber,
                               BlockPos min, BlockPos max, int securityLevel) {

        PlotRegion prison = PrisonManager.getInstance().getDefaultPrison();

        if (prison == null) {
            source.sendFailure(Component.translatable("command.prison.no_prison_create"));
            return 0;
        }

        // Prüfe ob Zelle bereits existiert
        for (var area : prison.getSubAreas()) {
            if (area instanceof PrisonCell cell && cell.getCellNumber() == cellNumber) {
                source.sendFailure(Component.translatable("command.prison.cell_exists", cellNumber));
                return 0;
            }
        }

        PrisonCell cell = new PrisonCell(
            "cell_" + cellNumber,
            cellNumber,
            prison.getPlotId(),
            min,
            max,
            securityLevel
        );

        prison.addSubArea(cell);
        PlotManager.savePlots();

        source.sendSuccess(() -> Component.translatable("command.prison.cell_added",
            cellNumber, securityLevel, min.toShortString(), max.toShortString()), true);
        return 1;
    }

    private static int removeCell(CommandSourceStack source, int cellNumber) {
        PlotRegion prison = PrisonManager.getInstance().getDefaultPrison();

        if (prison == null) {
            source.sendFailure(Component.translatable("command.prison.no_prison"));
            return 0;
        }

        boolean removed = prison.removeSubArea("cell_" + cellNumber);

        if (removed) {
            PlotManager.savePlots();
            source.sendSuccess(() -> Component.translatable("command.prison.cell_removed", cellNumber), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("command.prison.cell_not_found", cellNumber));
            return 0;
        }
    }

    private static int listPrisons(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("command.prison.prisons_header"), false);

        int count = 0;
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.getType() == PlotType.PRISON) {
                int cellCount = (int) plot.getSubAreas().stream()
                    .filter(a -> a instanceof PrisonCell).count();

                source.sendSuccess(() -> Component.translatable(
                    "command.prison.prisons_list_entry", plot.getPlotId(), cellCount), false);
                count++;
            }
        }

        if (count == 0) {
            source.sendSuccess(() -> Component.translatable("command.prison.no_prisons"), false);
        }

        return count;
    }

    private static int listCells(CommandSourceStack source) {
        PlotRegion prison = PrisonManager.getInstance().getDefaultPrison();

        if (prison == null) {
            source.sendFailure(Component.translatable("command.prison.no_prison"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("command.prison.cells_header"), false);

        int count = 0;
        for (var area : prison.getSubAreas()) {
            if (area instanceof PrisonCell cell) {
                String status = cell.isOccupied() ?
                    Component.translatable("command.prison.cell_occupied").getString() :
                    Component.translatable("command.prison.cell_free").getString();
                source.sendSuccess(() -> Component.translatable("command.prison.cell_info",
                    cell.getCellNumber(), cell.getSecurityLevel(), status), false);
                count++;
            }
        }

        if (count == 0) {
            source.sendSuccess(() -> Component.translatable("command.prison.no_cells"), false);
        }

        return count;
    }

    private static int listInmates(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("command.prison.inmates_header"), false);

        var prisoners = PrisonManager.getInstance().getAllPrisoners();

        if (prisoners.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.prison.no_prisoners"), false);
            return 0;
        }

        for (var data : prisoners) {
            long remainingTicks = data.releaseTime - source.getLevel().getGameTime();
            int remainingSeconds = Math.max(0, (int)(remainingTicks / 20));

            source.sendSuccess(() -> Component.translatable("command.prison.inmate_info",
                data.playerName, data.cellNumber, remainingSeconds), false);
        }

        return prisoners.size();
    }

    private static int releasePlayer(CommandSourceStack source, ServerPlayer player) {
        PrisonManager manager = PrisonManager.getInstance();

        if (!manager.isPrisoner(player.getUUID())) {
            source.sendFailure(Component.translatable("command.prison.not_prisoner", player.getName().getString()));
            return 0;
        }

        manager.releasePlayer(player, PrisonManager.ReleaseReason.ADMIN_RELEASE);

        source.sendSuccess(() -> Component.translatable("command.prison.released", player.getName().getString()), true);
        return 1;
    }

    private static int showStatus(CommandSourceStack source, ServerPlayer player) {
        PrisonManager manager = PrisonManager.getInstance();
        PrisonManager.PrisonerData data = manager.getPrisonerData(player.getUUID());

        if (data == null) {
            source.sendSuccess(() -> Component.translatable(
                "command.prison.not_in_prison", player.getName().getString()), false);
            return 0;
        }

        long remainingTicks = data.releaseTime - source.getLevel().getGameTime();
        int remainingSeconds = Math.max(0, (int)(remainingTicks / 20));
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;

        source.sendSuccess(() -> Component.translatable("command.prison.status_header"), false);
        source.sendSuccess(() -> Component.translatable("message.prison.player_label", data.playerName), false);
        source.sendSuccess(() -> Component.translatable("command.prison.status_cell", data.cellNumber), false);
        source.sendSuccess(() -> Component.translatable("command.prison.status_remaining", minutes, seconds), false);
        source.sendSuccess(() -> Component.translatable("command.prison.status_bail", (int)data.bailAmount), false);
        source.sendSuccess(() -> Component.translatable("command.prison.status_wantedlevel", data.originalWantedLevel), false);

        return 1;
    }

    private static int payBail(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("command.prison.bail_players_only"));
            return 0;
        }

        boolean success = PrisonManager.getInstance().payBail(player);
        return success ? 1 : 0;
    }

    private static int showJailTime(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("command.prison.players_only"));
            return 0;
        }

        PrisonManager.PrisonerData data = PrisonManager.getInstance().getPrisonerData(player.getUUID());

        if (data == null) {
            source.sendSuccess(() -> Component.translatable("command.prison.not_in_jail"), false);
            return 0;
        }

        long remainingTicks = data.releaseTime - player.level().getGameTime();
        int remainingSeconds = Math.max(0, (int)(remainingTicks / 20));
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;

        source.sendSuccess(() -> Component.translatable(
            "command.prison.jail_time_remaining", minutes, seconds), false);
        source.sendSuccess(() -> Component.translatable(
            "command.prison.jail_time_bail", (int)data.bailAmount), false);

        return 1;
    }
}
