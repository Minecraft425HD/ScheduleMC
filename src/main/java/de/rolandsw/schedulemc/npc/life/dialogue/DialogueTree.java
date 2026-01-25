package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;

import javax.annotation.Nullable;
import java.util.*;

/**
 * DialogueTree - Ein kompletter Dialogbaum
 *
 * Enthält alle Nodes und verwaltet die Navigation durch den Dialog.
 */
public class DialogueTree {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final String id;
    private final String name;

    /** Alle Nodes im Baum, nach ID */
    private final Map<String, DialogueNode> nodes = new LinkedHashMap<>();

    /** ID des Start-Nodes */
    private String startNodeId = "start";

    /** Alternative Start-Nodes basierend auf Bedingungen */
    private final List<ConditionalStart> conditionalStarts = new ArrayList<>();

    /** Bedingung um diesen Dialog zu starten */
    private DialogueCondition startCondition = DialogueCondition.always();

    /** Priorität wenn mehrere Dialoge verfügbar sind */
    private int priority = 0;

    /** Tags für Kategorisierung */
    private final Set<String> tags = new HashSet<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR & BUILDER
    // ═══════════════════════════════════════════════════════════

    public DialogueTree(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Fügt einen Node hinzu
     */
    public DialogueTree addNode(DialogueNode node) {
        nodes.put(node.getId(), node);
        return this;
    }

    /**
     * Fügt mehrere Nodes hinzu
     */
    public DialogueTree addNodes(DialogueNode... nodes) {
        for (DialogueNode node : nodes) {
            this.nodes.put(node.getId(), node);
        }
        return this;
    }

    /**
     * Setzt den Start-Node
     */
    public DialogueTree setStartNode(String nodeId) {
        this.startNodeId = nodeId;
        return this;
    }

    /**
     * Fügt einen bedingten Start hinzu
     */
    public DialogueTree addConditionalStart(DialogueCondition condition, String nodeId) {
        conditionalStarts.add(new ConditionalStart(condition, nodeId));
        return this;
    }

    /**
     * Setzt die Start-Bedingung
     */
    public DialogueTree startCondition(DialogueCondition condition) {
        this.startCondition = condition;
        return this;
    }

    /**
     * Setzt die Priorität
     */
    public DialogueTree priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Fügt Tags hinzu
     */
    public DialogueTree addTags(String... tags) {
        this.tags.addAll(Arrays.asList(tags));
        return this;
    }

    /**
     * Alias für priority - Setzt die Priorität
     */
    public DialogueTree setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Fügt einen einzelnen Tag hinzu
     */
    public DialogueTree addTag(String tag) {
        this.tags.add(tag);
        return this;
    }

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob der Dialog gestartet werden kann
     */
    public boolean canStart(DialogueContext context, CustomNPCEntity npc) {
        return startCondition.test(context, npc);
    }

    /**
     * Gibt den Start-Node zurück (mit Bedingungen geprüft)
     */
    @Nullable
    public DialogueNode getStartNode(DialogueContext context, CustomNPCEntity npc) {
        // Prüfe bedingte Starts
        for (ConditionalStart cs : conditionalStarts) {
            if (cs.condition.test(context, npc)) {
                return nodes.get(cs.nodeId);
            }
        }
        // Standard-Start
        return nodes.get(startNodeId);
    }

    /**
     * Holt einen Node nach ID
     */
    @Nullable
    public DialogueNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Prüft ob ein Node existiert
     */
    public boolean hasNode(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    /**
     * Findet den nächsten gültigen Node (mit Entry-Bedingungen geprüft)
     */
    @Nullable
    public DialogueNode findNextValidNode(String nodeId, DialogueContext context, CustomNPCEntity npc) {
        DialogueNode node = nodes.get(nodeId);
        if (node == null) return null;

        if (node.canEnter(context, npc)) {
            return node;
        }

        // Wenn Node nicht betreten werden kann, versuche Auto-Next
        if (node.hasAutoNext()) {
            return findNextValidNode(node.getAutoNextNodeId(), context, npc);
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt alle Node-IDs zurück
     */
    public Set<String> getNodeIds() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    /**
     * Gibt alle Nodes zurück
     */
    public Collection<DialogueNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
     * Zählt die Nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * Prüft ob der Baum einen Tag hat
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStartNodeId() {
        return startNodeId;
    }

    public DialogueCondition getStartCondition() {
        return startCondition;
    }

    public int getPriority() {
        return priority;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    // ═══════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Validiert den Dialogbaum
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        // Start-Node existiert?
        if (!nodes.containsKey(startNodeId)) {
            errors.add("Start-Node '" + startNodeId + "' existiert nicht");
        }

        // Alle referenzierten Nodes existieren?
        for (DialogueNode node : nodes.values()) {
            // Auto-Next
            if (node.getAutoNextNodeId() != null && !nodes.containsKey(node.getAutoNextNodeId())) {
                errors.add("Node '" + node.getId() + "': Auto-Next '" +
                          node.getAutoNextNodeId() + "' existiert nicht");
            }

            // Option Targets
            for (DialogueOption option : node.getOptions()) {
                String target = option.getTargetNodeId();
                if (target != null && !nodes.containsKey(target)) {
                    errors.add("Node '" + node.getId() + "', Option '" + option.getId() +
                              "': Target '" + target + "' existiert nicht");
                }
            }
        }

        // Bedingte Starts
        for (ConditionalStart cs : conditionalStarts) {
            if (!nodes.containsKey(cs.nodeId)) {
                errors.add("Bedingter Start '" + cs.nodeId + "' existiert nicht");
            }
        }

        return errors;
    }

    /**
     * Prüft ob der Baum gültig ist
     */
    public boolean isValid() {
        return validate().isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    // INNER CLASS
    // ═══════════════════════════════════════════════════════════

    /**
     * Bedingter Start
     */
    public static class ConditionalStart {
        public final DialogueCondition condition;
        public final String nodeId;

        public ConditionalStart(DialogueCondition condition, String nodeId) {
            this.condition = condition;
            this.nodeId = nodeId;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("DialogueTree{id='%s', name='%s', nodes=%d, priority=%d}",
            id, name, nodes.size(), priority);
    }

    /**
     * Gibt eine grafische Darstellung des Baums zurück
     */
    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DialogueTree: ").append(name).append("\n");
        sb.append("Start: ").append(startNodeId).append("\n\n");

        for (DialogueNode node : nodes.values()) {
            sb.append("[").append(node.getId()).append("]\n");
            sb.append("  Text: ").append(node.getText().length() > 50 ?
                node.getText().substring(0, 50) + "..." : node.getText()).append("\n");

            if (node.hasAutoNext()) {
                sb.append("  -> Auto: ").append(node.getAutoNextNodeId()).append("\n");
            }

            for (DialogueOption opt : node.getOptions()) {
                sb.append("  [").append(opt.getId()).append("] ")
                  .append(opt.getText());
                if (opt.getTargetNodeId() != null) {
                    sb.append(" -> ").append(opt.getTargetNodeId());
                } else {
                    sb.append(" -> END");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
