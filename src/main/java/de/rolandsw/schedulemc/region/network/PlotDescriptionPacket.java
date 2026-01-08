package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Plot-Beschreibung (Client → Server)
 */
public class PlotDescriptionPacket {

    private final String plotId;
    private final String description;

    public PlotDescriptionPacket(String plotId, String description) {
        this.plotId = plotId;
        this.description = description;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(PlotDescriptionPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.plotId);
        buffer.writeUtf(msg.description);
    }

    /**
     * Decode - Liest Daten aus Packet
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static PlotDescriptionPacket decode(FriendlyByteBuf buffer) {
        String plotId = buffer.readUtf(256);
        String description = buffer.readUtf(512);
        return new PlotDescriptionPacket(plotId, description);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlotDescriptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            PlotRegion plot = PlotManager.getPlot(msg.plotId);

            if (plot == null) {
                player.sendSystemMessage(Component.translatable("message.plot.not_found"));
                return;
            }

            // Prüfe ob Spieler Besitzer ist
            if (!plot.getOwnerUUID().equals(player.getUUID().toString())) {
                player.sendSystemMessage(Component.translatable("message.plot.not_owner"));
                return;
            }

            // Validiere Beschreibung
            if (msg.description.length() > 16) {
                player.sendSystemMessage(Component.translatable("message.plot.description_too_long"));
                return;
            }

            plot.setDescription(msg.description.trim());
            PlotManager.savePlots();

            player.sendSystemMessage(Component.translatable("message.plot.description_updated"));
        });
    }
}
