package de.rolandsw.schedulemc.mission.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für das Missions-System.
 */
public class MissionNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "mission_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // Client → Server: Missionen anfragen
        INSTANCE.messageBuilder(RequestMissionsPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(RequestMissionsPacket::decode)
            .encoder(RequestMissionsPacket::encode)
            .consumerMainThread(RequestMissionsPacket::handle)
            .add();

        // Server → Client: Missionen synchronisieren
        INSTANCE.messageBuilder(SyncMissionsPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncMissionsPacket::decode)
            .encoder(SyncMissionsPacket::encode)
            .consumerMainThread(SyncMissionsPacket::handle)
            .add();

        // Client → Server: Mission-Aktion
        INSTANCE.messageBuilder(MissionActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(MissionActionPacket::decode)
            .encoder(MissionActionPacket::encode)
            .consumerMainThread(MissionActionPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
