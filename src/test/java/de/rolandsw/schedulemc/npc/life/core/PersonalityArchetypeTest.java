package de.rolandsw.schedulemc.npc.life.core;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class PersonalityArchetypeTest {

    @Test
    void balancedBelowThreshold() {
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(29, -29, 29)))
            .isEqualTo(PersonalityArchetype.BALANCED);
    }

    @Test
    void dominantAxes() {
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(0, 0, 80))).isEqualTo(PersonalityArchetype.GREEDY);
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(0, 0, -80))).isEqualTo(PersonalityArchetype.GENEROUS);
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(0, 80, 0))).isEqualTo(PersonalityArchetype.HONEST);
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(0, -80, 0))).isEqualTo(PersonalityArchetype.DECEITFUL);
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(80, 0, 0))).isEqualTo(PersonalityArchetype.BRAVE);
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(-80, 0, 0))).isEqualTo(PersonalityArchetype.TIMID);
    }

    @Test
    void tieBreakGreedOverHonestyOverCourage() {
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(50, 50, 50))).isEqualTo(PersonalityArchetype.GREEDY);
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(50, 50, 0))).isEqualTo(PersonalityArchetype.HONEST);
    }

    @Test
    void exactThresholdCounts() {
        assertThat(PersonalityArchetype.fromTraits(new NPCTraits(0, 0, 30))).isEqualTo(PersonalityArchetype.GREEDY);
    }
}
