package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Zentraler Backup-Manager für alle Persistenz-Dateien
 *
 * Features:
 * - Automatische Backup-Rotation (max 5 Backups)
 * - Timestamped Backups
 * - Backup-Wiederherstellung
 * - Cleanup alter Backups
 */
public class BackupManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_BACKUPS = 5;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Erstellt ein Backup einer Datei mit Retry-Mechanismus
     *
     * @param sourceFile Die zu sichernde Datei
     * @return true wenn erfolgreich
     */
    public static boolean createBackup(@Nonnull File sourceFile) {
        if (!sourceFile.exists()) {
            LOGGER.warn("Backup übersprungen - Datei existiert nicht: {}", sourceFile.getAbsolutePath());
            return false;
        }

        // Use ErrorRecovery for resilient backup creation
        ErrorRecovery.Result<Boolean> result = ErrorRecovery.retry(() -> {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String backupName = sourceFile.getName() + ".backup_" + timestamp;
            File backupFile = new File(sourceFile.getParent(), backupName);

            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Backup erstellt: {}", backupFile.getName());

            // Cleanup alter Backups
            cleanupOldBackups(sourceFile);

            return true;
        }, ErrorRecovery.RetryConfig.fast(), "Backup creation for " + sourceFile.getName());

        if (result.isSuccess()) {
            if (result.getAttempts() > 1) {
                LOGGER.info("Backup erfolgreich nach {} Versuchen", result.getAttempts());
            }
            return true;
        }

        LOGGER.error("Fehler beim Erstellen des Backups für {} nach {} Versuchen",
            sourceFile.getName(), result.getAttempts(), result.getError());
        return false;
    }

    /**
     * Stellt die neueste Backup-Datei wieder her mit Retry-Mechanismus
     *
     * @param targetFile Die wiederherzustellende Datei
     * @return true wenn erfolgreich
     */
    public static boolean restoreFromBackup(@Nonnull File targetFile) {
        File latestBackup = getLatestBackup(targetFile);

        if (latestBackup == null) {
            LOGGER.error("Kein Backup gefunden für: {}", targetFile.getName());
            return false;
        }

        // Use ErrorRecovery for resilient backup restoration
        final File backup = latestBackup;
        ErrorRecovery.Result<Boolean> result = ErrorRecovery.retry(() -> {
            Files.copy(backup.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Backup wiederhergestellt von: {} nach {}", backup.getName(), targetFile.getName());
            return true;
        }, ErrorRecovery.RetryConfig.defaults(), "Backup restore for " + targetFile.getName());

        if (result.isSuccess()) {
            if (result.getAttempts() > 1) {
                LOGGER.info("Backup-Wiederherstellung erfolgreich nach {} Versuchen", result.getAttempts());
            }
            return true;
        }

        LOGGER.error("Fehler beim Wiederherstellen des Backups nach {} Versuchen",
            result.getAttempts(), result.getError());
        return false;
    }

    /**
     * Findet das neueste Backup für eine Datei
     *
     * @param targetFile Die Zieldatei
     * @return Die neueste Backup-Datei oder null
     */
    @Nullable
    public static File getLatestBackup(@Nonnull File targetFile) {
        File parentDir = targetFile.getParentFile();
        if (parentDir == null || !parentDir.exists()) {
            return null;
        }

        String baseFileName = targetFile.getName() + ".backup_";

        File[] backups = parentDir.listFiles((dir, name) -> name.startsWith(baseFileName));

        if (backups == null || backups.length == 0) {
            return null;
        }

        // Sortiere nach Änderungsdatum (neueste zuerst)
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified).reversed());

        return backups[0];
    }

    /**
     * Löscht alte Backups und behält nur die neuesten MAX_BACKUPS
     * Nutzt Safe Execution - Fehler beim Löschen blockieren nicht den Backup-Prozess
     *
     * @param targetFile Die Zieldatei
     */
    private static void cleanupOldBackups(@Nonnull File targetFile) {
        // Use safe execution - cleanup failures should not block backup creation
        ErrorRecovery.safeExecute(() -> {
            File parentDir = targetFile.getParentFile();
            if (parentDir == null || !parentDir.exists()) {
                return;
            }

            String baseFileName = targetFile.getName() + ".backup_";

            File[] backups = parentDir.listFiles((dir, name) -> name.startsWith(baseFileName));

            if (backups == null || backups.length <= MAX_BACKUPS) {
                return;
            }

            // Sortiere nach Änderungsdatum (älteste zuerst)
            Arrays.sort(backups, Comparator.comparingLong(File::lastModified));

            // Lösche die ältesten Backups
            int toDelete = backups.length - MAX_BACKUPS;
            for (int i = 0; i < toDelete; i++) {
                final File fileToDelete = backups[i];
                // Each delete is safe - one failure doesn't stop others
                ErrorRecovery.safeExecute(() -> {
                    if (fileToDelete.delete()) {
                        LOGGER.debug("Altes Backup gelöscht: {}", fileToDelete.getName());
                    } else {
                        LOGGER.warn("Konnte altes Backup nicht löschen: {}", fileToDelete.getName());
                    }
                }, "Delete old backup " + fileToDelete.getName());
            }
        }, "Cleanup old backups for " + targetFile.getName());
    }

    /**
     * Listet alle verfügbaren Backups für eine Datei auf
     *
     * @param targetFile Die Zieldatei
     * @return Array von Backup-Dateien (neueste zuerst)
     */
    @Nonnull
    public static File[] listBackups(@Nonnull File targetFile) {
        File parentDir = targetFile.getParentFile();
        if (parentDir == null || !parentDir.exists()) {
            return new File[0];
        }

        String baseFileName = targetFile.getName() + ".backup_";

        File[] backups = parentDir.listFiles((dir, name) -> name.startsWith(baseFileName));

        if (backups == null || backups.length == 0) {
            return new File[0];
        }

        // Sortiere nach Änderungsdatum (neueste zuerst)
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified).reversed());

        return backups;
    }

    /**
     * Gibt die Anzahl verfügbarer Backups zurück
     *
     * @param targetFile Die Zieldatei
     * @return Anzahl der Backups
     */
    public static int getBackupCount(@Nonnull File targetFile) {
        return listBackups(targetFile).length;
    }
}
