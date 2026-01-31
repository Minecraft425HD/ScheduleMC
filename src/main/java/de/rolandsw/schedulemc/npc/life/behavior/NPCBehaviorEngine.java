package de.rolandsw.schedulemc.npc.life.behavior;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.*;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * NPCBehaviorEngine - Haupt-Entscheidungssystem für NPC-Verhalten
 *
 * Features:
 * - Prioritätsbasierte Aktions-Auswahl
 * - Integration mit Life-System (Needs, Emotions, Traits)
 * - Reaktion auf Umgebungsereignisse
 * - Zeitplan-Integration (Arbeit, Freizeit, Schlaf)
 */
public class NPCBehaviorEngine {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final CustomNPCEntity npc;
    private BehaviorState currentState = BehaviorState.IDLE;

    // Aktive Aktion
    @Nullable
    private BehaviorAction currentAction;

    // Verfügbare Aktionen (sortiert nach Priorität, höchste zuerst)
    private final List<BehaviorAction> availableActions = new ArrayList<>();
    /** Index: BehaviorState → Aktionen für schnellen Lookup statt linearer Suche */
    private final Map<BehaviorState, List<BehaviorAction>> actionsByState = new EnumMap<>(BehaviorState.class);

    // Aktion-History (für Debug)
    private final Deque<String> actionHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY = 10;

    // Performance: Tick-Throttling
    private int tickCounter = 0;
    private static final int DECISION_INTERVAL = 10; // Alle 10 Ticks entscheiden

    // Notfall-Modus
    private boolean inEmergency = false;
    private int emergencyTicksRemaining = 0;

    // Temporäre Zustände
    @Nullable
    private Player lastInteractingPlayer;
    private int interactionCooldown = 0;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public NPCBehaviorEngine(CustomNPCEntity npc) {
        this.npc = npc;
        registerDefaultActions();
    }

    /**
     * Registriert die Standard-Aktionen
     */
    private void registerDefaultActions() {
        // Standard-Aktionen werden hier registriert
        // Die spezifischen Action-Klassen werden separat implementiert
        // Beispiele: FleeAction, AlertPoliceAction, InvestigateAction, etc.
    }

    /**
     * Fügt eine Aktion zum Pool hinzu (sorted insertion statt sort() nach jedem add).
     */
    public void registerAction(BehaviorAction action) {
        // Sorted insert: höchste Priorität zuerst
        int insertIndex = 0;
        int actionPriority = action.getPriority().getValue();
        for (int i = 0; i < availableActions.size(); i++) {
            if (availableActions.get(i).getPriority().getValue() < actionPriority) {
                break;
            }
            insertIndex = i + 1;
        }
        availableActions.add(insertIndex, action);
        // State-Index aktualisieren
        actionsByState.computeIfAbsent(action.getResultState(), k -> new ArrayList<>()).add(action);
    }

    /**
     * Entfernt eine Aktion aus dem Pool
     */
    public void unregisterAction(String actionId) {
        availableActions.removeIf(a -> {
            if (a.getId().equals(actionId)) {
                List<BehaviorAction> stateList = actionsByState.get(a.getResultState());
                if (stateList != null) stateList.remove(a);
                return true;
            }
            return false;
        });
    }

    /**
     * Findet die erste ausführbare Aktion für einen BehaviorState via Index (O(1) statt O(n)).
     */
    @Nullable
    private BehaviorAction findExecutableByState(BehaviorState state) {
        List<BehaviorAction> actions = actionsByState.get(state);
        if (actions == null) return null;
        for (BehaviorAction action : actions) {
            if (action.canExecute(npc)) return action;
        }
        return null;
    }

