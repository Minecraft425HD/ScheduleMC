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
        return CommandExecutor.executeAdminCommand(ctx, "Fehler beim Setzen des Guthabens", 2,
            source -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION: Betrag validieren (erlaubt 0, aber nicht negativ)
                if (amount < 0) {
                    CommandExecutor.sendFailure(source, "§c❌ Betrag darf nicht negativ sein!");
                    return;
                }
                if (amount > InputValidation.MAX_AMOUNT) {
                    CommandExecutor.sendFailure(source,
                        "§c❌ Betrag zu hoch! Maximum: " + InputValidation.MAX_AMOUNT
                    );
                    return;
                }

                String adminName = source.getTextName();
                EconomyManager.setBalance(target.getUUID(), amount, TransactionType.ADMIN_SET,
                    "Admin: " + adminName);

                CommandExecutor.sendSuccess(source,
                    "Guthaben gesetzt!\n" +
                    "Spieler: " + target.getName().getString() + "\n" +
                    "Neues Guthaben: " + String.format("%.2f", amount) + " €"
                );

                target.sendSystemMessage(Component.literal(
                    "§eDein Guthaben wurde auf §6" + String.format("%.2f", amount) + " € §egesetzt."
                ));
            });
    }

    // ───────────────────────────────
    // Admin: Geld geben
    // ───────────────────────────────
    private static int giveBalance(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeAdminCommand(ctx, "Fehler beim Hinzufügen von Geld", 2,
            source -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION: Betrag validieren
                InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                if (validation.isFailure()) {
                    CommandExecutor.sendFailure(source,
                        "§c❌ Ungültiger Betrag: §f" + validation.getErrorMessage()
                    );
                    return;
                }

                String adminName = source.getTextName();
                EconomyManager.deposit(target.getUUID(), amount, TransactionType.ADMIN_GIVE,
                    "Admin: " + adminName);

                CommandExecutor.sendSuccess(source,
                    "Geld hinzugefügt!\n" +
                    "Spieler: " + target.getName().getString() + "\n" +
                    "Betrag: +" + String.format("%.2f", amount) + " €\n" +
                    "Neues Guthaben: " + String.format("%.2f", EconomyManager.getBalance(target.getUUID())) + " €"
                );

                target.sendSystemMessage(Component.literal(
                    "§a✓ Du hast §e" + String.format("%.2f", amount) + " € §aerhalten!"
                ));
            });
    }

    // ───────────────────────────────
    // Admin: Geld nehmen
    // ───────────────────────────────
    private static int takeBalance(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeAdminCommand(ctx, "Fehler beim Abziehen von Geld", 2,
            source -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION: Betrag validieren
                InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                if (validation.isFailure()) {
                    CommandExecutor.sendFailure(source,
                        "§c❌ Ungültiger Betrag: §f" + validation.getErrorMessage()
                    );
                    return;
                }

                String adminName = source.getTextName();
                boolean success = EconomyManager.withdraw(target.getUUID(), amount, TransactionType.ADMIN_TAKE,
                    "Admin: " + adminName);

                if (success) {
                    CommandExecutor.sendSuccess(source,
                        "Geld abgezogen!\n" +
                        "Spieler: " + target.getName().getString() + "\n" +
                        "Betrag: -" + String.format("%.2f", amount) + " €\n" +
                        "Neues Guthaben: " + String.format("%.2f", EconomyManager.getBalance(target.getUUID())) + " €"
                    );

                    target.sendSystemMessage(Component.literal(
                        "§c" + String.format("%.2f", amount) + " € §cwurden von deinem Konto abgezogen."
                    ));
                } else {
                    CommandExecutor.sendFailure(source, "Nicht genug Guthaben beim Zielspieler!");
                }
            });
    }

    // ───────────────────────────────
    // Admin: Zeigt Transaktionshistorie für Spieler
    // ───────────────────────────────
    private static void showHistoryFor(CommandContext<CommandSourceStack> ctx, ServerPlayer target, int limit) {
        TransactionHistory history = TransactionHistory.getInstance();
        if (history == null) {
            CommandExecutor.sendFailure(ctx.getSource(), "Transaktionshistorie nicht verfügbar!");
            return;
        }

        List<Transaction> transactions = history.getRecentTransactions(target.getUUID(), limit);

        if (transactions.isEmpty()) {
            CommandExecutor.sendInfo(ctx.getSource(),
                target.getName().getString() + " hat noch keine Transaktionen.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§a§l━━━━━━━━━ TRANSAKTIONS-HISTORIE ━━━━━━━━━\n");
        sb.append("§eSpieler: §f").append(target.getName().getString()).append("\n");
        sb.append("§7Letzte ").append(transactions.size()).append(" Transaktionen:\n\n");

        for (Transaction transaction : transactions) {
            sb.append(transaction.getFormattedDescription()).append("\n\n");
        }

        // Statistiken
        double totalIncome = history.getTotalIncome(target.getUUID());
        double totalExpenses = history.getTotalExpenses(target.getUUID());
        int totalCount = history.getTransactionCount(target.getUUID());

        sb.append("§a§l━━━━━━━━━ STATISTIKEN ━━━━━━━━━\n");
        sb.append("§7Gesamt-Einnahmen: §a+").append(String.format("%.2f€", totalIncome)).append("\n");
        sb.append("§7Gesamt-Ausgaben: §c-").append(String.format("%.2f€", totalExpenses)).append("\n");
        sb.append("§7Gesamt-Transaktionen: §e").append(totalCount).append("\n");
        sb.append("§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
    }
}
