package de.rolandsw.schedulemc.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.StandardCopyOption;

/**
 * Abstrakte Basisklasse für alle Manager mit Datenpersistenz
 *
 * Features:
 * - Automatische Backup-Rotation
 * - Atomic file writes
 * - Backup-Wiederherstellung bei Korruption
 * - Health-Status-Tracking
 * - Graceful degradation
 *
 * Eliminiert ~165 Zeilen duplizierter Code pro Manager
 *
 * @param <T> Der Datentyp, der persistiert werden soll
 */
public abstract class AbstractPersistenceManager<T> implements IPersistenceService<T> {

    protected static final Logger LOGGER = LogUtils.getLogger();

    private final File dataFile;
    private final Gson gson;
    private boolean needsSave = false;
    private boolean isHealthy = true;
    private String lastError = null;

    /**
     * Konstruktor für Persistence-Manager
     *
     * @param dataFile Die Datei, in der die Daten gespeichert werden
     * @param gson Die Gson-Instanz für Serialisierung
     */
    protected AbstractPersistenceManager(File dataFile, Gson gson) {
        this.dataFile = dataFile;
        this.gson = gson;
    }

    /**
     * Lädt die Daten mit Backup-Wiederherstellung und Retry-Mechanismus
     */
    public void load() {
        if (!dataFile.exists()) {
            LOGGER.info("{}: Keine Datei gefunden, starte mit leeren Daten", getComponentName());
            isHealthy = true;
            onNoDataFileFound();
            return;
        }

        // Use ErrorRecovery with retry for resilient file I/O
        ErrorRecovery.Result<T> result = ErrorRecovery.retry(() -> {
            return loadFromFile(dataFile);
        }, ErrorRecovery.RetryConfig.fast(), getComponentName() + " load");

        if (result.isSuccess()) {
            onDataLoaded(result.getValue());
            isHealthy = true;
            lastError = null;
            LOGGER.info("{}: Daten erfolgreich geladen{}", getComponentName(),
                result.getAttempts() > 1 ? " (nach " + result.getAttempts() + " Versuchen)" : "");
        } else {
            LOGGER.error("{}: Fehler beim Laden der Daten nach {} Versuchen",
                getComponentName(), result.getAttempts(), result.getError());
            lastError = "Failed to load: " + result.getError().getMessage();

            // Backup-Wiederherstellung
            if (BackupManager.restoreFromBackup(dataFile)) {
                LOGGER.warn("{}: Datei korrupt, versuche Backup wiederherzustellen...", getComponentName());

                ErrorRecovery.Result<T> backupResult = ErrorRecovery.retry(() -> {
                    return loadFromFile(dataFile);
                }, ErrorRecovery.RetryConfig.fast(), getComponentName() + " backup restore");

                if (backupResult.isSuccess()) {
                    onDataLoaded(backupResult.getValue());
                    LOGGER.info("{}: Erfolgreich von Backup wiederhergestellt", getComponentName());
                    isHealthy = true;
                    lastError = "Recovered from backup";
                } else {
                    LOGGER.error("{}: KRITISCH: Backup-Wiederherstellung fehlgeschlagen!",
                        getComponentName(), backupResult.getError());
                    handleCriticalLoadFailure();
                }
            } else {
                LOGGER.error("{}: KRITISCH: Kein Backup verfügbar!", getComponentName());
                handleCriticalLoadFailure();
            }
        }
    }

    /**
     * Lädt Daten aus einer Datei
     */
    private T loadFromFile(File file) throws IOException, JsonSyntaxException {
        try (FileReader reader = new FileReader(file)) {
            T data = gson.fromJson(reader, getDataType());

            if (data == null) {
                throw new IOException("Geladene Daten sind null");
            }

            return data;
        }
    }

    /**
     * Behandelt kritische Ladefehler mit Graceful Degradation
     */
    private void handleCriticalLoadFailure() {
        LOGGER.error("{}: KRITISCH: System konnte nicht geladen werden!", getComponentName());
        LOGGER.error("{}: Starte mit leeren Daten als Fallback", getComponentName());

        onCriticalLoadFailure();

        isHealthy = false;
        lastError = "Critical load failure - running with empty data";

        // Preserve corrupt file for forensics
        if (dataFile.exists()) {
            File corruptBackup = new File(dataFile.getParent(),
                dataFile.getName() + ".CORRUPT_" + System.currentTimeMillis());
            try {
                java.nio.file.Files.copy(dataFile.toPath(), corruptBackup.toPath());
                LOGGER.info("{}: Korrupte Datei gesichert nach: {}", getComponentName(), corruptBackup.getName());
            } catch (IOException e) {
                LOGGER.error("{}: Konnte korrupte Datei nicht sichern", getComponentName(), e);
            }
        }
    }

