package de.rolandsw.schedulemc.util;

import java.util.function.Supplier;

/**
 * Wrapper-Klasse um Manager mit AbstractPersistenceManager
 * in IncrementalSaveManager zu integrieren
 *
 * Ermöglicht Manager die nicht direkt ISaveable implementieren können
 * (z.B. wegen innerer persistence-Klassen) trotzdem zu registrieren
 */
public class SaveableWrapper implements IncrementalSaveManager.ISaveable {

    private final Runnable saveMethod;
    private final Supplier<Boolean> isDirtyMethod;
    private final String name;
    private final int priority;

    /**
     * Erstellt einen Wrapper für einen Manager
     *
     * @param name Der Name des Managers (für Logging)
     * @param saveMethod Die save()-Methode des Managers
     * @param isDirtyMethod Die isDirty()-Methode (oder () -> true für always-dirty)
     * @param priority Die Priorität (0 = höchste, 10 = niedrigste)
     */
    public SaveableWrapper(String name, Runnable saveMethod, Supplier<Boolean> isDirtyMethod, int priority) {
        this.name = name;
        this.saveMethod = saveMethod;
        this.isDirtyMethod = isDirtyMethod;
        this.priority = priority;
    }

    /**
     * Convenience Constructor mit default-isDirty (always true)
     */
    public SaveableWrapper(String name, Runnable saveMethod, int priority) {
        this(name, saveMethod, () -> true, priority);
    }

    @Override
    public boolean isDirty() {
        return isDirtyMethod.get();
    }

    @Override
    public void save() {
        saveMethod.run();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
