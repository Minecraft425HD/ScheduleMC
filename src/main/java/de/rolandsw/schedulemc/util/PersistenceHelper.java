package de.rolandsw.schedulemc.util;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

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
     * Lädt Daten aus einer JSON-Datei mit automatischer Backup-Wiederherstellung
     *
     * @param file Die Datei, aus der geladen werden soll
     * @param gson Die Gson-Instanz
     * @param type Der Datentyp (z.B. new TypeToken<Map<String, Data>>(){}.getType())
     * @param componentName Name für Logging (z.B. "EconomyManager")
     * @return LoadResult mit Daten oder Fehler
     */
    public static <T> LoadResult<T> load(File file, Gson gson, Type type, String componentName) {
        if (!file.exists()) {
            LOGGER.info("{}: Keine Datei gefunden, starte mit leeren Daten", componentName);
            return LoadResult.noFile();
        }

        try {
            T data = loadFromFile(file, gson, type);
            LOGGER.info("{}: Daten erfolgreich geladen", componentName);
            return LoadResult.success(data);

        } catch (Exception e) {
            LOGGER.error("{}: Fehler beim Laden der Daten", componentName, e);

            // Versuch Backup wiederherzustellen
            if (BackupManager.restoreFromBackup(file)) {
                LOGGER.warn("{}: Datei korrupt, versuche Backup wiederherzustellen...", componentName);
                try {
                    T data = loadFromFile(file, gson, type);
                    LOGGER.info("{}: Erfolgreich von Backup wiederhergestellt", componentName);
                    return LoadResult.recoveredFromBackup(data);

                } catch (Exception backupError) {
                    LOGGER.error("{}: KRITISCH: Backup-Wiederherstellung fehlgeschlagen!", componentName, backupError);
                    preserveCorruptFile(file, componentName);
                    return LoadResult.failure("Backup recovery failed: " + backupError.getMessage());
                }
            } else {
                LOGGER.error("{}: CRITICAL: No backup available!", componentName);
                preserveCorruptFile(file, componentName);
                return LoadResult.failure("Load failed, no backup: " + e.getMessage());
            }
        }
    }

    /**
     * Speichert Daten in eine JSON-Datei mit Backup und atomarem Schreiben
     *
     * @param file Die Zieldatei
     * @param gson Die Gson-Instanz
     * @param data Die zu speichernden Daten
     * @param componentName Name für Logging
     * @return SaveResult mit Erfolg oder Fehler
     */
    public static <T> SaveResult save(File file, Gson gson, T data, String componentName) {
        try {
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

            LOGGER.debug("{}: Daten erfolgreich gespeichert", componentName);
            return SaveResult.success();

        } catch (Exception e) {
            LOGGER.error("{}: KRITISCH: Fehler beim Speichern!", componentName, e);

            // Retry einmal
            SaveResult retryResult = retrySave(file, gson, data, componentName);
            if (retryResult.isSuccess()) {
                return retryResult;
            }

            return SaveResult.failure("Save failed: " + e.getMessage());
        }
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

    private static <T> T loadFromFile(File file, Gson gson, Type type) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            T data = gson.fromJson(reader, type);

            if (data == null) {
                throw new IOException("Geladene Daten sind null");
            }

            return data;
        }
    }

    private static <T> SaveResult retrySave(File file, Gson gson, T data, String componentName) {
        LOGGER.warn("{}: Versuche erneut zu speichern...", componentName);
        try {
            File tempFile = new File(file.getParent(), file.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(data, writer);
                writer.flush();
            }

            Files.move(tempFile.toPath(), file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);

            LOGGER.info("{}: Retry erfolgreich", componentName);
            return SaveResult.success();

        } catch (Exception retryError) {
            LOGGER.error("{}: KRITISCH: Retry fehlgeschlagen!", componentName, retryError);
            return SaveResult.failure("Retry failed: " + retryError.getMessage());
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
    public static String getHealthInfo(File file, boolean isHealthy, @Nullable String lastError,
                                       String details) {
        if (isHealthy) {
            return Component.translatable("health.persistence.healthy",
                details,
                BackupManager.getBackupCount(file)).getString();
        } else {
            return Component.translatable("health.persistence.unhealthy",
                lastError != null ? lastError : "Unknown",
                details).getString();
        }
    }
}
