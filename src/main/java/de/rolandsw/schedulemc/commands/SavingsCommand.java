package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.SavingsAccount;
import de.rolandsw.schedulemc.economy.SavingsAccountManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.List;

/**
 * Sparkonto-Befehle
 */
public class SavingsCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());

            if (manager.createSavingsAccount(player.getUUID(), amount)) {
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal(
                    "§cSparkonto konnte nicht erstellt werden!\n" +
                    "§7Mindesteinlage: §e1000€\n" +
                    "§7Max. Gesamtsumme: §e50.000€"
                ));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Error creating savings account", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Erstellen des Sparkontos!"));
            return 0;
        }
    }

    private static int listAccounts(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
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

            return 1;
        } catch (Exception e) {
            LOGGER.error("Error listing savings accounts", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Abrufen der Sparkonten!"));
            return 0;
        }
    }

    private static int depositToAccount(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String accountId = StringArgumentType.getString(ctx, "accountId");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());

            if (manager.depositToSavings(player.getUUID(), accountId, amount)) {
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("§cEinzahlung fehlgeschlagen!"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Error depositing to savings", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler bei der Einzahlung!"));
            return 0;
        }
    }

    private static int withdrawFromAccount(CommandContext<CommandSourceStack> ctx, boolean forced) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String accountId = StringArgumentType.getString(ctx, "accountId");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());

            if (manager.withdrawFromSavings(player.getUUID(), accountId, amount, forced)) {
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal(
                    "§cAbhebung fehlgeschlagen!\n" +
                    "§7Konto noch gesperrt? Nutze §e/savings forcewithdraw§7 mit 10% Strafe"
                ));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Error withdrawing from savings", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler bei der Abhebung!"));
            return 0;
        }
    }

    private static int closeAccount(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String accountId = StringArgumentType.getString(ctx, "accountId");

            SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());

            if (manager.closeSavingsAccount(player.getUUID(), accountId)) {
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("§cKonto konnte nicht geschlossen werden!"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Error closing savings account", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Schließen des Kontos!"));
            return 0;
        }
    }
}
