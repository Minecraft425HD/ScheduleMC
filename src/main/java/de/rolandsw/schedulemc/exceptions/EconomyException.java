package de.rolandsw.schedulemc.exceptions;

/**
 * Exception thrown when economy-related operations fail.
 * <p>
 * This exception covers all economic system errors including:
 * <ul>
 *   <li>Insufficient funds for transactions</li>
 *   <li>Invalid transaction amounts (negative, exceeding limits)</li>
 *   <li>Bank account operation failures</li>
 *   <li>Salary calculation errors</li>
 *   <li>Tax computation failures</li>
 *   <li>Credit score violations</li>
 * </ul>
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 * @see de.rolandsw.schedulemc.economy.EconomyManager
 * @see de.rolandsw.schedulemc.economy.CreditScoreManager
 */
public class EconomyException extends ScheduleMCException {

    /**
     * Constructs a new economy exception with the specified detail message.
     *
     * @param message the detail message explaining the economic error
     */
    public EconomyException(String message) {
        super(message);
    }

    /**
     * Constructs a new economy exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the economic error
     * @param cause the underlying cause of this exception
     */
    public EconomyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new economy exception with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public EconomyException(Throwable cause) {
        super(cause);
    }
}
