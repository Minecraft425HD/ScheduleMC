package de.rolandsw.schedulemc.level.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler fuer ProducerLevel Packets.
 */
public class ProducerLevelNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "producer_level_network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // Client -> Server: Request ProducerLevel Data
        INSTANCE.messageBuilder(RequestProducerLevelDataPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestProducerLevelDataPacket::decode)
                .encoder(RequestProducerLevelDataPacket::encode)
                .consumerMainThread(RequestProducerLevelDataPacket::handle)
                .add();

        // Server -> Client: Sync ProducerLevel Data
        INSTANCE.messageBuilder(SyncProducerLevelDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncProducerLevelDataPacket::decode)
                .encoder(SyncProducerLevelDataPacket::encode)
                .consumerMainThread(SyncProducerLevelDataPacket::handle)
                .add();

        // Server -> Client: Level-Up Notification
        INSTANCE.messageBuilder(LevelUpNotificationPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LevelUpNotificationPacket::decode)
                .encoder(LevelUpNotificationPacket::encode)
                .consumerMainThread(LevelUpNotificationPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
