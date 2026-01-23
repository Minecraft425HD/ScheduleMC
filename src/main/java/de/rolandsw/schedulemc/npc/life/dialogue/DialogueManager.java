package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.*;

/**
 * DialogueManager - Verwaltet aktive Dialoge und Dialogbäume
 *
 * Verantwortlich für:
 * - Starten und Beenden von Dialogen
 * - Navigation durch Dialogbäume
 * - Registrierung von Dialogbäumen für NPCs
 */
public class DialogueManager {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, DialogueManager> MANAGERS = new HashMap<>();

    public static DialogueManager getManager(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, l -> new DialogueManager());
    }

    public static void removeManager(ServerLevel level) {
        MANAGERS.remove(level);
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Registrierte Dialogbäume: TreeID -> Tree */
    private final Map<String, DialogueTree> registeredTrees = new HashMap<>();

    /** NPC-spezifische Bäume: NPC UUID -> Liste von Tree IDs */
    private final Map<UUID, List<String>> npcTrees = new HashMap<>();

    /** Aktive Dialoge: Player UUID -> Context */
    private final Map<UUID, DialogueContext> activeDialogues = new HashMap<>();

    // ═══════════════════════════════════════════════════════════
    // TREE REGISTRATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen Dialogbaum global
     */
    public void registerTree(DialogueTree tree) {
        registeredTrees.put(tree.getId(), tree);
    }

    /**
     * Entfernt einen Dialogbaum
     */
    public void unregisterTree(String treeId) {
        registeredTrees.remove(treeId);
    }

    /**
     * Weist einem NPC einen Dialogbaum zu
     */
    public void assignTreeToNPC(UUID npcUUID, String treeId) {
        npcTrees.computeIfAbsent(npcUUID, k -> new ArrayList<>()).add(treeId);
    }

    /**
     * Entfernt einen Baum von einem NPC
     */
    public void removeTreeFromNPC(UUID npcUUID, String treeId) {
        List<String> trees = npcTrees.get(npcUUID);
        if (trees != null) {
            trees.remove(treeId);
        }
    }

    /**
     * Holt einen Dialogbaum nach ID
     */
    @Nullable
    public DialogueTree getTree(String treeId) {
        return registeredTrees.get(treeId);
    }

    /**
     * Holt alle Bäume für einen NPC
     */
    public List<DialogueTree> getTreesForNPC(UUID npcUUID) {
        List<String> treeIds = npcTrees.getOrDefault(npcUUID, Collections.emptyList());
        List<DialogueTree> trees = new ArrayList<>();
        for (String id : treeIds) {
            DialogueTree tree = registeredTrees.get(id);
            if (tree != null) {
                trees.add(tree);
            }
        }
        return trees;
    }

    // ═══════════════════════════════════════════════════════════
    // DIALOGUE LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet einen Dialog mit einem NPC
     *
     * @return Der Kontext des Dialogs, oder null wenn kein Dialog gestartet werden kann
     */
    @Nullable
    public DialogueContext startDialogue(ServerPlayer player, CustomNPCEntity npc) {
        // Prüfe ob Spieler bereits in Dialog
        if (activeDialogues.containsKey(player.getUUID())) {
            return null;
        }

        // Finde passenden Dialogbaum
        DialogueTree tree = findBestTree(player, npc);
        if (tree == null) {
            // Kein spezifischer Dialog - verwende generischen
            tree = getOrCreateGenericTree(npc);
        }

        // Kontext erstellen
        DialogueContext context = new DialogueContext(player, npc, tree);

        // Start-Node finden
        DialogueNode startNode = tree.getStartNode(context, npc);
        if (startNode == null) {
            return null;
        }

        // Entry-Aktionen ausführen
        startNode.executeEntryActions(context, npc);
        context.setCurrentNode(startNode);

        // Dialog registrieren
        activeDialogues.put(player.getUUID(), context);

        // NPC-Memory: Gespräch speichern
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData != null) {
            lifeData.getMemory().addMemory(
                player.getUUID(),
                MemoryType.CONVERSATION,
                "Gespräch begonnen",
                2
            );
        }

        return context;
    }

    /**
     * Findet den besten Dialogbaum für die Situation
     */
    @Nullable
    private DialogueTree findBestTree(ServerPlayer player, CustomNPCEntity npc) {
        UUID npcUUID = npc.getNpcData().getNpcUUID();

        // NPC-spezifische Bäume
        List<DialogueTree> trees = getTreesForNPC(npcUUID);

        // Plus globale Bäume die für alle gelten
        for (DialogueTree tree : registeredTrees.values()) {
            if (tree.hasTag("global") && !trees.contains(tree)) {
                trees.add(tree);
            }
        }

        if (trees.isEmpty()) {
            return null;
        }

        // Erstelle temporären Kontext für Bedingungsprüfung
        DialogueContext tempContext = new DialogueContext(player, npc, trees.get(0));

        // Filtere nach Start-Bedingung und sortiere nach Priorität
        return trees.stream()
            .filter(t -> t.canStart(tempContext, npc))
            .max(Comparator.comparingInt(DialogueTree::getPriority))
            .orElse(null);
    }

    /**
     * Erstellt einen generischen Dialogbaum falls kein spezifischer existiert
     */
    private DialogueTree getOrCreateGenericTree(CustomNPCEntity npc) {
        String genericId = "generic_" + npc.getNpcType().name().toLowerCase();

        DialogueTree existing = registeredTrees.get(genericId);
        if (existing != null) {
            return existing;
        }

        // Erstelle generischen Dialog
        DialogueTree generic = new DialogueTree(genericId, "Allgemeiner Dialog")
            .addNode(DialogueNode.simple("start",
                "Hallo, {player}. Wie kann ich Ihnen helfen?",
                DialogueOption.simple("talk", "Ich wollte nur reden.", "chat"),
                DialogueOption.trade("Ich möchte handeln."),
                DialogueOption.exit("Auf Wiedersehen.")
            ))
            .addNode(DialogueNode.end("chat",
                "Das ist schön. Haben Sie noch einen angenehmen Tag."));

        registeredTrees.put(genericId, generic);
        return generic;
    }

    /**
     * Wählt eine Option aus
     */
    public DialogueNode selectOption(ServerPlayer player, String optionId) {
        DialogueContext context = activeDialogues.get(player.getUUID());
        if (context == null || context.isEnded()) {
            return null;
        }

        DialogueNode currentNode = context.getCurrentNode();
        if (currentNode == null) {
            return null;
        }

        // Option finden
        DialogueOption selectedOption = currentNode.getOptions().stream()
            .filter(opt -> opt.getId().equals(optionId))
            .findFirst()
            .orElse(null);

        if (selectedOption == null) {
            return null;
        }

        CustomNPCEntity npc = context.getNpc();

        // Prüfe ob Option wählbar
        if (!selectedOption.isEnabled(context, npc)) {
            return null;
        }

        // Aktionen ausführen
        selectedOption.executeActions(context, npc);

        // Prüfe ob Dialog beendet wurde
        if (context.isEnded()) {
            endDialogue(player);
            return null;
        }

        // Nächsten Node finden
        String nextNodeId = context.consumeNextNodeId(); // Von Aktion gesetzt?
        if (nextNodeId == null) {
            nextNodeId = selectedOption.getTargetNodeId();
        }

        if (nextNodeId == null) {
            // Kein Ziel = Dialog beenden
            endDialogue(player);
            return null;
        }

        // Nächsten Node laden
        DialogueTree tree = context.getTree();
        DialogueNode nextNode = tree.findNextValidNode(nextNodeId, context, npc);

        if (nextNode == null) {
            endDialogue(player);
            return null;
        }

        // Entry-Aktionen
        nextNode.executeEntryActions(context, npc);
        context.setCurrentNode(nextNode);

        return nextNode;
    }

    /**
     * Beendet einen Dialog
     */
    public void endDialogue(ServerPlayer player) {
        DialogueContext context = activeDialogues.remove(player.getUUID());
        if (context != null) {
            context.endDialogue();

            // Memory Update
            CustomNPCEntity npc = context.getNpc();
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData != null) {
                lifeData.getMemory().addMemory(
                    player.getUUID(),
                    MemoryType.CONVERSATION,
                    "Gespräch beendet (" + context.getDurationSeconds() + "s)",
                    1
                );
            }
        }
    }

    /**
     * Holt den aktiven Dialog eines Spielers
     */
    @Nullable
    public DialogueContext getActiveDialogue(ServerPlayer player) {
        return activeDialogues.get(player.getUUID());
    }

    /**
     * Prüft ob ein Spieler in einem Dialog ist
     */
    public boolean isInDialogue(ServerPlayer player) {
        return activeDialogues.containsKey(player.getUUID());
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Speichert dynamische Dialogzuweisungen
     * (Registrierte Bäume werden bei Setup neu erstellt)
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // NPC-Tree-Zuweisungen speichern
        CompoundTag npcTreesTag = new CompoundTag();
        for (Map.Entry<UUID, List<String>> entry : npcTrees.entrySet()) {
            ListTag treeIdList = new ListTag();
            for (String treeId : entry.getValue()) {
                CompoundTag treeIdTag = new CompoundTag();
                treeIdTag.putString("id", treeId);
                treeIdList.add(treeIdTag);
            }
            npcTreesTag.put(entry.getKey().toString(), treeIdList);
        }
        tag.put("npcTrees", npcTreesTag);

        return tag;
    }

    /**
     * Lädt dynamische Dialogzuweisungen
     */
    public void load(CompoundTag tag) {
        // NPC-Tree-Zuweisungen laden
        npcTrees.clear();
        if (tag.contains("npcTrees")) {
            CompoundTag npcTreesTag = tag.getCompound("npcTrees");
            for (String uuidStr : npcTreesTag.getAllKeys()) {
                UUID npcUUID = UUID.fromString(uuidStr);
                List<String> treeIds = new ArrayList<>();
                ListTag treeIdList = npcTreesTag.getList(uuidStr, Tag.TAG_COMPOUND);
                for (int i = 0; i < treeIdList.size(); i++) {
                    treeIds.add(treeIdList.getCompound(i).getString("id"));
                }
                npcTrees.put(npcUUID, treeIds);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("DialogueManager{trees=%d, active=%d}",
            registeredTrees.size(), activeDialogues.size());
    }
}