    /**
     * Findet die erste ausführbare Aktion für einen von mehreren States.
     */
    @Nullable
    private BehaviorAction findExecutableByStates(BehaviorState... states) {
        for (BehaviorState state : states) {
            BehaviorAction action = findExecutableByState(state);
            if (action != null) return action;
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Haupt-Tick-Methode - wird vom NPC aufgerufen
     */
    public void tick() {
        tickCounter++;
        interactionCooldown = Math.max(0, interactionCooldown - 1);

        // Notfall-Timer
        if (inEmergency) {
            emergencyTicksRemaining--;
            if (emergencyTicksRemaining <= 0) {
                endEmergency();
            }
        }

        // Aktive Aktion ausführen
        if (currentAction != null && currentAction.isRunning()) {
            boolean continueAction = currentAction.executeTick(npc);
            if (!continueAction) {
                finishCurrentAction(false);
            }
        }

        // Regelmäßige Entscheidung (Performance)
        if (tickCounter >= DECISION_INTERVAL) {
            tickCounter = 0;
            makeDecision();
        }
    }

    /**
     * Trifft eine Entscheidung über das nächste Verhalten
     */
    private void makeDecision() {
        // Wenn aktuelle Aktion läuft und nicht unterbrechbar, warten
        if (currentAction != null && currentAction.isRunning()) {
            if (!currentState.canBeInterrupted()) {
                return;
            }
        }

        // Beste verfügbare Aktion finden
        BehaviorAction bestAction = findBestAction();

        // Wenn bessere Aktion gefunden, wechseln
        if (bestAction != null && bestAction != currentAction) {
            if (currentAction == null || bestAction.canOverride(currentAction)) {
                switchToAction(bestAction);
            }
        }
    }

    /**
     * Findet die beste verfügbare Aktion basierend auf aktueller Situation.
     * Optimiert: Nutzt State-Index statt wiederholter linearer Suche.
     */
    @Nullable
    private BehaviorAction findBestAction() {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return null;

        // Notfall-Check: Emotionen und Bedürfnisse
        if (lifeData.getEmotions().wouldFlee()) {
            BehaviorAction flee = findExecutableByState(BehaviorState.FLEEING);
            if (flee != null) return flee;
        }

        if (lifeData.getNeeds().isCritical(NeedType.SAFETY)) {
            BehaviorAction safety = findExecutableByStates(BehaviorState.HIDING, BehaviorState.ALERTING);
            if (safety != null) return safety;
        }

        // Trait-basierte Entscheidung: Mutige NPCs untersuchen eher
        if (lifeData.getTraits().wouldInvestigate() && ThreadLocalRandom.current().nextDouble() < 0.3) {
            BehaviorAction investigate = findExecutableByState(BehaviorState.INVESTIGATING);
            if (investigate != null) return investigate;
        }

        // Standard: Erste passende Aktion mit höchster Priorität
        for (BehaviorAction action : availableActions) {
            if (action.canExecute(npc)) {
                return action;
            }
        }

        return null;
    }

    /**
     * Wechselt zu einer neuen Aktion
     */
    private void switchToAction(BehaviorAction newAction) {
        // Aktuelle Aktion beenden
        if (currentAction != null && currentAction.isRunning()) {
            currentAction.end(npc, true);
        }

        // Neue Aktion starten
        currentAction = newAction;
        currentState = newAction.getResultState();
        newAction.begin(npc);

        // History
        addToHistory(newAction.getId());

        // Emergency-Check
        if (currentState.isEmergency() && !inEmergency) {
            startEmergency();
        }
    }

    /**
     * Beendet die aktuelle Aktion
     */
    private void finishCurrentAction(boolean interrupted) {
        if (currentAction != null) {
            currentAction.end(npc, interrupted);
            currentAction = null;
        }
        currentState = BehaviorState.IDLE;
    }

    // ═══════════════════════════════════════════════════════════
    // EMERGENCY HANDLING
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet den Notfall-Modus
     */
    private void startEmergency() {
        inEmergency = true;
        emergencyTicksRemaining = 6000; // 5 Minuten

        // Emotion auslösen
        if (npc.getLifeData() != null) {
            npc.getLifeData().getEmotions().trigger(EmotionState.FEARFUL, 70.0f);
        }
    }

    /**
     * Beendet den Notfall-Modus
     */
    private void endEmergency() {
        inEmergency = false;
        emergencyTicksRemaining = 0;
    }

    /**
     * Erzwingt einen Notfall-Zustand
     */
    public void triggerEmergency(BehaviorState emergencyState, int durationTicks) {
        inEmergency = true;
        emergencyTicksRemaining = durationTicks;
        currentState = emergencyState;
    }

    // ═══════════════════════════════════════════════════════════
    // EXTERNAL TRIGGERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird aufgerufen wenn ein Spieler den NPC anspricht
     */
    public void onPlayerInteract(Player player) {
        if (interactionCooldown > 0) return;

        lastInteractingPlayer = player;
        interactionCooldown = 60; // 3 Sekunden Cooldown

        // Entscheidung basierend auf Emotion und Traits
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        // Wenn in Notfall, Interaktion ignorieren
        if (inEmergency) return;

        // Wenn nicht gesprächsbereit, kurze Antwort
        if (!lifeData.isWillingToTalk()) {
            // Kurze Abweisung
            return;
        }

        // Wechsle zu Konversations-Zustand
        currentState = BehaviorState.CONVERSING;
    }

    /**
     * Wird aufgerufen wenn der NPC Zeuge eines Verbrechens wird
     */
    public void onWitnessCrime(Player criminal, String crimeType, int severity) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        // Erinnerung speichern
        lifeData.getMemory().addMemory(
            criminal.getUUID(),
            MemoryType.CRIME_WITNESSED,
            "Zeuge: " + crimeType,
            severity + 2
        );

        // Tag hinzufügen
        lifeData.getMemory().addPlayerTag(criminal.getUUID(), "Kriminell");

        // Emotion auslösen
        if (severity >= 5) {
            lifeData.getEmotions().trigger(EmotionState.FEARFUL, severity * 10.0f);
        } else {
            lifeData.getEmotions().trigger(EmotionState.SUSPICIOUS, severity * 8.0f);
        }

        // Verhalten: Entscheidung ob melden oder nicht (nutze State-Index)
        if (lifeData.getTraits().wouldReport(severity)) {
            BehaviorAction alert = findExecutableByState(BehaviorState.ALERTING);
            if (alert != null) { switchToAction(alert); return; }
        }

        // Wenn nicht melden, vielleicht fliehen
        if (severity >= 7 || lifeData.getEmotions().wouldFlee()) {
            BehaviorAction flee = findExecutableByState(BehaviorState.FLEEING);
            if (flee != null) { switchToAction(flee); return; }
        }
    }

    /**
     * Wird aufgerufen wenn der NPC bedroht wird
     */
    public void onThreatened(Player attacker, float threatLevel) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        // Sicherheit senken
        lifeData.getNeeds().modifySafety(-threatLevel * 20);

        // Erinnerung speichern
        lifeData.getMemory().addMemory(
            attacker.getUUID(),
            MemoryType.THREAT_RECEIVED,
            "Bedrohung (Level " + (int) threatLevel + ")",
            (int) (threatLevel + 5)
        );

        // Tags
        lifeData.getMemory().addPlayerTag(attacker.getUUID(), "Gefährlich");
        if (threatLevel > 5) {
            lifeData.getMemory().addPlayerTag(attacker.getUUID(), "Gewalttätig");
        }

        // Emotion
        lifeData.getEmotions().trigger(EmotionState.FEARFUL, threatLevel * 15.0f);

        // Verhalten: Je nach Mut fliehen oder nicht (nutze State-Index)
        float fearThreshold = lifeData.getTraits().getFearThreshold();
        if (threatLevel * 10 > fearThreshold) {
            BehaviorAction flee = findExecutableByState(BehaviorState.FLEEING);
            if (flee != null) {
                flee.setTargetEntity(attacker);
                switchToAction(flee);
            }
        }
    }

