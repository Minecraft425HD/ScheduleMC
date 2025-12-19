package de.rolandsw.schedulemc.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AbstractPersistenceManager
 *
 * Tests cover:
 * - Basic load/save operations
 * - Backup rotation
 * - Corruption recovery
 * - Health monitoring
 * - Atomic writes
 * - Dirty flag tracking
 */
class AbstractPersistenceManagerTest {

    @TempDir
    Path tempDir;

    private File testFile;
    private Gson gson;
    private TestPersistenceManager manager;

    @BeforeEach
    void setUp() {
        testFile = tempDir.resolve("test_data.json").toFile();
        gson = new Gson();
        manager = new TestPersistenceManager(testFile, gson);
    }

    // ==================== Basic Load/Save Tests ====================

    @Test
    @DisplayName("Should save and load data successfully")
    void testBasicSaveAndLoad() {
        // Arrange
        manager.data.put("key1", "value1");
        manager.data.put("key2", "value2");
        manager.markDirty();

        // Act
        manager.save();
        TestPersistenceManager newManager = new TestPersistenceManager(testFile, gson);
        newManager.load();

        // Assert
        assertThat(newManager.data)
            .containsEntry("key1", "value1")
            .containsEntry("key2", "value2");
        assertThat(manager.isHealthy()).isTrue();
        assertThat(manager.getLastError()).isNull();
    }

    @Test
    @DisplayName("Should handle missing file gracefully")
    void testLoadWithMissingFile() {
        // Act
        manager.load();

        // Assert
        assertThat(manager.data).isEmpty();
        assertThat(manager.isHealthy()).isTrue();
        assertThat(manager.getLastError()).isNull();
        assertThat(manager.wasNoDataFileFoundCalled).isTrue();
    }

    @Test
    @DisplayName("Should create parent directories if needed")
    void testSaveCreatesParentDirectories() {
        // Arrange
        File nestedFile = tempDir.resolve("sub/nested/test.json").toFile();
        TestPersistenceManager nestedManager = new TestPersistenceManager(nestedFile, gson);
        nestedManager.data.put("test", "value");
        nestedManager.markDirty();

        // Act
        nestedManager.save();

        // Assert
        assertThat(nestedFile).exists();
        assertThat(nestedFile.getParentFile()).exists();
    }

    // ==================== Dirty Flag Tests ====================

    @Test
    @DisplayName("Should only save when dirty flag is set")
    void testSaveIfNeeded() throws IOException {
        // Arrange
        manager.data.put("key", "value");
        manager.markDirty();
        manager.save();

        long lastModified = testFile.lastModified();
        Thread.sleep(10); // Ensure time difference

        // Act - saveIfNeeded without changes
        manager.saveIfNeeded();

        // Assert - file should not be modified
        assertThat(testFile.lastModified()).isEqualTo(lastModified);

        // Act - modify data and saveIfNeeded
        manager.data.put("key2", "value2");
        manager.markDirty();
        Thread.sleep(10);
        manager.saveIfNeeded();

        // Assert - file should be modified
        assertThat(testFile.lastModified()).isGreaterThan(lastModified);
    }

    @Test
    @DisplayName("Should clear dirty flag after successful save")
    void testDirtyFlagClearedAfterSave() {
        // Arrange
        manager.data.put("key", "value");
        manager.markDirty();

        // Act
        manager.save();

        // Assert
        assertThat(manager.needsSave()).isFalse();
    }

    // ==================== Backup Tests ====================

    @Test
    @DisplayName("Should create backup before overwriting")
    void testBackupCreation() {
        // Arrange - Create initial file
        manager.data.put("original", "data");
        manager.markDirty();
        manager.save();

        // Act - Modify and save again
        manager.data.put("modified", "data");
        manager.markDirty();
        manager.save();

        // Assert - Backup should exist
        File backupDir = new File(testFile.getParent(), "backups");
        assertThat(backupDir).exists();

        File[] backups = backupDir.listFiles((dir, name) ->
            name.startsWith(testFile.getName()));
        assertThat(backups).isNotNull().isNotEmpty();
    }

    // ==================== Corruption Recovery Tests ====================

    @Test
    @DisplayName("Should recover from corrupted file using backup")
    void testCorruptionRecovery() throws IOException {
        // Arrange - Save valid data first
        manager.data.put("key", "value");
        manager.markDirty();
        manager.save();

        // Act - Corrupt the file
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("{ corrupted json data }{");
        }

        // Create a new manager instance to trigger load
        TestPersistenceManager recoveredManager = new TestPersistenceManager(testFile, gson);
        recoveredManager.load();

