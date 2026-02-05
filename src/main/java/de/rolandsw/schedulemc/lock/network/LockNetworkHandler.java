package de.rolandsw.schedulemc.lock.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Network Handler fuer Lock-System Packets.
 *
 * Packets:
 * - OpenHackingScreenPacket: Server -> Client (oeffne Hacking-Minigame)
 * - HackingResultPacket:     Client -> Server (Ergebnis des Hacking-Versuchs)
 */
public class LockNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "lock_network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static final AtomicInteger packetId = new AtomicInteger(0);

    private static int id() {
        return packetId.getAndIncrement();
    }

    public static void register() {
        // Server -> Client: Hacking-Screen oeffnen
        INSTANCE.messageBuilder(OpenHackingScreenPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenHackingScreenPacket::decode)
                .encoder(OpenHackingScreenPacket::encode)
                .consumerMainThread(OpenHackingScreenPacket::handle)
                .add();

        // Client -> Server: Hacking-Ergebnis
        INSTANCE.messageBuilder(HackingResultPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(HackingResultPacket::decode)
                .encoder(HackingResultPacket::encode)
                .consumerMainThread(HackingResultPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
