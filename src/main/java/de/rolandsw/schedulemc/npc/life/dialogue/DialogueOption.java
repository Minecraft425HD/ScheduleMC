package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * DialogueOption - Eine Antwortoption die der Spieler wählen kann
 *
 * Optionen können Bedingungen haben (wann sichtbar/wählbar),
 * Aktionen auslösen und zu einem anderen Node führen.
 */
public class DialogueOption {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final String id;
    private final String text;

    /** ID des Zielnodes (null = Dialog beenden) */
    @Nullable
    private String targetNodeId;

    /** Bedingung für Sichtbarkeit */
    private DialogueCondition visibilityCondition = DialogueCondition.always();

    /** Bedingung für Wählbarkeit (kann sichtbar aber ausgegraut sein) */
    private DialogueCondition enabledCondition = DialogueCondition.always();

    /** Text der angezeigt wird wenn Option nicht wählbar */
    private String disabledReason = "";

    /** Aktionen die bei Auswahl ausgeführt werden */
    private final List<DialogueAction> actions = new ArrayList<>();

    /** Priorität für Sortierung (höher = weiter oben) */
    private int priority = 0;

    /** Tooltip/Hinweis */
    private String tooltip = "";

    /** Icon-ID (optional) */
    @Nullable
    private String iconId;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR & BUILDER
    // ═══════════════════════════════════════════════════════════

    public DialogueOption(String id, String text) {
        this.id = id;
        this.text = text;
    }

    /**
     * Setzt den Zielnode
     */
    public DialogueOption targetNode(String nodeId) {
        this.targetNodeId = nodeId;
        return this;
    }

    /**
     * Setzt die Sichtbarkeits-Bedingung
     */
    public DialogueOption visibleWhen(DialogueCondition condition) {
        this.visibilityCondition = condition;
        return this;
    }

    /**
     * Setzt die Wählbarkeits-Bedingung
     */
    public DialogueOption enabledWhen(DialogueCondition condition) {
        this.enabledCondition = condition;
        return this;
    }

    /**
     * Setzt den Grund warum die Option nicht wählbar ist
     */
    public DialogueOption disabledReason(String reason) {
        this.disabledReason = reason;
        return this;
    }

    /**
     * Fügt eine Aktion hinzu
     */
    public DialogueOption addAction(DialogueAction action) {
        this.actions.add(action);
        return this;
    }

    /**
     * Fügt mehrere Aktionen hinzu
     */
    public DialogueOption addActions(DialogueAction... actions) {
        for (DialogueAction action : actions) {
            this.actions.add(action);
        }
        return this;
    }

    /**
     * Setzt die Priorität
     */
    public DialogueOption priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Setzt den Tooltip
     */
    public DialogueOption tooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    /**
     * Setzt das Icon
     */
    public DialogueOption icon(String iconId) {
        this.iconId = iconId;
        return this;
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob die Option sichtbar ist
     */
    public boolean isVisible(DialogueContext context, CustomNPCEntity npc) {
        return visibilityCondition.test(context, npc);
    }

    /**
     * Prüft ob die Option wählbar ist
     */
    public boolean isEnabled(DialogueContext context, CustomNPCEntity npc) {
        return enabledCondition.test(context, npc);
    }

    /**
     * Führt alle Aktionen aus
     */
    public void executeActions(DialogueContext context, CustomNPCEntity npc) {
        for (DialogueAction action : actions) {
            action.execute(context, npc);
        }
    }

    /**
     * Gibt den verarbeiteten Text zurück (mit Platzhaltern ersetzt)
     */
    public String getProcessedText(DialogueContext context) {
        return context.processText(text);
    }

    /**
     * Gibt den verarbeiteten deaktivierten Grund zurück
     */
    public String getProcessedDisabledReason(DialogueContext context) {
        return context.processText(disabledReason);
    }

    /**
     * Gibt den verarbeiteten Tooltip zurück
     */
    public String getProcessedTooltip(DialogueContext context) {
        return context.processText(tooltip);
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

    @Nullable
    public String getTargetNodeId() {
        return targetNodeId;
    }

    public DialogueCondition getVisibilityCondition() {
        return visibilityCondition;
    }

    public DialogueCondition getEnabledCondition() {
        return enabledCondition;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public List<DialogueAction> getActions() {
        return actions;
    }

    public int getPriority() {
        return priority;
    }

    public String getTooltip() {
        return tooltip;
    }

    @Nullable
    public String getIconId() {
        return iconId;
    }

    // ═══════════════════════════════════════════════════════════
    // CONVENIENCE BUILDERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt eine einfache Option die zu einem anderen Node führt
     */
    public static DialogueOption simple(String id, String text, String targetNodeId) {
        return new DialogueOption(id, text).targetNode(targetNodeId);
    }

    /**
     * Erstellt eine Option die den Dialog beendet
     */
    public static DialogueOption exit(String text) {
        return new DialogueOption("exit", text)
            .targetNode(null)
            .addAction(DialogueAction.endDialogue());
    }

    /**
     * Erstellt eine Option die zum Handel führt
     */
    public static DialogueOption trade(String text) {
        return new DialogueOption("trade", text)
            .targetNode(null)
            .addAction(DialogueAction.openTradeMenu());
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("DialogueOption{id='%s', text='%s', target='%s', actions=%d}",
            id, text.length() > 30 ? text.substring(0, 30) + "..." : text,
            targetNodeId, actions.size());
    }
}
