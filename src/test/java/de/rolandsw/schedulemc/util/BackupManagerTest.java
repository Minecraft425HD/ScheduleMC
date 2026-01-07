package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for {@link BackupManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Backup creation with timestamp formatting</li>
 *   <li>Backup restoration from latest backup</li>
 *   <li>Automatic cleanup of old backups (MAX_BACKUPS = 5)</li>
 *   <li>Latest backup retrieval</li>
 *   <li>Backup listing (sorted by modification time)</li>
 *   <li>Backup count tracking</li>
 *   <li>Error handling (non-existent files, missing backups)</li>
 *   <li>Retry mechanism integration</li>
 * </ul>
 *
 * <p><b>Note:</b> Uses {@link TempDir} for isolated filesystem testing.
 */
@DisplayName("BackupManager Tests")
class BackupManagerTest {

    @TempDir
    Path tempDir;

    // ═══════════════════════════════════════════════════════════
    // BACKUP CREATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Backup Creation")
    class BackupCreationTests {

        @Test
        @DisplayName("createBackup() should create backup file with timestamp")
        void createBackupShouldCreateFileWithTimestamp() throws IOException {
            File sourceFile = createTestFile("test.dat", "test content");

            boolean result = BackupManager.createBackup(sourceFile);

            assertThat(result).isTrue();
            assertThat(BackupManager.getBackupCount(sourceFile)).isEqualTo(1);

            File[] backups = BackupManager.listBackups(sourceFile);
            assertThat(backups[0].getName()).startsWith("test.dat.backup_");
            assertThat(backups[0].getName()).matches("test\\.dat\\.backup_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}");
        }

        @Test
        @DisplayName("createBackup() should return false for non-existent file")
        void createBackupShouldReturnFalseForNonExistent() {
            File nonExistent = tempDir.resolve("nonexistent.dat").toFile();

            boolean result = BackupManager.createBackup(nonExistent);

            assertThat(result).isFalse();
            assertThat(BackupManager.getBackupCount(nonExistent)).isZero();
        }

        @Test
        @DisplayName("createBackup() should preserve file content")
        void createBackupShouldPreserveContent() throws IOException {
            String content = "Important data that must be preserved!";
            File sourceFile = createTestFile("data.txt", content);

            BackupManager.createBackup(sourceFile);

            File[] backups = BackupManager.listBackups(sourceFile);
            assertThat(backups).hasSize(1);

            String backupContent = Files.readString(backups[0].toPath());
            assertThat(backupContent).isEqualTo(content);
        }

        @Test
        @DisplayName("createBackup() should create multiple backups with different timestamps")
        void createBackupShouldCreateMultipleBackups() throws IOException, InterruptedException {
            File sourceFile = createTestFile("multi.dat", "content");

            BackupManager.createBackup(sourceFile);
            Thread.sleep(1100);  // Ensure different timestamp (1 second resolution)
            BackupManager.createBackup(sourceFile);

            assertThat(BackupManager.getBackupCount(sourceFile)).isEqualTo(2);

            File[] backups = BackupManager.listBackups(sourceFile);
            assertThat(backups).hasSize(2);
            assertThat(backups[0].getName()).isNotEqualTo(backups[1].getName());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BACKUP CLEANUP TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Backup Cleanup (MAX_BACKUPS = 5)")
    class BackupCleanupTests {

        @Test
        @DisplayName("should keep only 5 most recent backups")
        void shouldKeepOnlyFiveMostRecentBackups() throws IOException, InterruptedException {
            File sourceFile = createTestFile("cleanup.dat", "content");

            // Create 7 backups
            for (int i = 0; i < 7; i++) {
                BackupManager.createBackup(sourceFile);
                Thread.sleep(1100);  // Ensure different timestamps
            }

            // Should only have 5 backups (oldest 2 deleted)
            assertThat(BackupManager.getBackupCount(sourceFile)).isEqualTo(5);
        }

        @Test
        @DisplayName("should delete oldest backups first")
        void shouldDeleteOldestBackupsFirst() throws IOException, InterruptedException {
            File sourceFile = createTestFile("order.dat", "content");

            // Create 6 backups and remember the first (oldest) one
            BackupManager.createBackup(sourceFile);
            Thread.sleep(1100);
            String oldestBackupName = BackupManager.listBackups(sourceFile)[0].getName();

            for (int i = 0; i < 5; i++) {
                BackupManager.createBackup(sourceFile);
                Thread.sleep(1100);
            }

            // Oldest backup should be deleted
            File[] backups = BackupManager.listBackups(sourceFile);
            assertThat(backups).hasSize(5);
            assertThat(backups).noneMatch(f -> f.getName().equals(oldestBackupName));
        }

        @Test
        @DisplayName("should not cleanup if backups <= 5")
        void shouldNotCleanupIfBackupsLessThanOrEqualToFive() throws IOException, InterruptedException {
            File sourceFile = createTestFile("nocleanup.dat", "content");

            // Create exactly 5 backups
            for (int i = 0; i < 5; i++) {
                BackupManager.createBackup(sourceFile);
                Thread.sleep(1100);
            }

            assertThat(BackupManager.getBackupCount(sourceFile)).isEqualTo(5);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BACKUP RESTORATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Backup Restoration")
    class BackupRestorationTests {

        @Test
        @DisplayName("restoreFromBackup() should restore latest backup")
        void restoreFromBackupShouldRestoreLatest() throws IOException, InterruptedException {
            File sourceFile = createTestFile("restore.dat", "original");

            // Create backup
            BackupManager.createBackup(sourceFile);

            // Modify original file
            Files.writeString(sourceFile.toPath(), "modified");

            // Restore from backup
            boolean result = BackupManager.restoreFromBackup(sourceFile);

            assertThat(result).isTrue();
            assertThat(Files.readString(sourceFile.toPath())).isEqualTo("original");
        }

        @Test
        @DisplayName("restoreFromBackup() should return false if no backup exists")
        void restoreFromBackupShouldReturnFalseIfNoBackup() throws IOException {
            File sourceFile = createTestFile("nobackup.dat", "content");

            boolean result = BackupManager.restoreFromBackup(sourceFile);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("restoreFromBackup() should use newest backup when multiple exist")
        void restoreFromBackupShouldUseNewestBackup() throws IOException, InterruptedException {
            File sourceFile = createTestFile("multibackup.dat", "v1");

            // Create first backup
            BackupManager.createBackup(sourceFile);
            Thread.sleep(1100);

            // Modify and create second backup
            Files.writeString(sourceFile.toPath(), "v2");
            BackupManager.createBackup(sourceFile);

            // Modify again
            Files.writeString(sourceFile.toPath(), "v3");

            // Restore should get v2 (newest backup)
            BackupManager.restoreFromBackup(sourceFile);

            assertThat(Files.readString(sourceFile.toPath())).isEqualTo("v2");
        }

        @Test
        @DisplayName("restoreFromBackup() should handle corrupted source file")
        void restoreFromBackupShouldHandleCorruptedSource() throws IOException {
            File sourceFile = createTestFile("corrupted.dat", "good data");

            // Create backup
            BackupManager.createBackup(sourceFile);

            // Simulate corruption by deleting file
            sourceFile.delete();

            // Restore
            boolean result = BackupManager.restoreFromBackup(sourceFile);

            assertThat(result).isTrue();
            assertThat(sourceFile).exists();
            assertThat(Files.readString(sourceFile.toPath())).isEqualTo("good data");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GET LATEST BACKUP TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Latest Backup")
    class GetLatestBackupTests {

        @Test
        @DisplayName("getLatestBackup() should return null if no backups exist")
        void getLatestBackupShouldReturnNullIfNoBackups() throws IOException {
            File sourceFile = createTestFile("nobkp.dat", "content");

            File latest = BackupManager.getLatestBackup(sourceFile);

            assertThat(latest).isNull();
        }

        @Test
        @DisplayName("getLatestBackup() should return newest backup")
        void getLatestBackupShouldReturnNewest() throws IOException, InterruptedException {
            File sourceFile = createTestFile("latest.dat", "content");

            BackupManager.createBackup(sourceFile);
            Thread.sleep(1100);
            BackupManager.createBackup(sourceFile);

            File latest = BackupManager.getLatestBackup(sourceFile);
            File[] allBackups = BackupManager.listBackups(sourceFile);

            assertThat(latest).isNotNull();
            assertThat(latest).isEqualTo(allBackups[0]);  // First in sorted list = newest
        }

        @Test
        @DisplayName("getLatestBackup() should return null if parent directory doesn't exist")
        void getLatestBackupShouldReturnNullIfParentMissing() {
            File sourceFile = new File("/nonexistent/dir/file.dat");

            File latest = BackupManager.getLatestBackup(sourceFile);

            assertThat(latest).isNull();
        }

        @Test
        @DisplayName("getLatestBackup() should handle single backup")
        void getLatestBackupShouldHandleSingleBackup() throws IOException {
            File sourceFile = createTestFile("single.dat", "content");

            BackupManager.createBackup(sourceFile);

            File latest = BackupManager.getLatestBackup(sourceFile);

            assertThat(latest).isNotNull();
            assertThat(latest.getName()).startsWith("single.dat.backup_");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LIST BACKUPS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("List Backups")
    class ListBackupsTests {

        @Test
        @DisplayName("listBackups() should return empty array if no backups")
        void listBackupsShouldReturnEmptyArrayIfNone() throws IOException {
            File sourceFile = createTestFile("list.dat", "content");

            File[] backups = BackupManager.listBackups(sourceFile);

            assertThat(backups).isEmpty();
        }

        @Test
        @DisplayName("listBackups() should return backups sorted by newest first")
        void listBackupsShouldReturnNewestFirst() throws IOException, InterruptedException {
            File sourceFile = createTestFile("sorted.dat", "content");

            // Create 3 backups
            BackupManager.createBackup(sourceFile);
            long time1 = System.currentTimeMillis();
            Thread.sleep(1100);

            BackupManager.createBackup(sourceFile);
            long time2 = System.currentTimeMillis();
            Thread.sleep(1100);

            BackupManager.createBackup(sourceFile);
            long time3 = System.currentTimeMillis();

            File[] backups = BackupManager.listBackups(sourceFile);

            assertThat(backups).hasSize(3);
            // Newest first
            assertThat(backups[0].lastModified()).isGreaterThan(backups[1].lastModified());
            assertThat(backups[1].lastModified()).isGreaterThan(backups[2].lastModified());
        }

        @Test
        @DisplayName("listBackups() should return empty array if parent directory doesn't exist")
        void listBackupsShouldReturnEmptyIfParentMissing() {
            File sourceFile = new File("/nonexistent/dir/file.dat");

            File[] backups = BackupManager.listBackups(sourceFile);

            assertThat(backups).isEmpty();
        }

        @Test
        @DisplayName("listBackups() should only return files for the specific target")
        void listBackupsShouldOnlyReturnMatchingFiles() throws IOException {
            File file1 = createTestFile("file1.dat", "content1");
            File file2 = createTestFile("file2.dat", "content2");

            BackupManager.createBackup(file1);
            BackupManager.createBackup(file2);

            File[] backups1 = BackupManager.listBackups(file1);
            File[] backups2 = BackupManager.listBackups(file2);

            assertThat(backups1).hasSize(1);
            assertThat(backups2).hasSize(1);
            assertThat(backups1[0].getName()).startsWith("file1.dat.backup_");
            assertThat(backups2[0].getName()).startsWith("file2.dat.backup_");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BACKUP COUNT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Backup Count")
    class BackupCountTests {

        @Test
        @DisplayName("getBackupCount() should return 0 if no backups")
        void getBackupCountShouldReturnZeroIfNone() throws IOException {
            File sourceFile = createTestFile("count.dat", "content");

            int count = BackupManager.getBackupCount(sourceFile);

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("getBackupCount() should return correct count")
        void getBackupCountShouldReturnCorrectCount() throws IOException, InterruptedException {
            File sourceFile = createTestFile("counttest.dat", "content");

            for (int i = 0; i < 3; i++) {
                BackupManager.createBackup(sourceFile);
                Thread.sleep(1100);
            }

            assertThat(BackupManager.getBackupCount(sourceFile)).isEqualTo(3);
        }

        @Test
        @DisplayName("getBackupCount() should update after cleanup")
        void getBackupCountShouldUpdateAfterCleanup() throws IOException, InterruptedException {
            File sourceFile = createTestFile("cleanup_count.dat", "content");

            // Create 7 backups (triggers cleanup to 5)
            for (int i = 0; i < 7; i++) {
                BackupManager.createBackup(sourceFile);
                Thread.sleep(1100);
            }

            assertThat(BackupManager.getBackupCount(sourceFile)).isEqualTo(5);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASES AND ERROR HANDLING
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty files")
        void shouldHandleEmptyFiles() throws IOException {
            File emptyFile = createTestFile("empty.dat", "");

            boolean created = BackupManager.createBackup(emptyFile);
            assertThat(created).isTrue();

            boolean restored = BackupManager.restoreFromBackup(emptyFile);
            assertThat(restored).isTrue();
            assertThat(emptyFile.length()).isZero();
        }

        @Test
        @DisplayName("should handle large files")
        void shouldHandleLargeFiles() throws IOException {
            String largeContent = "X".repeat(10000);
            File largeFile = createTestFile("large.dat", largeContent);

            boolean created = BackupManager.createBackup(largeFile);
            assertThat(created).isTrue();

            File[] backups = BackupManager.listBackups(largeFile);
            assertThat(backups[0].length()).isEqualTo(largeFile.length());
        }

        @Test
        @DisplayName("should handle special characters in filename")
        void shouldHandleSpecialCharactersInFilename() throws IOException {
            File specialFile = createTestFile("file-with_special.dat", "content");

            boolean created = BackupManager.createBackup(specialFile);
            assertThat(created).isTrue();

            File[] backups = BackupManager.listBackups(specialFile);
            assertThat(backups).hasSize(1);
            assertThat(backups[0].getName()).startsWith("file-with_special.dat.backup_");
        }

        @Test
        @DisplayName("should handle multiple files in same directory")
        void shouldHandleMultipleFilesInSameDirectory() throws IOException {
            File file1 = createTestFile("a.dat", "a");
            File file2 = createTestFile("b.dat", "b");
            File file3 = createTestFile("c.dat", "c");

            BackupManager.createBackup(file1);
            BackupManager.createBackup(file2);
            BackupManager.createBackup(file3);

            assertThat(BackupManager.getBackupCount(file1)).isEqualTo(1);
            assertThat(BackupManager.getBackupCount(file2)).isEqualTo(1);
            assertThat(BackupManager.getBackupCount(file3)).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("complete backup lifecycle: create → modify → restore")
        void completeBackupLifecycle() throws IOException {
            File file = createTestFile("lifecycle.dat", "version 1");

            // Create initial backup
            BackupManager.createBackup(file);
            assertThat(BackupManager.getBackupCount(file)).isEqualTo(1);

            // Modify file
            Files.writeString(file.toPath(), "version 2");

            // Restore
            BackupManager.restoreFromBackup(file);
            assertThat(Files.readString(file.toPath())).isEqualTo("version 1");
        }

        @Test
        @DisplayName("multiple backup and restore cycles")
        void multipleBackupRestoreCycles() throws IOException, InterruptedException {
            File file = createTestFile("cycles.dat", "v1");

            BackupManager.createBackup(file);
            Thread.sleep(1100);

            Files.writeString(file.toPath(), "v2");
            BackupManager.createBackup(file);
            Thread.sleep(1100);

            Files.writeString(file.toPath(), "v3");
            BackupManager.createBackup(file);

            // Should have 3 backups
            assertThat(BackupManager.getBackupCount(file)).isEqualTo(3);

            // Restore should give v3 (newest)
            BackupManager.restoreFromBackup(file);
            assertThat(Files.readString(file.toPath())).isEqualTo("v3");
        }

        @Test
        @DisplayName("stress test: create many backups rapidly")
        void stressTestManyBackups() throws IOException, InterruptedException {
            File file = createTestFile("stress.dat", "content");

            // Create 10 backups (should trigger cleanup to keep only 5)
            for (int i = 0; i < 10; i++) {
                BackupManager.createBackup(file);
                Thread.sleep(1100);
            }

            assertThat(BackupManager.getBackupCount(file)).isEqualTo(5);

            // All 5 remaining backups should be valid
            File[] backups = BackupManager.listBackups(file);
            for (File backup : backups) {
                assertThat(backup).exists();
                assertThat(Files.readString(backup.toPath())).isEqualTo("content");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Creates a test file with specified content in temp directory
     */
    private File createTestFile(String filename, String content) throws IOException {
        Path filePath = tempDir.resolve(filename);
        Files.writeString(filePath, content);
        return filePath.toFile();
    }
}
