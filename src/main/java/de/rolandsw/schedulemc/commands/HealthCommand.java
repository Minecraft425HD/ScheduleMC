package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.HealthCheckManager;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.region.PlotManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.File;

/**
 * Admin-Command für System-Health-Checks
 *
 * Commands:
 * - /health - Zeigt Health-Status aller Systeme
 * - /health economy - Economy-System Details
 * - /health plot - Plot-System Details
 * - /health backups - Backup-Übersicht
 */
public class HealthCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("health")
            .requires(source -> source.hasPermission(2)) // OP-Level 2
            .executes(HealthCommand::showOverallHealth)
            .then(Commands.literal("economy")
                .executes(HealthCommand::showEconomyHealth))
            .then(Commands.literal("plot")
                .executes(HealthCommand::showPlotHealth))
            .then(Commands.literal("backups")
                .executes(HealthCommand::showBackupInfo))
            .then(Commands.literal("log")
                .executes(HealthCommand::logHealthCheck))
        );
    }

    /**
     * Zeigt Overall-Health-Status
     */
    private static int showOverallHealth(CommandContext<CommandSourceStack> context) {
        String report = HealthCheckManager.getHealthReport();

        // Split report in Zeilen und sende jede einzeln
        for (String line : report.split("\n")) {
            context.getSource().sendSuccess(() -> Component.literal(line), false);
        }

        return 1;
    }

    /**
     * Zeigt Economy-System Details
     */
    private static int showEconomyHealth(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l═══ ECONOMY SYSTEM ═══§r"), false);

        context.getSource().sendSuccess(() ->
            Component.literal(EconomyManager.getHealthInfo()), false);

        if (!EconomyManager.isHealthy()) {
            context.getSource().sendSuccess(() ->
                Component.literal("§c⚠ Letzte Fehler: " + EconomyManager.getLastError()), false);
        }

        // Backup-Info
        File economyFile = new File("config/plotmod_economy.json");
        int backupCount = BackupManager.getBackupCount(economyFile);
        context.getSource().sendSuccess(() ->
            Component.literal("§7Backups verfügbar: §f" + backupCount), false);

        if (backupCount > 0) {
            File latestBackup = BackupManager.getLatestBackup(economyFile);
            if (latestBackup != null) {
                long age = (System.currentTimeMillis() - latestBackup.lastModified()) / 1000 / 60;
                context.getSource().sendSuccess(() ->
                    Component.literal("§7Letztes Backup: §f" + latestBackup.getName() +
                        " §7(vor " + age + " Minuten)"), false);
            }
        }

        context.getSource().sendSuccess(() ->
            Component.literal("§e§l═══════════════════════§r"), false);

        return 1;
    }

    /**
     * Zeigt Plot-System Details
     */
    private static int showPlotHealth(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l═══ PLOT SYSTEM ═══§r"), false);

        context.getSource().sendSuccess(() ->
            Component.literal(PlotManager.getHealthInfo()), false);

        if (!PlotManager.isHealthy()) {
            context.getSource().sendSuccess(() ->
                Component.literal("§c⚠ Letzte Fehler: " + PlotManager.getLastError()), false);
        }

        // Cache-Statistiken
        var cacheStats = PlotManager.getCacheStatistics();
        context.getSource().sendSuccess(() ->
            Component.literal(cacheStats.toDisplayString()), false);

        // Backup-Info
        File plotFile = new File("config/plotmod_plots.json");
        int backupCount = BackupManager.getBackupCount(plotFile);
        context.getSource().sendSuccess(() ->
            Component.literal("§7Backups verfügbar: §f" + backupCount), false);

        if (backupCount > 0) {
            File latestBackup = BackupManager.getLatestBackup(plotFile);
            if (latestBackup != null) {
                long age = (System.currentTimeMillis() - latestBackup.lastModified()) / 1000 / 60;
                context.getSource().sendSuccess(() ->
                    Component.literal("§7Letztes Backup: §f" + latestBackup.getName() +
                        " §7(vor " + age + " Minuten)"), false);
            }
        }

        context.getSource().sendSuccess(() ->
            Component.literal("§e§l═══════════════════════§r"), false);

        return 1;
    }

    /**
     * Zeigt Backup-Übersicht
     */
    private static int showBackupInfo(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l═══ BACKUP ÜBERSICHT ═══§r"), false);

        // Economy Backups
        File economyFile = new File("config/plotmod_economy.json");
        showFileBackups(context, "Economy", economyFile);

        // Plot Backups
        File plotFile = new File("config/plotmod_plots.json");
        showFileBackups(context, "Plots", plotFile);

        context.getSource().sendSuccess(() ->
            Component.literal("§e§l═══════════════════════════§r"), false);

        context.getSource().sendSuccess(() ->
            Component.literal("§7Backups werden automatisch bei jedem Speichervorgang erstellt."), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§7Maximal 5 Backups werden pro Datei behalten."), false);

        return 1;
    }

    /**
     * Helper-Methode für Backup-Anzeige
     */
    private static void showFileBackups(CommandContext<CommandSourceStack> context, String name, File file) {
        File[] backups = BackupManager.listBackups(file);

        context.getSource().sendSuccess(() ->
            Component.literal("§6" + name + ": §f" + backups.length + " Backups"), false);

        if (backups.length > 0) {
            for (int i = 0; i < Math.min(3, backups.length); i++) {
                File backup = backups[i];
                long age = (System.currentTimeMillis() - backup.lastModified()) / 1000 / 60;
                long size = backup.length() / 1024; // KB

                String ageStr = age < 60 ? age + " Min" : (age / 60) + " Std";

                final int index = i + 1;
                final String finalAgeStr = ageStr;
                final long finalSize = size;

                context.getSource().sendSuccess(() ->
                    Component.literal(String.format("  §7%d. %s §8(vor %s, %d KB)",
                        index, backup.getName(), finalAgeStr, finalSize)), false);
            }

            if (backups.length > 3) {
                final int remaining = backups.length - 3;
                context.getSource().sendSuccess(() ->
                    Component.literal("  §8... und " + remaining + " weitere"), false);
            }
        }
    }

    /**
     * Loggt Health-Check in Server-Console
     */
    private static int logHealthCheck(CommandContext<CommandSourceStack> context) {
        HealthCheckManager.logHealthCheck();

        context.getSource().sendSuccess(() ->
            Component.literal("§aHealth-Check in Server-Console geloggt!"), false);

        context.getSource().sendSuccess(() ->
            Component.literal(HealthCheckManager.getQuickStatus()), false);

        return 1;
    }
}
