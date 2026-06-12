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

        mgr.registerTree(buildSupplyRequestTree());
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
    /**
     * Offene Warenanfrage: NPC trägt sein Anliegen vor, Spieler kann
     * annehmen (-> SUPPLY-Quest), ablehnen oder vertagen.
     */
    private static DialogueTree buildSupplyRequestTree() {
        return new DialogueTree("supply_request_global", "NPC asks for goods")
            .addTag("global")
            .startCondition(DialogueCondition.hasPendingSupplyRequest())
            .priority(15)
            .addNodes(
                DialogueNode.builder("start")
                    .setText("Good to see you, {player_name}! I need {var:supply_amount}x {var:supply_item}. "
                        + "Bring them to the meeting point ({var:supply_meeting}) — I will pay you {var:supply_payment}€. Are you in?")
                    .addEntryAction(DialogueAction.describeSupplyRequest())
                    .addOption(new DialogueOption("accept", "Sure, I will do it!")
                        .targetNode("supply_result")
                        .addAction(DialogueAction.acceptSupplyRequest()))
                    .addOption(new DialogueOption("decline", "No, that does not suit me right now.")
                        .targetNode("supply_result")
                        .addAction(DialogueAction.declineSupplyRequest()))
                    .addOption(DialogueOption.exit("I will think about it — see you later."))
                    .build(),
                DialogueNode.builder("supply_result")
                    .setText("...")
                    .addConditionalText(DialogueCondition.contextFlagSet("supply_accepted"),
                        "Wonderful! I will wait for you at the meeting point. Do not let me down!")
                    .addConditionalText(DialogueCondition.contextFlagSet("supply_declined"),
                        "Too bad... all right, maybe another time.")
                    .addOption(DialogueOption.exit("See you."))
                    .build()
            );
    }

    private static DialogueTree buildFearfulTree() {
        return new DialogueTree("fearful_global", "Fearful NPC dialog")
            .addTag("global")
            .startCondition(DialogueCondition.npcIsFearful())
            .priority(20)
            .addConditionalStart(DialogueCondition.playerIsAggressor(), "start_aggressor")
            .addNodes(
                DialogueNode.builder("start_aggressor")
                    .setText("P-please... do not hurt me again! What more do you want from me?")
                    .addEntryAction(DialogueAction.computeCompensation())
                    .addOption(new DialogueOption("pay", "I am sorry. Here, {var:compensation}€ compensation.")
                        .targetNode("apology_result")
                        .addAction(DialogueAction.apologizeWithPayment()))
                    .addOption(new DialogueOption("verbal", "I am truly sorry. Please forgive me.")
                        .targetNode("apology_result")
                        .addAction(DialogueAction.apologizeVerbal()))
                    .addOption(DialogueOption.exit("All right, I am leaving.")),
                DialogueNode.builder("apology_result")
                    .setText("...")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_paid"),
                        "*takes the money, trembling* A-all right... I accept your apology. Let us forget about it.")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_accepted"),
                        "*takes a deep breath* All right... I believe you. But never do that again!")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_rejected"),
                        "Words! Just words! That is not enough for me...")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_no_money"),
                        "You do not even have enough money for an honest apology...")
                    .addOption(DialogueOption.exit("See you.")),
                DialogueNode.builder("start")
                    .setText("*trembles* S-sorry... I am a bit shaken right now. Something terrible happened...")
                    .addOption(new DialogueOption("comfort", "Is everything all right? I will not hurt you.")
                        .targetNode("comforted")
                        .addAction(DialogueAction.calmDown(30f)))
                    .addOption(DialogueOption.exit("I will leave you alone.")),
                DialogueNode.builder("comforted")
                    .setText("*slowly calms down* Thank you... that helps. Just give me a moment.")
                    .addOption(DialogueOption.exit("Take care of yourself."))
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
                    .setText("YOU again?! You have some nerve showing your face here!")
                    .addEntryAction(DialogueAction.computeCompensation())
                    .addOption(new DialogueOption("pay", "I want to make it right: {var:compensation}€ compensation.")
                        .targetNode("apology_result")
                        .addAction(DialogueAction.apologizeWithPayment()))
                    .addOption(new DialogueOption("verbal", "I am sincerely sorry.")
                        .targetNode("apology_result")
                        .addAction(DialogueAction.apologizeVerbal()))
                    .addOption(DialogueOption.exit("I am leaving already.")),
                DialogueNode.builder("apology_result")
                    .setText("...")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_paid"),
                        "*counts the money* Hmpf. Now we are even. But remember this!")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_accepted"),
                        "*growls* Fine. One more time. ONCE.")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_rejected"),
                        "You think a few nice words will make up for that?!")
                    .addConditionalText(DialogueCondition.contextFlagSet("reconciliation_no_money"),
                        "You do not even have money! Get lost!")
                    .addOption(DialogueOption.exit("Verstanden.")),
                DialogueNode.builder("start")
                    .setText("*furious* What?! I REALLY have no patience right now!")
                    .addOption(new DialogueOption("calm", "Easy now... what happened?")
                        .targetNode("vented")
                        .addAction(DialogueAction.calmDown(25f)))
                    .addOption(DialogueOption.exit("All right, all right.")),
                DialogueNode.builder("vented")
                    .setText("*snorts* ...Sorry. It has been a terrible day. What do you want?")
                    .addOption(DialogueOption.exit("I will come back later."))
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
                    new DialogueOption("ask_wellbeing", "How are you?")
                        .targetNode("wellbeing")
                        .addAction(DialogueAction.modifyNPCFactionReputation(2)),
                    DialogueOption.exit("Have a nice day!")
                        .addAction(DialogueAction.modifyNPCFactionReputation(1))
                ),
                DialogueNode.simple("wellbeing",
                    "Thanks, I am doing well! The weather is lovely today.",
                    new DialogueOption("nice", "Glad to hear it!")
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
                    "Hello, {player_name}! I urgently need your help.",
                    new DialogueOption("ask_mission", "What is the problem?")
                        .targetNode("offer_mission"),
                    DialogueOption.exit("Have a nice day.")
                ),
                // Mission läuft noch
                DialogueNode.simple("start_active",
                    "Are you done yet? I am waiting for you!",
                    DialogueOption.exit("I am still working on it.")
                ),
                // Mission abgeschlossen → Belohnung abholen
                DialogueNode.simple("start_claim",
                    "Oh, you're back! Did you get everything done?",
                    new DialogueOption("claim", "Yes, I have taken care of everything!")
                        .targetNode("reward")
                        .addAction(DialogueAction.claimMissionReward(missionId))
                        .addAction(DialogueAction.modifyNPCFactionReputation(20)),
                    DialogueOption.exit("Not quite done yet.")
                ),
                // Mission anbieten
                DialogueNode.simple("offer_mission",
                    "I need help with a job. Can you take care of it for me?",
                    new DialogueOption("accept", "Sure, I will do it!")
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
                DialogueNode.builder("start")
                    .setText("Welcome! What can I get you?")
                    .addConditionalText(DialogueCondition.relationshipAtLeast(50),
                        "{player_name}, my best customer! I always have time for you — and the best prices.")
                    .addConditionalText(DialogueCondition.relationshipAtLeast(10),
                        "Ah, {player_name}! Good to see you again. What will it be today?")
                    .addConditionalText(DialogueCondition.relationshipBelow(-10),
                        "*eyes you coldly* What do you want? Payment up front.")
                    .addConditionalText(DialogueCondition.npcTraitAbove("greed", 50),
                        "Time is money. What do you want to buy?")
                    .addConditionalText(DialogueCondition.npcTraitBelow("greed", -50),
                        "Welcome, welcome! For good customers I am happy to make a good price!")
                    .addOption(new DialogueOption("buy", "I want to buy something.")
                        .targetNode(null)
                        .addAction(DialogueAction.openTradeMenu()))
                    .addOption(new DialogueOption("sell", "I want to sell.")
                        .targetNode("sell_node"))
                    .addOption(new DialogueOption("haggle", "Can we negotiate the prices?")
                        .visibleWhen(DialogueCondition.npcTraitAbove("greed", 40))
                        .targetNode(null)
                        .addAction(DialogueAction.startNegotiation()))
                    .addOption(new DialogueOption("friend_discount", "Do you have something for an old friend?")
                        .visibleWhen(DialogueCondition.and(
                            DialogueCondition.relationshipAtLeast(25),
                            DialogueCondition.npcTraitBelow("greed", -40)))
                        .targetNode("discount_given")
                        .addAction(DialogueAction.giveTempDiscount(0.1f, 30)))
                    .addOption(new DialogueOption("why_prices", "Warum diese Preise?")
                        .targetNode("price_breakdown")
                        .addAction(DialogueAction.explainPrices()))
                    .addOption(DialogueOption.exit("Just looking, thanks.")),
                DialogueNode.builder("discount_given")
                    .setText("*winks* For you? 10% discount for the next half hour. But do not spread the word!")
                    .addOption(DialogueOption.exit("Thank you!")),
                DialogueNode.builder("price_breakdown")
                    .setText("{var:price_breakdown}")
                    .addOption(DialogueOption.exit("I see. Thanks.")),
                DialogueNode.simple("sell_node",
                    "What do you have to offer?",
                    new DialogueOption("open_sell", "Here you go.")
                        .targetNode(null)
                        .addAction(DialogueAction.startNegotiation()),
                    DialogueOption.exit("Nothing after all, thanks.")
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
                    new DialogueOption("no_problem", "All good, thanks.")
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
                            "Player reported a crime", 3)),
                    DialogueOption.exit("Forget it.")
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
                    DialogueOption.exit("Goodbye.")
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
                    new DialogueOption("vehicle", "I need help with a vehicle.")
                        .targetNode(null)
                        .addAction(DialogueAction.openTradeMenu()),
                    DialogueOption.exit("Nothing, thanks.")
                )
            );
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════

    private static DialogueCondition npcTypeIs(NPCType type) {
        return new DialogueCondition(
            "npc_type_" + type.name().toLowerCase(),
            "NPC type is " + type.name(),
            (ctx, npc) -> npc.getNpcType() == type
        );
    }
}
