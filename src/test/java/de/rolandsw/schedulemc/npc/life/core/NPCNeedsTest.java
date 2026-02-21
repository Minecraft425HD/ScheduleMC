package de.rolandsw.schedulemc.npc.life.core;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for NPCNeeds (data model)
 *
 * Tests cover pure calculation and clamping logic that does NOT require
 * Minecraft/Forge. Methods using Minecraft types (calculateSafety, save/load)
 * are excluded from unit tests.
 */
class NPCNeedsTest {

    private NPCNeeds needs;

    @BeforeEach
    void setUp() {
        needs = new NPCNeeds();
    }

    // ==================== Initial State ====================

    @Test
    @DisplayName("Initial energy should be at maximum (100)")
    void testInitialEnergy() {
        assertThat(needs.getEnergy()).isEqualTo(NPCNeeds.MAX_VALUE);
    }

    @Test
    @DisplayName("Initial safety should be at maximum (100)")
    void testInitialSafety() {
        assertThat(needs.getSafety()).isEqualTo(NPCNeeds.MAX_VALUE);
    }

    @Test
    @DisplayName("Initial state should not be sleeping")
    void testInitialNotSleeping() {
        assertThat(needs.isSleeping()).isFalse();
    }

    // ==================== satisfy() ====================

    @Test
    @DisplayName("satisfy(ENERGY) should increase energy by the given amount")
    void testSatisfy_Energy_Increases() {
        needs.setEnergy(50.0f);
        needs.satisfy(NeedType.ENERGY, 20.0f);
        assertThat(needs.getEnergy()).isEqualTo(70.0f);
    }

    @Test
    @DisplayName("satisfy(ENERGY) should cap at MAX_VALUE (100)")
    void testSatisfy_Energy_CapsAtMax() {
        needs.setEnergy(95.0f);
        needs.satisfy(NeedType.ENERGY, 20.0f);
        assertThat(needs.getEnergy()).isEqualTo(NPCNeeds.MAX_VALUE);
    }

    @Test
    @DisplayName("satisfy(SAFETY) should increase safety by the given amount")
    void testSatisfy_Safety_Increases() {
        needs.setSafety(30.0f);
        needs.satisfy(NeedType.SAFETY, 15.0f);
        assertThat(needs.getSafety()).isEqualTo(45.0f);
    }

    @Test
    @DisplayName("satisfy(SAFETY) should cap at MAX_VALUE (100)")
    void testSatisfy_Safety_CapsAtMax() {
        needs.setSafety(90.0f);
        needs.satisfy(NeedType.SAFETY, 50.0f);
        assertThat(needs.getSafety()).isEqualTo(NPCNeeds.MAX_VALUE);
    }

    // ==================== reduce() ====================

    @Test
    @DisplayName("reduce(ENERGY) should decrease energy by the given amount")
    void testReduce_Energy_Decreases() {
        needs.setEnergy(80.0f);
        needs.reduce(NeedType.ENERGY, 30.0f);
        assertThat(needs.getEnergy()).isEqualTo(50.0f);
    }

    @Test
    @DisplayName("reduce(ENERGY) should floor at MIN_VALUE (0)")
    void testReduce_Energy_FloorsAtMin() {
        needs.setEnergy(10.0f);
        needs.reduce(NeedType.ENERGY, 50.0f);
        assertThat(needs.getEnergy()).isEqualTo(NPCNeeds.MIN_VALUE);
    }

    @Test
    @DisplayName("reduce(SAFETY) should floor at MIN_VALUE (0)")
    void testReduce_Safety_FloorsAtMin() {
        needs.setSafety(5.0f);
        needs.reduce(NeedType.SAFETY, 100.0f);
        assertThat(needs.getSafety()).isEqualTo(NPCNeeds.MIN_VALUE);
    }

    // ==================== isCritical() / isLow() ====================

    @Test
    @DisplayName("isCritical should return true when value is below CRITICAL_THRESHOLD (20)")
    void testIsCritical_BelowThreshold_ReturnsTrue() {
        needs.setEnergy(NPCNeeds.CRITICAL_THRESHOLD - 1.0f);
        assertThat(needs.isCritical(NeedType.ENERGY)).isTrue();
    }

    @Test
    @DisplayName("isCritical should return false when value equals CRITICAL_THRESHOLD")
    void testIsCritical_AtThreshold_ReturnsFalse() {
        needs.setEnergy(NPCNeeds.CRITICAL_THRESHOLD);
        assertThat(needs.isCritical(NeedType.ENERGY)).isFalse();
    }

    @Test
    @DisplayName("isLow should return true when value is below LOW_THRESHOLD (40)")
    void testIsLow_BelowThreshold_ReturnsTrue() {
        needs.setSafety(NPCNeeds.LOW_THRESHOLD - 1.0f);
        assertThat(needs.isLow(NeedType.SAFETY)).isTrue();
    }

