package de.rolandsw.schedulemc.gang;

import de.rolandsw.schedulemc.gang.mission.GangMission;
import de.rolandsw.schedulemc.gang.mission.MissionTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests für GangMission — Fortschritts-Tracking, Abschluss-Logik und Claim-Mechanismus.
 */
public class GangMissionTest {

    private GangMission mission;

    @BeforeEach
    void setUp() {
        // Nutzt den einfachsten HOURLY-Template mit bekanntem Zielwert
        mission = new GangMission("test-id", MissionTemplate.H_SELL_PRODUCTS,
            "Verkaufe 5 Produkte", 5, 100, 500);
    }

    // ── addProgress ─────────────────────────────────────────────────────────

    @Test
    void testAddProgress_IncrementsProgress() {
        mission.addProgress(2);
        assertThat(mission.getCurrentProgress()).isEqualTo(2);
    }

    @Test
    void testAddProgress_CompletesAtTarget() {
        boolean completed = mission.addProgress(5);
        assertThat(completed).isTrue();
        assertThat(mission.isCompleted()).isTrue();
    }

    @Test
    void testAddProgress_DoesNotExceedTarget() {
        mission.addProgress(10); // Ziel ist 5 → sollte bei 5 bleiben
        assertThat(mission.getCurrentProgress()).isEqualTo(5);
    }

    @Test
    void testAddProgress_IgnoredWhenCompleted() {
        mission.addProgress(5); // abschließen
        boolean result = mission.addProgress(3); // nach Abschluss
        assertThat(result).isFalse();
        assertThat(mission.getCurrentProgress()).isEqualTo(5);
    }

    @Test
    void testAddProgress_PartialProgress_NotCompleted() {
        boolean result = mission.addProgress(3);
        assertThat(result).isFalse();
        assertThat(mission.isCompleted()).isFalse();
    }

    // ── setProgress ─────────────────────────────────────────────────────────

    @Test
    void testSetProgress_SetsAbsoluteValue() {
        mission.setProgress(3);
        assertThat(mission.getCurrentProgress()).isEqualTo(3);
    }

    @Test
    void testSetProgress_CompletesAtTarget() {
        boolean completed = mission.setProgress(5);
        assertThat(completed).isTrue();
        assertThat(mission.isCompleted()).isTrue();
    }

    @Test
    void testSetProgress_CapsAtTarget() {
        mission.setProgress(100);
        assertThat(mission.getCurrentProgress()).isEqualTo(5);
    }

    @Test
    void testSetProgress_IgnoredWhenCompleted() {
        mission.setProgress(5);
        boolean result = mission.setProgress(0);
        assertThat(result).isFalse();
    }

    // ── claim ────────────────────────────────────────────────────────────────

    @Test
    void testClaim_SucceedsWhenCompletedAndUnclaimed() {
        mission.addProgress(5);
        assertThat(mission.claim()).isTrue();
        assertThat(mission.isClaimed()).isTrue();
    }

    @Test
    void testClaim_FailsWhenNotCompleted() {
        assertThat(mission.claim()).isFalse();
        assertThat(mission.isClaimed()).isFalse();
    }

    @Test
    void testClaim_FailsWhenAlreadyClaimed() {
        mission.addProgress(5);
        mission.claim();
        assertThat(mission.claim()).isFalse();
    }

    // ── getProgressPercent ───────────────────────────────────────────────────

    @Test
    void testGetProgressPercent_EmptyMission_Returns0() {
        assertThat(mission.getProgressPercent()).isEqualTo(0.0);
    }

    @Test
    void testGetProgressPercent_HalfDone_Returns0_5() {
        // Ziel = 5, Fortschritt = 2 → aber addProgress(2) gibt 2/5 = 0.4
        // Fortschritt auf genau die Hälfte setzen: 2.5 nicht möglich → nutze 5/2 = 2 bei target 4
        GangMission m = new GangMission("t2", MissionTemplate.H_SELL_PRODUCTS,
            "Test", 4, 50, 200);
        m.addProgress(2);
        assertThat(m.getProgressPercent()).isEqualTo(0.5);
    }

    @Test
    void testGetProgressPercent_Completed_Returns1() {
        mission.addProgress(5);
        assertThat(mission.getProgressPercent()).isEqualTo(1.0);
    }

    // ── isClaimable ──────────────────────────────────────────────────────────

    @Test
    void testIsClaimable_FalseWhenNotCompleted() {
        assertThat(mission.isClaimable()).isFalse();
    }

    @Test
    void testIsClaimable_TrueWhenCompletedAndUnclaimed() {
        mission.addProgress(5);
        assertThat(mission.isClaimable()).isTrue();
    }

    @Test
    void testIsClaimable_FalseAfterClaim() {
        mission.addProgress(5);
        mission.claim();
        assertThat(mission.isClaimable()).isFalse();
    }
}