    /**
     * Speichert die Daten mit Backup, atomic writes und Retry-Mechanismus
     */
    public void save() {
        // Use ErrorRecovery with retry for resilient file I/O
        ErrorRecovery.Result<Void> result = ErrorRecovery.retry(() -> {
            dataFile.getParentFile().mkdirs();

            // Create backup before overwriting
            if (dataFile.exists() && dataFile.length() > 0) {
                BackupManager.createBackup(dataFile);
            }

            // Temporary file for atomic writing
            File tempFile = new File(dataFile.getParent(), dataFile.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                T data = getCurrentData();
                gson.toJson(data, writer);
                writer.flush();
            }

            // Atomic replace
            java.nio.file.Files.move(tempFile.toPath(), dataFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);

            return null; // Success
        }, ErrorRecovery.RetryConfig.defaults(), getComponentName() + " save");

        if (result.isSuccess()) {
            needsSave = false;
            isHealthy = true;
            lastError = null;
            LOGGER.info("{}: Daten erfolgreich gespeichert{}", getComponentName(),
                result.getAttempts() > 1 ? " (nach " + result.getAttempts() + " Versuchen)" : "");
        } else {
            LOGGER.error("{}: KRITISCH: Fehler beim Speichern nach {} Versuchen!",
                getComponentName(), result.getAttempts(), result.getError());
            isHealthy = false;
            lastError = "Save failed: " + result.getError().getMessage();
            needsSave = true; // Keep dirty flag for retry
        }
    }

    /**
     * Speichert nur wenn Änderungen vorhanden sind
     */
    public void saveIfNeeded() {
        if (needsSave) {
            save();
        }
    }

    /**
     * Markiert die Daten als geändert
     */
    public void markDirty() {
        needsSave = true;
    }

    /**
     * Gibt den Health-Status zurück
     */
    public boolean isHealthy() {
        return isHealthy;
    }

    /**
     * Gibt die letzte Fehlermeldung zurück
     */
    @Nullable
    public String getLastError() {
        return lastError;
    }

    /**
     * Gibt Health-Info für Monitoring zurück
     */
    public String getHealthInfo() {
        if (isHealthy) {
            return String.format("§aGESUND§r - %s, %d Backups verfügbar",
                getHealthDetails(), BackupManager.getBackupCount(dataFile));
        } else {
            return String.format("§cUNGESUND§r - Letzter Fehler: %s, %s",
                lastError != null ? lastError : "Unknown", getHealthDetails());
        }
    }

    // ========== Abstract Methods (zu implementieren von Subklassen) ==========

    /**
     * Gibt den Typ der Daten zurück (für Gson Deserialisierung)
     */
    protected abstract Type getDataType();

    /**
     * Wird aufgerufen, wenn Daten erfolgreich geladen wurden
     */
    protected abstract void onDataLoaded(T data);

    /**
     * Gibt die aktuellen Daten zum Speichern zurück
     */
    protected abstract T getCurrentData();

    /**
     * Gibt den Namen der Komponente zurück (für Logging)
     */
    protected abstract String getComponentName();

    /**
     * Gibt detaillierte Health-Informationen zurück (z.B. Anzahl Einträge)
     */
    protected abstract String getHealthDetails();

    /**
     * Wird aufgerufen, wenn keine Datendatei existiert
     * (Standard: nichts tun)
     */
    protected void onNoDataFileFound() {
        // Optional override
    }

    /**
     * Wird aufgerufen bei kritischem Ladefehler
     * (Subklassen sollten hier Daten clearen)
     */
    protected abstract void onCriticalLoadFailure();

    // ========== Getter für Subklassen ==========

    protected File getDataFile() {
        return dataFile;
    }

    protected Gson getGson() {
        return gson;
    }

    protected boolean needsSave() {
        return needsSave;
    }
}
