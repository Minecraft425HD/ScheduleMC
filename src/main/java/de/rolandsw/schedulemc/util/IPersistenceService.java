package de.rolandsw.schedulemc.util;

import javax.annotation.Nullable;

/**
 * Service Interface für Persistenz-Operationen
 *
 * Dieses Interface definiert den Contract für Datenpersistenz und ermöglicht:
 * - Austauschbare Implementierungen (File-based, Database, In-Memory)
 * - Bessere Testbarkeit durch Mock-Implementierungen
 * - Dependency Injection
 * - Loose Coupling zwischen Komponenten
 *
 * @param <T> Der Datentyp, der persistiert werden soll
 * @since 1.0
 */
public interface IPersistenceService<T> {

    /**
     * Lädt die Daten aus dem Persistenz-Layer
     *
     * <p>Bei Fehlern sollte Backup-Wiederherstellung versucht werden.
     * Bei kritischen Fehlern sollte Graceful Degradation erfolgen.</p>
     */
    void load();

    /**
     * Speichert die aktuellen Daten in den Persistenz-Layer
     *
     * <p>Sollte atomare Writes und Backup-Erstellung verwenden.</p>
     */
    void save();

    /**
     * Speichert nur wenn Änderungen vorhanden sind
     *
     * <p>Optimierung für periodisches Speichern.</p>
     */
    void saveIfNeeded();

    /**
     * Markiert die Daten als geändert (dirty flag)
     */
    void markDirty();

    /**
     * Prüft ob das Persistenz-System gesund ist
     *
     * @return true wenn gesund, false bei Problemen
     */
    boolean isHealthy();

    /**
     * Gibt die letzte Fehlermeldung zurück
     *
     * @return Die Fehlermeldung, oder null wenn gesund
     */
    @Nullable
    String getLastError();

    /**
     * Gibt formatierte Health-Information für Monitoring zurück
     *
     * @return Formatierter Health-Status String mit Details
     */
    String getHealthInfo();

    /**
     * Gibt den Namen der Komponente zurück (für Logging)
     *
     * @return Der Komponentenname (z.B. "EconomyManager", "PlotManager")
     */
    String getComponentName();

    /**
     * Gibt detaillierte Health-Informationen zurück
     *
     * @return Details wie Anzahl Einträge, Cache-Größe, etc.
     */
    String getHealthDetails();

    /**
     * Prüft ob Änderungen zum Speichern vorhanden sind
     *
     * @return true wenn Daten gespeichert werden müssen
     */
    boolean needsSave();
}
