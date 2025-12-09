package de.rolandsw.schedulemc.vehicle.network;

import de.rolandsw.schedulemc.vehicle.VehicleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network handler for vehicle-related packets.
 */
public class VehicleNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VehicleMod.MOD_ID, "vehicle"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(VehicleControlPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(VehicleControlPacket::encode)
                .decoder(VehicleControlPacket::decode)
                .consumerMainThread(VehicleControlPacket::handle)
                .add();

        CHANNEL.messageBuilder(VehicleStartEnginePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(VehicleStartEnginePacket::encode)
                .decoder(VehicleStartEnginePacket::decode)
                .consumerMainThread(VehicleStartEnginePacket::handle)
                .add();

        CHANNEL.messageBuilder(VehicleSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(VehicleSyncPacket::encode)
                .decoder(VehicleSyncPacket::decode)
                .consumerMainThread(VehicleSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(VehicleHornPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(VehicleHornPacket::encode)
                .decoder(VehicleHornPacket::decode)
                .consumerMainThread(VehicleHornPacket::handle)
                .add();

        VehicleMod.LOGGER.info("Registered {} vehicle network packets", packetId);
    }

    /**
     * Sends a packet to the server.
     */
    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    /**
     * Sends a packet to a specific player.
     */
    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /**
     * Sends a packet to all players tracking an entity.
     */
    public static void sendToTracking(Object packet, net.minecraft.world.entity.Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }
}
