package de.rolandsw.schedulemc.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for all ScheduleMC custom exception classes.
 * <p>
 * Tests exception constructors, inheritance hierarchy, message handling,
 * and context-specific fields (e.g., fieldName, requiredPermission).
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 */
@DisplayName("ScheduleMC Exception System Tests")
class ExceptionTest {

    private static final String TEST_MESSAGE = "Test exception message";
    private static final Throwable TEST_CAUSE = new RuntimeException("Test cause");

    @Nested
    @DisplayName("ScheduleMCException (Base Exception)")
    class ScheduleMCExceptionTest {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateWithMessage() {
            ScheduleMCException exception = new ScheduleMCException(TEST_MESSAGE);

            assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(TEST_MESSAGE)
                .hasNoCause();
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateWithMessageAndCause() {
            ScheduleMCException exception = new ScheduleMCException(TEST_MESSAGE, TEST_CAUSE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .hasCause(TEST_CAUSE);
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateWithCause() {
            ScheduleMCException exception = new ScheduleMCException(TEST_CAUSE);

            assertThat(exception)
                .hasCause(TEST_CAUSE);
        }
    }

    @Nested
    @DisplayName("PlotException")
    class PlotExceptionTest {

        @Test
        @DisplayName("Should inherit from ScheduleMCException")
        void shouldInheritFromBase() {
            PlotException exception = new PlotException(TEST_MESSAGE);

            assertThat(exception).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("Should create with all constructor variants")
        void shouldSupportAllConstructors() {
            PlotException ex1 = new PlotException(TEST_MESSAGE);
            PlotException ex2 = new PlotException(TEST_MESSAGE, TEST_CAUSE);
            PlotException ex3 = new PlotException(TEST_CAUSE);

            assertThat(ex1).hasMessage(TEST_MESSAGE).hasNoCause();
            assertThat(ex2).hasMessage(TEST_MESSAGE).hasCause(TEST_CAUSE);
            assertThat(ex3).hasCause(TEST_CAUSE);
        }

        @Test
        @DisplayName("Should be used for plot-related errors")
        void shouldRepresentPlotErrors() {
            PlotException exception = new PlotException("Invalid plot coordinates: (999999, 999999)");

            assertThat(exception.getMessage()).contains("plot coordinates");
        }
    }

    @Nested
    @DisplayName("EconomyException")
    class EconomyExceptionTest {

        @Test
        @DisplayName("Should inherit from ScheduleMCException")
        void shouldInheritFromBase() {
            EconomyException exception = new EconomyException(TEST_MESSAGE);

            assertThat(exception).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("Should create with all constructor variants")
        void shouldSupportAllConstructors() {
            EconomyException ex1 = new EconomyException(TEST_MESSAGE);
            EconomyException ex2 = new EconomyException(TEST_MESSAGE, TEST_CAUSE);
            EconomyException ex3 = new EconomyException(TEST_CAUSE);

            assertThat(ex1).hasMessage(TEST_MESSAGE).hasNoCause();
            assertThat(ex2).hasMessage(TEST_MESSAGE).hasCause(TEST_CAUSE);
            assertThat(ex3).hasCause(TEST_CAUSE);
        }

        @Test
        @DisplayName("Should be used for economic errors")
        void shouldRepresentEconomicErrors() {
            EconomyException exception = new EconomyException("Insufficient funds: required 1000.0, available 500.0");

            assertThat(exception.getMessage()).contains("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("ProductionException")
    class ProductionExceptionTest {

        @Test
        @DisplayName("Should inherit from ScheduleMCException")
        void shouldInheritFromBase() {
            ProductionException exception = new ProductionException(TEST_MESSAGE);

            assertThat(exception).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("Should create with all constructor variants")
        void shouldSupportAllConstructors() {
            ProductionException ex1 = new ProductionException(TEST_MESSAGE);
            ProductionException ex2 = new ProductionException(TEST_MESSAGE, TEST_CAUSE);
            ProductionException ex3 = new ProductionException(TEST_CAUSE);

            assertThat(ex1).hasMessage(TEST_MESSAGE).hasNoCause();
            assertThat(ex2).hasMessage(TEST_MESSAGE).hasCause(TEST_CAUSE);
            assertThat(ex3).hasCause(TEST_CAUSE);
        }

        @Test
        @DisplayName("Should be used for production errors")
        void shouldRepresentProductionErrors() {
            ProductionException exception = new ProductionException("Invalid production recipe: missing input items");

            assertThat(exception.getMessage()).contains("production recipe");
        }
    }

    @Nested
    @DisplayName("NPCException")
    class NPCExceptionTest {

        @Test
        @DisplayName("Should inherit from ScheduleMCException")
        void shouldInheritFromBase() {
            NPCException exception = new NPCException(TEST_MESSAGE);

            assertThat(exception).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("Should create with all constructor variants")
        void shouldSupportAllConstructors() {
            NPCException ex1 = new NPCException(TEST_MESSAGE);
            NPCException ex2 = new NPCException(TEST_MESSAGE, TEST_CAUSE);
            NPCException ex3 = new NPCException(TEST_CAUSE);

            assertThat(ex1).hasMessage(TEST_MESSAGE).hasNoCause();
            assertThat(ex2).hasMessage(TEST_MESSAGE).hasCause(TEST_CAUSE);
            assertThat(ex3).hasCause(TEST_CAUSE);
        }

        @Test
        @DisplayName("Should be used for NPC errors")
        void shouldRepresentNPCErrors() {
            NPCException exception = new NPCException("NPC spawn failed: invalid personality trait 'INVALID'");

            assertThat(exception.getMessage()).contains("NPC spawn");
        }
    }

    @Nested
    @DisplayName("ValidationException")
    class ValidationExceptionTest {

        @Test
        @DisplayName("Should inherit from ScheduleMCException")
        void shouldInheritFromBase() {
            ValidationException exception = new ValidationException(TEST_MESSAGE);

            assertThat(exception).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessage() {
            ValidationException exception = new ValidationException(TEST_MESSAGE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .extracting(ValidationException::getFieldName, ValidationException::getInvalidValue)
                .containsExactly(null, null);
        }

        @Test
        @DisplayName("Should create with field context")
        void shouldCreateWithFieldContext() {
            ValidationException exception = new ValidationException(
                "Value exceeds maximum",
                "amount",
                99999999
            );

            assertThat(exception.getMessage())
                .contains("Value exceeds maximum")
                .contains("field: amount")
                .contains("value: 99999999");

            assertThat(exception.getFieldName()).isEqualTo("amount");
            assertThat(exception.getInvalidValue()).isEqualTo(99999999);
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithCause() {
            ValidationException exception = new ValidationException(TEST_MESSAGE, TEST_CAUSE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .hasCause(TEST_CAUSE);
        }

        @Test
        @DisplayName("Should handle null field values gracefully")
        void shouldHandleNullFields() {
            ValidationException exception = new ValidationException(
                "Null value not allowed",
                "playerName",
                null
            );

            assertThat(exception.getMessage()).contains("null");
            assertThat(exception.getFieldName()).isEqualTo("playerName");
            assertThat(exception.getInvalidValue()).isNull();
        }
    }

    @Nested
    @DisplayName("PermissionException")
    class PermissionExceptionTest {

        @Test
        @DisplayName("Should inherit from ScheduleMCException")
        void shouldInheritFromBase() {
            PermissionException exception = new PermissionException(TEST_MESSAGE);

            assertThat(exception).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessage() {
            PermissionException exception = new PermissionException(TEST_MESSAGE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .extracting(PermissionException::getPlayerName, PermissionException::getRequiredPermission)
                .containsExactly(null, null);
        }

        @Test
        @DisplayName("Should create with permission context")
        void shouldCreateWithPermissionContext() {
            PermissionException exception = new PermissionException(
                "Access denied",
                "PlayerName",
                "schedulemc.plot.admin"
            );

            assertThat(exception.getMessage())
                .contains("Access denied")
                .contains("player: PlayerName")
                .contains("required: schedulemc.plot.admin");

            assertThat(exception.getPlayerName()).isEqualTo("PlayerName");
            assertThat(exception.getRequiredPermission()).isEqualTo("schedulemc.plot.admin");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithCause() {
            PermissionException exception = new PermissionException(TEST_MESSAGE, TEST_CAUSE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .hasCause(TEST_CAUSE);
        }
    }

    @Nested
    @DisplayName("ConfigurationException")
    class ConfigurationExceptionTest {

        @Test
        @DisplayName("Should inherit from ScheduleMCException")
        void shouldInheritFromBase() {
            ConfigurationException exception = new ConfigurationException(TEST_MESSAGE);

            assertThat(exception).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessage() {
            ConfigurationException exception = new ConfigurationException(TEST_MESSAGE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .extracting(ConfigurationException::getConfigKey)
                .isEqualTo(null);
        }

        @Test
        @DisplayName("Should create with config key context")
        void shouldCreateWithConfigKeyContext() {
            ConfigurationException exception = new ConfigurationException(
                "Invalid configuration value",
                "economy.startingBalance"
            );

            assertThat(exception.getMessage())
                .contains("Invalid configuration value")
                .contains("config key: economy.startingBalance");

            assertThat(exception.getConfigKey()).isEqualTo("economy.startingBalance");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithCause() {
            ConfigurationException exception = new ConfigurationException(TEST_MESSAGE, TEST_CAUSE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .hasCause(TEST_CAUSE);
        }

        @Test
        @DisplayName("Should create with config key and cause")
        void shouldCreateWithKeyAndCause() {
            ConfigurationException exception = new ConfigurationException(
                "Parse error",
                "plot.maxSize",
                TEST_CAUSE
            );

            assertThat(exception.getMessage()).contains("config key: plot.maxSize");
            assertThat(exception.getConfigKey()).isEqualTo("plot.maxSize");
            assertThat(exception).hasCause(TEST_CAUSE);
        }
    }

    @Nested
    @DisplayName("StorageException")
    class StorageExceptionTest {

        @Test
        @DisplayName("Should inherit from ScheduleMCException")
        void shouldInheritFromBase() {
            StorageException exception = new StorageException(TEST_MESSAGE);

            assertThat(exception).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessage() {
            StorageException exception = new StorageException(TEST_MESSAGE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .extracting(StorageException::getFilePath)
                .isEqualTo(null);
        }

        @Test
        @DisplayName("Should create with file path context")
        void shouldCreateWithFilePathContext() {
            StorageException exception = new StorageException(
                "Failed to save data",
                "/world/schedulemc/plots.json"
            );

            assertThat(exception.getMessage())
                .contains("Failed to save data")
                .contains("file: /world/schedulemc/plots.json");

            assertThat(exception.getFilePath()).isEqualTo("/world/schedulemc/plots.json");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithCause() {
            StorageException exception = new StorageException(TEST_MESSAGE, TEST_CAUSE);

            assertThat(exception)
                .hasMessage(TEST_MESSAGE)
                .hasCause(TEST_CAUSE);
        }

        @Test
        @DisplayName("Should create with file path and cause")
        void shouldCreateWithPathAndCause() {
            StorageException exception = new StorageException(
                "JSON parse error",
                "/world/config.json",
                TEST_CAUSE
            );

            assertThat(exception.getMessage()).contains("file: /world/config.json");
            assertThat(exception.getFilePath()).isEqualTo("/world/config.json");
            assertThat(exception).hasCause(TEST_CAUSE);
        }
    }

    @Nested
    @DisplayName("Exception Hierarchy Tests")
    class HierarchyTest {

        @Test
        @DisplayName("All domain exceptions should extend ScheduleMCException")
        void allExceptionsShouldExtendBase() {
            assertThat(new PlotException("test")).isInstanceOf(ScheduleMCException.class);
            assertThat(new EconomyException("test")).isInstanceOf(ScheduleMCException.class);
            assertThat(new ProductionException("test")).isInstanceOf(ScheduleMCException.class);
            assertThat(new NPCException("test")).isInstanceOf(ScheduleMCException.class);
            assertThat(new ValidationException("test")).isInstanceOf(ScheduleMCException.class);
            assertThat(new PermissionException("test")).isInstanceOf(ScheduleMCException.class);
            assertThat(new ConfigurationException("test")).isInstanceOf(ScheduleMCException.class);
            assertThat(new StorageException("test")).isInstanceOf(ScheduleMCException.class);
        }

        @Test
        @DisplayName("ScheduleMCException should extend RuntimeException")
        void baseShouldExtendRuntimeException() {
            assertThat(new ScheduleMCException("test")).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("All exceptions should be unchecked (RuntimeException)")
        void allExceptionsShouldBeUnchecked() {
            assertThat(new PlotException("test")).isInstanceOf(RuntimeException.class);
            assertThat(new EconomyException("test")).isInstanceOf(RuntimeException.class);
            assertThat(new ProductionException("test")).isInstanceOf(RuntimeException.class);
            assertThat(new NPCException("test")).isInstanceOf(RuntimeException.class);
            assertThat(new ValidationException("test")).isInstanceOf(RuntimeException.class);
            assertThat(new PermissionException("test")).isInstanceOf(RuntimeException.class);
            assertThat(new ConfigurationException("test")).isInstanceOf(RuntimeException.class);
            assertThat(new StorageException("test")).isInstanceOf(RuntimeException.class);
        }
    }
}
