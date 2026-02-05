package de.rolandsw.schedulemc.lock.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler fuer Lock-System Packets.
 */
public class LockNetworkHandler {

    private static final String PROTOCOL_VERSION = "3";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "lock_network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        // Server -> Client: Code-Eingabe GUI oeffnen
        INSTANCE.registerMessage(id++, OpenCodeEntryPacket.class,
                OpenCodeEntryPacket::encode, OpenCodeEntryPacket::new, OpenCodeEntryPacket::handle);

        // Client -> Server: Code wurde eingegeben
        INSTANCE.registerMessage(id++, CodeEntryPacket.class,
                CodeEntryPacket::encode, CodeEntryPacket::new, CodeEntryPacket::handle);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
