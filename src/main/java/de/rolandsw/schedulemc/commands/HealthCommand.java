package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.HealthCheckManager;
import de.rolandsw.schedulemc.util.HealthCheckManager.ComponentHealth;
import de.rolandsw.schedulemc.util.HealthCheckManager.SystemHealth;
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
 * - /health <system> - Zeigt Details für ein spezifisches System
 * - /health backups - Backup-Übersicht
 * - /health log - Loggt Health-Check in Server-Console
 *
 * Verfügbare Systeme:
 *   Kern: economy, plot, wallet
 *   Finanz: loan, creditloan, creditscore, savings, tax, overdraft, recurring, shopaccount
 *   NPC/Crime: crime, bounty, npc
 *   Spieler: gang, territory, achievement, daily, message
 *   Welt: lock, market, warehouse, towing
 */
public class HealthCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("health")
            .requires(source -> source.hasPermission(2)) // OP-Level 2
            .executes(HealthCommand::showOverallHealth)
            // Kern-Systeme
            .then(Commands.literal("economy")
                .executes(HealthCommand::showEconomyHealth))
            .then(Commands.literal("plot")
                .executes(HealthCommand::showPlotHealth))
            .then(Commands.literal("wallet")
                .executes(ctx -> showSystemDetail(ctx, "wallet")))
            // Finanz-Systeme
            .then(Commands.literal("loan")
                .executes(ctx -> showSystemDetail(ctx, "loan")))
            .then(Commands.literal("creditloan")
                .executes(ctx -> showSystemDetail(ctx, "creditloan")))
            .then(Commands.literal("creditscore")
                .executes(ctx -> showSystemDetail(ctx, "creditscore")))
            .then(Commands.literal("savings")
                .executes(ctx -> showSystemDetail(ctx, "savings")))
            .then(Commands.literal("tax")
                .executes(ctx -> showSystemDetail(ctx, "tax")))
            .then(Commands.literal("overdraft")
                .executes(ctx -> showSystemDetail(ctx, "overdraft")))
            .then(Commands.literal("recurring")
                .executes(ctx -> showSystemDetail(ctx, "recurring")))
            .then(Commands.literal("shopaccount")
                .executes(ctx -> showSystemDetail(ctx, "shopaccount")))
            // NPC & Crime
            .then(Commands.literal("crime")
                .executes(ctx -> showSystemDetail(ctx, "crime")))
            .then(Commands.literal("bounty")
                .executes(ctx -> showSystemDetail(ctx, "bounty")))
            .then(Commands.literal("npc")
                .executes(ctx -> showSystemDetail(ctx, "npc")))
            // Spieler-Systeme
            .then(Commands.literal("gang")
                .executes(ctx -> showSystemDetail(ctx, "gang")))
            .then(Commands.literal("territory")
                .executes(ctx -> showSystemDetail(ctx, "territory")))
            .then(Commands.literal("achievement")
                .executes(ctx -> showSystemDetail(ctx, "achievement")))
            .then(Commands.literal("daily")
                .executes(ctx -> showSystemDetail(ctx, "daily")))
            .then(Commands.literal("message")
                .executes(ctx -> showSystemDetail(ctx, "message")))
            // Welt-Systeme
            .then(Commands.literal("lock")
                .executes(ctx -> showSystemDetail(ctx, "lock")))
            .then(Commands.literal("market")
                .executes(ctx -> showSystemDetail(ctx, "market")))
            .then(Commands.literal("warehouse")
                .executes(ctx -> showSystemDetail(ctx, "warehouse")))
            .then(Commands.literal("towing")
                .executes(ctx -> showSystemDetail(ctx, "towing")))
            // Utility-Commands
            .then(Commands.literal("backups")
                .executes(HealthCommand::showBackupInfo))
            .then(Commands.literal("log")
                .executes(HealthCommand::logHealthCheck))
        );
    }

    // ==========================================
    // Overall-Health
    // ==========================================

    /**
     * Zeigt Overall-Health-Status aller 23 Systeme
     */
    private static int showOverallHealth(CommandContext<CommandSourceStack> context) {
        String report = HealthCheckManager.getHealthReport();

        // Split report in Zeilen und sende jede einzeln
        for (String line : report.split("\n")) {
            context.getSource().sendSuccess(() -> Component.literal(line), false);
        }

        return 1;
    }

    // ==========================================
    // Generischer System-Detail-Handler
    // ==========================================

    /**
     * Zeigt Detail-Informationen für ein einzelnes System
     */
    private static int showSystemDetail(CommandContext<CommandSourceStack> context, String systemKey) {
        ComponentHealth health = HealthCheckManager.checkSystem(systemKey);

        if (health == null) {
            context.getSource().sendFailure(
                Component.literal("§cUnbekanntes System: " + systemKey));
            return 0;
        }

        String statusColor = switch (health.getStatus()) {
            case HEALTHY -> "§a";
            case DEGRADED -> "§e";
            case UNHEALTHY -> "§c";
        };

        String statusIcon = switch (health.getStatus()) {
            case HEALTHY -> "✔";
            case DEGRADED -> "⚠";
            case UNHEALTHY -> "✘";
        };

        // Header
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§6§l  " + health.getComponentName().toUpperCase() + " HEALTH"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);

        // Status
        context.getSource().sendSuccess(() ->
            Component.literal("  §7Status: " + statusColor + statusIcon + " " + health.getStatus().getDisplayName()), false);

        // Detail-Info
        context.getSource().sendSuccess(() ->
            Component.literal("  §7Info: §f" + health.getMessage()), false);

        // Zeitstempel
        long checkAge = (System.currentTimeMillis() - health.getLastCheckTime()) / 1000;
        context.getSource().sendSuccess(() ->
            Component.literal("  §7Geprüft vor: §f" + checkAge + "s"), false);

        // Footer
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);

        return 1;
    }

    // ==========================================
    // Spezielle System-Handler (mit Extra-Info)
    // ==========================================

    /**
     * Economy-System mit Backup-Details
     */
    private static int showEconomyHealth(CommandContext<CommandSourceStack> context) {
        // Generischer Health-Check zuerst
        showSystemDetail(context, "economy");

        // Extra: Backup-Info
        File economyFile = new File("config/plotmod_economy.json");
        int backupCount = BackupManager.getBackupCount(economyFile);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7Backups: §f" + backupCount + " verfügbar"), false);

        if (backupCount > 0) {
            File latestBackup = BackupManager.getLatestBackup(economyFile);
            if (latestBackup != null) {
                long age = (System.currentTimeMillis() - latestBackup.lastModified()) / 1000 / 60;
                context.getSource().sendSuccess(() ->
                    Component.literal("  §7Letztes Backup: §f" + latestBackup.getName() + " §7(vor " + age + " Min)"), false);
            }
        }

        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);

        return 1;
    }

    /**
     * Plot-System mit Cache-Statistiken und Backup-Details
     */
    private static int showPlotHealth(CommandContext<CommandSourceStack> context) {
        // Generischer Health-Check zuerst
        showSystemDetail(context, "plot");

        // Extra: Cache-Statistiken
        var cacheStats = PlotManager.getCacheStatistics();
        context.getSource().sendSuccess(() ->
            Component.literal("  §6Cache-Statistiken:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §f" + cacheStats.toDisplayString()), false);

        // Extra: Backup-Info
        File plotFile = new File("config/plotmod_plots.json");
        int backupCount = BackupManager.getBackupCount(plotFile);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7Backups: §f" + backupCount + " verfügbar"), false);

        if (backupCount > 0) {
            File latestBackup = BackupManager.getLatestBackup(plotFile);
            if (latestBackup != null) {
                long age = (System.currentTimeMillis() - latestBackup.lastModified()) / 1000 / 60;
                context.getSource().sendSuccess(() ->
                    Component.literal("  §7Letztes Backup: §f" + latestBackup.getName() + " §7(vor " + age + " Min)"), false);
            }
        }

        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);

        return 1;
    }

    // ==========================================
    // Backup-Übersicht
    // ==========================================

    /**
     * Zeigt Backup-Übersicht aller persistenten Systeme
     */
    private static int showBackupInfo(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§6§l     BACKUP ÜBERSICHT"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);

        // Economy Backups
        File economyFile = new File("config/plotmod_economy.json");
        showFileBackups(context, "Economy", economyFile);

        // Plot Backups
        File plotFile = new File("config/plotmod_plots.json");
        showFileBackups(context, "Plots", plotFile);

        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7Auto-Backup: §fAlle 5 Minuten"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7Max Backups: §f10 pro Datei"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e§l══════════════════════════════════"), false);

        return 1;
    }

    /**
     * Helper-Methode für Backup-Anzeige einer einzelnen Datei
     */
    private static void showFileBackups(CommandContext<CommandSourceStack> context, String name, File file) {
        File[] backups = BackupManager.listBackups(file);

        context.getSource().sendSuccess(() ->
            Component.literal("  §6" + name + ": §f" + backups.length + " Backups"), false);

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
                    Component.literal("    §7" + index + ". §f" + backup.getName() +
                        " §7(vor " + finalAgeStr + ", " + finalSize + " KB)"), false);
            }

            if (backups.length > 3) {
                final int remaining = backups.length - 3;
                context.getSource().sendSuccess(() ->
                    Component.literal("    §7... und " + remaining + " weitere"), false);
            }
        }
    }

    // ==========================================
    // Log-Command
    // ==========================================

    /**
     * Loggt Health-Check in Server-Console
     */
    private static int logHealthCheck(CommandContext<CommandSourceStack> context) {
        HealthCheckManager.logHealthCheck();

        context.getSource().sendSuccess(() ->
            Component.literal("§aHealth-Check wurde in die Server-Console geloggt."), false);

        context.getSource().sendSuccess(() ->
            Component.literal(HealthCheckManager.getQuickStatus()), false);

        return 1;
    }
}
