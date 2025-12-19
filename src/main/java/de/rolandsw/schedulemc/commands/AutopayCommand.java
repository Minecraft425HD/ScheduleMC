package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.economy.RecurringPayment;
import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.commands.CommandExecutor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Dauerauftrags-Befehle
 * Refactored mit CommandExecutor
 */
public class AutopayCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("autopay")
                .then(Commands.literal("add")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
                            .then(Commands.argument("intervalDays", IntegerArgumentType.integer(1))
                                .then(Commands.argument("description", StringArgumentType.greedyString())
                                    .executes(AutopayCommand::addPayment)
                                )
                            )
                        )
                    )
                )
                .then(Commands.literal("list")
                    .executes(AutopayCommand::listPayments)
                )
                .then(Commands.literal("pause")
                    .then(Commands.argument("paymentId", StringArgumentType.word())
                        .executes(AutopayCommand::pausePayment)
                    )
                )
                .then(Commands.literal("resume")
                    .then(Commands.argument("paymentId", StringArgumentType.word())
                        .executes(AutopayCommand::resumePayment)
                    )
                )
                .then(Commands.literal("delete")
                    .then(Commands.argument("paymentId", StringArgumentType.word())
                        .executes(AutopayCommand::deletePayment)
                    )
                )
        );
    }

    private static int addPayment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Erstellen des Dauerauftrags",
            player -> {
                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                double amount = DoubleArgumentType.getDouble(ctx, "amount");
                int intervalDays = IntegerArgumentType.getInteger(ctx, "intervalDays");
                String description = StringArgumentType.getString(ctx, "description");

                RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());

                if (manager.createRecurringPayment(player.getUUID(), target.getUUID(),
                        amount, intervalDays, description)) {
                    // Success message wird vom Manager gesendet
                } else {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Dauerauftrag konnte nicht erstellt werden!\n" +
                        "Max. 10 Daueraufträge pro Spieler"
                    );
                }
            });
    }

    private static int listPayments(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Daueraufträge",
            player -> {
                RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());
                List<RecurringPayment> payments = manager.getPayments(player.getUUID());

                if (payments.isEmpty()) {
                    player.sendSystemMessage(Component.literal(
                        "§e§l[DAUERAUFTRÄGE]\n" +
                        "§7Du hast keine Daueraufträge.\n\n" +
                        "§aErstelle einen Dauerauftrag:\n" +
                        "§7/autopay add <spieler> <betrag> <tage> <beschreibung>"
                    ));
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("§a§l━━━━ DAUERAUFTRÄGE ━━━━\n\n");

                    long day = player.getServer().overworld().getDayTime() / 24000L;

                    for (RecurringPayment payment : payments) {
                        sb.append("§7ID: §f").append(payment.getPaymentId().substring(0, 8)).append("\n");
                        sb.append("§7An: §e").append(payment.getToPlayer()).append("\n");
                        sb.append("§7Betrag: §c-").append(String.format("%.2f€", payment.getAmount())).append("\n");
                        sb.append("§7Interval: §e").append(payment.getIntervalDays()).append(" Tage\n");
                        sb.append("§7Beschreibung: §f").append(payment.getDescription()).append("\n");

                        if (payment.isActive()) {
                            int daysUntil = payment.getDaysUntilNext(day);
                            sb.append("§7Status: §aAktiv §7(nächste Zahlung in ").append(daysUntil).append(" Tagen)\n");
                        } else {
                            sb.append("§7Status: §cPausiert\n");
                        }

                        if (payment.getFailureCount() > 0) {
                            sb.append("§cFehlversuche: ").append(payment.getFailureCount()).append("/3\n");
                        }

                        sb.append("\n");
                    }

                    player.sendSystemMessage(Component.literal(sb.toString()));
                }
            });
    }

    private static int pausePayment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Pausieren",
            player -> {
                String paymentId = StringArgumentType.getString(ctx, "paymentId");
                RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());

                if (!manager.pauseRecurringPayment(player.getUUID(), paymentId)) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dauerauftrag nicht gefunden!");
                }
            });
    }

    private static int resumePayment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Aktivieren",
            player -> {
                String paymentId = StringArgumentType.getString(ctx, "paymentId");
                RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());

                if (!manager.resumeRecurringPayment(player.getUUID(), paymentId)) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dauerauftrag nicht gefunden!");
                }
            });
    }

    private static int deletePayment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Löschen",
            player -> {
                String paymentId = StringArgumentType.getString(ctx, "paymentId");
                RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());

                if (!manager.deleteRecurringPayment(player.getUUID(), paymentId)) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dauerauftrag nicht gefunden!");
                }
            });
    }
}
