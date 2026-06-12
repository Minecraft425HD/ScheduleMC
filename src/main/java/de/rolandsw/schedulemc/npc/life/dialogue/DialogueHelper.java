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
            return "Hello.";
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
        boolean isDangerous = lifeData.getMemory().hasPlayerTag(playerUUID, "Dangerous");

        // Priorisiere nach Wichtigkeit
        if (isDangerous && intensity > 30) {
            return "Y-you again... p-please leave...";
        }

        if (isCriminal) {
            return "I am keeping an eye on you...";
        }

        // Emotionsbasierte Begrüßungen
        if (emotion == EmotionState.FEARFUL && intensity > 40) {
            return "O-oh, hello... Can I help you?";
        }

        if (emotion == EmotionState.ANGRY && intensity > 50) {
            return "What do you want?";
        }

        if (emotion == EmotionState.SAD && intensity > 40) {
            return "*sighs* Yes, what is it?";
        }

        // Positive Begrüßungen
        if (emotion == EmotionState.HAPPY && intensity > 30) {
            if (isGoodCustomer) {
                return "Ah, " + playerName + "! My best customer! What will it be today?";
            }
            return "Welcome! What a beautiful day!";
        }

        // Bekannte Kunden
        if (isRegular) {
            return "Hello " + playerName + "! Nice to see you again!";
        }

        if (isGoodCustomer) {
            return "Welcome back, " + playerName + "! What can I do for you?";
        }

        if (isKnown) {
            return "Ah, I know you. What brings you here?";
        }

        // Standard-Begrüßung
        return "Good day! How can I help you?";
    }

    /**
     * Generiert Begrüßung für bestimmten NPC-Typ
     */
    public static String getGreetingForType(CustomNPCEntity npc, ServerPlayer player) {
        String baseGreeting = getGreeting(npc, player);

        // NPC-Typ-spezifische Varianten
        switch (npc.getNpcType()) {
            case POLICE -> {
                if (npc.getLifeData() != null &&
                    npc.getLifeData().getMemory().hasPlayerTag(player.getUUID(), "Gesucht")) {
                    return "Stop! You are a wanted criminal!";
                }
                return "Good day, citizen. Everything all right?";
            }
            case MERCHANT -> {
                return baseGreeting.isEmpty() ? "Welcome to my shop!" : baseGreeting;
            }
            case CITIZEN -> {
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
        options.add(new DialogueOptionInfo("talk", "Reden", "Have a conversation with the NPC"));

        // Händler-spezifisch
        if (npc.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.MERCHANT) {
            if (npc.isWillingToTrade()) {
                options.add(new DialogueOptionInfo("trade", "Handeln", "Buy or sell goods"));
            } else {
                options.add(new DialogueOptionInfo("trade_disabled", "Trade (not available)",
                    "The merchant is currently not willing to trade"));
            }
        }

        // Fraktions-basierte Optionen
        if (lifeData != null && player.level() instanceof ServerLevel level) {
            FactionManager factionManager = FactionManager.getManager(level);
            Faction npcFaction = Faction.forNPCType(npc.getNpcType());

            int reputation = factionManager.getReputation(player.getUUID(), npcFaction);

            // Gerüchte teilen (bei ausreichend Reputation)
            if (reputation >= 20) {
                options.add(new DialogueOptionInfo("rumors", "Ask about rumors",
                    "Ask about interesting news"));
            }

            // Quest anbieten (bei ausreichend Reputation)
            if (reputation >= 10) {
                options.add(new DialogueOptionInfo("quest", "Ask for tasks",
                    "Ask if the NPC needs help"));
            }
        }

        // Begleiter-Option (nur bei bestimmten NPCs)
        if (canBecomeCompanion(npc, player)) {
            options.add(new DialogueOptionInfo("recruit", "Als Begleiter anwerben",
                "Try to recruit the NPC as a companion"));
        }

        // Zeuge bestechen (wenn relevant)
        if (lifeData != null && lifeData.getMemory().hasPlayerTag(player.getUUID(), "Kriminell")) {
            if (lifeData.getTraits().getGreed() > 20) {
                options.add(new DialogueOptionInfo("bribe", "Bestechung versuchen",
                    "Try to bribe the witness"));
            }
        }

        return options;
    }

    /**
     * Prüft ob ein NPC als Begleiter rekrutiert werden kann
     */
    private static boolean canBecomeCompanion(CustomNPCEntity npc, ServerPlayer player) {
        // NPCs vom Typ CITIZEN können potentiell Begleiter werden
        if (npc.getNpcType() != de.rolandsw.schedulemc.npc.data.NPCType.CITIZEN) {
            return false;
        }

        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return false;

        // Braucht gute Beziehung
        if (!lifeData.getMemory().hasPlayerTag(player.getUUID(), "Freund")) {  // NOPMD
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
        return manager.startDialogue(player, npc);
    }

    /**
     * Beendet einen laufenden Dialog
     */
    public static void endDialogue(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        DialogueManager manager = DialogueManager.getManager(level);
        manager.endDialogue(player);
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
