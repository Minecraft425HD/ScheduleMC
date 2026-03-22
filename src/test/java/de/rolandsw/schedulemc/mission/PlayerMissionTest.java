package de.rolandsw.schedulemc.mission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests für PlayerMission — Fortschritts-Tracking, Abschluss-Logik und Claim-Mechanismus.
 * Reine Java-Klassen ohne Minecraft-Abhängigkeiten — kein Bootstrap nötig.
 */
public class PlayerMissionTest {

    private static final int TARGET = 10;

    private MissionDefinition definition;
    private PlayerMission mission;
    private UUID playerUUID;

    @BeforeEach
    void setUp() {
        definition = new MissionDefinition(
            "test-mission",
            "Testmission",
            "Liefere 10 Produkte",
            MissionCategory.HAUPT,
            500,    // xpReward
            1000,   // moneyReward
            TARGET, // targetAmount
            "deliver_products"
        );
        playerUUID = UUID.randomUUID();
        mission = new PlayerMission("pm-1", definition, playerUUID);
    }

    // ── Initialer Zustand ─────────────────────────────────────────────────────

    @Test
    void testInitialState_StatusIsActive() {
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.ACTIVE);
    }

    @Test
    void testInitialState_ProgressIsZero() {
        assertThat(mission.getCurrentProgress()).isEqualTo(0);
    }

    @Test
    void testInitialState_IsNotClaimable() {
        assertThat(mission.isClaimable()).isFalse();
    }

    @Test
    void testInitialState_ProgressPercentIsZero() {
        assertThat(mission.getProgressPercent()).isEqualTo(0.0);
    }

    // ── addProgress ──────────────────────────────────────────────────────────

    @Test
    void testAddProgress_IncrementsCorrectly() {
        mission.addProgress(3);
        assertThat(mission.getCurrentProgress()).isEqualTo(3);
    }

    @Test
    void testAddProgress_AccumulatesMultipleCalls() {
        mission.addProgress(4);
        mission.addProgress(3);
        assertThat(mission.getCurrentProgress()).isEqualTo(7);
    }

    @Test
    void testAddProgress_ReturnsFalseWhenNotComplete() {
        boolean completed = mission.addProgress(5);
        assertThat(completed).isFalse();
    }

    @Test
    void testAddProgress_ReturnsTrueWhenTargetReached() {
        boolean completed = mission.addProgress(TARGET);
        assertThat(completed).isTrue();
    }

    @Test
    void testAddProgress_StatusBecomesCompleted() {
        mission.addProgress(TARGET);
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
    }

    @Test
    void testAddProgress_CapsProgressAtTarget() {
        mission.addProgress(TARGET * 3);
        assertThat(mission.getCurrentProgress()).isEqualTo(TARGET);
    }

    @Test
    void testAddProgress_IgnoredAfterCompletion() {
        mission.addProgress(TARGET);
        boolean result = mission.addProgress(5);
        assertThat(result).isFalse();
        assertThat(mission.getCurrentProgress()).isEqualTo(TARGET);
    }

    @Test
    void testAddProgress_SetsCompletedAt() {
        long before = System.currentTimeMillis();
        mission.addProgress(TARGET);
        long after = System.currentTimeMillis();
        assertThat(mission.getCompletedAt()).isBetween(before, after);
    }

    @Test
    void testAddProgress_CompletedAtIsZeroBeforeCompletion() {
        mission.addProgress(5);
        assertThat(mission.getCompletedAt()).isEqualTo(0L);
    }

    // ── setProgress ──────────────────────────────────────────────────────────

    @Test
    void testSetProgress_SetsAbsoluteValue() {
        mission.setProgress(7);
        assertThat(mission.getCurrentProgress()).isEqualTo(7);
    }

    @Test
    void testSetProgress_CapsAtTarget() {
        mission.setProgress(TARGET + 100);
        assertThat(mission.getCurrentProgress()).isEqualTo(TARGET);
    }

    @Test
    void testSetProgress_ReturnsTrueWhenTargetReached() {
        boolean completed = mission.setProgress(TARGET);
        assertThat(completed).isTrue();
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
    }

    @Test
    void testSetProgress_ReturnsFalseWhenBelowTarget() {
        boolean completed = mission.setProgress(TARGET - 1);
        assertThat(completed).isFalse();
    }

    @Test
    void testSetProgress_IgnoredAfterCompletion() {
        mission.setProgress(TARGET);
        boolean result = mission.setProgress(0);
        assertThat(result).isFalse();
        assertThat(mission.getCurrentProgress()).isEqualTo(TARGET);
    }

    @Test
    void testSetProgress_IgnoredWhenClaimed() {
        mission.addProgress(TARGET);
        mission.claim();
        boolean result = mission.setProgress(0);
        assertThat(result).isFalse();
        assertThat(mission.getCurrentProgress()).isEqualTo(TARGET);
    }

    // ── claim ─────────────────────────────────────────────────────────────────

    @Test
    void testClaim_SucceedsWhenCompleted() {
        mission.addProgress(TARGET);
        boolean result = mission.claim();
        assertThat(result).isTrue();
    }

    @Test
    void testClaim_StatusBecomesClaimed() {
        mission.addProgress(TARGET);
        mission.claim();
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.CLAIMED);
    }

    @Test
    void testClaim_FailsWhenNotCompleted() {
        boolean result = mission.claim();
        assertThat(result).isFalse();
    }

    @Test
    void testClaim_FailsWhenAlreadyClaimed() {
        mission.addProgress(TARGET);
        mission.claim();
        boolean result = mission.claim();
        assertThat(result).isFalse();
    }

    @Test
    void testClaim_IsNotClaimableAfterClaim() {
        mission.addProgress(TARGET);
        mission.claim();
        assertThat(mission.isClaimable()).isFalse();
    }

    @Test
    void testClaim_SetsClaimedAt() {
        mission.addProgress(TARGET);
        long before = System.currentTimeMillis();
        mission.claim();
        long after = System.currentTimeMillis();
        assertThat(mission.getClaimedAt()).isBetween(before, after);
    }

    // ── getProgressPercent ───────────────────────────────────────────────────

    @Test
    void testGetProgressPercent_ZeroProgress_ReturnsZero() {
        assertThat(mission.getProgressPercent()).isEqualTo(0.0);
    }

    @Test
    void testGetProgressPercent_HalfProgress_Returns0_5() {
        mission.addProgress(TARGET / 2);
        assertThat(mission.getProgressPercent()).isEqualTo(0.5);
    }

    @Test
    void testGetProgressPercent_FullProgress_Returns1_0() {
        mission.addProgress(TARGET);
        assertThat(mission.getProgressPercent()).isEqualTo(1.0);
    }

    @Test
    void testGetProgressPercent_ZeroTarget_ReturnsZero() {
        MissionDefinition zeroTargetDef = new MissionDefinition(
            "zero-target", "Zero", "Zero target mission",
            MissionCategory.NEBEN, 0, 0, 0, "key"
        );
        PlayerMission zeroMission = new PlayerMission("z-1", zeroTargetDef, playerUUID);
        assertThat(zeroMission.getProgressPercent()).isEqualTo(0.0);
    }

    // ── isClaimable ──────────────────────────────────────────────────────────

    @Test
    void testIsClaimable_FalseWhenActive() {
        assertThat(mission.isClaimable()).isFalse();
    }

    @Test
    void testIsClaimable_TrueWhenCompleted() {
        mission.addProgress(TARGET);
        assertThat(mission.isClaimable()).isTrue();
    }

    @Test
    void testIsClaimable_FalseWhenClaimed() {
        mission.addProgress(TARGET);
        mission.claim();
        assertThat(mission.isClaimable()).isFalse();
    }

    // ── Deserialisierungs-Konstruktor ─────────────────────────────────────────

    @Test
    void testDeserializationConstructor_RestoresState() {
        long now = System.currentTimeMillis();
        PlayerMission restored = new PlayerMission(
            "pm-restored", "test-mission", definition,
            playerUUID, 7, MissionStatus.ACTIVE,
            now - 1000, 0L, 0L
        );
        assertThat(restored.getMissionId()).isEqualTo("pm-restored");
        assertThat(restored.getCurrentProgress()).isEqualTo(7);
        assertThat(restored.getStatus()).isEqualTo(MissionStatus.ACTIVE);
        assertThat(restored.getPlayerUUID()).isEqualTo(playerUUID);
    }

    @Test
    void testDeserializationConstructor_CompletedState_IsClaimable() {
        long now = System.currentTimeMillis();
        PlayerMission completed = new PlayerMission(
            "pm-done", "test-mission", definition,
            playerUUID, TARGET, MissionStatus.COMPLETED,
            now - 5000, now - 1000, 0L
        );
        assertThat(completed.isClaimable()).isTrue();
        assertThat(completed.getCompletedAt()).isEqualTo(now - 1000);
    }
}
