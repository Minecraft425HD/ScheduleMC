package de.rolandsw.schedulemc.npc.crime;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CrimeManager
 *
 * Tests cover:
 * - Static helper: getEscapeDuration() — pure switch expression
 * - Wanted level operations: add (with clamping), get, set, clear, decay
 * - Escape timer: getEscapeTimeRemaining()
 *
 * Note: Methods that interact with BountyManager or file I/O are covered
 * at a basic level (addWantedLevel with BountyManager in try-catch is safe
 * to call in unit tests; it swallows the BountyManager lookup failure).
 */
class CrimeManagerTest {

    @BeforeEach
    void clearState() throws Exception {
        // Reset all static maps between tests via reflection
        clearMap("wantedLevels");
        clearMap("lastCrimeDay");
        clearMap("escapeTimers");
        clearMap("crimeHistory");
    }

    private void clearMap(String fieldName) throws Exception {
        Field field = CrimeManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    // ==================== getEscapeDuration() ====================

    @Test
    @DisplayName("Level 1 escape duration should be 20 seconds (400 ticks)")
    void testEscapeDuration_Level1() {
        assertThat(CrimeManager.getEscapeDuration(1)).isEqualTo(20 * 20L);
    }

    @Test
    @DisplayName("Level 2 escape duration should be 40 seconds (800 ticks)")
    void testEscapeDuration_Level2() {
        assertThat(CrimeManager.getEscapeDuration(2)).isEqualTo(40 * 20L);
    }

    @Test
    @DisplayName("Level 3 escape duration should be 60 seconds (1200 ticks)")
    void testEscapeDuration_Level3() {
        assertThat(CrimeManager.getEscapeDuration(3)).isEqualTo(60 * 20L);
    }

    @Test
    @DisplayName("Level 4 escape duration should be 90 seconds (1800 ticks)")
    void testEscapeDuration_Level4() {
        assertThat(CrimeManager.getEscapeDuration(4)).isEqualTo(90 * 20L);
    }

    @Test
    @DisplayName("Level 5 escape duration should be 120 seconds (2400 ticks)")
    void testEscapeDuration_Level5() {
        assertThat(CrimeManager.getEscapeDuration(5)).isEqualTo(120 * 20L);
    }

    @Test
    @DisplayName("Unknown level escape duration should fall back to BASE_ESCAPE_DURATION")
    void testEscapeDuration_UnknownLevel_ReturnsBase() {
        assertThat(CrimeManager.getEscapeDuration(0))
            .isEqualTo(CrimeManager.BASE_ESCAPE_DURATION);
        assertThat(CrimeManager.getEscapeDuration(99))
            .isEqualTo(CrimeManager.BASE_ESCAPE_DURATION);
    }

    // ==================== getWantedLevel() ====================

    @Test
    @DisplayName("Player with no record should have wanted level 0")
    void testGetWantedLevel_NoRecord_ReturnsZero() {
        UUID player = UUID.randomUUID();
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(0);
    }

    // ==================== addWantedLevel() ====================

    @Test
    @DisplayName("addWantedLevel should increase wanted level by given amount")
    void testAddWantedLevel_Accumulates() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 2, 0L);
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(2);
    }

    @Test
    @DisplayName("addWantedLevel should clamp at MAX_WANTED_LEVEL (5)")
    void testAddWantedLevel_ClampsAtMax() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 10, 0L);
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(5);
    }

    @Test
    @DisplayName("addWantedLevel multiple times accumulates until clamped")
    void testAddWantedLevel_MultipleAccumulates() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 2, 0L);
        CrimeManager.addWantedLevel(player, 2, 0L);
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(4);
    }

    // ==================== setWantedLevel() ====================

    @Test
    @DisplayName("setWantedLevel should set wanted level directly")
    void testSetWantedLevel_SetsLevel() {
        UUID player = UUID.randomUUID();
        CrimeManager.setWantedLevel(player, 3);
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(3);
    }

    @Test
    @DisplayName("setWantedLevel with 0 should clear the player's record")
    void testSetWantedLevel_ZeroClears() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 2, 0L);
        CrimeManager.setWantedLevel(player, 0);
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(0);
    }

    @Test
    @DisplayName("setWantedLevel should clamp at MAX_WANTED_LEVEL (5)")
    void testSetWantedLevel_ClampsAtMax() {
        UUID player = UUID.randomUUID();
        CrimeManager.setWantedLevel(player, 99);
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(5);
    }

    // ==================== clearWantedLevel() ====================

    @Test
    @DisplayName("clearWantedLevel should remove player from all records")
    void testClearWantedLevel_RemovesAll() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 3, 5L);
        CrimeManager.clearWantedLevel(player);
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(0);
    }

    // ==================== decayWantedLevel() ====================

    @Test
    @DisplayName("decayWantedLevel should reduce wanted level by daysPassed × 2")
    void testDecayWantedLevel_ReducesByDays() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 5, 0L); // level = 5 at day 0
        CrimeManager.decayWantedLevel(player, 2L);   // 2 days → decay = 4
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(1); // 5 - 4 = 1
    }

    @Test
    @DisplayName("decayWantedLevel should clear player when level reaches 0")
    void testDecayWantedLevel_ClearsWhenReachesZero() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 2, 0L);
        CrimeManager.decayWantedLevel(player, 5L); // 5 days → decay = 10, clamped at max
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(0);
    }

    @Test
    @DisplayName("decayWantedLevel should do nothing if player has no crime record")
    void testDecayWantedLevel_NoRecord_NoOp() {
        UUID player = UUID.randomUUID();
        // Should not throw
        CrimeManager.decayWantedLevel(player, 5L);
        assertThat(CrimeManager.getWantedLevel(player)).isEqualTo(0);
    }

    // ==================== Escape Timer ====================

    @Test
    @DisplayName("getEscapeTimeRemaining should return 0 when no timer is active")
    void testEscapeTimeRemaining_NoTimer_ReturnsZero() {
        UUID player = UUID.randomUUID();
        assertThat(CrimeManager.getEscapeTimeRemaining(player, 1000L)).isEqualTo(0L);
    }

    @Test
    @DisplayName("isHiding returns false before escape timer is started")
    void testIsHiding_NoTimer_ReturnsFalse() {
        UUID player = UUID.randomUUID();
        assertThat(CrimeManager.isHiding(player)).isFalse();
    }

    @Test
    @DisplayName("isHiding returns true after escape timer is started")
    void testIsHiding_AfterStart_ReturnsTrue() {
        UUID player = UUID.randomUUID();
        CrimeManager.startEscapeTimer(player, 100L);
        assertThat(CrimeManager.isHiding(player)).isTrue();
    }

    @Test
    @DisplayName("stopEscapeTimer removes the timer")
    void testStopEscapeTimer_RemovesTimer() {
        UUID player = UUID.randomUUID();
        CrimeManager.startEscapeTimer(player, 100L);
        CrimeManager.stopEscapeTimer(player);
        assertThat(CrimeManager.isHiding(player)).isFalse();
    }

    @Test
    @DisplayName("getEscapeTimeRemaining reflects elapsed ticks correctly for level 1")
    void testEscapeTimeRemaining_Level1_ReflectsElapsed() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 1, 0L);      // level = 1
        CrimeManager.startEscapeTimer(player, 0L);        // started at tick 0
        long duration = CrimeManager.getEscapeDuration(1); // 400 ticks
        long elapsed = 100L;
        long remaining = CrimeManager.getEscapeTimeRemaining(player, elapsed);
        assertThat(remaining).isEqualTo(duration - elapsed);
    }

    @Test
    @DisplayName("getEscapeTimeRemaining returns 0 when time has expired")
    void testEscapeTimeRemaining_Expired_ReturnsZero() {
        UUID player = UUID.randomUUID();
        CrimeManager.addWantedLevel(player, 1, 0L);
        CrimeManager.startEscapeTimer(player, 0L);
        long duration = CrimeManager.getEscapeDuration(1);
        // Tick is past duration
        assertThat(CrimeManager.getEscapeTimeRemaining(player, duration + 1000L)).isEqualTo(0L);
    }
}
