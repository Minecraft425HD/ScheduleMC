package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DialogueNode - Ein Knoten im Dialogbaum
 *
 * Ein Node enthält:
 * - Text den der NPC sagt
 * - Optionen die der Spieler wählen kann
 * - Bedingungen und Aktionen
 */
public class DialogueNode {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final String id;

    /** Text den der NPC sagt */
    private String text;

    /** Alternative Texte basierend auf Bedingungen */
    private final List<ConditionalText> conditionalTexts = new ArrayList<>();

    /** Verfügbare Antwortoptionen */
    private final List<DialogueOption> options = new ArrayList<>();

    /** Aktionen die beim Betreten des Nodes ausgeführt werden */
    private final List<DialogueAction> entryActions = new ArrayList<>();

    /** Bedingung um diesen Node zu erreichen */
    private DialogueCondition entryCondition = DialogueCondition.always();

    /** Automatischer nächster Node (wenn keine Optionen) */
    @Nullable
    private String autoNextNodeId;

    /** Verzögerung vor Auto-Weiter (in Ticks) */
    private int autoNextDelay = 0;

    /** Sprechername (überschreibt NPC-Namen) */
    @Nullable
    private String speakerName;

    /** Icon/Bild das angezeigt wird */
    @Nullable
    private String imageId;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR & BUILDER
    // ═══════════════════════════════════════════════════════════

    public DialogueNode(String id, String text) {
        this.id = id;
        this.text = text;
    }

    /**
     * Setzt den Text
     */
    public DialogueNode setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Fügt einen bedingten Text hinzu
     */
    public DialogueNode addConditionalText(DialogueCondition condition, String text) {
        conditionalTexts.add(new ConditionalText(condition, text));
        return this;
    }

    /**
     * Fügt eine Option hinzu
     */
    public DialogueNode addOption(DialogueOption option) {
        options.add(option);
        return this;
    }

    /**
     * Fügt mehrere Optionen hinzu
     */
    public DialogueNode addOptions(DialogueOption... options) {
        for (DialogueOption option : options) {
            this.options.add(option);
        }
        return this;
    }

    /**
     * Fügt eine Entry-Aktion hinzu
     */
    public DialogueNode addEntryAction(DialogueAction action) {
        entryActions.add(action);
        return this;
    }

    /**
     * Setzt die Entry-Bedingung
     */
    public DialogueNode entryCondition(DialogueCondition condition) {
        this.entryCondition = condition;
        return this;
    }

    /**
     * Setzt den automatischen nächsten Node
     */
    public DialogueNode autoNext(String nodeId) {
        this.autoNextNodeId = nodeId;
        return this;
    }

    /**
     * Setzt den automatischen nächsten Node mit Verzögerung
     */
    public DialogueNode autoNext(String nodeId, int delayTicks) {
        this.autoNextNodeId = nodeId;
        this.autoNextDelay = delayTicks;
        return this;
    }

    /**
     * Setzt den Sprechernamen
     */
    public DialogueNode speaker(String name) {
        this.speakerName = name;
        return this;
    }

    /**
     * Setzt das Bild
     */
    public DialogueNode image(String imageId) {
        this.imageId = imageId;
        return this;
    }

    // ═══════════════════════════════════════════════════════════
    // EXECUTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob der Node betreten werden kann
     */
    public boolean canEnter(DialogueContext context, CustomNPCEntity npc) {
        return entryCondition.test(context, npc);
    }

    /**
     * Führt Entry-Aktionen aus
     */
    public void executeEntryActions(DialogueContext context, CustomNPCEntity npc) {
        for (DialogueAction action : entryActions) {
            action.execute(context, npc);
        }
    }

    /**
     * Gibt den anzuzeigenden Text zurück (mit Bedingungen geprüft)
     */
    public String getDisplayText(DialogueContext context, CustomNPCEntity npc) {
        // Prüfe bedingte Texte
        for (ConditionalText ct : conditionalTexts) {
            if (ct.condition.test(context, npc)) {
                return context.processText(ct.text);
            }
        }
        // Standard-Text
        return context.processText(text);
    }

