package de.rolandsw.schedulemc.messaging.network;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.util.RateLimiter;
import de.rolandsw.schedulemc.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from client to server when sending a message
 * OPTIMIERT: recipientName wird nur für NPCs gesendet (Player-Namen werden server-seitig aufgelöst)
 * SICHERHEIT: Rate-Limited gegen Message-Spam
 */
public class SendMessagePacket {
    // SICHERHEIT: Rate Limiting - Max 10 messages per second to prevent spam
    private static final RateLimiter MESSAGE_RATE_LIMITER = new RateLimiter("sendMessage", 10);

    private final UUID recipientUUID;
    private final String recipientName;  // Nur für NPCs nötig
    private final boolean isRecipientPlayer;
    private final String content;

    public SendMessagePacket(UUID recipientUUID, String recipientName, boolean isRecipientPlayer, String content) {
        this.recipientUUID = recipientUUID;
        this.recipientName = recipientName;
        this.isRecipientPlayer = isRecipientPlayer;
        this.content = content;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(recipientUUID);
        buf.writeBoolean(isRecipientPlayer);
        // OPTIMIERT: Sende recipientName nur für NPCs (spart Bandbreite bei Player-Nachrichten)
        if (!isRecipientPlayer) {
            buf.writeUtf(recipientName);
        }
        buf.writeUtf(content);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     * + Input-Sanitization gegen Command-Injection
     */
    public static SendMessagePacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        boolean isPlayer = buf.readBoolean();
        // OPTIMIERT: Lese recipientName nur für NPCs
        String name = isPlayer ? "" : StringUtils.sanitizeUserInput(buf.readUtf(64)); // NPC name max 64 chars
        String content = StringUtils.sanitizeUserInput(buf.readUtf(1024)); // Message max 1024 chars + SANITIZED
        return new SendMessagePacket(uuid, name, isPlayer, content);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, sender -> {
            // SICHERHEIT: Rate Limiting - prevent message spam/DoS attacks
            if (!MESSAGE_RATE_LIMITER.allowOperation(sender.getUUID())) {
                sender.sendSystemMessage(Component.literal("⚠ Zu viele Nachrichten! Bitte langsamer.")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // OPTIMIERT: Löse Player-Namen server-seitig auf
            String resolvedRecipientName = recipientName;
            ServerPlayer recipientPlayer = null;

            if (isRecipientPlayer) {
                recipientPlayer = sender.getServer().getPlayerList().getPlayer(recipientUUID);
                if (recipientPlayer != null) {
                    resolvedRecipientName = recipientPlayer.getName().getString();
                } else {
                    // Offline-Player: Versuche aus GameProfile
                    var cache = sender.getServer().getProfileCache();
                    if (cache != null) {
                        var profile = cache.get(recipientUUID);
                        resolvedRecipientName = profile.map(p -> p.getName()).orElse("Unbekannt");
                    } else {
                        resolvedRecipientName = "Unbekannt";
                    }
                }
            }

            // Send message on server side
            MessageManager.sendMessage(
                sender.getUUID(),
                sender.getName().getString(),
                true, // sender is always a player
                recipientUUID,
                resolvedRecipientName,
                isRecipientPlayer,
                content
            );

            ScheduleMC.LOGGER.debug("Message from {} to {}: {}",
                sender.getName().getString(), resolvedRecipientName, content);

            // If recipient is online player, send notification
            if (recipientPlayer != null) {
                // Send notification packet to recipient
                MessageNetworkHandler.sendToClient(
                    new ReceiveMessagePacket(
                        sender.getUUID(),
                        sender.getName().getString(),
                        true,
                        content
                    ),
                    recipientPlayer
                );
            }
        });
    }
}
