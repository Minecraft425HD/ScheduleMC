package de.rolandsw.schedulemc.exceptions;

/**
 * Exception thrown when plot-related operations fail.
 * <p>
 * This exception covers all plot system errors including:
 * <ul>
 *   <li>Invalid plot coordinates or boundaries</li>
 *   <li>Plot ownership conflicts</li>
 *   <li>Plot merge/split failures</li>
 *   <li>Plot data persistence errors</li>
 *   <li>Spatial index inconsistencies</li>
 * </ul>
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 * @see de.rolandsw.schedulemc.region.PlotManager
 * @see de.rolandsw.schedulemc.region.PlotSpatialIndex
 */
public class PlotException extends ScheduleMCException {

    /**
     * Constructs a new plot exception with the specified detail message.
     *
     * @param message the detail message explaining the plot error
     */
    public PlotException(String message) {
        super(message);
    }

    /**
     * Constructs a new plot exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the plot error
     * @param cause the underlying cause of this exception
     */
    public PlotException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new plot exception with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public PlotException(Throwable cause) {
        super(cause);
    }
}
