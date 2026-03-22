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
        assertThat(TobaccoQuality.SCHLECHT.getLevel()).isEqualTo(0);
        assertThat(TobaccoQuality.GUT.getLevel()).isEqualTo(1);
        assertThat(TobaccoQuality.SEHR_GUT.getLevel()).isEqualTo(2);
        assertThat(TobaccoQuality.LEGENDAER.getLevel()).isEqualTo(3);
    }

    // ── Preis-Multiplikatoren ─────────────────────────────────────────────────

    @Test
    void testPriceMultiplier_SCHLECHT_IsBelowOne() {
        assertThat(TobaccoQuality.SCHLECHT.getPriceMultiplier()).isLessThan(1.0);
    }

    @Test
    void testPriceMultiplier_GUT_IsOne() {
        assertThat(TobaccoQuality.GUT.getPriceMultiplier()).isEqualTo(1.0);
    }

    @Test
    void testPriceMultiplier_SEHR_GUT_IsAboveOne() {
        assertThat(TobaccoQuality.SEHR_GUT.getPriceMultiplier()).isGreaterThan(1.0);
    }

    @Test
    void testPriceMultiplier_LEGENDAER_IsHighest() {
        assertThat(TobaccoQuality.LEGENDAER.getPriceMultiplier())
            .isGreaterThan(TobaccoQuality.SEHR_GUT.getPriceMultiplier());
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
        assertThat(TobaccoQuality.SCHLECHT.upgrade()).isEqualTo(TobaccoQuality.GUT);
    }

    @Test
    void testUpgrade_GUT_BeomesSEHR_GUT() {
        assertThat(TobaccoQuality.GUT.upgrade()).isEqualTo(TobaccoQuality.SEHR_GUT);
    }

    @Test
    void testUpgrade_SEHR_GUT_BecomesLEGENDAER() {
        assertThat(TobaccoQuality.SEHR_GUT.upgrade()).isEqualTo(TobaccoQuality.LEGENDAER);
    }

    @Test
    void testUpgrade_LEGENDAER_StaysLEGENDAER() {
        assertThat(TobaccoQuality.LEGENDAER.upgrade()).isEqualTo(TobaccoQuality.LEGENDAER);
    }

    @Test
    void testUpgrade_IncreasesLevel() {
        for (TobaccoQuality q : new TobaccoQuality[]{
                TobaccoQuality.SCHLECHT, TobaccoQuality.GUT, TobaccoQuality.SEHR_GUT}) {
            assertThat(q.upgrade().getLevel())
                .as("upgrade() should increase level for %s", q)
                .isGreaterThan(q.getLevel());
        }
    }

    // ── downgrade() ───────────────────────────────────────────────────────────

    @Test
    void testDowngrade_SCHLECHT_StaysSCHLECHT() {
        assertThat(TobaccoQuality.SCHLECHT.downgrade()).isEqualTo(TobaccoQuality.SCHLECHT);
    }

    @Test
    void testDowngrade_GUT_BeomesSCHLECHT() {
        assertThat(TobaccoQuality.GUT.downgrade()).isEqualTo(TobaccoQuality.SCHLECHT);
    }

    @Test
    void testDowngrade_SEHR_GUT_BecomesGUT() {
        assertThat(TobaccoQuality.SEHR_GUT.downgrade()).isEqualTo(TobaccoQuality.GUT);
    }

    @Test
    void testDowngrade_LEGENDAER_BeomesSEHR_GUT() {
        assertThat(TobaccoQuality.LEGENDAER.downgrade()).isEqualTo(TobaccoQuality.SEHR_GUT);
    }

    @Test
    void testDowngrade_DecreasesLevel() {
        for (TobaccoQuality q : new TobaccoQuality[]{
                TobaccoQuality.GUT, TobaccoQuality.SEHR_GUT, TobaccoQuality.LEGENDAER}) {
            assertThat(q.downgrade().getLevel())
                .as("downgrade() should decrease level for %s", q)
                .isLessThan(q.getLevel());
        }
    }

    // ── Upgrade/Downgrade Symmetrie ───────────────────────────────────────────

    @Test
    void testUpgradeThenDowngrade_IsIdentityForGUT() {
        // GUT → upgrade → SEHR_GUT → downgrade → GUT (symmetrisch)
        assertThat(TobaccoQuality.GUT.upgrade().downgrade()).isEqualTo(TobaccoQuality.GUT);
    }

    @Test
    void testUpgradeThenDowngrade_IsIdentityForSEHR_GUT() {
        assertThat(TobaccoQuality.SEHR_GUT.upgrade().downgrade()).isEqualTo(TobaccoQuality.SEHR_GUT);
    }

    // ── fromLevel() ───────────────────────────────────────────────────────────

    @Test
    void testFromLevel_ReturnsCorrectQuality() {
        assertThat(TobaccoQuality.fromLevel(0)).isEqualTo(TobaccoQuality.SCHLECHT);
        assertThat(TobaccoQuality.fromLevel(1)).isEqualTo(TobaccoQuality.GUT);
        assertThat(TobaccoQuality.fromLevel(2)).isEqualTo(TobaccoQuality.SEHR_GUT);
        assertThat(TobaccoQuality.fromLevel(3)).isEqualTo(TobaccoQuality.LEGENDAER);
    }

    @Test
    void testFromLevel_InvalidLevel_ReturnsSCHLECHT() {
        assertThat(TobaccoQuality.fromLevel(-1)).isEqualTo(TobaccoQuality.SCHLECHT);
        assertThat(TobaccoQuality.fromLevel(99)).isEqualTo(TobaccoQuality.SCHLECHT);
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
