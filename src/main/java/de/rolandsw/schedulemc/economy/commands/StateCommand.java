package de.rolandsw.schedulemc.economy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.StateAccount;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

/**
 * State Command - Admin-only commands for state account management
 *
 * /state balance - Shows state account balance
 * /state deposit <amount> - Deposits money into state account
 * /state withdraw <amount> <reason> - Withdraws money (logs the reason)
 * /state history - Shows recent transactions
 */
public class StateCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("state")
                .requires(source -> source.hasPermission(2)) // Admin only

                .then(Commands.literal("balance")
                    .executes(StateCommand::showBalance)
                )

                .then(Commands.literal("deposit")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(StateCommand::deposit)
                    )
                )

                .then(Commands.literal("withdraw")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(StateCommand::withdraw)
                    )
                )
        );
    }

    private static int showBalance(CommandContext<CommandSourceStack> ctx) {
        try {
            int balance = StateAccount.getBalance();

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§e§l=== Staatskasse ===\n" +
                "§7Kontostand: §e" + balance + "€"
            ), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /state balance", e);
            return 0;
        }
    }

    private static int deposit(CommandContext<CommandSourceStack> ctx) {
        try {
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            StateAccount.deposit(amount, "Admin-Einzahlung");

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Einzahlung erfolgreich!\n" +
                "§7Betrag: §e+" + amount + "€\n" +
                "§7Neuer Kontostand: §e" + StateAccount.getBalance() + "€"
            ), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /state deposit", e);
            return 0;
        }
    }

    private static int withdraw(CommandContext<CommandSourceStack> ctx) {
        try {
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            if (StateAccount.withdraw(amount, "Admin-Abhebung")) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Abhebung erfolgreich!\n" +
                    "§7Betrag: §e-" + amount + "€\n" +
                    "§7Neuer Kontostand: §e" + StateAccount.getBalance() + "€"
                ), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld in der Staatskasse!\n" +
                    "§7Kontostand: §e" + StateAccount.getBalance() + "€\n" +
                    "§7Benötigt: §e" + amount + "€"
                ));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei /state withdraw", e);
            return 0;
        }
    }
}
