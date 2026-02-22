package de.rolandsw.schedulemc.vehicle;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Sanity-Tests für VehicleConstants — stellt sicher, dass alle Konstanten
 * sinnvolle Werte haben und keine versehentlichen Regressionen auftreten.
 */
public class VehicleConstantsTest {

    // ── Physics & Collision ──────────────────────────────────────────────────

    @Test
    void testMinDamageSpeed_IsPositive() {
        assertThat(VehicleConstants.MIN_DAMAGE_SPEED).isGreaterThan(0F);
    }

    @Test
    void testDamageThresholds_AreOrdered() {
        assertThat(VehicleConstants.DAMAGE_THRESHOLD_LOW)
            .isLessThan(VehicleConstants.DAMAGE_THRESHOLD_MEDIUM);
        assertThat(VehicleConstants.DAMAGE_THRESHOLD_MEDIUM)
            .isLessThan(VehicleConstants.DAMAGE_THRESHOLD_HIGH);
        assertThat(VehicleConstants.DAMAGE_THRESHOLD_HIGH)
            .isLessThan(VehicleConstants.DAMAGE_THRESHOLD_CRITICAL);
    }

    @Test
    void testCollisionThresholds_AreBetween0And1() {
        assertThat(VehicleConstants.COLLISION_DAMAGE_THRESHOLD)
            .isBetween(0F, 1F);
        assertThat(VehicleConstants.COLLISION_ENGINE_STOP_THRESHOLD)
            .isBetween(0F, 1F);
        assertThat(VehicleConstants.COLLISION_DAMAGE_THRESHOLD)
            .isLessThan(VehicleConstants.COLLISION_ENGINE_STOP_THRESHOLD);
    }

    @Test
    void testMaxDamage_Is100() {
        assertThat(VehicleConstants.MAX_DAMAGE).isEqualTo(100F);
    }

    // ── Temperature ─────────────────────────────────────────────────────────

    @Test
    void testTemperatureTargets_ArePositive() {
        assertThat(VehicleConstants.TEMP_HOT_ENGINE_TARGET).isGreaterThan(0F);
        assertThat(VehicleConstants.TEMP_COLD_ENGINE_TARGET).isGreaterThan(0F);
    }

    @Test
    void testTempRateMax_IsPositive() {
        assertThat(VehicleConstants.TEMP_RATE_MAX).isGreaterThan(0);
    }

    // ── Horn & Fleet ─────────────────────────────────────────────────────────

    @Test
    void testHornRadius_IsPositive() {
        assertThat(VehicleConstants.HORN_FLEE_RADIUS).isGreaterThan(0.0);
    }

    @Test
    void testFleeDistance_IsPositive() {
        assertThat(VehicleConstants.FLEE_DISTANCE).isGreaterThan(0.0);
    }

    // ── Defaults (neu) ───────────────────────────────────────────────────────

    @Test
    void testDefaultTankSize_IsPositive() {
        assertThat(VehicleConstants.DEFAULT_TANK_SIZE_MB).isGreaterThan(0);
    }

    @Test
    void testDefaultRotationModifier_IsBetween0And1() {
        assertThat(VehicleConstants.DEFAULT_ROTATION_MODIFIER)
            .isGreaterThan(0F)
            .isLessThanOrEqualTo(1F);
    }

    @Test
    void testDefaultPlayerYOffset_IsPositive() {
        assertThat(VehicleConstants.DEFAULT_PLAYER_Y_OFFSET).isGreaterThan(0.0);
    }
}
