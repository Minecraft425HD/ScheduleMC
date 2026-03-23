package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.commands.CommandExecutor;
import de.rolandsw.schedulemc.util.InputValidation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

/**
 * Money-Befehle für das Economy-System
 * Refactored mit CommandExecutor für einheitliches Error-Handling
 */
public class MoneyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // ───────────────────────────────
        // /money set <spieler> <betrag> (Admin)
        // ───────────────────────────────
        dispatcher.register(
                Commands.literal("money")
                        .then(Commands.literal("set")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                .executes(MoneyCommand::setBalance)
                                        )
                                )
                        )
        );

        // ───────────────────────────────
        // /money give <spieler> <betrag> (Admin)
        // ───────────────────────────────
        dispatcher.register(
                Commands.literal("money")
                        .then(Commands.literal("give")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                                .executes(MoneyCommand::giveBalance)
                                        )
                                )
                        )
        );

        // ───────────────────────────────
        // /money take <spieler> <betrag> (Admin)
        // ───────────────────────────────
        dispatcher.register(
                Commands.literal("money")
                        .then(Commands.literal("take")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                                .executes(MoneyCommand::takeBalance)
                                        )
                                )
                        )
        );

        // ───────────────────────────────
        // /money history <spieler> [limit] (Admin)
        // ───────────────────────────────
        dispatcher.register(
                Commands.literal("money")
                        .then(Commands.literal("history")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(ctx -> { showHistoryFor(ctx, EntityArgument.getPlayer(ctx, "target"), 10); return 1; })
                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 100))
                                                .executes(ctx -> { showHistoryFor(ctx, EntityArgument.getPlayer(ctx, "target"),
                                                        IntegerArgumentType.getInteger(ctx, "limit")); return 1; })
                                        )
                                )
                        )
        );
    }

    // ───────────────────────────────
    // Admin: Guthaben setzen
    // ───────────────────────────────
    private static int setBalance(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeAdminCommand(ctx, "command.money.set.error", 2,
            source -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION: Betrag validieren (erlaubt 0, aber nicht negativ)
                if (amount < 0) {
                    CommandExecutor.sendFailure(source, Component.translatable("command.money.amount_negative").getString());
                    return;
                }
                if (amount > InputValidation.MAX_AMOUNT) {
                    CommandExecutor.sendFailure(source,
                        Component.translatable("command.money.amount_too_high", InputValidation.MAX_AMOUNT).getString()
                    );
                    return;
                }

                String adminName = source.getTextName();
                EconomyManager.setBalance(target.getUUID(), amount, TransactionType.ADMIN_SET,
                    "Admin: " + adminName);

                CommandExecutor.sendSuccess(source,
                    Component.translatable("command.money.set.success",
                        target.getName().getString(), String.format("%.2f", amount)
                    ).getString()
                );

                target.sendSystemMessage(Component.translatable("command.money.set.notification",
                    String.format("%.2f", amount)
                ));
            });
    }

    // ───────────────────────────────
    // Admin: Geld geben
    // ───────────────────────────────
    private static int giveBalance(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeAdminCommand(ctx, "command.money.give.error", 2,
            source -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION: Betrag validieren
                InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                if (validation.isFailure()) {
                    CommandExecutor.sendFailure(source,
                        Component.translatable("command.money.invalid_amount", validation.getErrorMessage()).getString()
                    );
                    return;
                }

                String adminName = source.getTextName();
                EconomyManager.deposit(target.getUUID(), amount, TransactionType.ADMIN_GIVE,
                    "Admin: " + adminName);

                CommandExecutor.sendSuccess(source,
                    Component.translatable("command.money.give.success",
                        target.getName().getString(), String.format("%.2f", amount),
                        String.format("%.2f", EconomyManager.getBalance(target.getUUID()))
                    ).getString()
                );

                target.sendSystemMessage(Component.translatable("command.money.give.notification",
                    String.format("%.2f", amount)
                ));
            });
    }

    // ───────────────────────────────
    // Admin: Geld nehmen
    // ───────────────────────────────
    private static int takeBalance(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeAdminCommand(ctx, "command.money.take.error", 2,
            source -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION: Betrag validieren
                InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                if (validation.isFailure()) {
                    CommandExecutor.sendFailure(source,
                        Component.translatable("command.money.invalid_amount", validation.getErrorMessage()).getString()
                    );
                    return;
                }

                String adminName = source.getTextName();
                boolean success = EconomyManager.withdraw(target.getUUID(), amount, TransactionType.ADMIN_TAKE,
                    "Admin: " + adminName);

                if (success) {
                    CommandExecutor.sendSuccess(source,
                        Component.translatable("command.money.take.success",
                            target.getName().getString(), String.format("%.2f", amount),
                            String.format("%.2f", EconomyManager.getBalance(target.getUUID()))
                        ).getString()
                    );

                    target.sendSystemMessage(Component.translatable("command.money.take.notification",
                        String.format("%.2f", amount)
                    ));
                } else {
                    CommandExecutor.sendFailure(source, Component.translatable("command.money.take.insufficient").getString());
                }
            });
    }

    // ───────────────────────────────
    // Admin: Zeigt Transaktionshistorie für Spieler
    // ───────────────────────────────
    private static void showHistoryFor(CommandContext<CommandSourceStack> ctx, ServerPlayer target, int limit) {
        TransactionHistory history = TransactionHistory.getInstance();
        if (history == null) {
            CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.money.history_unavailable").getString());
            return;
        }

        List<Transaction> transactions = history.getRecentTransactions(target.getUUID(), limit);

        if (transactions.isEmpty()) {
            CommandExecutor.sendInfo(ctx.getSource(),
                Component.translatable("command.money.no_transactions", target.getName().getString()).getString());
            return;
        }

        ctx.getSource().sendSuccess(() -> Component.translatable("command.money.history_header", target.getName().getString(), transactions.size()), false);
        ctx.getSource().sendSuccess(() -> Component.literal(""), false);

        for (Transaction transaction : transactions) {
            ctx.getSource().sendSuccess(() -> Component.literal(transaction.getFormattedDescription()), false);
            ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        }

        // Statistiken
        double totalIncome = history.getTotalIncome(target.getUUID());
        double totalExpenses = history.getTotalExpenses(target.getUUID());
        int totalCount = history.getTransactionCount(target.getUUID());

        ctx.getSource().sendSuccess(() -> Component.translatable("command.money.stats_header"), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.money.stats_income", String.format("%.2f", totalIncome)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.money.stats_expenses", String.format("%.2f", totalExpenses)), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.money.stats_count", totalCount), false);
    }
}
