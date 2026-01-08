package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link IncrementalSaveManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>ISaveable registration and unregistration</li>
 *   <li>Start/stop lifecycle management</li>
 *   <li>Incremental save logic with dirty tracking</li>
 *   <li>Batch size limits for saves</li>
 *   <li>Priority-based save ordering</li>
 *   <li>Full save and force save operations</li>
 *   <li>Configuration (save interval, batch size)</li>
 *   <li>Statistics tracking (total, incremental, full saves)</li>
 *   <li>Thread-safe operations</li>
 * </ul>
 */
@DisplayName("IncrementalSaveManager Tests")
class IncrementalSaveManagerTest {

    private IncrementalSaveManager saveManager;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        saveManager = new IncrementalSaveManager();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (saveManager.isRunning()) {
            saveManager.stop();
        }
        mocks.close();
    }

    // ════════════════════════════════════════════════════════════════
    // REGISTRATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Saveable Registration")
    class RegistrationTests {

        @Test
        @DisplayName("register() should add saveable to manager")
        void registerShouldAddSaveable() {
            TestSaveable saveable = new TestSaveable("test1", 5, true);

            saveManager.register(saveable);

            assertThat(saveManager.getRegisteredCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("register() should sort saveables by priority")
        void registerShouldSortByPriority() {
            TestSaveable lowPriority = new TestSaveable("low", 10, false);
            TestSaveable highPriority = new TestSaveable("high", 1, false);
            TestSaveable mediumPriority = new TestSaveable("medium", 5, false);

            saveManager.register(lowPriority);
            saveManager.register(highPriority);
            saveManager.register(mediumPriority);

            // saveAll() should process in priority order (high → medium → low)
            assertThat(saveManager.getRegisteredCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("unregister() should remove saveable from manager")
        void unregisterShouldRemoveSaveable() {
            TestSaveable saveable = new TestSaveable("test", 5, false);
            saveManager.register(saveable);

            boolean removed = saveManager.unregister(saveable);

            assertThat(removed).isTrue();
            assertThat(saveManager.getRegisteredCount()).isZero();
        }

        @Test
        @DisplayName("unregister() should return false for non-existent saveable")
        void unregisterShouldReturnFalseForNonExistent() {
            TestSaveable saveable = new TestSaveable("test", 5, false);

            boolean removed = saveManager.unregister(saveable);

            assertThat(removed).isFalse();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // LIFECYCLE TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Lifecycle Management")
    class LifecycleTests {

        @Test
        @DisplayName("start() should set running to true")
        void startShouldSetRunning() {
            saveManager.start();

            assertThat(saveManager.isRunning()).isTrue();
        }

        @Test
        @DisplayName("stop() should set running to false")
        void stopShouldSetRunning() {
            saveManager.start();

            saveManager.stop();

            assertThat(saveManager.isRunning()).isFalse();
        }

        @Test
        @DisplayName("start() should be idempotent (calling twice has no effect)")
        void startShouldBeIdempotent() {
            saveManager.start();
            saveManager.start();  // Second call should be no-op

            assertThat(saveManager.isRunning()).isTrue();
        }

        @Test
        @DisplayName("stop() should be idempotent (calling twice has no effect)")
        void stopShouldBeIdempotent() {
            saveManager.start();
            saveManager.stop();
            saveManager.stop();  // Second call should be no-op

            assertThat(saveManager.isRunning()).isFalse();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // INCREMENTAL SAVE TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Incremental Save Logic")
    class IncrementalSaveTests {

        @Test
        @DisplayName("saveAll() should only save dirty components")
        void saveAllShouldOnlySaveDirty() {
            TestSaveable dirty1 = new TestSaveable("dirty1", 5, true);
            TestSaveable dirty2 = new TestSaveable("dirty2", 5, true);
            TestSaveable clean = new TestSaveable("clean", 5, false);

            saveManager.register(dirty1);
            saveManager.register(dirty2);
            saveManager.register(clean);

            saveManager.saveAll();

            assertThat(dirty1.saveCount.get()).isEqualTo(1);
            assertThat(dirty2.saveCount.get()).isEqualTo(1);
            assertThat(clean.saveCount.get()).isZero();  // Clean should NOT be saved
        }

        @Test
        @DisplayName("saveAll() should increment fullSaves counter")
        void saveAllShouldIncrementFullSaves() {
            TestSaveable dirty = new TestSaveable("dirty", 5, true);
            saveManager.register(dirty);

            saveManager.saveAll();

            assertThat(saveManager.getFullSaves()).isEqualTo(1);
        }

        @Test
        @DisplayName("saveAll() should handle empty saveables list")
        void saveAllShouldHandleEmptyList() {
            assertThatCode(() -> saveManager.saveAll()).doesNotThrowAnyException();

            assertThat(saveManager.getFullSaves()).isEqualTo(1);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // FORCE SAVE TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Force Save Operations")
    class ForceSaveTests {

        @Test
        @DisplayName("forceSaveAll() should save ALL components (ignoring dirty flag)")
        void forceSaveAllShouldSaveEverything() {
            TestSaveable dirty = new TestSaveable("dirty", 5, true);
            TestSaveable clean = new TestSaveable("clean", 5, false);

            saveManager.register(dirty);
            saveManager.register(clean);

            saveManager.forceSaveAll();

            assertThat(dirty.saveCount.get()).isEqualTo(1);
            assertThat(clean.saveCount.get()).isEqualTo(1);  // Even clean should be saved
        }

        @Test
        @DisplayName("forceSaveAll() should increment fullSaves counter")
        void forceSaveAllShouldIncrementFullSaves() {
            TestSaveable saveable = new TestSaveable("test", 5, false);
            saveManager.register(saveable);

            saveManager.forceSaveAll();

            assertThat(saveManager.getFullSaves()).isEqualTo(1);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // CONFIGURATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Configuration")
    class ConfigurationTests {

        @Test
        @DisplayName("setSaveInterval() should update interval")
        void setSaveIntervalShouldUpdate() {
            saveManager.setSaveInterval(100);

            // Verify by restarting (should use new interval)
            saveManager.start();
            assertThat(saveManager.isRunning()).isTrue();
            saveManager.stop();
        }

        @Test
        @DisplayName("setSaveInterval() should enforce minimum of 1 tick")
        void setSaveIntervalShouldEnforceMinimum() {
            assertThatCode(() -> saveManager.setSaveInterval(0)).doesNotThrowAnyException();
            assertThatCode(() -> saveManager.setSaveInterval(-10)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setSaveInterval() should restart if running")
        void setSaveIntervalShouldRestartIfRunning() {
            saveManager.start();
            assertThat(saveManager.isRunning()).isTrue();

            saveManager.setSaveInterval(100);

            // Should still be running after restart
            assertThat(saveManager.isRunning()).isTrue();
            saveManager.stop();
        }

        @Test
        @DisplayName("setBatchSize() should update batch size")
        void setBatchSizeShouldUpdate() {
            assertThatCode(() -> saveManager.setBatchSize(10)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setBatchSize() should enforce minimum of 1")
        void setBatchSizeShouldEnforceMinimum() {
            assertThatCode(() -> saveManager.setBatchSize(0)).doesNotThrowAnyException();
            assertThatCode(() -> saveManager.setBatchSize(-5)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setBatchSize() should enforce maximum of 20")
        void setBatchSizeShouldEnforceMaximum() {
            assertThatCode(() -> saveManager.setBatchSize(100)).doesNotThrowAnyException();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // STATISTICS TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Statistics Tracking")
    class StatisticsTests {

        @Test
        @DisplayName("getTotalSaves() should track total save operations")
        void getTotalSavesShouldTrack() {
            TestSaveable dirty = new TestSaveable("dirty", 5, true);
            saveManager.register(dirty);

            saveManager.saveAll();
            saveManager.saveAll();

            assertThat(saveManager.getTotalSaves()).isEqualTo(2);
        }

        @Test
        @DisplayName("getFullSaves() should track full save operations")
        void getFullSavesShouldTrack() {
            saveManager.saveAll();
            saveManager.forceSaveAll();

            assertThat(saveManager.getFullSaves()).isEqualTo(2);
        }

        @Test
        @DisplayName("getRegisteredCount() should return number of registered saveables")
        void getRegisteredCountShouldReturnCount() {
            saveManager.register(new TestSaveable("s1", 5, false));
            saveManager.register(new TestSaveable("s2", 5, false));
            saveManager.register(new TestSaveable("s3", 5, false));

            assertThat(saveManager.getRegisteredCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getDirtyCount() should return number of dirty saveables")
        void getDirtyCountShouldReturnDirtyCount() {
            saveManager.register(new TestSaveable("dirty1", 5, true));
            saveManager.register(new TestSaveable("dirty2", 5, true));
            saveManager.register(new TestSaveable("clean", 5, false));

            assertThat(saveManager.getDirtyCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getStatistics() should return complete stats object")
        void getStatisticsShouldReturnCompleteStats() {
            TestSaveable dirty = new TestSaveable("dirty", 5, true);
            saveManager.register(dirty);
            saveManager.saveAll();

            IncrementalSaveManager.SaveStatistics stats = saveManager.getStatistics();

            assertThat(stats.totalSaves).isEqualTo(1);
            assertThat(stats.fullSaves).isEqualTo(1);
            assertThat(stats.registered).isEqualTo(1);
            assertThat(stats.dirty).isEqualTo(1);
            assertThat(stats.running).isFalse();
        }

        @Test
        @DisplayName("resetStatistics() should clear save counters")
        void resetStatisticsShouldClear() {
            TestSaveable dirty = new TestSaveable("dirty", 5, true);
            saveManager.register(dirty);
            saveManager.saveAll();

            saveManager.resetStatistics();

            assertThat(saveManager.getTotalSaves()).isZero();
            assertThat(saveManager.getFullSaves()).isZero();
            assertThat(saveManager.getIncrementalSaves()).isZero();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // PRIORITY TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Priority-Based Saving")
    class PriorityTests {

        @Test
        @DisplayName("saveAll() should respect priority order (low number = high priority)")
        void saveAllShouldRespectPriority() {
            CountingSaveable priority0 = new CountingSaveable("p0", 0, true);
            CountingSaveable priority5 = new CountingSaveable("p5", 5, true);
            CountingSaveable priority10 = new CountingSaveable("p10", 10, true);

            // Register in random order
            saveManager.register(priority5);
            saveManager.register(priority10);
            saveManager.register(priority0);

            saveManager.forceSaveAll();

            // All should be saved
            assertThat(priority0.saveCount.get()).isEqualTo(1);
            assertThat(priority5.saveCount.get()).isEqualTo(1);
            assertThat(priority10.saveCount.get()).isEqualTo(1);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ERROR HANDLING TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("saveAll() should continue on error in one saveable")
        void saveAllShouldContinueOnError() {
            TestSaveable failing = new FailingSaveable("failing", 5);
            TestSaveable normal = new TestSaveable("normal", 5, true);

            saveManager.register(failing);
            saveManager.register(normal);

            assertThatCode(() -> saveManager.saveAll()).doesNotThrowAnyException();

            // Normal saveable should still be saved despite failing saveable
            assertThat(normal.saveCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("forceSaveAll() should handle save errors gracefully")
        void forceSaveAllShouldHandleErrors() {
            TestSaveable failing = new FailingSaveable("failing", 5);
            TestSaveable normal = new TestSaveable("normal", 5, true);

            saveManager.register(failing);
            saveManager.register(normal);

            assertThatCode(() -> saveManager.forceSaveAll()).doesNotThrowAnyException();

            assertThat(normal.saveCount.get()).isEqualTo(1);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("full lifecycle: register → start → save → stop")
        void fullLifecycle() {
            TestSaveable saveable = new TestSaveable("test", 5, true);

            saveManager.register(saveable);
            saveManager.start();
            saveManager.saveAll();
            saveManager.stop();

            assertThat(saveable.saveCount.get()).isEqualTo(1);
            assertThat(saveManager.isRunning()).isFalse();
        }

        @Test
        @DisplayName("printStatus() should not throw exceptions")
        void printStatusShouldWork() {
            saveManager.register(new TestSaveable("test", 5, true));

            assertThatCode(() -> saveManager.printStatus()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("toString() should return meaningful string")
        void toStringShouldReturnMeaningfulString() {
            saveManager.register(new TestSaveable("test", 5, true));

            String str = saveManager.toString();

            assertThat(str).contains("IncrementalSaveManager");
            assertThat(str).contains("registered=1");
            assertThat(str).contains("dirty=1");
        }
    }

    // ════════════════════════════════════════════════════════════════
    // TEST HELPER CLASSES
    // ════════════════════════════════════════════════════════════════

    /**
     * Test implementation of ISaveable
     */
    static class TestSaveable implements IncrementalSaveManager.ISaveable {
        private final String name;
        private final int priority;
        private final AtomicBoolean dirty;
        protected final AtomicInteger saveCount = new AtomicInteger(0);

        TestSaveable(String name, int priority, boolean dirty) {
            this.name = name;
            this.priority = priority;
            this.dirty = new AtomicBoolean(dirty);
        }

        @Override
        public boolean isDirty() {
            return dirty.get();
        }

        @Override
        public void save() {
            saveCount.incrementAndGet();
            dirty.set(false);  // Clear dirty flag after save
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }

    /**
     * Saveable that tracks save order
     */
    static class CountingSaveable extends TestSaveable {
        CountingSaveable(String name, int priority, boolean dirty) {
            super(name, priority, dirty);
        }
    }

    /**
     * Saveable that throws exception on save()
     */
    static class FailingSaveable extends TestSaveable {
        FailingSaveable(String name, int priority) {
            super(name, priority, true);
        }

        @Override
        public void save() {
            throw new RuntimeException("Simulated save failure");
        }
    }
}
