package de.rolandsw.schedulemc.exceptions;

/**
 * Exception thrown when production system operations fail.
 * <p>
 * This exception covers all production-related errors including:
 * <ul>
 *   <li>Invalid production recipes or configurations</li>
 *   <li>Insufficient resources for production</li>
 *   <li>Production chain interruptions</li>
 *   <li>Drug production system failures</li>
 *   <li>Quality tier calculation errors</li>
 *   <li>Processing block entity state errors</li>
 * </ul>
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 * @see de.rolandsw.schedulemc.production.ProductionManager
 * @see de.rolandsw.schedulemc.production.blockentity.UnifiedProcessingBlockEntity
 */
public class ProductionException extends ScheduleMCException {

    /**
     * Constructs a new production exception with the specified detail message.
     *
     * @param message the detail message explaining the production error
     */
    public ProductionException(String message) {
        super(message);
    }

    /**
     * Constructs a new production exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the production error
     * @param cause the underlying cause of this exception
     */
    public ProductionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new production exception with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public ProductionException(Throwable cause) {
        super(cause);
    }
}
