package de.rolandsw.schedulemc.exceptions;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Exception thrown when data persistence operations fail.
 * <p>
 * This exception covers all storage and persistence errors including:
 * <ul>
 *   <li>File I/O errors during save/load operations</li>
 *   <li>JSON serialization/deserialization failures</li>
 *   <li>Corrupted save data detection</li>
 *   <li>SavedData persistence errors</li>
 *   <li>Incremental save manager failures</li>
 *   <li>Backup creation/restoration errors</li>
 * </ul>
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 * @see de.rolandsw.schedulemc.storage.IncrementalSaveManager
 */
public class StorageException extends ScheduleMCException {

    /**
     * Standard error codes for storage operations.
     */
    public static final class ErrorCodes {
        public static final String FILE_NOT_FOUND = "STO_001";
        public static final String IO_ERROR = "STO_002";
        public static final String CORRUPTED_DATA = "STO_003";
        public static final String SERIALIZATION_ERROR = "STO_004";
        public static final String BACKUP_FAILED = "STO_005";
        public static final String PERMISSION_DENIED = "STO_006";
        public static final String DISK_FULL = "STO_007";

        private ErrorCodes() {}
    }

    private final String filePath;

    /**
     * Constructs a new storage exception with the specified detail message.
     *
     * @param message the detail message explaining the storage error
     */
    public StorageException(String message) {
        super(message);
        this.filePath = null;
    }

    /**
     * Constructs a new storage exception with file path context.
     *
     * @param message the detail message explaining the storage error
     * @param filePath the file path where the error occurred
     */
    public StorageException(String message, String filePath) {
        super(String.format("%s (file: %s)", message, filePath));
        this.filePath = filePath;
        this.withContext("filePath", filePath);
    }

    /**
     * Constructs a new storage exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the storage error
     * @param cause the underlying cause of this exception
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
    }

    /**
     * Constructs a new storage exception with file path context and cause.
     *
     * @param message the detail message explaining the storage error
     * @param filePath the file path where the error occurred
     * @param cause the underlying cause of this exception
     */
    public StorageException(String message, String filePath, Throwable cause) {
        super(String.format("%s (file: %s)", message, filePath), cause);
        this.filePath = filePath;
        this.withContext("filePath", filePath);
    }

    /**
     * Advanced constructor with full error context.
     *
     * @param message Technical message for developers/logs
     * @param userMessage User-friendly message for display
     * @param errorCode Machine-readable error code
     * @param retryable Whether this error can be retried
     * @param filePath File path where error occurred
     * @param cause Underlying cause
     */
    public StorageException(String message, @Nullable String userMessage,
                           @Nullable String errorCode, boolean retryable,
                           @Nullable String filePath, @Nullable Throwable cause) {
        super(message, userMessage, errorCode, retryable, cause);
        this.filePath = filePath;
        if (filePath != null) {
            this.withContext("filePath", filePath);
        }
    }

    /**
     * Gets the file path where the storage error occurred.
     *
     * @return the file path, or null if not specified
     */
    public String getFilePath() {
        return filePath;
    }

    // ========== Factory Methods for Common Errors ==========

    /**
     * Creates an exception for file I/O errors.
     *
     * @param filePath File path where error occurred
     * @param cause IOException that caused the failure
     * @return Configured exception
     */
    public static StorageException ioError(String filePath, IOException cause) {
        return new StorageException(
            String.format("I/O error while accessing file: %s", filePath),
            "§cDatei konnte nicht gelesen/geschrieben werden. Bitte überprüfe die Berechtigungen.",
            ErrorCodes.IO_ERROR,
            true, // Retryable - might be transient
            filePath,
            cause
        );
    }

    /**
     * Creates an exception for corrupted data.
     *
     * @param filePath File path with corrupted data
     * @param details Details about corruption
     * @return Configured exception
     */
    public static StorageException corruptedData(String filePath, String details) {
        return new StorageException(
            String.format("Corrupted data in file: %s - %s", filePath, details),
            "§cDatei ist beschädigt! Backup wird wiederhergestellt...",
            ErrorCodes.CORRUPTED_DATA,
            false, // Not retryable - needs backup restore
            filePath,
            null
        ).withContext("details", details);
    }

    /**
     * Creates an exception for backup failures.
     *
     * @param filePath File path where backup failed
     * @param cause Underlying cause
     * @return Configured exception
     */
    public static StorageException backupFailed(String filePath, Throwable cause) {
        return new StorageException(
            String.format("Backup creation failed for: %s", filePath),
            "§cBackup konnte nicht erstellt werden! Daten könnten gefährdet sein.",
            ErrorCodes.BACKUP_FAILED,
            true, // Retryable
            filePath,
            cause
        );
    }

    /**
     * Creates an exception for permission errors.
     *
     * @param filePath File path with permission error
     * @return Configured exception
     */
    public static StorageException permissionDenied(String filePath) {
        return new StorageException(
            String.format("Permission denied accessing: %s", filePath),
            "§cKeine Berechtigung für Dateizugriff! Bitte Admin kontaktieren.",
            ErrorCodes.PERMISSION_DENIED,
            false, // Not retryable - needs admin intervention
            filePath,
            null
        );
    }
}
