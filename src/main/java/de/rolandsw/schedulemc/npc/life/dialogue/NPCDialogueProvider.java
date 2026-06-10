package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPCDialogueProvider - Stellt vorgefertigte Dialogbäume für verschiedene NPC-Typen bereit
 *
 * Enthält:
 * - Standard-Dialoge für jeden NPC-Typ
 * - Spezial-Dialoge für bestimmte Situationen
 * - Dynamische Dialog-Generierung
 */
public class NPCDialogueProvider {

    // ═══════════════════════════════════════════════════════════
    // STATIC INITIALIZATION
    // ═══════════════════════════════════════════════════════════

    private static final Map<NPCType, DialogueTree> TYPE_DIALOGUES = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;

    /**
     * Initialisiert alle Standard-Dialogbäume
     */
    public static void initialize() {
        if (initialized) return;

        // Händler-Dialoge
        registerMerchantDialogues();

        // Polizei-Dialoge
        registerPoliceDialogues();

        // Bürger-Dialoge
        registerCitizenDialogues();

        // Untergrund-Dialoge
        registerUnderworldDialogues();

        initialized = true;
    }

    /**
     * Registriert alle Dialogbäume bei einem DialogueManager
     */
    public static void registerAllTrees(DialogueManager manager) {
        initialize();
        for (DialogueTree tree : TYPE_DIALOGUES.values()) {
            manager.registerTree(tree);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MERCHANT DIALOGUES
    // ═══════════════════════════════════════════════════════════

    private static void registerMerchantDialogues() {
        // Allgemeiner Händler-Dialog
        DialogueTree merchantGeneral = new DialogueTree("merchant_general", "Merchant dialog")
            .setPriority(10)
            .addTag("merchant")

            // Start-Node
            .addNode(DialogueNode.builder("start")
                .addText("Welcome to my shop, {player}! What can I do for you?")
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Stammkunde"),
                    "Ah, {player}! Mein treuer Stammkunde! Was darf es heute sein?"
                )
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Dieb"),
                    "Sie schon wieder... Ich behalte Sie im Auge."
                )
                .addOption(DialogueOption.simple("browse", "I want to see your offer.", "show_wares"))
                .addOption(DialogueOption.simple("sell", "Ich habe etwas zu verkaufen.", "buy_from_player"))
                .addOption(DialogueOption.simple("info", "Tell me something about yourself.", "merchant_info"))
                .addOption(DialogueOption.builder("negotiate")
                    .setText("Can we negotiate the prices?")
                    .setTargetNode("negotiate_start")
                    .addCondition(DialogueCondition.factionStanding(Faction.TRADERS, 20))
                    .build())
                .addOption(DialogueOption.exit("Auf Wiedersehen."))
                .build())

            // Waren zeigen
            .addNode(DialogueNode.builder("show_wares")
                .addText("Of course! Here is my current assortment.")
                .addEntryAction(DialogueAction.openTrade())
                .setEndNode(true)
                .build())

            // Vom Spieler kaufen
            .addNode(DialogueNode.builder("buy_from_player")
                .addText("Let's see what you have... Yes, I could use that.")
                .addOption(DialogueOption.simple("back", "Maybe later.", "start"))
                .addOption(DialogueOption.trade("Zeigen Sie mir Ihre Preise."))
                .build())

            // Info über Händler
            .addNode(DialogueNode.builder("merchant_info")
                .addText("I've been in business for years. My family has traded here for generations.")
                .addOption(DialogueOption.simple("rumors", "Have you heard any interesting news?", "share_rumors"))
                .addOption(DialogueOption.simple("back", "Interesting. Back to business.", "start"))
                .build())

            // Gerüchte teilen
            .addNode(DialogueNode.builder("share_rumors")
                .addText("Well, you hear things... But that stays between us, understood?")
                .addEntryAction(DialogueAction.shareRumors())
                .addOption(DialogueOption.simple("back", "Of course. Thank you for the information.", "start"))
                .build())

            // Verhandlung starten
            .addNode(DialogueNode.builder("negotiate_start")
                .addText("Negotiate? Well, for a good customer like you... what do you have in mind?")
                .addOption(DialogueOption.builder("negotiate_friendly")
                    .setText("Wir kennen uns doch schon lange. Ein kleiner Rabatt?")
                    .setTargetNode("negotiate_result")
                    .addAction(DialogueAction.setVariable("negotiate_tactic", "friendly"))
                    .build())
                .addOption(DialogueOption.builder("negotiate_pressure")
                    .setText("I could also shop elsewhere...")
                    .setTargetNode("negotiate_result")
                    .addAction(DialogueAction.setVariable("negotiate_tactic", "pressure"))
                    .build())
                .addOption(DialogueOption.simple("back", "Vergessen Sie es.", "start"))
                .build())

            // Verhandlungsergebnis
            .addNode(DialogueNode.builder("negotiate_result")
                .addText("Hmm... Let me think about it.")
                .addEntryAction(DialogueAction.startNegotiation())
                .addOption(DialogueOption.simple("back", "Also, was sagen Sie?", "start"))
                .build());

        TYPE_DIALOGUES.put(NPCType.MERCHANT, merchantGeneral);
    }

