package de.rolandsw.schedulemc.exceptions;

/**
 * Base exception class for all ScheduleMC-specific exceptions.
 * <p>
 * Provides consistent exception handling across all mod subsystems.
 * All domain-specific exceptions should extend this base class.
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 */
public class ScheduleMCException extends RuntimeException {

    /**
     * Constructs a new ScheduleMC exception with the specified detail message.
     *
     * @param message the detail message explaining the exception
     */
    public ScheduleMCException(String message) {
        super(message);
    }

    /**
     * Constructs a new ScheduleMC exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the exception
     * @param cause the underlying cause of this exception
     */
    public ScheduleMCException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ScheduleMC exception with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public ScheduleMCException(Throwable cause) {
        super(cause);
    }
}
