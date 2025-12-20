package de.rolandsw.schedulemc.production;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für ProductionSize Enum
 */
public class ProductionSizeTest {

    @Test
    public void testSmallSize() {
        assertThat(ProductionSize.SMALL.getCapacity()).isEqualTo(6);
        assertThat(ProductionSize.SMALL.getMaxFuel()).isEqualTo(500);
        assertThat(ProductionSize.SMALL.getSpeedMultiplier()).isEqualTo(1.0);
        assertThat(ProductionSize.SMALL.getDisplayName()).isEqualTo("Small");
    }

    @Test
    public void testMediumSize() {
        assertThat(ProductionSize.MEDIUM.getCapacity()).isEqualTo(12);
        assertThat(ProductionSize.MEDIUM.getMaxFuel()).isEqualTo(1000);
        assertThat(ProductionSize.MEDIUM.getSpeedMultiplier()).isEqualTo(1.5);
        assertThat(ProductionSize.MEDIUM.getDisplayName()).isEqualTo("Medium");
    }

    @Test
    public void testBigSize() {
        assertThat(ProductionSize.BIG.getCapacity()).isEqualTo(24);
        assertThat(ProductionSize.BIG.getMaxFuel()).isEqualTo(2000);
        assertThat(ProductionSize.BIG.getSpeedMultiplier()).isEqualTo(2.0);
        assertThat(ProductionSize.BIG.getDisplayName()).isEqualTo("Big");
    }

    @Test
    public void testGetProcessingTime() {
        int baseTime = 1000;

        // Small: 1x speed (no change)
        assertThat(ProductionSize.SMALL.getProcessingTime(baseTime)).isEqualTo(1000);

        // Medium: 1.5x speed (faster)
        assertThat(ProductionSize.MEDIUM.getProcessingTime(baseTime)).isEqualTo(666);

        // Big: 2x speed (fastest)
        assertThat(ProductionSize.BIG.getProcessingTime(baseTime)).isEqualTo(500);
    }

    @Test
    public void testCapacityProgression() {
        // Verify sizes progress consistently
        assertThat(ProductionSize.MEDIUM.getCapacity())
            .isEqualTo(ProductionSize.SMALL.getCapacity() * 2);

        assertThat(ProductionSize.BIG.getCapacity())
            .isEqualTo(ProductionSize.SMALL.getCapacity() * 4);
    }

    @Test
    public void testFuelProgression() {
        // Verify fuel levels progress consistently
        assertThat(ProductionSize.MEDIUM.getMaxFuel())
            .isEqualTo(ProductionSize.SMALL.getMaxFuel() * 2);

        assertThat(ProductionSize.BIG.getMaxFuel())
            .isEqualTo(ProductionSize.SMALL.getMaxFuel() * 4);
    }
}
