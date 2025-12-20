package de.rolandsw.schedulemc.achievement;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.commands.CommandExecutor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Achievement-Befehle
 */
public class AchievementCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("achievement")
                .then(Commands.literal("list")
                    .executes(AchievementCommand::listAll)
                    .then(Commands.argument("category", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (AchievementCategory cat : AchievementCategory.values()) {
                                builder.suggest(cat.name());
                            }
                            return builder.buildFuture();
                        })
                        .executes(AchievementCommand::listCategory)
                    )
                )
                .then(Commands.literal("progress")
                    .executes(AchievementCommand::showProgress)
                )
                .then(Commands.literal("stats")
                    .executes(AchievementCommand::showStats)
                )
        );
    }

    /**
     * Zeigt alle Achievements
     */
    private static int listAll(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Achievements",
            player -> {
                AchievementManager manager = AchievementManager.getInstance(player.getServer());
                PlayerAchievements playerAch = manager.getPlayerAchievements(player.getUUID());

                player.sendSystemMessage(Component.literal(
                    "§a§l━━━━━━━━━ ACHIEVEMENTS ━━━━━━━━━"
                ));

                for (AchievementCategory category : AchievementCategory.values()) {
                    List<Achievement> achievements = manager.getAchievementsByCategory(category);

                    if (achievements.isEmpty()) continue;

                    player.sendSystemMessage(Component.literal(
                        "\n" + category.getFormattedName()
                    ));

                    for (Achievement ach : achievements) {
                        boolean unlocked = playerAch.isUnlocked(ach.getId());
                        String status = unlocked ? "§a✓" : "§7○";
                        double progress = playerAch.getProgress(ach.getId());
                        String progressStr = ach.getProgressString(progress);

                        if (ach.isHidden() && !unlocked) {
                            player.sendSystemMessage(Component.literal(
                                "  " + status + " §8??? - " + progressStr
                            ));
                        } else {
                            player.sendSystemMessage(Component.literal(
                                "  " + status + " " + ach.getFormattedName() +
                                " §7- " + progressStr
                            ));
                        }
                    }
                }

                player.sendSystemMessage(Component.literal(
                    "\n§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                ));
                player.sendSystemMessage(Component.literal(
                    "§7" + manager.getStatistics(player.getUUID())
                ));
            });
    }

    /**
     * Zeigt Achievements einer Kategorie
     */
    private static int listCategory(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Achievements",
            player -> {
                String categoryName = StringArgumentType.getString(ctx, "category").toUpperCase();
                AchievementCategory category;

                try {
                    category = AchievementCategory.valueOf(categoryName);
                } catch (IllegalArgumentException e) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Ungültige Kategorie! Verfügbar: ECONOMY, CRIME, PRODUCTION, SOCIAL, EXPLORATION");
                    return;
                }

                AchievementManager manager = AchievementManager.getInstance(player.getServer());
                PlayerAchievements playerAch = manager.getPlayerAchievements(player.getUUID());
                List<Achievement> achievements = manager.getAchievementsByCategory(category);

                player.sendSystemMessage(Component.literal(
                    "§a§l━━ " + category.getFormattedName().toUpperCase() + " ━━"
                ));
                player.sendSystemMessage(Component.literal(
                    "§7" + category.getDescription()
                ));

                for (Achievement ach : achievements) {
                    boolean unlocked = playerAch.isUnlocked(ach.getId());
                    String status = unlocked ? "§a✓" : "§7○";
                    double progress = playerAch.getProgress(ach.getId());
                    String progressStr = ach.getProgressString(progress);

                    if (ach.isHidden() && !unlocked) {
                        player.sendSystemMessage(Component.literal(
                            "\n" + status + " §8???"
                        ));
                        player.sendSystemMessage(Component.literal(
                            "  §7Fortschritt: " + progressStr
                        ));
                    } else {
                        player.sendSystemMessage(Component.literal(
                            "\n" + status + " " + ach.getFormattedName()
                        ));
                        player.sendSystemMessage(Component.literal(
                            "  " + ach.getFormattedDescription()
                        ));
                        player.sendSystemMessage(Component.literal(
                            "  §7Fortschritt: " + progressStr
                        ));
                        if (!unlocked) {
                            player.sendSystemMessage(Component.literal(
                                "  §7Belohnung: " + ach.getRewardString()
                            ));
                        }
                    }
                }
            });
    }

    /**
     * Zeigt Fortschritt
     */
    private static int showProgress(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen des Fortschritts",
            player -> {
                AchievementManager manager = AchievementManager.getInstance(player.getServer());
                PlayerAchievements playerAch = manager.getPlayerAchievements(player.getUUID());

                int total = manager.getAllAchievements().size();
                int unlocked = playerAch.getUnlockedCount();
                double percentage = (double) unlocked / total * 100.0;

                player.sendSystemMessage(Component.literal(
                    "§a§l━━━━━ ACHIEVEMENT FORTSCHRITT ━━━━━\n" +
                    "§7Freigeschaltet: §e" + unlocked + " §7/ §e" + total + "\n" +
                    "§7Fortschritt: §e" + String.format("%.1f%%", percentage) + "\n" +
                    "§7Verdient: §a" + String.format("%.2f€", playerAch.getTotalPointsEarned()) + "\n" +
                    "§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                ));

                // Zeige fast fertige Achievements
                player.sendSystemMessage(Component.literal("\n§e§lFast geschafft:"));

                int shown = 0;
                for (Achievement ach : manager.getAllAchievements()) {
                    if (playerAch.isUnlocked(ach.getId())) continue;

                    double progress = playerAch.getProgress(ach.getId());
                    double percentage2 = progress / ach.getRequirement();

                    if (percentage2 >= 0.7 && shown < 5) { // >= 70% Fortschritt
                        player.sendSystemMessage(Component.literal(
                            "  §7• " + ach.getName() + " - " + ach.getProgressString(progress)
                        ));
                        shown++;
                    }
                }

                if (shown == 0) {
                    player.sendSystemMessage(Component.literal(
                        "  §7Keine Achievements nahe der Vollendung"
                    ));
                }
            });
    }

    /**
     * Zeigt Statistiken
     */
    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen der Statistiken",
            player -> {
                AchievementManager manager = AchievementManager.getInstance(player.getServer());
                PlayerAchievements playerAch = manager.getPlayerAchievements(player.getUUID());

                player.sendSystemMessage(Component.literal(
                    "§a§l━━━━━ ACHIEVEMENT STATISTIKEN ━━━━━"
                ));

                // Pro Kategorie
                for (AchievementCategory category : AchievementCategory.values()) {
                    List<Achievement> catAch = manager.getAchievementsByCategory(category);
                    int catTotal = catAch.size();
                    int catUnlocked = (int) catAch.stream()
                        .filter(a -> playerAch.isUnlocked(a.getId()))
                        .count();

                    if (catTotal > 0) {
                        double catPercentage = (double) catUnlocked / catTotal * 100.0;
                        player.sendSystemMessage(Component.literal(
                            category.getEmoji() + " §7" + category.getDisplayName() + ": §e" +
                            catUnlocked + "§7/§e" + catTotal + " §7(" +
                            String.format("%.1f%%", catPercentage) + ")"
                        ));
                    }
                }

                player.sendSystemMessage(Component.literal(
                    "\n§7Gesamt verdient: §a" +
                    String.format("%.2f€", playerAch.getTotalPointsEarned())
                ));
            });
    }
}
