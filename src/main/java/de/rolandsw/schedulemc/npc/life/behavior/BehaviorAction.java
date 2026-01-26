package de.rolandsw.schedulemc.npc.life.behavior;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * BehaviorAction - Abstrakte Basisklasse für NPC-Verhalten
 *
 * Jede Aktion hat:
 * - Priorität (bestimmt Ausführungsreihenfolge)
 * - Bedingungen (wann kann sie ausgeführt werden)
 * - Ausführungslogik (was passiert)
 * - Abbruchlogik (wann wird abgebrochen)
 */
public abstract class BehaviorAction {

    protected final String id;
    protected final String displayName;
    protected final BehaviorPriority priority;
    protected final BehaviorState resultState;

    // Laufzeit-Daten
    protected boolean isRunning = false;
    protected int ticksRunning = 0;
    protected int maxDurationTicks = -1; // -1 = unbegrenzt

    // Ziel der Aktion (optional)
    @Nullable
    protected Entity targetEntity;

    public BehaviorAction(String id, String displayName, BehaviorPriority priority, BehaviorState resultState) {
        this.id = id;
        this.displayName = displayName;
        this.priority = priority;
        this.resultState = resultState;
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob diese Aktion aktuell ausgeführt werden kann
     *
     * @param npc Der NPC der die Aktion ausführen soll
     * @return true wenn die Bedingungen erfüllt sind
     */
    public abstract boolean canExecute(CustomNPCEntity npc);

    /**
     * Startet die Aktion
     *
     * @param npc Der ausführende NPC
     */
    public abstract void start(CustomNPCEntity npc);

    /**
     * Wird jeden Tick aufgerufen während die Aktion läuft
     *
     * @param npc Der ausführende NPC
     * @return true wenn die Aktion fortgesetzt werden soll, false wenn beendet
     */
    public abstract boolean tick(CustomNPCEntity npc);

    /**
     * Stoppt die Aktion (normal oder durch Interrupt)
     *
     * @param npc Der ausführende NPC
     * @param interrupted true wenn durch höhere Priorität unterbrochen
     */
    public abstract void stop(CustomNPCEntity npc, boolean interrupted);

    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet die Aktion mit internem State-Management
     */
    public final void begin(CustomNPCEntity npc) {
        this.isRunning = true;
        this.ticksRunning = 0;
        start(npc);
    }

    /**
     * Führt einen Tick aus und prüft auf Timeout
     *
     * @return true wenn fortgesetzt werden soll
     */
    public final boolean executeTick(CustomNPCEntity npc) {
        if (!isRunning) return false;

        ticksRunning++;

        // Timeout-Check
        if (maxDurationTicks > 0 && ticksRunning >= maxDurationTicks) {
            end(npc, false);
            return false;
        }

        return tick(npc);
    }

    /**
     * Beendet die Aktion
     */
    public final void end(CustomNPCEntity npc, boolean interrupted) {
        if (!isRunning) return;

        this.isRunning = false;
        stop(npc, interrupted);
        this.targetEntity = null;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BehaviorPriority getPriority() {
        return priority;
    }

    public BehaviorState getResultState() {
        return resultState;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getTicksRunning() {
        return ticksRunning;
    }

    public int getSecondsRunning() {
        return ticksRunning / 20;
    }

    @Nullable
    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(@Nullable Entity target) {
        this.targetEntity = target;
    }

    public void setMaxDuration(int ticks) {
        this.maxDurationTicks = ticks;
    }

    public void setMaxDurationSeconds(int seconds) {
        this.maxDurationTicks = seconds * 20;
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob diese Aktion eine andere überschreiben kann
     */
    public boolean canOverride(BehaviorAction other) {
        if (other == null) return true;
        return this.priority.isHigherThan(other.priority);
    }

    /**
     * Prüft ob diese Aktion von einer anderen überschrieben werden kann
     */
    public boolean canBeOverriddenBy(BehaviorAction other) {
        if (other == null) return false;
        return other.priority.isHigherThan(this.priority);
    }

    @Override
    public String toString() {
        return String.format("BehaviorAction{id='%s', priority=%s, state=%s, running=%s}",
            id, priority.name(), resultState.name(), isRunning);
    }
}
