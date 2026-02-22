package de.rolandsw.schedulemc.vehicle;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.TireSeasonType;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.TireType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests für das TireType-Enum — prüft Zuordnung von Reifentyp, Jahreszeit und Radgröße.
 */
public class TireTypeTest {

    @Test
    void testWinterTire_HasWinterSeasonType() {
        assertThat(TireType.WINTER.getSeasonType()).isEqualTo(TireSeasonType.WINTER);
    }

    @Test
    void testSummerTires_HaveSmallWheel() {
        assertThat(TireType.STANDARD.isLargeWheel()).isFalse();
        assertThat(TireType.SPORT.isLargeWheel()).isFalse();
        assertThat(TireType.PREMIUM.isLargeWheel()).isFalse();
        assertThat(TireType.WINTER.isLargeWheel()).isFalse();
    }

    @Test
    void testOffroadTires_HaveLargeWheel() {
        assertThat(TireType.OFFROAD.isLargeWheel()).isTrue();
        assertThat(TireType.ALLTERRAIN.isLargeWheel()).isTrue();
        assertThat(TireType.HEAVYDUTY.isLargeWheel()).isTrue();
    }

    @Test
    void testSummerTires_HaveSummerSeasonType() {
        assertThat(TireType.STANDARD.getSeasonType()).isEqualTo(TireSeasonType.SUMMER);
        assertThat(TireType.SPORT.getSeasonType()).isEqualTo(TireSeasonType.SUMMER);
        assertThat(TireType.PREMIUM.getSeasonType()).isEqualTo(TireSeasonType.SUMMER);
    }

    @Test
    void testOffroadTires_HaveAllSeasonType() {
        assertThat(TireType.OFFROAD.getSeasonType()).isEqualTo(TireSeasonType.ALL_SEASON);
        assertThat(TireType.ALLTERRAIN.getSeasonType()).isEqualTo(TireSeasonType.ALL_SEASON);
        assertThat(TireType.HEAVYDUTY.getSeasonType()).isEqualTo(TireSeasonType.ALL_SEASON);
    }

    @Test
    void testAllTireTypes_HaveNonNullSeasonType() {
        for (TireType type : TireType.values()) {
            assertThat(type.getSeasonType())
                .as("SeasonType für %s sollte nicht null sein", type)
                .isNotNull();
        }
    }

    @Test
    void testValues_HaveCorrectCount() {
        assertThat(TireType.values()).hasSize(7);
    }
}
