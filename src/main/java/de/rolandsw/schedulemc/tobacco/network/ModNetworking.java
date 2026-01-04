package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für Tobacco Packets
 */
public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "tobacco_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(SmallPackageRequestPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(SmallPackageRequestPacket::decode)
            .encoder(SmallPackageRequestPacket::encode)
            .consumerMainThread(SmallPackageRequestPacket::handle)
            .add();

        INSTANCE.messageBuilder(MediumPackageRequestPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(MediumPackageRequestPacket::decode)
            .encoder(MediumPackageRequestPacket::encode)
            .consumerMainThread(MediumPackageRequestPacket::handle)
            .add();

        INSTANCE.messageBuilder(LargePackageRequestPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(LargePackageRequestPacket::decode)
            .encoder(LargePackageRequestPacket::encode)
            .consumerMainThread(LargePackageRequestPacket::handle)
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

        INSTANCE.messageBuilder(PurchaseDecisionSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(PurchaseDecisionSyncPacket::decode)
            .encoder(PurchaseDecisionSyncPacket::encode)
            .consumerMainThread(PurchaseDecisionSyncPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
