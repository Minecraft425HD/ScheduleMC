package de.rolandsw.schedulemc.npc.entity.component;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.nbt.CompoundTag;

/**
 * Basis-Interface fuer NPC Entity Components.
 *
 * Component-System zerlegt den monolithischen CustomNPCEntity in
 * austauschbare, testbare Komponenten.
 *
 * Jede Komponente:
 * - Hat eigene tick()-Logik
 * - Kann NBT speichern/laden
 * - Hat eine definierte Update-Frequenz
 * - Ist unabhaengig testbar
 */
public interface NPCComponent {

    /**
     * Eindeutige ID der Komponente.
     */
    String getComponentId();

    /**
     * Wird jeden Tick aufgerufen (server-side).
     */
    void tick(CustomNPCEntity entity);

    /**
     * Update-Intervall in Ticks.
     * Rueckgabe von 1 = jeden Tick, 20 = jede Sekunde, etc.
     */
    default int getUpdateInterval() {
        return 1;
    }

    /**
     * Speichert Komponentendaten.
     */
    default CompoundTag save() {
        return new CompoundTag();
    }

    /**
     * Laedt Komponentendaten.
     */
    default void load(CompoundTag tag) {}

    /**
     * Wird aufgerufen wenn die Komponente entfernt wird.
     */
    default void onRemoved(CustomNPCEntity entity) {}

    /**
     * Wird aufgerufen wenn die Komponente hinzugefuegt wird.
     */
    default void onAdded(CustomNPCEntity entity) {}
}
