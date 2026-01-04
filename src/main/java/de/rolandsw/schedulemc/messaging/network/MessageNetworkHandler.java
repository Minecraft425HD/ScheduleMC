package de.rolandsw.schedulemc.messaging.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler for Messaging Packets
 */
public class MessageNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "messaging_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // Client -> Server: Send message
        INSTANCE.messageBuilder(SendMessagePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(SendMessagePacket::decode)
            .encoder(SendMessagePacket::encode)
            .consumerMainThread(SendMessagePacket::handle)
            .add();

        // Server -> Client: Receive message notification
        INSTANCE.messageBuilder(ReceiveMessagePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(ReceiveMessagePacket::decode)
            .encoder(ReceiveMessagePacket::encode)
            .consumerMainThread(ReceiveMessagePacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
