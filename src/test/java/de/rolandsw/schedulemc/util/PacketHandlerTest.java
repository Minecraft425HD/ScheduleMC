package de.rolandsw.schedulemc.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PacketHandler
 *
 * Tests cover:
 * - handleServerPacket with valid/null player
 * - handleAdminPacket with permission checks
 * - handleServerPacketWithErrorHandler
 * - handleClientPacket
 * - handlePacket (generic)
 * - Helper methods (sendSuccess, sendError, sendInfo, sendWarning)
 * - Exception handling
 *
 * NOTE: These tests are disabled because they require mocking Minecraft classes
 * (ServerPlayer, NetworkEvent.Context, etc.) which triggers static initialization
 * of Minecraft's registry system. This causes NoClassDefFoundError in unit test
 * environments. These tests require a full Minecraft test harness.
 */
@Disabled("Requires full Minecraft test environment - mocking ServerPlayer triggers static initialization")
class PacketHandlerTest {

    private NetworkEvent.Context mockContext;
    private Supplier<NetworkEvent.Context> contextSupplier;
    private ServerPlayer mockPlayer;

    @BeforeEach
    void setUp() {
        mockContext = mock(NetworkEvent.Context.class);
        contextSupplier = () -> mockContext;
        mockPlayer = mock(ServerPlayer.class);

        // Default: enqueueWork executes immediately for testing
        when(mockContext.enqueueWork(any())).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        });
    }

    // ==================== handleServerPacket Tests ====================

    @Test
    @DisplayName("handleServerPacket should execute handler with valid player")
    void testHandleServerPacketSuccess() {
        // Arrange
        when(mockContext.getSender()).thenReturn(mockPlayer);
        boolean[] handlerExecuted = {false};

        // Act
        PacketHandler.handleServerPacket(contextSupplier, player -> {
            assertThat(player).isSameAs(mockPlayer);
            handlerExecuted[0] = true;
        });

        // Assert
        assertThat(handlerExecuted[0]).isTrue();
        verify(mockContext).setPacketHandled(true);
        verify(mockPlayer, never()).sendSystemMessage(any());
    }

    @Test
    @DisplayName("handleServerPacket should skip handler when player is null")
    void testHandleServerPacketNullPlayer() {
        // Arrange
        when(mockContext.getSender()).thenReturn(null);

        // Act
        PacketHandler.handleServerPacket(contextSupplier, player ->
            fail("Handler should not be called with null player")
        );

        // Assert
        verify(mockContext).setPacketHandled(true);
    }

    @Test
    @DisplayName("handleServerPacket should catch and handle exceptions")
    void testHandleServerPacketWithException() {
        // Arrange
        when(mockContext.getSender()).thenReturn(mockPlayer);

        // Act
        PacketHandler.handleServerPacket(contextSupplier, player -> {
            throw new RuntimeException("Test exception");
        });

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockPlayer).sendSystemMessage(captor.capture());
        assertThat(captor.getValue().getString())
            .contains("Packet-Fehler")
            .contains("Test exception");
        verify(mockContext).setPacketHandled(true);
    }

    // ==================== handleAdminPacket Tests ====================

    @Test
    @DisplayName("handleAdminPacket should execute with sufficient permissions")
    void testHandleAdminPacketWithPermission() {
        // Arrange
        when(mockContext.getSender()).thenReturn(mockPlayer);
        when(mockPlayer.hasPermissions(2)).thenReturn(true);
        boolean[] handlerExecuted = {false};

        // Act
        PacketHandler.handleAdminPacket(contextSupplier, 2, player -> {
            handlerExecuted[0] = true;
        });

        // Assert
        assertThat(handlerExecuted[0]).isTrue();
        verify(mockPlayer).hasPermissions(2);
        verify(mockContext).setPacketHandled(true);
    }

    @Test
    @DisplayName("handleAdminPacket should deny access without permissions")
    void testHandleAdminPacketWithoutPermission() {
        // Arrange
        when(mockContext.getSender()).thenReturn(mockPlayer);
        when(mockPlayer.hasPermissions(4)).thenReturn(false);

        // Act
        PacketHandler.handleAdminPacket(contextSupplier, 4, player ->
            fail("Handler should not execute without permissions")
        );

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockPlayer).sendSystemMessage(captor.capture());
        assertThat(captor.getValue().getString()).contains("Keine Berechtigung");
        verify(mockContext).setPacketHandled(true);
    }

    @Test
    @DisplayName("handleAdminPacket should test various permission levels")
    void testHandleAdminPacketPermissionLevels() {
        // Test level 2 (Admin)
        when(mockContext.getSender()).thenReturn(mockPlayer);
        when(mockPlayer.hasPermissions(2)).thenReturn(true);
        boolean[] executed = {false};
        PacketHandler.handleAdminPacket(contextSupplier, 2, p -> executed[0] = true);
        assertThat(executed[0]).isTrue();

        // Reset
        reset(mockContext, mockPlayer);
        setUp();

        // Test level 4 (Owner) - denied
        when(mockContext.getSender()).thenReturn(mockPlayer);
        when(mockPlayer.hasPermissions(4)).thenReturn(false);
        executed[0] = false;
        PacketHandler.handleAdminPacket(contextSupplier, 4, p -> executed[0] = true);
        assertThat(executed[0]).isFalse();
    }

    // ==================== handleServerPacketWithErrorHandler Tests ====================

    @Test
    @DisplayName("handleServerPacketWithErrorHandler should use custom error handler")
    void testHandleServerPacketWithCustomErrorHandler() {
        // Arrange
        when(mockContext.getSender()).thenReturn(mockPlayer);
        boolean[] customErrorHandled = {false};

        // Act
        PacketHandler.handleServerPacketWithErrorHandler(
            contextSupplier,
            player -> {
                throw new IllegalArgumentException("Custom error");
            },
            (player, exception) -> {
                assertThat(player).isSameAs(mockPlayer);
                assertThat(exception).isInstanceOf(IllegalArgumentException.class);
                assertThat(exception.getMessage()).isEqualTo("Custom error");
                customErrorHandled[0] = true;
            }
        );

        // Assert
        assertThat(customErrorHandled[0]).isTrue();
        verify(mockContext).setPacketHandled(true);
        verify(mockPlayer, never()).sendSystemMessage(any()); // Custom handler, no default message
    }

    @Test
    @DisplayName("handleServerPacketWithErrorHandler should succeed without errors")
    void testHandleServerPacketWithErrorHandlerSuccess() {
        // Arrange
        when(mockContext.getSender()).thenReturn(mockPlayer);
        boolean[] handlerExecuted = {false};

        // Act
        PacketHandler.handleServerPacketWithErrorHandler(
            contextSupplier,
            player -> {
                handlerExecuted[0] = true;
            },
            (player, exception) -> fail("Error handler should not be called")
        );

        // Assert
        assertThat(handlerExecuted[0]).isTrue();
        verify(mockContext).setPacketHandled(true);
    }

    // ==================== handleClientPacket Tests ====================

    @Test
    @DisplayName("handleClientPacket should execute handler")
    void testHandleClientPacketSuccess() {
        // Arrange
        boolean[] handlerExecuted = {false};

        // Act
        PacketHandler.handleClientPacket(contextSupplier, () -> {
            handlerExecuted[0] = true;
        });

        // Assert
        assertThat(handlerExecuted[0]).isTrue();
        verify(mockContext).setPacketHandled(true);
    }

    @Test
    @DisplayName("handleClientPacket should catch exceptions")
    void testHandleClientPacketWithException() {
        // Act - Should not throw, just log
        assertThatCode(() ->
            PacketHandler.handleClientPacket(contextSupplier, () -> {
                throw new RuntimeException("Client error");
            })
        ).doesNotThrowAnyException();

        // Assert
        verify(mockContext).setPacketHandled(true);
    }

    // ==================== handlePacket Tests ====================

    @Test
    @DisplayName("handlePacket should execute generic handler")
    void testHandlePacketGeneric() {
        // Arrange
        boolean[] handlerExecuted = {false};

        // Act
        PacketHandler.handlePacket(contextSupplier, () -> {
            handlerExecuted[0] = true;
        });

        // Assert
        assertThat(handlerExecuted[0]).isTrue();
        verify(mockContext).setPacketHandled(true);
    }

    // ==================== Helper Method Tests ====================

    @Test
    @DisplayName("sendSuccess should send green message with checkmark")
    void testSendSuccess() {
        // Act
        PacketHandler.sendSuccess(mockPlayer, "Operation completed");

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockPlayer).sendSystemMessage(captor.capture());
        assertThat(captor.getValue().getString())
            .startsWith("§a✓")
            .contains("Operation completed");
    }

    @Test
    @DisplayName("sendError should send red message with X")
    void testSendError() {
        // Act
        PacketHandler.sendError(mockPlayer, "Something failed");

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockPlayer).sendSystemMessage(captor.capture());
        assertThat(captor.getValue().getString())
            .startsWith("§c✗")
            .contains("Something failed");
    }

    @Test
    @DisplayName("sendInfo should send gray message")
    void testSendInfo() {
        // Act
        PacketHandler.sendInfo(mockPlayer, "Information");

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockPlayer).sendSystemMessage(captor.capture());
        assertThat(captor.getValue().getString())
            .startsWith("§7")
            .contains("Information");
    }

    @Test
    @DisplayName("sendWarning should send yellow message with warning symbol")
    void testSendWarning() {
        // Act
        PacketHandler.sendWarning(mockPlayer, "Be careful");

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockPlayer).sendSystemMessage(captor.capture());
        assertThat(captor.getValue().getString())
            .startsWith("§e⚠")
            .contains("Be careful");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle empty messages in helpers")
    void testHelperMethodsWithEmptyMessages() {
        // Act
        PacketHandler.sendSuccess(mockPlayer, "");
        PacketHandler.sendError(mockPlayer, "");
        PacketHandler.sendInfo(mockPlayer, "");
        PacketHandler.sendWarning(mockPlayer, "");

        // Assert
        verify(mockPlayer, times(4)).sendSystemMessage(any());
    }

    @Test
    @DisplayName("Should handle special characters in messages")
    void testHelperMethodsWithSpecialCharacters() {
        // Act
        PacketHandler.sendSuccess(mockPlayer, "äöü § special");

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockPlayer).sendSystemMessage(captor.capture());
        assertThat(captor.getValue().getString()).contains("äöü § special");
    }

    @Test
    @DisplayName("Should handle null exception messages")
    void testNullExceptionMessage() {
        // Arrange
        when(mockContext.getSender()).thenReturn(mockPlayer);

        // Act
        PacketHandler.handleServerPacket(contextSupplier, player -> {
            throw new RuntimeException((String) null);
        });

        // Assert
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockPlayer).sendSystemMessage(captor.capture());
        assertThat(captor.getValue().getString()).contains("Packet-Fehler");
    }

    @Test
    @DisplayName("Should handle multiple packet calls sequentially")
    void testMultiplePacketCalls() {
        // Arrange
        when(mockContext.getSender()).thenReturn(mockPlayer);
        int[] counter = {0};

        // Act
        PacketHandler.handleServerPacket(contextSupplier, p -> counter[0]++);
        PacketHandler.handleServerPacket(contextSupplier, p -> counter[0]++);
        PacketHandler.handleServerPacket(contextSupplier, p -> counter[0]++);

        // Assert
        assertThat(counter[0]).isEqualTo(3);
        verify(mockContext, times(3)).setPacketHandled(true);
    }

    @Test
    @DisplayName("Should enqueue work properly")
    void testEnqueueWork() {
        // Arrange
        boolean[] workEnqueued = {false};
        when(mockContext.enqueueWork(any())).thenAnswer(invocation -> {
            workEnqueued[0] = true;
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        });
        when(mockContext.getSender()).thenReturn(mockPlayer);

        // Act
        PacketHandler.handleServerPacket(contextSupplier, p -> {});

        // Assert
        assertThat(workEnqueued[0]).isTrue();
        verify(mockContext).enqueueWork(any());
    }
}
