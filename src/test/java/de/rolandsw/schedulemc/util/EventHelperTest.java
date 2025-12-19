package de.rolandsw.schedulemc.util;

import de.rolandsw.schedulemc.test.MinecraftTestBootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.LogicalSide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventHelper
 *
 * Tests cover:
 * - handleServerPlayerEvent and variants
 * - handleServerTick and handleServerTickEnd
 * - Block events (break, place)
 * - Player interact events
 * - Combat events
 * - Item events
 * - Tick events
 * - Login/Logout events
 * - Error handling
 * - Guard methods
 */
class EventHelperTest {

    private ServerPlayer mockServerPlayer;
    private Player mockPlayer;
    private Entity mockEntity;
    private Level mockLevel;

    @BeforeAll
    static void initMinecraft() {
        MinecraftTestBootstrap.init();
    }

    @BeforeEach
    void setUp() {
        mockServerPlayer = mock(ServerPlayer.class);
        mockPlayer = mock(Player.class);
        mockEntity = mock(Entity.class);
        mockLevel = mock(Level.class);

        when(mockServerPlayer.level()).thenReturn(mockLevel);
        when(mockPlayer.level()).thenReturn(mockLevel);
        when(mockEntity.level()).thenReturn(mockLevel);
        when(mockLevel.isClientSide()).thenReturn(false);
    }

    // ==================== ServerPlayer Event Tests ====================

