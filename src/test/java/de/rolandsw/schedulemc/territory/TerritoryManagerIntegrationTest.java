package de.rolandsw.schedulemc.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for TerritoryManager
 *
 * Tests comprehensive territory management scenarios:
 * - Territory CRUD Operations
 * - Territory Queries & Filtering
 * - Persistence & Recovery
 * - Multi-territory Management
 * - Owner-based Queries
 *
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TerritoryManager Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TerritoryManagerIntegrationTest {

    @TempDir
    Path tempDir;

    @Mock
    private MinecraftServer mockServer;

    private TerritoryManager manager;
    private UUID ownerA;
    private UUID ownerB;

    @BeforeEach
    void setUp() {
        // Configure temp directory
        when(mockServer.getServerDirectory()).thenReturn(tempDir.toFile());

        // Create manager instance
        manager = TerritoryManager.getInstance(mockServer);

        // Create test owners
        ownerA = UUID.randomUUID();
        ownerB = UUID.randomUUID();
    }

    @Test
    @Order(1)
    @DisplayName("Territory Creation - Should create territory")
    void testCreateTerritory() {
        // Act
        manager.setTerritory(10, 20, TerritoryType.PLAYER_OWNED, "Test Territory", ownerA);

        // Assert
        assertThat(manager.hasTerritory(10, 20)).isTrue();
        Territory territory = manager.getTerritory(10, 20);
        assertThat(territory).isNotNull();
        assertThat(territory.getName()).isEqualTo("Test Territory");
        assertThat(territory.getOwnerUUID()).isEqualTo(ownerA);
        assertThat(territory.getType()).isEqualTo(TerritoryType.PLAYER_OWNED);
    }

    @Test
    @Order(2)
    @DisplayName("Territory Update - Should update existing territory")
    void testUpdateTerritory() {
        // Arrange
        manager.setTerritory(10, 20, TerritoryType.PLAYER_OWNED, "Original", ownerA);

        // Act
        manager.setTerritory(10, 20, TerritoryType.FACTION_OWNED, "Updated", ownerB);

        // Assert
        Territory territory = manager.getTerritory(10, 20);
        assertThat(territory.getName()).isEqualTo("Updated");
        assertThat(territory.getOwnerUUID()).isEqualTo(ownerB);
        assertThat(territory.getType()).isEqualTo(TerritoryType.FACTION_OWNED);
    }

    @Test
    @Order(3)
    @DisplayName("Territory Removal - Should remove territory")
    void testRemoveTerritory() {
        // Arrange
        manager.setTerritory(10, 20, TerritoryType.PLAYER_OWNED, "Test", ownerA);

        // Act
        boolean removed = manager.removeTerritory(10, 20);

        // Assert
        assertThat(removed).isTrue();
        assertThat(manager.hasTerritory(10, 20)).isFalse();
        assertThat(manager.getTerritory(10, 20)).isNull();
    }

    @Test
    @Order(4)
    @DisplayName("Territory by BlockPos - Should find territory at block position")
    void testGetTerritoryByBlockPos() {
        // Arrange
        manager.setTerritory(5, 10, TerritoryType.SAFEZONE, "Safe Zone", null);

        // Block at chunk (5, 10) - BlockPos (80, 64, 160) = chunk (5, 10)
        BlockPos pos = new BlockPos(80, 64, 160);

        // Act
        Territory territory = manager.getTerritoryAt(pos);

        // Assert
        assertThat(territory).isNotNull();
        assertThat(territory.getName()).isEqualTo("Safe Zone");
    }

    @Test
    @Order(5)
    @DisplayName("Territory Queries - Should filter by type")
    void testGetTerritoriesByType() {
        // Arrange
        manager.setTerritory(1, 1, TerritoryType.PLAYER_OWNED, "Player 1", ownerA);
        manager.setTerritory(2, 2, TerritoryType.PLAYER_OWNED, "Player 2", ownerB);
        manager.setTerritory(3, 3, TerritoryType.SAFEZONE, "Safe", null);

        // Act
        var playerTerritories = manager.getTerritoriesByType(TerritoryType.PLAYER_OWNED);

        // Assert
        assertThat(playerTerritories).hasSize(2);
        assertThat(playerTerritories).allMatch(t -> t.getType() == TerritoryType.PLAYER_OWNED);
    }

    @Test
    @Order(6)
    @DisplayName("Territory Queries - Should filter by owner")
    void testGetTerritoriesByOwner() {
        // Arrange
        manager.setTerritory(1, 1, TerritoryType.PLAYER_OWNED, "Owner A - 1", ownerA);
        manager.setTerritory(2, 2, TerritoryType.PLAYER_OWNED, "Owner A - 2", ownerA);
        manager.setTerritory(3, 3, TerritoryType.PLAYER_OWNED, "Owner B", ownerB);

        // Act
        var ownerATerritories = manager.getTerritoriesByOwner(ownerA);

        // Assert
        assertThat(ownerATerritories).hasSize(2);
        assertThat(ownerATerritories).allMatch(t -> t.getOwnerUUID().equals(ownerA));
    }

    @Test
    @Order(7)
    @DisplayName("Territory Count - Should count all territories")
    void testGetTerritoryCount() {
        // Arrange
        manager.setTerritory(1, 1, TerritoryType.PLAYER_OWNED, "T1", ownerA);
        manager.setTerritory(2, 2, TerritoryType.SAFEZONE, "T2", null);
        manager.setTerritory(3, 3, TerritoryType.FACTION_OWNED, "T3", ownerB);

        // Act
        int count = manager.getTerritoryCount();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @Order(8)
    @DisplayName("Bulk Operations - Should clear territories by type")
    void testClearTerritoriesByType() {
        // Arrange
        manager.setTerritory(1, 1, TerritoryType.PLAYER_OWNED, "P1", ownerA);
        manager.setTerritory(2, 2, TerritoryType.PLAYER_OWNED, "P2", ownerB);
        manager.setTerritory(3, 3, TerritoryType.SAFEZONE, "Safe", null);

        // Act
        int removed = manager.clearTerritoriesByType(TerritoryType.PLAYER_OWNED);

        // Assert
        assertThat(removed).isEqualTo(2);
        assertThat(manager.getTerritoryCount()).isEqualTo(1);
        assertThat(manager.getTerritory(3, 3)).isNotNull(); // Safezone remains
    }

    @Test
    @Order(9)
    @DisplayName("Statistics - Should generate territory statistics")
    void testGetStatistics() {
        // Arrange
        manager.setTerritory(1, 1, TerritoryType.PLAYER_OWNED, "P1", ownerA);
        manager.setTerritory(2, 2, TerritoryType.PLAYER_OWNED, "P2", ownerB);
        manager.setTerritory(3, 3, TerritoryType.SAFEZONE, "Safe", null);

        // Act
        String stats = manager.getStatistics();

        // Assert
        assertThat(stats).contains("Territories: 3");
        assertThat(stats).contains("Player-Owned: 2");
        assertThat(stats).contains("Safezone: 1");
    }

    @Test
    @Order(10)
    @DisplayName("Persistence - Should save and load territories")
    void testPersistence() {
        // Arrange
        manager.setTerritory(10, 20, TerritoryType.PLAYER_OWNED, "Persistent", ownerA);

        // Act - Save
        manager.save();

        // Create new manager instance (simulating server restart)
        TerritoryManager newManager = TerritoryManager.getInstance(mockServer);

        // Assert
        assertThat(newManager.hasTerritory(10, 20)).isTrue();
        Territory loaded = newManager.getTerritory(10, 20);
        assertThat(loaded.getName()).isEqualTo("Persistent");
        assertThat(loaded.getOwnerUUID()).isEqualTo(ownerA);
    }
}
