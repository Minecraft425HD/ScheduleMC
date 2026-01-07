package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive JUnit 5 tests for ThreadPoolManager
 *
 * Tests cover:
 * - Pool creation and configuration
 * - Task execution across all pools
 * - Retry mechanism with exponential backoff
 * - Shutdown behavior
 * - Error handling
 * - Statistics and monitoring
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 */
@DisplayName("ThreadPoolManager Tests")
class ThreadPoolManagerTest {

    private static final int EXPECTED_IO_POOL_SIZE = 4;
    private static final int EXPECTED_RENDER_POOL_SIZE = 2;
    private static final int EXPECTED_SCHEDULED_POOL_SIZE = 2;
    private static final int EXPECTED_COMPUTATION_POOL_SIZE = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);

    private List<Future<?>> submittedTasks;

    @BeforeEach
    void setUp() {
        submittedTasks = new ArrayList<>();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Cancel and clean up all submitted tasks
        for (Future<?> task : submittedTasks) {
            if (!task.isDone()) {
                task.cancel(true);
            }
        }
        submittedTasks.clear();

        // Give tasks time to finish
        Thread.sleep(100);
    }

    // ═══════════════════════════════════════════════════════════
    // POOL CREATION & CONFIGURATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("IO Pool should have correct size (4 threads)")
    void testIOPoolSize() {
        // Given
        var ioPool = ThreadPoolManager.getIOPool();

        // When
        var poolSize = ((ThreadPoolExecutor) ioPool).getCorePoolSize();

        // Then
        assertThat(poolSize)
            .as("IO Pool should have %d threads", EXPECTED_IO_POOL_SIZE)
            .isEqualTo(EXPECTED_IO_POOL_SIZE);
    }

    @Test
    @DisplayName("Render Pool should have correct size (2 threads)")
    void testRenderPoolSize() {
        // Given
        var renderPool = ThreadPoolManager.getRenderPool();

        // When
        var poolSize = ((ThreadPoolExecutor) renderPool).getCorePoolSize();

        // Then
        assertThat(poolSize)
            .as("Render Pool should have %d threads", EXPECTED_RENDER_POOL_SIZE)
            .isEqualTo(EXPECTED_RENDER_POOL_SIZE);
    }

    @Test
    @DisplayName("Computation Pool should have correct size (CPU cores / 2, min 2)")
    void testComputationPoolSize() {
        // Given
        var computationPool = ThreadPoolManager.getComputationPool();

        // When
        var poolSize = ((ThreadPoolExecutor) computationPool).getCorePoolSize();

        // Then
        assertThat(poolSize)
            .as("Computation Pool should have %d threads", EXPECTED_COMPUTATION_POOL_SIZE)
            .isEqualTo(EXPECTED_COMPUTATION_POOL_SIZE);
    }

    @Test
    @DisplayName("Async Pool should use correct configuration (0-20 threads, 60s keep-alive)")
    void testAsyncPoolConfiguration() {
        // Given
        var asyncPool = (ThreadPoolExecutor) ThreadPoolManager.getAsyncPool();

        // When
        var corePoolSize = asyncPool.getCorePoolSize();
        var maxPoolSize = asyncPool.getMaximumPoolSize();
        var keepAliveTime = asyncPool.getKeepAliveTime(TimeUnit.SECONDS);

        // Then
        assertThat(corePoolSize)
            .as("Async Pool core size should be 0 (cached pool)")
            .isEqualTo(0);
        assertThat(maxPoolSize)
            .as("Async Pool max size should be 20")
            .isEqualTo(20);
        assertThat(keepAliveTime)
            .as("Async Pool keep-alive should be 60 seconds")
            .isEqualTo(60L);
    }

    @Test
    @DisplayName("Scheduled Pool should have correct size (2 threads)")
    void testScheduledPoolSize() {
        // Given
        var scheduledPool = ThreadPoolManager.getScheduledPool();

        // When
        var poolSize = ((ThreadPoolExecutor) scheduledPool).getCorePoolSize();

        // Then
        assertThat(poolSize)
            .as("Scheduled Pool should have %d threads", EXPECTED_SCHEDULED_POOL_SIZE)
            .isEqualTo(EXPECTED_SCHEDULED_POOL_SIZE);
    }

    @Test
    @DisplayName("Thread names should have correct format")
    @Timeout(2)
    void testThreadNameFormat() throws Exception {
        // Given
        AtomicBoolean correctNameFound = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        // When - Submit task to IO pool and capture thread name
        var future = ThreadPoolManager.submitIO(() -> {
            String threadName = Thread.currentThread().getName();
            if (threadName.matches("ScheduleMC-IO-\\d+")) {
                correctNameFound.set(true);
            }
            latch.countDown();
        });
        submittedTasks.add(future);

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS))
            .as("Task should complete within timeout")
            .isTrue();
        assertThat(correctNameFound.get())
            .as("Thread name should match pattern 'ScheduleMC-IO-<number>'")
            .isTrue();
    }

    // ═══════════════════════════════════════════════════════════
    // TASK EXECUTION TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("IO Pool should execute tasks successfully")
    @Timeout(2)
    void testIOPoolExecution() throws Exception {
        // Given
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        // When
        var future = ThreadPoolManager.submitIO(() -> {
            executed.set(true);
            latch.countDown();
        });
        submittedTasks.add(future);

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS))
            .as("IO task should complete within timeout")
            .isTrue();
        assertThat(executed.get())
            .as("IO task should be executed")
            .isTrue();
        assertThat(future.isDone())
            .as("Future should be done")
            .isTrue();
    }

    @Test
    @DisplayName("Render Pool should execute tasks successfully")
    @Timeout(2)
    void testRenderPoolExecution() throws Exception {
        // Given
        AtomicInteger executionCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        // When
        var future = ThreadPoolManager.submitRender(() -> {
            executionCount.incrementAndGet();
            latch.countDown();
        });
        submittedTasks.add(future);

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS))
            .as("Render task should complete within timeout")
            .isTrue();
        assertThat(executionCount.get())
            .as("Render task should be executed exactly once")
            .isEqualTo(1);
    }

    @Test
    @DisplayName("Computation Pool should execute tasks successfully")
    @Timeout(2)
    void testComputationPoolExecution() throws Exception {
        // Given
        AtomicInteger result = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        // When
        var future = ThreadPoolManager.submitComputation(() -> {
            result.set(42);
            latch.countDown();
        });
        submittedTasks.add(future);

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS))
            .as("Computation task should complete within timeout")
            .isTrue();
        assertThat(result.get())
            .as("Computation result should be correct")
            .isEqualTo(42);
    }

    @Test
    @DisplayName("Async Pool should execute tasks successfully")
    @Timeout(2)
    void testAsyncPoolExecution() throws Exception {
        // Given
        CountDownLatch latch = new CountDownLatch(3);

        // When - Submit multiple async tasks
        for (int i = 0; i < 3; i++) {
            var future = ThreadPoolManager.submitAsync(latch::countDown);
            submittedTasks.add(future);
        }

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS))
            .as("All async tasks should complete within timeout")
            .isTrue();
    }

    @Test
    @DisplayName("Scheduled Pool should execute delayed tasks")
    @Timeout(2)
    void testScheduledDelayedExecution() throws Exception {
        // Given
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        long startTime = System.currentTimeMillis();

        // When - Schedule task with 500ms delay
        ScheduledFuture<?> future = ThreadPoolManager.schedule(() -> {
            executed.set(true);
            latch.countDown();
        }, 500, TimeUnit.MILLISECONDS);
        submittedTasks.add(future);

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS))
            .as("Scheduled task should complete within timeout")
            .isTrue();
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertThat(elapsedTime)
            .as("Task should be delayed by at least 500ms")
            .isGreaterThanOrEqualTo(500);
        assertThat(executed.get())
            .as("Scheduled task should be executed")
            .isTrue();
    }

    @Test
    @DisplayName("Scheduled Pool should execute periodic tasks at fixed rate")
    @Timeout(3)
    void testScheduledFixedRateExecution() throws Exception {
        // Given
        AtomicInteger executionCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        // When - Schedule task to run every 200ms
        ScheduledFuture<?> future = ThreadPoolManager.scheduleAtFixedRate(() -> {
            executionCount.incrementAndGet();
            latch.countDown();
        }, 0, 200, TimeUnit.MILLISECONDS);
        submittedTasks.add(future);

        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS))
            .as("Task should execute at least 3 times")
            .isTrue();

        // Cancel periodic task
        future.cancel(true);

        assertThat(executionCount.get())
            .as("Task should have executed at least 3 times")
            .isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Pools should support Callable with return value")
    @Timeout(2)
    void testCallableExecution() throws Exception {
        // Given
        String expectedResult = "Hello from ThreadPool";

        // When
        Future<String> future = ThreadPoolManager.getIOPool().submit(() -> {
            Thread.sleep(100);
            return expectedResult;
        });
        submittedTasks.add(future);

        // Then
        String result = future.get(1, TimeUnit.SECONDS);
        assertThat(result)
            .as("Callable should return correct value")
            .isEqualTo(expectedResult);
    }

    // ═══════════════════════════════════════════════════════════
    // RETRY MECHANISM TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Retry mechanism should succeed on first attempt")
    @Timeout(3)
    void testRetrySuccessOnFirstAttempt() throws Exception {
        // Given
        AtomicInteger executionCount = new AtomicInteger(0);
        Runnable task = executionCount::incrementAndGet;

        // When
        CompletableFuture<Void> future = ThreadPoolManager.submitIOWithRetry(task, "test-task");
        future.get(2, TimeUnit.SECONDS);

        // Give it time to complete
        Thread.sleep(100);

        // Then
        assertThat(executionCount.get())
            .as("Task should execute exactly once on success")
            .isEqualTo(1);
    }

    @Test
    @DisplayName("Retry mechanism should retry exactly 3 times on failure")
    @Timeout(5)
    void testRetryMechanismAttempts() throws Exception {
        // Given
        AtomicInteger executionCount = new AtomicInteger(0);
        Runnable failingTask = () -> {
            executionCount.incrementAndGet();
            throw new RuntimeException("Simulated failure");
        };

        long startTime = System.currentTimeMillis();

        // When
        CompletableFuture<Void> future = ThreadPoolManager.submitIOWithRetry(failingTask, "failing-task");

        // Wait for all retries to complete (initial + 3 retries = 4 total)
        // Total time should be: 500ms + 1000ms + 2000ms = 3500ms minimum
        Thread.sleep(4000);

        long elapsedTime = System.currentTimeMillis() - startTime;

        // Then
        assertThat(executionCount.get())
            .as("Task should execute 4 times (initial + 3 retries)")
            .isEqualTo(4);

        assertThat(elapsedTime)
            .as("Total retry time should be at least 3500ms (exponential backoff)")
            .isGreaterThanOrEqualTo(3500);
    }

    @Test
    @DisplayName("Retry mechanism should succeed on second attempt")
    @Timeout(5)
    void testRetrySuccessOnSecondAttempt() throws Exception {
        // Given
        AtomicInteger executionCount = new AtomicInteger(0);
        Runnable task = () -> {
            int count = executionCount.incrementAndGet();
            if (count == 1) {
                throw new RuntimeException("First attempt fails");
            }
            // Second attempt succeeds
        };

        // When
        CompletableFuture<Void> future = ThreadPoolManager.submitIOWithRetry(task, "retry-success-task");

        // Wait for completion (initial failure + 500ms delay + success)
        Thread.sleep(1500);

        // Then
        assertThat(executionCount.get())
            .as("Task should execute twice (fail, then succeed)")
            .isEqualTo(2);
    }

    @Test
    @DisplayName("Retry mechanism should use exponential backoff")
    @Timeout(6)
    void testRetryExponentialBackoff() throws Exception {
        // Given
        List<Long> executionTimes = new ArrayList<>();
        Runnable failingTask = () -> {
            executionTimes.add(System.currentTimeMillis());
            throw new RuntimeException("Always fails");
        };

        // When
        CompletableFuture<Void> future = ThreadPoolManager.submitIOWithRetry(failingTask, "backoff-test");

        // Wait for all retries
        Thread.sleep(5000);

        // Then
        assertThat(executionTimes)
            .as("Should have 4 execution attempts")
            .hasSize(4);

        // Check delays between attempts
        if (executionTimes.size() >= 2) {
            long delay1 = executionTimes.get(1) - executionTimes.get(0);
            assertThat(delay1)
                .as("First retry delay should be ~500ms")
                .isBetween(450L, 650L);
        }

        if (executionTimes.size() >= 3) {
            long delay2 = executionTimes.get(2) - executionTimes.get(1);
            assertThat(delay2)
                .as("Second retry delay should be ~1000ms")
                .isBetween(950L, 1150L);
        }

        if (executionTimes.size() >= 4) {
            long delay3 = executionTimes.get(3) - executionTimes.get(2);
            assertThat(delay3)
                .as("Third retry delay should be ~2000ms")
                .isBetween(1950L, 2150L);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ERROR HANDLING TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Exceptions in tasks should not crash the pool")
    @Timeout(2)
    void testExceptionHandling() throws Exception {
        // Given
        AtomicBoolean secondTaskExecuted = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        // When - Submit failing task
        var failingFuture = ThreadPoolManager.submitIO(() -> {
            throw new RuntimeException("Task exception");
        });
        submittedTasks.add(failingFuture);

        // Submit second task to verify pool still works
        var successFuture = ThreadPoolManager.submitIO(() -> {
            secondTaskExecuted.set(true);
            latch.countDown();
        });
        submittedTasks.add(successFuture);

        // Then
        assertThatThrownBy(() -> failingFuture.get(1, TimeUnit.SECONDS))
            .as("First task should throw exception")
            .isInstanceOf(ExecutionException.class);

        assertThat(latch.await(1, TimeUnit.SECONDS))
            .as("Second task should execute despite first task's exception")
            .isTrue();
        assertThat(secondTaskExecuted.get())
            .as("Pool should still be functional after exception")
            .isTrue();
    }

    @Test
    @DisplayName("Multiple concurrent exceptions should not crash pools")
    @Timeout(3)
    void testMultipleConcurrentExceptions() throws Exception {
        // Given
        CountDownLatch latch = new CountDownLatch(5);
        List<Future<?>> futures = new ArrayList<>();

        // When - Submit multiple failing tasks
        for (int i = 0; i < 5; i++) {
            Future<?> future = ThreadPoolManager.submitAsync(() -> {
                latch.countDown();
                throw new RuntimeException("Concurrent exception");
            });
            futures.add(future);
            submittedTasks.add(future);
        }

        // Then - All tasks should execute despite exceptions
        assertThat(latch.await(2, TimeUnit.SECONDS))
            .as("All tasks should execute even with exceptions")
            .isTrue();

        // Verify pool is still functional
        AtomicBoolean poolWorking = new AtomicBoolean(false);
        var testFuture = ThreadPoolManager.submitAsync(() -> poolWorking.set(true));
        submittedTasks.add(testFuture);
        testFuture.get(1, TimeUnit.SECONDS);

        assertThat(poolWorking.get())
            .as("Pool should still be functional after multiple exceptions")
            .isTrue();
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS & MONITORING TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Statistics should return formatted string with pool info")
    void testGetStatistics() {
        // When
        String stats = ThreadPoolManager.getStatistics();

        // Then
        assertThat(stats)
            .as("Statistics should contain header")
            .contains("ThreadPoolManager Statistics")
            .contains("IO Pool:")
            .contains("Render Pool:")
            .contains("Computation Pool:")
            .contains("Async Pool:")
            .contains("Scheduled Pool:");
    }

    @Test
    @DisplayName("Queue size methods should return valid values")
    @Timeout(2)
    void testQueueSizeMethods() throws Exception {
        // Given
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);

        // When - Fill up the IO pool with blocking tasks
        List<Future<?>> blockingTasks = new ArrayList<>();
        for (int i = 0; i < EXPECTED_IO_POOL_SIZE; i++) {
            Future<?> future = ThreadPoolManager.submitIO(() -> {
                try {
                    startLatch.await();
                    finishLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            blockingTasks.add(future);
            submittedTasks.add(future);
        }

        // Add tasks to queue
        Thread.sleep(100);
        for (int i = 0; i < 3; i++) {
            Future<?> future = ThreadPoolManager.submitIO(() -> {});
            blockingTasks.add(future);
            submittedTasks.add(future);
        }

        startLatch.countDown();
        Thread.sleep(50);

        // Then
        int queueSize = ThreadPoolManager.getIOPoolQueueSize();
        assertThat(queueSize)
            .as("Queue size should be non-negative")
            .isGreaterThanOrEqualTo(0);

        // Cleanup
        finishLatch.countDown();
        for (Future<?> task : blockingTasks) {
            task.get(1, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Active count should reflect running tasks")
    @Timeout(2)
    void testActiveCount() throws Exception {
        // Given
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);

        // When - Submit tasks to computation pool
        List<Future<?>> activeTasks = new ArrayList<>();
        int tasksToSubmit = Math.min(2, EXPECTED_COMPUTATION_POOL_SIZE);
        for (int i = 0; i < tasksToSubmit; i++) {
            Future<?> future = ThreadPoolManager.submitComputation(() -> {
                try {
                    startLatch.await();
                    finishLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            activeTasks.add(future);
            submittedTasks.add(future);
        }

        startLatch.countDown();
        Thread.sleep(50);

        // Then
        int activeCount = ThreadPoolManager.getComputationPoolActiveCount();
        assertThat(activeCount)
            .as("Active count should reflect running tasks")
            .isGreaterThanOrEqualTo(0)
            .isLessThanOrEqualTo(EXPECTED_COMPUTATION_POOL_SIZE);

        // Cleanup
        finishLatch.countDown();
        for (Future<?> task : activeTasks) {
            task.get(1, TimeUnit.SECONDS);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Constructor should throw UnsupportedOperationException")
    void testConstructorThrows() {
        // When & Then
        assertThatThrownBy(() -> {
            var constructor = ThreadPoolManager.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .as("Constructor should throw UnsupportedOperationException")
        .hasCauseInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("Utility class");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    @DisplayName("Pool should handle multiple concurrent tasks")
    @Timeout(3)
    void testConcurrentTaskExecution(int taskCount) throws Exception {
        // Given
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger completedTasks = new AtomicInteger(0);

        // When
        for (int i = 0; i < taskCount; i++) {
            var future = ThreadPoolManager.submitAsync(() -> {
                completedTasks.incrementAndGet();
                latch.countDown();
            });
            submittedTasks.add(future);
        }

        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS))
            .as("All %d tasks should complete", taskCount)
            .isTrue();
        assertThat(completedTasks.get())
            .as("Exactly %d tasks should be completed", taskCount)
            .isEqualTo(taskCount);
    }
}
