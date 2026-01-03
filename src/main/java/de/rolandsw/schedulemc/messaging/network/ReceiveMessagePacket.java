package de.rolandsw.schedulemc.messaging.network;

import de.rolandsw.schedulemc.messaging.MessageNotificationOverlay;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from server to client when receiving a message
 */
public class ReceiveMessagePacket {
    private final UUID senderUUID;
    private final String senderName;
    private final boolean isSenderPlayer;
    private final String content;

    public ReceiveMessagePacket(UUID senderUUID, String senderName, boolean isSenderPlayer, String content) {
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.isSenderPlayer = isSenderPlayer;
        this.content = content;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(senderUUID);
        buf.writeUtf(senderName);
        buf.writeBoolean(isSenderPlayer);
        buf.writeUtf(content);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static ReceiveMessagePacket decode(FriendlyByteBuf buf) {
        return new ReceiveMessagePacket(
            buf.readUUID(),
            buf.readUtf(64),   // Sender name max 64 chars
            buf.readBoolean(),
            buf.readUtf(1024)  // Message max 1024 chars
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleClientPacket(ctx, () -> {
            // Client-side only
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                // Show notification overlay
                MessageNotificationOverlay.showNotification(senderName, content);
            });
        });
    }
}
