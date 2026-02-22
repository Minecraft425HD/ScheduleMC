package de.rolandsw.schedulemc.gang;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests für GangReputation — Enum-Methoden, Level-zu-Reputation-Mapping und Stern-Anzeige.
 */
public class GangReputationTest {

    // ── getForLevel ─────────────────────────────────────────────────────────

    @Test
    void testGetForLevel_0_ReturnsUnknown() {
        assertThat(GangReputation.getForLevel(0)).isEqualTo(GangReputation.UNKNOWN);
    }

    @Test
    void testGetForLevel_5_ReturnsBekannt() {
        assertThat(GangReputation.getForLevel(5)).isEqualTo(GangReputation.BEKANNT);
    }

    @Test
    void testGetForLevel_12_ReturnsRespektiert() {
        assertThat(GangReputation.getForLevel(12)).isEqualTo(GangReputation.RESPEKTIERT);
    }

    @Test
    void testGetForLevel_20_ReturnsGefuerchtet() {
        assertThat(GangReputation.getForLevel(20)).isEqualTo(GangReputation.GEFUERCHTET);
    }

    @Test
    void testGetForLevel_27_ReturnsLegendaer() {
        assertThat(GangReputation.getForLevel(27)).isEqualTo(GangReputation.LEGENDAER);
    }

    @Test
    void testGetForLevel_30_ReturnsLegendaer() {
        assertThat(GangReputation.getForLevel(30)).isEqualTo(GangReputation.LEGENDAER);
    }

    // ── getStarsForLevel ────────────────────────────────────────────────────

    @Test
    void testGetStarsForLevel_0_ReturnsEmpty() {
        // UNKNOWN hat 0 Sterne
        assertThat(GangReputation.getStarsForLevel(0)).isEmpty();
    }

    @Test
    void testGetStarsForLevel_27_HasStars() {
        // LEGENDAER hat 4 Sterne
        String stars = GangReputation.getStarsForLevel(27);
        assertThat(stars).isNotEmpty();
        // Enthält Stern-Zeichen (★)
        assertThat(stars).contains("\u2605");
    }

    // ── getLevelStars ───────────────────────────────────────────────────────

    @Test
    void testGetLevelStars_Level1_Returns1Star() {
        String stars = GangReputation.getLevelStars(1);
        // Level 1 → (1-1)/6 + 1 = 1 Stern
        long starCount = stars.chars().filter(c -> c == '\u2605').count();
        assertThat(starCount).isEqualTo(1);
    }

    @Test
    void testGetLevelStars_Level6_Returns1Star() {
        String stars = GangReputation.getLevelStars(6);
        long starCount = stars.chars().filter(c -> c == '\u2605').count();
        assertThat(starCount).isEqualTo(1);
    }

    @Test
    void testGetLevelStars_Level7_Returns2Stars() {
        String stars = GangReputation.getLevelStars(7);
        long starCount = stars.chars().filter(c -> c == '\u2605').count();
        assertThat(starCount).isEqualTo(2);
    }

    @Test
    void testGetLevelStars_Level30_Returns5Stars() {
        String stars = GangReputation.getLevelStars(30);
        long starCount = stars.chars().filter(c -> c == '\u2605').count();
        assertThat(starCount).isEqualTo(5);
    }

    // ── allgemeine Enum-Integrität ───────────────────────────────────────────

    @Test
    void testAllReputations_HaveNonNegativeStarCount() {
        for (GangReputation rep : GangReputation.values()) {
            assertThat(rep.getStarCount())
                .as("StarCount für %s sollte >= 0 sein", rep)
                .isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    void testAllReputations_HaveNonNullDisplayName() {
        for (GangReputation rep : GangReputation.values()) {
            assertThat(rep.getDisplayName())
                .as("DisplayName für %s sollte nicht null/leer sein", rep)
                .isNotBlank();
        }
    }

    @Test
    void testReputationLevels_AreOrdered() {
        GangReputation[] reps = GangReputation.values();
        for (int i = 1; i < reps.length; i++) {
            assertThat(reps[i].getRequiredLevel())
                .as("%s sollte höheres requiredLevel haben als %s", reps[i], reps[i - 1])
                .isGreaterThanOrEqualTo(reps[i - 1].getRequiredLevel());
        }
    }
}
