package de.rolandsw.schedulemc.tutorial;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Tutorial Command - /tutorial
 *
 * Commands:
 * - /tutorial - Zeigt aktuellen Schritt
 * - /tutorial start - Startet Tutorial
 * - /tutorial next - Geht zum nächsten Schritt
 * - /tutorial skip - Überspringt aktuellen Schritt
 * - /tutorial quit - Beendet Tutorial
 * - /tutorial reset - Setzt Tutorial zurück
 * - /tutorial status - Zeigt Status
 */
public class TutorialCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tutorial")
                .executes(TutorialCommand::showCurrentStep)
                .then(Commands.literal("start")
                    .executes(TutorialCommand::startTutorial))
                .then(Commands.literal("next")
                    .executes(TutorialCommand::nextStep))
                .then(Commands.literal("skip")
                    .executes(TutorialCommand::skipStep))
                .then(Commands.literal("quit")
                    .executes(TutorialCommand::quitTutorial))
                .then(Commands.literal("reset")
                    .executes(TutorialCommand::resetTutorial))
                .then(Commands.literal("status")
                    .executes(TutorialCommand::showStatus))
        );
    }

    /**
     * /tutorial - Zeigt aktuellen Schritt
     */
    private static int showCurrentStep(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§cNur Spieler können diesen Befehl nutzen!"));
            return 0;
        }

        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            context.getSource().sendFailure(Component.literal("§cTutorial-System nicht verfügbar!"));
            return 0;
        }

        manager.showCurrentStep(player);
        return 1;
    }

    /**
     * /tutorial start - Startet Tutorial
     */
    private static int startTutorial(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§cNur Spieler können diesen Befehl nutzen!"));
            return 0;
        }

        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            context.getSource().sendFailure(Component.literal("§cTutorial-System nicht verfügbar!"));
            return 0;
        }

        manager.startTutorial(player);
        return 1;
    }

    /**
     * /tutorial next - Geht zum nächsten Schritt
     */
    private static int nextStep(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§cNur Spieler können diesen Befehl nutzen!"));
            return 0;
        }

        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            context.getSource().sendFailure(Component.literal("§cTutorial-System nicht verfügbar!"));
            return 0;
        }

        manager.completeCurrentStep(player);
        return 1;
    }

    /**
     * /tutorial skip - Überspringt aktuellen Schritt
     */
    private static int skipStep(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§cNur Spieler können diesen Befehl nutzen!"));
            return 0;
        }

        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            context.getSource().sendFailure(Component.literal("§cTutorial-System nicht verfügbar!"));
            return 0;
        }

        manager.skipCurrentStep(player);
        return 1;
    }

    /**
     * /tutorial quit - Beendet Tutorial
     */
    private static int quitTutorial(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§cNur Spieler können diesen Befehl nutzen!"));
            return 0;
        }

        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            context.getSource().sendFailure(Component.literal("§cTutorial-System nicht verfügbar!"));
            return 0;
        }

        manager.quitTutorial(player);
        return 1;
    }

    /**
     * /tutorial reset - Setzt Tutorial zurück
     */
    private static int resetTutorial(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§cNur Spieler können diesen Befehl nutzen!"));
            return 0;
        }

        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            context.getSource().sendFailure(Component.literal("§cTutorial-System nicht verfügbar!"));
            return 0;
        }

        manager.resetTutorial(player);
        return 1;
    }

    /**
     * /tutorial status - Zeigt Status
     */
    private static int showStatus(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§cNur Spieler können diesen Befehl nutzen!"));
            return 0;
        }

        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            context.getSource().sendFailure(Component.literal("§cTutorial-System nicht verfügbar!"));
            return 0;
        }

        manager.showStatus(player);
        return 1;
    }
}
