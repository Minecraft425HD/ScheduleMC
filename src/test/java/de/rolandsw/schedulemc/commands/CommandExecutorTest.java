package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.rolandsw.schedulemc.test.MinecraftTestBootstrap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommandExecutor
 *
 * Tests cover:
 * - executePlayerCommand success and failure
 * - executeSourceCommand success and failure
 * - executePlayerCommandWithMessage
 * - executeAdminCommand with permission checks
 * - Helper methods (sendSuccess, sendFailure, sendInfo)
 * - Error message formatting
 *
 * NOTE: These tests are disabled because they require mocking Minecraft classes
 * (ServerPlayer, CommandSourceStack, etc.) which triggers static initialization
 * of Minecraft's registry system. This causes NoClassDefFoundError in unit test
 * environments. These tests require a full Minecraft test harness.
 */
@Disabled("Requires full Minecraft test environment - mocking ServerPlayer triggers static initialization")
class CommandExecutorTest {

    private CommandContext<CommandSourceStack> mockContext;
    private CommandSourceStack mockSource;
    private ServerPlayer mockPlayer;

    @BeforeAll
    static void initMinecraft() {
        MinecraftTestBootstrap.init();
    }

    @BeforeEach
    void setUp() {
        mockContext = mock(CommandContext.class);
        mockSource = mock(CommandSourceStack.class);
        mockPlayer = mock(ServerPlayer.class);

        when(mockContext.getSource()).thenReturn(mockSource);
    }

    // ==================== executePlayerCommand Tests ====================

    @Test
    @DisplayName("executePlayerCommand should succeed with valid player")
    void testExecutePlayerCommandSuccess() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenReturn(mockPlayer);
        boolean[] commandExecuted = {false};

        // Act
        int result = CommandExecutor.executePlayerCommand(
            mockContext,
            "Test error",
            player -> {
                assertThat(player).isSameAs(mockPlayer);
                commandExecuted[0] = true;
            }
        );

