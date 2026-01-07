package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ErrorRecovery
 *
 * @since 1.0
 */
@DisplayName("ErrorRecovery Tests")
class ErrorRecoveryTest {

    // ========== Retry Tests ==========

    @Test
    @DisplayName("retry - Should succeed on first attempt")
    void testRetrySuccessFirstAttempt() {
        // Arrange
        Callable<String> operation = () -> "success";

        // Act
        ErrorRecovery.Result<String> result = ErrorRecovery.retry(
            operation,
            ErrorRecovery.RetryConfig.defaults(),
            "testOperation"
        );

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo("success");
        assertThat(result.getAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("retry - Should succeed after retries")
    void testRetrySuccessAfterRetries() {
        // Arrange
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<String> operation = () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("Transient error");
            }
            return "success";
        };

        // Act
        ErrorRecovery.Result<String> result = ErrorRecovery.retry(
            operation,
            ErrorRecovery.RetryConfig.defaults(),
            "testOperation"
        );

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo("success");
        assertThat(result.getAttempts()).isEqualTo(3);
    }

    @Test
    @DisplayName("retry - Should fail after max retries")
    void testRetryFailureAfterMaxRetries() {
        // Arrange
        Callable<String> operation = () -> {
            throw new RuntimeException("Persistent error");
        };

        // Act
        ErrorRecovery.Result<String> result = ErrorRecovery.retry(
            operation,
            ErrorRecovery.RetryConfig.defaults(),
            "testOperation"
        );

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isNotNull();
        assertThat(result.getError().getMessage()).contains("Persistent error");
        assertThat(result.getAttempts()).isEqualTo(3);
    }

    @Test
    @DisplayName("retry - Should use fast config")
    void testRetryFastConfig() {
        // Arrange
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<String> operation = () -> {
            attempts.incrementAndGet();
            throw new RuntimeException("Error");
        };

        // Act
        ErrorRecovery.Result<String> result = ErrorRecovery.retry(
            operation,
            ErrorRecovery.RetryConfig.fast(),
            "testOperation"
        );

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(attempts.get()).isEqualTo(2); // Fast config = 2 attempts
    }

    @Test
    @DisplayName("retry - Should use aggressive config")
    void testRetryAggressiveConfig() {
        // Arrange
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<String> operation = () -> {
            attempts.incrementAndGet();
            throw new RuntimeException("Error");
        };

        // Act
        ErrorRecovery.Result<String> result = ErrorRecovery.retry(
            operation,
            ErrorRecovery.RetryConfig.aggressive(),
            "testOperation"
        );

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(attempts.get()).isEqualTo(5); // Aggressive config = 5 attempts
    }

    // ========== Fallback Tests ==========

    @Test
    @DisplayName("withFallback - Should return operation result on success")
    void testWithFallbackSuccess() {
        // Act
        String result = ErrorRecovery.withFallback(
            () -> "primary",
            "fallback",
            "testOperation"
        );

        // Assert
        assertThat(result).isEqualTo("primary");
    }

