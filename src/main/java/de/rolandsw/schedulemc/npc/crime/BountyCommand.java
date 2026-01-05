package de.rolandsw.schedulemc.npc.crime;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.commands.CommandExecutor;
import de.rolandsw.schedulemc.util.InputValidation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Kopfgeld-Befehle
 */
public class BountyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("bounty")
                .then(Commands.literal("list")
                    .executes(BountyCommand::listBounties)
                )
                .then(Commands.literal("place")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(100.0))
                            .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(BountyCommand::placeBounty)
                            )
                        )
                    )
                )
                .then(Commands.literal("info")
                    .executes(BountyCommand::showOwnBounty)
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(BountyCommand::showTargetBounty)
                    )
                )
                .then(Commands.literal("history")
                    .executes(BountyCommand::showHistory)
                )
        );
    }

    /**
     * Zeigt alle aktiven Bounties
     */
    private static int listBounties(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Kopfgelder",
            player -> {
                BountyManager manager = BountyManager.getInstance(player.getServer());
                List<BountyData> bounties = manager.getTopBounties(10);

                if (bounties.isEmpty()) {
                    player.sendSystemMessage(Component.literal(
                        "§e§l[KOPFGELDER]\n" +
                        "§7Keine aktiven Kopfgelder vorhanden."
                    ));
                    return;
                }

                player.sendSystemMessage(Component.literal(
                    "§6§l━━━━━━━━ TOP KOPFGELDER ━━━━━━━━"
                ));

                int rank = 1;
                for (BountyData bounty : bounties) {
                    // Get player name from server (UUID -> name lookup)
                    String targetName = server.getProfileCache() != null && bounty.getTargetPlayer() != null
                        ? server.getProfileCache().get(bounty.getTargetPlayer()).map(p -> p.getName()).orElse("Unknown Player")
                        : "Unknown Player";

                    player.sendSystemMessage(Component.literal(
                        String.format(
                            "\n§e#%d §7- §f%s\n" +
                            "  §7Betrag: §a%.2f€\n" +
                            "  §7Grund: §e%s",
                            rank,
                            targetName,
                            bounty.getAmount(),
                            bounty.getReason()
                        )
                    ));
                    rank++;
                }

                player.sendSystemMessage(Component.literal(
                    "\n§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                ));
            });
    }

    /**
     * Platziert Kopfgeld
     */
    private static int placeBounty(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Platzieren des Kopfgelds",
            player -> {
                try {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                    double amount = DoubleArgumentType.getDouble(ctx, "amount");
                    String reason = StringArgumentType.getString(ctx, "reason");

                    // Input validation
                    InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                    if (validation.isFailure()) {
                        CommandExecutor.sendFailure(ctx.getSource(),
                            "§c❌ Ungültiger Betrag: §f" + validation.getErrorMessage());
                        return;
                    }

                    // Name validation
                    InputValidation.ValidationResult nameValidation = InputValidation.validateName(reason);
                    if (nameValidation.isFailure()) {
                        CommandExecutor.sendFailure(ctx.getSource(),
                            "§c❌ Ungültiger Grund: §f" + nameValidation.getErrorMessage());
                        return;
                    }

                    BountyManager manager = BountyManager.getInstance(player.getServer());

                    if (manager.placeBounty(player.getUUID(), target.getUUID(), amount, reason)) {
                        player.sendSystemMessage(Component.literal(
                            "§a§l✓ KOPFGELD PLATZIERT!\n" +
                            "§7Auf §e" + target.getName().getString() + " §7wurde ein Kopfgeld von\n" +
                            "§a" + String.format("%.2f€", amount) + " §7ausgesetzt!"
                        ));
                    } else {
                        CommandExecutor.sendFailure(ctx.getSource(),
                            "Kopfgeld konnte nicht platziert werden!\n" +
                            "Mögliche Gründe:\n" +
                            "- Nicht genug Geld\n" +
                            "- Ungültiges Ziel (du selbst)"
                        );
                    }
                } catch (Exception e) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Fehler beim Platzieren: " + e.getMessage());
                }
            });
    }

    /**
     * Zeigt eigenes Bounty
     */
    private static int showOwnBounty(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen des Kopfgelds",
            player -> {
                BountyManager manager = BountyManager.getInstance(player.getServer());
                BountyData bounty = manager.getActiveBounty(player.getUUID());

                if (bounty == null) {
                    player.sendSystemMessage(Component.literal(
                        "§a§l✓ Keine aktiven Kopfgelder auf dich!"
                    ));
                } else {
                    player.sendSystemMessage(Component.literal(bounty.getFormattedDescription()));
                }
            });
    }

    /**
     * Zeigt Bounty eines anderen Spielers
     */
    private static int showTargetBounty(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen des Kopfgelds",
            player -> {
                try {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                    BountyManager manager = BountyManager.getInstance(player.getServer());
                    BountyData bounty = manager.getActiveBounty(target.getUUID());

                    if (bounty == null) {
                        player.sendSystemMessage(Component.literal(
                            "§7Kein aktives Kopfgeld auf §e" + target.getName().getString()
                        ));
                    } else {
                        player.sendSystemMessage(Component.literal(
                            "§6§lKOPFGELD: §f" + target.getName().getString() + "\n" +
                            bounty.getFormattedDescription()
                        ));
                    }
                } catch (Exception e) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Spieler nicht gefunden!");
                }
            });
    }

    /**
     * Zeigt Bounty-Historie
     */
    private static int showHistory(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Historie",
            player -> {
                BountyManager manager = BountyManager.getInstance(player.getServer());
                List<BountyData> history = manager.getBountyHistory(player.getUUID());

                if (history.isEmpty()) {
                    player.sendSystemMessage(Component.literal(
                        "§7Keine Kopfgeld-Historie vorhanden."
                    ));
                    return;
                }

                player.sendSystemMessage(Component.literal(
                    "§6§l━━━━━ KOPFGELD-HISTORIE ━━━━━"
                ));

                int count = 0;
                for (BountyData bounty : history) {
                    if (count >= 5) break; // Nur letzte 5

                    String status = bounty.isClaimed() ? "§c✗ Eingelöst" : "§7⏱ Abgelaufen";
                    player.sendSystemMessage(Component.literal(
                        String.format(
                            "\n§7[%s]\n" +
                            "  §7Betrag: §a%.2f€\n" +
                            "  §7Grund: §e%s\n" +
                            "  §7Status: %s",
                            bounty.getFormattedDate(),
                            bounty.getAmount(),
                            bounty.getReason(),
                            status
                        )
                    ));
                    count++;
                }

                player.sendSystemMessage(Component.literal(
                    "\n§6§l━━━━━━━━━━━━━━━━━━━━━━━━"
                ));
            });
    }
}
