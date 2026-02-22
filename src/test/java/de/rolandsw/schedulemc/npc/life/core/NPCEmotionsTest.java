package de.rolandsw.schedulemc.npc.life.core;

import de.rolandsw.schedulemc.npc.life.NPCLifeConstants;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for NPCEmotions (data model)
 *
 * Tests cover pure calculation logic (getPriceModifier, getSocialModifier,
 * wouldFlee, wouldFight, wouldTrade, trigger, hasActiveEmotion, hasStrongEmotion).
 * NBT serialization (save/load) is excluded as it requires Minecraft CompoundTag.
 */
class NPCEmotionsTest {

    private NPCEmotions emotions;

    @BeforeEach
    void setUp() {
        emotions = new NPCEmotions();
    }

    // ==================== Initial State ====================

    @Test
    @DisplayName("Initial state should be NEUTRAL with zero intensity")
    void testInitialState_Neutral() {
        assertThat(emotions.getCurrentEmotion()).isEqualTo(EmotionState.NEUTRAL);
        assertThat(emotions.getIntensity()).isEqualTo(0.0f);
    }

    @Test
    @DisplayName("Initial state should not have an active emotion")
    void testInitialState_NoActiveEmotion() {
        assertThat(emotions.hasActiveEmotion()).isFalse();
    }

    @Test
    @DisplayName("Initial baseline emotion should be NEUTRAL")
    void testInitialBaseline_Neutral() {
        assertThat(emotions.getBaselineEmotion()).isEqualTo(EmotionState.NEUTRAL);
    }

    // ==================== trigger() ====================

    @Test
    @DisplayName("trigger() should set the current emotion and intensity")
    void testTrigger_SetsEmotionAndIntensity() {
        emotions.trigger(EmotionState.HAPPY, 60.0f, 1000);
        assertThat(emotions.getCurrentEmotion()).isEqualTo(EmotionState.HAPPY);
        assertThat(emotions.getIntensity()).isEqualTo(60.0f);
        assertThat(emotions.getRemainingTicks()).isEqualTo(1000);
    }

    @Test
    @DisplayName("trigger() with higher intensity should override current emotion")
    void testTrigger_StrongerEmotionOverrides() {
        emotions.trigger(EmotionState.SAD, 40.0f, 500);
        emotions.trigger(EmotionState.ANGRY, 80.0f, 800);
        assertThat(emotions.getCurrentEmotion()).isEqualTo(EmotionState.ANGRY);
        assertThat(emotions.getIntensity()).isEqualTo(80.0f);
    }

    @Test
    @DisplayName("trigger() with same emotion should extend duration and boost intensity")
    void testTrigger_SameEmotion_ExtendsAndBoosts() {
        emotions.trigger(EmotionState.HAPPY, 50.0f, 500);
        emotions.trigger(EmotionState.HAPPY, 40.0f, 1000);
        // Duration: max(500, 1000) = 1000
        assertThat(emotions.getRemainingTicks()).isEqualTo(1000);
        // Intensity: 50 + 40*0.3 = 50 + 12 = 62
        assertThat(emotions.getIntensity()).isCloseTo(62.0f, within(0.1f));
    }

    @Test
    @DisplayName("trigger() should cap intensity at MAX_INTENSITY (100)")
    void testTrigger_IntensityCapsAtMax() {
        emotions.trigger(EmotionState.ANGRY, 999.0f, 1000);
        assertThat(emotions.getIntensity()).isEqualTo(NPCEmotions.MAX_INTENSITY);
    }

    // ==================== hasActiveEmotion() / hasStrongEmotion() ====================

    @Test
    @DisplayName("hasActiveEmotion should return true when intensity >= ACTIVE_THRESHOLD")
    void testHasActiveEmotion_AboveThreshold_ReturnsTrue() {
        emotions.trigger(EmotionState.FEARFUL, NPCEmotions.ACTIVE_EMOTION_THRESHOLD + 1.0f, 500);
        assertThat(emotions.hasActiveEmotion()).isTrue();
    }

    @Test
    @DisplayName("hasActiveEmotion should return false when emotion is NEUTRAL")
    void testHasActiveEmotion_Neutral_ReturnsFalse() {
        assertThat(emotions.hasActiveEmotion()).isFalse();
    }

    @Test
    @DisplayName("hasStrongEmotion should return true when intensity >= STRONG_THRESHOLD (70)")
    void testHasStrongEmotion_HighIntensity_ReturnsTrue() {
        emotions.trigger(EmotionState.ANGRY, NPCEmotions.STRONG_EMOTION_THRESHOLD, 500);
        assertThat(emotions.hasStrongEmotion()).isTrue();
    }

    @Test
    @DisplayName("hasStrongEmotion should return false when intensity < STRONG_THRESHOLD")
    void testHasStrongEmotion_LowIntensity_ReturnsFalse() {
        emotions.trigger(EmotionState.ANGRY, NPCEmotions.STRONG_EMOTION_THRESHOLD - 1.0f, 500);
        assertThat(emotions.hasStrongEmotion()).isFalse();
    }

    // ==================== getPriceModifier() ====================

