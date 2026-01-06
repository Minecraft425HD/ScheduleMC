package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for RecurringPayment (Standing Order Data Model)
 *
 * Tests cover:
 * - Payment creation and initialization
 * - Execution logic (timing, success/failure)
 * - Failure tracking (max 3 failures, auto-deactivation)
 * - Pause/resume mechanics
 * - Days until next calculation
 * - JSON serialization
 * - Edge cases: negative intervals, zero amounts
 *
 * BUSINESS LOGIC:
 * - Payments execute at specified intervals (days)
 * - Max 3 failures before auto-deactivation
 * - Retry failed payments after 1 day
 * - Paused payments don't execute
 */
@DisplayName("RecurringPayment Tests")
class RecurringPaymentTest {

    private static final UUID TEST_FROM = UUID.randomUUID();
    private static final UUID TEST_TO = UUID.randomUUID();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ═══════════════════════════════════════════════════════════
    // CREATION AND INITIALIZATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Creation and Initialization Tests")
    class CreationAndInitializationTests {

        @Test
        @DisplayName("Payment should be created with all required fields")
        void paymentShouldBeCreatedWithAllFields() {
            RecurringPayment payment = new RecurringPayment(
                TEST_FROM, TEST_TO, 1000.0, 7, "Rent", 0L
            );

            assertThat(payment.getFromPlayer()).isEqualTo(TEST_FROM);
            assertThat(payment.getToPlayer()).isEqualTo(TEST_TO);
            assertThat(payment.getAmount()).isEqualTo(1000.0);
            assertThat(payment.getIntervalDays()).isEqualTo(7);
            assertThat(payment.getDescription()).isEqualTo("Rent");
        }

        @Test
        @DisplayName("Payment should generate unique payment ID")
        void paymentShouldGenerateUniqueId() {
            RecurringPayment payment1 = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test", 0L);
            RecurringPayment payment2 = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test", 0L);

            assertThat(payment1.getPaymentId()).isNotNull();
            assertThat(payment2.getPaymentId()).isNotNull();
            assertThat(payment1.getPaymentId()).isNotEqualTo(payment2.getPaymentId());
        }

        @Test
        @DisplayName("Payment ID should be valid UUID format")
        void paymentIdShouldBeValidUUID() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test", 0L);

            assertThatCode(() -> UUID.fromString(payment.getPaymentId()))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("New payment should be active")
        void newPaymentShouldBeActive() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test", 0L);

            assertThat(payment.isActive()).isTrue();
        }

        @Test
        @DisplayName("New payment should have zero failure count")
        void newPaymentShouldHaveZeroFailureCount() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test", 0L);

