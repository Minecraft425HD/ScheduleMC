package de.rolandsw.schedulemc.utility.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.utility.PlotUtilityData;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;
import de.rolandsw.schedulemc.utility.UtilityCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Command für Utility-Informationen
 *
 * Verwendung:
 * /utility - Zeigt Verbrauch des aktuellen Plots
 * /utility <plotId> - Zeigt Verbrauch eines spezifischen Plots
 * /utility top - Zeigt Top-10 Verbraucher
 * /utility scan - Scannt aktuellen Plot nach Verbrauchern
 * /utility stats - Server-weite Statistiken
 */
public class UtilityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("utility")
                // /utility - Aktueller Plot
                .executes(ctx -> showCurrentPlot(ctx.getSource()))

                // /utility <plotId>
                .then(Commands.argument("plotId", StringArgumentType.word())
                        .executes(ctx -> showPlot(ctx.getSource(),
                                StringArgumentType.getString(ctx, "plotId"))))

                // /utility top
                .then(Commands.literal("top")
                        .executes(ctx -> showTopConsumers(ctx.getSource())))

                // /utility scan
                .then(Commands.literal("scan")
                        .requires(src -> src.hasPermission(2)) // OP only
                        .executes(ctx -> scanCurrentPlot(ctx.getSource())))

                // /utility stats
                .then(Commands.literal("stats")
                        .executes(ctx -> showStats(ctx.getSource())))

                // /utility breakdown <plotId>
                .then(Commands.literal("breakdown")
                        .then(Commands.argument("plotId", StringArgumentType.word())
                                .executes(ctx -> showBreakdown(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "plotId")))))
        );

        // Alias: /strom und /wasser
        dispatcher.register(Commands.literal("strom")
                .executes(ctx -> showCurrentPlot(ctx.getSource())));
        dispatcher.register(Commands.literal("wasser")
                .executes(ctx -> showCurrentPlot(ctx.getSource())));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMMANDS
    // ═══════════════════════════════════════════════════════════════════════════

    private static int showCurrentPlot(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("command.utility.players_only"));
            return 0;
        }

        Vec3 pos = player.position();
        PlotRegion plot = PlotManager.getPlotAt(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));

        if (plot == null) {
            source.sendFailure(Component.translatable("command.utility.not_on_plot"));
            return 0;
        }

        return showPlot(source, plot.getPlotId());
    }

    private static int showPlot(CommandSourceStack source, String plotId) {
        Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plotId);

        if (dataOpt.isEmpty()) {
            source.sendFailure(Component.translatable("command.utility.no_data", plotId));
            return 0;
        }

        PlotUtilityData data = dataOpt.get();
        data.calculateCurrentConsumption(); // Aktualisiere

        StringBuilder msg = new StringBuilder();
        msg.append("§6§l═══ UTILITY VERBRAUCH ═══\n");
        msg.append("§7Plot: §f").append(plotId).append("\n");
        msg.append("§7Verbraucher: §f").append(data.getConsumerCount()).append(" Blöcke\n\n");

        msg.append("§e⚡ STROM:\n");
        msg.append("  §7Aktuell: §f").append(PlotUtilityManager.formatElectricity(data.getCurrentElectricity())).append("/Tag\n");
        msg.append("  §77-Tage-Ø: §f").append(PlotUtilityManager.formatElectricity(data.get7DayAverageElectricity())).append("/Tag\n\n");

        msg.append("§b💧 WASSER:\n");
        msg.append("  §7Aktuell: §f").append(PlotUtilityManager.formatWater(data.getCurrentWater())).append("/Tag\n");
        msg.append("  §77-Tage-Ø: §f").append(PlotUtilityManager.formatWater(data.get7DayAverageWater())).append("/Tag\n");

        source.sendSuccess(() -> Component.literal(msg.toString()), false);
        return 1;
    }

    private static int showBreakdown(CommandSourceStack source, String plotId) {
        Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plotId);

        if (dataOpt.isEmpty()) {
            source.sendFailure(Component.translatable("command.utility.no_data", plotId));
            return 0;
        }

        PlotUtilityData data = dataOpt.get();
        data.calculateCurrentConsumption();

        StringBuilder msg = new StringBuilder();
        msg.append("§6§l═══ AUFSCHLÜSSELUNG ═══\n");
        msg.append("§7Plot: §f").append(plotId).append("\n\n");

        msg.append("§e⚡ STROM nach Kategorie:\n");
        Map<UtilityCategory, Double> elecByCategory = data.getCategoryElectricity();
        for (Map.Entry<UtilityCategory, Double> entry : elecByCategory.entrySet()) {
            if (entry.getValue() > 0) {
                msg.append("  §7").append(entry.getKey().getFormattedName())
                        .append(": §f").append(PlotUtilityManager.formatElectricity(entry.getValue())).append("\n");
            }
        }

        msg.append("\n§b💧 WASSER nach Kategorie:\n");
        Map<UtilityCategory, Double> waterByCategory = data.getCategoryWater();
        for (Map.Entry<UtilityCategory, Double> entry : waterByCategory.entrySet()) {
            if (entry.getValue() > 0) {
                msg.append("  §7").append(entry.getKey().getFormattedName())
                        .append(": §f").append(PlotUtilityManager.formatWater(entry.getValue())).append("\n");
            }
        }

        source.sendSuccess(() -> Component.literal(msg.toString()), false);
        return 1;
    }

    private static int showTopConsumers(CommandSourceStack source) {
        List<PlotUtilityData> topPlots = PlotUtilityManager.getTopConsumers(10);

        if (topPlots.isEmpty()) {
            source.sendFailure(Component.translatable("message.utility.no_consumption_data"));
            return 0;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("§6§l═══ TOP 10 VERBRAUCHER ═══\n\n");

        int rank = 1;
        for (PlotUtilityData data : topPlots) {
            String elec = PlotUtilityManager.formatElectricity(data.get7DayAverageElectricity());
            String water = PlotUtilityManager.formatWater(data.get7DayAverageWater());

            msg.append("§e").append(rank).append(". §f").append(data.getPlotId()).append("\n");
            msg.append("   §7⚡ ").append(elec).append(" §8| §7💧 ").append(water).append("\n");
            rank++;
        }

        source.sendSuccess(() -> Component.literal(msg.toString()), false);
        return 1;
    }

    private static int scanCurrentPlot(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("command.utility.players_only"));
            return 0;
        }

        Vec3 pos = player.position();
        PlotRegion plot = PlotManager.getPlotAt(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));

        if (plot == null) {
            source.sendFailure(Component.translatable("command.utility.not_on_plot"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("message.utility.scanning_plot", plot.getPlotId()), false);

        ServerLevel level = player.serverLevel();
        PlotUtilityManager.scanPlotForConsumers(level, plot);

        Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plot.getPlotId());
        int count = dataOpt.map(PlotUtilityData::getConsumerCount).orElse(0);

        source.sendSuccess(() -> Component.translatable("message.utility.scan_complete", count), false);
        return 1;
    }

    private static int showStats(CommandSourceStack source) {
        String summary = PlotUtilityManager.getStatsSummary();
        source.sendSuccess(() -> Component.translatable("message.command.info_yellow", summary), false);
        return 1;
    }
}
