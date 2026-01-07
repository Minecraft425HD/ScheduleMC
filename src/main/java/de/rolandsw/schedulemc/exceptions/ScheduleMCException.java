package de.rolandsw.schedulemc.exceptions;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all ScheduleMC-specific exceptions.
 * <p>
 * Provides consistent exception handling across all mod subsystems with:
 * <ul>
 *   <li>Retryable flag for transient errors</li>
 *   <li>Error codes for machine-readable identification</li>
 *   <li>User-friendly messages separate from technical details</li>
 *   <li>Contextual metadata for debugging</li>
 * </ul>
 * All domain-specific exceptions should extend this base class.
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 */
public class ScheduleMCException extends RuntimeException {

    private final boolean retryable;
    private final String errorCode;
    private final String userMessage;
    private final Map<String, Object> context;

    /**
     * Constructs a new ScheduleMC exception with the specified detail message.
     *
     * @param message the detail message explaining the exception
     */
    public ScheduleMCException(String message) {
        super(message);
        this.retryable = false;
        this.errorCode = null;
        this.userMessage = null;
        this.context = new HashMap<>();
    }

    /**
     * Constructs a new ScheduleMC exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the exception
     * @param cause the underlying cause of this exception
     */
    public ScheduleMCException(String message, Throwable cause) {
        super(message, cause);
        this.retryable = false;
        this.errorCode = null;
        this.userMessage = null;
        this.context = new HashMap<>();
    }

    /**
     * Constructs a new ScheduleMC exception with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public ScheduleMCException(Throwable cause) {
        super(cause);
        this.retryable = false;
        this.errorCode = null;
        this.userMessage = null;
        this.context = new HashMap<>();
    }

    /**
     * Advanced constructor with full error context.
     *
     * @param message Technical message for developers/logs
     * @param userMessage User-friendly message for display
     * @param errorCode Machine-readable error code
     * @param retryable Whether this error can be retried
     * @param cause Underlying cause
     */
    public ScheduleMCException(String message, @Nullable String userMessage,
                              @Nullable String errorCode, boolean retryable,
                              @Nullable Throwable cause) {
        super(message, cause);
        this.userMessage = userMessage;
        this.errorCode = errorCode;
        this.retryable = retryable;
        this.context = new HashMap<>();
    }

    /**
     * Indicates whether this exception represents a transient error that can be retried.
     *
     * @return true if the operation can be retried, false otherwise
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Gets the machine-readable error code for this exception.
     *
     * @return the error code, or null if not specified
     */
    @Nullable
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the user-friendly error message suitable for display.
     * Falls back to technical message if not specified.
     *
     * @return the user-friendly message
     */
    public String getUserMessage() {
        return userMessage != null ? userMessage : getMessage();
    }

    /**
     * Adds contextual information to this exception for debugging.
     *
     * @param key Context key
     * @param value Context value
     * @return this exception for method chaining
     */
    public ScheduleMCException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
     * Gets all contextual metadata attached to this exception.
     *
     * @return unmodifiable map of context data
     */
    public Map<String, Object> getContext() {
        return new HashMap<>(context);
    }

    /**
     * Gets a specific context value.
     *
     * @param key Context key
     * @return Context value or null if not found
     */
    @Nullable
    public Object getContextValue(String key) {
        return context.get(key);
    }
}
