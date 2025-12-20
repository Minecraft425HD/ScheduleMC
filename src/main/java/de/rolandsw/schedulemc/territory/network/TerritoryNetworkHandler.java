package de.rolandsw.schedulemc.territory.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für Territory-Pakete
 */
public class TerritoryNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "territory_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(OpenMapEditorPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(OpenMapEditorPacket::decode)
            .encoder(OpenMapEditorPacket::encode)
            .consumerMainThread(OpenMapEditorPacket::handle)
            .add();

        INSTANCE.messageBuilder(SetTerritoryPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(SetTerritoryPacket::decode)
            .encoder(SetTerritoryPacket::encode)
            .consumerMainThread(SetTerritoryPacket::handle)
            .add();

        INSTANCE.messageBuilder(SyncTerritoriesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncTerritoriesPacket::decode)
            .encoder(SyncTerritoriesPacket::encode)
            .consumerMainThread(SyncTerritoriesPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
