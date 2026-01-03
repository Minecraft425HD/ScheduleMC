package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für Plot-Management-Packets
 */
public class PlotNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "plot_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    /**
     * Registriert alle Plot-Management-Packets
     */
    public static void register() {
        // Verkauf/Vermietung (als Besitzer)
        INSTANCE.messageBuilder(PlotSalePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PlotSalePacket::decode)
            .encoder(PlotSalePacket::encode)
            .consumerMainThread(PlotSalePacket::handle)
            .add();

        // Kauf/Mietung (als Käufer/Mieter)
        INSTANCE.messageBuilder(PlotPurchasePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PlotPurchasePacket::decode)
            .encoder(PlotPurchasePacket::encode)
            .consumerMainThread(PlotPurchasePacket::handle)
            .add();

        // Umbenennen
        INSTANCE.messageBuilder(PlotRenamePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PlotRenamePacket::decode)
            .encoder(PlotRenamePacket::encode)
            .consumerMainThread(PlotRenamePacket::handle)
            .add();

        // Beschreibung setzen
        INSTANCE.messageBuilder(PlotDescriptionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PlotDescriptionPacket::decode)
            .encoder(PlotDescriptionPacket::encode)
            .consumerMainThread(PlotDescriptionPacket::handle)
            .add();

        // Trust-Verwaltung
        INSTANCE.messageBuilder(PlotTrustPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PlotTrustPacket::decode)
            .encoder(PlotTrustPacket::encode)
            .consumerMainThread(PlotTrustPacket::handle)
            .add();

        // Plot aufgeben
        INSTANCE.messageBuilder(PlotAbandonPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PlotAbandonPacket::decode)
            .encoder(PlotAbandonPacket::encode)
            .consumerMainThread(PlotAbandonPacket::handle)
            .add();
    }

    /**
     * Sendet Packet vom Client zum Server
     */
    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