    @Test
    @DisplayName("handleServerPlayerEvent should execute with ServerPlayer")
    void testHandleServerPlayerEventSuccess() {
        // Arrange
        EntityEvent event = mock(EntityEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleServerPlayerEvent(event, player -> {
            assertThat(player).isSameAs(mockServerPlayer);
            executed[0] = true;
        });

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleServerPlayerEvent should skip non-ServerPlayer")
    void testHandleServerPlayerEventSkipsNonServerPlayer() {
        // Arrange
        EntityEvent event = mock(EntityEvent.class);
        when(event.getEntity()).thenReturn(mockEntity);

        // Act
        EventHelper.handleServerPlayerEvent(event, player ->
            fail("Should not execute for non-ServerPlayer")
        );

        // No assertions needed - test passes if handler not called
    }

    @Test
    @DisplayName("handleServerPlayerLivingEvent should work with LivingEvents")
    void testHandleServerPlayerLivingEvent() {
        // Arrange
        LivingEvent event = mock(LivingEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleServerPlayerLivingEvent(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleServerPlayerOnly should work with PlayerEvents")
    void testHandleServerPlayerOnly() {
        // Arrange
        PlayerEvent event = mock(PlayerEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleServerPlayerOnly(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handlePlayerEvent should work with any Player")
    void testHandlePlayerEvent() {
        // Arrange
        PlayerEvent event = mock(PlayerEvent.class);
        when(event.getEntity()).thenReturn(mockPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handlePlayerEvent(event, player -> {
            assertThat(player).isSameAs(mockPlayer);
            executed[0] = true;
        });

        // Assert
        assertThat(executed[0]).isTrue();
    }

    // ==================== Server Tick Tests ====================

    @Test
    @DisplayName("handleServerTick should execute on correct side and phase")
    void testHandleServerTickCorrectSideAndPhase() {
        // Arrange
        TickEvent.ServerTickEvent event = mock(TickEvent.ServerTickEvent.class);
        MinecraftServer mockServer = mock(MinecraftServer.class);
        // Use field access instead of assignment for final fields
        when(event.side).thenReturn(LogicalSide.SERVER);
        when(event.phase).thenReturn(TickEvent.Phase.END);
        when(event.getServer()).thenReturn(mockServer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleServerTick(event, TickEvent.Phase.END, server -> {
            assertThat(server).isSameAs(mockServer);
            executed[0] = true;
        });

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleServerTick should skip wrong phase")
    void testHandleServerTickSkipsWrongPhase() {
        // Arrange
        TickEvent.ServerTickEvent event = mock(TickEvent.ServerTickEvent.class);
        when(event.side).thenReturn(LogicalSide.SERVER);
        when(event.phase).thenReturn(TickEvent.Phase.START);

        // Act
        EventHelper.handleServerTick(event, TickEvent.Phase.END, server ->
            fail("Should not execute on wrong phase")
        );
    }

    @Test
    @DisplayName("handleServerTick should skip client side")
    void testHandleServerTickSkipsClientSide() {
        // Arrange
        TickEvent.ServerTickEvent event = mock(TickEvent.ServerTickEvent.class);
        when(event.side).thenReturn(LogicalSide.CLIENT);
        when(event.phase).thenReturn(TickEvent.Phase.END);

        // Act
        EventHelper.handleServerTick(event, TickEvent.Phase.END, server ->
            fail("Should not execute on client side")
        );
    }

    @Test
    @DisplayName("handleServerTickEnd should use END phase")
    void testHandleServerTickEnd() {
        // Arrange
        TickEvent.ServerTickEvent event = mock(TickEvent.ServerTickEvent.class);
        MinecraftServer mockServer = mock(MinecraftServer.class);
        when(event.side).thenReturn(LogicalSide.SERVER);
        when(event.phase).thenReturn(TickEvent.Phase.END);
        when(event.getServer()).thenReturn(mockServer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleServerTickEnd(event, server -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    // ==================== Block Event Tests ====================

    @Test
    @DisplayName("handleBlockBreak should execute with player")
    void testHandleBlockBreak() {
        // Arrange
        BlockEvent.BreakEvent event = mock(BlockEvent.BreakEvent.class);
        when(event.getPlayer()).thenReturn(mockPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleBlockBreak(event, player -> {
            assertThat(player).isSameAs(mockPlayer);
            executed[0] = true;
        });

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleBlockPlace should execute with player entity")
    void testHandleBlockPlace() {
        // Arrange
        BlockEvent.EntityPlaceEvent event = mock(BlockEvent.EntityPlaceEvent.class);
        when(event.getEntity()).thenReturn(mockPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleBlockPlace(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleBlockPlace should skip non-player entities")
    void testHandleBlockPlaceSkipsNonPlayer() {
        // Arrange
        BlockEvent.EntityPlaceEvent event = mock(BlockEvent.EntityPlaceEvent.class);
        when(event.getEntity()).thenReturn(mockEntity);

        // Act
        EventHelper.handleBlockPlace(event, player ->
            fail("Should not execute for non-player")
        );
    }

    // ==================== Player Interact Tests ====================

    @Test
    @DisplayName("handleRightClickBlock should execute")
    void testHandleRightClickBlock() {
        // Arrange
        PlayerInteractEvent.RightClickBlock event = mock(PlayerInteractEvent.RightClickBlock.class);
        when(event.getEntity()).thenReturn(mockPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleRightClickBlock(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleLeftClickBlock should execute")
    void testHandleLeftClickBlock() {
        // Arrange
        PlayerInteractEvent.LeftClickBlock event = mock(PlayerInteractEvent.LeftClickBlock.class);
        when(event.getEntity()).thenReturn(mockPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handleLeftClickBlock(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleEntityInteract should skip client side")
    void testHandleEntityInteractSkipsClient() {
        // Arrange
        PlayerInteractEvent.EntityInteract event = mock(PlayerInteractEvent.EntityInteract.class);
        when(event.getLevel()).thenReturn(mockLevel);
        when(mockLevel.isClientSide()).thenReturn(true);

        // Act
        EventHelper.handleEntityInteract(event, player ->
            fail("Should not execute on client side")
        );
    }

    @Test
    @DisplayName("handleEntityInteract should execute on server")
    void testHandleEntityInteract() {
        // Arrange
        PlayerInteractEvent.EntityInteract event = mock(PlayerInteractEvent.EntityInteract.class);
        when(event.getEntity()).thenReturn(mockPlayer);
        when(event.getLevel()).thenReturn(mockLevel);
        when(mockLevel.isClientSide()).thenReturn(false);
        boolean[] executed = {false};

        // Act
        EventHelper.handleEntityInteract(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    // ==================== Combat Event Tests ====================

    @Test
    @DisplayName("handleAttackEntity should execute on server")
    void testHandleAttackEntity() {
        // Arrange
        AttackEntityEvent event = mock(AttackEntityEvent.class);
        when(event.getEntity()).thenReturn(mockPlayer);
        when(mockPlayer.level()).thenReturn(mockLevel);
        when(mockLevel.isClientSide()).thenReturn(false);
        boolean[] executed = {false};

        // Act
        EventHelper.handleAttackEntity(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleAttackEntity should skip client side")
    void testHandleAttackEntitySkipsClient() {
        // Arrange
        AttackEntityEvent event = mock(AttackEntityEvent.class);
        when(event.getEntity()).thenReturn(mockPlayer);
        when(mockPlayer.level()).thenReturn(mockLevel);
        when(mockLevel.isClientSide()).thenReturn(true);

        // Act
        EventHelper.handleAttackEntity(event, player ->
            fail("Should not execute on client")
        );
    }

    @Test
    @DisplayName("handlePlayerDeath should execute for ServerPlayer")
    void testHandlePlayerDeath() {
        // Arrange
        LivingDeathEvent event = mock(LivingDeathEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handlePlayerDeath(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handlePlayerDrops should execute for Player")
    void testHandlePlayerDrops() {
        // Arrange
        LivingDropsEvent event = mock(LivingDropsEvent.class);
        when(event.getEntity()).thenReturn(mockPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handlePlayerDrops(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    // ==================== Login/Logout Event Tests ====================

    @Test
    @DisplayName("handlePlayerJoin should execute for ServerPlayer")
    void testHandlePlayerJoin() {
        // Arrange
        PlayerEvent.PlayerLoggedInEvent event = mock(PlayerEvent.PlayerLoggedInEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handlePlayerJoin(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handlePlayerLogout should execute for ServerPlayer")
    void testHandlePlayerLogout() {
        // Arrange
        PlayerEvent.PlayerLoggedOutEvent event = mock(PlayerEvent.PlayerLoggedOutEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handlePlayerLogout(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handlePlayerRespawn should execute for ServerPlayer")
    void testHandlePlayerRespawn() {
        // Arrange
        PlayerEvent.PlayerRespawnEvent event = mock(PlayerEvent.PlayerRespawnEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);
        boolean[] executed = {false};

        // Act
        EventHelper.handlePlayerRespawn(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("Should handle exceptions gracefully")
    void testExceptionHandling() {
        // Arrange
        EntityEvent event = mock(EntityEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);

        // Act - Should not throw
        assertThatCode(() ->
            EventHelper.handleServerPlayerEvent(event, player -> {
                throw new RuntimeException("Test exception");
            })
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("handleEvent should execute generic handlers")
    void testHandleEvent() {
        // Arrange
        boolean[] executed = {false};

        // Act
        EventHelper.handleEvent(() -> executed[0] = true, "TestEvent");

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handleEvent should catch exceptions")
    void testHandleEventWithException() {
        // Act - Should not throw
        assertThatCode(() ->
            EventHelper.handleEvent(() -> {
                throw new RuntimeException("Generic event error");
            }, "TestEvent")
        ).doesNotThrowAnyException();
    }

    // ==================== Guard Method Tests ====================

    @Test
    @DisplayName("isServerPlayer should correctly identify ServerPlayer")
    void testIsServerPlayer() {
        // Assert
        assertThat(EventHelper.isServerPlayer(mockServerPlayer)).isTrue();
        assertThat(EventHelper.isServerPlayer(mockPlayer)).isFalse();
        assertThat(EventHelper.isServerPlayer(mockEntity)).isFalse();
    }

    @Test
    @DisplayName("isServerSide should correctly identify server side")
    void testIsServerSide() {
        // Arrange
        TickEvent.ServerTickEvent serverEvent = mock(TickEvent.ServerTickEvent.class);
        when(serverEvent.side).thenReturn(LogicalSide.SERVER);

        TickEvent.ServerTickEvent clientEvent = mock(TickEvent.ServerTickEvent.class);
        when(clientEvent.side).thenReturn(LogicalSide.CLIENT);

        // Assert
        assertThat(EventHelper.isServerSide(serverEvent)).isTrue();
        assertThat(EventHelper.isServerSide(clientEvent)).isFalse();
    }

    @Test
    @DisplayName("isEndPhase should correctly identify END phase")
    void testIsEndPhase() {
        // Arrange
        TickEvent.ServerTickEvent endEvent = mock(TickEvent.ServerTickEvent.class);
        when(endEvent.phase).thenReturn(TickEvent.Phase.END);

        TickEvent.ServerTickEvent startEvent = mock(TickEvent.ServerTickEvent.class);
        when(startEvent.phase).thenReturn(TickEvent.Phase.START);

        // Assert
        assertThat(EventHelper.isEndPhase(endEvent)).isTrue();
        assertThat(EventHelper.isEndPhase(startEvent)).isFalse();
    }

    // ==================== Tick Event Tests ====================

    @Test
    @DisplayName("handlePlayerTickEnd should execute on END phase server-side")
    void testHandlePlayerTickEnd() {
        // Arrange
        TickEvent.PlayerTickEvent event = mock(TickEvent.PlayerTickEvent.class);
        when(event.phase).thenReturn(TickEvent.Phase.END);
        when(event.player).thenReturn(mockPlayer);
        when(mockPlayer.level()).thenReturn(mockLevel);
        when(mockLevel.isClientSide()).thenReturn(false);
        boolean[] executed = {false};

        // Act
        EventHelper.handlePlayerTickEnd(event, player -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    @Test
    @DisplayName("handlePlayerTickEnd should skip START phase")
    void testHandlePlayerTickEndSkipsStartPhase() {
        // Arrange
        TickEvent.PlayerTickEvent event = mock(TickEvent.PlayerTickEvent.class);
        when(event.phase).thenReturn(TickEvent.Phase.START);
        when(event.player).thenReturn(mockPlayer);

        // Act
        EventHelper.handlePlayerTickEnd(event, player ->
            fail("Should not execute on START phase")
        );
    }

    @Test
    @DisplayName("handleLivingTick should execute on server")
    void testHandleLivingTick() {
        // Arrange
        LivingEvent.LivingTickEvent event = mock(LivingEvent.LivingTickEvent.class);
        when(event.getEntity()).thenReturn(mockServerPlayer);
        when(mockServerPlayer.level()).thenReturn(mockLevel);
        when(mockLevel.isClientSide()).thenReturn(false);
        boolean[] executed = {false};

        // Act
        EventHelper.handleLivingTick(event, () -> executed[0] = true);

        // Assert
        assertThat(executed[0]).isTrue();
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle null safely in guards")
    void testNullSafety() {
        // Assert - Should not throw
        assertThatCode(() -> {
            EventHelper.isServerPlayer(null);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle multiple events in sequence")
    void testMultipleEvents() {
        // Arrange
        EntityEvent event1 = mock(EntityEvent.class);
        EntityEvent event2 = mock(EntityEvent.class);
        EntityEvent event3 = mock(EntityEvent.class);
        when(event1.getEntity()).thenReturn(mockServerPlayer);
        when(event2.getEntity()).thenReturn(mockServerPlayer);
        when(event3.getEntity()).thenReturn(mockServerPlayer);
        int[] counter = {0};

        // Act
        EventHelper.handleServerPlayerEvent(event1, p -> counter[0]++);
        EventHelper.handleServerPlayerEvent(event2, p -> counter[0]++);
        EventHelper.handleServerPlayerEvent(event3, p -> counter[0]++);

        // Assert
        assertThat(counter[0]).isEqualTo(3);
    }
}
