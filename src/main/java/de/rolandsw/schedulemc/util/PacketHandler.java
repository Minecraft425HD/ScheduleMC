package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
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

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Standard Packet-Handler mit automatischem Player-Check
     *
     * @param ctx Network context
     * @param handler Handler-Logic die den Player benötigt
     */
    public static void handleServerPacket(
            @Nonnull Supplier<NetworkEvent.Context> ctx,
            @Nonnull Consumer<ServerPlayer> handler
    ) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                try {
                    handler.accept(player);
                } catch (Exception e) {  // Intentionally catching all exceptions - packet handler must not crash server
                    player.sendSystemMessage(
                        Component.literal("§cPacket-Fehler: " + e.getMessage())
                    );
                    // Log but don't crash
                    LOGGER.error("Packet handling error for player {}: {}", player.getName().getString(), e.getMessage(), e);
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
            @Nonnull Supplier<NetworkEvent.Context> ctx,
            int permissionLevel,
            @Nonnull Consumer<ServerPlayer> handler
    ) {
        handleServerPacket(ctx, player -> {
            if (!player.hasPermissions(permissionLevel)) {
                player.sendSystemMessage(
                    Component.literal("§cKeine Berechtigung für diese Aktion!")
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
            @Nonnull Supplier<NetworkEvent.Context> ctx,
            @Nonnull Consumer<ServerPlayer> handler,
            @Nonnull BiConsumer<ServerPlayer, Exception> errorHandler
    ) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                try {
                    handler.accept(player);
                } catch (Exception e) {  // Intentionally catching all exceptions - custom error handler provided
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
            @Nonnull Supplier<NetworkEvent.Context> ctx,
            @Nonnull Runnable handler
    ) {
        ctx.get().enqueueWork(() -> {
            try {
                handler.run();
            } catch (Exception e) {  // Intentionally catching all exceptions - client packet handler must not crash
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
            @Nonnull Supplier<NetworkEvent.Context> ctx,
            @Nonnull Runnable handler
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
    public static void sendSuccess(@Nonnull ServerPlayer player, @Nonnull String message) {
        player.sendSystemMessage(Component.literal("§a✓ " + message));
    }

    /**
     * Sendet eine Fehler-Nachricht an den Player
     */
    public static void sendError(@Nonnull ServerPlayer player, @Nonnull String message) {
        player.sendSystemMessage(Component.literal("§c✗ " + message));
    }

    /**
     * Sendet eine Info-Nachricht an den Player
     */
    public static void sendInfo(@Nonnull ServerPlayer player, @Nonnull String message) {
        player.sendSystemMessage(Component.literal("§7" + message));
    }

    /**
     * Sendet eine Warnung an den Player
     */
    public static void sendWarning(@Nonnull ServerPlayer player, @Nonnull String message) {
        player.sendSystemMessage(Component.literal("§e⚠ " + message));
    }
}
