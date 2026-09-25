package de.rolandsw.schedulemc.managers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command: /tutorial skip
 *
 * Erlaubt Spielern, das Onboarding-Tutorial ({@link TutorialManager}) zu ueberspringen.
 */
public class TutorialCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tutorial")
                .then(Commands.literal("skip")
                    .executes(TutorialCommand::skip)
                )
        );
    }

    private static int skip(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            source.sendFailure(Component.literal("Tutorial system is not available."));
            return 0;
        }

        manager.skipTutorial(player);
        return 1;
    }
}
