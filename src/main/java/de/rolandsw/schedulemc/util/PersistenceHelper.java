package de.rolandsw.schedulemc.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

/**
 * Hilfsklasse für Datenpersistenz - eliminiert ~165 Zeilen duplizierten Code pro Manager
 *
 * Features:
 * - Automatische Backup-Rotation
 * - Atomic file writes
 * - Backup-Wiederherstellung bei Korruption
 * - Retry-Mechanismus bei Fehlern
 *
 * Verwendung:
 * <pre>
 * PersistenceHelper.LoadResult<Map<String, Data>> result =
 *     PersistenceHelper.load(file, gson, new TypeToken<Map<String, Data>>(){}.getType());
 *
 * if (result.isSuccess()) {
 *     myData = result.getData();
 * }
 *
 * PersistenceHelper.save(file, gson, myData);
 * </pre>
 */
public class PersistenceHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Ergebnis eines Ladevorgangs
     */
    public static class LoadResult<T> {
        private final T data;
        private final boolean success;
        private final boolean recoveredFromBackup;
        private final String error;

        private LoadResult(T data, boolean success, boolean recoveredFromBackup, String error) {
            this.data = data;
            this.success = success;
            this.recoveredFromBackup = recoveredFromBackup;
            this.error = error;
        }

        public static <T> LoadResult<T> success(T data) {
            return new LoadResult<>(data, true, false, null);
        }

        public static <T> LoadResult<T> recoveredFromBackup(T data) {
            return new LoadResult<>(data, true, true, "Recovered from backup");
        }

        public static <T> LoadResult<T> failure(String error) {
            return new LoadResult<>(null, false, false, error);
        }

        public static <T> LoadResult<T> noFile() {
            return new LoadResult<>(null, true, false, null);
        }

        public T getData() { return data; }
        public boolean isSuccess() { return success; }
        public boolean isRecoveredFromBackup() { return recoveredFromBackup; }
        public boolean hasData() { return data != null; }
        @Nullable public String getError() { return error; }
    }

    /**
     * Ergebnis eines Speichervorgangs
     */
    public static class SaveResult {
        private final boolean success;
        private final String error;

        private SaveResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        public static SaveResult success() {
            return new SaveResult(true, null);
        }

        public static SaveResult failure(String error) {
            return new SaveResult(false, error);
        }

        public boolean isSuccess() { return success; }
        @Nullable public String getError() { return error; }
    }

    /**
     * Lädt Daten aus einer JSON-Datei mit automatischer Backup-Wiederherstellung und Retry
     *
     * @param file Die Datei, aus der geladen werden soll
     * @param gson Die Gson-Instanz
     * @param type Der Datentyp (z.B. new TypeToken<Map<String, Data>>(){}.getType())
     * @param componentName Name für Logging (z.B. "EconomyManager")
     * @return LoadResult mit Daten oder Fehler
     */
    @Nonnull
    public static <T> LoadResult<T> load(@Nonnull File file, @Nonnull Gson gson, @Nonnull Type type, @Nonnull String componentName) {
        if (!file.exists()) {
            LOGGER.info("{}: Keine Datei gefunden, starte mit leeren Daten", componentName);
            return LoadResult.noFile();
        }

        // Use ErrorRecovery for resilient loading with retry
        ErrorRecovery.Result<T> result = ErrorRecovery.retry(() -> {
            return loadFromFile(file, gson, type);
        }, ErrorRecovery.RetryConfig.fast(), componentName + " load");

        if (result.isSuccess()) {
            LOGGER.info("{}: Daten erfolgreich geladen{}", componentName,
                result.getAttempts() > 1 ? " (nach " + result.getAttempts() + " Versuchen)" : "");
            return LoadResult.success(result.getValue());
        }

        LOGGER.error("{}: Fehler beim Laden der Daten nach {} Versuchen",
            componentName, result.getAttempts(), result.getError());

        // Versuch Backup wiederherzustellen
        if (BackupManager.restoreFromBackup(file)) {
            LOGGER.warn("{}: Datei korrupt, versuche Backup wiederherzustellen...", componentName);

            ErrorRecovery.Result<T> backupResult = ErrorRecovery.retry(() -> {
                return loadFromFile(file, gson, type);
            }, ErrorRecovery.RetryConfig.fast(), componentName + " backup restore");

            if (backupResult.isSuccess()) {
                LOGGER.info("{}: Erfolgreich von Backup wiederhergestellt", componentName);
                return LoadResult.recoveredFromBackup(backupResult.getValue());
            }

            LOGGER.error("{}: KRITISCH: Backup-Wiederherstellung fehlgeschlagen!",
                componentName, backupResult.getError());
            preserveCorruptFile(file, componentName);
            return LoadResult.failure("Backup recovery failed: " + backupResult.getError().getMessage());
        }

        LOGGER.error("{}: KRITISCH: Kein Backup verfügbar!", componentName);
        preserveCorruptFile(file, componentName);
        return LoadResult.failure("Load failed, no backup: " + result.getError().getMessage());
    }

    /**
     * Speichert Daten in eine JSON-Datei mit Backup, atomarem Schreiben und Retry
     *
     * @param file Die Zieldatei
     * @param gson Die Gson-Instanz
     * @param data Die zu speichernden Daten
     * @param componentName Name für Logging
     * @return SaveResult mit Erfolg oder Fehler
     */
    @Nonnull
    public static <T> SaveResult save(@Nonnull File file, @Nonnull Gson gson, @Nonnull T data, @Nonnull String componentName) {
        // Use ErrorRecovery for resilient saving with retry
        ErrorRecovery.Result<Void> result = ErrorRecovery.retry(() -> {
            file.getParentFile().mkdirs();

            // Backup erstellen falls Datei existiert
            if (file.exists() && file.length() > 0) {
                BackupManager.createBackup(file);
            }

            // Temporäre Datei für atomares Schreiben
            File tempFile = new File(file.getParent(), file.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(data, writer);
                writer.flush();
            }

            // Atomares Ersetzen
            Files.move(tempFile.toPath(), file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);

            return null; // Success
        }, ErrorRecovery.RetryConfig.defaults(), componentName + " save");

        if (result.isSuccess()) {
            LOGGER.debug("{}: Daten erfolgreich gespeichert{}", componentName,
                result.getAttempts() > 1 ? " (nach " + result.getAttempts() + " Versuchen)" : "");
            return SaveResult.success();
        }

        LOGGER.error("{}: KRITISCH: Fehler beim Speichern nach {} Versuchen!",
            componentName, result.getAttempts(), result.getError());
        return SaveResult.failure("Save failed: " + result.getError().getMessage());
    }

    /**
     * Speichert Daten mit Transformation (z.B. UUID -> String Konvertierung)
     */
    public static <T, S> SaveResult saveWithTransform(File file, Gson gson, T data,
                                                       java.util.function.Function<T, S> transformer,
                                                       String componentName) {
        S transformed = transformer.apply(data);
        return save(file, gson, transformed, componentName);
    }

    // ========== Private Hilfsmethoden ==========

    private static <T> T loadFromFile(File file, Gson gson, Type type) throws IOException, JsonSyntaxException {
        try (FileReader reader = new FileReader(file)) {
            T data = gson.fromJson(reader, type);

            if (data == null) {
                throw new IOException("Geladene Daten sind null");
            }

            return data;
        }
    }

    private static void preserveCorruptFile(File file, String componentName) {
        if (file.exists()) {
            File corruptBackup = new File(file.getParent(),
                file.getName() + ".CORRUPT_" + System.currentTimeMillis());
            try {
                Files.copy(file.toPath(), corruptBackup.toPath());
                LOGGER.info("{}: Korrupte Datei gesichert nach: {}", componentName, corruptBackup.getName());
            } catch (IOException e) {
                LOGGER.error("{}: Konnte korrupte Datei nicht sichern", componentName, e);
            }
        }
    }

    /**
     * Gibt Health-Info für einen Datei-basierten Manager zurück
     */
    @Nonnull
    public static String getHealthInfo(@Nonnull File file, boolean isHealthy, @Nullable String lastError,
                                       @Nonnull String details) {
        if (isHealthy) {
            return String.format("§aGESUND§r - %s, %d Backups verfügbar",
                details, BackupManager.getBackupCount(file));
        } else {
            return String.format("§cUNGESUND§r - Letzter Fehler: %s, %s",
                lastError != null ? lastError : "Unknown", details);
        }
    }
}
