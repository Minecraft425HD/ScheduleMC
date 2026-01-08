package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Plot-Rating (Client → Server)
 * Erlaubt Spielern, Plots mit 1-5 Sternen zu bewerten
 */
public class PlotRatingPacket {

    private final String plotId;
    private final int rating; // 1-5 Sterne

    public PlotRatingPacket(String plotId, int rating) {
        this.plotId = plotId;
        this.rating = rating;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(PlotRatingPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.plotId);
        buffer.writeInt(msg.rating);
    }

    /**
     * Decode - Liest Daten aus Packet
     * SICHERHEIT: Max-Länge für plotId gegen DoS/Memory-Angriffe
     */
    public static PlotRatingPacket decode(FriendlyByteBuf buffer) {
        String plotId = buffer.readUtf(256);
        int rating = buffer.readInt();
        return new PlotRatingPacket(plotId, rating);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlotRatingPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            PlotRegion plot = PlotManager.getPlot(msg.plotId);

            if (plot == null) {
                player.sendSystemMessage(Component.translatable("message.plot.not_found"));
                return;
            }

            // Validiere Rating
            if (msg.rating < 1 || msg.rating > 5) {
                player.sendSystemMessage(Component.translatable("message.plot.rating_invalid"));
                return;
            }

            // Spieler darf eigene Plots nicht bewerten
            if (plot.getOwnerUUID().equals(player.getUUID().toString())) {
                player.sendSystemMessage(Component.translatable("message.plot.cannot_rate_own"));
                return;
            }

            // Rating hinzufügen/aktualisieren
            boolean wasUpdated = plot.hasRated(player.getUUID());
            plot.addRating(player.getUUID(), msg.rating);
            PlotManager.savePlots();

            String stars = "★".repeat(msg.rating) + "☆".repeat(5 - msg.rating);
            if (wasUpdated) {
                player.sendSystemMessage(Component.translatable("message.plot.rating_updated", stars));
            } else {
                player.sendSystemMessage(Component.translatable("message.plot.rating_thankyou", stars));
            }
        });
    }
}