            assertThat(payment.getFailureCount()).isZero();
        }

        @Test
        @DisplayName("Next execution day should be currentDay + interval")
        void nextExecutionDayShouldBeCorrect() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 10L);

            assertThat(payment.getNextExecutionDay()).isEqualTo(17L); // 10 + 7
        }

        @ParameterizedTest
        @CsvSource({
            "0, 5, 5",    // Day 0, interval 5 → execute on day 5
            "10, 3, 13",  // Day 10, interval 3 → execute on day 13
            "100, 1, 101" // Day 100, interval 1 → execute on day 101
        })
        @DisplayName("Next execution should calculate correctly for various inputs")
        void nextExecutionShouldCalculateCorrectly(long currentDay, int interval, long expected) {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, interval, "Test", currentDay);

            assertThat(payment.getNextExecutionDay()).isEqualTo(expected);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EXECUTION LOGIC TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Execution Logic Tests")
    class ExecutionLogicTests {

        @Test
        @DisplayName("Execute should return false before execution day")
        void executeShouldReturnFalseBeforeExecutionDay() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

            boolean result = payment.execute(5L); // Day 5, should execute on day 7

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Execute should succeed on execution day with sufficient funds")
        void executeShouldSucceedOnExecutionDayWithFunds() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                boolean result = payment.execute(7L);

                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("Execute should call EconomyManager.transfer with correct parameters")
        void executeShouldCallTransferWithCorrectParameters() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 500.0, 1, "Salary", 0L);

                payment.execute(1L);

                economyMock.verify(() -> EconomyManager.transfer(
                    eq(TEST_FROM),
                    eq(TEST_TO),
                    eq(500.0),
                    contains("Salary")
                ));
            }
        }

        @Test
        @DisplayName("Successful execute should schedule next execution")
        void successfulExecuteShouldScheduleNextExecution() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.execute(7L);

                assertThat(payment.getNextExecutionDay()).isEqualTo(14L); // 7 + 7
            }
        }

        @Test
        @DisplayName("Successful execute should reset failure count")
        void successfulExecuteShouldResetFailureCount() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false) // First fail
                    .thenReturn(false) // Second fail
                    .thenReturn(true); // Then succeed

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.execute(7L);  // Fail
                payment.execute(8L);  // Fail
                payment.execute(9L);  // Succeed

                assertThat(payment.getFailureCount()).isZero();
            }
        }

        @Test
        @DisplayName("Execute after execution day should work")
        void executeAfterExecutionDayShouldWork() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                boolean result = payment.execute(10L); // Day 10, should have executed on day 7

                assertThat(result).isTrue();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FAILURE TRACKING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Failure Tracking Tests")
    class FailureTrackingTests {

        @Test
        @DisplayName("Failed execute should increase failure count")
        void failedExecuteShouldIncreaseFailureCount() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.execute(7L);

                assertThat(payment.getFailureCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("Failed execute should retry after 1 day")
        void failedExecuteShouldRetryAfterOneDay() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.execute(7L);

                assertThat(payment.getNextExecutionDay()).isEqualTo(8L); // 7 + 1
            }
        }

        @Test
        @DisplayName("Three failures should deactivate payment")
        void threeFailuresShouldDeactivatePayment() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.execute(7L);  // Failure 1
                payment.execute(8L);  // Failure 2
                payment.execute(9L);  // Failure 3

                assertThat(payment.isActive()).isFalse();
                assertThat(payment.getFailureCount()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("Two failures should not deactivate payment")
        void twoFailuresShouldNotDeactivatePayment() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.execute(7L);  // Failure 1
                payment.execute(8L);  // Failure 2

                assertThat(payment.isActive()).isTrue();
                assertThat(payment.getFailureCount()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("Failed execute should return false")
        void failedExecuteShouldReturnFalse() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                boolean result = payment.execute(7L);

                assertThat(result).isFalse();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PAUSE/RESUME TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Pause/Resume Tests")
    class PauseResumeTests {

        @Test
        @DisplayName("Pause should deactivate payment")
        void pauseShouldDeactivatePayment() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

            payment.pause();

            assertThat(payment.isActive()).isFalse();
        }

        @Test
        @DisplayName("Paused payment should not execute")
        void pausedPaymentShouldNotExecute() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.pause();
                boolean result = payment.execute(7L);

                assertThat(result).isFalse();
                economyMock.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("Resume should reactivate payment")
        void resumeShouldReactivatePayment() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

            payment.pause();
            payment.resume(10L);

            assertThat(payment.isActive()).isTrue();
        }

        @Test
        @DisplayName("Resume should reset failure count")
        void resumeShouldResetFailureCount() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.execute(7L);  // Fail once
                payment.resume(10L);

                assertThat(payment.getFailureCount()).isZero();
            }
        }

        @Test
        @DisplayName("Resume should reschedule next execution")
        void resumeShouldRescheduleNextExecution() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

            payment.pause();
            payment.resume(10L);

            assertThat(payment.getNextExecutionDay()).isEqualTo(17L); // 10 + 7
        }

        @Test
        @DisplayName("Resumed payment should execute normally")
        void resumedPaymentShouldExecuteNormally() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

                payment.pause();
                payment.resume(10L);
                boolean result = payment.execute(17L);

                assertThat(result).isTrue();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DAYS UNTIL NEXT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Days Until Next Tests")
    class DaysUntilNextTests {

        @Test
        @DisplayName("getDaysUntilNext should return correct value")
        void getDaysUntilNextShouldReturnCorrectValue() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

            int daysUntil = payment.getDaysUntilNext(3L);

            assertThat(daysUntil).isEqualTo(4); // Next execution on day 7, current day 3
        }

        @ParameterizedTest
        @CsvSource({
            "0, 7",  // Day 0 → 7 days until day 7
            "3, 4",  // Day 3 → 4 days until day 7
            "6, 1",  // Day 6 → 1 day until day 7
            "7, 0"   // Day 7 → 0 days (execution day)
        })
        @DisplayName("getDaysUntilNext should calculate correctly for various days")
        void getDaysUntilNextShouldCalculateCorrectly(long currentDay, int expected) {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

            int daysUntil = payment.getDaysUntilNext(currentDay);

            assertThat(daysUntil).isEqualTo(expected);
        }

        @Test
        @DisplayName("getDaysUntilNext should return -1 for inactive payment")
        void getDaysUntilNextShouldReturnMinusOneForInactive() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);

            payment.pause();
            int daysUntil = payment.getDaysUntilNext(3L);

            assertThat(daysUntil).isEqualTo(-1);
        }

        @Test
        @DisplayName("getDaysUntilNext should never be negative for active payment")
        void getDaysUntilNextShouldNeverBeNegativeForActive() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 7, 7, "Test", 0L);

            int daysUntil = payment.getDaysUntilNext(100L); // Way past execution day

            assertThat(daysUntil).isGreaterThanOrEqualTo(0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // JSON SERIALIZATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JSONSerializationTests {

        @Test
        @DisplayName("Payment should serialize to JSON")
        void paymentShouldSerializeToJSON() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 1000.0, 7, "Test", 0L);

            String json = gson.toJson(payment);

            assertThat(json)
                .contains("\"id\"")
                .contains("\"fromPlayer\"")
                .contains("\"toPlayer\"")
                .contains("\"amount\"")
                .contains("\"intervalDays\"")
                .contains("\"description\"");
        }

        @Test
        @DisplayName("Payment should deserialize from JSON")
        void paymentShouldDeserializeFromJSON() {
            RecurringPayment original = new RecurringPayment(TEST_FROM, TEST_TO, 1000.0, 7, "Rent", 0L);

            String json = gson.toJson(original);
            RecurringPayment deserialized = gson.fromJson(json, RecurringPayment.class);

            assertThat(deserialized.getFromPlayer()).isEqualTo(original.getFromPlayer());
            assertThat(deserialized.getToPlayer()).isEqualTo(original.getToPlayer());
            assertThat(deserialized.getAmount()).isEqualTo(original.getAmount());
            assertThat(deserialized.getIntervalDays()).isEqualTo(original.getIntervalDays());
            assertThat(deserialized.getDescription()).isEqualTo(original.getDescription());
        }

        @Test
        @DisplayName("Serialization should preserve payment ID")
        void serializationShouldPreservePaymentId() {
            RecurringPayment original = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test", 0L);

            String json = gson.toJson(original);
            RecurringPayment deserialized = gson.fromJson(json, RecurringPayment.class);

            assertThat(deserialized.getPaymentId()).isEqualTo(original.getPaymentId());
        }

        @Test
        @DisplayName("Serialization should preserve active state")
        void serializationShouldPreserveActiveState() {
            RecurringPayment original = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test", 0L);
            original.pause();

            String json = gson.toJson(original);
            RecurringPayment deserialized = gson.fromJson(json, RecurringPayment.class);

            assertThat(deserialized.isActive()).isFalse();
        }

        @Test
        @DisplayName("Serialization should preserve failure count")
        void serializationShouldPreserveFailureCount() {
            try (MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                RecurringPayment original = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", 0L);
                original.execute(7L); // Fail once

                String json = gson.toJson(original);
                RecurringPayment deserialized = gson.fromJson(json, RecurringPayment.class);

                assertThat(deserialized.getFailureCount()).isEqualTo(1);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Payment with zero amount should be created")
        void paymentWithZeroAmountShouldBeCreated() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 0.0, 1, "Test", 0L);

            assertThat(payment.getAmount()).isZero();
        }

        @Test
        @DisplayName("Payment with negative amount should be created")
        void paymentWithNegativeAmountShouldBeCreated() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, -100.0, 1, "Test", 0L);

            assertThat(payment.getAmount()).isEqualTo(-100.0);
        }

        @Test
        @DisplayName("Payment with empty description should work")
        void paymentWithEmptyDescriptionShouldWork() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "", 0L);

            assertThat(payment.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("Payment with very long description should work")
        void paymentWithVeryLongDescriptionShouldWork() {
            String longDesc = "A".repeat(1000);
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, longDesc, 0L);

            assertThat(payment.getDescription()).isEqualTo(longDesc);
        }

        @Test
        @DisplayName("Payment with interval of 1 day should work")
        void paymentWithIntervalOfOneDayShouldWork() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Daily", 0L);

            assertThat(payment.getIntervalDays()).isEqualTo(1);
            assertThat(payment.getNextExecutionDay()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Payment with very large interval should work")
        void paymentWithVeryLargeIntervalShouldWork() {
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 365, "Yearly", 0L);

            assertThat(payment.getIntervalDays()).isEqualTo(365);
        }

        @Test
        @DisplayName("Same player as from and to should be allowed by data model")
        void samePlayerAsFromAndToShouldBeAllowed() {
            // Note: This is prevented by RecurringPaymentManager, not the data model
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_FROM, 100.0, 1, "Self", 0L);

            assertThat(payment.getFromPlayer()).isEqualTo(TEST_FROM);
            assertThat(payment.getToPlayer()).isEqualTo(TEST_FROM);
        }

        @Test
        @DisplayName("Payment with very large current day should work")
        void paymentWithVeryLargeCurrentDayShouldWork() {
            long largeDay = Long.MAX_VALUE / 2;
            RecurringPayment payment = new RecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test", largeDay);

            assertThat(payment.getNextExecutionDay()).isGreaterThan(largeDay);
        }
    }
}
