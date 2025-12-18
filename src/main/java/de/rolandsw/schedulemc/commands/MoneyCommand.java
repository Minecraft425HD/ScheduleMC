package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.FeeManager;
import de.rolandsw.schedulemc.economy.RateLimiter;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.economy.TransactionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Money-Befehle für das Economy-System
 */
public class MoneyCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

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
                                        .executes(ctx -> showHistoryFor(ctx, EntityArgument.getPlayer(ctx, "target"), 10))
                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 100))
                                                .executes(ctx -> showHistoryFor(ctx, EntityArgument.getPlayer(ctx, "target"),
                                                        IntegerArgumentType.getInteger(ctx, "limit")))
                                        )
                                )
                        )
        );
    }

    // ───────────────────────────────
    // Zeigt das eigene Guthaben
    // ───────────────────────────────
    private static int showBalance(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            double balance = EconomyManager.getBalance(player.getUUID());
            player.sendSystemMessage(Component.literal(
                "§a§l━━━━━━━━━━━━━━━━━━\n" +
                "§aDein Kontostand:\n" +
                "§e" + String.format("%.2f", balance) + " €\n" +
                "§a§l━━━━━━━━━━━━━━━━━━"
            ));
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abrufen des Kontostands", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Abrufen deines Kontostands!"));
            return 0;
        }
    }

    // ───────────────────────────────
    // Geld an anderen Spieler senden
    // ───────────────────────────────
    private static int payPlayer(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer sender = ctx.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            UUID senderUUID = sender.getUUID();
            UUID targetUUID = target.getUUID();

            // Rate-Limiting prüfen
            if (!RateLimiter.canPerformTransaction(senderUUID)) {
                int seconds = RateLimiter.getSecondsUntilNextTransaction(senderUUID);
                ctx.getSource().sendFailure(Component.literal(
                    "§c✗ Zu viele Transaktionen!\n" +
                    "§7Bitte warte §e" + seconds + " Sekunden §7bevor du erneut sendest."
                ));
                return 0;
            }

            // Prüfen ob Sender = Empfänger
            if (senderUUID.equals(targetUUID)) {
                ctx.getSource().sendFailure(Component.literal("§cDu kannst dir nicht selbst Geld senden!"));
                return 0;
            }

            // Betrag validieren
            if (amount <= 0) {
                ctx.getSource().sendFailure(Component.literal("§cDer Betrag muss größer als 0 sein!"));
                return 0;
            }

            // Berechne Gebühr
            double transferFee = FeeManager.getTransferFee(amount);
            double totalCost = amount + transferFee;

            // Guthaben prüfen (inkl. Gebühr)
            double senderBalance = EconomyManager.getBalance(senderUUID);
            if (senderBalance < totalCost) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld!\n" +
                    "§7Dein Guthaben: §e" + String.format("%.2f", senderBalance) + " €\n" +
                    "§7Benötigt: §e" + String.format("%.2f", amount) + " € §7+ §c" +
                    String.format("%.2f", transferFee) + " € §7Gebühr"
                ));
                return 0;
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

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Pay-Befehl", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Ausführen des Befehls!"));
            return 0;
        }
    }

    // ───────────────────────────────
    // Admin: Guthaben setzen
    // ───────────────────────────────
    private static int setBalance(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            String adminName = ctx.getSource().getTextName();
            EconomyManager.setBalance(target.getUUID(), amount, TransactionType.ADMIN_SET,
                "Admin: " + adminName);

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§aGuthaben gesetzt!\n" +
                "§7Spieler: §f" + target.getName().getString() + "\n" +
                "§7Neues Guthaben: §e" + String.format("%.2f", amount) + " €"
            ), true);

            target.sendSystemMessage(Component.literal(
                "§eDein Guthaben wurde auf §6" + String.format("%.2f", amount) + " € §egesetzt."
            ));

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Setzen des Guthabens", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Setzen des Guthabens!"));
            return 0;
        }
    }

    // ───────────────────────────────
    // Admin: Geld geben
    // ───────────────────────────────
    private static int giveBalance(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            String adminName = ctx.getSource().getTextName();
            EconomyManager.deposit(target.getUUID(), amount, TransactionType.ADMIN_GIVE,
                "Admin: " + adminName);

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§aGeld hinzugefügt!\n" +
                "§7Spieler: §f" + target.getName().getString() + "\n" +
                "§7Betrag: §e+" + String.format("%.2f", amount) + " €\n" +
                "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(target.getUUID())) + " €"
            ), true);

            target.sendSystemMessage(Component.literal(
                "§a✓ Du hast §e" + String.format("%.2f", amount) + " € §aerhalten!"
            ));

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Hinzufügen von Geld", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Hinzufügen von Geld!"));
            return 0;
        }
    }

    // ───────────────────────────────
    // Admin: Geld nehmen
    // ───────────────────────────────
    private static int takeBalance(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            String adminName = ctx.getSource().getTextName();
            boolean success = EconomyManager.withdraw(target.getUUID(), amount, TransactionType.ADMIN_TAKE,
                "Admin: " + adminName);

            if (success) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§aGeld abgezogen!\n" +
                    "§7Spieler: §f" + target.getName().getString() + "\n" +
                    "§7Betrag: §c-" + String.format("%.2f", amount) + " €\n" +
                    "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(target.getUUID())) + " €"
                ), true);

                target.sendSystemMessage(Component.literal(
                    "§c" + String.format("%.2f", amount) + " € §cwurden von deinem Konto abgezogen."
                ));

                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("§cNicht genug Guthaben beim Zielspieler!"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abziehen von Geld", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Abziehen von Geld!"));
            return 0;
        }
    }

    // ───────────────────────────────
    // Zeigt Transaktionshistorie
    // ───────────────────────────────
    private static int showHistory(CommandContext<CommandSourceStack> ctx, int limit) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            return showHistoryFor(ctx, player, limit);
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abrufen der Historie", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Abrufen der Historie!"));
            return 0;
        }
    }

    private static int showHistoryFor(CommandContext<CommandSourceStack> ctx, ServerPlayer target, int limit) {
        try {
            TransactionHistory history = TransactionHistory.getInstance();
            if (history == null) {
                ctx.getSource().sendFailure(Component.literal("§cTransaktionshistorie nicht verfügbar!"));
                return 0;
            }

            List<Transaction> transactions = history.getRecentTransactions(target.getUUID(), limit);

            if (transactions.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§e" + target.getName().getString() + " §7hat noch keine Transaktionen."
                ), false);
                return 0;
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
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abrufen der Historie", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Abrufen der Historie!"));
            return 0;
        }
    }
}
