package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für Gefängnis-Pakete
 */
public class PrisonNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "prison_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(OpenPrisonScreenPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(OpenPrisonScreenPacket::decode)
            .encoder(OpenPrisonScreenPacket::encode)
            .consumerMainThread(OpenPrisonScreenPacket::handle)
            .add();

        INSTANCE.messageBuilder(ClosePrisonScreenPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(ClosePrisonScreenPacket::decode)
            .encoder(ClosePrisonScreenPacket::encode)
            .consumerMainThread(ClosePrisonScreenPacket::handle)
            .add();

        INSTANCE.messageBuilder(PayBailPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PayBailPacket::decode)
            .encoder(PayBailPacket::encode)
            .consumerMainThread(PayBailPacket::handle)
            .add();

        INSTANCE.messageBuilder(UpdatePrisonBalancePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(UpdatePrisonBalancePacket::decode)
            .encoder(UpdatePrisonBalancePacket::encode)
            .consumerMainThread(UpdatePrisonBalancePacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
