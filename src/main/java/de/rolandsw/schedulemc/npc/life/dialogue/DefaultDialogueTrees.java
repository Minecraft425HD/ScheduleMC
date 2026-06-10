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

        mgr.registerTree(buildCitizenTree());
        mgr.registerTree(buildCitizenMissionTree());
        mgr.registerTree(buildMerchantTree());
        mgr.registerTree(buildPoliceTree());
        mgr.registerTree(buildBankTree());
        mgr.registerTree(buildTowTruckTree());
    }

    // ═══════════════════════════════════════════════════════════
    // CITIZEN
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildCitizenTree() {
        return new DialogueTree("citizen_global", "Bewohner Standard-Dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.CITIZEN))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "Hello, {player_name}! Good to see you.",
                    new DialogueOption("ask_wellbeing", "Wie geht es dir?")
                        .targetNode("wellbeing")
                        .addAction(DialogueAction.modifyNPCFactionReputation(2)),
                    DialogueOption.exit("Have a nice day!")
                        .addAction(DialogueAction.modifyNPCFactionReputation(1))
                ),
                DialogueNode.simple("wellbeing",
                    "Danke, mir geht es gut! Das Wetter ist herrlich heute.",
                    new DialogueOption("nice", "Das freut mich!")
                        .targetNode(null)
                        .addAction(DialogueAction.endDialogue())
                        .addAction(DialogueAction.modifyNPCFactionReputation(3)),
                    DialogueOption.exit("Well then, goodbye.")
                )
            );
    }

    /**
     * Höherprioritäter Baum wenn CITIZEN Missionen in seiner NPCData hat.
     */
    private static DialogueTree buildCitizenMissionTree() {
        String missionId = "neben_handel_01";
        return new DialogueTree("citizen_mission_global", "Bewohner Mission-Dialog")
            .addTag("global")
            .startCondition(DialogueCondition.and(
                npcTypeIs(NPCType.CITIZEN),
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
                    DialogueOption.exit("Have a nice day.")
                ),
                // Mission läuft noch
                DialogueNode.simple("start_active",
                    "Bist du schon fertig? Ich warte auf dich!",
                    DialogueOption.exit("Ich bin noch dabei.")
                ),
                // Mission abgeschlossen → Belohnung abholen
                DialogueNode.simple("start_claim",
                    "Oh, you're back! Did you get everything done?",
                    new DialogueOption("claim", "Ja, ich habe alles erledigt!")
                        .targetNode("reward")
                        .addAction(DialogueAction.claimMissionReward(missionId))
                        .addAction(DialogueAction.modifyNPCFactionReputation(20)),
                    DialogueOption.exit("Not quite done yet.")
                ),
                // Mission anbieten
                DialogueNode.simple("offer_mission",
                    "I need help with a job. Can you take care of it for me?",
                    new DialogueOption("accept", "Klar, ich mache das!")
                        .targetNode("accepted")
                        .addAction(DialogueAction.giveMission(missionId))
                        .addAction(DialogueAction.modifyNPCFactionReputation(5)),
                    DialogueOption.exit("No thanks, not now.")
                ),
                DialogueNode.end("accepted",
                    "Wonderful! I'm counting on you. Good luck!"
                ),
                DialogueNode.end("reward",
                    "Great! Here is your reward. You did a fine job!"
                )
            );
    }

    // ═══════════════════════════════════════════════════════════
    // MERCHANT
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildMerchantTree() {
        return new DialogueTree("merchant_global", "Merchant default dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.MERCHANT))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "Willkommen! Was darf es sein?",
                    new DialogueOption("buy", "I want to buy something.")
                        .targetNode(null)
                        .addAction(DialogueAction.openTradeMenu()),
                    new DialogueOption("sell", "I want to sell.")
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
    // POLICE
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildPoliceTree() {
        return new DialogueTree("police_global", "Polizei Standard-Dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.POLICE))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "Good day, citizen. Is there a problem?",
                    new DialogueOption("no_problem", "Alles gut, danke.")
                        .targetNode(null)
                        .addAction(DialogueAction.endDialogue()),
                    new DialogueOption("report", "I want to report something.")
                        .targetNode("report_node")
                ),
                DialogueNode.simple("report_node",
                    "I'll take this down. What would you like to report?",
                    new DialogueOption("crime", "A crime has been committed.")
                        .targetNode("noted")
                        .addAction(DialogueAction.addMemory(
                            de.rolandsw.schedulemc.npc.life.core.MemoryType.CRIME_WITNESSED,
                            "Spieler hat Verbrechen gemeldet", 3)),
                    DialogueOption.exit("Vergessen Sie es.")
                ),
                DialogueNode.end("noted",
                    "Thank you for the report. We will look into it."
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
                    "Good day! How can I serve you?",
                    new DialogueOption("deposit", "I want to deposit.")
                        .targetNode(null)
                        .addAction(DialogueAction.endDialogue()),
                    new DialogueOption("withdraw", "I want to withdraw.")
                        .targetNode(null)
                        .addAction(DialogueAction.endDialogue()),
                    DialogueOption.exit("Auf Wiedersehen.")
                )
            );
    }

    // ═══════════════════════════════════════════════════════════
    // TOW_TRUCK_DRIVER
    // ═══════════════════════════════════════════════════════════

    private static DialogueTree buildTowTruckTree() {
        return new DialogueTree("tow_truck_driver_global", "Tow truck driver default dialog")
            .addTag("global")
            .startCondition(npcTypeIs(NPCType.TOW_TRUCK_DRIVER))
            .priority(5)
            .addNodes(
                DialogueNode.simple("start",
                    "What can I do for you?",
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
