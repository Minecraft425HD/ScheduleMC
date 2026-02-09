package de.rolandsw.schedulemc.npc.life.dialogue;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DialogueManager - Verwaltet aktive Dialoge und Dialogbäume mit JSON-Persistenz
 *
 * Verantwortlich für:
 * - Starten und Beenden von Dialogen
 * - Navigation durch Dialogbäume
 * - Registrierung von Dialogbäumen für NPCs
 */
public class DialogueManager extends AbstractPersistenceManager<DialogueManager.DialogueManagerData> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile DialogueManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static DialogueManager getInstance() {
        return instance;
    }

    public static DialogueManager getInstance(MinecraftServer server) {
        DialogueManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new DialogueManager(server);
                }
            }
        }
        return result;
    }

    /**
     * Gets manager instance for a specific level (convenience method).
     * Note: Manager is server-wide, not per-level.
     */
    public static DialogueManager getManager(ServerLevel level) {
        return getInstance(level.getServer());
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private MinecraftServer server;

    /** Registrierte Dialogbäume: TreeID -> Tree (TRANSIENT - nicht persistiert) */
    private final Map<String, DialogueTree> registeredTrees = new ConcurrentHashMap<>();

    /** NPC-spezifische Bäume: NPC UUID -> Liste von Tree IDs */
    private final Map<UUID, List<String>> npcTrees = new ConcurrentHashMap<>();

    /** Aktive Dialoge: Player UUID -> Context (TRANSIENT - nicht persistiert) */
    private final Map<UUID, DialogueContext> activeDialogues = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private DialogueManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_dialogues.json").toFile(),
            GsonHelper.get()
        );
        this.server = server;
        load();
    }

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
        npcTrees.computeIfAbsent(npcUUID, k -> new CopyOnWriteArrayList<>()).add(treeId);
        markDirty();
    }

    /**
     * Entfernt einen Baum von einem NPC
     */
    public void removeTreeFromNPC(UUID npcUUID, String treeId) {
        List<String> trees = npcTrees.get(npcUUID);
        if (trees != null) {
            trees.remove(treeId);
            markDirty();
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

        // Filtere nach Start-Bedingung und finde höchste Priorität
        DialogueTree best = null;
        int bestPriority = Integer.MIN_VALUE;
        for (DialogueTree t : trees) {
            if (t.canStart(tempContext, npc)) {
                int prio = t.getPriority();
                if (prio > bestPriority) {
                    bestPriority = prio;
                    best = t;
                }
            }
        }
        return best;
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
        DialogueOption selectedOption = null;
        for (DialogueOption opt : currentNode.getOptions()) {
            if (opt.getId().equals(optionId)) {
                selectedOption = opt;
                break;
            }
        }

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
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<DialogueManagerData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(DialogueManagerData data) {
        npcTrees.clear();

        int invalidCount = 0;
        int correctedCount = 0;

        // Validate and load npcTrees
        if (data.npcTrees != null) {
            // Check collection size
            if (data.npcTrees.size() > 10000) {
                LOGGER.warn("NPC dialogue trees map size ({}) exceeds limit, potential corruption",
                    data.npcTrees.size());
                correctedCount++;
            }

            for (Map.Entry<UUID, List<String>> entry : data.npcTrees.entrySet()) {
                try {
                    UUID npcUUID = entry.getKey();
                    List<String> treeIds = entry.getValue();

                    // NULL CHECK
                    if (treeIds == null) {
                        LOGGER.warn("Null tree IDs list for NPC {}, skipping", npcUUID);
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE LIST SIZE
                    if (treeIds.size() > 100) {
                        LOGGER.warn("NPC {} has too many tree IDs ({}), truncating to 100",
                            npcUUID, treeIds.size());
                        treeIds = new ArrayList<>(treeIds.subList(0, 100));
                        correctedCount++;
                    }

                    // VALIDATE TREE IDS - check for null or empty strings
                    List<String> validTreeIds = new ArrayList<>();
                    for (String treeId : treeIds) {
                        if (treeId == null || treeId.isEmpty()) {
                            LOGGER.warn("NPC {} has null/empty tree ID, skipping", npcUUID);
                            invalidCount++;
                            continue;
                        }
                        if (treeId.length() > 200) {
                            LOGGER.warn("NPC {} has too long tree ID ({} chars), skipping",
                                npcUUID, treeId.length());
                            invalidCount++;
                            continue;
                        }
                        validTreeIds.add(treeId);
                    }

                    if (validTreeIds.size() != treeIds.size()) {
                        correctedCount++;
                    }

                    npcTrees.put(npcUUID, validTreeIds);
                } catch (Exception e) {
                    LOGGER.error("Error loading dialogue trees for NPC {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // SUMMARY
        if (invalidCount > 0 || correctedCount > 0) {
            LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                invalidCount, correctedCount);
            if (correctedCount > 0) {
                markDirty(); // Re-save corrected data
            }
        }
    }

    @Override
    protected DialogueManagerData getCurrentData() {
        DialogueManagerData data = new DialogueManagerData();
        data.npcTrees = new HashMap<>(npcTrees);
        return data;
    }

    @Override
    protected String getComponentName() {
        return "DialogueManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d trees, %d active", registeredTrees.size(), activeDialogues.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        npcTrees.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASS FOR JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public static class DialogueManagerData {
        public Map<UUID, List<String>> npcTrees;
    }

    @Override
    public String toString() {
        return String.format("DialogueManager{trees=%d, active=%d}",
            registeredTrees.size(), activeDialogues.size());
    }
}