    /**
     * Wird aufgerufen wenn etwas Verdächtiges passiert
     */
    public void onSuspiciousActivity(@Nullable Player suspect, String description) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        // Emotion
        lifeData.getEmotions().trigger(EmotionState.SUSPICIOUS, 40.0f);

        // Wenn mutig genug, untersuchen (nutze State-Index)
        if (lifeData.getTraits().wouldInvestigate()) {
            BehaviorAction investigate = findExecutableByState(BehaviorState.INVESTIGATING);
            if (investigate != null) {
                if (suspect != null) investigate.setTargetEntity(suspect);
                switchToAction(investigate);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Erzwingt einen bestimmten Zustand (für Quests, etc.)
     */
    public void forceState(BehaviorState state, int durationTicks) {
        finishCurrentAction(true);
        currentState = state;
        if (state.isEmergency()) {
            triggerEmergency(state, durationTicks);
        }
    }

    /**
     * Setzt den Zustand auf Idle zurück
     */
    public void resetToIdle() {
        finishCurrentAction(false);
        currentState = BehaviorState.IDLE;
        inEmergency = false;
    }

    // ═══════════════════════════════════════════════════════════
    // HISTORY / DEBUG
    // ═══════════════════════════════════════════════════════════

    private void addToHistory(String actionId) {
        if (actionHistory.size() >= MAX_HISTORY) {
            actionHistory.removeLast();
        }
        actionHistory.addFirst(actionId);
    }

    public Collection<String> getActionHistory() {
        return Collections.unmodifiableCollection(actionHistory);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public BehaviorState getCurrentState() {
        return currentState;
    }

    @Nullable
    public BehaviorAction getCurrentAction() {
        return currentAction;
    }

    public boolean isInEmergency() {
        return inEmergency;
    }

    public int getEmergencyTicksRemaining() {
        return emergencyTicksRemaining;
    }

    @Nullable
    public Player getLastInteractingPlayer() {
        return lastInteractingPlayer;
    }

    public List<BehaviorAction> getAvailableActions() {
        return Collections.unmodifiableList(availableActions);
    }

    @Override
    public String toString() {
        return String.format("NPCBehaviorEngine{state=%s, action=%s, emergency=%s}",
            currentState.name(),
            currentAction != null ? currentAction.getId() : "none",
            inEmergency
        );
    }
}
