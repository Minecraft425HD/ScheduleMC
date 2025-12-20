package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für Input-Validierung
 */
public class InputValidationTest {

    @Test
    public void testValidatePrice_Valid() {
        // Valid prices
        assertThat(InputValidation.validatePrice(0.01).isSuccess()).isTrue();
        assertThat(InputValidation.validatePrice(100.0).isSuccess()).isTrue();
        assertThat(InputValidation.validatePrice(1000000.0).isSuccess()).isTrue();
    }

    @Test
    public void testValidatePrice_Invalid() {
        // Zero
        assertThat(InputValidation.validatePrice(0.0).isFailure()).isTrue();

        // Negative
        assertThat(InputValidation.validatePrice(-100.0).isFailure()).isTrue();

        // Too large
        assertThat(InputValidation.validatePrice(2_000_000_000.0).isFailure()).isTrue();

        // NaN
        assertThat(InputValidation.validatePrice(Double.NaN).isFailure()).isTrue();

        // Infinity
        assertThat(InputValidation.validatePrice(Double.POSITIVE_INFINITY).isFailure()).isTrue();
    }

    @Test
    public void testValidateName_Valid() {
        assertThat(InputValidation.validateName("MyPlot").isSuccess()).isTrue();
        assertThat(InputValidation.validateName("Test123").isSuccess()).isTrue();
        assertThat(InputValidation.validateName("A very long name but still valid").isSuccess()).isTrue();
    }

    @Test
    public void testValidateName_Invalid() {
        // Null
        assertThat(InputValidation.validateName(null).isFailure()).isTrue();

        // Empty
        assertThat(InputValidation.validateName("").isFailure()).isTrue();
        assertThat(InputValidation.validateName("   ").isFailure()).isTrue();

        // Too long (> 64 chars)
        String tooLong = "A".repeat(65);
        assertThat(InputValidation.validateName(tooLong).isFailure()).isTrue();

        // Formatting codes
        assertThat(InputValidation.validateName("§cRed Name").isFailure()).isTrue();
    }

    @Test
    public void testValidateAmount_Valid() {
        assertThat(InputValidation.validateAmount(1.0).isSuccess()).isTrue();
        assertThat(InputValidation.validateAmount(1000000).isSuccess()).isTrue();
    }

    @Test
    public void testValidateAmount_Invalid() {
        assertThat(InputValidation.validateAmount(0.0).isFailure()).isTrue();
        assertThat(InputValidation.validateAmount(-100.0).isFailure()).isTrue();
        assertThat(InputValidation.validateAmount(2_000_000_000.0).isFailure()).isTrue();
    }

    @Test
    public void testValidatePercentage_Valid() {
        assertThat(InputValidation.validatePercentage(0).isSuccess()).isTrue();
        assertThat(InputValidation.validatePercentage(50).isSuccess()).isTrue();
        assertThat(InputValidation.validatePercentage(100).isSuccess()).isTrue();
    }

    @Test
    public void testValidatePercentage_Invalid() {
        assertThat(InputValidation.validatePercentage(-1).isFailure()).isTrue();
        assertThat(InputValidation.validatePercentage(101).isFailure()).isTrue();
    }

    @Test
    public void testValidationResult_ErrorMessages() {
        InputValidation.ValidationResult result = InputValidation.validatePrice(-100.0);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getErrorMessage()).isNotNull();
        assertThat(result.getErrorMessage()).contains("positiv");
    }
}
