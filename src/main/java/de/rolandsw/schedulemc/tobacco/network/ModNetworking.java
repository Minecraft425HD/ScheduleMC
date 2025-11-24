package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für Tobacco Packets
 */
public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "tobacco_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(PackageRequestPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PackageRequestPacket::decode)
            .encoder(PackageRequestPacket::encode)
            .consumerMainThread(PackageRequestPacket::handle)
            .add();

        INSTANCE.messageBuilder(NegotiationPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(NegotiationPacket::decode)
            .encoder(NegotiationPacket::encode)
            .consumerMainThread(NegotiationPacket::handle)
            .add();

        INSTANCE.messageBuilder(OpenTobaccoNegotiationPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(OpenTobaccoNegotiationPacket::decode)
            .encoder(OpenTobaccoNegotiationPacket::encode)
            .consumerMainThread(OpenTobaccoNegotiationPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
