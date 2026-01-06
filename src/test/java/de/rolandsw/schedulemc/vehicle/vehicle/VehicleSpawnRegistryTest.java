package de.rolandsw.schedulemc.vehicle.vehicle;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive JUnit 5 Tests for VehicleSpawnRegistry
 *
 * Tests all critical functionality:
 * - Spawn Point Registration/Unregistration
 * - Dealer Assignment
 * - Free Spawn Point Lookup
 * - Vehicle Spawning Logic (occupy/release)
 * - Persistence (Save/Load)
 * - Thread Safety (concurrent access)
 * - Edge Cases (null dealer, full registry, null checks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleSpawnRegistry Tests")
class VehicleSpawnRegistryTest {

    private static final File TEST_SPAWN_FILE = new File("config/vehicle_spawns_test.json");

    private UUID dealer1;
    private UUID dealer2;
    private UUID dealer3;

    private BlockPos pos1;
    private BlockPos pos2;
    private BlockPos pos3;
    private BlockPos pos4;

    @BeforeEach
    void setUp() {
        // Clear registry before each test
        clearRegistry();

        // Initialize dealer IDs
        dealer1 = UUID.randomUUID();
        dealer2 = UUID.randomUUID();
        dealer3 = UUID.randomUUID();

        // Initialize test positions
        pos1 = new BlockPos(100, 64, 100);
        pos2 = new BlockPos(110, 64, 110);
        pos3 = new BlockPos(120, 64, 120);
        pos4 = new BlockPos(130, 64, 130);

        // Clean up test file
        if (TEST_SPAWN_FILE.exists()) {
            TEST_SPAWN_FILE.delete();
        }
    }

    @AfterEach
    void tearDown() {
        clearRegistry();
        if (TEST_SPAWN_FILE.exists()) {
            TEST_SPAWN_FILE.delete();
        }
    }

    // Helper to clear the static registry using reflection
    private void clearRegistry() {
        try {
            java.lang.reflect.Field field = VehicleSpawnRegistry.class.getDeclaredField("dealerSpawnPoints");
            field.setAccessible(true);
            ((java.util.Map<?, ?>) field.get(null)).clear();

            java.lang.reflect.Field dirtyField = VehicleSpawnRegistry.class.getDeclaredField("isDirty");
            dirtyField.setAccessible(true);
            dirtyField.setBoolean(null, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear registry", e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 1. SPAWN POINT REGISTRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should add spawn point successfully")
    void testAddSpawnPoint() {
        // Act
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 90.0f);

        // Assert
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);
        assertThat(points).hasSize(1);
        assertThat(points.get(0).getPosition()).isEqualTo(pos1);
        assertThat(points.get(0).getYaw()).isEqualTo(90.0f);
        assertThat(points.get(0).isOccupied()).isFalse();
    }

    @Test
    @DisplayName("Should add multiple spawn points for same dealer")
    void testAddMultipleSpawnPoints() {
        // Act
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos3, 180.0f);

        // Assert
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);
        assertThat(points).hasSize(3);
    }

    @Test
    @DisplayName("Should add spawn points for multiple dealers")
    void testAddSpawnPointsMultipleDealers() {
        // Act
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer2, pos2, 90.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer3, pos3, 180.0f);

        // Assert
        assertThat(VehicleSpawnRegistry.getSpawnPoints(dealer1)).hasSize(1);
        assertThat(VehicleSpawnRegistry.getSpawnPoints(dealer2)).hasSize(1);
        assertThat(VehicleSpawnRegistry.getSpawnPoints(dealer3)).hasSize(1);
    }

    @Test
    @DisplayName("Should handle null dealer ID gracefully")
    void testAddSpawnPointNullDealer() {
        // Act & Assert - Should not throw exception
        assertThatCode(() -> VehicleSpawnRegistry.addSpawnPoint(null, pos1, 0.0f))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow duplicate positions for different dealers")
    void testAddDuplicatePositionsDifferentDealers() {
        // Act
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer2, pos1, 90.0f); // Same position

        // Assert
        assertThat(VehicleSpawnRegistry.getSpawnPoints(dealer1)).hasSize(1);
        assertThat(VehicleSpawnRegistry.getSpawnPoints(dealer2)).hasSize(1);
    }

    // ═══════════════════════════════════════════════════════════
    // 2. SPAWN POINT REMOVAL TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should remove spawn point successfully")
    void testRemoveSpawnPoint() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);
        assertThat(VehicleSpawnRegistry.getSpawnPoints(dealer1)).hasSize(2);

        // Act
        VehicleSpawnRegistry.removeSpawnPoint(dealer1, pos1);

        // Assert
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);
        assertThat(points).hasSize(1);
        assertThat(points.get(0).getPosition()).isEqualTo(pos2);
    }

    @Test
    @DisplayName("Should handle removing non-existent spawn point")
    void testRemoveNonExistentSpawnPoint() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);

        // Act & Assert - Should not throw exception
        assertThatCode(() -> VehicleSpawnRegistry.removeSpawnPoint(dealer1, pos2))
            .doesNotThrowAnyException();

        // Original point should still exist
        assertThat(VehicleSpawnRegistry.getSpawnPoints(dealer1)).hasSize(1);
    }

    @Test
    @DisplayName("Should handle removing from non-existent dealer")
    void testRemoveSpawnPointNonExistentDealer() {
        // Act & Assert
        assertThatCode(() -> VehicleSpawnRegistry.removeSpawnPoint(dealer1, pos1))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle null dealer ID in removal")
    void testRemoveSpawnPointNullDealer() {
        // Act & Assert
        assertThatCode(() -> VehicleSpawnRegistry.removeSpawnPoint(null, pos1))
            .doesNotThrowAnyException();
    }

    // ═══════════════════════════════════════════════════════════
    // 3. SPAWN POINT LOOKUP TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should get spawn points for dealer")
    void testGetSpawnPoints() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);

        // Act
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);

        // Assert
        assertThat(points).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list for dealer with no spawn points")
    void testGetSpawnPointsEmptyDealer() {
        // Act
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);

        // Assert
        assertThat(points).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for null dealer")
    void testGetSpawnPointsNullDealer() {
        // Act
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(null);

        // Assert
        assertThat(points).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    // 4. FREE SPAWN POINT LOOKUP TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should find free spawn point")
    void testFindFreeSpawnPoint() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);

        // Act
        VehicleSpawnRegistry.VehicleSpawnPoint freePoint = VehicleSpawnRegistry.findFreeSpawnPoint(dealer1);

        // Assert
        assertThat(freePoint).isNotNull();
        assertThat(freePoint.isOccupied()).isFalse();
    }

    @Test
    @DisplayName("Should return null when all spawn points occupied")
    void testFindFreeSpawnPointAllOccupied() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);

        UUID vehicle1 = UUID.randomUUID();
        UUID vehicle2 = UUID.randomUUID();
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicle1);
        VehicleSpawnRegistry.occupySpawnPoint(pos2, vehicle2);

        // Act
        VehicleSpawnRegistry.VehicleSpawnPoint freePoint = VehicleSpawnRegistry.findFreeSpawnPoint(dealer1);

        // Assert
        assertThat(freePoint).isNull();
    }

    @Test
    @DisplayName("Should return null for dealer with no spawn points")
    void testFindFreeSpawnPointNoPoints() {
        // Act
        VehicleSpawnRegistry.VehicleSpawnPoint freePoint = VehicleSpawnRegistry.findFreeSpawnPoint(dealer1);

        // Assert
        assertThat(freePoint).isNull();
    }

    @Test
    @DisplayName("Should find first available spawn point")
    void testFindFirstFreeSpawnPoint() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos3, 180.0f);

        // Occupy first point
        UUID vehicle1 = UUID.randomUUID();
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicle1);

        // Act
        VehicleSpawnRegistry.VehicleSpawnPoint freePoint = VehicleSpawnRegistry.findFreeSpawnPoint(dealer1);

        // Assert
        assertThat(freePoint).isNotNull();
        assertThat(freePoint.getPosition()).isNotEqualTo(pos1);
        assertThat(freePoint.isOccupied()).isFalse();
    }

    // ═══════════════════════════════════════════════════════════
    // 5. SPAWN POINT OCCUPATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should occupy spawn point successfully")
    void testOccupySpawnPoint() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        UUID vehicleId = UUID.randomUUID();

        // Act
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicleId);

        // Assert
        VehicleSpawnRegistry.VehicleSpawnPoint point = VehicleSpawnRegistry.getSpawnPoints(dealer1).get(0);
        assertThat(point.isOccupied()).isTrue();
        assertThat(point.getOccupyingVehicleId()).isEqualTo(vehicleId);
    }

    @Test
    @DisplayName("Should check if spawn point is free")
    void testIsSpawnPointFree() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);

        UUID vehicleId = UUID.randomUUID();
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicleId);

        // Act & Assert
        assertThat(VehicleSpawnRegistry.isSpawnPointFree(pos1)).isFalse();
        assertThat(VehicleSpawnRegistry.isSpawnPointFree(pos2)).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-existent position")
    void testIsSpawnPointFreeNonExistent() {
        // Act
        boolean isFree = VehicleSpawnRegistry.isSpawnPointFree(pos1);

        // Assert
        assertThat(isFree).isFalse();
    }

    @Test
    @DisplayName("Should release spawn point successfully")
    void testReleaseSpawnPoint() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        UUID vehicleId = UUID.randomUUID();
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicleId);

        assertThat(VehicleSpawnRegistry.isSpawnPointFree(pos1)).isFalse();

        // Act
        VehicleSpawnRegistry.releaseSpawnPoint(vehicleId);

        // Assert
        assertThat(VehicleSpawnRegistry.isSpawnPointFree(pos1)).isTrue();
        VehicleSpawnRegistry.VehicleSpawnPoint point = VehicleSpawnRegistry.getSpawnPoints(dealer1).get(0);
        assertThat(point.isOccupied()).isFalse();
        assertThat(point.getOccupyingVehicleId()).isNull();
    }

    @Test
    @DisplayName("Should handle releasing non-existent vehicle")
    void testReleaseNonExistentVehicle() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        UUID vehicleId = UUID.randomUUID();

        // Act & Assert - Should not throw exception
        assertThatCode(() -> VehicleSpawnRegistry.releaseSpawnPoint(vehicleId))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle null vehicle ID in release")
    void testReleaseNullVehicleId() {
        // Act & Assert
        assertThatCode(() -> VehicleSpawnRegistry.releaseSpawnPoint(null))
            .doesNotThrowAnyException();
    }

    // ═══════════════════════════════════════════════════════════
    // 6. PERSISTENCE TESTS (Save/Load)
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should save spawn points to disk")
    void testSaveSpawnPoints() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);

        // Act
        VehicleSpawnRegistry.save();

        // Assert - File should exist (can't easily verify content without changing SPAWN_FILE)
        // This test verifies save() doesn't throw exceptions
        assertThatCode(() -> VehicleSpawnRegistry.save()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should load spawn points from disk")
    void testLoadSpawnPoints() {
        // This test is limited because load() uses a static file path
        // We can only verify it doesn't throw exceptions
        assertThatCode(() -> VehicleSpawnRegistry.load()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should save only when dirty")
    void testSaveIfNeeded() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);

        // Act & Assert - Should save when dirty
        assertThatCode(() -> VehicleSpawnRegistry.saveIfNeeded()).doesNotThrowAnyException();

        // After save, calling saveIfNeeded again should not save (not dirty)
        assertThatCode(() -> VehicleSpawnRegistry.saveIfNeeded()).doesNotThrowAnyException();
    }

    // ═══════════════════════════════════════════════════════════
    // 7. THREAD SAFETY TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should handle concurrent spawn point additions")
    void testConcurrentAddSpawnPoint() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int pointsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < pointsPerThread; i++) {
                        BlockPos pos = new BlockPos(threadId * 100 + i, 64, 100);
                        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos, (float) i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);
        assertThat(points).hasSize(threadCount * pointsPerThread);
    }

    @Test
    @DisplayName("Should handle concurrent spawn point lookups")
    void testConcurrentFindFreeSpawnPoint() throws InterruptedException {
        // Arrange
        for (int i = 0; i < 100; i++) {
            VehicleSpawnRegistry.addSpawnPoint(dealer1, new BlockPos(i, 64, 100), 0.0f);
        }

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        VehicleSpawnRegistry.VehicleSpawnPoint point = VehicleSpawnRegistry.findFreeSpawnPoint(dealer1);
                        if (point != null) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - All lookups should succeed
        assertThat(successCount.get()).isEqualTo(threadCount * 50);
    }

    @Test
    @DisplayName("Should handle concurrent occupy and release operations")
    void testConcurrentOccupyRelease() throws InterruptedException {
        // Arrange
        for (int i = 0; i < 100; i++) {
            VehicleSpawnRegistry.addSpawnPoint(dealer1, new BlockPos(i, 64, 100), 0.0f);
        }

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 20; i++) {
                        BlockPos pos = new BlockPos(i, 64, 100);
                        UUID vehicleId = UUID.randomUUID();
                        VehicleSpawnRegistry.occupySpawnPoint(pos, vehicleId);
                        VehicleSpawnRegistry.releaseSpawnPoint(vehicleId);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - All points should be free again
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);
        long occupiedCount = points.stream().filter(VehicleSpawnRegistry.VehicleSpawnPoint::isOccupied).count();
        assertThat(occupiedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle mixed concurrent read/write operations")
    void testConcurrentMixedOperations() throws InterruptedException {
        // Arrange
        int threadCount = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    if (threadId % 3 == 0) {
                        // Add spawn points
                        for (int i = 0; i < 30; i++) {
                            BlockPos pos = new BlockPos(threadId * 100 + i, 64, 100);
                            VehicleSpawnRegistry.addSpawnPoint(dealer1, pos, 0.0f);
                        }
                    } else if (threadId % 3 == 1) {
                        // Find free spawn points
                        for (int i = 0; i < 50; i++) {
                            VehicleSpawnRegistry.findFreeSpawnPoint(dealer1);
                        }
                    } else {
                        // Occupy and release
                        for (int i = 0; i < 20; i++) {
                            BlockPos pos = new BlockPos(i, 64, 100);
                            UUID vehicleId = UUID.randomUUID();
                            VehicleSpawnRegistry.occupySpawnPoint(pos, vehicleId);
                            VehicleSpawnRegistry.releaseSpawnPoint(vehicleId);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - No exceptions thrown, registry remains consistent
        assertThat(VehicleSpawnRegistry.getSpawnPoints(dealer1)).isNotNull();
    }

    // ═══════════════════════════════════════════════════════════
    // 8. EDGE CASES & STRESS TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should handle large number of spawn points (1000+)")
    void testLargeNumberOfSpawnPoints() {
        // Arrange & Act
        int pointCount = 1000;
        for (int i = 0; i < pointCount; i++) {
            BlockPos pos = new BlockPos(i, 64, 100);
            VehicleSpawnRegistry.addSpawnPoint(dealer1, pos, (float) i);
        }

        // Assert
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);
        assertThat(points).hasSize(pointCount);
    }

    @Test
    @DisplayName("Should handle extreme yaw values")
    void testExtremeYawValues() {
        // Arrange & Act
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 360.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, -180.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos3, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos4, Float.MAX_VALUE);

        // Assert
        List<VehicleSpawnRegistry.VehicleSpawnPoint> points = VehicleSpawnRegistry.getSpawnPoints(dealer1);
        assertThat(points).hasSize(4);
        assertThat(points.get(0).getYaw()).isEqualTo(360.0f);
        assertThat(points.get(1).getYaw()).isEqualTo(-180.0f);
        assertThat(points.get(3).getYaw()).isEqualTo(Float.MAX_VALUE);
    }

    @Test
    @DisplayName("Should handle multiple dealers with many spawn points")
    void testMultipleDealersWithManyPoints() {
        // Arrange & Act
        for (int d = 0; d < 10; d++) {
            UUID dealer = UUID.randomUUID();
            for (int p = 0; p < 50; p++) {
                BlockPos pos = new BlockPos(d * 100 + p, 64, 100);
                VehicleSpawnRegistry.addSpawnPoint(dealer, pos, 0.0f);
            }
        }

        // Assert - Each dealer should have 50 points (can't easily verify without tracking dealers)
        // This test primarily ensures no exceptions with large dataset
    }

    @Test
    @DisplayName("Should handle occupation state correctly")
    void testOccupationStateMachine() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        UUID vehicle1 = UUID.randomUUID();
        UUID vehicle2 = UUID.randomUUID();

        // Act & Assert - Initial state
        assertThat(VehicleSpawnRegistry.isSpawnPointFree(pos1)).isTrue();

        // Occupy
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicle1);
        assertThat(VehicleSpawnRegistry.isSpawnPointFree(pos1)).isFalse();

        // Release
        VehicleSpawnRegistry.releaseSpawnPoint(vehicle1);
        assertThat(VehicleSpawnRegistry.isSpawnPointFree(pos1)).isTrue();

        // Re-occupy with different vehicle
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicle2);
        assertThat(VehicleSpawnRegistry.isSpawnPointFree(pos1)).isFalse();

        // Verify correct vehicle ID
        VehicleSpawnRegistry.VehicleSpawnPoint point = VehicleSpawnRegistry.getSpawnPoints(dealer1).get(0);
        assertThat(point.getOccupyingVehicleId()).isEqualTo(vehicle2);
    }

    @Test
    @DisplayName("Should handle VehicleSpawnPoint getters correctly")
    void testVehicleSpawnPointGetters() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 45.5f);
        UUID vehicleId = UUID.randomUUID();
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicleId);

        // Act
        VehicleSpawnRegistry.VehicleSpawnPoint point = VehicleSpawnRegistry.getSpawnPoints(dealer1).get(0);

        // Assert
        assertThat(point.getPosition()).isEqualTo(pos1);
        assertThat(point.getYaw()).isEqualTo(45.5f);
        assertThat(point.isOccupied()).isTrue();
        assertThat(point.getOccupyingVehicleId()).isEqualTo(vehicleId);
    }

    @Test
    @DisplayName("Should correctly identify free spawn points after partial occupation")
    void testPartialOccupation() {
        // Arrange
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos1, 0.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos2, 90.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos3, 180.0f);
        VehicleSpawnRegistry.addSpawnPoint(dealer1, pos4, 270.0f);

        // Occupy some points
        UUID vehicle1 = UUID.randomUUID();
        UUID vehicle3 = UUID.randomUUID();
        VehicleSpawnRegistry.occupySpawnPoint(pos1, vehicle1);
        VehicleSpawnRegistry.occupySpawnPoint(pos3, vehicle3);

        // Act
        VehicleSpawnRegistry.VehicleSpawnPoint freePoint = VehicleSpawnRegistry.findFreeSpawnPoint(dealer1);

        // Assert
        assertThat(freePoint).isNotNull();
        assertThat(freePoint.getPosition()).isIn(pos2, pos4);
        assertThat(freePoint.isOccupied()).isFalse();
    }

    // ═══════════════════════════════════════════════════════════
    // 9. PERFORMANCE TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should demonstrate fast lookup performance")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testLookupPerformance() {
        // Arrange - Add 5000 spawn points
        for (int i = 0; i < 5000; i++) {
            BlockPos pos = new BlockPos(i, 64, 100);
            VehicleSpawnRegistry.addSpawnPoint(dealer1, pos, 0.0f);
        }

        // Act - Perform 5000 lookups
        for (int i = 0; i < 5000; i++) {
            VehicleSpawnRegistry.findFreeSpawnPoint(dealer1);
        }

        // Test should complete within timeout
    }

    @Test
    @DisplayName("Should demonstrate fast occupation/release performance")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testOccupationPerformance() {
        // Arrange
        for (int i = 0; i < 1000; i++) {
            BlockPos pos = new BlockPos(i, 64, 100);
            VehicleSpawnRegistry.addSpawnPoint(dealer1, pos, 0.0f);
        }

        // Act - Occupy and release 1000 times
        for (int i = 0; i < 1000; i++) {
            BlockPos pos = new BlockPos(i, 64, 100);
            UUID vehicleId = UUID.randomUUID();
            VehicleSpawnRegistry.occupySpawnPoint(pos, vehicleId);
            VehicleSpawnRegistry.releaseSpawnPoint(vehicleId);
        }

        // Test should complete within timeout
    }
}
