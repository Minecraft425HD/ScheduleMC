package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for RateLimiter
 *
 * Tests rate limiting scenarios:
 * - Basic Rate Limiting
 * - Concurrent Access
 * - Window Sliding
 * - Multiple Clients
 * - Reset Behavior
 *
 * @since 1.0
 */
@DisplayName("RateLimiter Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RateLimiterIntegrationTest {

    private RateLimiter rateLimiter;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        // Create rate limiter: 5 operations per 1000ms
        rateLimiter = new RateLimiter("test_limiter", 5, 1000L);
        clientId = UUID.randomUUID();
    }

    @Test
    @Order(1)
    @DisplayName("Basic Rate Limiting - Should allow operations within limit")
    void testAllowWithinLimit() {
        // Act & Assert - First 5 operations should be allowed
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimiter.allowOperation(clientId)).isTrue();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Basic Rate Limiting - Should reject operations exceeding limit")
    void testRejectExceedingLimit() {
        // Arrange - Use up the limit
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowOperation(clientId);
        }

        // Act & Assert - 6th operation should be rejected
        assertThat(rateLimiter.allowOperation(clientId)).isFalse();
    }

    @Test
    @Order(3)
    @DisplayName("Window Sliding - Should reset after time window")
    void testResetAfterTimeWindow() throws InterruptedException {
        // Arrange - Use up the limit
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowOperation(clientId);
        }
        assertThat(rateLimiter.allowOperation(clientId)).isFalse();

        // Act - Wait for window to expire
        Thread.sleep(1100); // Wait slightly longer than 1000ms window

        // Assert - Should allow operations again
        assertThat(rateLimiter.allowOperation(clientId)).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("Multiple Clients - Should track clients independently")
    void testMultipleClients() {
        // Arrange
        UUID client1 = UUID.randomUUID();
        UUID client2 = UUID.randomUUID();

        // Act - Client 1 uses up limit
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowOperation(client1);
        }

        // Assert - Client 2 should still have full quota
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimiter.allowOperation(client2)).isTrue();
        }

        // Client 1 should be blocked
        assertThat(rateLimiter.allowOperation(client1)).isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("Concurrent Access - Should handle concurrent requests safely")
    void testConcurrentAccess() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act - Multiple threads trying to perform operations
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                if (rateLimiter.allowOperation(clientId)) {
                    successCount.incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - Only 5 operations should succeed (the limit)
        assertThat(successCount.get()).isEqualTo(5);
    }

    @Test
    @Order(6)
    @DisplayName("Partial Window - Should allow partial quota after partial wait")
    void testPartialWindowReset() throws InterruptedException {
        // Arrange - Use 3 out of 5 operations
        for (int i = 0; i < 3; i++) {
            rateLimiter.allowOperation(clientId);
        }

        // Act - Wait for window to expire
        Thread.sleep(1100);

        // Assert - Should have full 5 operations again
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimiter.allowOperation(clientId)).isTrue();
        }
    }

    @Test
    @Order(7)
    @DisplayName("Rapid Fire - Should handle rapid successive calls")
    void testRapidFire() {
        // Act - Try 10 operations as fast as possible
        int successCount = 0;
        for (int i = 0; i < 10; i++) {
            if (rateLimiter.allowOperation(clientId)) {
                successCount++;
            }
        }

        // Assert - Only 5 should succeed
        assertThat(successCount).isEqualTo(5);
    }

    @Test
    @Order(8)
    @DisplayName("Zero Limit - Should reject all operations with zero limit")
    void testZeroLimit() {
        // Arrange
        RateLimiter zeroLimiter = new RateLimiter("zero_limiter", 0, 1000L);

        // Act & Assert
        assertThat(zeroLimiter.allowOperation(clientId)).isFalse();
    }

    @Test
    @Order(9)
    @DisplayName("High Limit - Should allow many operations with high limit")
    void testHighLimit() {
        // Arrange
        RateLimiter highLimiter = new RateLimiter("high_limiter", 1000, 1000L);

        // Act - Try 500 operations
        int successCount = 0;
        for (int i = 0; i < 500; i++) {
            if (highLimiter.allowOperation(clientId)) {
                successCount++;
            }
        }

        // Assert - All 500 should succeed
        assertThat(successCount).isEqualTo(500);
    }

    @Test
    @Order(10)
    @DisplayName("Short Window - Should reset quickly with short window")
    void testShortWindow() throws InterruptedException {
        // Arrange - Rate limiter with 100ms window
        RateLimiter shortLimiter = new RateLimiter("short_limiter", 2, 100L);

        // Use up limit
        shortLimiter.allowOperation(clientId);
        shortLimiter.allowOperation(clientId);
        assertThat(shortLimiter.allowOperation(clientId)).isFalse();

        // Wait for short window
        Thread.sleep(150);

        // Assert - Should allow operations again
        assertThat(shortLimiter.allowOperation(clientId)).isTrue();
    }
}
