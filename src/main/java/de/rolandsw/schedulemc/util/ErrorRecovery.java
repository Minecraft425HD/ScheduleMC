package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Error Recovery Utility for resilient error handling.
 *
 * <p>Provides automated retry logic, fallback mechanisms, and graceful degradation
 * for operations that may fail due to transient errors.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Retry with exponential backoff</li>
 *   <li>Fallback values on failure</li>
 *   <li>Graceful degradation</li>
 *   <li>Circuit breaker pattern</li>
 *   <li>Automatic error logging</li>
 * </ul>
 *
 * @since 1.0
 */
public class ErrorRecovery {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Default retry configuration
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_INITIAL_DELAY_MS = 100L;
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;

    /**
     * Retry configuration for operations.
     */
    public static class RetryConfig {
        private final int maxRetries;
        private final long initialDelayMs;
        private final double backoffMultiplier;
        private final boolean logRetries;

        public RetryConfig(int maxRetries, long initialDelayMs, double backoffMultiplier, boolean logRetries) {
            this.maxRetries = maxRetries;
            this.initialDelayMs = initialDelayMs;
            this.backoffMultiplier = backoffMultiplier;
            this.logRetries = logRetries;
        }

        public static RetryConfig defaults() {
            return new RetryConfig(DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY_MS,
                                 DEFAULT_BACKOFF_MULTIPLIER, true);
        }

        public static RetryConfig fast() {
            return new RetryConfig(2, 50L, 2.0, false);
        }

        public static RetryConfig aggressive() {
            return new RetryConfig(5, 200L, 1.5, true);
        }
    }

    /**
     * Result of an operation with error recovery.
     *
     * @param <T> The result type
     */
    public static class Result<T> {
        private final T value;
        private final boolean success;
        private final Exception error;
        private final int attempts;

        private Result(T value, boolean success, Exception error, int attempts) {
            this.value = value;
            this.success = success;
            this.error = error;
            this.attempts = attempts;
        }

        public static <T> Result<T> success(T value, int attempts) {
            return new Result<>(value, true, null, attempts);
        }

        public static <T> Result<T> failure(Exception error, int attempts) {
            return new Result<>(null, false, error, attempts);
        }

        public boolean isSuccess() { return success; }
        public boolean isFailure() { return !success; }
        @Nullable public T getValue() { return value; }
        @Nullable public Exception getError() { return error; }
        public int getAttempts() { return attempts; }

        /**
         * Gets the value or throws the error.
         */
        public T getOrThrow() throws Exception {
            if (success) {
                return value;
            }
            throw error;
        }

        /**
         * Gets the value or returns a fallback.
         */
        public T getOrDefault(T fallback) {
            return success ? value : fallback;
        }

        /**
         * Applies a consumer if successful.
         */
        public void ifSuccess(Consumer<T> consumer) {
            if (success && value != null) {
                consumer.accept(value);
            }
        }

        /**
         * Applies a consumer if failed.
         */
        public void ifFailure(Consumer<Exception> consumer) {
            if (!success && error != null) {
                consumer.accept(error);
            }
        }
    }

    // ========== Retry Methods ==========

