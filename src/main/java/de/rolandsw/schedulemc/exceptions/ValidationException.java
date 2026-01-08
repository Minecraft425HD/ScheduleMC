package de.rolandsw.schedulemc.exceptions;

/**
 * Exception thrown when input validation fails.
 * <p>
 * This exception covers all validation errors including:
 * <ul>
 *   <li>Invalid input formats (names, coordinates, amounts)</li>
 *   <li>Out-of-range values exceeding defined limits</li>
 *   <li>Regex pattern validation failures</li>
 *   <li>Null or empty required fields</li>
 *   <li>Data type mismatches</li>
 *   <li>Constraint violations (max length, max amount, etc.)</li>
 * </ul>
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 * @see de.rolandsw.schedulemc.util.InputValidation
 */
public class ValidationException extends ScheduleMCException {

    private final String fieldName;
    private final Object invalidValue;

    /**
     * Constructs a new validation exception with the specified detail message.
     *
     * @param message the detail message explaining the validation error
     */
    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
    }

    /**
     * Constructs a new validation exception with field context.
     *
     * @param message the detail message explaining the validation error
     * @param fieldName the name of the field that failed validation
     * @param invalidValue the invalid value that was rejected
     */
    public ValidationException(String message, String fieldName, Object invalidValue) {
        super(String.format("%s (field: %s, value: %s)", message, fieldName, invalidValue));
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    /**
     * Constructs a new validation exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the validation error
     * @param cause the underlying cause of this exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.invalidValue = null;
    }

    /**
     * Gets the name of the field that failed validation.
     *
     * @return the field name, or null if not specified
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the invalid value that was rejected.
     *
     * @return the invalid value, or null if not specified
     */
    public Object getInvalidValue() {
        return invalidValue;
    }
}
