package de.rolandsw.schedulemc.tobacco;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests für TobaccoQuality — Upgrade/Downgrade-Ketten, Preis-Multiplikatoren und fromLevel-Lookup.
 * Reine Java-Enum-Klasse ohne Minecraft-Abhängigkeiten (getDisplayName/getDescription ausgenommen).
 */
public class TobaccoQualityTest {

    // ── Level-Werte ───────────────────────────────────────────────────────────

    @Test
    void testLevels_AreInAscendingOrder() {
        assertThat(TobaccoQuality.POOR.getLevel()).isEqualTo(0);
        assertThat(TobaccoQuality.GOOD.getLevel()).isEqualTo(1);
        assertThat(TobaccoQuality.VERY_GOOD.getLevel()).isEqualTo(2);
        assertThat(TobaccoQuality.LEGENDARY.getLevel()).isEqualTo(3);
    }

    // ── Preis-Multiplikatoren ─────────────────────────────────────────────────

    @Test
    void testPriceMultiplier_POOR_IsBelowOne() {
        assertThat(TobaccoQuality.POOR.getPriceMultiplier()).isLessThan(1.0);
    }

    @Test
    void testPriceMultiplier_GUT_IsOne() {
        assertThat(TobaccoQuality.GOOD.getPriceMultiplier()).isEqualTo(1.0);
    }

    @Test
    void testPriceMultiplier_VERY_GOOD_IsAboveOne() {
        assertThat(TobaccoQuality.VERY_GOOD.getPriceMultiplier()).isGreaterThan(1.0);
    }

    @Test
    void testPriceMultiplier_LEGENDARY_IsHighest() {
        assertThat(TobaccoQuality.LEGENDARY.getPriceMultiplier())
            .isGreaterThan(TobaccoQuality.VERY_GOOD.getPriceMultiplier());
    }

    @Test
    void testPriceMultiplier_MatchesYieldMultiplier() {
        for (TobaccoQuality q : TobaccoQuality.values()) {
            assertThat(q.getPriceMultiplier())
                .as("getPriceMultiplier() should equal getYieldMultiplier() for %s", q)
                .isEqualTo(q.getYieldMultiplier());
        }
    }

    @Test
    void testPriceMultiplier_IncreasesByQuality() {
        double prev = -1;
        for (TobaccoQuality q : TobaccoQuality.values()) {
            assertThat(q.getPriceMultiplier())
                .as("Price multiplier should increase with quality: %s", q)
                .isGreaterThan(prev);
            prev = q.getPriceMultiplier();
        }
    }

    // ── upgrade() ─────────────────────────────────────────────────────────────

    @Test
    void testUpgrade_SCHLECHT_BecomesGUT() {
        assertThat(TobaccoQuality.POOR.upgrade()).isEqualTo(TobaccoQuality.GOOD);
    }

    @Test
    void testUpgrade_GUT_BeomesSEHR_GUT() {
        assertThat(TobaccoQuality.GOOD.upgrade()).isEqualTo(TobaccoQuality.VERY_GOOD);
    }

    @Test
    void testUpgrade_SEHR_GUT_BecomesLEGENDAER() {
        assertThat(TobaccoQuality.VERY_GOOD.upgrade()).isEqualTo(TobaccoQuality.LEGENDARY);
    }

    @Test
    void testUpgrade_LEGENDAER_StaysLEGENDAER() {
        assertThat(TobaccoQuality.LEGENDARY.upgrade()).isEqualTo(TobaccoQuality.LEGENDARY);
    }

    @Test
    void testUpgrade_IncreasesLevel() {
        for (TobaccoQuality q : new TobaccoQuality[]{
                TobaccoQuality.POOR, TobaccoQuality.GOOD, TobaccoQuality.VERY_GOOD}) {
            assertThat(q.upgrade().getLevel())
                .as("upgrade() should increase level for %s", q)
                .isGreaterThan(q.getLevel());
        }
    }

