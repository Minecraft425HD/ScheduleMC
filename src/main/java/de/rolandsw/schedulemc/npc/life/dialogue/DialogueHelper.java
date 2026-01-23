package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DialogueHelper - Hilfsklasse für Dialog-Integration mit NPCs
 *
 * Ermöglicht:
 * - Dialog-Auswahl basierend auf NPC-Zustand
 * - Dynamische Begrüßungen basierend auf Gedächtnis und Emotionen
 * - Integration mit dem Quest-System
 */
public class DialogueHelper {

    // ═══════════════════════════════════════════════════════════
    // GREETING GENERATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Generiert eine dynamische Begrüßung basierend auf NPC-Zustand und Spieler-History
     */
    public static String getGreeting(CustomNPCEntity npc, ServerPlayer player) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) {
            return "Hallo.";
        }

        UUID playerUUID = player.getUUID();
        String playerName = player.getName().getString();

        // Prüfe Emotionen
        EmotionState emotion = lifeData.getEmotions().getCurrentEmotion();
        float intensity = lifeData.getEmotions().getIntensity();

        // Prüfe Spieler-Tags aus dem Gedächtnis
        boolean isKnown = lifeData.getMemory().hasMemoryOf(playerUUID);
        boolean isGoodCustomer = lifeData.getMemory().hasPlayerTag(playerUUID, "GutKunde");
        boolean isRegular = lifeData.getMemory().hasPlayerTag(playerUUID, "Stammkunde");
        boolean isCriminal = lifeData.getMemory().hasPlayerTag(playerUUID, "Kriminell");
        boolean isDangerous = lifeData.getMemory().hasPlayerTag(playerUUID, "Gefährlich");

        // Priorisiere nach Wichtigkeit
        if (isDangerous && intensity > 30) {
            return "S-Sie schon wieder... B-bitte gehen Sie...";
        }

        if (isCriminal) {
            return "Ich behalte Sie im Auge...";
        }

        // Emotionsbasierte Begrüßungen
        if (emotion == EmotionState.FEARFUL && intensity > 40) {
            return "O-oh, hallo... Kann ich Ihnen helfen?";
        }

        if (emotion == EmotionState.ANGRY && intensity > 50) {
            return "Was wollen Sie?";
        }

        if (emotion == EmotionState.SAD && intensity > 40) {
            return "*seufzt* Ja, was gibt's?";
        }

        // Positive Begrüßungen
        if (emotion == EmotionState.HAPPY && intensity > 30) {
            if (isGoodCustomer) {
                return "Ah, " + playerName + "! Mein bester Kunde! Was darf es heute sein?";
            }
            return "Willkommen! Was für ein schöner Tag!";
        }

        // Bekannte Kunden
        if (isRegular) {
            return "Hallo " + playerName + "! Schön Sie wiederzusehen!";
        }

        if (isGoodCustomer) {
            return "Willkommen zurück, " + playerName + "! Was kann ich für Sie tun?";
        }

        if (isKnown) {
            return "Ah, Sie kenne ich doch. Was führt Sie her?";
        }

        // Standard-Begrüßung
        return "Guten Tag! Wie kann ich Ihnen helfen?";
    }

    /**
     * Generiert Begrüßung für bestimmten NPC-Typ
     */
    public static String getGreetingForType(CustomNPCEntity npc, ServerPlayer player) {
        String baseGreeting = getGreeting(npc, player);

        // NPC-Typ-spezifische Varianten
        switch (npc.getNpcType()) {
            case POLIZEI -> {
                if (npc.getLifeData() != null &&
                    npc.getLifeData().getMemory().hasPlayerTag(player.getUUID(), "Gesucht")) {
                    return "Halt! Sie sind zur Fahndung ausgeschrieben!";
                }
                return "Guten Tag, Bürger. Alles in Ordnung?";
            }
            case VERKAEUFER -> {
                return baseGreeting.isEmpty() ? "Willkommen in meinem Geschäft!" : baseGreeting;
            }
            case BEWOHNER -> {
                return baseGreeting;
            }
            default -> {
                return baseGreeting;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DIALOGUE OPTIONS BASED ON STATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt verfügbare Dialog-Optionen basierend auf NPC-Zustand zurück
     */
    public static List<DialogueOptionInfo> getAvailableOptions(CustomNPCEntity npc, ServerPlayer player) {
        List<DialogueOptionInfo> options = new ArrayList<>();
        NPCLifeData lifeData = npc.getLifeData();

        // Standard-Optionen
        options.add(new DialogueOptionInfo("talk", "Reden", "Unterhalten Sie sich mit dem NPC"));

        // Händler-spezifisch
        if (npc.getNpcType() == de.rolandsw.schedulemc.npc.NPCType.VERKAEUFER) {
            if (npc.isWillingToTrade()) {
                options.add(new DialogueOptionInfo("trade", "Handeln", "Kaufen oder verkaufen Sie Waren"));
            } else {
                options.add(new DialogueOptionInfo("trade_disabled", "Handeln (nicht verfügbar)",
                    "Der Händler ist momentan nicht bereit zu handeln"));
            }
        }

        // Fraktions-basierte Optionen
        if (lifeData != null && player.level() instanceof ServerLevel level) {
            FactionManager factionManager = FactionManager.getManager(level);
            Faction npcFaction = Faction.forNPCType(npc.getNpcType());

            int reputation = factionManager.getReputation(player.getUUID(), npcFaction);

            // Gerüchte teilen (bei ausreichend Reputation)
            if (reputation >= 20) {
                options.add(new DialogueOptionInfo("rumors", "Nach Gerüchten fragen",
                    "Fragen Sie nach interessanten Neuigkeiten"));
            }

            // Quest anbieten (bei ausreichend Reputation)
            if (reputation >= 10) {
                options.add(new DialogueOptionInfo("quest", "Nach Aufgaben fragen",
                    "Fragen Sie ob der NPC Hilfe benötigt"));
            }
        }

        // Begleiter-Option (nur bei bestimmten NPCs)
        if (canBecomeCompanion(npc, player)) {
            options.add(new DialogueOptionInfo("recruit", "Als Begleiter anwerben",
                "Versuchen Sie den NPC als Begleiter zu gewinnen"));
        }

        // Zeuge bestechen (wenn relevant)
        if (lifeData != null && lifeData.getMemory().hasPlayerTag(player.getUUID(), "Kriminell")) {
            if (lifeData.getTraits().getGreed() > 20) {
                options.add(new DialogueOptionInfo("bribe", "Bestechung versuchen",
                    "Versuchen Sie den Zeugen zu bestechen"));
            }
        }

        return options;
    }

    /**
     * Prüft ob ein NPC als Begleiter rekrutiert werden kann
     */
    private static boolean canBecomeCompanion(CustomNPCEntity npc, ServerPlayer player) {
        // NPCs vom Typ BEWOHNER können potentiell Begleiter werden
        if (npc.getNpcType() != de.rolandsw.schedulemc.npc.NPCType.BEWOHNER) {
            return false;
        }

        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return false;

        // Braucht gute Beziehung
        if (!lifeData.getMemory().hasPlayerTag(player.getUUID(), "Freund")) {
            return false;
        }

        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // DIALOGUE TREE ACCESS
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet einen Dialog mit einem NPC
     */
    @Nullable
    public static DialogueContext startDialogue(CustomNPCEntity npc, ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return null;

        DialogueManager manager = DialogueManager.getManager(level);

        // Dynamischen Dialog für diesen NPC erstellen
        DialogueTree tree = NPCDialogueProvider.createDynamicDialogue(npc);
        if (tree == null) return null;

        // Dialog starten
        return manager.startDialogue(player.getUUID(), npc.getNpcData().getNpcUUID(), tree.getId());
    }

    /**
     * Beendet einen laufenden Dialog
     */
    public static void endDialogue(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        DialogueManager manager = DialogueManager.getManager(level);
        manager.endDialogue(player.getUUID());
    }

    // ═══════════════════════════════════════════════════════════
    // INTERACTION RECORDING
    // ═══════════════════════════════════════════════════════════

    /**
     * Zeichnet eine Interaktion im NPC-Gedächtnis auf
     */
    public static void recordInteraction(CustomNPCEntity npc, ServerPlayer player, String interactionType) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        MemoryType memoryType;
        int importance;

        switch (interactionType) {
            case "talk" -> {
                memoryType = MemoryType.CONVERSATION;
                importance = 2;
            }
            case "trade" -> {
                memoryType = MemoryType.TRADED;
                importance = 3;
            }
            case "quest_complete" -> {
                memoryType = MemoryType.QUEST_COMPLETED;
                importance = 5;
            }
            case "helped" -> {
                memoryType = MemoryType.HELPED;
                importance = 4;
            }
            case "bribe_attempt" -> {
                memoryType = MemoryType.BRIBE_OFFERED;
                importance = 6;
            }
            default -> {
                memoryType = MemoryType.CONVERSATION;
                importance = 1;
            }
        }

        lifeData.getMemory().addMemory(
            player.getUUID(),
            memoryType,
            "Interaktion: " + interactionType,
            importance
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER CLASSES
    // ═══════════════════════════════════════════════════════════

    /**
     * Informationen über eine Dialog-Option
     */
    public static class DialogueOptionInfo {
        public final String id;
        public final String label;
        public final String tooltip;

        public DialogueOptionInfo(String id, String label, String tooltip) {
            this.id = id;
            this.label = label;
            this.tooltip = tooltip;
        }
    }
}
