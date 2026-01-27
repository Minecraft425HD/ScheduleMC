package de.rolandsw.schedulemc.util;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility für konsistentes Packet-Handling
 * Reduziert Boilerplate in Packet handle() Methoden
 *
 * Pattern inspiriert von CommandExecutor (Phase D)
 */
public class PacketHandler {

    /**
     * Standard Packet-Handler mit automatischem Player-Check
     *
     * @param ctx Network context
     * @param handler Handler-Logic die den Player benötigt
     */
    public static void handleServerPacket(
            Supplier<NetworkEvent.Context> ctx,
            Consumer<ServerPlayer> handler
    ) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                try {
                    handler.accept(player);
                } catch (Exception e) {
                    player.sendSystemMessage(
                        Component.translatable("error.packet.failed", e.getMessage())
                    );
                    // Log but don't crash
                    ScheduleMC.LOGGER.error("Exception handling packet for player {}", player.getName().getString(), e);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Packet-Handler mit Permission-Check (für Admin-Funktionen)
     *
     * @param ctx Network context
     * @param permissionLevel Benötigtes Permission Level (2 = Admin, 4 = Owner)
     * @param handler Handler-Logic
     */
    public static void handleAdminPacket(
            Supplier<NetworkEvent.Context> ctx,
            int permissionLevel,
            Consumer<ServerPlayer> handler
    ) {
        handleServerPacket(ctx, player -> {
            if (!player.hasPermissions(permissionLevel)) {
                player.sendSystemMessage(
                    Component.translatable("message.common.no_permission_action")
                );
                return;
            }
            handler.accept(player);
        });
    }

    /**
     * Packet-Handler mit Custom Error-Handler
     *
     * @param ctx Network context
     * @param handler Handler-Logic
     * @param errorHandler Custom Error-Handler
     */
    public static void handleServerPacketWithErrorHandler(
            Supplier<NetworkEvent.Context> ctx,
            Consumer<ServerPlayer> handler,
            BiConsumer<ServerPlayer, Exception> errorHandler
    ) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                try {
                    handler.accept(player);
                } catch (Exception e) {
                    errorHandler.accept(player, e);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Client-seitiger Packet-Handler (ohne Player-Check)
     *
     * @param ctx Network context
     * @param handler Handler-Logic
     */
    public static void handleClientPacket(
            Supplier<NetworkEvent.Context> ctx,
            Runnable handler
    ) {
        ctx.get().enqueueWork(() -> {
            try {
                handler.run();
            } catch (Exception e) {
                // Client-side logging
                ScheduleMC.LOGGER.error("Client packet error: {}", e.getMessage(), e);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Generic Packet-Handler (für spezielle Fälle)
     *
     * @param ctx Network context
     * @param handler Handler-Logic
     */
    public static void handlePacket(
            Supplier<NetworkEvent.Context> ctx,
            Runnable handler
    ) {
        ctx.get().enqueueWork(handler);
        ctx.get().setPacketHandled(true);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Sendet eine Erfolgs-Nachricht an den Player
     */
    public static void sendSuccess(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.translatable("message.packet.success", message));
    }

    /**
     * Sendet eine Fehler-Nachricht an den Player
     */
    public static void sendError(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.translatable("message.packet.error", message));
    }

    /**
     * Sendet eine Info-Nachricht an den Player
     */
    public static void sendInfo(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.translatable("message.packet.info", message));
    }

    /**
     * Sendet eine Warnung an den Player
     */
    public static void sendWarning(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.translatable("message.packet.warning", message));
    }
}
