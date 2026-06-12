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

        mgr.registerTree(buildFearfulTree());
        mgr.registerTree(buildAngryTree());
        mgr.registerTree(buildCitizenTree());
        mgr.registerTree(buildCitizenMissionTree());
        mgr.registerTree(buildMerchantTree());
        mgr.registerTree(buildPoliceTree());
        mgr.registerTree(buildBankTree());
        mgr.registerTree(buildTowTruckTree());
    }

    // ═══════════════════════════════════════════════════════════
    // FEAR / ANGER — degradierte Dialoge mit Versöhnungspfad
    // ═══════════════════════════════════════════════════════════

    /**
     * Hochprioritärer Baum für verängstigte NPCs: statt versteckter Optionen
     * gibt es einen erklärenden Dialog — Aggressoren können sich entschuldigen
     * (Schmerzensgeld oder chance-basiert mit Worten).
     */
    private static DialogueTree buildFearfulTree() {
        return new DialogueTree("fearful_global", "Fearful NPC dialog")
            .addTag("global")
            .startCondition(DialogueCondition.npcIsFearful())
            .priority(20)
            .addConditionalStart(DialogueCondition.playerIsAggressor(), "start_aggressor")
            .addNodes(
                DialogueNode.builder("start_aggressor")
                    .setText("B-bitte... tu mir nicht wieder weh! Was willst du noch von mir?")
                    .addEntryAction(DialogueAction.computeCompensation())
                    .addOption(new DialogueOption("pay", "Es tut mir leid. Hier, {var:compensation}€ Schmerzensgeld.")
                        .targetNode("apology_result")
                        .addAction(DialogueAction.apologizeWithPayment()))
                    .addOption(new DialogueOption("verbal", "Es tut mir wirklich leid. Bitte verzeih mir.")
                        .targetNode("apology_result")
                        .addAction(DialogueAction.apologizeVerbal()))
                    .addOption(DialogueOption.exit("Schon gut, ich gehe.")),
                DialogueNode.builder("apology_result")
                    .setText("...")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_paid"),
                        "*nimmt das Geld zitternd* I-in Ordnung... ich nehme deine Entschuldigung an. Lass uns das vergessen.")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_accepted"),
                        "*atmet tief durch* Also gut... ich glaube dir. Aber tu das nie wieder!")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_rejected"),
                        "Worte! Nur Worte! Das reicht mir nicht...")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_no_money"),
                        "Du hast nicht mal genug Geld für eine ehrliche Entschuldigung...")
                    .addOption(DialogueOption.exit("Bis dann.")),
                DialogueNode.builder("start")
                    .setText("*zittert* E-entschuldige... ich bin gerade etwas durcheinander. Etwas Schreckliches ist passiert...")
                    .addOption(new DialogueOption("comfort", "Ist alles in Ordnung? Ich tue dir nichts.")
                        .targetNode("comforted")
                        .addAction(DialogueAction.calmDown(30f)))
                    .addOption(DialogueOption.exit("Ich lasse dich in Ruhe.")),
                DialogueNode.builder("comforted")
                    .setText("*beruhigt sich langsam* Danke... das hilft. Gib mir nur einen Moment.")
                    .addOption(DialogueOption.exit("Pass auf dich auf."))
            );
    }

    /**
     * Hochprioritärer Baum für wütende NPCs (Intensität > 70).
     */
    private static DialogueTree buildAngryTree() {
        return new DialogueTree("angry_global", "Angry NPC dialog")
            .addTag("global")
            .startCondition(DialogueCondition.npcEmotion(
                de.rolandsw.schedulemc.npc.life.core.EmotionState.ANGRY, 70))
            .priority(20)
            .addConditionalStart(DialogueCondition.playerIsAggressor(), "start_aggressor")
            .addNodes(
                DialogueNode.builder("start_aggressor")
                    .setText("DU schon wieder?! Du hast vielleicht Nerven, dich hier blicken zu lassen!")
                    .addEntryAction(DialogueAction.computeCompensation())
                    .addOption(new DialogueOption("pay", "Ich will es wiedergutmachen: {var:compensation}€ Schmerzensgeld.")
                        .targetNode("apology_result")
                        .addAction(DialogueAction.apologizeWithPayment()))
                    .addOption(new DialogueOption("verbal", "Es tut mir aufrichtig leid.")
                        .targetNode("apology_result")
                        .addAction(DialogueAction.apologizeVerbal()))
                    .addOption(DialogueOption.exit("Ich gehe ja schon.")),
                DialogueNode.builder("apology_result")
                    .setText("...")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_paid"),
                        "*zählt das Geld nach* Hmpf. Damit sind wir quitt. Aber merk dir das!")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_accepted"),
                        "*knurrt* Na schön. Einmal noch. EINMAL.")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_rejected"),
                        "Glaubst du, ein paar nette Worte machen das wieder gut?!")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_no_money"),
                        "Nicht mal Geld hast du! Verschwinde!")
                    .addOption(DialogueOption.exit("Verstanden.")),
                DialogueNode.builder("start")
                    .setText("*wütend* Was?! Ich habe gerade WIRKLICH keine Geduld!")
                    .addOption(new DialogueOption("calm", "Ganz ruhig... was ist denn passiert?")
                        .targetNode("vented")
                        .addAction(DialogueAction.calmDown(25f)))
                    .addOption(DialogueOption.exit("Schon gut, schon gut.")),
                DialogueNode.builder("vented")
                    .setText("*schnaubt* ...Entschuldige. Es war ein furchtbarer Tag. Was willst du?")
                    .addOption(DialogueOption.exit("Ich komme später wieder."))
            );
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