    @Test
    @DisplayName("withFallback - Should return fallback on failure")
    void testWithFallbackFailure() {
        // Act
        String result = ErrorRecovery.withFallback(
            () -> { throw new RuntimeException("Error"); },
            "fallback",
            "testOperation"
        );

        // Assert
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    @DisplayName("withFallbackSupplier - Should return operation result on success")
    void testWithFallbackSupplierSuccess() {
        // Act
        String result = ErrorRecovery.withFallbackSupplier(
            () -> "primary",
            () -> "fallback",
            "testOperation"
        );

        // Assert
        assertThat(result).isEqualTo("primary");
    }

    @Test
    @DisplayName("withFallbackSupplier - Should use fallback supplier on failure")
    void testWithFallbackSupplierFailure() {
        // Act
        String result = ErrorRecovery.withFallbackSupplier(
            () -> { throw new RuntimeException("Error"); },
            () -> "fallback",
            "testOperation"
        );

        // Assert
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    @DisplayName("withFallbackSupplier - Should throw if both fail")
    void testWithFallbackSupplierBothFail() {
        // Act & Assert
        assertThatThrownBy(() ->
            ErrorRecovery.withFallbackSupplier(
                () -> { throw new RuntimeException("Primary error"); },
                () -> { throw new RuntimeException("Fallback error"); },
                "testOperation"
            )
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Both operation and fallback failed");
    }

    // ========== Safe Execution Tests ==========

    @Test
    @DisplayName("safeExecute - Should return true on success")
    void testSafeExecuteSuccess() {
        // Arrange
        AtomicInteger counter = new AtomicInteger(0);

        // Act
        boolean result = ErrorRecovery.safeExecute(
            counter::incrementAndGet,
            "testOperation"
        );

        // Assert
        assertThat(result).isTrue();
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("safeExecute - Should return false on failure")
    void testSafeExecuteFailure() {
        // Act
        boolean result = ErrorRecovery.safeExecute(
            () -> { throw new RuntimeException("Error"); },
            "testOperation"
        );

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("safeCall - Should return result on success")
    void testSafeCallSuccess() {
        // Act
        String result = ErrorRecovery.safeCall(
            () -> "success",
            "testOperation"
        );

        // Assert
        assertThat(result).isEqualTo("success");
    }

    @Test
    @DisplayName("safeCall - Should return null on failure")
    void testSafeCallFailure() {
        // Act
        String result = ErrorRecovery.safeCall(
            () -> { throw new RuntimeException("Error"); },
            "testOperation"
        );

        // Assert
        assertThat(result).isNull();
    }

    // ========== Validation with Recovery Tests ==========

    @Test
    @DisplayName("validateOrDefault - Should return value if valid")
    void testValidateOrDefaultValid() {
        // Act
        int result = ErrorRecovery.validateOrDefault(
            50,
            v -> v >= 0 && v <= 100,
            0,
            "percentage"
        );

        // Assert
        assertThat(result).isEqualTo(50);
    }

    @Test
    @DisplayName("validateOrDefault - Should return default if invalid")
    void testValidateOrDefaultInvalid() {
        // Act
        int result = ErrorRecovery.validateOrDefault(
            150,
            v -> v >= 0 && v <= 100,
            50,
            "percentage"
        );

        // Assert
        assertThat(result).isEqualTo(50);
    }

    @Test
    @DisplayName("validateOrDefault - Should return default if null")
    void testValidateOrDefaultNull() {
        // Act
        Integer result = ErrorRecovery.validateOrDefault(
            null,
            v -> v >= 0 && v <= 100,
            50,
            "percentage"
        );

        // Assert
        assertThat(result).isEqualTo(50);
    }

    @Test
    @DisplayName("validateOrDefault - Should return default if validator throws")
    void testValidateOrDefaultValidatorThrows() {
        // Act
        String result = ErrorRecovery.validateOrDefault(
            "test",
            v -> { throw new RuntimeException("Validator error"); },
            "default",
            "testField"
        );

        // Assert
        assertThat(result).isEqualTo("default");
    }

    // ========== Error Aggregation Tests ==========

    @Test
    @DisplayName("executeAll - Should execute all operations")
    void testExecuteAll() {
        // Arrange
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> operations = Arrays.asList(
            counter::incrementAndGet,
            counter::incrementAndGet,
            counter::incrementAndGet
        );

        // Act
        int successCount = ErrorRecovery.executeAll(operations, "testBatch");

        // Assert
        assertThat(successCount).isEqualTo(3);
        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("executeAll - Should continue on failures")
    void testExecuteAllWithFailures() {
        // Arrange
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> operations = Arrays.asList(
            counter::incrementAndGet,
            () -> { throw new RuntimeException("Error"); },
            counter::incrementAndGet
        );

        // Act
        int successCount = ErrorRecovery.executeAll(operations, "testBatch");

        // Assert
        assertThat(successCount).isEqualTo(2);
        assertThat(counter.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("executeAll - Should handle empty list")
    void testExecuteAllEmpty() {
        // Act
        int successCount = ErrorRecovery.executeAll(new ArrayList<>(), "testBatch");

        // Assert
        assertThat(successCount).isEqualTo(0);
    }

    // ========== Circuit Breaker Tests ==========

    @Test
    @DisplayName("CircuitBreaker - Should open after threshold")
    void testCircuitBreakerOpens() {
        // Arrange
        ErrorRecovery.CircuitBreaker breaker = new ErrorRecovery.CircuitBreaker(3, 1000L);

        // Act
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.isOpen()).isFalse();

        breaker.recordFailure();

        // Assert
        assertThat(breaker.isOpen()).isTrue();
        assertThat(breaker.getConsecutiveFailures()).isEqualTo(3);
    }

    @Test
    @DisplayName("CircuitBreaker - Should reset on success")
    void testCircuitBreakerResetOnSuccess() {
        // Arrange
        ErrorRecovery.CircuitBreaker breaker = new ErrorRecovery.CircuitBreaker(3, 1000L);

        // Act
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordSuccess();

        // Assert
        assertThat(breaker.isOpen()).isFalse();
        assertThat(breaker.getConsecutiveFailures()).isEqualTo(0);
    }

    @Test
    @DisplayName("CircuitBreaker - Should close after timeout")
    void testCircuitBreakerClosesAfterTimeout() throws InterruptedException {
        // Arrange
        ErrorRecovery.CircuitBreaker breaker = new ErrorRecovery.CircuitBreaker(2, 100L);

        // Act
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.isOpen()).isTrue();

        // Wait for reset timeout
        Thread.sleep(150);

        // Assert
        assertThat(breaker.isOpen()).isFalse();
    }

    @Test
    @DisplayName("CircuitBreaker - Should reset manually")
    void testCircuitBreakerManualReset() {
        // Arrange
        ErrorRecovery.CircuitBreaker breaker = new ErrorRecovery.CircuitBreaker(2, 1000L);

        // Act
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.isOpen()).isTrue();

        breaker.reset();

        // Assert
        assertThat(breaker.isOpen()).isFalse();
        assertThat(breaker.getConsecutiveFailures()).isEqualTo(0);
    }

    // ========== Result Tests ==========

    @Test
    @DisplayName("Result - getOrThrow should return value on success")
    void testResultGetOrThrowSuccess() throws Exception {
        // Arrange
        ErrorRecovery.Result<String> result = ErrorRecovery.Result.success("value", 1);

        // Act & Assert
        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    @DisplayName("Result - getOrThrow should throw on failure")
    void testResultGetOrThrowFailure() {
        // Arrange
        Exception error = new RuntimeException("Error");
        ErrorRecovery.Result<String> result = ErrorRecovery.Result.failure(error, 3);

        // Act & Assert
        assertThatThrownBy(result::getOrThrow)
            .isEqualTo(error);
    }

    @Test
    @DisplayName("Result - getOrDefault should return value on success")
    void testResultGetOrDefaultSuccess() {
        // Arrange
        ErrorRecovery.Result<String> result = ErrorRecovery.Result.success("value", 1);

        // Act & Assert
        assertThat(result.getOrDefault("default")).isEqualTo("value");
    }

    @Test
    @DisplayName("Result - getOrDefault should return default on failure")
    void testResultGetOrDefaultFailure() {
        // Arrange
        ErrorRecovery.Result<String> result = ErrorRecovery.Result.failure(
            new RuntimeException("Error"), 3
        );

        // Act & Assert
        assertThat(result.getOrDefault("default")).isEqualTo("default");
    }

    @Test
    @DisplayName("Result - ifSuccess should execute consumer on success")
    void testResultIfSuccessExecutes() {
        // Arrange
        ErrorRecovery.Result<String> result = ErrorRecovery.Result.success("value", 1);
        AtomicInteger counter = new AtomicInteger(0);

        // Act
        result.ifSuccess(v -> counter.incrementAndGet());

        // Assert
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Result - ifSuccess should not execute on failure")
    void testResultIfSuccessDoesNotExecute() {
        // Arrange
        ErrorRecovery.Result<String> result = ErrorRecovery.Result.failure(
            new RuntimeException("Error"), 3
        );
        AtomicInteger counter = new AtomicInteger(0);

        // Act
        result.ifSuccess(v -> counter.incrementAndGet());

        // Assert
        assertThat(counter.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("Result - ifFailure should execute consumer on failure")
    void testResultIfFailureExecutes() {
        // Arrange
        ErrorRecovery.Result<String> result = ErrorRecovery.Result.failure(
            new RuntimeException("Error"), 3
        );
        AtomicInteger counter = new AtomicInteger(0);

        // Act
        result.ifFailure(e -> counter.incrementAndGet());

        // Assert
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Result - ifFailure should not execute on success")
    void testResultIfFailureDoesNotExecute() {
        // Arrange
        ErrorRecovery.Result<String> result = ErrorRecovery.Result.success("value", 1);
        AtomicInteger counter = new AtomicInteger(0);

        // Act
        result.ifFailure(e -> counter.incrementAndGet());

        // Assert
        assertThat(counter.get()).isEqualTo(0);
    }
}
