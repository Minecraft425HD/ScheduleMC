package de.rolandsw.schedulemc.npc.crime;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.npc.crime.evidence.Evidence;
import de.rolandsw.schedulemc.npc.crime.evidence.EvidenceManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Command: /crimerecord <player>
 *
 * Zeigt die Crime-Historie und Beweise fuer einen Spieler.
 * Nur fuer Polizei-Beamte (OP Level 2) verfuegbar.
 */
public class CrimeRecordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("crimerecord")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(CrimeRecordCommand::showCrimeRecord)
                    .then(Commands.literal("evidence")
                        .executes(CrimeRecordCommand::showEvidence)
                    )
                    .then(Commands.literal("clear")
                        .executes(CrimeRecordCommand::clearRecord)
                    )
                )
        );
    }

    private static int showCrimeRecord(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        CommandSourceStack source = context.getSource();

        List<CrimeRecord> records = CrimeManager.getCrimeHistory(target.getUUID());
        int wantedLevel = CrimeManager.getWantedLevel(target.getUUID());

        source.sendSuccess(() ->
            Component.literal("\u00A76\u2550\u2550\u2550 Crime Record: " + target.getName().getString() + " \u2550\u2550\u2550"),
            false);
        source.sendSuccess(() ->
            Component.literal("\u00A77Wanted Level: \u00A7c" + "\u2B50".repeat(wantedLevel) + " (" + wantedLevel + "/5)"),
            false);
        source.sendSuccess(() ->
            Component.literal("\u00A77Verbrechen gesamt: \u00A7e" + records.size()),
            false);
        source.sendSuccess(() ->
            Component.literal("\u00A77Offene Strafen: \u00A7c" + CrimeManager.getUnservedCrimeCount(target.getUUID())),
            false);

        if (records.isEmpty()) {
            source.sendSuccess(() -> Component.literal("\u00A7aKeine Verbrechen registriert."), false);
        } else {
            // Letzte 10 Verbrechen anzeigen
            int start = Math.max(0, records.size() - 10);
            source.sendSuccess(() -> Component.literal("\u00A77\u2500 Letzte Verbrechen:"), false);
            for (int i = start; i < records.size(); i++) {
                CrimeRecord record = records.get(i);
                final String desc = record.getFormattedDescription();
                source.sendSuccess(() -> Component.literal(desc), false);
            }
        }

        return 1;
    }

    private static int showEvidence(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        CommandSourceStack source = context.getSource();

        EvidenceManager evidenceManager = EvidenceManager.getInstance();
        if (evidenceManager == null) {
            source.sendFailure(Component.literal("\u00A7cEvidenceManager nicht verfuegbar!"));
            return 0;
        }

        List<Evidence> evidenceList = evidenceManager.getEvidence(target.getUUID());
        float strength = evidenceManager.calculateEvidenceStrength(target.getUUID());
        float multiplier = evidenceManager.calculateSentenceMultiplier(target.getUUID());

        source.sendSuccess(() ->
            Component.literal("\u00A76\u2550\u2550\u2550 Beweise: " + target.getName().getString() + " \u2550\u2550\u2550"),
            false);
        source.sendSuccess(() ->
            Component.literal("\u00A77Beweisstaerke: \u00A7e" + String.format("%.0f%%", strength * 100)),
            false);
        source.sendSuccess(() ->
            Component.literal("\u00A77Strafmultiplikator: \u00A7c" + String.format("%.1fx", multiplier)),
            false);
        source.sendSuccess(() ->
            Component.literal("\u00A77Beweise: \u00A7e" + evidenceList.size()),
            false);

        for (Evidence evidence : evidenceList) {
            source.sendSuccess(() ->
                Component.literal(String.format("\u00A77  - [%s] %s \u00A78(%.0f%% zuverlaessig)",
                    evidence.getType().name(),
                    evidence.getDescription(),
                    evidence.getReliability() * 100)),
                false);
        }

        return 1;
    }

    private static int clearRecord(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        CommandSourceStack source = context.getSource();

        CrimeManager.clearWantedLevel(target.getUUID());

        EvidenceManager evidenceManager = EvidenceManager.getInstance();
        if (evidenceManager != null) {
            evidenceManager.clearEvidence(target.getUUID());
        }

        source.sendSuccess(() ->
            Component.literal("\u00A7aCrime Record und Beweise fuer " + target.getName().getString() + " geloescht."),
            false);

        return 1;
    }
}
