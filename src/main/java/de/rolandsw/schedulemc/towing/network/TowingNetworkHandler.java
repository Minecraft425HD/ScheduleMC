package de.rolandsw.schedulemc.towing.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network handler for towing service packets
 */
public class TowingNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "towing_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // Client -> Server: Change membership tier
        INSTANCE.messageBuilder(ChangeMembershipPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(ChangeMembershipPacket::decode)
            .encoder(ChangeMembershipPacket::encode)
            .consumerMainThread(ChangeMembershipPacket::handle)
            .add();

        // Client -> Server: Request vehicle towing
        INSTANCE.messageBuilder(RequestTowingPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(RequestTowingPacket::decode)
            .encoder(RequestTowingPacket::encode)
            .consumerMainThread(RequestTowingPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
