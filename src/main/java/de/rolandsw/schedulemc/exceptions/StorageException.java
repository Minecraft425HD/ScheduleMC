package de.rolandsw.schedulemc.exceptions;

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
    }

    /**
     * Gets the file path where the storage error occurred.
     *
     * @return the file path, or null if not specified
     */
    public String getFilePath() {
        return filePath;
    }
}
