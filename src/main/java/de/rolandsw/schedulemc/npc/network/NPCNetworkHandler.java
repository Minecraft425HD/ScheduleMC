package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für NPC Packets
 */
public class NPCNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "npc_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(SpawnNPCPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(SpawnNPCPacket::decode)
            .encoder(SpawnNPCPacket::encode)
            .consumerMainThread(SpawnNPCPacket::handle)
            .add();

        INSTANCE.messageBuilder(NPCActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(NPCActionPacket::decode)
            .encoder(NPCActionPacket::encode)
            .consumerMainThread(NPCActionPacket::handle)
            .add();

        INSTANCE.messageBuilder(SyncNPCDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncNPCDataPacket::decode)
            .encoder(SyncNPCDataPacket::encode)
            .consumerMainThread(SyncNPCDataPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
