package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

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

    private static final Map<NPCType, DialogueTree> TYPE_DIALOGUES = new HashMap<>();
    private static boolean initialized = false;

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
        DialogueTree merchantGeneral = new DialogueTree("merchant_general", "Händler-Dialog")
            .setPriority(10)
            .addTag("merchant")

            // Start-Node
            .addNode(DialogueNode.builder("start")
                .addText("Willkommen in meinem Geschäft, {player}! Was kann ich für Sie tun?")
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Stammkunde"),
                    "Ah, {player}! Mein treuer Stammkunde! Was darf es heute sein?"
                )
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Dieb"),
                    "Sie schon wieder... Ich behalte Sie im Auge."
                )
                .addOption(DialogueOption.simple("browse", "Ich möchte Ihr Angebot sehen.", "show_wares"))
                .addOption(DialogueOption.simple("sell", "Ich habe etwas zu verkaufen.", "buy_from_player"))
                .addOption(DialogueOption.simple("info", "Erzählen Sie mir etwas über sich.", "merchant_info"))
                .addOption(DialogueOption.builder("negotiate")
                    .setText("Können wir über die Preise verhandeln?")
                    .setTargetNode("negotiate_start")
                    .addCondition(DialogueCondition.factionStanding(Faction.HAENDLER, 20))
                    .build())
                .addOption(DialogueOption.exit("Auf Wiedersehen."))
                .build())

            // Waren zeigen
            .addNode(DialogueNode.builder("show_wares")
                .addText("Natürlich! Hier ist mein aktuelles Sortiment.")
                .addEntryAction(DialogueAction.openTrade())
                .setEndNode(true)
                .build())

            // Vom Spieler kaufen
            .addNode(DialogueNode.builder("buy_from_player")
                .addText("Mal sehen, was Sie haben... Ja, das könnte ich gebrauchen.")
                .addOption(DialogueOption.simple("back", "Vielleicht später.", "start"))
                .addOption(DialogueOption.trade("Zeigen Sie mir Ihre Preise."))
                .build())

            // Info über Händler
            .addNode(DialogueNode.builder("merchant_info")
                .addText("Ich bin schon seit Jahren im Geschäft. Meine Familie handelt hier seit Generationen.")
                .addOption(DialogueOption.simple("rumors", "Haben Sie interessante Neuigkeiten gehört?", "share_rumors"))
                .addOption(DialogueOption.simple("back", "Interessant. Zurück zum Geschäft.", "start"))
                .build())

            // Gerüchte teilen
            .addNode(DialogueNode.builder("share_rumors")
                .addText("Nun ja, man hört so einiges... Aber das bleibt unter uns, verstanden?")
                .addEntryAction(DialogueAction.shareRumors())
                .addOption(DialogueOption.simple("back", "Natürlich. Danke für die Information.", "start"))
                .build())

            // Verhandlung starten
            .addNode(DialogueNode.builder("negotiate_start")
                .addText("Verhandeln? Nun, für einen guten Kunden wie Sie... was schwebt Ihnen vor?")
                .addOption(DialogueOption.builder("negotiate_friendly")
                    .setText("Wir kennen uns doch schon lange. Ein kleiner Rabatt?")
                    .setTargetNode("negotiate_result")
                    .addAction(DialogueAction.setVariable("negotiate_tactic", "friendly"))
                    .build())
                .addOption(DialogueOption.builder("negotiate_pressure")
                    .setText("Ich könnte auch woanders einkaufen...")
                    .setTargetNode("negotiate_result")
                    .addAction(DialogueAction.setVariable("negotiate_tactic", "pressure"))
                    .build())
                .addOption(DialogueOption.simple("back", "Vergessen Sie es.", "start"))
                .build())

            // Verhandlungsergebnis
            .addNode(DialogueNode.builder("negotiate_result")
                .addText("Hmm... Lassen Sie mich darüber nachdenken.")
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
                .addText("Guten Tag, Bürger. Kann ich Ihnen helfen?")
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Gesucht"),
                    "Halt! Sie sind zur Fahndung ausgeschrieben! Keine Bewegung!"
                )
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Kriminell"),
                    "Sie... Ich habe Sie im Auge. Machen Sie keinen Unsinn."
                )
                .addOption(DialogueOption.builder("wanted_response")
                    .setText("Warten Sie, ich kann das erklären!")
                    .setTargetNode("wanted_explain")
                    .addCondition(DialogueCondition.hasPlayerTag("Gesucht"))
                    .build())
                .addOption(DialogueOption.builder("wanted_flee")
                    .setText("[Fliehen]")
                    .setTargetNode("wanted_flee")
                    .addCondition(DialogueCondition.hasPlayerTag("Gesucht"))
                    .build())
                .addOption(DialogueOption.simple("report", "Ich möchte ein Verbrechen melden.", "report_crime"))
                .addOption(DialogueOption.simple("info", "Was gibt es Neues in der Stadt?", "police_info"))
                .addOption(DialogueOption.simple("bribe", "Vielleicht kann ich Ihnen... helfen?", "bribe_attempt"))
                .addOption(DialogueOption.exit("Auf Wiedersehen, Offizier."))
                .build())

            // Gesucht - Erklären
            .addNode(DialogueNode.builder("wanted_explain")
                .addText("Erklären? Das können Sie vor Gericht tun. Hände hoch!")
                .addOption(DialogueOption.builder("surrender")
                    .setText("[Ergeben]")
                    .addAction(DialogueAction.triggerEmotion(EmotionState.FEARFUL, 40))
                    .addAction(DialogueAction.modifyFaction(Faction.ORDNUNG, 5))
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
                .addText("Widerstand gegen die Staatsgewalt? Das wird Ihnen teuer zu stehen kommen!")
                .addEntryAction(DialogueAction.alertNearbyNPCs("dangerous"))
                .addEntryAction(DialogueAction.modifyFaction(Faction.ORDNUNG, -20))
                .setEndNode(true)
                .build())

            // Verbrechen melden
            .addNode(DialogueNode.builder("report_crime")
                .addText("Ein Verbrechen? Erzählen Sie mir, was passiert ist.")
                .addOption(DialogueOption.simple("report_theft", "Ich wurde bestohlen.", "crime_reported"))
                .addOption(DialogueOption.simple("report_assault", "Ich wurde angegriffen.", "crime_reported"))
                .addOption(DialogueOption.simple("report_suspicious", "Ich habe verdächtige Aktivitäten beobachtet.", "crime_reported"))
                .addOption(DialogueOption.simple("back", "Eigentlich... vergessen Sie es.", "start"))
                .build())

            // Verbrechen gemeldet
            .addNode(DialogueNode.builder("crime_reported")
                .addText("Danke für die Meldung. Wir werden der Sache nachgehen.")
                .addEntryAction(DialogueAction.modifyFaction(Faction.ORDNUNG, 5))
                .addEntryAction(DialogueAction.addPlayerTag("Hilfreich"))
                .addOption(DialogueOption.exit("Danke, Offizier."))
                .build())

            // Polizei-Info
            .addNode(DialogueNode.builder("police_info")
                .addText("Die Lage ist ruhig... meistens. Halten Sie sich von Ärger fern.")
                .addEntryAction(DialogueAction.shareRumors())
                .addOption(DialogueOption.simple("back", "Verstanden.", "start"))
                .build())

            // Bestechungsversuch
            .addNode(DialogueNode.builder("bribe_attempt")
                .addText("Was soll das heißen? Wollen Sie mich etwa bestechen?")
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
                .addEntryAction(DialogueAction.modifyFaction(Faction.ORDNUNG, -15))
                .addEntryAction(DialogueAction.addPlayerTag("Bestecher"))
                .addOption(DialogueOption.exit("[Schnell verschwinden]"))
                .build());

        TYPE_DIALOGUES.put(NPCType.POLICE, policeGeneral);
    }

    // ═══════════════════════════════════════════════════════════
    // CITIZEN DIALOGUES
    // ═══════════════════════════════════════════════════════════

    private static void registerCitizenDialogues() {
        DialogueTree citizenGeneral = new DialogueTree("citizen_general", "Bürger-Dialog")
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
                    "Was für ein schöner Tag! Wie geht es Ihnen?"
                )
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Freund"),
                    "Mein Freund! Schön, Sie zu sehen!"
                )
                .addOption(DialogueOption.simple("chat", "Nur ein freundliches Gespräch.", "friendly_chat"))
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
                .addText("Es ist schön, mit jemandem zu plaudern. In letzter Zeit ist hier viel los.")
                .addEntryAction(DialogueAction.triggerEmotion(EmotionState.HAPPY, 10))
                .addOption(DialogueOption.simple("more", "Erzählen Sie mehr!", "chat_continue"))
                .addOption(DialogueOption.simple("back", "Es war nett, mit Ihnen zu sprechen.", "start"))
                .build())

            // Gespräch fortsetzen
            .addNode(DialogueNode.builder("chat_continue")
                .addText("Wissen Sie, früher war alles einfacher. Aber was kann man tun...")
                .addEntryAction(DialogueAction.modifyFaction(Faction.BUERGER, 2))
                .addOption(DialogueOption.exit("Ich verstehe. Passen Sie auf sich auf!"))
                .build())

            // Lokale Infos
            .addNode(DialogueNode.builder("local_info")
                .addText("Oh ja, ich lebe schon mein ganzes Leben hier. Was möchten Sie wissen?")
                .addOption(DialogueOption.simple("shops", "Wo finde ich gute Geschäfte?", "info_shops"))
                .addOption(DialogueOption.simple("danger", "Gibt es gefährliche Gegenden?", "info_danger"))
                .addOption(DialogueOption.simple("back", "Danke, das reicht.", "start"))
                .build())

            // Shop-Infos
            .addNode(DialogueNode.builder("info_shops")
                .addText("Die Händler im Zentrum sind zuverlässig. Vorsicht bei denen in den Seitenstraßen...")
                .addOption(DialogueOption.simple("back", "Danke für den Tipp!", "start"))
                .build())

            // Gefahren-Infos
            .addNode(DialogueNode.builder("info_danger")
                .addText("Meiden Sie die dunklen Gassen bei Nacht. Da treiben sich... fragwürdige Gestalten herum.")
                .addEntryAction(DialogueAction.shareRumors())
                .addOption(DialogueOption.simple("back", "Ich werde vorsichtig sein.", "start"))
                .build())

            // Gerüchte
            .addNode(DialogueNode.builder("share_rumors")
                .addText("Psst... ich habe da etwas gehört...")
                .addEntryAction(DialogueAction.shareRumors())
                .addOption(DialogueOption.simple("back", "Interessant...", "start"))
                .build())

            // Hilfe anbieten
            .addNode(DialogueNode.builder("offer_help")
                .addText("Hilfe? Das ist sehr freundlich von Ihnen!")
                .addConditionalText(
                    DialogueCondition.random(0.3f),
                    "Tatsächlich hätte ich da etwas..."
                )
                .addOption(DialogueOption.builder("accept_quest")
                    .setText("Was kann ich für Sie tun?")
                    .setTargetNode("give_quest")
                    .addAction(DialogueAction.checkForQuest())
                    .build())
                .addOption(DialogueOption.simple("back", "Vielleicht ein andermal.", "start"))
                .build())

            // Quest geben
            .addNode(DialogueNode.builder("give_quest")
                .addText("Wunderbar! Ich wäre Ihnen sehr dankbar.")
                .addEntryAction(DialogueAction.offerQuest())
                .addOption(DialogueOption.exit("Ich werde sehen, was ich tun kann."))
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
                    DialogueCondition.factionStanding(Faction.UNTERGRUND, 30),
                    "Ah, ein bekanntes Gesicht. Was führt Sie her?"
                )
                .addConditionalText(
                    DialogueCondition.hasPlayerTag("Polizeispitzel"),
                    "Ein Spitzel! Verschwinden Sie, bevor es Ärger gibt!"
                )
                .addOption(DialogueOption.builder("business")
                    .setText("Ich suche nach... speziellen Waren.")
                    .setTargetNode("special_goods")
                    .addCondition(DialogueCondition.factionStanding(Faction.UNTERGRUND, 10))
                    .build())
                .addOption(DialogueOption.simple("info", "Ich brauche Informationen.", "underworld_info"))
                .addOption(DialogueOption.builder("join")
                    .setText("Ich will mitmachen.")
                    .setTargetNode("join_underworld")
                    .addCondition(DialogueCondition.factionStanding(Faction.UNTERGRUND, 50))
                    .build())
                .addOption(DialogueOption.exit("Nichts. Vergessen Sie es."))
                .build())

            // Spezielle Waren
            .addNode(DialogueNode.builder("special_goods")
                .addText("Spezielle Waren, ja? Ich könnte da etwas arrangieren...")
                .addOption(DialogueOption.builder("buy_illegal")
                    .setText("Zeigen Sie mir, was Sie haben.")
                    .addAction(DialogueAction.openIllegalTrade())
                    .setEndNode(true)
                    .build())
                .addOption(DialogueOption.simple("back", "Vielleicht später.", "start"))
                .build())

            // Untergrund-Infos
            .addNode(DialogueNode.builder("underworld_info")
                .addText("Informationen kosten. Was haben Sie anzubieten?")
                .addOption(DialogueOption.builder("pay_info")
                    .setText("[100 Münzen bezahlen]")
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
                .addText("Gut. *flüstert* Hören Sie genau zu...")
                .addEntryAction(DialogueAction.shareRumors())
                .addEntryAction(DialogueAction.modifyFaction(Faction.UNTERGRUND, 3))
                .addOption(DialogueOption.exit("Danke für die Info."))
                .build())

            // Info-Tausch
            .addNode(DialogueNode.builder("trade_info")
                .addText("Ah, ein Kollege. Dann lass uns tauschen.")
                .addEntryAction(DialogueAction.shareRumors())
                .addEntryAction(DialogueAction.modifyFaction(Faction.UNTERGRUND, 5))
                .addOption(DialogueOption.exit("Ein fairer Tausch."))
                .build())

            // Untergrund beitreten
            .addNode(DialogueNode.builder("join_underworld")
                .addText("Mitmachen? Das ist nicht so einfach. Beweisen Sie sich zuerst.")
                .addOption(DialogueOption.builder("accept_job")
                    .setText("Was muss ich tun?")
                    .setTargetNode("underworld_job")
                    .addAction(DialogueAction.offerQuest())
                    .build())
                .addOption(DialogueOption.simple("back", "Ich überlege es mir.", "start"))
                .build())

            // Untergrund-Job
            .addNode(DialogueNode.builder("underworld_job")
                .addText("Es gibt da eine Sache... erledigen Sie das, und wir reden weiter.")
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
     * Registriert alle Dialoge für ein Level
     */
    public static void setupForLevel(ServerLevel level) {
        DialogueManager manager = DialogueManager.getManager(level);
        registerAllTrees(manager);
    }
}