        // Assert
        assertThat(result).isEqualTo(1);
        assertThat(commandExecuted[0]).isTrue();
        verify(mockSource, never()).sendFailure(any());
    }

    @Test
    @DisplayName("executePlayerCommand should handle CommandSyntaxException")
    void testExecutePlayerCommandNoPlayer() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenThrow(new CommandSyntaxException(null, null));

        // Act
        int result = CommandExecutor.executePlayerCommand(
            mockContext,
            "Test error",
            player -> fail("Should not execute command")
        );

        // Assert
        assertThat(result).isEqualTo(0);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        assertThat(captor.getValue().getString()).contains("Test error");
    }

    @Test
    @DisplayName("executePlayerCommand should handle exceptions in command logic")
    void testExecutePlayerCommandWithException() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenReturn(mockPlayer);

        // Act
        int result = CommandExecutor.executePlayerCommand(
            mockContext,
            "Command failed",
            player -> {
                throw new RuntimeException("Something went wrong");
            }
        );

        // Assert
        assertThat(result).isEqualTo(0);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        String message = captor.getValue().getString();
        assertThat(message).contains("Command failed");
        assertThat(message).contains("Something went wrong");
    }

    // ==================== executeSourceCommand Tests ====================

    @Test
    @DisplayName("executeSourceCommand should succeed")
    void testExecuteSourceCommandSuccess() {
        // Arrange
        boolean[] commandExecuted = {false};

        // Act
        int result = CommandExecutor.executeSourceCommand(
            mockContext,
            "Test error",
            source -> {
                assertThat(source).isSameAs(mockSource);
                commandExecuted[0] = true;
            }
        );

        // Assert
        assertThat(result).isEqualTo(1);
        assertThat(commandExecuted[0]).isTrue();
        verify(mockSource, never()).sendFailure(any());
    }

    @Test
    @DisplayName("executeSourceCommand should handle exceptions")
    void testExecuteSourceCommandWithException() {
        // Act
        int result = CommandExecutor.executeSourceCommand(
            mockContext,
            "Source command failed",
            source -> {
                throw new IllegalStateException("Invalid state");
            }
        );

        // Assert
        assertThat(result).isEqualTo(0);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        String message = captor.getValue().getString();
        assertThat(message).contains("Source command failed");
        assertThat(message).contains("Invalid state");
    }

    // ==================== executePlayerCommandWithMessage Tests ====================

    @Test
    @DisplayName("executePlayerCommandWithMessage should send custom success message")
    void testExecutePlayerCommandWithMessageSuccess() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenReturn(mockPlayer);

        // Act
        int result = CommandExecutor.executePlayerCommandWithMessage(
            mockContext,
            "Test error",
            "§aSuccess!",
            player -> {
                // Command logic
            }
        );

        // Assert
        assertThat(result).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Supplier<Component>> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(mockSource).sendSuccess(captor.capture(), eq(false));
        assertThat(captor.getValue().get().getString()).isEqualTo("§aSuccess!");
    }

    @Test
    @DisplayName("executePlayerCommandWithMessage should handle exceptions")
    void testExecutePlayerCommandWithMessageFailure() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenReturn(mockPlayer);

        // Act
        int result = CommandExecutor.executePlayerCommandWithMessage(
            mockContext,
            "Command error",
            "Should not appear",
            player -> {
                throw new Exception("Test exception");
            }
        );

        // Assert
        assertThat(result).isEqualTo(0);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        assertThat(captor.getValue().getString()).contains("Command error");
        verify(mockSource, never()).sendSuccess(any(), anyBoolean());
    }

    // ==================== executeAdminCommand Tests ====================

    @Test
    @DisplayName("executeAdminCommand should succeed with sufficient permissions")
    void testExecuteAdminCommandWithPermission() {
        // Arrange
        when(mockSource.hasPermission(2)).thenReturn(true);
        boolean[] commandExecuted = {false};

        // Act
        int result = CommandExecutor.executeAdminCommand(
            mockContext,
            "Admin error",
            2,
            source -> {
                commandExecuted[0] = true;
            }
        );

        // Assert
        assertThat(result).isEqualTo(1);
        assertThat(commandExecuted[0]).isTrue();
        verify(mockSource).hasPermission(2);
    }

    @Test
    @DisplayName("executeAdminCommand should fail without permissions")
    void testExecuteAdminCommandWithoutPermission() {
        // Arrange
        when(mockSource.hasPermission(4)).thenReturn(false);

        // Act
        int result = CommandExecutor.executeAdminCommand(
            mockContext,
            "Admin error",
            4,
            source -> fail("Should not execute")
        );

        // Assert
        assertThat(result).isEqualTo(0);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        assertThat(captor.getValue().getString()).contains("Keine Berechtigung");
    }

    @Test
    @DisplayName("executeAdminCommand should handle exceptions")
    void testExecuteAdminCommandWithException() {
        // Arrange
        when(mockSource.hasPermission(2)).thenReturn(true);

        // Act
        int result = CommandExecutor.executeAdminCommand(
            mockContext,
            "Admin command failed",
            2,
            source -> {
                throw new Exception("Admin error");
            }
        );

        // Assert
        assertThat(result).isEqualTo(0);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        assertThat(captor.getValue().getString()).contains("Admin command failed");
    }

    @Test
    @DisplayName("executeAdminCommand should test various permission levels")
    void testExecuteAdminCommandPermissionLevels() {
        // Test level 2 (gamemode)
        when(mockSource.hasPermission(2)).thenReturn(true);
        int result = CommandExecutor.executeAdminCommand(mockContext, "Error", 2, s -> {});
        assertThat(result).isEqualTo(1);

        // Reset mock
        reset(mockSource);
        when(mockContext.getSource()).thenReturn(mockSource);

        // Test level 3 (ban)
        when(mockSource.hasPermission(3)).thenReturn(false);
        result = CommandExecutor.executeAdminCommand(mockContext, "Error", 3, s -> {});
        assertThat(result).isEqualTo(0);

        // Reset mock
        reset(mockSource);
        when(mockContext.getSource()).thenReturn(mockSource);

        // Test level 4 (stop)
        when(mockSource.hasPermission(4)).thenReturn(true);
        result = CommandExecutor.executeAdminCommand(mockContext, "Error", 4, s -> {});
        assertThat(result).isEqualTo(1);
    }

    // ==================== Helper Method Tests ====================

    @Test
    @DisplayName("sendSuccess should send green message")
    void testSendSuccess() {
        // Act
        CommandExecutor.sendSuccess(mockSource, "Success message");

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Supplier<Component>> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(mockSource).sendSuccess(captor.capture(), eq(false));
        assertThat(captor.getValue().get().getString()).isEqualTo("§aSuccess message");
    }

    @Test
    @DisplayName("sendFailure should send red message")
    void testSendFailure() {
        // Act
        CommandExecutor.sendFailure(mockSource, "Error message");

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        assertThat(captor.getValue().getString()).isEqualTo("§cError message");
    }

    @Test
    @DisplayName("sendInfo should send yellow message")
    void testSendInfo() {
        // Act
        CommandExecutor.sendInfo(mockSource, "Info message");

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Supplier<Component>> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(mockSource).sendSuccess(captor.capture(), eq(false));
        assertThat(captor.getValue().get().getString()).isEqualTo("§eInfo message");
    }

    // ==================== Integration-like Tests ====================

    @Test
    @DisplayName("Should handle multiple consecutive commands")
    void testMultipleCommands() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenReturn(mockPlayer);

        // Act - Execute multiple commands
        int result1 = CommandExecutor.executePlayerCommand(mockContext, "Error1", p -> {});
        int result2 = CommandExecutor.executePlayerCommand(mockContext, "Error2", p -> {});
        int result3 = CommandExecutor.executePlayerCommand(mockContext, "Error3", p -> {});

        // Assert
        assertThat(result1).isEqualTo(1);
        assertThat(result2).isEqualTo(1);
        assertThat(result3).isEqualTo(1);
    }

    @Test
    @DisplayName("Should properly format error messages with special characters")
    void testErrorMessageFormatting() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenReturn(mockPlayer);

        // Act
        CommandExecutor.executePlayerCommand(
            mockContext,
            "Error with special chars: äöü §",
            player -> {
                throw new Exception("Exception with: äöü §");
            }
        );

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        String message = captor.getValue().getString();
        assertThat(message).contains("äöü §");
    }

    @Test
    @DisplayName("Should handle null exception messages gracefully")
    void testNullExceptionMessage() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenReturn(mockPlayer);

        // Act
        CommandExecutor.executePlayerCommand(
            mockContext,
            "Command failed",
            player -> {
                throw new RuntimeException((String) null);
            }
        );

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockSource).sendFailure(captor.capture());
        assertThat(captor.getValue().getString()).contains("Command failed");
    }

    @Test
    @DisplayName("Should allow commands to modify player state")
    void testCommandModifiesPlayerState() throws Exception {
        // Arrange
        when(mockSource.getPlayerOrException()).thenReturn(mockPlayer);
        when(mockPlayer.getName()).thenReturn(Component.literal("TestPlayer"));

        // Act
        int result = CommandExecutor.executePlayerCommand(
            mockContext,
            "Error",
            player -> {
                // Simulate modifying player
                assertThat(player.getName().getString()).isEqualTo("TestPlayer");
            }
        );

        // Assert
        assertThat(result).isEqualTo(1);
    }
}