    @Test
    @DisplayName("isLow should return false when value equals LOW_THRESHOLD")
    void testIsLow_AtThreshold_ReturnsFalse() {
        needs.setSafety(NPCNeeds.LOW_THRESHOLD);
        assertThat(needs.isLow(NeedType.SAFETY)).isFalse();
    }

    // ==================== hasCriticalNeed() ====================

    @Test
    @DisplayName("hasCriticalNeed should return true when energy is critical")
    void testHasCriticalNeed_EnergyCritical_ReturnsTrue() {
        needs.setEnergy(5.0f);
        needs.setSafety(80.0f);
        assertThat(needs.hasCriticalNeed()).isTrue();
    }

    @Test
    @DisplayName("hasCriticalNeed should return true when safety is critical")
    void testHasCriticalNeed_SafetyCritical_ReturnsTrue() {
        needs.setEnergy(80.0f);
        needs.setSafety(10.0f);
        assertThat(needs.hasCriticalNeed()).isTrue();
    }

    @Test
    @DisplayName("hasCriticalNeed should return false when all needs are above threshold")
    void testHasCriticalNeed_AllGood_ReturnsFalse() {
        needs.setEnergy(80.0f);
        needs.setSafety(80.0f);
        assertThat(needs.hasCriticalNeed()).isFalse();
    }

    // ==================== getMostCritical() ====================

    @Test
    @DisplayName("getMostCritical returns null when both needs are above LOW_THRESHOLD")
    void testGetMostCritical_AllSatisfied_ReturnsNull() {
        needs.setEnergy(80.0f);
        needs.setSafety(80.0f);
        assertThat(needs.getMostCritical()).isNull();
    }

    @Test
    @DisplayName("getMostCritical returns ENERGY when energy is lower and below LOW_THRESHOLD")
    void testGetMostCritical_EnergyLowest_ReturnsEnergy() {
        needs.setEnergy(10.0f);
        needs.setSafety(30.0f);
        assertThat(needs.getMostCritical()).isEqualTo(NeedType.ENERGY);
    }

    @Test
    @DisplayName("getMostCritical returns SAFETY when safety is lower and below LOW_THRESHOLD")
    void testGetMostCritical_SafetyLowest_ReturnsSafety() {
        needs.setEnergy(35.0f);
        needs.setSafety(5.0f);
        assertThat(needs.getMostCritical()).isEqualTo(NeedType.SAFETY);
    }

    // ==================== getOverallSatisfaction() ====================

    @Test
    @DisplayName("getOverallSatisfaction returns average of energy and safety")
    void testGetOverallSatisfaction_Average() {
        needs.setEnergy(80.0f);
        needs.setSafety(40.0f);
        assertThat(needs.getOverallSatisfaction()).isEqualTo(60.0f);
    }

    @Test
    @DisplayName("getOverallSatisfaction returns 100 when fully satisfied")
    void testGetOverallSatisfaction_Full() {
        // Both default to MAX_VALUE
        assertThat(needs.getOverallSatisfaction()).isEqualTo(NPCNeeds.MAX_VALUE);
    }

    // ==================== setEnergy() / setSafety() clamping ====================

    @Test
    @DisplayName("setEnergy clamps values above MAX_VALUE")
    void testSetEnergy_ClampsBelowMax() {
        needs.setEnergy(9999.0f);
        assertThat(needs.getEnergy()).isEqualTo(NPCNeeds.MAX_VALUE);
    }

    @Test
    @DisplayName("setEnergy clamps values below MIN_VALUE")
    void testSetEnergy_ClampsAboveMin() {
        needs.setEnergy(-99.0f);
        assertThat(needs.getEnergy()).isEqualTo(NPCNeeds.MIN_VALUE);
    }

    @Test
    @DisplayName("setSafety clamps values above MAX_VALUE")
    void testSetSafety_ClampsBelowMax() {
        needs.setSafety(999.0f);
        assertThat(needs.getSafety()).isEqualTo(NPCNeeds.MAX_VALUE);
    }

    // ==================== Sleeping ====================

    @Test
    @DisplayName("startSleeping should set sleeping state to true")
    void testSleeping_Start() {
        needs.startSleeping();
        assertThat(needs.isSleeping()).isTrue();
    }

    @Test
    @DisplayName("stopSleeping should set sleeping state to false")
    void testSleeping_Stop() {
        needs.startSleeping();
        needs.stopSleeping();
        assertThat(needs.isSleeping()).isFalse();
    }

    // ==================== getValue() ====================

    @Test
    @DisplayName("getValue(ENERGY) returns current energy")
    void testGetValue_Energy() {
        needs.setEnergy(42.5f);
        assertThat(needs.getValue(NeedType.ENERGY)).isEqualTo(42.5f);
    }

    @Test
    @DisplayName("getValue(SAFETY) returns current safety")
    void testGetValue_Safety() {
        needs.setSafety(77.0f);
        assertThat(needs.getValue(NeedType.SAFETY)).isEqualTo(77.0f);
    }
}
