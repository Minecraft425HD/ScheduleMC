package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

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
     * Erstellt ein Backup einer Datei
     *
     * @param sourceFile Die zu sichernde Datei
     * @return true wenn erfolgreich
     */
    public static boolean createBackup(File sourceFile) {
        if (!sourceFile.exists()) {
            LOGGER.warn("Backup skipped - file does not exist: {}", sourceFile.getAbsolutePath());
            return false;
        }

        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String backupName = sourceFile.getName() + ".backup_" + timestamp;
            File backupFile = new File(sourceFile.getParent(), backupName);

            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Backup erstellt: {}", backupFile.getName());

            // Cleanup alter Backups
            cleanupOldBackups(sourceFile);

            return true;
        } catch (IOException e) {
            LOGGER.error("Error creating backup for {}", sourceFile.getName(), e);
            return false;
        }
    }

    /**
     * Stellt die neueste Backup-Datei wieder her
     *
     * @param targetFile Die wiederherzustellende Datei
     * @return true wenn erfolgreich
     */
    public static boolean restoreFromBackup(File targetFile) {
        File latestBackup = getLatestBackup(targetFile);

        if (latestBackup == null) {
            LOGGER.error("No backup found for: {}", targetFile.getName());
            return false;
        }

        try {
            Files.copy(latestBackup.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Backup wiederhergestellt von: {} nach {}", latestBackup.getName(), targetFile.getName());
            return true;
        } catch (IOException e) {
            LOGGER.error("Fehler beim Wiederherstellen des Backups", e);
            return false;
        }
    }

    /**
     * Findet das neueste Backup für eine Datei
     *
     * @param targetFile Die Zieldatei
     * @return Die neueste Backup-Datei oder null
     */
    public static File getLatestBackup(File targetFile) {
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
     *
     * @param targetFile Die Zieldatei
     */
    private static void cleanupOldBackups(File targetFile) {
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
            if (backups[i].delete()) {
                LOGGER.debug("Old backup deleted: {}", backups[i].getName());
            } else {
                LOGGER.warn("Could not delete old backup: {}", backups[i].getName());
            }
        }
    }

    /**
     * Listet alle verfügbaren Backups für eine Datei auf
     *
     * @param targetFile Die Zieldatei
     * @return Array von Backup-Dateien (neueste zuerst)
     */
    public static File[] listBackups(File targetFile) {
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
    public static int getBackupCount(File targetFile) {
        return listBackups(targetFile).length;
    }
}
