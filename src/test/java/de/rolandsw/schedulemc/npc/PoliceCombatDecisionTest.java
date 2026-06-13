package de.rolandsw.schedulemc.npc;

import de.rolandsw.schedulemc.npc.events.PoliceCombatHandler;
import de.rolandsw.schedulemc.npc.events.PoliceCombatHandler.EngagementMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-Tests für die reine Polizei-Kampf-Entscheidung (keine MC-Abhängigkeit).
 * Defaults: arrest 2.5, ranged min 8, escapeThreshold 2, pursuitThreshold 200t,
 * melee >=3, ranged >=4.
 */
public class PoliceCombatDecisionTest {

    private EngagementMode decide(int wanted, double dist, boolean inArrest,
                                  boolean los, boolean vehicle, int escapes, long pursuit) {
        return PoliceCombatHandler.decideEngagement(true, wanted, dist, 2.5, 8.0,
            inArrest, los, vehicle, escapes, pursuit, 2, 200L, 3, 4);
    }

    @Test
    @DisplayName("Arrest always wins: in countdown or in arrest range -> NONE")
    void arrestPreferred() {
        assertThat(decide(5, 1.5, false, true, false, 5, 999)).isEqualTo(EngagementMode.NONE);
        assertThat(decide(5, 12.0, true, true, false, 5, 999)).isEqualTo(EngagementMode.NONE);
    }

    @Test
    @DisplayName("No escalation yet -> NONE even at high wanted")
    void notEscalated() {
        assertThat(decide(5, 12.0, false, true, false, 0, 50)).isEqualTo(EngagementMode.NONE);
    }

    @Test
    @DisplayName("Escalated via escape count: ranged at wanted 4 beyond min distance")
    void rangedByEscapes() {
        assertThat(decide(4, 12.0, false, true, false, 2, 0)).isEqualTo(EngagementMode.RANGED);
    }

    @Test
    @DisplayName("Escalated via long pursuit: melee in close range at wanted 3")
    void meleeByPursuit() {
        assertThat(decide(3, 6.0, false, true, false, 0, 200)).isEqualTo(EngagementMode.MELEE);
    }

    @Test
    @DisplayName("Ranged needs line of sight and no vehicle")
    void rangedGuards() {
        assertThat(decide(4, 12.0, false, false, false, 2, 0)).isEqualTo(EngagementMode.NONE);
        assertThat(decide(4, 12.0, false, true, true, 2, 0)).isEqualTo(EngagementMode.NONE);
    }

    @Test
    @DisplayName("Wanted below thresholds -> NONE")
    void belowThresholds() {
        assertThat(decide(2, 6.0, false, true, false, 5, 999)).isEqualTo(EngagementMode.NONE);
    }

    @Test
    @DisplayName("Disabled master switch -> NONE")
    void disabled() {
        assertThat(PoliceCombatHandler.decideEngagement(false, 5, 12.0, 2.5, 8.0,
            false, true, false, 5, 999, 2, 200L, 3, 4)).isEqualTo(EngagementMode.NONE);
    }
}
