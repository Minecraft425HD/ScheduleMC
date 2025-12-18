package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.Loan;
import de.rolandsw.schedulemc.economy.LoanManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/**
 * Kredit-Befehle
 */
public class LoanCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String typeStr = StringArgumentType.getString(ctx, "type").toUpperCase();

            Loan.LoanType type;
            try {
                type = Loan.LoanType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                ctx.getSource().sendFailure(Component.literal("§cUngültiger Kredit-Typ! Nutze: SMALL, MEDIUM, LARGE"));
                return 0;
            }

            LoanManager manager = LoanManager.getInstance(player.getServer());

            if (manager.hasActiveLoan(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDu hast bereits einen aktiven Kredit!"));
                return 0;
            }

            if (manager.applyForLoan(player.getUUID(), type)) {
                player.sendSystemMessage(Component.literal(
                    "§a§l✓ Kredit bewilligt!\n" +
                    "§7Typ: §e" + type.name() + "\n" +
                    "§7Betrag: §a+" + String.format("%.2f€", type.getAmount()) + "\n" +
                    "§7Zinssatz: §c" + (int)(type.getInterestRate() * 100) + "%\n" +
                    "§7Laufzeit: §e" + type.getDurationDays() + " Tage"
                ));
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal(
                    "§cKredit abgelehnt!\n" +
                    "§7Voraussetzungen:\n" +
                    "§7- Kein aktiver Kredit\n" +
                    "§7- Mindestens 1000€ auf dem Konto"
                ));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Error applying for loan", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Beantragen des Kredits!"));
            return 0;
        }
    }

    private static int showLoanInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
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

            return 1;
        } catch (Exception e) {
            LOGGER.error("Error showing loan info", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Abrufen der Kredit-Info!"));
            return 0;
        }
    }

    private static int repayLoan(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            LoanManager manager = LoanManager.getInstance(player.getServer());

            if (manager.repayLoan(player.getUUID())) {
                player.sendSystemMessage(Component.literal(
                    "§a§l✓ Kredit vollständig abbezahlt!\n" +
                    "§aDu bist nun schuldenfrei!"
                ));
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("§cKredit konnte nicht abbezahlt werden! Nicht genug Geld."));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Error repaying loan", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Abbezahlen des Kredits!"));
            return 0;
        }
    }
}
