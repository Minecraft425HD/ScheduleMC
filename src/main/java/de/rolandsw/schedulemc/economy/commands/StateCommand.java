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

            ctx.getSource().sendSuccess(() -> Component.translatable("command.state.header")
                .append("\n")
                .append(Component.translatable("command.state.balance", balance)), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("command.state.error.balance", e);
            return 0;
        }
    }

    private static int deposit(CommandContext<CommandSourceStack> ctx) {
        try {
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            StateAccount.deposit(amount, "Admin-Einzahlung");

            int newBalance = StateAccount.getBalance();
            ctx.getSource().sendSuccess(() -> Component.translatable("command.state.deposit_success")
                .append("\n")
                .append(Component.translatable("command.state.deposit_amount", amount))
                .append("\n")
                .append(Component.translatable("command.state.new_balance", newBalance)), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("command.state.error.deposit", e);
            return 0;
        }
    }

    private static int withdraw(CommandContext<CommandSourceStack> ctx) {
        try {
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            if (StateAccount.withdraw(amount, "Admin-Abhebung")) {
                int newBalance = StateAccount.getBalance();
                ctx.getSource().sendSuccess(() -> Component.translatable("command.state.withdraw_success")
                    .append("\n")
                    .append(Component.translatable("command.state.withdraw_amount", amount))
                    .append("\n")
                    .append(Component.translatable("command.state.new_balance", newBalance)), false);
                return 1;
            } else {
                int currentBalance = StateAccount.getBalance();
                ctx.getSource().sendFailure(Component.translatable("command.state.insufficient_funds")
                    .append("\n")
                    .append(Component.translatable("command.state.balance", currentBalance))
                    .append("\n")
                    .append(Component.translatable("command.state.required_amount", amount)));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("command.state.error.withdraw", e);
            return 0;
        }
    }
}