    // ═══════════════════════════════════════════════════════════
    // POLICE DIALOGUES
    // ═══════════════════════════════════════════════════════════

    private static void registerPoliceDialogues() {
        DialogueTree policeGeneral = new DialogueTree("police_general", "Polizei-Dialog")
            .setPriority(10)
            .addTag("police")

            // Start - Normal
            .addNode(DialogueNode.builder("start")
                .addText("Good day, citizen. Can I help you?")
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Gesucht"),
                    "Halt! Sie sind zur Fahndung ausgeschrieben! Keine Bewegung!"
                )
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Kriminell"),
                    "Sie... Ich habe Sie im Auge. Machen Sie keinen Unsinn."
                )
                .addOption(DialogueOption.builder("wanted_response")
                    .setText("Wait, I can explain!")
                    .setTargetNode("wanted_explain")
                    .addCondition(DialogueCondition.hasPlayerTag("Gesucht"))
                    .build())
                .addOption(DialogueOption.builder("wanted_flee")
                    .setText("[Fliehen]")
                    .setTargetNode("wanted_flee")
                    .addCondition(DialogueCondition.hasPlayerTag("Gesucht"))
                    .build())
                .addOption(DialogueOption.simple("report", "I want to report a crime.", "report_crime"))
                .addOption(DialogueOption.simple("info", "Was gibt es Neues in der Stadt?", "police_info"))
                .addOption(DialogueOption.simple("bribe", "Maybe I can... help you?", "bribe_attempt"))
                .addOption(DialogueOption.exit("Auf Wiedersehen, Offizier."))
                .build())

            // Gesucht - Erklären
            .addNode(DialogueNode.builder("wanted_explain")
                .addText("Explain? You can do that in court. Hands up!")
                .addOption(DialogueOption.builder("surrender")
                    .setText("[Ergeben]")
                    .addAction(DialogueAction.triggerEmotion(EmotionState.FEARFUL, 40))
                    .addAction(DialogueAction.modifyFaction(Faction.LAW, 5))
                    .addAction(DialogueAction.removePlayerTag("Gesucht"))
                    .setEndNode(true)
                    .build())
                .addOption(DialogueOption.builder("resist")
                    .setText("[Widerstand leisten]")
                    .setTargetNode("resist_arrest")
                    .build())
                .build())

            // Flucht
            .addNode(DialogueNode.builder("wanted_flee")
                .addText("Halt! Stehen bleiben!")
                .addEntryAction(DialogueAction.alertNearbyNPCs("criminal"))
                .setEndNode(true)
                .build())

            // Widerstand
            .addNode(DialogueNode.builder("resist_arrest")
                .addText("Resisting law enforcement? That will cost you dearly!")
                .addEntryAction(DialogueAction.alertNearbyNPCs("dangerous"))
                .addEntryAction(DialogueAction.modifyFaction(Faction.LAW, -20))
                .setEndNode(true)
                .build())

            // Verbrechen melden
            .addNode(DialogueNode.builder("report_crime")
                .addText("A crime? Tell me what happened.")
                .addOption(DialogueOption.simple("report_theft", "I was robbed.", "crime_reported"))
                .addOption(DialogueOption.simple("report_assault", "I was attacked.", "crime_reported"))
                .addOption(DialogueOption.simple("report_suspicious", "I observed suspicious activities.", "crime_reported"))
                .addOption(DialogueOption.simple("back", "Eigentlich... vergessen Sie es.", "start"))
                .build())

            // Verbrechen gemeldet
            .addNode(DialogueNode.builder("crime_reported")
                .addText("Thank you for the report. We will look into the matter.")
                .addEntryAction(DialogueAction.modifyFaction(Faction.LAW, 5))
                .addEntryAction(DialogueAction.addPlayerTag("Hilfreich"))
                .addOption(DialogueOption.exit("Danke, Offizier."))
                .build())

            // Polizei-Info
            .addNode(DialogueNode.builder("police_info")
                .addText("Things are quiet... mostly. Stay out of trouble.")
                .addEntryAction(DialogueAction.shareRumors())
                .addOption(DialogueOption.simple("back", "Verstanden.", "start"))
                .build())

            // Bestechungsversuch
            .addNode(DialogueNode.builder("bribe_attempt")
                .addText("What is that supposed to mean? Are you trying to bribe me?")
                .addConditionalText(
                    DialogueCondition.npcTraitAbove("greed", 30),
                    "Hmm... was genau schwebt Ihnen da vor?"
                )
                .addOption(DialogueOption.builder("bribe_yes")
                    .setText("Nur eine kleine Aufmerksamkeit...")
                    .setTargetNode("bribe_offer")
                    .addCondition(DialogueCondition.npcTraitAbove("greed", 30))
                    .build())
                .addOption(DialogueOption.builder("bribe_caught")
                    .setText("Nur eine kleine Aufmerksamkeit...")
                    .setTargetNode("bribe_rejected")
                    .addCondition(DialogueCondition.npcTraitBelow("greed", 30))
                    .build())
                .addOption(DialogueOption.simple("bribe_no", "Nein, nein! Ich meinte nur... Kaffee!", "start"))
                .build())

            // Bestechung anbieten
            .addNode(DialogueNode.builder("bribe_offer")
                .addText("Sprechen Sie leise... Wieviel?")
                .addEntryAction(DialogueAction.startBribery())
                .addOption(DialogueOption.simple("back", "[Verhandlung beginnen]", "start"))
                .build())

            // Bestechung abgelehnt
            .addNode(DialogueNode.builder("bribe_rejected")
                .addText("Bestechung eines Beamten! Das ist eine schwere Straftat!")
                .addEntryAction(DialogueAction.modifyFaction(Faction.LAW, -15))
                .addEntryAction(DialogueAction.addPlayerTag("Bestecher"))
                .addOption(DialogueOption.exit("[Schnell verschwinden]"))
                .build());

        TYPE_DIALOGUES.put(NPCType.POLICE, policeGeneral);
    }

    // ═══════════════════════════════════════════════════════════
    // CITIZEN DIALOGUES
    // ═══════════════════════════════════════════════════════════

    private static void registerCitizenDialogues() {
        DialogueTree citizenGeneral = new DialogueTree("citizen_general", "Citizen dialog")
            .setPriority(5)
            .addTag("citizen")
            .addTag("global")

            // Start
            .addNode(DialogueNode.builder("start")
                .addText("Oh, hallo! Kann ich Ihnen irgendwie helfen?")
                .addConditionalText(
                    DialogueCondition.npcEmotion(EmotionState.FEARFUL, 30),
                    "B-bitte tun Sie mir nichts..."
                )
                .addConditionalText(
                    DialogueCondition.npcEmotion(EmotionState.HAPPY, 50),
                    "What a beautiful day! How are you?"
                )
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Freund"),
                    "My friend! Good to see you!"
                )
                .addOption(DialogueOption.simple("chat", "Just a friendly chat.", "friendly_chat"))
                .addOption(DialogueOption.simple("info", "Kennen Sie sich hier aus?", "local_info"))
                .addOption(DialogueOption.simple("rumors", "Gibt es interessante Neuigkeiten?", "share_rumors"))
                .addOption(DialogueOption.builder("help")
                    .setText("Brauchen Sie vielleicht Hilfe?")
                    .setTargetNode("offer_help")
                    .addAction(DialogueAction.triggerEmotion(EmotionState.HAPPY, 15))
                    .build())
                .addOption(DialogueOption.exit("Auf Wiedersehen."))
                .build())

            // Freundliches Gespräch
            .addNode(DialogueNode.builder("friendly_chat")
                .addText("It's nice to chat with someone. A lot has been going on here lately.")
                .addEntryAction(DialogueAction.triggerEmotion(EmotionState.HAPPY, 10))
                .addOption(DialogueOption.simple("more", "Tell me more!", "chat_continue"))
                .addOption(DialogueOption.simple("back", "Es war nett, mit Ihnen zu sprechen.", "start"))
                .build())

            // Gespräch fortsetzen
            .addNode(DialogueNode.builder("chat_continue")
                .addText("You know, everything used to be simpler. But what can you do...")
                .addEntryAction(DialogueAction.modifyFaction(Faction.CITIZENS, 2))
                .addOption(DialogueOption.exit("Ich verstehe. Passen Sie auf sich auf!"))
                .build())

            // Lokale Infos
            .addNode(DialogueNode.builder("local_info")
                .addText("Oh yes, I've lived here all my life. What would you like to know?")
                .addOption(DialogueOption.simple("shops", "Where can I find good shops?", "info_shops"))
                .addOption(DialogueOption.simple("danger", "Are there dangerous areas?", "info_danger"))
                .addOption(DialogueOption.simple("back", "Danke, das reicht.", "start"))
                .build())

            // Shop-Infos
            .addNode(DialogueNode.builder("info_shops")
                .addText("The merchants in the center are reliable. Beware of the ones in the side streets...")
                .addOption(DialogueOption.simple("back", "Thanks for the tip!", "start"))
                .build())

            // Gefahren-Infos
            .addNode(DialogueNode.builder("info_danger")
                .addText("Avoid the dark alleys at night. There are... questionable characters around.")
                .addEntryAction(DialogueAction.shareRumors())
                .addOption(DialogueOption.simple("back", "Ich werde vorsichtig sein.", "start"))
                .build())

            // Gerüchte
            .addNode(DialogueNode.builder("share_rumors")
                .addText("Psst... I heard something...")
                .addEntryAction(DialogueAction.shareRumors())
                .addOption(DialogueOption.simple("back", "Interessant...", "start"))
                .build())

            // Hilfe anbieten
            .addNode(DialogueNode.builder("offer_help")
                .addText("Hilfe? Das ist sehr freundlich von Ihnen!")
                .addConditionalText(
                    DialogueCondition.random(0.3f),
                    "Actually, I might have something..."
                )
                .addOption(DialogueOption.builder("accept_quest")
                    .setText("What can I do for you?")
                    .setTargetNode("give_quest")
                    .addAction(DialogueAction.checkForQuest())
                    .build())
                .addOption(DialogueOption.simple("back", "Vielleicht ein andermal.", "start"))
                .build())

            // Quest geben
            .addNode(DialogueNode.builder("give_quest")
                .addText("Wonderful! I would be very grateful.")
                .addEntryAction(DialogueAction.offerQuest())
                .addOption(DialogueOption.exit("I'll see what I can do."))
                .build());

        TYPE_DIALOGUES.put(NPCType.CITIZEN, citizenGeneral);
    }

    // ═══════════════════════════════════════════════════════════
    // UNDERWORLD DIALOGUES
    // ═══════════════════════════════════════════════════════════

    private static void registerUnderworldDialogues() {
        DialogueTree underworldGeneral = new DialogueTree("underworld_general", "Untergrund-Dialog")
            .setPriority(10)
            .addTag("underworld")

            // Start
            .addNode(DialogueNode.builder("start")
                .addText("*mustert Sie misstrauisch* Was wollen Sie?")
                .addConditionalText(
                    DialogueCondition.factionStanding(Faction.UNDERWORLD, 30),
                    "Ah, a familiar face. What brings you here?"
                )
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Polizeispitzel"),
                    "A snitch! Get lost before there's trouble!"
                )
                .addOption(DialogueOption.builder("business")
                    .setText("Ich suche nach... speziellen Waren.")
                    .setTargetNode("special_goods")
                    .addCondition(DialogueCondition.factionStanding(Faction.UNDERWORLD, 10))
                    .build())
                .addOption(DialogueOption.simple("info", "Ich brauche Informationen.", "underworld_info"))
                .addOption(DialogueOption.builder("join")
                    .setText("Ich will mitmachen.")
                    .setTargetNode("join_underworld")
                    .addCondition(DialogueCondition.factionStanding(Faction.UNDERWORLD, 50))
                    .build())
                .addOption(DialogueOption.exit("Nichts. Vergessen Sie es."))
                .build())

            // Spezielle Waren
            .addNode(DialogueNode.builder("special_goods")
                .addText("Special goods, eh? I could arrange something...")
                .addOption(DialogueOption.builder("buy_illegal")
                    .setText("Zeigen Sie mir, was Sie haben.")
                    .addAction(DialogueAction.openIllegalTrade())
                    .setEndNode(true)
                    .build())
                .addOption(DialogueOption.simple("back", "Maybe later.", "start"))
                .build())

            // Untergrund-Infos
            .addNode(DialogueNode.builder("underworld_info")
                .addText("Informationen kosten. Was haben Sie anzubieten?")
                .addOption(DialogueOption.builder("pay_info")
                    .setText("[Pay 100 coins]")
                    .setTargetNode("paid_info")
                    .addAction(DialogueAction.payMoney(100))
                    .build())
                .addOption(DialogueOption.builder("trade_info")
                    .setText("Ich habe selbst interessante Informationen.")
                    .setTargetNode("trade_info")
                    .addCondition(DialogueCondition.hasPlayerTag("Informant"))
                    .build())
                .addOption(DialogueOption.simple("back", "Zu teuer.", "start"))
                .build())

            // Bezahlte Info
            .addNode(DialogueNode.builder("paid_info")
                .addText("Good. *whispers* Listen carefully...")
                .addEntryAction(DialogueAction.shareRumors())
                .addEntryAction(DialogueAction.modifyFaction(Faction.UNDERWORLD, 3))
                .addOption(DialogueOption.exit("Thanks for the info."))
                .build())

            // Info-Tausch
            .addNode(DialogueNode.builder("trade_info")
                .addText("Ah, ein Kollege. Dann lass uns tauschen.")
                .addEntryAction(DialogueAction.shareRumors())
                .addEntryAction(DialogueAction.modifyFaction(Faction.UNDERWORLD, 5))
                .addOption(DialogueOption.exit("Ein fairer Tausch."))
                .build())

            // Untergrund beitreten
            .addNode(DialogueNode.builder("join_underworld")
                .addText("Join? It's not that simple. Prove yourself first.")
                .addOption(DialogueOption.builder("accept_job")
                    .setText("What do I have to do?")
                    .setTargetNode("underworld_job")
                    .addAction(DialogueAction.offerQuest())
                    .build())
                .addOption(DialogueOption.simple("back", "I'll think about it.", "start"))
                .build())

            // Untergrund-Job
            .addNode(DialogueNode.builder("underworld_job")
                .addText("There is this one thing... take care of it, and we'll talk further.")
                .addEntryAction(DialogueAction.offerQuest())
                .addOption(DialogueOption.exit("Ich bin dabei."))
                .build());

        TYPE_DIALOGUES.put(NPCType.DRUG_DEALER, underworldGeneral);
    }

    // ═══════════════════════════════════════════════════════════
    // PUBLIC ACCESS
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt den Standard-Dialog für einen NPC-Typ
     */
    public static DialogueTree getDialogueForType(NPCType type) {
        initialize();
        return TYPE_DIALOGUES.get(type);
    }

    /**
     * Erstellt einen dynamischen Dialog basierend auf NPC-Zustand
     */
    public static DialogueTree createDynamicDialogue(CustomNPCEntity npc) {
        initialize();

        NPCType type = npc.getNpcType();
        DialogueTree baseTree = TYPE_DIALOGUES.get(type);

        if (baseTree == null) {
            // Fallback zu generischem Dialog
            baseTree = TYPE_DIALOGUES.get(NPCType.CITIZEN);
        }

        // Hier könnte man basierend auf NPC-Zustand den Dialog anpassen
        // Für jetzt geben wir einfach den Basis-Dialog zurück
        return baseTree;
    }

    /**
     * Registriert alle Dialoge für ein Level.
     * Wird beim Level-Load aufgerufen, aber auch nach der Manager-Initialisierung.
     * Ist ein No-op wenn der DialogueManager noch nicht initialisiert wurde.
     */
    public static void setupForLevel(ServerLevel level) {
        DialogueManager manager = DialogueManager.getManager(level);
        if (manager == null) return;
        registerAllTrees(manager);
    }
}
