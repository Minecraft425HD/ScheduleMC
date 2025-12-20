package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.economy.SavingsAccount;
import de.rolandsw.schedulemc.economy.SavingsAccountManager;
import de.rolandsw.schedulemc.commands.CommandExecutor;
import de.rolandsw.schedulemc.util.InputValidation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Sparkonto-Befehle
 * Refactored mit CommandExecutor
 */
public class SavingsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("savings")
                .then(Commands.literal("create")
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1000))
                        .executes(SavingsCommand::createAccount)
                    )
                )
                .then(Commands.literal("list")
                    .executes(SavingsCommand::listAccounts)
                )
                .then(Commands.literal("deposit")
                    .then(Commands.argument("accountId", StringArgumentType.word())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
                            .executes(SavingsCommand::depositToAccount)
                        )
                    )
                )
                .then(Commands.literal("withdraw")
                    .then(Commands.argument("accountId", StringArgumentType.word())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
                            .executes(ctx -> withdrawFromAccount(ctx, false))
                        )
                    )
                )
                .then(Commands.literal("forcewithdraw")
                    .then(Commands.argument("accountId", StringArgumentType.word())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
                            .executes(ctx -> withdrawFromAccount(ctx, true))
                        )
                    )
                )
                .then(Commands.literal("close")
                    .then(Commands.argument("accountId", StringArgumentType.word())
                        .executes(SavingsCommand::closeAccount)
                    )
                )
        );
    }

    private static int createAccount(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Erstellen des Sparkontos",
            player -> {
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION
                InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                if (validation.isFailure()) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "§c❌ Ungültiger Betrag: §f" + validation.getErrorMessage());
                    return;
                }

                SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());

                if (!manager.createSavingsAccount(player.getUUID(), amount)) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Sparkonto konnte nicht erstellt werden!\n" +
                        "Mindesteinlage: 1000€\n" +
                        "Max. Gesamtsumme: 50.000€"
                    );
                }
            });
    }

    private static int listAccounts(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Sparkonten",
            player -> {
                SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());
                List<SavingsAccount> accounts = manager.getAccounts(player.getUUID());

                if (accounts.isEmpty()) {
                    player.sendSystemMessage(Component.literal(
                        "§e§l[SPARKONTEN]\n" +
                        "§7Du hast keine Sparkonten.\n\n" +
                        "§aErstelle ein Sparkonto:\n" +
                        "§7/savings create <betrag>\n\n" +
                        "§7Zinssatz: §a5.0% §7pro Woche\n" +
                        "§7Sperrfrist: §e4 Wochen\n" +
                        "§7Mindesteinlage: §e1.000€\n" +
                        "§7Max. Gesamtsumme: §e50.000€"
                    ));
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("§a§l━━━━━━━━━ SPARKONTEN ━━━━━━━━━\n");

                    double total = 0;
                    for (SavingsAccount account : accounts) {
                        total += account.getBalance();
                        long day = player.getServer().overworld().getDayTime() / 24000L;
                        int daysUntilUnlock = account.getDaysUntilUnlock(day);

                        sb.append("§7ID: §f").append(account.getAccountId().substring(0, 8)).append("\n");
                        sb.append("§7Guthaben: §6").append(String.format("%.2f€", account.getBalance())).append("\n");

                        if (daysUntilUnlock > 0) {
                            sb.append("§7Status: §c🔒 Gesperrt (noch ").append(daysUntilUnlock).append(" Tage)\n");
                        } else {
                            sb.append("§7Status: §a🔓 Entsperrt\n");
                        }

                        sb.append("\n");
                    }

                    sb.append("§a§l━━━━━━━━━ GESAMT ━━━━━━━━━\n");
                    sb.append("§7Gesamtsumme: §6§l").append(String.format("%.2f€", total)).append("\n");
                    sb.append("§7Zinssatz: §a5.0% §7pro Woche");

                    player.sendSystemMessage(Component.literal(sb.toString()));
                }
            });
    }

    private static int depositToAccount(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei der Einzahlung",
            player -> {
                String accountId = StringArgumentType.getString(ctx, "accountId");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION
                InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                if (validation.isFailure()) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "§c❌ Ungültiger Betrag: §f" + validation.getErrorMessage());
                    return;
                }

                SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());

                if (!manager.depositToSavings(player.getUUID(), accountId, amount)) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Einzahlung fehlgeschlagen!");
                }
            });
    }

    private static int withdrawFromAccount(CommandContext<CommandSourceStack> ctx, boolean forced) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei der Abhebung",
            player -> {
                String accountId = StringArgumentType.getString(ctx, "accountId");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");

                // ✅ INPUT VALIDATION
                InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                if (validation.isFailure()) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "§c❌ Ungültiger Betrag: §f" + validation.getErrorMessage());
                    return;
                }

                SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());

                if (!manager.withdrawFromSavings(player.getUUID(), accountId, amount, forced)) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Abhebung fehlgeschlagen!\n" +
                        "Konto noch gesperrt? Nutze /savings forcewithdraw mit 10% Strafe"
                    );
                }
            });
    }

    private static int closeAccount(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Schließen des Kontos",
            player -> {
                String accountId = StringArgumentType.getString(ctx, "accountId");
                SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());

                if (!manager.closeSavingsAccount(player.getUUID(), accountId)) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Konto konnte nicht geschlossen werden!");
                }
            });
    }
}
