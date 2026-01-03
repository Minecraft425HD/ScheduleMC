package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Network Handler für Economy-Packets
 */
public class EconomyNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "economy_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    // SICHERHEIT: AtomicInteger für Thread-safe Packet-ID Inkrement
    private static final AtomicInteger packetId = new AtomicInteger(0);

    private static int id() {
        return packetId.getAndIncrement();
    }
    
    /**
     * Registriert alle Economy-Packets
     */
    public static void register() {
        INSTANCE.messageBuilder(ATMTransactionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(ATMTransactionPacket::decode)
            .encoder(ATMTransactionPacket::encode)
            .consumerMainThread(ATMTransactionPacket::handle)
            .add();
    }
}
