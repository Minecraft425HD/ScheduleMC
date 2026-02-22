package de.rolandsw.schedulemc.gang;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests für GangLevelRequirements — XP-Berechnungen, Member-Limits und Territory-Bases.
 */
public class GangLevelRequirementsTest {

    // ── getRequiredXP ───────────────────────────────────────────────────────

    @Test
    void testGetRequiredXP_Level0_Returns0() {
        assertThat(GangLevelRequirements.getRequiredXP(0)).isEqualTo(0);
    }

    @Test
    void testGetRequiredXP_Level1_ReturnsBase() {
        // 200 * 1^1.7 = 200
        assertThat(GangLevelRequirements.getRequiredXP(1)).isEqualTo(200);
    }

    @Test
    void testGetRequiredXP_LevelClamped_NegativeStaysAt0() {
        assertThat(GangLevelRequirements.getRequiredXP(-5)).isEqualTo(0);
    }

    @Test
    void testGetRequiredXP_LevelClamped_AboveMaxStaysAtMax() {
        int atMax = GangLevelRequirements.getRequiredXP(GangLevelRequirements.MAX_LEVEL);
        int aboveMax = GangLevelRequirements.getRequiredXP(GangLevelRequirements.MAX_LEVEL + 10);
        assertThat(aboveMax).isEqualTo(atMax);
    }

    @Test
    void testGetRequiredXP_Increasing() {
        for (int i = 1; i < GangLevelRequirements.MAX_LEVEL; i++) {
            assertThat(GangLevelRequirements.getRequiredXP(i + 1))
                .as("Level %d sollte mehr XP brauchen als Level %d", i + 1, i)
                .isGreaterThan(GangLevelRequirements.getRequiredXP(i));
        }
    }

    // ── getLevelForXP ───────────────────────────────────────────────────────

    @Test
    void testGetLevelForXP_0_Returns0() {
        assertThat(GangLevelRequirements.getLevelForXP(0)).isEqualTo(0);
    }

    @Test
    void testGetLevelForXP_MaxXP_ReturnsMaxLevel() {
        int xpAtMax = GangLevelRequirements.getRequiredXP(GangLevelRequirements.MAX_LEVEL);
        assertThat(GangLevelRequirements.getLevelForXP(xpAtMax))
            .isEqualTo(GangLevelRequirements.MAX_LEVEL);
    }

    @Test
    void testGetLevelForXP_RoundTrip() {
        for (int level = 1; level <= GangLevelRequirements.MAX_LEVEL; level++) {
            int xp = GangLevelRequirements.getRequiredXP(level);
            assertThat(GangLevelRequirements.getLevelForXP(xp))
                .as("getLevelForXP(getRequiredXP(%d)) sollte %d sein", level, level)
                .isEqualTo(level);
        }
    }

    // ── getProgress ─────────────────────────────────────────────────────────

    @Test
    void testGetProgress_AtMaxLevel_Returns1() {
        assertThat(GangLevelRequirements.getProgress(GangLevelRequirements.MAX_LEVEL, 999999))
            .isEqualTo(1.0);
    }

    @Test
    void testGetProgress_NegativeLevel_Returns0() {
        assertThat(GangLevelRequirements.getProgress(-1, 100)).isEqualTo(0.0);
    }

    @Test
    void testGetProgress_NormalRange_IsBetween0And1() {
        double progress = GangLevelRequirements.getProgress(5, 1000);
        assertThat(progress).isBetween(0.0, 1.0);
    }

    // ── getMaxMembers ───────────────────────────────────────────────────────

    @Test
    void testGetMaxMembers_Level0_ReturnsBase() {
        assertThat(GangLevelRequirements.getMaxMembers(0)).isEqualTo(5);
    }

    @Test
    void testGetMaxMembers_Level5_ReturnsIncreased() {
        // extra = (5/5)*3 = 3 → 5+3 = 8
        assertThat(GangLevelRequirements.getMaxMembers(5)).isEqualTo(8);
    }

    @Test
    void testGetMaxMembers_HighLevel_CapsAt20() {
        assertThat(GangLevelRequirements.getMaxMembers(30)).isEqualTo(20);
    }

    // ── getBaseMaxTerritory ─────────────────────────────────────────────────

    @Test
    void testGetBaseMaxTerritory_Level1_Returns1() {
        assertThat(GangLevelRequirements.getBaseMaxTerritory(1)).isEqualTo(1);
    }

    @Test
    void testGetBaseMaxTerritory_Level30_Returns25() {
        assertThat(GangLevelRequirements.getBaseMaxTerritory(30)).isEqualTo(25);
    }

    // ── getAvailablePerkPoints ──────────────────────────────────────────────

    @Test
    void testGetAvailablePerkPoints_Level2_Returns0() {
        assertThat(GangLevelRequirements.getAvailablePerkPoints(2)).isEqualTo(0);
    }

    @Test
    void testGetAvailablePerkPoints_Level5_Returns3() {
        // max(0, 5-2) = 3
        assertThat(GangLevelRequirements.getAvailablePerkPoints(5)).isEqualTo(3);
    }

    @Test
    void testGetAvailablePerkPoints_Level0_Returns0() {
        assertThat(GangLevelRequirements.getAvailablePerkPoints(0)).isEqualTo(0);
    }
}