        // Assert - Should recover from backup
        assertThat(recoveredManager.data).containsEntry("key", "value");
        assertThat(recoveredManager.isHealthy()).isTrue();
        assertThat(recoveredManager.getLastError()).isEqualTo("Recovered from backup");
    }

    @Test
    @DisplayName("Should handle critical failure when no backup exists")
    void testCriticalFailureWithoutBackup() throws IOException {
        // Arrange - Create corrupted file without any backup
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("{ corrupted json }");
        }

        // Act
        manager.load();

        // Assert - Should call critical failure handler
        assertThat(manager.wasCriticalFailureCalled).isTrue();
        assertThat(manager.isHealthy()).isFalse();
        assertThat(manager.getLastError()).contains("Critical load failure");

        // Corrupt file should be preserved
        File[] corruptFiles = testFile.getParentFile().listFiles((dir, name) ->
            name.contains("CORRUPT"));
        assertThat(corruptFiles).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Should preserve corrupt file for forensics")
    void testCorruptFilePreservation() throws IOException {
        // Arrange
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("{ invalid json");
        }

        // Act
        manager.load();

        // Assert - Original corrupt file should be backed up
        File[] corruptBackups = testFile.getParentFile().listFiles((dir, name) ->
            name.contains("CORRUPT") && name.startsWith(testFile.getName()));

        assertThat(corruptBackups).isNotNull().hasSize(1);
        assertThat(Files.readString(corruptBackups[0].toPath())).contains("{ invalid json");
    }

    // ==================== Health Monitoring Tests ====================

    @Test
    @DisplayName("Should report healthy status after successful operations")
    void testHealthyStatus() {
        // Arrange
        manager.data.put("key", "value");
        manager.markDirty();

        // Act
        manager.save();
        manager.load();

        // Assert
        assertThat(manager.isHealthy()).isTrue();
        assertThat(manager.getLastError()).isNull();
        assertThat(manager.getHealthInfo()).contains("GESUND");
    }

    @Test
    @DisplayName("Should report unhealthy status after save failure")
    void testUnhealthyStatusAfterSaveFailure() {
        // Arrange - Make file read-only to cause save failure
        testFile.getParentFile().setWritable(false);
        manager.data.put("key", "value");
        manager.markDirty();

        // Act
        manager.save();

        // Cleanup
        testFile.getParentFile().setWritable(true);

        // Assert
        assertThat(manager.isHealthy()).isFalse();
        assertThat(manager.getLastError()).contains("Save failed");
        assertThat(manager.needsSave()).isTrue(); // Dirty flag should remain
        assertThat(manager.getHealthInfo()).contains("UNGESUND");
    }

    @Test
    @DisplayName("Should include backup count in health info")
    void testHealthInfoIncludesBackupCount() {
        // Arrange - Create some backups
        manager.data.put("v1", "data");
        manager.markDirty();
        manager.save();

        manager.data.put("v2", "data");
        manager.markDirty();
        manager.save();

        // Act
        String healthInfo = manager.getHealthInfo();

        // Assert
        assertThat(healthInfo).contains("Backups verfügbar");
    }

    // ==================== Atomic Write Tests ====================

    @Test
    @DisplayName("Should use temporary file for atomic writes")
    void testAtomicWrites() {
        // Arrange
        manager.data.put("key", "value");
        manager.markDirty();

        // Act
        manager.save();

        // Assert - Temp file should not exist after save
        File tempFile = new File(testFile.getParent(), testFile.getName() + ".tmp");
        assertThat(tempFile).doesNotExist();
        assertThat(testFile).exists();
    }

    @Test
    @DisplayName("Should not corrupt existing file if save fails mid-write")
    void testNoCorruptionOnSaveFailure() throws IOException {
        // Arrange - Save initial valid data
        manager.data.put("safe", "data");
        manager.markDirty();
        manager.save();

        String originalContent = Files.readString(testFile.toPath());

        // Act - Attempt to save with failure simulation
        TestPersistenceManager failingManager = new FailingSaveManager(testFile, gson);
        failingManager.data.put("fail", "data");
        failingManager.markDirty();
        failingManager.save();

        // Assert - Original file should be intact
        String currentContent = Files.readString(testFile.toPath());
        assertThat(currentContent).isEqualTo(originalContent);
    }

    // ==================== Null Safety Tests ====================

    @Test
    @DisplayName("Should handle null data gracefully")
    void testNullDataHandling() throws IOException {
        // Arrange - Write null to file
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("null");
        }

        // Act & Assert
        assertThatThrownBy(() -> manager.load())
            .doesNotThrowAnyException();

        assertThat(manager.isHealthy()).isFalse();
        assertThat(manager.wasCriticalFailureCalled).isTrue();
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle empty file")
    void testEmptyFile() throws IOException {
        // Arrange
        testFile.createNewFile(); // Create empty file

        // Act
        manager.load();

        // Assert
        assertThat(manager.isHealthy()).isFalse();
        assertThat(manager.wasCriticalFailureCalled).isTrue();
    }

    @Test
    @DisplayName("Should handle very large data")
    void testLargeData() {
        // Arrange - Create large dataset
        for (int i = 0; i < 10000; i++) {
            manager.data.put("key" + i, "value" + i + "_".repeat(100));
        }
        manager.markDirty();

        // Act
        manager.save();
        TestPersistenceManager loadedManager = new TestPersistenceManager(testFile, gson);
        loadedManager.load();

        // Assert
        assertThat(loadedManager.data).hasSize(10000);
        assertThat(loadedManager.isHealthy()).isTrue();
    }

    // ==================== Test Helper Classes ====================

    /**
     * Concrete implementation of AbstractPersistenceManager for testing
     */
    private static class TestPersistenceManager extends AbstractPersistenceManager<Map<String, String>> {

        public Map<String, String> data = new HashMap<>();
        public boolean wasNoDataFileFoundCalled = false;
        public boolean wasCriticalFailureCalled = false;

        public TestPersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return new TypeToken<Map<String, String>>() {}.getType();
        }

        @Override
        protected void onDataLoaded(Map<String, String> loadedData) {
            this.data = loadedData;
        }

        @Override
        protected Map<String, String> getCurrentData() {
            return data;
        }

        @Override
        protected String getComponentName() {
            return "TestManager";
        }

        @Override
        protected String getHealthDetails() {
            return String.format("%d Einträge", data.size());
        }

        @Override
        protected void onNoDataFileFound() {
            wasNoDataFileFoundCalled = true;
        }

        @Override
        protected void onCriticalLoadFailure() {
            wasCriticalFailureCalled = true;
            data.clear();
        }
    }

    /**
     * Manager that simulates save failures for testing
     */
    private static class FailingSaveManager extends TestPersistenceManager {

        public FailingSaveManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Map<String, String> getCurrentData() {
            // Simulate failure by returning null
            throw new RuntimeException("Simulated save failure");
        }
    }
}