    /**
     * Retries an operation with exponential backoff.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Result<Data> result = ErrorRecovery.retry(
     *     () -> loadDataFromNetwork(),
     *     RetryConfig.defaults(),
     *     "loadNetworkData"
     * );
     * }</pre>
     *
     * @param operation The operation to retry
     * @param config Retry configuration
     * @param operationName Name for logging
     * @param <T> Result type
     * @return Result with value or error
     */
    public static <T> Result<T> retry(@Nonnull Callable<T> operation,
                                     @Nonnull RetryConfig config,
                                     @Nonnull String operationName) {
        Exception lastError = null;
        long delay = config.initialDelayMs;

        for (int attempt = 1; attempt <= config.maxRetries; attempt++) {
            try {
                T result = operation.call();
                if (attempt > 1 && config.logRetries) {
                    LOGGER.info("{}: Succeeded on attempt {}/{}",
                              operationName, attempt, config.maxRetries);
                }
                return Result.success(result, attempt);

            } catch (Exception e) {
                lastError = e;

                if (attempt < config.maxRetries) {
                    if (config.logRetries) {
                        LOGGER.warn("{}: Attempt {}/{} failed: {}. Retrying in {}ms...",
                                  operationName, attempt, config.maxRetries,
                                  e.getMessage(), delay);
                    }

                    // Exponential backoff
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        LOGGER.error("{}: Retry interrupted", operationName);
                        return Result.failure(lastError, attempt);
                    }

                    delay = (long) (delay * config.backoffMultiplier);
                } else {
                    if (config.logRetries) {
                        LOGGER.error("{}: All {} attempts failed. Last error: {}",
                                   operationName, config.maxRetries, e.getMessage());
                    }
                }
            }
        }

