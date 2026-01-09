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
        return CommandExecutor.executePlayerCommand(ctx, Component.translatable("command.bounty.fetch_error"),
            player -> {
                BountyManager manager = BountyManager.getInstance(player.getServer());
                List<BountyData> bounties = manager.getTopBounties(10);

                if (bounties.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("command.bounty.no_bounties"));
                    return;
                }

                player.sendSystemMessage(Component.translatable("command.bounty.header"));

                int rank = 1;
                for (BountyData bounty : bounties) {
                    String targetName = "Spieler"; // TODO: Get player name

                    player.sendSystemMessage(Component.translatable(
                        "command.bounty.list_entry",
                        rank,
                        targetName,
                        String.format("%.2f", bounty.getAmount()),
                        bounty.getReason()
                    ));
                    rank++;
                }

                player.sendSystemMessage(Component.translatable("command.bounty.divider"));
            });
    }

    /**
     * Platziert Kopfgeld
     */
    private static int placeBounty(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, Component.translatable("command.bounty.place_error"),
            player -> {
                try {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                    double amount = DoubleArgumentType.getDouble(ctx, "amount");
                    String reason = StringArgumentType.getString(ctx, "reason");

                    // Input validation
                    InputValidation.ValidationResult validation = InputValidation.validateAmount(amount);
                    if (validation.isFailure()) {
                        CommandExecutor.sendFailure(ctx.getSource(),
                            Component.translatable("command.bounty.invalid_amount", validation.getErrorMessage()));
                        return;
                    }

                    // Name validation
                    InputValidation.ValidationResult nameValidation = InputValidation.validateName(reason);
                    if (nameValidation.isFailure()) {
                        CommandExecutor.sendFailure(ctx.getSource(),
                            Component.translatable("command.bounty.invalid_reason", nameValidation.getErrorMessage()));
                        return;
                    }

                    BountyManager manager = BountyManager.getInstance(player.getServer());

                    if (manager.placeBounty(player.getUUID(), target.getUUID(), amount, reason)) {
                        player.sendSystemMessage(Component.translatable(
                            "command.bounty.place_success",
                            target.getName().getString(),
                            String.format("%.2f", amount)
                        ));
                    } else {
                        CommandExecutor.sendFailure(ctx.getSource(),
                            Component.translatable("command.bounty.place_failed")
                        );
                    }
                } catch (Exception e) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        Component.translatable("command.bounty.place_exception", e.getMessage()));
                }
            });
    }

    /**
     * Zeigt eigenes Bounty
     */
    private static int showOwnBounty(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, Component.translatable("command.bounty.fetch_error"),
            player -> {
                BountyManager manager = BountyManager.getInstance(player.getServer());
                BountyData bounty = manager.getActiveBounty(player.getUUID());

                if (bounty == null) {
                    player.sendSystemMessage(Component.translatable("command.bounty.no_bounty_on_you"));
                } else {
                    player.sendSystemMessage(Component.literal(bounty.getFormattedDescription()));
                }
            });
    }

    /**
     * Zeigt Bounty eines anderen Spielers
     */
    private static int showTargetBounty(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, Component.translatable("command.bounty.fetch_error"),
            player -> {
                try {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                    BountyManager manager = BountyManager.getInstance(player.getServer());
                    BountyData bounty = manager.getActiveBounty(target.getUUID());

                    if (bounty == null) {
                        player.sendSystemMessage(Component.translatable(
                            "command.bounty.no_bounty_on_target",
                            target.getName().getString()
                        ));
                    } else {
                        player.sendSystemMessage(Component.literal(
                            Component.translatable("command.bounty.target_bounty_header", target.getName().getString()).getString() + "\n" +
                            bounty.getFormattedDescription()
                        ));
                    }
                } catch (Exception e) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        Component.translatable("command.bounty.player_not_found"));
                }
            });
    }

    /**
     * Zeigt Bounty-Historie
     */
    private static int showHistory(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, Component.translatable("command.bounty.history_error"),
            player -> {
                BountyManager manager = BountyManager.getInstance(player.getServer());
                List<BountyData> history = manager.getBountyHistory(player.getUUID());

                if (history.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("command.bounty.no_history"));
                    return;
                }

                player.sendSystemMessage(Component.translatable("command.bounty.history_header"));

                int count = 0;
                for (BountyData bounty : history) {
                    if (count >= 5) break; // Nur letzte 5

                    String status = bounty.isClaimed() ? Component.translatable("command.bounty.claimed").getString() : Component.translatable("command.bounty.expired").getString();
                    player.sendSystemMessage(Component.translatable(
                        "command.bounty.history_entry",
                        bounty.getFormattedDate(),
                        String.format("%.2f", bounty.getAmount()),
                        bounty.getReason(),
                        status
                    ));
                    count++;
                }

                player.sendSystemMessage(Component.translatable("command.bounty.divider"));
            });
    }
}
