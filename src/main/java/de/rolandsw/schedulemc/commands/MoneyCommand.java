package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.EconomyManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

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

            // Guthaben prüfen
            double senderBalance = EconomyManager.getBalance(senderUUID);
            if (senderBalance < amount) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld!\n" +
                    "§7Dein Guthaben: §e" + String.format("%.2f", senderBalance) + " €\n" +
                    "§7Benötigt: §e" + String.format("%.2f", amount) + " €"
                ));
                return 0;
            }

            // Transaktion durchführen
            EconomyManager.withdraw(senderUUID, amount);
            EconomyManager.deposit(targetUUID, amount);

            // Benachrichtigungen
            sender.sendSystemMessage(Component.literal(
                "§a✓ Zahlung erfolgreich!\n" +
                "§7An: §f" + target.getName().getString() + "\n" +
                "§7Betrag: §e" + String.format("%.2f", amount) + " €\n" +
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

            EconomyManager.setBalance(target.getUUID(), amount);

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

            EconomyManager.deposit(target.getUUID(), amount);

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

            boolean success = EconomyManager.withdraw(target.getUUID(), amount);

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
}
