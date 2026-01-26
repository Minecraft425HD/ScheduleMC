package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.*;

/**
 * DialogueContext - Kontext einer aktiven Dialogsitzung
 *
 * Speichert den aktuellen Zustand des Dialogs, besuchte Nodes,
 * Flags und Variablen.
 */
public class DialogueContext {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final ServerPlayer player;
    private final CustomNPCEntity npc;
    private final DialogueTree tree;

    /** Aktueller Node im Dialogbaum */
    @Nullable
    private DialogueNode currentNode;

    /** ID des nächsten Nodes (für Sprünge) */
    @Nullable
    private String nextNodeId;

    /** Besuchte Node-IDs in dieser Sitzung */
    private final Set<String> visitedNodes = new HashSet<>();

    /** Flags die in dieser Sitzung gesetzt wurden */
    private final Set<String> sessionFlags = new HashSet<>();

    /** Variablen die in dieser Sitzung gesetzt wurden */
    private final Map<String, Object> sessionVariables = new HashMap<>();

    /** Ob der Dialog beendet wurde */
    private boolean ended = false;

    /** Anzahl der durchlaufenen Nodes (für Schleifen-Schutz) */
    private int nodeCount = 0;
    private static final int MAX_NODES = 100;

    /** Startzeit des Dialogs */
    private final long startTime;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public DialogueContext(ServerPlayer player, CustomNPCEntity npc, DialogueTree tree) {
        this.player = player;
        this.npc = npc;
        this.tree = tree;
        this.startTime = System.currentTimeMillis();
    }

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt den aktuellen Node
     */
    public void setCurrentNode(@Nullable DialogueNode node) {
        this.currentNode = node;
        if (node != null) {
            visitedNodes.add(node.getId());
            nodeCount++;
        }
    }

    /**
     * Holt den aktuellen Node
     */
    @Nullable
    public DialogueNode getCurrentNode() {
        return currentNode;
    }

    /**
     * Setzt die ID des nächsten Nodes (für Sprünge)
     */
    public void setNextNodeId(@Nullable String nodeId) {
        this.nextNodeId = nodeId;
    }

    /**
     * Holt und löscht die nächste Node-ID
     */
    @Nullable
    public String consumeNextNodeId() {
        String id = nextNodeId;
        nextNodeId = null;
        return id;
    }

    /**
     * Prüft ob ein Node bereits besucht wurde
     */
    public boolean hasVisitedNode(String nodeId) {
        return visitedNodes.contains(nodeId);
    }

    /**
     * Gibt alle besuchten Nodes zurück
     */
    public Set<String> getVisitedNodes() {
        return Collections.unmodifiableSet(visitedNodes);
    }

    /**
     * Prüft ob zu viele Nodes durchlaufen wurden (Schleifen-Schutz)
     */
    public boolean hasExceededNodeLimit() {
        return nodeCount > MAX_NODES;
    }

    // ═══════════════════════════════════════════════════════════
    // FLAGS
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt ein Flag
     */
    public void setFlag(String flag) {
        sessionFlags.add(flag);
    }

    /**
     * Löscht ein Flag
     */
    public void clearFlag(String flag) {
        sessionFlags.remove(flag);
    }

    /**
     * Prüft ob ein Flag gesetzt ist
     */
    public boolean hasFlag(String flag) {
        return sessionFlags.contains(flag);
    }

    /**
     * Gibt alle Flags zurück
     */
    public Set<String> getFlags() {
        return Collections.unmodifiableSet(sessionFlags);
    }

    // ═══════════════════════════════════════════════════════════
    // VARIABLES
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt eine Variable
     */
    public void setVariable(String key, Object value) {
        sessionVariables.put(key, value);
    }

    /**
     * Holt eine Variable
     */
    @Nullable
    public Object getVariable(String key) {
        return sessionVariables.get(key);
    }

    /**
     * Holt eine Variable mit Default-Wert
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, T defaultValue) {
        Object value = sessionVariables.get(key);
        if (value == null) return defaultValue;
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Prüft ob eine Variable existiert
     */
    public boolean hasVariable(String key) {
        return sessionVariables.containsKey(key);
    }

    /**
     * Löscht eine Variable
     */
    public void clearVariable(String key) {
        sessionVariables.remove(key);
    }

    // ═══════════════════════════════════════════════════════════
    // DIALOGUE STATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Beendet den Dialog
     */
    public void endDialogue() {
        this.ended = true;
    }

    /**
     * Prüft ob der Dialog beendet ist
     */
    public boolean isEnded() {
        return ended;
    }

    /**
     * Gibt die Dauer des Dialogs in Millisekunden zurück
     */
    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Gibt die Dauer des Dialogs in Sekunden zurück
     */
    public int getDurationSeconds() {
        return (int) (getDuration() / 1000);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public ServerPlayer getPlayer() {
        return player;
    }

    public CustomNPCEntity getNpc() {
        return npc;
    }

    public DialogueTree getTree() {
        return tree;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    // ═══════════════════════════════════════════════════════════
    // TEXT REPLACEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Ersetzt Platzhalter in Text
     *
     * Unterstützte Platzhalter:
     * - {player} -> Spielername
     * - {npc} -> NPC-Name
     * - {var:key} -> Variable aus Context
     * - {emotion} -> Aktuelle NPC-Emotion
     */
    public String processText(String text) {
        if (text == null) return "";

        String result = text;

        // Spieler
        result = result.replace("{player}", player.getName().getString());

        // NPC
        result = result.replace("{npc}", npc.getNpcName());

        // Emotion
        if (npc.getLifeData() != null) {
            result = result.replace("{emotion}",
                npc.getLifeData().getEmotions().getCurrentEmotion().getDisplayName());
        }

        // Variablen
        for (Map.Entry<String, Object> entry : sessionVariables.entrySet()) {
            result = result.replace("{var:" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }

        return result;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("DialogueContext{player=%s, npc=%s, currentNode=%s, visited=%d, ended=%s}",
            player.getName().getString(),
            npc.getNpcName(),
            currentNode != null ? currentNode.getId() : "null",
            visitedNodes.size(),
            ended
        );
    }
}
