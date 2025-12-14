package de.rolandsw.schedulemc.messaging.network;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.messaging.MessageManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from client to server when sending a message
 */
public class SendMessagePacket {
    private final UUID recipientUUID;
    private final String recipientName;
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
        buf.writeUtf(recipientName);
        buf.writeBoolean(isRecipientPlayer);
        buf.writeUtf(content);
    }

    public static SendMessagePacket decode(FriendlyByteBuf buf) {
        return new SendMessagePacket(
            buf.readUUID(),
            buf.readUtf(),
            buf.readBoolean(),
            buf.readUtf()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                // Send message on server side
                MessageManager.sendMessage(
                    sender.getUUID(),
                    sender.getName().getString(),
                    true, // sender is always a player
                    recipientUUID,
                    recipientName,
                    isRecipientPlayer,
                    content
                );

                ScheduleMC.LOGGER.debug("Message from {} to {}: {}",
                    sender.getName().getString(), recipientName, content);

                // If recipient is online player, send notification
                if (isRecipientPlayer) {
                    ServerPlayer recipient = sender.getServer().getPlayerList().getPlayer(recipientUUID);
                    if (recipient != null) {
                        // Send notification packet to recipient
                        MessageNetworkHandler.sendToClient(
                            new ReceiveMessagePacket(
                                sender.getUUID(),
                                sender.getName().getString(),
                                true,
                                content
                            ),
                            recipient
                        );
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
