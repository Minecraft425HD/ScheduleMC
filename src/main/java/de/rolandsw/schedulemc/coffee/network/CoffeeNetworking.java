package de.rolandsw.schedulemc.coffee.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für Coffee System Packets
 */
public class CoffeeNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "coffee_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(RoasterLevelPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(RoasterLevelPacket::decode)
            .encoder(RoasterLevelPacket::encode)
            .consumerMainThread(RoasterLevelPacket::handle)
            .add();

        INSTANCE.messageBuilder(GrindSizePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(GrindSizePacket::decode)
            .encoder(GrindSizePacket::encode)
            .consumerMainThread(GrindSizePacket::handle)
            .add();

        INSTANCE.messageBuilder(CoffeePackageRequestPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(CoffeePackageRequestPacket::decode)
            .encoder(CoffeePackageRequestPacket::encode)
            .consumerMainThread(CoffeePackageRequestPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