    @Test
    @DisplayName("getPriceModifier should return 1.0 when no active emotion")
    void testGetPriceModifier_NoEmotion_ReturnsOne() {
        assertThat(emotions.getPriceModifier()).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("getPriceModifier with HAPPY at 100% intensity returns 0.9 (10% cheaper)")
    void testGetPriceModifier_Happy_Full_Returns0Point9() {
        // HAPPY priceModifier = 0.9, at intensity 100: 1.0 + (0.9 - 1.0) * 1.0 = 0.9
        emotions.trigger(EmotionState.HAPPY, 100.0f, 1000);
        assertThat(emotions.getPriceModifier()).isCloseTo(0.9f, within(0.001f));
    }

    @Test
    @DisplayName("getPriceModifier with FEARFUL at 100% intensity returns 1.5 (50% more expensive)")
    void testGetPriceModifier_Fearful_Full_Returns1Point5() {
        // FEARFUL priceModifier = 1.5, at intensity 100: 1.0 + (1.5 - 1.0) * 1.0 = 1.5
        emotions.trigger(EmotionState.FEARFUL, 100.0f, 1000);
        assertThat(emotions.getPriceModifier()).isCloseTo(1.5f, within(0.001f));
    }

    @Test
    @DisplayName("getPriceModifier scales with intensity (FEARFUL at 50% intensity → ~1.25)")
    void testGetPriceModifier_Fearful_HalfIntensity_Scales() {
        // FEARFUL priceModifier = 1.5, at intensity 50: 1.0 + (1.5 - 1.0) * 0.5 = 1.25
        emotions.trigger(EmotionState.FEARFUL, 50.0f, 1000);
        assertThat(emotions.getPriceModifier()).isCloseTo(1.25f, within(0.001f));
    }

    // ==================== getSocialModifier() ====================

    @Test
    @DisplayName("getSocialModifier should return 1.0 when no active emotion")
    void testGetSocialModifier_NoEmotion_ReturnsOne() {
        assertThat(emotions.getSocialModifier()).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("getSocialModifier with ANGRY at 100% intensity returns 0.3 (70% less talkative)")
    void testGetSocialModifier_Angry_Full_Returns0Point3() {
        // ANGRY socialModifier = 0.3, at intensity 100: 1.0 + (0.3 - 1.0) * 1.0 = 0.3
        emotions.trigger(EmotionState.ANGRY, 100.0f, 1000);
        assertThat(emotions.getSocialModifier()).isCloseTo(0.3f, within(0.001f));
    }

    @Test
    @DisplayName("getSocialModifier with HAPPY at 100% intensity returns 1.3")
    void testGetSocialModifier_Happy_Full_Returns1Point3() {
        // HAPPY socialModifier = 1.3, at intensity 100: 1.0 + (1.3 - 1.0) * 1.0 = 1.3
        emotions.trigger(EmotionState.HAPPY, 100.0f, 1000);
        assertThat(emotions.getSocialModifier()).isCloseTo(1.3f, within(0.001f));
    }

    // ==================== wouldFlee() / wouldFight() / wouldTrade() ====================

    @Test
    @DisplayName("wouldFlee returns true for FEARFUL emotion above flee threshold")
    void testWouldFlee_Fearful_HighIntensity_ReturnsTrue() {
        emotions.trigger(EmotionState.FEARFUL,
            NPCLifeConstants.Emotions.FLEE_INTENSITY_THRESHOLD + 1.0f, 500);
        assertThat(emotions.wouldFlee()).isTrue();
    }

    @Test
    @DisplayName("wouldFlee returns false when intensity is below flee threshold")
    void testWouldFlee_Fearful_LowIntensity_ReturnsFalse() {
        emotions.trigger(EmotionState.FEARFUL,
            NPCLifeConstants.Emotions.FLEE_INTENSITY_THRESHOLD - 1.0f, 500);
        assertThat(emotions.wouldFlee()).isFalse();
    }

    @Test
    @DisplayName("wouldFlee returns false for non-FEARFUL emotion")
    void testWouldFlee_NonFearful_ReturnsFalse() {
        emotions.trigger(EmotionState.ANGRY, 100.0f, 500);
        assertThat(emotions.wouldFlee()).isFalse();
    }

    @Test
    @DisplayName("wouldFight returns true for ANGRY emotion above fight threshold")
    void testWouldFight_Angry_HighIntensity_ReturnsTrue() {
        emotions.trigger(EmotionState.ANGRY,
            NPCLifeConstants.Emotions.FIGHT_INTENSITY_THRESHOLD + 1.0f, 500);
        assertThat(emotions.wouldFight()).isTrue();
    }

    @Test
    @DisplayName("wouldFight returns false for non-ANGRY emotion")
    void testWouldFight_NonAngry_ReturnsFalse() {
        emotions.trigger(EmotionState.FEARFUL, 100.0f, 500);
        assertThat(emotions.wouldFight()).isFalse();
    }

    @Test
    @DisplayName("wouldTrade returns true in neutral state")
    void testWouldTrade_Neutral_ReturnsTrue() {
        assertThat(emotions.wouldTrade()).isTrue();
    }

    // ==================== setBaseline() ====================

    @Test
    @DisplayName("setBaseline changes the baseline emotion")
    void testSetBaseline_ChangesBaseline() {
        emotions.setBaseline(EmotionState.SUSPICIOUS);
        assertThat(emotions.getBaselineEmotion()).isEqualTo(EmotionState.SUSPICIOUS);
    }

    // ==================== reset() ====================

    @Test
    @DisplayName("reset() returns to baseline with zero intensity")
    void testReset_ReturnsToBaseline() {
        emotions.trigger(EmotionState.ANGRY, 80.0f, 1000);
        emotions.reset();
        assertThat(emotions.getCurrentEmotion()).isEqualTo(EmotionState.NEUTRAL);
        assertThat(emotions.getIntensity()).isEqualTo(0.0f);
    }
}
