package de.rolandsw.schedulemc.npc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.npc.items.NPCItems;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Admin-Commands für spezielle Admin-Tools
 * /admintools remover - Gibt dem Spieler den Entity-Remover
 *
 * Erfordert OP-Level 2
 */
public class AdminToolsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("admintools")
                .requires(source -> source.hasPermission(2)) // OP Level 2 erforderlich
                .then(Commands.literal("remover")
                    .executes(AdminToolsCommand::giveRemover)
                )
                .then(Commands.literal("help")
                    .executes(AdminToolsCommand::showHelp)
                )
        );
    }

    /**
     * Gibt dem Spieler den Entity-Remover
     */
    private static int giveRemover(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            ItemStack removerItem = new ItemStack(NPCItems.ENTITY_REMOVER.get());

            // Versuche Item ins Inventar zu geben
            if (!player.getInventory().add(removerItem)) {
                // Inventar voll - droppe Item
                player.drop(removerItem, false);
                player.sendSystemMessage(
                    Component.translatable("command.admintools.inventory_full")
                        .withStyle(ChatFormatting.YELLOW)
                );
            }

            context.getSource().sendSuccess(() ->
                Component.translatable("command.admintools.remover_given")
                    .withStyle(ChatFormatting.GREEN),
                false
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.translatable("command.admintools.player_only")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }
    }

    /**
     * Zeigt Hilfe für Admin-Tools
     */
    private static int showHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("=== Admin Tools ===").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("\n"))
                .append(Component.literal("/admintools remover").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("command.admintools.help.remover").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\n"))
                .append(Component.literal("/admintools help").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("command.admintools.help.help").withStyle(ChatFormatting.WHITE)),
            false
        );

        return 1;
    }
}
