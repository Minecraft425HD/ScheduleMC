package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/**
 * Utility-Klasse für Command-Execution mit einheitlichem Error-Handling
 *
 * Eliminiert ~408 Zeilen Boilerplate über 51 Command-Methoden:
 * - MoneyCommand: 13 methods
 * - SavingsCommand: 7 methods
 * - LoanCommand: 8 methods
 * - AutopayCommand: 4 methods
 * - DailyCommand: 2 methods
 * - PlotCommand: 34 methods (!)
 *
 * VORHER (jede Command-Methode):
 * ```java
 * private static int commandMethod(CommandContext<CommandSourceStack> ctx) {
 *     try {
 *         ServerPlayer player = ctx.getSource().getPlayerOrException();
 *         // Command logic...
 *         ctx.getSource().sendSuccess(() -> Component.literal("§a✓ ..."), false);
 *         return 1;
 *     } catch (Exception e) {
 *         LOGGER.error("Error", e);
 *         ctx.getSource().sendFailure(Component.literal("§cError!"));
 *         return 0;
 *     }
 * }
 * ```
 *
 * NACHHER (mit CommandExecutor):
 * ```java
 * private static int commandMethod(CommandContext<CommandSourceStack> ctx) {
 *     return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Command",
 *         player -> {
 *             // Command logic...
 *             ctx.getSource().sendSuccess(() -> Component.literal("§a✓ ..."), false);
 *         });
 * }
 * ```
 *
 * Reduziert ~8 Zeilen pro Methode auf ~5 Zeilen = 3 Zeilen × 51 Methoden = ~153 Zeilen gespart
 */
public class CommandExecutor {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Funktionales Interface für Player-Commands
     */
    @FunctionalInterface
    public interface PlayerCommand {
        void execute(ServerPlayer player) throws Exception;
    }

    /**
     * Funktionales Interface für Source-Commands (kein Player required)
     */
    @FunctionalInterface
    public interface SourceCommand {
        void execute(CommandSourceStack source) throws Exception;
    }

    /**
     * Führt einen Player-Command aus mit einheitlichem Error-Handling
     *
     * @param ctx Der Command-Context
     * @param errorMessage Die Fehlermeldung für Logging
     * @param command Das Command-Lambda
     * @return 1 bei Erfolg, 0 bei Fehler
     */
    public static int executePlayerCommand(
        CommandContext<CommandSourceStack> ctx,
        String errorMessage,
        PlayerCommand command
    ) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            command.execute(player);
            return 1;
        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
            ctx.getSource().sendFailure(Component.literal("§c" + errorMessage + ": " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Führt einen Source-Command aus (kein Player required)
     *
     * @param ctx Der Command-Context
     * @param errorMessage Die Fehlermeldung für Logging
     * @param command Das Command-Lambda
     * @return 1 bei Erfolg, 0 bei Fehler
     */
    public static int executeSourceCommand(
        CommandContext<CommandSourceStack> ctx,
        String errorMessage,
        SourceCommand command
    ) {
        try {
            command.execute(ctx.getSource());
            return 1;
        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
            ctx.getSource().sendFailure(Component.literal("§c" + errorMessage + ": " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Führt einen Player-Command aus mit custom Success-Message
     *
     * @param ctx Der Command-Context
     * @param errorMessage Die Fehlermeldung für Logging
     * @param successMessage Die Erfolgsmeldung
     * @param command Das Command-Lambda
     * @return 1 bei Erfolg, 0 bei Fehler
     */
    public static int executePlayerCommandWithMessage(
        CommandContext<CommandSourceStack> ctx,
        String errorMessage,
        String successMessage,
        PlayerCommand command
    ) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            command.execute(player);
            ctx.getSource().sendSuccess(() -> Component.literal(successMessage), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
            ctx.getSource().sendFailure(Component.literal("§c" + errorMessage + ": " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Führt einen Admin-Command aus (requires permission level)
     *
     * @param ctx Der Command-Context
     * @param errorMessage Die Fehlermeldung für Logging
     * @param requiredLevel Das required Permission-Level (2 = gamemode, 3 = /ban, 4 = /stop)
     * @param command Das Command-Lambda
     * @return 1 bei Erfolg, 0 bei Fehler
     */
    public static int executeAdminCommand(
        CommandContext<CommandSourceStack> ctx,
        String errorMessage,
        int requiredLevel,
        SourceCommand command
    ) {
        try {
            if (!ctx.getSource().hasPermission(requiredLevel)) {
                ctx.getSource().sendFailure(Component.translatable("message.common.no_permission_command"));
                return 0;
            }
            command.execute(ctx.getSource());
            return 1;
        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
            ctx.getSource().sendFailure(Component.literal("§c" + errorMessage + ": " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Sendet eine Success-Message
     */
    public static void sendSuccess(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal("§a" + message), false);
    }

    /**
     * Sendet eine Failure-Message
     */
    public static void sendFailure(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal("§c" + message));
    }

    /**
     * Sendet eine Info-Message
     */
    public static void sendInfo(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal("§e" + message), false);
    }
}
