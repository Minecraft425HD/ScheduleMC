package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for BackupManager
 *
 * Tests backup and recovery scenarios:
 * - Backup Creation
 * - Backup Recovery
 * - Backup Cleanup (old backups)
 * - Multi-file Backups
 * - Error Handling
 *
 * @since 1.0
 */
@DisplayName("BackupManager Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BackupManagerIntegrationTest {

    @TempDir
    Path tempDir;

    private File testDataFile;

    @BeforeEach
    void setUp() throws IOException {
        testDataFile = tempDir.resolve("test_data.json").toFile();

        // Create test data file
        Files.writeString(testDataFile.toPath(), "{\"data\": \"test content\"}");
    }

    @Test
    @Order(1)
    @DisplayName("Backup Creation - Should create backup file")
    void testCreateBackup() {
        // Act
        File backupFile = BackupManager.createBackup(testDataFile, "TestComponent");

        // Assert
        assertThat(backupFile).isNotNull();
        assertThat(backupFile).exists();
        assertThat(backupFile.getName()).contains("test_data");
        assertThat(backupFile.getName()).contains(".backup_");
        assertThat(backupFile.getName()).contains("TestComponent");
    }

    @Test
    @Order(2)
    @DisplayName("Backup Creation - Should preserve file content")
    void testBackupPreservesContent() throws IOException {
        // Arrange
        String originalContent = Files.readString(testDataFile.toPath());

        // Act
        File backupFile = BackupManager.createBackup(testDataFile, "TestComponent");

        // Assert
        String backupContent = Files.readString(backupFile.toPath());
        assertThat(backupContent).isEqualTo(originalContent);
    }

    @Test
    @Order(3)
    @DisplayName("Backup Recovery - Should recover from latest backup")
    void testRecoverFromBackup() throws IOException {
        // Arrange
        File backupFile = BackupManager.createBackup(testDataFile, "TestComponent");

        // Corrupt the original file
        Files.writeString(testDataFile.toPath(), "{corrupted}");

        // Act
        boolean recovered = BackupManager.recoverFromBackup(testDataFile, "TestComponent");

        // Assert
        assertThat(recovered).isTrue();
        String recoveredContent = Files.readString(testDataFile.toPath());
        assertThat(recoveredContent).isEqualTo("{\"data\": \"test content\"}");
    }

    @Test
    @Order(4)
    @DisplayName("Backup Recovery - Should return false when no backup exists")
    void testRecoverWithNoBackup() {
        // Arrange
        File nonExistentFile = tempDir.resolve("nonexistent.json").toFile();

        // Act
        boolean recovered = BackupManager.recoverFromBackup(nonExistentFile, "NonExistent");

        // Assert
        assertThat(recovered).isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("Multiple Backups - Should create multiple backup versions")
    void testMultipleBackups() throws InterruptedException {
        // Act - Create 3 backups
        File backup1 = BackupManager.createBackup(testDataFile, "TestComponent");
        Thread.sleep(10); // Ensure different timestamps
        File backup2 = BackupManager.createBackup(testDataFile, "TestComponent");
        Thread.sleep(10);
        File backup3 = BackupManager.createBackup(testDataFile, "TestComponent");

        // Assert
        assertThat(backup1).exists();
        assertThat(backup2).exists();
        assertThat(backup3).exists();
        assertThat(backup1.getName()).isNotEqualTo(backup2.getName());
        assertThat(backup2.getName()).isNotEqualTo(backup3.getName());
    }

    @Test
    @Order(6)
    @DisplayName("Backup Cleanup - Should cleanup old backups")
    void testBackupCleanup() throws InterruptedException {
        // Arrange - Create 5 backups
        for (int i = 0; i < 5; i++) {
            BackupManager.createBackup(testDataFile, "TestComponent");
            Thread.sleep(10);
        }

        // Act - Cleanup keeping only 3
        BackupManager.cleanupOldBackups(testDataFile, "TestComponent", 3);

        // Assert - Count remaining backups
        File[] backups = testDataFile.getParentFile().listFiles((dir, name) ->
            name.contains("test_data") && name.contains(".backup_") && name.contains("TestComponent")
        );
        assertThat(backups).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    @Order(7)
    @DisplayName("Latest Backup - Should find latest backup file")
    void testFindLatestBackup() throws InterruptedException {
        // Arrange
        BackupManager.createBackup(testDataFile, "TestComponent");
        Thread.sleep(50);
        File latestBackup = BackupManager.createBackup(testDataFile, "TestComponent");

        // Act
        File found = BackupManager.findLatestBackup(testDataFile, "TestComponent");

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(latestBackup.getName());
    }

    @Test
    @Order(8)
    @DisplayName("Backup Health - Should report backup health status")
    void testBackupHealthStatus() {
        // Arrange
        BackupManager.createBackup(testDataFile, "TestComponent");

        // Act
        boolean hasBackup = BackupManager.hasBackup(testDataFile, "TestComponent");

        // Assert
        assertThat(hasBackup).isTrue();
    }
}
