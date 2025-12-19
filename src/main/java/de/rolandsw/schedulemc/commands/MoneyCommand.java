package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.FeeManager;
import de.rolandsw.schedulemc.economy.RateLimiter;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.commands.CommandExecutor;
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
        // /money - Zeigt eigenes Guthaben
        // ───────────────────────────────
        dispatcher.register(
                Commands.literal("money")
                        .executes(MoneyCommand::showBalance)
        );

        // ───────────────────────────────
        // /pay <spieler> <betrag>
        // ───────────────────────────────
        dispatcher.register(
                Commands.literal("pay")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(MoneyCommand::payPlayer)
                                )
                        )
        );

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
        // /money history [limit] - Zeigt Transaktionshistorie
        // ───────────────────────────────
        dispatcher.register(
                Commands.literal("money")
                        .then(Commands.literal("history")
                                .executes(ctx -> showHistory(ctx, 10))
                                .then(Commands.argument("limit", IntegerArgumentType.integer(1, 100))
                                        .executes(ctx -> showHistory(ctx, IntegerArgumentType.getInteger(ctx, "limit")))
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
    // Zeigt das eigene Guthaben
    // ───────────────────────────────
    private static int showBalance(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen des Kontostands",
            player -> {
                double balance = EconomyManager.getBalance(player.getUUID());
                player.sendSystemMessage(Component.literal(
                    "§a§l━━━━━━━━━━━━━━━━━━\n" +
                    "§aDein Kontostand:\n" +
                    "§e" + String.format("%.2f", balance) + " €\n" +
                    "§a§l━━━━━━━━━━━━━━━━━━"
                ));
            });
    }

    // ───────────────────────────────
    // Geld an anderen Spieler senden
    // ───────────────────────────────
    private static int payPlayer(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Pay-Befehl",
            sender -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                UUID senderUUID = sender.getUUID();
                UUID targetUUID = target.getUUID();

                // Rate-Limiting prüfen
                if (!RateLimiter.canPerformTransaction(senderUUID)) {
                    int seconds = RateLimiter.getSecondsUntilNextTransaction(senderUUID);
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "✗ Zu viele Transaktionen!\n" +
                        "Bitte warte " + seconds + " Sekunden bevor du erneut sendest."
                    );
                    return;
                }

                // Prüfen ob Sender = Empfänger
                if (senderUUID.equals(targetUUID)) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du kannst dir nicht selbst Geld senden!");
                    return;
                }

                // Betrag validieren
                if (amount <= 0) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Der Betrag muss größer als 0 sein!");
                    return;
                }

                // Berechne Gebühr
                double transferFee = FeeManager.getTransferFee(amount);
                double totalCost = amount + transferFee;

                // Guthaben prüfen (inkl. Gebühr)
                double senderBalance = EconomyManager.getBalance(senderUUID);
                if (senderBalance < totalCost) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Nicht genug Geld!\n" +
                        "Dein Guthaben: " + String.format("%.2f", senderBalance) + " €\n" +
                        "Benötigt: " + String.format("%.2f", amount) + " € + " +
                        String.format("%.2f", transferFee) + " € Gebühr"
                    );
                    return;
                }

                // Transaktion durchführen
                EconomyManager.withdraw(senderUUID, amount, TransactionType.TRANSFER,
                    "Transfer an " + target.getName().getString());
                EconomyManager.deposit(targetUUID, amount, TransactionType.TRANSFER,
                    "Transfer von " + sender.getName().getString());

                // Gebühr abziehen
                FeeManager.chargeTransferFee(senderUUID, amount, sender.getServer());

                // Transaktion aufzeichnen
                RateLimiter.recordTransaction(senderUUID);

                // Benachrichtigungen
                sender.sendSystemMessage(Component.literal(
                    "§a✓ Zahlung erfolgreich!\n" +
                    "§7An: §f" + target.getName().getString() + "\n" +
                    "§7Betrag: §e" + String.format("%.2f", amount) + " €\n" +
                    "§7Transfer-Gebühr: §c-" + String.format("%.2f", transferFee) + " €\n" +
                    "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(senderUUID)) + " €"
                ));

                target.sendSystemMessage(Component.literal(
                    "§a✓ Geld erhalten!\n" +
                    "§7Von: §f" + sender.getName().getString() + "\n" +
                    "§7Betrag: §e" + String.format("%.2f", amount) + " €\n" +
                    "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(targetUUID)) + " €"
                ));
            });
    }

    // ───────────────────────────────
    // Admin: Guthaben setzen
    // ───────────────────────────────
    private static int setBalance(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeAdminCommand(ctx, "Fehler beim Setzen des Guthabens", 2,
            source -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

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
    // Zeigt Transaktionshistorie
    // ───────────────────────────────
    private static int showHistory(CommandContext<CommandSourceStack> ctx, int limit) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Historie",
            player -> showHistoryFor(ctx, player, limit));
    }

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
