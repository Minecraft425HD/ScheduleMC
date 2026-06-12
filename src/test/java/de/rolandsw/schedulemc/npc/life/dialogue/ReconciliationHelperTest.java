package de.rolandsw.schedulemc.npc.life.dialogue;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ReconciliationHelperTest {

    @Test
    void compensationScalesWithGreedAndIntensity() {
        int generous = ReconciliationHelper.calculateCompensation(-100, 50f, 0, false);
        int neutral  = ReconciliationHelper.calculateCompensation(0, 50f, 0, false);
        int greedy   = ReconciliationHelper.calculateCompensation(100, 50f, 0, false);
        assertThat(generous).isLessThan(neutral);
        assertThat(neutral).isLessThan(greedy);

        int calm    = ReconciliationHelper.calculateCompensation(0, 0f, 0, false);
        int furious = ReconciliationHelper.calculateCompensation(0, 100f, 0, false);
        assertThat(furious).isEqualTo(calm * 2);
    }

    @Test
    void friendsPayLessAndSpamDoubles() {
        int stranger = ReconciliationHelper.calculateCompensation(0, 50f, 0, false);
        int friend   = ReconciliationHelper.calculateCompensation(0, 50f, 10, false);
        int spammer  = ReconciliationHelper.calculateCompensation(0, 50f, 0, true);
        assertThat(friend).isLessThan(stranger);
        assertThat(spammer).isEqualTo(stranger * 2);
    }

    @Test
    void verbalChanceIsClamped() {
        assertThat(ReconciliationHelper.verbalApologyChance(100, -100, -100)).isEqualTo(0.05f);
        assertThat(ReconciliationHelper.verbalApologyChance(-100, 100, 100)).isEqualTo(0.9f);
        assertThat(ReconciliationHelper.verbalApologyChance(0, 0, 0)).isEqualTo(0.3f);
    }
}
