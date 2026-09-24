package de.rolandsw.schedulemc.market;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Season Command - /season
 *
 * Zeigt die aktuelle Jahreszeit, den Fortschritt bis zur naechsten Saison
 * und die dadurch aktiven Preisaenderungen an. Nutzt SeasonalPriceModifier#getSeasonReport(),
 * das sich automatisch an die Saisonquelle (Serene Seasons oder interner Kalender) anpasst.
 */
public class SeasonCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("season")
                .executes(SeasonCommand::showSeason)
        );
    }

    private static int showSeason(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.translatable("command.season.header"), false);

        SeasonalPriceModifier modifier = SeasonalPriceModifier.getInstance();
        String[] lines = modifier.getSeasonReport().split("\n", -1);
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            source.sendSuccess(() -> Component.literal(line), false);
        }

        return 1;
    }
}
