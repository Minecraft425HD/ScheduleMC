package de.rolandsw.schedulemc.economy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

/**
 * Unit Tests für Memory Cleanup Manager
 */
public class MemoryCleanupManagerTest {

    @BeforeEach
    public void setUp() {
        MemoryCleanupManager.reset();
    }

    @Test
    public void testMarkPlayerOffline() {
        UUID player = UUID.randomUUID();

        MemoryCleanupManager.markPlayerOffline(player);

        assertThat(MemoryCleanupManager.getOfflinePlayerCount()).isEqualTo(1);
    }

    @Test
    public void testMarkPlayerOnline_RemovesFromTracking() {
        UUID player = UUID.randomUUID();

        MemoryCleanupManager.markPlayerOffline(player);
        assertThat(MemoryCleanupManager.getOfflinePlayerCount()).isEqualTo(1);

        MemoryCleanupManager.markPlayerOnline(player);
        assertThat(MemoryCleanupManager.getOfflinePlayerCount()).isEqualTo(0);
    }

    @Test
    public void testMultiplePlayers() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();

        MemoryCleanupManager.markPlayerOffline(player1);
        MemoryCleanupManager.markPlayerOffline(player2);
        MemoryCleanupManager.markPlayerOffline(player3);

        assertThat(MemoryCleanupManager.getOfflinePlayerCount()).isEqualTo(3);

        MemoryCleanupManager.markPlayerOnline(player2);

        assertThat(MemoryCleanupManager.getOfflinePlayerCount()).isEqualTo(2);
    }

    @Test
    public void testReset() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        MemoryCleanupManager.markPlayerOffline(player1);
        MemoryCleanupManager.markPlayerOffline(player2);

        assertThat(MemoryCleanupManager.getOfflinePlayerCount()).isEqualTo(2);

        MemoryCleanupManager.reset();

        assertThat(MemoryCleanupManager.getOfflinePlayerCount()).isEqualTo(0);
    }

    @Test
    public void testMarkOnline_NonExistentPlayer_DoesNotThrow() {
        UUID player = UUID.randomUUID();

        // Should not throw exception
        MemoryCleanupManager.markPlayerOnline(player);

        assertThat(MemoryCleanupManager.getOfflinePlayerCount()).isEqualTo(0);
    }
}