    /**
     * Gibt den Sprechernamen zurück
     */
    public String getSpeakerName(CustomNPCEntity npc) {
        if (speakerName != null) {
            return speakerName;
        }
        return npc.getNpcName();
    }

    /**
     * Gibt die sichtbaren Optionen zurück
     */
    public List<DialogueOption> getVisibleOptions(DialogueContext context, CustomNPCEntity npc) {
        return options.stream()
            .filter(opt -> opt.isVisible(context, npc))
            .sorted(Comparator.comparingInt(DialogueOption::getPriority).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Prüft ob der Node automatisch weitergeht
     */
    public boolean hasAutoNext() {
        return autoNextNodeId != null && options.isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<ConditionalText> getConditionalTexts() {
        return conditionalTexts;
    }

    public List<DialogueOption> getOptions() {
        return options;
    }

    public List<DialogueAction> getEntryActions() {
        return entryActions;
    }

    public DialogueCondition getEntryCondition() {
        return entryCondition;
    }

    @Nullable
    public String getAutoNextNodeId() {
        return autoNextNodeId;
    }

    public int getAutoNextDelay() {
        return autoNextDelay;
    }

    @Nullable
    public String getSpeakerName() {
        return speakerName;
    }

    @Nullable
    public String getImageId() {
        return imageId;
    }

    // ═══════════════════════════════════════════════════════════
    // CONVENIENCE BUILDERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt einen Builder für einen neuen Node
     */
    public static DialogueNode builder(String id) {
        return new DialogueNode(id, "");
    }

    /**
     * Alias für setPriority - Setzt die Priorität (für Fluent API Kompatibilität)
     * Hinweis: DialogueNode hat keine eigene Priorität, diese Methode ist für API-Kompatibilität
     */
    public DialogueNode setPriority(int priority) {
        // DialogueNode hat keine Priorität, aber die Methode existiert für API-Kompatibilität
        return this;
    }

    /**
     * Alias für addCondition - Fügt eine Bedingung hinzu (Entry-Bedingung)
     */
    public DialogueNode addCondition(DialogueCondition condition) {
        this.entryCondition = condition;
        return this;
    }

    /**
     * Alias für setText - Fügt Text hinzu (für Builder-Pattern Kompatibilität)
     */
    public DialogueNode addText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Markiert diesen Node als End-Node
     */
    public DialogueNode setEndNode(boolean isEndNode) {
        if (isEndNode && this.options.isEmpty()) {
            // Füge automatisch Exit-Option hinzu wenn keine Optionen vorhanden
            this.addOption(DialogueOption.exit("Auf Wiedersehen"));
        }
        return this;
    }

    /**
     * Finalisiert den Node (Builder-Pattern)
     */
    public DialogueNode build() {
        return this;
    }

    /**
     * Erstellt einen einfachen Node mit Text und Optionen
     */
    public static DialogueNode simple(String id, String text, DialogueOption... options) {
        DialogueNode node = new DialogueNode(id, text);
        for (DialogueOption option : options) {
            node.addOption(option);
        }
        return node;
    }

    /**
     * Erstellt einen Node der automatisch zum nächsten weitergeht
     */
    public static DialogueNode transition(String id, String text, String nextNodeId) {
        return new DialogueNode(id, text).autoNext(nextNodeId);
    }

    /**
     * Erstellt einen End-Node
     */
    public static DialogueNode end(String id, String text) {
        return new DialogueNode(id, text)
            .addOption(DialogueOption.exit("Auf Wiedersehen"));
    }

    // ═══════════════════════════════════════════════════════════
    // INNER CLASS
    // ═══════════════════════════════════════════════════════════

    /**
     * Bedingter Text
     */
    public static class ConditionalText {
        public final DialogueCondition condition;
        public final String text;

        public ConditionalText(DialogueCondition condition, String text) {
            this.condition = condition;
            this.text = text;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("DialogueNode{id='%s', options=%d, autoNext=%s}",
            id, options.size(), autoNextNodeId);
    }
}