        return Result.failure(lastError, config.maxRetries);
    }

    /**
     * Retries with default configuration.
     */
    public static <T> Result<T> retry(@Nonnull Callable<T> operation, @Nonnull String operationName) {
        return retry(operation, RetryConfig.defaults(), operationName);
    }

    // ========== Fallback Methods ==========

    /**
     * Executes operation with fallback value on failure.
     *
     * <p>Example:</p>
     * <pre>{@code
     * int value = ErrorRecovery.withFallback(
     *     () -> loadConfigValue(),
     *     100, // fallback
     *     "loadConfig"
     * );
     * }</pre>
     *
     * @param operation The operation to execute
     * @param fallbackValue Value to return on failure
     * @param operationName Name for logging
     * @param <T> Result type
     * @return Operation result or fallback
     */
    public static <T> T withFallback(@Nonnull Supplier<T> operation,
                                     @Nonnull T fallbackValue,
                                     @Nonnull String operationName) {
        try {
            return operation.get();
        } catch (Exception e) {
            LOGGER.warn("{}: Operation failed, using fallback. Error: {}",
                      operationName, e.getMessage());
            return fallbackValue;
        }
    }

    /**
     * Executes operation with fallback supplier on failure.
     */
    public static <T> T withFallbackSupplier(@Nonnull Supplier<T> operation,
                                            @Nonnull Supplier<T> fallbackSupplier,
                                            @Nonnull String operationName) {
        try {
            return operation.get();
        } catch (Exception e) {
            LOGGER.warn("{}: Operation failed, using fallback supplier. Error: {}",
                      operationName, e.getMessage());
            try {
                return fallbackSupplier.get();
            } catch (Exception fallbackError) {
                LOGGER.error("{}: Fallback supplier also failed: {}",
                           operationName, fallbackError.getMessage());
                throw new RuntimeException("Both operation and fallback failed", fallbackError);
            }
        }
    }

    // ========== Safe Execution Methods ==========

    /**
     * Executes operation and logs errors without throwing.
     *
     * <p>Useful for non-critical operations that shouldn't crash the server:</p>
     * <pre>{@code
     * ErrorRecovery.safeExecute(
     *     () -> updateStatistics(),
     *     "statisticsUpdate"
     * );
     * }</pre>
     *
     * @param operation The operation to execute
     * @param operationName Name for logging
     * @return true if successful, false if failed
     */
    public static boolean safeExecute(@Nonnull Runnable operation, @Nonnull String operationName) {
        try {
            operation.run();
            return true;
        } catch (Exception e) {
            LOGGER.error("{}: Safe execution failed: {}", operationName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Executes operation and returns result, or null on failure.
     */
    @Nullable
    public static <T> T safeCall(@Nonnull Supplier<T> operation, @Nonnull String operationName) {
        try {
            return operation.get();
        } catch (Exception e) {
            LOGGER.error("{}: Safe call failed: {}", operationName, e.getMessage(), e);
            return null;
        }
    }

    // ========== Validation with Recovery ==========

    /**
     * Validates input and recovers with default value if invalid.
     *
     * <p>Example:</p>
     * <pre>{@code
     * int safeValue = ErrorRecovery.validateOrDefault(
     *     userInput,
     *     v -> v >= 0 && v <= 100,
     *     50, // default
     *     "percentage"
     * );
     * }</pre>
     *
     * @param value The value to validate
     * @param validator Validation function
     * @param defaultValue Default value if validation fails
     * @param fieldName Field name for logging
     * @param <T> Value type
     * @return Validated value or default
     */
    public static <T> T validateOrDefault(@Nullable T value,
                                         @Nonnull java.util.function.Predicate<T> validator,
                                         @Nonnull T defaultValue,
                                         @Nonnull String fieldName) {
        if (value == null) {
            LOGGER.warn("{}: Value is null, using default: {}", fieldName, defaultValue);
            return defaultValue;
        }

        try {
            if (validator.test(value)) {
                return value;
            } else {
                LOGGER.warn("{}: Validation failed for value: {}, using default: {}",
                          fieldName, value, defaultValue);
                return defaultValue;
            }
        } catch (Exception e) {
            LOGGER.error("{}: Validation threw exception for value: {}, using default: {}",
                       fieldName, value, defaultValue);
            return defaultValue;
        }
    }

    // ========== Error Aggregation ==========

    /**
     * Executes multiple operations and collects all errors.
     *
     * <p>Useful for batch operations where you want to attempt all operations
     * even if some fail:</p>
     * <pre>{@code
     * List<Runnable> tasks = List.of(
     *     () -> savePlayer1(),
     *     () -> savePlayer2(),
     *     () -> savePlayer3()
     * );
     * ErrorRecovery.executeAll(tasks, "saveAllPlayers");
     * }</pre>
     *
     * @param operations List of operations to execute
     * @param operationName Name for logging
     * @return Number of successful operations
     */
    public static int executeAll(@Nonnull Iterable<Runnable> operations, @Nonnull String operationName) {
        int successCount = 0;
        int failureCount = 0;

        for (Runnable operation : operations) {
            try {
                operation.run();
                successCount++;
            } catch (Exception e) {
                failureCount++;
                LOGGER.error("{}: Operation #{} failed: {}",
                           operationName, successCount + failureCount, e.getMessage());
            }
        }

        if (failureCount > 0) {
            LOGGER.warn("{}: Completed with {} successes and {} failures",
                      operationName, successCount, failureCount);
        }

        return successCount;
    }

    // ========== Circuit Breaker State ==========

    /**
     * Simple circuit breaker for preventing repeated failures.
     */
    public static class CircuitBreaker {
        private final int failureThreshold;
        private final long resetTimeoutMs;
        private int consecutiveFailures = 0;
        private long lastFailureTime = 0;
        private boolean isOpen = false;

        public CircuitBreaker(int failureThreshold, long resetTimeoutMs) {
            this.failureThreshold = failureThreshold;
            this.resetTimeoutMs = resetTimeoutMs;
        }

        public boolean isOpen() {
            if (isOpen && System.currentTimeMillis() - lastFailureTime > resetTimeoutMs) {
                reset();
            }
            return isOpen;
        }

        public void recordSuccess() {
            consecutiveFailures = 0;
            isOpen = false;
        }

        public void recordFailure() {
            consecutiveFailures++;
            lastFailureTime = System.currentTimeMillis();
            if (consecutiveFailures >= failureThreshold) {
                isOpen = true;
            }
        }

        public void reset() {
            consecutiveFailures = 0;
            isOpen = false;
        }

        public int getConsecutiveFailures() {
            return consecutiveFailures;
        }
    }

    private ErrorRecovery() {
        throw new UnsupportedOperationException("Utility class");
    }
}
