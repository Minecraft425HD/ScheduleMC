package de.rolandsw.schedulemc.npc.crime.prison;

import de.rolandsw.schedulemc.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for PrisonManager
 *
 * Tests comprehensive prison system scenarios:
 * - Imprisonment Process
 * - Release Mechanisms (Time-based, Bail, Admin)
 * - Cell Assignment
 * - Prisoner Tracking
 * - Bail Calculations
 * - Persistence
 *
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PrisonManager Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PrisonManagerIntegrationTest {

    @TempDir
    Path tempDir;

    @Mock
    private MinecraftServer mockServer;

    @Mock
    private ServerPlayer mockPlayer;

    private PrisonManager manager;
    private UUID playerId;
    private BlockPos prisonLocation;

    @BeforeEach
    void setUp() {
        // Configure temp directory
        when(mockServer.getServerDirectory()).thenReturn(tempDir.toFile());

        // Create manager instance
        manager = PrisonManager.getInstance(mockServer);

        // Setup test data
        playerId = UUID.randomUUID();
        prisonLocation = new BlockPos(0, 64, 0);

        // Configure mock player
        when(mockPlayer.getUUID()).thenReturn(playerId);
        when(mockPlayer.getName()).thenReturn(net.minecraft.network.chat.Component.literal("TestPlayer"));
        when(mockPlayer.getPersistentData()).thenReturn(new CompoundTag());

        // Setup prison configuration
        manager.configurePrison(prisonLocation, 5); // 5 cells
    }

    @Test
    @Order(1)
    @DisplayName("Prison Setup - Should configure prison correctly")
    void testConfigurePrison() {
        // Arrange & Act
        manager.configurePrison(new BlockPos(100, 64, 100), 10);

        // Assert
        assertThat(manager.getPrisonLocation()).isEqualTo(new BlockPos(100, 64, 100));
        assertThat(manager.getTotalCells()).isEqualTo(10);
    }

    @Test
    @Order(2)
    @DisplayName("Imprisonment - Should imprison player with wanted level")
    void testImprisonPlayer() {
        // Arrange
        int wantedLevel = 3;

        // Act
        boolean imprisoned = manager.imprisonPlayer(mockPlayer, wantedLevel);

        // Assert
        assertThat(imprisoned).isTrue();
        assertThat(manager.isPrisoner(playerId)).isTrue();
        assertThat(manager.getPrisonerCount()).isEqualTo(1);
    }

    @Test
    @Order(3)
    @DisplayName("Imprisonment - Should assign cell to prisoner")
    void testCellAssignment() {
        // Act
        manager.imprisonPlayer(mockPlayer, 3);

        // Assert
        PrisonManager.PrisonerData data = manager.getPrisonerData(playerId);
        assertThat(data).isNotNull();
        assertThat(data.cellNumber).isBetween(1, 5); // 5 cells configured
        assertThat(data.getCellSpawn()).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("Imprisonment - Should calculate sentence based on wanted level")
    void testSentenceCalculation() {
        // Arrange
        int wantedLevel = 5;

        // Act
        manager.imprisonPlayer(mockPlayer, wantedLevel);

        // Assert
        PrisonManager.PrisonerData data = manager.getPrisonerData(playerId);
        assertThat(data.totalSentenceTicks).isGreaterThan(0);
        assertThat(data.releaseTime).isGreaterThan(0);
    }

    @Test
    @Order(5)
    @DisplayName("Bail - Should calculate bail amount correctly")
    void testBailCalculation() {
        // Arrange
        int wantedLevel = 3;

        // Act
        manager.imprisonPlayer(mockPlayer, wantedLevel);

        // Assert
        PrisonManager.PrisonerData data = manager.getPrisonerData(playerId);
        assertThat(data.bailAmount).isGreaterThan(0.0);
        assertThat(data.bailPaid).isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("Bail - Should allow bail payment with sufficient funds")
    void testPayBail() {
        // Arrange
        manager.imprisonPlayer(mockPlayer, 3);
        PrisonManager.PrisonerData data = manager.getPrisonerData(playerId);

        // Mock economy
        EconomyManager.createAccount(playerId);
        EconomyManager.setBalance(playerId, data.bailAmount + 1000.0);

        // Act
        boolean bailPaid = manager.payBail(mockPlayer);

        // Assert
        assertThat(bailPaid).isTrue();
        assertThat(manager.isPrisoner(playerId)).isFalse(); // Released after bail
    }

    @Test
    @Order(7)
    @DisplayName("Bail - Should reject bail payment with insufficient funds")
    void testPayBailInsufficientFunds() {
        // Arrange
        manager.imprisonPlayer(mockPlayer, 3);
        PrisonManager.PrisonerData data = manager.getPrisonerData(playerId);

        // Mock economy with insufficient funds
        EconomyManager.createAccount(playerId);
        EconomyManager.setBalance(playerId, data.bailAmount - 100.0);

        // Act
        boolean bailPaid = manager.payBail(mockPlayer);

        // Assert
        assertThat(bailPaid).isFalse();
        assertThat(manager.isPrisoner(playerId)).isTrue(); // Still imprisoned
    }

    @Test
    @Order(8)
    @DisplayName("Release - Should release player after sentence served")
    void testTimeBasedRelease() {
        // Arrange
        manager.imprisonPlayer(mockPlayer, 1);
        PrisonManager.PrisonerData data = manager.getPrisonerData(playerId);

        // Simulate time passing
        long currentTime = System.currentTimeMillis();
        data.releaseTime = currentTime - 1000; // Sentence already served

        // Act
        manager.releasePlayer(mockPlayer, PrisonManager.ReleaseReason.SENTENCE_SERVED);

        // Assert
        assertThat(manager.isPrisoner(playerId)).isFalse();
        assertThat(manager.getPrisonerCount()).isEqualTo(0);
    }

    @Test
    @Order(9)
    @DisplayName("Release - Should support admin release")
    void testAdminRelease() {
        // Arrange
        manager.imprisonPlayer(mockPlayer, 5);

        // Act
        manager.releasePlayer(mockPlayer, PrisonManager.ReleaseReason.ADMIN_RELEASE);

        // Assert
        assertThat(manager.isPrisoner(playerId)).isFalse();
        assertThat(manager.getPrisonerCount()).isEqualTo(0);
    }

    @Test
    @Order(10)
    @DisplayName("Prisoner Tracking - Should get all prisoners")
    void testGetAllPrisoners() {
        // Arrange
        ServerPlayer player2 = mock(ServerPlayer.class);
        UUID player2Id = UUID.randomUUID();
        when(player2.getUUID()).thenReturn(player2Id);
        when(player2.getName()).thenReturn(net.minecraft.network.chat.Component.literal("Player2"));
        when(player2.getPersistentData()).thenReturn(new CompoundTag());

        manager.imprisonPlayer(mockPlayer, 3);
        manager.imprisonPlayer(player2, 2);

        // Act
        List<PrisonManager.PrisonerData> prisoners = manager.getAllPrisoners();

        // Assert
        assertThat(prisoners).hasSize(2);
        assertThat(manager.getPrisonerCount()).isEqualTo(2);
    }

    @Test
    @Order(11)
    @DisplayName("Prisoner Tracking - Should check if player is prisoner")
    void testIsPrisoner() {
        // Arrange
        UUID nonPrisonerId = UUID.randomUUID();

        // Act
        manager.imprisonPlayer(mockPlayer, 3);

        // Assert
        assertThat(manager.isPrisoner(playerId)).isTrue();
        assertThat(manager.isPrisoner(nonPrisonerId)).isFalse();
    }

    @Test
    @Order(12)
    @DisplayName("Cell Management - Should not exceed cell capacity")
    void testCellCapacity() {
        // Arrange - Configure prison with 2 cells
        manager.configurePrison(prisonLocation, 2);

        // Act - Try to imprison 3 players
        ServerPlayer p1 = createMockPlayer();
        ServerPlayer p2 = createMockPlayer();
        ServerPlayer p3 = createMockPlayer();

        boolean r1 = manager.imprisonPlayer(p1, 2);
        boolean r2 = manager.imprisonPlayer(p2, 2);
        boolean r3 = manager.imprisonPlayer(p3, 2);

        // Assert
        assertThat(r1).isTrue();
        assertThat(r2).isTrue();
        // Third imprisonment should fail (no cells available)
        assertThat(r3).isFalse();
    }

    @Test
    @Order(13)
    @DisplayName("Prisoner Data - Should retrieve prisoner data")
    void testGetPrisonerData() {
        // Arrange
        manager.imprisonPlayer(mockPlayer, 4);

        // Act
        PrisonManager.PrisonerData data = manager.getPrisonerData(playerId);

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.playerUUID).isEqualTo(playerId);
        assertThat(data.playerName).isEqualTo("TestPlayer");
        assertThat(data.cellNumber).isGreaterThan(0);
        assertThat(data.totalSentenceTicks).isGreaterThan(0);
        assertThat(data.bailAmount).isGreaterThan(0.0);
    }

    @Test
    @Order(14)
    @DisplayName("Empty Prison - Should handle empty prison state")
    void testEmptyPrison() {
        // Act
        List<PrisonManager.PrisonerData> prisoners = manager.getAllPrisoners();
        PrisonerData data = manager.getPrisonerData(UUID.randomUUID());

        // Assert
        assertThat(prisoners).isEmpty();
        assertThat(data).isNull();
        assertThat(manager.getPrisonerCount()).isEqualTo(0);
        assertThat(manager.isPrisoner(UUID.randomUUID())).isFalse();
    }

    @Test
    @Order(15)
    @DisplayName("Statistics - Should provide prison statistics")
    void testPrisonStatistics() {
        // Arrange
        manager.imprisonPlayer(mockPlayer, 3);

        ServerPlayer p2 = createMockPlayer();
        manager.imprisonPlayer(p2, 2);

        // Act
        int count = manager.getPrisonerCount();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    // Helper method to create mock players
    private ServerPlayer createMockPlayer() {
        ServerPlayer player = mock(ServerPlayer.class);
        UUID id = UUID.randomUUID();
        when(player.getUUID()).thenReturn(id);
        when(player.getName()).thenReturn(net.minecraft.network.chat.Component.literal("Player_" + id.toString().substring(0, 8)));
        when(player.getPersistentData()).thenReturn(new CompoundTag());
        return player;
    }

    @AfterEach
    void tearDown() {
        // Cleanup
        manager.getAllPrisoners().forEach(p -> {
            ServerPlayer player = mock(ServerPlayer.class);
            when(player.getUUID()).thenReturn(p.playerUUID);
            when(player.getPersistentData()).thenReturn(new CompoundTag());
            manager.releasePlayer(player, PrisonManager.ReleaseReason.ADMIN_RELEASE);
        });
    }
}
