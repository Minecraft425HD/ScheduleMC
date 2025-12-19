package de.rolandsw.schedulemc.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.function.Consumer;

/**
 * Utility für konsistentes Event-Handling
 * Reduziert Boilerplate und fügt automatisches Error-Handling hinzu
 *
 * Inspiriert von PacketHandler (Phase E) und CommandExecutor (Phase D)
 */
public class EventHelper {

    /**
     * Führt Event-Handler nur für ServerPlayer aus
     * Automatisches Type-Checking und Error-Handling
     *
     * @param event Event mit Entity
     * @param handler Handler-Logic
     */
    public static void handleServerPlayerEvent(EntityEvent event, Consumer<ServerPlayer> handler) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        safeExecute(() -> handler.accept(player), "handleServerPlayerEvent");
    }

    /**
     * Führt Event-Handler nur für ServerPlayer aus (Living-Events)
     *
     * @param event LivingEvent
     * @param handler Handler-Logic
     */
    public static void handleServerPlayerLivingEvent(LivingEvent event, Consumer<ServerPlayer> handler) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        safeExecute(() -> handler.accept(player), "handleServerPlayerLivingEvent");
    }

    /**
     * Führt Event-Handler nur für ServerPlayer aus (Player-Events)
     *
     * @param event PlayerEvent
     * @param handler Handler-Logic
     */
    public static void handleServerPlayerOnly(PlayerEvent event, Consumer<ServerPlayer> handler) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        safeExecute(() -> handler.accept(player), "handleServerPlayerOnly");
    }

    /**
     * Führt Event-Handler für alle Players aus (Server & Client)
     *
     * @param event PlayerEvent
     * @param handler Handler-Logic
     */
    public static void handlePlayerEvent(PlayerEvent event, Consumer<Player> handler) {
        if (!(event.getEntity() instanceof Player player)) return;
        safeExecute(() -> handler.accept(player), "handlePlayerEvent");
    }

    /**
     * Server-Tick Handler mit automatischem Side/Phase Check
     *
     * @param event TickEvent
     * @param phase Gewünschte Phase (START oder END)
     * @param handler Handler-Logic
     */
    public static void handleServerTick(TickEvent.ServerTickEvent event, TickEvent.Phase phase, Runnable handler) {
        if (event.side != LogicalSide.SERVER || event.phase != phase) return;
        safeExecute(handler, "handleServerTick");
    }

    /**
     * Server-Tick Handler (nur END-Phase, häufigster Fall)
     *
     * @param event TickEvent
     * @param handler Handler-Logic
     */
    public static void handleServerTickEnd(TickEvent.ServerTickEvent event, Runnable handler) {
        handleServerTick(event, TickEvent.Phase.END, handler);
    }

    /**
     * Generic Event Handler mit Error-Handling
     *
     * @param handler Handler-Logic
     * @param eventName Event-Name für Logging
     */
    public static void handleEvent(Runnable handler, String eventName) {
        safeExecute(handler, eventName);
    }

    // =========================================================================
    // ERROR HANDLING
    // =========================================================================

    /**
     * Führt Code mit automatischem Error-Handling aus
     * Verhindert Crashes durch unbehandelte Exceptions in Event-Handlern
     */
    private static void safeExecute(Runnable handler, String context) {
        try {
            handler.run();
        } catch (Exception e) {
            System.err.println("Error in event handler (" + context + "): " + e.getMessage());
            e.printStackTrace();
            // Log but don't crash - Event handlers should be resilient
        }
    }

    // =========================================================================
    // COMMON GUARDS
    // =========================================================================

    /**
     * Prüft ob Entity ein ServerPlayer ist
     */
    public static boolean isServerPlayer(net.minecraft.world.entity.Entity entity) {
        return entity instanceof ServerPlayer;
    }

    /**
     * Prüft ob Event auf Server-Seite ist
     */
    public static boolean isServerSide(TickEvent event) {
        return event.side == LogicalSide.SERVER;
    }

    /**
     * Prüft ob Event in END-Phase ist
     */
    public static boolean isEndPhase(TickEvent event) {
        return event.phase == TickEvent.Phase.END;
    }
}
