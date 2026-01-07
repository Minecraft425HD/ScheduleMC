package de.rolandsw.schedulemc.exceptions;
nimport de.rolandsw.schedulemc.util.StringUtils;

import javax.annotation.Nullable;
import java.util.UUID;

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
     * Standard error codes for economy operations.
     */
    public static final class ErrorCodes {
        public static final String INSUFFICIENT_FUNDS = "ECO_001";
        public static final String INVALID_AMOUNT = "ECO_002";
        public static final String ACCOUNT_NOT_FOUND = "ECO_003";
        public static final String RATE_LIMIT_EXCEEDED = "ECO_004";
        public static final String TRANSFER_LIMIT_EXCEEDED = "ECO_005";
        public static final String CREDIT_SCORE_TOO_LOW = "ECO_006";
        public static final String TRANSACTION_FAILED = "ECO_007";

        private ErrorCodes() {}
    }

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

    /**
     * Advanced constructor with full error context.
     *
     * @param message Technical message for developers/logs
     * @param userMessage User-friendly message for display
     * @param errorCode Machine-readable error code (use ErrorCodes constants)
     * @param retryable Whether this error can be retried
     * @param cause Underlying cause
     */
    public EconomyException(String message, @Nullable String userMessage,
                           @Nullable String errorCode, boolean retryable,
                           @Nullable Throwable cause) {
        super(message, userMessage, errorCode, retryable, cause);
    }

    // ========== Factory Methods for Common Errors ==========

    /**
     * Creates an exception for insufficient funds.
     *
     * @param playerUUID Player UUID
     * @param required Required amount
     * @param available Available balance
     * @return Configured exception
     */
    public static EconomyException insufficientFunds(UUID playerUUID, double required, double available) {
        return new EconomyException(
            String.format("Insufficient funds: required %.2f€, available %.2f€ (player: %s)", required, available, playerUUID),
            String.format("§cNicht genug Guthaben! Benötigt: §6%.2f€§c, Verfügbar: §e%.2f€", required, available),
            ErrorCodes.INSUFFICIENT_FUNDS,
            false, // Not retryable - need more money
            null
        ).withContext("playerUUID", playerUUID)
         .withContext("required", required)
         .withContext("available", available);
    }

    /**
     * Creates an exception for invalid amount.
     *
     * @param amount Invalid amount
     * @param reason Reason why invalid
     * @return Configured exception
     */
    public static EconomyException invalidAmount(double amount, String reason) {
        return new EconomyException(
            String.format("Invalid amount: %.2f - %s", amount, reason),
            String.format("§cUngültiger Betrag: §f%s", reason),
            ErrorCodes.INVALID_AMOUNT,
            false, // Not retryable - input error
            null
        ).withContext("amount", amount)
         .withContext("reason", reason);
    }

    /**
     * Creates an exception for rate limit exceeded.
     *
     * @param operation Operation that was rate limited
     * @param playerUUID Player UUID
     * @return Configured exception
     */
    public static EconomyException rateLimitExceeded(String operation, UUID playerUUID) {
        return new EconomyException(
            String.format("Rate limit exceeded for %s (player: %s)", operation, playerUUID),
            "§cZu viele Anfragen! Bitte warte kurz und versuche es erneut.",
            ErrorCodes.RATE_LIMIT_EXCEEDED,
            true, // Retryable after cooldown
            null
        ).withContext("operation", operation)
         .withContext("playerUUID", playerUUID);
    }

    /**
     * Creates an exception for account not found.
     *
     * @param playerUUID Player UUID
     * @return Configured exception
     */
    public static EconomyException accountNotFound(UUID playerUUID) {
        return new EconomyException(
            String.format("Account not found for player: %s", playerUUID),
            "§cKonto nicht gefunden! Bitte kontaktiere einen Administrator.",
            ErrorCodes.ACCOUNT_NOT_FOUND,
            true, // Retryable - might be created
            null
        ).withContext("playerUUID", playerUUID);
    }
}
