package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.economy.Loan;
import de.rolandsw.schedulemc.economy.LoanManager;
import de.rolandsw.schedulemc.commands.CommandExecutor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Kredit-Befehle
 * Refactored mit CommandExecutor
 */
public class LoanCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("loan")
                .then(Commands.literal("apply")
                    .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("SMALL");
                            builder.suggest("MEDIUM");
                            builder.suggest("LARGE");
                            return builder.buildFuture();
                        })
                        .executes(LoanCommand::applyForLoan)
                    )
                )
                .then(Commands.literal("info")
                    .executes(LoanCommand::showLoanInfo)
                )
                .then(Commands.literal("repay")
                    .executes(LoanCommand::repayLoan)
                )
        );
    }

    private static int applyForLoan(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Beantragen des Kredits",
            player -> {
                String typeStr = StringArgumentType.getString(ctx, "type").toUpperCase();

                Loan.LoanType type;
                try {
                    type = Loan.LoanType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Ungültiger Kredit-Typ! Nutze: SMALL, MEDIUM, LARGE");
                    return;
                }

                LoanManager manager = LoanManager.getInstance(player.getServer());

                if (manager.hasActiveLoan(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du hast bereits einen aktiven Kredit!");
                    return;
                }

                if (manager.applyForLoan(player.getUUID(), type)) {
                    player.sendSystemMessage(Component.literal(
                        "§a§l✓ Kredit bewilligt!\n" +
                        "§7Typ: §e" + type.name() + "\n" +
                        "§7Betrag: §a+" + String.format("%.2f€", type.getAmount()) + "\n" +
                        "§7Zinssatz: §c" + (int)(type.getInterestRate() * 100) + "%\n" +
                        "§7Laufzeit: §e" + type.getDurationDays() + " Tage"
                    ));
                } else {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Kredit abgelehnt!\n" +
                        "Voraussetzungen:\n" +
                        "- Kein aktiver Kredit\n" +
                        "- Mindestens 1000€ auf dem Konto"
                    );
                }
            });
    }

    private static int showLoanInfo(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Kredit-Info",
            player -> {
                LoanManager manager = LoanManager.getInstance(player.getServer());
                Loan loan = manager.getLoan(player.getUUID());

                if (loan == null) {
                    player.sendSystemMessage(Component.literal(
                        "§e§l[KREDIT-INFO]\n" +
                        "§7Du hast keinen aktiven Kredit.\n\n" +
                        "§eVerfügbare Kredite:\n" +
                        "§7SMALL: §a5.000€ §7(10%, 14 Tage)\n" +
                        "§7MEDIUM: §a25.000€ §7(15%, 28 Tage)\n" +
                        "§7LARGE: §a100.000€ §7(20%, 56 Tage)\n\n" +
                        "§7Beantragen: §e/loan apply <typ>"
                    ));
                } else {
                    player.sendSystemMessage(Component.literal(
                        "§e§l[AKTIVER KREDIT]\n" +
                        "§7Typ: §e" + loan.getType().name() + "\n" +
                        "§7Kreditsumme: §a" + String.format("%.2f€", loan.getPrincipal()) + "\n" +
                        "§7Zinssatz: §c" + (int)(loan.getInterestRate() * 100) + "%\n" +
                        "§7Verbleibend: §c" + String.format("%.2f€", loan.getRemaining()) + "\n" +
                        "§7Tägliche Rate: §c-" + String.format("%.2f€", loan.getDailyPayment()) + "\n" +
                        "§7Vorzeitig zurückzahlen: §e/loan repay"
                    ));
                }
            });
    }

    private static int repayLoan(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abbezahlen des Kredits",
            player -> {
                LoanManager manager = LoanManager.getInstance(player.getServer());

                if (manager.repayLoan(player.getUUID())) {
                    player.sendSystemMessage(Component.literal(
                        "§a§l✓ Kredit vollständig abbezahlt!\n" +
                        "§aDu bist nun schuldenfrei!"
                    ));
                } else {
                    CommandExecutor.sendFailure(ctx.getSource(), "Kredit konnte nicht abbezahlt werden! Nicht genug Geld.");
                }
            });
    }
}
