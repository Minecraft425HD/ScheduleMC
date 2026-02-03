package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netzwerk-Handler fuer das Gang-System.
 */
public class GangNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "gang_network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static final AtomicInteger packetId = new AtomicInteger(0);
    private static int id() { return packetId.getAndIncrement(); }

    public static void register() {
        // Server -> Client: Sync aller Spieler-Gang-Infos (fuer Nametag + TAB)
        INSTANCE.messageBuilder(SyncAllPlayerGangInfoPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAllPlayerGangInfoPacket::decode)
                .encoder(SyncAllPlayerGangInfoPacket::encode)
                .consumerMainThread(SyncAllPlayerGangInfoPacket::handle)
                .add();

        // Server -> Client: Vollstaendige Gang-Daten (fuer Gang-App)
        INSTANCE.messageBuilder(SyncGangDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncGangDataPacket::decode)
                .encoder(SyncGangDataPacket::encode)
                .consumerMainThread(SyncGangDataPacket::handle)
                .add();

        // Client -> Server: Gang-Daten anfordern
        INSTANCE.messageBuilder(RequestGangDataPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestGangDataPacket::decode)
                .encoder(RequestGangDataPacket::encode)
                .consumerMainThread(RequestGangDataPacket::handle)
                .add();

        // Client -> Server: Gang-Aktionen (create, invite, leave, kick, promote, perk)
        INSTANCE.messageBuilder(GangActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(GangActionPacket::decode)
                .encoder(GangActionPacket::encode)
                .consumerMainThread(GangActionPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAll(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