    // ── downgrade() ───────────────────────────────────────────────────────────

    @Test
    void testDowngrade_POOR_StaysPOOR() {
        assertThat(TobaccoQuality.POOR.downgrade()).isEqualTo(TobaccoQuality.POOR);
    }

    @Test
    void testDowngrade_GOOD_BecomesPOOR() {
        assertThat(TobaccoQuality.GOOD.downgrade()).isEqualTo(TobaccoQuality.POOR);
    }

    @Test
    void testDowngrade_VERY_GOOD_BecomesGOOD() {
        assertThat(TobaccoQuality.VERY_GOOD.downgrade()).isEqualTo(TobaccoQuality.GOOD);
    }

    @Test
    void testDowngrade_LEGENDARY_BecomesVERY_GOOD() {
        assertThat(TobaccoQuality.LEGENDARY.downgrade()).isEqualTo(TobaccoQuality.VERY_GOOD);
    }

    @Test
    void testDowngrade_DecreasesLevel() {
        for (TobaccoQuality q : new TobaccoQuality[]{
                TobaccoQuality.GOOD, TobaccoQuality.VERY_GOOD, TobaccoQuality.LEGENDARY}) {
            assertThat(q.downgrade().getLevel())
                .as("downgrade() should decrease level for %s", q)
                .isLessThan(q.getLevel());
        }
    }

    // ── Upgrade/Downgrade Symmetrie ───────────────────────────────────────────

    @Test
    void testUpgradeThenDowngrade_IsIdentityForGUT() {
        // GOOD → upgrade → VERY_GOOD → downgrade → GOOD (symmetrisch)
        assertThat(TobaccoQuality.GOOD.upgrade().downgrade()).isEqualTo(TobaccoQuality.GOOD);
    }

    @Test
    void testUpgradeThenDowngrade_IsIdentityForSEHR_GUT() {
        assertThat(TobaccoQuality.VERY_GOOD.upgrade().downgrade()).isEqualTo(TobaccoQuality.VERY_GOOD);
    }

    // ── fromLevel() ───────────────────────────────────────────────────────────

    @Test
    void testFromLevel_ReturnsCorrectQuality() {
        assertThat(TobaccoQuality.fromLevel(0)).isEqualTo(TobaccoQuality.POOR);
        assertThat(TobaccoQuality.fromLevel(1)).isEqualTo(TobaccoQuality.GOOD);
        assertThat(TobaccoQuality.fromLevel(2)).isEqualTo(TobaccoQuality.VERY_GOOD);
        assertThat(TobaccoQuality.fromLevel(3)).isEqualTo(TobaccoQuality.LEGENDARY);
    }

    @Test
    void testFromLevel_InvalidLevel_ReturnsPOOR() {
        assertThat(TobaccoQuality.fromLevel(-1)).isEqualTo(TobaccoQuality.POOR);
        assertThat(TobaccoQuality.fromLevel(99)).isEqualTo(TobaccoQuality.POOR);
    }

    @Test
    void testFromLevel_RoundTrip() {
        for (TobaccoQuality q : TobaccoQuality.values()) {
            assertThat(TobaccoQuality.fromLevel(q.getLevel()))
                .as("fromLevel(getLevel()) should return original quality for %s", q)
                .isEqualTo(q);
        }
    }

    // ── ColorCode ─────────────────────────────────────────────────────────────

    @Test
    void testColorCode_IsNotNull() {
        for (TobaccoQuality q : TobaccoQuality.values()) {
            assertThat(q.getColorCode())
                .as("Color code should not be null for %s", q)
                .isNotNull()
                .isNotEmpty();
        }
    }

    @Test
    void testColorCode_StartsWithParagraphSign() {
        for (TobaccoQuality q : TobaccoQuality.values()) {
            assertThat(q.getColorCode())
                .as("Color code should start with § for %s", q)
                .startsWith("§");
        }
    }
}
