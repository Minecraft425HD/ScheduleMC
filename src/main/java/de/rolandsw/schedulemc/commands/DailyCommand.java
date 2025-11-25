package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.managers.DailyRewardManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Tägliche Belohnungs-Commands
 */
public class DailyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("daily")
                .executes(DailyCommand::claimDaily)
                
                .then(Commands.literal("streak")
                        .executes(DailyCommand::showStreak))
        );
    }

    private static int claimDaily(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            
            if (!DailyRewardManager.canClaim(player.getUUID())) {
                String timeLeft = DailyRewardManager.getFormattedTimeUntilNext(player.getUUID());
                ctx.getSource().sendFailure(Component.literal(
                    "§cDu hast heute bereits deine Belohnung abgeholt!\n" +
                    "§7Nächste Belohnung in: §e" + timeLeft
                ));
                return 0;
            }
            
            double amount = DailyRewardManager.claimDaily(player.getUUID());
            EconomyManager.deposit(player.getUUID(), amount);
            
            int streak = DailyRewardManager.getStreak(player.getUUID());
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a§l✓ TÄGLICHE BELOHNUNG!\n" +
                "§a+§e" + String.format("%.2f", amount) + "€\n" +
                "§7Streak: §e" + streak + " Tag" + (streak == 1 ? "" : "e") + " 🔥\n" +
                "§7Komm morgen wieder für mehr!"
            ), false);
            
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cFehler!"));
            return 0;
        }
    }

    private static int showStreak(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            Map<String, Object> stats = DailyRewardManager.getStats(player.getUUID());
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§6═══ §eTägliche Belohnungen §6═══\n" +
                "§7Aktueller Streak: §e" + stats.get("currentStreak") + " 🔥\n" +
                "§7Längster Streak: §e" + stats.get("longestStreak") + "\n" +
                "§7Gesamt geclaimed: §e" + stats.get("totalClaims") + "x\n" +
                "§7Nächste Belohnung: §e" + stats.get("timeUntilNext")
            ), false);
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
