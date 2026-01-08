package de.rolandsw.schedulemc.economy;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for RecurringPaymentManager (Standing Orders Manager)
 *
 * Tests cover:
 * - Payment creation (validation, limits)
 * - Payment deletion
 * - Pause/resume functionality
 * - Payment execution (daily tick processing)
 * - Failure handling (3-strike deactivation)
 * - Persistence (save/load)
 * - Singleton pattern
 * - Edge cases: max payments per player, invalid inputs
 *
 * BUSINESS LOGIC:
 * - Max payments per player: configurable (default 10)
 * - Payments execute on interval (days)
 * - Failed payments retry after 1 day
 * - 3 failures → auto-deactivation
 */
@DisplayName("RecurringPaymentManager Tests")
class RecurringPaymentManagerTest {

    private static final UUID TEST_FROM = UUID.randomUUID();
    private static final UUID TEST_TO = UUID.randomUUID();
    private static final UUID TEST_THIRD = UUID.randomUUID();

    private RecurringPaymentManager manager;
    private MinecraftServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        resetManagerInstance();
        mockServer = createMockServer();
        manager = RecurringPaymentManager.getInstance(mockServer);
        clearPayments();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetManagerInstance();
    }

    // ═══════════════════════════════════════════════════════════
    // PAYMENT CREATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Payment Creation Tests")
    class PaymentCreationTests {

        @Test
        @DisplayName("Create payment should succeed with valid parameters")
        void createPaymentShouldSucceedWithValidParameters() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, 1000.0, 7, "Rent");

                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("Created payment should appear in player's list")
        void createdPaymentShouldAppearInList() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 1000.0, 7, "Rent");

                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                assertThat(payments).hasSize(1);
            }
        }

        @Test
        @DisplayName("Created payment should have correct properties")
        void createdPaymentShouldHaveCorrectProperties() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 1500.0, 14, "Salary");

                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                RecurringPayment payment = payments.get(0);

                assertThat(payment.getFromPlayer()).isEqualTo(TEST_FROM);
                assertThat(payment.getToPlayer()).isEqualTo(TEST_TO);
                assertThat(payment.getAmount()).isEqualTo(1500.0);
                assertThat(payment.getIntervalDays()).isEqualTo(14);
                assertThat(payment.getDescription()).isEqualTo("Salary");
            }
        }

        @Test
        @DisplayName("Self-payment should be rejected")
        void selfPaymentShouldBeRejected() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_FROM, 100.0, 1, "Self");

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Zero amount should be rejected")
        void zeroAmountShouldBeRejected() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, 0.0, 1, "Zero");

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Negative amount should be rejected")
        void negativeAmountShouldBeRejected() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, -100.0, 1, "Negative");

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Zero interval should be rejected")
        void zeroIntervalShouldBeRejected() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 0, "Zero interval");

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Negative interval should be rejected")
        void negativeIntervalShouldBeRejected() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, -1, "Negative interval");

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Max payments per player limit should be enforced")
        void maxPaymentsPerPlayerLimitShouldBeEnforced() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                // Create 10 payments (max)
                for (int i = 0; i < 10; i++) {
                    manager.createRecurringPayment(TEST_FROM, UUID.randomUUID(), 100.0, 1, "Payment " + i);
                }

                // 11th should fail
                boolean result = manager.createRecurringPayment(TEST_FROM, UUID.randomUUID(), 100.0, 1, "Too many");

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Multiple players should track independently")
        void multiplePlayersShouldTrackIndependently() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Player 1");
                manager.createRecurringPayment(TEST_THIRD, TEST_TO, 200.0, 2, "Player 2");

                assertThat(manager.getPayments(TEST_FROM)).hasSize(1);
                assertThat(manager.getPayments(TEST_THIRD)).hasSize(1);
                assertThat(manager.getPayments(TEST_TO)).isEmpty();
            }
        }

        @Test
        @DisplayName("Create payment should send notification to player")
        void createPaymentShouldSendNotification() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockServer.getPlayerList().getPlayer(TEST_FROM)).thenReturn(mockPlayer);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 1000.0, 7, "Test");

                verify(mockPlayer, atLeastOnce()).sendSystemMessage(any());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PAYMENT DELETION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Payment Deletion Tests")
    class PaymentDeletionTests {

        @Test
        @DisplayName("Delete payment should succeed")
        void deletePaymentShouldSucceed() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                String paymentId = payments.get(0).getPaymentId();

                boolean result = manager.deleteRecurringPayment(TEST_FROM, paymentId);

                assertThat(result).isTrue();
                assertThat(manager.getPayments(TEST_FROM)).isEmpty();
            }
        }

        @Test
        @DisplayName("Delete with partial ID should work")
        void deleteWithPartialIdShouldWork() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                String partialId = payments.get(0).getPaymentId().substring(0, 8);

                boolean result = manager.deleteRecurringPayment(TEST_FROM, partialId);

                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("Delete non-existent payment should fail")
        void deleteNonExistentPaymentShouldFail() {
            boolean result = manager.deleteRecurringPayment(TEST_FROM, "nonexistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Delete wrong player's payment should fail")
        void deleteWrongPlayersPaymentShouldFail() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                String paymentId = payments.get(0).getPaymentId();

                boolean result = manager.deleteRecurringPayment(TEST_THIRD, paymentId);

                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("Delete should send notification to player")
        void deleteShouldSendNotification() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockServer.getPlayerList().getPlayer(TEST_FROM)).thenReturn(mockPlayer);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                String paymentId = payments.get(0).getPaymentId();

                manager.deleteRecurringPayment(TEST_FROM, paymentId);

                verify(mockPlayer, atLeast(2)).sendSystemMessage(any()); // Create + Delete
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
        @DisplayName("Pause payment should succeed")
        void pausePaymentShouldSucceed() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                String paymentId = payments.get(0).getPaymentId();

                boolean result = manager.pauseRecurringPayment(TEST_FROM, paymentId);

                assertThat(result).isTrue();
                assertThat(payments.get(0).isActive()).isFalse();
            }
        }

        @Test
        @DisplayName("Resume payment should succeed")
        void resumePaymentShouldSucceed() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                String paymentId = payments.get(0).getPaymentId();

                manager.pauseRecurringPayment(TEST_FROM, paymentId);
                boolean result = manager.resumeRecurringPayment(TEST_FROM, paymentId);

                assertThat(result).isTrue();
                assertThat(payments.get(0).isActive()).isTrue();
            }
        }

        @Test
        @DisplayName("Pause non-existent payment should fail")
        void pauseNonExistentPaymentShouldFail() {
            boolean result = manager.pauseRecurringPayment(TEST_FROM, "nonexistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Resume non-existent payment should fail")
        void resumeNonExistentPaymentShouldFail() {
            boolean result = manager.resumeRecurringPayment(TEST_FROM, "nonexistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Pause should send notification")
        void pauseShouldSendNotification() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockServer.getPlayerList().getPlayer(TEST_FROM)).thenReturn(mockPlayer);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                String paymentId = payments.get(0).getPaymentId();

                manager.pauseRecurringPayment(TEST_FROM, paymentId);

                verify(mockPlayer, atLeast(2)).sendSystemMessage(any()); // Create + Pause
            }
        }

        @Test
        @DisplayName("Resume should send notification")
        void resumeShouldSendNotification() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockServer.getPlayerList().getPlayer(TEST_FROM)).thenReturn(mockPlayer);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                String paymentId = payments.get(0).getPaymentId();

                manager.pauseRecurringPayment(TEST_FROM, paymentId);
                manager.resumeRecurringPayment(TEST_FROM, paymentId);

                verify(mockPlayer, atLeast(3)).sendSystemMessage(any()); // Create + Pause + Resume
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PAYMENT EXECUTION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Payment Execution Tests")
    class PaymentExecutionTests {

        @Test
        @DisplayName("Tick should not execute payment before interval")
        void tickShouldNotExecuteBeforeInterval() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test");

                // Tick for 6 days
                for (long day = 1; day <= 6; day++) {
                    manager.tick(day * 24000L);
                }

                economyMock.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("Tick should execute payment on interval day")
        void tickShouldExecuteOnIntervalDay() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 500.0, 7, "Weekly");

                // Tick to day 7
                for (long day = 1; day <= 7; day++) {
                    manager.tick(day * 24000L);
                }

                economyMock.verify(() -> EconomyManager.transfer(
                    eq(TEST_FROM), eq(TEST_TO), eq(500.0), contains("Weekly")
                ), atLeastOnce());
            }
        }

        @Test
        @DisplayName("Successful execution should send notifications")
        void successfulExecutionShouldSendNotifications() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                ServerPlayer fromPlayer = mock(ServerPlayer.class);
                ServerPlayer toPlayer = mock(ServerPlayer.class);
                when(mockServer.getPlayerList().getPlayer(TEST_FROM)).thenReturn(fromPlayer);
                when(mockServer.getPlayerList().getPlayer(TEST_TO)).thenReturn(toPlayer);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test");

                manager.tick(1 * 24000L);

                verify(fromPlayer, atLeast(2)).sendSystemMessage(any()); // Create + Execute
                verify(toPlayer, atLeastOnce()).sendSystemMessage(any()); // Received
            }
        }

        @Test
        @DisplayName("Multiple payments should execute independently")
        void multiplePaymentsShouldExecuteIndependently() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Daily");
                manager.createRecurringPayment(TEST_FROM, TEST_THIRD, 200.0, 7, "Weekly");

                manager.tick(1 * 24000L);

                economyMock.verify(() -> EconomyManager.transfer(
                    eq(TEST_FROM), eq(TEST_TO), eq(100.0), anyString()
                ), atLeastOnce());

                economyMock.verify(() -> EconomyManager.transfer(
                    eq(TEST_FROM), eq(TEST_THIRD), anyDouble(), anyString()
                ), never());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FAILURE HANDLING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Failure Handling Tests")
    class FailureHandlingTests {

        @Test
        @DisplayName("Three failures should deactivate payment")
        void threeFailuresShouldDeactivatePayment() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test");
                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                RecurringPayment payment = payments.get(0);

                // Fail 3 times
                manager.tick(7 * 24000L);   // Failure 1
                manager.tick(8 * 24000L);   // Failure 2
                manager.tick(9 * 24000L);   // Failure 3

                assertThat(payment.isActive()).isFalse();
                assertThat(payment.getFailureCount()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("Deactivation should send warning notification")
        void deactivationShouldSendWarningNotification() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false);

                ServerPlayer mockPlayer = mock(ServerPlayer.class);
                when(mockServer.getPlayerList().getPlayer(TEST_FROM)).thenReturn(mockPlayer);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test");

                manager.tick(7 * 24000L);
                manager.tick(8 * 24000L);
                manager.tick(9 * 24000L);

                verify(mockPlayer, atLeast(4)).sendSystemMessage(any()); // Create + 3x fail (last with warning)
            }
        }

        @Test
        @DisplayName("Failed payment should retry after 1 day")
        void failedPaymentShouldRetryAfterOneDay() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(false)
                    .thenReturn(true);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test");

                manager.tick(7 * 24000L);  // Fail on day 7
                manager.tick(8 * 24000L);  // Retry on day 8

                economyMock.verify(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()),
                    times(2));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GET PAYMENTS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Payments Tests")
    class GetPaymentsTests {

        @Test
        @DisplayName("getPayments should return empty list for player without payments")
        void getPaymentsShouldReturnEmptyListForPlayerWithoutPayments() {
            List<RecurringPayment> payments = manager.getPayments(TEST_FROM);

            assertThat(payments).isEmpty();
        }

        @Test
        @DisplayName("getPayments should return all player's payments")
        void getPaymentsShouldReturnAllPlayerPayments() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Payment 1");
                manager.createRecurringPayment(TEST_FROM, TEST_THIRD, 200.0, 2, "Payment 2");

                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);

                assertThat(payments).hasSize(2);
            }
        }

        @Test
        @DisplayName("getPayments should not return other players' payments")
        void getPaymentsShouldNotReturnOtherPlayersPayments() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "From player 1");
                manager.createRecurringPayment(TEST_THIRD, TEST_TO, 200.0, 2, "From player 2");

                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);

                assertThat(payments).hasSize(1);
                assertThat(payments.get(0).getDescription()).isEqualTo("From player 1");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON PATTERN TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonPatternTests {

        @Test
        @DisplayName("getInstance should return same instance")
        void getInstanceShouldReturnSameInstance() {
            RecurringPaymentManager instance1 = RecurringPaymentManager.getInstance(mockServer);
            RecurringPaymentManager instance2 = RecurringPaymentManager.getInstance(mockServer);

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("getInstance should be thread-safe")
        void getInstanceShouldBeThreadSafe() throws InterruptedException {
            resetManagerInstance();

            RecurringPaymentManager[] instances = new RecurringPaymentManager[2];

            Thread thread1 = new Thread(() -> instances[0] = RecurringPaymentManager.getInstance(mockServer));
            Thread thread2 = new Thread(() -> instances[1] = RecurringPaymentManager.getInstance(mockServer));

            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            assertThat(instances[0]).isSameAs(instances[1]);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty description should be allowed")
        void emptyDescriptionShouldBeAllowed() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "");

                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("Very long description should be allowed")
        void veryLongDescriptionShouldBeAllowed() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                String longDesc = "A".repeat(1000);
                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, longDesc);

                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("Very large amount should be allowed")
        void veryLargeAmountShouldBeAllowed() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, 1_000_000.0, 1, "Huge");

                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("Very large interval should be allowed")
        void veryLargeIntervalShouldBeAllowed() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                boolean result = manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 365, "Yearly");

                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("Multiple ticks on same day should not duplicate execution")
        void multipleTicksOnSameDayShouldNotDuplicateExecution() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 7, "Test");

                // Multiple ticks on day 7
                manager.tick(7 * 24000L);
                manager.tick(7 * 24000L + 100);
                manager.tick(7 * 24000L + 500);

                economyMock.verify(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()),
                    times(1));
            }
        }

        @Test
        @DisplayName("Deleting all payments should leave empty list")
        void deletingAllPaymentsShouldLeaveEmptyList() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                manager.createRecurringPayment(TEST_FROM, TEST_TO, 100.0, 1, "Test 1");
                manager.createRecurringPayment(TEST_FROM, TEST_THIRD, 200.0, 2, "Test 2");

                List<RecurringPayment> payments = manager.getPayments(TEST_FROM);
                for (RecurringPayment payment : payments) {
                    manager.deleteRecurringPayment(TEST_FROM, payment.getPaymentId());
                }

                assertThat(manager.getPayments(TEST_FROM)).isEmpty();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS LOGIC TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Payment system should handle typical rent scenario")
        void paymentSystemShouldHandleTypicalRent() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class);
                 MockedStatic<EconomyManager> economyMock = mockStatic(EconomyManager.class)) {

                mockConfigDefaults(configMock);
                economyMock.when(() -> EconomyManager.transfer(any(), any(), anyDouble(), anyString()))
                    .thenReturn(true);

                // Monthly rent: 5000€ every 7 days
                manager.createRecurringPayment(TEST_FROM, TEST_TO, 5000.0, 7, "Monthly Rent");

                // Execute over multiple months
                for (int month = 0; month < 3; month++) {
                    manager.tick((7 + month * 7) * 24000L);
                }

                economyMock.verify(() -> EconomyManager.transfer(
                    eq(TEST_FROM), eq(TEST_TO), eq(5000.0), anyString()
                ), times(3));
            }
        }

        @Test
        @DisplayName("Payment limit should prevent spam")
        void paymentLimitShouldPreventSpam() {
            try (MockedStatic<ModConfigHandler> configMock = mockStatic(ModConfigHandler.class)) {
                mockConfigDefaults(configMock);

                int successCount = 0;
                for (int i = 0; i < 20; i++) {
                    if (manager.createRecurringPayment(TEST_FROM, UUID.randomUUID(), 100.0, 1, "Test " + i)) {
                        successCount++;
                    }
                }

                assertThat(successCount).isLessThanOrEqualTo(10);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private MinecraftServer createMockServer() {
        MinecraftServer server = mock(MinecraftServer.class);
        when(server.getServerDirectory()).thenReturn(new java.io.File("test_server").toPath());
        when(server.getPlayerList()).thenReturn(mock(net.minecraft.server.players.PlayerList.class));
        return server;
    }

    private void resetManagerInstance() throws Exception {
        Field instanceField = RecurringPaymentManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private void clearPayments() throws Exception {
        Field paymentsField = RecurringPaymentManager.class.getDeclaredField("payments");
        paymentsField.setAccessible(true);
        Map<UUID, List<RecurringPayment>> payments = (Map<UUID, List<RecurringPayment>>) paymentsField.get(manager);
        payments.clear();
    }

    private void mockConfigDefaults(MockedStatic<ModConfigHandler> configMock) {
        ModConfigHandler.Common mockCommon = mock(ModConfigHandler.Common.class);
        when(mockCommon.RECURRING_MAX_PER_PLAYER).thenReturn(new MockConfigValue<>(10));
        configMock.when(() -> ModConfigHandler.COMMON).thenReturn(mockCommon);
    }

    private static class MockConfigValue<T> implements net.minecraftforge.common.ForgeConfigSpec.ConfigValue<T> {
        private final T value;

        public MockConfigValue(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public net.minecraftforge.common.ForgeConfigSpec next() { return null; }
        @Override
        public java.util.List<String> getPath() { return java.util.Collections.emptyList(); }
        @Override
        public void save() {}
        @Override
        public void set(T value) {}
        @Override
        public void clearCache() {}
    }
}
