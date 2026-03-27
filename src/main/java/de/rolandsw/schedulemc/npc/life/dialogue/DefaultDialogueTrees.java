package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.mission.MissionStatus;
import de.rolandsw.schedulemc.npc.data.NPCType;

/**
 * DefaultDialogueTrees - Standard-Dialogbäume für alle NPC-Typen
 *
 * Wird beim Server-Start einmal aufgerufen und registriert globale Bäume
 * mit Start-Bedingungen je nach NPC-Typ.
 */
public class DefaultDialogueTrees {

    /**
     * Registriert alle Standard-Dialogbäume im DialogueManager.
     * Aufruf nach DialogueManager.initialize().
     */
    public static void registerAll(DialogueManager mgr) {
        if (mgr == null) return;

        mgr.registerTree(buildBewohnerTree());
        mgr.registerTree(buildBewohnerMissionTree());
        mgr.registerTree(buildVerkaeuferTree());
        mgr.registerTree(buildPolizeiTree());
        mgr.registerTree(buildBankTree());
        mgr.registerTree(buildAbschlepperTree());
    }

    // ═══════════════════════════════════════════════════════════
    // BEWOHNER
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildBewohnerTree() {
        return new DialogueTree("bewohner_global", "Bewohner Standard-Dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.BEWOHNER))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "Hallo, {player_name}! Schön dich zu sehen.",
                    new DialogueOption("ask_wellbeing", "Wie geht es dir?")
                        .targetNode("wellbeing")
                        .addAction(DialogueAction.modifyNPCFactionReputation(2)),
                    DialogueOption.exit("Schönen Tag noch!")
                        .addAction(DialogueAction.modifyNPCFactionReputation(1))
                ),
                DialogueNode.simple("wellbeing",
                    "Danke, mir geht es gut! Das Wetter ist herrlich heute.",
                    new DialogueOption("nice", "Das freut mich!")
                        .targetNode(null)
                        .addAction(DialogueAction.endDialogue())
                        .addAction(DialogueAction.modifyNPCFactionReputation(3)),
                    DialogueOption.exit("Na dann, tschüss.")
                )
            );
    }

    /**
     * Höherprioritäter Baum wenn BEWOHNER Missionen in seiner NPCData hat.
     */
    private static DialogueTree buildBewohnerMissionTree() {
        String missionId = "neben_handel_01";
        return new DialogueTree("bewohner_mission_global", "Bewohner Mission-Dialog")
            .addTag("global")
            .startCondition(DialogueCondition.and(
                npcTypeIs(NPCType.BEWOHNER),
                DialogueCondition.npcHasMission(missionId)
            ))
            .priority(10)
            .addConditionalStart(
                DialogueCondition.hasMissionStatus(missionId, MissionStatus.COMPLETED),
                "start_claim"
            )
            .addConditionalStart(
                DialogueCondition.hasMissionStatus(missionId, MissionStatus.ACTIVE),
                "start_active"
            )
            .addNodes(
                // Default start: Mission noch nicht angenommen
                DialogueNode.simple("start",
                    "Hallo, {player_name}! Ich brauche dringend deine Hilfe.",
                    new DialogueOption("ask_mission", "Was ist das Problem?")
                        .targetNode("offer_mission"),
                    DialogueOption.exit("Schönen Tag noch.")
                ),
                // Mission läuft noch
                DialogueNode.simple("start_active",
                    "Bist du schon fertig? Ich warte auf dich!",
                    DialogueOption.exit("Ich bin noch dabei.")
                ),
                // Mission abgeschlossen → Belohnung abholen
                DialogueNode.simple("start_claim",
                    "Oh, du bist zurück! Hast du alles erledigt?",
                    new DialogueOption("claim", "Ja, ich habe alles erledigt!")
                        .targetNode("reward")
                        .addAction(DialogueAction.claimMissionReward(missionId))
                        .addAction(DialogueAction.modifyNPCFactionReputation(20)),
                    DialogueOption.exit("Noch nicht ganz fertig.")
                ),
                // Mission anbieten
                DialogueNode.simple("offer_mission",
                    "Ich brauche Hilfe mit einem Auftrag. Kannst du das für mich übernehmen?",
                    new DialogueOption("accept", "Klar, ich mache das!")
                        .targetNode("accepted")
                        .addAction(DialogueAction.giveMission(missionId))
                        .addAction(DialogueAction.modifyNPCFactionReputation(5)),
                    DialogueOption.exit("Nein danke, nicht jetzt.")
                ),
                DialogueNode.end("accepted",
                    "Wunderbar! Ich zähle auf dich. Viel Erfolg!"
                ),
                DialogueNode.end("reward",
                    "Großartig! Hier ist deine Belohnung. Du hast das prima gemacht!"
                )
            );
    }

    // ═══════════════════════════════════════════════════════════
    // VERKAEUFER
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildVerkaeuferTree() {
        return new DialogueTree("verkaeufer_global", "Verkäufer Standard-Dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.VERKAEUFER))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "Willkommen! Was darf es sein?",
                    new DialogueOption("buy", "Ich möchte etwas kaufen.")
                        .targetNode(null)
                        .addAction(DialogueAction.openTradeMenu()),
                    new DialogueOption("sell", "Ich möchte verkaufen.")
                        .targetNode("sell_node"),
                    DialogueOption.exit("Nur schauen, danke.")
                ),
                DialogueNode.simple("sell_node",
                    "Was haben Sie anzubieten?",
                    new DialogueOption("open_sell", "Hier bitte.")
                        .targetNode(null)
                        .addAction(DialogueAction.startNegotiation()),
                    DialogueOption.exit("Doch nichts, danke.")
                )
            );
    }

    // ═══════════════════════════════════════════════════════════
    // POLIZEI
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildPolizeiTree() {
        return new DialogueTree("polizei_global", "Polizei Standard-Dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.POLIZEI))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "Guten Tag, Bürger. Gibt es ein Problem?",
                    new DialogueOption("no_problem", "Alles gut, danke.")
                        .targetNode(null)
                        .addAction(DialogueAction.endDialogue()),
                    new DialogueOption("report", "Ich möchte etwas melden.")
                        .targetNode("report_node")
                ),
                DialogueNode.simple("report_node",
                    "Ich nehme das zu Protokoll. Was möchten Sie melden?",
                    new DialogueOption("crime", "Ein Verbrechen wurde begangen.")
                        .targetNode("noted")
                        .addAction(DialogueAction.addMemory(
                            de.rolandsw.schedulemc.npc.life.core.MemoryType.CRIME_WITNESSED,
                            "Spieler hat Verbrechen gemeldet", 3)),
                    DialogueOption.exit("Vergessen Sie es.")
                ),
                DialogueNode.end("noted",
                    "Vielen Dank für die Meldung. Wir werden dem nachgehen."
                )
            );
    }

    // ═══════════════════════════════════════════════════════════
    // BANK
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildBankTree() {
        return new DialogueTree("bank_global", "Banker Standard-Dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.BANK))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "Guten Tag! Wie kann ich Ihnen dienen?",
                    new DialogueOption("deposit", "Ich möchte einzahlen.")
                        .targetNode(null)
                        .addAction(DialogueAction.endDialogue()),
                    new DialogueOption("withdraw", "Ich möchte abheben.")
                        .targetNode(null)
                        .addAction(DialogueAction.endDialogue()),
                    DialogueOption.exit("Auf Wiedersehen.")
                )
            );
    }

    // ═══════════════════════════════════════════════════════════
    // ABSCHLEPPER
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildAbschlepperTree() {
        return new DialogueTree("abschlepper_global", "Abschlepper Standard-Dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.ABSCHLEPPER))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "Was kann ich für Sie tun?",
                    new DialogueOption("vehicle", "Ich brauche Hilfe mit einem Fahrzeug.")
                        .targetNode(null)
                        .addAction(DialogueAction.openTradeMenu()),
                    DialogueOption.exit("Nichts, danke.")
                )
            );
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════

    private static DialogueCondition npcTypeIs(NPCType type) {
        return new DialogueCondition(
            "npc_type_" + type.name().toLowerCase(),
            "NPC-Typ ist " + type.name(),
            (ctx, npc) -> npc.getNpcType() == type
        );
    }
}
